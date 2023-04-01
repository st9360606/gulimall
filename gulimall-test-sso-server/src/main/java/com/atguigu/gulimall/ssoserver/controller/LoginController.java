package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * ClassName: LoginController
 * Package: com.atguigu.gulimall.ssoserver.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/12 上午 02:25
 * @Version 1.0
 */

@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    //client1.com:8081/employees
    //127.0.0.1 client1.com
    //127.0.0.1 client2.com
    //127.0.0.1 ssoserver.com

    @ResponseBody
    @GetMapping("userInfo")
    public String userInfo(@RequestParam("token") String token) {

        String s = redisTemplate.opsForValue().get(token);
        return s;
    }


    /**
     * 遠端的註冊請求地址，跳轉至登錄頁面
     */
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,
                            Model model,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {
        //判斷瀏覽器是否有Cookie(sso_token)，即之前是否有人登錄過，sso_token是CookieName
        if (!StringUtils.isEmpty(sso_token)) {
            //之前有人登錄過，給瀏覽器留下了痕跡，把這個sso_token返回去
            return "redirect:" + url + "?token=" + sso_token;
        }
        //正常執行登錄
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //登入成功跳轉，跳回到之前的頁面

            //把登入成功的用戶保存起來。
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid, username);
            Cookie sso_token = new Cookie("sso_token", uuid);
            response.addCookie(sso_token);

            return "redirect:" + url + "?token=" + uuid;
        }
        //登入失敗
        return "login";
    }
}
