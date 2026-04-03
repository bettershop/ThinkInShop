package com.laiketui.core.utils.tool;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 网络节点是否可用
 *
 * @author wangxian
 */
public class NetworkUtils
{

    private static final String UNKNOWN = "unknown";

    // IPv4 / IPv6 简单校验（不做 DNS）
    private static final Pattern IP_PATTERN = Pattern.compile(
            "((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}" +
                    "(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)|" +
                    "([0-9a-fA-F:]+)"
    );

    /**
     * 节点是否在线
     *
     * @param host
     * @param timeOut
     * @return
     */
    public static boolean isHostReachable(String host, Integer timeOut)
    {
        try
        {
            return InetAddress.getByName(host).isReachable(timeOut);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 节点指定端口是否开启了
     *
     * @param host
     * @param port
     * @return
     */
    public static boolean isHostConnectable(String host, int port, int timeOut)
    {
        Socket socket = new Socket();
        try
        {
            socket.setSoTimeout(timeOut);
            //这方式添加连接超时时间，在时间内进行尝试建立连接期间该方法是阻塞的，就是有多个不可用端口的请求进入的时候会在这里阻塞等待，导致正常的请求也会在这里阻塞
            socket.connect(new InetSocketAddress(host, port));
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 获取客户端真实 IP（新增方法，推荐使用）
     * <p>
     * 优先级：
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. Proxy-Client-IP
     * 4. WL-Proxy-Client-IP
     * 5. request.getRemoteAddr()
     */
    public static String getClientIp(HttpServletRequest request)
    {
        if (request == null)
        {
            return "0.0.0.0";
        }

        String ip;

        // 1️⃣ X-Forwarded-For（最重要）
        ip = request.getHeader("X-Forwarded-For");
        if (isValid(ip))
        {
            // 多级代理，第一个才是真实客户端
            ip = ip.split(",")[0].trim();
            if (isValid(ip))
            {
                return normalize(ip);
            }
        }

        // 2️⃣ X-Real-IP（Nginx）
        ip = request.getHeader("X-Real-IP");
        if (isValid(ip))
        {
            return normalize(ip);
        }

        // 3️⃣ 其他代理头
        ip = request.getHeader("Proxy-Client-IP");
        if (isValid(ip))
        {
            return normalize(ip);
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValid(ip))
        {
            return normalize(ip);
        }

        // 4️⃣ 最终兜底
        ip = request.getRemoteAddr();
        return normalize(ip);
    }

    /**
     * 校验 IP 是否可信
     */
    private static boolean isValid(String ip)
    {
        if (ip == null || ip.length() == 0) return false;
        if (UNKNOWN.equalsIgnoreCase(ip)) return false;
        return IP_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * 处理 IPv6 本地回环
     */
    private static String normalize(String ip)
    {
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip))
        {
            return "127.0.0.1";
        }
        return ip;
    }
}
