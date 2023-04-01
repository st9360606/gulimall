package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: WareFeignService
 * Package: com.atguigu.gulimall.product.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 上午 10:28
 * @Version 1.0
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 1. R設計的時候可以加上泛型
     * 2. 直接返回我們想要的結果
     * 3. 自己封裝解析結果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
