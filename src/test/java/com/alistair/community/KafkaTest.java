package com.alistair.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//启用CommunityApplication作为配置类
public class KafkaTest {

    @Autowired
    private MyConsumer consumer;

    @Autowired
    private MyProducer producer;

    @Test
    public void kafkaTest() throws InterruptedException {
        producer.sendMessage("test", "你好");
        producer.sendMessage("test", "在吗");
        // sleep3秒钟，留时间给消费者，让我们能够结果
        Thread.sleep(3000);
    }

}

@Component
class MyProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}

@Component
class MyConsumer {
    //试图读取test的主题，有消息立马读取，没消息就阻塞
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println("这里是消费者， 我收到消息：" +record.value());
    }
}