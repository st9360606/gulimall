package com.atguigu.gulimall.member.exception;

/**
 * ClassName: PhoneExistException
 * Package: com.atguigu.gulimall.member.exception
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/9 上午 01:18
 * @Version 1.0
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手機號已經存在");
    }
}