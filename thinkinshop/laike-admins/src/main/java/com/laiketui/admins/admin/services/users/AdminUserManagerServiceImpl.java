package com.laiketui.admins.admin.services.users;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.users.AdminUserManagerService;
import com.laiketui.common.api.PublicAddressService;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.file.FileDeliveryModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.UserCollectionModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.user.UserRuleModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.user.AddUserVo;
import com.laiketui.domain.vo.user.UpdateUserVo;
import com.laiketui.domain.vo.user.UserVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 会员管理
 *
 * @author Trick
 * @date 2021/1/7 10:58
 */
@Service
public class AdminUserManagerServiceImpl implements AdminUserManagerService
{
    private final Logger logger = LoggerFactory.getLogger(AdminUserManagerServiceImpl.class);

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicAddressService publicAddressService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private FileDeliveryModelMapper fileDeliveryModelMapper;

    @Override
    public Map<String, Object> getUserInfo(UserVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User queryUser = new User();
            queryUser.setStore_id(vo.getStoreId());
            queryUser.setUser_id(vo.getUid());
            queryUser.setGrade(vo.getGrade());
            queryUser.setIs_out(StringUtils.toString(vo.getIsOverdue()));
            queryUser.setSource(StringUtils.toString(vo.getSource()));
            Map<String, Object> parmaMap = JSON.parseObject(JSON.toJSONString(queryUser), new TypeReference<Map<String, Object>>()
            {
            });
            if (StringUtils.isNotEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            if (StringUtils.isNotEmpty(vo.getCpc()))
            {
                parmaMap.put("cpc", vo.getCpc());
            }
            //获取当前自营店
            String storeUserId = customerModelMapper.getStoreUserId(vo.getStoreId());

            parmaMap.put("userName", vo.getUname());
            parmaMap.put("tel", vo.getTel());
            parmaMap.put("Register_data_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("isStore", "isStore");
            parmaMap.put("vague", vo.getVague());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> userList = userBaseMapper.selectDynamic(parmaMap);
            int                       total    = userBaseMapper.countDynamic(parmaMap);
            boolean maskMobile = vo.getExportType() == null || vo.getExportType() != 1;
            for (Map<String, Object> map : userList)
            {
                map.put("user_name", map.get("user_name"));
                //用户id
                String userId = String.valueOf(map.get("user_id"));
                //用户的等级id
                int levelId = StringUtils.stringParseInt(String.valueOf(map.get("grade")));
                //订单类型
                String orderType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;

                //获取用户消费金额-->包含退款金额
                BigDecimal orderPrice = orderModelMapper.sumUserOrderPrice(vo.getStoreId(), userId);
                //用户以退款金额
//                BigDecimal refundPrice = orderModelMapper.sumUserOrderRefundPrice(vo.getStoreId(), userId);
                //用户实际消费金额
                map.put("z_price", orderPrice);
                //获取用户有效订单数量
                int orderNum = orderModelMapper.countUserEffectiveOrdernoNum(vo.getStoreId(), userId);
                map.put("z_num", orderNum);

                String registerDate = DateUtil.dateFormate(MapUtils.getString(map, "Register_data"), GloabConst.TimePattern.YMDHMS);
                String gradeEndDate = "暂无";
                if (map.containsKey("grade_end"))
                {
                    gradeEndDate = DateUtil.dateFormate(MapUtils.getString(map, "grade_end"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("Register_data", registerDate);
                map.put("grade_end", gradeEndDate);

                //获取用户等级
//                String levelName = "普通会员";
//                String gradeDiscount = "暂无折扣";
//                UserGradeModel userGradeModel = new UserGradeModel();
//                userGradeModel.setStore_id(vo.getStoreId());
//                userGradeModel.setId(levelId);
//                userGradeModel = userGradeModelMapper.selectOne(userGradeModel);
//                if (userGradeModel != null) {
//                    levelName = userGradeModel.getName();
//                    //获取会员折扣
//                    BigDecimal vipDic = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(orderType, userId, vo.getStoreId()));
//                    if (new BigDecimal("1").compareTo(vipDic) != 0) {
//                        gradeDiscount = vipDic.toString();
//                    }
//                }
//                map.put("grade", levelName);
//                map.put("gradeDiscount", gradeDiscount);

                String birthday = "";
                if (map.containsKey("birthday"))
                {
                    birthday = DateUtil.dateFormate(MapUtils.getString(map, "birthday"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("birthday", birthday);
                String clientId = MapUtils.getString(map, "clientid");
                map.put("clientId", clientId);

                //是否有密码
                User    user     = userBaseMapper.selectByPrimaryKey(MapUtils.getString(map, "id"));
                boolean loginPwd = StringUtils.isNotEmpty(user.getMima());
                map.put("loginPwd", loginPwd);
                boolean isPaymentPwd = StringUtils.isNotEmpty(user.getPassword());
                map.put("isPaymentPwd", isPaymentPwd);

                //判断店铺是否是该用户的
                int isMch = 0;
                int count = mchModelMapper.countMchIsByUser(vo.getStoreId(), userId);
                if (count > 0)
                {
                    isMch = 1;
                }
                map.put("is_mch", isMch);
                //是否有删除按钮
                map.put("isDelBtn", !userId.equals(storeUserId));

                String mobile = MapUtils.getString(map, "mobile");
                if (StringUtils.isEmpty(mobile))
                {
                    map.put("cpc",null);
                }
                else if (maskMobile)
                {
                    map.put("mobile", StringUtils.desensitizedPhoneNumber(mobile));
                }

                map.put("last_time", MapUtils.getString(map, "last_time"));
                //获取当前用户默认地址
                UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), userId, null);
                map.put("userAddress", userAddress);

                //货币处理
                Integer userCurrency = MapUtils.getInteger(map, "preferred_currency");
                if (userCurrency != null)
                {
                    Map userCurrencyInfoMap = currencyStoreModelMapper.getCurrencyInfo(vo.getStoreId(), userCurrency);
                    if (userCurrencyInfoMap != null)
                    {
                        map.put("currency_code", MapUtils.getString(userCurrencyInfoMap, "currency_code"));
                        map.put("exchange_rate", MapUtils.getDouble(userCurrencyInfoMap, "exchange_rate"));
                        map.put("currency_symbol", MapUtils.getString(userCurrencyInfoMap, "currency_symbol"));
                    }
                }

            }
            if (vo.getExportType() == 1)
            {
                exportUserList(userList, response);
                return null;
            }


            resultMap.put("total", total);
            resultMap.put("list", userList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载会员列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserInfo");
        }
        return resultMap;
    }

    private void exportUserList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户ID", "用户头像", "用户名称", "用户账号", "区号", "手机号码", "邮箱", "账户余额", "积分余额", "账号来源", "有效订单数", "交易金额", "注册时间"};
            //对应字段
            String[] kayList = new String[]{"user_id", "headimgurl", "user_name", "zhanghao", "cpc", "mobile", "e_mail", "money", "score", "sourceName", "z_num", "z_price", "Register_data"};
            EasyPoiExcelUtil.excelExport("用户列表", headerList, kayList, goodsList, response);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出用户数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserById(UpdateUserVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (StringUtils.isEmpty(vo.getUserId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "userid不能为空", "updateUserById");
            }
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setUser_id(vo.getUserId());
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "updateUserById");
            }
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setUser_name(vo.getUname());
            updateUser.setMobile(vo.getPhone());
            updateUser.setMima(vo.getPwd());
            updateUser.setPassword(vo.getPaypwd());
            updateUser.setScore(vo.getJifen());
            updateUser.setSex(vo.getSex());
            if (Objects.nonNull(vo.getCpc()))
            {
                updateUser.setCpc(vo.getCpc());
            }
            if (StringUtils.isNotEmpty(vo.getEmail()))
            {
                count = userBaseMapper.getEmailCount(vo.getEmail(), vo.getStoreId(), user.getUser_id());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXYCZ, "邮箱地址已存在");
                }
                updateUser.setE_mail(vo.getEmail());
            }
            if (vo.getMoney().compareTo(new BigDecimal("9999999999.99")) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEYCGSX, "余额已超过上限");
            }
            updateUser.setMoney(vo.getMoney());
            updateUser.setBirthday(DateUtil.dateFormateToDate(vo.getBirthday(), GloabConst.TimePattern.YMDHMS));
            if (vo.getHeaderUrl() != null)
            {
                updateUser.setHeadimgurl(vo.getHeaderUrl());
            }
            //验证数据
            updateUser = DataCheckTool.checkUserDataFormate(updateUser);
            if (updateUser.equals(new User()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSBCSCW, "修改失败,参数错误", "updateUserById");
            }
            if (StringUtils.isNotEmpty(vo.getUname()) && !user.getUser_name().equals(vo.getUname()))
            {
                if (!DataCheckTool.checkLength(user.getUser_name(), 0, 16))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHMGSBZQ, "用户名格式不正确");
                }
            }
            if (StringUtils.isNotEmpty(vo.getPhone()))
            {
                int flag = userBaseMapper.getPhoneCountByUserId(vo.getStoreId(), vo.getPhone(), vo.getCpc(), user.getId());
                if (flag > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHMYCZ, "手机号码已存在");
                }

                flag = userBaseMapper.validataPhoneOrNoIsRegister(vo.getStoreId(), vo.getPhone(), user.getId());
                if (flag > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSJHMYJZC, "手机号已被注册");
                }
            }

            // 检查区号是否合法
            String countryCode = vo.getCpc();
            if (countryCode == null || countryCode.trim().isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHBHF, "区号不能为空", "updateUserById");
            }

            CountryModel countryModel = countryModelMapper.selectByCode2(countryCode.trim());
            if (countryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHBHF,
                        String.format("区号 [%s] 不合法", countryCode), "updateUserById");
            }

            count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败", "updateUserById");
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了用户ID：" + vo.getUserId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改用户资料 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateUserById");
        }
    }

    @Override
    public Map<String, Object> getUserGradeType(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            UserRuleModel             userRuleModel = userRuleModelMapper.getUserRule(vo.getStoreId());
            List<Map<String, Object>> resultList    = new ArrayList<>();
            if (userRuleModel != null)
            {
                String   gradeType = userRuleModel.getMethod();
                String[] list      = gradeType.split(SplitUtils.DH);
                for (String type : list)
                {
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("value", type);
                    switch (type)
                    {
                        case "1":
                            map.put("label", "包月");
                            break;
                        case "2":
                            map.put("label", "包季");
                            break;
                        case "3":
                            map.put("label", "包年");
                            break;
                        default:
                            break;
                    }
                    resultList.add(map);
                }
            }

            resultMap.put("gradeType", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取会员配置升级方式 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserGradeType");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userRechargeMoney(MainVo vo, int id, BigDecimal money, Integer type, String remake) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (type == null)
            {
                type = 1;
            }
            //获取用户信息
            User   user  = userBaseMapper.selectByPrimaryKey(id);
            String event = "";
            //添加操作日志
            if (type == 1)
            {
                event = "将用户ID：" + user.getUser_id() + "充值了：" + money + "余额";
            }
            else
            {
                event = "将用户ID：" + user.getUser_id() + "充值了：" + money + "积分";
            }

            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return publicUserService.userRechargeMoney(vo.getStoreId(), id, money, type, null, remake);


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员充值余额 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "userRechargeMoney");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delUserById(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取用户信息
            User user = userBaseMapper.selectByPrimaryKey(id);
            if (user == null)
            {
                logger.error("用户删除失败:用户不存在");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "delUserById");
            }
            if (user.getMoney() != null && user.getMoney().doubleValue() > 0)
            {
                logger.error("用户删除失败:该用户有余额未使用");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSBGYHYYEWSY, "删除失败:该用户有余额未使用", "delUserById");
            }
            int count = orderModelMapper.countUserUnfinishedOrder(vo.getStoreId(), user.getUser_id());
            if (count > 0)
            {
                logger.error("用户删除失败:该用户有未完成的订单");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSBGYHYWWCDDD, "删除失败:该用户有未完成的订单", "delUserById");
            }
            //判断店铺是否未注销
            MchModel mchModel = new MchModel();
            mchModel.setUser_id(user.getUser_id());
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null && DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString().equals(mchModel.getReview_status()))
            {
                logger.error("用户删除失败:该用户有未注销的店铺");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSBGYHYWZXDDP, "删除失败:该用户有未注销的店铺", "delUserById");
            }
            //判断该用户是不是分销员
            count = userDistributionModelMapper.counUserDistribution(vo.getStoreId(), user.getUser_id());
            if (count > 0)
            {
                //用户是否是分销员并且有分销等级
                count = userDistributionModelMapper.countUserDistributionAndHaveLevel(vo.getStoreId(), user.getUser_id());
                if (count > 0)
                {
                    logger.error("用户删除失败:该用户有分销身份");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSBGYHYFXSF, "删除失败:该用户有分销身份", "delUserById");
                }
                //没有分销等级可以删除
                UserDistributionModel userDistributionModel = new UserDistributionModel();
                userDistributionModel.setStore_id(vo.getStoreId());
                userDistributionModel.setUser_id(user.getUser_id());
                userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                if (userDistributionModel != null)
                {
                    count = userDistributionModelMapper.deleteByPrimaryKey(userDistributionModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXDJSCSB, "分销等级删除失败");
                    }
                }
            }

            //删除用户
            count = userBaseMapper.deleteByPrimaryKey(id);

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了用户ID：" + user.getUser_id() + " 的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            if (count < 1)
            {
                logger.error("用户删除失败:用户删除失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delUserById");
            }
            //删除用户订单
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setUser_id(user.getUser_id());
            count = orderModelMapper.delete(orderModel);
            logger.error("删除订单{}个 userid={}", count, user.getUser_id());

            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(vo.getStoreId());
            orderDetailsModel.setUser_id(user.getUser_id());
            count = orderDetailsModelMapper.delete(orderDetailsModel);
            logger.error("订单明细删除{}个 userid={}", count, user.getUser_id());
            //删除用户地址
            UserAddress userAddress = new UserAddress();
            userAddress.setStore_id(vo.getStoreId());
            userAddress.setUid(user.getUser_id());
            count = userAddressMapper.delete(userAddress);
            logger.error("用户地址删除{}个 userid={}", count, user.getUser_id());
            //删除用户收藏历史
            UserCollectionModel userCollectionModel = new UserCollectionModel();
            userCollectionModel.setStore_id(vo.getStoreId());
            userCollectionModel.setUser_id(user.getUser_id());
            count = userCollectionModelMapper.delete(userCollectionModel);
            logger.error("用户历史删除{}个 userid={}", count, user.getUser_id());

            //删除用户店铺信息
            MchModel mchDel = new MchModel();
            mchDel.setId(mchDel.getId());
            mchDel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
            count = mchModelMapper.updateByPrimaryKeySelective(mchDel);
            logger.error("用户店铺删除{}个 userid={}", count, user.getUser_id());

            return true;
        }
        catch (LaiKeAPIException l)
        {
            logger.error(l.getMessage());
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除用户 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delUserById");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(AddUserVo vo) throws LaiKeAPIException
    {
        try
        {
            addUser(vo);
        }
        catch (LaiKeAPIException l)
        {
            logger.error(l.getMessage());
            throw l;
        }
        catch (Exception e)
        {
            logger.error("后台添加会员 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delUserById");
        }
    }

    /**
     * 添加用户公共方法
     * @param vo
     */
    private void addUser(AddUserVo vo)
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(vo.getUserName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHMBNWK, "用户名不能为空");
            }
            else if (StringUtils.isEmpty(vo.getPhone()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机号不能为空");
            }
            else if (StringUtils.isEmpty(vo.getMima()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMBNWK, "密码不能为空");
            }
            // 检查区号是否合法
            String countryCode = vo.getCpc();
            if (countryCode == null || countryCode.trim().isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHBHF, "区号不能为空", "updateUserById");
            }

            CountryModel countryModel = countryModelMapper.selectByCode2(countryCode.trim());
            if (countryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHBHF,
                        String.format("区号 [%s] 不合法", countryCode), "updateUserById");
            }
            User saveUser = new User();
            saveUser.setStore_id(vo.getStoreId());
            saveUser.setUser_name(vo.getUserName());
            saveUser.setSource(String.valueOf(vo.getSource()));
            saveUser.setZhanghao(vo.getZhanghao());
            saveUser.setMobile(vo.getPhone());
            saveUser.setGrade(vo.getGrade());
            saveUser.setMima(vo.getMima());
            saveUser.setHeadimgurl(vo.getHeaderUrl());
            if (StringUtils.isNotEmpty(vo.getCpc()))
            {
                saveUser.setCpc(vo.getCpc());
                saveUser.setCountry_num(vo.getCountry_num());
            }
            if (StringUtils.isNotEmpty(vo.getE_mail()))
            {
                int count = userBaseMapper.emailCount(vo.getE_mail(),vo.getStoreId());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXYCZ,"邮箱已存在");
                }
                saveUser.setE_mail(vo.getE_mail());
            }
            //验证数据
            saveUser = DataCheckTool.checkUserDataFormate(saveUser);
            //账号唯一 手机号唯一
            if (userBaseMapper.validataUserPhoneOrNoIsRegister(vo.getStoreId(), saveUser.getZhanghao()) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHYCZ, "账号" + saveUser.getZhanghao() + "已存在");
            }
            if (userBaseMapper.getPhoneCount(vo.getStoreId(), saveUser.getMobile(),vo.getCpc()) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHMYCZ, "手机号码" + saveUser.getMobile() + "已存在");
            }
            if (StringUtils.isEmpty(vo.getBirthday()) && StringUtils.isEmpty(vo.getSex()))
            {
                //使用的默认用户配置   性别未知  生日当前时间
                saveUser.setSex(0);
                //saveUser.setBirthday(new Date());
                saveUser.setIs_default_value(1);
                saveUser.setIs_default_birthday(1);
            }
            else
            {
                saveUser.setIs_default_value(1);
                saveUser.setIs_default_birthday(1);
                saveUser.setSex(vo.getSex());
                saveUser.setBirthday(DateUtil.dateFormateToDate(vo.getBirthday(), GloabConst.TimePattern.YMD));
            }
            //生成userId
            String userId = "U";
            //获取配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            {
//                saveUser.setWx_name(configModel.getWx_name());
//                if (StringUtils.isEmpty(saveUser.getHeadimgurl())) {
//                    saveUser.setHeadimgurl(publiceService.getImgPath(configModel.getWx_headimgurl(), vo.getStoreId()));
//                }
                if (StringUtils.isNotEmpty(configModel.getUser_id()))
                {
                    userId = configModel.getUser_id();
                }
            }
            //获取商城自营店用户
            String zyUserId = customerModelMapper.getStoreUserId(vo.getStoreId());
            User   zyUser   = new User();
            zyUser.setStore_id(vo.getStoreId());
            zyUser.setUser_id(zyUserId);
            User user = userBaseMapper.selectOne(zyUser);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXCJZYD, "请先创建自营店");
            }
            saveUser.setReferee(user.getUser_id());

            //默认设置为商城默认币种
            Map storeCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
            saveUser.setPreferred_currency(MapUtils.getInteger(storeCurrencyMap, "currency_id"));
            userBaseMapper.insertSelective(saveUser);
            User userParameter = userBaseMapper.selectByPrimaryKey(saveUser.getId());
            userParameter.setUser_id(userId + (saveUser.getId() + 1));
            int count = userBaseMapper.updateByPrimaryKeySelective(userParameter);
            if (count < 1)
            {
                logger.error(saveUser.getId() + ": userid生成失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZCSBQLXGLY, "注册失败,请联系管理员");
            }
            //获取分销设置
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            //是否是注册绑定永久关系
            if (distributionConfigModel != null && distributionConfigModel.getRelationship().equals(DistributionConfigModel.RelationshipType.PLUGIN_OPEN))
            {
                logger.info("分销商注册绑定永久关系 创建分销商={}和上级绑定永久关系上级={}", userParameter.getUser_id(), user.getUser_id());
                publiceDistributionService.createLevel(vo.getStoreId(), userParameter.getUser_id(), 0, user.getUser_id());
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "添加了用户ID：" + userId + (saveUser.getId() + 1), AdminRecordModel.Type.ADD, vo.getAccessId());

        }
        catch (LaiKeAPIException l)
        {
            logger.error(l.getMessage());
            throw l;
        }
        catch (Exception e)
        {
            logger.error("后台添加会员 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delUserById");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadAddUser(MainVo vo, List<MultipartFile> image) throws LaiKeAPIException
    {
        boolean flag = true;
        try
        {
            AdminModel      user            = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ExcelAnalysisVo excelAnalysisVo = new ExcelAnalysisVo();
            excelAnalysisVo.setFile(image);
            List<String> titleNames = new ArrayList<>();
            titleNames.add("用户密码");
            titleNames.add("手机号码");
            titleNames.add("账号来源");
            titleNames.add("区号");
            titleNames.add("国家代码");
            excelAnalysisVo.setTitleName(titleNames);
            List<String> valueNames = new ArrayList<>();
            valueNames.add("password");
            valueNames.add("phone");
            valueNames.add("source");
            valueNames.add("cpc");
            valueNames.add("country_num");
            excelAnalysisVo.setValueKey(valueNames);
            Map<String, Object>       resultMap     = EasyPoiExcelUtil.analysisExcel(excelAnalysisVo);
            List<Map<String, Object>> excelDataList = DataUtils.cast(resultMap.get("list"));

            if (excelDataList != null)
            {
                List<AddUserVo> addUserVoList = new ArrayList<>();
                String          fileName      = image.get(0).getOriginalFilename();
                StringBuilder   errorText     = new StringBuilder();
                label:
                for (Map<String, Object> map : excelDataList)
                {
                    int x = MapUtils.getIntValue(map, "x");
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "errorText")))
                    {
                        errorText.append(MapUtils.getString(map, "errorText"));
                    }
                    //用户头像
                    String userHeadUrl = "";
                    //用户昵称
                    String userName = "";
                    //用户密码
                    String password = MapUtils.getString(map, "password");
                    //用户手机号
                    String phone = MapUtils.getString(map, "phone");
                    //账号来源
                    String source = MapUtils.getString(map, "source");
                    //区号
                    String cpc = MapUtils.getString(map, "cpc");
                    // 检查区号是否合法
                    String countryCode = cpc;
                    if (countryCode == null || countryCode.trim().isEmpty())
                    {
                        flag = false;
                        errorText.append("区号不能为空").append(SplitUtils.DH);
                        break;
                    }

                    CountryModel countryModel = countryModelMapper.selectByCode2(countryCode.trim());
                    if (countryModel == null)
                    {
                        flag = false;
                        errorText.append(String.format("区号 [%s] 不合法", countryCode)).append(SplitUtils.DH);
                        break;
                    }
                    //国家代码
                    Integer country_num = MapUtils.getInteger(map, "country_num");
                    //系统默认头像/昵称
                    ConfigModel configModel = new ConfigModel();
                    configModel.setStore_id(vo.getStoreId());
                    configModel = configModelMapper.selectOne(configModel);
                    if (configModel != null)
                    {
                        if (configModel.getWx_headimgurl() == null || configModel.getWx_name() == null)
                        {
                            errorText.append("请先完成用户设置").append(SplitUtils.DH);
                            flag = false;
                            break;
                        }
                        userHeadUrl = publiceService.getImgPath(configModel.getWx_headimgurl(), vo.getStoreId());
                        userName = configModel.getWx_name();
                    }
                        AddUserVo addUserVo = new AddUserVo();
                        addUserVo.setStoreId(vo.getStoreId());
                        addUserVo.setHeaderUrl(userHeadUrl);
                        addUserVo.setUserName(userName);
                        addUserVo.setZhanghao(publiceService.generateAccount(8));
                        addUserVo.setMima(password);
                        addUserVo.setPhone(phone);
                        addUserVo.setCpc(cpc);
                        addUserVo.setCountry_num(country_num);
                        switch (source)
                        {
                            case "PC端":
                                addUserVo.setSource(Integer.valueOf(DictionaryConst.StoreSource.LKT_LY_006));
                                break;
                            case "小程序":
                                addUserVo.setSource(Integer.valueOf(DictionaryConst.StoreSource.LKT_LY_001));
                                break;
                            case "APP端":
                                addUserVo.setSource(Integer.valueOf(DictionaryConst.StoreSource.LKT_LY_011));
                                break;
                            case "H5移动端":
                                addUserVo.setSource(Integer.valueOf(DictionaryConst.StoreSource.LKT_LY_002));
                                break;
                            default:
                                errorText.append(String.format("第%s行 %s", x, "请完善正确的账号来源:PC端,小程序,APP端,H5移动端")).append(SplitUtils.DH);
                                flag = false;
                                break label;
                        }
                        addUserVo.setAccessId(vo.getAccessId());
                        addUserVoList.add(addUserVo);
                }
                try
                {
                    extracted(addUserVoList, errorText);
                }
                catch (LaiKeAPIException ignored)
                {
                    flag = false;
                }

                //记录
                FileDeliveryModel fileDeliveryModel = new FileDeliveryModel();
                fileDeliveryModel.setId(BuilderIDTool.getSnowflakeId() + "");
                fileDeliveryModel.setName(fileName);
                fileDeliveryModel.setAdd_date(new Date());
                int status = 1;
                if (StringUtils.isNotEmpty(errorText))
                {
                    status = 0;
                    fileDeliveryModel.setText(errorText.deleteCharAt(errorText.length() - 1).toString());
                }
                fileDeliveryModel.setStatus(status);
                fileDeliveryModel.setType(2);
                fileDeliveryModel.setMch_id(user.getShop_id());
                fileDeliveryModelMapper.insertSelective(fileDeliveryModel);

                publiceService.addAdminRecord(vo.getStoreId(), "执行了批量导入用户操作", AdminRecordModel.Type.ADD, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量添加用户 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadAddGoods");
        }
        return flag;
    }

    //@Transactional(rollbackFor = Exception.class)
    public void extracted(List<AddUserVo> addUserVoList, StringBuilder errorText)
    {
        //设置回滚点
        int errorLineIndex = 1;
        try
        {
            if (StringUtils.isEmpty(errorText.toString()))
            {
                for (AddUserVo addUserVo : addUserVoList)
                {
                    logger.debug("================== 正在添加用户:" + addUserVo.getPhone() + "========================");
                    logger.debug("手机号:{} 账号:{}", addUserVo.getPhone(), addUserVo.getZhanghao());
                    try
                    {
                        this.addUser(addUserVo);
                    }
                    finally
                    {
                        logger.debug("================== 添加用户结束 ========================");
                        errorLineIndex++;
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("批量上传用户 异常", l);
            //回滚
            errorText.append(String.format("第%s行 %s", errorLineIndex, l.getMessage())).append(SplitUtils.DH);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadAddGoods");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delUploadRecord(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            AdminModel        user              = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            FileDeliveryModel fileDeliveryModel = fileDeliveryModelMapper.selectByPrimaryKey(id);
            if (fileDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
            }
            int row = fileDeliveryModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了文件id：" + id + " 的批量用户导入记录的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除导入记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delUploadRecord");
        }
    }

    @Override
    public Map<String, Object> uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("mchId", user.getShop_id());
            paramMap.put("name", key);
            paramMap.put("type", 2);
            paramMap.put("status", status);
            paramMap.put("startDate", startDate);
            paramMap.put("endDate", endDate);
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());

            int                       total      = fileDeliveryModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (total > 0)
            {
                resultList = fileDeliveryModelMapper.selectDynamic(paramMap);
            }

            resultMap.put("total", total);
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量上传记录列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadRecordList");
        }
        return resultMap;
    }


    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private UserRuleModelMapper userRuleModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

}
