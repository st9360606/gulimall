package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: SeckillFeignServiceFallBack
 * Package: com.atguigu.gulimall.product.feign.fallback
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/24 下午 02:22
 * @Version 1.0
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSkuSeckillInfo(Long skuId) {
        log.info("熔斷方法調用....getSkuSeckillInfo");
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMessage());
    }
}
