package com.alistair.community.service;

import com.alistair.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //判断是点赞还是取消赞， 即userId在不在集合里
        boolean ifLike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (ifLike) {
            // 取消赞
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        }else {
            // 点赞
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }

    // 查询某个实体被点赞的数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某用户是否对实体点赞(用int型是因为将来有可能有点踩的功能)
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //判断是点赞还是取消赞， 即userId在不在集合里
        boolean ifLike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        return ifLike ? 1 : 0;
    }
}
