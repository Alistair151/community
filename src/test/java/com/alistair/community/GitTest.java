package com.alistair.community;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
public class GitTest {
    //测试一下Git能不能成功上传到github

    @Autowired
    private JavaMailSender mailSender;


}
