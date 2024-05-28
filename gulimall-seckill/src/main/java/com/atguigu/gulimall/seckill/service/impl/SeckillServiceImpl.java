package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : z_dd
 * @date : 2024/4/27 17:28
 **/
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private static final String SKUKILL_CACHE_PREFIX = "seckill:sku";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码

    /**
     * 上架最近三天要秒杀的商品
     *
     * @return void
     * @author z_dd
     * @date 2024/4/27 17:29
     **/
    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1.扫描最近三天需要参与秒杀的活动
        final R r = couponFeignService.getLates3DaySession();
        if (r.getCode() == 0) {
            final List<SeckillSessionsWithSkus> data = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis

            if (!CollectionUtils.isEmpty(data)) {
                //1.缓存活动信息
                saveSessionInfos(data);

                //2.缓存活动的商品信息
                saveSessionSkuInfos(data);
            }
        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //1.确定当时时间属于哪个秒杀场次
        final long time = new Date().getTime();

        final Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");

        for (String key : keys) {

            final String[] split = key.replace(SESSIONS_CACHE_PREFIX, "").split("_");
            final long start = Long.parseLong(split[0]);
            final long end = Long.parseLong(split[1]);

            if (time >= start && time <= end) {
                //2.获取当前场次的商品信息
                final List<String> range = redisTemplate.opsForList().range(key, -100, 100);

                final BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                final List<String> list = hashOps.multiGet(range);

                if (!CollectionUtils.isEmpty(list)) {
                    final List<SecKillSkuRedisTo> collect = list.stream().map(item -> JSON.parseObject(item, SecKillSkuRedisTo.class)).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }

        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        final BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        final Set<String> keys = ops.keys();

        if(!CollectionUtils.isEmpty(keys)){
            String regx="^\\d+_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)) {
                    final String s = ops.get(key);
                    final SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, SecKillSkuRedisTo.class);
                    //随机码处理
                    final long time = new Date().getTime();
                    if (time <secKillSkuRedisTo.getStartTime() || time>secKillSkuRedisTo.getEndTime() ){
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }

    //活动信息
    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        sessions.stream().forEach(session -> {
            final Long startTime = session.getStartTime().getTime();
            final Long endTime = session.getEndTime().getTime();
            final String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;


            final Boolean aBoolean = redisTemplate.hasKey(key);
            if (!aBoolean) {

                final List<String> collect = session.getRelationEntities()
                        .stream()
                        .map(relation -> relation.getPromotionSessionId() + "_" + relation.getSkuId().toString())
                        .collect(Collectors.toList());

                redisTemplate.opsForList().leftPushAll(key, collect);
            }


        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {

        sessions.stream().forEach(session -> {
            final BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationEntities()
                    .stream()
                    .forEach(seckillSkuVo -> {
                        //4.随机码
                        final String token = UUID.randomUUID().toString().replace("-", "");

                        if (Boolean.FALSE.equals(ops.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString()))) {
                            SecKillSkuRedisTo to = new SecKillSkuRedisTo();
                            //1.SKU的秒杀信息
                            BeanUtils.copyProperties(seckillSkuVo, to);

                            //2.SKU的基本信息
                            final R info = productFeignService.info(seckillSkuVo.getSkuId());
                            if (info.getCode() == 0) {
                                final SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                                });
                                to.setSkuInfoVo(skuInfo);
                            }

                            //3.设置当前商品的秒杀信息
                            to.setStartTime(session.getStartTime().getTime());
                            to.setEndTime(session.getEndTime().getTime());


                            to.setRandomCode(token);

                            final String jsonString = JSON.toJSONString(to);
                            ops.put(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);

                            //创建库存信号量
                            final RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                            semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                        }


                    });

        });

    }
}
