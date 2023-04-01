package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * ClassName: SkuItemVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/1 下午 09:27
 * @Version 1.0
 */
@Data
public class SkuItemVo {

    //1 sku基本信息獲取 pms_sku_info
    SkuInfoEntity info;

    //是否有貨
    boolean hasStock = true;

    //2 sku圖片信息 pms_sku_images
    List<SkuImagesEntity> images;

    //3 spu的銷售屬性組合
    List<SkuItemSaleAttrVo> saleAttr;

    //4 spu的介紹
    SpuInfoDescEntity desc;

    //5 spu的規格參數信息
    private List<SpuItemAttrGroupVo> groupAttrs;

    //6 當前商品秒殺的優惠信息
    SeckillInfoVo seckillInfo;

}