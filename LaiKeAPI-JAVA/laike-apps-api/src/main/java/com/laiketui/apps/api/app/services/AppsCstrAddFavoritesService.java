package com.laiketui.apps.api.app.services;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 收藏接口
 *
 * @author Trick
 * @date 2020/10/22 15:18
 */
public interface AppsCstrAddFavoritesService
{


    /**
     * 点击收藏
     *
     * @param vo    - 商店
     * @param proId - 商品id
     * @param type  - 类型
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/22 15:20
     */
    Map<String, Object> index(MainVo vo, int proId, int type) throws LaiKeAPIException;


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
     * 查看收藏
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/9 9:03
     */
    Map<String, Object> collection(MainVo vo, int type) throws LaiKeAPIException;

    /**
     * 类型 1=商品收藏 2=店铺收藏
     */
    interface CollectionType
    {
        Integer COMMODITY = 1;
        Integer STORE     = 2;
    }


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
}
