package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * ClassName: UserLoginVo
 * Package: com.atguigu.gulimall.auth.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 05:45
 * @Version 1.0
 */
@Data
public class UserLoginVo {

    private String loginacct;
    private String password;
}