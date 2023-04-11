package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.order.model.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import feign.Param;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //查询当前登录用户订单列表以及订单明细内容
    IPage<OrderInfo> getOrderList(IPage iPage, String orderStatus, @Param("userId") String userId);
}
