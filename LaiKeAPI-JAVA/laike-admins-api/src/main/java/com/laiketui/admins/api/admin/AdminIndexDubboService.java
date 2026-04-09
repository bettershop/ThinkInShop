package com.laiketui.admins.api.admin;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 首页
 *
 * @author Trick
 * @date 2020/12/28 14:34
 */
public interface AdminIndexDubboService
{

    /**
     * 商城首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/13 17:18
     */
    Map<String, Object> home(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 首页
     * 【php JurisdictionAction.getDefaultView】
     *
     * @param vo       -
     * @param response -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 14:37
     */
    Map<String, Object> index(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException;


    /**
     * 获取商品状态信息
     * 【php JurisdictionAction.execute】
     *
     * @param vo  -
     * @param ids -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/28 16:07
     */
    Map<String, Object> goodsStatus(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 是否显示下架商品
     * 【php JurisdictionAction.is_open】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/28 16:44
     */
    Map<String, Object> isopen(MainVo vo) throws LaiKeAPIException;

    /**
     * 是否展示已售罄的商品
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void displaySellOut(MainVo vo) throws LaiKeAPIException;
}
