package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import com.atguigu.gulimall.order.vo.SkuStockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

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

}