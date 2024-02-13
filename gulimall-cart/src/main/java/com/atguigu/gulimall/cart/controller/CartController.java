package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author : z_dd
 * @date : 2024/1/1 20:36
 **/
@Controller
public class CartController {

    @Autowired
    private CartService cartService;


    @RequestMapping("/cart.html")
    public String cartListPage(Model model) {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     *
     * @return String
     * @author z_dd
     * @date 2024/1/3 21:40
     **/
    @RequestMapping("/addToCart")
    public String addToCart(@RequestParam Long skuId,
                            @RequestParam Integer num,
                            RedirectAttributes redirectAttributes) {

        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccessPage.html";

    }

    @RequestMapping("/addToCartSuccessPage.html")
    public ModelAndView addToCartSuccessPage(@RequestParam Long skuId) {
        CartItem cartItem = cartService.getCartItem(skuId);


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("item", cartItem);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam Long skuId, @RequestParam Integer check) {
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam Long skuId, @RequestParam Integer num){
        cartService.countItem(skuId,num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam Long skuId){
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
