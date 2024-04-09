package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : z_dd
 * @date : 2024/3/20 22:00
 **/
@FeignClient("gulimall-order")
public interface OrderFeignSerice {

    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable String orderSn);
}
