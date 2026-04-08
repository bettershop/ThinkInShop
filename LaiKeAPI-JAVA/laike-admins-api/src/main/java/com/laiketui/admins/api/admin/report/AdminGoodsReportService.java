package com.laiketui.admins.api.admin.report;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

public interface AdminGoodsReportService
{
    /**
     * 商品相关指标统计
     *
     * @param storeid
     * @return
     */
    Map<String, Object> getCountList(int storeid) throws LaiKeAPIException;

    /**
     * 商品报表库存及预警的统计
     *
     * @param vo
     * @param type
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getStockRecord(MainVo vo, String type) throws LaiKeAPIException;

    /**
     * 商品数量--入库，出库，预警
     *
     * @param storeid
     * @param type
     * @return
     * @throws LaiKeAPIException
     */
    List<Long> getGoodsStockInfo(int storeid, String type) throws LaiKeAPIException;

    /**
     * 商品销量汇总
     *
     * @param storeid
     * @return
     */
    List<List> getGoodsSalesInfo(int storeid) throws LaiKeAPIException;

    /**
     * 商品状态对应数量
     *
     * @param storeid
     * @return
     * @throws LaiKeAPIException
     */
    List<List> getGoodsSalesInfoByStatus(int storeid) throws LaiKeAPIException;

    /**
     * 商品规格对应商品数量
     *
     * @param storeid
     * @return
     */
    List<List> getSkuNumber(int storeid) throws LaiKeAPIException;

    /**
     * 商品数量报表统计
     *
     * @param storeid
     * @return
     */
    List<List> getGoodsNumList(int storeid) throws LaiKeAPIException;

}
