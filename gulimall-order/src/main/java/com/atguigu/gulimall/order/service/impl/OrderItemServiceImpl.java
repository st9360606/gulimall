package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 監聽消息
     * queues 聲明需要監聽的所有隊列
     * org.springframework.amqp.core.Message
     * <p>
     * 參數可以寫一下類型
     * 1、Message essage: 原生消息詳細信息。頭+體
     * 2、T <發送的消息的類型>  OrderReturnReasonEntity content;
     * 3、Channel channel:當前傳輸數據的通道
     * <p>
     * Queue:可以很多人都來監聽。 只要收到消息,隊列刪除消息,而且只能有一個人收到此消息。
     * 1)、訂單服務啟動多個：同一個消息,只能有一個客戶端收到
     * 2)、只有一個消息完全處理完,方法運行結束，我們就可以接收到下一個消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void recieveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException {
        //消息體
        System.out.println("接收到消息...內容: " + content);
        byte[] body = message.getBody();

        //消息頭的屬性信息
        MessageProperties properties = message.getMessageProperties();
//        System.out.println("接收到消息...內容: " + message + "==> 類型:" + message.getClass());
//        System.out.println("接收到消息...內容: " + message + "===> 內容: " + content);
//        Thread.sleep(3000);
        System.out.println("消息處理完成=>" + content.getName());
        //消息處理完 手動確認  deliveryTag在Channel內按順序自增。
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==> " + deliveryTag);

        //簽收消息，false:非批量簽收模式
        try {
            if (deliveryTag % 2 == 0) {
                //收貨
                channel.basicAck(deliveryTag, false);
                System.out.println("簽收了消息..." + deliveryTag);
            } else {
                //退貨 requeue=false 丟棄  requeue=true 發回服務器，服務器重新入隊。
                channel.basicNack(deliveryTag,false,false);  //第二個參數可以批量拒絕，第三个参数 -> true:重新入队 false:丢弃。
//                channel.basicReject(); //不可以批量
                System.out.println("沒有簽收了消息..." + deliveryTag);
            }
        } catch (Exception e) {
            //網路中斷
            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void recieveMessage2(OrderEntity content) throws InterruptedException {
        //消息體
        System.out.println("接收到消息...內容: " + content);
    }

}