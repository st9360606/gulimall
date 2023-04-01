package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * ClassName: HelloController
 * Package: com.atguigu.gulimall.order.web
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/15 下午 10:12
 * @Version 1.0
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        //訂單下單成功
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());

        //给MQ发消息
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
        return "ok";

    }

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page")String page){
        return page;
    }


}
