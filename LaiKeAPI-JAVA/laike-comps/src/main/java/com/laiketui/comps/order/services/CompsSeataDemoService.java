package com.laiketui.comps.order.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.domain.seata.SeataAccount;
import com.laiketui.domain.seata.SeataStorage;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CompsSeataDemoService
{

    private final Logger logger = LoggerFactory.getLogger(CompsSeataDemoService.class);

    @Autowired
    private HttpApiUtils httpApiUtils;

    //    @GlobalTransactional
    public String seataDemoMethod() throws Exception
    {
        Map retMap = new HashMap();
        try
        {
            Map<String, Object> paramMap1 = new HashMap<>(1);
            MainVo              vo        = new MainVo();
            vo.setAccessId("eyJUeXBlIjoiSnd0IiwiYWxnIjoiSFMyNTYifQ.eyJpYXQiOjE2NjUyMDk3MTgsImV4cCI6NTM5NzY4OTcxOH0.26EUMXOjpN8y5ga07NSA0POyGi1QNcByPGHZS0f33LE");
            vo.setStoreId(1);
            vo.setStoreType(7);
            vo.setExportType(1);
            vo.setLanguage("cn");
            paramMap1.put("vo", JSON.toJSONString(vo));
            Map<String, Object> resultMap = httpApiUtils.executeApi("com.laike.app.token", JSON.toJSONString(paramMap1));
            System.out.println(JSON.toJSONString(resultMap));
            SeataStorage storage = new SeataStorage();
            storage.setNum(222);
            storage.setSid(3);
            storage.setPid(1);
            paramMap1.put("storage", JSON.toJSONString(storage));
            Object ret = httpApiUtils.executeApi("laike.seata.updatekuncun", JSON.toJSONString(paramMap1));
            System.out.println(ret);
            //这里可以看到 t_KUCUN表的num变成 222
            Thread.sleep(5000);

            //这里人为制造异常后 ， 可以看到 t_KUCUN表的num变成 2 数据回滚了 分布式事务起效
            int k = 1 / 0;

            SeataAccount account = new SeataAccount();
            account.setId(1);
            account.setUser_name("J.M.M-吉米");
            paramMap1.put("account", JSON.toJSONString(account));
            Object ret1 = httpApiUtils.executeApi("laike.seata.updateuser", JSON.toJSONString(paramMap1));
            System.out.println(ret1);

        }
        catch (Exception e)
        {
            throw e;
        }
        retMap.put("msg", "success");
        return JSON.toJSONString(retMap);
    }

}
