package com.laiketui.apps.user.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.user.AppsUserLoginService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.alipay.AliPayUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.common.utils.weixin.AuthUtil;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.LKTSnowflakeIdWorker;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeCommonException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AgreementModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.user.ThirdMappingModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAuthorizeUserInfoVo;
import com.laiketui.domain.vo.user.AlipayUserVo;
import com.laiketui.domain.vo.user.AppletsVo;
import com.laiketui.domain.vo.user.WxAuthPhoneVo;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.*;

/**
 * 登陆
 *
 * @author Trick
 * @date 2020/10/19 9:17
 */
@Service
public class AppsUserLoginServiceImpl implements AppsUserLoginService
{

    private final Logger logger = LoggerFactory.getLogger(AppsUserLoginServiceImpl.class);

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    PubliceService publicService;

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    private ThirdMappingModelMapper thirdMappingModelMapper;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

    @Override
    public Map<String, Object> token(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        String              token     = vo.getAccessId();
        boolean             flag;
        try
        {
            if (StringUtils.isEmpty(token))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录");
            }
            //验证token是否过期
            flag = publicService.verifyToken(vo.getAccessId());
            if (!flag)
            {
                User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user != null)
                {
                    //token 存在
                    resultMap.put("login_status", DictionaryConst.WhetherMaven.WHETHER_OK);
                    flag = true;
                }
                else
                {
                    resultMap.put("login_status", DictionaryConst.WhetherMaven.WHETHER_NO);
                }
            }
            if (!flag)
            {
                //过期或者不存在
                Map<String, Object> parmaMap = new HashMap<>(16);
                if (DictionaryConst.StoreSource.LKT_LY_001.equals(vo.getStoreType() + ""))
                {
                    //小程序 不存在则重新生成token
                    User user = new User();
                    user.setStore_id(vo.getStoreId());
                    user.setAccess_id(vo.getAccessId());
                    user = userBaseMapper.selectOne(user);
                    if (user != null)
                    {
                        token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
                        //刷新用户信息
                        User updateUser = new User();
                        updateUser.setId(user.getId());
                        updateUser.setAccess_id(vo.getAccessId());
                        int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                        if (count < 1)
                        {
                            logger.error("小程序更新token失败");
                        }
                        RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
                        resultMap.put("login_status", DictionaryConst.WhetherMaven.WHETHER_OK);
                        resultMap.put("access_id", token);
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录");
                    }
                }
                else
                {
                    //不是小程序则清空token
                    parmaMap.put("token", token);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("access_id", vo.getAccessId());
                    int count = userBaseMapper.updateUserAccessId(parmaMap);
                    if (count < 1)
                    {
                        logger.debug("用户token更新失败! accessId=" + vo.getAccessId());
                        if (!StringUtils.isEmpty(token))
                        {
                            resultMap.put("login_status", 0);
                        }
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取token出现 异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQYC, "获取token异常", "token");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> isRegister(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取上传配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                String isRegister = configModel.getIs_register();
                // 当注册为免注册，并且来源为小程序
                if (GloabConst.LktConfig.REGISTER_TYPE2.equals(isRegister) && vo.getStoreType() == 1)
                {
                    isRegister = GloabConst.LktConfig.REGISTER_TYPE2;
                }
                else
                {
                    isRegister = GloabConst.LktConfig.REGISTER_TYPE;
                }
                resultMap.put("is_register", isRegister);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDDPPZXX, "未找到店铺配置信息", "isRegister");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("判断是否需要注册 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isRegister");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> loginAccess(MainVo vo, String openid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setWx_id(openid);
            user = userBaseMapper.selectOne(user);
            if (user != null)
            {
                //是否需要更新令牌
                boolean flag  = false;
                String  token = user.getAccess_id();
                if (StringUtils.isEmpty(token))
                {
                    flag = true;
                }
                else
                {
                    //验证令牌是否过期
                    try
                    {
                        JwtUtils.verifyJwt(token);
                    }
                    catch (LaiKeCommonException c)
                    {
                        logger.debug("token:" + token + " 已过期!");
                        flag = true;
                    }
                }
                String newToken = "";
                if (flag)
                {
                    newToken = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
                    User updateUser = new User();
                    updateUser.setId(user.getId());
                    updateUser.setAccess_id(token);
                    int count = userBaseMapper.updateUserInfoById(updateUser);
                    if (count < 1)
                    {
                        logger.info("更新令牌失败 参数:" + JSON.toJSONString(updateUser));
                    }
                }
                Map<String, Object> resultUserMap = new HashMap<>(16);
                resultUserMap.put("user_name", user.getUser_name());
                resultUserMap.put("headimgurl", user.getHeadimgurl());
                if (StringUtils.isEmpty(user.getMima()))
                {
                    resultUserMap.put("nopswd", 1);
                }

                resultMap.put("openid", openid);
                resultMap.put("user_status", 1);
                resultMap.put("user", resultUserMap);
                resultMap.put("access_id", newToken);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "loginAccess");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("更新token 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginAccess");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> saveAuthorizeUserInfo(SaveAuthorizeUserInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //小程序唯一标识
            String appid = "";
            //小程序授权令牌
            String appsecret = "";
            //openid
            String openid = "";
            //session key
            String sessionKey = "";
            //token
            String token = vo.getAccessId();
            //userid
            String userid = "";

            //获取小程序配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                appid = configModel.getAppid();
                appsecret = configModel.getAppsecret();

                if (DictionaryConst.StoreSource.LKT_LY_001.equals(vo.getStoreType() + ""))
                {
                    if (StringUtils.isEmpty(appid) || StringUtils.isEmpty(appsecret))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XCXWPZ, "小程序未配置", "saveAuthorizeUserInfo");
                    }
                    //获取微信session_key
                    String url        = String.format(GloabConst.WeiXinUrl.SESSION_KEY_GET_URL, appid, appsecret, vo.getCode());
                    String resultJson = HttpUtils.get(url);
                    Map<String, Object> resultHttpMap = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
                    {
                    });
                    if (resultHttpMap.containsKey(GloabConst.ManaValue.MANA_VALUE_ERRCODE))
                    {
                        logger.info("授权获取失败 参数：" + JSON.toJSONString(resultHttpMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "saveAuthorizeUserInfo");
                    }
                    else if (resultHttpMap.containsKey(GloabConst.ManaValue.MANA_VALUE_OPPENID))
                    {
                        openid = resultHttpMap.get(GloabConst.ManaValue.MANA_VALUE_OPPENID).toString();
                        sessionKey = resultHttpMap.get(GloabConst.ManaValue.MANA_VALUE_SESSION_KEY).toString();
                    }
                }
                String isRegister = configModel.getIs_register();
                // 当注册为免注册，并且来源为小程序
                if (GloabConst.LktConfig.REGISTER_TYPE2.equals(isRegister) && DictionaryConst.StoreSource.LKT_LY_001.equals(vo.getStoreType() + ""))
                {
                    //登录生命周期
                    int lifeTime = publicUserService.getUserLoginLife(vo.getStoreId());
                    // 授权ID为空,代表没有进入商品详情
                    if (StringUtils.isEmpty(vo.getAccessId()) || GloabConst.ManaValue.MANA_VALUE_UNDEFINDED.equals(vo.getAccessId()))
                    {
                        //生成token
                        token = JwtUtils.getToken(lifeTime);
                    }
                    else
                    {
                        //验证令牌是否过期
                        try
                        {
                            JwtUtils.verifyJwt(token);
                        }
                        catch (LaiKeCommonException c)
                        {
                            logger.debug("token:" + token + " 已过期!正在重新生成token...");
                            token = JwtUtils.getToken(lifeTime);
                        }
                    }

                    //判断是否有用户信息
                    User user = new User();
                    user.setStore_id(vo.getStoreId());
                    user.setWx_id(openid);
                    user = userBaseMapper.selectOne(user);
                    if (user != null)
                    {
                        String uname = user.getUser_name();
                        vo.setNickName(uname);
                        vo.setHeadimgurl(user.getHeadimgurl());

                        User updateUser = new User();
                        updateUser.setId(user.getId());
                        //设置用户名
                        if (!StringUtils.isEmpty(vo.getNickName()))
                        {
                            updateUser.setUser_name(vo.getNickName());
                        }
                        //设置头像
                        if (!StringUtils.isEmpty(vo.getHeadimgurl()))
                        {
                            updateUser.setHeadimgurl(vo.getHeadimgurl());
                        }
                        //设置性别
                        if (vo.getSex() != null)
                        {
                            updateUser.setSex(vo.getSex());
                        }
                        //设置toke
                        updateUser.setAccess_id(token);

                        int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                        if (count < 1)
                        {
                            logger.info("更新用户失败 参数:" + JSON.toJSONString(updateUser));
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return null;
                        }
                    }
                    else
                    {
                        //注册
                        LKTSnowflakeIdWorker builderId = new LKTSnowflakeIdWorker();
                        String               uuid      = builderId.nextId() + "";
                        //邀请人id
                        String fatherUserId = "";
                        //根据规则生成userid
                        userid = configModel.getUser_id() + uuid;
                        if (!StringUtils.isEmpty(vo.getPid()))
                        {
                            //获取邀请人信息
                            UserDistributionModel userDistributionModel = new UserDistributionModel();
                            userDistributionModel.setUser_id(vo.getPid());
                            userDistributionModel.setStore_id(vo.getStoreId());
                            userDistributionModel = userDistributionModelMapper.selectOne(userDistributionModel);
                            if (userDistributionModel != null)
                            {
                                fatherUserId = userDistributionModel.getUser_id();
                            }
                        }
                        //保存用户信息
                        User saveUser = new User();
                        saveUser.setStore_id(vo.getStoreId());
                        saveUser.setAccess_id(token);
                        saveUser.setUser_name(vo.getNickName());
                        saveUser.setWx_id(openid);
                        saveUser.setHeadimgurl(vo.getHeadimgurl());
                        saveUser.setSex(vo.getSex());
                        saveUser.setMobile("");
                        Random r = new Random();
                        saveUser.setZhanghao(r.nextInt(99999999) + "");
                        saveUser.setMima("");
                        saveUser.setSource(vo.getStoreType() + "");
                        saveUser.setReferee(fatherUserId);
                        int id = userBaseMapper.insert(saveUser);
                        if (id < 1)
                        {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            logger.info("保存用户信息失败 参数:" + JSON.toJSONString(saveUser));
                            return null;
                        }
                        id = saveUser.getId();
                        //登陆
                        String event     = "会员" + userid + "授权注册成功";
                        User   loginUser = new User();
                        loginUser.setId(id);
                        loginUser = userBaseMapper.selectOne(loginUser);
                        if (loginUser != null)
                        {
                            //添加一条登陆信息
                            RecordModel recordModel = new RecordModel();
                            recordModel.setStore_id(vo.getStoreId());
                            recordModel.setUser_id(userid);
                            recordModel.setEvent(event);
                            recordModel.setType(RecordModel.RecordType.LOGIN_OR_OUT);
                            int count = recordModelMapper.insert(recordModel);
                            if (count < 1)
                            {
                                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                                logger.info("保存登陆信息失败 参数:" + JSON.toJSONString(recordModel));
                                return null;
                            }
                        }
                        else
                        {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            logger.info("未找到登陆信息 id:" + id);
                            return null;
                        }
                    }
                    Map<String, String> resultUserMap = new HashMap<>(16);
                    resultUserMap.put("user_name", vo.getNickName());
                    resultUserMap.put("headimgurl", vo.getHeadimgurl());

                    resultMap.put("access_id", token);
                    resultMap.put("user", resultUserMap);
                    resultMap.put("openid", openid);
                    //漏洞直接在后台处理 存redis中 不给前端返回这个值
                    redisUtil.set("sessionKey_" + openid, sessionKey);
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDDPPZXX, "未找到店铺配置信息", "saveAuthorizeUserInfo");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("授权时保存用户信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveAuthorizeUserInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> loginIndex(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //店铺图标
            String logoImg = "";
            //店铺名称
            String company = "";
            //注册协议
            String agreementRegisterName = "";
            //隐私协议
            String agreementPrivacyName = "";
            //小程序授权登录logo
            String appLogo = "";
            //获取店铺配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                logoImg = configModel.getLogo();
                logoImg = publicService.getImgPath(logoImg, vo.getStoreId());
                company = configModel.getCompany();
                appLogo = configModel.getApp_logo();
            }
            //查询协议名称
            AgreementModel agreementRegister = new AgreementModel();
            agreementRegister.setStore_id(vo.getStoreId());
            agreementRegister.setType(DictionaryConst.AgreementType.AGREEMENTTYPE_REGISTER);
            agreementRegister = agreementModelMapper.selectOne(agreementRegister);
            if (agreementRegister != null)
            {
                agreementRegisterName = StringUtils.isEmpty(agreementRegister.getName()) ? "注册协议1" : agreementRegister.getName();
            }
            //隐私协议
            AgreementModel agreementPrivacy = new AgreementModel();
            agreementPrivacy.setStore_id(vo.getStoreId());
            agreementPrivacy.setType(DictionaryConst.AgreementType.AGREEMENTTYPE_PRIVACY);
            agreementPrivacy = agreementModelMapper.selectOne(agreementPrivacy);
            if (agreementPrivacy != null)
            {
                agreementPrivacyName = StringUtils.isEmpty(agreementPrivacy.getName()) ? "隐私协议2" : agreementPrivacy.getName();
            }

            resultMap.put("logo", logoImg);
            resultMap.put("company", company);
            resultMap.put("Agreement", agreementRegisterName);
            resultMap.put("Agreement_1", agreementPrivacyName);
            resultMap.put("appLogo", appLogo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("初始化登陆页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginIndex");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getOldPassword(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User   user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String pwd  = user.getMima();
            pwd = CryptoUtil.strDecode(pwd);
            resultMap.put("oldpassword", pwd);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取旧密码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getOldPassword");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> forgetZhanghao(MainVo vo, String zhanghao, Integer type, String cpc, Integer country_num) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = new User();
            user.setStore_id(vo.getStoreId());
            if (type == 0)
            {
                user.setMobile(zhanghao);
                user.setCpc(cpc);
                user.setCountry_num(country_num);
            }
            else
            {
                user.setE_mail(zhanghao);
            }
            user = userBaseMapper.selectOne(user);
            if (Objects.isNull(user))
            {
                throw new LaiKeAPIException(type == 0 ? ErrorCode.BizErrorCode.ERROE_CODE_SJHBCZ : ErrorCode.BizErrorCode.ERROR_CODE_YXBCZ,"账号不存在");
            }

            resultMap.put("mobile", user.getMobile());
            if (StringUtils.isNotEmpty(user.getE_mail()))
            {
                resultMap.put("e_mail", user.getE_mail());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("忘记密码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "forgetZhanghao");
        }
        return resultMap;
    }

    @Override
    public boolean validatePcode(MainVo vo, String phone, String pcode, int type) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.verificationPcode(type, phone, pcode, redisUtil);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证手机验证码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validatePcode");
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(CryptoUtil.strEncode("228651"));
    }

    @Override
    public boolean forgotpassword(MainVo vo, String phone, String pcode, String password) throws LaiKeAPIException
    {
        try
        {
            //短信验证码校验
            if (validatePcode(vo, phone, pcode, GloabConst.VcodeCategory.CURRENCY_CODE))
            {
                return resetPassword(vo, phone, password, null, null, null);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("重置密码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "forgotpassword");
        }
        return false;
    }

    private boolean resetPassword(MainVo vo, String phone, String password, String email, Integer country_num, String cpc)
    {
        try
        {
            User user = new User();
            user.setStore_id(vo.getStoreId());
            if (StringUtils.isNotEmpty(phone))
            {
                user.setCpc(cpc);
                user.setCountry_num(country_num);
                user.setMobile(phone);
            }
            else if (StringUtils.isNotEmpty(email))
            {
                user.setE_mail(email);
            }
            user = userBaseMapper.selectOne(user);
            if (Objects.nonNull(user))
            {
                String pwd = CryptoUtil.strEncode(password);
                if (StringUtils.isNotEmpty(user.getMima()) && password.equals(CryptoUtil.strDecode(user.getMima())))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMHJMMXT, "新密码和旧密码相同");
                }
                User updateUser = new User();
                updateUser.setId(user.getId());
                updateUser.setMima(pwd);
                int count = userBaseMapper.updateUserInfoById(updateUser);
                if (count < 1)
                {
                    logger.info("密码修改失败 参数" + JSON.toJSONString(updateUser));
                }
                else
                {
                    if (StringUtils.isNotEmpty(phone))
                    {
                        //删除短信
                        redisUtil.del(GloabConst.VcodeCategory.CURRENCY_CODE + phone);
                    }
                    else if (StringUtils.isNotEmpty(email))
                    {
                        //删除邮箱
                        redisUtil.del(GloabConst.RedisHeaderKey.MAIL_CODE_KEY + email);
                    }
                    return true;
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "resetPassword");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("重置密码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "resetPassword");
        }
        return false;
    }

    @Override
    public boolean quit(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setAccess_id("");
            int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                logger.info("退出登录失败 参数" + JSON.toJSONString(updateUser));
            }
            else
            {
                //删除缓存
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + user.getUser_id());
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退出登录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "quit");
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changPid(MainVo vo, String fatherId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(fatherId))
            {
                return;
            }
            //分销插件是否开启
            PluginsModel pluginsModel = new PluginsModel();
            pluginsModel.setPlugin_code(DictionaryConst.Plugin.DISTRIBUTION);
            pluginsModel.setStatus(PluginsModel.PluginStatus.STATUS_SUCCESS);
            pluginsModel.setFlag(PluginsModel.PluginUninstall.UNINSTALL_NOT);
            int count = pluginsModelMapper.selectCount(pluginsModel);
            if (count > 0)
            {
                //获取分销配置信息
                DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                distributionConfigModel.setStore_id(vo.getStoreId());
                distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                if (distributionConfigModel != null && DistributionConfigModel.PluginSwitch.PLUGIN_OPEN.equals(distributionConfigModel.getStatus()))
                {
                    //消费确定
                    if (DistributionConfigModel.RelationshipType.PLUGIN_OPEN.equals(distributionConfigModel.getRelationship()))
                    {
                        //判断分销身份
                        UserDistributionModel userDistributionModel = new UserDistributionModel();
                        userDistributionModel.setStore_id(vo.getStoreId());
                        userDistributionModel.setUser_id(user.getUser_id());
                        userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                        if (userDistributionModel != null && !user.getUser_id().equals(fatherId) && !fatherId.equals(user.getReferee()))
                        {
                            logger.error("用户{} 原来上级{} 现在修改成{} ", user.getUser_id(), user.getReferee(), fatherId);
                            //修改关系
                            User userUpdate = new User();
                            userUpdate.setId(user.getId());
                            userUpdate.setReferee(fatherId);
                            count = userBaseMapper.updateByPrimaryKeySelective(userUpdate);
                            if (count < 1)
                            {
                                logger.error("修改失败!{}", JSON.toJSONString(userUpdate));
                            }
                            //刷新缓存
                            user = userBaseMapper.selectByPrimaryKey(user.getId());
                            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
                        }
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改用户推荐人 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "changPid");
        }
    }

    @Override
    public Map<String, Object> appletsParam(AppletsVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            logger.error("configModel为：{}", configModel);
            JSONObject jsonObject = AuthUtil.getOpenIdAndToken(configModel.getAppid(), configModel.getAppsecret(), vo.getCode());
            if (Objects.isNull(jsonObject.getString("openid")))
            {
                logger.error("授权失败:" + jsonObject.toJSONString());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "appletsParam");
            }
            String openId     = jsonObject.getString("openid");
            String sessionKey = jsonObject.getString("session_key");
            redisUtil.set("sessionKey_" + openId, sessionKey);
            resultMap.put("openid", openId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("小程序授权获取参数 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "appletsParam");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applets(AppletsVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
//            ConfigModel configModel = new ConfigModel();
//            configModel.setStore_id(vo.getStoreId());
//            configModel = configModelMapper.selectOne(configModel);
//            JSONObject jsonObject = AuthUtil.getOpenIdAndToken(configModel.getAppid(), configModel.getAppsecret(), vo.getCode());
            if (Objects.isNull(vo.getOpenid()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
            }
            if (vo.getSex() == null || vo.getSex() < 1)
            {
                vo.setSex(1);
            }
            //公告id
            Integer tell_id = 0;
            //公告标题
            String systemMsgTitle = "";
            //系统公告
            String systemMsg = "";
            //公告类型 1=系统维护 2=升级公告 3-- 普通公告
            int systemMsgType = 0;
            //维护开始、结束时间
            String systemMsgStartDate = "";
            String systemMsgEndDate   = "";
            String openId             = vo.getOpenid();
            //清空当前微信用户所绑定的账户
            userBaseMapper.emptyingWxId(vo.getStoreId(), openId);
            User user = new User();
            //注册用户信息
            User user1 = new User();
            user1.setUser_name(vo.getNickName());
            user1.setHeadimgurl(vo.getHeadImgUrl());
            user1.setSex(vo.getSex());
            user1.setWx_id(openId);
            user1.setWx_name(vo.getNickName());
            //小程序一键注册随机生成账号
            user1.setZhanghao(BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10));
            publicUserService.register(vo, vo.getPid(), user1);
            user = user1;
            resultMap.put("access_id", user1.getAccess_id());
            resultMap.put("user_name", user1.getUser_name());
            resultMap.put("headimgurl", user1.getHeadimgurl());
            String token = vo.getAccessId();
            //验证Token是否过期
            try
            {
                JwtUtils.verifyJwt(user.getAccess_id());
            }
            catch (LaiKeCommonException c)
            {
                logger.debug("token已过期!");
            }
            if (StringUtils.isEmpty(token))
            {
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            //登录成功流程
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            publicUserService.loginSuccess(user, token, vo.getPid());

            resultMap.put("y_password", 1);
            resultMap.put("access_id", token);
            resultMap.put("user", user);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", 0);
            parmaMap.put("startDate_lt", new Date());
            parmaMap.put("endDate_gt", new Date());
            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("user_tell", SystemTellModel.TELL.YES);
            parmaMap.put("user_store_id", vo.getStoreId());
            parmaMap.put("store_type", vo.getStoreType());
            parmaMap.put("read_id", user.getUser_id());
            List<Map<String, Object>> systemList = systemTellModelMapper.selectDynamic(parmaMap);
            if (systemList.size() > 0)
            {
                Map<String, Object> systemMap = systemList.get(0);
                systemMsgTitle = MapUtils.getString(systemMap, "title");
                systemMsg = MapUtils.getString(systemMap, "content");
                systemMsgType = MapUtils.getIntValue(systemMap, "type");
                tell_id = MapUtils.getInteger(systemMap, "id");
                systemMsgEndDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "enddate"), GloabConst.TimePattern.YMDHMS);
                systemMsgStartDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "startdate"), GloabConst.TimePattern.YMDHMS);
            }
            String info = "登录成功";
            if (systemMsgType == 1)
            {
                info = "系统维护中";
            }

            //系统公告
            resultMap.put("systemMsgTitle", systemMsgTitle);
            resultMap.put("systemMsg", systemMsg);
            resultMap.put("systemMsgType", systemMsgType);
            resultMap.put("tell_id", tell_id);
            resultMap.put("systemMsgEndDate", systemMsgEndDate);
            resultMap.put("systemMsgStartDate", systemMsgStartDate);
            resultMap.put("info", info);
            //登陆成功
            resultMap.put("access_id", user.getAccess_id());
            String userName = user.getUser_name();
            resultMap.put("user_name", userName);

            Integer preferred_currency = user.getPreferred_currency();

            //默认币种和汇率 默认币种一定会有 不能删除 除非是在数据库层面删除
            Map defaultCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(user.getStore_id());
            resultMap.put("storeCurrency", defaultCurrencyMap);
            if (preferred_currency != null)
            {
                resultMap.put("preferred_currency", user.getPreferred_currency());
                resultMap.put("userCurrency", currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency()));
            }
            else
            {
                resultMap.put("preferred_currency", MapUtils.getIntValue(defaultCurrencyMap, "currency_id"));
                resultMap.put("userCurrency", defaultCurrencyMap);
            }

            resultMap.put("user_id", user.getUser_id());
            resultMap.put("headimgurl", user.getHeadimgurl());
            resultMap.put("lang", user.getLang());
            resultMap.put("cpc", user.getCpc());
            resultMap.put("country_num", user.getCountry_num());

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("小程序授权登录/注册异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "applets");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> appletsWx(AppletsVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String appId, appSecret, openId = "", unionId, userApiToken;
            logger.debug("公众号授权");
            String configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT);
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "微信公众号配置信息不存在", "aliUserLogin");
            }
            JSONObject dataJson = new JSONObject();
            if (StringUtils.isEmpty(vo.getUserApiToken()))
            {
                //将表中取出来的值map化
                Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
                {
                });
                appId = MapUtils.getString(map, "appid");
                appSecret = MapUtils.getString(map, "appsecret");
                String url = String.format(GloabConst.WeiXinUrl.SESSION_KEY_GET_ACCESS_TOKEN_BY_CODE, appId, appSecret, vo.getCode(), "authorization_code");
                logger.debug("通过 code 获取access_token url:{}", url);
                String data = HttpUtils.get(url);
                if (StringUtils.isEmpty(data))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
                }
                dataJson = JSONObject.parseObject(data);
                openId = dataJson.getString("openid");
                if (StringUtils.isEmpty(openId))
                {
                    logger.error("授权失败,没有获取到openid");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
                }
                unionId = dataJson.getString("unionid");
                userApiToken = dataJson.getString("access_token");
            }
            else
            {
                unionId = vo.getUnionId();
                userApiToken = vo.getUserApiToken();
            }
            logger.error("开始获取用户信息 openid:{} unionId:{}", openId, unionId);
            //https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s
            String url = String.format(GloabConst.WeiXinUrl.SESSION_KEY_GET_USER_INFO_BY_UNION_ID, userApiToken, unionId);
            logger.debug("获取用户个人信息(UnionID机制) url:{}", url);
            String data = HttpUtils.get(url);
            if (StringUtils.isEmpty(data))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
            }
            dataJson = JSONObject.parseObject(data);
            if (dataJson == null)
            {
                logger.error("获取用户个人信息(UnionID机制) 获取失败! url:{}", url);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
            }
            if (openId.equals(""))
            {
                openId = dataJson.getString("openid");
            }
            User userSave = new User();
            userSave.setUser_name(dataJson.getString("nickname"));
            userSave.setHeadimgurl(dataJson.getString("headimgurl"));
            userSave.setSex(dataJson.getIntValue("sex"));
            userSave.setWx_id(openId);
            userSave.setWx_name(dataJson.getString("nickname"));

            String userId = thirdMappingModelMapper.getUserIdByUnionId(vo.getStoreId(), unionId, ThirdMappingModel.Type.weiXin);
            User   user;
            if (StringUtils.isNotEmpty(userId))
            {
                user = userBaseMapper.selectByUserId(vo.getStoreId(), userId);
                if (user == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "数据错误", "applets");
                }
                //更新此用户信息
                userSave.setId(user.getId());
                userBaseMapper.updateUserInfoById(userSave);
                resultMap.put("access_id", user.getAccess_id());
                resultMap.put("user_name", user.getUser_name());
                resultMap.put("headimgurl", user.getHeadimgurl());
            }
            else
            {
                //注册用户信息 一键注册随机生成账号
                userSave.setZhanghao(BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10));
                publicUserService.register(vo, vo.getPid(), userSave);
                user = userSave;
                resultMap.put("access_id", userSave.getAccess_id());
                resultMap.put("user_name", userSave.getUser_name());
                resultMap.put("headimgurl", userSave.getHeadimgurl());
                logger.debug("user :{}", JSON.toJSONString(user));
                //添加到映射表
                ThirdMappingModel thirdMappingSave = new ThirdMappingModel();
                thirdMappingSave.setStore_id(vo.getStoreId());
                thirdMappingSave.setId(BuilderIDTool.getGuid());
                thirdMappingSave.setOpen_id(openId);
                thirdMappingSave.setUnion_id(unionId);
                thirdMappingSave.setUser_id(user.getUser_id());
                thirdMappingSave.setType(ThirdMappingModel.Type.weiXin);
                thirdMappingModelMapper.insertSelective(thirdMappingSave);
            }
            String token = vo.getAccessId();
            //验证Token是否过期
            try
            {
                JwtUtils.verifyJwt(user.getAccess_id());
            }
            catch (LaiKeCommonException c)
            {
                logger.debug("token已过期!");
            }
            if (StringUtils.isEmpty(token))
            {
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            //登录成功流程
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            publicUserService.loginSuccess(user, token, null);

            resultMap.put("access_id", token);
            resultMap.put("openid", openId);
            resultMap.put("user", user);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信公众号授权登录 登录/注册异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "appletsWx");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getWxPhone(WxAuthPhoneVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String     sessionKey = (String) redisUtil.get("sessionKey_" + vo.getOpenid());
            String     result     = AuthUtil.decrypt1(vo.getEncryptedData(), sessionKey, vo.getIv());
            JSONObject json       = JSONObject.parseObject(result);
            resultMap.put("phoneNumber", json.getString("purePhoneNumber"));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("小程序授权获取用户手机异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWxPhone");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> tokenAccess(MainVo vo, String openid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = null;
            //判断用户是否要更新Token
            boolean flag  = false;
            String  token = vo.getAccessId();
            //查询当前商城是否存在此openId用户
            Example example = new Example(User.class);
            example.createCriteria()
                    .andEqualTo("store_id", vo.getStoreId())
                    .andEqualTo("wx_id", openid);
            List<User> users = userBaseMapper.selectByExample(example);
            if (users.size() == 0)
            {
                if (StringUtils.isEmpty(token))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录");
                }
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user == null)
                {
                    flag = true;

                    user = new User();
                    user.setStore_id(vo.getStoreId());
                    user.setAccess_id(vo.getAccessId());
                    user = userBaseMapper.selectOne(user);

                    if (user == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录");
                    }
                }

//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "tokenAccess");
            }
            else
            {
                user = users.get(0);
            }
            if (Objects.isNull(user.getAccess_id()))
            {
                flag = true;
            }
            else
            {
                //验证Token是否过期
                try
                {
                    JwtUtils.verifyJwt(user.getAccess_id());
                }
                catch (LaiKeCommonException c)
                {
                    logger.debug("token已过期!");
                    flag = true;
                }
            }
            String newToken = "";
            if (flag)
            {
                newToken = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
                user.setAccess_id(newToken);
                userBaseMapper.updateUserInfoById(user);
            }
            else
            {
                newToken = user.getAccess_id();
            }
            publicUserService.loginSuccess(userBaseMapper.selectByPrimaryKey(user.getId()), newToken, user.getReferee());

            Map<String, Object> userMap = new HashMap<>(16);
            userMap.put("user_name", user.getUser_name());
            userMap.put("headimgurl", user.getHeadimgurl());
            userMap.put("user_id", user.getUser_id());
            if (Objects.isNull(user.getMima()))
            {
                userMap.put("nopswd", 123456);
            }
            else
            {
                userMap.put("nopswd", user.getMima());
            }
            resultMap.put("openid", openid);
            resultMap.put("user_status", 1);
            resultMap.put("user", userMap);
            resultMap.put("access_id", newToken);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("小程序用户Token异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "tokenAccess");
        }
        return resultMap;
    }


    /**
     * 更新用户购物车
     *
     * @param userid
     * @param token
     */
    private void updateUserCart(int storeId, String userid, String token)
    {
        try
        {
            // 根据商城ID、token，查询购物车信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            //判断是否为游客
            parmaMap.put("yk_token", token);
            parmaMap.put("is_open", "is_open");
            List<Map<String, Object>> resultCartList = cartModelMapper.getUserShopCartList(parmaMap);
            if (resultCartList != null)
            {
                for (Map<String, Object> map : resultCartList)
                {
                    //商品状态
                    int status = Integer.parseInt(map.get("status").toString());
                    //购物车id
                    int id = MapUtils.getIntValue(map, "id");
                    //商品id
                    int goodsId = MapUtils.getIntValue(map, "Goods_id");
                    //属性id
                    int sizeId = MapUtils.getIntValue(map, "Size_id");
                    //数量
                    int goodsNum = MapUtils.getIntValue(map, "Goods_num");
                    //产品信息
                    List<Map<String, Object>> skuBeanList = new ArrayList<>();
                    String                    price       = map.get("price") + "";
                    BigDecimal                vipPrice    = new BigDecimal(price);
                    //商品id
                    int pid = Integer.parseInt(map.get("pid") + "");
                    //库存数量
                    int stockNum = Integer.parseInt(map.get("num") + "");
                    int mchId    = Integer.parseInt(map.get("mch_id") + "");
                    // 根据商城id、用户id、商品id、属性id，查询用户购物车信息
                }
            }

//            if ($r0)
//            {
//                foreach ($r0 as $k => $v)
//                {
//
//                    $sql1 = "select id,Goods_num from lkt_cart where store_id = '$store_id' and user_id = '$user_id' and Goods_id = '$Goods_id' and Size_id = '$Size_id'";
//                    $r1 = $db->select($sql1);
//                    if ($r1)
//                    { // 存在 表示用户购物车有该商品
//                        $id = $r1[0]->id; // 购物车id
//                        $Goods_num1 = $r1[0]->Goods_num;// 数量
//                        // 根据商品id、属性id,查询属性库存
//                        $sql2 = "select num from lkt_configure where id = '$Size_id' and pid = '$Goods_id'";
//                        $r2 = $db->select($sql2);
//                        if ($r2)
//                        {
//                            $num = $r2[0]->num; // 库存
//                            if ($Goods_num + $Goods_num1 >= $num)
//                            { // 没登录时购物车数量 + 登入后已存在的购物车数量 >= 库存剩余数量
//                                $cart_num = $num;
//                            }
//                            else
//                            {
//                                $cart_num = $Goods_num + $Goods_num1;
//                            }
//                            // 根据商城ID、用户ID、购物车ID，修改购物车数量
//                            $sql4 = "update lkt_cart set Goods_num = '$cart_num' where store_id = '$store_id' and user_id = '$user_id' and id = '$id'";
//                            $r4 = $db->update($sql4);
//                            if($r4 < 0){
//                                $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，修改购物车信息失败!sql:' . $sql4;
//                                $this->Log($Log_content);
//                            }
//                            // 根据商城ID、购物车ID，删除购物车信息
//                            $sql5 = "delete from lkt_cart where store_id = '$store_id' and id = '$cart_id'";
//                            $r5 = $db->delete($sql5);
//                        }
//                    }
//                    else
//                    {
//                        // 根据商城ID、token、购物车ID，修改购物车用户ID
//                        $sql6 = "update lkt_cart set user_id = '$user_id' where store_id = '$store_id' and token = '$token' and Goods_id = '$Goods_id'";
//                        $r6 = $db->update($sql6);
//                        if($r6 < 0){
//                            $Log_content = __METHOD__ . '->' . __LINE__ . '用户登录成功，修改购物车信息失败!sql:' . $sql6;
//                            $this->Log($Log_content);
//                        }
//                    }
//                }
//            }
//
//            // 根据商城ID、token，修改店铺浏览记录表
//
//            $sql3 = "update lkt_mch_browse set user_id = '$user_id' where store_id = '$store_id' and token = '$token'";
//            $r3 = $db->update($sql3);

        }
        catch (LaiKeAPIException e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GXGWCSB, "更新购物车失败！");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> aliUser(AlipayUserVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //数据库是否存在
            User   userDb = userBaseMapper.getAliUser(vo.getStoreId(), vo.getZfb_id());
            String token  = null;
            if (userDb != null)
            {
                logger.info("用户已经存在：{}", JSON.toJSONString(userDb));
            }
            else
            {
                User user = new User();
                user.setZfb_id(vo.getZfb_id());
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
                user.setAccess_id(token);
                if (StringUtils.isNotEmpty(vo.getNickName()))
                {
                    user.setUser_name(vo.getNickName());
                }
                if (StringUtils.isNotEmpty(vo.getHeadImgUrl()))
                {
                    user.setHeadimgurl(vo.getHeadImgUrl());
                }
                user.setStore_id(vo.getStoreId());
                Random r = new Random();
                user.setZhanghao(vo.getZfb_id());
                publicUserService.register(vo, null, user);
                logger.info("用户不存在，保存信息：{}", JSON.toJSONString(user));
                userDb = user;
            }
            token = userDb.getAccess_id();
            //验证Token是否过期
            try
            {
                JwtUtils.verifyJwt(userDb.getAccess_id());
            }
            catch (LaiKeCommonException c)
            {
                logger.debug("token已过期!");
            }
            if (com.alibaba.druid.util.StringUtils.isEmpty(token))
            {
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            publicUserService.loginSuccess(userDb, token, null);

            resultMap.put("accessId", token);
            resultMap.put("storeId", vo.getStoreId());
            resultMap.put("userInfo", userDb);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("支付宝授权失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aliUser");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> aliUserLogin(MainVo vo, String alimp_auth_code, boolean flag) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String configData;
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_APP)
            {
                configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ALIPAY_MOBILE_PHP);
            }
            else
            {
                configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), flag ? DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY : DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP);
            }
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "支付宝小程序配置信息不存在", "aliUserLogin");
            }
            Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
            {
            });
            //参数准备
            String appId              = MapUtils.getString(map, "appid");//appId
            String signType           = MapUtils.getString(map, "signType");//签名方式
            String rsaPrivateKey      = MapUtils.getString(map, "rsaPrivateKey");//支付宝私钥
            String alipayrsaPublicKey = MapUtils.getString(map, "alipayrsaPublicKey");//支付宝公钥
            String aLiPayUserInfo     = AliPayUtil.getALiPayUserInfo(alimp_auth_code, appId, signType, rsaPrivateKey, alipayrsaPublicKey);
            logger.debug("aLiPayUserInfo 阿里用户信息:{}", aLiPayUserInfo);
            JSONObject jsonObject = JSONObject.parseObject(aLiPayUserInfo);
            String     userId     = jsonObject.getString("user_id");
            String     nickName   = jsonObject.getString("nick_name");
            String     avatar     = jsonObject.getString("avatar");
            User       aliUser    = userBaseMapper.getAliUser(vo.getStoreId(), userId);
            if (aliUser == null)
            {
                //注册用户信息
                User userSave = new User();
                userSave.setStore_id(vo.getStoreId());
                userSave.setUser_name(nickName);
                userSave.setHeadimgurl(avatar);
                userSave.setSex("M".equals(jsonObject.getString("gender")) ? 1 : 2);
                userSave.setZfb_id(userId);
                //一键注册随机生成账号
                userSave.setZhanghao(BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10));
                publicUserService.register(vo, null, userSave);
                aliUser = userSave;
            }

            String token = "";
            //验证Token是否过期
            try
            {
                JwtUtils.verifyJwt(aliUser.getAccess_id());
            }
            catch (LaiKeCommonException c)
            {
                logger.debug("token已过期!");
            }
            if (StringUtils.isEmpty(token))
            {
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            try
            {
                RedisDataTool.isLogin(vo.getAccessId(), redisUtil);
            }
            catch (LaiKeAPIException ll)
            {
                //阿里小程序自动登录
                publicUserService.loginSuccess(aliUser, token, null);
            }

            resultMap.put("code", 200);
            resultMap.put("userInfo", aliUser);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里用户登陆 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveAuthorizeUserInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> aliUserLoginByApp(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY);
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "支付宝小程序配置信息不存在", "aliUserLogin");
            }
            Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
            {
            });
            //参数准备
            String appId = MapUtils.getString(map, "appid");//appId
            //"RSA2"
            String signType           = MapUtils.getString(map, "signType");//签名方式
            String rsaPrivateKey      = MapUtils.getString(map, "rsaPrivateKey");//支付宝私钥
            String alipayrsaPublicKey = MapUtils.getString(map, "alipayrsaPublicKey");//支付宝公钥
            String url                = AliPayUtil.getALiPayUserInfoByApp(appId, signType, rsaPrivateKey, alipayrsaPublicKey);

            resultMap.put("url", url);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里用户登陆-app 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aliUserLoginByApp");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> aliUserLoginByWeb(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String configData;
            logger.debug("getStoreType : {}", vo.getStoreType());
            configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP);
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "支付宝配置信息不存在", "aliUserLogin");
            }
            Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
            {
            });
            //获取h5配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "请配置网站域名");
            }
            //参数准备
            String appId = MapUtils.getString(map, "appid");//appId
            //java appid无法获取用户名,只能使用php的去获取了
            if (GloabConst.StoreType.STORE_TYPE_APP == vo.getStoreType())
            {
                configModel.setH5_domain(configModel.getApp_domain_name());
            }
            resultMap.put("url", AliPayUtil.getAliPayUserInfoByWeb(appId, vo.getStoreType(), configModel.getH5_domain()));
            logger.debug("阿里授权跳转rul : {}", JSON.toJSONString(resultMap));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("阿里用户登陆-web 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aliUserLoginByWeb");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getWxAppId(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String configData = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT);
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "支付宝小程序配置信息不存在", "aliUserLogin");
            }
            Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
            {
            });
            //参数准备
            String appId = MapUtils.getString(map, "appid");
            resultMap.put("appId", appId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取微信appid 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWxAppId");
        }
        return resultMap;
    }


    @Override
    public void bindWechat(AppletsVo vo) throws LaiKeAPIException
    {
        try
        {
            //获取会员信息
            User   user                     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String appId, appSecret, openId = "", unionId, userApiToken, wx_name = "";
            String configData               = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT);
            if (StringUtils.isEmpty(configData))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQZFPZXX, "微信公众号配置信息不存在", "aliUserLogin");
            }
            JSONObject dataJson = new JSONObject();
            if (StringUtils.isEmpty(vo.getUserApiToken()))
            {
                Map<String, Object> map = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
                {
                });
                appId = MapUtils.getString(map, "appid");
                appSecret = MapUtils.getString(map, "appsecret");
                String url = String.format(GloabConst.WeiXinUrl.SESSION_KEY_GET_ACCESS_TOKEN_BY_CODE, appId, appSecret, vo.getCode(), "authorization_code");
                logger.debug("通过 code 获取access_token url:{}", url);
                String data = HttpUtils.get(url);
                if (StringUtils.isEmpty(data))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
                }
                dataJson = JSONObject.parseObject(data);
                openId = dataJson.getString("openid");
                if (StringUtils.isEmpty(openId))
                {
                    logger.error("授权失败,没有获取到openid");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
                }
                unionId = dataJson.getString("unionid");
                userApiToken = dataJson.getString("access_token");
            }
            else
            {
                unionId = vo.getUnionId();
                userApiToken = vo.getUserApiToken();
            }
            logger.error("开始获取用户信息 openid:{} unionId:{}", openId, unionId);
            //https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s
            String url = String.format(GloabConst.WeiXinUrl.SESSION_KEY_GET_USER_INFO_BY_UNION_ID, userApiToken, unionId);
            logger.debug("获取用户个人信息(UnionID机制) url:{}", url);
            String data = HttpUtils.get(url);
            if (StringUtils.isEmpty(data))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
            }
            dataJson = JSONObject.parseObject(data);
            if (dataJson == null)
            {
                logger.error("获取用户个人信息(UnionID机制) 获取失败! url:{}", url);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQSB, "授权失败", "applets");
            }
            if (openId.equals(""))
            {
                openId = dataJson.getString("openid");
            }
            wx_name = dataJson.getString("nickname");
            User odlUser = new User();
            odlUser.setStore_id(vo.getStoreId());
            odlUser.setWx_id(openId);
            if (userBaseMapper.selectCount(odlUser) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GWXYBDQTZH, "该微信已绑定其他账号");
            }
            //绑定
            user.setWx_name(wx_name);
            user.setWx_id(openId);
            if (userBaseMapper.updateByPrimaryKeySelective(user) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("微信公众号授权登录 登录/注册异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "appletsWx");
        }
    }

    @Override
    public void validatePcodeAndUpdatePassword(MainVo vo, String tel, String keyCode, Integer type, String email, String password, String cpc, Integer countryNum, int currencyCode)
    {
        //0.手机号 1.邮箱
        try
        {
            User user = new User();
            user.setStore_id(vo.getStoreId());
            if (type == 0)
            {
                this.validatePcode(vo, tel, keyCode, currencyCode);
                user.setMobile(tel);
                user.setCpc(cpc);
                user.setCountry_num(countryNum);
            }
            else if (type == 1)
            {
                user.setE_mail(email);
                String key  = GloabConst.RedisHeaderKey.MAIL_CODE_KEY + email + keyCode;
                Object code = redisUtil.get(key);
                if (Objects.isNull(code) || !keyCode.equals(code.toString()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确");
                }
            }
            user = userBaseMapper.selectOne(user);
            if (Objects.isNull(user))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "resetPassword");
            }
            if (password.equals(CryptoUtil.strDecode(user.getMima())))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMHJMMXT, "新密码和旧密码相同");
            }

            user = new User();
            user.setMima(password);
            user.setMobile(tel);
            user.setCpc(cpc);
            DataCheckTool.checkUserDataFormate(user);

            boolean reset = resetPassword(vo, tel, password, email, countryNum, cpc);
            if (!reset)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMXGSB, "密码修改失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("重置密码异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validatePcodeAndUpdatePassword");
        }
    }

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private AgreementModelMapper agreementModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;


    @Autowired
    private CartModelMapper cartModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;
}

