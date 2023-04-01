package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: FareVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 11:12
 * @Version 1.0
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
