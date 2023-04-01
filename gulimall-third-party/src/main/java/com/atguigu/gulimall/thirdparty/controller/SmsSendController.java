package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: SmsSendController
 * Package: com.atguigu.gulimall.thirdparty.controller
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 03:42
 * @Version 1.0
 */

//要返回Json數據
@RestController
//基準路徑
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供給別的服務調用的
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {

        smsComponent.sendSmsCode(phone, code);

        return R.ok();
    }

}