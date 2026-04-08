package com.laiketui.comps.payment.controller;

import com.laiketui.comps.api.payment.CompsWechatPayService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangxian
 */
@RestController
@RequestMapping("/comps/wechat/v3")
public class CompsWechatPayV3NotifyController
{

    private final Logger logger = LoggerFactory.getLogger(CompsWechatPayV3NotifyController.class);

    @Autowired
    private CompsWechatPayService compsWechatPayServiceV3DubboImpl;

    @ApiOperation("商家转账到零钱")
    @RequestMapping("/MerchantTransfersToChange")
    @HttpApiMethod(urlMapping = {"app.v3.MerchantTransfersToChange"})
    public Result MerchantTransfersToChange(String paramJson)
    {
        try
        {
            return Result.success(compsWechatPayServiceV3DubboImpl.MerchantTransfersToChange(paramJson));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
    }

    @ApiOperation("商家转账到零钱查询批次转账单结果")
    @RequestMapping("/queryBatchTransferOrder")
    @HttpApiMethod(urlMapping = {"app.v3.QueryBatchTransferOrder"})
    public Result QueryBatchTransferOrder(MainVo vo)
    {
        try
        {
            compsWechatPayServiceV3DubboImpl.QueryBatchTransferOrder(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage(), e.getMethod());
        }
    }
}
