package com.atguigu.gulimall.coupon;

import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.listener.RechargeChangeEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallCouponApplicationTests {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    public void contextLoads() throws InterruptedException {
        // 发布事件通知
        applicationEventPublisher.publishEvent(new RechargeChangeEvent(this,new MemberPriceEntity()));

        Thread.sleep(50000);
    }

}
