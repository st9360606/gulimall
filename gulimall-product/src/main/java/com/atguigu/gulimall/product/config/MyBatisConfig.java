package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ClassName: MyBatisConfig
 * Package: com.atguigu.gulimall.product.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/6 下午 03:53
 * @Version 1.0
 */

@Configuration  //宣告是一個配置類
@EnableTransactionManagement //開啟事務
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyBatisConfig {
    //引入分頁插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }
}
