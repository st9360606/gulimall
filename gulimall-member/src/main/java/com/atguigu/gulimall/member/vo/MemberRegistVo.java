package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * ClassName: MemberRegistVo
 * Package: com.atguigu.gulimall.member.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 10:26
 * @Version 1.0
 */
@Data
public class MemberRegistVo {
    private String userName;
    private String password;
    private String phone;
}
