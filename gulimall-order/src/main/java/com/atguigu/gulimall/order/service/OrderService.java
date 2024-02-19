package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.vo.OrderConfirmVo;
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
}

