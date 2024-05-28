package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SeckillInfoVo;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemGroupAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageCondition(Map<String, Object> params) {

        //todo
        //key: '华为',//检索关键字
        //catelogId: 0,
        //brandId: 0,
        //min: 0,
        //max: 0

        try {
            final String catelogId = (String) params.get("catelogId");
            final String brandId = (String) params.get("brandId");
            final String min = (String) params.get("min");
            final String max = (String) params.get("max");
            final String key = (String) params.get("key");
            final BigDecimal maxDecimal = new BigDecimal(max);
            final LambdaQueryWrapper<SkuInfoEntity> wrapper = Wrappers
                    .<SkuInfoEntity>lambdaQuery()
                    .eq(StringUtils.isNotBlank(catelogId) && !"0".equals(catelogId), SkuInfoEntity::getCatalogId, catelogId)
                    .eq(StringUtils.isNotBlank(brandId) && !"0".equals(brandId), SkuInfoEntity::getBrandId, brandId)
                    .ge(SkuInfoEntity::getPrice, min)
                    .le(StringUtils.isNotBlank(max) && maxDecimal.compareTo(new BigDecimal("0")) == 1, SkuInfoEntity::getPrice, max)
                    .and(StringUtils.isNotBlank(key), ew -> ew.eq(SkuInfoEntity::getSkuName, key).or().like(SkuInfoEntity::getSkuName, key));


            IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(Wrappers.<SkuInfoEntity>lambdaQuery()
                .eq(SkuInfoEntity::getSpuId, spuId)
        );
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo vo = new SkuItemVo();

        final CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.SKU基本信息获取
            final SkuInfoEntity info = this.getById(skuId);
            vo.setInfo(info);
            return info;
        }, executor);


        final CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3.SPU的销售组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVo = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            vo.setSaleAttr(skuItemSaleAttrVo);
        }, executor);

        final CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4.获取SPU的介绍
            log.info("444");
            final SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            vo.setDesp(spuInfoDesc);
        }, executor);

        final CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5.SPU规格参数信息
            List<SpuItemGroupAttrVo> spuItemGroupAttrVo = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            vo.setGroupAttrs(spuItemGroupAttrVo);
        }, executor);


        final CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2.SKU图片信息
            final List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            vo.setImages(images);
        }, executor);

        final CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {

            //6.查询当前Sku是否参与秒杀优惠
            final R r = seckillFeignService.getSkuSeckillInfo(skuId);

            if (r.getCode() == 0) {
                final SeckillInfoVo data = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                vo.setSeckillInfo(data);
            }
        }, executor);


        //等待异步任务都完成
        try {
            CompletableFuture.allOf(saleAttrFuture, descFuture, baseAttrFuture, imageFuture, seckillFuture).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return vo;
    }

}