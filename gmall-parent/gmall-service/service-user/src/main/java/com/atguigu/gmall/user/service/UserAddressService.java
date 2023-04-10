package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.model.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户地址表 业务接口类
 */
public interface UserAddressService extends IService<UserAddress> {

    //根据用户ID查询用户收件地址薄列表
    List<UserAddress> findUserAddressListByUserId(Long userId);

}
