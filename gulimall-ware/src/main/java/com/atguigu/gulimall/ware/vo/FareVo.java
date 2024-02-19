package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : z_dd
 * @date : 2024/2/19 14:00
 **/
@Data
public class FareVo {

    private MemberAddressVo address;

    private BigDecimal fare;
}
