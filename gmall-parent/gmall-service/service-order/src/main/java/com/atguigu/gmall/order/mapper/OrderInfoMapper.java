package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.order.model.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.data.repository.query.Param;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    IPage<OrderInfo> getOrderList(IPage iPage, String orderStatus, @Param("userId") String userId);
}
