package com.atguigu.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Rabbit 配置
 *
 * @author : z_dd
 * @date : 2024/1/30 21:37
 **/
@Slf4j
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    @PostConstruct
    public void initRabbitTemplate() {
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData:  当前消息的唯一关联数据(这个是消息的唯一ID)
             * @param ack:  消息是否成功收到
             * @param cause:    失败的原因
             **/
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("confirm...correlationData[{}]===>ack[{}]===>cause[{}]", correlationData, ack, cause);
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**消息没有投递到指定队列，就会触发这个消息回调
             * @param message: 投递失败的消息
             * @param replyCode: 回复的状态码
             * @param replyText: 回复的文本内容
             * @param exchange: 消息发送的目标交换机
             * @param routingKey: 消息发送的路由键
             * @return void
             **/
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("Fail Message[{}]===>replyCode[{}]===>replyText[{}]===>exchange[{}]===>routingKey[{}]", message, replyCode, replyText, exchange, routingKey);
            }
        });
    }
}
