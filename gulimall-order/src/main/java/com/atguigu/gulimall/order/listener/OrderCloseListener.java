package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ClassName: OrderCloseListener
 * Package: com.atguigu.gulimall.order.listener
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/20 下午 01:54
 * @Version 1.0
 */

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    //監聽隊列 (要關閉訂單的隊列)
    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单，准备关闭订单。orderID:" + entity.getId() + "; orderSn:" + entity.getOrderSn());
        try {
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //消息失敗拒絕消息，讓他重新回到消息隊列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
