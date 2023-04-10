package com.atguigu.gmall.user.client;

import com.atguigu.gmall.user.client.impl.UserDegradeFeignClient;
import com.atguigu.gmall.user.model.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "service-user", fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {


    /**
     * 根据用户ID查询用户收件地址薄列表
     *
     * @param userId
     */
    @GetMapping("/api/user/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") Long userId);

}
