package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    private OrderInfoService orderInfoService;


    /**
     * 订单确认页面数据模型汇总
     *
     * @param request
     */
    @GetMapping("/auth/trade")
    public Result<Map> tradeDatas(HttpServletRequest request) {
        //1.获取用户ID
        String userId = AuthContextHolder.getUserId(request);

        //2.调用业务层封装相关数据
        Map<String, Object> mapData = orderInfoService.tradeDatas(userId);
        return Result.ok(mapData);
    }

}
