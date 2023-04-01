package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * ClassName: LockStockResult
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/18 下午 02:22
 * @Version 1.0
 */
@Data
public class LockStockResult {
    private Long skuId;
    private Integer num;
    private Boolean locked;
}
