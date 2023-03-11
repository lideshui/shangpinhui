package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 订单确认页面数据模型汇总
     * @return
     */
    Map<String, Object> tradeDatas(String userId);

    /**
     * 提交订单，成功后返回订单ID
     * @param orderInfo
     * @return
     */
    Long submitOrder(OrderInfo orderInfo, String tradeNo);

    /**
     * 生成流水号
     * @param userId
     * @return
     */
    String generateTradeNo(String userId);


    /**
     * 验证流水号是否一致
     * @param userId
     * @param tradeNo
     * @return
     */
    Boolean checkTradeNo(String userId, String tradeNo);


    /**
     * 删除业务流水号
     * @param userId
     */
    void deleteTradeNo(String userId);
}
