package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.GuideModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;

import java.util.List;
import java.util.Map;

/**
 * 首页接口
 *
 * @author Trick
 * @date 2020/10/10 9:24
 */
public interface AppsCstrIndexService
{


    /**
     * 是否是div接口
     *
     * @param storeId -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 9:26
     */
    boolean hasDiy(Integer storeId) throws LaiKeAPIException;

    /**
     * 插件状态
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> pluginStatus(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 首页接口
     *
     * @param storeId   - 商店
     * @param accessId  - 授权id
     * @param storeType - 来源
     * @param language  - 语言
     * @param longitude - 经度
     * @param latitude  - 纬度
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 11:28
     */
    Map<String, Object> index(MainVo vo, String longitude, String latitude) throws LaiKeAPIException;


    /**
     * 获取会员状态
     * 【php index.get_membership_status】
     *
     * @param storeId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/23 16:39
     */
    Map<String, Object> getMembershipStatus(int storeId) throws LaiKeAPIException;

    /**
     * 获取分类列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/9/5 19:00
     */
    Map<String, Object> classList(MainVo vo, Integer shop_id, String sort_criteria, String sort) throws LaiKeAPIException;

    /**
     * 加载更多商品
     *
     * @param vo  -
     * @param cid - 分类id
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 17:11
     */
    List<Map<String, Object>> getMore(MainVo vo, int cid, String sort_criteria, String sort) throws LaiKeAPIException;


    /**
     * 获取地理位置
     *
     * @param vo      -
     * @param groupId - 县/区
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 10:37
     */
    Map<String, Object> getLocation(MainVo vo, Integer groupId) throws LaiKeAPIException;


    /**
     * 获取引导图
     *
     * @param storeId   -
     * @param accessId  -
     * @param language  -
     * @param storeType - 来源
     * @return GuideModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/26 9:13
     */
    List<GuideModel> guidedGraph(int storeId, String accessId, String language, int storeType) throws LaiKeAPIException;


    /**
     * 新品上市
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/26 13:43
     */
    Map<String, Object> newArrival(MainVo vo) throws LaiKeAPIException;


    /**
     * 推荐门店
     *
     * @param longitude - 经度
     * @param latitude  - 纬度
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/26 14:58
     */
    Map<String, Object> recommendStores(MainVo vo, String longitude, String latitude, Integer cid,String lang_code) throws LaiKeAPIException;

    /**
     * 店铺分类
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> mchClass(MainVo vo) throws LaiKeAPIException;


    /**
     * 更改语言
     *
     * @param storeId  -
     * @param accessId -
     * @param language -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 9:18
     */
    boolean selectLanguage(int storeId, String accessId, String language) throws LaiKeAPIException;


    /**
     * 好物优选
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 9:52
     */
    Map<String, Object> recommend(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取平台用户公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo);


    /**
     * 标记公告以读
     *
     * @param vo
     * @param tell_id 公告id
     */
    void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException;

    /**
     * 查询正在直播的直播
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryLiving(MainVo vo) throws LaiKeAPIException;


    /**
     * 更改货币
     *
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 9:18
     */
    void changeCurrency(CurrencyStoreVo vo) throws LaiKeAPIException;

    /**
     * 获取分销商品信息
     * @param vo
     * @return
     */
    Map<String,Object> distributionList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取推荐商家
     * @param vo
     * @return
     */
    Map<String,Object> getMchList(MainVo vo) throws LaiKeAPIException;

}
