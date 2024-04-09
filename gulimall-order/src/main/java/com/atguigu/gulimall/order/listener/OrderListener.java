package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author : z_dd
 * @date : 2024/3/24 20:45
 **/
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderListener {

    @Autowired
    private OrderService orderService;

    /**
     * 定时关单
     *
     * @param entity:
     * @param channel:
     * @param message:
     * @return void
     * @author z_dd
     * @date 2024/3/24 20:48
     **/
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {

        try {
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }


    }


}
