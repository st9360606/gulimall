package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SeckillInfoVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/23 下午 06:16
 * @Version 1.0
 */
@Data
public class SeckillInfoVo {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;

    //商品秒杀随机码
    private String randomCode;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //当前sku的秒杀开始时间
    private Long startTime;

    //当前sku的秒杀结束时间
    private Long endTime;
}
