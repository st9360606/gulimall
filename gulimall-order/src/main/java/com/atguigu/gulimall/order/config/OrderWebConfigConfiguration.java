package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: OrderWebConfigConfiguration
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 03:33
 * @Version 1.0
 */
@Configuration
public class OrderWebConfigConfiguration implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截哪个拦截器的所有请求
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
