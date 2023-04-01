package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ClassName: OrderConfirmVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 03:58
 * @Version 1.0
 */

public class OrderConfirmVo {

    @Getter
    @Setter
    /** 會員收穫地址列表 **/
            List<MemberAddressVo> address;

    @Getter
    @Setter
    /** 所有選中的購物項 **/
            List<OrderItemVo> items;

    /**
     * 發票記錄
     **/
    @Getter
    @Setter
    /** 優惠券（會員積分） **/
    private Integer integration;

    /**
     * 防止重複提交的令牌
     **/
    @Getter
    @Setter
    private String orderToken;

    @Getter
    @Setter
    Map<Long, Boolean> stocks;

    //总件数
    public Integer getCount() {
        Integer sum = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                sum += item.getCount();
            }
        }
        return sum;
    }

    //订单总额 需要计算
    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                //當前價格X數量
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    //    private BigDecimal payPrice;
    //应付价格 需要计算
    public BigDecimal getPayPrice() {

        return getTotal();
    }
}