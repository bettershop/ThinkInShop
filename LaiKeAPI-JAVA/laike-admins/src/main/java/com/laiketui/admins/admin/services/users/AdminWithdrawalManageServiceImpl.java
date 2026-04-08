package com.laiketui.admins.admin.services.users;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.users.AdminWithdrawalManageService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.user.WalletVo;
import com.laiketui.domain.vo.user.WithdrawalVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 提现管理实现
 *
 * @author Trick
 * @date 2021/1/11 14:05
 */
@Service
public class AdminWithdrawalManageServiceImpl implements AdminWithdrawalManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminWithdrawalManageServiceImpl.class);

    @Override
    public Map<String, Object> getWithdrawalInfo(WithdrawalVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (vo.getStatus() != null)
            {
                parmaMap.put("status", vo.getStatus());
            }
            parmaMap.put("is_mch", 0);
            if (vo.getWid() != null && vo.getWid() > 0)
            {
                parmaMap.put("wid", vo.getWid());
            }
            if (StringUtils.isNotEmpty(vo.getMchName()))
            {
                parmaMap.put("like_name", vo.getMchName());
            }
            if (StringUtils.isNotEmpty(vo.getUserName()))
            {
                parmaMap.put("like_user_name", vo.getUserName());
            }
            if (StringUtils.isNotEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (StringUtils.isNotEmpty(vo.getUserNameAndPhone()))
            {
                parmaMap.put("like_user_name_mobile", vo.getUserNameAndPhone());
            }
            if (StringUtils.isNotEmpty(vo.getMchNameAndPhone()))
            {
                parmaMap.put("like_mch_name_mobile", vo.getMchNameAndPhone());
            }
            if (StringUtils.isNotEmpty(vo.getWithdrawStatus()))
            {
                parmaMap.put("withdrawStatus", vo.getWithdrawStatus());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total    = withdrawModelMapper.countWithdrawLeftUserBank(parmaMap);
            List<Map<String, Object>> dataList = withdrawModelMapper.getWithdrawLeftUserBank(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                if (MapUtils.getInteger(map, "withdraw_status").equals(WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    map.put("Bank_card_number", MapUtils.getString(map, "wx_name"));
                }
            }
            if (vo.getExportType() == 1)
            {
                exportWithdrawalData(dataList, false, response);
                return null;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现列表信息", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWithdrawalInfo");
        }
        return resultMap;
    }

    //导出 提现审核列表
    private void exportWithdrawalData(List<Map<String, Object>> list, boolean flag, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户昵称", "申请时间", "提现金额", "提现手续费", "银行名称", "支行名称", "持卡人姓名", "卡号", "联系电话", "审核状态"};
            //对应字段
            String[] kayList = new String[]{"userName", "add_date", "money", "s_charge", "Bank_name", "branch", "Cardholder", "Bank_card_number", "mobile", "type_name"};

            if (flag)
            {
                List<String> headers = new ArrayList<>();
                Collections.addAll(headers, headerList);
                headers.add("备注");
                headerList = headers.toArray(new String[0]);

                List<String> kay = new ArrayList<>();
                Collections.addAll(kay, kayList);
                kay.add("refuse");
                kayList = kay.toArray(new String[0]);
            }

            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("提现审核列表");
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
            logger.error("提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportWithdrawalData");
        }
    }

    @Override
    public Map<String, Object> getWithdrawalRecord(WithdrawalVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);
            if (vo.getStatus() != null)
            {
                parmaMap.put("status", vo.getStatus());
            }
            parmaMap.put("is_mch", 0);

            if (vo.getStatus() != null)
            {
                parmaMap.put("status", vo.getStatus());
            }
            if (vo.getWid() != null && vo.getWid() > 0)
            {
                parmaMap.put("wid", vo.getWid());
            }
            if (StringUtils.isNotEmpty(vo.getUserName()))
            {
                parmaMap.put("like_user_name", vo.getUserName());
            }
            if (StringUtils.isNotEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (StringUtils.isNotEmpty(vo.getUserNameAndPhone()))
            {
                parmaMap.put("like_user_name_mobile", vo.getUserNameAndPhone());
            }
            if (StringUtils.isNotEmpty(vo.getMchNameAndPhone()))
            {
                parmaMap.put("like_mch_name_mobile", vo.getMchNameAndPhone());
            }
            if (StringUtils.isNotEmpty(vo.getWithdrawStatus()))
            {
                parmaMap.put("withdrawStatus", vo.getWithdrawStatus());
            }
            if (StringUtils.isNotEmpty(vo.getWxStatus()))
            {
                parmaMap.put("wxStatus", vo.getWxStatus());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            //只展示审核通过了、审核拒绝
            List<Integer> statusList = Arrays.asList(1, 2);
            parmaMap.put("statusList", statusList);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());

            int                       total    = withdrawModelMapper.countWithdrawLeftUserBank(parmaMap);
            List<Map<String, Object>> dataList = withdrawModelMapper.getWithdrawLeftUserBank(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                int    examine = MapUtils.getIntValue(map, "status");
                String examineName;
                switch (examine)
                {
                    //0：审核中 1：审核通过 2：拒绝
                    case 0:
                        examineName = "审核中";
                        break;
                    case 1:
                        examineName = "审核通过";
                        break;
                    case 2:
                        examineName = "拒绝";
                        break;
                    default:
                        examineName = "-";
                }
                map.put("examineName", examineName);
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                if (MapUtils.getInteger(map, "withdraw_status").equals(WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    map.put("Bank_card_number", MapUtils.getString(map, "wx_name"));
                }
            }
            if (vo.getExportType() == 1)
            {
                exportWithdrawalData(dataList, true, response);
                return null;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWithdrawalExamineInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawalExamine(MainVo vo, int id, int status, String refuse) throws LaiKeAPIException
    {
        try
        {
            AdminModel    adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            WithdrawModel withdrawModel = withdrawModelMapper.selectByPrimaryKey(id);
            if (withdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJLBCZ, "提现记录不存在");
            }
            //站内通知
            String             sysNoticeTitle    = "余额提现申请审批通过!";
            String             sysNoticeText     = "您的提现申请已通过审核，提现金额将会在1-3个工作日到账。";
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setStore_id(vo.getStoreId());
            systemMessageSave.setRecipientid(withdrawModel.getUser_id());
            systemMessageSave.setSenderid(adminModel.getName());
            systemMessageSave.setType(GloabConst.LktConfig.SYSMESSAGE_NOT_READ);
            //操作记录
            User user = new User();
            user.setStore_id(vo.getStoreId());
            user.setUser_id(withdrawModel.getUser_id());
            user = userBaseMapper.selectOne(user);
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(vo.getStoreId());
            recordModel.setUser_id(withdrawModel.getUser_id());
            recordModel.setMoney(withdrawModel.getMoney());
            recordModel.setOldmoney(user.getMoney());
            recordModel.setEvent(String.format("%s提现了%s", user.getUser_id(), withdrawModel.getMoney()));
            recordModel.setType(21);
            //原来的提现记录
            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
            recordDetailsModel.setStore_id(vo.getStoreId());
            recordDetailsModel.setsNo(withdrawModel.getTxsno());
            recordDetailsModel = recordDetailsModelMapper.selectOne(recordDetailsModel);
            if (recordDetailsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //状态 0：审核中 1：审核通过 2：拒绝
            WithdrawModel withdrawUpdate = new WithdrawModel();
            withdrawUpdate.setId(withdrawModel.getId());
            withdrawUpdate.setStatus(StringUtils.toString(status));
            if (DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS.equals(status + ""))
            {
                sysNoticeTitle = "余额提现失败!";
                sysNoticeText = String.format("您申请的提现被驳回!驳回原因：%s", refuse);
                withdrawUpdate.setRefuse(refuse);
                //拒绝记录
                recordModel.setEvent(String.format("%s提现%s被拒绝", user.getUser_id(), withdrawModel.getMoney()));
                recordModel.setType(22);
                //退还至余额
                int count = userBaseMapper.rechargeUserPrice(user.getId(), withdrawModel.getMoney().abs());
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFMQSHZS, "服务器繁忙,请稍后重试");
                }
                user = userBaseMapper.selectByPrimaryKey(user.getId());
                //添加提现记录详情
                RecordDetailsModel newRecordDetailsModel = new RecordDetailsModel();
                newRecordDetailsModel.setStore_id(vo.getStoreId());
                newRecordDetailsModel.setMoney(withdrawModel.getMoney().abs());
                newRecordDetailsModel.setUserMoney(user.getMoney());
                newRecordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
//                newRecordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.BALANCE_DEDUCTION);
                newRecordDetailsModel.setType(RecordDetailsModel.type.WITHDRAWAL_OF_BALANCE);
                newRecordDetailsModel.setsNo(recordDetailsModel.getsNo());
                newRecordDetailsModel.setWithdrawalFees(recordDetailsModel.getWithdrawalFees());
                newRecordDetailsModel.setWithdrawalMethod(recordDetailsModel.getWithdrawalMethod());
                newRecordDetailsModel.setRecordTime(new Date());
                newRecordDetailsModel.setAddTime(new Date());
                recordDetailsModelMapper.insert(newRecordDetailsModel);
                recordModel.setDetails_id(newRecordDetailsModel.getId());
                recordModel.setAdd_date(new Date());
                count = recordModelMapper.insertSelective(recordModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFMQSHZS, "服务器繁忙,请稍后重试");
                }
                //添加操作日志x``
                publiceService.addAdminRecord(vo.getStoreId(), "拒绝了用户ID：" + user.getUser_id() + "的提现", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            else if (DictionaryConst.ExameStatus.EXAME_PASS_STATUS.equals(status + ""))
            {
                withdrawUpdate.setExamine_date(new Date());
                //判断是否为微信零钱提现
                if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    withdrawUpdate.setWxStatus(WithdrawModel.WX_STATUS.WAIT_PAY);
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("app.v3.MerchantTransfersToChange", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                        //添加操作日志
                        publiceService.addAdminRecord(vo.getStoreId(), "通过了用户ID：" + user.getUser_id() + "的提现", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
                //贝宝提现
                else if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.PAYPAL))
                {
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        BigDecimal money    = withdrawModel.getMoney();
                        BigDecimal s_charge = withdrawModel.getS_charge();
                        money = money.subtract(s_charge);
                        params.put("money", money);
                        params.put("email", withdrawModel.getEmail());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("v1.payouts", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                        //添加操作日志
                        publiceService.addAdminRecord(vo.getStoreId(), "通过了用户ID：" + user.getUser_id() + "的提现", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
                //stripe提现
                else if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.STRIPE))
                {
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        BigDecimal money    = withdrawModel.getMoney();
                        BigDecimal s_charge = withdrawModel.getS_charge();
                        money = money.subtract(s_charge);
                        params.put("money", money);
                        params.put("stripe_account_id", withdrawModel.getStripeAccountId());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("comps.stripe.payouts", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                        //添加操作日志
                        publiceService.addAdminRecord(vo.getStoreId(), "通过了用户ID：" + user.getUser_id() + "的提现", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXLBWZ, "提现类型不存在");
                }

            }
            int count = withdrawModelMapper.updateByPrimaryKeySelective(withdrawUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            systemMessageSave.setTitle(sysNoticeTitle);
            systemMessageSave.setContent(sysNoticeText);
            systemMessageSave.setTime(new Date());
            count = systemMessageModelMapper.insertSelective(systemMessageSave);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFMQSHZS, "服务器繁忙,请稍后重试");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("审核提现", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawalExamine");
        }
    }

    @Override
    public Map<String, Object> getWalletInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(vo.getStoreId());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            resultMap.put("data", financeConfigModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取钱包设置信息", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWalletInfo");
        }
        return resultMap;
    }

    @Override
    public boolean setWalletInfo(WalletVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (vo.getMinMoney() == null || vo.getMinMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXCZJEBNXYDY, "最小充值金额不能小于等于0");
            }
            if (vo.getMinOutMoney() == null || vo.getMinOutMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNXYDY, "最小提现金额不能小于等于0");
            }
            if (vo.getMaxOutMoney() == null || vo.getMaxOutMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDTXJEBNXYDY, "最大提现金额不能小于等于0");
            }
            if (vo.getServiceMoney() == null || vo.getServiceMoney().doubleValue() >= 1 || vo.getServiceMoney().doubleValue() < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXFWDYXYDXS, "手续费为大于等于0小于100的小数");
            }
            if (vo.getMinOutMoney().compareTo(vo.getServiceMoney()) == 0 || vo.getMinOutMoney().compareTo(vo.getServiceMoney()) < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNXYDYSXF, "最小提现金额不能小于等于手续费");
            }
            String unit = "元";
            if (!StringUtils.isEmpty(vo.getUnit()))
            {
                unit = vo.getUnit();
            }

            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(vo.getStoreId());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);

            FinanceConfigModel saveFinanceConfig = new FinanceConfigModel();
            saveFinanceConfig.setUnit(unit);
            saveFinanceConfig.setMin_cz(vo.getMinMoney());
            saveFinanceConfig.setMin_amount(vo.getMinOutMoney());
            saveFinanceConfig.setMax_amount(vo.getMaxOutMoney());
            saveFinanceConfig.setService_charge(vo.getServiceMoney());
            saveFinanceConfig.setModify_date(new Date());

            if (financeConfigModel != null)
            {
                saveFinanceConfig.setId(financeConfigModel.getId());
                count = financeConfigModelMapper.updateByPrimaryKeySelective(saveFinanceConfig);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了用户的提现设置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                saveFinanceConfig.setStore_id(vo.getStoreId());
                count = financeConfigModelMapper.insertSelective(saveFinanceConfig);
            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("设置钱包设置信息" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setWalletInfo");
        }
    }


    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

}

