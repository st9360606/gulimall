package com.atguigu.gulimall.seckill.config;

import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName: SeckillWebConfig
 * Package: com.atguigu.gulimall.seckill.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/23 下午 10:51
 * @Version 1.0
 */
@Configuration
public class SeckillWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**"); //攔截所有路徑
    }
}
