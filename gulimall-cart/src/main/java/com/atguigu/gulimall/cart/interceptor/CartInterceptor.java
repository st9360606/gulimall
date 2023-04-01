package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * ClassName: CartInterceptor
 * Package: com.atguigu.gulimall.cart.interceptor
 * Description: 攔截器
 *
 * @Author kurt
 * @Create 2023/3/13 下午 05:48
 * @Version 1.0
 * <p>
 * 攔截器:
 * 在執行目標方法之前，判斷用戶的登錄狀態，並封裝傳遞給controller的目標請求
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    //ThreadLocal 同一線程上信息共享
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthServiceConstant.LOGIN_USER);
        if (member != null) {
            //說明用戶登錄了
            userInfoTo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //user-key
                //有cookie 可能是臨時用戶，但是此方法針對登錄用戶
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果沒有臨時用戶，一定要分配一個臨時用戶
        //如果沒有登錄 就準備臨時set一個cookie，首先設置To的userKey
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        //目標方法執行之前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 業務執行之後，讓瀏覽器保存cookie
     * 分配臨時用戶，讓瀏覽器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        //如果沒有臨時用戶，一定要保存一個臨時用戶，isTempUser默認是false，不是臨時用戶，如果是true的話就是臨時用戶就不走下面。
        if (!userInfoTo.isTempUser()) {
            //不是臨時用戶
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //設置cookie作用域
            cookie.setDomain("gulimall.com");
            //cookie的過期時間
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
