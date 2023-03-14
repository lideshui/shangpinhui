package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.order.model.OrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: atguigu
 * @create: 2023-03-13 15:20
 */
@Controller
public class PaymentController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 渲染订单提交成功页面
     *
     * @param orderId
     * @param model
     * @return
     */
    @GetMapping("/pay.html")
    public String orderSubmitSuccess(@RequestParam("orderId") Long orderId, Model model) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        if (orderInfo != null) {
            model.addAttribute("orderInfo", orderInfo);
        }
        return "/payment/pay";
    }
}
