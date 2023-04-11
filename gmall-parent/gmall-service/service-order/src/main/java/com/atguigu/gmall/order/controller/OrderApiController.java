package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.order.model.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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


    /**
     * 在订单确认页面保存订单，响应成功保存的订单的ID
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    public Result<Long> submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo) {
        //1.获取用户ID，设置到OrderInfo中
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));

        //2.获取前端页面提交的流水号，和订单信息一块传给service层
        String tradeNo = request.getParameter("tradeNo");
        Long orderId = orderInfoService.submitOrder(orderInfo, tradeNo);
        return Result.ok(orderId);
    }


    /**
     * 查询当前登录用户订单列表以及订单明细内容
     *
     * @param page
     * @param limit
     */
    @GetMapping("/auth/{page}/{limit}")
    public Result getOrderList(HttpServletRequest request, @PathVariable("page") Long page, @PathVariable("limit") Long limit, String status) {
        //1.通过公共类，获取登录用户ID
        String userId = AuthContextHolder.getUserId(request);

        //2.构建分页对象
        IPage<OrderInfo> iPage = new Page<>(page, limit);

        //3.调用业务逻辑查询
        iPage = orderInfoService.getOrderList(iPage, userId, status);
        return Result.ok(iPage);
    }

}
