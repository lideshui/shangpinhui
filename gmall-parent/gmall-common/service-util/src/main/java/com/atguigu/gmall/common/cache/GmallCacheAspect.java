package com.atguigu.gmall.common.cache;


import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 切面类:对@GmallCache修饰方法进行增强
 * 通知:优先从缓存中获取数据;通过分布式锁避免缓存击穿问题
 */

//日志注解
@Slf4j
//切面类注解
@Aspect
//将切面类注入到IoC容器
@Component
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    //注入Redisson对象
    @Autowired
    private RedissonClient redissonClient;


    //该注解相当于外层包了个try...catch，这是怕兜底方法有异常
    @SneakyThrows
    //环绕通知，直接对@GmallCache注解增强，然后标注该注解的类也会被增强
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    /**
     * 缓存+分布式锁切面逻辑
     *
     * @param joinPoint 切入点（真正被增强位置）包含目标方法各种信息
     * @return
     */
    public Object cacheAspect(ProceedingJoinPoint joinPoint) {

        //存储从Redis缓存中获取的业务数据的变量
        Object objectResult = null;

        try {
            log.info("前置通知...");
            //1.优先从缓存中获取数据 命中缓存:直接返回  未命中:获取分布式锁后执行查库业务
            //1.1 构建redis缓存中业务数据的Key 从@Gmallcache注解获取业务Key前缀跟后缀,从方法中获取方法参数作为业务标识
            //1.1.1 通过切点对象获取目标方法信息,通过目标方法获取方法上注解,获取注解中参数
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //1.1.2 获取方法上自定义注解 从注解获取信息
            GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
            //1.1.3 处理方法参数,先把参数从集合变成数组，才可以使用stream()遍历
            List<Object> argList = Arrays.asList(joinPoint.getArgs());
            //1.1.4 将集合中参数遍历，转为字符串并使用指定符号拼接，来区分不同业务的的key
            String argsStr = argList.stream().map(arg -> {
                return arg.toString();
            }).collect(Collectors.joining("|"));
            //拼接业务数据key
            String dataKey = gmallCache.prefix() + argsStr + gmallCache.suffix();


            //1.2 尝试从缓存中获取业务数据，使用业务key获取
            objectResult = redisTemplate.opsForValue().get(dataKey);
            //判断是否从Redis缓存中获取到了值，若获取到了直接返回即可
            if (objectResult == null) {
                //1.2.1 没有命中缓存数据
                //2.尝试获取分布式锁 成功:执行查库业务代码  失败:自旋,尝试下次获取
                //2.1 构建业务数据锁的Key
                String lockKey = gmallCache.prefix() + argsStr + RedisConst.SKULOCK_SUFFIX;

                //2.2 创建锁对象
                RLock lock = redissonClient.getLock(lockKey);

                //2.3 尝试获取锁 成功:执行目标方法进行查询数据库操作  失败:自旋
                Boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                try {
                    //获取锁成功走的分支
                    if (flag) {
                        //2.3.1 获取锁成功 查询数据库业务数据,将业务数据进行缓存
                        //二.执行目标方法-执行目标方法(原来类中方法)-都是执行查询数据库操作
                        objectResult = joinPoint.proceed(joinPoint.getArgs());
                        if (objectResult == null) {
                            //数据库中不存在该数据 短暂进行缓存null对象
                            redisTemplate.opsForValue().set(dataKey, objectResult, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return objectResult;
                        } else {
                            //数据库中有数据,则进行缓存 返回业务数据
                            redisTemplate.opsForValue().set(dataKey, objectResult, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                            return objectResult;
                        }
                    } else {
                        //获取锁失败走的分支，睡100毫秒然后递归自旋
                        Thread.sleep(100);
                        //2.3.2 本次获取锁失败,等待下次获取,自旋:再次调用增强方法
                        return this.cacheAspect(joinPoint);
                    }
                } finally {

                    //3.业务执行完毕后将锁释放
                    lock.unlock();
                }
            } else {
                //1.2.2 命中缓存 直接返回即可
                return objectResult;
            }
            //三.后置通知,目标发方法执行后
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        //兜底当法，执行查询数据库-执行目标方法
        return joinPoint.proceed(joinPoint.getArgs());
    }

}
