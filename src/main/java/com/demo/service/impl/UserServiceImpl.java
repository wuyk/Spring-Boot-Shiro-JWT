package com.demo.service.impl;

import com.demo.mapper.UserMapper;
import com.demo.pojo.entity.User;
import com.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by WUYK on 2019-12-26.
 */
@Service
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User queryByUsername(String username) {
        return userMapper.queryByUsername(username);
    }
}
