package com.maike.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maike.mdm.entity.BaseUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BaseUserMapper extends BaseMapper<BaseUser> {

    BaseUser selectByUsername(@Param("username") String username);
}