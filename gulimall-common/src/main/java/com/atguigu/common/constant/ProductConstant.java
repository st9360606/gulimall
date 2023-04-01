package com.atguigu.common.constant;

/**
 * ClassName: ProductConstant
 * Package: com.atguigu.common.constant
 * Description:
 *
 * @Author kurt
 * @Create 2023/2/7 下午 08:56
 * @Version 1.0
 */
public class ProductConstant {
    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
        private final int code;
        private final String msg;

        AttrEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum {
        SPU_NEW(0, "新建"), SPU_UP(1, "上架"), SPU_DOWN(2, "下架");

        private int code;

        private String msg;

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
