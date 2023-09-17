package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 远程调用和会员服务接口
 *
 * @author: z_dd
 * @date: 2023/8/27 19:56
 * @Description:
 */
@FeignClient("gulimall-member")
@RequestMapping("/member/member")
public interface MemberFeignService {

    @PostMapping("/regist")
    R regist(@RequestBody UserRegistVo vo);


    @PostMapping("/login")
    R login(@RequestBody UserLoginVo vo);


    @PostMapping("/oauth/login")
    R oauthLogin(@RequestBody SocialUser vo);
}
