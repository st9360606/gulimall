package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * ClassName: FareVo
 * Package: com.atguigu.gulimall.ware.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 03:50
 * @Version 1.0
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}