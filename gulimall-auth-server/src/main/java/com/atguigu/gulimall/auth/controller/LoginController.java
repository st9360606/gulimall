package com.atguigu.gulimall.auth.controller;


import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: LoginController
 * Package: com.atguigu.gulimall.auth.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 上午 11:34
 * @Version 1.0
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        //TODO 1 接口防刷
        //驗證碼60s內
        String redisCode = redisTemplate.opsForValue().get(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60 * 1000) {
                //60s內 不能再次發送
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        //2 驗證碼再次校驗 存key-phone,value-code    sms:code:13344445555->123456
        //取1-6位 作為驗證碼
        String code = UUID.randomUUID().toString().substring(0, 5);
        String substring = code + "_" + System.currentTimeMillis();
        //redis缓存验证码
        redisTemplate.opsForValue().set(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + phone, substring, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     * //TODO 重定向攜帶數據，利用session原理。將數據放愛session中。
     * 只要跳到下一個頁面取出這個數據以後，session裡面的數據就會刪掉。
     * <p>
     * //TODO 1.分布式下的session的問題。
     * RedirectAttributes redirectAttributes 模擬重定向模擬攜帶數據
     *
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {

//        if (result.hasErrors()) {
//            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//            redirectAttributes.addFlashAttribute("errors", errors);
//
//            //校驗出錯，轉發到注冊頁
//            return "redirect:http://auth.gulimall.com/reg.html";
//        }

        //1 校驗驗證碼
//        String code = vo.getCode();
//        String s = redisTemplate.opsForValue().get(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
//        if(!StringUtils.isEmpty(s)){
//            if(code.equals(s.split("_")[0])) {
//                //刪除驗證碼; 令牌機制
//                redisTemplate.delete(AuthServiceConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
//                //驗證碼通過。 //真正註冊。調用遠程服務進行註冊
//                R r = memberFeignService.regist(vo);
//                if(r.getCode()==0){
//                    //成功
//
//                    return "redirect:http://auth.gulimall.com/login.html";
//                }else {
//                    Map<String, String> errors = new HashMap<>();
//                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));//R错误消息都在msg里
//                    redirectAttributes.addFlashAttribute("errors", errors);
//                    return "redirect:http://auth.gulimall.com/reg.html";
//                }
//
//            }else {
//                Map<String, String> errors = new HashMap<>();
//                errors.put("code","驗證碼錯誤");
//                redirectAttributes.addFlashAttribute("errors", errors);
//                return "redirect:http://auth.gulimall.com/reg.html";
//            }
//
//        }else {
//            //出現異常 或者 失敗
//            Map<String, String> errors = new HashMap<>();
//            errors.put("code","驗證碼錯誤");
//            redirectAttributes.addFlashAttribute("errors", errors);
//            //校驗出錯，轉發到注冊頁
//            return "redirect:http://auth.gulimall.com/reg.html";
//        }
        //沒買短信服務，暫時先不驗證
        R r = memberFeignService.regist(vo);
        return "redirect:http://auth.gulimall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {

        //遠程登陸
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            //成功，遠程登錄成功，將遠程服務返回的entity放入session中
            session.setAttribute(AuthServiceConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        } else {
            //登錄失敗，遠程登錄失敗
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        Object attribute = session.getAttribute(AuthServiceConstant.LOGIN_USER);
        //判斷是否登入過
        if (attribute == null) {
            //沒登入
            return "login";
        } else {
            //登入過了，就回首頁
            return "redirect:http://gulimall.com";
        }
    }


    /**
     * ！ ！ ！ ！後來代碼優化 見本文的->loginPage()
     * 下面兩個空方法僅僅是發送一個請求【直接】跳轉一個頁面
     * 這樣不太好 不要寫空方法 去GulimallWebConfig.class
     * 使用 SpringMVC ViewController 將請求和頁面映射過來
     * @return
     */
//    @GetMapping("/login.html")
//    public String loginPage() {
//
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage() {
//
//        return "reg";
//    }
}
