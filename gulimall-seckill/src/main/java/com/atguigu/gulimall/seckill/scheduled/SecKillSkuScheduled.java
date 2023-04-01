package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: SecKillSkuScheduled
 * Package: com.atguigu.gulimall.seckill.scheduled
 * Description: 秒殺商品定時上架
 * 每天晚上3:00 ：上架最近三天需要秒杀的商品
 * 当天0点 - 23：59
 * 明天0点 - 23：59
 * 后天0点 - 23：59
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:04
 * @Version 1.0
 */
@Slf4j
@Service
public class SecKillSkuScheduled {
    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String unload_lock = "seckill:upload:lock";

    //秒 分 時 日 月 週
//    Cron表达式有三种：
//       *： 表示所有可能的值，即表示任意一个单元（在秒位就是一秒，在分位就是一分）
//       /： 表示数值的增量，简单地说，比如在分钟为协商0/5，则它的含义就表示为从0分钟开始，每隔5分钟。
//       ?： 仅用在天(月)和天(周)，表示不指定值，当其中一个有值时，另外一个需要设为?

    //TODO 冪等性處理
    @Scheduled(cron = "*/30 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        //1、重複上架無需處理
        log.info("上架秒殺商品的信息.....");

        //加上分布式鎖
        //得到一個分布式鎖命名為 seckill:upload:lock
        //鎖的业务执行完成，状态已经更新完成。释放鎖以後，其他人获取到就会拿到最新的状态(保證整個原子性)。
        RLock lock = redissonClient.getLock(unload_lock);
        lock.lock(10, TimeUnit.SECONDS);//預計下面業務邏輯10秒內就執行完
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }
}
