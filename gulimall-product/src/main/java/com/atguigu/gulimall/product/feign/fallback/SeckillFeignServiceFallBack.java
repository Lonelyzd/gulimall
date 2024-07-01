package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : z_dd
 * @date : 2024/7/8 21:28
 **/
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔断方法执行..........................");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}
