package com.atguigu.gmall.item.util;

import sun.nio.ch.ThreadPool;

import java.util.concurrent.*;
//懒汉式 创建线程池 使用的时候才创建
//饿汉式 类加载的时候就创建
public class ThreadPoolUtil {

    private static ThreadPoolExecutor threadPoolExecutor;

    private ThreadPoolUtil(){

    }
    public synchronized static ThreadPoolExecutor getThreadPoolExecutor(){
        if (threadPoolExecutor==null){
            // [1]线程池的核心线程数
            int corePoolSize = 8;

            // [2]线程池的最大线程数
            int maximumPoolSize = 16;

            // [3]设置最大过期时间的数量
            long keepAliveTime = 10;

            // [4]设置最大过期时间的单位
            TimeUnit timeUnit = TimeUnit.SECONDS;

            // [5]存放等待中任务的阻塞队列
            ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1000);

            // [6]创建线程工厂
            ThreadFactory threadFactory = Executors.defaultThreadFactory();

            // [7]创建拒绝策略对象
            ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
            threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,timeUnit,queue,threadFactory,abortPolicy);
        }
        return  threadPoolExecutor;
    }
}
