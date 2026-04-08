package com.laiketui.plugins.api.distribution.ext;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 分销 ：新的移动端分类页面
 *
 * @date 2024/4/2
 */
public interface PluginsDistributionCategoryService
{

    /**
     * 返回全部分类
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> getAllClass(MainVo vo) throws LaiKeAPIException;

    /**
     * 根据分类id获取普通商品+ 分销商品
     *
     * @param vo
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> getProductListByClassId(MainVo vo, Integer cid) throws LaiKeAPIException;

    /**
     * 根据分类id获取普通商品
     *
     * @param vo
     * @param cid
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getPtProductListByClassId(MainVo vo, Integer cid, String product_title) throws LaiKeAPIException;

}
