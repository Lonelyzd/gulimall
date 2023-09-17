package com.atguigu.gulimall.member.vo;

import lombok.Data;

@Data
public class SocialUser {

    private String client_id;

    private String openid;

    private String accessToken;
}
