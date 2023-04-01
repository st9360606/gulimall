package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * ClassName: AttrRespVo
 * Package: com.atguigu.gulimall.product.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/6 下午 09:33
 * @Version 1.0
 */
@Data
public class AttrRespVo extends AttrVo {
    /**
     * 			"catelogName": "手机/数码/手机", //所属分类名字
     * 			"groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
