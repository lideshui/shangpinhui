package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.enums.model.OrderStatus;
import com.atguigu.gmall.enums.model.PaymentStatus;
import com.atguigu.gmall.enums.model.PaymentType;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.model.PaymentInfo;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author: atguigu
 * @create: 2023-03-13 15:34
 */
@Slf4j
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 只要展示支付宝支付页面,需要在本地存储一条本地交易记录
     *
     * @param paymentInfo
     * @param paymentType
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo, String paymentType) {
        //1.根据订单编号,支付方式 查询交易记录是否存在
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOutTradeNo, paymentInfo.getOutTradeNo());
        queryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        int count = this.count(queryWrapper);
        if (count != 0) {
            return;
        }

        //2.如果交易记录不存在,则新增本地交易记录
        this.save(paymentInfo);
    }


    /**
     * 展示支付宝支付表单页面
     *
     * @param orderId 订单ID
     * @return
     */
    @Override
    public String createAlipayForm(Long orderId, String userId) {
        try {
            //1.远程调用订单微服务获取订单信息
            OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
            if (orderInfo != null && OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())) {
                //2.保存本地交易记录
                PaymentInfo paymentInfo = new PaymentInfo();
                //2.1 商家生成订单编号
                paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
                paymentInfo.setOrderId(orderInfo.getId());
                paymentInfo.setUserId(Long.valueOf(userId));
                paymentInfo.setPaymentType(PaymentType.ALIPAY.name());
                //todo 为了开发方便将支付总金额本地设置0.01元
                paymentInfo.setTotalAmount(new BigDecimal("0.01"));
                paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
                //todo 支付宝回调结果;支付宝端交易编号 等用户支付成功后才会更新
                this.savePaymentInfo(paymentInfo, PaymentType.ALIPAY.name());

                //3.调用支付宝生成支付页面接口
                AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
                //异步接收地址，仅支持http/https，公网可访问 提供给支付宝异步回调接口地址,通知商户端支付结果
                request.setNotifyUrl(AlipayConfig.notify_payment_url);
                //同步跳转地址，仅支持http/https  提供给支付宝同步回到支付结果,通知用户查看支付成功页面
                request.setReturnUrl(AlipayConfig.return_payment_url);
                /******必传参数******/
                JSONObject bizContent = new JSONObject();
                //商户订单号，商家自定义，保持唯一性
                bizContent.put("out_trade_no", orderInfo.getOutTradeNo());
                //支付金额，最小值0.01元
                bizContent.put("total_amount", 0.01);
                //订单标题，不可使用特殊符号
                bizContent.put("subject", orderInfo.getTradeBody());
                //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
                bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
                bizContent.put("timeout_express", "10m");
                request.setBizContent(bizContent.toString());
                AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
                if (response.isSuccess()) {
                    //支付表单
                    return response.getBody();
                }
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.error("生成支付宝支付页面异常,订单ID:{},异常信息:{}", orderId.toString(), e);
        }
        return null;
    }
}
