package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : z_dd
 * @date : 2024/2/19 21:29
 **/
@Data
public class OrderSubmitVo {
    //地址ID
    private Long addrId;

    //支付方式
    private Integer payType;

    //防重令牌
    private String orderToken;

    //应付价格 验价
    private BigDecimal payPrice;

    //订单备注
    private String note;
}
