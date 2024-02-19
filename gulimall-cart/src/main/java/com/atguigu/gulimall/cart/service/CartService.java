package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;

/**
 * @author : z_dd
 * @date : 2024/1/1 20:41
 **/
public interface CartService {
    CartItem addToCart(Long skuId, Integer num);

    CartItem getCartItem(Long skuId);

    Cart getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物车商品数量
     *
     * @param skuId:
     * @param num:
     * @return void
     * @author z_dd
     * @date 2024/1/25 20:43
     **/
    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
