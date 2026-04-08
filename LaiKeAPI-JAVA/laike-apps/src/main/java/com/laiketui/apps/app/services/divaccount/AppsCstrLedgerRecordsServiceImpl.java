package com.laiketui.apps.app.services.divaccount;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.apps.api.app.divaccount.AppsCstrLedgerRecordsService;
import com.laiketui.common.mapper.MchDistributionRecordModelMapper;
import com.laiketui.common.mapper.PaymentConfigModelMapper;
import com.laiketui.common.mapper.ReturnOrderModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;


/**
 * @author zhuqingyu
 * @create 2024/3/13
 */
@Service
public class AppsCstrLedgerRecordsServiceImpl implements AppsCstrLedgerRecordsService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private MchDistributionRecordModelMapper mchDistributionRecordModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;


    @Override
    public Map<String, Object> queryLedgerRecord(MainVo vo, Integer mchId, String condition, String startDate, String endDate)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //查询服务商id
            String     paymentJson  = URLDecoder.decode(paymentConfigModelMapper.getPaymentConfigInfo(vo.getStoreId(), "mini_wechat"), GloabConst.Chartset.UTF_8);
            JSONObject payJson      = JSONObject.parseObject(paymentJson);
            String     serviceMchId = payJson.getString("mch_id");
            logger.info("mchID:{}", mchId);
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.ASC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(mchId))
            {
                paramMap.put("mchId", mchId);
                paramMap.put("serviceMchId", serviceMchId);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("condition", condition);
            }
            if (StringUtils.isNotEmpty(startDate))
            {
                paramMap.put("startDate", startDate);
            }
            if (StringUtils.isNotEmpty(endDate))
            {
                paramMap.put("endDate", endDate);
            }
            List<Map<String, Object>> list = new ArrayList<>();
            int                       i    = mchDistributionRecordModelMapper.countDynamic(paramMap);
            if (i > 0)
            {
                list = mchDistributionRecordModelMapper.selectDynamic(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    String     orderNo          = MapUtils.getString(map, "order_no");
                    BigDecimal returnAmtByOrder = returnOrderModelMapper.getReturnAmtByOrder(orderNo);
                    map.put("refund_price", returnAmtByOrder);
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("分账记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "divideRecord");
        }
        return resultMap;
    }
}
