package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author : z_dd
 * @date : 2024/2/14 20:02
 **/
@Controller
public class OrderWebController {


    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo data = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", data);
        return "confirm";
    }


    /** 提交订单
     * @author z_dd
     * @date 2024/2/19 21:32
     * @param vo: 
     * @return String
     **/
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo){


        return null;
    }

}
