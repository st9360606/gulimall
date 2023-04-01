package com.atguigu.gulimall.member.web;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;

import com.atguigu.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MemberWebController
 * Package: com.atguigu.gulimall.member.web
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/21 上午 02:09
 * @Version 1.0
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 我的订单
     * @RequestParam : 分頁數 默認第1頁
     */
    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                  Model model) {

        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        System.out.println(JSON.toJSONString(r));
        model.addAttribute("orders", r);
        return "orderList";
    }


}