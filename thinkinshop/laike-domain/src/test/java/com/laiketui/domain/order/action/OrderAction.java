package com.laiketui.domain.order.action;

import com.laiketui.domain.plugins.BasePlugins;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: wx
 * @date: Created in 2019/10/25 9:49
 * @version: 1.0
 * @modified By:
 */
public class OrderAction
{

    public Object jiesuan(Object params) throws Exception
    {
        //逻辑
        //先从数据库查询出所有跟订单有关的插件
        List<BasePlugins> plugins = new ArrayList<>();
        for (BasePlugins basePlugins : plugins)
        {
            basePlugins.handle(params);
        }
        //
        return null;
    }

}
