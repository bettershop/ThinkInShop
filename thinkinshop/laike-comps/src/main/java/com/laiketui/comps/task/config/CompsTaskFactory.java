package com.laiketui.comps.task.config;

import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.comps.task.services.CompsTaskServiceImpl;
import com.laiketui.comps.task.services.plugin.CompsTaskAuctionServiceImpl;
import com.laiketui.comps.task.services.plugin.CompsTaskDistributionServiceImpl;
import com.laiketui.comps.task.services.plugin.CompsTaskSecondsSerciceImpl;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.help.SpringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务工厂
 *
 * @author Trick
 * @date 2021/4/13 18:10
 */
@Component
@Deprecated
public class CompsTaskFactory
{

    @Autowired
    SpringHelper springHelper;

    public PublicTaskService getTaskService(String type) throws LaiKeAPIException
    {
        try
        {
            PublicTaskService publicTaskService;
            switch (type)
            {
                case DictionaryConst.OrdersType.ORDERS_HEADER_MS:
                    publicTaskService = springHelper.getBean(CompsTaskSecondsSerciceImpl.class);
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_JP:
                    publicTaskService = springHelper.getBean(CompsTaskAuctionServiceImpl.class);
                    break;
                case DictionaryConst.OrdersType.ORDERS_HEADER_FX:
                    publicTaskService = springHelper.getBean(CompsTaskDistributionServiceImpl.class);
                    break;
                default:
                    publicTaskService = springHelper.getBean(CompsTaskServiceImpl.class);
            }

            return publicTaskService;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, e.getMessage());
        }
    }
}
