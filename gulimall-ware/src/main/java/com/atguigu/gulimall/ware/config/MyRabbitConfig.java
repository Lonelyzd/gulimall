package com.atguigu.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : z_dd
 * @date : 2024/3/19 21:24
 **/
@Slf4j
@Configuration
public class MyRabbitConfig {

//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message) {
//
//    }

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

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null
        );
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }
}
