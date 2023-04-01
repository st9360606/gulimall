package com.atguigu.gulimall.product;


import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;


import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * 1、引入oss-starter
 * 2、配置key，endpoint相关信息即可
 * 3、使用OSSClient 进行相关操作
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimallProductApplication.class)
public class GulimallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void testgetSaleAttrBySpuId(){
        List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(5L);
        System.out.println(saleAttrBySpuId);
    }

    @Test
    public void testgetAttrGroupWithAttrsBySpuId(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(5L, 225L);
        System.out.println(attrGroupWithAttrsBySpuId);
    }

    @Test
    public void redisson(){
        System.out.println(redissonClient);
    }


    @Test
    public void teststringRedisTemplate() {
        //hello world
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        ops.set("hello", "world_" + UUID.randomUUID().toString());

        //查詢
        String hello = ops.get("hello");
        System.out.println("之前保存的數據是: " + hello);
    }

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路徑: {}", Arrays.asList(catelogPath));
    }

    @Test
    public void contextLoads() {
//        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("華為");

//        brandEntity.setName("apple");
//        brandService.save(brandEntity);
//        System.out.println("保存成功...");
//        brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        list.forEach((item) -> {
            System.out.println(item);
        });
    }

}
