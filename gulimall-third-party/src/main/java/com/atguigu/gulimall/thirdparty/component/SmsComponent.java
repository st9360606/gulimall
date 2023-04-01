package com.atguigu.gulimall.thirdparty.component;

import com.atguigu.gulimall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: SmsComponent
 * Package: com.atguigu.gulimall.thirdparty.component
 * Description:
 *
 * @Author kurt
 * @Create 2023/3/8 下午 01:15
 * @Version 1.0
 */
//跟配置文件綁定
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String skin;
    private String sign;
    private String appcode;

    public void sendSmsCode(String phone, String code){
        String method = "GET";
        String appcoder = "93b7e19861a24c519a7548b17dc16d75";
        Map<String, String> headers = new HashMap<String, String>();
        //最後在header中的格式(中間是英文空格)為Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcoder);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("code", code);
        querys.put("phone", phone);
        querys.put("skin", skin);
        querys.put("sign", sign);



        try {
            /**
             * 重要提示如下:
             * HttpUtils請從
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下載
             *
             * 相應的依賴請參照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            System.out.println(response.toString());
            //獲取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
