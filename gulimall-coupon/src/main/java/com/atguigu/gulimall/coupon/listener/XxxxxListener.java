package com.atguigu.gulimall.coupon.listener;

import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author: z_dd
 * @date: 2023/3/13 22:09
 * @Description:
 */

@Component
public class XxxxxListener {
    @Async
    @Order(-1)
    @EventListener
    public void doXxxxx(RechargeChangeEvent event) {
        System.out.println("---------------doXxxxx----------------" + event.hashCode());
        final MemberPriceEntity memberPriceEntity = event.getMemberPriceEntity();
        System.out.println("处理Xxxxx业务");
        System.out.println("Xxxxx 成功");
    }

    @Async
    @Order(-2)
    @EventListener
    public void doYyyy(RechargeChangeEvent event) throws InterruptedException {
        System.out.println("---------------doYyyy----------------" + event.hashCode());

        Thread.sleep(3000);
        final MemberPriceEntity memberPriceEntity = event.getMemberPriceEntity();
        System.out.println("处理doYyyy业务");
        System.out.println("doYyyy 成功");
    }
}
