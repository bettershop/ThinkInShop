package com.laiketui.common.utils.tool.jwt;

import com.laiketui.core.exception.LaiKeCommonException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 支持 UA/IP 可选绑定校验
 */
public final class JwtUtils
{

    /**
     * 默认 token 过期时间（秒）= 2小时
     */
    public static final int DEFAULT_TOKEN_EXPIRED_TIME = 7200;

    /**
     * 签名密钥（可从配置中心获取）
     */
    private static String TOKEN_SECRET = System.getenv("JWT_SECRET");
    static {
        if (TOKEN_SECRET == null || TOKEN_SECRET.isEmpty()) {
            TOKEN_SECRET = "hello..laiketui.com";
        }
    }

    /**
     * 预计算的密钥字节数组
     */
    private static final byte[] SECRET_KEY_BYTES = TextCodec.BASE64.encode(TOKEN_SECRET).getBytes();

    /**
     * 自定义 Claim Key
     */
    private static final String CLAIM_KEY_USER_AGENT = "ua";
    private static final String CLAIM_KEY_IP_ADDRESS = "ip";

    /**
     * 是否启用 UA/IP 校验，默认关闭
     */
    private static final boolean ENABLE_UA_IP_CHECK = true;

    private JwtUtils()
    {
    }

    // ========================
    // Token 生成方法
    // ========================

    /**
     * 生成 JWT Token
     * 会在 Web 上下文中自动获取 UA/IP 并写入
     */
    public static String createToken(Map<String, Object> data, int lifeTimeSeconds)
    {
        if (data != null)
        {
            data.remove("exp");
        }

        Date exp = new Date(System.currentTimeMillis() + lifeTimeSeconds * 1000L);

        JwtBuilder builder = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setIssuedAt(new Date())
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY_BYTES);

        if (!CollectionUtils.isEmpty(data))
        {
            builder.addClaims(data);
        }

        // 尝试从 Web 上下文获取 UA/IP
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null)
        {
            String ua = request.getHeader("User-Agent");
            String ip = getClientIpAddress(request);

            if (ua != null) builder.claim(CLAIM_KEY_USER_AGENT, ua);
            if (ip != null) builder.claim(CLAIM_KEY_IP_ADDRESS, ip);
        }

        return builder.compact();
    }

    public static String getToken()
    {
        return createToken(null, DEFAULT_TOKEN_EXPIRED_TIME);
    }

    public static String getToken(int lifeTimeSeconds)
    {
        return createToken(null, lifeTimeSeconds);
    }

    public static String getToken(Map<String, Object> tokenData, int lifeTimeSeconds)
    {
        return createToken(tokenData, lifeTimeSeconds);
    }

    public static String getTokenWithData(Map<String, Object> data)
    {
        return createToken(data, DEFAULT_TOKEN_EXPIRED_TIME);
    }

    /**
     * 验证 Token 合法性 + 过期 + UA/IP（可选）
     */
    public static Claims verifyJwt(String token) throws LaiKeCommonException
    {
        try
        {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY_BYTES)
                    .parseClaimsJws(token)
                    .getBody();

            Date exp = claims.getExpiration();
            if (exp == null || exp.before(new Date()))
            {
                throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "令牌已过期", "verifyJwt");
            }

            // 可选 UA/IP 校验
            if (ENABLE_UA_IP_CHECK)
            {
                HttpServletRequest request = getCurrentHttpRequest();
                if (request != null)
                {
                    String uaStored = claims.get(CLAIM_KEY_USER_AGENT, String.class);
                    String ipStored = claims.get(CLAIM_KEY_IP_ADDRESS, String.class);

                    String uaNow = request.getHeader("User-Agent");
                    String ipNow = getClientIpAddress(request);

                    if (uaStored != null && !uaStored.equals(uaNow))
                    {
                        throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "User-Agent 不匹配", "verifyJwt");
                    }
                    if (ipStored != null && !ipStored.equals(ipNow))
                    {
                        throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "IP 不匹配", "verifyJwt");
                    }
                }
            }

            return claims;
        }
        catch (ExpiredJwtException e)
        {
            throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "令牌已过期", "verifyJwt");
        }
        catch (SignatureException e)
        {
            throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "签名验证失败", "verifyJwt");
        }
        catch (MalformedJwtException e)
        {
            throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "令牌格式错误", "verifyJwt");
        }
        catch (JwtException | IllegalArgumentException e)
        {
            throw new LaiKeCommonException(ErrorCode.ContainerErrorCode.JWT_VERIFY_FAIL, "令牌验证失败: " + e.getMessage(), "verifyJwt");
        }
    }

    /**
     * 获取当前 Web 请求（可能为空）
     */
    private static HttpServletRequest getCurrentHttpRequest()
    {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes)
        {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    /**
     * 获取客户端真实 IP
     */
    private static String getClientIpAddress(HttpServletRequest request)
    {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !"unknown".equalsIgnoreCase(xForwardedFor))
        {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !"unknown".equalsIgnoreCase(xRealIp))
        {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    public static void main(String[] args)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        data.put("role", "admin");

        String token = getTokenWithData(data);
        System.out.println("生成的Token: " + token);

        try
        {
            Claims claims = verifyJwt(token);
            System.out.println("验证通过，过期时间: " + DateUtil.dateFormate(claims.getExpiration(), GloabConst.TimePattern.YMDHMS));
            System.out.println("携带数据: " + claims);
        }
        catch (LaiKeCommonException e)
        {
            System.err.println("令牌验证失败: " + e.getMessage());
        }
    }
}
