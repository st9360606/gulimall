package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MyRabbitConfig
 * Package: com.atguigu.gulimall.ware.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/19 下午 11:57
 * @Version 1.0
 */

@Configuration
public class MyRabbitConfig {

    /**
     * 使用 JSON 序列化机制，进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //需要監聽RabbitMQ，才會創建隊列跟交換機，否則不能创建RabbitMQ的东西。
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message) {
//
//    }

    @Bean
    public Exchange stockEventExchange() {

        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        //普通交换机
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {

        //普通队列
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    //容器中的组建Queue Exchange Binding 都会自动创建（前提是RabbitMQ没有）
    //延時隊列
    @Bean
    public Queue stockDelayQueue() {

        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");//死信交换机
        args.put("x-dead-letter-routing-key", "stock.release");//死信路由键
        args.put("x-message-ttl", 120000);//消息过期时间 ms 2分钟
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding stockReleaseBinding() {

        //和普通队列绑定
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding() {

        //和延时队列绑定
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

}
