package com.laiketui.comps.gateway.plugins.security;

import com.laiketui.comps.gateway.loadbalance.GatewayProperties;
import com.laiketui.comps.gateway.plugins.monitor.GatewayMetrics;
import com.laiketui.core.annotation.ApiLimit;
import com.laiketui.core.annotation.InterceptorConfig;
import com.laiketui.core.cache.GatewayRedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.utils.tool.NetworkUtils;
import com.laiketui.root.annotation.HttpApiMethod;
import com.laiketui.root.gateway.dto.LaiKeApiDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import static com.laiketui.core.lktconst.gwconst.LaiKeGWConst.API_DEFAULT_METHOD;
import static com.laiketui.core.lktconst.gwconst.LaiKeGWConst.API_VERSION;


@InterceptorConfig(
        includePatterns = "/**",
        excludePatterns = {
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/api-docs",
                "/static/**"
        },
        order = 20
)
@Component
@Slf4j
public class GatewaySecurityInterceptor implements HandlerInterceptor
{
    private static final String ATTR_START            = "_gw_start";
    private static final String ATTR_IP               = "_gw_ip";
    private static final String ATTR_GOVERN_KEY       = "governKey";
    private static final String ATTR_BUSINESS_API_KEY = "businessApiKey";
    private static final String ATTR_TRACE_ID         = "traceId";
    private static final long   IP_LIST_CACHE_TTL_MS  = 10_000L;
    private static final int    IP_CACHE_MAX_SIZE     = 10_000;
    private static final long   SLOW_LOG_THRESHOLD_MS = 500L;
    private static final int    LOG_SAMPLE_DENOM      = 100; // 1% sampling for fast requests

    private final Map<String, CacheEntry> whiteIpCache = new ConcurrentHashMap<>(256);
    private final Map<String, CacheEntry> blackIpCache = new ConcurrentHashMap<>(256);

    @Autowired
    private GatewayMetrics gatewayMetrics;

    @Autowired
    private GatewayProperties properties;

    @Autowired
    private GatewayRedisUtil gatewayRedisUtil;

    @PostConstruct
    public void init()
    {
        // 预编译正则
//        properties.getRegxPattern();
        if (properties.getWhitelist() != null)
        {
            long now = System.currentTimeMillis();
            properties.getWhitelist().forEach(ip ->
            {
                gatewayRedisUtil.addWhiteIp(ip);
                whiteIpCache.put(ip, new CacheEntry(true, now + IP_LIST_CACHE_TTL_MS));
            });
        }

        if (properties.getBlacklist() != null)
        {
            long now = System.currentTimeMillis();
            properties.getBlacklist().forEach(ip ->
            {
                gatewayRedisUtil.addBlackIp(ip);
                blackIpCache.put(ip, new CacheEntry(true, now + IP_LIST_CACHE_TTL_MS));
            });
        }
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String ip = NetworkUtils.getClientIp(request);
        request.setAttribute(ATTR_START, System.currentTimeMillis());
        request.setAttribute(ATTR_IP, ip);

        // 静态资源等直接放行
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method        realMethod    = handlerMethod.getMethod();

        HttpApiMethod apiMethod = realMethod.getAnnotation(HttpApiMethod.class);
        // ================= 计算业务 apiKey（优先注解，避免解析参数） =================
        request.setAttribute(ATTR_BUSINESS_API_KEY, buildBusinessApiKey(apiMethod, request));
        // ================= 白名单直通 =================
        if (isWhiteIpCached(ip))
        {
            return true;
        }

        // ================= 黑名单拦截 =================
        if (isBlackIpCached(ip))
        {
            writeError(response, 403, "IP 已被封禁");
            return false;
        }

        // ================= SQL 注入关键字检测 =================
//        if (hasSqlInject(request))
//        {
//            writeError(response, 400, "请求参数非法");
//            return false;
//        }

        // ================= IP 级限流 =================
        if (!doRateLimit("ip", ip))
        {
            writeError(response, 429, "请求过于频繁");
            return false;
        }

        String governKey = getGovernKey(apiMethod);
        //这行作用是什么
        request.setAttribute(ATTR_GOVERN_KEY, governKey);

        // ================= 按照用户级别的接口限流 =================
        if (!doRateLimitAPI(realMethod, request, governKey))
        {
            writeError(response, 429, "请求过于频繁");
            recordPrometheus(governKey, false, System.currentTimeMillis() - (long) request.getAttribute(ATTR_START));
            return false;
        }

        // ================= TraceId 注入 =================
        injectTraceId(request);

        return true;
    }

    /**
     * ===== Prometheus 指标收集 =====
     */
    private void recordPrometheus(String governKey, boolean success, long costMs)
    {
        try
        {
            gatewayMetrics.record(governKey, success, costMs);
        }
        catch (Exception e)
        {
            log.warn("Prometheus record fail for key {}", governKey, e);
        }
    }

    /**
     * 接口限流
     *
     * @param method
     * @param request
     * @param governKey
     * @return
     */
    private boolean doRateLimitAPI(Method method, HttpServletRequest request, String governKey)
    {

        if (!method.isAnnotationPresent(ApiLimit.class))
        {
            return true;
        }

        ApiLimit apiLimit      = method.getAnnotation(ApiLimit.class);
        long     windowSeconds = apiLimit.unit().toSeconds(apiLimit.time());
        if (windowSeconds <= 0)
        {
            windowSeconds = 1;
        }

        String userIdOrIp = getUserIdentifier(request);
        String key        = "gw:rate:" + governKey + ":" + userIdOrIp;

        if (apiLimit.limit() <= 0)
        {
            return true;
        }
        return gatewayRedisUtil.rateLimit(key, apiLimit.limit(), (int) windowSeconds);
    }

    private String getGovernKey(Method method)
    {
        return getGovernKey(method.getAnnotation(HttpApiMethod.class));
    }

    private String getGovernKey(HttpApiMethod ann)
    {
        if (ann == null)
        {
            return "gw:unknown";
        }
        String apiKey = ann.apiKey();
        if (apiKey == null || apiKey.isEmpty())
        {
            String[] urls = ann.urlMapping();
            apiKey = (urls != null && urls.length > 0) ? urls[0] : "unknown";
        }

        String version = ann.version();
        if (version == null || version.isEmpty())
        {
            version = "1.0.0";
        }

        return "gw:" + apiKey + ":" + version;
    }


    /**
     * 获取用户唯一标识（优先级：user_id > Token > IP ）
     */
    private String getUserIdentifier(HttpServletRequest request)
    {
        String user_id = request.getParameter("user_id");
        if (user_id != null && !user_id.isEmpty())
        {
            return "u_" + user_id;
        }

        String token = request.getParameter("access_id");
        if (token != null)
        {
            return "u_" + token;
        }

        token = request.getHeader("accessId");
        if (token != null && !token.isEmpty())
        {
            return "t_" + token.substring(0, Math.min(32, token.length()));
        }

        String ip = (String) request.getAttribute(ATTR_IP);
        if (ip == null || ip.isEmpty())
        {
            ip = NetworkUtils.getClientIp(request);
        }
        return "ip_" + ip;
    }

    /**
     * 限流
     */
    private boolean doRateLimit(String type, String value)
    {
        GatewayProperties.RequestLimit limitCfg = properties.getRequestLimitPerIp();

        // 未配置 or 配置非法 → 不限流
        if (limitCfg == null || limitCfg.getLimit() <= 0 || limitCfg.getTime() <= 0)
        {
            return true;
        }

        //gw:rate:ip:192.168.0.1 = 1,2,3,...N
        String key = "gw:rate:" + type + ":" + value;
        try
        {
            // 使用 GatewayRedisUtil 中的 Lua 原子限流
            return gatewayRedisUtil.rateLimit(
                    key,
                    // 最大请求数
                    limitCfg.getLimit(),
                    // 时间窗口（秒）
                    limitCfg.getTime()
            );
        }
        catch (Exception e)
        {
            // Redis 异常时降级放行，避免网关雪崩
            return true;
        }
    }

    /**
     * 注入 TraceId（支持上游透传）
     */
    private void injectTraceId(HttpServletRequest request)
    {
        // 尝试从请求头中获取
        String traceId = getOrCreateTraceId(request);

        // 放入 MDC（日志自动携带）
        MDC.put("traceId", traceId);

        // 放入 request，供下游使用
        request.setAttribute(ATTR_TRACE_ID, traceId);
    }

    private boolean hasSqlInject(HttpServletRequest request)
    {
        Pattern pattern = properties.getRegxPattern();
        if (pattern == null) return false;

        Map<String, String[]> params = request.getParameterMap();
        for (String[] values : params.values())
        {
            for (String v : values)
            {
                if (v != null && pattern.matcher(v).find())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws Exception
    {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        Result r = Result.error(String.valueOf(code), msg);
        response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(r));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        try
        {
            Object startAttr = request.getAttribute(ATTR_START);
            long cost = startAttr instanceof Long ? (System.currentTimeMillis() - (long) startAttr) : 0L;
            if (cost < SLOW_LOG_THRESHOLD_MS && !shouldSampleLog())
            {
                return;
            }
            String businessApiKey = (String) request.getAttribute(ATTR_BUSINESS_API_KEY);
            if (StringUtils.isEmpty(businessApiKey) && handler instanceof HandlerMethod)
            {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                businessApiKey = buildBusinessApiKey(handlerMethod.getMethod().getAnnotation(HttpApiMethod.class), request);
            }
            if (StringUtils.isEmpty(businessApiKey))
            {
                businessApiKey = request.getRequestURI();
            }
            log.info("GW_ACCESS uri={} cost={}ms", businessApiKey, cost);
        }
        finally
        {
            MDC.clear();
        }
    }

    private String getOrCreateTraceId(HttpServletRequest request)
    {
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    private boolean shouldSampleLog()
    {
        return ThreadLocalRandom.current().nextInt(LOG_SAMPLE_DENOM) == 0;
    }

    private boolean isWhiteIpCached(String ip)
    {
        return isIpCached(ip, whiteIpCache, true);
    }

    private boolean isBlackIpCached(String ip)
    {
        return isIpCached(ip, blackIpCache, false);
    }

    private boolean isIpCached(String ip, Map<String, CacheEntry> cache, boolean white)
    {
        if (StringUtils.isEmpty(ip))
        {
            return false;
        }
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(ip);
        if (entry != null && entry.expireAt > now)
        {
            return entry.hit;
        }

        boolean hit = white ? gatewayRedisUtil.isWhiteIp(ip) : gatewayRedisUtil.isBlackIp(ip);
        cache.put(ip, new CacheEntry(hit, now + IP_LIST_CACHE_TTL_MS));
        if (cache.size() > IP_CACHE_MAX_SIZE)
        {
            cache.clear();
        }
        return hit;
    }

    private static final class CacheEntry
    {
        private final boolean hit;
        private final long    expireAt;

        private CacheEntry(boolean hit, long expireAt)
        {
            this.hit = hit;
            this.expireAt = expireAt;
        }
    }


    private LaiKeApiDto buildLaiKeApiDtoFromRequest(HttpServletRequest request)
    {
        LaiKeApiDto dto = new LaiKeApiDto();
        dto.setApi(request.getParameter("api"));
        dto.setModule(request.getParameter("module"));
        dto.setApp(request.getParameter("app"));
        dto.setAction(request.getParameter("action"));
        dto.setM(request.getParameter("m"));
        dto.setVersion(request.getParameter("version"));
        return dto;
    }

    private String buildBusinessApiKey(HttpApiMethod apiMethod, HttpServletRequest request)
    {
        if (apiMethod != null)
        {
            String apiKey = apiMethod.apiKey();
            if (StringUtils.isEmpty(apiKey))
            {
                String[] urls = apiMethod.urlMapping();
                apiKey = (urls != null && urls.length > 0) ? urls[0] : "";
            }
            if (StringUtils.isNotEmpty(apiKey))
            {
                String version = StringUtils.defaultIfEmpty(apiMethod.version(), API_VERSION);
                return apiKey + SplitUtils.XSD + version;
            }
        }
        try
        {
            LaiKeApiDto dto = buildLaiKeApiDtoFromRequest(request);
            return getApiKey(dto, request);
        }
        catch (Exception e)
        {
            log.debug("Failed to build apiKey in gateway interceptor", e);
        }
        return request.getRequestURI();
    }

    public String getApiKey(LaiKeApiDto dto,HttpServletRequest request) throws LaiKeAPIException
    {
        String apiKey = dto.getApi();
        if (StringUtils.isEmpty(apiKey))
        {
            String module = dto.getModule();
            String app    = dto.getApp();
            String action = dto.getAction();
            if (StringUtils.isEmpty(module) || StringUtils.isEmpty(action))
            {
                // 这时候是网关测试或者xxl-job的请求地址
                // 兜底：用 URI + 几个关键参数
                String fallback = request.getRequestURI();
                String query = request.getQueryString();
                if (query != null && query.length() > 0) {
                    fallback += "?" + query.substring(0, Math.min(80, query.length()));
                }
                return fallback;
            }
            if (StringUtils.isEmpty(app))
            {
                app = StringUtils.defaultIfEmpty(dto.getM(), API_DEFAULT_METHOD);
            }
            apiKey = module.trim() + SplitUtils.XSD + action.trim() + SplitUtils.XSD + app.trim();
            String version = StringUtils.defaultIfEmpty(dto.getVersion(), API_VERSION);
            return apiKey + SplitUtils.XSD + version;
        }
        else
        {
            String version = StringUtils.defaultIfEmpty(dto.getVersion(), API_VERSION);
            return apiKey + SplitUtils.XSD + version;
        }
    }
}
