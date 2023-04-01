package com.atguigu.gulimall.member.interceptor;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * ClassName: LoginUserInterceptor
 * Package: com.atguigu.gulimall.order.interceptor
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/16 下午 03:28
 * @Version 1.0
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static  ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //  /order/order/status/{orderSn}
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if(match){
            return true;
        }

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthServiceConstant.LOGIN_USER);
        if(attribute!=null){
            //說明登入了
            loginUser.set(attribute);
            return true;
        }else {
            //沒登入就去登入
            request.getSession().setAttribute("msg","請先進行登入");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }
}
