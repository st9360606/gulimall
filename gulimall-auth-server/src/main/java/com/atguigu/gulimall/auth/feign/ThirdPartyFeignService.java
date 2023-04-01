package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ClassName: ThirdPartyFeignService
 * Package: com.atguigu.gulimall.auth.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 03:55
 * @Version 1.0
 */
//告訴spring cloud 這個接口是一個遠程客戶端 調用遠程服務
@FeignClient("gulimall-third-party")//這個遠程服務
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}