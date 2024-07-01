package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:04:23
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页需要的数据
     *
     * @return OrderConfirmVo
     * @author z_dd
     * @date 2024/2/16 20:39
     **/
    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    /**
     * 获取订单的支付信息
     *
     * @param orderSn:
     * @return PayVo
     * @author z_dd
     * @date 2024/4/2 21:26
     **/
    PayVo getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * 处理支付宝的支付结果
     *
     * @param vo:
     * @return String
     * @author z_dd
     * @date 2024/4/17 20:49
     **/
    String handleAliResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

