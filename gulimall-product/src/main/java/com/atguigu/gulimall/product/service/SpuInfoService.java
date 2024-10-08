package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 19:33:39
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfo);

    PageUtils queryPageCondition(Map<String, Object> params);

    /** 上架商品
     * @Author: z_dd
     * @Date: 2023/4/15 21:41
     * @param spuId:
     * @return: void
     * @Description:
     **/
    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

