package com.atguigu.gulimall.coupon.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

/**
 * @author : z_dd
 * @date : 2024/6/30 21:14
 **/
@Configuration
public class CouponSentinelConfig {

    public CouponSentinelConfig() {
        WebCallbackManager.setUrlBlockHandler((httpServletRequest, httpServletResponse, e) -> {
            final R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(JSON.toJSONString(error));
        });
    }
}
