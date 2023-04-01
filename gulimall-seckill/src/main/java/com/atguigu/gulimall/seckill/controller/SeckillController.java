package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: SeckillController
 * Package: com.atguigu.gulimall.seckill.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/23 下午 12:40
 * @Version 1.0
 */
@Slf4j
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回當前時間可以參與秒殺的商品信息
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        log.info("currentSeckillSkus正在執行....");
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {
//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        //判斷是否校驗成功
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
