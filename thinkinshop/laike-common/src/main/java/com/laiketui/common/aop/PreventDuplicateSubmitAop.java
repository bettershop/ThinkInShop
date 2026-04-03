package com.laiketui.common.aop;

import com.alibaba.fastjson2.JSON;
import com.laiketui.core.annotation.Idempotency;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.IpUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.root.annotation.HttpApiMethod;
import com.laiketui.root.license.Md5Util;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect
@Component
@Order(1)
public class PreventDuplicateSubmitAop
{

    public static final String REQUEST_KEY = "Request-ID:";

    public static final String LOCK_PRE = "DuplicateSubmit-LOCK:";

    @Autowired
    private RedissonClient redissonClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("@annotation(com.laiketui.core.annotation.Idempotency)")
    public void preventDuplicateSubmit()
    {
    }

    @Autowired
    private RedisUtil redisUtil;

    @Around("preventDuplicateSubmit()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable
    {

        RLock         lock          = null;
        String        apiName       = null;
        String        apiMethodName = null;
        StringBuilder parameterName = null;

        try
        {
            Signature       signature       = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method          method          = methodSignature.getMethod();
            apiMethodName = method.getName();

            logger.info("方法名{}",apiMethodName);

            //不同功能的方法不要同名
            lock = redissonClient.getLock(LOCK_PRE + apiMethodName);

            if (lock.tryLock())
            {
                // 获取方法参数
                Object[] args = joinPoint.getArgs();

                //控制层方法添加 HttpApiMethod 注解
                HttpApiMethod httpApiMethod = method.getAnnotation(HttpApiMethod.class);
                //控制层方法添加了 PreventDuplicateSubmit 注解的方法
                Idempotency idempotency = method.getAnnotation(Idempotency.class);

                //无注解放行
                if (httpApiMethod == null || idempotency == null)
                {
                    return joinPoint.proceed();
                }

                RequestAttributes  requestAttributes = RequestContextHolder.getRequestAttributes();
                HttpServletRequest request           = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
                String             ipAddr            = IpUtil.getIpAddr(request);
                String             accessId          = request.getParameter("accessId");
                String             storeType         = request.getParameter("storeType");
                String             storeId           = request.getParameter("storeId");

                logger.info("商城id:{}", storeId);
                logger.info("终端类型:{}", storeType);
                logger.info("用户:{}", accessId);

                apiName = httpApiMethod.apiKey();
                if (StringUtils.isEmpty(apiName))
                {
                    String[] urls = httpApiMethod.urlMapping();
                    if (urls != null && urls.length > 0)
                    {
                        apiName = httpApiMethod.urlMapping()[0];
                    }
                }

                Parameter[] parameters = method.getParameters();
                if (args != null && parameters != null)
                {
                    parameterName = new StringBuilder();
                    for (int i = 0; i <= args.length - 1; i++)
                    {
                        Object    v = args[i];
                        Parameter k = parameters[i];
                        //售后文件上传图片失败问题
                        if (v instanceof MultipartFile)
                        {
                            MultipartFile file = (MultipartFile) v;
                            parameterName.append(k.getName()).append(SplitUtils.MH).append(file.getOriginalFilename()).append(SplitUtils.FH);
                        }
                        else
                        {
                            parameterName.append(k.getName()).append(SplitUtils.MH).append(JSON.toJSONString(v)).append(SplitUtils.FH);
                        }
                    }
                }

                String requestId = REQUEST_KEY + storeId + SplitUtils.MH + storeType + SplitUtils.MH + accessId + SplitUtils.MH + ipAddr + SplitUtils.MH + apiName + SplitUtils.MH + apiMethodName + SplitUtils.MH + parameterName;
                logger.info("请求日志:{}", requestId);
                requestId = Md5Util.MD5endoce(requestId);
                if (redisUtil.hasKey(requestId))
                {
                    logger.info("重复请求ID:{}", requestId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QWCFTJ, idempotency.message());
                }
                else
                {
                    logger.info("首次请求ID:{}", requestId);
                    redisUtil.set(requestId, "HelloLaike", idempotency.timeout());
                }
                // 继续执行原始方法
                return joinPoint.proceed();
            }
            else
            {
                //客户端提示的是 请稍后再试
                logger.error("未获取到业务锁，请下次再试.");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
            }
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
    }
}
