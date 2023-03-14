package com.atguigu.gmall.order.service;

import com.atguigu.gmall.enums.model.ProcessStatus;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.product.model.BaseAttrInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 验证库存
     * @param skuId
     * @param skuNum
     * @return
     */
    boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 查询当前登录用户订单列表(包括订单明细)
     *
     * @param iPage
     * @param userId
     * @param status
     * @return
     */
    IPage<OrderInfo> getOrderList(IPage<OrderInfo> iPage, String userId, String status);





    /**
     * 关闭订单
     * @param orderId
     */
    void execExpiredOrder(Long orderId);

    /**
     * 按照指定状态修改订单
     * @param orderId
     * @param processStatus
     */
    void updateOrderStatus(Long orderId, ProcessStatus processStatus);

    /**
     * 根据订单Id 查询订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfo(Long orderId);
}
