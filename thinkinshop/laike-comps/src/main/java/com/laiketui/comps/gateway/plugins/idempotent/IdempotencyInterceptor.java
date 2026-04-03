package com.laiketui.comps.gateway.plugins.idempotent;

import com.laiketui.core.annotation.Idempotency;
import com.laiketui.core.annotation.InterceptorConfig;
import com.laiketui.core.cache.GatewayRedisUtil;
import com.laiketui.core.utils.tool.NetworkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wangxian
 */
@InterceptorConfig(
        includePatterns = "/**",
        excludePatterns = {
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/api-docs",
                "/static/**"
        },
        order = 25
)
@Component
public class IdempotencyInterceptor implements HandlerInterceptor
{

    @Autowired
    private GatewayRedisUtil gatewayRedisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod)) return true;

        HandlerMethod method = (HandlerMethod) handler;
        Idempotency   ann    = method.getMethodAnnotation(Idempotency.class);
        if (ann == null) return true;

        String key = buildIdempotentKey(request, method);

        boolean ok = gatewayRedisUtil.idempotent(key, ann.timeout());
        if (!ok)
        {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":429,\"message\":\"" + ann.message() + "\"}");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        if (!(handler instanceof HandlerMethod)) return;

        HandlerMethod method = (HandlerMethod) handler;
        Idempotency   ann    = method.getMethodAnnotation(Idempotency.class);
        if (ann != null && ann.delKey())
        {
            String key = buildIdempotentKey(request, method);
            gatewayRedisUtil.deleteIdempotentKey(key);
        }
    }

    private String buildIdempotentKey(HttpServletRequest request, HandlerMethod method)
    {
        String governKey  = (String) request.getAttribute("governKey");
        String httpMethod = method.getMethod().getName();
        String ip         = NetworkUtils.getClientIp(request);

        String params;
        try
        {
            params = request.getQueryString();
        }
        catch (Exception e)
        {
            params = "";
        }
        // ✅ 使用治理 key
        String raw = governKey + ":" + httpMethod + ":" + ip + ":" + params;
        return "idem:" + DigestUtils.md5DigestAsHex(raw.getBytes());
    }
}

