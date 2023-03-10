package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.user.model.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户表 业务实现类
 *
 * @author atguigu
 * @since 2023-03-07
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate redisTemplate;

    //处理用户登录业务
    @Override
    public Result login(UserInfo loginUser, HttpServletRequest request) {
        //SELECT id,login_name,phone_num FROM user_info  where passwd = '96e79218965eb72c92a549dd5a330112' and (phone_num='11111' or email = 'atguigu.com' or login_name = 'atguigu')
        //1.根据用户认证信息 账号(手机号,邮箱,用户名称)跟密码 查询用户记录-判断用户是否存在
        //1.1 对用户提交密码进行加密
        String userPwd = DigestUtils.md5DigestAsHex(loginUser.getPasswd().getBytes());
        //构建查询条件
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPasswd, userPwd);
        //参数wrapper是当前的查询对象，分别根据名字或邮箱或电话登陆
        queryWrapper.and(wrapper -> {
            wrapper.or().eq(UserInfo::getPhoneNum, loginUser.getLoginName())
                    .or().eq(UserInfo::getEmail, loginUser.getLoginName())
                    .or().eq(UserInfo::getLoginName, loginUser.getLoginName());
        });
        UserInfo userInfo = this.getOne(queryWrapper);
        //如果没有根据密码查到，则说明用户名或密码错误
        if (userInfo == null) {
            return Result.fail().message("用户名或者密码错误!");
        }

        //2.用户存在-生成用户令牌 将令牌信息存入Redis
        //2.1 生成存入Redis用户令牌 uuid
        String uuId = UUID.randomUUID().toString().replaceAll("-", "");
        String userKey = "user:login:" + uuId;

        //2.2 生成存入Redis用户信息 用户ID 用户登录IP/城市
        //2.2.1 得到的登录用户IP
        String ipAddress = IpUtil.getIpAddress(request);

        //2.2.2 根据IP获取用户所在城市 TODO 调用百度API

        HashMap<String, String> userRedis = new HashMap<>();
        userRedis.put("userId", userInfo.getId().toString());
        userRedis.put("ip", ipAddress);
        userRedis.put("city", "北京市");

        redisTemplate.opsForValue().set(userKey, userRedis, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);

        //3.按照前端要求响应登录结果 token nickName
        HashMap<String, String> loginResult = new HashMap<>();
        loginResult.put("token", uuId);
        loginResult.put("nickName", userInfo.getNickName());
        return Result.ok(loginResult);
    }

    /**
     * 退出系统 只需要将存储在Redis中的token删除即可
     *
     * @param token
     */
    @Override
    public void logout(String token) {
        String redisKey = "user:login:" + token;
        redisTemplate.delete(redisKey);
    }
}
