package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ClassName: IndexController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 下午 08:49
 * @Version 1.0
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        System.out.println(""+Thread.currentThread().getId());
        //TODO 1、查出所有的1級分類
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        //視圖解析器進行拚串
        //classpath:/template/ +返回值+ .html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    //    index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> catelogJson = categoryService.getCatelogJson();
        return catelogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1、獲取一把鎖，只要鎖的名字一樣，就是同一把鎖
        RLock lock = redisson.getLock("my-lock");

        //2、加鎖
//        lock.lock(); //阻塞式等待。默認加的鎖都是30s時間。
        //1)、鎖的自動續期，如果業務超長，運行期間自動給鎖續上新的30s。不用擔心業務時間長，鎖自動過期被刪掉。
        //2） 加鎖的業務只要運行完成，不會給當前鎖續期，即使當機或不手動解鎖，鎖也會在30s以後自動刪除。

        lock.lock(10, TimeUnit.SECONDS);//10秒自動解鎖，自動解鎖時間一定要大於業務的執行時間。
        //問題: lock.lock(10, TimeUnit.SECONDS); 在鎖時間到了以後，不會自動續期。
        //1) 如果我們傳遞了鎖的超時時間，就發送給redis執行腳本進行佔鎖，默認超時時間就是我們指定的時間
        //2) 如果我們沒有指定鎖的超時時間，就使用30*1000 【LockWatchdogTimeout看門狗默認時間】
        //      只要佔鎖成功，就會啟動一個定時任務【重新給鎖設置過期時間，新的過期時間就是看門狗默認時間】每隔10秒都會自動續期，續成30s
        //      internalLockLeaseTime【看門狗時間】/3， 10s
        //最佳實戰
        //1） 推薦lock.lock(30, TimeUnit.SECONDS); 省掉了續期操作。手動解鎖
        try {
            System.out.println("加鎖成功，執行業務..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3、解鎖  假設解鎖代碼沒有運行，redisson會不會出現死鎖
            System.out.println("釋放鎖..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    //保證一定讀到最新數據， 修改期間，寫鎖是一個排他鎖（互斥鎖、獨享鎖）。 讀鎖是一個共享鎖
    //寫鎖沒釋放，讀就必須等待
    // 寫鎖 -》: 讀鎖讀取不到數據 寫鎖完成後 才能讀取到數據 保證數據最新 一致性
    // 讀 + 讀: 相當於無鎖模式 並發讀 只會在redis中記錄好 所有當前的讀鎖，他們都會同時加鎖成功
    // 寫 + 讀: 等待寫鎖釋放
    // 寫 + 寫: 阻塞方式
    // 讀 + 寫: 有讀鎖，寫也需要等待
    // 只要有寫的存在，都需要等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            //1 改數據加寫鎖，讀數據加讀鎖
            rLock.lock();
            System.out.println("寫鎖加鎖成功..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("寫鎖釋放" + Thread.currentThread().getId());
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
//        ReentrantReadWriteLock writeLock = new ReentrantReadWriteLock();
        String s = "";
        //拿讀鎖
        RLock rLock = lock.readLock();
        //加讀鎖
        rLock.lock();
        try {
            System.out.println("讀鎖加鎖成功..." + Thread.currentThread().getId());
            s = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("讀鎖釋放" + Thread.currentThread().getId());
        }
        return s;
    }

    /**
     * 信號量
     * 車庫停車
     * 3 車位
     * 信號量也可以作為分佈式限流
     */

    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire();//獲取一個信號，獲取一個值，占一個車位
        boolean b = park.tryAcquire();
        if (b) {
            //執行業務
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//釋放一個車位

//        Semaphore semaphore = new Semaphore(5);
//        semaphore.release();
//        semaphore.acquire();
        return "ok";
    }

    /**
     * 閉鎖
     * 放假、鎖門
     * 1 班沒人了， 2···
     * 5個班 全部走完 我們才可以佔鎖
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);//等待5個鎖都鎖
        door.await();//等待閉鎖完成 等待鎖的數量為0
        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//計數-1，鎖的數-1 (i--)
//        CountDownLatch
        return id + "班的人都走了....";
    }
}

