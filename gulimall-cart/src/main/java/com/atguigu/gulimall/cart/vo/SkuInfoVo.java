package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SkuInfoVo
 * Package: com.atguigu.gulimall.cart.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 04:26
 * @Version 1.0
 */
@Data
public class SkuInfoVo {

    /**
     * skuId
     */
    private Long skuId;
    /**
     * spuId
     */
    private Long spuId;
    /**
     * sku名稱
     */
    private String skuName;
    /**
     * sku介紹描述
     */
    private String skuDesc;
    /**
     * 所屬分類id
     */
    private Long catalogId;
    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 默認圖片
     */
    private String skuDefaultImg;
    /**
     * 標題
     */
    private String skuTitle;
    /**
     * 副標題
     */
    private String skuSubtitle;
    /**
     * 價格
     */
    private BigDecimal price;
    /**
     * 銷量
     */
    private Long saleCount;
}