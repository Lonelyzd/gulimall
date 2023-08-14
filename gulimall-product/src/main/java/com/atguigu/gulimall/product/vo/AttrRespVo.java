package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author: z_dd
 * @date: 2023/3/16 21:25
 * @Description:
 */
@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;

    private String groupName;

    /**
     * 分类完整路径
     **/
    private Long[] catelogPath;
}
