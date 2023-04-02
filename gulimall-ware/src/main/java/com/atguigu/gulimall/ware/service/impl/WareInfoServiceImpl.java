package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String key = (String) params.get("key");

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                Wrappers.<WareInfoEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(key), WareInfoEntity::getId, key)
                        .or()
                        .like(StringUtils.isNotBlank(key), WareInfoEntity::getAreacode, key)
                        .or()
                        .like(StringUtils.isNotBlank(key), WareInfoEntity::getAddress, key)
                        .or()
                        .like(StringUtils.isNotBlank(key), WareInfoEntity::getName, key)
        );

        return new PageUtils(page);
    }

}