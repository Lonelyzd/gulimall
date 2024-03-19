package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:08:21
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    /** 查询SKU库存
     * @Author: z_dd
     * @Date: 2023/4/16 11:34
     * @param skuId:
     * @return: long
     * @Description:
     **/
    Long getSkuStock(Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId")Long skuId);

    Integer lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

}
