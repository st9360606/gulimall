package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ClassName: WareFeignService
 * Package: com.atguigu.gulimall.order.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/17 上午 01:35
 * @Version 1.0
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId")Long addId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
