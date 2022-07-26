package com.alistair.community;

import com.alistair.community.dao.DiscussPostMapper;
import com.alistair.community.dao.LoginTicketMapper;
import com.alistair.community.dao.MessageMapper;
import com.alistair.community.dao.UserMapper;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.LoginTicket;
import com.alistair.community.entity.Message;
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

    @Autowired
    private MessageMapper messageMapper;


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
        userMapper.updatePassword(111, CommunityUtil.md5("123"+"167f9"));
    }

    @Test
    public void insertDiscussPostTest() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(166);
        discussPost.setStatus(0);
        discussPost.setCreateTime(new Date());
        discussPost.setType(0);
        discussPost.setContent("今天天气真好，我发布了一个帖子");
        discussPost.setCommentCount(0);
        discussPost.setScore(0);
        discussPostMapper.insertDiscussPost(discussPost);
    }

    //测试私信
    @Test
    public void messageTest() {
        List<Message> messageList = messageMapper.selectConversations(111,0,20);
        for (Message message:messageList){
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        messageList =  messageMapper.selectLetter("111_112",0,10);
        for (Message message:messageList){
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);


        count = messageMapper.selectLetterUnreadCount(131,null);
        System.out.println(count);
    }

    @Test
    public void sendMessage(){
        Message message = new Message();
        message.setCreateTime(new Date());
        message.setContent("haniyasiyou");
        message.setFromId(111);
        message.setToId(166);
        message.setConversationId("111_166");
        messageMapper.insertMessage(message);
    }
}
