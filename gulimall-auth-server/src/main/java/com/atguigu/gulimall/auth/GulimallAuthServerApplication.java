package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Spring Session 核心原理：
 * @EnableRedisHttpSession 導入RedisHttpSessionConfiguration配置
 *      1、給容器添加了一個組件
 *          SessionRepositry  = 》》》 【RedisOperationsSessionRepository】 = 》 redis操作session。 session的增刪改查封裝類
 *      2、SessionRepositoryFilter --》Filter: session存儲的過濾器，每個請求都必須經過filter
 *              1、創建的時候 ，就自動從容器中獲取SessionRepository
 *              2、原始的 request，response都被包裝。 SessionRequestWrapper，SeesionRepositoryResponseWrapper
 *              3、以後獲取session。 request.getSession();
 *                 //SessionRepositoryRequestWrapper
 *              4、wrappedRequest.getSession()  ==>  SessionRepositry中獲取的。
 *      裝飾者模式：把原生的請求封裝成自己的
 *
 *      只要瀏覽器不關，Seesion是會自動延期，redis中的數據也是有過期時間的
 */

@EnableRedisHttpSession   //整合redis作為session存儲
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
