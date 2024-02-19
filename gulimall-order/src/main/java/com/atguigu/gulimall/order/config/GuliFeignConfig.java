package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : z_dd
 * @date : 2024/2/17 19:37
 **/

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            final HttpServletRequest request;
            if (requestAttributes != null) {
                request = requestAttributes.getRequest();
                //同步请求信息
                final String cookie = request.getHeader("Cookie");
                requestTemplate.header("Cookie", cookie);
            }

        };
    }
}
