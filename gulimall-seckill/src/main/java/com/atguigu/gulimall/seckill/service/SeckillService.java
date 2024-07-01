package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author : z_dd
 * @date : 2024/4/27 17:28
 **/
public interface SeckillService {
    /**
     * 上架最近三天要秒杀的商品
     *
     * @return void
     * @author z_dd
     * @date 2024/4/27 17:29
     **/
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
