package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author: z_dd
 * @date: 2023/4/1 16:12
 * @Description:
 */
@Data
public class PurchaseItemDoneVO {

    private Long itemId;

    private Integer status;

    private String reason;
}
