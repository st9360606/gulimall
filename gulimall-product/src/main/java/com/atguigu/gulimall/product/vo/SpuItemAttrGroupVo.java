package com.atguigu.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: SpuItemAttrGroupVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/2 上午 01:32
 * @Version 1.0
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}