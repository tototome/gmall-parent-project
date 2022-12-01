package com.atguigu.gmall.product.aopcache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.config.RedissonConfig;

import com.atguigu.gmall.common.constant.RedisConst;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import springfox.documentation.spring.web.json.Json;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
//表示当前类是一个切面类
@Aspect
public class GmallCacheAspect {

    @Autowired
    //使用redisson框架做分布式锁
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate redisTemplate;

    //凡是加上了GmallCache注解都要走环绕通知
    @Around(value = "@annotation(com.atguigu.gmall.product.aopcache.GmallCache)")
    public Object getDataFromCache(ProceedingJoinPoint joinPoint) throws InterruptedException {
        //获得方法签名 就是方法的信息
        Signature signature = joinPoint.getSignature();
        //获得目标方法上的自定义注解
        MethodSignature methodSignature = (MethodSignature) signature;
        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);
        //获得前驻节中的属性值 这个是属性值是我们自定义传入的
        //在skuInfo中是sku
        String prefix = annotation.prefix();

        // 2、把实参数组拼接为“:”连起来的字符串
        // [1]声明拼接字符串变量
        String argumentSequence = "noArgAllData";
        //获取传入的参数
        Object[] args = joinPoint.getArgs();
        if (!ArrayUtils.isEmpty(args)) {
            //拼接字符串 后面用与key中做标识
            argumentSequence=StringUtils.arrayToDelimitedString(args, ":");
        }
        //key的命名 见名知意
        String cacheDataKey = prefix + argumentSequence + ":data";
        //opsForValue操作的数据结构是String类型
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //查缓存
        Object cacheDate = valueOperations.get(cacheDataKey);
        //判断是否有缓存
        if (cacheDate != null) {
            //返回数据
            return cacheDate;
        }
        //没有命中 加锁 走切入点的方法进行查询 放缓存中
        String lockKey = prefix + argumentSequence + ":lock";
        //获取锁对象
        RLock lock = redissonClient.getLock(lockKey);
        //加锁操作
        boolean lockResult = lock.tryLock();
        if (lockResult) {
            //加锁成功 掉切入点的方法查询
            try {
                Object dataFromDB = joinPoint.proceed(args);
                if (dataFromDB != null) {
                    //如果存在 设置到缓存中 过期时间加上一个随机时间防止雪崩效应的产生 同一时间 缓存都失效
                    valueOperations.set(cacheDataKey, dataFromDB,
                            RedisConst.SKUKEY_TIMEOUT + (int) Math.random() * 10, TimeUnit.SECONDS);
                    return dataFromDB;
                }
                //没有数据 查数据库查不到值 缓存穿透
                //创建空对象
                cacheDate = JSON.toJSONString(new Object());
                //存入缓存
                valueOperations.set(cacheDataKey, dataFromDB,
                        RedisConst.SKUKEY_TIMEOUT + (int) Math.random() * 10, TimeUnit.SECONDS);
                //返回空对象
                return dataFromDB;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                //解锁
                lock.unlock();
            }
        }
        //加锁失败
        // ●等待一段时间
        TimeUnit.MILLISECONDS.sleep(50);

        System.out.println(Thread.currentThread().getName() + " 来过了。");

        // ●重新尝试查询缓存：这其中就包含重新查缓存，有希望不申请分布式锁
        return getDataFromCache(joinPoint);
    }
}
