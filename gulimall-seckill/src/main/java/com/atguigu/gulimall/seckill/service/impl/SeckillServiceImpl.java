package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ClassName: SeckillServiceImpl
 * Package: com.atguigu.gulimall.seckill.service.impl
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:21
 * @Version 1.0
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    //活动信息
    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    //sku信息
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";//多了个:
    //高平发 信号量
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+商品随机码

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1 扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaysSession();
        if (session.getCode() == 0) {
            //上架商品
            List<SeckillSessionsWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //緩存到redis
            //1、緩存活動信息
            saveSessionInfos(sessionData);
            //2、緩存活動的關聯商品信息
            saveSessionSkuInfos(sessionData);
        }


    }


    public List<SeckillSkuRedisTo> blockHandler(BlockException e) {

        log.info("getCurrentSeckillSkusResource资源被限流了.....");
        return null;
    }

    /**
     * 返回當前時間可以參與秒殺的商品信息
     * 被保护资源 try (Entry entry = SphU.entry("seckillSkus")) {
     * 被限流了就调用blockHandler = "blockHandler"方法
     * fallback = ""针对异常的处理
     *
     * @return
     */
    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1 确定当前时间属于哪个秒杀场次
        long time = System.currentTimeMillis();

        //自定義Sentinel受保護的資源
        try (Entry entry = SphU.entry("seckillSkus")) {
            Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys) {
                //seckill:sessions:1594396764000_1594453242000
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");  //得到1594396764000,1594453242000
                //获取存入Redis商品的开始时间
                Long start = Long.parseLong(s[0]);
                //获取存入Redis商品的结束时间
                Long end = Long.parseLong(s[1]);

                //判断是否是当前秒杀场次
                if (time >= start && time <= end) {
                    //2 獲取這個秒殺場次需要的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if (list != null) {
                        List<SeckillSkuRedisTo> collect = list.stream().map(item -> {
                            SeckillSkuRedisTo redis = JSON.parseObject((String) item, SeckillSkuRedisTo.class);//將item 型別轉為  SeckillSkuRedisTo
                            // redisTo.setRandomCode(null); 当前秒杀开始才需要随机码
                            return redis;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        } catch (BlockException e) {
            log.error("資源被限流,{}",e.getMessage());
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //1、找到所有需要參與秒殺商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                //6_4
                boolean matches = Pattern.matches(regx, key);
                //正則匹配上了
                if (matches) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    //随机码需要处理 是不是在秒杀时间内
                    long current = System.currentTimeMillis(); //當前時間
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();
                    if (current >= startTime && current <= endTime) {
                        //在时间范围内部
                    } else {
                        //不在时间内部 随机码置空
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        long s1 = System.currentTimeMillis();
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        //1、獲取當前秒殺商品的詳細信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = hashOps.get(killId);//7_1
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo redis = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //校驗時間的合法性
            Long startTime = redis.getStartTime();
            Long endTime = redis.getEndTime();
            long now = System.currentTimeMillis();

            long ttl = endTime - startTime;

            if (now >= startTime && now <= endTime) {
                //2、校驗隨機碼和商品id
                String randomCode = redis.getRandomCode();
                String skuId = redis.getPromotionSessionId() + "_" + redis.getSkuId();

                if (randomCode.equals(key) && skuId.equals(killId)) {
                    //3、驗證購物數量是否合理
                    if (num <= redis.getSeckillLimit()) {  //購物的數量小於或等於 秒殺限購的數量
                        //4、驗證這個人是否已經購買過了。 冪等性，如果只要秒殺成功，就去佔位。 userId_SessionId_skuId
                        //SETNX
                        String rediskey = respVo.getId() + "_" + skuId;  //userId_SessionId_skuId

                        //自動過期，來佔位，但不是永久佔的要加超時時間ttl。
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(rediskey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //佔位成功說明這個人從未買過
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);//獲取信號量(秒殺總量)

                            boolean b = semaphore.tryAcquire(num);//從信號量取出  : 秒殺總量 - 購買數量
                            if (b) {
                                //秒殺成功;
                                //快速下單，發送MQ消息 10ms
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo orderTo = new SeckillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redis.getPromotionSessionId());
                                orderTo.setSkuId(redis.getSkuId());
                                orderTo.setSeckillPrice(redis.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                long s2 = System.currentTimeMillis();
                                log.info("耗時時間....{}", (s2 - s1));
                                return timeId;
                            }
                            return null;
                        } else {
                            //說明這人已購買過了。
                            return null;
                        }
                    }

                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }


    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessions) {
        if (sessions != null && sessions.size() > 0) {
            sessions.stream().forEach(session -> {
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();
                String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
                Boolean hasKey = redisTemplate.hasKey(key);

                if (!hasKey) {
                    List<String> collect = session.getRelationSkus().stream().map(
                                    item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString())
                            .collect(Collectors.toList());
                    //緩存活動信息
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }


            });
        }
    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> sessions) {
        if (sessions != null && sessions.size() > 0) {
            sessions.stream().forEach(session -> {
                //準備Hash操作
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                    //4 秒杀随机码 : 防止恶意多刷 高并发
                    String token = UUID.randomUUID().toString().replace("-", "");

                    if (!ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString())) {

                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        //緩存商品
                        //1、sku的基本數據
                        R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                        if (skuInfo.getCode() == 0) {
                            SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfo(info);
                        }

                        //2、sku的秒殺信息
                        BeanUtils.copyProperties(seckillSkuVo, redisTo);

                        //3、設置當前商品的秒殺時間信息
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());

                        redisTo.setRandomCode(token);

                        String jsonString = JSON.toJSONString(redisTo);
                        ops.put(seckillSkuVo.getPromotionSessionId().toString() + "_" + seckillSkuVo.getSkuId().toString(), jsonString);

                        /**
                         * 5 商品可以秒杀的数量(库存)作为信号量  信号量的作用 -- 限流
                         * 如果当前这个场次的商品的库存信息已经上架就不需要上架
                         *
                         * 说明：信号量为存储在redis中的一个数字，当这个数字大于0时，即可以调用acquire()方法增加
                         * 数量，也可以调用release()方法减少数量，但是当调用release()之后小于0的话方法就会阻塞，直到数字大于0。
                         */
                        //使用庫存作為分布式的信號量
                        //SKU_STOCK_SEMAPHORE + token 是用于命名分布式 Semaphore 的名称。

                        //得到getSemaphore() 方法可以通过 Redisson 客户端获取一个分布式 Semaphore 对象，开发者可以使用该对象的 API 来控制多个进程或节点对共享资源的访问。

                        //如果當前這個場次的商品的庫存信息已經上架就不需要上架
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);

                        //商品可以秒殺的數量作為信號量  trySetPermits:尝试设置许可数量。
                        semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                    }

                });
            });
        }
    }
}
