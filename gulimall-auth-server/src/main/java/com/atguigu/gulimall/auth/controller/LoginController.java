package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThridPartFeignService;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.atguigu.common.exception.BizCodeEnum.SMS_CODE_EXCEPTION;

/**
 * @author: z_dd
 * @date: 2023/8/20 21:16
 * @Description:
 */
@Controller
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
        final String code = String.format("%04d", new Random().nextInt(4000));
        final String codeTime = code + '_' + System.currentTimeMillis();
        thridPartFeignService.sendCode(phone, code);

        redisTemplate.opsForValue().set(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone, codeTime, 10, TimeUnit.MINUTES);

        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, Model model) {
        //注册成功回到首页
         if (result.hasErrors()) {
            final Map<String, String> errors = result.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            model.addAttribute("errors", errors);
            return "reg";
        }


        return "redirect:/login.html";
    }
}
