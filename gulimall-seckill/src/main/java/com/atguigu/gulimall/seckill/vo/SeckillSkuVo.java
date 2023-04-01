package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SeckillSkuVo
 * Package: com.atguigu.gulimall.seckill.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:19
 * @Version 1.0
 */
@Data
public class SeckillSkuVo {

    private Long id;
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
}
