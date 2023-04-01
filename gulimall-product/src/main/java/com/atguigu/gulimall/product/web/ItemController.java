package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * ClassName: ItemController
 * Package: com.atguigu.gulimall.product.web
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/1 下午 08:57
 * @Version 1.0
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 展示當前sku的詳情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("準備查詢 " + skuId+" 詳情");
        SkuItemVo vo = skuInfoService.item(skuId);
        model.addAttribute("item", vo);
        return "item";
    }
}
