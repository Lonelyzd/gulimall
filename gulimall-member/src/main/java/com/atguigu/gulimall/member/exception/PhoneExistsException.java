package com.atguigu.gulimall.member.exception;

/** 手机号已存在
 * @author: z_dd
 * @date: 2023/8/27 12:54
 * @Description:
 */
public class PhoneExistsException extends RuntimeException{
    public PhoneExistsException() {
        super("手机号已存在");
    }
}