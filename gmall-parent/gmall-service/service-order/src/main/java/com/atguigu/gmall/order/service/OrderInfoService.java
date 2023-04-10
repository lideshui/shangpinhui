package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 订单确认页面数据模型汇总
     */
    Map<String, Object> tradeDatas(String userId);

}
