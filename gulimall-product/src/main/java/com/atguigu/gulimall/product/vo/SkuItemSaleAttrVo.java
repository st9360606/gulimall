package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: SkuItemSaleAttrVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/2 上午 01:35
 * @Version 1.0
 */
@ToString
@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}

