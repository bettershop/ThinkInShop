package com.laiketui.common.api;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 关于商品分类公共类
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
public interface PublicGoodsClassService
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
     * 查询类别
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 10:12
     */
    Map<String, Object> getClassInfo(GoodsClassVo vo, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 查询待审核类别
     *
     * @param vo        -
     * @param condition
     * @param status
     * @param startTime
     * @param endTime
     * @param mch_id
     * @param level
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 10:12
     */
    Map<String, Object> getExamineClassInfo(MainVo vo, String condition, Integer status, String startTime, String endTime, Integer mch_id,Integer level) throws LaiKeAPIException;

    /**
     * 添加商品类别
     *
     * @param vo        -
     * @param classId   -
     * @param className -
     * @param ename     -
     * @param img       -
     * @param level     -
     * @param fatherId  -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 15:31
     */
    void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId,Integer type) throws LaiKeAPIException;


}
