package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * ClassName: SearchParam
 * Package: com.atguigu.gulimallsearch.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/24 下午 09:11
 * @Version 1.0
 *
 * 封裝頁面所有可能傳遞過來的查詢條件
 * catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0/1&brandId=1&brandId=2
 */
@Data
public class SearchParam {

    private String keyword;//頁面傳遞過來的檢索參數 相當於全文匹配關鍵字
    private Long catalog3Id;//三級分類id

    /**
     * 排序條件
     *  sort=saleCount_asc/desc 倒序
     *  sort=skuPrice_asc/desc 根據價格
     *  sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * hasStock(是否有貨) skuPrice區間 brandId catalog3Id attrs
     * hasStock 0/1
     * skuPrice= 1_500 / 500_ / _500  三種篩選規則
     * brandId = 1
     * attrs= 2_5存:6寸
     * // 0 無庫存 1有庫存
     */
    private Integer hasStock;   //是否只顯示有貨  0(無庫存) 1(有庫存)

    /**
     * 價格區查詢
     */
    private String skuPrice;

    /**
     * 按照品牌進行查詢，可以多選
     * 多個品牌id
     */
    private List<Long> brandId;

    /**
     * 按照屬性進行篩選
     */
    private List<String> attrs;

    /**
     * 頁碼
     */
    private Integer pageNum = 1;

    /**
     * 原生所有的查詢條件
     */
    private String _queryString;
}
