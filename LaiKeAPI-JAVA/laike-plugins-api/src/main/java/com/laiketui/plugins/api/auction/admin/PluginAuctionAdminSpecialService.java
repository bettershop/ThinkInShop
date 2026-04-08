package com.laiketui.plugins.api.auction.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddSpecialVo;

import java.util.Map;

/**
 * 后台-竞拍专场接口
 *
 * @author Trick
 * @date 2022/7/1 15:21
 */
public interface PluginAuctionAdminSpecialService
{

    /**
     * 获取竞拍专场
     *
     * @param vo        -
     * @param key       - id/专场名称/店铺名称
     * @param status    - 1=未开始 2=进行中 3=已结束
     * @param type      -
     * @param startDate -
     * @param endDate   -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:23
     */
    Map<String, Object> getSpecialList(MainVo vo, String key, Integer status, Integer type, String startDate, String endDate, String id) throws LaiKeAPIException;

    /**
     * 查看拍品
     *
     * @param vo        -
     * @param goodsName -
     * @param specialId -
     * @param sessionId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/21 12:06
     */
    Map<String, Object> lookGoodsList(MainVo vo, String goodsName, String specialId, String sessionId) throws LaiKeAPIException;

    /**
     * 获取专场下拉
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/4 14:13
     */
    Map<String, Object> getSpecials(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加竞拍专场
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:36
     */
    void addSpecial(AddSpecialVo vo) throws LaiKeAPIException;

    /**
     * 删除专场
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:51
     */
    void delSpecial(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 快速开始专场
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 15:59
     */
    void startedSpecial(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 专场是否显示
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/2 11:05
     */
    void switchSpecial(MainVo vo, String id) throws LaiKeAPIException;

}
