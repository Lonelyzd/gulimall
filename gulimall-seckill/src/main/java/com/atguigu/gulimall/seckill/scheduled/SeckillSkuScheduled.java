package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 * 每天3:00定时上架最近三天要秒杀的商品
 *
 * @author : z_dd
 * @date : 2024/4/27 17:13
 **/
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    public static final String uploadLock = "seckill:upload:lock";

    @Scheduled(cron = "*/5 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //重复上架无需处理
        log.info("===上架秒杀的商品信息");
        //分布式锁

        final RLock lock = redissonClient.getLock(uploadLock);

        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }
    }
}
