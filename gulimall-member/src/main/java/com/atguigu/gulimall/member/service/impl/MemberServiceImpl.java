package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneExistsException;
import com.atguigu.gulimall.member.exception.UsernameExistsException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.MemberLoginVo;
import com.atguigu.gulimall.member.vo.MemberRegistVo;
import com.atguigu.gulimall.member.vo.QQAccountInfoVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableBiMap;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 会员注册功能
     *
     * @param vo:
     * @Author: z_dd
     * @Date: 2023/8/27 12:27
     * @return: void
     * @Description:
     **/
    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity entity = new MemberEntity();

        //检查手机号用户名唯一性
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        entity.setUsername(vo.getUsername());
        entity.setMobile(vo.getPhone());

        //设置默认等级
        final MemberLevelEntity memberLevelEntity = memberLevelService.getOne(Wrappers.<MemberLevelEntity>lambdaQuery().eq(MemberLevelEntity::getDefaultStatus, 1));
        entity.setLevelId(memberLevelEntity.getId());

        //密码进行加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        final String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //其他默认值
        this.baseMapper.insert(entity);
    }


    /**
     * 检查手机号是否唯一
     *
     * @param phone:
     * @Author: z_dd
     * @Date: 2023/8/27 12:50
     * @return: void
     * @Description:
     **/
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistsException {
        final Integer integer = this.baseMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery().eq(MemberEntity::getMobile, phone));
        if (integer > 0) {
            throw new PhoneExistsException();
        }
    }

    /**
     * 检查用户名是否唯一
     *
     * @param username:
     * @Author: z_dd
     * @Date: 2023/8/27 12:50
     * @return: void
     * @Description:
     **/
    @Override
    public void checkUsernameUnique(String username) throws UsernameExistsException {
        final Integer integer = this.baseMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery().eq(MemberEntity::getUsername, username));
        if (integer > 0) {
            throw new UsernameExistsException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        final MemberEntity entity = this.baseMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getUsername, vo.getLoginacct())
                .or()
                .eq(MemberEntity::getMobile, vo.getLoginacct())
        );

        if (entity != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (passwordEncoder.matches(vo.getPassword(), entity.getPassword())) {
                return entity;
            }
        }
        return null;
    }

    /**
     * 社交账号注册和登录
     *
     * @param vo :
     * @Author: z_dd
     * @Date: 2023/9/13 21:43
     * @return: com.atguigu.gulimall.member.entity.MemberEntity
     * @Description:
     **/
    @Override
    public MemberEntity login(SocialUser vo) {
        final String openid = vo.getOpenid();
        final MemberEntity entity = this.baseMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getSocialUid, vo.getClient_id())
        );

        if (entity != null) {
//            表示用户已注册，

            entity.setAccessToken(openid);
            this.baseMapper.updateById(entity);

            return entity;
        } else {
            //用户未注册,进行注册

            try {
                final HttpResponse httpResponse = HttpUtils.doGet("https://graph.qq.com", "/user/get_user_info", "GET",
                        new HashMap<>(),
                        ImmutableBiMap.of("access_token", vo.getAccessToken(), "oauth_consumer_key", "102067382", "openid", vo.getOpenid(), "fmt", "json")
                );


                MemberEntity memberEntity = new MemberEntity();
                final String string = EntityUtils.toString(httpResponse.getEntity());

                final QQAccountInfoVo qqAccountInfoVo = JSONObject.parseObject(string, QQAccountInfoVo.class);
                memberEntity.setAccessToken(vo.getAccessToken());
                memberEntity.setUsername(qqAccountInfoVo.getNickname());
                memberEntity.setGender(qqAccountInfoVo.getGender_type() == 2 ? 1 : 2);
                memberEntity.setCreateTime(new Date());
                memberEntity.setHeader(qqAccountInfoVo.getFigureurl_qq());
                memberEntity.setCity(qqAccountInfoVo.getCity());
                this.baseMapper.insert(memberEntity);

                return memberEntity;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


}