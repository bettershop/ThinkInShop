package com.laiketui.common.aop;

import com.laiketui.core.annotation.PreventOrderResubmit;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 防止多个账号同时支付
 * @version 1.0
 * @description: liuao
 * @date 2025/5/15 19:51
 */

@Slf4j
@Component
@Aspect
public class PreventOrderResubmitAop
{
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedissonClient redissonClient;

    private final       SpelExpressionParser           parser     = new SpelExpressionParser();
    private final       DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
    private final       Logger                         logger     = LoggerFactory.getLogger(this.getClass());
    public static final String                         LOCK_PRE   = "PreventOrderResubmit-LOCK:";


    //拦截带PreventOrderResubmit注解的方法
    @Pointcut("@annotation(com.laiketui.core.annotation.PreventOrderResubmit)")
    public void preventOrderResubmit()
    {
    }

    @Around("preventOrderResubmit()")
    public Object checkDuplicate(ProceedingJoinPoint joinPoint) throws Throwable
    {
        RLock     lock      = null;
        Signature signature = joinPoint.getSignature();
        Object[]  args      = joinPoint.getArgs();
        String    sNo       = "";
        String    accessId  = "";
        //方法信息
        MethodSignature      methodSignature      = (MethodSignature) signature;
        Method               method               = methodSignature.getMethod();
        String               apiMethodName        = method.getName();
        PreventOrderResubmit preventOrderResubmit = method.getAnnotation(PreventOrderResubmit.class);
        //无注解放行
        if (preventOrderResubmit == null)
        {
            return joinPoint.proceed();
        }
        String lockOrderKey = "lock:order:";

        try
        {
            lock = redissonClient.getLock(LOCK_PRE + apiMethodName);
            if (!lock.tryLock(preventOrderResubmit.waitTime(), preventOrderResubmit.leaseTime(), TimeUnit.SECONDS))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
            }
            // 解析SpEL表达式
            String spelString = parseExpression(methodSignature, args, preventOrderResubmit.keyExpr());
            //获取用户token和当前需要支付的订单号
            if (StringUtils.isNotEmpty(spelString))
            {
                String[] values = spelString.split(SplitUtils.DH);
                if (values.length > 1)
                {
                    accessId = values[0];
                    sNo = values[1];
                }
            }
            if (StringUtils.isEmpty(sNo))
            {
                return joinPoint.proceed();
            }
            //如果订单号存在，则代表有人进行下单操作
            if (redisUtil.hasKey(lockOrderKey + sNo))
            {
                String oldUserToken = redisUtil.get(lockOrderKey + sNo).toString();
                if (!Objects.equals(oldUserToken, accessId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDZZCLZ, "订单正在处理中");
                }
            }
            else
            {
                redisUtil.set(lockOrderKey + sNo, accessId, preventOrderResubmit.lockedTime());
            }
        }
        catch (LaiKeAPIException laiKeAPIException)
        {
            throw laiKeAPIException;
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
        return joinPoint.proceed();
    }


    /**
     * 解析表达式
     *
     * @param signature 方法信息
     * @param args      方法参数
     * @param SpELStr   表达式字符串
     * @return string
     */
    private String parseExpression(MethodSignature signature, Object[] args, String SpELStr)
    {
        Method method = signature.getMethod();
        logger.info("method::::{}", method.getName());

        // 构建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        //方法参数名：vo
        String[] paramNames = discoverer.getParameterNames(method);

        if (args != null && paramNames != null)
        {
            logger.info("方法参数：：：：：{}", args);
            //赋值
            for (int i = 0; i < paramNames.length; i++)
            {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        // 解析表达式
        Expression expressionStr = parser.parseExpression(SpELStr);
        logger.info("expression:::{}", expressionStr.getValue(context));
        return expressionStr.getValue(context, String.class);
    }
}
