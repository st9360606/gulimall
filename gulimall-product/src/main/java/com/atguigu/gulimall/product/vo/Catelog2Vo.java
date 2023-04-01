package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName: Catelog2Vo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/16 下午 10:18
 * @Version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo implements Serializable {
    private String catelog1Id;  //1級父分類id
    private List<Catalog3Vo> catalog3List; //三級子分類
    private String id;
    private String name;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id;//父分类 2级分类id
        private String id;
        private String name;
    }

}
