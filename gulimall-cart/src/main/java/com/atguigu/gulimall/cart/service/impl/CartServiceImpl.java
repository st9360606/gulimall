package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * ClassName: CartServiceImpl
 * Package: com.atguigu.gulimall.cart.service.impl
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 04:28
 * @Version 1.0
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    //購物車前綴
    public final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addTocart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        //獲取到我們要操作的購物車
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(res)) {
            //代表購物車無此商品
            //說明購物車沒有此商品，新增商品類型
            //2 添加新商品到購物車
            //1 遠程查詢當前要操作的商品信息 獲得真正的sku商品信息 並封裝
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                //遠程調用方法取出商品所有信息
                R skuInfo = productFeignService.getSkuInfo(skuId);
                SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(data.getPrice());
            }, executor);


            //3 遠程查詢sku屬性的組合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);
            //等 getSkuInfoTask,getSkuSaleAttrValues 兩個方法都完成，再給redis裡面放數據
            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);

            return cartItem;
        } else {
            //購物車有此商品，修改數量
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }


    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    /**
     * 跳到購物車列表，並檢測是否需要合併
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //1、已登入
            //假如他臨時購物車有數據，要把臨時購物車的數據合併到在線購物車的數據上
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //2、如果臨時購物車的數據還沒有進行合併『 合併購物車 』。
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            //查看臨時購物車是否有數據
            if (tempCartItems != null && tempCartItems.size() > 0) {
                //臨時購物車有數據，需要合併
                for (CartItem tempCartItem : tempCartItems) {
                    addTocart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //清除臨時購物車的數據
                clearCart(tempCartKey);

            }

            //3、再來獲取登入後購物車的數據『 包含合併過來的臨時購物車的數據，和登入後購物車的數據 』。
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            //2、沒登入
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //獲取臨時購物車的所有購物項
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }
        return cart;
    }

    /**
     * 獲取到我們要操作的購物車
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;
        if (userInfoTo.getUserId() != null) {
            //說明用戶登錄了，Redis存入帶UserId gulimall:cart:11
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //說明是臨時用戶，Redis存入帶uuid gulimall:cart:fg1argadr3gdab3dgfsr41ag
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //讓Redis全部操作 cartKey 這個key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        return operations;
    }

    /**
     * 獲取臨時購物車的所有購物項
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        //從Redis拿數據
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 清空購物車
     */
    @Override
    public void clearCart(String cartKey) {

        redisTemplate.delete(cartKey);
    }

    /**
     * 勾選購物項目
     */
    @Override
    public void checkItem(Long skuId, Integer check) {

        //獲取到我們要操作的購物車
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //獲取購物車的一個購物項
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    /**
     * 修改購物項目的數量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {

        //獲取到我們要操作的購物車
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //獲取購物車的一個購物項
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    /**
     * 刪除購物車裡的某一項
     */
    @Override
    public void deleteItem(Long skuId) {

        //獲取到我們要操作的購物車
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            //獲取所有被選中的購物項
            List<CartItem> collect = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item->{
                        R price = productFeignService.getPrice(item.getSkuId());
                        //TODO 1、更新為最新價格

                        String data = (String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
    }


}
