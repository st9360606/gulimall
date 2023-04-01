package com.atguigu.common.to;

import lombok.Data;

/**
 * ClassName: SkuHasStockVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 上午 10:14
 * @Version 1.0
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
