package com.atguigu.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * ClassName: GulimallSessionConfig
 * Package: com.atguigu.gulimall.auth.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/11 下午 02:46
 * @Version 1.0
 */
//開啟Spring Session功能 redis
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {

    /**
     * 自定義session作用域：整個網站
     * 使用一樣的session配置，能保證全網站共享一樣的session
     */
    @Bean
    public CookieSerializer cookieSerializer() {

        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();

        //設置作用域
        defaultCookieSerializer.setDomainName("gulimall.com");
        defaultCookieSerializer.setCookieName("GULISESSION");

        return defaultCookieSerializer;
    }

    /**
     * 序列化機制
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){

        return new GenericJackson2JsonRedisSerializer();
    }
}