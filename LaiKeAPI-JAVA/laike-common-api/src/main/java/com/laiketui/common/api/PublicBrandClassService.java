package com.laiketui.common.api;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.BrandClassVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 关于商品分类公共类
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
public interface PublicBrandClassService
{


    interface ClassType
    {
        /**
         * 查询下级
         */
        Integer SUBORDINATE = 1;
        /**
         * 查询上级
         */
        Integer SUPERIOR    = 2;
        /**
         * 根据id查询
         */
        Integer ID          = 3;
        /**
         * 查询第一级
         */
        Integer FIRST_STAGE = 0;
    }


    /**
     * 查询品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 10:12
     */
    Map<String, Object> getBrandInfo(BrandClassVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 查询待审核类别
     *
     * @param vo        -
     * @param id
     * @param condition
     * @param status
     * @param startTime
     * @param endTime
     * @param mch_id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 10:12
     */
    Map<String, Object> getExamineBrandInfo(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime, Integer mch_id) throws LaiKeAPIException;


    /**
     * 增加/修改品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    boolean addBrand(BrandClassVo vo) throws LaiKeAPIException;


}
