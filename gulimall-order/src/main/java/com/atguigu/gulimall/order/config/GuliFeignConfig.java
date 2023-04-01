package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: GuliFeignConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 09:43
 * @Version 1.0
 */
@Configuration
public class GuliFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //1、RequestContextHolder拿到剛進來的這個請求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
//                    System.out.println("RequestInterceptor線程...." + Thread.currentThread().getId());
                    HttpServletRequest request = attributes.getRequest();  //老請求
                    if (request != null) {
                        //同步請求頭數據, Cookie
                        String cookie = request.getHeader("Cookie");

                        //給新請求同步了老請求的Cookie
                        template.header("Cookie", cookie);  //新請求
                    }
                }


            }
        };
    }
}
