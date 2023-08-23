package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThridPartFeignService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.atguigu.common.exception.BizCodeEnum.SMS_CODE_EXCEPTION;

/**
 * @author: z_dd
 * @date: 2023/8/20 21:16
 * @Description:
 */
@RestController
public class LoginController {

    @Autowired
    private ThridPartFeignService thridPartFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //1.接口防刷
        final String oldCode = redisTemplate.opsForValue().get(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (StringUtils.isNotBlank(oldCode)) {
            final long l = Long.parseLong(oldCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60 * 1000) {
                //60s内存不能再发送
                return R.error(SMS_CODE_EXCEPTION.getCode(), SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //2.验证码的再次校验 sms:code:phone  124564
        final String code  = String.format("%04d", new Random().nextInt(4000));
        final String codeTime = code + '_' + System.currentTimeMillis();
        thridPartFeignService.sendCode(phone, code);

        redisTemplate.opsForValue().set(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone, codeTime, 10, TimeUnit.MINUTES);

        return R.ok();
    }

    public String regist(){
        //注册成功回到首页


        return "";
    }
}
