package com.demo.service;

import com.demo.pojo.entity.User;

/**
 * Created by WUYK on 2019-12-26.
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    User queryByUsername(String username);
}
