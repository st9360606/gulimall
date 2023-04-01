package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * ClassName: ProductSaveService
 * Package: com.atguigu.gulimallsearch.service
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 上午 10:57
 * @Version 1.0
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;


}
