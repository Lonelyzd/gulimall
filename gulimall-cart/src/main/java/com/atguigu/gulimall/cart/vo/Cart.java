package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车实体类
 *
 * @author : z_dd
 * @date : 2023/12/30 21:45
 **/
@Data
public class Cart {

    private List<CartItem> items;

    private Integer countNum;   //商品数据

    private Integer countType;  //商品种类数量

    private BigDecimal totalAmount; //商品总价

    private BigDecimal reduce = new BigDecimal("0.00");      //优惠金额

    public Integer getCountNum() {
        int countNum = 0;
        if (!CollectionUtils.isEmpty(this.items)) {
            for (CartItem item : this.items) {
                countNum += item.getCount();
            }
        }
        return countNum;
    }


    public Integer getCountType() {
        if (!CollectionUtils.isEmpty(this.items)) {
            return this.items.size();
        }
        return null;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");

        //计算购物项总价
        if (!CollectionUtils.isEmpty(this.items)) {
            for (CartItem item : this.items) {
                if(item.getCheck()){
                    //只计算选中商品
                    final BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }

        //减去优惠总价
        final BigDecimal subtract = amount.subtract(getReduce());
        return subtract;
    }


}
