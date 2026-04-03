package com.laiketui.comps.order.controlller;

import com.laiketui.comps.order.services.CompsSeataDemoService;
import com.laiketui.core.domain.Result;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.root.annotation.HttpApiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comps/seatademo")
public class CompsSeateDemoController
{

    private final Logger logger = LoggerFactory.getLogger(CompsSeateDemoController.class);

    @Autowired
    CompsSeataDemoService compsSeataDemoService;

    @RequestMapping("/seataDemo")
    @HttpApiMethod(urlMapping = {"app.order.seataDemo"})
    public Result settlement(OrderVo vo) throws Exception
    {
        try
        {
            return Result.success(compsSeataDemoService.seataDemoMethod());
        }
        catch (Exception e)
        {
            throw e;
        }
    }

}
