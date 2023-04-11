package com.atguigu.gmall.order.service;

import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface OrderInfoService extends IService<OrderInfo> {

    //订单确认页面数据模型汇总
    Map<String, Object> tradeDatas(String userId);

    //在订单确认页面保存订单，响应成功保存的订单的ID
    Long submitOrder(OrderInfo orderInfo, String tradeNo);

    //调用第三方仓库存储系统进行验证商品库存是否充足
    boolean checkStock(Long skuId, Integer skuNum);

}
