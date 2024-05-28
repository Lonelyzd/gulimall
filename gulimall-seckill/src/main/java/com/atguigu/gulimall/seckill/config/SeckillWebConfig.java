package com.atguigu.gulimall.seckill.config;

import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : z_dd
 * @date : 2024/2/14 20:06
 **/
@Configuration
public class SeckillWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginUserInterceptor())
                .addPathPatterns("/**");

    }
}
