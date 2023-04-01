package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ClassName: CouponFeignService
 * Package: com.atguigu.gulimall.seckill.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:25
 * @Version 1.0
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();
}
