package com.laiketui.apps.app.controller;

import com.laiketui.apps.api.app.AppsCstrRechargeService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 充值
 *
 * @author Trick
 * @date 2020/11/4 16:19
 */
@RestController
@Validated
@RequestMapping("/app/recharge")
public class AppsCstrRechargeController
{

    @Autowired
    AppsCstrRechargeService appsCstrRechargeService;


    @ApiOperation("充值界面")
    @PostMapping("rechargeIndex")
    public Result index(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> resultJson = appsCstrRechargeService.index(vo);
            return Result.success(resultJson);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

}
