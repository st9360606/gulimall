package com.atguigu.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * ClassName: SeckillSessionsWithSkus
 * Package: com.atguigu.gulimall.seckill.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/22 上午 01:20
 * @Version 1.0
 */
@Data
public class SeckillSessionsWithSkus {

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuVo> relationSkus;
}
