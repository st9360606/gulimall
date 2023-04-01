package com.atguigu.gulimall.thirdparty;


import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import com.atguigu.gulimall.thirdparty.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void testSmsComponent(){

        smsComponent.sendSmsCode("13344445555", "111111");
    }

    @Test
    public void testSendSms(){

        String host = "https://smssend.shumaidata.com";
        String path = "/sms/send";
        String method = "POST";
        String appcode = "f2ecc815f63d46eeb5aa82ad974817af";
        Map<String, String> headers = new HashMap<String, String>();
        //最後在header中的格式(中間是英文空格)為Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("receive", "13344445555");
        querys.put("tag", "1314520");
        querys.put("templateId", "M09DD535F4");
        Map<String, String> bodys = new HashMap<String, String>();

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
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //獲取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州為例，其它Region請按實際情況填寫。
//        String endpoint = "oss-cn-hongkong.aliyuncs.com";
//        // 雲帳號AccessKey有所有API存取權限，建議遵循阿里雲安全最佳實務，建立並使用RAM子帳號進行API訪問或日常運維，請登入 https://ram.console.aliyun.com 建立。
//        String accessKeyId = "LTAI5tJHsUU7ww1DkaXbAY6u";
//        String accessKeySecret = "1cpQvXZ2quGqQCU0UXpHPMoz7DgaAT";
//        // 建立OSSClient執行個體。
//        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        // 上傳檔案流。
        InputStream inputStream = new FileInputStream("C:\\Users\\kurt\\Desktop\\pics\\apple.png");
        ossClient.putObject("gulimall-kurt", "haha.png", inputStream);
        // 關閉OSSClient。
        ossClient.shutdown();

        System.out.println("上傳完成...");
    }
}
