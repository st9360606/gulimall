package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: SearchResult
 * Package: com.atguigu.gulimallsearch.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/25 上午 10:07
 * @Version 1.0
 */

@Data
public class SearchResult {
    //查詢到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 以下是分頁信息
     */
    private Integer pageNum;//當前頁碼
    private Long total;//總記錄數
    private Integer totalPages;//總頁碼數
    private List<Integer> pageNavs; //導航頁碼

    private List<BrandVo> brands;//當前查到的結果，所有涉及到的品牌
    private List<CatalogVo> catalogs;//當前查到的結果，所有涉及到的分類
    private List<AttrVo> attrs;//當前查詢到的結果，所涉及到的所有屬性

    //麵包屑導航數據
    private List<NavVo> navs = new ArrayList<>();

    private List<Long> attrIds = new ArrayList<>();

    //=================以上是返回給頁面的所有信息=====================

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    //麵包屑導航數據
    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

}
