package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * ClassName: UserRegistVo
 * Package: com.atguigu.gulimall.auth.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 05:46
 * @Version 1.0
 */
@Data
public class UserRegistVo {

    @NotEmpty(message = "用戶名必須提交")
    @Length(min = 6, max = 18, message = "用戶名長度必須是6-18位")
    private String userName;

    @NotEmpty(message = "密碼不能為空")
    @Length(min = 6, max = 18, message = "密碼長度必須是6-18位")
    private String password;

    //這裡的手機號碼長度，為中國手機號碼長度
    @NotEmpty(message = "手機號不能為空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手機號格式不對")
    private String phone;

    @NotEmpty(message = "驗證碼不能為空")
    private String code;
}