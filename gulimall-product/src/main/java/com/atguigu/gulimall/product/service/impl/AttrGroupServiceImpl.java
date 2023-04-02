package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        final String key = (String) params.get("key");
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                Wrappers.<AttrGroupEntity>lambdaQuery()
                        .eq(!catelogId.equals(0L), AttrGroupEntity::getCatelogId, catelogId)
                        .and(
                                StringUtils.isNoneBlank(key), ew -> ew
                                        .eq(AttrGroupEntity::getAttrGroupId, key)
                                        .or()
                                        .like(AttrGroupEntity::getAttrGroupName, key)
                        )
        );

        return new PageUtils(page);
    }

    /** 根据分类id查出所有分组以及分组里面的属性
     * @Author: z_dd
     * @Date: 2023/3/22 21:25
     * @param catelogId:
     * @return: java.util.List<com.atguigu.gulimall.product.vo.AttrGroupWithAttrVo>
     * @Description:
     **/
    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(String catelogId) {
        final List<AttrGroupEntity> attrGroupList = this.baseMapper.selectList(Wrappers.<AttrGroupEntity>lambdaQuery()
                .eq(AttrGroupEntity::getCatelogId, catelogId));

        final List<AttrGroupWithAttrVo> collect = attrGroupList.stream().map(attrGroup -> {
            final AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(attrGroup, attrGroupWithAttrVo);
            final List<AttrEntity> relationAttr = attrService.getRelationAttr(attrGroupWithAttrVo.getAttrGroupId());
            attrGroupWithAttrVo.setAttrs(relationAttr);
            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());

        return collect;
    }

}