package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-29 13:00:52
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 会员注册功能
     *
     * @param vo:
     * @Author: z_dd
     * @Date: 2023/8/27 12:27
     * @return: void
     * @Description:
     **/
    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone);

    void checkUsernameUnique(String username);

    MemberEntity login(MemberLoginVo vo);

    /**
     * 社交账号注册和登录
     *
     * @param vo:
     * @Author: z_dd
     * @Date: 2023/9/13 21:43
     * @return: com.atguigu.gulimall.member.entity.MemberEntity
     * @Description:
     **/
    MemberEntity login(SocialUser vo);
}

