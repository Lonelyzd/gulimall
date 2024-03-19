package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 库存系统远程调用
 *
 * @author : z_dd
 * @date : 2024/2/18 14:35
 **/
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @RequestMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam Long addrId);


    @PostMapping("/ware/waresku/lock/order")
     R orderLockStock(@RequestBody WareSkuLockVo vo);
}
