package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.app.SearchCatVo;
import com.laiketui.domain.vo.app.SearchGoodsVo;
import com.laiketui.domain.vo.goods.GoodsSearchVo;

import java.util.List;
import java.util.Map;

/**
 * 搜索接口
 *
 * @author Trick
 * @date 2020/10/15 12:42
 */
public interface AppsCstrSearchService
{

    /**
     * 热搜接口
     *
     * @param vo   -
     * @param type -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 12:44
     */
    Map<String, Object> hotSearch(MainVo vo, Integer type) throws LaiKeAPIException;


    /**
     * 搜索详情
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 16:50
     */
    Map<String, Object> searchDetail(SearchGoodsVo vo) throws LaiKeAPIException;

    /**
     * 搜索接口
     *
     * @param vo -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 12:44
     */
    Map<String, Object> search(GoodsSearchVo vo) throws LaiKeAPIException;

    /**
     * 搜索提示
     *
     * @param vo      -
     * @param type    - 0=全部 1=热销 2=店铺
     * @param keyword - 关键字
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/15 12:44
     */
    List<String> inputSearch(MainVo vo, Integer type, String keyword) throws LaiKeAPIException;

    /**
     * 搜索界面api
     *
     * @param vo@return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 14:58
     */
    Map<String, Object> index(SearchCatVo vo) throws LaiKeAPIException;
}
