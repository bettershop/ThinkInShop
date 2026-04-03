package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.StockInfoVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 库存管理
 *
 * @author Trick
 * @date 2021/1/4 9:15
 */
public interface AdminStockService
{


    /**
     * 获取库存信息列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 10:53
     */
    Map<String, Object> getStockInfo(StockInfoVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取库存详细信息列表
     *
     * @param vo     -
     * @param attrId - 属性id
     * @param pid    - 商品id
     * @param type   - 类型 0.入库 1.出库 2.预警
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 10:53
     */
    Map<String, Object> getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 增加库存
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 14:21
     */
    Map<String, Object> addStock(AddStockVo vo) throws LaiKeAPIException;

    /**
     * 批量增加库存
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 14:21
     */
    Map<String, Object> batchAddStock(AddStockVo vo) throws LaiKeAPIException;
}
