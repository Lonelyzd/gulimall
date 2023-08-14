package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/** 前端检索参数VO
 * @author: z_dd
 * @date: 2023/6/2 5:56
 * @Description:
 */
@Data
public class SearchParam {
    private String keyword;//页面传递过来的全文匹配关键字

    private Long catalog3Id; //三级分类ID

    /**
     * sort=saleCount_asc/desc  销量排序
     * sort=skuPrice_asc/desc   价格排序
     * sort=hotScore_asc/desc   热度排序
     **/
    private  String sort;

    private Integer hasStock;//是否只显示有货

    private  String skuPrice; //价格区间  1_500;  1_;  _500

    private List<Long> brandId; //按照品牌进行查询，多选

    private List<String> attrs; //按照属性进行筛选

    private  Integer pageNum=1;

    private String _queryString;
}
