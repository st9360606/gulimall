package com.atguigu.gulimall.cart.vo;

import lombok.Data;
import lombok.ToString;

/**
 * ClassName: UserInfoTo
 * Package: com.atguigu.gulimall.cart.vo
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/13 下午 04:27
 * @Version 1.0
 */
@ToString
@Data
public class UserInfoTo {

    private Long userId;
    private String userKey;//一定要有的

    //用來判斷是否有臨時用戶
    private boolean tempUser = false;
}