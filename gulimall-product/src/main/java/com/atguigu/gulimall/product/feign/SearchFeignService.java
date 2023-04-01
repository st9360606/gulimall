package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: SearchFeignService
 * Package: com.atguigu.gulimall.product.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 上午 11:25
 * @Version 1.0
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
