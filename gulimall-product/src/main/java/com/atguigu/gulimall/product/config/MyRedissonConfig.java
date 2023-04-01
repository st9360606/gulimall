package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * ClassName: MyRedissonConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/20 下午 02:53
 * @Version 1.0
 */
@Configuration
public class MyRedissonConfig {
    /**
     * 所有對Redisson的使用都是通過RedissonClient對象
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        //1 創建配置
        //Redis url should start with redis:// or rediss:// (for SSL connection)
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");

        //2 根據Config創建出RedissonClient實例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
