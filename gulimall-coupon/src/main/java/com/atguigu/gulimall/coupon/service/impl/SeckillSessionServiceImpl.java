package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;
import com.atguigu.gulimall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        final LocalDate now = LocalDate.now();
        final LocalDate plus = now.plusDays(2);

        final LocalDateTime startDateTime = LocalDateTime.of(now, LocalTime.MIN);
        final LocalDateTime endDateTime = LocalDateTime.of(plus, LocalTime.MAX);

        final List<SeckillSessionEntity> list = this.list(Wrappers.<SeckillSessionEntity>lambdaQuery()
                .between(SeckillSessionEntity::getStartTime, startDateTime, endDateTime));

        if (!CollectionUtils.isEmpty(list)) {
            final List<SeckillSessionEntity> collect = list.stream().map(session -> {
                final List<SeckillSkuRelationEntity> list1 = seckillSkuRelationService.list(Wrappers.<SeckillSkuRelationEntity>lambdaQuery().eq(SeckillSkuRelationEntity::getPromotionSessionId, session.getId()));
                session.setRelationEntities(list1);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return list;
    }

}