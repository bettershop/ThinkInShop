package com.laiketui.plugins.api.distribution.goods;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.AddDistributionGoodsVo;

import java.util.List;
import java.util.Map;

/**
 * 商品分销实现
 *
 * @author Trick
 * @date 2021/2/8 9:24
 */
public interface PluginsDistributionGoodsService
{
    /**
     * 获取分销商品列表
     *
     * @param vo        -
     * @param id        -
     * @param goodsNo   -
     * @param goodsName -
     * @param level     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 17:03
     */
    Map<String, Object> getGoodsDistributionInfo(MainVo vo, Integer id, String goodsNo, String goodsName, Integer level, String mchName) throws LaiKeAPIException;


    /**
     * 获取非分销商品列表
     *
     * @param vo        -
     * @param classId   -
     * @param goodsName -
     * @return LaiKeAPIException
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 9:27
     */
    Map<String, Object> getGoodsNotDistributionInfo(MainVo vo, Integer classId, String goodsName, String mchName) throws LaiKeAPIException;

    /**
     * 添加/编辑分销商品
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 10:22
     */
    boolean addDistributionGoods(AddDistributionGoodsVo vo) throws LaiKeAPIException;


    /**
     * 批量删除分销商品
     *
     * @param ids -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 10:59
     */
    boolean delDistributionGoods(MainVo vo, List<Integer> ids) throws LaiKeAPIException;
}
