package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * ClassName: MemberFeignService
 * Package: com.atguigu.gulimall.order.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 04:16
 * @Version 1.0
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
