package com.alistair.community.controller;

import com.alistair.community.entity.Message;
import com.alistair.community.entity.Page;
import com.alistair.community.entity.User;
import com.alistair.community.service.MessageService;
import com.alistair.community.service.UserService;
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
@RequestMapping("/message")
public class MessageController {

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

}
