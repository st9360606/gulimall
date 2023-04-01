package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ClassName: OrderFeignService
 * Package: com.atguigu.gulimall.ware.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/20 上午 02:28
 * @Version 1.0
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
