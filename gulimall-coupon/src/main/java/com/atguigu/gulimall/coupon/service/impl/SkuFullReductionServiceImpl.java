package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPrice;
import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //6.4 SKU的优惠、满减等信息  sms_sku_ladder|sms_sku_full_reduction|sms_member_price

        if (skuReductionTo.getFullCount() > 0) {
            //1.阶梯价格sms_sku_ladder
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            skuLadderService.save(skuLadderEntity);
        }


        //2.满减信息sms_sku_full_reduction
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            SkuFullReductionEntity skuFullReduction = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo, skuFullReduction);
            this.save(skuFullReduction);
        }

        //3.会员价格
        final List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        final List<MemberPriceEntity> collect = memberPrice.stream()
                .map(item -> {
                    final MemberPriceEntity entity = new MemberPriceEntity();
                    entity.setSkuId(skuReductionTo.getSkuId());
                    entity.setMemberLevelId(item.getId());
                    entity.setMemberLevelName(item.getName());
                    entity.setMemberPrice(item.getPrice());
                    entity.setAddOther(1);
                    return entity;
                })
                .filter(item -> item.getMemberPrice().compareTo(new BigDecimal("0")) == 1)
                .collect(Collectors.toList());
        memberPriceService.saveBatch(collect);
    }

}