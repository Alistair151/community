package com.alistair.community.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alistair.community.dao.MessageMapper;
import com.alistair.community.entity.DiscussPost;
import com.alistair.community.entity.Event;
import com.alistair.community.entity.Message;
import com.alistair.community.service.DiscussPostService;
import com.alistair.community.service.ElasticsearchService;
import com.alistair.community.service.MessageService;
import com.alistair.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    //在一个方法中，处理多种主题
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleEvent(ConsumerRecord record){
        if (record == null || record.value() ==null){
            logger.error("传入的系统消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息格式错误");
        }
        //将事件存到message表中作为系统消息
        Message message = new Message();
        //将topic作为message的conversation_id
        message.setConversationId(event.getTopic());
        //系统用户作为发送方
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());

        //将其余的数据（什么类型，内容id等等放入content中）
        Map<String, Object> map = new HashMap<>();
        map.put("entityType", event.getEntityType());
        map.put("entityId", event.getEntityId());
        map.put("userId", event.getUserId());
        //还有event中data属性（可供扩展）
        if(event.getData() != null){
            for(Map.Entry<String, Object> entry : event.getData().entrySet()){
                map.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(map));
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() ==null){
            logger.error("传入的消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息格式错误");
        }

        //将帖子传入
        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(discussPost);

    }

}
