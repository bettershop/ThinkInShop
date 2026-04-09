package com.laiketui.apps.app.services;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.AppsCstrJssdkService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.mapper.PaymentConfigModelMapper;
import com.laiketui.common.utils.weixin.Jssdk;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;


/**
 * h5授权信息接口
 *
 * @author Trick
 * @date 2020/9/23 9:22
 */
@Service
public class AppsCstrJssdkServiceImpl implements AppsCstrJssdkService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrJssdkServiceImpl.class);

    @Autowired
    PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    Jssdk jssdk;


    @Override
    public Map<String, String> getData(@NotNull int storeId, @NotNull String url) throws LaiKeAPIException
    {
        Map<String, String> resultMap = new HashMap<>(16);
        try
        {

            String configData = paymentConfigModelMapper.getPaymentConfigData(storeId);
            if (!StringUtils.isEmpty(configData))
            {
                Map<String, Object> paymentConfigMap = JSON.parseObject(configData, new TypeReference<Map<String, Object>>()
                {
                });
                String              appid            = paymentConfigMap.get("appid") + "";
                String              secret           = paymentConfigMap.get("secret") + "";

                //获取ticket todo 暂时注释
//                resultMap = jssdk.getTicket(url, appid, secret);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUrl");
        }

        return resultMap;
    }

}

