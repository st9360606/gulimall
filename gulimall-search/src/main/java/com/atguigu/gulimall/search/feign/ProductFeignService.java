package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ClassName: ProductFeignService
 * Package: com.atguigu.gulimall.search.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/28 下午 08:32
 * @Version 1.0
 */
//告訴spring cloud 這個接口是一個遠程客戶端 調用遠程服務
@FeignClient("gulimall-product")//這個遠程服務
public interface ProductFeignService {

    @GetMapping("product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("product/brand/infos")
    public R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);

}