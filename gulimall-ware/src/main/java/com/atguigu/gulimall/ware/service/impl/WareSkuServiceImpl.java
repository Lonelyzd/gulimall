package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String skuId = (String) params.get("skuId");
        final String wareId = (String) params.get("wareId");

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(skuId), WareSkuEntity::getSkuId, skuId)
                        .eq(StringUtils.isNotBlank(wareId), WareSkuEntity::getWareId, wareId)
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity wareSku = this.getOne(Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(WareSkuEntity::getSkuId, skuId)
                        .eq(WareSkuEntity::getWareId, wareId),
                false
        );
        if (wareSku == null) {
            wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(0);
            wareSku.setStockLocked(0);
            //远程查询SKU名称，如果失败，整个事务无需回滚
            try {
                final R info = productFeignService.info(skuId);
                final Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSku.setSkuName((String) skuInfo.get("skuName"));
            } catch (Exception e) {

            }
        }
        wareSku.setStock(wareSku.getStock() + skuNum);

        this.saveOrUpdate(wareSku);
    }

}