package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //本地緩存
//    private Map<String,Object> cache = new HashMap<>();

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1. 查出所有分類
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2. 組裝成父子的樹形結構

        //2.1 找到所有的一級分類
        //使用stream Api ，filter過濾器
        //categoryEntity : 可以任意命名，類似變數
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                        //getParentCid 取得父分類ID，若等於0代表一級分類
                        categoryEntity.getParentCid() == 0
                ).map((menu) -> {
                    //保存當前菜單的子分類
                    //遞歸方式
                    menu.setChildren(getChildrens(menu, entities));
                    //返回當前菜單
                    return menu;
                }).sorted((menu1, menu2) -> {
                    //升降排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1. 檢查當前刪除的菜單，是否被別的地方引用

        //邏輯刪除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        //轉成Long 數組
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 級聯更新所有關聯的數劇
     * @CacheEvict :失效模式
     * 1. 同時進行多種緩存操作 @Caching
     * 2. 指定刪除某個分區下的所有數據 @CacheEvict(value = "category",allEntries = true)
     * 3. 存儲同一類型的數據，都可以指定成同一個分區。 分區名默認就是緩存的前綴
     * @param category
     */

//    @Caching(evict = {
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key = "'getCatelogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)  //失效模式
    //@CachePut //雙寫模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        //同时修改缓存中的数据
        //redis.del(catalogJSON);等待下次主动查询进行更新
    }

    /**
     * 1. 每一個需要緩存的數據我們都要來指定要放在哪個名字的緩存裡【緩存的分區(按照業務類型區分)】
     * 2. @Cacheable({"category"})
     *    代表當前方法的結果需要緩存，如果緩存中有，方法不用調用，如果緩存中沒有，就會調用方法，最後將方法的結果放入緩存。
     * 3. 默認行為
     *   1）、如果緩存中有 方法不用調用
     *   2）、 key默認自動生成 緩存的名字::simplekey[](自動生成的key值)
     *   3）、緩存value的值 默認使用Java序列化機制 將序列化後的數據存到redis
     *   4）、默認過期時間ttl為: -1
     *
     *  自定義:
     *   1）、指定生成的緩存使用的key:  key的屬性指定,接受一個SpEL表達式
     *       SpEL的詳細:https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-spel-context
     *       # Available Caching SpEL Evaluation Context
     *   2)、 指定緩存的數據的存活時間:  配置文件中修改ttl
     *   3）、將數據保存為json格式
     *        sync = true解決緩存擊穿 默認是false
     *
     * 4、Spring-Cache的不足之處：
     *  1）、讀模式
     *      緩存穿透：查詢一個null數據。   解決方案：緩存空數據
     *      緩存擊穿：大量並發進來同時查詢一個正好過期的數據。       解決方案：加鎖 ? 默認是無加鎖的;使用sync = true來解決擊穿問題
     *      緩存雪崩：大量的key同時過期。        解決：加隨機時間。加上過期時間
     *  2)、寫模式：（緩存與數據庫一致）
     *      1）、讀寫加鎖。
     *      2）、引入Canal,感知到MySQL的更新去更新Redis緩存
     *      3）、讀多寫多，直接去數據庫查詢就行
     *  總結：
     *      常規數據（讀多寫少，即時性，一致性要求不高的數據，完全可以使用Spring-Cache）：寫模式(只要緩存的數據有過期時間就足夠了)
     *      特殊數據：特殊設計
     *  原理：
     *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache負責緩存的讀寫
     *
     * @return
     */



    //代表當前方法的結果需要緩存，如果緩存中有，方法不用調用，如果緩存中沒有，就會調用方法，最後將方法的結果放入緩存。
    @Cacheable(value = {"category"}, key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys...");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗時間: " + (System.currentTimeMillis() - l));
        return categoryEntities;
//        return null;//測試緩存空值
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("查詢了數據庫.....");

        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //2.封裝數據
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1. 每一個的一級分類，查到這個一級分類的二級分類
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2. 封裝上面的結果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1. 找當前二級分類的三級分類封裝成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2. 封裝成指定格式
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    //TODO 产生堆外内存溢出：OutOfDirectMemoryError
    //1)、Springboot2.0以后默认使用Lettuce作为操作redis的客户端。它使用netty进行网络通信。
    //2), Lettuce的bug导致netty堆外内存溢出 -Xmx300m; netty如果没有指定堆外内存，默认使用Xmx300m
    //      可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案  不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
    //        1)、升级Lettuce客户端。 2),切换使用jedis
    // redisTemplate:
    // Lettuce、jedis都是操作redis的底層客戶端。spring再次封裝redisTemplate:
    //

//    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        //給緩存中放json字符串，拿出的json字符串，還用逆轉為能用的對象類型:【序列化與反序列化】


        /**
         * 1、空结果缓存:解决缓存穿透
         * 2、设置过期时间(加随机值):解决缓存雪崩
         * 3、加锁;解决缓存击穿
         */


        //1. 加入緩存邏輯，緩存中存的數據是json字符串。
        //JSON跨語言，跨平台的兼容
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            //2. 緩存中沒有，查詢數據庫
            System.out.println("緩存不命中....將要查詢數據庫....");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedisLock();
            return catelogJsonFromDb;
        }
        System.out.println("緩存命中....直接返回....");
        //轉為我們指定的對象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * 使用分佈式鎖
     * 從數據庫查詢並封裝分類數據
     * <p>
     * 緩存一致性問題:
     * 緩存裡面的數據如何和數據庫裡面的數據保持一致？
     * 1） 雙寫模式 數據庫改完後，緩存也改，不然容易產生髒數據
     * 2） 失效模式 數據庫改完後，把緩存刪掉
     * <p>
     * 緩存數據一致性-解決方案
     * 無論是雙寫模式還是失效模式,都會導致緩存的不一致問題,即多個實例同時更新會出事,怎麼辦?
     * 1、如果是用戶緯度數據(訂單數據、用戶數據),這種並發機率非常小,不用考慮這個問題,緩存數據加上過期時間,每隔一段時間觸發讀的主動更新即可
     * 2、如果是菜單,商品介紹等基礎數據,也可以去使用canal訂閱binlog的方式。
     * 3、緩存數據+過期時間也足夠解決大部分業務對於緩存的要求。
     * 4、通過加效保證並發讀寫,寫寫的時候按順序排好隊,讀讀無所謂,所以適合使用讀寫鎖,(業務不關心臟數據,允許臨時臟數據可忽略);
     * 總結。
     * 我們能放入緩存的數據本就不應該是實時性、一致性要求超高的,所以緩存數據的時候加上過期時間,保證每天拿到當前最新數據即可,
     * 我們不應該過度設計,增加系統的複雜性
     * 遇到實時性、一致性要求高的數據,就應該查數據庫,即使慢點。
     * <p>
     * 我們系統的一致性解決方案:
     * 1、緩存的所有數據都有過期時間,數據過期下一次查詢觸發主動更新
     * 2、讀寫敵據的時候,加上分佈式的讀寫鎖。
     * 經常寫,經常讀
     */
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {
        //1. 鎖的名字，鎖的粒度，越細越快。
        //鎖的粒度: 具體緩存的是某個數據，11-號商品: product-11-lock product-12-lock
        RLock lock = redisson.getLock("catelogJson-lock");
        //加鎖
        lock.lock();


        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
          lock.unlock();
        }


        return dataFromDb;


    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {
        //1. 抢占分布式锁 去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("獲取分佈式鎖成功......");
            //加鎖成功...執行業務
            //2. 設置過期時間，必須和加鎖是同步的，原子的
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1]  then return redis.call('del',KEYS[1]) else return 0 end";
                //刪除鎖
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            //獲取值對比 + 對比成功刪除 = 原子操作  lua腳本解鎖
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)){
//                //刪除我自己的鎖
//                redisTemplate.delete("lock");
//            }

            return dataFromDb;
        } else {
            //加鎖失敗...重試。synchronized()
            //休眠200ms重試
            System.out.println("獲取分佈式鎖失敗....等待重試...");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDbWithRedisLock();//自旋的方式
        }

    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catelogJSON)) {
            //緩存不為null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查詢了數據庫.....");

        List<CategoryEntity> selectList = baseMapper.selectList(null);


        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //2.封裝數據
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1. 每一個的一級分類，查到這個一級分類的二級分類
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2. 封裝上面的結果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1. 找當前二級分類的三級分類封裝成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2. 封裝成指定格式
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        //3. 查到的數據再放入緩存，為了下次更快獲取到，將對象轉為json放入緩存中
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catelogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }


    //從數據庫查詢並封裝分類數據
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock() {
//        //1. 如果緩存中有就用緩存的
//        Map<String, List<Catelog2Vo>> catelogJson = (Map<String, List<Catelog2Vo>>) cache.get("catelogJson");
//        if(cache.get("catelogJson")==null){
//            //調用業務
//            //返回數據又放入緩存
//            cache.put("catelogJson",parent_cid);
//        }
//        return catelogJson;

        //加锁 只要是同一把锁，就能锁住，需要这一把锁的所有线程
        //synchronized (this) {springBoot所有的组件，在容器中都是单例的。
        //TODO 本地锁 synchronized JUC(Lock), 在分布式情况下，想要锁住所有，必须使用分布式锁

        synchronized (this) {
            //得到锁以后,我们应该再去缓存中确定一次，如果没有才需要继续查询。
            return getDataFromDb();
        }


    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parent_cid)).collect(Collectors.toList());
        return collect;
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }


    //225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            //找到父節點，用paths收集數據，直到沒有父節點
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;

    }

    //遞歸查找所有菜單的子菜單
    //root 當前菜單
    //all  所有菜單
    //categoryEntity : 可以任意命名，類似變數
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
                    return categoryEntity.getParentCid().equals(root.getCatId());
                }).map(categoryEntity -> {
                    //1.找到子菜單
                    //categoryEntity : 當前菜單
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                    return categoryEntity;
                }).sorted((menu1, menu2) -> {
                    //2.菜單的排序
                    //升降排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                })
                .collect(Collectors.toList());
        return children;
    }

}