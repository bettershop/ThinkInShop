package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchFinanceService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.BankTool;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.mch.FinanceVo;
import com.laiketui.domain.vo.user.AddBankVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 资金管理
 *
 * @author Trick
 * @date 2021/6/7 9:40
 */
@Service
public class MchFinanceServiceImpl implements MchFinanceService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private BankTool bankTool;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;


    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取账户余额
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }

            //获取提现配置(自营店)
            Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), storeMchId);
            if (mchConfigModel != null)
            {
                resultMap.put("min_charge", mchConfigModel.getMin_charge());
                resultMap.put("max_charge", mchConfigModel.getMax_charge());
                resultMap.put("service_charge", mchConfigModel.getService_charge().multiply(BigDecimal.valueOf(100)));
                resultMap.put("illustrate", mchConfigModel.getIllustrate());
            }

            resultMap.put("money", mchModel.getAccount_money());
            resultMap.put("cashable_money", mchModel.getCashable_money());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("资金管理首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> withdrawList(MainVo vo, Integer status, Integer id, String startDate, String endDate, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取账户余额
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("is_mch", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("mch_id", user.getMchId());
            if (status != null)
            {
                parmaMap.put("status", status);
            }
            if (!StringUtils.isEmpty(startDate))
            {
                parmaMap.put("startDate", startDate);
            }
            if (!StringUtils.isEmpty(endDate))
            {
                parmaMap.put("endDate", endDate);
            }
            if (!StringUtils.isEmpty(id))
            {
                parmaMap.put("wid", id);
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total    = withdrawModelMapper.countWithdrawLeftUserBank(parmaMap);
            List<Map<String, Object>> dataList = withdrawModelMapper.getWithdrawLeftUserBank(parmaMap);

            for (Map<String, Object> map : dataList)
            {
                BigDecimal money       = new BigDecimal(MapUtils.getString(map, "money"));
                BigDecimal serverMoney = new BigDecimal(MapUtils.getString(map, "s_charge"));
                money = money.subtract(serverMoney);
                map.put("arrivalMoney", money);
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
            }

            if (vo.getExportType() == 1)
            {
                exportWithdrawData(dataList, response);
                return null;
            }
            //提现提示
            String PopUpContent = "";
            //获取提现配置(自营店)
            Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), storeMchId);
            if (mchConfigModel != null)
            {
                if (Objects.nonNull(mchConfigModel.getMin_charge()))
                {
                    resultMap.put("min_charge", mchConfigModel.getMin_charge());
                }
                if (Objects.nonNull(mchConfigModel.getMax_charge()))
                {
                    resultMap.put("max_charge", mchConfigModel.getMax_charge());
                }
                if (Objects.nonNull(mchConfigModel.getService_charge()))
                {
                    resultMap.put("service_charge", mchConfigModel.getService_charge().multiply(BigDecimal.valueOf(100)));
                }
                if (Objects.nonNull(mchConfigModel.getIllustrate()))
                {
                    resultMap.put("illustrate", mchConfigModel.getIllustrate());
                }

                if (Objects.nonNull(mchConfigModel.getWithdrawal_time()) && Objects.nonNull(mchConfigModel.getWithdrawal_time_open()))
                {
                    //提现时间
                    String withdrawal_time = mchConfigModel.getWithdrawal_time();
                    //当前时间日  没有前置0
                    Integer dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME))
                    {
                        if (!dayOfMonth.equals(Integer.valueOf(withdrawal_time)))
                        {
                            PopUpContent = "每月" + withdrawal_time + "日可提现，请提现日再操作";
                        }
                    }
                    else if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME_PERIOD))
                    {
                        String[] split = withdrawal_time.split("-");
                        if (dayOfMonth < Integer.parseInt(split[0]) || dayOfMonth > Integer.parseInt(split[1]))
                        {
                            PopUpContent = "每月" + split[0] + "日至" + split[1] + "日可提现，请提现日再操作";
                        }
                    }
                }
            }
            //是否缴纳保证金
            boolean isPayment = publicMchService.judgeMchPromise(vo, user.getUser_id());
            //是否存在保证金审核
            boolean        examineStatus = false;
            PromiseShModel oldPromise    = new PromiseShModel();
            oldPromise.setMch_id(user.getMchId());
            oldPromise.setIs_pass(3);
            int i = promiseShModelMapper.selectCount(oldPromise);
            if (i > 0)
            {
                examineStatus = false;
            }
            else
            {
                examineStatus = true;
            }
            resultMap.put("isPromiseExamine", examineStatus);
            resultMap.put("is_Payment", isPayment);
            resultMap.put("list", dataList);
            resultMap.put("total", total);
            resultMap.put("money", mchModel.getAccount_money());
            resultMap.put("cashable_money", mchModel.getCashable_money());
            resultMap.put("PopUpContent", PopUpContent);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawList");
        }
        return resultMap;
    }


    //导出提现记录
    private void exportWithdrawData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"提现金额", "手续费", "到账金额", "银行卡号", "开户银行", "状态", "提现时间", "备注"};
            //对应字段
            String[]     kayList = new String[]{"money", "s_charge", "arrivalMoney", "Bank_card_number", "Bank_name", "type_name", "add_date", "refuse"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("提现记录");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }


    @Override
    public Map<String, Object> revenueRecords(FinanceVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("status", vo.getStatus());
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDay", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDay", vo.getEndDate());
            }
            if (StringUtils.isNotEmpty(vo.getOid()))
            {
                parmaMap.put("orderNo", vo.getOid());
            }
            //入账总金额
            //parmaMap.put("type","1");
            BigDecimal income = mchAccountLogModelMapper.countMchAccountMoney(parmaMap);
            //parmaMap.put("type","2");
            //出账总金额
            BigDecimal outcome = mchAccountLogModelMapper.countMchAccountMoney(parmaMap);
            parmaMap.put("type", vo.getType());

            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> financeList = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);
            int                       total       = mchAccountLogModelMapper.countMchAccountLogDynamic(parmaMap);

            for (Map<String, Object> map : financeList)
            {
                map.put("addtime", DateUtil.dateFormate(MapUtils.getString(map, "addtime"), GloabConst.TimePattern.YMDHMS));
                // 1.订单 2.退款 3.提现
                int    type = MapUtils.getIntValue(map, "type");
                String typeName;
                switch (type)
                {
                    case 1:
                        typeName = "订单";
                        break;
                    case 2:
                        typeName = "退款";
                        break;
                    case 3:
                        typeName = "提现";
                        break;
                    case 4:
                        typeName = "保证金";
                        break;
                    case 5:
                        typeName = "供应商";
                        break;
                    case 6:
                        typeName = "拼团佣金";
                        break;
                    default:
                        typeName = "-";
                        break;
                }
                map.put("typeName", typeName);
            }

            if (vo.getExportType() == 1)
            {
                exportOrderData(financeList, vo.getResponse(), vo.getStatus() == 2);
                return null;
            }

            resultMap.put("list", financeList);
            resultMap.put("total", total);
            resultMap.put("income", income);
            resultMap.put("outcome", outcome);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("出/入账记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "revenueRecords");
        }
        return resultMap;
    }

    //导出订单列表
    private void exportOrderData(List<Map<String, Object>> list, HttpServletResponse response, boolean isOut) throws LaiKeAPIException
    {
        try
        {
            String title = "出账";
            if (!isOut)
            {
                title = "入账";
            }
            //表头
            String[] headerList = new String[]{"id", title + "金额", title + "类型", title + "时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "price", "typeName", "addtime"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle(title + "记录");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Override
    public Map<String, Object> withdrawPage(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取店铺信息
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }
            List<Map<String, Object>> mchBankcardList = bankCardModelMapper.selectBankcardByMch(vo.getStoreId(), user.getMchId());
            for (Map<String, Object> map : mchBankcardList)
            {
                String bankcardNo = MapUtils.getString(map, "Bank_card_number");
                String bandName   = bankTool.getBankNameByNo(bankcardNo);
                map.put("Bank_card_number", bankcardNo.substring(bankcardNo.length() - 4));
                map.put("bandName", bandName);
            }
            //获取提现配置(自营店)
            Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), storeMchId);
            if (mchConfigModel != null)
            {
                if (Objects.isNull(mchConfigModel.getMin_charge()) || Objects.isNull(mchConfigModel.getMax_charge()) || Objects.isNull(mchConfigModel.getService_charge()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPTXWPZ,"店铺提现未配置");
                }
                resultMap.put("min_charge", mchConfigModel.getMin_charge());
                resultMap.put("max_charge", mchConfigModel.getMax_charge());
                resultMap.put("service_charge", mchConfigModel.getService_charge().multiply(BigDecimal.valueOf(100)));
                resultMap.put("illustrate", mchConfigModel.getIllustrate());
            }
            Integer wechatV3Withdraw = paymentConfigModelMapper.getPaymentTypeInfoByClassName(vo.getStoreId(), "wechat_v3_withdraw");
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
            resultMap.put("list", mchBankcardList);
            resultMap.put("tel", mchModel.getTel());
            resultMap.put("cpc", mchModel.getCpc());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawPage");
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setShopId(user.getMchId());
            if (vo.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.YHK))
            {
                //校验验证码
                publiceService.validatePhoneCode(GloabConst.RedisHeaderKey.DRAWING_CODE, vo.getMobile(), vo.getKeyCode());
            }
            if (!publiceService.withdrawals(vo, user))
            {
                logger.warn("pc店铺提现记录失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSBQSHZS, "提现失败,请稍后再试");
            }
            //管理后台审核通知
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(user.getMchId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
            messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.STORE_WITHDRAWAL_URL);
            messageLoggingSave.setParameter(user.getUser_name());
            messageLoggingSave.setContent(String.format("ID为%s的店铺申请提取余额，请及时处理！", user.getUser_id()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //h5店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
            messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(user.getMchId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url("");
            messageLoggingSave.setParameter("提现申请已提交");
            messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //pc店铺消息通知(您的店铺提现申请已提交成功，正在等待管理员审核！)
            messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(user.getMchId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setParameter("");
            messageLoggingSave.setContent("您的店铺提现申请已提交成功，正在等待管理员审核！");
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //删除短信
            redisUtil.del(GloabConst.RedisHeaderKey.DRAWING_CODE + vo.getMobile());
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "申请了提现操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawList");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> withdrawalsSms(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取店铺信息
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }
            if (!publiceService.sendSms(vo.getStoreId(), mchModel.getTel(), GloabConst.VcodeCategory.TYPE_VERIFICATION, GloabConst.VcodeCategory.DRAWING_CODE, null))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DXFSSB, "短信发送失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bankList(MainVo vo, Integer id, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", id);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (StringUtils.isNotEmpty(startDate))
            {
                parmaMap.put("startDate", startDate);
                parmaMap.put("endDate", endDate);
            }
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total    = bankCardModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> bankList = bankCardModelMapper.selectDynamic(parmaMap);

            resultMap.put("list", bankList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("银行卡列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bankList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(MainVo vo, int bankId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            BankCardModel bankCardOld = bankCardModelMapper.selectByPrimaryKey(bankId);
            if (bankCardOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }
            if (bankCardOld.getIs_default() == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZSBLYGMRYXK, "至少保留一个默认银行卡");
            }

            //清空之前默认
            int           row           = bankCardModelMapper.clearDefaultByMchId(vo.getStoreId(), user.getMchId());
            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            bankCardModel.setId(bankId);
            row = bankCardModelMapper.updateByPrimaryKeySelective(bankCardModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
            }

            publiceService.addAdminRecord(vo.getStoreId(), "将银行卡账户：" + bankCardOld.getBank_card_number() + " 设为默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认银行卡 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBank(AddBankVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //验证银行卡信息
            verificationBank(vo, vo.getBankName(), vo.getBankCardNumber());

            BankCardModel bankCardOld = null;
            if (vo.getId() != null)
            {
                bankCardOld = bankCardModelMapper.selectByPrimaryKey(vo.getId());
                if (bankCardOld.getIs_default() == DictionaryConst.DefaultMaven.DEFAULT_OK && vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_NO)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRYXKWFXG, "默认银行卡无法修改");
                }
            }

            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setStore_id(vo.getStoreId());
            bankCardModel.setMch_id(user.getMchId());
            bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);

            //查询是否存在默认银行卡,没有默认则这个银行卡为默认
            bankCardModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            BankCardModel bankCardModelTemp = bankCardModelMapper.selectOne(bankCardModel);
            if (bankCardModelTemp != null)
            {
                BankCardModel bankCardModelUpdate = new BankCardModel();
                bankCardModelUpdate.setId(bankCardModelTemp.getId());
                //判断此银行卡是否为默认
                if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
                {
                    //清空之前默认
                    bankCardModelUpdate.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                    if (bankCardModelMapper.updateByPrimaryKeySelective(bankCardModelUpdate) < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
                    }
                }
                else
                {
                    bankCardModel.setIs_default(0);
                }
            }
            bankCardModel.setBank_card_number(vo.getBankCardNumber());
            if (bankCardOld == null || !bankCardOld.getBank_card_number().equals(vo.getBankCardNumber()))
            {
                if (bankCardModelMapper.selectCount(bankCardModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKYCZ, "银行卡已存在");
                }
            }
            bankCardModel.setMch_id(user.getMchId());
            bankCardModel.setCardholder(vo.getCardholder());
            bankCardModel.setBank_name(vo.getBankName());
            bankCardModel.setBranch(vo.getBranchName());
            //添加银行卡
            int count;
            if (vo.getId() != null)
            {
                bankCardModel.setId(vo.getId());
                count = bankCardModelMapper.updateByPrimaryKeySelective(bankCardModel);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了银行卡账户：" + vo.getBankCardNumber(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                bankCardModel.setAdd_date(new Date());
                count = bankCardModelMapper.insertSelective(bankCardModel);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了银行卡账户：" + vo.getBankCardNumber(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加银行卡 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bankList");
        }
    }

    @Override
    public void verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException
    {
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
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delBank(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            BankCardModel bankCardModel = bankCardModelMapper.selectByPrimaryKey(id);
            if (bankCardModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }
            if (bankCardModel.getIs_default() == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRSJWFSC, "默认数据无法删除");
            }
            BankCardModel bankCardUpdate = new BankCardModel();
            bankCardUpdate.setId(bankCardModel.getId());
            bankCardUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            int count = bankCardModelMapper.updateByPrimaryKeySelective(bankCardUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了银行卡账户：" + bankCardModel.getBank_card_number(), AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除银行卡 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBank");
        }
    }

    @Override
    public Map<String, Object> returnDetail(MainVo vo, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("status", DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
            parmaMap.put("type", DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND);
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("startDay", startDate);
            parmaMap.put("endDay", endDate);
            int                       total = mchAccountLogModelMapper.countMchAccountLogDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取售后明细 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnDetail");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> PromiseList(MainVo vo, String startDate, String endDate, HttpServletResponse response, String type, String proStatus) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("startDate", startDate);
            parmaMap.put("endDate", endDate);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("type", type);
            parmaMap.put("status", proStatus);
            int                       total       = promiseRecordModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> promiseList = new ArrayList<>();
            if (total > 0)
            {
                promiseList = promiseRecordModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : promiseList)
                {
                    String addDate = MapUtils.getString(map, "add_date");
                    map.put("add_date", DateUtil.dateFormate(addDate, GloabConst.TimePattern.YMDHMS));
                    if (map.get("money") != null)
                    {
                        map.put("money", new BigDecimal(MapUtils.getString(map, "money")).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
                    }

                }
            }
            //导出
            if (vo.getExportType() == 1)
            {
                exportPromiseList(promiseList, response);
                return null;
            }
            boolean isPromisePay = false;
            //是否缴纳保证金
            try
            {
                isPromisePay = publicMchService.isPromisePay(vo, user.getUser_id());
            }
            catch (LaiKeAPIException l)
            {
                logger.debug(l.getMessage());
            }
            resultMap.put("isPromisePay", isPromisePay);
            //判断保证金是否在退还中
            boolean             examineStatus = true;
            Map<String, Object> first         = promiseShModelMapper.getFirst(user.getMchId());
            Integer             id            = MapUtils.getInteger(first, "id");
            if (!Objects.isNull(id))
            {
                Integer status = MapUtils.getInteger(first, "status");
                if (status == 3)
                {
                    examineStatus = false;
                }
            }
            resultMap.put("isPromiseExamine", examineStatus);
            //是否有未完成的订单
            int orderNum = mchModelMapper.countMchUnfinishedOrder(vo.getStoreId(), user.getUser_id());
            if (orderNum > 0)
            {
                resultMap.put("haveOrder", false);
            }
            else
            {
                resultMap.put("haveOrder", true);
            }
            //有商品不能退款
            ProductListModel productListCount = new ProductListModel();
            productListCount.setStore_id(vo.getStoreId());
            productListCount.setMch_id(user.getMchId());
            productListCount.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            if (productListModelMapper.selectCount(productListCount) > 0)
            {
                resultMap.put("haveProduct", false);
            }
            else
            {
                resultMap.put("haveProduct", true);
            }
            MchPromiseModel mchPromiseModel = new MchPromiseModel();
            mchPromiseModel.setMch_id(user.getMchId());
            mchPromiseModel.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseModel = mchPromiseModelMapper.selectOne(mchPromiseModel);
            if (mchPromiseModel != null)
            {
                //保证金金额
                resultMap.put("promisePrice", mchPromiseModel.getPromise_amt());
                resultMap.put("payDate", DateUtil.dateFormate(mchPromiseModel.getAdd_date(), GloabConst.TimePattern.YMDHMS));
            }
            resultMap.put("list", promiseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金管理页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "promiseManage");
        }
        return resultMap;
    }

    //导出保证金记录
    private void exportPromiseList(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"序号", "保证金金额", "退还/缴纳", "审核状态", "操作时间", "备注"};
            //对应字段
            String[]     kayList = new String[]{"id", "money", "typeName", "statusName", "add_date", "remarks"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("保证金记录");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertPromisePrice(MainVo vo)
    {
        try
        {
            User            user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(user.getMchId());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            if (mchPromiseOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JNJWJ, "缴纳金未缴");
            }
            //是否有未完成的订单
            int orderNum = mchModelMapper.countMchUnfinishedOrder(vo.getStoreId(), user.getUser_id());
            if (orderNum > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYJXZDDWFTHBZJ, "您的店铺账户尚有进行中订单,无法退还保证金");
            }
            //有商品不能退款
            ProductListModel productListCount = new ProductListModel();
            productListCount.setStore_id(vo.getStoreId());
            productListCount.setMch_id(user.getMchId());
            productListCount.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            if (productListModelMapper.selectCount(productListCount) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYSJDSPWFTHBZJ, "您的店铺账户尚有上架的商品,无法退还保证金");
            }
            PromiseShModel oldPromise = new PromiseShModel();
            oldPromise.setMch_id(user.getMchId());
            oldPromise.setIs_pass(3);
            int i = promiseShModelMapper.selectCount(oldPromise);
            if (i > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ALREADY_EXISTS, "当前已存在正在审核的保证金记录");
            }
            //保存保证金信息到lkt_promise_sh表
            PromiseShModel promiseShModel = new PromiseShModel();
            promiseShModel.setMch_id(user.getMchId());
            promiseShModel.setPromise_amt(mchPromiseOld.getPromise_amt());
            promiseShModel.setStatus(3);
            promiseShModel.setPay_type(mchPromiseOld.getPay_type());
            promiseShModel.setIs_pass(3);
            promiseShModel.setAdd_date(new Date());
            int row = promiseShModelMapper.insertUseGeneratedKeys(promiseShModel);

            PromiseRecordModel promiseRecordModel = new PromiseRecordModel();
            promiseRecordModel.setStore_id(user.getStore_id());
            promiseRecordModel.setMch_id(user.getMchId());
            promiseRecordModel.setMoney(mchPromiseOld.getPromise_amt());
            promiseRecordModel.setType(PromiseRecordModel.RecordType.REFUND_MCH_MARGIN);
            promiseRecordModel.setStatus(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS);
            promiseRecordModel.setAdd_date(new Date());
            promiseRecordModel.setPromise_sh_id(promiseShModel.getId().toString());
            promiseRecordModelMapper.insertSelective(promiseRecordModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "returnPromisePrice");
        }
    }

    @Override
    public Map<String, Object> MchVerificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException
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

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PromiseRecordModelMapper promiseRecordModelMapper;
}

