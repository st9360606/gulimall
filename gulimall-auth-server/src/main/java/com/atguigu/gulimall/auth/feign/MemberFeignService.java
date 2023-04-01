package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.GiteeUser;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ClassName: MemberFeignService
 * Package: com.atguigu.gulimall.auth.feign
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/9 下午 01:04
 * @Version 1.0
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    //微博登入
    @PostMapping("/member/member/oauth2/weibo/login")
    R oauthLogin(@RequestBody SocialUser socialUser) throws Exception;

    //Gitee 登入
    @PostMapping("/member/member/oauth2/login")
    R oauthLogin_Gitee(@RequestBody GiteeUser giteeUser);
}
