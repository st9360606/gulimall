package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: SpuBoundTo
 * Package: com.atguigu.common.to
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/9 下午 02:49
 * @Version 1.0
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}
