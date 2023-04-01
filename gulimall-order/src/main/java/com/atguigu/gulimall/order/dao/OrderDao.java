package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author kurt
 * @email st9360606@gmail.com
 * @date 2023-01-23 22:09:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {


    void updateOrderStatus(@Param("outTradeNo") String outTradeNo, @Param("code") Integer code);
}
