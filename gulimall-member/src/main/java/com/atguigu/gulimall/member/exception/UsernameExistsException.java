package com.atguigu.gulimall.member.exception;

/** 用户名已存在
 * @author: z_dd
 * @date: 2023/8/27 12:52
 * @Description:
 */
public class UsernameExistsException extends RuntimeException{
    public UsernameExistsException() {
        super("用户名已存在");
    }
}
