package com.laiketui.common.api.plugin.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.goods.AddStockVo;

/**
 * @Author: sunH_
 * @Date: Create in 15:33 2022/10/13
 */
public interface PublicSupplierStockService
{

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

}
