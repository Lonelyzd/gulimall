package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : z_dd
 * @date : 2024/4/27 17:32
 **/
@FeignClient("gulimall-coupon")
public interface CouponFeignService {


    @GetMapping("/coupon/seckillsession/lates3DaySession")
    R getLates3DaySession();

}
