package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author : z_dd
 * @date : 2024/1/1 20:43
 **/
@Service
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX = "gulimall:cart:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    @SneakyThrows
    @Override
    public CartItem addToCart(Long skuId, Integer num) {

        final BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();


        final String res = (String) cartOperations.get(skuId.toString());

        if (Objects.isNull(res)) {
            CartItem cartItem = new CartItem();
            final CompletableFuture<Void> skuFuture = CompletableFuture.runAsync(() -> {
                final R rspInfo = productFeignService.info(skuId);
                final SkuInfoVo skuInfo = rspInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setCount(num);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setCheck(true);
            }, threadPoolExecutor);

            final CompletableFuture<Void> skuSaleAttrValueFuture = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, threadPoolExecutor);

            CompletableFuture.allOf(skuFuture, skuSaleAttrValueFuture).get();

            final String jsonString = JSON.toJSONString(cartItem);
            cartOperations.put(skuId.toString(), jsonString);
            return cartItem;
        } else {
            final CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            final String jsonString = JSON.toJSONString(cartItem);
            cartOperations.put(skuId.toString(), jsonString);
            return cartItem;
        }


    }

    @Override
    public CartItem getCartItem(Long skuId) {
        final BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();

        final String res = (String) cartOperations.get(skuId.toString());

        final CartItem cartItem = JSON.parseObject(res, CartItem.class);

        return cartItem;
    }

    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        final UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (userInfoTo.getUserId() != null) {
            //已登录
            final List<CartItem> cartItemTempList = getCartItem(CART_PREFIX + userInfoTo.getUserKey());
            if (!CollectionUtils.isEmpty(cartItemTempList)) {
                for (CartItem cartItem : cartItemTempList) {
                    this.addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                this.clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            final List<CartItem> cartItemList = getCartItem(CART_PREFIX + userInfoTo.getUserId());
            cart.setItems(cartItemList);

        } else {
            //未登录
            final List<CartItem> cartItem = getCartItem(CART_PREFIX + userInfoTo.getUserKey());
            cart.setItems(cartItem);
        }
        return cart;
    }

    private List<CartItem> getCartItem(String cartKey) {
        final BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.boundHashOps(cartKey);

        final List<Object> values = hashOperations.values();
        if (!CollectionUtils.isEmpty(values)) {
            final List<CartItem> collect = values.stream()
                    .map((obj) -> {
                        String str = (String) obj;
                        return JSON.parseObject(str, CartItem.class);
                    })
                    .collect(Collectors.toList());

            return collect;
        }
        return null;
    }

    /**
     * 获取购物车操作
     *
     * @return BoundHashOperations<String, Object, Object>
     * @author z_dd
     * @date 2024/1/23 21:35
     **/
    private BoundHashOperations<String, Object, Object> getCartOperations() {
        final UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = Optional
                .ofNullable(userInfoTo.getUserId())
                .map(temp -> CART_PREFIX + temp)
                .orElse(CART_PREFIX + userInfoTo.getUserKey());

        final BoundHashOperations<String, Object, Object> hashOperations = stringRedisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }

    /**
     * 清空购物车
     *
     * @param cartKey:
     * @return void
     * @author z_dd
     * @date 2024/1/23 21:36
     **/
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        final CartItem cartItem = this.getCartItem(skuId);
        cartItem.setCheck(check.equals(1));

        final String jsonString = JSON.toJSONString(cartItem);
        getCartOperations().put(skuId.toString(), jsonString);
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        final CartItem cartItem = this.getCartItem(skuId);
        cartItem.setCount(num);
        final String jsonString = JSON.toJSONString(cartItem);
        getCartOperations().put(skuId.toString(), jsonString);
    }

    @Override
    public void deleteItem(Long skuId) {
        final BoundHashOperations<String, Object, Object> cartOperations = getCartOperations();
        cartOperations.delete(skuId.toString());
    }
}
