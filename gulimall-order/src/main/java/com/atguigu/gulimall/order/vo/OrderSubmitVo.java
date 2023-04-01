package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * ClassName: OrderSubmitVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 08:57
 * @Version 1.0
 */
@ToString
@Data
public class OrderSubmitVo {

    //收貨地址id
    private Long addrId;

    //支付方式
    private Integer payType;

    //无需提交购买的商品，去购物车重新再获取一遍

    //优惠、发票....

    //防重令牌
    private String orderToken;

    //应付价格 校验价格
    private BigDecimal payPrice;

    //订单备注 要不要辣的
    private String node;

    //用户相关信息，直接去session取出登录用户
}
