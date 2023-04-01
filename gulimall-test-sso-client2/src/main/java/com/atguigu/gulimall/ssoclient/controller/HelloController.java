package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: HelloController
 * Package: com.atguigu.gulimall.ssoclient.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/12 上午 01:34
 * @Version 1.0
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;


    /**
     * 無須登入就可以訪問
     *
     * @return
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 感知這次是在ssoserver登陸成功跳回來的。
     * required = false  (非必須參數)
     * @param model
     * @param session
     * @param token 只要去ssoserver登入成功跳回來就會帶上
     * @return
     */
    @GetMapping("/boss")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token",required = false) String token) {

        if(!StringUtils.isEmpty(token)){
            //只要去ssoserver登入成功跳回來就會帶上
            //TODO 1、去ssoserver 獲取當前token真正對應的用戶信息
            RestTemplate restTemplate = new RestTemplate();
            //給遠端的登錄服務發userInfo請求，要用戶信息
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser",body);
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //沒登入，跳轉到登入服務器

            //跳轉過去以後，使用url上的查詢參數標示我們自己是哪個頁面
            //redirect_url=http://client1.com:8081/employees
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("Kurt");
            emps.add("Jack");
            model.addAttribute("emps", emps);
            return "list";

        }

    }
}
