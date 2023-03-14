package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: atguigu
 * @create: 2023-03-13 15:44
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentInfoService paymentInfoService;


    /**
     * 展示支付宝支付表单页面
     *
     * @param orderId 订单ID
     * @return
     */
    @GetMapping("/alipay/submit/{orderId}")
    public String createAlipayForm(HttpServletRequest request, @PathVariable("orderId") Long orderId) {
        String userId = AuthContextHolder.getUserId(request);
        return paymentInfoService.createAlipayForm(orderId, userId);
    }

}
