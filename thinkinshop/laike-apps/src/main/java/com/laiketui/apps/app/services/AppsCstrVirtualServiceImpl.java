package com.laiketui.apps.app.services;


import com.google.common.base.Strings;
import com.laiketui.apps.api.app.services.AppsCstrVirtualService;
import com.laiketui.common.mapper.OrderModelMapper;
import com.laiketui.common.mapper.ReturnOrderModelMapper;
import com.laiketui.common.mapper.WriteRecordModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.virtual.WriteRecordModel;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 虚拟商品接口
 *
 * @author Trick
 * @date 2020/12/17 11:23
 */
@Service
public class AppsCstrVirtualServiceImpl implements AppsCstrVirtualService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrVirtualServiceImpl.class);

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private WriteRecordModelMapper writeRecordModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;


    @Override
    public Map<String, Object> getWriteRecord(Integer id)
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            if (id == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数错误");
            }
            //r.write_code,r.status,r.write_time,s.name
            List<Map<String, Object>> Map       = writeRecordModelMapper.selectByOrderDetailId(id);
            List<Map<String, Object>> resultMap = new ArrayList<>();
            int                       num       = 0;
            int                       i         = 0;
            for (Map<String, Object> map : Map)
            {
                //处于售后中
                if (DataUtils.getIntegerVal(map, "status").equals(WriteRecordModel.status.refund))
                {
                    if (i < 1)
                    {
                        //取出退款次数、退款金额、退款时间
                        List<ReturnOrderModel> returnOrderModel = returnOrderModelMapper.selectByOrderDetailId(DataUtils.getIntegerVal(map, "p_id"));
                        //撤销审核直接删了售后表中的数据，所以这里直接跳过显示
                        if (returnOrderModel == null)
                        {
                            continue;
                        }
                        Map<String, Object> refund = null;
                        for (ReturnOrderModel returnOrder : returnOrderModel)
                        {
                            refund = new HashMap<>();
                            String sNo = MapUtils.getString(map, "s_no");
                            if (!map.containsKey("write_code"))
                            {
                                String writeCode = orderModelMapper.getWriteCode(sNo);
                                refund.put("write_code",writeCode.split(",")[0]);
                            }
                            refund.put("name",MapUtils.getString(map,"name"));

                            refund.put("currency_symbol", MapUtils.getString(map, "currency_symbol"));
                            refund.put("exchange_rate", MapUtils.getString(map, "exchange_rate"));

                            refund.put("refund_num", returnOrder.getR_write_off_num());
                            refund.put("refund_price", returnOrder.getRe_money());
                            refund.put("write_time", DateUtil.dateFormate(DateUtil.dateFormate(returnOrder.getRe_time(), GloabConst.TimePattern.YMDHMS), GloabConst.TimePattern.YMDHMS));
                            //1为已核销，2为退款审核中，4为退款成功
                            if (returnOrder.getR_type() == 0)
                            {
                                refund.put("status", WriteRecordModel.status.refund);
                            }
                            else if (returnOrder.getR_type() == 4 || returnOrder.getR_type() == 9 || returnOrder.getR_type() == 13)
                            {
                                refund.put("status", WriteRecordModel.status.finishWrite);
                            }
                            else if (returnOrder.getR_type() == 8)
                            {
                                refund.put("status",WriteRecordModel.status.returnFail);
                            }
                            resultMap.add(refund);
                        }
                        i++;
                    }
                }
                else
                {
                    num++;
                    map.put("write_time", DateUtil.dateFormate(DataUtils.getStringVal(map, "write_time"), GloabConst.TimePattern.YMDHMS));
                    if (Strings.isNullOrEmpty(MapUtils.getString(map, "name")))
                    {
                        map.put("name", "店铺核销");
                    }
                    resultMap.add(map);
                }

            }
            result.put("list", resultMap);
            result.put("num", num);

        }
        catch (LaiKeAPIException l)
        {
            logger.error("订单详情查看核销记录 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单详情查看核销记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_, e.getMessage());
        }
        return result;
    }
}

