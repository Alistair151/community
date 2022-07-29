package com.alistair.community.dao;

import com.alistair.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 查询出所有帖子，或者按照作者查询帖子，支持分页
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //查询帖子的总数
    //@Param用来给参数起别名
    //如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子详情
    DiscussPost selectDiscussPostById(int id);

}
