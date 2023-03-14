package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.enums.model.PaymentType;
import com.atguigu.gmall.payment.model.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PaymentInfoService extends IService<PaymentInfo> {


    /**
     * 保存本地交易记录
     * @param paymentInfo
     * @param paymentType
     */
    void savePaymentInfo(PaymentInfo paymentInfo, String paymentType);


    /**
     * 展示支付宝支付表单页面
     *
     * @param orderId 订单ID
     * @return
     */
    String createAlipayForm(Long orderId, String userId);
}
