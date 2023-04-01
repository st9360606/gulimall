package com.atguigu.gulimall.member.vo;

import lombok.Data;
import lombok.ToString;

/**
 * ClassName: GiteeUser
 * Package: com.atguigu.gulimall.auth.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/10 下午 08:42
 * @Version 1.0
 */
@Data
@ToString
public class GiteeUser {
    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String refreshToken;

    private String scope;

    private String createdAt;


}
