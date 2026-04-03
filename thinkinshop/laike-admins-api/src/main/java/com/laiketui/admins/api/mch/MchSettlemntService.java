package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.OrderSettlementVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 订单结算
 *
 * @author Trick
 * @date 2021/6/4 16:44
 */
public interface MchSettlemntService
{

    /**
     * 订单结算列表
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 16:52
     */
    Map<String, Object> index(OrderSettlementVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 查看订单明细
     *
     * @param vo  -
     * @param sNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 17:50
     */
    Map<String, Object> orderDetail(MainVo vo, String sNo) throws LaiKeAPIException;


    /**
     * 删除结算订单
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/4 18:01
     */
    void del(MainVo vo, int id) throws LaiKeAPIException;
}
