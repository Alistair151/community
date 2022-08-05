package com.alistair.community.controller;

import com.alistair.community.annotation.LoginRequired;
import com.alistair.community.entity.Event;
import com.alistair.community.entity.Page;
import com.alistair.community.entity.User;
import com.alistair.community.event.EventProducer;
import com.alistair.community.service.FollowService;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityConstant;
import com.alistair.community.util.CommunityUtil;
import com.alistair.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUsers();
        followService.follow(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("followCount", followService.findFollowerCount(entityType,entityId));

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "已关注", map);
    }

    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        System.out.println("entityType: " + entityType);
        System.out.println("entityId: "+ entityId);
        User user = hostHolder.getUsers();
        followService.unFollow(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("followCount", followService.findFollowerCount(entityType,entityId));

        return CommunityUtil.getJsonString(0, "已取消关注", map);
    }

    //关注列表
    @RequestMapping(path = "/followee/{userId}", method = RequestMethod.GET)
    public String getFolloweePage(@PathVariable("userId") int userId, Page page, Model model) {
        //检查用户是否存在
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //设置分页信息
        page.setPath("/followee/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(ENTITY_TYPE_USER, userId));
        //查询关注列表的信息
        List<Map<String, Object>> userList = followService.findFolloweeUsers(userId, page.getOffset(), page.getLimit());
        if (user != null){
            //看看”我“是否关注了列表里的这些人
            for(Map<String, Object> map:userList){
                User followeeUSer = (User) map.get("user");
                boolean isFollow = hasFollowed(followeeUSer.getId());
                map.put("isFollow", isFollow);
            }
        }
        model.addAttribute("userList", userList);
        model.addAttribute("userId",userId);
        return "/site/followee";
    }

    //我在浏览他人的关注列表时，看看他关注的人我是否关注了
    private boolean hasFollowed(int userId){
        User me = hostHolder.getUsers();
        return followService.findHasFollow(me.getId(), ENTITY_TYPE_USER, userId);
    }

    //粉丝列表
    @RequestMapping(path = "/follower/{userId}", method = RequestMethod.GET)
    public String getFollowerPage(@PathVariable("userId") int userId, Page page, Model model) {
        //检查用户是否存在
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //设置分页信息
        page.setPath("/follower/" + userId);
        page.setLimit(5);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        //查询关注列表的信息
        List<Map<String, Object>> userList = followService.findFollowerUsers(userId, page.getOffset(), page.getLimit());
        if (user != null){
            //看看”我“是否关注了列表里的这些人
            for(Map<String, Object> map:userList){
                User followerUser = (User) map.get("user");
                boolean isFollow = hasFollowed(followerUser.getId());
                map.put("isFollow", isFollow);
            }
        }
        model.addAttribute("userList", userList);
        model.addAttribute("userId",userId);
        return "/site/follower";
    }

}
