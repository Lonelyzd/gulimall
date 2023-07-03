package com.atguigu.common.to;

import lombok.Data;

/**
 * SKU是否有库存
 *
 * @author: z_dd
 * @date: 2023/4/16 11:26
 * @Description:
 */
@Data
public class SkuHasStockVo {

    private Long skuId;
    private Boolean hasStock;
}
