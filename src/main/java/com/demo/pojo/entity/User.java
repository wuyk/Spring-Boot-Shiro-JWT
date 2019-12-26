package com.demo.pojo.entity;

import lombok.Data;
import lombok.ToString;

/**
 * 用户实体类
 */
@Data
@ToString
public class User {

    private Integer id;

    private String username;

    private String password;

}
