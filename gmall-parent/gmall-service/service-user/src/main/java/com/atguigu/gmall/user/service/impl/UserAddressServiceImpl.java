package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.user.model.UserAddress;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户地址表 业务实现类
 */

@Service
public class  UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    /**
     * 根据用户ID查询用户收件地址薄列表
     *
     * @param userId
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        LambdaQueryWrapper<UserAddress> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserAddress::getUserId,userId);
        return this.list(lambdaQueryWrapper);
    }

}
