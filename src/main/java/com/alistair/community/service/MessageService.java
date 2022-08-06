package com.alistair.community.service;

import com.alistair.community.dao.MessageMapper;
import com.alistair.community.entity.Message;
import com.alistair.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetter(String conversationId, int offset, int limit){
        return messageMapper.selectLetter(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    // 添加一条消息
    public int addMessage(Message message){
        //处理文本
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        //调用数据层
        return messageMapper.insertMessage(message);
    }

    // 读消息
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    // 查询某个主题下最新的通知
    public Message findLatestNotice(int userId, String topic){
        return messageMapper.selectLatestNotice(userId, topic);
    }

    // 查询某个主题所包含的通知数量
    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticeCount(userId, topic);
    }

    // 查询未读的统治的数量
    public int findUnreadNoticeCount(int userId, String topic){
        return messageMapper.selectUnreadNoticeCount(userId, topic);
    }

    // 查询某一主题的所有系统消息
    public List<Message> findNoticeList(int userId, String topic, int offset, int limit){
        return messageMapper.selectNoticeList(userId, topic, offset, limit);
    }
}
