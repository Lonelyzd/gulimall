package com.atguigu.gulimall.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rabbit 配置
 *
 * @author : z_dd
 * @date : 2024/1/30 21:37
 **/
@Slf4j
@Configuration
public class MyRabbitConfig {

    /**
     * 使用json方式序列化消息
     *
     * @return MessageConverter
     * @author z_dd
     * @date 2024/1/30 21:39
     **/
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
