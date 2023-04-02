package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrResponse;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVo vo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.baseMapper.insert(attrEntity);

        if (vo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //保存关联信息
            final AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> param, Long catelogId, String attrType) {
        final String key = (String) param.get("key");
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(param),
                Wrappers.<AttrEntity>lambdaQuery()
                        .eq(!catelogId.equals(0L), AttrEntity::getCatelogId, catelogId)
                        .eq(AttrEntity::getAttrType, "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode())
                        .and(StringUtils.isNoneBlank(key),
                                ew -> ew.eq(AttrEntity::getAttrId, key)
                                        .or()
                                        .like(AttrEntity::getAttrName, key)
                        )

        );

        final PageUtils pageUtils = new PageUtils(page);
        final List<AttrEntity> list = pageUtils.getList();
        final List<AttrResponse> res = list.stream().map((attr) -> {
            final AttrResponse attrResponse = new AttrResponse();
            BeanUtils.copyProperties(attr, attrResponse);


            if ("base".equalsIgnoreCase(attrType)) {
                //填充分组名称
                final AttrAttrgroupRelationEntity attrId = attrAttrgroupRelationService.getOne(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
                if (attrId != null&&attrId.getAttrGroupId()!=null) {
                    final AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
                    attrResponse.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            //填充分类名称
            final CategoryEntity categoryEntity = categoryDao.selectById(attr.getCatelogId());
            if (categoryEntity != null) {
                attrResponse.setCatelogName(categoryEntity.getName());
            }

            return attrResponse;
        }).collect(Collectors.toList());
        pageUtils.setList(res);
        return pageUtils;
    }

    @Override
    public AttrResponse getAttrInfo(Long attrId) {
        final AttrEntity attr = this.baseMapper.selectById(attrId);
        AttrResponse response = new AttrResponse();
        BeanUtils.copyProperties(attr, response);
        //设置分类路径
        final Long[] catelogPath = categoryService.findCatelogPath(attr.getCatelogId());
        response.setCatelogPath(catelogPath);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //设置分组信息
            final AttrAttrgroupRelationEntity one = attrAttrgroupRelationService.getOne(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
            if (null != one) {
                response.setAttrGroupId(one.getAttrGroupId());
            }
        }

        return response;
    }

    @Override
    @Transactional
    public void updateAttr(AttrVo attr) {
        final AttrEntity target = new AttrEntity();
        BeanUtils.copyProperties(attr, target);
        this.baseMapper.updateById(target);

        if (target.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //  修改分组关联
            final int count = attrAttrgroupRelationService.count(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                    .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationService.update(Wrappers.<AttrAttrgroupRelationEntity>lambdaUpdate()
                        .set(AttrAttrgroupRelationEntity::getAttrGroupId, attr.getAttrGroupId())
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId())
                );
            } else {
                AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                relationEntity.setAttrId(attr.getAttrId());
                relationEntity.setAttrGroupId(attr.getAttrGroupId());
                attrAttrgroupRelationService.save(relationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {

        final List<AttrAttrgroupRelationEntity> relationList = attrAttrgroupRelationService.list(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));
        final Set<Long> attrIdSet = relationList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());
//        final List<AttrEntity> attrEntities = this.baseMapper.selectList(Wrappers.<AttrEntity>lambdaQuery()
//                .in(AttrEntity::getAttrId, attrIdSet));
        if (!CollectionUtils.isEmpty(attrIdSet)) {
            final List<AttrEntity> attrEntities = this.listByIds(attrIdSet);
            return attrEntities;
        }
        return null;
    }

    @Override
    public void deleteRelation(List<AttrGroupRelationVo> vos) {
        vos.forEach(vo -> attrAttrgroupRelationService.remove(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                .eq(AttrAttrgroupRelationEntity::getAttrId, vo.getAttrId())
                .eq(AttrAttrgroupRelationEntity::getAttrGroupId, vo.getAttrGroupId())));
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        final String key = (String) params.get("key");
        //获取当前属性分组信息
        final AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        //获取当前分类的所有分组信息
        final List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(Wrappers.<AttrGroupEntity>lambdaQuery()
                .eq(AttrGroupEntity::getCatelogId, attrGroupEntity.getCatelogId()));
        final Set<Long> attrGroupIdSet = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toSet());

        //已经关联过的属性
        final List<AttrAttrgroupRelationEntity> attrgroupRelationEntityList = attrAttrgroupRelationService.list(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery()
                .in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIdSet)
        );
        //查询当前分类的未关联属性
        final Set<Long> attrIdSet = attrgroupRelationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());
        final LambdaQueryWrapper<AttrEntity> queryWrapper = Wrappers.<AttrEntity>lambdaQuery()
                .notIn(!CollectionUtils.isEmpty(attrIdSet), AttrEntity::getAttrId, attrIdSet)
                .eq(AttrEntity::getCatelogId, attrGroupEntity.getCatelogId())
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
                .and(StringUtils.isNotBlank(key), ew -> ew.eq(AttrEntity::getAttrId, key)
                        .or()
                        .like(AttrEntity::getAttrName, key)
                );
        final IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils<>(page);
    }

}