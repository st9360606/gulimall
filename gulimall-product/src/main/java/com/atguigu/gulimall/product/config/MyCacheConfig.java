package com.atguigu.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ClassName: MyCacheConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/23 下午 03:41
 * @Version 1.0
 */

//綁定配置文件的配置
@EnableConfigurationProperties(CacheProperties.class)
//開啟緩存功能
@Configuration
@EnableCaching
public class MyCacheConfig {
    //@Autowired
    //CacheProperties cacheProperties;

    /**
     * 配置文件中的東西沒有用上，解決如下;
     * 1）、原來和配置文件的綁定配置是這樣子的
     *     @ConfigurationProperties(prefix = "spring.cache")
     *     public class CacheProperties {
     * 2)、要讓他生效
     *      @EnableConfigurationProperties(CacheProperties.class)
     * @return
     */

    /**
     * 主要目的是加入Json序列化機制，讓redis 能以Json格式顯現出資料
     * @param cacheProperties
     * @return
     */
    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

//        config = config.entryTtl();

        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        //將配置文件中的所有配置都生效
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }


        return config;
    }
}
