package com.laiketui.admins.admin.services.users;

import com.laiketui.admins.api.admin.users.AdminRechargeServive;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.SaveAddressVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.dic.DicVo;
import com.laiketui.domain.vo.user.RechargeVo;
import com.laiketui.domain.vo.user.UserMoneyVo;
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
 * 充值列表
 *
 * @author Trick
 * @date 2021/1/11 15:48
 */
@Service
public class AdminRechargeServiveImpl implements AdminRechargeServive
{
    private final Logger logger = LoggerFactory.getLogger(AdminRechargeServiveImpl.class);

    @Override
    public Map<String, Object> getRechargeInfo(RechargeVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", "record");
            if (vo.getId() != null && vo.getId() > 0)
            {
                parmaMap.put("id", vo.getId());
            }
            if (!StringUtils.isEmpty(vo.getUserid()))
            {
                parmaMap.put("userid", vo.getUserid());
            }
            if (!StringUtils.isEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (!StringUtils.isEmpty(vo.getUserName()))
            {
                parmaMap.put("like_name", vo.getUserName());
            }
            //类型
            parmaMap.put("type", "record");
            if (!StringUtils.isEmpty(vo.getoType()))
            {
                parmaMap.put("otype", vo.getoType());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = recordModelMapper.getUserWalletRecordInfo(parmaMap);
            int                       total    = recordModelMapper.countUserWalletRecordInfo(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取充值记录" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getupInfo(RechargeVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            //用于查询总的数据即充值的总金额
            Map<String, Object> parmaAllMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", "record");
            if (vo.getId() != null && vo.getId() > 0)
            {
                parmaMap.put("id", vo.getId());
            }
            if (!StringUtils.isEmpty(vo.getUserid()))
            {
                parmaMap.put("userid", vo.getUserid());
            }
            if (!StringUtils.isEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (!StringUtils.isEmpty(vo.getUserName()))
            {
                parmaMap.put("like_name", vo.getUserName());
            }
            if (!StringUtils.isEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            if (!StringUtils.isEmpty(vo.getoType()))
            {
                parmaMap.put("otype", vo.getoType());
            }
            parmaAllMap.putAll(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = recordModelMapper.getUserWalletRecordInfo(parmaMap);
            BigDecimal                allMoney = recordModelMapper.sumUserWalletRecordInfo(parmaAllMap);
            for (Map<String, Object> map : dataList)
            {
                String addDate      = DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS);
                String registerData = "";
                if (map.containsKey("Register_data"))
                {
                    registerData = DateUtil.dateFormate(MapUtils.getString(map, "Register_data"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("add_date", addDate);
                map.put("Register_data", registerData);
            }
            int total = recordModelMapper.countUserWalletRecordInfo(parmaMap);
            //用户导出
            if (vo.getExportType() != null)
            {
                if (vo.getExportType() == 1)
                {
                    exportUpList(dataList, response);
                    return null;
                }
            }
            resultMap.put("list", dataList);
            resultMap.put("total", total);
            resultMap.put("allMoney", allMoney);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取充值记录" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserMoneyInfo(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (!StringUtils.isEmpty(vo.getUserName()))
            {
                parmaMap.put("key", vo.getUserName());
            }
            //来源
            if (!StringUtils.isEmpty(vo.getSource()))
            {
                parmaMap.put("source", vo.getSource());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            //降序排序 43002
            parmaMap.put("Register_data_sort", DataUtils.Sort.DESC.toString());

            List<Map<String, Object>> dataList = userBaseMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                String birthday = "";
                if (map.containsKey("birthday"))
                {
                    birthday = DateUtil.dateFormate(MapUtils.getString(map, "birthday"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("birthday", birthday);
                String gradeAddDate = "";
                if (map.containsKey("grade_add"))
                {
                    gradeAddDate = DateUtil.dateFormate(MapUtils.getString(map, "grade_add"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("grade_add", gradeAddDate);
                map.put("last_time", MapUtils.getString(map, "last_time"));
                String gradeEndDate = "暂无";
                if (map.containsKey("grade_end"))
                {
                    gradeEndDate = DateUtil.dateFormate(MapUtils.getString(map, "grade_end"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("grade_end", gradeEndDate);
                String registerDate = DateUtil.dateFormate(MapUtils.getString(map, "Register_data"), GloabConst.TimePattern.YMDHMS);
                map.put("Register_data", registerDate);
            }
            int total = userBaseMapper.countDynamic(parmaMap);

            //用户资金记录导出
            if (vo.getExportType() != null)
            {
                if (vo.getExportType() == 1)
                {
                    exportUserMoneyList(dataList, response);
                    return null;
                }
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
            e.printStackTrace();
            logger.error("获取用户资金记录" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getUserIntegralInfo(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (!StringUtils.isEmpty(vo.getUserName()))
            {
                parmaMap.put("user_name1", vo.getUserName());
            }
            //电话
            if (!StringUtils.isEmpty(vo.getTel()))
            {
                parmaMap.put("mobile", vo.getTel());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            //降序 43004
            parmaMap.put("Register_data_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> dataList = userBaseMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                if (map.containsKey("birthday"))
                {
                    String birthday = DateUtil.dateFormate(MapUtils.getString(map, "birthday"), GloabConst.TimePattern.YMDHMS);
                    map.put("birthday", birthday);
                }
                String gradeAddDate = "";
                if (map.containsKey("grade_add"))
                {
                    gradeAddDate = DateUtil.dateFormate(MapUtils.getString(map, "grade_add"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("grade_add", gradeAddDate);
                map.put("last_time", MapUtils.getString(map, "last_time"));
                String gradeEndDate = "暂无";
                if (map.containsKey("grade_end"))
                {
                    gradeEndDate = DateUtil.dateFormate(MapUtils.getString(map, "grade_end"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("grade_end", gradeEndDate);
                String registerDate = DateUtil.dateFormate(MapUtils.getString(map, "Register_data"), GloabConst.TimePattern.YMDHMS);
                map.put("Register_data", registerDate);
            }
            int total = userBaseMapper.countDynamic(parmaMap);
            //用户导出
            if (vo.getExportType() != null)
            {
                if (vo.getExportType() == 1)
                {
                    exportUserIntegralList(dataList, response);
                    return null;
                }
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
            e.printStackTrace();
            logger.error("获取用户资金记录" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserMoneyInfo_see(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            //id
            if (!StringUtils.isEmpty(vo.getUserid()))
            {
                parmaMap.put("userid", vo.getUserid());
            }
            //用户名称
            if (!StringUtils.isEmpty(vo.getUserName()))
            {
                parmaMap.put("like_name", vo.getUserName());
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("type", "record_detail");
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            //类型
            if (!StringUtils.isEmpty(vo.getotype()))
            {
                parmaMap.put("otype", vo.getotype());
            }
            if (!StringUtils.isEmpty(vo.getostatus()))
            {
                List<Integer> typeList = null;
                if (vo.getostatus().equals("2"))
                {
                    typeList = Arrays.asList(RecordModel.RecordType.APPLICATION_FOR_WITHDRAWAL, RecordModel.RecordType.BALANCE_CONSUMPTION,
                            RecordModel.RecordType.SYSTEM_DEDUCTION, RecordModel.RecordType.PAY_AUCTION_DEPOSIT,
                            RecordModel.RecordType.PAY_MCH_DEPOSIT);
                }
                else if (vo.getostatus().equals("1"))
                {
                    typeList = Arrays.asList(RecordModel.RecordType.RECHARGE, RecordModel.RecordType.REFUND, RecordModel.RecordType.SYSTEM_RECHARGE
                            , RecordModel.RecordType.REFUND_AUCTION_DEPOSIT, RecordModel.RecordType.WITHDRAWAL_FAILED, RecordModel.RecordType.REFUND_MCH_DEPOSIT
                            , RecordModel.RecordType.LIVING_COMMISSION);
                }
                parmaMap.put("typeList", typeList);
            }
            else
            {
                List<Integer> typeList = Arrays.asList(RecordModel.RecordType.APPLICATION_FOR_WITHDRAWAL,
                        RecordModel.RecordType.BALANCE_CONSUMPTION, RecordModel.RecordType.SYSTEM_DEDUCTION,
                        RecordModel.RecordType.RECHARGE, RecordModel.RecordType.REFUND, RecordModel.RecordType.SYSTEM_RECHARGE
                        , RecordModel.RecordType.PAY_AUCTION_DEPOSIT, RecordModel.RecordType.REFUND_AUCTION_DEPOSIT, RecordModel.RecordType.WITHDRAWAL_FAILED
                        , RecordModel.RecordType.PAY_MCH_DEPOSIT, RecordModel.RecordType.REFUND_MCH_DEPOSIT, RecordModel.RecordType.LIVING_COMMISSION);
                parmaMap.put("typeList", typeList);
            }
//            //状态
//            if (!StringUtils.isEmpty(vo.getostatus())) {
//                parmaMap.put("ostatus", vo.getostatus());
//            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = recordModelMapper.selectMoneyInfo_see(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                Integer    type  = MapUtils.getInteger(map, "type");
                BigDecimal money = new BigDecimal(MapUtils.getString(map, "money"));
                if (RecordModel.TYPE_EXPENDITURE.contains(type))
                {
                    map.put("statusName", "支出");
                    map.put("money", money.negate());
                    //系统扣除金额，数据库本来就是负数
                    if (RecordModel.RecordType.SYSTEM_DEDUCTION.equals(type))
                    {
                        map.put("money", money);
                    }
                }
                else
                {
                    map.put("statusName", "入账");
                }
                String addTime = DateUtil.dateFormate(MapUtils.getString(map, "addtime"), GloabConst.TimePattern.YMDHMS);
                map.put("addtime", addTime);
                map.put("registerData", DateUtil.dateFormate(MapUtils.getString(map, "Register_data"), GloabConst.TimePattern.YMDHMS));
            }
            int total = recordModelMapper.countMoneyInfo_see(parmaMap);
            //获取用户资金入账、出账总额
            HashMap<String, Object> moneyMap = new HashMap<>();
            moneyMap.put("store_id", vo.getStoreId());
            moneyMap.put("type", "record_detail");
            moneyMap.put("userid", vo.getUserid());
            //入账
            List<Integer> incomeList = Arrays.asList(RecordModel.RecordType.RECHARGE, RecordModel.RecordType.REFUND, RecordModel.RecordType.SYSTEM_RECHARGE
                    , RecordModel.RecordType.REFUND_AUCTION_DEPOSIT, RecordModel.RecordType.WITHDRAWAL_FAILED, RecordModel.RecordType.REFUND_MCH_DEPOSIT
                    , RecordModel.RecordType.LIVING_COMMISSION);
            moneyMap.put("typeList", incomeList);
            BigDecimal incomeMoney = recordModelMapper.countMoney(moneyMap);
            //出账
            List<Integer> outcomeList = Arrays.asList(RecordModel.RecordType.APPLICATION_FOR_WITHDRAWAL, RecordModel.RecordType.BALANCE_CONSUMPTION,
                    RecordModel.RecordType.SYSTEM_DEDUCTION, RecordModel.RecordType.PAY_AUCTION_DEPOSIT,
                    RecordModel.RecordType.PAY_MCH_DEPOSIT);
            moneyMap.put("typeList", outcomeList);
            BigDecimal outcomeMoney = recordModelMapper.countMoney(moneyMap);
            //用户导出
            if (vo.getExportType() != null)
            {
                if (vo.getExportType() == 1)
                {
                    exportUserMoneySeeList(dataList, response);
                    return null;
                }
            }
            resultMap.put("list", dataList);
            resultMap.put("total", total);
            resultMap.put("income", incomeMoney.abs());
            resultMap.put("outcome", outcomeMoney.abs());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户资金详情记录", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserIntegralInfo_see(UserMoneyVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //id
            if (!StringUtils.isEmpty(vo.getUserid()))
            {
                parmaMap.put("userid", vo.getUserid());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            //类型
            if (!StringUtils.isEmpty(vo.getotype()))
            {
                parmaMap.put("otype", vo.getotype());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            //查询积分类型
            final String        value           = "value";
            Map<String, String> integralNameMap = new HashMap<>(16);
            DicVo               dicVo           = new DicVo();
            dicVo.setName("积分类型");
            Map<String, Object> dicType = publicDictionaryService.getDictionaryByName(dicVo);
            if (dicType != null)
            {
                if (dicType.containsKey(value))
                {
                    List<DictionaryListModel> dicValueList = DataUtils.cast(dicType.get("value"));
                    if (dicValueList != null)
                    {
                        for (DictionaryListModel dicValue : dicValueList)
                        {
                            integralNameMap.put(dicValue.getValue(), dicValue.getCtext());
                        }
                    }
                }
            }
            List<Map<String, Object>> dataList = recordModelMapper.selectIntegralInfo_see(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                BigDecimal score        = new BigDecimal(MapUtils.getString(map, "sign_score", "0"));
                String     type         = MapUtils.getString(map, "type");
                String     scoreInvalid = DateUtil.dateFormate(MapUtils.getString(map, "score_invalid"), GloabConst.TimePattern.YMDHMS);
                String     signTime     = "";
                if (map.containsKey("sign_time"))
                {
                    signTime = DateUtil.dateFormate(MapUtils.getString(map, "sign_time"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("score_invalid", scoreInvalid);
                map.put("sign_time", signTime);
                if (SignRecordModel.TYPE_EXPENDITURE.contains(Integer.parseInt(type)))
                {
                    map.put("sign_score", score.negate());
                }
                map.put("typeName", integralNameMap.get(type));
            }
            int total = recordModelMapper.countIntegralInfo_see(parmaMap);
            //用户导出
            if (vo.getExportType() != null)
            {
                if (vo.getExportType() == 1)
                {
                    exportIntegralSeeList(dataList, response);
                    return null;
                }
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
            e.printStackTrace();
            logger.error("获取用户积分详情记录" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getRechargeInfo");
        }
        return resultMap;
    }

    private void exportUpList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户ID", "用户昵称", "充值总金额", "来源", "手机号码", "类型", "备注", "充值时间"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "user_name", "money", "sourceName", "mobile", "typeName", "remake", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("用户资金列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
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
            logger.error("导出用户充值数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    private void exportUserMoneyList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户ID", "用户名称", "来源", "余额", "注册时间"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "user_name", "sourceName", "money", "Register_data"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("用户资金列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
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
            logger.error("导出用户资金数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    private void exportUserIntegralList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"用户ID", "用户名称", "手机号码", "积分", "来源", "注册时间"};
            //对应字段
            String[]     kayList = new String[]{"user_id", "user_name", "mobile", "score", "sourceName", "Register_data"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("用户积分列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
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
            logger.error("导出用户积分数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    private void exportUserMoneySeeList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"序号", "用户名称", "入账/支出", "金额", "类型", "入账/支出时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "user_name", "statusName", "money", "typeName", "addtime"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("用户资金详情列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
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
            logger.error("导出用户资金详情数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    private void exportIntegralSeeList(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"序号", "用户名称", "积数分", "时间", "类型"};
            //对应字段
            String[]     kayList = new String[]{"id", "user_name", "sign_score", "sign_time", "typeName"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("用户积分详情列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
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
            logger.error("导出用户积分详情数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserAddress saveAddress(SaveAddressVo vo, String userId) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //校验手机号是否正确
           /* if (!MobileUtils.isMobile(vo.getMobile()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PHONE_NOT_CHECK, "手机号格式不正确");
            }*/
            //是否为默认
            int isDefualt = vo.getIsDefault();
            //拆分省市县
            String[] place = vo.getPlace().split("-");
            //合并地址
            StringBuilder addressXq = new StringBuilder("");
            for (String str : place)
            {
                addressXq.append(str);
            }
            addressXq.append(vo.getAddress());

            //获取当前用户默认地址
            UserAddress userAddress = new UserAddress();
            userAddress.setStore_id(vo.getStoreId());
            userAddress.setUid(userId);
            userAddress.setIs_default(1);
            userAddress = userAddressMapper.selectOne(userAddress);
            if (userAddress != null)
            {
                //判断是否为默认地址模式
                if (isDefualt == 1)
                {
                    //重置当前默认的地址
                    UserAddress updateUserAddress = new UserAddress();
                    updateUserAddress.setId(userAddress.getId());
                    updateUserAddress.setUid(userId);
                    updateUserAddress.setIs_default(0);
                    int count = userAddressMapper.updateById(updateUserAddress);
                    if (count < 1)
                    {
                        logger.info("用户:" + userId + "重置默认地址失败");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "添加地址失败");
                    }
                }
                else
                {
                    isDefualt = 0;
                }
            }

            //添加地址
            UserAddress addUserAddress = new UserAddress();
            addUserAddress.setStore_id(vo.getStoreId());
            addUserAddress.setName(vo.getUserName());
            addUserAddress.setTel(vo.getMobile());
            addUserAddress.setSheng(place[0]);
            addUserAddress.setCity(place[1]);
            addUserAddress.setQuyu(place[2]);
            addUserAddress.setAddress(vo.getAddress());
            addUserAddress.setAddress_xq(addressXq.toString());
            addUserAddress.setUid(userId);
            addUserAddress.setIs_default(isDefualt);
            int count = userAddressMapper.insertSelective(addUserAddress);
            if (count < 1)
            {
                logger.info("用户:" + userId + "添加地址失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "添加地址失败");
            }
            return addUserAddress;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存地址异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "saveAddress");
        }
    }

    @Override
    public List<Map<String, Object>> getItuList(String keyword)
    {
        List<Map<String,Object>> res = new ArrayList<>();
       try
       {
           res =  countryModelMapper.getItuList(keyword);
       }  catch (LaiKeAPIException l)
       {
           throw l;
       }
       catch (Exception e)
       {
           logger.error("获取区号异常 ", e);
           throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "getItuList");
       }
       return res;
    }

    @Override
    public String generateAccount()
    {
        try
        {
            return publiceService.generateAccount(8);
        }catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("生成账号异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "generateAccount");
        }
    }


    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private CountryModelMapper countryModelMapper;

}
