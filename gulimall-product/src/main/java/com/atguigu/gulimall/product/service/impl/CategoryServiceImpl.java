package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


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