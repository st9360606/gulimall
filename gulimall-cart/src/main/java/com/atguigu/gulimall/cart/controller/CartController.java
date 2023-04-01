package com.atguigu.gulimall.cart.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: CartController
 * Package: com.atguigu.gulimall.cart.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 04:33
 * @Version 1.0
 */

@Controller
public class CartController {

    @Autowired
    CartService cartService;


    /**
     * 给远程gulimall-order调用
     */
    @ResponseBody
    @GetMapping("/currentUserItems")
    public List<CartItem> currentUserItems() {
        return cartService.getUserCartItems();
    }

    /**
     * 刪除購物車裡的某一項
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 修改購物項目的數量
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {

        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 勾選購物項目
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {

        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 瀏覽器有一個cookie: user-key: 標示用戶身份，一個月後過期
     * 假如是未登入第一次使用購物車功能，都會給一個臨時身份
     * 瀏覽器保存以後，每次訪問都會帶上這個cookie : user-key
     * <p>
     * 登入: session有
     * 沒登入: 按照cookie裡面帶來user-key來做。
     * 第一次: 如果沒有臨時用戶，就幫忙創建一個臨時用戶。
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //1 快速得到用戶信息，id user-key
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 將商品添加到購物車
     * 防止用戶惡意刷新，可以使用重定向的辦法，本方法不跳轉頁面，只是執行完業務代碼後，跳轉到addToCartSuccessPage方法，讓那個方法跳轉頁面
     * redirectAttributes.addFlashAttribute() 將數據保存在session裡面可以在頁面取出，但是只能取一次
     * redirectAttributes.addAttribute("skuId", skuId); 將數據放在URL後面
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        //業務代碼
        cartService.addTocart(skuId, num);
        //將數據放在URL後面
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 跳轉到成功頁
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功頁面，再次從Redis查詢購物車數據即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
