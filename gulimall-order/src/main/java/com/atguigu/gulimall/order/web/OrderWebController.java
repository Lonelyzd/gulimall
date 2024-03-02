package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
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


    /**
     * 提交订单
     *
     * @param vo:
     * @return String
     * @author z_dd
     * @date 2024/2/19 21:32
     **/
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        if (responseVo.getCode().equals(0)) {
            //下单成功，来到支付页

            return "pay";
        }else {
            //下单失败重新回到订单确认页确认订单信息
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

}
