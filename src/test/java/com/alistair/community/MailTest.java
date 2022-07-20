package com.alistair.community;

import com.alistair.community.CommunityApplication;
import com.alistair.community.util.MailClient;
import com.alistair.community.util.MailClient;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;




    @Test
    public void testTextMail() {
        mailClient.sendMail("hnu_liweijia@foxmail.com", "TEST", "Welcome.");
    }


    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "alistair");

        String  content = templateEngine.process("mail/demo",context);
        System.out.println(content);

        mailClient.sendMail("hnu_liweijia@foxmail.com","THML",content);
    }


}