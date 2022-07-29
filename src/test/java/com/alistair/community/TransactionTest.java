package com.alistair.community;

import com.alistair.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
public class TransactionTest {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void save1Test() {
        alphaService.save1();
    }

    @Test
    public void save2Test() {
        alphaService.save2();
    }

}
