package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 22:39:30
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
