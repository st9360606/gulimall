package com.atguigu.common.exception;

/**
 * ClassName: NoStockException
 * Package: com.atguigu.gulimall.ware.exception
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/18 下午 03:09
 * @Version 1.0
 */
public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id : " + skuId + ": 沒有足夠的庫存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public NoStockException(String msg) {
        super(msg);
    }
}
