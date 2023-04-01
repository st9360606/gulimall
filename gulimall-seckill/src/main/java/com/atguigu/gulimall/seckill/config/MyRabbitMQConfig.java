package com.atguigu.gulimall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ClassName: MyRabbitMQConfig
 * Package: com.atguigu.gulimall.order.config
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/15 上午 11:59
 * @Version 1.0
 */
@Configuration
public class MyRabbitMQConfig {

    /**
     * 使用 JSON 序列化机制，进行消息转换
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
