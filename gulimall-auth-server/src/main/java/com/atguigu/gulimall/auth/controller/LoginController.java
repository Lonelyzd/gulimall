package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThridPartFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
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
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/sms/sendcode")
    @ResponseBody
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
        final String codeTime = code + "_" + System.currentTimeMillis();
        thridPartFeignService.sendCode(phone, code);

        redisTemplate.opsForValue().set(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone, codeTime, 10, TimeUnit.MINUTES);

        return R.ok();
    }

    /**
     * @param vo:
     * @param result:
     * @param attributes: RedirectAttributes 用于redirect重定向是传递请求域数据
     * @Author: z_dd
     * @Date: 2023/8/27 11:55
     * @return: java.lang.String
     * @Description:
     **/
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes attributes) {

        if (result.hasErrors()) {
            final Map<String, String> errors = result.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //注册，调用远程服务进行注册
        //校验验证码
        final String code = vo.getCode();

        //1.接口防刷
        final String redisData = redisTemplate.opsForValue().get(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isNotBlank(redisData)) {
            final String redisCode = redisData.split("_")[0];
            if (StringUtils.equals(redisCode, code)) {
                //校验成功后删除验证码
                redisTemplate.delete(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                //调用远程服务进行注册
                final R registResult = memberFeignService.regist(vo);
                if (registResult.getCode().equals(0)) {
                    //注册成功回到首页
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    //验证码过期或不存在
                    final Map<String, String> errors = new HashMap<>();
                    final String data = registResult.getData("msg", new TypeReference<String>() {
                    });
                    errors.put("msg", data);
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }

        }
        //验证码过期或不存在
        final Map<String, String> errors = new HashMap<>();
        errors.put("code", "验证码错误");
        attributes.addFlashAttribute("errors", errors);
        return "redirect:http://auth.gulimall.com/reg.html";

    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes) {
        final R login = memberFeignService.login(vo);

        if (login.getCode().equals(0)) {
            return "redirect:http://gulimall.com";
        } else {
            final Map<String, String> errors = new HashMap<>();

            final String data = login.getData("msg", new TypeReference<String>() {
            });
            errors.put("msg", data);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }


    }
}
