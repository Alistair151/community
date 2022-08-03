package com.alistair.community.controller;

import com.alistair.community.entity.User;
import com.alistair.community.service.LikeService;
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
public class LikeController {

   @Autowired
   private LikeService likeService;

   @Autowired
   private HostHolder hostHolder;

   // 异步请求用post
   @RequestMapping(path = "/like", method = RequestMethod.POST)
   @ResponseBody
   public String like(int entityType, int entityId, int authorId) {
       User user = hostHolder.getUsers();
       if (user == null) {
           return CommunityUtil.getJsonString(1, "用户未登录");
       }
       // 点赞
       likeService.like(user.getId(), entityType, entityId, authorId);
       // 数量
       long likeCount = likeService.findEntityLikeCount(entityType, entityId);
       // 状态
       int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

       Map<String, Object> map = new HashMap<>();
       map.put("likeCount", likeCount);
       map.put("likeStatus", likeStatus);

       return CommunityUtil.getJsonString(0,"成功", map);

   }

}
