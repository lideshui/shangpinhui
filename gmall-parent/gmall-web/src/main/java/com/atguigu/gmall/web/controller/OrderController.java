package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 渲染订单确认页面，并调用远程Feign接口添加model数据
     *
     * @param model
     */
    @GetMapping("/trade.html")
    public String tradeHtml(Model model, HttpServletRequest request) {
        //1.远程调用订单微服务-获取渲染订单确认页面相关数据
        Result<Map> result = orderFeignClient.tradeDatas();
        model.addAllAttributes(result.getData());

        //2.渲染页面
        return "/order/trade";
    }

    /**
     * 渲染订单列表页，不需要关系数据问题，页面加载后通过ajax获取
     */
    @GetMapping("/myOrder.html")
    public String myOrderHtml() {
        return "/order/myOrder";
    }
}