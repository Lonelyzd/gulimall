package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: z_dd
 * @date: 2023/8/2 20:31
 * @Description:
 */
@Data
public class SkuItemVo {

    /**
     * SKU基本信息
     **/
    private SkuInfoEntity info;

    /**
     * 是否有货
     **/
    private boolean hasStock = true;

    /**
     * SKU图片信息
     **/
    private List<SkuImagesEntity> images;

    /**
     * SPU销售属性组合
     **/
    private List<SkuItemSaleAttrVo> saleAttr;

    /**
     * SPU介绍
     **/
    private SpuInfoDescEntity desp;

    /**
     * SPU规格参数信息
     **/
    private List<SpuItemGroupAttrVo> groupAttrs;

    /**
     * 当前商品的秒杀优惠信息
     **/
    private  SeckillInfoVo seckillInfo;
}
