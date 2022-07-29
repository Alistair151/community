package com.alistair.community;

import com.alistair.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void setSensitiveFilterTest(){
        String text = "这里可以赌博，fuck u, bull shit, 嫖娼开票吸@毒, ";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}
