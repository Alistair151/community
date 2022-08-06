package com.alistair.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.alistair.community.entity.Message;
import com.alistair.community.entity.Page;
import com.alistair.community.entity.User;
import com.alistair.community.service.MessageService;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityConstant;
import com.alistair.community.util.CommunityUtil;
import com.alistair.community.util.HostHolder;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        //当前用户
        User user = hostHolder.getUsers();

        //分页信息
        page.setLimit(5);
        page.setPath("/message/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> messageList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        //会话的显示列表
        List<Map<String, Object>> messageViewList = new ArrayList<>();
        if(messageList != null){
            for (Message message:messageList){
                Map<String ,Object> map = new HashMap<>();
                //放入message信息
                map.put("conversation",message);
                //放入未读消息数量
                int unreadCount = messageService.findLetterUnreadCount(user.getId(), message.getConversationId());
                map.put("unreadCount",unreadCount);
                //放入消息数量
                int letterCount = messageService.findLetterCount(message.getConversationId());
                map.put("letterCount", letterCount);
                //会话对方用户的信息
                int targetId = user.getId() == message.getFromId()?message.getToId():message.getFromId();
                User targer = userService.findUserById(targetId);
                map.put("target", targer);
                messageViewList.add(map);
            }
        }

        //总共的未读消息数量
        model.addAttribute("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("conversations", messageViewList);
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null/*sql语句规定topic为null查所有*/);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {
        User user = hostHolder.getUsers();
        // 分页信息
        page.setLimit(5);
        page.setPath("/message/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 会话的所有消息
        List<Message> letterList = messageService.findLetter(conversationId, page.getOffset(), page.getLimit());
        // 会话的显示列表
        List<Map<String, Object>> letterViewList = new ArrayList<>();
        if(letterList != null) {
            for(Message message : letterList){
                Map<String, Object> map = new HashMap<>();
                //放入消息
                map.put("letter",message);
                //放入发送方用户的信息
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letterViewList.add(map);
            }
        }
        model.addAttribute("letters", letterViewList);
        // 放入会话的另一端的用户
        int targetId = getLetterTargetId(conversationId);
        model.addAttribute("targetUser", userService.findUserById(targetId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    private int getLetterTargetId(String conversationId) {
        String[] targets =  conversationId.split("_");
        int id0 = Integer.parseInt(targets[0]);
        int id1 = Integer.parseInt(targets[1]);
        return hostHolder.getUsers().getId()==id0?id1:id0;
    }

    // 发送私信
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
        User user = hostHolder.getUsers();
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJsonString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setCreateTime(new Date());
        message.setToId(target.getId());
        message.setFromId(user.getId());
        message.setContent(content);
        message.setStatus(0);
        message.setConversationId(getConversationId(user.getId(),target.getId()));
        messageService.addMessage(message);
        return CommunityUtil.getJsonString(0,"发送成功");
    }

    private String getConversationId(int id1, int id2){
            return id1>id2?id2+"_"+id1:id1+"_"+id2;
    }

    //在一些消息列表中返回未读消息的id列表
    private List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            for (Message message : letterList) {
                if (message.getToId() == hostHolder.getUsers().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticePage(Model model){
        User me = hostHolder.getUsers();

        //查询评论类通知
        Message message = messageService.findLatestNotice(me.getId(), TOPIC_COMMENT);
        Map<String, Object> messageView = new HashMap<>();
        messageView.put("message", message);
        if (message != null){
            //通知的显示信息，包括通知的第一条，通知的对方，未读通知的数量
            Map<String, Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(message.getContent()),HashMap.class);
            messageView.put("user", userService.findUserById((Integer) data.get("userId")));
            messageView.put("entityType", data.get("entityType"));
            messageView.put("entityId", data.get("entityId"));
            int count = messageService.findNoticeCount(me.getId(), TOPIC_COMMENT);
            messageView.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(me.getId(), TOPIC_COMMENT);
            messageView.put("unreadCount", unreadCount);
        }
        model.addAttribute("commentNotice", messageView);

        //查询点赞类通知
        message = messageService.findLatestNotice(me.getId(), TOPIC_LIKE);
        messageView = new HashMap<>();
        messageView.put("message", message);
        if (message != null){
            //通知的显示信息，包括通知的第一条，通知的对方，未读通知的数量
            Map<String, Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(message.getContent()),HashMap.class);
            messageView.put("user", userService.findUserById((Integer) data.get("userId")));
            messageView.put("entityType", data.get("entityType"));
            messageView.put("entityId", data.get("entityId"));
            int count = messageService.findNoticeCount(me.getId(), TOPIC_LIKE);
            messageView.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(me.getId(), TOPIC_LIKE);
            messageView.put("unreadCount", unreadCount);
        }
        model.addAttribute("likeNotice", messageView);

        //查询关注类通知
        message = messageService.findLatestNotice(me.getId(), TOPIC_FOLLOW);
        messageView = new HashMap<>();
        messageView.put("message", message);
        if (message != null){
            //通知的显示信息，包括通知的第一条，通知的对方，未读通知的数量
            Map<String, Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(message.getContent()),HashMap.class);
            messageView.put("user", userService.findUserById((Integer) data.get("userId")));
//            messageView.put("postId", data.get("postId")); 关注行为只是个人不需要链接到相应帖子处
            int count = messageService.findNoticeCount(me.getId(), TOPIC_FOLLOW);
            messageView.put("count", count);
            int unreadCount = messageService.findUnreadNoticeCount(me.getId(), TOPIC_FOLLOW);
            messageView.put("unreadCount", unreadCount);
        }
        model.addAttribute("followNotice", messageView);

        //查询总的未读消息的数量，（包括个人私信与系统消息都需要显示）
        int unreadLetterCount = messageService.findLetterUnreadCount(me.getId(), null/*sql语句规定conversation-id为null查所有*/);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeCount = messageService.findUnreadNoticeCount(me.getId(), null/*sql语句规定topic为null查所有*/);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);
        return "/site/notice";
    }

    // 系统通知的详情页面
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page){
        User me = hostHolder.getUsers();

        page.setRows(messageService.findNoticeCount(me.getId(), topic));
        page.setLimit(5);
        page.setPath("/message/notice/detail/" + topic);

        List<Message> noticeList = messageService.findNoticeList(me.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>>  noticeViewList = new ArrayList<>();
        if(noticeList != null) {
            for(Message notice : noticeList){
                Map<String, Object> noticeView = new HashMap<>();
                //通知
                noticeView.put("notice", notice);
                // 内容
                Map<String,Object> data = JSONObject.parseObject(HtmlUtils.htmlUnescape(notice.getContent()));
                noticeView.put("user", userService.findUserById((Integer) data.get("userId")));
                noticeView.put("entityType", data.get("entityType"));
                noticeView.put("postId", data.get("postId"));
                noticeViewList.add(noticeView);
            }
        }
        model.addAttribute("notices", noticeViewList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";

    }

}
