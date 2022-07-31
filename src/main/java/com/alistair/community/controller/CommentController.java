package com.alistair.community.controller;

import com.alistair.community.entity.Comment;
import com.alistair.community.service.CommentService;
import com.alistair.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    //用post来接受评论的数据,路径中的entityId用于重定向是找到页面
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        //设置评论，其中content等内容有表单提交后自动set
        comment.setUserId(hostHolder.getUsers().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        //添加评论
        commentService.addComment(comment);

        //重定向到帖子详情（post的url与详情url不同，需要重定向）
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
