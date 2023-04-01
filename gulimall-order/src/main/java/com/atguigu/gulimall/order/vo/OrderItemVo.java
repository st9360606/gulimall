package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: OrderItemVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 04:01
 * @Version 1.0
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String image;
    //套餐信息
    private List<String> skuAttr;
    //涉及到计算 必须用BigDecimal
    private BigDecimal price;
    private Integer count;
    //总价
    private BigDecimal totalPrice;

    private BigDecimal weight;

}
