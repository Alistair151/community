package com.alistair.community.service;

import com.alistair.community.dao.CommentMapper;
import com.alistair.community.entity.Comment;
import com.alistair.community.util.CommunityConstant;
import com.alistair.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityID, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityID, offset, limit);
    }

    public int findCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    //增加评论, 用声明式事务来管理事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment == null) {
            new IllegalArgumentException("参数不能为空");
        }
        //处理评论内容
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        //添加评论
        int rows = commentMapper.insertComment(comment);

        //更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

}
