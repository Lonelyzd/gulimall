package com.atguigu.gulimall.ware.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.gulimall.ware.vo.MergeVO;
import com.atguigu.gulimall.ware.vo.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 采购信息
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:08:21
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    //    /ware/purchase/unreceive/list
    @RequestMapping("/unreceive/list")
    public R unReceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryPageUnReceiveList(params);

        return R.ok().put("page", page);
    }

    //   /ware/purchase/merge
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO vo) {
        purchaseService.mergePurchase(vo);

        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase) {
        purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase) {
        purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 领取采购单
     *
     * @param purchaseId:
     * @Author: z_dd
     * @Date: 2023/4/1 10:54
     * @return: com.atguigu.common.utils.R
     * @Description:
     **/
    //  /ware/purchase/received
    @PostMapping("/received")
    public R received(@RequestBody List<Long> purchaseId) {
        purchaseService.received(purchaseId);

        return R.ok();
    }

    /**
     * 完成采购
     *`
     * @param vo:
     * @Author: z_dd
     * @Date: 2023/4/1 16:15
     * @return: com.atguigu.common.utils.R
     * @Description:
     **/
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO vo) {
        purchaseService.done(vo);
        return R.ok();
    }

}
