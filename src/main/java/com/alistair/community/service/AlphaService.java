package com.alistair.community.service;

import com.alistair.community.dao.AlphaDao;
import com.alistair.community.dao.DiscussPostMapper;
import com.alistair.community.dao.UserMapper;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.User;
import com.alistair.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public String find(){
        return alphaDao.select();
    }

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destory(){
        System.out.println("销毁AlphaService");
    }


    // 模拟事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("https://s2.loli.net/2022/07/28/Y2dJOUeHG6scQiD.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //新增帖子
        DiscussPost discussPost = new DiscussPost();
        //insert后mybatis会向数据库要id
        discussPost.setUserId(user.getId());
        discussPost.setTitle("hello");
        discussPost.setContent("新人报到，请多多关照");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        //人为地报个错，将字母转成整数，使事务回滚
        Integer.valueOf("abc");
        return "ok";
    }

    //编程式管理事务
    @Autowired
    private TransactionTemplate transactionTemplate;

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior((TransactionDefinition.PROPAGATION_REQUIRED));
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("https://s2.loli.net/2022/07/28/Y2dJOUeHG6scQiD.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                //新增帖子
                DiscussPost discussPost = new DiscussPost();
                //insert后mybatis会向数据库要id
                discussPost.setUserId(user.getId());
                discussPost.setTitle("hello");
                discussPost.setContent("新人报到，请多多关照");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                //人为地报个错，将字母转成整数，使事务回滚
                Integer.valueOf("abc");
                return "ok";
            }
        });

        return "ok";
    }


}
