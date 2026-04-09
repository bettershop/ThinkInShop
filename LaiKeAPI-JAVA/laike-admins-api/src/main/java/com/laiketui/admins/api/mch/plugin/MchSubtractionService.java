package com.laiketui.admins.api.mch.plugin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubConfigVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubtractionVo;

import java.util.Map;

/**
 * 满减
 *
 * @author Trick
 * @date 2021/5/12 17:12
 */
public interface MchSubtractionService
{

    /**
     * 满减活动列表
     *
     * @param vo     -
     * @param title  -
     * @param status -
     * @param id     -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/12 17:18
     */
    Map<String, Object> index(MainVo vo, String title, Integer status, Integer id) throws LaiKeAPIException;

    interface StatusEnum
    {
        /**
         * 未开始
         */
        Integer NOT_START = 1;
        /**
         * 开启
         */
        Integer IN_START  = 2;
        /**
         * 关闭
         */
        Integer CLOSE     = 3;
        /**
         * 已结束
         */
        Integer END       = 4;
    }


    /**
     * 添加/编辑满减
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 9:55
     */
    Map<String, Object> addSubtraction(AddSubtractionVo vo) throws LaiKeAPIException;


    /**
     * 满减活动开始/结束
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 11:36
     */
    Map<String, Object> subtractionSwitch(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 删除满减活动
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 11:36
     */
    Map<String, Object> del(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取满减配置信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 11:59
     */
    Map<String, Object> getonfigInfo(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取满赠商品列表
     *
     * @param vo      -
     * @param classId -
     * @param brandId -
     * @param title   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 13:54
     */
    Map<String, Object> getSubGoods(MainVo vo, Integer classId, Integer brandId, String title) throws LaiKeAPIException;


    /**
     * 添加/编辑满减设置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/13 14:50
     */
    Map<String, Object> addSubConfig(AddSubConfigVo vo) throws LaiKeAPIException;
}
