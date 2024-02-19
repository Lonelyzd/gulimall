package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : z_dd
 * @date : 2024/2/16 20:31
 **/
@Data
public class OrderItemVo {

    private Long skuId;


    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    private boolean hasStock;
}
