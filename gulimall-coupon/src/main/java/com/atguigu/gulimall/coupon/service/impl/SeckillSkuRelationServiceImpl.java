package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        final String promotionSessionId = (String) params.get("promotionSessionId");

        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                Wrappers.<SeckillSkuRelationEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(promotionSessionId), SeckillSkuRelationEntity::getPromotionSessionId, promotionSessionId)
        );

        return new PageUtils(page);
    }

}