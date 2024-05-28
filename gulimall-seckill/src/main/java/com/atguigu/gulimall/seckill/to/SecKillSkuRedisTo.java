package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : z_dd
 * @date : 2024/4/27 21:28
 **/
@Data
public class SecKillSkuRedisTo {
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀商品的随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    /**
     * SKU的基本信息
     */
    private SkuInfoVo skuInfoVo;

    /**
     * 当前商品秒杀的开始时间
     */
    private Long startTime;

    /**
     * 当前商品秒杀的开始==结束时间
     */
    private Long endTime;
}
