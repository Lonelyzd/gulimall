package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.vo.MemberResponseVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * @author : z_dd
 * @date : 2024/2/14 20:05
 **/
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final String requestURI = request.getRequestURI();
        final AntPathMatcher antPathMatcher = new AntPathMatcher();
        final boolean match = antPathMatcher.match("/kill", requestURI);
        if (match) {

            final MemberResponseVo attribute = (MemberResponseVo) request.getSession().getAttribute(AuthServiceConstant.LOGIN_USER);

            if (Objects.nonNull(attribute)) {
                loginUser.set(attribute);
                return true;
            } else {
                request.getSession().setAttribute("msg", "请先登录");
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
            }
        }


        return true;
    }
}
