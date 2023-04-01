package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

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

    // @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     *  使用 JSON 序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    //TODO RabbitTemplate
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }



    /**
     * 定制rabbitTemplate
     * 1、服務收到消息就回調
     *       1.spring.rabbitmq.publisher-confirms=true
     *       2.設置確認回調 ConfirmCallback
     * 2、消息正確抵達隊列進行回調
     *      1. spring.rabbitmq.publisher-returns=true
     *         spring.rabbitmq.template.mandatory=true
     *      2.設定確認回調ReturnCallback
     * <p>
     *
     *
     * 3.消費端確認 (保證每個消息被正確消費，此時才可以broker刪除這個消息)。
     *      #手動ack模式 消息
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual  手動簽收
     *
     *      1.默認是自動確認的，只要消息接收到，客戶端自動確認，服務端就會移除這個消息
     *         問題 ：
     *             我們收到很多消息，自動回复給服務器ack，只有一個消息處理成功，當機了，發生消息丟失。
     *         解決 :
     *             消費者手動確認模式： 只要我們沒有明確告訴MQ，消息被簽收。就沒有Ack，消息就一直是unacked狀態。
     *            ，即使Consumer當機。 消息也不會丟失，會重新變為Ready狀態，下一次有新的Consumer連接進來就發給他。
     *      2.如果簽收
     *            channel.basicAck(deliveryTag, false); 簽收消息: 業務成功完成就應該簽收
     *            channel.basicNack(deliveryTag,false,false); 拒簽消息: 業務失敗，拒簽
     */
    //@PostConstruct   //MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {
        //設置確認回調
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 1、只要消息抵达Broker代理服務 就 ack=true
             * @param correlationData 当前消息唯一关联的数据 (这个是消息的唯一id)
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /**
                 * 1、做好消息確認機制 (publiser，consumer手動Ack)
                 * 2、每一個發送的消息都在數據庫做好紀錄，定期將失敗的消息再次發送一遍
                 */
                //服務器收到了
                //修改消息的狀態
                System.out.println("confirm....correlationData[" + correlationData + "]==>ack[" + ack + "]cause>>>" + cause);
            }
        });

        //設置消息抵達隊列的確認回調
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             * 只要消息沒有投遞給指定的隊列，就觸發這個失敗回調
             * @param message  投遞失敗消息的詳細信息
             * @param replyCode 回复的狀態碼
             * @param replyText 回复的文本內容
             * @param exchange 當時這個消息發給那個交換機
             * @param routingKey 當時這個消息用那個路由鍵
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //報錯誤了，未收到消息。 修改數據庫當前消息的狀態 -> 錯誤。
                System.out.println("Fail!! Message[" + message + "]==>replyCode[" + replyCode +
                        "]==>exchange[" + exchange + "]==>routingKey[" + routingKey + "]"+
                        "]==>replyText[" + replyText + "]"

                );
            }
        });

    }

}
