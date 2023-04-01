package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ClassName: OrderSeckillListener
 * Package: com.atguigu.gulimall.order.listener
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/24 上午 01:16
 * @Version 1.0
 */
@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {


    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listening(SeckillOrderTo seckillOrder, Channel channel, Message message) throws IOException {

        try {
            log.info("创建秒杀单的詳細信息.....");
            orderService.creatSeckillOrder(seckillOrder);
            //手动调用支付宝收单 p310 暂时不用手动
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            //true 重新回到消息队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
