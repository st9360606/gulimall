package com.atguigu.gulimall.order.vo;

import lombok.Data;

/**
 * ClassName: SkuStockVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 上午 01:38
 * @Version 1.0
 */
@Data
public class SkuStockVo {

    private Long skuId;
    private Boolean hasStock;
}
