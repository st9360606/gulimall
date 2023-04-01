package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: GulimallWebConfig
 * Package: com.atguigu.gulimall.auth.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 12:26
 * @Version 1.0
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
