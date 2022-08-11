package com.alistair.community.service;

import com.alistair.community.dao.UserMapper;
import com.alistair.community.entity.LoginTicket;
import com.alistair.community.entity.User;
import com.alistair.community.util.CommunityConstant;
import com.alistair.community.util.CommunityUtil;
import com.alistair.community.util.MailClient;
import com.alistair.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Value("{${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    //查询用户
    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null) {
            user = initCache(id);
        }
        return user;
    }


    //注册用户
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("userPasswordMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("userEmailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号
        User exist = userMapper.selectByName(user.getUsername());
        if (exist != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }

        //验证邮箱
        exist = userMapper.selectByEmail(user.getEmail());
        if (exist != null) {
            map.put("userEmailMsg", "该邮箱已经注册过账号");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/id/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //System.out.println(content);
        return  map;
    }

    //激活用户
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            clearUserCache(userId);
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    //验证登录
    public Map<String, Object> login(String username, String password, int expiredSecond){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //账号合法性验证
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        //密码合法性验证
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSecond * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        redisTemplate.expire(redisKey, expiredSecond, TimeUnit.SECONDS);

        //登陆成功后将凭证放到map中
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    //退出登录
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        //取出
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        //放回
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    //查询登录凭证
    public LoginTicket findLoginTicket(String ticket) {
//         return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    //更新头像
    public int updateHeader(int userId, String headerUrl) {
        clearUserCache(userId);
        return userMapper.updateHeader(userId, headerUrl);
    }

    //修改密码
    public Map<String, Object> updatePassword(User user, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldMsg", "密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(newPassword)) {
            map.put("newMsg", "密码不能为空");
            return map;
        }
        //合法性检查
        if(!CommunityUtil.md5(oldPassword + user.getSalt()).equals(user.getPassword())){
            map.put("oldMsg", "密码错误");
            return map;
        }
        //数据合法说明可以修改密码
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword + user.getSalt()));
        return map;
    }

    public User findUserByName(String  username){
        return userMapper.selectByName(username);
    }

    //优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 1, TimeUnit.HOURS);
        return user;
    }

    //数据变更时清除缓存数据
    private void clearUserCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 0:
                        return AUTHORITY_USER;
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return "null";
                }
            }
        });

        return list;
    }

}
