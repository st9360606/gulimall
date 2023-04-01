package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MyMQConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/19 下午 09:50
 * @Version 1.0
 */
@Configuration
public class MyMQConfig {

    /**
     * 容器中的Queue、Exchange、Binding 会自动创建（在RabbitMQ）不存在的情况下
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        /*
            Queue(String name,  隊列名字
            boolean durable,  是否持久化
            boolean exclusive,  是否排他
            boolean autoDelete, 是否自動刪除
            Map<String, Object> arguments) 自定義屬性
         */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");     //死信路由
        arguments.put("x-dead-letter-routing-key", "order.release.order");   //死信路由鍵
        arguments.put("x-message-ttl", 60000); // 消息过期时间 1分钟
        //延时队列
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);

        return queue;

    }
    @Bean
    public Queue orderReleaseOrderQueue(){
        //普通队列
        return new Queue("order.release.order.queue", true, false, false);
    }
    @Bean
    public Exchange orderEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        //Topic類型的交换机
        return new TopicExchange("order-event-exchange", true, false);
    }
    @Bean
    public Binding orderCreateOrderBinding() {
        //和延时队列绑定
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }
    @Bean
    public Binding orderReleaseOrderBinding() {
        //和普通队列绑定
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 訂單釋放直接和庫存釋放進行綁定
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        //订单释放直接和库存释放进行绑定
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Queue orderSeckillOrderQueue(){
        //是否持久化的 是否排他的(大家都能监听，谁抢到算谁的) 是否自动删除
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding orderSeckillOrderQueueBinding() {

        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }


}
