package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在Controller有@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
 *      @Documented
 * @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
 * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 * @Retention(RUNTIME)
 * public @interface ListValue {
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *       1）、编写异常处理类，使用@ControllerAdvice。
 *       2）、使用@ExceptionHandler标注方法可以处理的异常。
 *
 *  5、模板引擎
 *       1)、thymeleaf-starter:關閉緩存
 *       2)、靜態資源都放在static文件夾下就可以按照路徑直接訪問
 *       3)、頁面放在template下，直接訪問
 *           SpringBoot，訪問項目的時候，默認會找index
 *       4)、頁面修改不重啟服務器實時更新
 *       1)、引入dev-tools
 *       2)、修改完頁面 controller shift+f9 重新自動編譯下頁面，如果是代碼配置推薦重啟
 *
 *  6、整合redis
 *       1)、引入data-redis-starter
 *       2)、簡單配置redis的host等信息
 *       3)、使用SpringBoot自動配置好的StaringRedisTemplate來操作redis
 *       redis =>> Map: 存放數據key,數據值value
 *  7、整合redisson作為分布式鎖等功能框架
 *      1)、引入依賴
 *       <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *       </dependency>
 *       2)、配置redisson
 *              MyRedissonConfig給容器中配置一個RedissonClient實例即可
 *       3)、使用
 *          參照文檔做。
 *
 *  8、整合SringCache 簡化緩存開發
 *       1）、引入依賴
 *       spring-boot-starter-cache、spring-Boot-starter-data-redis
 *       2）、寫配置
 *           1）自動配置了那些？
 *                  CacheAutoConfiguration 會導入 RedisCacheConfiguration
 *                  自動配置好了緩存管理器RedisCacheManage
 *           2)配置使用redis作為緩存
 *                  spring.cache.type=redis
 *       3）、測試使用緩存
 *          @Cacheable     保存 觸發將數據保存到緩存的操作
 *          @CacheEvict    刪除 觸發將數據從緩存刪除的操作
 *          @CachePut      更新 不影響方法執行更新緩存的操作
 *          @Caching       組合 組合以上多個操作
 *          @CacheConfig   在類級別共享緩存的相同配置
 *       1)、開啟緩存功能
 *           @EnableCaching
 *       2）、只需要使用註解
 *       3）、將數據保存為json格式
 *           自定義RedisCacheConfiguration
 *
 *   4.Spring-cache的不足：
 *       1）、讀模式
 *           緩存穿透：查詢一個null的數據 解決：緩存空數據 cache-null-value=true
 *           緩存擊穿：大量並發進來同時查詢一個正好過期的數據。解決：加鎖 默認是無加鎖的 sync=true
 *           緩存雪崩：大量key同時過期 解決 加隨機時間，加上過期時間，spring-cache-redis-time-to
 *
 *       2）、寫模式： 緩存與數據一致
 *           1）、讀寫枷鎖
 *           2）、引入Canal 感知到MySQL的更新去更新數據庫
 *           3）、讀多寫多，直接去數據庫查詢就行
 *
 *       總結：
 *       常規數據 讀多寫少 即時性，一致性要求不高的數據 完全可以使用Spring-cache：寫模式 只要緩存數據有過期時間就行
 *
 *       原理：
 *       CacheAutoConfiguration -> RedisCacheConfiguration ->
 *       自動配置了 RedisCacheManager -> 初始化所有的緩存 -> 每個緩存決定使用什麼配置
 *       -> 如果 redisCacheConfiguration 有就用已有的，沒有就用默認配置
 *       -> 想改緩存的配置，只需要給容器中放一個 RedisCacheConfiguration 即可
 *       -> 就會應用到當前的 RedisCacheManager 管理的所有緩存分區中
 *
 *
 *
 *  4、統一的異常處理
 *       @ControllerAdvice
 *       1）、編寫異常處理類，使用@ControllerAdvice。
 *       2）、使用@ExceptionHandler標註方法可以處理的異常。
 */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
