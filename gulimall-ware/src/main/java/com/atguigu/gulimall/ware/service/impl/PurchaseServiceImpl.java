package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.MergeVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import com.atguigu.gulimall.ware.vo.PurchaseItemDoneVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                Wrappers.<PurchaseEntity>lambdaQuery().eq(PurchaseEntity::getStatus, 0)
                        .or()
                        .eq(PurchaseEntity::getStatus, 1)
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void mergePurchase(MergeVO vo) {
        Long purchaseId = vo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchase = new PurchaseEntity();
            purchase.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchase.setCreateTime(new Date());
            purchase.setUpdateTime(new Date());
            this.save(purchase);
            purchaseId = purchase.getId();
        }

        final List<Long> items = vo.getItems();

      /*
       Long finalPurchaseId = purchaseId;
      final List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(i);
            entity.setPurchaseId(finalPurchaseId);
            entity.setStatus(WareConstant.PurchaseDetialStatusEnum.ASSIGNED.getCode());
            return entity;
        }).collect(Collectors.toList());
        */

        //采购需求状态是0，1才可以合并
        purchaseDetailService.update(Wrappers.<PurchaseDetailEntity>lambdaUpdate()
                .set(PurchaseDetailEntity::getPurchaseId, purchaseId)
                .set(PurchaseDetailEntity::getStatus, WareConstant.PurchaseDetialStatusEnum.ASSIGNED.getCode())
                .in(PurchaseDetailEntity::getId, items)
                .and(wr -> wr.eq(PurchaseDetailEntity::getStatus, WareConstant.PurchaseDetialStatusEnum.CREATED.getCode())
                        .or()
                        .eq(PurchaseDetailEntity::getStatus, WareConstant.PurchaseDetialStatusEnum.ASSIGNED.getCode())
                )
        );

        PurchaseEntity update = new PurchaseEntity();
        update.setId(purchaseId);
        update.setUpdateTime(new Date());
        this.updateById(update);

    }

    @Override
    @Transactional
    public void received(List<Long> purchaseId) {
        final List<PurchaseEntity> purchaseList = this.baseMapper.selectBatchIds(purchaseId);
        //1.确认当前采购单是新建活已分配状态
        final List<PurchaseEntity> filterPurchaseList = purchaseList
                .stream()
                .filter(purchase -> WareConstant.PurchaseStatusEnum.CREATED.getCode() == purchase.getStatus() || WareConstant.PurchaseStatusEnum.ASSIGNED.getCode() == purchase.getStatus())
                //2.改变采购单状态
                .map(purchase -> {
                    purchase.setUpdateTime(new Date());
                    purchase.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return purchase;
                })
                .collect(Collectors.toList());

        this.updateBatchById(filterPurchaseList);

        //3. 改变采购项状态
        final Set<Long> purchaseIdSet = purchaseList.stream().map(PurchaseEntity::getId).collect(Collectors.toSet());
        final List<PurchaseDetailEntity> purchaseDetailList = purchaseDetailService.list(Wrappers.<PurchaseDetailEntity>lambdaQuery()
                .in(PurchaseDetailEntity::getPurchaseId, purchaseIdSet));

        final List<PurchaseDetailEntity> collect = purchaseDetailList.stream().map(purchaseDetail -> {
            purchaseDetail.setStatus(WareConstant.PurchaseDetialStatusEnum.BUSING.getCode());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVO vo) {
        //改变采购单状态
        final List<PurchaseItemDoneVO> items = vo.getItems();
        final boolean hasError = items.stream().anyMatch(item -> WareConstant.PurchaseDetialStatusEnum.HASERROR.getCode() == item.getStatus());
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(vo.getId());
        purchaseEntity.setStatus(hasError ? WareConstant.PurchaseStatusEnum.HASERROR.getCode() : WareConstant.PurchaseStatusEnum.FINISH.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

        //改变采购项状态
        final List<PurchaseDetailEntity> purchaseDetailList = items.stream().map(item -> {
            final PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(item.getItemId());
            entity.setStatus(item.getStatus());
            return entity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailList);

        //将采购成功的入库
        purchaseDetailList.stream()
                .filter(purchaseDetail -> WareConstant.PurchaseDetialStatusEnum.FINISH.getCode() == purchaseDetail.getStatus())
                .forEach(purchaseDetail -> {
                    final PurchaseDetailEntity entity = purchaseDetailService.getById(purchaseDetail.getId());
                    wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
                });

    }

}