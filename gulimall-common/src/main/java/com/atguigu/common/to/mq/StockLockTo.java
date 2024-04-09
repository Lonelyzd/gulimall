package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * @author : z_dd
 * @date : 2024/3/20 21:35
 **/
@Data
public class StockLockTo {
    private Long id;

    private StockDetailTo detail;

}
