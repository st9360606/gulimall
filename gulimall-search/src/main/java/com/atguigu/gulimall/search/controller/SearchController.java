package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: SearchController
 * Package: com.atguigu.gulimallsearch.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/24 上午 01:56
 * @Version 1.0
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自動將頁面提交過來的所有請求查詢參數封裝成指定的對象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        param.set_queryString(request.getQueryString());
        //1、 根據傳遞來的頁面的查詢參數，去ES中解鎖商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
