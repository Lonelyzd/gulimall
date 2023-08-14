package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupWithAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemGroupAttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 19:33:39
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(String catelogId);

    /**
     * 根据spuId获取属性分组
     *
     * @param spuId     :
     * @param catalogId
     * @Author: z_dd
     * @Date: 2023/8/3 21:01
     * @return: java.util.List<com.atguigu.gulimall.product.vo.SpuItemGroupAttrVo>
     * @Description:
     **/
    List<SpuItemGroupAttrVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}

