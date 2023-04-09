package com.atguigu.gmall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证接口
 */
@Controller
public class PassportController {


    /**
     * 渲染登录页面
     *
     * @param originUrl 登录前用户访问地址，登录成功后重定向到该地址
     */
    @GetMapping("/login.html")
    public String loginHtml(@RequestParam(value = "originUrl", required = false) String originUrl, Model model) {
        //给页面中用于调整登录前页面进行赋值
        model.addAttribute("originUrl", originUrl);
        return "/login";
    }
}