package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String key = (String) params.get("key");
        final String status = (String) params.get("status");
        final String wareId = (String) params.get("wareId");

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                Wrappers.<PurchaseDetailEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(status), PurchaseDetailEntity::getStatus, status)
                        .eq(StringUtils.isNotBlank(wareId), PurchaseDetailEntity::getWareId, wareId)
                        .and(StringUtils.isNotBlank(key), wr -> wr
                                .eq(PurchaseDetailEntity::getPurchaseId, key)
                                .or()
                                .eq(PurchaseDetailEntity::getSkuId, key)
                        )
        );

        return new PageUtils(page);
    }

}