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
        content = "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "\t<meta charset=\"utf-8\">\n" +
                "\t<link rel=\"icon\" href=\"https://static.nowcoder.com/images/logo_87_87.png\"/>\n" +
                "\t<title>牛客网-激活账号</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div>\n" +
                "\t<p>\n" +
                "\t\t<b>1518016264@qq.com</b>, 您好!\n" +
                "\t</p>\n" +
                "\t<p>\n" +
                "\t\t您正在注册牛客网, 这是一封激活邮件, 请点击\n" +
                "\t\t<a href=\"http://127.0.0.1:8080/community/activation/158/a1bfb56e-79c6-47cb-909b-3071296c330a\">此链接</a>,\n" +
                "\t\t激活您的牛客账号!\n" +
                "\t</p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";
        mailClient.sendMail("hnu_liweijia@foxmail.com","THML",content);
    }


}