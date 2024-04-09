package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignSerice;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignSerice orderFeignSerice;


    /**
     * 解锁库存
     *
     * @param skuId:
     * @param wareId:
     * @param num:
     * @param taskDetailId:
     * @return void
     * @author z_dd
     * @date 2024/3/20 23:20
     **/
    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //库存解锁
        this.baseMapper.unLockStock(skuId, wareId, num, taskDetailId);

        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        //修改库存工作单状态
        entity.setId(taskDetailId);
        entity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(entity);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String skuId = (String) params.get("skuId");
        final String wareId = (String) params.get("wareId");

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(skuId), WareSkuEntity::getSkuId, skuId)
                        .eq(StringUtils.isNotBlank(wareId), WareSkuEntity::getWareId, wareId)
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity wareSku = this.getOne(Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(WareSkuEntity::getSkuId, skuId)
                        .eq(WareSkuEntity::getWareId, wareId),
                false
        );
        if (wareSku == null) {
            wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(0);
            wareSku.setStockLocked(0);
            //远程查询SKU名称，如果失败，整个事务无需回滚
            try {
                final R info = productFeignService.info(skuId);
                final Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSku.setSkuName((String) skuInfo.get("skuName"));
            } catch (Exception e) {

            }
        }
        wareSku.setStock(wareSku.getStock() + skuNum);

        this.saveOrUpdate(wareSku);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        final List<SkuHasStockVo> collect = skuIds
                .stream()
                .map(skuId -> {
                    SkuHasStockVo vo = new SkuHasStockVo();
                    Long count = this.baseMapper.getSkuStock(skuId);
                    vo.setSkuId(skuId);
                    vo.setHasStock(count != null && count > 0);
                    return vo;
                })
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param vo :
     * @return List<LockStockResult>
     * @author z_dd
     * @date 2024/3/2 21:35
     **/
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /**
         * 保存库存工作单的详情，以便追溯
         **/

        final WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        //1.找到每个商品在那个仓库有库存
        final List<OrderItemVo> locks = vo.getLocks();

        final List<SkuWareHasStock> collect = locks.stream()
                .map(item -> {
                    SkuWareHasStock stock = new SkuWareHasStock();
                    final Long skuId = item.getSkuId();
                    stock.setSkuId(skuId);
                    stock.setNum(item.getCount());
                    //查询这个商品在哪有库存
                    List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
                    stock.setWareId(wareIds);

                    return stock;
                })
                .collect(Collectors.toList());


        //2.锁定库存
        Boolean allLock = true;
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            final List<Long> wareIds = stock.getWareId();
            final Long skuId = stock.getSkuId();
            for (Long wareId : wareIds) {
                if (CollectionUtils.isEmpty(wareIds)) {
                    //没有任何仓库有这个商品的库存
                    throw new NoStockException(skuId);
                } else {
                    //成功返回1，否则返回0
                    Integer count = this.baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                    if (count.equals(1)) {
                        //锁定成功，直接结束本轮
                        skuStocked = true;
                        //TODO 告诉MQ库存锁定成
                        final WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", stock.getNum(), wareOrderTaskEntity.getId(), wareId, 1);
                        wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                        final StockLockTo stockLockTo = new StockLockTo();
                        stockLockTo.setId(wareOrderTaskEntity.getId());

                        final StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTo);
                        stockLockTo.setDetail(stockDetailTo);

                        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockTo);
                        break;
                    } else {
                        //当前仓库锁定失败，重试下一个仓库
                    }
                }
            }
            if (!skuStocked) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }

        //3.全部锁定成
        return true;
    }

    /**
     * 解锁库存方法
     *
     * @param to:
     * @return void
     * @author z_dd
     * @date 2024/3/21 21:12
     **/
    @Override
    public void unlockStock(StockLockTo to) {
        final StockDetailTo detail = to.getDetail();

        //查询数据库关于这个订单的锁定库存信息
        //存在：证明库存锁定成功了

        final WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detail.getId());
        if (byId != null) {
            //工作单详情存在，就解锁
            final WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(to.getId());
            final String orderSn = taskEntity.getOrderSn();
            final R r = orderFeignSerice.getOrderStatus(orderSn);

            if (r.getCode() == 0) {
                //订单数据返回成功
                final OrderVo data = r.getData(new TypeReference<OrderVo>() {
                });


                if (data == null || data.getStatus() == 4) {
                    //订单不存在 或 订单已经被取消，才能解锁库存

                    if (byId.getLockStatus() == 1) {
                        //当前库存工作单详情状态是1，已锁定
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
                        //通知RabbitMQ解锁成功
//                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    }
                }
            } else {
                //解锁失败
                //通知RabbitMQ拒绝消息，并将消息重新放回队列，等待下次消费
//                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                throw new RuntimeException("远程服务调用失败");
            }
        } else {
            //不存在，说明已经回滚，无需解锁
            //通知RabbitMQ解锁成功
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    //防止订单服务卡顿，导致库存消息优先到期，此时订单状态还没有改变，解锁库存方法直接空执行，然后库存永远无法解锁
    @Override
    @Transactional
    public void unlockStock(OrderTo to) {
        final String orderSn = to.getOrderSn();
        //查一下库存解锁状态，防止重复解锁库存
        final WareOrderTaskEntity taskEntity = wareOrderTaskService.getOne(Wrappers.<WareOrderTaskEntity>lambdaQuery().eq(WareOrderTaskEntity::getOrderSn, orderSn));
        //按照库存工作单ID找到所有没有解锁的库存详情，并进行解锁

        final List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(Wrappers.<WareOrderTaskDetailEntity>lambdaQuery()
                .eq(WareOrderTaskDetailEntity::getTaskId, taskEntity.getId())
                .eq(WareOrderTaskDetailEntity::getLockStatus, 1));

        for (WareOrderTaskDetailEntity wareOrderTaskDetailEntity : list) {
            unLockStock(wareOrderTaskDetailEntity.getSkuId(), wareOrderTaskDetailEntity.getWareId(), wareOrderTaskDetailEntity.getSkuNum(), wareOrderTaskDetailEntity.getId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;

        private Integer num;

        private List<Long> wareId;
    }
}