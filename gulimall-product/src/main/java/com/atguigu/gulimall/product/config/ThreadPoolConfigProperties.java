package com.atguigu.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: ThreadPoolConfigProperties
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 上午 09:34
 * @Version 1.0
 */
//跟配置文件绑定
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
