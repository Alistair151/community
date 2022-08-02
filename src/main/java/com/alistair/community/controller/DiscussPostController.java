package com.alistair.community.controller;

import com.alistair.community.annotation.LoginRequired;
import com.alistair.community.entity.Comment;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.Page;
import com.alistair.community.entity.User;
import com.alistair.community.service.CommentService;
import com.alistair.community.service.DiscussPostService;
import com.alistair.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUsers();
        if(user == null) {
            return CommunityUtil.getJsonString(403, "没有登录，不能发布");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //报错的情况，将来统一处理
        return CommunityUtil.getJsonString(0, "发布成功");
    }

    //查询帖子详情
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //对与page，实体类型的参数，会自动装载到model中，不需要额外addAttribute
        // 帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);
        // 作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = user==null?0:likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息,其中current属性会自动从url中调用setCurrent方法传入到page对象中
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        //查询出的对帖子的评论
        List<Comment> commentList =  commentService.findCommentsByEntity(
                ENTITY_TYPE_POST,discussPostId, page.getOffset(),page.getLimit());
        //显示的对象
        List<Map<String, Object>>  commentViewList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                //某一个评论
                Map<String, Object> commentView = new HashMap<>();
                //评论放进入
                commentView.put("comment", comment);
                //作者放进去
                commentView.put("user", userService.findUserById(comment.getUserId()));

                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = user==null?0:likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("likeStatus", likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(),0,Integer.MAX_VALUE);
                //回复的Vo列表
                List<Map<String, Object>> replyViewList = new ArrayList<>();
                if (replyList != null) {
                    for(Comment reply : replyList) {
                        //某一个回复
                        Map<String, Object> replyView = new HashMap<>();
                        //将回复放进入
                        replyView.put("reply", reply);
                        //将作者放进去
                        replyView.put("user", userService.findUserById(reply.getUserId()));
                        //将回复目标放进去
                        replyView.put("target", reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId()));
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyView.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = user==null?0:likeService.findEntityLikeStatus(user.getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyView.put("likeStatus", likeStatus);
                        replyViewList.add(replyView);
                    }
                }
                //将这条评论的所有回复放进去
                commentView.put("replies", replyViewList);
                //回复的数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentView.put("replyCount", replyCount);
                commentViewList.add(commentView);
            }
        }
        //将需要显示的评论的所有数据加入到model中
        model.addAttribute("comments", commentViewList);
        return "/site/discuss-detail";
    }



}
