package com.laiketui.admins.mch.controller;

import com.laiketui.admins.api.mch.MchFinanceService;
import com.laiketui.core.annotation.ParamsMapping;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.mch.FinanceVo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 资金管理
 *
 * @author Trick
 * @date 2021/6/7 9:37
 */
@Api(tags = "资金管理")
@RestController
@RequestMapping("/admin/mch/finance/")
public class MchFinanceController
{

    @Autowired
    private MchFinanceService mchFinanceService;


    @ApiOperation("账户明细")
    @PostMapping("/index")
    @HttpApiMethod(apiKey = "admin.mch.finance.index")
    public Result index(MainVo vo)
    {
        try
        {
            return Result.success(mchFinanceService.index(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("提现明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "状态 0：审核中 1：审核通过 2：拒绝", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "id", value = "审核记录id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "int", paramType = "form")
    })
    @PostMapping("/withdrawList")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.WithdrawList")
    public Result withdrawList(MainVo vo, Integer status, Integer id, String startDate, String endDate, HttpServletResponse response)
    {
        try
        {
            return Result.success(mchFinanceService.withdrawList(vo, status, id, startDate, endDate, response));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("出/入账记录")
    @PostMapping("/revenueRecords")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.RevenueRecords")
    public Result revenueRecords(FinanceVo vo, HttpServletResponse response)
    {
        try
        {
            vo.setResponse(response);
            return Result.success(mchFinanceService.revenueRecords(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("提现页面")
    @PostMapping("/withdrawPage")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.WithdrawPage")
    public Result withdrawPage(MainVo vo)
    {
        try
        {
            return Result.success(mchFinanceService.withdrawPage(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("申请提现")
    @PostMapping("/withdrawals")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.Withdrawals")
    public Result withdrawals(Withdrawals1Vo vo)
    {
        try
        {
            return Result.success(mchFinanceService.withdrawals1(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("发送提现短信")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "storeId", value = "商户id", required = true, dataType = "int", paramType = "form")
    })
    @PostMapping("/withdrawalsSms")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.WithdrawalsSms")
    public Result withdrawalsSms(MainVo vo)
    {
        try
        {
            return Result.success(mchFinanceService.withdrawalsSms(vo));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡id", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "int", paramType = "form")
    })
    @PostMapping("/bankList")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.BankList")
    public Result bankList(MainVo vo, Integer id, String startDate, String endDate)
    {
        try
        {
            return Result.success(mchFinanceService.bankList(vo, id, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("设置默认银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankId", value = "银行卡id", dataType = "string", paramType = "form")
    })
    @PostMapping("/setDefault")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.SetDefault")
    public Result setDefault(MainVo vo, int bankId)
    {
        try
        {
            mchFinanceService.setDefault(vo, bankId);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("添加/编辑店铺银行卡")
    @PostMapping("/addBank")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.AddBank")
    public Result addBank(AddBankVo vo)
    {
        try
        {
            mchFinanceService.addBank(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("验证银行卡与银行名称是否匹配")
    @PostMapping("/Verification")
    @HttpApiMethod(urlMapping = "mch.Mch.Finance.Verification")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bankName", value = "银行卡名称", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "bankCardNumber", value = "银行卡号", dataType = "string", paramType = "form")
    })
    public Result verificationBank(MainVo vo, @ParamsMapping("Bank_name") String bankName, @ParamsMapping("Bank_card_number") String bankCardNumber)
    {
        try
        {
            return Result.success(mchFinanceService.MchVerificationBank(vo, bankName, bankCardNumber));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

//    @ApiOperation("验证银行卡与银行名称是否匹配")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "bankName", value = "银行卡名称", dataType = "string", paramType = "form"),
//            @ApiImplicitParam(name = "bankCardNumber", value = "银行卡号", dataType = "string", paramType = "form")
//    })
//    @PostMapping("/verificationBank")
//    @HttpApiMethod(apiKey = "admin.mch.finance.verificationBank")
//    public Result verificationBank(MainVo vo, String bankName, String bankCardNumber) {
//        try {
//            financeService.verificationBank(vo, bankName, bankCardNumber);
//            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
//        } catch (LaiKeAPIException e) {
//            return Result.error(e.getCode(), e.getMessage());
//        }
//    }


    @ApiOperation("删除银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "银行卡主键id", dataType = "int", paramType = "form")
    })
    @PostMapping("/delBank")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.DelBank")
    public Result delBank(MainVo vo, int id)
    {
        try
        {
            mchFinanceService.delBank(vo, id);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


    @ApiOperation("售后明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/returnDetail")
    @HttpApiMethod(apiKey = "mch.Mch.Finance.ReturnDetail")
    public Result returnDetail(MainVo vo, String startDate, String endDate)
    {
        try
        {
            return Result.success(mchFinanceService.returnDetail(vo, startDate, endDate));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金管理,获取记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始时间", dataType = "string", paramType = "form"),
            @ApiImplicitParam(name = "endDate", value = "结束时间", dataType = "string", paramType = "form"),
    })
    @PostMapping("/PromiseList")
    @HttpApiMethod(apiKey = "mch.Mch.Promise.PromiseList")
    public Result PromiseList(MainVo vo, String startDate, String endDate, HttpServletResponse response, String type, String status)
    {
        try
        {
            return Result.success(mchFinanceService.PromiseList(vo, startDate, endDate, response, type, status));
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("保证金审核")
    @PostMapping("/insertPromisePrice")
    @HttpApiMethod(apiKey = "mch.Mch.Promise.InsertPromisePrice")
    public Result insertPromisePrice(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            mchFinanceService.insertPromisePrice(vo);
            return Result.success(GloabConst.ManaValue.MANA_VALUE_SUCCESS);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
