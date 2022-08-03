package com.alistair.community.service;

import com.alistair.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞,
    // 每次给一个内容点赞，内容的作者的个人主页的赞的数量也需要增加
    public void like(int userId, int entityType, int entityId, int authorId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        //判断是点赞还是取消赞， 即userId在不在集合里
//        boolean ifLike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (ifLike) {
//            // 取消赞
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        }else {
//            // 点赞
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }

        // 点赞，内容的赞，与用户的赞都需要发生变化，要么都变，要么都不变，所以应该用事务管理
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 注意，这里的参数不能填userId,而是应该填写内容的作者。可以通过MessageService来查询
                // 但是要通过mysql，访问硬盘，完全摒弃了redis内存数据库的优势。
                // 所以，应该重构这个方法的参数列表，将作者的Id直接通过前端传进来authorId
                String userLikeKey = RedisKeyUtil.getUserLikeKey(authorId);
                // 查询用户的是否给作者的这篇内容点过赞
                boolean isLike = operations.opsForSet().isMember(entityLikeKey,userId);
                // 开启事务
                operations.multi();
                if (isLike) {
                    //取消
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else {
                    //赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                // 结束事务
                return operations.exec();
            }
        });
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

    // 查询某个用户收到的赞
    public int findUserLikeCount(int authorId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(authorId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count;
    }
}
