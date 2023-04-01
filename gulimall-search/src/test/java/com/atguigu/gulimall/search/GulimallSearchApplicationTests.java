package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class Account {

        private int accountNumber;

        private int balance;

        private String firstname;

        private String lastname;

        private int age;

        private String gender;

        private String address;

        private String employer;

        private String email;

        private String city;

        private String state;
    }

    @Test
    public void searchData() throws IOException {
        //1. 創建檢索請求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，(檢索條件)
        //SearchSourceBuilder sourceBuilder 封裝的條件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.1) 構造檢索條件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation();
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        // 1.2 按照年龄值聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        // 1.3 计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("檢索條件" + sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        // 2.执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3.分析结果
        System.out.println(searchResponse.toString());
//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);


        //3.1 獲取所有查到的數據
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
//            hit.getIndex();hit.getType();hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account: " + account);
        }

        //3.2 獲取這次檢索到的分析信息
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println("當前聚合: " + aggregation.getName());
//        }
        Terms agg = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : agg.getBuckets()) {
            System.out.println("年龄: " + bucket.getKeyAsString() + "-->" + bucket.getDocCount() + "人");
        }

        Avg avg = aggregations.get("balanceAvg");
        System.out.println("平均薪资： " + avg.getValue());

    }

    @Data
    class User {
        private String username;
        private String gender;
        private Integer age;
    }

    /**
     * 測試存儲數據到es
     * 更新也可以
     *
     * @throws IOException
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        // 设置索引id
        indexRequest.id("1");
        // 第1种方式
//		request.source("userName","kurt","age","20","gender","男");


        // 第2种方式
        User user = new User();
        user.setUsername("kurt");
        user.setAge(20);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);

        // 传入json时 指定类型
        indexRequest.source(jsonString, XContentType.JSON);

        // 执行保存操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(index);
        System.out.println(index.status());
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
