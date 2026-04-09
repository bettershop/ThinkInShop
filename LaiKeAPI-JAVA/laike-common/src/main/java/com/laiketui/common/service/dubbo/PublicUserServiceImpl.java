package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.order.PublicIntegralService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
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
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.BankCardModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.plugin.bbs.BbsForumModel;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserGradeModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.integral.AddScoreVo;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.license.Md5Util;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 会员公共接口
 *
 * @author Trick
 * @date 2020/12/23 10:26
 */
@Service
public class PublicUserServiceImpl implements PublicUserService
{
    private final Logger logger = LoggerFactory.getLogger(PublicAlipayServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicIntegralService publicIntegralService;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Override
    public BigDecimal orderTotal(int storeId, int id, String userId, int flag, int method) throws LaiKeAPIException
    {
        BigDecimal totalAmt = BigDecimal.ZERO;
        try
        {
            //获取当前等级信息
            UserGradeModel currentGradeModel = new UserGradeModel();
            currentGradeModel.setId(id);
            currentGradeModel.setStore_id(storeId);
            currentGradeModel = userGradeModelMapper.selectOne(currentGradeModel);
            if (currentGradeModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "orderTotal");
            }
            if (flag != 3)
            {
                //非升级
                if (method == 1)
                {
                    totalAmt = currentGradeModel.getMoney();
                }
                else if (method == 2)
                {
                    totalAmt = currentGradeModel.getMoney_j();
                }
                else if (method == 3)
                {
                    totalAmt = currentGradeModel.getMoney_n();
                }
            }
            else
            {
                //升级 获取会员等级信息
                Map<String, Object> userGreadInfo = userBaseMapper.getUserGradeExpire(storeId, userId);
                if (userGreadInfo != null && !userGreadInfo.isEmpty())
                {
                    //包月金额
                    BigDecimal baoyueAmt = new BigDecimal(String.valueOf(userGreadInfo.get("bymoney")));
                    //到期时间 年月日
                    String gradeEnd = DateUtil.dateFormate(String.valueOf(userGreadInfo.get("grade_end")), GloabConst.TimePattern.YMD);
                    //当前时间
                    String nowDate = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD);
                    //拆分年月日
                    String[] gradeEndList = gradeEnd.split(SplitUtils.HG);
                    String[] nowDateList  = nowDate.split(SplitUtils.HG);
                    //月份倍数
                    BigDecimal monthNum;
                    //根据要升级的等级计算费用
                    int num = DateUtil.calculationMonthNum(gradeEndList, nowDateList);
                    monthNum = new BigDecimal(num);
                    //需要升级的会员一个月金额 - 当前用户会员一个月的金额 * 月数
                    totalAmt = monthNum.multiply(currentGradeModel.getMoney().subtract(baoyueAmt));
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXKTHY, "请先开通会员", "orderTotal");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员等级订单金额计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderTotal");
        }

        return totalAmt;
    }

    @Override
    public boolean balancePay(String accessId, BigDecimal payPrice, String text, int type) throws LaiKeAPIException
    {
        try
        {
            User userCache = RedisDataTool.getRedisUserCache(accessId, redisUtil, true);
            int  count     = userBaseMapper.rechargeUserPrice(userCache.getId(), payPrice.negate());
            if (count < 1)
            {
                logger.info("余额支付失败");
                return false;
            }
            //记录一笔记录
            RecordModel recordModel = new RecordModel(userCache.getStore_id(), userCache.getUser_id(), payPrice, userCache.getMoney(), new Date(), text, type);
            if (recordModelMapper.insertSelective(recordModel) < 1)
            {
                logger.info("余额支付失败 -金额记录失败 参数{}", JSON.toJSONString(recordModel));
                return false;
            }
            //刷新用户信息
            User userInfo = new User();
            userInfo.setId(userCache.getId());
            userInfo = userBaseMapper.selectOne(userInfo);
            RedisDataTool.refreshRedisUserCache(accessId, userInfo, redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("余额支付 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "balancePay");
        }
        return true;
    }


    @Override
    public void validatePayPwd(String userId, String pwd) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (StringUtils.isEmpty(pwd))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZFMM, "请输入支付密码");
            }
            User user = new User();
            user.setUser_id(userId);
            user = userBaseMapper.selectOne(user);
            //重试次数
            Integer validateNum = user.getLogin_num();
            if (validateNum.equals(GloabConst.LktConfig.PASSWORD_VALIDATE_NUM))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YBSDQMTZS, "已被锁定,请明天再试", "paymentPassword");
            }
            if (StringUtils.isEmpty(user.getPassword()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WSZZFMM, "未设置支付密码");
            }
            User userParam = new User();
            userParam.setId(user.getId());
            pwd = Md5Util.MD5endoce(pwd);
            if (user.getPassword().equals(pwd))
            {
                //验证成功
                userParam.setLogin_num(0);
                int count = userBaseMapper.updateByPrimaryKeySelective(userParam);
                if (count < 0)
                {
                    logger.info("用户 id = " + user.getId() + " 支付密码验证通过,login_num 未清空");
                }
            }
            else
            {
                userParam.setLogin_num(++validateNum);
                userBaseMapper.updateUserInfoById(userParam);
                logger.info("用户 id = " + user.getId() + " 正在重试支付密码 当前次数 " + userParam.getLogin_num());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFMMBZQ, "支付密码不正确");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证支付密码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validatePayPwd");
        }
    }

    @Override
    public boolean userRechargeMoney(int storeId, int id, BigDecimal money, int type, String sNo, String remake) throws LaiKeAPIException
    {
        try
        {
            int count;
            //之前金额
            BigDecimal oldPrice;
            //记录类型
            int recordType;

            //获取用户信息
            User user = userBaseMapper.selectByPrimaryKey(id);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在", "userRechargeMoney");
            }
            if (money.compareTo(BigDecimal.ZERO) == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZQDJE, "请输入正确的金额");
            }
            //添加充值记录详情
            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
            boolean            isRechaarge        = true;
            String             text               = "系统%s";
            if (money.compareTo(BigDecimal.ZERO) > 0)
            {
                text = String.format(text, "充值" + money);
            }
            else
            {
                isRechaarge = false;
                text = String.format(text, "扣除" + money);
            }
            //1=余额 2=消费金额 3=积分
            switch (type)
            {
                case 1:
                    if (isRechaarge)
                    {
                        //系统充值
                        recordType = RecordModel.RecordType.SYSTEM_RECHARGE;
                    }
                    else
                    {
                        //系统扣款
                        recordType = RecordModel.RecordType.SYSTEM_DEDUCTION;
                        BigDecimal amt = user.getMoney().add(money);
                        if (user.getMoney().compareTo(BigDecimal.ZERO) <= 0 || amt.compareTo(BigDecimal.ZERO) < 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHYEBZBNJXKC, "用户余额不足");
                        }
                    }
                    text += "余额";
                    oldPrice = user.getMoney();
                    if (oldPrice.add(money).compareTo(new BigDecimal("999999999.99")) > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_YEHJFDDSX, "用户余额已超过上限");
                    }
                    count = userBaseMapper.rechargeUserPrice(id, money);
                    break;
                case 2:
                    if (isRechaarge)
                    {
                        //系统充值消费金额
                        recordType = RecordModel.RecordType.SYSTEM_CONSUMPTION_FUND;
                    }
                    else
                    {
                        //系统扣
                        recordType = RecordModel.RecordType.SYSTEM_BUCKLE_CONSUMPTION;
                        BigDecimal amt = user.getConsumer_money().add(money);
                        if (user.getConsumer_money().compareTo(BigDecimal.ZERO) <= 0 || amt.compareTo(BigDecimal.ZERO) < 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHXFJFBZBNJXKC, "用户消费积分不足,不能进行扣除");
                        }
                    }
                    text += "消费金额";
                    oldPrice = user.getConsumer_money();
                    count = userBaseMapper.rechargeUserByConsumerMoney(id, money);
                    break;
                case 3:
                    //记录积分
                    AddScoreVo addScoreVo = new AddScoreVo();
                    addScoreVo.setOrderNo("");
                    addScoreVo.setUserId(user.getUser_id());
                    addScoreVo.setStoreId(storeId);
                    addScoreVo.setScoreOld(user.getScore());
                    //记录积分
                    String event = String.format("系统充值%s积分", money);
                    if (isRechaarge)
                    {
                        //系统充值积分
                        recordType = RecordModel.RecordType.SYSTEM_RECHARGE_INTEGRAL;
                        addScoreVo.setType(SignRecordModel.ScoreType.SYSTEM_RECHARGE);
                    }
                    else
                    {
                        //系统扣
                        event = String.format("系统扣除%s积分", money);
                        recordType = RecordModel.RecordType.SYSTEM_BUCKLE_INTEGRAL;
                        addScoreVo.setType(SignRecordModel.ScoreType.SYSTEM_DIFF);
                        int amt = user.getScore() + money.intValue();
                        if (user.getScore() <= 0 || amt < 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHJFBZBNJXKC, "用户积分不足,不能进行扣除");
                        }
                    }
                    addScoreVo.setEvent(event);
                    addScoreVo.setScore(money.abs().intValue());
                    publicIntegralService.addScore(addScoreVo);
                    text += "积分";
                    oldPrice = new BigDecimal(String.valueOf(user.getScore()));
                    //充值金额/积分不能大于9位数
                    if (oldPrice.add(money).compareTo(new BigDecimal("999999999.99")) > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_YEHJFDDSX, "充值金额/积分不能大于9位数");
                    }
                    count = userBaseMapper.rechargeUserByScore(id, money);
                    break;
                case 4:
                    //系统扣款
                    recordType = RecordModel.RecordType.SYSTEM_DEDUCTION;
                    BigDecimal amt = user.getMoney().add(money);
                    if (user.getMoney().compareTo(BigDecimal.ZERO) <= 0 || amt.compareTo(BigDecimal.ZERO) < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHYEBZBNJXKC, "用户余额不足");
                    }
                    text += "余额";
                    oldPrice = user.getMoney();
                    if (oldPrice.add(money).compareTo(new BigDecimal("9999999999.99")) > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEYCGSX, "用户余额已超过上限");
                    }
                    count = userBaseMapper.rechargeUserPrice(id, money);
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "userRechargeMoney");
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "userRechargeMoney");
            }
            user = userBaseMapper.selectByPrimaryKey(user.getId());

            Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(storeId, user.getPreferred_currency());
            Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(storeId);
            if (type == 1)
            {
                recordDetailsModel.setStore_id(storeId);
                recordDetailsModel.setMoney(money.abs());
                recordDetailsModel.setUserMoney(user.getMoney());
                recordDetailsModel.setMoneyType(isRechaarge ? RecordDetailsModel.moneyType.INCOME : RecordDetailsModel.moneyType.EXPENDITURE);
                recordDetailsModel.setMoneyTypeName(isRechaarge ? RecordDetailsModel.moneyTypeName.STORE_BALANCE_RECHARGE : RecordDetailsModel.moneyTypeName.BALANCE_DEDUCTION);
                recordDetailsModel.setType(isRechaarge ? RecordDetailsModel.type.STORE_BALANCE_RECHARGE : RecordDetailsModel.type.PLATFORM_DEDUCTION);
                recordDetailsModel.setRecordTime(new Date());
                recordDetailsModel.setRecordNotes(remake);
                recordDetailsModel.setAddTime(new Date());

                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                recordDetailsModelMapper.insert(recordDetailsModel);
            }
            if (type == 4)
            {
                recordDetailsModel.setStore_id(storeId);
                recordDetailsModel.setMoney(money.abs());
                recordDetailsModel.setUserMoney(user.getMoney());
                recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.ORDER_HELP_ORDER);
                recordDetailsModel.setType(RecordDetailsModel.type.ORDER_PAYMENT);
                recordDetailsModel.setRecordTime(new Date());
                recordDetailsModel.setRecordNotes(remake);
                recordDetailsModel.setAddTime(new Date());
                recordDetailsModel.setsNo(sNo);
                recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                recordDetailsModelMapper.insert(recordDetailsModel);
            }
            //添加充值记录
            RecordModel recordModel = new RecordModel(storeId, user.getUser_id(), money, oldPrice, new Date(), text, recordType, recordDetailsModel.getId());
            recordModelMapper.insertSelective(recordModel);
            RedisDataTool.refreshRedisUserCache(user, redisUtil);

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("会员 充值/消费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage(), "网络异常", "userRechargeMoney");
        }
    }

    @Autowired
    private PublicMchService publicMchService;

    @Override
    public Map<String, Object> getIntoWallet(MainVo vo, Integer shopId, User user, String pluginType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            BigDecimal minCharge;
            BigDecimal maxCharge;
            //商户/用户 余额
            BigDecimal accountMoney;
            //可用余额
            BigDecimal cashableMoney;
            String     phone;
            String cpc;
            String     serviceChargeStr;
            //单位
            String unit = "元";

            //获取店铺/用户银行卡信息
            List<Map<String, Object>> bankCardModelMaps = new ArrayList<>();
            BankCardModel             bankCardModel     = new BankCardModel();
            bankCardModel.setStore_id(vo.getStoreId());
            bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);

            List<BankCardModel> bankCardModelList;
            if (shopId != null)
            {
                //获取店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setUser_id(user.getUser_id());
                mchModel.setReview_status(DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
                mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    bankCardModel.setMch_id(mchModel.getId());
                    phone = mchModel.getTel();
                    cpc = mchModel.getCpc();
                    accountMoney = mchModel.getAccount_money();
                    cashableMoney = mchModel.getCashable_money();
                    if (mchModel.getId().equals(shopId))
                    {
                        bankCardModelList = bankCardModelMapper.select(bankCardModel);
                        //获取店铺配置信息(自营店)
                        Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
                        MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), storeMchId);
                        if (mchConfigModel != null)
                        {
                            minCharge = mchConfigModel.getMin_charge();
                            maxCharge = mchConfigModel.getMax_charge();
                            BigDecimal serviceCharge = mchConfigModel.getService_charge();
                            serviceCharge = serviceCharge.multiply(new BigDecimal("100"));
                            serviceChargeStr = serviceCharge + "%";
                            resultMap.put("total", cashableMoney.toString());
                            resultMap.put("serviceCharge", mchConfigModel.getService_charge());
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WHQDDPPZXX, "未获取到店铺配置信息", "intoWallet1");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHYDPBPP, "用户与店铺不匹配", "intoWallet1");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "intoWallet1");
                }
            }
            else
            {
                bankCardModel.setUser_id(user.getUser_id());
                bankCardModelList = bankCardModelMapper.selectBankcardByUser(bankCardModel.getStore_id(), user.getUser_id());

                phone = user.getMobile();
                cpc = user.getCpc();
                accountMoney = user.getMoney();
                //获取钱包配置信息
                FinanceConfigModel financeConfigModel = new FinanceConfigModel();
                financeConfigModel.setStore_id(vo.getStoreId());
                financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
                if (financeConfigModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QBPZBCZ, "钱包配置不存在");
                }
                unit = financeConfigModel.getUnit();
                minCharge = financeConfigModel.getMin_amount();
                maxCharge = financeConfigModel.getMax_amount();
                BigDecimal serviceCharge = financeConfigModel.getService_charge();
                serviceCharge = serviceCharge.multiply(new BigDecimal("100"));
                serviceChargeStr = serviceCharge.stripTrailingZeros().toPlainString() + "%";
                resultMap.put("serviceCharge", financeConfigModel.getService_charge());
                if (StringUtils.isNotEmpty(pluginType))
                {
                    if (pluginType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                    {
                        //获取当前用户可提现金额
                        BigDecimal txAmt = new BigDecimal("0.00");
                        //获取分销用户可提现金额
                        UserDistributionModel userDistributionModel = new UserDistributionModel();
                        userDistributionModel.setUser_id(user.getUser_id());
                        userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                        if (userDistributionModel != null)
                        {
                            txAmt = userDistributionModel.getTx_commission();
                        }
                        accountMoney = txAmt;
                        resultMap.put("total", txAmt.toString());
                    }
                }
            }
            for (BankCardModel bankCard : bankCardModelList)
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("id", bankCard.getId());
                map.put("Cardholder", bankCard.getCardholder());
                map.put("Bank_name", bankCard.getBank_name());
                String str     = "%s 尾号(%s)";
                String bankNum = bankCard.getBank_card_number();
                map.put("Bank_card_number", String.format(str, bankCard.getBank_name(), bankNum.substring(bankNum.length() - 4)));
                map.put("branch", bankCard.getBranch());
                map.put("is_default", bankCard.getIs_default());
                bankCardModelMaps.add(map);
            }

            StringBuilder pshd             = new StringBuilder("请输入提现金额(大于").append(minCharge).append("小于").append(maxCharge).append(")");
            Integer       wechatV3Withdraw = paymentConfigModelMapper.getPaymentTypeInfoByClassName(vo.getStoreId(), "wechat_v3_withdraw");
            //是否开启微信余额提现
            if (wechatV3Withdraw != null && wechatV3Withdraw == DictionaryConst.WhetherMaven.WHETHER_NO)
            {
                resultMap.put("wx_open", true);
            }
            else
            {
                resultMap.put("wx_open", false);
            }
            //判断用户是否绑定过微信可以进行微信零钱提现
            if (StringUtils.isEmpty(user.getWx_id()))
            {
                resultMap.put("wx_withdraw", false);
                resultMap.put("wx_name", "");
            }
            else
            {
                resultMap.put("wx_withdraw", true);
                //resultMap.put("wx_name", user.getWx_name());
                //89，绑定后此字段直接返回已绑定
                resultMap.put("wx_name", "已绑定");
            }
            resultMap.put("min_amount", minCharge);
            resultMap.put("max_amount", maxCharge);
            resultMap.put("money", accountMoney);
            resultMap.put("pshd", pshd);
            resultMap.put("unit", unit);
            resultMap.put("bank_information", bankCardModelMaps);
            resultMap.put("mobile", phone);
            resultMap.put("cpc", cpc);
            resultMap.put("service_charge", serviceChargeStr);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺提现页面数据 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchIntoWallet");
        }
        return resultMap;
    }

    @Override
    public void register(MainVo vo, String pid, User user) throws LaiKeAPIException
    {
        try
        {
            //用户id
            String userId = "";
            //获取用户默认信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                if (StringUtils.isEmpty(user.getUser_name()))
                {
                    String name = configModel.getWx_name();
                    user.setUser_name(name);
                    if (StringUtils.isEmpty(user.getUser_name()))
                    {
                        user.setUser_name(BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10));
                        user.setWx_name(user.getWx_name());
                    }
                }
                if (StringUtils.isEmpty(user.getHeadimgurl()))
                {
                    user.setHeadimgurl(publiceService.getImgPath(configModel.getWx_headimgurl(), vo.getStoreId()));
                }
                userId = configModel.getUser_id();
            }
            if (StringUtils.isEmpty(userId))
            {
                userId = "U";
            }
            //获取推荐人,没有推荐人默认第一条用户为推荐人
            String fatherId = "";
            logger.debug("获取到前端传的推荐人id为:{}", pid);
            if (StringUtils.isNotEmpty(pid))
            {
                //获取分销信息
                UserDistributionModel userDistributionModel = new UserDistributionModel();
                userDistributionModel.setStore_id(vo.getStoreId());
                userDistributionModel.setUser_id(pid);
                userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                if (userDistributionModel != null)
                {
                    fatherId = userDistributionModel.getUser_id();
                }
            }
            else
            {
                fatherId = customerModelMapper.getStoreUserId(vo.getStoreId());
            }
            //是否有传token,如果有token则表示进入商品详情
            String token = vo.getAccessId();
            if (StringUtils.isEmpty(token))
            {
                //获取token
                token = JwtUtils.getToken(getUserLoginLife(vo.getStoreId()));
            }
            user.setGrade(User.USER);
            user.setMoney(BigDecimal.ZERO);
            user.setScore(0);
            user.setLang(vo.getLanguage());
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_ADMIN)
            {
                //添加自营店默认使用pc商城
                user.setSource(String.valueOf(GloabConst.StoreType.STORE_TYPE_PC_MALL));
            }
            else
            {
                user.setSource(String.valueOf(vo.getStoreType()));
            }

            user.setAccess_id(token);
            user.setStore_id(vo.getStoreId());
            user.setReferee(fatherId);
            user.setRegister_data(new Date());
            //使用的默认用户配置   性别未知  生日当前时间
            user.setSex(0);
            user.setBirthday(user.getBirthday());
            user.setIs_default_value(1);
            user.setIs_default_birthday(1);

            //获取商城默认语种
            user.setLang(configModel.getDefault_lang_code());
            //默认设置为商城默认币种
            Map storeCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
            user.setPreferred_currency(MapUtils.getInteger(storeCurrencyMap, "currency_id"));

            int count = userBaseMapper.insertSelective(user);
            if (count < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZCSB, "注册失败");
            }
            User userParameter = userBaseMapper.selectByPrimaryKey(user.getId());
            userParameter.setUser_id(userId + (user.getId() + 1));
            count = userBaseMapper.updateByPrimaryKeySelective(userParameter);
            if (count < 1)
            {
                logger.error(user.getId() + ": userid生成失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZCSBQLXGLY, "注册失败,请联系管理员");
            }
            userId = userParameter.getUser_id();
            User loginUser = new User();
            loginUser.setUser_id(userId);
            User userNew = userBaseMapper.selectOne(loginUser);
            if (userNew == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZCSBWLFM, "注册失败,网络繁忙");
            }
            BeanUtils.copyProperties(userNew, user);
            //操作记录
            String text = "会员%s注册成功";
            if (saveUserRecord(vo.getStoreId(), user.getUser_id(), String.format(text, user.getUser_id()), 0))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZCSBWLFM, "注册失败,网络繁忙");
            }
            //获取分销设置
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            //是否是注册绑定永久关系
            if (distributionConfigModel != null && distributionConfigModel.getRelationship().equals(DistributionConfigModel.RelationshipType.PLUGIN_OPEN) && fatherId != null)
            {
                logger.info("分销商注册绑定永久关系 创建分销商={}和上级绑定永久关系上级={}", userId, fatherId);
                publiceDistributionService.createLevel(vo.getStoreId(), userId, 0, fatherId);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("注册用户公共方法 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "register");
        }
    }

    @Override
    public boolean saveUserRecord(int storeId, String userId, String text, int type) throws LaiKeAPIException
    {
        try
        {
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(storeId);
            recordModel.setUser_id(userId);
            recordModel.setEvent(text);
            recordModel.setType(type);
            recordModel.setAdd_date(new Date());
            int count = recordModelMapper.insertSelective(recordModel);
            return count <= 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加一条操作记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveUserRecord");
        }
    }

    @Override
    public void loginSuccess(User loginUser, String token, String fatherUid) throws LaiKeAPIException
    {
        try
        {
            loginSuccess(loginUser, token, fatherUid, null, null);
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
    public void loginSuccess(User loginUser, String token, String fatherUid, String flagKey, String loginKey) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(flagKey))
            {
                flagKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG;
            }
            if (StringUtils.isEmpty(loginKey))
            {
                loginKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
            }
            String event = "用户" + loginUser.getUser_id() + "登陆";
            //整理购物车
            loginCart(token, loginUser.getStore_id(), StringUtils.stringParseInt(loginUser.getSource()), loginUser.getUser_id());
            //登录生命周期
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(loginUser.getStore_id());
            ConfigModel model = configModelMapper.selectOne(configModel);
            Integer expTime = 0;
            if (Objects.nonNull(model) && Objects.nonNull(model.getExp_time())){
               expTime = DateUtil.convToSecond(model.getExp_time(),2);
            }else {
                expTime = DateUtil.convToSecond(null,2);
            }
            //没有则生成token
            Map<String, Object> tokenMap = new HashMap<>(16);
            tokenMap.put("user", loginUser.getUser_id());
            tokenMap.put("exp", new Date());
            if (StringUtils.isEmpty(token))
            {
                //生成token
                token = JwtUtils.getToken(tokenMap, expTime);
            }
            else
            {
                //是否过期
                try
                {
                    JwtUtils.verifyJwt(token);
                }
                catch (LaiKeCommonException c)
                {
                    //过期
                    token = JwtUtils.getToken(tokenMap, expTime);
                }
            }
            //修改推荐人
            String topUserId = userDistributionModelMapper.getTheTopLevelUser(loginUser.getStore_id());
            if (StringUtils.isNotEmpty(fatherUid))
            {
                //获取分销设置
                DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                distributionConfigModel.setStore_id(loginUser.getStore_id());
                distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                if (distributionConfigModel != null && distributionConfigModel.getRelationship().equals(DistributionConfigModel.RelationshipType.PLUGIN_OPEN))
                {
                    logger.debug("{}当前分销设置为注册绑永久,当前上级为:{},当前分享人为:{}", loginUser.getUser_id(), loginUser.getReferee(), fatherUid);
                    if (StringUtils.isNotEmpty(loginUser.getReferee()))
                    {
                        fatherUid = loginUser.getReferee();
                    }
                }
                //推荐人不能是自己,如果是自己则使用平台
                if (loginUser.getUser_id().equals(fatherUid))
                {
                    logger.debug("{}推荐人是自己,获取商城总分销商", loginUser.getUser_id());
                }
                else
                {
                    //获取推荐人信息
                    User fatherUser = new User();
                    fatherUser.setStore_id(loginUser.getStore_id());
                    fatherUser.setUser_id(fatherUid);
                    fatherUser = userBaseMapper.selectOne(fatherUser);
                    if (fatherUser != null)
                    {
                        //获取推荐人分销Id
                        UserDistributionModel userDistributionModel = new UserDistributionModel();
                        userDistributionModel.setStore_id(loginUser.getStore_id());
                        userDistributionModel.setUser_id(fatherUser.getUser_id());
                        userDistributionModel = userDistributionModelMapper.selectOne(userDistributionModel);
                        if (userDistributionModel != null)
                        {
                            fatherUid = userDistributionModel.getUser_id();
                        }
                    }
                    else
                    {
                        fatherUid = "";
                        logger.debug("{}不是分销商,获取商城总分销商", fatherUid);
                    }
                }
            }
            if (StringUtils.isEmpty(fatherUid))
            {
                fatherUid = topUserId;
            }
            //覆盖表中token
            userBaseMapper.resettingToken(loginUser.getStore_id(), token);
            User updateUser = new User();
            updateUser.setId(loginUser.getId());
            if (!StringUtils.isEmpty(loginUser.getClientid()))
            {
                updateUser.setClientid(loginUser.getClientid());
            }

            //当前登录用户所属店铺id
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(loginUser.getStore_id());
            mchModel.setUser_id(loginUser.getUser_id());
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (Objects.nonNull(mchModel)) {
                loginUser.setMchId(mchModel.getId());
            }

            updateUser.setAccess_id(token);
            updateUser.setReferee(fatherUid);
            updateUser.setLast_time(new Date());
//            updateUser.setLang(loginUser.getLang());
            int count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                logger.info("修改登陆信息失败 参数:" + JSON.toJSONString(updateUser));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //添加一条操作记录
            if (publicUserService.saveUserRecord(loginUser.getStore_id(), loginUser.getUser_id(), event, 0))
            {
                logger.info("添加操作记录失败 参数:" + JSON.toJSONString(updateUser));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            count = userBaseMapper.updateByPrimaryKeySelective(updateUser);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }

            /**
             * 添加种草官身份
             * @param user
             * @param store_id
             */
            addForum(loginUser,loginUser.getStore_id());

            //刷新用户信息
            User userInfo = userBaseMapper.selectByPrimaryKey(loginUser.getId());
            BeanUtils.copyProperties(userInfo, loginUser);

            //之前用户是否登录,如果登录则删除缓存
            //禅道38469需要可同时在多端登录
//            String tokenOld = redisUtil.get(flagKey + loginUser.getUser_id()) + "";
//            if (!StringUtils.isEmpty(tokenOld)) {
//                redisUtil.del(tokenOld);
//            }
            //账号登录互斥 禅道34563要求不互斥
//            RedisDataTool.kickingUser(loginUser.getUser_id(), redisUtil);
            //保存token
            loginUser.setAccess_token(loginKey + token);
            //获取登录生命周期
            Integer lifeTile = publicUserService.getUserLoginLife(loginUser.getStore_id());
            //映射token 用于踢人
            String jsonString = JSON.toJSONString(loginUser);
            redisUtil.set(loginUser.getAccess_token(), jsonString, lifeTile);
            redisUtil.set(flagKey + loginUser.getUser_id(), loginKey + token, lifeTile);
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

    private void addForum(User user,Integer store_id)
    {
        //判断是否是种草官
        BbsForumModel bbsForumModel = new BbsForumModel();
        bbsForumModel.setRecycle(0);
        bbsForumModel.setUser_id(user.getUser_id());
        bbsForumModel = bbsForumModelMapper.selectOne(bbsForumModel);
        if (Objects.isNull(bbsForumModel))
        {
            bbsForumModel = new BbsForumModel();
            bbsForumModel.setStore_id(store_id);
            bbsForumModel.setUser_id(user.getUser_id());
            bbsForumModel.setStatus(2);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(user.getHeadimgurl()))
            {
                bbsForumModel.setHead_img(user.getHeadimgurl());
            }
            bbsForumModel.setName(user.getUser_name());
            bbsForumModelMapper.insertSelective(bbsForumModel);
        }
    }



    @Override
    public void loginCart(String token, int storeId, int storeType, String userId) throws LaiKeAPIException
    {
        try
        {
            //整理购物车
            MainVo mainVo = new MainVo();
            mainVo.setAccessId(token);
            mainVo.setStoreId(storeId);
            mainVo.setStoreType(storeType);
            publiceService.arrangeUserCart(mainVo, userId);
        }
        catch (Exception e)
        {
            logger.error("登录后购物车处理 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginCart");
        }
    }

    @Override
    public Integer getUserLoginLife(int storeId) throws LaiKeAPIException
    {
        try
        {
            Integer lifeTime = configModelMapper.getLoginExistenceTime(storeId);
            if (lifeTime == null)
            {
                lifeTime = GloabConst.LktConfig.LOGIN_EXISTENCE_TIME;
            }
            return lifeTime;
        }
        catch (Exception e)
        {
            logger.error("获取移动端登录有效时长 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "loginCart");
        }
    }

    @Autowired
    private UserMapper userMapper;

    /**
     * 缓存平台新增用户信息
     *
     * @return
     */
    @Override
    public Map<String, Object> getAdditionUserData(Integer storeId) throws LaiKeAPIException
    {
        try
        {
            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            //减一周
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            //减一月
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            //减一年
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            Map<String, Object> detailMap = new HashMap<>();

            for (int i = 0; i < dateList.size(); i++)
            {
                List<List> dataList = new ArrayList<>();
                //获取输入时间到当前时间之间的时间
                List<Date> days = DateUtil.createDays(dateList.get(i));
                //全部日期
                List<String> daysStr = new ArrayList<>();
                //对应平台的新增用户
                List<Integer> app_num = new ArrayList<>();
                List<Integer> h5_num  = new ArrayList<>();
                List<Integer> pc_num  = new ArrayList<>();
                List<Integer> xcx_num = new ArrayList<>();
                for (Date day : days)
                {
                    String dayStr = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                    daysStr.add(dayStr);
                    int[] sources = {2, 1, 7, 6};
                    for (int source : sources)
                    {
                        //查询用户数量
                        Integer additionUserByDay = userMapper.getAdditionUserByDay(storeId, dayStr, source);
                        switch (source)
                        {
                            case 2:
                                app_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 7:
                                h5_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 6:
                                pc_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                            case 1:
                                xcx_num.add(additionUserByDay == null ? 0 : additionUserByDay);
                                break;
                        }
                    }
                }
                dataList.add(daysStr);
                dataList.add(app_num);
                dataList.add(xcx_num);
                dataList.add(h5_num);
                dataList.add(pc_num);

                switch (i)
                {
                    case 0:
                        detailMap.put("week", dataList);
                        break;
                    case 1:
                        detailMap.put("month", dataList);
                        break;
                    case 2:
                        detailMap.put("year", dataList);
                        break;
                }
            }
            return detailMap;
        }
        catch (Exception e)
        {
            logger.error("获取用户报表异常:{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, "网络异常",e.getMessage());
        }
    }
    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private BbsForumModelMapper bbsForumModelMapper;

    @Autowired
    private BbsConfigModelMapper bbsConfigModelMapper;
}

