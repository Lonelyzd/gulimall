package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@Slf4j
@RabbitListener(queues = "hello-java-queue")
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );
        return new PageUtils(page);
    }


    @RabbitHandler
    public void recieveMessage(Message message, OrderReturnReasonEntity entity, Channel channel) {
        log.info("接收到消息1===>{}", entity.getName());

        //deliveryTag : 消息确认标识，channel内按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            //确认消息接收
            //两个参数：消息确认标识 ,是否批量确认
            channel.basicAck(deliveryTag,false);

            //拒绝签收
            //三个参数：消息确认标识 ,是否批量确认消息是否重回Queue
            channel.basicNack(deliveryTag,false,true);

            //拒绝签收
            //两个参数：消息确认标识 ,是否批量确认
            channel.basicReject(deliveryTag,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler
    public void recieveMessage(OrderEntity entity) {
        log.info("接收到消息2===>{}", entity.getOrderSn());
    }
}