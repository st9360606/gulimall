package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import lombok.ToString;

/**
 * ClassName: SocialUser
 * Package: com.atguigu.gulimall.auth.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/10 下午 08:40
 * @Version 1.0
 */
@Data
@ToString
public class SocialUser {

    /**
     * 用戶授權的唯一票據，用於調用微博的開放接口，同時也是第三方應用驗證微博用戶登錄的唯一票據，第三方應用應該用該票據和自己應用內的用戶建立唯一影射關係，來識別登錄狀態，不能使用本返回值裡的UID字段來做登錄識別。
     */
    private String access_token;

    //access_token的生命週期，單位是秒數。
    private String remind_in;

    //access_token的生命週期（該參數即將廢棄，開發者請使用expires_in）。
    private long expires_in;

    //授權用戶的UID，本字段只是為了方便開發者，減少一次user/show接口調用而返回的，第三方應用不能用此字段作為用戶登錄狀態的識別，只有access_token才是用戶授權的唯一票據。
    private String uid;//相當於微博賬號 作為註冊gulimall的賬號 同一個人的uid是固定的

    private String isRealName;

}