package com.laiketui.plugins.api.pc;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
import com.laiketui.domain.vo.saas.AddBlockGoodsVo;
import com.laiketui.domain.vo.saas.BlockManageVo;

import java.util.Map;

/**
 * 楼层管理
 *
 * @author Trick
 * @date 2023/2/15 11:08
 */
public interface AdminBlockHomeModelService
{

    /**
     * 添加/编辑 楼层下的商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:08
     */
    Map<String, Object> addOrDeleteGoodsWithBlock(AddBlockGoodsVo vo) throws LaiKeAPIException;

    /**
     * 获取楼层列表
     *
     * @param vo   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:11
     */
    Map<String, Object> list(MainVo vo, String name) throws LaiKeAPIException;

    /**
     * 单个楼层查询
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:15
     */
    Map<String, Object> getBlockById(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 添加/编辑楼层
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:16
     */
    void add(BlockManageVo vo) throws LaiKeAPIException;

    /**
     * 获取指定楼层下的所有商品
     *
     * @param vo         -
     * @param id         -
     * @param key        -
     * @param cid        -
     * @param bid        -
     * @param isPlatform -
     * @param tradeModel -
     * @param startDate  -
     * @param endDate    -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:18
     */
    Map<String, Object> getGoodsByBlock(MainVo vo, String id, String key, String cid, String bid, Integer tradeModel, String isPlatform, String startDate, String endDate, String sourceId, String sourceType) throws LaiKeAPIException;

    /**
     * 删除楼层及下面的商品
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 11:21
     */
    void deleteBlock(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 删除楼层下的指定商品
     *
     * @param vo       -
     * @param goodsIds -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/15 16:55
     */
    void delGoods(MainVo vo, String id, String goodsIds) throws LaiKeAPIException;

    /**
     * 商品排序
     *
     * @param vo        -
     * @param mappingId -
     * @param sort      -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/16 14:10
     */
    void editSort(MainVo vo, int mappingId, int sort) throws LaiKeAPIException;

    /**
     * 楼层是否开启显示
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/2/16 14:10
     */
    void EnableOrNot(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 获取指定商品列表
     *
     * @param vo
     * @param id
     * @return
     */
    Map<String, Object> getSpecifiedGoodsInfo(GoodsConfigureVo vo, String id, String sourceId, String sourceType) throws LaiKeAPIException;

    /**
     * 获取所有店铺或者供应商
     *
     * @param vo
     * @param keyWord
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getALlMchOrSupplier(MainVo vo, String keyWord) throws LaiKeAPIException;

}
