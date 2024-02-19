package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.gulimall.ware.vo.FareVo;
import com.atguigu.gulimall.ware.vo.MemberAddressVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;


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

    /**
     * 根据用户收获地址计算运费
     *
     * @param addrId :
     * @return BigDecimal
     * @author z_dd
     * @date 2024/2/18 20:56
     **/
    @Override
    public FareVo getFare(Long addrId) {
        final FareVo fareVo = new FareVo();
        final R info = memberFeignService.addrInfo(addrId);
        final MemberAddressVo data = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });

        if (data != null) {
            final String phone = data.getPhone();
            final String substring = phone.substring(phone.length() - 1);
            fareVo.setFare(new BigDecimal(substring));
            fareVo.setAddress(data);

        }
        return fareVo;
    }

}