package com.laiketui.common.aop;

import com.laiketui.common.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class DistributedLockAspect
{

    @Autowired
    private RedissonClient redissonClient;

    @Pointcut("@annotation(com.laiketui.common.annotation.DistributedLock)")
    public void RlockAspect()
    {
    }

    @Around("RlockAspect()")
    public Object arround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        Object object = null;
        RLock  lock   = null;
        // 获取锁名称
        String lockName = null;
        try
        {
            DistributedLock distributedLock = proceedingJoinPoint.getSignature().getDeclaringType().getMethod(proceedingJoinPoint.getSignature().getName()).getAnnotation(DistributedLock.class);
            lockName = distributedLock.lockName();
            // 获取锁的释放时间
            long releaseTime = distributedLock.releaseTime();
            // 获取时间单位
            TimeUnit timeUnit = distributedLock.timeUnit();
            lock = redissonClient.getLock(lockName);
            boolean success = lock.tryLock(releaseTime, timeUnit);
            if (success)
            {
                log.info("获取锁成功，lockName={}", lockName);
                object = proceedingJoinPoint.proceed();
            }
            else
            {
                log.info("获取锁失败，lockName={}", lockName);
            }
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
                log.info("释放锁成功，lockName={}", lockName);
            }
        }
        return object;
    }
}
