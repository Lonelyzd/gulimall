package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 秒杀订单监听
 * @author : z_dd
 * @date : 2024/6/6 21:47
 **/
@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckikkListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {

        log.info("准备创建秒杀单的详细详细");
        try {
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }


    }
}
