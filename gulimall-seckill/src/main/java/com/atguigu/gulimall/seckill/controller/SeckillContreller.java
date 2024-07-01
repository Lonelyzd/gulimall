package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author : z_dd
 * @date : 2024/5/9 21:24
 **/
@Controller
public class SeckillContreller {

    @Autowired
    private SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return R
     * @author z_dd
     * @date 2024/5/9 21:27
     **/
    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable Long skuId) {
        SecKillSkuRedisTo vo = seckillService.getSkuSeckillInfo(skuId);

        return R.ok().setData(vo);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam String killId,
                          @RequestParam String key,
                          @RequestParam Integer num,
                          Model model
    ) {

        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
