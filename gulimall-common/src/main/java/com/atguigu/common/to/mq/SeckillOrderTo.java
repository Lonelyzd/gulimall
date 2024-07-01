package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 秒杀的快速订单
 *
 * @author : z_dd
 * @date : 2024/6/6 21:26
 **/
@Data
public class SeckillOrderTo {

    private String orderSn;


    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer num;

    private Long memberId;
}
