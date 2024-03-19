package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        final String skuId = (String) params.get("skuId");
        final String wareId = (String) params.get("wareId");

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(StringUtils.isNotBlank(skuId), WareSkuEntity::getSkuId, skuId)
                        .eq(StringUtils.isNotBlank(wareId), WareSkuEntity::getWareId, wareId)
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        WareSkuEntity wareSku = this.getOne(Wrappers.<WareSkuEntity>lambdaQuery()
                        .eq(WareSkuEntity::getSkuId, skuId)
                        .eq(WareSkuEntity::getWareId, wareId),
                false
        );
        if (wareSku == null) {
            wareSku = new WareSkuEntity();
            wareSku.setSkuId(skuId);
            wareSku.setWareId(wareId);
            wareSku.setStock(0);
            wareSku.setStockLocked(0);
            //远程查询SKU名称，如果失败，整个事务无需回滚
            try {
                final R info = productFeignService.info(skuId);
                final Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSku.setSkuName((String) skuInfo.get("skuName"));
            } catch (Exception e) {

            }
        }
        wareSku.setStock(wareSku.getStock() + skuNum);

        this.saveOrUpdate(wareSku);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        final List<SkuHasStockVo> collect = skuIds
                .stream()
                .map(skuId -> {
                    SkuHasStockVo vo = new SkuHasStockVo();
                    Long count = this.baseMapper.getSkuStock(skuId);
                    vo.setSkuId(skuId);
                    vo.setHasStock(count != null && count > 0);
                    return vo;
                })
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * 为某个订单锁定库存
     *
     * @param vo :
     * @return List<LockStockResult>
     * @author z_dd
     * @date 2024/3/2 21:35
     **/
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public Boolean orderLockStock(WareSkuLockVo vo) {
        //1.找到每个商品在那个仓库有库存
        final List<OrderItemVo> locks = vo.getLocks();

        final List<SkuWareHasStock> collect = locks.stream()
                .map(item -> {
                    SkuWareHasStock stock = new SkuWareHasStock();
                    final Long skuId = item.getSkuId();
                    stock.setSkuId(skuId);
                    stock.setNum(item.getCount());
                    //查询这个商品在哪有库存
                    List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
                    stock.setWareId(wareIds);

                    return stock;
                })
                .collect(Collectors.toList());


        //2.锁定库存
        Boolean allLock = true;
        for (SkuWareHasStock stock : collect) {
            Boolean skuStocked = false;
            final List<Long> wareIds = stock.getWareId();
            final Long skuId = stock.getSkuId();
            for (Long wareId : wareIds) {
                if (CollectionUtils.isEmpty(wareIds)) {
                    //没有任何仓库有这个商品的库存
                    throw new NoStockException(skuId);
                } else {
                    //成功返回1，否则返回0
                    Integer count = this.baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                    if (count.equals(1)) {
                        //锁定成功，直接结束本轮
                      skuStocked=true;
                      break;
                    } else {
                        //当前仓库锁定失败，重试下一个仓库
                    }
                }
            }
            if(!skuStocked){
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }

        //3.全部锁定成
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;

        private Integer num;

        private List<Long> wareId;
    }
}