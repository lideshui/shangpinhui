package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.user.model.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/inner")
public class UserApiController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 根据用户ID查询用户收件地址薄列表
     *
     * @param userId
     */
    @GetMapping("/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") Long userId){
        return userAddressService.findUserAddressListByUserId(userId);
    }

}
