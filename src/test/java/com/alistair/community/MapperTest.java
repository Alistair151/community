package com.alistair.community;

import com.alistair.community.dao.DiscussPostMapper;
import com.alistair.community.dao.LoginTicketMapper;
import com.alistair.community.dao.UserMapper;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.LoginTicket;
import com.alistair.community.entity.User;
import com.alistair.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void selectTest(){
        User user1 = userMapper.selectById(101);
        System.out.println(user1);

        User user2 = userMapper.selectByName("guanyu");
        System.out.println(user2);

        User user3 = userMapper.selectByEmail("nowcoder102@sina.com");
        System.out.println(user3 );
    }

    @Test
    public void insertTest(){
        User user = new User();
        user.setPassword("123456");
        user.setHeaderUrl("http://www.nowcoder.com/99.png");
        user.setActivationCode("11111");
        user.setUsername("test");
        user.setCreateTime(new Date());
        user.setEmail("test@test.com");

        int row = userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user.getId());
    }

    @Test
    public  void updateTest(){
        int rows = userMapper.updateHeader(150, "http://www.nowcoder.com/1.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "654321");
        System.out.println(rows);

        rows = userMapper.updateStatus(150,0);
        System.out.println(rows);
    }


    /**
     * 测试discuss_post
     */
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void discussPostsSelectTest(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0,1,10);
        for(DiscussPost post :list){
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }


    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    public void insertLoginTicketTest(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(2312);
        loginTicket.setTicket("alistair");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void selectLoginTicketTest(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("alistair");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("alistair", 1);

        loginTicket = loginTicketMapper.selectByTicket("alistair");
        System.out.println(loginTicket);
    }

    @Test
    public void updatePassword(){
        userMapper.updatePassword(166, CommunityUtil.md5("123"+"5fdb6"));
    }
}
