package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 用户注册VO
 *
 * @author: z_dd
 * @date: 2023/8/23 21:34
 * @Description:
 */
@Data
public class UserRegistVo {

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6,max = 18,message = "用户名必须是6-18位")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码必须是6-18位")
    private String password;

    @NotEmpty(message = "手机号码不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{3,9}$",message = "手机号码格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;

}
