package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.atguigu.gulimall.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 优惠券信息
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 22:39:30
 */
@RestController
@RequestMapping("coupon/coupon")
@RefreshScope
public class CouponController {

//    @Value("${coupon.user.name}")
//    String userName;
//
//    @Value("${coupon.user.age}")
//    String userAge;

    @Autowired
    private CouponService couponService;


    @RequestMapping("/test")
    public R test() {
        // 获取Nacos配置中心的配置
//        return R.ok().put("userName",userName).put("userAge",userAge);
        return null;
    }

    /** 测试接口
     * @Author: z_dd
     * @Date: 2023/1/29 12:15
     * @return: com.atguigu.common.utils.R
     * @Description:
     **/
    @RequestMapping("/member/list")
    public R memberCoupons() {
        // 模拟数据库查询内容
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10");//优惠券的名字
        return R.ok().put("coupons", Arrays.asList(couponEntity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CouponEntity coupon) {
        couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CouponEntity coupon) {
        couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
