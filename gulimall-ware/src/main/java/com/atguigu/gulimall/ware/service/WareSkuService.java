package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:08:21
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /** 为某个订单锁定库存
     * @author z_dd
     * @date 2024/3/2 21:35
     * @param vo: 
     * @return List<LockStockResult>
     **/
   Boolean orderLockStock(WareSkuLockVo vo);

    void unlockStock(StockLockTo to);

    void unlockStock(OrderTo to);
}

