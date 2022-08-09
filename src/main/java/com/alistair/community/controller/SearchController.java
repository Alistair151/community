package com.alistair.community.controller;

import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.Page;
import com.alistair.community.service.ElasticsearchService;
import com.alistair.community.service.LikeService;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //search?keyword=XXX
    @RequestMapping(path = "search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        //设置分页信息
        page.setRows((int) elasticsearchService.findDiscussPostCount(keyword));
        page.setPath("/search?keyword=" + keyword);

        //搜索帖子
        List<DiscussPost> discussPostList =  elasticsearchService.findDiscussPost(keyword, page.getOffset(), page.getLimit());
        //聚合数据
        List<Map<String, Object>> discussPostViewList = new ArrayList<>();
        if(discussPostList != null){
            for(DiscussPost discussPost : discussPostList){
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", discussPost);
                //作者
                map.put("user", userService.findUserById(discussPost.getUserId()));
                //点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                discussPostViewList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPostViewList);
        model.addAttribute("keyword", keyword);
        return "/site/search";
    }

}
