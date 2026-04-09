package com.laiketui.plugins.api.integral.admin;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.integral.AddIntegralConfigVo;
import com.laiketui.domain.vo.plugin.integral.AddIntegralVo;
import com.laiketui.domain.vo.sec.QueryProVo;

import java.util.Map;

/**
 * 积分商城
 *
 * @author Trick
 * @date 2021/5/12 9:52
 */
public interface PluginsIntegralAdminService
{

    /**
     * 积分商品列表
     *
     * @param vo        -
     * @param goodsName -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 9:55
     */
    Map<String, Object> index(MainVo vo, String goodsName, Integer id, String mchName, String startTime, String endTime, Integer isPcMch) throws LaiKeAPIException;

    /**
     * 获取商品列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/2 16:45
     */
    Map<String, Object> getProList(QueryProVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑积分商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 11:37
     */
    Map<String, Object> addIntegral(AddIntegralVo vo) throws LaiKeAPIException;


    /**
     * 置顶
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 14:34
     */
    Map<String, Object> top(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 删除
     *
     * @param vo  -
     * @param ids -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 15:55
     */
    Map<String, Object> del(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 获取积分商城配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 16:11
     */
    Map<String, Object> getConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 16:20
     */
    Map<String, Object> addConfigInfo(AddIntegralConfigVo vo) throws LaiKeAPIException;

    /**
     * 添加库存
     *
     * @param vo    -
     * @param proId -
     * @param num   -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/2 9:36
     */
    void addStock(MainVo vo, int proId, int num) throws LaiKeAPIException;

    /**
     * 商品上下架
     *
     * @param vo
     * @param proId
     * @author gp
     * @date 2023-08-31
     */
    void onAndOff(MainVo vo, int proId) throws LaiKeAPIException;

    /**
     * 获取商品兑换记录
     *
     * @param vo
     * @param proId
     * @return
     * @author gp
     * @date 2023-08-31
     */
    Map<String, Object> getRecords(MainVo vo, int proId) throws LaiKeAPIException;
}
