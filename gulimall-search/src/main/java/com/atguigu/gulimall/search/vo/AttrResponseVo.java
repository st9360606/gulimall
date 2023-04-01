package com.atguigu.gulimall.search.vo;

import lombok.Data;

/**
 * ClassName: AttrResponseVo
 * Package: com.atguigu.gulimall.search.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/28 下午 08:41
 * @Version 1.0
 */
@Data
public class AttrResponseVo {

    /**
     * 屬性id
     * 在vo中就不要用跟數據庫有關的註解了
     */
//    @TableId
    private Long attrId;
    /**
     * 屬性名
     */
    private String attrName;
    /**
     * 是否需要檢索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 屬性圖標
     */
    private String icon;
    /**
     * 可選值列表[用逗號分隔]
     */
    private String valueSelect;
    /**
     * 屬性類型[0-銷售屬性，1-基本屬性，2-既是銷售屬性又是基本屬性]
     */
    private Integer attrType;
    /**
     * 啟用狀態[0 - 禁用，1 - 啟用]
     */
    private Long enable;
    /**
     * 所屬分類
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介紹上；0-否 1-是】，在sku中仍然可以調整
     */
    private Integer showDesc;

    private Long attrGroupId;

    private String catelogName;
    private String groupName;

    private Long catelogPath[];
}