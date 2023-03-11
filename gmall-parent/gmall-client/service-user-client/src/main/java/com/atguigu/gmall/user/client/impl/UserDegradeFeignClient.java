package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.user.model.UserAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDegradeFeignClient implements UserFeignClient {
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        return null;
    }
}
