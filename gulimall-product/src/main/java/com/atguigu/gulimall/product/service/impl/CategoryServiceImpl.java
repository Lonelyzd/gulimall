package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    @Override
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    public List<CategoryEntity> getLevel1Categorys() {
        return this.baseMapper.selectList(Wrappers.<CategoryEntity>lambdaQuery()
                .eq(CategoryEntity::getParentCid, 0));
    }

    @Override
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        final List<CategoryEntity> categoryAllList = this.baseMapper.selectList(null);

        final List<CategoryEntity> level1Categorys = getParent_cid(categoryAllList, 0L);

        final Map<String, List<Catelog2Vo>> collect = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            final List<CategoryEntity> categoryEntities = getParent_cid(categoryAllList, v.getCatId());
            //封装二级分类
            List<Catelog2Vo> catelog2VoList = null;
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                catelog2VoList = categoryEntities.stream().map(lv2 -> {
                    final Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), lv2.getCatId().toString(), lv2.getName(), null);
                    //封装三级分类
                    final List<CategoryEntity> categoryEntities1 = getParent_cid(categoryAllList, lv2.getCatId());
                    if (!CollectionUtils.isEmpty(categoryEntities1)) {
                        final List<Catelog2Vo.Catelog3Vo> collect1 = categoryEntities1.stream().map(lv3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(lv2.getCatId().toString(), lv3.getCatId().toString(), lv3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect1);

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));

        return collect;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryAllList, Long parentCid) {
        return categoryAllList.stream()
                .filter(category -> category.getParentCid().equals(parentCid))
                .collect(Collectors.toList());
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