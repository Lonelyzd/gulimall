package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.TestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;

import java.util.Map;

/**
 * @author : z_dd
 * @date : 2023/9/26 19:18
 **/
public interface TestDao extends BaseMapper<TestEntity> {

     @MapKey("position")
     Map<String, Map<String, Map<String,String>>>  getTest();

}