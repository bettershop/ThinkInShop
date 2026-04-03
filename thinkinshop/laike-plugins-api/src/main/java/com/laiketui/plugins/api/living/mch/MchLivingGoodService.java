package com.laiketui.plugins.api.living.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.living.QueryLiveProVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/5
 */
public interface MchLivingGoodService
{
    /**
     * 移动店铺端-直播商品-添加商品
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getProPool(QueryLiveProVo vo) throws LaiKeAPIException;

    /**
     * 移动店铺端-直播商品
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> index(QueryLiveProVo vo) throws LaiKeAPIException;

    /**
     * 加载 修改商品库存 页面
     *
     * @param vo     -
     * @param shopId -
     * @param pid    -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> queryCommissionPage(MainVo vo, int shopId, int pid, String type) throws LaiKeAPIException;

    /**
     * 修改商品规格库存
     *
     * @param vo              -
     * @param shopId          -
     * @param confiGureModels - 商品规格json参数 [{"id":3095,"pid":755,commission:1},{"id":3096,"pid":755,commission:1},...]
     * @throws LaiKeAPIException -
     */
    void upCommission(MainVo vo, int shopId, String confiGureModels) throws LaiKeAPIException;

    /**
     * 移除商品
     *
     * @param vo
     * @param shopId
     * @param pid
     * @throws LaiKeAPIException
     */
    void del_CommissionPro(MainVo vo, int shopId, Integer pid) throws LaiKeAPIException;


    /**
     * 编辑商品-加载数据
     *
     * @param vo     -
     * @param shopId -
     * @param pid    -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> modify(MainVo vo, int shopId, int pid) throws LaiKeAPIException;
}
