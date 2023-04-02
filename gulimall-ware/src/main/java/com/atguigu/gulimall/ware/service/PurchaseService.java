package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MergeVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:08:21
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceiveList(Map<String, Object> params);

    void mergePurchase(MergeVO vo);

    void received(List<Long> purchaseId);

    void done(PurchaseDoneVO vo);
}

