package com.atguigu.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单确认页需要用的数据
 *
 * @author : z_dd
 * @date : 2024/2/16 20:23
 **/
public class OrderConfirmVo {

    //收货地址 ums_member_receive_address表
    @Getter
    @Setter
    private List<MemberAddressVo> address;

    //所有选中的购物项
    @Getter
    @Setter
    private List<OrderItemVo> items;

    //发票信息
    //...

    //优惠券信息
    @Getter
    @Setter
    private Integer integration;

    //订单总额
    @Setter
    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                final BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }

        return sum;
    }

    //应付价格
    @Setter
    private BigDecimal payPrice;

    //应付价格
    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                final BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    //防重令牌
    @Setter
    @Getter
    private String orderToken;

    //商品数量
    public Integer getCount() {
        return Optional.ofNullable(items)
                .map(List::size)
                .orElse(0);
    }

    //库存
    @Setter
    @Getter
    private Map<Long, Boolean> stocks;

}
