package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ClassName: ProductFeignService
 * Package: com.atguigu.gulimall.seckill.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/23 上午 01:09
 * @Version 1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);
}
