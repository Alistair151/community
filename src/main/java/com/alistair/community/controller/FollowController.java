package com.alistair.community.controller;

import com.alistair.community.annotation.LoginRequired;
import com.alistair.community.entity.User;
import com.alistair.community.service.FollowService;
import com.alistair.community.util.CommunityUtil;
import com.alistair.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolderl;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolderl.getUsers();
        followService.follow(user.getId(), entityType, entityId);
        Map<String, Object> map = new HashMap<>();
        map.put("followCount", followService.findFollowerCount(entityType,entityId));
        return CommunityUtil.getJsonString(0, "已关注", map);
    }

    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        System.out.println("entityType: " + entityType);
        System.out.println("entityId: "+ entityId);
        User user = hostHolderl.getUsers();
        followService.unFollow(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("followCount", followService.findFollowerCount(entityType,entityId));

        return CommunityUtil.getJsonString(0, "已取消关注", map);
    }

}
