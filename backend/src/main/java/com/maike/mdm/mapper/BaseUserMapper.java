package com.maike.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maike.mdm.entity.BaseUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BaseUserMapper extends BaseMapper<BaseUser> {

    @Select("SELECT * FROM BASE_USER WHERE USERNAME = #{username} LIMIT 1")
    BaseUser selectByUsername(@Param("username") String username);
}