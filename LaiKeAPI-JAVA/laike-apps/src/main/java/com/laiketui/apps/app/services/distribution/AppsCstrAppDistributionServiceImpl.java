package com.laiketui.apps.app.services.distribution;

import com.alibaba.fastjson2.JSON;
import com.laiketui.apps.api.app.services.AppsCstrAppDistributionService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.consts.MchConst;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.DistributionGoodsModel;
import com.laiketui.domain.distribution.DistributionWithdrawModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.mch.BankCardModel;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 分销中心
 *
 * @author Trick
 * @date 2021/2/9 9:27
 */
@Service
public class AppsCstrAppDistributionServiceImpl implements AppsCstrAppDistributionService
{
    private final Logger logger = LoggerFactory.getLogger(AppsCstrAppDistributionServiceImpl.class);

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;

    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private DistributionWithdrawModelMapper distributionWithdrawModelMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DistributionRankingModelMapper distributionRankingModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DistributionGoodsModelMapper distributionGoodsModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //分销规则
            String content = "";
            //用户头像
            String imgUrl = ImgUploadUtils.getUrlPure(user.getHeadimgurl(), true);
            //用户昵称
            String userName = user.getUser_name();
            //用户id
            String userId = user.getUser_id();
            //累计佣金
            BigDecimal comm2 = BigDecimal.ZERO;
            //可提现佣金
            BigDecimal comm3 = BigDecimal.ZERO;
            //推荐人id
            String fatherId = user.getReferee();
            //是否为分销商 1=true 0=false
            int     isDistribution = 0;
            Integer mchIdMain      = customerModelMapper.getStoreMchId(vo.getStoreId());
            //是否为总店
            boolean isMain = mchIdMain.equals(user.getMchId());

            //查询配置参数
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (distributionConfigModel != null)
            {
                //分销配置参数
                Map<String, Object> configSetsMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                content = configSetsMap.get("content") + "";
            }
            //获取会员分销信息
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                //可提现佣金
                comm3 = Objects.nonNull(userDistributionModel.getTx_commission()) ? userDistributionModel.getTx_commission() : comm3;
                //累计佣金
                comm2 = Objects.nonNull(userDistributionModel.getAccumulative()) ? userDistributionModel.getAccumulative() : comm2;

                if (isMain || userDistributionModel.getLevel() > 0)
                {
                    isDistribution = 1;
                    //获取等级名称
                    String gradeName = "默认等级";
                    if (isMain)
                    {
                        gradeName = "平台";
                    }
                    DistributionGradeModel distributionGradeModel = distributionGradeModelMapper.selectByPrimaryKey(userDistributionModel.getLevel());
                    if (distributionGradeModel != null)
                    {
                        Map<String, String> gradeSetMap = DataUtils.cast(SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets()));
                        if (gradeSetMap != null && !gradeSetMap.isEmpty())
                        {
                            gradeName = gradeSetMap.get("s_dengjiname");
                        }
                    }
                    resultMap.put("levelname", gradeName);
                    //获取预计佣金
                    BigDecimal estimateAmt = distributionRecordModelMapper.sumEstimateAmt(user.getStore_id(), user.getUser_id());
                    resultMap.put("comm1", estimateAmt);
                    //获取推荐人信息
                    String fatherName = "总店";
                    if (StringUtils.isNotEmpty(fatherId) && !isMain)
                    {
                        User fatherUser = new User();
                        fatherUser.setStore_id(user.getStore_id());
                        fatherUser.setUser_id(userDistributionModel.getPid());
                        fatherUser = userBaseMapper.selectOne(fatherUser);
                        if (fatherUser != null)
                        {
                            fatherName = fatherUser.getUser_name();
                        }
                    }
                    else
                    {
                        //总店是上级是自己
                        fatherId = user.getUser_id();
                    }
                    resultMap.put("pidname", fatherName);
                    //是否存在未审核提现申请
                    Integer wid = distributionWithdrawModelMapper.selectNotExaineToId(vo.getStoreId(), userId);
                    resultMap.put("tixian_id", wid == null ? 0 : wid);
                }
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("userId", user.getUser_id());
            List<String> orderTypeList = new ArrayList<>();
            orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_FX);
            parmaMap.put("orderTypeList", orderTypeList);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            int orderNum = orderModelMapper.countDynamic(parmaMap);
            //获取所有分销订单
            resultMap.put("orderNum", orderNum);
            resultMap.put("content", content);
            resultMap.put("headimgurl", imgUrl);
            resultMap.put("user_name", userName);
            resultMap.put("user_id", userId);
            resultMap.put("comm2", comm2);
            resultMap.put("comm3", comm3);
            resultMap.put("pid", fatherId);
            resultMap.put("is_distribution", isDistribution);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的分销信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    public Map<String, Object> mygroup(MainVo vo, String userId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            List<Map<String, Object>> childrenList = null;
            //团队总数量
            int team = 0;
            //分销配置参数
            Map<String, Object> configSetsMap = null;
            //查询配置参数
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (distributionConfigModel != null)
            {
                //分销配置参数
                configSetsMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
            }
            //获取会员分销信息
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                team = userDistributionModelMapper.countChildrenNum(vo.getStoreId(), userDistributionModel.getLt(), userDistributionModel.getRt());
                if (userDistributionModel.getLevel() > 0)
                {
                    UserDistributionModel userDistributionQuery = new UserDistributionModel();
                    userDistributionQuery.setStore_id(vo.getStoreId());
                    userDistributionQuery.setUser_id(userId);
                    //如果没有指定查询人,则查询当前登录用户的所有下级,否则查询指定用户的下级
                    if (!StringUtils.isEmpty(userId))
                    {
                        userDistributionQuery = userDistributionModelMapper.selectOne1(userDistributionQuery);
                    }
                    else
                    {
                        userDistributionQuery = userDistributionModel;
                    }
                    if (userDistributionQuery != null)
                    {
                        //系统设置允许几层查询，x层查询y层,查询x-(y+1)<=系统设置层
                        int configUplevel = 0;
                        if (configSetsMap != null && configSetsMap.containsKey(DistributionConfigModel.SetsKey.C_CENGJI))
                        {
                            configUplevel = Integer.parseInt(configSetsMap.get(DistributionConfigModel.SetsKey.C_CENGJI).toString());
                        }
                        //用户层级
                        int currentUplevel = userDistributionModel.getUplevel();
                        //被查询用户层级
                        int uplevel = userDistributionQuery.getUplevel();
                        //计算当前会员与被查询的会员层级差
                        if ((uplevel - currentUplevel) > configUplevel)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYGDL, "没有更多了");
                        }
                        else
                        {
                            //查看下级
                            childrenList = userDistributionModelMapper.getChildrenList(vo.getStoreId(), userDistributionQuery.getLt(), userDistributionQuery.getRt(), uplevel + 1, vo.getPageNo(), vo.getPageSize());
                            for (Map<String, Object> map : childrenList)
                            {
                                int lt         = Integer.parseInt(map.get("lt").toString());
                                int rt         = Integer.parseInt(map.get("rt").toString());
                                int level      = Integer.parseInt(map.get("level").toString());
                                int sonUplevel = Integer.parseInt(map.get("uplevel").toString());
                                //下级数量
                                int sonNum = userDistributionModelMapper.childrenNum(vo.getStoreId(), lt, rt, sonUplevel + 1);
                                map.put("soncount", sonNum);
                                //与当前用户差层级
                                map.put("uplevel", sonUplevel - currentUplevel);
                                //获取分销等级
                                String                 gradeName              = "默认级别";
                                DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                                distributionGradeModel.setStore_id(vo.getStoreId());
                                distributionGradeModel.setId(level);
                                distributionGradeModel = distributionGradeModelMapper.selectOne(distributionGradeModel);
                                if (distributionGradeModel != null)
                                {
                                    Map<String, String> gradeSetMap = DataUtils.cast(SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets()));
                                    if (gradeSetMap != null && !gradeSetMap.isEmpty())
                                    {
                                        gradeName = gradeSetMap.get("s_dengjiname");
                                    }
                                }
                                map.put("levelname", gradeName);
                            }
                        }
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYGDL, "没有更多了");
                }
            }
            resultMap.put("list", childrenList);
            resultMap.put("teamNum", team);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的团队 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mygroup");
        }
    }

    @Override
    public Map<String, Object> commList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取当前用户分销信息
            BigDecimal            userAccumulative      = new BigDecimal("0");
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                userAccumulative = userDistributionModel.getAccumulative();
            }
            //获取当前用户佣金明细信息
            List<Map<String, Object>> commissionDetails = userDistributionModelMapper.getCommissionDetail(vo.getStoreId(), user.getUser_id(), vo.getPageNo(), vo.getPageSize());
            for (Map<String, Object> map : commissionDetails)
            {
                //分销员id
                String fromId   = map.get("from_id") + "";
                String fromName = "";
                if (!StringUtils.isEmpty(fromId))
                {
                    User fromUser = new User();
                    fromUser.setStore_id(vo.getStoreId());
                    fromUser.setUser_id(fromId);
                    fromUser = userBaseMapper.selectOne(fromUser);
                    if (fromUser != null)
                    {
                        fromName = fromUser.getUser_name();
                    }
                }
                map.put("from_user", fromName);
                map.put("time", DateUtil.dateFormate(map.get("add_date").toString(), GloabConst.TimePattern.YMDHMS));
                // 禅道 50611 保留两位小数
                map.put("money", new BigDecimal(MapUtils.getString(map, "money")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                map.put("z_price", new BigDecimal(MapUtils.getString(map, "z_price")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
            }
            resultMap.put("list", commissionDetails);
            resultMap.put("total", userAccumulative.toString());
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取佣金明细 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "commList");
        }
    }

    public static void main(String[] args)
    {
        System.out.println(new BigDecimal("3.7").setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Override
    public Map<String, Object> cashList(MainVo vo, Integer status, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取成功提现佣金
            BigDecimal          total    = distributionWithdrawModelMapper.sumWithdrawSuccessAmt(vo.getStoreId(), user.getUser_id());
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("moneyGT", 0);
            if (status != null)
            {
                parmaMap.put("status", status);
            }
            if (id != null)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = distributionWithdrawModelMapper.selectDynamic(parmaMap);

            for (Map<String, Object> map : dataList)
            {
                int           bankId        = MapUtils.getIntValue(map, "Bank_id");
                BankCardModel bankCardModel = bankCardModelMapper.selectByPrimaryKey(bankId);
                String        number        = "";
                String        bankName      = "";
                if (bankCardModel != null)
                {
                    number = bankCardModel.getBank_card_number();
                    number = number.substring(number.length() - 4);
                    bankName = bankCardModel.getBank_name();
                }
                map.put("bank_name", bankName);
                map.put("last", number);
                map.put("money", new BigDecimal(MapUtils.getString(map, "money")).setScale(2, RoundingMode.HALF_DOWN).toString());
                map.put("s_charge", new BigDecimal(MapUtils.getString(map, "s_charge")).setScale(2, RoundingMode.HALF_DOWN).toString());
                map.put("geyUserMoney", new BigDecimal(MapUtils.getString(map, "money"))
                        .subtract(new BigDecimal(MapUtils.getString(map, "s_charge"))).setScale(2, RoundingMode.HALF_DOWN).toString());
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                map.put("examine_date", DateUtil.dateFormate(MapUtils.getString(map, "examine_date"), GloabConst.TimePattern.YMDHMS));
            }

            resultMap.put("res", dataList);
            resultMap.put("list", dataList);
            resultMap.put("total", total);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现明细列表 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "cashList");
        }
    }

    @Override
    public Map<String, Object> cashDetail(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (id == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                resultMap = cashList(vo, null, id);
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取提现详情 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "cashDetail");
        }
    }

    @Override
    public Map<String, Object> intoWithdrawals(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取用户银行卡信息
            List<Map<String, Object>> bankInformationList = new ArrayList<>();

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //获取金额单位
                FinanceConfigModel financeConfigModel = new FinanceConfigModel();
                financeConfigModel.setStore_id(vo.getStoreId());
                financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
                if (financeConfigModel == null)
                {
                    financeConfigModel = new FinanceConfigModel();
                    financeConfigModel.setUnit("元");
                    financeConfigModel.setMin_amount(new BigDecimal("0"));
                    financeConfigModel.setMax_amount(new BigDecimal("99999"));
                    financeConfigModel.setService_charge(new BigDecimal("0"));
                }
                String text = String.format("请输入提现金额(大于%s)", financeConfigModel.getMin_amount().toString());
                //获取当前用户可提现金额
                BigDecimal            txAmt                 = new BigDecimal("0");
                UserDistributionModel userDistributionModel = new UserDistributionModel();
                userDistributionModel.setUser_id(user.getUser_id());
                userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                if (userDistributionModel != null)
                {
                    txAmt = Objects.nonNull(userDistributionModel.getTx_commission()) ? userDistributionModel.getTx_commission() : txAmt;
                    /*if (financeConfigModel.getMax_amount().compareTo(txAmt) >= 0) {
                        financeConfigModel.setMax_amount(txAmt);
                    }*/
                    String              bankInfo          = "";
                    List<BankCardModel> bankCardModelList = bankCardModelMapper.getAllDefaultBankcardByUser(vo.getStoreId(), user.getUser_id());
                    for (BankCardModel userBankCard : bankCardModelList)
                    {
                        Map<String, Object> bankInformationMap = new HashMap<>(16);
                        String              number             = userBankCard.getBank_card_number();
                        bankInfo = String.format("%s尾号(%s)", userBankCard.getBank_name(), number.substring(number.length() - 4));

                        bankInformationMap.put("Cardholder", userBankCard.getCardholder());
                        bankInformationMap.put("Bank_name", userBankCard.getBank_name());
                        bankInformationMap.put("branch", userBankCard.getBranch());
                        bankInformationMap.put("Bank_card_number", bankInfo);
                        bankInformationMap.put("id", userBankCard.getId());
                        bankInformationList.add(bankInformationMap);
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFCS, "非法参数");
                }

                resultMap.put("min_amount", financeConfigModel.getMin_amount().toString());
                resultMap.put("max_amount", financeConfigModel.getMax_amount().toString());
                resultMap.put("cash_charge", financeConfigModel.getService_charge());
                resultMap.put("bank_information", bankInformationList);
                resultMap.put("unit", financeConfigModel.getUnit());
                resultMap.put("mobile", user.getMobile());
                resultMap.put("money", txAmt.toString());
                resultMap.put("pshd", text);
            }
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取提现页面数据 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "intoWithdrawals");
        }
    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public Map<String, Object> withdrawalsApply(MainVo vo, String pcode, String amt, int bankId) throws LaiKeAPIException
//    {
//        Map<String, Object> resultMap = new HashMap<>(16);
//        try
//        {
//            Integer id;
//            User    user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
//            int     count;
//            if (StringUtils.isEmpty(amt))
//            {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JEQSRJE, "金额请输入金额");
//            }
//            //需要提现的金额
//            BigDecimal txMoney = new BigDecimal(amt);
//            //校验数据
//            BigDecimal txAmt = withdrawalsVerification(user, pcode, txMoney, bankId);
//            //开始提现 更新佣金
//            count = userDistributionModelMapper.updateCommission(vo.getStoreId(), user.getUser_id(), txMoney);
//            if (count < 1)
//            {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJGXSB, "佣金更新失败");
//            }
//
//            //记录提现操作
//            DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
//            distributionWithdrawModel.setStore_id(vo.getStoreId());
//            distributionWithdrawModel.setUser_id(user.getUser_id());
//            distributionWithdrawModel.setName(user.getUser_name());
//            distributionWithdrawModel.setMobile(user.getMobile());
//            distributionWithdrawModel.setBank_id(bankId);
//            distributionWithdrawModel.setZ_money(txAmt);
//            distributionWithdrawModel.setMoney(txMoney);
//            distributionWithdrawModel.setS_charge(txMoney.subtract(txAmt));
//            distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
//            distributionWithdrawModel.setIs_mch(0);
//            distributionWithdrawModel.setAdd_date(new Date());
//            count = distributionWithdrawModelMapper.insertSelective(distributionWithdrawModel);
//            if (count < 1)
//            {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
//            }
//            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
//            messageLoggingSave.setMch_id(user.getMchId());
//            messageLoggingSave.setStore_id(vo.getStoreId());
//            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
//            messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_DISTRIBUTION_WITHDRAWAL_URL);
//            messageLoggingSave.setParameter(user.getUser_name());
//            messageLoggingSave.setContent(String.format("ID为%s的分销商申请提取余额，请及时处理！", user.getUser_id()));
//            messageLoggingSave.setAdd_date(new Date());
//            messageLoggingModalMapper.insertSelective(messageLoggingSave);
//            id = distributionWithdrawModel.getId();
//            resultMap.put("withdraw_id", id);
//        }
//        catch (LaiKeAPIException l)
//        {
//            throw l;
//        }
//        catch (Exception e)
//        {
//            logger.error("提现申请 异常:", e);
//            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
//        }
//        return resultMap;
//    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> withdrawalsApply(Withdrawals1Vo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            logger.error("进入分销提现申请服务处实现方法，参数vo为：" + vo.toString());
            Integer id;
            User    user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            int     count;
            if (StringUtils.isEmpty(vo.getAmoney()) || new BigDecimal(vo.getAmoney()).compareTo(BigDecimal.ZERO) == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JEQSRJE, "请输入正确的提现金额");
            }
            //需要提现的金额
            BigDecimal txMoney = new BigDecimal(vo.getAmoney());
            //校验数据
            BigDecimal txAmt = null;
            if (vo.getWithdrawStatus() == 1)
            {
                txAmt = withdrawalsVerification(user, vo.getKeyCode(), txMoney, vo.getBankId());
            }
            else if (vo.getWithdrawStatus() == 3)
            {
                txAmt = withdrawalsVerification(user, txMoney);
            }

            //开始提现 更新佣金
            count = userDistributionModelMapper.updateCommission(vo.getStoreId(), user.getUser_id(), txMoney);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJGXSB, "佣金更新失败");
            }

            //记录提现操作
            DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
            distributionWithdrawModel.setStore_id(vo.getStoreId());
            distributionWithdrawModel.setUser_id(user.getUser_id());
            distributionWithdrawModel.setName(user.getUser_name());
            distributionWithdrawModel.setMobile(user.getMobile());
            if (vo.getBankId() != null)
            {
                distributionWithdrawModel.setBank_id(vo.getBankId());
            }
            distributionWithdrawModel.setZ_money(txAmt);
            distributionWithdrawModel.setS_charge(txMoney.subtract(txAmt));
            distributionWithdrawModel.setMoney(txMoney);
            distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
            distributionWithdrawModel.setIs_mch(0);
            distributionWithdrawModel.setAdd_date(new Date());
            if (vo.getWithdrawStatus() == 3)
            {
                if (StringUtils.isEmpty(vo.getEmail()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRPPYXDZ,"请输入paypal邮箱地址");
                }
                distributionWithdrawModel.setEmail(vo.getEmail());
                distributionWithdrawModel.setWithdrawStatus(vo.getWithdrawStatus());
            }

            count = distributionWithdrawModelMapper.insertSelective(distributionWithdrawModel);
            logger.error("分销提现表插入情况：", count);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
            }
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(user.getMchId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_USER_WITHDRAWAL);
            messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.USER_DISTRIBUTION_WITHDRAWAL_URL);
            messageLoggingSave.setParameter(user.getUser_name());
            messageLoggingSave.setContent(String.format("ID为%s的分销商申请提取余额，请及时处理！", user.getUser_id()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            id = distributionWithdrawModel.getId();
            resultMap.put("withdraw_id", id);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("提现申请未成功:", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现申请 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getRanking(MainVo vo, Integer id, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", type);
            if (id != null)
            {
                parmaMap.put("rankId", id);
            }
            parmaMap.put("pageStart", 0);
            parmaMap.put("pageEnd", 10);
            parmaMap.put("commission_sort", "desc");

            //获取个人排名信息 前十
            List<Map<String, Object>> list = distributionRankingModelMapper.selectUserDynamic(parmaMap);
            //排名
            String rangkingTop10 = "未入榜";
            //佣金
            BigDecimal yj = new BigDecimal("0");
            if (user != null)
            {
                //是否入榜
                parmaMap.put("user_id", user.getUser_id());
                List<Map<String, Object>> listTemp = distributionRankingModelMapper.selectUserDynamic(parmaMap);
                if (listTemp == null || listTemp.size() < 1)
                {
                    //前十未找到则继续找
                    String              date = DateUtil.getSpanDate(type, true);
                    Map<String, Object> map  = distributionRankingModelMapper.selectUserRealRanking(user.getUser_id(), vo.getStoreId(), date);
                    if (map != null && !map.isEmpty())
                    {
                        int orderNo = Integer.parseInt(map.get("orderNo").toString()) + 10;
                        rangkingTop10 = orderNo + "";
                        yj = new BigDecimal(map.get("commission").toString());
                    }
                }
                else
                {
                    //入榜则获取排行位置
                    int i = 1;
                    for (Map<String, Object> map : list)
                    {
                        String userId = map.get("user_id").toString();
                        if (user.getUser_id().equals(userId))
                        {
                            rangkingTop10 = i + "";
                            yj = new BigDecimal(map.get("commission").toString());
                            break;
                        }
                        i++;
                    }
                }
            }
            resultMap.put("orderNo", rangkingTop10);
            resultMap.put("commission", yj.toString());
            resultMap.put("ranking", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取佣金排行榜 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRanking");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> promoteOrder(MainVo vo, String search, String userId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(vo.getStoreId());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            int rank = 0;
            List<String> userIdList = new ArrayList<>();
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (Objects.nonNull(distributionConfigModel) && Objects.nonNull(distributionConfigModel.getSets()))
            {
                Map<String,Object> configMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                if (configMap.containsKey(DistributionConfigModel.SetsKey.C_CENGJI))
                {
                    rank = Integer.parseInt(String.valueOf(configMap.get(DistributionConfigModel.SetsKey.C_CENGJI)));
                }
            }
            if (Objects.isNull(userDistributionModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            //int storeId, int lt, int rt, int uplecel, int start, int end
            List<String> allChildrenList = userDistributionModelMapper.getAllChildrenList(vo.getStoreId(), userDistributionModel.getLt(), userDistributionModel.getRt(), userDistributionModel.getLevel(), userDistributionModel.getLevel() + rank);
            userIdList.add(user.getUser_id());
            userIdList.addAll(allChildrenList);
            logger.info("userId:::::::{}",userIdList);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", 1);
            parmaMap.put("genreLT", 3);
            parmaMap.put("oType",DictionaryConst.OrdersType.ORDERS_HEADER_FX);
            if (StringUtils.isNotEmpty(search))
            {
                parmaMap.put("search", search);
            }
            parmaMap.put("userIdList",userIdList);
            List<Integer> statusList = new ArrayList<>();
            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED);
            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
            parmaMap.put("statusList", statusList);
            //不查询自己的订单 36006 -->改为查询全部37188
            //parmaMap.put("notOrderUserId", user.getUser_id());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("group", "group");
            //获取当前用户下级所产生的订单
            //select a.*, b.z_price, b.add_time, c.p_name, c.num, d.img, d.attribute, f.user_name FROM lkt_distribution_record as a LEFT JOIN lkt_order as b ON a.sNo = b.sNo LEFT JOIN lkt_order_details as c ON a.sNo = c.r_sNo LEFT JOIN lkt_configure as d ON c.sid = d.id LEFT JOIN lkt_user_distribution as e ON a.from_id = e.user_id LEFT JOIN lkt_user as f ON a.from_id = f.user_id WHERE a.store_id = 1 and a.user_id = 'xx' and a.type = 1 and a.genre < 3 and b.status in( 1 , 2 , 5) group by a.sNo LIMIT 0,10;
            int                       total = distributionRecordModelMapper.countRecordDynamic(parmaMap);
            List<Map<String, Object>> list  = distributionRecordModelMapper.selectRecordDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                //产品图
                String     img   = MapUtils.getString(map, "img");
                if (StringUtils.isNotEmpty(img))
                {
                    img = publiceService.getImgPath(img, vo.getStoreId());
                }
                map.put("img", img);
                //商品属性处理
                map.put("size", GoodsDataUtils.getProductSkuValue(MapUtils.getString(map, "attribute")));
                //获取分销员信息
                String fromId = MapUtils.getString(map,"from_id",user.getUser_id());
                parmaMap.clear();
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("userId", fromId);
                List<Map<String, Object>> formInfo = userDistributionModelMapper.selectUserDistributionInfo(parmaMap);
                for (Map<String, Object> formMap : formInfo)
                {
                    String fromName = formMap.get("user_name").toString();
                    Integer level = MapUtils.getInteger(formMap, "level", 0);
                    //用户头像
                    map.put("userHeadImg", MapUtils.getString(formMap, "headimgurl"));

                    if (StringUtils.isEmpty(fromName))
                    {
                        fromName = user.getUser_name();
                    }
                    map.put("user_name", fromName);
                    if (level > 0)
                    {
                        //获取等级名
                        String                 gradeName              = "";
                        DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                        distributionGradeModel.setStore_id(vo.getStoreId());
                        distributionGradeModel.setId(level);
                        distributionGradeModel = distributionGradeModelMapper.selectOne(distributionGradeModel);
                        if (distributionGradeModel != null)
                        {
                            Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets());
                            gradeName = String.valueOf(gradeMap.get("s_dengjiname"));
                        }
                        map.put("grade", gradeName);
                    }
                }
                //购买时间
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                //保留两位小数  禅道 50685
                map.put("z_price", new BigDecimal(MapUtils.getString(map, "z_price")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                //销售业绩处理
                //allamount = allamount.add(DataUtils.getBigDecimalVal(map, "money"));
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
            resultMap.put("allamount", userDistributionModel.getAllamount());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("推广订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "promoteOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getStart(MainVo vo, String productTitle, Integer distributorId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            //获取所有分销等级
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> disGradeList = distributionGradeModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : disGradeList)
            {
                String              sets     = map.get("sets").toString();
                Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(sets);
                map.put("levelname", gradeMap.get("s_dengjiname"));
            }

            //等级信息
            Map<String, Object> userGradeinfo = null;
            Map<String, Object> gradeMap      = null;
            BigDecimal          directMtype   = new BigDecimal("0");
            BigDecimal          directM       = new BigDecimal("0");
            User                user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //当前分销身份信息
            //当前分销身份信息
            DistributionGradeModel distributionGrade = null;
            if (user != null)
            {
                //获取当前用户分销信息
                distributionGrade = distributionGradeModelMapper.getUserDistribution(user);
            }
            if (distributionGrade == null)
            {
                //不是分销身份则获取商城top1分销人
                distributionGrade = distributionGradeModelMapper.getUserTop1Distribution(vo.getStoreId());
            }
            if (distributionGrade == null)
            {
                logger.error("商城【{}】分销配置有问题：未配置分销等级!", vo.getStoreId());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSZFXDJ, "请设置分销等级", "getGoodsList");
            }
            //如果没有获取到会员等级信息,则默认一个
            if (userGradeinfo == null || userGradeinfo.isEmpty())
            {
                //获取默认等级信息
                parmaMap.clear();
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("id_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("pageStart", 0);
                parmaMap.put("pageEnd", 1);
                List<Map<String, Object>> list = userGradeModelMapper.selectDynamic(parmaMap);
                if (list != null && list.size() > 0)
                {
                    userGradeinfo = list.get(0);
                }
            }
            //组装数据
            if (userGradeinfo != null && !userGradeinfo.isEmpty())
            {
                gradeMap = SerializePhpUtils.getDistributionGradeBySets(String.valueOf(userGradeinfo.get("sets")));
                directMtype = new BigDecimal(String.valueOf(gradeMap.get("direct_m_type")));
                directM = new BigDecimal(String.valueOf(gradeMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
            }
            //获取分销商品信息
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            if (distributorId != null)
            {
                parmaMap.put("uplevel1", distributorId);
            }
            if (!StringUtils.isEmpty(productTitle))
            {
                parmaMap.put("goodsName", productTitle);
            }
            parmaMap.put("uplevel", "0");
            parmaMap.put("pid_group", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> goodsList = distributionGoodsModelMapper.selectGoodsInfo(parmaMap);
            List<Map<String, Object>> goodsDisDiyRules = null;
            //分销商品对应的等级折扣 100 为无折扣
            BigDecimal fxGoodsLevelDiscount = new BigDecimal(100);
            for (Map<String, Object> map : goodsList)
            {
                String imgUrl = publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId());
                map.put("imgurl", imgUrl);

                //将直推佣金恢复成等级规则的直推佣金
                directM = directMtype;
                //是否是自定义规则
                if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM))
                {
                    goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(map.get("rules_set").toString());
                    if (Objects.isNull(goodsDisDiyRules))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                    }

                    //当前用户分销等级ID
                    if (distributionGrade.getId() != null)
                    {
                        int fxLevelId = distributionGrade.getId();
                        for (int i = 0; i < goodsDisDiyRules.size(); i++)
                        {
                            Map setsMap = (Map) goodsDisDiyRules.get(i);
                            int tempGid = MapUtils.getInteger(setsMap, "id");
                            if (tempGid == fxLevelId)
                            {
                                Double gradeDiydisCount = MapUtils.getDouble(setsMap, "diy_discount");
                                directMtype = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_MODE_TYPE)));
                                directM = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
                                fxGoodsLevelDiscount = BigDecimal.valueOf(gradeDiydisCount);
                                logger.info("商品->{} 对应的自定义分销折扣->{}", JSON.toJSONString(map), gradeDiydisCount);
                                break;
                            }
                        }
                    }
                }
                //分销等级规则
                else if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_LEVEL))
                {
                    fxGoodsLevelDiscount = distributionGrade.getDiscount();
                }
                //分销价格
                BigDecimal fxPrice;
                //商品价格
                BigDecimal goodsPrice = new BigDecimal(map.get("price").toString());
                BigDecimal price      = BigDecimal.ZERO;
                //计算分销商折扣价
                if (user != null)
                {
                    if (fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0)
                    {
                        price = fxGoodsLevelDiscount.multiply(goodsPrice).multiply(new BigDecimal(0.01));
                    }
                    else
                    {
                        price = publiceDistributionService.getGoodsPrice(vo.getStoreId(), user.getUser_id(), goodsPrice);
                    }
                }
                else
                {
                    price = goodsPrice;
                }
                //最高可获取的金额
                if (directMtype.compareTo(BigDecimal.ONE) == 0)
                {
                    //固定值
                    fxPrice = directM;
                }
                else
                {
                    //商品pv值
                    BigDecimal pv = new BigDecimal(MapUtils.getString(map, "pv"));
                    //商品成本价
                    BigDecimal costPrice = new BigDecimal(MapUtils.getString(map, "costprice"));
                    //获取分润基值
                    BigDecimal profit = publiceDistributionService.getProfit(vo.getStoreId(), price, costPrice, null, BigDecimal.ONE, pv);
                    //百分比
                    fxPrice = profit.multiply(directM.multiply(new BigDecimal("0.01")));
                }
                if (BigDecimal.ZERO.compareTo(fxPrice) >= 0)
                {
                    fxPrice = BigDecimal.ZERO;
                }
                /*if (directMtype.doubleValue() == 0)
                {
                    fxPrice = price.multiply(directM).multiply(new BigDecimal("0.01"));
                }
                else
                {
                    fxPrice = directM;
                }
                if (user != null)
                {
                    map.put("price", publiceDistributionService.getGoodsPrice(vo.getStoreId(), user.getUser_id(), price));
                }*/
                map.put("goodsPrice", goodsPrice);
                map.put("fx_price", fxPrice);
                map.put("price", price.setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            resultMap.put("level", disGradeList);
            resultMap.put("pro", goodsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取礼包列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getstart");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getGoodsList(MainVo vo, String productTitle, String sortKey, int queue, Integer cid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            resultMap = publiceDistributionService.getGoodsList(vo,productTitle,sortKey,queue,cid,user);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分销商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getClass(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> goodsClassList = new ArrayList<>();
            List<Integer>             classIds       = distributionGoodsModelMapper.selectDistributionGoodsClassInfo(vo.getStoreId());
            for (Integer id : classIds)
            {
                String className = "";
                //获取类别名称
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setStore_id(vo.getStoreId());
                productClassModel.setCid(id);
                productClassModel = productClassModelMapper.selectOne(productClassModel);
                if (productClassModel != null)
                {
                    className = productClassModel.getPname();
                }
                Map<String, Object> map = new HashMap<>(16);
                map.put("id", id);
                map.put("name", className);
                goodsClassList.add(map);
            }

            resultMap.put("product_class", goodsClassList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取分销商品所有类别 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClass");
        }
        return resultMap;
    }

    /**
     * 提现数据验证 银行卡
     *
     * @param user   -
     * @param pcode  -
     * @param txAmt  -
     * @param bankId -
     * @return BigDecimal - 最终可以提现的金额
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 17:21
     */
    private BigDecimal withdrawalsVerification(User user, String pcode, BigDecimal txAmt, int bankId) throws LaiKeAPIException
    {
        try
        {
            int count;
            if (bankId == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "请绑定银行卡");
            }
            //验证短信
            RedisDataTool.verificationPcode(GloabConst.VcodeCategory.DRAWING_CODE, user.getMobile(), pcode, redisUtil);
            //获取金额单位
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(user.getStore_id());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            if (financeConfigModel == null)
            {
                financeConfigModel = new FinanceConfigModel();
                financeConfigModel.setUnit("元");
                financeConfigModel.setMin_amount(new BigDecimal("0"));
                financeConfigModel.setMax_amount(new BigDecimal("999"));
                financeConfigModel.setService_charge(new BigDecimal("0"));
            }
            //验证银行卡
            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setStore_id(user.getStore_id());
            bankCardModel.setUser_id(user.getUser_id());
            bankCardModel.setId(bankId);
            count = bankCardModelMapper.selectCount(bankCardModel);
            if (count < 1)
            {
                logger.warn("userid{}正在提现分佣金额,使用了他人银行卡id:{}", user.getUser_id(), bankId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFCS, "非法参数");
            }

            //查询可提现金额
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYKTXJE, "没有可提现金额");
            }
            double txCommission = Objects.nonNull(userDistributionModel.getTx_commission()) ? userDistributionModel.getTx_commission().doubleValue() : 0.00d ;
            //提现金额是否>0或者大于可提现金额
            if (txAmt.doubleValue() <= 0 || txAmt.doubleValue() > txCommission)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QKJESFXYHDYHDYXYJE, "取款金额是否小于或等于0或大于现有金额");
            }
            //提现金额小于最小提现金额
            if (txAmt.doubleValue() < financeConfigModel.getMin_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYZXTXJE, "提现金额小于最小提现金额");
            }
            // 提现金额大于最大提现金额
            if (txAmt.doubleValue() > financeConfigModel.getMax_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEDYZDTXJE, "提现金额大于最大提现金额");
            }
            // 提现金额小于等于手续费
            if (txAmt.doubleValue() <= financeConfigModel.getService_charge().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYDYSXF, "提现金额小于等于手续费");
            }
            //查询用户之前是否有未完成的提现申请
            DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
            distributionWithdrawModel.setStore_id(user.getStore_id());
            distributionWithdrawModel.setUser_id(user.getUser_id());
            distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
            count = distributionWithdrawModelMapper.selectCount(distributionWithdrawModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NHYYBTXZZSHZ, "您还有一笔提现正在审核中");
            }
            //一天只允许提现一次
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", user.getStore_id());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("status", DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_PASS);
            parmaMap.put("startDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            parmaMap.put("endDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            count = distributionWithdrawModelMapper.countDynamic(parmaMap);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YTZYXTXYC, "一天只允许提现一次");
            }
            //获取手续费
            BigDecimal serverAmt = txAmt.multiply(financeConfigModel.getService_charge());
            //四舍五入保留两位小数
            serverAmt = serverAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
            //扣除手续费
            txAmt = txAmt.subtract(serverAmt);
            return txAmt;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("提现数据验证 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
        }
    }

    /**
     * 提现数据验证 非银行卡
     *
     * @param user
     * @param txAmt
     * @return
     * @throws LaiKeAPIException
     */
    private BigDecimal withdrawalsVerification(User user, BigDecimal txAmt) throws LaiKeAPIException
    {
        try
        {
            int count;


            //获取金额单位
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(user.getStore_id());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            if (financeConfigModel == null)
            {
                financeConfigModel = new FinanceConfigModel();
                financeConfigModel.setUnit("元");
                financeConfigModel.setMin_amount(new BigDecimal("0"));
                financeConfigModel.setMax_amount(new BigDecimal("999"));
                financeConfigModel.setService_charge(new BigDecimal("0"));
            }


            //查询可提现金额
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYKTXJE, "没有可提现金额");
            }
            double txCommission = Objects.nonNull(userDistributionModel.getTx_commission()) ? userDistributionModel.getTx_commission().doubleValue() : 0.00d ;
            //提现金额是否>0或者大于可提现金额
            if (txAmt.doubleValue() <= 0 || txAmt.doubleValue() > txCommission)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QKJESFXYHDYHDYXYJE, "取款金额是否小于或等于0或大于现有金额");
            }
            //提现金额小于最小提现金额
            if (txAmt.doubleValue() < financeConfigModel.getMin_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYZXTXJE, "提现金额小于最小提现金额");
            }
            // 提现金额大于最大提现金额
            if (txAmt.doubleValue() > financeConfigModel.getMax_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEDYZDTXJE, "提现金额大于最大提现金额");
            }
            // 提现金额小于等于手续费
            if (txAmt.doubleValue() <= financeConfigModel.getService_charge().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYDYSXF, "提现金额小于等于手续费");
            }
            //查询用户之前是否有未完成的提现申请
            DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
            distributionWithdrawModel.setStore_id(user.getStore_id());
            distributionWithdrawModel.setUser_id(user.getUser_id());
            distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
            count = distributionWithdrawModelMapper.selectCount(distributionWithdrawModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NHYYBTXZZSHZ, "您还有一笔提现正在审核中");
            }
            //一天只允许提现一次
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", user.getStore_id());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("status", DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_PASS);
            parmaMap.put("startDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            parmaMap.put("endDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            count = distributionWithdrawModelMapper.countDynamic(parmaMap);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YTZYXTXYC, "一天只允许提现一次");
            }
            //获取手续费
            BigDecimal serverAmt = txAmt.multiply(financeConfigModel.getService_charge());
            //四舍五入保留两位小数
            serverAmt = serverAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
            //扣除手续费
            txAmt = txAmt.subtract(serverAmt);
            return txAmt;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("提现数据验证 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
        }
    }
}

