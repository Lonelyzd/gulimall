package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/** 检索结果VO
 * @author: z_dd
 * @date: 2023/6/14 21:09
 * @Description:
 */
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    private Integer pageNum;//当前页码

    private Long total;//总记录数

    private Integer totalPages;

    private List<BrandVo> brands; //当前查询到到结果涉及到的所有品牌

    private List<AttrVo> attrs; //当前查询到到结果涉及到的所有属性

    private List<CatalogVo> catalogs; //当前查询到到结果涉及到的所有分类

    private List<Integer> pageNavs;
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

}
