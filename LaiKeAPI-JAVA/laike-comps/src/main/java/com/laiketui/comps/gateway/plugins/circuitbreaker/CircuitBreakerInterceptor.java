package com.laiketui.comps.gateway.plugins.circuitbreaker;

import com.laiketui.comps.gateway.loadbalance.GatewayProperties;
import com.laiketui.core.annotation.InterceptorConfig;
import com.laiketui.core.cache.GatewayRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@InterceptorConfig(
        includePatterns = "/**",
        order = 25
)
@Component
public class CircuitBreakerInterceptor implements HandlerInterceptor
{
    @Autowired
    private GatewayRedisUtil redis;

    @Autowired
    private GatewayProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod)) return true;

        String governKey = (String) request.getAttribute("governKey");
        if (governKey == null) return true;

        String statKey = "gw:cb:stat:" + governKey;
        String openKey = "gw:cb:open:" + governKey;

        boolean allow = redis.circuitCheck(
                statKey,
                openKey,
                properties.getMinRequest(),
                properties.getErrorRate(),
                properties.getSlowRate(),
                properties.getOpenSeconds()
        );

        if (!allow)
        {
            response.setStatus(503);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":503,\"message\":\"服务暂时不可用，请稍后再试\"}"
            );
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex)
    {
        if (!(handler instanceof HandlerMethod)) return;

        String governKey = (String) request.getAttribute("governKey");
        if (governKey == null) return;

        long cost = System.currentTimeMillis() - (long) request.getAttribute("_gw_start");

        boolean isError = response.getStatus() >= 500;
        boolean isSlow  = cost >= properties.getSlowThresholdMs();

        String statKey = "gw:cb:stat:" + governKey;

        redis.circuitStat(
                statKey,
                properties.getCircuitWindowSeconds(),
                isError,
                isSlow
        );
    }
}


