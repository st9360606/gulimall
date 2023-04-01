package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: WareSkuLockVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/18 下午 02:03
 * @Version 1.0
 */
@Data
public class WareSkuLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}