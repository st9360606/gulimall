package com.atguigu.gulimall.order.to;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: OrderCreateTo
 * Package: com.atguigu.gulimall.order.to
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 10:40
 * @Version 1.0
 */
@Data
public class OrderCreateTo {
    //訂單實體
    private OrderEntity order;

    //訂單項
    private List<OrderItemEntity> orderItems;

    //訂單應付價格
    private BigDecimal payPrice;

    //運費
    private BigDecimal fare;
}
