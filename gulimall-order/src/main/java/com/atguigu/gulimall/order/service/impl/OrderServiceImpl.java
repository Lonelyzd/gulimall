package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页需要的数据
     *
     * @return OrderConfirmVo
     * @author z_dd
     * @date 2024/2/16 20:39
     **/
    @SneakyThrows
    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo vo = new OrderConfirmVo();

        final MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //远程查询所有收货地址
        final CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            final List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            vo.setAddress(address);
        }, threadPoolExecutor);


        final CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            //远程查询购物车中所选中项目
            RequestContextHolder.setRequestAttributes(requestAttributes);
            final List<OrderItemVo> currentUserItems = cartFeignService.getCurrentUserItems();
            vo.setItems(currentUserItems);
        }, threadPoolExecutor).thenRunAsync(() -> {
            //远程查询购物车中商品库存
            final List<OrderItemVo> items = vo.getItems();
            final List<Long> collect = items.stream()
                    .map(OrderItemVo::getSkuId)
                    .collect(Collectors.toList());

            final R hasStock = wmsFeignService.getSkusHasStock(collect);
            final List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                final Map<Long, Boolean> stockMap = data.stream()
                        .collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                vo.setStocks(stockMap);
            }

        }, threadPoolExecutor);


        //用户积分
        vo.setIntegration(memberResponseVo.getIntegration());

        //zdd TODO 2024/2/16 21:45 : 防重令牌
        final String token = UUID.randomUUID().toString().replace("-", "");
        vo.setOrderToken(token);

        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);


        CompletableFuture
                .allOf(addressFuture, orderItemFuture)
                .get();


        return vo;
    }

    //    @GlobalTransactional
    @Override
    @Transactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        final MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        confirmVoThreadLocal.set(vo);
        //1.验证令牌(令牌的对比和删除必须保证原子性)
        final String orderToken = vo.getOrderToken();


        //Lua脚本返回结果：0失败；1成功
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        final Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);


        if (Objects.equals(0L, execute)) {
            //验证不通过
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证通过
            //下单：创建订单，验价，锁库存
            //1.创建订单，订单项目
            final OrderCreateTo order = createOrder();
            //2.验价
            final BigDecimal payAmount = order.getOrder().getPayAmount();
            final BigDecimal payPrice = vo.getPayPrice();

            //金额对比
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //3.保存订单
                saveOrder(order);
                //4.锁库存，只要有异常就回滚
                final WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                final List<OrderItemVo> collect = order.getOrderItems().stream()
                        .map(item -> {
                            final OrderItemVo orderItemVo = new OrderItemVo();
                            orderItemVo.setSkuId(item.getSkuId());
                            orderItemVo.setCount(item.getSkuQuantity());
                            orderItemVo.setTitle(item.getSkuName());
                            return orderItemVo;
                        })
                        .collect(Collectors.toList());

                lockVo.setLocks(collect);
                final R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode().equals(0)) {
                    //锁定成功
                    responseVo.setOrder(order.getOrder());
                    //zdd TODO 2024/3/24 20:56 : 订单创建成功，发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange",
                            "order.create.order",
                            order.getOrder());
                    responseVo.setCode(0);
                    return responseVo;
                } else {
                    //锁定失败
                    final String s = (String) r.get("msg");
                    throw new NoStockException(s);
                }
            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return
                this.getOne(Wrappers.<OrderEntity>lambdaQuery().eq(OrderEntity::getOrderSn, orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前这个订单的最新状态
        final OrderEntity orderEntity = this.getById(entity.getId());

        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            //关单
            OrderEntity order = new OrderEntity();
            order.setId(entity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());

            this.updateById(order);

            //给MQ发送一个关单消息，立即解锁库存
            final OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(entity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);

        }
    }

    /**
     * 获取订单的支付信息
     *
     * @param orderSn :
     * @return PayVo
     * @author z_dd
     * @date 2024/4/2 20:58
     **/
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo vo = new PayVo();

        final OrderEntity order = this.getOrderByOrderSn(orderSn);
        final List<OrderItemEntity> list = orderItemService.list(Wrappers.<OrderItemEntity>lambdaQuery().eq(OrderItemEntity::getOrderSn, orderSn));

        final BigDecimal bigDecimal = order.getTotalAmount().setScale(2, BigDecimal.ROUND_UP);


        vo.setTotal_amount(bigDecimal.toString());
        vo.setOut_trade_no(orderSn);
        final OrderItemEntity orderItemEntity = list.get(0);
        vo.setSubject(orderItemEntity.getSkuName());
        vo.setBody(orderItemEntity.getSkuAttrsVals());

        return vo;
    }

    private void saveOrder(OrderCreateTo order) {
        final OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        final List<OrderItemEntity> orderItems = order.getOrderItems();

        orderItemService.saveBatch(orderItems);
    }

    //创建订单
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();

        //1.订单信息

        //1.1 生成订单号
        final String orderSn = IdWorker.getTimeId();

        //1.2 构建订单
        final OrderEntity orderEntity = buildOrder(orderSn);
        createTo.setOrder(orderEntity);

        //2. 获取到所有订单项
        final List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        createTo.setOrderItems(orderItemEntities);

        //3. 计算价格相关
        computePrice(orderEntity, orderItemEntities);


        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer giftTotal = 0, growthTotal = 0;
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            final BigDecimal realAmount = orderItemEntity.getRealAmount();
            total = total.add(realAmount);
            final BigDecimal couponAmount = orderItemEntity.getCouponAmount();
            coupon = coupon.add(couponAmount);
            final BigDecimal integrationAmount = orderItemEntity.getIntegrationAmount();
            integration = integration.add(integrationAmount);
            final BigDecimal promotionAmount = orderItemEntity.getPromotionAmount();
            promotion = promotion.add(promotionAmount);
            giftTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }
        //1.订单价格相关
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);

        //设置积分
        orderEntity.setGrowth(growthTotal);
        orderEntity.setIntegration(giftTotal);

        orderEntity.setStatus(0);

    }

    /**
     * 构建订单
     *
     * @param orderSn:
     * @return OrderEntity
     * @author z_dd
     * @date 2024/2/25 16:46
     **/
    private OrderEntity buildOrder(String orderSn) {
        final MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberResponseVo.getId());


        final OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        final R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        final FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });

        //获取收货地址运费信息
        entity.setFreightAmount(fareResp.getFare());
        //设置收费人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);
        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn:
     * @return List<OrderItemEntity>
     * @author z_dd
     * @date 2024/2/25 16:57
     **/
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //2.获取所有订单项
        final List<OrderItemVo> currentUserItems = cartFeignService.getCurrentUserItems();
        if (!CollectionUtils.isEmpty(currentUserItems)) {
            final List<OrderItemEntity> collect = currentUserItems.stream()
                    .map(item -> {
                        final OrderItemEntity itemEntity = buildOrderItem(item);
                        itemEntity.setOrderSn(orderSn);
                        return itemEntity;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建单个订单项
     *
     * @return List<OrderItemEntity>
     * @author z_dd
     * @date 2024/2/25 16:51
     **/
    private OrderItemEntity buildOrderItem(OrderItemVo orderItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1.订单信息：订单号

        //2.商品的SPU信息
        final Long skuId = orderItem.getSkuId();
        final R r = productFeignService.getSpuInfoBySkuId(skuId);
        final SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());

        //3.商品的SKU信息
        itemEntity.setSkuId(orderItem.getSkuId());
        itemEntity.setSkuName(orderItem.getTitle());
        itemEntity.setSkuPic(orderItem.getImage());
        itemEntity.setSkuPrice(orderItem.getPrice());

        final String skuAttr = StringUtils.collectionToDelimitedString(orderItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(orderItem.getCount());

        //4.优惠信息【不做】

        //5.积分信息
        itemEntity.setGiftGrowth(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount().toString())).intValue());

        //6.订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        final BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        final BigDecimal subtract = orign.subtract(itemEntity.getPromotionAmount()).subtract(itemEntity.getCouponAmount()).subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }
}