package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author : z_dd
 * @date : 2024/1/1 21:30
 **/
@Data
@ToString
public class UserInfoTo {

    private Long userId;

    private String userKey;

    private boolean tempUser=false;
}
