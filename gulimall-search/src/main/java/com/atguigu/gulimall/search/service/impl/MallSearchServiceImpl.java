package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;

import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;

import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: MallSearchServiceImpl
 * Package: com.atguigu.gulimallsearch.service.impl
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/24 下午 09:14
 * @Version 1.0
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    //去es進行檢索
    @Override
    public SearchResult search(SearchParam param) {
        //1 動態構建出查詢需要的DSL語句
        SearchResult result = null;

        //1 準備檢索請求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            //2 執行檢索請求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3 分析響應數據，封裝成我們需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 準備檢索請求 查詢！
     * 模糊匹配 過濾 按照屬性 分類 品牌 價格區間 庫存 排序 分頁 高亮 聚合分析
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//構建DSL語句

        /**
         * 查詢: 模糊匹配 過濾 按照屬性 分類 品牌 價格區間 庫存
         */
        //1 構建boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //1.2 bool = filter 按照3級分類id查詢
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //1.2 bool = filter 按照品牌id查詢
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //1.2 bool = filter 按照所指的屬性進行查詢
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            for (String attrStr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                //attr = 1_5寸:8寸
                String[] s = attrStr.split("_");
                String attrId = s[0];//檢索的屬性id
                String[] attrValues = s[1].split(":");//檢索的屬性值
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一個都必須生成一個nested查詢
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //1.2 bool = filter 按照是否有庫存進行查詢
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //1.2 bool = filter 按照價格區間進行查詢
        //1_500  _500  500_
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            /**
             *    "range": {
             *        "skuPrice": {
             *           "gte": 0,
             *           "lte": 6000
             *        }
             *     }
             */
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //區間
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    //判斷是否以_為開頭  (_500)
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    //判斷是否以_為結尾  (500_)
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        //把以前的所有條件都拿來進行封裝
        sourceBuilder.query(boolQuery);
        /**
         * 排序 分頁 高亮
         */
        //2.1 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=saleCount_asc/desc 倒序
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //2.2 分頁 每頁5個，pagesize=5
        // pageNum:1 from:0 size:5 [0,1,2,3,4]
        // pageNum:2 from:5 size:5
        // from =(pageNum-1)*size
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }


        /**
         * 聚合分析
         */
        //3.1 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        //TODO 1、聚合brand
        sourceBuilder.aggregation(brand_agg);

        //3.2 分類聚合 catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //分類的子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        //TODO 2、聚合catalog
        sourceBuilder.aggregation(catalog_agg);

        //3.3 屬性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");

        //子聚合 (聚合出當前所有的attrId)
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");

        //子子聚合 2個
        //聚合分析出當前所有attrId對應的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));

        //聚合分析出當前attrid對應的所有可能的屬性值 attrvalue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        //TODO 3、聚合attr
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("構建的DSL" + s);

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * 構建結果數據
     * 根據es查詢到的結果，分析得到頁面真正得到的數據模型
     *
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();

        //1 封裝返回的所有查詢到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                //高亮
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);

        //2 當前所有商品涉及到的所有屬性信息 Aggregation ->
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");

        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到屬性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            //2、得到屬性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //3、得到屬性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3 當前所有商品所涉及的品牌信息 Aggregation ->
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");

        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //1、得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            //2、得到品牌名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            //3、得到品牌圖片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4 當前所有商品所涉及到的所有分類信息 Aggregation ->
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            //得到分類id
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分類名
            //bucket拿到子聚合
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();//因为这个属性不是List
            catalogVo.setCatalogName(catalog_name);

            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
        //5 分頁信息 - 頁碼
        result.setPageNum(param.getPageNum());
        //5 分頁信息 - 總記錄數
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5 分頁信息 - 總頁碼 計算得到 11 / 2 = 5 ... 1  ，有餘數+1
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : (int) (total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //6、構建麵包屑導航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                //1 分析每個attrs傳過來的查詢參數值
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attrs = 2_5寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                //2 取消了麵包屑以後，我們要跳轉到哪個地方，將請求地址的url裡面的當前請求參數置空
                //拿到所有的查詢條件 去掉當前
                //attrs=  9_海思（Hisilicon）

                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(collect);
        }
        //品牌，分類 麵包屑
        if(param.getBrandId() != null && param.getBrandId().size()>0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //TODO 遠程查詢
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brands) {
                    buffer.append(brandVo.getName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        //TODO 分類: 不需要導航取消

        //返回這個大對像給前端
        return result;
    }

    //編寫麵包屑的功能時，刪除指定請求
    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = "";
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            //+ 對應瀏覽器的%20編碼
            encode = encode.replace("+","%20");//瀏覽器對空格編碼和java不一樣
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
