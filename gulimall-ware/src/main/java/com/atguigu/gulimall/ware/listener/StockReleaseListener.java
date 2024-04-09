package com.atguigu.gulimall.ware.listener;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author : z_dd
 * @date : 2024/3/21 21:08
 **/
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 监听MQ，自动解锁库存
     * 只要下单失败，解锁库存
     *
     * @param to:
     * @param message:
     * @return void
     * @author z_dd
     * @date 2024/3/20 21:50
     **/
    @RabbitHandler
    public void handleStockLockedRelease(StockLockTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到自动解锁库存消息...");

        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 超时关单同时解锁库存
     *
     * @param to:
     * @param message:
     * @return void
     * @author z_dd
     * @date 2024/3/24 21:30
     **/
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        System.out.println("订单到期关闭，准备解锁库存...");
        try {
            wareSkuService.unlockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
