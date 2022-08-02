package com.alistair.community.util;

public class RedisKeyUtil {

    // 以冒号分隔变量的单词
    public static final String SPLIT = ":";
    // 帖子与评论的赞的前缀
    public static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 获得key的字符串方法
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

}
