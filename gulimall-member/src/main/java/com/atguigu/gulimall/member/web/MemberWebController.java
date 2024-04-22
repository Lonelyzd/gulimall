package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : z_dd
 * @date : 2024/4/8 21:10
 **/
@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  Model model) {

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("page", pageNum.toString());

        final R r = orderFeignService.listWithItem(paramMap);

        System.out.println(JSON.toJSONString(r));

        model.addAttribute("orders",r);

        //查出当前登录用户的所有订单列表
        return "orderList";
    }
}
