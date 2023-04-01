package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: CartItem
 * Package: com.atguigu.gulimall.cart.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 上午 02:09
 * @Version 1.0
 * @Data 不用的原因
 * 需要計算的屬性必須重新get()的方法，這樣才能保證每一次屬性都會進行重新計算
 */
//@Data
public class CartItem {
    private Long skuId;

    //是否被選中
    private Boolean check = true;
    private String title;
    private String image;

    //套餐信息
    private List<String> skuAttr;

    //涉及到計算 必須用BigDecimal
    private BigDecimal price;

    private Integer count;

    //總價
    private BigDecimal totalPrice;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 計算每一個購物項的總價
     */
    public BigDecimal getTotalPrice() {
        //multiply: 乘號
        //價格 X 數量
        return this.price.multiply(new BigDecimal("" + this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}