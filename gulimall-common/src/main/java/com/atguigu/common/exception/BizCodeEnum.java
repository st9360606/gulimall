package com.atguigu.common.exception;

/**
 * ClassName: BizCodeEnum
 * Package: com.atguigu.common.exception
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/5 下午 07:02
 * @Version 1.0
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系統未知異常"),
    VALID_EXCEPTION(10001, "參數格式校驗失敗"),
    SMS_CODE_EXCEPTION(10002, "短信驗證碼獲取頻率太高，請稍後再試"),
    TOO_MANY_REQUEST(10003, "請求流量過大，請稍後再試"),
    SMS_COD_SEND_EXCEPTION(10004, "短信發送失敗"),
    USER_EXISTED_EXCEPTION(15001, "用戶名已存在"),
    PHONE_REGISTERED_EXCEPTION(15002, "手機號已註冊"),
    LOGIN_ACCOUNT_OR_PASSWORD_EXCEPTION(15003, "帳號或密碼錯誤"),
    SOCIAL_USER_LOGIN_EXCEPTION(15004, "社交帳號登錄失敗"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架異常"),
    NO_STOCK_EXCEPTION(21000, "商品庫存不足");


    private Integer code;
    private String message;


    BizCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
