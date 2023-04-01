package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ClassName: ProductFeignService
 * Package: com.atguigu.gulimall.order.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 下午 11:54
 * @Version 1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id")Long skuId);
}
