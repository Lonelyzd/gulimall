package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSS;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    @Autowired
    OSS ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void contextLoads() throws FileNotFoundException {
        InputStream is = new FileInputStream("G:\\BaiduNetdiskDownload\\bf9185c2f70a015caa887123155d758.png");
        ossClient.putObject("gulimall-blsz", "test00001.jpg", is);
        ossClient.shutdown();
        System.out.println("上传完成");
    }

    public static void main(String[] args) {
    }
    @Test
    public void sendSmsTets(){


//        smsComponent.sendSmsCode("15656779710","12333");
    }

}
