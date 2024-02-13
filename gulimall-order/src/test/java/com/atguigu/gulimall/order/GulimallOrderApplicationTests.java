package com.atguigu.gulimall.order;

import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //创建交换机
    @Test
    public void createExchange() {
        final DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    //创建队列
    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Exchange[{}]创建成功", "hello-java-queue");
    }

    //创建绑定
    @Test
    public void createBinding() {
        /**
         * String destination, 目的地
         * DestinationType destinationType, 目的地类型
         * String exchange, 交换机
         * String routingKey, 路由键
         * Map<String, Object> arguments 自定义参数
         * 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为路由键
         **/
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);

        amqpAdmin.declareBinding(binding);
        log.info("Exchange[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void sendMessageTest(){
        String msg="hello world!";

        OrderReturnReasonEntity entity=new OrderReturnReasonEntity();
        entity.setId(1L);
        entity.setName("haha");
        entity.setCreateTime(new Date());
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",entity);
        log.info("消息发送完成");
    }
}
