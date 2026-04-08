package com.laiketui.admins.api.admin.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.auction.AddAuctionConfigVo;
import com.laiketui.domain.vo.plugin.auction.AddAuctionVo;

import java.util.Map;

/**
 * 竞拍后台管理
 *
 * @author Trick
 * @date 2021/5/14 12:03
 */
public interface AdminAuctionService
{

    /**
     * 竞拍商品首页数据
     *
     * @param vo        -
     * @param goodsName -
     * @param status    -
     * @param id        -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 12:08
     */
    Map<String, Object> index(MainVo vo, String goodsName, Integer status, Integer id) throws LaiKeAPIException;


    /**
     * 编辑竞拍商品-加载数据
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 14:46
     */
    Map<String, Object> auctionDetail(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 获取竞拍商品列表
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @param title   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 15:20
     */
    Map<String, Object> getAuctionGoods(MainVo vo, Integer classId, Integer brandId, String title) throws LaiKeAPIException;

    /**
     * 添加/编辑竞拍商品信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 15:16
     */
    Map<String, Object> addAuction(AddAuctionVo vo) throws LaiKeAPIException;


    /**
     * 竞拍是否显示开关
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 16:31
     */
    Map<String, Object> showSwitch(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 获取竞拍详情
     *
     * @param vo     -
     * @param id     -
     * @param name   -
     * @param userId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/14 17:30
     */
    Map<String, Object> getAcutionDetail(MainVo vo, int id, String name, Integer userId) throws LaiKeAPIException;


    /**
     * 删除竞拍活动
     *
     * @param vo  -
     * @param ids -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 9:26
     */
    Map<String, Object> del(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 开始，结束活动
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 10:09
     */
    Map<String, Object> siwtch(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 保证金列表
     *
     * @param vo        -
     * @param userName  -
     * @param type      -
     * @param startDate -
     * @param endDate   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 15:14
     */
    Map<String, Object> promiseList(MainVo vo, String userName, Integer type, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 获取竞拍设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 16:32
     */
    Map<String, Object> config(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑竞拍配置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 16:37
     */
    Map<String, Object> addConfig(AddAuctionConfigVo vo) throws LaiKeAPIException;
}
