package com.atguigu.gulimall.product.exception;



import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: GulimallExceptionControllerAdvice
 * Package: com.atguigu.gulimall.product.exception
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/5 下午 03:28
 * @Version 1.0
 */

//@ControllerAdvice : 統一處理異常
//可以指定這com.atguigu.gulimall.product包下所有Controller的異常發生處理
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.atguigu.gulimall.product.app")

@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.app")
public class GulimallExceptionControllerAdvice {

    //因為都是返回Json數據，所以返回 R

    //@ExceptionHandler : 指定異常類型
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("數據校驗出現問題{},異常類型: {}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", errorMap);
    }
    //BizCodeEnum 枚舉 存放各式錯誤信息
    //負責處理最大的異常
    @ExceptionHandler(value = Throwable.class)
    public R handlerException(Throwable e) {
        log.error("Exception class: {}, msg: {}", e.getClass(), e.getMessage(), e);
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }


}
