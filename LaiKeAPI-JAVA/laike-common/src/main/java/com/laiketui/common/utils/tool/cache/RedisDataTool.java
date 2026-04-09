package com.laiketui.common.utils.tool.cache;

import com.alibaba.fastjson2.JSON;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.son.MchAdminModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存辅助类
 *
 * @author Trick
 * @date 2020/9/30 15:38
 */
public class RedisDataTool
{

    private static Logger logger = LoggerFactory.getLogger(RedisDataTool.class);


    /**
     * 通过storetype 获取用户redis中存储的前缀
     *
     * @param storeType
     * @return
     */
    public static String getUserLoginKeyByStoreType(int storeType)
    {
        String userCachePreKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
        switch (storeType)
        {
            case GloabConst.StoreType.STORE_TYPE_H5:
            case GloabConst.StoreType.STORE_TYPE_APP:
            case GloabConst.StoreType.STORE_TYPE_PC_MALL:
            case GloabConst.StoreType.STORE_TYPE_WX_MP:
                userCachePreKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
                break;
            case GloabConst.StoreType.STORE_TYPE_PC_ADMIN:
                userCachePreKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN;
                break;
            case GloabConst.StoreType.STORE_TYPE_PC_LIVING:
                userCachePreKey = GloabConst.RedisHeaderKey.LOGIN_CODE_PC_LIVING_KEY;
                break;
            case GloabConst.StoreType.STORE_TYPE_PC_MCH:
                userCachePreKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN;
                break;
        }
        return userCachePreKey;
    }

    /**
     * 根据token获取用户-app
     *
     * @param accessId -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 9:52
     */
    public static User getRedisUserCache(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        return getRedisUserCache(accessId, redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN);
    }

    public static User getRedisUserCache(String accessId, RedisUtil redisUtil, String tokenKey) throws LaiKeAPIException
    {
        if (StringUtils.isEmpty(tokenKey))
        {
            tokenKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
        }
        //获取当前登录用户数据
        Object cacheValue = redisUtil.get(tokenKey + accessId);
        User   userCache  = null;
        if (cacheValue != null)
        {
            try
            {
                userCache = JSON.parseObject(cacheValue.toString(), User.class);
                if (GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN.equals(tokenKey))
                {
                    if (userCache.getMchId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "未开通店铺或者店铺已注销");
                    }
                }
            }
            catch (LaiKeAPIException l)
            {
                throw l;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }

        return userCache;
    }


    /**
     * 获取用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @param isLogin   - 是否需要登录
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/24 15:41
     */
    public static User getRedisUserCache(String accessId, RedisUtil redisUtil, boolean isLogin) throws LaiKeAPIException
    {
        User user;
        if (isLogin)
        {
            //获取当前登录用户数据
            user = getRedisUserCache(accessId, redisUtil);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
            }
        }
        else
        {
            user = getRedisUserCache(accessId, redisUtil);
        }
        return user;
    }

    public static User getRedisUserCache(String accessId, RedisUtil redisUtil, String tokenKey, boolean isLogin) throws LaiKeAPIException
    {
        User user;
        if (isLogin)
        {
            //获取当前登录用户数据
            user = getRedisUserCache(accessId, redisUtil, tokenKey);
            if (user == null)
            {
                //商城端查看pc店铺授权
                user = getRedisUserCache(accessId, redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN);
                if (user == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
                }
            }
        }
        else
        {
            user = getRedisUserCache(accessId, redisUtil, tokenKey);
        }
        return user;
    }


    /**
     * 获取后台用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @return AdminModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 15:30
     */
    public static AdminModel getRedisAdminUserCache(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        //获取当前登录用户数据
        Object     cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + accessId);
        AdminModel userCache;
        if (cacheValue != null)
        {
            try
            {
                userCache = JSON.parseObject(cacheValue.toString(), AdminModel.class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }
        else
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
        }

        return userCache;
    }

    /**
     * 获取供应商平台用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @return SupplierModel
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/09/21 15:30
     */
    public static SupplierModel getSupplierCache(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        //获取当前登录用户数据
        Object        cacheValue    = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN + accessId);
        SupplierModel supplierModel = null;
        if (cacheValue != null)
        {
            try
            {
                supplierModel = JSON.parseObject(cacheValue.toString(), SupplierModel.class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }
        else
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
        }

        return supplierModel;
    }

    /**
     * 是否登录-任意模块
     *
     * @param accessId  -
     * @param redisUtil -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:07
     */
    public static void isLogin(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        Object cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + accessId);
        if (cacheValue == null)
        {
            cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + accessId);
            if (cacheValue == null)
            {
                cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + accessId);
                if (cacheValue == null)
                {
                    cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN + accessId);
                    if (cacheValue == null)
                    {
                        cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN + accessId);
                        if (cacheValue == null)
                        {
                            cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN + accessId);
                            if (cacheValue == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 返回 用户表 lkt_user 的用户信息 可能有漏的key 【后续完整补充】
     *
     * @return
     */
    public static User getLktUser(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        List<String> keys = new ArrayList<>();
        keys.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN);
        keys.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN);
        keys.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN);
        User user = null;
        for (String key : keys)
        {
            String userKey    = key + accessId;
            Object cacheValue = redisUtil.get(userKey);
            if (cacheValue == null)
            {
                continue;
            }
            user = JSON.parseObject(cacheValue.toString(), User.class);
            if (user != null)
            {
                return user;
            }
        }
        return null;
    }

    /**
     * 是否登录-任意模块，改造能获取到不同端的登录用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 11:07
     */
    public static String isLoginForUserInfo(String accessId, RedisUtil redisUtil)
    {
        String RedisHeaderKey = "";
        Object cacheValue     = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + accessId);
        RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN;
        if (cacheValue == null)
        {
            cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + accessId);
            RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN;
            if (cacheValue == null)
            {
                cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + accessId);
                RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
                if (cacheValue == null)
                {
                    cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN + accessId);
                    RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN;
                    if (cacheValue == null)
                    {
                        cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN + accessId);
                        RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_SUPPLIER_TOKEN;
                        if (cacheValue == null)
                        {
                            cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN + accessId);
                            RedisHeaderKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN;
                            if (cacheValue == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
                            }
                        }
                    }
                }
            }
        }
        return RedisHeaderKey;
    }

    /**
     * redis 更新缓存【管理员】
     *
     * @param user -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/17 16:39
     */
    public static void refreshRedisAdminCache(String accessId, AdminModel user, String tokenKey, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(tokenKey))
            {
                tokenKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN;
            }
            Object cacheValue = redisUtil.get(tokenKey + accessId);
            if (cacheValue != null)
            {
                //时间不变
                long    outTime = redisUtil.getExpire(tokenKey + accessId);
                boolean flag    = redisUtil.set(tokenKey + accessId, JSON.toJSONString(user), outTime);
                if (!flag)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录", "refreshRedisUserCache");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
        }
    }

    /**
     * redis 更新用户信息
     *
     * @param accessId -
     * @param user     -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 9:52
     */
    public static void refreshRedisUserCache(String accessId, User user, String tokenKey, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(tokenKey))
            {
                tokenKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
            }
            if (redisUtil.hasKey(tokenKey + accessId))
            {
                //时间不变
                long    outTime = redisUtil.getExpire(tokenKey + accessId);
                boolean flag    = redisUtil.set(tokenKey + accessId, JSON.toJSONString(user), outTime);
                if (!flag)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
                }
            }
            else if (redisUtil.hasKey(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN + accessId))
            {
                //时间不变
                long    outTime = redisUtil.getExpire(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN + accessId);
                boolean flag    = redisUtil.set(tokenKey + accessId, JSON.toJSONString(user), outTime);
                if (!flag)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
                }
            }
            else if (redisUtil.hasKey(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + accessId))
            {
                //时间不变
                long    outTime = redisUtil.getExpire(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + accessId);
                boolean flag    = redisUtil.set(tokenKey + accessId, JSON.toJSONString(user), outTime);
                if (!flag)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "请重新登录", "refreshRedisUserCache");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
        }
    }

    public static void refreshRedisUserCache(String accessId, User user, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            refreshRedisUserCache(accessId, user, GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN, redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
        }
    }

    /**
     * 验证短信
     *
     * @param phone     - 手机号
     * @param smsType   - 短信类别
     * @param pcode     - 验证码
     * @param redisUtil -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 15:30
     */
    public static void verificationPcode(int smsType, String phone, String pcode, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            Object cacheValue = redisUtil.get(RedisDataTool.getSmsHeade(smsType) + phone);
            if (cacheValue != null)
            {
                String rCode = cacheValue.toString();
                if (!rCode.equals(pcode))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYZMBZQ, "手机验证码不正确");
                }
                redisUtil.del(phone + pcode);
            }
            else
            {
                throw new LaiKeAPIException(StringUtils.isNotEmpty(pcode) ? ErrorCode.BizErrorCode.ERROR_CODE_YZMCWQCXSR : ErrorCode.BizErrorCode.READ_PHONE_CODE_NOT_DATA, "请重新获取验证码");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }

    /**
     * 获取短信缓存头部
     *
     * @param smsType -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 9:52
     */
    public static String getSmsHeade(int smsType) throws LaiKeAPIException
    {
        try
        {
            String header = "";
            switch (smsType)
            {
                case GloabConst.VcodeCategory.LOGIN_CODE:
                    header = GloabConst.RedisHeaderKey.LOGIN_HEADER;
                    break;
                case GloabConst.VcodeCategory.REGISTER_CODE:
                    header = GloabConst.RedisHeaderKey.REGISTER_HEADER;
                    break;
                case GloabConst.VcodeCategory.UPDATE_PHOE_CODE:
                    //修改手机号
                    header = GloabConst.RedisHeaderKey.UPDATE_PHOE_CODE;
                    break;
                case GloabConst.VcodeCategory.UPDATE_PWD_CODE:
                    header = GloabConst.RedisHeaderKey.UPDATE_PASSWORDE;
                    break;
                case GloabConst.VcodeCategory.UPDATE_PWD_PAY_CODE:
                    header = GloabConst.RedisHeaderKey.UPDATE_PWD_PAY_CODE;
                    break;
                case GloabConst.VcodeCategory.DRAWING_CODE:
                    header = GloabConst.RedisHeaderKey.DRAWING_CODE;
                    break;
                case GloabConst.VcodeCategory.CURRENCY_CODE:
                case GloabConst.VcodeCategory.PAY_REFUND_ORDER:
                    header = GloabConst.RedisHeaderKey.CURRENCY_CODE;
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "短信类型参数不存在!", "sendSms");
            }
            return header;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }

    /**
     * 获取订单缓存头部
     *
     * @param smsType -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 9:52
     */
    public static String getOrderHeade(int smsType) throws LaiKeAPIException
    {
        try
        {
            String header = "";
            switch (smsType)
            {
                case 1:
                    header = "SPLIT_ORDER_HEADER_";
                    break;
            }
            return header;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }

    /**
     * 刷新所有端缓存
     *
     * @param user      - 最新数据
     * @param redisUtil -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/8 13:03
     */
    public static void refreshRedisUserCache(User user, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            List<String> loginKeyList = new ArrayList<>();
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG);
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH);
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_FLAG);
            for (String key : loginKeyList)
            {
                String flagKey = key + user.getUser_id();
                String token   = redisUtil.get(flagKey) + "";
                if (StringUtils.isNotEmpty(token))
                {
                    redisUtil.set(token, JSON.toJSONString(user), redisUtil.getExpire(token));
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("刷新用户信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "refreshRedisUserCache");
        }
    }

    /**
     * h5账号登录互斥
     *
     * @param userId    -  当前登录人
     * @param redisUtil -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/5/24 21:03
     */
    public static void kickingUser(String userId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        try
        {
            List<String> loginKeyList = new ArrayList<>();
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG);
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH);
            loginKeyList.add(GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_FLAG);
            for (String key : loginKeyList)
            {
                String flagKey = key + userId;
                redisUtil.del(redisUtil.get(flagKey) + "");
            }
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "系统异常", "kickingUser");
        }
    }

    /**
     * 获取门店核销登录用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @param isLogin   - 是否需要登录
     * @return MchAdminModel
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2024-02-01
     */
    public static MchAdminModel getMchSonUserCache(String accessId, RedisUtil redisUtil, String tokenKey, boolean isLogin) throws LaiKeAPIException
    {
        MchAdminModel adminModel = null;
        if (StringUtils.isEmpty(tokenKey))
        {
            tokenKey = GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_PC_TOKEN;
        }
        //获取当前登录用户数据
        Object cacheValue = redisUtil.get(tokenKey + accessId);
        if (cacheValue != null)
        {
            try
            {
                adminModel = JSON.parseObject(cacheValue.toString(), MchAdminModel.class);
            }
            catch (LaiKeAPIException l)
            {
                throw l;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }
        if (isLogin)
        {
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
            }
        }
        return adminModel;
    }


    /**
     * 根据token获取用户-app
     *
     * @param accessId -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 9:52
     */
    public static User getRedisPcLivingCache(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        return getRedisPcLivingCache(accessId, redisUtil, GloabConst.RedisHeaderKey.LOGIN_CODE_PC_LIVING_KEY);
    }

    /**
     * 通过端类型来拿登录用户信息
     *
     * @param accessId
     * @param redisUtil
     * @param storeType
     * @return
     * @throws LaiKeAPIException
     */
    public static User getRedisPcLivingCache(String accessId, RedisUtil redisUtil, int storeType) throws LaiKeAPIException
    {
        return getRedisPcLivingCache(accessId, redisUtil, getUserLoginKeyByStoreType(storeType));
    }

    public static User getRedisPcLivingCache(String accessId, RedisUtil redisUtil, String tokenKey) throws LaiKeAPIException
    {
        if (StringUtils.isEmpty(tokenKey))
        {
            tokenKey = GloabConst.RedisHeaderKey.LOGIN_CODE_PC_LIVING_KEY;
        }
        //获取当前登录用户数据
        Object cacheValue = redisUtil.get(tokenKey + accessId);
        User   userCache  = null;
        if (cacheValue != null)
        {
            try
            {
                userCache = JSON.parseObject(cacheValue.toString(), User.class);
            }
            catch (LaiKeAPIException l)
            {
                throw l;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }

        return userCache;
    }
}
