package com.atguigu.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SeckillOrderTo
 * Package: com.atguigu.common.to.mq
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/24 上午 01:01
 * @Version 1.0
 */
@Data
public class SeckillOrderTo {

    /**
     * 訂單號
     */
    private String orderSn;
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
     * 購買數量
     */
    private Integer num;

    //会员id
    private Long memberId;

}
