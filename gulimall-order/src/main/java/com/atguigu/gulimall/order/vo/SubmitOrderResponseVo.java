package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * ClassName: SubmitOrderResponseVo
 * Package: com.atguigu.gulimall.order.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 09:44
 * @Version 1.0
 */
@Data
public class SubmitOrderResponseVo {

    //下单成功返回这个实体
    private OrderEntity order;
    //下单错误给一个状态码    0:成功  1:錯誤之類的
    private Integer code;
}