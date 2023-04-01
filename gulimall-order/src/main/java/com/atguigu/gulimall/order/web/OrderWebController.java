package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: OrderWebController
 * Package: com.atguigu.gulimall.order.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 03:23
 * @Version 1.0
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        //展示訂單確認的數據
        return "confirm";
    }

    /**
     * 下單功能
     *
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        try {
        SubmitOrderResponseVo responseVo =orderService.submitOrder(vo);

        System.out.println("訂單提交的數據...."+vo);
        if(responseVo.getCode()==0){
            //下單成功來到支付選擇頁
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }else {
            String msg = "下單失敗";
            switch (responseVo.getCode()){
                case 1: msg+="訂單信息過期，請刷新再次提交";break;
                case 2: msg+="訂單商品價格發生變化，請確認後再次提交";break;
                case 3: msg+="庫存鎖定失敗，商品庫存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            //下單失敗回到訂單確認頁重新確認訂單信息
            return "redirect:http://order.gulimall.com/toTrade";
        }
        } catch (Exception e) {
            if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg", message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
