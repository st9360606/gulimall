package com.atguigu.gulimall.order;


import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage() {

        //1、測試發送消息，如果發送的消息是個對象，我們會使用序列化機制，將對象寫出去，對象必須實現Serializable
        String msg = "Hello World!";

        //2、發送的對象類型的消息，可以是一個json，但前提是你需要先配置
        /**
         *  @Bean
         *     public MessageConverter messageConverter(){
         *         return new Jackson2JsonMessageConverter();
         *     }
         */
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("哈哈 - " + i);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
                        reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
                        entity, new CorrelationData(UUID.randomUUID().toString()));
            }

//            log.info("消息发送完成 {} ", reasonEntity);
            log.info("消息发送完成 {} ");
        }

    }


    /**
     * 1、如何创建Exchange、Queue、Binding
     * 1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        //創建交換機
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueue() {
        //創建對列
        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    /**
     * 创建 Binging
     * hello.java
     */
    @Test
    public void creatBinging() {
        // public Binding(String destination,:目的地
        // DestinationType destinationType,:目的地类型
        // String exchange,:交换机  (哪個交換機跟隊列進行綁定)
        // String routingKey, :路由key
        // map<String,Object> arguments :自定义参数

        //將exchange指定的交換機和destination目的地進行綁定，使用routingKey作為指定的路由鍵
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

}
