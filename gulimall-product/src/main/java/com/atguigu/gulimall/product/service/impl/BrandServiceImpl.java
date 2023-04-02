package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.BrandDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String key = (String) params.get("key");
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>().lambda()
                        .and(StringUtils.isNoneBlank(key), ew -> ew
                                .eq(BrandEntity::getBrandId, key)
                                .or()
                                .like(BrandEntity::getName, key)
                        )
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand) {
        this.baseMapper.updateById(brand);
        final String name = brand.getName();
        if (StringUtils.isNotEmpty(name)) {
            //品牌名称不为空，则需要同步更新关联表的冗余字段
            categoryBrandRelationService.update(Wrappers.<CategoryBrandRelationEntity>lambdaUpdate()
                    .set(CategoryBrandRelationEntity::getBrandName, name)
                    .eq(CategoryBrandRelationEntity::getBrandId, brand.getBrandId()));
        }
    }

}