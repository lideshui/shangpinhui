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
     * @param request
     * @return
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
     * 保存订单,响应保存后订单ID
     *
     * @param orderInfo
     * @return
     */
    @PostMapping("/auth/submitOrder")
    public Result<Long> submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo) {

        //获取用户ID，设置到OrderInfo中
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.valueOf(userId));

        //前端页面提交流水号
        String tradeNo = request.getParameter("tradeNo");
        Long orderId = orderInfoService.submitOrder(orderInfo, tradeNo);
        return Result.ok(orderId);
    }


    /**
     * 查询当前登录用户订单列表(包括订单明细)
     *
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/auth/{page}/{limit}")
    public Result getOrderList(HttpServletRequest request, @PathVariable("page") Long page, @PathVariable("limit") Long limit, String status) {
        //1.获取登录用户ID
        String userId = AuthContextHolder.getUserId(request);
        //2.构建分页对象
        IPage<OrderInfo> iPage = new Page<>(page, limit);

        //3.调用业务逻辑查询
        iPage = orderInfoService.getOrderList(iPage, userId, status);
        return Result.ok(iPage);
    }

    /**
     * 内部调用获取订单
     * @param orderId
     * @return
     */
    @GetMapping("inner/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable(value = "orderId") Long orderId){
        return orderInfoService.getOrderInfo(orderId);
    }
}
