package com.atguigu.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: StockLockedTo
 * Package: com.atguigu.common.to.mq
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/20 上午 12:39
 * @Version 1.0
 */
@Data
public class StockLockedTo implements Serializable {
    private static final long serialVersionUID = 1L;

    //库存工作单id
    private Long id;

    //工作单详情的所有id
    private StockDetailTo detail;
}