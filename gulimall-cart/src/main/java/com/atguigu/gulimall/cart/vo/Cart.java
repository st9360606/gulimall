package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: Cart
 * Package: com.atguigu.gulimall.cart.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 上午 02:09
 * @Version 1.0
 * @Data 不用的原因
 * 需要計算的屬性，必須重寫他的get()方法，這樣才能保證每一次獲取屬性都會進行重新計算
 */
//@Data
public class Cart {
    List<CartItem> items;

    //全部sku的總數 3+3=6
    private Integer countNum;   //商品數量

    //共有多少種類型
    private Integer countType;

    //所有sku總價
    private BigDecimal totalAmount; //商品總價

    //優惠減免價格
    private BigDecimal reduce = new BigDecimal("0.00");

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public Integer getCountNum() {

        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {

        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {

        BigDecimal amount = new BigDecimal("0");
        //1 計算購物項的總價
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                //被選擇才會疊加價格
                if (item.getCheck()) {
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        //2 減去優惠總價
        // subtract : 減號
        //總價 - 優惠
        BigDecimal subtract = amount.subtract(getReduce());

        return subtract;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}