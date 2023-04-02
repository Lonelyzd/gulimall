package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: z_dd
 * @date: 2023/3/31 21:23
 * @Description:
 */
@Data
public class MergeVO {

    /**
     * 整单id
     **/
    private Long purchaseId;

    /**
     * 合并项集合
     **/
    private List<Long> items;
}
