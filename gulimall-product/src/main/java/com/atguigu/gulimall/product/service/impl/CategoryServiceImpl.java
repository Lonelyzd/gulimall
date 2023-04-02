package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        final List<CategoryEntity> all = baseMapper.selectList(null);

        //组装成父子的树形结构
        final List<CategoryEntity> level1 = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0l)
                .map(categoryEntity -> {
                    final List<CategoryEntity> childrens = getChildrens(categoryEntity, all);
                    categoryEntity.setChildren(childrens);
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());


        return level1;
    }

    @Override
    public void removeMenuByIds(List<Long> catIds) {
        // todo 1. 检查当前删除的菜单，是否被别的地方引用

        baseMapper.deleteBatchIds(catIds);

    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> catelogPath = new ArrayList<>();
        findeCatelogPath(catelogPath, catelogId);
        Collections.reverse(catelogPath);
        return catelogPath.toArray(new Long[catelogPath.size()]);
    }

    @Override
    @Transactional
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);
        final String name = category.getName();
        if (StringUtils.isNotEmpty(name)) {
            //分类名称不为空，则需要同步更新关联表的冗余字段
            categoryBrandRelationService.update(Wrappers.<CategoryBrandRelationEntity>lambdaUpdate()
                    .set(CategoryBrandRelationEntity::getCatelogName, name)
                    .eq(CategoryBrandRelationEntity::getCatelogId, category.getCatId()));
        }
    }

    private void findeCatelogPath(List<Long> catelogPath, Long catelogId) {
        final CategoryEntity categoryEntity = this.getById(catelogId);
        catelogPath.add(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findeCatelogPath(catelogPath, categoryEntity.getParentCid());
        }
    }


    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        final List<CategoryEntity> collect = all.stream().filter(categoryEntity -> categoryEntity.getParentCid().longValue() == root.getCatId().longValue())
                .map(categoryEntity -> {
                    final List<CategoryEntity> childrens = getChildrens(categoryEntity, all);
                    categoryEntity.setChildren(childrens);
                    return categoryEntity;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                .collect(Collectors.toList());
        return collect;
    }


}