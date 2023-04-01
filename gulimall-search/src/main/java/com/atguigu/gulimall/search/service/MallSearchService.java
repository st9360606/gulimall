package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * ClassName: MallSearchService
 * Package: com.atguigu.gulimallsearch.service
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/24 下午 09:14
 * @Version 1.0
 */

public interface MallSearchService {
    /**
     *
     * @param param  檢索的所有參數
     * @return    返回檢索的結果,裡面包含頁面需要的所有信息
     */
    SearchResult search(SearchParam param);
}
