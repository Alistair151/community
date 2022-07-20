package com.alistair.community;

import com.alistair.community.dao.AlphaDao;
import com.alistair.community.dao.AlphaDaoHibernateImpel;
import com.alistair.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.ApplicationContextAssertProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
class CommunityApplicationTests implements ApplicationContextAware
{

    private  ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext(){
        System.out.println(applicationContext);

        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);//通过容器获得bean
        System.out.println(alphaDao.select());

        AlphaDao alphaDao1 = applicationContext.getBean("H",AlphaDao.class);//不加第二个参数则为object需要转型
        System.out.println(alphaDao1.select());
    }

    @Test
    public void  testBeanManagement(){
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);

        AlphaService alphaService1 = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService1);
    }

    @Test
    public  void testBeanConfig(){
        SimpleDateFormat simpleDateFormat =
                applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired
    @Qualifier("H")
    private AlphaDao alphaDao;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Test
    public void testDI(){
        System.out.println(alphaDao);
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }
}
