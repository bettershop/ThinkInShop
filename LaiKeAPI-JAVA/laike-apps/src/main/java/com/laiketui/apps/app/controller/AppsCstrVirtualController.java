package com.laiketui.apps.app.controller;


import com.laiketui.apps.api.app.services.AppsCstrVirtualService;
import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.root.annotation.HttpApiMethod;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 二维码
 *
 * @author Trick
 * @date 2020/12/17 12:02
 */
@RestController
@RequestMapping("/app/virtual")
public class AppsCstrVirtualController
{

    @Autowired
    private AppsCstrVirtualService appsCstrVirtualService;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @ApiOperation("虚拟商品查看核销码")
    @PostMapping("/seeWriteCode")
    @HttpApiMethod(urlMapping = "app.order.see_write_code")
    public Result seeWriteCode(OrderVo vo)
    {
        try
        {
            //Map<String, Object> resultMap = orderService.(vo);
            return null;
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }

    @ApiOperation("订单详情中查询核销记录")
    @PostMapping("/getWriteRecord")
    @HttpApiMethod(urlMapping = "app.order.get_write_record")
    public Result getWriteRecord(Integer id)
    {
        try
        {
            Map<String, Object> resultMap = appsCstrVirtualService.getWriteRecord(id);
            return Result.success(resultMap);
        }
        catch (LaiKeAPIException e)
        {
            return Result.error(e.getCode(), e.getMessage());
        }
    }


}
