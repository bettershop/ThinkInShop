package com.laiketui.admins.admin.services;

import com.alibaba.fastjson2.JSON;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.laiketui.admins.api.admin.AdminUserService;
import com.laiketui.common.api.PublicSystemTellService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.UpdateAdminVo;
import com.laiketui.domain.vo.user.AdminColorVo;
import com.laiketui.domain.vo.user.AdminLoginVo;
import com.laiketui.root.license.Md5Util;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 商城后台登录
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
@Service
public class AdminUserServiceImpl implements AdminUserService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    public Map<String, Object> login(AdminLoginVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //商城默认语言
            String storeDefaultLang = null;
            if (StringUtils.isEmpty(vo.getUserName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZHMM, "请输入账号/密码");
            }
            else if (StringUtils.isEmpty(vo.getPwd()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZHMM, "请输入账号/密码");
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
            //后台登录商城 获取默认商城id
            Integer defaultStoreId = customerModelMapper.getDefaultStoreId();
            if (defaultStoreId != null)
            {
                vo.setStoreId(defaultStoreId);
            }
            if (StringUtils.isNotEmpty(vo.getImgCode()))
            {
                publiceService.validateImgCode(vo.getImgCodeToken(), vo.getImgCode());
            }
            AdminModel adminModel = new AdminModel();
            adminModel.setName(vo.getUserName());
            adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (StringUtils.isNotEmpty(vo.getCustomerNumber()))
            {
                //客户登录
                CustomerModel customerModel = new CustomerModel();
                customerModel.setCustomer_number(vo.getCustomerNumber());
                customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                customerModel = customerModelMapper.selectOne(customerModel);
                if (customerModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZQDSHBH, "请输入正确的商户编号");
                }
                adminModel.setStore_id(customerModel.getId());
            }
            else
            {
                //管理员登录
                adminModel.setStore_id(AdminModel.PLATFORM_STORE_ID);
            }
            adminModel = adminModelMapper.selectOne(adminModel);
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHBCZ, "账号不存在");
            }
            if (StringUtils.isEmpty(adminModel.getRole()))
            {
                logger.debug("商城id={} 管理员id={} 尝试登录,但是没有绑定角色,登录失败!", adminModel.getStore_id(), adminModel.getId());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
            }
            //还有几天过期
            Integer endDay = null;
            boolean haveStoreMchId = true;
            //非管理员
            if (adminModel.getType() != 0)
            {

                Integer storeMchId = customerModelMapper.getStoreMchId(adminModel.getStore_id());
                if (Objects.isNull(storeMchId) || storeMchId == 0)
                {
                    haveStoreMchId = false;
                }
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", 0);
                parmaMap.put("startDate_lt", new Date());
                parmaMap.put("endDate_gt", new Date());
                parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
                parmaMap.put("store_tell", SystemTellModel.TELL.YES);
                parmaMap.put("user_store_id", vo.getStoreId());
                parmaMap.put("store_type", GloabConst.StoreType.STORE_TYPE_PC_ADMIN);
                parmaMap.put("read_id", adminModel.getId());
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

                CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(adminModel.getStore_id());
                if (customerModel != null)
                {
                    if (customerModel.getStatus().equals(CustomerModel.CUSTOMER_STATUS_EXPIRE))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDSQYDQQLXGLYZSYXX, "您的授权已到期,请联系管理员再使用,谢谢");
                    }
                    else if (customerModel.getStatus().equals(CustomerModel.CUSTOMER_STATUS_LOCK))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDSCYSDQLXGLYZSYXX, "您的商城已锁定,请联系管理员再使用,谢谢");
                    }
                    //到期时间
                    Date endDate = customerModel.getEnd_date();
                    if (endDate != null)
                    {
                        //提前一个星期提醒客户
                        Date dingDate = DateUtil.getAddDate(endDate, -7);
                        if (!DateUtil.dateCompare(endDate, new Date()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDSQYDQQLXKFWCXFZSYXX, "您的授权已到期,请联系客服完成续费再使用,谢谢");
                        }
                        if (DateUtil.dateCompare(new Date(), dingDate))
                        {
                            //提醒客户商城要过期了
                            endDay = Integer.parseInt(DateUtil.dateConversion(endDate.getTime() / 1000 - System.currentTimeMillis() / 1000, DateUtil.TimeType.DAY) + "");
                        }
                    }
                    ConfigModel configModel = new ConfigModel();
                    configModel.setStore_id(customerModel.getId());
                    configModel = configModelMapper.selectOne(configModel);
                    //踢人规则
                    if (configModel != null)
                    {
                        if (configModel.getIs_Kicking().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                        {
                            String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId();
                            String tokenOld = redisUtil.get(logKey) + "";
                            if (StringUtils.isNotEmpty(tokenOld))
                            {
                                //踢人
                                logger.info("【{}】已在其它地方登录", adminModel.getName());
                                redisUtil.del(logKey);
                                redisUtil.del(tokenOld);
                            }
                        }
                        storeDefaultLang = configModel.getDefault_lang_code();
                    }
                }
            }
            resultMap.put("haveStoreMchId",haveStoreMchId);
            //登录次数限制
            Integer loginNum = 3;
            if (adminModel.getPassword().equals(Md5Util.MD5endoce(vo.getPwd())))
            {
                if (adminModel.getLogin_num() >= loginNum)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHYSDQLXKF, "账号已锁定,请联系客服");
                }
                if (adminModel.getStatus().equals(AdminModel.STATUS_DISABLE))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHYBJYRYYWQYSCGLYLX, "账号已被禁用!若有疑问,请与商城管理员联系");
                }
                //生成token
                Map<String, Object> tokenMap = new HashMap<>(16);
                tokenMap.put("admin", adminModel.getId());
                tokenMap.put("exp", new Date());
                String token = JwtUtils.getToken(tokenMap, GloabConst.LktConfig.LOGIN_EXISTENCE_TIME);
                //登录成功
                AdminModel adminUpdate = new AdminModel();
                adminUpdate.setId(adminModel.getId());
                adminUpdate.setToken(token);
                adminUpdate.setLogin_num(0);
                adminUpdate.setStatus(AdminModel.STATUS_OPEN);
                adminModelMapper.updateByPrimaryKeySelective(adminUpdate);
            }
            else
            {
                //登录失败 禅道49186 商城的管理员不会被锁，和平台管理员一样
                if (StringUtils.isNotEmpty(vo.getCustomerNumber()) && adminModel.getType() != AdminModel.TYPE_CLIENT)
                {
                    if (adminModel.getType() != AdminModel.TYPE_SYSTEM_ADMIN)
                    {
                        adminModelMapper.adminLoginFail(adminModel.getId());
                    }
                    if (adminModel.getLogin_num() + 1 >= 3)
                    {
                        //三次输入错误锁定账户
                        AdminModel adminUpdate = new AdminModel();
                        adminUpdate.setId(adminModel.getId());
                        adminUpdate.setStatus(1);
                        adminModelMapper.updateByPrimaryKeySelective(adminUpdate);
                        //禅道bug  46041 不锁商城
//                        if (adminModel.getType() != AdminModel.TYPE_SYSTEM_ADMIN) {
//                            //如果是客户,锁定商城
//                            CustomerModel customerUpdate = new CustomerModel();
//                            customerUpdate.setId(adminModel.getStore_id());
//                            customerUpdate.setStatus(CustomerModel.CUSTOMER_STATUS_LOCK);
//                            customerModelMapper.updateByPrimaryKeySelective(customerUpdate);
//                        }
                        throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_ZHYSDQLXKF, "账号已锁定,请联系客服");
                    }
                }
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(vo.getStoreId());
                recordModel.setUser_id(vo.getUserName());
                recordModel.setEvent("登录密码错误");
                recordModelMapper.insertSelective(recordModel);
                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_MMCWQZXSR, "密码错误,请重新输入");
            }
            adminModel = adminModelMapper.selectByPrimaryKey(adminModel);
            //之前用户是否登录,如果登录则删除缓存
/*            String tokenOld = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + adminModel.getId()) + "";
            if (!StringUtils.isEmpty(tokenOld)) {
                redisUtil.del(tokenOld);
            }*/
            if (adminModel.getShop_id() == null || adminModel.getShop_id() == 0)
            {
                //获取当前商城自营店
                Integer mchId;
                if (adminModel.getType().equals(0))
                {
                    mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                }
                else
                {
                    mchId = customerModelMapper.getStoreMchId(adminModel.getStore_id());
                }
                if (mchId != null)
                {
                    adminModel.setShop_id(mchId);
                }
            }
            if (systemMsgType != 1)
            {
                //映射token 用于单点登录
                redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + adminModel.getToken(), JSON.toJSONString(adminModel), GloabConst.LktConfig.LOGIN_EXISTENCE_TIME);
                redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId(), GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + adminModel.getToken(), GloabConst.LktConfig.LOGIN_EXISTENCE_TIME);
            }
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(vo.getStoreId());
            recordModel.setUser_id(adminModel.getId().toString());
            recordModel.setEvent("登录成功");
            recordModelMapper.insertSelective(recordModel);

            String info = "登录成功";
            if (endDay != null)
            {
                String dingStr = String.format("您的账号还有%s天到期,请及时续费!", endDay);
                if (endDay == 0)
                {
                    dingStr = "您的账号将在今日到期,请及时续费";
                }
                info += dingStr;
            }
            else if (systemMsgType == 1)
            {
                info = "系统维护中";
            }
            publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), adminModel.getName() + info, AdminRecordModel.Type.LOGIN_OR_OUT, AdminRecordModel.Source.PC_PLATFORM, -1, adminModel.getId());

            resultMap.put("status", 1);
            resultMap.put("info", info);
            resultMap.put("name", adminModel.getName());
            resultMap.put("token", adminModel.getToken());
            resultMap.put("mchId", adminModel.getShop_id());

            if (adminModel.getColor() != null && !adminModel.getColor().isEmpty())
            {
                //如果有颜色则使用颜色
                resultMap.put("color", adminModel.getColor());
            }
            else
            {
                //如果没有颜色则使用默认颜色 蓝色
                resultMap.put("color", "{\"name\":\"topNav.Blue\",\"icon\":0,\"color\":\"#2d6dcc\",\"key\":0}");
            }
            if (adminModel.getType() == AdminModel.TYPE_SYSTEM_ADMIN)
            {
                resultMap.put("storeId", defaultStoreId);
            }
            else
            {
                resultMap.put("storeId", adminModel.getStore_id());
            }

            resultMap.put("portrait", adminModel.getPortrait());
            resultMap.put("nickname", adminModel.getNickname());
            resultMap.put("birthday", adminModel.getBirthday());
            resultMap.put("sex", adminModel.getSex());
            resultMap.put("phone", adminModel.getTel());
            resultMap.put("type", adminModel.getType());
            resultMap.put("role", adminModel.getRole());

            String defaultLang = adminModel.getLang();
            if (!StringUtils.isEmpty(defaultLang))
            {
                resultMap.put("lang", defaultLang);
            }
            else if (!StringUtils.isEmpty(storeDefaultLang))
            {
                resultMap.put("lang", storeDefaultLang);
            }
            else
            {
                resultMap.put("lang", GloabConst.Lang.CN);
            }

            //系统公共
            //默认货币获取登录用户的storeid 当登录进来的是超级管理员则需要使用默认商城的货币设置信息
            Map storeCurrencyResult = null;
            if (adminModel.getStore_id() == 0)
            {
                storeCurrencyResult = currencyStoreModelMapper.getDefaultCurrency(defaultStoreId);
            }
            else
            {
                storeCurrencyResult = currencyStoreModelMapper.getDefaultCurrency(adminModel.getStore_id());
            }

            if (storeCurrencyResult != null)
            {
                resultMap.put("storeDefaultCurrencyInfo", storeCurrencyResult);
            }

            resultMap.put("systemMsgTitle", systemMsgTitle);
            resultMap.put("systemMsg", systemMsg);
            resultMap.put("systemMsgType", systemMsgType);
            resultMap.put("tell_id", tell_id);
            resultMap.put("systemMsgEndDate", systemMsgEndDate);
            resultMap.put("systemMsgStartDate", systemMsgStartDate);
            resultMap.put("cpc", adminModel.getCpc());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("后台登录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "login");
        }
        return resultMap;
    }

    @Override
    public void logout(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退出登录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "logout");
        }
    }

    @Override
    public Map<String, Object> setUserAdmin(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (adminModel.getType() == AdminModel.TYPE_SYSTEM_ADMIN)
            {
                if (vo.getStoreId() == 0)
                {
                    return resultMap;
                }
                //只有系统管理员才执行
                CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(vo.getStoreId());
                if (customerModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
                }
                AdminModel storeAdmin = adminModelMapper.selectByPrimaryKey(customerModel.getAdmin_id());
                if (storeAdmin == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCMYGLY, "商城没有管理员");
                }
                adminModel.setStore_id(storeAdmin.getStore_id());
                adminModel.setShop_id(storeAdmin.getShop_id());
                //刷新缓存
                RedisDataTool.refreshRedisAdminCache(vo.getAccessId(), adminModel, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, redisUtil);
            }
            resultMap.put("mchId", adminModel.getShop_id());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("赋予管理员商城信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setUserAdmin");
        }
        return resultMap;
    }

    @Override
    public void updateAdminInfo(UpdateAdminVo vo) throws LaiKeAPIException
    {
        try
        {
            //是否修改了密码,修改了密码则退出登录
            boolean    isUpdatePwd = false;
            AdminModel adminModel  = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            adminModel = adminModelMapper.selectByPrimaryKey(adminModel.getId());

            AdminModel adminUpdate = new AdminModel();
            adminUpdate.setId(adminModel.getId());
            adminUpdate.setNickname(vo.getNickname());
            adminUpdate.setPortrait(vo.getPortrait());
            adminUpdate.setBirthday(vo.getBirthday());
            adminUpdate.setSex(vo.getSex());
            adminUpdate.setTel(vo.getPhone());
            adminUpdate.setCpc(vo.getCpc());

           /* if (StringUtils.isNotEmpty(adminUpdate.getTel()) && !MobileUtils.isMobile(adminUpdate.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
            }*/
            if (StringUtils.isNotEmpty(vo.getPasswordOld()) && StringUtils.isNotEmpty(vo.getPassword()))
            {
                Integer less = 6;
                Integer many = 20;
                if (DataCheckTool.checkLength(adminModel.getPassword(), less, many))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYMMZDW, "管理员密码长度为 6-20");
                }
                String pwd = Md5Util.MD5endoce(vo.getPasswordOld());
                if (!adminModel.getPassword().equals(pwd))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMMBZQ, "原密码不正确");
                }
                pwd = Md5Util.MD5endoce(vo.getPassword());
                if (adminModel.getPassword().equals(pwd))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMYJMMXT, "原密与旧密码相同");
                }
                adminUpdate.setPassword(pwd);
                isUpdatePwd = true;
            }

            int row = adminModelMapper.updateByPrimaryKeySelective(adminUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //修改密码后退出登录
            String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId();
            String tokenOld = redisUtil.get(logKey) + "";
            if (StringUtils.isNotEmpty(tokenOld) && isUpdatePwd)
            {
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), adminModel.getName() + "修改了密码", AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM);
                //踢人
                logger.info("adminId:{}修改了密码,强制退出系统", adminModel.getId());
                redisUtil.del(logKey);
                redisUtil.del(tokenOld);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改基本信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateAdminInfo");
        }
    }

    @Override
    public void updateLanguageByUser(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            AdminModel admin      = new AdminModel();
            if (StringUtils.isEmpty(vo.getLanguage()))
            {
                admin.setLang(GloabConst.Lang.CN);
            }
            else
            {
                admin.setLang(vo.getLanguage());
            }
            admin.setId(adminModel.getId());
            adminModelMapper.updateByPrimaryKeySelective(admin);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置管理用户系统语言 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateLanguageByUser");
        }
    }

    @Override
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
    public void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(tell_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "markToRead");
            }
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicSystemTellService.markToRead(vo, adminModel.getId().toString(), tell_id, false);
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
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //公告类型 1=系统维护 2=升级公告 3--普通公告
            int systemMsgType = 0;
            resultMap.put("systemMsgType", systemMsgType);
            if (!adminModel.getType().equals(AdminModel.TYPE_SYSTEM_ADMIN))
            {
                resultMap = publicSystemTellService.getUserTell(vo, "store_tell");
            }
            //清除当前用户登录信息
            if (MapUtils.getIntValue(resultMap, "systemMsgType") == 1 && !adminModel.getType().equals(AdminModel.TYPE_SYSTEM_ADMIN))
            {

/*                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + vo.getAccessId());
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId());
                adminModel.setToken("");
                adminModelMapper.updateByPrimaryKeySelective(adminModel);*/
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdminColor(AdminColorVo vo) throws LaiKeAPIException
    {
        try
        {
//            // 参数验证
//            if (!vo.validate()) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZHYS, "请提供管理员名称和颜色");
//            }

            // 检查颜色是否合法（可选，根据实际需求添加）
//            validateColor(vo.getColor());

            // 更新颜色
            int affectedRows = adminModelMapper.updateColorByName(vo.getName(), vo.getColor());

            if (affectedRows == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHBCZ, "未找到该管理员账号");
            }

            logger.info("管理员【{}】成功修改商城样式颜色为【{}】", vo.getName(), vo.getColor());
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("修改管理员商城样式颜色异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateAdminColor");
        }
    }

    // 颜色合法性验证 250701目前未使用
    private void validateColor(String color) throws LaiKeAPIException
    {
        // 可根据实际需求扩展合法颜色列表
        String[] validColors = {"red", "blue", "green", "yellow", "purple", "orange", "pink", "black", "white", "gray"};
        boolean  isValid      = false;

        for (String validColor : validColors)
        {
            if (validColor.equals(color))
            {
                isValid = true;
                break;
            }
        }

//        if (!isValid) {
//            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SYCWSZ, "颜色值不合法，请使用英文单词表示（如red、blue）");
//        }
    }

    @Autowired
    private PublicSystemTellService publicSystemTellService;

    @Autowired
    private DefaultKaptcha defaultKaptcha;
}

