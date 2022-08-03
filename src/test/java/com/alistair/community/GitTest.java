package com.alistair.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
public class GitTest {
    //测试一下Git能不能成功上传到github

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void test() {
        Integer obj = null;
        long testLong = Long.valueOf(obj);
        System.out.println(testLong);
    }


}
