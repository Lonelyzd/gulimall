package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 19:33:39
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageCondition(Map<String, Object> params);

    /** 根据spuId获取对应的所有SKU信息
     * @Author: z_dd
     * @Date: 2023/4/15 21:56
     * @param spuId:
     * @return: java.util.List<com.atguigu.gulimall.product.entity.SkuInfoEntity>
     * @Description:
     **/
    List<SkuInfoEntity> getSkusBySpuId(Long spuId);
}

