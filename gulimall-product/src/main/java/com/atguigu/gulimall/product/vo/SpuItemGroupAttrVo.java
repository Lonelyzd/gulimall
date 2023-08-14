package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/** SPU的基本属性
 * @author: z_dd
 * @date: 2023/8/2 21:20
 * @Description:
 */
@Data
public class SpuItemGroupAttrVo {

    /**
     * 属性组名
     **/
    private String groupName;

    private List<SpuBaseAttrVo> attrs;
}
