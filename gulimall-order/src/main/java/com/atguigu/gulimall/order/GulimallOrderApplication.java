package com.atguigu.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用 RabbitMQ 消息隊列
 * 1 引入依賴
 *      spring-boot-starter-amqp
 * 2 配置文件 配置host port virtual-host
 * 3 加註解
 *      @EnableRabbit
 *
 *  @Transactional 本地事務失效問題
 *  同一個對象內事務互調默認方法 原因 繞過了代理對象 事務使用代理對象來控制
 *  解決：使用代理對象調用事務方法
 *      1）、引入aop-starter; spring-boot-starter-aop 引入aspectj
 *      2）、@EnableAspectjAutoProxy(exposeProxy = true) 開啟 aspectj 動態代理功能， 以後所有的動態代理都是aspectj創建的(即使沒有接口也可以創建動態代理)。
 *              對外暴露代理對象
 *      3)、本類互調用調用對象
 *          OrderServiceImpl orderservice = (OrderServiceImpl) AopContext.currentProxy();
 *          orderservice.b();
 *          orderservice.c();
 *
 * 解決數據一致性問題
 *  Raft算法 http://thesecretlivesofdata.com/raft/
 *          BASE理論
 *              使用AP
 *              捨棄CP強一致(本地數據庫就是)， 使用最終一致即可
 *
 *  分佈式事物幾種解決方案
 *      1 2PC模式 二級提交協議
 *      2 柔性事物 TCC事務補償型方案
 *          剛性事務：遵循ACID原則，強一致性原則
 *          柔性事務：遵循BASE理論，最終一致性
 *      3 柔性事務 最大努力通知型方案
 *      4 柔性事務 可靠消息+最終一致性方案（異步確保型）‍️
 *
 * Seata 控制分佈式事務 【不用】
 *   1）、每一個微服務必須創建uodo_log
 *   2)、安裝事務協調器 seata-server https://github.com/seata.seata/releases
 *   3)、整合
 *        1、導入依賴 spring-cloud-starter-alibaba-seata ｜ seata-all-0.7.1
 *        2、解壓並啟動seata-server：(TC)
 *            registry.conf 註冊中心配置 修改registry type=nacos
 *            file.conf
 *        3、所有想要用到分佈式事務的微服務使用seata : DataSourceProxy
 *        4、每個服務都必須導入
 *            registry.conf
 *            file.conf 設置：vgroup_mapping.gulimall-order-fescar-service-group = "default"
 *        5、啟動測試分布式事務
 *        6、給分布式大事務的入口標註：@GlobalTransactional
 *        7、每一個遠程的小事務用：@Transactional
 *
 * RabbitMQ延時隊列 用
 *  TTL 消息存活時間 隊列和消息可以一設置TTL
 *      死信：1 被消費者拒收，且不讓回歸隊列，直接丟棄
 *           2 過了時間
 *           3 隊列限制滿了，老消息被丟棄
 *  延時隊列實現-1 【用】
 *      設置【隊列】過期時間 實現延時隊列
 *  延時隊列實現-2 【不用】
 *      設置【消息】過期時間 實現延時隊列
 */

/**
 使用RabbitMQ
 * 引入rabbitMQ
 * 1. 引入amqp场景 RabbitAutoConfiguration就会自动生效
 * 2. 给容器中配置了 rabbitTemplate、amqpAdmin、CachingConnectionFactory、 RabbitMessagingTemplate
 *      所有的属性都是
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 * 3. 给配置文件添加 spring.rabbitmq信息
 * 4. @EnableRabbit 开启功能
 * 5. 監聽消息，使用有 @RabbitListener，想監聽必須有 @EnableRabbit
 *      @RabbitListener :     類+方法上 (監聽那些隊列即可)
 *      @RabbitHandler  : 標在 方法上 (重載區分不同的消息)
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableRabbit
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
