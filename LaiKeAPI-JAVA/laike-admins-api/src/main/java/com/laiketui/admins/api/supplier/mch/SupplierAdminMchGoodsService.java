package com.laiketui.admins.api.supplier.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.domain.vo.supplier.GoodsQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 15:56 2022/9/21
 */
public interface SupplierAdminMchGoodsService
{

    /**
     * 店铺端查询商品池
     *
     * @param vo
     * @param response
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> proList(GoodsQueryVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 店铺端可选供应商商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/01/29 11:32
     */
    Map<String, Object> consignmentPro(DefaultViewVo vo) throws LaiKeAPIException;

    /**
     * 根据id获取商品信息
     *
     * @param vo     -
     * @param goodId -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, int goodId) throws LaiKeAPIException;

    /**
     * 上下架商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @return boolean
     * @throws LaiKeAPIException -
     */
    boolean upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;

    /**
     * 根据商品id删除商品
     *
     * @param vo       -
     * @param goodsIds -
     * @throws LaiKeAPIException -
     */
    void delGoodsById(MainVo vo, String goodsIds) throws LaiKeAPIException;

    /**
     * 编辑商品序号
     *
     * @param vo   -
     * @param id   -
     * @param sort -
     * @throws LaiKeAPIException-
     */
    void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException;

    /**
     * 添加供应商商品
     *
     * @param vo       -
     * @param goodsIds -
     * @throws LaiKeAPIException-
     */
    void addSupplierPro(MainVo vo, String goodsIds) throws LaiKeAPIException;

    /**
     * 添加商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;
}
