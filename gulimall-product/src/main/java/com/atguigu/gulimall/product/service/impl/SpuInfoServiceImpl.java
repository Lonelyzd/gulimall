package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrService attrService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {
        //1.保存SPU基本信息   pms_spu_info
        SpuInfoEntity spuInfo = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfo);
        spuInfo.setCreateTime(new Date());
        spuInfo.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfo);

        //2.保存SPU的描述图片  pms_spu_info_desc
        final SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        final List<String> decriptList = vo.getDecript();
        spuInfoDescEntity.setSpuId(spuInfo.getId());
        spuInfoDescEntity.setDecript(String.join(",", decriptList));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3.保存SPU的图片集   pms_spu_images
        final List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfo.getId(), images);

        //4.保存SPU规则参数   pms_product_attr_value
        final List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        final List<ProductAttrValueEntity> productAttrValueList = baseAttrs.stream().map(baseAttr -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            entity.setSpuId(spuInfo.getId());
            entity.setAttrId(baseAttr.getAttrId());
            final AttrEntity byId = attrService.getById(baseAttr.getAttrId());
            entity.setAttrName(byId.getAttrName());
            entity.setAttrValue(baseAttr.getAttrValues());
            entity.setQuickShow(baseAttr.getShowDesc());
            return entity;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(productAttrValueList);

        //5.保存SPU的积分信息  sms_spu_bound
        final Bounds bounds = vo.getBounds();
        SpuBoundTo to = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, to);
        to.setSpuId(spuInfo.getId());
        R r = couponFeignService.saveSpuBounds(to);

        if (r.getCode() != 0) {
            log.error("远程保存SPU积分信息失败");
        }


        //6.保存当前SPU对应的所有SKU信息
        //6.1 SKU的基本信息  pms_sku_info
        final List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(item -> {
                //默认图片路径
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfo.getBrandId());
                skuInfoEntity.setCatalogId(spuInfo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfo.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);
                final Long skuId = skuInfoEntity.getSkuId();

                //6.2 SKU的图片信息  pms_sku_images
                final List<SkuImagesEntity> skuImagesList = item.getImages().stream()
                        .map(img -> {
                            final SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(img.getImgUrl());
                            skuImagesEntity.setDefaultImg(img.getDefaultImg());
                            return skuImagesEntity;
                        })
                        .filter(entity -> StringUtils.isNotBlank(entity.getImgUrl()))
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesList);

                //6.3 SKU的销售属性信息    pms_sku_sale_attr_value
                final List<Attr> attrList = item.getAttr();
                final List<SkuSaleAttrValueEntity> skuSaleAttrValueList = attrList.stream().map(attr -> {
                    SkuSaleAttrValueEntity entity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, entity);
                    entity.setSkuId(skuId);
                    return entity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueList);

                //6.4 SKU的优惠、满减等信息  sms_sku_ladder|sms_sku_full_reduction|sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    final R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存SKU优惠信息失败");
                    }
                }


            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfo) {
        this.baseMapper.insert(spuInfo);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {
        final String catelogId = (String) params.get("catelogId");
        final String brandId = (String) params.get("brandId");
        final String status = (String) params.get("status");
        final String key = (String) params.get("key");
        final LambdaQueryWrapper<SpuInfoEntity> wrapper = Wrappers.<SpuInfoEntity>lambdaQuery()
                .eq(StringUtils.isNotBlank(catelogId) && !"0".equals(catelogId), SpuInfoEntity::getCatalogId, catelogId)
                .eq(StringUtils.isNotBlank(brandId) && !"0".equals(brandId), SpuInfoEntity::getBrandId, brandId)
                .eq(StringUtils.isNotBlank(status) , SpuInfoEntity::getPublishStatus, status)
                .and(StringUtils.isNotBlank(key), ew -> ew.eq(SpuInfoEntity::getSpuName, key).or().like(SpuInfoEntity::getSpuName, key));

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}