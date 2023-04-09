package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.model.UserInfo;
import com.atguigu.gmall.user.service.UserInfoService;
import lombok.experimental.Tolerate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/user")
public class PassportController {

    @Autowired
    private UserInfoService userInfoService;


    /**
     * 用户登录接口，也可提供给PC端小程序端使用
     *
     * @param loginUser
     */
    @PostMapping("/passport/login")
    public Result login(@RequestBody UserInfo loginUser, HttpServletRequest request){
        return userInfoService.login(loginUser, request);
    }

    /**
     * 用户退出系统
     *
     * @param token
     */
    @GetMapping("/passport/logout")
    public Result logout(@RequestHeader("token") String token){
        userInfoService.logout(token);
        return Result.ok();
    }
}
