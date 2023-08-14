package com.atguigu.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/** SKU的销售属性
 * @author: z_dd
 * @date: 2023/8/2 21:17
 * @Description:
 */
@Data
public class SkuItemSaleAttrVo {

    private Long attrId;

    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;
}
