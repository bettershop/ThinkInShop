package com.laiketui.apps.user.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.apps.api.user.AppsUserBaseService;
import com.laiketui.common.api.PublicCouponService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.BankTool;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeCommonException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.ProductConfigModel;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.BankCardModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.UserCollectionModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.presell.PreSellRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.UserLoginVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.domain.vo.user.SycnUserVo;
import com.laiketui.domain.vo.user.UserRegisterVo;
import com.laiketui.root.license.CryptoUtil;
import com.laiketui.root.license.Md5Util;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.*;


/**
 * 用户接口实现
 *
 * @author Trick
 * @date 2020/9/23 9:22
 */
@Service
public class AppsUserBaseServiceImpl implements AppsUserBaseService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

    @Autowired
    ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    PubliceService publicService;

    @Autowired
    ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Override
    public User getUser(User user) throws LaiKeAPIException
    {
        return userBaseMapper.selectOne(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> insertUser(UserRegisterVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            boolean isexist;
            if (vo.getType() == 0 && StringUtils.isEmpty(vo.getPhone()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机号不能为空");
            }
            else if (vo.getType() == 1 && StringUtils.isEmpty(vo.getE_mail()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXBNWK,"邮箱不能为空");
            }
            User user = new User();
            if (Objects.nonNull(vo.getPhone()))
            {
                user.setMobile(vo.getPhone());
                user.setCpc(vo.getCpc());
                user.setCountry_num(vo.getCountry_num());
            }
            user.setMima(vo.getMima());
            //随机生成账号
            user.setZhanghao(publiceService.generateAccount(8));
            //校验数据格式
            user = DataCheckTool.checkUserDataFormate(user);
            //验证短信
            Object value = null;
            if (vo.getType() == 0)
            {
                value = redisUtil.get(GloabConst.RedisHeaderKey.REGISTER_HEADER + user.getMobile());
            }else if (vo.getType() == 1)
            {
                //邮箱注册
                value = redisUtil.get(GloabConst.RedisHeaderKey.MAIL_CODE_KEY + vo.getE_mail() + vo.getPcode());
                user.setE_mail(vo.getE_mail());
            }

            isexist = value == null;
            if (isexist || !value.toString().equals(vo.getPcode()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确");
            }

            //检查账户/手机号是否已存在
            isexist = this.isRegister(vo.getStoreId(), user.getZhanghao());
            boolean register = false;
            int count = 0;
            if (Objects.nonNull(vo.getPhone()))
            {
                register = this.isRegister(vo.getStoreId(), user.getMobile());
                if (register)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.REGISTER_PHONE_FIAL,"手机号已被注册");
                }
            }else if (Objects.nonNull(vo.getE_mail()))
            {
                count = userBaseMapper.countByEmail(vo.getStoreId(),vo.getE_mail());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERR0R_CODE_YXYBZC,"邮箱已被注册");
                }
            }
            if (!isexist)
            {
                user.setMima(CryptoUtil.strEncode(vo.getMima()));
                //注册
                publicUserService.register(vo, vo.getPid(), user);
                //自动登录
                publicUserService.loginSuccess(user, user.getAccess_id(), vo.getPid());

                resultMap.put("access_id", user.getAccess_id());
                resultMap.put("user_name", user.getMobile());
                resultMap.put("headimgurl", user.getHeadimgurl());
                resultMap.put("y_password", 1);
                resultMap.put("user_id", user.getUser_id());
            }
            else
            {
                logger.debug("{}或者{} 该账号已被注册!", user.getZhanghao(), user.getMobile());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZHYBZC, "该账号已被注册", "insertUser");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("用户注册 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "insertUser");
        }
        return resultMap;
    }

    @Override
    public boolean isRegister(int storeId, String zhanghao) throws LaiKeAPIException
    {
        try
        {
            int flag = userBaseMapper.validataUserPhoneOrNoIsRegister(storeId, zhanghao);
            return flag > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("验证账号是否注册 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isRegister");
        }
    }


    @Override
    public boolean sendSms(String mobile, int smsType, int storeId,String cpc) throws LaiKeAPIException
    {
        Map<String,String> paramMap = new HashMap<>();
        /*if (StringUtils.isNotEmpty(cpc))
        {
            paramMap.put("cpc",cpc);
        }*/
        return publiceService.sendSms(storeId, mobile, GloabConst.VcodeCategory.TYPE_VERIFICATION, smsType, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> login(UserLoginVo user) throws LaiKeAPIException
    {
        Map<String, Object> resultMap    = new HashMap<>(16);
        User                userTemp     = new User();
        Integer             sellOrderNum = 0;
        try
        {
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
            String token              = user.getAccessId();
            String fatherUid          = user.getPid();
            String lang = GloabConst.Lang.CN;
            userTemp.setStore_id(user.getStoreId());
            if (GloabConst.Lang.EN.equals(user.getLanguage()))
            {
                lang = GloabConst.Lang.EN;
            }
            int wxStatus = 0;
            logger.info("商城id：：：：{}",user.getStoreId());
            if (!StringUtils.isEmpty(user.getPhone()) && !StringUtils.isEmpty(user.getPassword()))
            {
                //账号或手机号
                userTemp.setZhanghao(user.getPhone());
                //登录类型 1：手机号 2：账号/邮箱
                userTemp.setType(user.getType());
                if (Objects.nonNull(user.getCpc()))
                {
                    userTemp.setCpc(user.getCpc());
                }
                //获取用户信息
                logger.info("用户登录信息：：：：{}",JSON.toJSONString(userTemp));
                User userinfo = userBaseMapper.getUserByAccount(userTemp);
                if (userinfo != null)
                {
                    logger.debug("该账号名称为{}", userinfo.getUser_name());
                    if (!StringUtils.isEmpty(userinfo.getMima()))
                    {
                        String decoderPwd = CryptoUtil.strDecode(userinfo.getMima());
                        if (decoderPwd != null && decoderPwd.equals(user.getPassword()))
                        {
                            //是否为小程序
                            if (DictionaryConst.StoreSource.LKT_LY_001.equals(user.getStoreType() + ""))
                            {
                                if (StringUtils.isEmpty(userinfo.getWx_id()))
                                {
                                    wxStatus = 1;
                                }
                            }
                            //登录成功流程
                            userinfo.setLang(lang);
                            publicUserService.loginSuccess(userinfo, token, fatherUid);
                            //预售待支付订单提醒
                            String        startOfDay = DateUtil.getStartOfDay(new Date());
                            List<Integer> integers   = preSellRecordModelMapper.countPending(userinfo.getStore_id(), userinfo.getUser_id());
                            if (integers != null && integers.size() > 0)
                            {
                                for (int i = 0; i < integers.size(); i++)
                                {
                                    Integer            integer            = integers.get(i);
                                    PreSellRecordModel preSellRecordModel = preSellRecordModelMapper.selectByPrimaryKey(integer);
                                    PreSellGoodsModel  preSellGoodsModel  = new PreSellGoodsModel();
                                    preSellGoodsModel.setProduct_id(preSellRecordModel.getProduct_id());
                                    preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                                    //定金模式商品只有到了待付尾款时间才算订单提醒
                                    if (!Objects.isNull(preSellGoodsModel) && preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.DEPOSIT_PATTERN))
                                    {
                                        if (!DateUtil.dateFormate(preSellGoodsModel.getBalance_pay_time(), GloabConst.TimePattern.YMDHMS).equals(startOfDay))
                                        {
                                            integers.remove(i);
                                            i--;
                                        }
                                    }
                                }
                                sellOrderNum = integers.size();
                            }
                            Map<String, Object> parmaMap = new HashMap<>(16);
                            parmaMap.put("store_id", 0);
                            parmaMap.put("startDate_lt", new Date());
                            parmaMap.put("endDate_gt", new Date());
                            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
                            parmaMap.put("user_tell", SystemTellModel.TELL.YES);
                            parmaMap.put("user_store_id", user.getStoreId());
                            parmaMap.put("store_type", user.getStoreType());
                            parmaMap.put("read_id", userinfo.getUser_id());
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
                            //登陆成功
                            resultMap.put("token", token);
                            //系统公告
                            resultMap.put("systemMsgTitle", systemMsgTitle);
                            resultMap.put("systemMsg", systemMsg);
                            resultMap.put("systemMsgType", systemMsgType);
                            resultMap.put("tell_id", tell_id);
                            resultMap.put("systemMsgEndDate", systemMsgEndDate);
                            resultMap.put("systemMsgStartDate", systemMsgStartDate);
                            resultMap.put("info", info);
                            //登陆成功
                            resultMap.put("access_id", userinfo.getAccess_id());
                            String userName = userinfo.getUser_name();
                            resultMap.put("user_name", userName);

                            getCurrencyInfos(userinfo, resultMap);

                            resultMap.put("user_id", userinfo.getUser_id());
                            resultMap.put("headimgurl", userinfo.getHeadimgurl());
                            resultMap.put("y_password", 1);
                            resultMap.put("wx_status", wxStatus);
                            resultMap.put("sellOrderNum", sellOrderNum);
                            resultMap.put("lang", userinfo.getLang());
                            resultMap.put("cpc", userinfo.getCpc());
                            resultMap.put("country_num", userinfo.getCountry_num());
                            return resultMap;
                        }
                        else
                        {
                            if (user.getType() == 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHHMMBPP,"手机或密码不匹配","login");
                            }
                            else if (user.getType() == 2)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXHMMBPP, "邮箱或密码不匹配", "login");
                            }
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHHMMBPP, "账号或密码不匹配", "login");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NHWSZMM, "您还未设置密码", "login");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLSBYHBCZ, "登录失败，用户不存在", "login");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("登录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "quit");
        }
        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLSBCSCW, "登陆失败,参数错误", "login");
    }

    private void getCurrencyInfos(User userinfo, Map<String, Object> resultMap)
    {
        Integer preferred_currency = userinfo.getPreferred_currency();
        //默认币种和汇率 默认币种一定会有 不能删除 除非是在数据库层面删除
        Map defaultCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(userinfo.getStore_id());
        resultMap.put("storeCurrency", defaultCurrencyMap);
        if (preferred_currency != null)
        {
            resultMap.put("preferred_currency", userinfo.getPreferred_currency());
            resultMap.put("userCurrency", currencyStoreModelMapper.getCurrencyInfo(userinfo.getStore_id(), userinfo.getPreferred_currency()));
        }
        else
        {
            //注册时候没有默认币种则使用商城默认币种
            resultMap.put("preferred_currency", MapUtils.getIntValue(defaultCurrencyMap, "currency_id"));
            resultMap.put("userCurrency", defaultCurrencyMap);
        }
    }

    @Override
    public Map<String, Object> loginSms(MainVo vo, String phone, String pcode, String pid, String clientid,String cpc,Integer country_num,Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String loginKey = GloabConst.RedisHeaderKey.LOGIN_HEADER + phone;
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
            //验证
            User userResult = new User();
            userResult.setStore_id(vo.getStoreId());
            switch (type)
            {
                case 1:
                    //检测手机号格式
                   /* if (!MobileUtils.isMobile(phone))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
                    }*/
                    int count = userBaseMapper.checkPhoneIsRegister(vo.getStoreId(), phone);
                    if (count > 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.REGISTER_PHONE_FIAL,"手机号已被注册");
                    }
                    userResult.setMobile(phone);
                    userResult.setCpc(cpc);
                    userResult.setCountry_num(country_num);
                    break;
                case 2:
                    loginKey = GloabConst.RedisHeaderKey.MAIL_CODE_KEY + phone + pcode;
                    userResult.setE_mail(phone);
                    break;
            }
            if (redisUtil.hasKey(loginKey))
            {
                String rpcode = redisUtil.get(loginKey).toString();
                logger.info("缓存里的验证码：{}",rpcode);
                if (rpcode.equals(pcode))
                {
                    userResult = userBaseMapper.selectOne(userResult);
                    if (userResult == null)
                    {
                        userResult = new User();
                        if (type == 1)
                        {
                            userResult.setMobile(phone);
                            userResult.setCpc(cpc);
                            userResult.setCountry_num(country_num);
                        }
                        else if (type == 2)
                        {
                            userResult.setE_mail(phone);
                        }
                        userResult.setMima("");
                        userResult.setZhanghao(publiceService.generateAccount(8));
                        //校验数据格式
                        userResult = DataCheckTool.checkUserDataFormate(userResult);
                        //注册
                        publicUserService.register(vo, pid, userResult);
                    }
                    else
                    {
                        userResult.setReferee(pid);
                        userResult.setAccess_id(vo.getAccessId());
                    }
                    if (DictionaryConst.StoreSource.LKT_LY_001.equals(vo.getStoreType() + ""))
                    {
                        int wxStatus = 0;
                        if (!StringUtils.isEmpty(userResult.getWx_id()))
                        {
                            wxStatus = 1;
                        }
                        resultMap.put("wx_status", wxStatus);
                    }
                    //登录成功流程
                    publicUserService.loginSuccess(userResult, userResult.getAccess_id(), pid);
                    //移除验证码
                    redisUtil.del(loginKey);

                    int isPwd = 0;
                    if (!StringUtils.isEmpty(userResult.getMima()))
                    {
                        isPwd = 1;
                    }
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", 0);
                    parmaMap.put("startDate_lt", new Date());
                    parmaMap.put("endDate_gt", new Date());
                    parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
                    parmaMap.put("user_tell", SystemTellModel.TELL.YES);
                    parmaMap.put("user_store_id", vo.getStoreId());
                    parmaMap.put("store_type", vo.getStoreType());
                    parmaMap.put("read_id", userResult.getUser_id());
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
                    resultMap.put("y_password", isPwd);
                    //系统公告
                    resultMap.put("systemMsgTitle", systemMsgTitle);
                    resultMap.put("systemMsg", systemMsg);
                    resultMap.put("systemMsgType", systemMsgType);
                    resultMap.put("tell_id", tell_id);
                    resultMap.put("systemMsgEndDate", systemMsgEndDate);
                    resultMap.put("systemMsgStartDate", systemMsgStartDate);
                    resultMap.put("info", info);
                    //登陆成功
                    resultMap.put("access_id", userResult.getAccess_id());
                    String userName = userResult.getUser_name();
                    resultMap.put("user_name", userName);
                    String langcode = userResult.getLang();
                    resultMap.put("user_lang_code", StringUtils.isNotEmpty(langcode) ? langcode : GloabConst.Lang.CN);
                    getCurrencyInfos(userResult, resultMap);

                    resultMap.put("user_id", userResult.getUser_id());
                    resultMap.put("headimgurl", userResult.getHeadimgurl());
//                    resultMap.put("wx_status", wxStatus);
//                    resultMap.put("sellOrderNum", sellOrderNum);
                    resultMap.put("lang", userResult.getLang());
                    resultMap.put("cpc", userResult.getCpc());
                    resultMap.put("country_num", userResult.getCountry_num());

                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确", "loginSms");
                }
            }
            else
            {
                throw new LaiKeAPIException(StringUtils.isNotEmpty(pcode) ? ErrorCode.BizErrorCode.ERROR_CODE_YZMCWQCXSR :ErrorCode.BizErrorCode.ERROR_CODE_QZXHQYZM,"验证码错误","loginSms");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("短信登录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginSms");
        }
        return resultMap;
    }

    @Override
    public boolean updateUser(MainVo vo, Integer sex, String birthday, String nickname, MultipartFile file,String e_mail,String keyCode) throws LaiKeAPIException
    {
        try
        {
            User updateUser;
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            User user = new User();
            if (!StringUtils.isEmpty(birthday))
            {
                user.setBirthday(DateUtil.dateFormateToDate(birthday, GloabConst.TimePattern.YMD));
                user.setIs_default_birthday(2);
            }
            user.setUser_name(nickname);
            if (sex != null)
            {
                user.setSex(sex);
            }

            //校验数据格式
            updateUser = DataCheckTool.checkUserDataFormate(user);

            User userTemp = new User();
            userTemp.setId(userCache.getId());
            userCache = userBaseMapper.selectOne(userTemp);

            updateUser.setId(userCache.getId());
/*            //生日只允许修改一次
            if (updateUser.getBirthday() != null && userCache.getBirthday() != null) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSRQZNXGYC, "出生日期只能修改一次", "updateUser");
            }*/
            updateUser.setIs_default_value(2);
            //头像上传 头像上传用oss
            if (file != null && !file.isEmpty())
            {
                MultipartFile[] files     = new MultipartFile[]{file};
                List<String>    reusltUrl = uploadImage(vo.getStoreId(), vo.getStoreType(), GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, files);
                if (reusltUrl != null && !reusltUrl.isEmpty())
                {
                    updateUser.setHeadimgurl(ImgUploadUtils.getUrlPure(reusltUrl.get(0), true));
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试", "updateUser");
                }
            }

            if (StringUtils.isNotEmpty(e_mail))
            {
                int emailCount = userBaseMapper.getEmailCount(e_mail,vo.getStoreId(), userCache.getUser_id());

                Object value = redisUtil.get(GloabConst.RedisHeaderKey.MAIL_CODE_KEY + e_mail + keyCode);
                if (Objects.isNull(value) || !value.equals(keyCode))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确");
                }
                if (emailCount > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERR0R_CODE_YXYBZC,"邮箱已被注册");
                }
                if (StringUtils.isNotEmpty(userCache.getE_mail()) && Objects.equals(userCache.getE_mail(),e_mail))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYXYDQBDYXXT,"该邮箱与当前绑定邮箱相同");
                }
                updateUser.setE_mail(e_mail);
            }

            //修改用户资料
            int count;
            try
            {
                count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                if (count > 0)
                {
                    //更新缓存
                    User user1 = userBaseMapper.selectByPrimaryKey(userCache.getId());
                    RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user1, redisUtil);
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试", "updateUser");
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试", "updateUser");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改用户资料 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateUser");
        }
    }

    @Override
    public List<String> uploadImage(int storeId, int storeType, String uploadType, MultipartFile[] files) throws LaiKeAPIException
    {
        try
        {
            if (files == null || files.length < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZXYSCDTP, "请选择需要上传的图片", "updateUser");
            }
            //获取商城配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                uploadType = configModel.getUpserver();
            }
            //图片上传
            return publiceService.uploadImage(DataUtils.convertToList(files), uploadType, storeType, storeId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("密码修改异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "uploadImage");
        }
    }

    @Override
    public boolean updatePassword(MainVo vo, String password, String newPwd, String confirm) throws LaiKeAPIException
    {
        try
        {
            User user;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user != null)
                {
                    String mima = CryptoUtil.strDecode(user.getMima());
                    if (!password.equals(mima))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMMBZQ, "原密码不正确");
                    }
                    if (newPwd.equals(password))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMHJMMXT, "新密码和旧密码相同");
                    }
                    if (!confirm.equals(newPwd))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QRMMYXMMXYZ, "确认密码与新密码需一致");
                    }
                    User userParma = new User();
                    userParma.setId(user.getId());
                    userParma.setMima(CryptoUtil.strEncode(newPwd));
                    int count = userBaseMapper.updateByPrimaryKeySelective(userParma);
                    if (count < 1)
                    {
                        logger.debug("用户id=" + user.getId() + " 密码修改失败");
                    }
                    else
                    {
                        //重置登陆缓存
                        user = userBaseMapper.selectByPrimaryKey(user.getId());
                        RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
                        return true;
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "updatepassword");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("密码修改异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updatepassword");
        }
        return false;
    }

    @Override
    public void setPaymentPassword(MainVo vo, String password, String keyCode) throws LaiKeAPIException
    {
        try
        {
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                user = userBaseMapper.selectByPrimaryKey(user.getId());
                if (StringUtils.isNotEmpty(keyCode))
                {
                    publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.UPDATE_PWD_PAY_CODE, user.getMobile(), keyCode);
                }
                String currentPwd = user.getPassword();
                if (StringUtils.isNotEmpty(currentPwd))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMYSZ, "密码已设置");
                }
                String updatePwd = password;
                User   userParma = new User();
                userParma.setId(user.getId());
                userParma.setPassword(updatePwd);
                //校验密码格式
                DataCheckTool.checkUserDataFormate(userParma);
                updatePwd = Md5Util.MD5endoce(updatePwd);
                userParma.setPassword(updatePwd);
                int count = userBaseMapper.updateUserInfoById(userParma);
                if (count < 1)
                {
                    logger.debug("用户id=" + user.getId() + " 支付密码设置失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMSZSB, "密码设置失败");
                }
                //重置登陆缓存
                RedisDataTool.refreshRedisUserCache(user, redisUtil);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "updatePaymentPassword");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("密码修改异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updatePaymentPassword");
        }
    }

    @Override
    public Map<String, Object> set(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //是否设置支付密码
        int passwordStatus = 0;
        //登陆密码
        int mimaStatus = 0;
        try
        {
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                if (userObj != null)
                {
                    JSONObject jsonObject = JSONObject.parseObject(userObj.toString());
                    Integer id  = jsonObject.getInteger("id");
                    user =  userBaseMapper.selectByPrimaryKey(id);
                    if (!StringUtils.isEmpty(user.getPassword()))
                    {
                        passwordStatus = 1;
                    }
                    if (!StringUtils.isEmpty(user.getMima()))
                    {
                        mimaStatus = 1;
                    }
                }
                resultMap.put("password_status", passwordStatus);
                resultMap.put("mima_status", mimaStatus);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取设置异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "set");
        }
        return resultMap;
    }

    @Override
    public boolean paymentPassword(MainVo vo, String password) throws LaiKeAPIException
    {
        try
        {
            User cashUser = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicUserService.validatePayPwd(cashUser.getUser_id(), password);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证支付密码异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "paymentPassword");
        }
    }

    @Override
    public Map<String, Object> getUserWallet(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //可提现状态
            int status = 0;
            //钱包单位
            String unit = "元";
            User   user = new User();
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
                if (user != null)
                {
                    //多端登录，重置用户信息
                    user = userBaseMapper.selectByUserId(vo.getStoreId(), user.getUser_id());
                    //获取钱包明细第一页数据
                    resultMap.putAll(getUserWalletDetail(vo, null));
                    //获取可提现数量
                    WithdrawModel withdrawModel = new WithdrawModel();
                    withdrawModel.setStore_id(vo.getStoreId());
                    withdrawModel.setUser_id(user.getUser_id());
                    withdrawModel.setStatus("0");
                    withdrawModel.setIs_mch(0);
                    int withdrawalNum = withdrawModelMapper.selectCount(withdrawModel);
                    if (withdrawalNum > 0)
                    {
                        status = 1;
                    }
                    //查询钱包单位
                    FinanceConfigModel financeConfigModel = new FinanceConfigModel();
                    financeConfigModel.setStore_id(vo.getStoreId());
                    financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
                    if (financeConfigModel != null)
                    {
                        unit = financeConfigModel.getUnit();
                    }
                    else
                    {
                        status = 2;
                    }
                    resultMap.put("unit", unit);
                    //保留两位小数
                    resultMap.put("user_money", user.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    resultMap.put("status", status);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的钱包信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserWallet");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getUserWalletDetail(MainVo vo, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("userid", user.getUser_id());
            //不是店铺
            parmaMap.put("isMch", 0);
            List<Integer> list = new ArrayList<>();
            if (type != null && type != 0)
            {
                if (type == 1)
                {
                    list.addAll(RecordModel.TYPE_RECHARGE);
                    list.add(RecordModel.RecordType.REFUND_MCH_BOND);
                    list.add(RecordModel.RecordType.REFUND);
                    list.add(RecordModel.RecordType.WITHDRAWAL_FAILED);
                    list.add(RecordModel.RecordType.LIVING_COMMISSION);
                    parmaMap.put("typeList", list);
                }
                else
                {
                    list.add(RecordModel.RecordType.APPLICATION_FOR_WITHDRAWAL);
                    list.add(RecordModel.RecordType.WITHDRAWAL_SUCCEED);
                    list.add(RecordModel.RecordType.PAY_MCH_DEPOSIT);
                    list.add(RecordModel.RecordType.BALANCE_CONSUMPTION);
                    list.add(RecordModel.RecordType.SYSTEM_DEDUCTION);
                    parmaMap.put("typeList", list);
                }
            }
            else
            {
                list.addAll(RecordModel.TYPE_RECHARGE);
                list.addAll(RecordModel.TYPE_CASH_WITHDRAWAL);
                list.add(RecordModel.RecordType.REFUND_MCH_BOND);
                list.add(RecordModel.RecordType.BALANCE_CONSUMPTION);
                list.add(RecordModel.RecordType.REFUND);
                list.add(RecordModel.RecordType.SYSTEM_DEDUCTION);
                list.add(RecordModel.RecordType.PAY_MCH_DEPOSIT);
                list.add(RecordModel.RecordType.LIVING_COMMISSION);
                parmaMap.put("typeList", list);
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());


            List<Map<String, Object>> myWalletInfoList = recordModelMapper.getUserWalletRecordInfo(parmaMap);
            for (Map<String, Object> map : myWalletInfoList)
            {
                Date   addDate = (Date) map.get("add_date");
                String time    = DateUtil.dateFormate(addDate, GloabConst.TimePattern.YMDHMS);
                map.put("add_date", time);
                map.put("money", new BigDecimal(map.get("money").toString()).abs().toString());
            }
            resultMap.put("list", myWalletInfoList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的钱包加载更多异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserWalletDetail");
        }
        return resultMap;
    }


    @Override
    public boolean setPassword(MainVo vo, String password, String phone, String keyCode) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //校验验证码
            boolean validateSms = true;
            if (StringUtils.isNotEmpty(keyCode))
            {
               validateSms = publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.UPDATE_PASSWORDE, phone, keyCode);
            }
            if (validateSms)
            {
                String updatePwd = password;
                User   userParma = new User();
                userParma.setId(user.getId());
                userParma.setMima(updatePwd);
                DataCheckTool.checkUserDataFormate(userParma);
                updatePwd = CryptoUtil.strEncode(updatePwd);
                userParma.setMima(updatePwd);
                int count = userBaseMapper.updateUserInfoById(userParma);
                if (count < 1)
                {
                    logger.debug("用户id=" + user.getId() + " 密码修改失败");
                }
                else
                {
                    //清除短信
                    if (StringUtils.isNotEmpty(phone))
                    {
                        redisUtil.del(GloabConst.RedisHeaderKey.UPDATE_PASSWORDE + phone);
                    }
                    //重置缓存
                    user = userBaseMapper.selectByPrimaryKey(user.getId());
                    RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
                    return true;
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
            logger.error("设置登陆密码异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setPassword");
        }
        return false;
    }

    @Override
    public boolean updatePayPassword(MainVo vo, String password, String phone, String keyCode) throws LaiKeAPIException
    {
        try
        {
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                if (userObj != null)
                {
                    //校验验证码
                    boolean validateSms =  true;
                    if (StringUtils.isNotEmpty(keyCode))
                    {
                        validateSms =  publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.UPDATE_PWD_PAY_CODE, phone, keyCode);
                    }
                    if (validateSms)
                    {
                        user = JSON.parseObject(userObj.toString(), User.class);
                        String currentPwd = user.getPassword();
                        String updatePwd  = password;
                        User   userParma  = new User();
                        userParma.setId(user.getId());
                        userParma.setPassword(updatePwd);
                        DataCheckTool.checkUserDataFormate(userParma);
                        updatePwd = Md5Util.MD5endoce(updatePwd);
                        if (currentPwd.equals(updatePwd))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMWBH, "密码未变化", "updatePayPassword");
                        }
                        userParma.setPassword(updatePwd);
                        int count = userBaseMapper.updateUserInfoById(userParma);
                        if (count < 1)
                        {
                            logger.debug("用户id=" + user.getId() + " 支付密码修改失败");
                        }
                        else
                        {
                            //清除短信缓存
                            if (redisUtil.hasKey(GloabConst.RedisHeaderKey.UPDATE_PWD_PAY_CODE + phone))
                            {
                                redisUtil.del(GloabConst.RedisHeaderKey.UPDATE_PWD_PAY_CODE + phone);
                            }
                            //获取登录生命周期
                            Integer lifeTile = publicUserService.getUserLoginLife(vo.getStoreId());
                            //重置登陆缓存
                            user.setPassword(updatePwd);
                            redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + user.getAccess_id(), JSON.toJSONString(user), lifeTile);
                            return true;
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "updatePayPassword");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改支付密码 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updatePayPassword");
        }
        return false;
    }

    @Override
    public boolean updatePhone(MainVo vo, String phoneNew, String keyCode,String cpc,Integer country_num) throws LaiKeAPIException
    {
        try
        {
            User user;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user != null)
                {
                    if (StringUtils.isEmpty(phoneNew))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机号不能为空");
                    }
                    if (phoneNew.equals(user.getMobile()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XSJHBNHJSJXT, "新手机号不能和旧手机相同");
                    }
                    //校验验证码
                    publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.UPDATE_PHOE_CODE, phoneNew, keyCode);
                    User userParma = new User();
                    userParma.setId(user.getId());
                    userParma.setMobile(phoneNew);
                    if (StringUtils.isNotEmpty(cpc))
                    {
                        userParma.setCpc(cpc);
                    }
                    if (Objects.nonNull(country_num))
                    {
                        userParma.setCountry_num(country_num);
                    }
                    DataCheckTool.checkUserDataFormate(userParma);
                    //校验手机号是否已被注册
                    int count = userBaseMapper.validataUserPhoneOrNoIsRegister(vo.getStoreId(), phoneNew);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHYBZC, "手机号已被注册");
                    }
                    count = userBaseMapper.updateUserInfoById(userParma);
                    if (count < 1)
                    {
                        logger.debug("用户id=" + user.getId() + " 密码修改失败");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                    }
                    //清除短信缓存
                    redisUtil.del(GloabConst.RedisHeaderKey.UPDATE_PHOE_CODE + phoneNew);
                    //重置登陆缓存
                    user = userBaseMapper.selectByPrimaryKey(user.getId());
                    RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);
                    return true;
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "updatePhone");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改手机号异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updatePhone");
        }
        return false;
    }

    @Override
    public Map<String, Object> aboutUs(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        String              aboutus   = "";
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                aboutus = configModel.getAboutus();
            }
//            aboutus = XssUtil.stripXSS(aboutus);
            resultMap.put("aboutus", aboutus);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取关于我们异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "aboutUs");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> intoWallet1(MainVo vo, Integer shopId, String pluginType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = publicUserService.getIntoWallet(vo, shopId, user, pluginType);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "intoWallet1");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> withdrawals1(Withdrawals1Vo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            logger.info("从Redis获取用户信息成功，用户信息: " + user);

            if (vo.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.YHK))
            {
                logger.info("开始校验验证码，手机号: " + vo.getMobile() + ", 验证码: " + vo.getKeyCode());
                // 校验验证码
                publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.DRAWING_CODE, vo.getMobile(), vo.getKeyCode());
                logger.info("验证码校验成功");
            }

            if (!publiceService.withdrawals(vo, user))
            {
                logger.error("提现记录失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSBQSHZS_001, "提现失败,请稍后重试");
            }
            logger.info("提现记录成功");

            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            logger.info("获取商户信息成功，商户信息: " + mchModel);

            // 删除短信
            redisUtil.del(GloabConst.RedisHeaderKey.DRAWING_CODE + vo.getMobile());
            logger.info("删除短信验证码缓存成功，手机号: " + vo.getMobile());

            WithdrawModel       withdrawOld        = new WithdrawModel();
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            if (vo.getShopId() != null)
            {
                withdrawOld = withdrawModelMapper.getWithdrawInfoByUserId(user.getUser_id(), DictionaryConst.WhetherMaven.WHETHER_OK);
                logger.info("获取店铺提现信息成功，提现信息: " + withdrawOld);

                // 店铺相关消息通知
                messageLoggingSave.setMch_id(user.getMchId());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
                messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.STORE_WITHDRAWAL_URL);
                messageLoggingSave.setParameter(mchModel.getName());
                messageLoggingSave.setContent(String.format("ID为%s的店铺申请提取余额，请及时处理！", mchModel.getId()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                logger.info("店铺提现消息通知插入成功");

                // h5店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(user.getMchId());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setParameter("提现申请已提交");
                messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                logger.info("h5店铺提现申请提交成功消息通知插入成功");

                // pc店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(user.getMchId());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
                messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
                messageLoggingSave.setParameter("");
                messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                logger.info("pc店铺提现申请提交成功消息通知插入成功");
            }
            else
            {
                withdrawOld = withdrawModelMapper.getWithdrawInfoByUserId(user.getUser_id(), DictionaryConst.WhetherMaven.WHETHER_NO);
                logger.info("获取用户提现信息成功，提现信息: " + withdrawOld);

                if (StringUtils.isNotEmpty(vo.getPluginType()))
                {
                    if (vo.getPluginType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                    {
                        messageLoggingSave.setMch_id(user.getMchId());
                        messageLoggingSave.setStore_id(vo.getStoreId());
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                        messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_DISTRIBUTION_WITHDRAWAL_URL);
                        messageLoggingSave.setParameter(user.getUser_name());
                        messageLoggingSave.setContent(String.format("ID为%s的分销商申请提取余额，请及时处理！", user.getUser_id()));
                        messageLoggingSave.setAdd_date(new Date());
                        logger.info("分销商提现消息通知设置成功");
                    }
                }
                else
                {
                    messageLoggingSave.setMch_id(user.getMchId());
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
                    messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_WITHDRAWAL_URL);
                    messageLoggingSave.setParameter(user.getUser_name());
                    messageLoggingSave.setContent(String.format("ID为%s的用户申请提取余额，请及时处理！", user.getUser_id()));
                    messageLoggingSave.setAdd_date(new Date());
                    logger.info("用户提现消息通知设置成功");
                }
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                logger.info("用户提现消息通知插入成功");
            }
            resultMap.put("addDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
            logger.info("结果Map添加日期成功，日期: " + resultMap.get("addDate"));
        }
        catch (LaiKeAPIException l)
        {
            logger.error("捕获到LaiKeAPIException异常，异常信息: " + l.getMessage(), l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请提现异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals1");
        }
        return resultMap;
    }
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public Map<String, Object> withdrawals1(Withdrawals1Vo vo) throws LaiKeAPIException
//    {
//        logger.error("进入withdrawals1方法了，vo参数为"+vo.toString());
//        Map<String, Object> resultMap = new HashMap<>(16);
//        try
//        {
//            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
//            if (vo.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.YHK))
//            {
//                //校验验证码
//                publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.DRAWING_CODE, vo.getMobile(), vo.getKeyCode());
//            }
//            if (!publiceService.withdrawals(vo, user))
//            {
//                logger.error("提现记录失败");
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSBQSHZS_001, "提现失败,请稍后重试");
//            }
//            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
//            //删除短信
//            redisUtil.del(GloabConst.RedisHeaderKey.DRAWING_CODE + vo.getMobile());
//            WithdrawModel       withdrawOld        = new WithdrawModel();
//            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
//            if (vo.getShopId() != null)
//            {
//                withdrawOld = withdrawModelMapper.getWithdrawInfoByUserId(user.getUser_id(), DictionaryConst.WhetherMaven.WHETHER_OK);
//                messageLoggingSave.setMch_id(user.getMchId());
//                messageLoggingSave.setStore_id(vo.getStoreId());
//                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
//                messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.STORE_WITHDRAWAL_URL);
//                messageLoggingSave.setParameter(mchModel.getName());
//                messageLoggingSave.setContent(String.format("ID为%s的店铺申请提取余额，请及时处理！", mchModel.getId()));
//                messageLoggingSave.setAdd_date(new Date());
//                messageLoggingModalMapper.insertSelective(messageLoggingSave);
//                //h5店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
//                messageLoggingSave = new MessageLoggingModal();
//                messageLoggingSave.setMch_id(user.getMchId());
//                messageLoggingSave.setStore_id(vo.getStoreId());
//                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
//                messageLoggingSave.setTo_url("");
//                messageLoggingSave.setParameter("提现申请已提交");
//                messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
//                messageLoggingSave.setAdd_date(new Date());
//                messageLoggingModalMapper.insertSelective(messageLoggingSave);
//                //pc店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
//                messageLoggingSave = new MessageLoggingModal();
//                messageLoggingSave.setMch_id(user.getMchId());
//                messageLoggingSave.setStore_id(vo.getStoreId());
//                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
//                messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
//                messageLoggingSave.setParameter("");
//                messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
//                messageLoggingSave.setAdd_date(new Date());
//                messageLoggingModalMapper.insertSelective(messageLoggingSave);
//            }
//            else
//            {
//                withdrawOld = withdrawModelMapper.getWithdrawInfoByUserId(user.getUser_id(), DictionaryConst.WhetherMaven.WHETHER_NO);
//                if (StringUtils.isNotEmpty(vo.getPluginType()))
//                {
//                    if (vo.getPluginType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
//                    {
//                        messageLoggingSave.setMch_id(user.getMchId());
//                        messageLoggingSave.setStore_id(vo.getStoreId());
//                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
//                        messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_DISTRIBUTION_WITHDRAWAL_URL);
//                        messageLoggingSave.setParameter(user.getUser_name());
//                        messageLoggingSave.setContent(String.format("ID为%s的分销商申请提取余额，请及时处理！", user.getUser_id()));
//                        messageLoggingSave.setAdd_date(new Date());
//                    }
//                }
//                else
//                {
//                    messageLoggingSave.setMch_id(user.getMchId());
//                    messageLoggingSave.setStore_id(vo.getStoreId());
//                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
//                    messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_WITHDRAWAL_URL);
//                    messageLoggingSave.setParameter(user.getUser_name());
//                    messageLoggingSave.setContent(String.format("ID为%s的用户申请提取余额，请及时处理！", user.getUser_id()));
//                    messageLoggingSave.setAdd_date(new Date());
//                }
//                messageLoggingModalMapper.insertSelective(messageLoggingSave);
//            }
//            resultMap.put("addDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
//        }
//        catch (LaiKeAPIException l)
//        {
//            logger.error("捕获到LaiKeAPIException异常，异常信息: " + l.getMessage(), l);
//            throw l;
//        }
//        catch (Exception e)
//        {
//            logger.error("申请提现异常 ", e);
//            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals1");
//        }
//        return resultMap;
//    }


    @Override
    public Map<String, Object> verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //是否为数字
            if (!bankTool.isNumber(bankCardNumber))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKGSCW, "银行卡格式错误");
            }
            //获取银行名称
            String name = bankTool.getBankNameByNo(bankCardNumber);
            if (StringUtils.isEmpty(name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKGSCW, "银行卡格式错误");
            }
            else
            {
                name = name.substring(0, name.indexOf("-"));
                resultMap.put("Bank_name", name);
                if (!StringUtils.isEmpty(bankName))
                {
                    if (!name.equals(bankName))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKXXBPP, "银行卡信息不匹配");
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
            logger.error("校验银行卡异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationBank");
        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> bankList(MainVo vo) throws LaiKeAPIException
    {
        List<Map<String, Object>> bankInfoList = new ArrayList<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取客户银行卡信息
            List<BankCardModel> bankCardModelList = bankCardModelMapper.selectBankcardByUser(vo.getStoreId(), user.getUser_id());
            for (BankCardModel bankCard : bankCardModelList)
            {
                Map<String, Object> bankMap = new HashMap<>(16);
                bankMap.put("id", bankCard.getId());
                bankMap.put("Bank_name", bankCard.getBank_name());
                String bankNo = bankCard.getBank_card_number();
                bankMap.put("Bank_card_number", bankNo.substring(bankNo.length() - 4));
                bankMap.put("branchName", bankCard.getBranch());
                bankInfoList.add(bankMap);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的银行卡列表 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bankList");
        }
        return bankInfoList;
    }

    @Override
    public Map<String, Object> getBankDetail(MainVo vo, int bankId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取客户银行卡信息
            BankCardModel bankCardModel = bankCardModelMapper.selectByPrimaryKey(bankId);
            if (bankCardModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "银行卡不存在");
            }
            if (!user.getUser_id().equals(bankCardModel.getUser_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ILLEGAL_INVASION, "非法入侵");
            }

            resultMap.put("cardholder", bankCardModel.getCardholder());
            resultMap.put("cardNo", bankCardModel.getBank_card_number());
            resultMap.put("bankName", bankCardModel.getBank_name());
            resultMap.put("branchName", bankCardModel.getBranch());
            resultMap.put("isDefault", bankCardModel.getIs_default());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取银行卡详情 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBankDetail");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addBank(AddBankVo vo) throws LaiKeAPIException
    {
        try
        {
            User          user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            BankCardModel bankCardOld = null;
            if (vo.getId() != null)
            {
                bankCardOld = bankCardModelMapper.selectByPrimaryKey(vo.getId());
                if (bankCardOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "银行卡不存在");
                }
            }
            //验证银行卡信息
            verificationBank(vo, vo.getBankName(), vo.getBankCardNumber());

            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setStore_id(vo.getStoreId());
            bankCardModel.setUser_id(user.getUser_id());
            bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (bankCardOld == null || !bankCardOld.getBank_card_number().equals(vo.getBankCardNumber()))
            {
                bankCardModel.setBank_card_number(vo.getBankCardNumber());
                if (bankCardModelMapper.selectCount(bankCardModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKYCZ, "银行卡已存在");
                }
            }
            //判断此银行卡是否为默认
            if (vo.getIsDefault() == 1)
            {
                int row = bankCardModelMapper.clearDefault(vo.getStoreId(), user.getUser_id());
                bankCardModel.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
            }
            else
            {
                //是否有默认,没有默认则设置默认
                BankCardModel bankCardModelTemp = bankCardModelMapper.getDefaultBankcardByUser(vo.getStoreId(), user.getUser_id());
                if (bankCardModelTemp == null)
                {
                    //没有默认必须要有一个默认
                    bankCardModel.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
            }
            bankCardModel.setCardholder(vo.getCardholder());
            bankCardModel.setBank_name(vo.getBankName());
            bankCardModel.setBranch(vo.getBranchName());
            bankCardModel.setBank_card_number(vo.getBankCardNumber());
            bankCardModel.setAdd_date(new Date());
            //添加银行卡
            int count;
            if (bankCardOld == null)
            {
                count = bankCardModelMapper.insertSelective(bankCardModel);
            }
            else
            {
                bankCardModel.setId(bankCardOld.getId());
                count = bankCardModelMapper.updateByPrimaryKeySelective(bankCardModel);
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBank");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultBank(MainVo vo, int bankId) throws LaiKeAPIException
    {
        try
        {
            User          user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setUser_id(user.getUser_id());
            bankCardModel.setId(bankId);
            bankCardModel = bankCardModelMapper.selectOne(bankCardModel);
            if (bankCardModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }

            //清空之前默认
            int           row             = bankCardModelMapper.clearDefault(vo.getStoreId(), user.getUser_id());
            BankCardModel bankCardDefault = new BankCardModel();
            bankCardDefault.setId(bankId);
            bankCardDefault.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
            bankCardModelMapper.updateByPrimaryKeySelective(bankCardDefault);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefaultBank");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBank(MainVo vo, int bankId) throws LaiKeAPIException
    {
        try
        {
            User          user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setUser_id(user.getUser_id());
            bankCardModel.setId(bankId);
            bankCardModel = bankCardModelMapper.selectOne(bankCardModel);
            if (bankCardModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }
            BankCardModel bankCardUpdate = new BankCardModel();
            bankCardUpdate.setId(bankCardModel.getId());
            bankCardUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            int row = bankCardModelMapper.updateByPrimaryKeySelective(bankCardUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //当前是否是默认银行卡,如果是则默认另外一张,否则不处理
            if (bankCardModel.getIs_default() == 1)
            {
                Integer id = bankCardModelMapper.getNewCardOneByUserId(user.getUser_id());
                if (id != null)
                {
                    BankCardModel bankCardDefault = new BankCardModel();
                    bankCardDefault.setId(id);
                    bankCardDefault.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                    bankCardModelMapper.updateByPrimaryKeySelective(bankCardDefault);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("解绑银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBank");
        }
    }

    @Override
    public Map<String, Object> index(MainVo vo, String mobile) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user    = null;
            Map<String, Object> dataMap = new HashMap<>(16);
            //插件状态
            Map<String, Object> pluginMap = new HashMap<>(16);
            //用户信息
            Map<String, Object> userMap = new HashMap<>(16);
            //店铺商标
            String logoImg = "";
            //公司名称
            String company = "";
            //未读消息数量
            int notReadNum = 0;

            //是否同步
            int synchronizeFlag = 0;
            //是否为完善用户信息需要跳转
            boolean      haveAllUserInfo = false;
            List<String> pluginCodeList  = pluginsModelMapper.getPluginsCodeAll(vo.getStoreId());
            for (String pluginCode : pluginCodeList)
            {
                publiceService.frontPlugin(vo.getStoreId(), null, pluginCode, pluginMap,false);
            }
            //获取会员信息
            user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (!StringUtils.isEmpty(mobile))
                {
                    //查询手机号是否绑定其他账号（这个账户有可能就是新账户）
                    User userOld = userBaseMapper.getUserByMobile(vo.getStoreId(), vo.getAccessId(), mobile);
                    if (userOld != null)
                    {
                        synchronizeFlag = 1;
                    }
                    else
                    {
                        //同步用户信息
                        userBaseMapper.syncUserInfo(vo.getAccessId(), mobile, vo.getStoreId());
                    }
                }
                //刷新缓存
                user = userBaseMapper.selectByPrimaryKey(user.getId());
                RedisDataTool.refreshRedisUserCache(user.getAccess_id(), user, redisUtil);
                if (user.getIs_default_value() != null && user.getIs_default_value() == 1)
                {
                    haveAllUserInfo = true;
                }
                userMap.put("haveAllUserInfo", haveAllUserInfo);
                userMap.put("headimgurl", user.getHeadimgurl());
                String userName = user.getUser_name();
                userMap.put("user_name", userName);
                //保留两位小数
                userMap.put("money", Objects.nonNull(user.getMoney()) ? user.getMoney().setScale(2, BigDecimal.ROUND_HALF_UP).toString() : 0.00d);
                userMap.put("score", user.getScore());
                String phone = user.getMobile();
                userMap.put("mobile", phone);
                userMap.put("zhanghao", user.getZhanghao());
                if (StringUtils.isNotEmpty(user.getCpc()) && Objects.nonNull(user.getCountry_num()))
                {
                    //国家代码
                    CountryModel countryModel = new CountryModel();
                    countryModel.setNum3(user.getCountry_num());
                    countryModel.setCode2(user.getCpc());
                    countryModel = countryModelMapper.selectOne(countryModel);
                    if (Objects.nonNull(countryModel))
                    {
                        userMap.put("code", countryModel.getCode());
                    }
                }
                userMap.put("havaPayPwd",Objects.nonNull(user.getPassword()));
                userMap.put("user_id", user.getUser_id());
                userMap.put("e_mail",user.getE_mail());
                userMap.put("cpc",user.getCpc());
                userMap.put("country_num",user.getCountry_num());
                userMap.put("havaMobile",StringUtils.isNotEmpty(user.getMobile()));
                userMap.put("wx_name", user.getWx_name() == null ? "" : user.getWx_name());
                if (StringUtils.isEmpty(user.getWx_id()))
                {
                    userMap.put("wx_withdraw", false);
                }
                else
                {
                    userMap.put("wx_withdraw", true);
                }
                //是否有登录密码
                userMap.put("isLoginPwd", StringUtils.isNotEmpty(user.getMima()));
                Date   birthday       = user.getBirthday();
                userMap.put("checkInfo",Objects.nonNull(user.getMobile()) && Objects.nonNull(birthday) && Objects.nonNull(
                        user.getSex()));
                if (birthday != null)
                {
                   String birthdayFormat = FastDateFormat.getInstance(GloabConst.TimePattern.YMD).format(birthday);
                   userMap.put("birthday", birthdayFormat);
                }
                userMap.put("sex", user.getSex());

                //获取我的优惠卷数量
                Map<String, Object> couponMap  = publicCouponService.mycoupon(vo.getStoreId(), user.getUser_id(), 0);
                List<Object>        couponList = DataUtils.cast(couponMap.get("list"));
                int                 couponNum  = 0;
                if (couponList != null)
                {
                    couponNum = couponList.size();
                }
                userMap.put("coupon_num", couponNum);
                //是否为分销商 1=true 0=false
                int     isDistribution = 0;
                Integer mchIdMain      = customerModelMapper.getStoreMchId(vo.getStoreId());
                //是否为总店
                boolean isMain = mchIdMain.equals(user.getMchId());
                //获取会员分销信息
                UserDistributionModel userDistributionModel = new UserDistributionModel();
                userDistributionModel.setStore_id(user.getStore_id());
                userDistributionModel.setUser_id(user.getUser_id());
                userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                if (userDistributionModel != null)
                {
                    if (isMain || userDistributionModel.getLevel() > 0)
                    {
                        isDistribution = 1;
                    }
                }
                userMap.put("isDistribution", isDistribution);
                userMap.put("is_default_birthday", user.getIs_default_birthday() == null ? 2 : user.getIs_default_birthday());
                //获取未读消息数量
                SystemMessageModel systemMessageModel = new SystemMessageModel();
                systemMessageModel.setStore_id(vo.getStoreId());
                systemMessageModel.setRecipientid(user.getUser_id());
                systemMessageModel.setType(1);
                notReadNum = systemMessageModelMapper.selectCount(systemMessageModel);

                String     imgurlMy  = "";
                BigDecimal rate      = new BigDecimal("0");
                String     imgurls   = "";
                String     fontColor = "";
                String     dateColor = "";
                //会员过期处理
                if (user.getGrade_end() != null)
                {
                    if (DateUtil.dateCompare(new Date(), user.getGrade_end()) && String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK).equals(user.getIs_out()))
                    {
                        User userUpdate = new User();
                        userUpdate.setId(user.getId());
                        userUpdate.setGrade(User.USER);
                        userUpdate.setIs_out(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK));
                        userBaseMapper.updateByPrimaryKeySelective(userUpdate);
                    }
                }
                //获取会员等级信息
                Map<String, Object> memberInfo = new HashMap<>(16);
                memberInfo.put("grade", user.getGrade());
                memberInfo.put("tuiId", user.getTui_id());
                memberInfo.put("gradeAddTime", DateUtil.dateFormate(user.getGrade_add(), GloabConst.TimePattern.YMDHMS));
                memberInfo.put("gradeM", user.getGrade_m());
                memberInfo.put("gradeEndTime", DateUtil.dateFormate(user.getGrade_end(), GloabConst.TimePattern.YMDHMS));
                memberInfo.put("isOut", user.getIs_out());
                memberInfo.put("isBox", user.getIs_box());
                //会员提醒
                boolean             remind = false;
                Map<String, Object> config = memberConfigMapper.getConfig(vo.getStoreId());
                if (MapUtils.getInteger(config, "id") != null && MapUtils.getInteger(config, "is_open") > 0)
                {
                    if (StringUtils.isNotEmpty(user.getUser_id()))
                    {
                        if (Integer.parseInt(user.getIs_box()) == DictionaryConst.WhetherMaven.WHETHER_OK && user.getGrade().equals(User.MEMBER))
                        {
                            if (config != null && MapUtils.getInteger(config, "is_open") > 0)
                            {
                                Integer renewOpen = MapUtils.getInteger(config, "renew_open");
                                if (renewOpen.equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                                {
                                    if (user.getGrade_end() != null)
                                    {
                                        Integer renewDay    = MapUtils.getInteger(config, "renew_day");
                                        long    end         = user.getGrade_end().getTime() / 1000;
                                        long    now         = new Date().getTime() / 1000;
                                        int     betweenDate = DateUtil.getBetweenDate(now, end);
                                        if (betweenDate < renewDay)
                                        {
                                            remind = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                memberInfo.put("remind", remind);
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                //获取个人中心小红点
                int[] numArr = {DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID, DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT, DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED, 3, 4};
                for (int num : numArr)
                {
                    List<String> orderTypeList = new ArrayList<>();
                    orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
                    orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);

                    switch (num)
                    {
                        case 3:
                            Map<String, Object> parmaMap1 = new HashMap<>(16);
                            parmaMap1.put("store_id", vo.getStoreId());
                            parmaMap1.put("user_id", user.getUser_id());
                            parmaMap1.put("typeList", orderTypeList);
                            parmaMap1.put("user_recycle", DictionaryConst.WhetherMaven.WHETHER_OK);
                            //待评价
                            int orderNum = orderModelMapper.countNotCommentNum(parmaMap1);
                            dataMap.put("dpj_num", orderNum);
                            break;
                        case 4:
                            //退款售后
                            parmaMap.put("user_id", user.getUser_id());
                            //a.r_type not in  (2,8,4,5,9,10,12,13,15) and b.r_status != 7
                            int number = returnOrderModelMapper.countReturnNotFinishedCount(parmaMap);
                            dataMap.put("th", number);
                            break;
                        default:
                            //更具状态获取订单数量
                            Map<String, Object> parmaMap2 = new HashMap<>(16);
                            parmaMap2.put("status", num);
                            parmaMap2.put("store_id", vo.getStoreId());
                            parmaMap2.put("userId", user.getUser_id());
                            parmaMap2.put("orderTypeList", orderTypeList);
                            parmaMap2.put("user_recycle", DictionaryConst.WhetherMaven.WHETHER_OK);
                            orderNum = orderModelMapper.countDynamic(parmaMap2);
                            if (num == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID)
                            {
                                dataMap.put("dfk_num", orderNum);
                            }
                            else if (num == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT)
                            {
                                dataMap.put("dfh_num", orderNum);
                            }
                            else
                            {
                                dataMap.put("dsh_num", orderNum);
                            }
                            break;
                    }
                }
                //处理限时折扣用户待处理订单，针对用户端来讲，待处理的订单就是待付款/待收货/待评价，一般待评价的订单会有自动评价的功能。另外还有就是退款状态需要邮寄这种
                if (pluginMap.containsKey("flashsale") && MapUtils.getInteger(pluginMap,"flashsale") == 1)
                {
                    List<String>  orderTypeList = new ArrayList<>();
                    List<Integer> statusList    = new ArrayList<>();
                    orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_FS);
                    statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                    statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
                    //订单完成待评价
                    statusList.add(5);

                    Map<String, Object> parmaMap2 = new HashMap<>(16);
                    parmaMap2.put("statusList", statusList);
                    parmaMap2.put("store_id", vo.getStoreId());
                    parmaMap2.put("userId", user.getUser_id());
                    parmaMap2.put("orderTypeList", orderTypeList);
                    parmaMap2.put("no_comment", "no_comment");
                    parmaMap2.put("user_recycle", DictionaryConst.WhetherMaven.WHETHER_OK);
                    int orderNum = orderModelMapper.countDynamic(parmaMap2);

                    dataMap.put("fs_num", orderNum);
                }
                //获取微信小程序支付配置
                String paymentJson = paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT);
                if (paymentJson != null)
                {
                    paymentJson = URLDecoder.decode(paymentJson, GloabConst.Chartset.UTF_8);
                    JSONObject payJson = JSONObject.parseObject(paymentJson);
                    String     mchID   = payJson.getString("mch_id");
                    logger.info("获取微信小程序支付配置mch_id :" + mchID);
                    dataMap.put("merchant_id", mchID);
                }

                //获取收藏数量
                UserCollectionModel userCollectionModel = new UserCollectionModel();
                userCollectionModel.setStore_id(vo.getStoreId());
                userCollectionModel.setUser_id(user.getUser_id());
                userCollectionModel.setType(DictionaryConst.UserCollectionType.COLLECTIONTYPE1);
                int collectionNum = userCollectionModelMapper.selectCount(userCollectionModel);
                //默认获取自营店id
                Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                dataMap.put("storeMchId", storeMchId);
                dataMap.put("user", userMap);
                dataMap.put("memberInfo", memberInfo);
                dataMap.put("logo", logoImg);
                dataMap.put("company", company);
                dataMap.put("collection_num", collectionNum);
                dataMap.put("footprint_num", collectionNum);
                dataMap.put("font_color", fontColor);
                dataMap.put("date_color", dateColor);
                dataMap.put("rate", rate);
                dataMap.put("imgurl_my", imgurlMy);
                dataMap.put("imgurl_s", imgurls);
                dataMap.put("xxnum", notReadNum);
                dataMap.put("synchronize_type", synchronizeFlag);
                resultMap.put("data", dataMap);
            }
            else
            {
                ConfigModel configOld = new ConfigModel();
                configOld.setStore_id(vo.getStoreId());
                configOld = configModelMapper.selectOne(configOld);
                String defaultHeaderImgUrl = "", defaultUserName = "";
                if (configOld != null)
                {
                    defaultHeaderImgUrl = publiceService.getImgPath(configOld.getWx_headimgurl(), vo.getStoreId());
                    defaultUserName = configOld.getWx_name();
                }
                resultMap.put("defaultHeaderImgUrl", defaultHeaderImgUrl);
                resultMap.put("defaultUserName", defaultUserName);
            }

            // TODO: 2024/4/17 【优化】  获取【我的-推荐】列表
            //获取【为您推荐】列表
            //商品状态 用于in查询
            List<Integer> goodsStatus = new ArrayList<>();

            //会员折扣率
            BigDecimal gradeRate = new BigDecimal("1");
            //商品信息
            List<Map<String, Object>> goodsList = new ArrayList<>();
            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }

            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.put("store_id", vo.getStoreId());
            parmaMap1.put("GoodsStatus", goodsStatus);
            parmaMap1.put("is_open", MchModel.IS_OPEN_IN_BUSINESS);
            parmaMap1.put("show_adr", DictionaryConst.GoodsShowAdr.GOODSSHOWADR_MY);
            parmaMap1.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap1.put("pageNo", vo.getPageNo());
            parmaMap1.put("pageSize", vo.getPageSize());
            parmaMap1.put("country_num", vo.getCountry_num());
            parmaMap1.put("lang_code", vo.getLanguage());
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap1.put("stockNum", "stockNum");
            }
            goodsList = productListModelMapper.getProductListDynamic(parmaMap1);
            for (Map<String, Object> map : goodsList)
            {
                String imgUrl = map.get("imgurl") + "";
                imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                int pid      = Integer.parseInt(map.get("pid") + "");
                int stockNum = confiGureModelMapper.countConfigGureNum(pid);
                //店铺信息
                String logoUrl = publicService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId());
                String mchName = map.get("mch_name").toString();
                //获取商品标签
                String sType = MapUtils.getString(map, "s_type");
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));

                //原来价格
                BigDecimal vipYprice = new BigDecimal(map.get("price") + "");
                //打折后的价格
                BigDecimal vipPrice = new BigDecimal(map.get("price") + "");
                if (gradeRate.floatValue() != 1)
                {
                    //折扣 原来价格 * 折扣  = 优惠价
                    vipPrice = vipYprice.multiply(gradeRate);
                }
                //虚拟商品特有的isAddCar，为1是可以加
                if (!map.containsKey("is_appointment") || (MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin))
                {
                    map.put("isAddCar", 1);
                }

                map.put("vip_yprice", vipYprice.toString());
                map.put("vip_price", vipPrice.toString());
                map.put("num", stockNum);
                map.put("imgurl", imgUrl);
                map.put("logo", logoUrl);
                map.put("mch_name", mchName);
            }

            resultMap.put("list", goodsList);
            resultMap.put("plugin", pluginMap);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取我的数据异常 ", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的数据异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(MainVo vo, String phone) throws LaiKeAPIException
    {
        try
        {
            //当前用户信息
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            User user      = new User();
            user.setStore_id(vo.getStoreId());
            user.setMobile(phone);
            User userDb = userBaseMapper.selectOne(user);
            if (Objects.isNull(userDb))
            {
                userCache.setZhanghao(phone);
                userCache.setMobile(phone);
                userBaseMapper.updateUserInfoById(userCache);
                String token = vo.getAccessId();
                RedisDataTool.refreshRedisUserCache(token, userCache, redisUtil);
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("绑定手机号码失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bindPhone");
        }


    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void synchronizeAccount(MainVo vo, String mobile, Integer source) throws LaiKeAPIException
    {
        try
        {
            //数据库老用户，通过手机号来查询老用户
            User userDb = userBaseMapper.getUserByMobile(vo.getStoreId(), vo.getAccessId(), mobile);
            //缓存新用户
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //若此用户为老用户
            if (userDb != null)
            {
                int id      = userCache.getId();
                int cacheId = userDb.getId();
                if (id != cacheId)
                {
                    //若为微信小程序登录则使用微信名称否则使用支付宝的名称
                    if (DictionaryConst.StoreSource.LKT_LY_001.equals(source.toString()))
                    {
                        userDb.setWx_id(userCache.getWx_id());
                        //现在用户小程序登录没有获取到用户微信名称  合并不需要更新微信名称
//                        userDb.setWx_name(userCache.getWx_name());
                    }
                    else
                    {
                        userDb.setZfb_id(userCache.getZfb_id());
                    }
                    //将新用户的信息更新同步到老用户（若有）上
                    userDb.setAccess_id(userCache.getAccess_id());
                    userBaseMapper.delete(userCache);
                    userBaseMapper.updateByPrimaryKeySelective(userDb);
                    //更新用户数据
                    userDb = userBaseMapper.selectByPrimaryKey(userDb.getId());
                    //更新redis
                    RedisDataTool.refreshRedisUserCache(vo.getAccessId(), userDb, redisUtil);
                }
            }
            else
            {
                //新用户则直接添加
                if (!Objects.isNull(userCache))
                {
                    userCache.setMobile(mobile);
                    userCache.setZhanghao(mobile);
                    userBaseMapper.updateByPrimaryKeySelective(userCache);
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("同步微信小程序用户信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "synchronizeAccount");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> synuser(SycnUserVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        logger.info("同步用户信息：{}", JSON.toJSONString(vo));
        try
        {
            //数据库是否存在
            User   userDb = userBaseMapper.getUserByUserFromID(vo.getStoreId(), vo.getUser_from_id());
            String token  = null;
            if (userDb != null)
            {
                logger.info("用户已经存在：{}", JSON.toJSONString(userDb));
            }
            else
            {
                User user = new User();
                user.setUser_from_id(vo.getUser_from_id());
                user.setSource(String.valueOf(vo.getSource()));
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
                user.setAccess_id(token);
                if (StringUtils.isNotEmpty(vo.getUser_name()))
                {
                    user.setUser_name(vo.getUser_name());
                }
                else
                {
                    user.setUser_name("用户" + vo.getUser_from_id());
                }
                user.setStore_id(vo.getStoreId());
                user.setMobile(vo.getMobile());
                user.setZhanghao(vo.getMobile());
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
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            String h5Url = "";
            if (configModel != null)
            {
                h5Url = configModel.getH5_domain();
            }

            resultMap.put("h5_url", h5Url + "pages/tabBar/Home?access_id=" + token + "&" + userDb.getUser_id() + "&" + userDb.getStore_id());
            resultMap.put("access_id", token);
            resultMap.put("store_id", vo.getStoreId());
            resultMap.put("user_id", userDb.getUser_id());
        }
        catch (LaiKeAPIException e)
        {
            logger.error("同步微信小程序用户信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "synchronizeAccount");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getRecordDetails(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (id == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getRecordDetails");
            }
            RecordDetailsModel recordDetailsModel = recordDetailsModelMapper.selectByPrimaryKey(id);
            if (recordDetailsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在", "getRecordDetails");
            }

            resultMap.put("storeId", recordDetailsModel.getStore_id());
            resultMap.put("money", recordDetailsModel.getMoney());
            resultMap.put("userMoney", recordDetailsModel.getUserMoney());
            resultMap.put("type", recordDetailsModel.getType());
            resultMap.put("moneyType", recordDetailsModel.getMoneyType());
            resultMap.put("moneyTypeName", recordDetailsModel.getMoneyTypeName());
            resultMap.put("recordTime", DateUtil.dateFormate(recordDetailsModel.getRecordTime(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("recordNotes", recordDetailsModel.getRecordNotes());
            resultMap.put("typeName", recordDetailsModel.getTypeName());
            resultMap.put("sNo", recordDetailsModel.getsNo());
            resultMap.put("titleName", recordDetailsModel.getTitleName());
            resultMap.put("activityCode", recordDetailsModel.getActivityCode());
            resultMap.put("mchName", recordDetailsModel.getMchName());
            resultMap.put("currency_symbol", recordDetailsModel.getCurrency_symbol());
            resultMap.put("currency_code", recordDetailsModel.getCurrency_code());
            resultMap.put("exchange_rate", recordDetailsModel.getExchange_rate());

            //余额提现特殊处理
            if (recordDetailsModel.getType().equals(RecordDetailsModel.type.WITHDRAWAL_OF_BALANCE))
            {
                String sNo = recordDetailsModel.getsNo();
                if (sNo == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在", "getRecordDetails");
                }
                WithdrawModel withdrawModel = new WithdrawModel();
                withdrawModel.setTxsno(sNo);
                withdrawModel.setStore_id(vo.getStoreId());
                withdrawModel = withdrawModelMapper.selectOne(withdrawModel);
                if (withdrawModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在", "getRecordDetails");
                }
                resultMap.put("withdrawalFees", recordDetailsModel.getWithdrawalFees());
                resultMap.put("withdrawalMethod", recordDetailsModel.getWithdrawalMethod());
                resultMap.put("status", withdrawModel.getStatus());
                resultMap.put("account", withdrawModel.getMoney().subtract(withdrawModel.getS_charge()).setScale(2, RoundingMode.HALF_DOWN).toString());
                resultMap.put("startTime", DateUtil.dateFormate(withdrawModel.getAdd_date(), GloabConst.TimePattern.YMDHMS));
                resultMap.put("withdrawStatus", withdrawModel.getWithdrawStatus());
                resultMap.put("wxStatus", withdrawModel.getWxStatus() == null ? "" : withdrawModel.getWxStatus());
                if (withdrawModel.getExamine_date() != null)
                {
                    resultMap.put("examineTime", DateUtil.dateFormate(withdrawModel.getExamine_date(), GloabConst.TimePattern.YMDHMS));
                    resultMap.put("refuse", withdrawModel.getRefuse());
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("我的钱包记录详情 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRecordDetails");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void wxUnbind(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            //获取会员信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取最新信息
            user = userBaseMapper.selectByUserId(vo.getStoreId(), user.getUser_id());
            //取消绑定
            user.setWx_name(null);
            user.setWx_id(null);
            if (userBaseMapper.updateByPrimaryKey(user) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("用户解除与微信的绑定 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRecordDetails");
        }
    }

    @Override
    public void wxBind(MainVo vo, String openId, String wxName) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(openId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //获取会员信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取最新信息
            user = userBaseMapper.selectByUserId(vo.getStoreId(), user.getUser_id());
            //查询改openId是否已经绑定账户
            User odlUser = new User();
            odlUser.setStore_id(vo.getStoreId());
            odlUser.setWx_id(openId);
            if (userBaseMapper.selectCount(odlUser) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GWXYBDQTZH, "该微信已绑定其他账号");
            }

            //绑定
            if (StringUtils.isNotEmpty(wxName))
            {
                user.setWx_name(wxName);
            }
            user.setWx_id(openId);
            if (userBaseMapper.updateByPrimaryKeySelective(user) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("用户解除与微信的绑定 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRecordDetails");
        }
    }

    @Override
    public Map<String, Object> getMyRecommendation(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            // TODO: 2024/4/17 【优化】  获取【我的-推荐】列表
            //获取【为您推荐】列表
            //商品状态 用于in查询
            List<Integer> goodsStatus = new ArrayList<>();

            //会员折扣率
            BigDecimal gradeRate = new BigDecimal("1");
            //商品信息
            List<Map<String, Object>> goodsList = new ArrayList<>();
            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }

            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.put("store_id", vo.getStoreId());
            parmaMap1.put("GoodsStatus", goodsStatus);
//            parmaMap1.put("is_open", MchModel.IS_OPEN_IN_BUSINESS);
            parmaMap1.put("show_adr", DictionaryConst.GoodsShowAdr.GOODSSHOWADR_MY);
            parmaMap1.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap1.put("pageNo", vo.getPageNo());
            parmaMap1.put("pageSize", vo.getPageSize());
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap1.put("stockNum", "stockNum");
            }
            parmaMap1.put("lang_code",vo.getLang_code());
            goodsList = productListModelMapper.getProductListDynamic(parmaMap1);
            for (Map<String, Object> map : goodsList)
            {
                String imgUrl = map.get("imgurl") + "";
                Integer status = MapUtils.getInteger(map, "status");
                imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                int pid      = Integer.parseInt(map.get("pid") + "");
                int stockNum = confiGureModelMapper.countConfigGureNum(pid);
                //店铺信息
                String logoUrl = publicService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId());
                String mchName = map.get("mch_name").toString();
                //获取商品标签
                String sType = MapUtils.getString(map, "s_type");
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));

                //原来价格
                BigDecimal vipYprice = new BigDecimal(map.get("price") + "");
                //打折后的价格
                BigDecimal vipPrice = new BigDecimal(map.get("price") + "");
                if (gradeRate.floatValue() != 1)
                {
                    //折扣 原来价格 * 折扣  = 优惠价
                    vipPrice = vipYprice.multiply(gradeRate);
                }
                //如果商品为需要预约时间的则不能加入购物车，为1是可以加入购物车
                if (!map.containsKey("is_appointment") || MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin)
                {
                    map.put("isAddCar", 1);
                }
                Integer writeOffSettings = MapUtils.getInteger(map, "write_off_settings");
                if ((Objects.nonNull(writeOffSettings) && writeOffSettings == 1) || Objects.equals(status, DictionaryConst.GoodsStatus.OFFLINE_GROUNDING) || stockNum == 0)
                {
                    map.put("isAddCar",2);
                }
                map.put("vip_yprice", vipYprice.toString());
                map.put("vip_price", vipPrice.toString());
                map.put("num", stockNum);
                map.put("contNum", stockNum);
                map.put("imgurl", imgUrl);
                map.put("logo", logoUrl);
                map.put("mch_name", mchName);

                int payPeople = orderDetailsModelMapper.payPeople(vo.getStoreId(), pid);
                map.put("payPeople", payPeople);
            }
            resultMap.put("list", goodsList);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取我的-推荐数据异常 ", l);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMyRecommendation");
        }
        return resultMap;
    }

    @Override
    public void isDefaultValue(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setIs_default_value(2);
            userBaseMapper.updateByPrimaryKeySelective(updateUser);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记公告以读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "markToRead");
        }
    }

    @Override
    public List<Map<String, Object>> getItuList(String keyword)
    {
        try
        {
            return countryModelMapper.getItuList(keyword);
        } catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("区号查询异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getItuList");
        }
    }

    @Override
    public Map getUserCurrnetCurrencyInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WDL, "未登录", "getUserCurrnetCurrencyInfo");
            }
            return currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户当前货币信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserCurrnetCurrencyInfo");
        }
    }

    @Override
    public void sendEmail(String email, MainVo vo) throws LaiKeAPIException
    {
        try
        {
            publiceService.sendEmail(email,vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXFSSB, "邮箱发送失败", "sendEmail");
        }
    }

    @Override
    public Map<String, Object> getSystemIconAndName(MainVo vo) throws LaiKeAPIException
    {
        Map<String,Object> result = new HashMap<>();
        try
        {
            result = publiceService.getSystemIconAndName(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "获取信息失败", "getSystemIconAndName");
        }
        return result;
    }

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private UserRuleModelMapper userRuleModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private BankTool bankTool;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private PublicCouponService publicCouponService;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private PreSellRecordModelMapper preSellRecordModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

}
