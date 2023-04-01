package com.atguigu.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 以下是一個自定義校驗註解
 */

@Documented

//指定校驗器
//ListValueConstraintValidator : 這是一個自定義校驗器
@Constraint(validatedBy = { ListValueConstraintValidator.class })
//指定類型
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
//運行時可以獲取到
@Retention(RUNTIME)
public @interface ListValue {
    String message() default "{com.atguigu.common.valid.ListValue.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int[] vals() default { };
}
