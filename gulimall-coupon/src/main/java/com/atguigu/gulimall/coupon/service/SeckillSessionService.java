package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 22:39:30
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getLates3DaySession();
}

