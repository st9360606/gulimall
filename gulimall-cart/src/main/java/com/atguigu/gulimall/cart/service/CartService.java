package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: CartService
 * Package: com.atguigu.gulimall.cart.service
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 04:26
 * @Version 1.0
 */
public interface CartService {
    /**
     * 將商品添加到購物車
     * @param skuId
     * @param num
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addTocart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 獲取購物車中某個購物項
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 獲取整個購物車
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空購物車數據
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾選購物項
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改購物項數量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 刪除購物項
     * @param skuId
     */
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
