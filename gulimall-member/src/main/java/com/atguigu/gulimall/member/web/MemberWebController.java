package com.atguigu.gulimall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : z_dd
 * @date : 2024/4/8 21:10
 **/
@Controller
public class MemberWebController {

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(){
        //查出当前登录用户的所有订单列表
        return "orderList";
    }
}
