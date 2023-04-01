package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * ClassName: SeckillService
 * Package: com.atguigu.gulimall.seckill.service
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:21
 * @Version 1.0
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
