package com.atguigu.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author: z_dd
 * @date: 2023/5/22 21:20
 * @Description:
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties()
public class MyCacheConfig {
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));

        final CacheProperties.Redis redisConfig = cacheProperties.getRedis();
        if (redisConfig.getTimeToLive() != null) {
            config = config.entryTtl(redisConfig.getTimeToLive());
        }

        if (redisConfig.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisConfig.getKeyPrefix());
        }

        if (!redisConfig.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (!redisConfig.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
