package com.laiketui.comps.gateway.controller;

import com.laiketui.comps.gateway.loadbalance.GatewayProperties;
import com.laiketui.comps.gateway.loadbalance.LoadBalanceStrategy;
import com.laiketui.comps.gateway.loadbalance.LoadBalanceStrategyFactory;
import com.laiketui.core.cache.GatewayRedisUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.LaiKeGlobleConst;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.domain.HttpApi;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import com.laiketui.core.utils.tool.NetworkUtils;
import com.laiketui.root.gateway.GatewayService;
import com.laiketui.root.gateway.dto.LaiKeApiDto;
import com.laiketui.root.gateway.impl.APIServiceFactory;
import com.laiketui.root.gateway.util.I18nUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import static com.laiketui.core.lktconst.ErrorCode.SysErrorCode.ALL_CODE;
import static com.laiketui.core.lktconst.gwconst.LaiKeGWConst.*;

/**
 * HTTP 服务注册中心 - 去中心化多模块注册实现
 * 支持分布式部署 水平扩展
 */
@RestController("laiKeApiController")
public class CompsLaiKeApiController
{

    private static final Logger logger               = LoggerFactory.getLogger(CompsLaiKeApiController.class);
    private static final long   HEARTBEAT_TIMEOUT_MS = 30_000L;

    private static final String[] AUTH_TOKEN_PREFIXES = {
            GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_PC_TOKEN,
            GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_APP_TOKEN
    };

    @Autowired
    private GatewayRedisUtil gatewayRedisUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private APIServiceFactory serviceFactory;

    @Autowired
    private GatewayProperties gatewayProperties;

    @Autowired
    private LoadBalanceStrategyFactory strategyFactory;


    @RequestMapping("/gw")
    @ResponseBody
    public Result gate(@NotNull @ModelAttribute LaiKeApiDto dto, HttpServletRequest request) throws IOException
    {
        Result result;
        try
        {
            String clientIp = NetworkUtils.getClientIp(request);

            if (gatewayRedisUtil.isBlackIp(clientIp)) {
                throw new LaiKeAPIException(
                        LaiKeGWConst.RESCODE_NOAUTH,
                        "IP 已被封禁"
                );
            }



            if (dto == null)
            {
                throw new LaiKeAPIException(LaiKeGWConst.GW_BAD_REQ, "网关已启动，本次请求异常");
            }

            String apiCacheKey = getApiKey(dto);

            // ================== 使用 GatewayRedisUtil 获取存活实例 ==================
            LinkedList<LaiKeApi> aliveApis = loadAliveApis(apiCacheKey);
            if (aliveApis.isEmpty())
            {
                throw new LaiKeAPIException(LaiKeGWConst.API_OFFLINE,
                        "服务【" + apiCacheKey + "】暂无可用实例");
            }

            //接口负载 支持在线修改 负载方式：轮询和随机 其他负载方式可扩展：权重、最低活跃度
            LoadBalanceStrategy strategy = strategyFactory.getStrategy();
            LaiKeApi api = strategy.select(aliveApis, apiCacheKey);

            GatewayService gatewayService = serviceFactory.getGatewayService(api.getProtocol());
            result = gatewayService.invoke(api, dto);

        }
        catch (LaiKeAPIException e)
        {
            logger.error("网关调用异常，code={}, message={}", e.getCode(), e.getMessage(), e);
            result = Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
        catch (Exception e)
        {
            logger.error("网关未知异常", e);
            result = Result.error("系统异常，请稍后再试");
        }

        return getResult(result, dto);
    }

    /**
     * 从 Redis 加载存活实例（基于心跳），高并发安全
     */
    private LinkedList<LaiKeApi> loadAliveApis(String apiCacheKey)
    {
        LinkedList<LaiKeApi> aliveApis    = new LinkedList<>();
        long                 now          = System.currentTimeMillis();
        Map<String, String>  allInstances = gatewayRedisUtil.hmget(apiCacheKey);
        for (String json : allInstances.values())
        {
            if (json == null || json.isEmpty()) continue;
            try
            {
                LaiKeApi api = com.alibaba.fastjson2.JSON.parseObject(json, LaiKeApi.class);
                if (api instanceof HttpApi)
                {
                    HttpApi httpApi       = (HttpApi) api;
                    long    lastHeartbeat = httpApi.getLastHeartbeatTime();
                    if (lastHeartbeat > 0 && (now - lastHeartbeat) < HEARTBEAT_TIMEOUT_MS)
                    {
                        aliveApis.add(api);
                    }
                }
                else
                {
                    aliveApis.add(api);
                }
            }
            catch (Exception e)
            {
                logger.warn("解析 Redis 实例失败: {}", e.getMessage());
            }
        }
        return aliveApis;
    }

    public String getApiKey(LaiKeApiDto dto) throws LaiKeAPIException
    {
        String apiKey = dto.getApi();
        if (StringUtils.isEmpty(apiKey))
        {
            String module = dto.getModule();
            String app    = dto.getApp();
            String action = dto.getAction();
            if (StringUtils.isEmpty(module) || StringUtils.isEmpty(action))
            {
                throw new LaiKeAPIException(LaiKeGWConst.GW_BAD_REQ, "异常请求");
            }
            if (StringUtils.isEmpty(app))
            {
                app = StringUtils.defaultIfEmpty(dto.getM(), API_DEFAULT_METHOD);
            }
            apiKey = module.trim() + SplitUtils.XSD + action.trim() + SplitUtils.XSD + app.trim();
        }
        String version = StringUtils.defaultIfEmpty(dto.getVersion(), API_VERSION);
        return LaiKeGlobleConst.RedisCacheKeyPre.LAIKE_API + SplitUtils.UNDERLIEN + apiKey + SplitUtils.UNDERLIEN + version;
    }

    private Result getResult(Result result, LaiKeApiDto dto)
    {
        try
        {
            if (result == null && !LaiKeGWConst.EXP_FLAG.equals(dto.getExportType()))
            {
                return Result.error("返回结果为空");
            }
            if (LaiKeGWConst.EXP_FLAG.equals(dto.getExportType())) return result;

            if (StringUtils.isNotEmpty(result.getCode()) && !ALL_CODE.equals(result.getCode()))
            {
                String tips = I18nUtils.getMessage(result.getCode());
                if (StringUtils.isNotEmpty(tips)) result.setMessage(tips);
            }
            return result;
        }
        catch (Exception e)
        {
            logger.error("getResult 异常", e);
            if (result == null) result = Result.error();
            result.setMessage("系统处理异常");
            return result;
        }
    }

    private boolean doAuth(@ModelAttribute @NotNull LaiKeApiDto dto, HttpServletRequest request, LaiKeApi api)
    {
        if (!api.getLogin()) return true;

        String token = request.getHeader(HEARDER_TOKEN);
        if (StringUtils.isEmpty(token)) token = dto.getAccess_id();
        if (StringUtils.isEmpty(token)) return false;

        for (String prefix : AUTH_TOKEN_PREFIXES)
        {
            if ( redisUtil.get(prefix + token) != null )
            {
                return true;
            }
        }
        return false;
    }

}
