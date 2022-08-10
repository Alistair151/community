package com.alistair.community.controller;

import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.Page;
import com.alistair.community.entity.User;
import com.alistair.community.service.DiscussPostService;
import com.alistair.community.service.LikeService;
import com.alistair.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        //方法调用前，springMVC会自动实例化Model与Page,并将page注入Model
        //所以，在thymeleaf中可以直接访问Page对象中的数据

        //查询总的帖子的行数，所以用户id设为0
        //帖子分页信息,其中current属性会自动从url中调用setCurrent方法传入到page对象中
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");


        //查询出的帖子结果集
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        //带有用户姓名的结果集
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post:list){
                Map<String , Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                //map中放入帖子赞的数量
                map.put("likeCount", likeService.findEntityLikeCount(1, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
