package com.demo.mapper;

import com.demo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User queryByUsername(@Param("usermame") String username);

}
