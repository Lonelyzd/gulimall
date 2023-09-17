package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * @author: z_dd
 * @date: 2023/9/12 21:21
 * @Description:
 */
@Data
public class SocialUser {

    private String client_id;

    private String openid;

    private String accessToken;
}
