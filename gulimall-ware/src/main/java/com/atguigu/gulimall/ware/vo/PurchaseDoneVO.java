package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: z_dd
 * @date: 2023/4/1 16:10
 * @Description:
 */
@Data
public class PurchaseDoneVO {

    @NotNull
    private Long id;    //采购单id

    private List<PurchaseItemDoneVO> items;

}
