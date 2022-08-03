package com.alistair.community.util;

public class RedisKeyUtil {

    // 以冒号分隔变量的单词
    private static final String SPLIT = ":";
    // 帖子与评论的赞的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 用户收到的赞
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注列表
    private static final String PREFIX_FOLLOWEE = "followee";
    // 粉丝列表
    private static final String PREFIX_FOLLOWER = "follower";

    // 获得key的字符串方法
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> ()
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 获得用户关注的实体的key， 其中entityTYpe
    // followee:userId:entityType ->zSet(entityId, now);
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝的key， 其中entityTYpe
    // follower:entityType:entityId ->zSet(userId, now);
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

}
