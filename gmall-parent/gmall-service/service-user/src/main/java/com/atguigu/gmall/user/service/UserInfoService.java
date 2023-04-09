package com.atguigu.gmall.user.service;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.model.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户表 业务接口类
 */

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户登录接口，也可提供给PC端小程序端使用
     *
     * @param loginUser
     */
    Result login(UserInfo loginUser, HttpServletRequest request);

    /**
     * 用户退出系统
     *
     * @param token
     */
    void logout(String token);
}
