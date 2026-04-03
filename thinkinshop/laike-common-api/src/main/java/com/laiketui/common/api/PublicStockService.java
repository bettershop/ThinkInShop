package com.laiketui.common.api;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.vo.goods.AddStockVo;

/**
 * 关于库存
 *
 * @author Trick
 * @date 2020/11/19 15:56
 */
public interface PublicStockService
{


    /**
     * 保存属性值
     *
     * @param skuModel -
     * @param num      - 当前属性值下标
     * @return SkuModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/21 13:58
     */
    SkuModel saveSku(SkuModel skuModel, int num) throws LaiKeAPIException;


    /**
     * 商品库存预警
     *
     * @param storeId -
     * @param attrId  -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/4 15:02
     */
    boolean addStockWarning(int storeId, int attrId) throws LaiKeAPIException;

    /**
     * 增减商品库存
     *
     * @param vo    -
     * @param uname - 操作库存账号
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 14:34
     */
    void addGoodsStock(AddStockVo vo, String uname) throws LaiKeAPIException;

    /**
     * 同步供应商商品下级所有代售
     *
     * @param storeId
     * @param cid
     * @param pid
     * @param stockNum
     * @param isUpStockTotal
     * @throws LaiKeAPIException
     */
    void synchronizationOtherStock(int storeId, int cid, int pid, int stockNum, boolean isUpStockTotal) throws LaiKeAPIException;

    /**
     * 增减供应商商品库存
     *
     * @param vo    -
     * @param uname - 操作库存账号
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/2 14:34
     */
    void addSupplierGoodsStock(AddStockVo vo, String uname) throws LaiKeAPIException;

    /**
     * 修正商品总库存
     *
     * @param storeId -
     * @param goodsId -
     * @return int - 库存数量
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/15 14:08
     */
    int refreshGoodsStock(int storeId, int goodsId) throws LaiKeAPIException;

    /**
     * 商品出库
     *
     * @param storeId -
     * @param goodsId -
     * @param attrId  -
     * @param needNum -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/19 14:33
     */
    void outStockNum(int storeId, int goodsId, int attrId, int needNum) throws LaiKeAPIException;

    /**
     * 商品出库 -
     *
     * @param storeId  -
     * @param goodsId  -
     * @param attrId   -
     * @param needNum  -
     * @param isPlugin - 如果是插件则不更新销量
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021-11-17 15:45:26
     */
    void outStockNum(int storeId, int goodsId, int attrId, int needNum, boolean isPlugin) throws LaiKeAPIException;
}
