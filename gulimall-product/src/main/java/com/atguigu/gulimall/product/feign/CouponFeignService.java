package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: z_dd
 * @date: 2023/3/26 15:46
 * @Description: 远程调用优惠券服务
 */
@FeignClient(name = "gulimall-coupon")
public interface CouponFeignService {
    /**
     * 保存优惠信息
     *
     * @param to:
     * @Author: z_dd
     * @Date: 2023/3/26 16:05
     * @return: com.atguigu.common.utils.R
     **/
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo to);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
