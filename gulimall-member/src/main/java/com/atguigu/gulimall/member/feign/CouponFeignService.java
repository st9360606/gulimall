package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ClassName: CouponFeignService
 * Package: com.atguigu.gulimall.member.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/1/24 下午 02:32
 * @Version 1.0
 */

/**
 * 這是一個聲明式的遠程調用
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/member/list")
    public R membercoupons();
}
