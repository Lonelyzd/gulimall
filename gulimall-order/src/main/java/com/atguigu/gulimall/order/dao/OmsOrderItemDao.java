package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OmsOrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-28 20:22:50
 */
@Mapper
public interface OmsOrderItemDao extends BaseMapper<OmsOrderItemEntity> {
	
}
