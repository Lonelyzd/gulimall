package com.atguigu.gulimall.order.controller;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @author : z_dd
 * @date : 2024/1/31 20:41
 **/
@RestController
@Slf4j
public class RebbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/sendMq")
    public String sendMq(@RequestParam(defaultValue = "10") Integer num) {

        for (int i = 0; i < num; i++) {

            if (i % 2 == 0) {
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setName("哈哈" + i);
                entity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity,new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(String.valueOf(i));
                entity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", entity);
            }

            log.info("消息发送完成");
        }


        return "ok";
    }
}
