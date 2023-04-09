package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

/**
 * 身份认证过滤器
 * GlobalFilter-网关全局过滤器
 * Ordered-有序接口，重写其中的 getOrder() 方法，返回一个整数值，这个值越小，多个过滤器存在时该过滤器执行优先级越高。
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    //AntPathMatcher 是 Spring 框架中的一个工具类，用于实现 Ant 风格的路径匹配⚠️
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private List<String> authUrlList;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 验证用户请求是否合法
     *
     * @param exchange 封装请求对象,响应对象
     * @param chain    过滤器链
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //通过exchange获取到请求对象以及响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取用户请求地址
        String path = request.getURI().getPath();

        //1.对于那些静态资源同步请求 img js css 直接放行-无论是否登录
        /**
         * 使用AntPathMatcher地址匹配器进行匹配：
         * 1. antPathMatcher.match(pattern, path) 是 Spring Framework 中的一个工具方法，用于对 URL 路径进行 Ant 风格的通配符匹配。⚠
         * 2. 如果在 pattern 中使用了 * 或 **，则会将其分隔为多个路径段进行匹配。
         * 3.而在 path 中出现的路径段和 pattern 中对应的路径段将进行匹配，如果满足匹配条件，则返回 true；否则返回 false。⚠️
         * */
        if (antPathMatcher.match("/*/img/**", path) || antPathMatcher.match("/*/js/**", path) || antPathMatcher.match("/*/css/**", path)) {
            //过滤器放行
            return chain.filter(exchange);
        }

        //2.对于微服务间调用Feign接口不允许用户直接调方法 "/inner" -无论是否登录
        if (antPathMatcher.match("/**/inner/**", path)) {
            //响应错误信息
            return outError(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //尝试从Redis获取当前登陆用户的ID，以此来判断是否已经登陆⚠️
        String userId = this.getUserId(request, response);

        //如果用户没登陆就往下走
        if (StringUtils.isBlank(userId)) {
            //3.如果用户没有登录-并且还要访问(需要登录)restuful接口 例如:-返回拒绝访问
            if (antPathMatcher.match("/**/auth/**", path)) {
                //响应错误信息-没权限
                return outError(response, ResultCodeEnum.PERMISSION);
            }

            //4.如果用户没有登录-并且访问(需要登录)页面请求-引导用户去登录
            if (!CollectionUtils.isEmpty(authUrlList)) {
                for (String auth : authUrlList) {
                    if (antPathMatcher.match("/" + auth + "*", path)) {
                        //引导用户登录
                        //说明用户未登录 访问地址要求登录 设置重定向 需要将http状态码设置为301
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        //通过Response对象 响应头设置重定向登录地址⚠️⚠️⚠️
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI().toString());
                        //结束：setComplete()方法在发送响应之后被调用，可以确保当客户端接收到响应时，请求-响应已经被关闭了⚠️
                        return response.setComplete();
                    }
                }
            }
        }

        //5.如果用户登录-将用户ID设置到请求头中-将用户ID参数值透传到业务微服务
        if (StringUtils.isNotBlank(userId)) {
            //为了能在其他微服务中获取用户ID 需要将用户ID存入
            //其他微服务就可以从头信息中获取当前登陆用户的ID
            //request.mutate().header(name, value) 方法可以用于向 HTTP 请求头中添加指定的头信息⚠️
            request.mutate().header("userId", userId);
        }

        //获取临时用户ID，将获取到的User临时ID存储到请求头中
        String userTempId = getUserTempId(request);
        if (StringUtils.isNotBlank(userTempId)){
            request.mutate().header("userTempId", userTempId);
        }
        return chain.filter(exchange);
    }


    /**
     * 尝试从Redis中获取用户ID：
     * 1. 先从cookie或请求头中尝试获取token
     * 2. 再通过token拼接redisKey，去redis查询登陆用户信息
     * 前端如果访问是静态html文件，令牌采用cookie中提交
     * 前端如果访问是ajax请求，令牌采用请求头中提交
     *
     * @param request
     * @param response
     */
    private String getUserId(ServerHttpRequest request, ServerHttpResponse response) {
        //1.先从请求头中获取Token
        String token = "";
        token = request.getHeaders().getFirst("token");

        //2.在尝试从请求对象中Cookie中获取
        if (StringUtils.isBlank(token)) {
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if (httpCookie != null) {
                token = httpCookie.getValue();
            }
        }

        //3.根据Token查询Redis中用户信息-判断用户是否为异地登录、是否更换设备等
        if (StringUtils.isNotBlank(token)) {
            String redisKey = "user:login:" + token;
            HashMap<String, String> userInfoMap = (HashMap<String, String>) redisTemplate.opsForValue().get(redisKey);
            if (userInfoMap != null) {
                //用户登录时所在城市
                String loginCity = userInfoMap.get("city");
                //获取本地访问所在城市
                String ipAddress = IpUtil.getGatwayIpAddress(request);
                //这里先写死，可通过百度API访问
                String nowCity = "北京";
                if (!nowCity.equals(loginCity)) {
                    /**
                     * 进行设备校验、异地登陆校验、IP校验等
                     * 1. 如果未通过校验，抛异常
                     * 2. 如果未通过校验，返回-1，告诉服务器令牌token被窃取，锁定该用户
                     */
                    //this.outError(response, ResultCodeEnum.ILLEGAL_REQUEST);
                    //return "-1";
                }

                //4.若通过验证，返回用户ID
                String userId = userInfoMap.get("userId");
                return userId;
            }
        }
        //没有token兜底返回null
        return null;
    }


    /**
     * 用来给前端响应错误提示信息
     *
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> outError(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        //1. 准备响应结果对象，转为JSON对象
        Result<Object> result = Result.build(null, resultCodeEnum);
        String resultString = JSON.toJSONString(result);

        //2. 响应结果给客户端
        //2.1 设置http状态码401 "Unauthorized"
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        //2.2 通过响应头设置响应数据格式-json
        response.getHeaders().add("content-type", "application/json;charset=utf-8");
        //将字符串转换成字节流，并创建一个数据缓冲区的方法，将响应内容转换成字节流，并将其写入到 HTTP 响应中
        DataBuffer wrap = response.bufferFactory().wrap(resultString.getBytes());
        //2.3 网关结束响应-不再路由转发，没有内容就直接响应，有内容就走下面的⚠️
        //return response.setComplete();
        //2.4 网关将响应数据返回给客户端
        return response.writeWith(Mono.just(wrap));
    }


    /**
     * Ordered-有序接口，重写其中的 getOrder() 方法
     * 返回一个整数值，这个值越小，多个过滤器存在时该过滤器执行优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }


    /**
     * 测试antPathMatcher.match()的用法
     * 以下结果返回true
     */
    public static void main(String[] args) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("*/user/**", "d/user/a/b/c");
        System.out.println(match);
    }


    /**
     * 尝试获取临时用户ID
     */
    private String getUserTempId(ServerHttpRequest request) {
        String userTempId;

        //1.尝试从Cookie中获取
        List<HttpCookie> cookieList = request.getCookies().get("userTempId");

        //若成功从Cookie中获取到了临时ID
        if (!CollectionUtils.isEmpty(cookieList)){
            //取出Cookie中第一个值对应的临时ID值，并返回
            userTempId = cookieList.get(0).getValue();
            return userTempId;
        }

        //2.若没从Cookie中取到，尝试从请求头中获取
        userTempId = request.getHeaders().getFirst("userTempId");
        //若成功从请求头中获取到了临时ID，直接返回
        if (StringUtils.isNotBlank(userTempId)){
            return userTempId;
        }

        return userTempId;
    }
}
