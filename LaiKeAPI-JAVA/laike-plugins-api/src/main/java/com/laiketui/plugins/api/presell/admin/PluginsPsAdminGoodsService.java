package com.laiketui.plugins.api.presell.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 预售商品
 *
 * @author sunH_
 * @date 2021/12/20 15:00
 */
public interface PluginsPsAdminGoodsService
{

    /**
     * 添加预售商品
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 15:00
     */
    void addOrUpdateGoods(UploadMerchandiseVo vo) throws LaiKeAPIException;

    /**
     * 上下架预售商品
     *
     * @param vo       -
     * @param goodsIds -
     * @param status   -
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2021/12/20 16:30
     */
    void upperAndLowerGoods(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException;

    /**
     * 删除预售商品
     *
     * @param vo       -
     * @param goodsIds - 商品id,支持多个
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2021/12/20 17:00
     */
    void delete(MainVo vo, String goodsIds) throws LaiKeAPIException;

    /**
     * 根据id获取预售商品信息
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> getGoodsInfoById(MainVo vo, Integer goodsId) throws LaiKeAPIException;

    /**
     * 预售商品列表
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2020/12/20 18:00
     */
    Map<String, Object> getGoodsList(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException;
}
