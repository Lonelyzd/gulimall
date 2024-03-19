package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author : z_dd
 * @date : 2024/3/2 21:33
 **/
@Data
public class LockStockResult {

    private Long skuId;

    private Integer num;

    private Boolean locked;
}
