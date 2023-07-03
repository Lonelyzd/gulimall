package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
                .eq(SkuInfoEntity::getSpuId,spuId)
        );
    }

}