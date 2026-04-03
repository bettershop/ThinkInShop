package com.laiketui.apps.app.services;

import com.laiketui.apps.api.app.AppsCstrRechargeService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.mapper.FinanceConfigModelMapper;
import com.laiketui.common.mapper.PaymentConfigModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值
 *
 * @author Trick
 * @date 2020/11/4 16:20
 */
@Service
public class AppsCstrRechargeServiceImpl implements AppsCstrRechargeService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrRechargeServiceImpl.class);


    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取最低充值金额
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(vo.getStoreId());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            //获取支付方式
            Map<String, String> payConfigInfoMap = new HashMap<>(16);
            Map<String, Object> parmaMap         = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            List<Map<String, Object>> resultPayConfigInfoMap = paymentConfigModelMapper.getPaymentConfigDynamic(parmaMap);
            for (Map<String, Object> map : resultPayConfigInfoMap)
            {
                String key = map.get("class_name").toString();
                payConfigInfoMap.put(key, map.get("STATUS").toString());
            }

            resultMap.put("min_cz", financeConfigModel.getMin_cz());
            resultMap.put("payment", payConfigInfoMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("充值界面异常 " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }

        return resultMap;
    }

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;
}

