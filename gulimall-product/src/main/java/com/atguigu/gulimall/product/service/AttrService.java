package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 19:33:39
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo vo);

    PageUtils queryBaseAttrPage(Map<String, Object> param, Long catelogId,String attrType);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    void deleteRelation(List<AttrGroupRelationVo> vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long catelogId);

    /**
     * @Author: z_dd
     * @Date: 2023/4/16 9:52
     * @param attrIds:
     * @return: java.util.List<java.lang.Long>
     * @Description: 获取所有可检索属性
     **/
    List<Long> selectSearchAttrs(Collection<Long> attrIds);
}

