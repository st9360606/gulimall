package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * ClassName: CartFeignService
 * Package: com.atguigu.gulimall.order.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 07:38
 * @Version 1.0
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserItems")
    List<OrderItemVo> currentUserItems();
}
