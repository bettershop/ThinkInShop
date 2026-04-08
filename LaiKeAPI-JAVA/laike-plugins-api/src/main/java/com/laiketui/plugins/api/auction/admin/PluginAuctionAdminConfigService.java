package com.laiketui.plugins.api.auction.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddAuctionConfigVo;

import java.util.Map;

/**
 * 竞拍配置接口
 *
 * @author Trick
 * @date 2022/7/1 16:14
 */
public interface PluginAuctionAdminConfigService
{

    /**
     * 竞拍设置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 16:15
     */
    void addAuctionConfig(AddAuctionConfigVo vo) throws LaiKeAPIException;

    /**
     * 获取竞拍设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/1 16:16
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取保证金列表
     *
     * @param vo          -
     * @param key         -
     * @param specialName -
     * @param type        -
     * @param startDate   -
     * @param endDate     -
     * @param specialId   - 专场id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 14:16
     */
    Map<String, Object> getPromiseList(MainVo vo, String key, String specialName, Integer type, String startDate, String endDate, String specialId) throws LaiKeAPIException;

    /**
     * 删除竞拍保证金
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 15:06
     */
    void delPromise(MainVo vo, Integer id) throws LaiKeAPIException;
}
