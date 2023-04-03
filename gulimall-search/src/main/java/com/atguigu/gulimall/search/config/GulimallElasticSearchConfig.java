package com.atguigu.gulimall.search.config;


import org.apache.http.HttpHost;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: GulimallElasticSearchConfig
 * Package: com.atguigu.gulimallsearch.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/15 下午 04:07
 * @Version 1.0
 */

/**
 * 1. 導入依賴
 * 2. 編寫配置 給容器中注入一個RestHighLevelClient
 */


@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//		builder.addHeader("Authorization", "Bearer " + TOKEN);
//		builder.setHttpAsyncResponseConsumerFactory(
//				new HttpAsyncResponseConsumerFactory
//						.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    @Bean
    public RestHighLevelClient esRestClient(@Value("${spring.elasticsearch.jest.uris}") String esUrl) {
        RestClientBuilder builder = null;
        //final String hostname, final int port, final String scheme
//        builder = RestClient.builder(new HttpHost("192.168.56.10", 9200, "http"));
        builder = RestClient.builder(HttpHost.create(esUrl));
        RestHighLevelClient client = new RestHighLevelClient(builder);
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("192.168.56.10", 9200, "http")));
        return client;
    }
}
