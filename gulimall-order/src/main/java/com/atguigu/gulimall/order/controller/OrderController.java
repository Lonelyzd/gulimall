package com.atguigu.gulimall.order.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 订单
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:04:23
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable String orderSn) {

        OrderEntity entity = orderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(entity);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取当前用户的订单信息
     *
     * @param params:
     * @return R
     * @author z_dd
     * @date 2024/4/9 21:12
     **/
    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params) {
        PageUtils page = orderService.queryPageWithItem(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody OrderEntity order) {
        orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody OrderEntity order) {
        orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
