package com.atguigu.gulimall.member;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {
        //e10adc3949ba59abbe56e057f20f883e
        //抗修改性: 彩虹表: 123456->XXXX  234567->XXXX
        String s = DigestUtils.md5Hex("123456");

        //MD5不能直接進行密碼的加密存儲;

        //鹽值加密: 隨機值  加鹽: $1+8位字符
        //$1$92/yIWgF$lLu.qP7629RqytbTboF.P0
        // 第一次 : $1$qqqqqqqq$AZofg3QwurbxV3KEOzwuI1    123456
        // 第二次 : $1$qqqqqqqq$AZofg3QwurbxV3KEOzwuI1   還是一樣，只要鹽值一樣
        // 驗證: 123456進行鹽值(去數據庫查)加密，數據庫需要保存鹽值
//        String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$qqqqqqqq");
//        System.out.println(s1);


        //springframework.security.crypto.bcrypt;
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 第一次 : $2a$10$FQPCJv1j/PucIOqSb7q68uGht9zw26D9R14o9eNg9TkmfD/ExF4Xm
        // 第二次 : $2a$10$RcdOotPmqNRYbKbY.Qo8jePTWElFSNSFerfrf3L2/1qB5MAIhnphq
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$FQPCJv1j/PucIOqSb7q68uGht9zw26D9R14o9eNg9TkmfD/ExF4Xm");
        boolean matches2 = passwordEncoder.matches("123456", "$2a$10$RcdOotPmqNRYbKbY.Qo8jePTWElFSNSFerfrf3L2/1qB5MAIhnphq");


        System.out.println(encode + " => " + matches);
        System.out.println(encode + " => " + matches2);

//        System.out.println(s);
    }

}
