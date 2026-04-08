package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.laiketui.admins.api.mch.MchUserLoginService;
import com.laiketui.common.api.PublicSystemTellService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchClassModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.ForgetPasswordVo;
import com.laiketui.domain.vo.user.LoginVo;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户登录
 *
 * @author Trick
 * @date 2021/5/26 9:47
 */
@Service("mchLoginImpl")
public class MchUserServiceImpl implements MchUserLoginService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @Autowired
    private UserAuthorityModelMapper userAuthorityModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private MchClassModelMapper mchClassModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(LoginVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        User                userTemp  = new User();
        try
        {
            //公告id
            Integer tell_id = 0;
            //公告标题
            String systemMsgTitle = "";
            //系统公告
            String systemMsg = "";
            //公告类型 1=系统维护 2=升级公告 3--普通公告
            int systemMsgType = 0;
            //维护开始、结束时间
            String systemMsgStartDate = "";
            String systemMsgEndDate   = "";

            //用户选择的语种
            String lang = vo.getLanguage();
            if (!StringUtils.isNotEmpty(lang))
            {
                lang = GloabConst.Lang.CN;
            }

            publiceService.validateImgCode(vo.getImgCodeToken(), vo.getImgCode());
            if (!StringUtils.isEmpty(vo.getLogin()) && !StringUtils.isEmpty(vo.getPwd()))
            {
                //账号或邮箱
                userTemp.setStore_id(vo.getStoreId());
                userTemp.setZhanghao(vo.getLogin());
                //获取用户信息
                User userinfo = userBaseMapper.getUserByzhanghaoOrEmail(userTemp);
                if (userinfo != null)
                {
                    if (!StringUtils.isEmpty(userinfo.getMima()))
                    {
                        String decoderPwd = CryptoUtil.strDecode(userinfo.getMima());
                        if (decoderPwd != null && decoderPwd.equals(vo.getPwd()))
                        {
                            //登录成功流程
                            userinfo.setLang(lang);
                            //登录用户的信息在loginSuccess这个方法里面塞到redis里面
                            loginSuccess(userinfo);
                            MchModel mchModel = mchModelMapper.selectByPrimaryKey(userinfo.getMchId());
                            mchModel.setLast_login_time(new Date());
                            mchModelMapper.updateByPrimaryKeySelective(mchModel);
                            Map<String, Object> parmaMap = new HashMap<>(16);
                            parmaMap.put("store_id", 0);
                            parmaMap.put("startDate_lt", new Date());
                            parmaMap.put("endDate_gt", new Date());
                            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
                            parmaMap.put("mch_tell", SystemTellModel.TELL.YES);
                            parmaMap.put("user_store_id", vo.getStoreId());
                            parmaMap.put("store_type", GloabConst.StoreType.STORE_TYPE_PC_MCH);
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
                            getCurrencyInfos(userinfo, resultMap);
                            //登陆成功
                            resultMap.put("token", userinfo.getMch_token());
                            resultMap.put("storeId", userinfo.getStore_id());
                            resultMap.put("user_id", userinfo.getUser_id());
                            //系统公告
                            resultMap.put("systemMsgTitle", systemMsgTitle);
                            resultMap.put("systemMsg", systemMsg);
                            resultMap.put("tell_id", tell_id);
                            resultMap.put("systemMsgType", systemMsgType);
                            resultMap.put("systemMsgEndDate", systemMsgEndDate);
                            resultMap.put("systemMsgStartDate", systemMsgStartDate);
                            resultMap.put("info", info);

                            Map mchInfo = getMchInfo(vo, mchModel);
                            resultMap.put("res", mchInfo);

                            return resultMap;
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHHMMBPP, "账户或密码不匹配", "login");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NHWSZMM, "您还未设置密码", "login");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLSBYHBCZ, "登录失败,用户不存在", "login");
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
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "login");
        }

        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLSBCSCW, "登陆失败,参数错误", "login");
    }

    /**
     * 获取店铺信息
     *
     * @param vo
     * @param mchModel
     * @return
     */
    private Map<String, Object> getMchInfo(LoginVo vo, MchModel mchModel)
    {
        int                 storeId = mchModel.getStore_id();
        Map<String, Object> mchMap  = new HashMap<>(16);
        if (mchModel != null && StringUtils.isNotEmpty(mchModel.getSheng()) && StringUtils.isNotEmpty(mchModel.getShi()) && StringUtils.isNotEmpty(mchModel.getXian()))
        {
            mchModel.setHead_img(publiceService.getImgPath(mchModel.getHead_img(), storeId));
            mchModel.setPoster_img(publiceService.getImgPath(mchModel.getPoster_img(), storeId));
            mchMap = JSON.parseObject(JSON.toJSONString(mchModel), new TypeReference<Map<String, Object>>()
            {
            });
            mchMap.put("logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
            Integer       cid           = MapUtils.getInteger(mchMap, "cid");
            MchClassModel mchClassModel = mchClassModelMapper.selectByPrimaryKey(cid);
            if (!Objects.isNull(mchClassModel))
            {
                mchMap.put("className", mchClassModel.getName());
            }
            //营业状态
            String is_open = MapUtils.getString(mchMap, "is_open");
            //未营业
            if ("0".equals(is_open) || "2".equals(is_open))
            {
                mchMap.put("is_open", is_open);
            }
            else if ("1".equals(is_open))
            {
                String businessHoursValue = MapUtils.getString(mchMap, "business_hours");
                if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                {
                    mchMap.put("is_open", "2");
                }
                else
                {
                    //营业时间判断是否营业
                    String[] businessHours = businessHoursValue.split(SplitUtils.BL);
                    //开始时间
                    Date startTime = DateUtil.dateFormateToDate(businessHours[0], GloabConst.TimePattern.HM);
                    //结束时间
                    Date endTime = DateUtil.dateFormateToDate(businessHours[1], GloabConst.TimePattern.HM);
                    //当前时间
                    Date currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.HM);
                    //开始时间大于结束时间(跨天：例如 19：00~04：00 则当前时间 >= 19:00 || 当前时间 <= 04:00 -> 营业 )
                    if (DateUtil.dateCompare(startTime, endTime))
                    {
                        if (!DateUtil.dateCompare(startTime, currentDate)
                                || !DateUtil.dateCompare(currentDate, endTime))
                        {
                            mchMap.put("is_open", "1");
                        }
                        else
                        {
                            //未营业
                            mchMap.put("is_open", "2");
                        }
                    }
                    else
                    {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                        if (!DateUtil.dateCompare(startTime, currentDate)
                                && !DateUtil.dateCompare(currentDate, endTime))
                        {
                            mchMap.put("is_open", "1");
                        }
                        else
                        {
                            mchMap.put("is_open", "2");
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
            }

            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictPid(0);
            adminCgModel.setDistrictName(mchModel.getSheng());
            AdminCgModel adminCg = adminCgModelMapper.selectOne(adminCgModel);
            if (adminCg != null)
            {
                mchMap.put("sheng_id", adminCg.getId());
                adminCgModel.setDistrictPid(adminCg.getId());
                adminCgModel.setDistrictName(mchModel.getShi());
                adminCg = adminCgModelMapper.selectOne(adminCgModel);
                if (adminCg != null)
                {
                    mchMap.put("shi_id", adminCg.getId());
                    adminCgModel.setDistrictPid(adminCg.getId());
                    adminCgModel.setDistrictName(mchModel.getXian());
                    adminCg = adminCgModelMapper.selectOne(adminCgModel);
                    if (adminCg != null)
                    {
                        mchMap.put("xian_id", adminCg.getId());
                    }
                }
            }
        }
        return mchMap;
    }

    private void getCurrencyInfos(User userinfo, Map<String, Object> resultMap)
    {
        resultMap.put("lang", userinfo.getLang());
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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginOut(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user;
            user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                User updateUser = new User();
                updateUser.setId(user.getId());
                updateUser.setMch_token("");
                int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
                if (count < 1)
                {
                    logger.info("退出登录失败 参数" + JSON.toJSONString(updateUser));
                }
                else
                {
                    String event = "退出店铺后台！";
                    //添加一条操作记录
                    publiceService.addAdminRecord(user.getStore_id(), user.getUser_id(), event, AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_SHOP, user.getMchId(), user.getId());
                    //删除缓存
                    redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH + user.getUser_id());
                    redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + vo.getAccessId());
                }
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
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginBySms(MainVo vo, String phone, String pcode, String imgCode, String imgCodeToken) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //公告标题
            String systemMsgTitle = "";
            //系统公告
            String systemMsg = "";
            //公告类型 1=系统维护 2=升级公告 3--普通公告
            int systemMsgType = 0;
            //维护开始、结束时间
            String systemMsgStartDate = "";
            String systemMsgEndDate   = "";
            String loginKey           = GloabConst.RedisHeaderKey.LOGIN_HEADER + phone;
            //检测手机号格式
          /*  if (!MobileUtils.isMobile(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
            }*/
//            publiceService.validateImgCode(imgCodeToken, imgCode);
            //验证
            if (redisUtil.hasKey(loginKey))
            {
                String rpcode = redisUtil.get(loginKey).toString();
                if (rpcode.equals(pcode))
                {
                    //用户是否存在
                    User userResult = new User();
                    userResult.setStore_id(vo.getStoreId());
                    userResult.setMobile(phone);
                    userResult = userBaseMapper.selectOne(userResult);
                    if (userResult == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
                    }

                    //登录成功流程
                    loginSuccess(userResult);
                    //移除验证码
                    redisUtil.del(loginKey);
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", 0);
                    parmaMap.put("startDate_lt", new Date());
                    parmaMap.put("endDate_gt", new Date());
                    parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
                    parmaMap.put("mch_tell", SystemTellModel.TELL.YES);
                    List<Map<String, Object>> systemList = systemTellModelMapper.selectDynamic(parmaMap);
                    if (systemList.size() > 0)
                    {
                        Map<String, Object> systemMap = systemList.get(0);
                        systemMsgTitle = MapUtils.getString(systemMap, "title");
                        systemMsg = MapUtils.getString(systemMap, "content");
                        systemMsgType = MapUtils.getIntValue(systemMap, "type");
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
                    resultMap.put("systemMsgEndDate", systemMsgEndDate);
                    resultMap.put("systemMsgStartDate", systemMsgStartDate);
                    resultMap.put("info", info);
                    resultMap.put("token", userResult.getMch_token());
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZXHQYZM, "请重新获取验证码");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("短信登录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginBySms");
        }
        return resultMap;
    }

    @Override
    public boolean sendSms(String mobile, int storeId) throws LaiKeAPIException
    {
        return publiceService.sendSms(storeId, mobile, GloabConst.VcodeCategory.TYPE_VERIFICATION, GloabConst.VcodeCategory.LOGIN_CODE, null);
    }

    @Override
    public boolean sendSmsForgetPwd(MainVo vo, String mobile) throws LaiKeAPIException
    {
        return publiceService.sendSms(vo.getStoreId(), mobile, GloabConst.VcodeCategory.TYPE_VERIFICATION, GloabConst.VcodeCategory.CURRENCY_CODE, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap = publiceService.getCode(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取验证码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCode");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> forgetPwd(ForgetPasswordVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
//            validateImgCode(vo.getAccessId(), vo.getImgCode());
            if (StringUtils.isEmpty(vo.getPwd()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRMM, "请输入密码");
            }
            if (StringUtils.isEmpty(vo.getRpwd()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZCQRMM, "请再次确认密码");
            }
            if (vo.getPwd().length() < 6 || vo.getPwd().length() > 16)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRWSDXMM, "请输入6-16位数的新密码");
            }
            if (!vo.getPwd().equals(vo.getRpwd()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMYYMMBXT, "两次输入密码不相同");
            }
            RedisDataTool.verificationPcode(GloabConst.VcodeCategory.CURRENCY_CODE, vo.getPhone(), vo.getPcode(), redisUtil);

            String pwd        = CryptoUtil.strEncode(vo.getPwd());
            User   loginParam = new User();
            loginParam.setStore_id(vo.getStoreId());
            loginParam.setZhanghao(vo.getPhone());
            User user = userBaseMapper.getUserByzhanghao(loginParam);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHBCZ, "账户不存在");
            }
            if (vo.getPwd().equals(CryptoUtil.strDecode(user.getMima())))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMYYMMXT, "新密码与原密码相同");
            }
            User userUpdate = new User();
            userUpdate.setId(user.getId());
            userUpdate.setMima(pwd);
            int count = userBaseMapper.updateByPrimaryKeySelective(userUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMXGSB, "密码修改失败");
            }
            String event = "管理员:" + user.getUser_id() + " 密码修改成功";
            //添加一条操作记录
            User userMchId = userBaseMapper.selectByPrimaryKey(user.getId());
            if (userMchId.getMchId() != null)
            {
                publiceService.addAdminRecord(userMchId.getStore_id(), userMchId.getUser_id(), event, AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_SHOP, userMchId.getMchId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改密码 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "forgetPwd");
        }
        return resultMap;
    }

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Override
    public Map<String, Object> storeLookMch(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (Objects.isNull(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "storeLookMch");
            }
            //系统配置
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (StringUtils.isEmpty(configModel.getPc_mch_path()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "商城未配置pc店铺地址");
            }
            MchModel mchModel = new MchModel();
            mchModel.setId(mchId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺信息不存在");
                }
                else
                {
                    if (mchModel.getRecovery().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺信息已被删除");
                    }
                    if (!mchModel.getReview_status().equals(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺信息暂未审核通过");
                    }
                }
            }
            if (mchModel.getIs_lock().equals(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK)))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "店铺已被自动注销,请联系管理员恢复");
            }
            User user = new User();
            user.setUser_id(mchModel.getUser_id());
            user = userBaseMapper.selectOne(user);
            if (user != null)
            {
                String              event = "商城管理员通过商城端访问店铺后台";
                Map<String, Object> map   = new HashMap<>(16);
                map.put("admin_id", user.getId());
                map.put("admin_name", user.getZhanghao());
                map.put("user_id", user.getUser_id());
                map.put("store_id", user.getStore_id());
                map.put("store_type", "pc");
                map.put("mch_id", mchModel.getId());
                map.put("login_time", new Date());
                //生成token
                String token    = JwtUtils.getTokenWithData(map);
                String tokenOld = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_STORE_MCH_FLAG + user.getUser_id()) + "";
                if (!StringUtils.isEmpty(tokenOld))
                {
                    redisUtil.del(tokenOld);
                }
                //获取登录生命周期
                Integer lifeTime = publicUserService.getUserLoginLife(user.getStore_id());

                //添加一条操作记录
                publiceService.addAdminRecord(user.getStore_id(), user.getUser_id(), event, AdminRecordModel.Type.LOGIN_OR_OUT, AdminRecordModel.Source.PC_SHOP, user.getMchId());
                //映射token 一个账号只能在一个地方登录
                redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN + token, JSON.toJSONString(user), lifeTime);
                redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_STORE_MCH_FLAG + user.getUser_id(), GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_STORE_TOKEN + token, lifeTime);
                //登陆成功
                resultMap.put("token", token);
                resultMap.put("pcMchPath", configModel.getPc_mch_path());
                resultMap.put("store_id", vo.getStoreId());
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DLSBYHBCZ, "登录失败,店铺账户不存在", "login");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商城查看店铺端所需参数 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeLookMch");
        }
    }


    /**
     * 验证图像验证码
     *
     * @param token -
     * @param code  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 17:08
     */
    private void validateImgCode(String token, String code) throws LaiKeAPIException
    {
        try
        {
            Object obj = redisUtil.get(token + code);
            if (obj != null)
            {
                redisUtil.del(token + code);
                return;
            }
        }
        catch (Exception e)
        {
            logger.error("验证图像验证码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validateImgCode");
        }
        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXYZMBZQ, "图形验证码不正确");
    }

    /**
     * 登陆成功后的统一操作
     *
     * @param loginUser - 引用对象
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/11 15:40
     */
    private void loginSuccess(User loginUser) throws LaiKeAPIException
    {
        try
        {
            String event = "登录店铺后台！";

            MchModel mchModel = new MchModel();
            mchModel.setStore_id(loginUser.getStore_id());
            mchModel.setUser_id(loginUser.getUser_id());
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                //没有则查看是否有权限
                String mchId = userAuthorityModelMapper.getMchIdByUserId(loginUser.getUser_id());
                if (StringUtils.isEmpty(mchId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
                mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
            }
            if (mchModel.getIs_lock().equals(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK)))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "店铺已被自动注销,请联系管理员恢复");
            }
            Map<String, Object> map = new HashMap<>(16);
            map.put("admin_id", loginUser.getId());
            map.put("admin_name", loginUser.getZhanghao());
            map.put("user_id", loginUser.getUser_id());
            map.put("store_id", loginUser.getStore_id());
            map.put("store_type", "pc");
            map.put("mch_id", mchModel.getId());
            map.put("login_time", new Date());
            //生成token
            String token = JwtUtils.getTokenWithData(map);
            //覆盖表中token
            userBaseMapper.resettingMchToken(loginUser.getStore_id(), token);
            User updateUser = new User();
            updateUser.setId(loginUser.getId());
            if (!StringUtils.isEmpty(loginUser.getClientid()))
            {
                updateUser.setClientid(loginUser.getClientid());
            }
            updateUser.setMch_token(token);
            int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                logger.info("修改登陆信息失败 参数:" + JSON.toJSONString(updateUser));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //刷新用户信息
            User userInfo = userBaseMapper.selectByPrimaryKey(loginUser.getId());
            //注入店铺id
            userInfo.setMchId(mchModel.getId());
            BeanUtils.copyProperties(userInfo, loginUser);
            String tokenOld = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH + loginUser.getUser_id()) + "";
            if (!StringUtils.isEmpty(tokenOld))
            {
                redisUtil.del(tokenOld);
            }
            //账号登录互斥 禅道34563要求不互斥
//            RedisDataTool.kickingUser(loginUser.getUser_id(), redisUtil);
            //获取登录生命周期
            Integer lifeTime = publicUserService.getUserLoginLife(loginUser.getStore_id());

            //添加一条操作记录
            publiceService.addAdminRecord(loginUser.getStore_id(), loginUser.getUser_id(), event, AdminRecordModel.Type.LOGIN_OR_OUT, AdminRecordModel.Source.PC_SHOP, loginUser.getMchId(), loginUser.getId());
            //映射token 一个账号只能在一个地方登录
            redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + token, JSON.toJSONString(loginUser), lifeTime);
            redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH + loginUser.getUser_id(), GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + token, lifeTime);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("登陆成功后的统一操作 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "login");
        }
    }

    @Override
    public void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(tell_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "markToRead");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicSystemTellService.markToRead(vo, user.getUser_id(), tell_id, false);
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
    public Map<String, Object> getUserTell(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            resultMap = publicSystemTellService.getUserTell(vo, "mch_tell");
            if (MapUtils.getIntValue(resultMap, "systemMsgType") == 1)
            {
/*                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN_MCH + vo.getAccessId());
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH + user.getUser_id());
                userBaseMapper.resettingMchToken(vo.getStoreId(), vo.getAccessId());*/
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取平台用户公告");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SYPTSPLCCC, "获取平台用户公告 出错", "goGroup");
        }
        return resultMap;
    }

    @Autowired
    private PublicSystemTellService publicSystemTellService;
}
