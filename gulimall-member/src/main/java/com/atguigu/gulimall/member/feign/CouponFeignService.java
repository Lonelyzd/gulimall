package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/** 远程调用接口
 * @author: z_dd
 * @date: 2023/1/29 12:26
 * @Description:
 */
@FeignClient("gulimall-coupon") //告诉spring cloud这个接口是一个远程客户端，要调用coupon服务，再去调用coupon服务/coupon/coupon/member/list对应的方法
@RequestMapping("/coupon/coupon")
public interface CouponFeignService {
    @RequestMapping("/member/list")
    R memberCoupons();//得到一个R对象
}
