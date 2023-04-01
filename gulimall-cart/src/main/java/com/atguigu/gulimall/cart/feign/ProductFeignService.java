package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * ClassName: ProductFeignService
 * Package: com.atguigu.gulimall.cart.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 10:43
 * @Version 1.0
 */
//告訴spring cloud 這個接口是一個遠程客戶端 調用遠程服務
@FeignClient("gulimall-product")//這個遠程服務
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);


    @GetMapping("/product/skuinfo/{skuId}/price")
    R getPrice(@PathVariable("skuId") Long skuId);


}