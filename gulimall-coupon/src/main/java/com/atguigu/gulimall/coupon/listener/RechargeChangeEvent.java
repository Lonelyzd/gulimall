package com.atguigu.gulimall.coupon.listener;

import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * @Author JCccc
 * @Description
 * @Date 2020/10/12 9:08
 */
public class RechargeChangeEvent extends ApplicationEvent {
 
    private MemberPriceEntity memberPriceEntity;

    public RechargeChangeEvent(Object source, MemberPriceEntity memberPriceEntity) {
        super(source);
        this.memberPriceEntity = memberPriceEntity;
    }

    public MemberPriceEntity getMemberPriceEntity() {
        return memberPriceEntity;
    }

    public void setMemberPriceEntity(MemberPriceEntity memberPriceEntity) {
        this.memberPriceEntity = memberPriceEntity;
    }
}