package com.laiketui.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.utils.okhttp.HttpUtils;
import com.laiketui.core.cache.GatewayRedisUtil;
import com.laiketui.core.common.LaiKeGlobleConst;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.domain.LaiKeApi;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.lktconst.gwconst.LaiKeGWConst;
import com.laiketui.core.utils.tool.NetworkUtils;
import com.laiketui.root.gateway.dto.LaiKeApiDto;
import com.laiketui.root.gateway.impl.APIServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 高并发安全 HTTP-API 调用工具
 * 支持指定版本号 version
 * 随机负载选择节点
 * TCP 探测可用节点
 * 兼容原调用方法
 * 时间轴 (秒)
 *
 * 0        15        30        45        60
 * |---------|---------|---------|---------|
 *
 * 注册端心跳 (HEARTBEAT_INTERVAL_MILLIS = 15s)
 * 每 15 秒刷新一次 lastHeartbeatTime
 * 0s ↑
 * 15s ↑
 * 30s ↑
 * 45s ↑
 *
 * 调用端节点有效判断 (HEARTBEAT_TIMEOUT_MS = 30s)
 * 节点被认为失效条件：
 * now - lastHeartbeatTime > 30s
 * 示例：
 * - 0~30s => 节点有效
 * - 30s+ => 如果心跳未更新，节点被判定失效
 *
 * Redis key TTL (API_TTL_SECONDS = 45s)
 * - 每次心跳刷新 key 的过期时间为 45s
 * - 保证 key 在节点失效前不被 Redis 删除
 * - 如果节点长时间心跳停止，45s 后 key 自动过期
 *
 */
@Component
public class HttpApiUtils
{

    /**
     * 调用端判断节点是否可用的最大允许间隔，如果 now - lastHeartbeatTime > HEARTBEAT_TIMEOUT_MS，就认为节点失效
     * api有效满足：now - api.getLastHeartbeatTime() <= HEARTBEAT_TIMEOUT_MS
     */
    private static final long HEARTBEAT_TIMEOUT_MS = 30_000L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GatewayRedisUtil gatewayRedisUtil;

    @Autowired
    private APIServiceFactory serviceFactory;

    // =========================================
    // 1️⃣ 内部接口调用（Dubbo / Gateway）
    // =========================================
    public Map<String, Object> executeApi(String apiKey, String params) throws LaiKeAPIException
    {
        return executeApi(apiKey, params, null);
    }

    public Map<String, Object> executeApi(String apiKey, String params, String version) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(1);
        try
        {
            String apiVersion = (version != null && !version.isEmpty()) ? version : LaiKeGWConst.API_VERSION;
            String apiCacheKey = LaiKeGlobleConst.RedisCacheKeyPre.LAIKE_API
                    + SplitUtils.UNDERLIEN + apiKey
                    + SplitUtils.UNDERLIEN + apiVersion;

            LinkedList<LaiKeApi> apis = getAliveApis(apiCacheKey);
            if (apis.isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.ContainerErrorCode.GW_API_NOT_EXISTENT,
                        "接口不存在或无可用节点, apiKey=" + apiKey + ", version=" + apiVersion);
            }

            LaiKeApi api = selectRandomApiWithTcpCheck(apis);

            LaiKeApiDto dto = new LaiKeApiDto();
            dto.setParams(params);

            Result result = serviceFactory.getGatewayService(api.getProtocol()).invoke(api, dto);

            if (LaiKeGWConst.GW_SUCCESS.equals(result.getCode()))
            {
                String data = result.getData().toString();
                if (!GloabConst.ManaValue.MANA_VALUE_SUCCESS.equals(data))
                {
                    resultMap = JSON.parseObject(data, new TypeReference<Map<String, Object>>()
                    {
                    });
                }
            }
            else
            {
                throw new LaiKeAPIException(result.getCode(), result.getMessage(), "executeApi");
            }

        }
        catch (Exception e)
        {
            logger.error("远程调用 {} 异常", apiKey, e);
            throw new LaiKeAPIException(ErrorCode.ContainerErrorCode.GW_API_EXECUTE_FAIL, e.getMessage(), "executeApi");
        }
        return resultMap;
    }

    // =========================================
    // 2️⃣ HTTP 接口调用（兼容老方法）
    // =========================================
    public Map<String, Object> executeHttpApi(String apiKey, Map<String, Object> params) throws LaiKeAPIException
    {
        return executeHttpApi(apiKey, params, MediaType.APPLICATION_JSON_VALUE, null);
    }

    public Map<String, Object> executeHttpApi(String apiKey, Map<String, Object> params, String mediaType) throws LaiKeAPIException
    {
        return executeHttpApi(apiKey, params, mediaType, null);
    }

    public Map<String, Object> executeHttpApi(String apiKey, Map<String, Object> params, String mediaType, String version) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(1);
        try
        {
            String apiVersion = (version != null && !version.isEmpty()) ? version : LaiKeGWConst.API_VERSION;
            String apiCacheKey = LaiKeGlobleConst.RedisCacheKeyPre.LAIKE_API
                    + SplitUtils.UNDERLIEN + apiKey
                    + SplitUtils.UNDERLIEN + apiVersion;

            LinkedList<LaiKeApi> apis = getAliveApis(apiCacheKey);
            if (apis.isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.ContainerErrorCode.GW_API_NOT_EXISTENT,
                        "接口不存在或无可用节点, apiKey=" + apiKey + ", version=" + apiVersion);
            }

            LaiKeApi api = selectRandomApiWithTcpCheck(apis);

            String json;
            switch (mediaType)
            {
                case MediaType.APPLICATION_FORM_URLENCODED_VALUE:
                case MediaType.MULTIPART_FORM_DATA_VALUE:
                    json = HttpUtils.postFormData(api.getUri(), params);
                    break;
                default:
                    json = HttpUtils.post(api.getUri(), JSON.toJSONString(params));
            }

            Result result = JSON.parseObject(json, Result.class);
            if (LaiKeGWConst.GW_SUCCESS.equals(result.getCode()))
            {
                String data = result.getData().toString();
                if (!GloabConst.ManaValue.MANA_VALUE_SUCCESS.equals(data))
                {
                    resultMap = JSON.parseObject(data, new TypeReference<Map<String, Object>>()
                    {
                    });
                }
            }
            else
            {
                throw new LaiKeAPIException(result.getCode(), result.getMessage(), "executeHttpApi");
            }

        }
        catch (Exception e)
        {
            logger.error("远程调用 {} 异常", apiKey, e);
            throw new LaiKeAPIException(ErrorCode.ContainerErrorCode.GW_API_EXECUTE_FAIL, e.getMessage(), "executeHttpApi");
        }
        return resultMap;
    }

    // =========================================
    // 3️⃣ 获取心跳有效节点
    // =========================================
    private LinkedList<LaiKeApi> getAliveApis(String redisKey)
    {
        Map<String, String>  allApis = gatewayRedisUtil.hmget(redisKey);
        LinkedList<LaiKeApi> result  = new LinkedList<>();
        long                 now     = System.currentTimeMillis();
        for (String v : allApis.values())
        {
            LaiKeApi api = JSON.parseObject(v, LaiKeApi.class);
            if (api.getLastHeartbeatTime() > 0 && (now - api.getLastHeartbeatTime()) <= HEARTBEAT_TIMEOUT_MS)
            {
                result.add(api);
            }
        }
        return result;
    }

    // =========================================
    // 4️⃣ 随机负载 + TCP 探测
    // =========================================
    private LaiKeApi selectRandomApiWithTcpCheck(LinkedList<LaiKeApi> apis)
    {
        ThreadLocalRandom random   = ThreadLocalRandom.current();
        // 尝试次数
        int               attempts = Math.min(apis.size(), 2);
        for (int i = 0; i < attempts; i++)
        {
            LaiKeApi api = apis.get(random.nextInt(apis.size()));
            if (NetworkUtils.isHostConnectable(api.getNodeIp(), api.getPort(), api.getTimeout()))
            {
                return api;
            }
            logger.warn("节点心跳有效但 TCP 连接失败: {}:{} (尝试第{}次)", api.getNodeIp(), api.getPort(), i + 1);
        }
        // 都失败则随机返回一个，保证调用不中断
        return apis.get(random.nextInt(apis.size()));
    }
}
