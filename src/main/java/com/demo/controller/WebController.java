package com.demo.controller;

import com.demo.exception.UnauthorizedException;
import com.demo.pojo.entity.User;
import com.demo.pojo.vo.ServerResponse;
import com.demo.service.UserService;
import com.demo.util.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class WebController {

    private UserService userService;

    @Autowired
    public void setService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ServerResponse login(@RequestParam("username") String username,
                                @RequestParam("password") String password) {
        User user = userService.queryByUsername(username);
        if (user.getPassword().equals(password)) {
            return ServerResponse.createBySuccess("登录成功", JWTUtil.sign(username, password));
        } else {
            throw new UnauthorizedException();
        }
    }

    @GetMapping("/users")
    public ServerResponse users() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            return ServerResponse.createBySuccess("You are already logged in");
        } else {
            return ServerResponse.createBySuccess("You are guest");
        }
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public ServerResponse requireAuth() {
        return ServerResponse.createBySuccess("You are authenticated");
    }

    @GetMapping("/require_role")
    @RequiresRoles("admin")
    public ServerResponse requireRole() {
        return ServerResponse.createBySuccess("You are visiting require_role");
    }

    @GetMapping("/require_permission")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    public ServerResponse requirePermission() {
        return ServerResponse.createBySuccess("You are visiting permission require edit,view");
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ServerResponse unauthorized() {
        return ServerResponse.createByUnauthorized();
    }
}
