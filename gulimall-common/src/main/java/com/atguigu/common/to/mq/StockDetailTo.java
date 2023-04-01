package com.atguigu.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: StockDetailTo
 * Package: com.atguigu.common.to.mq
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/20 上午 12:40
 * @Version 1.0
 */
@Data
public class StockDetailTo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;
}
