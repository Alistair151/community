package com.alistair.community.controller;

import com.alistair.community.entity.Comment;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.Event;
import com.alistair.community.event.EventProducer;
import com.alistair.community.service.CommentService;
import com.alistair.community.service.DiscussPostService;
import com.alistair.community.util.CommunityConstant;
import com.alistair.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;


    //用post来接受评论的数据,路径中的entityId用于重定向是找到页面
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        //设置评论，其中content等内容有表单提交后自动set
        comment.setUserId(hostHolder.getUsers().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        //添加评论
        commentService.addComment(comment);


        //创建事件，发送系统通知
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //根据不同的类型找到作者
            DiscussPost entityPost = discussPostService.findDiscussPostById(discussPostId);
            event.setEntityUserId(entityPost.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment entityComment = null;
            if(comment.getTargetId() == 0){
                //对评论的回复
                entityComment = commentService.findCommentById(comment.getEntityId());
            }else {
                //对回复的回复
                entityComment = commentService.findCommentById(comment.getTargetId());
            }
            event.setEntityUserId(entityComment.getUserId());
        }

        eventProducer.fireEvent(event);

        //触发修改帖子的事件
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUsers().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }


        //重定向到帖子详情（post的url与详情url不同，需要重定向）
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
