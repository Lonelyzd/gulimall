package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : z_dd
 * @date : 2024/5/13 21:37
 **/
@FeignClient(name = "gulimall-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable Long skuId);
}
