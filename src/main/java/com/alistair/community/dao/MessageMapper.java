package com.alistair.community.dao;

import com.alistair.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每个会话只返回最新的一条私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetter(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读的私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 增加私信
    int insertMessage(Message message);

    // 修改消息状态，设置已读，或者删除
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的统治的数量
    int selectUnreadNoticeCount(int userId, String topic);

    // 查询某个主题包含的通知列表
    List<Message> selectNoticeList(int userId, String topic, int offset, int limit);

}
