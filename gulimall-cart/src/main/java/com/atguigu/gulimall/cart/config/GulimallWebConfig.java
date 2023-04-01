package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: GulimallWebConfig
 * Package: com.atguigu.gulimall.cart.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 07:09
 * @Version 1.0
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //攔截所有請求
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
