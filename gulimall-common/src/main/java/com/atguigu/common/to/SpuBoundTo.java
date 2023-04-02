package com.atguigu.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: z_dd
 * @date: 2023/3/26 15:53
 * @Description:
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
