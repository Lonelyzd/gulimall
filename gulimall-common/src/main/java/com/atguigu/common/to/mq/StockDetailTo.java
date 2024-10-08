package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author : z_dd
 * @date : 2024/3/20 21:42
 **/
@Data
public class StockDetailTo {
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

    private Long wareId;

    private Integer lockStatus;
}
