package com.alistair.community.service;

import com.alistair.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;

@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 关注,某用户对某类型的实体，关注了这些id
    public void follow(int userId, int entityType, int entityId){
        // 关注列表与粉丝列表需要一起变，所以用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                //关注
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                //粉丝
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }
    // 取关
    public void unFollow(int userId, int entityType, int entityId){
        // 关注列表与粉丝列表需要一起变，所以用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
                operations.multi();
                //关注
                operations.opsForZSet().remove(followeeKey, entityId);
                //粉丝
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    // 查询某用户对实体的关注数量
    public long findFolloweeCount(int entityType, int userId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询某实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询当前用户是否已关注某实体
    public boolean findHasFollow(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().score(followeeKey, userId) != null;
    }

}
