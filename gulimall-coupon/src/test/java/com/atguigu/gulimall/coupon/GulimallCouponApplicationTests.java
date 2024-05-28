package com.atguigu.gulimall.coupon;

import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallCouponApplicationTests {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private SeckillSessionService seckillSessionService;

    @Test
    public void contextLoads() throws InterruptedException {
      /*  // 发布事件通知
        applicationEventPublisher.publishEvent(new RechargeChangeEvent(this,new MemberPriceEntity()));

        Thread.sleep(50000);*/

    }

}
