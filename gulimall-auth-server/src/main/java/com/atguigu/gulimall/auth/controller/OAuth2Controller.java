package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.GiteeUser;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpSession;

import com.atguigu.common.utils.HttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: OAuth2Controller
 * Package: com.atguigu.gulimall.auth.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/10 下午 03:08
 * @Version 1.0
 */
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> header = new HashMap<>();
        Map<String, String> query = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        //App Key：就是client_id
        map.put("client_id", "3605212883");//和login.html的要保持一致
        map.put("client_secret", "54a3190f94c14102a3aba79e98a5c4c8");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //1 根據 code 換取 access_token 能獲取則成功
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", header, query, map);

        //處理返回的 response 這個 json
        if (response.getStatusLine().getStatusCode() == 200) {
            //成功獲取了access_token JSON逆轉為對象
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //得知道是哪個社交用戶登錄的
            //1） 第一次用 微博 進行 社交登錄=>註冊，進行一對一綁定註冊到遠程服務的數據庫
            //2） 多次用 微博 進行 社交登錄=>登錄
            //由遠程服務來做判斷
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0) {
                MemberRespVo memberResVo = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("社交登錄成功，用戶信息為：{}" + memberResVo.toString());
                //1 第一次使用SESSION 命令瀏覽器保存JSESSIONID的cookie
                //以後瀏覽器訪問哪個網站就會帶上這個網站的cookie
                //子域之間：gulimall.com         auth.guliamll.com          member.gulimall.com
                //發卡發的時候(指定域名為父域名)，即使是子系統發的卡，也能讓父系統使用
                //TODO 1 默認發的令牌 session=asdfg 作用域是當前域：解決子域session共享問題
                //TODO 2 希望使用json序列化對像到redis中
                //遠程登錄成功，將遠程服務返回的entity放入session中
                session.setAttribute(AuthServiceConstant.LOGIN_USER, memberResVo);
//                servletResponse.addCookie(new Cookie("JSESSIONID", "dada").setDomain());
                //登錄成功 -> 跳轉首頁
                return "redirect:http://gulimall.com";
            } else {
                //失敗 重新登錄
                return "redirect:http://auth.gulimall.com/login.html";
            }

        } else {
            //沒有獲取了access_token 登錄失敗 返回到登錄頁
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }


    @GetMapping(value = "/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) throws Exception {
        //1.使用code換取token，換取成功繼續2，否則重定向至登錄頁
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "51b2960821a232aa3256c1e09fe520d4f254d5c38c317c65299a070f4e802900");
        map.put("client_secret", "fc8a85e5fc718d13b323961b99661fdd5cb5de0e3457382653da394377ade4bb");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("code", code);

        //2.發送 post 請求獲取 token
        //https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());
        System.out.println(response.getStatusLine().getStatusCode());
        Map<String, String> errors = new HashMap<>();
        if (response.getStatusLine().getStatusCode() == 200) {
            //獲取到了access_token
            String json = EntityUtils.toString(response.getEntity());// 獲取到json串
            //String json = JSON.toJSONString(response.getEntity());
            GiteeUser socialUser = JSON.parseObject(json, GiteeUser.class);

            //知道了哪個社交用戶
            //1）、當前用戶如果是第一次進網站，自動註冊進來（為當前社交用戶生成一個會員信息，以後這個社交賬號就對應指定的會員）
            //登錄或者註冊這個社交用戶
            System.out.println("登錄後用code換取的token值：" + socialUser.getAccessToken());
            //調用遠程服務
            R oauthLogin = memberFeignService.oauthLogin_Gitee(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登錄成功：用戶信息：\n{}", data.toString());

                //1、第一次使用session，命令瀏覽器保存卡號，JSESSIONID這個cookie
                //以後瀏覽器訪問哪個網站就會帶上這個網站的cookie
                //TODO 1、默認發的令牌。當前域（解決子域session共享問題）
                //TODO 2、使用JSON的序列化方式來序列化對像到Redis中
//                session.setAttribute("loginUser", data);
                session.setAttribute(AuthServiceConstant.LOGIN_USER, data);

                //2、登錄成功跳回首頁
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
