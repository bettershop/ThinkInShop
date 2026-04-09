package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.CollectionVo;
import com.laiketui.domain.vo.user.SearchVo;

import java.util.Map;

/**
 * 商城首页
 *
 * @author Trick
 * @date 2021/6/17 14:12
 */
public interface AppsMallHomeService
{

    /**
     * 获取用户信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 14:12
     */
    Map<String, Object> index(MainVo vo,Integer is_show_auction) throws LaiKeAPIException;

    /**
     * 获取首页数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 16:22
     */
    Map<String, Object> home(MainVo vo) throws LaiKeAPIException;

    /**
     * 加载热销商品列表
     *
     * @param vo    -
     * @param sType -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/2/15 14:29
     */
    Map<String, Object> loadHotGoodsList(MainVo vo, Integer sType) throws LaiKeAPIException;

    /**
     * 首页商品加载更多
     *
     * @param vo   -
     * @param type -产品值属性 1：新品,2：热销，3：推荐
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 9:09
     */
    Map<String, Object> homeDetail(MainVo vo, int type) throws LaiKeAPIException;

    /**
     * 首页商品加载更多
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 9:09
     */
    Map<String, Object> getMemberPro(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取用户搜索过的关键字
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 10:14
     */
    Map<String, Object> clickSearch(MainVo vo) throws LaiKeAPIException;

    /**
     * 删除用户搜索信息
     *
     * @param vo  -
     * @param key -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 10:45
     */
    void delSearch(MainVo vo, String key) throws LaiKeAPIException;

    /**
     * 一键清空所有关键字
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 10:54
     */
    void delSearchAll(MainVo vo) throws LaiKeAPIException;

    /**
     * 搜索
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 11:02
     */
    Map<String, Object> search(SearchVo vo) throws LaiKeAPIException;

    /**
     * 查看收藏
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/18 14:31
     */
    Map<String, Object> collection(CollectionVo vo) throws LaiKeAPIException;


    /**
     * 点击收藏
     *
     * @param vo    - 商店
     * @param proId - 商品id
     * @param type  - 类型
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-18 15:35:55
     */
    boolean index(MainVo vo, int proId, int type) throws LaiKeAPIException;


    /**
     * 取消收藏商品/店铺
     *
     * @param vo           -
     * @param collectionId -
     * @param type         -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/22 16:19
     */
    void removeFavorites(MainVo vo, String collectionId, int type) throws LaiKeAPIException;


    /**
     * 找相似商品
     *
     * @param vo      -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 14:34
     */
    Map<String, Object> similar(MainVo vo, int goodsId) throws LaiKeAPIException;

    /**
     * PC商城设置国际化语言
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void selectLanguage(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取楼层商品
     *
     * @param vo      -
     * @param blockId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023/6/14
     */
    Map<String, Object> getGoodsList(MainVo vo, String blockId) throws LaiKeAPIException;

    /**
     * 获取楼层商品更多
     *
     * @param vo      -
     * @param blockId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023/6/14
     */
    Map<String, Object> getGoodsListByBlockId(MainVo vo, String blockId) throws LaiKeAPIException;
}
