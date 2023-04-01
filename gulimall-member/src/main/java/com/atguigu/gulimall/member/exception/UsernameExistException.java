package com.atguigu.gulimall.member.exception;

/**
 * ClassName: UsernameExistException
 * Package: com.atguigu.gulimall.member.exception
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/9 上午 01:17
 * @Version 1.0
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用戶名已經存在");
    }
}