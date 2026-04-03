package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.AddStockVo;

import java.util.Map;

/**
 * 库存管理
 *
 * @author Trick
 * @date 2021/6/2 11:39
 */
public interface MchStockService
{

    /**
     * 获取库存列表
     *
     * @param vo        -
     * @param goodsName -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 11:42
     */
    Map<String, Object> index(MainVo vo, String goodsName) throws LaiKeAPIException;

    /**
     * 添加商品库存
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 14:47
     */
    Map<String, Object> addStock(AddStockVo vo) throws LaiKeAPIException;


    /**
     * 获取库存信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 14:57
     */
    Map<String, Object> getStockInfoById(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取库存明细
     *
     * @param vo  -
     * @param id  -
     * @param pid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 15:06
     */
    Map<String, Object> getStockDetail(MainVo vo, int id, int pid) throws LaiKeAPIException;


    /**
     * 获取库存列表
     *
     * @param vo        -
     * @param goodsId   -
     * @param goodsName -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 16:01
     */
    Map<String, Object> getStockWarningInfo(MainVo vo, Integer goodsId, String goodsName, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 获取库存 出/入库信息
     *
     * @param vo        -
     * @param goodsName -
     * @param startDate -
     * @param endDate   -
     * @param type      -   0=入库 1=出库
     * @return null
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 17:07
     */
    Map<String, Object> getStockInOutInfo(MainVo vo, String goodsName, String startDate, String endDate, int type) throws LaiKeAPIException;
}
