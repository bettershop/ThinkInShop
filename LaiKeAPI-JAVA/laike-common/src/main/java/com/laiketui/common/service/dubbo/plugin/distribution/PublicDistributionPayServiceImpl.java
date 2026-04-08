package com.laiketui.common.service.dubbo.plugin.distribution;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.plugin.payment.PaymentService;
import com.laiketui.common.mapper.DistributionConfigModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.vo.pay.PayCallBackVo;
import com.laiketui.domain.vo.pay.PayVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 分销支付流程
 *
 * @author Trick
 * @date 2021/5/21 16:43
 */
@Service("distributionPayService")
public class PublicDistributionPayServiceImpl implements PaymentService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Override
    public Map<String, Object> payment(PayVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.DISTRIBUTION, null))
            {
                //获取分销配置信息
                DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                distributionConfigModel.setStore_id(vo.getStoreId());
                distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                if (distributionConfigModel != null)
                {
                    Map<String, Object> cpayMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                    int                 type    = MapUtils.getIntValue(cpayMap, DistributionConfigModel.SetsKey.C_PAY);
                    if (type == DistributionConfigModel.SettementType.SETTEMENTTYPE_PAYMENT)
                    {
                        //分销结算
                        publiceDistributionService.commSettlement(vo.getStoreId(), vo.getUserId(), vo.getOrderno());
                    }
                    else
                    {
                        //分销等级升级流程
                        publiceDistributionService.uplevel(vo.getStoreId(), vo.getUserId(), vo.getOrderno());
                    }
                }
                else
                {
                    logger.info("{}商城分销设置未配置", vo.getStoreId());
                }
            }
            else
            {
                logger.info("{}商城分销设置未开启", vo.getStoreId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("分销支付回调流程 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "payment");
        }
        return resultMap;
    }

    @Override
    public boolean payValidata(PayVo vo) throws LaiKeAPIException
    {
        return false;
    }

    @Override
    public Map<String, Object> callBack(PayCallBackVo vo) throws LaiKeAPIException
    {
        return null;
    }
}

