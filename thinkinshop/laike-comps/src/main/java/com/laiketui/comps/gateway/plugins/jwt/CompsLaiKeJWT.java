package com.laiketui.comps.gateway.plugins.jwt;

import com.laiketui.root.gateway.exception.LaiKeGWException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/26 15:17
 * @version:
 * @modified By:
 */
public class CompsLaiKeJWT
{

    /**
     * token 过期时间, 单位: 秒. 这个值表示 30 天
     */
    public static final long TOKEN_EXPIRED_TIME = 30 * 24 * 60 * 60;

    /**
     * jwt 加密解密密钥
     */
    public static String JWT_SECRET = System.getenv("GATEWAY_JWT_SECRET");
    static {
        if (JWT_SECRET == null || JWT_SECRET.isEmpty()) {
            JWT_SECRET = "d3d3LmxhaWt0dWkudG9rZXJu";
        }
    }

    public static final String LKT_TOKEN_ID = "lkt_tokenId";

    /**
     * 创建JWT
     */
    public static String createJWT(Map<String, Object> claims, Long time)
    {
        //指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        Date               now                = new Date(System.currentTimeMillis());
        SecretKey          secretKey          = generalKey();
        //生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        //下面就是在为payload添加各种标准声明和私有声明了
        //这里其实就是new一个JwtBuilder，设置jwt的body
        JwtBuilder builder = Jwts.builder()
                //如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                //设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                .setId(LKT_TOKEN_ID)
                //iat: jwt的签发时间
                .setIssuedAt(now)
                //设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, secretKey);
        if (time >= 0)
        {
            long expMillis = nowMillis + time;
            Date exp       = new Date(expMillis);
            //设置过期时间
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 验证jwt
     */
    public static Claims verifyJwt(String token)
    {
        //签名秘钥，和生成的签名的秘钥一模一样
        SecretKey key = generalKey();
        Claims    claims;
        try
        {
            //得到DefaultJwtParser
            claims = Jwts.parser()
                    //设置签名的秘钥
                    .setSigningKey(key)
                    .parseClaimsJws(token).getBody();
        }
        catch (Exception e)
        {
            throw new LaiKeGWException("20003", "令牌校验出错!");
        }
        return claims;
    }


    /**
     * 由字符串生成加密key
     *
     * @return
     */
    public static SecretKey generalKey()
    {
        String    stringKey  = JWT_SECRET;
        byte[]    encodedKey = Base64.decodeBase64(stringKey);
        SecretKey key        = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

    /**
     * 根据userId和openid生成token
     */
    public static String generateToken(String openId, Integer userId)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("openId", openId);
        return createJWT(map, TOKEN_EXPIRED_TIME);
    }


    public static void main(String[] args)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "imvvx");
        claims.put("role", "1,23,");
        String jwt = CompsLaiKeJWT.createJWT(claims, 5000L);
        System.out.println(CompsLaiKeJWT.createJWT(claims, 5000L));
        Claims claims1 = CompsLaiKeJWT.verifyJwt(jwt);
        System.out.println(claims1.get("role"));
        System.out.println(claims1.get("username"));
    }

}
