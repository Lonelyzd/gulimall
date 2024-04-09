package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author : z_dd
 * @date : 2024/4/2 20:49
 **/
@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @ResponseBody
    @GetMapping(value = "/payOrder",produces="text/html")
    public String payOrder(@RequestParam String orderSn) throws AlipayApiException {

        final PayVo payVo = orderService.getOrderPay(orderSn);

        final String pay = alipayTemplate.pay(payVo);
        return pay;
    }

}
