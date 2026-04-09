package com.laiketui.plugins.api.seckill.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.group.QueryOpenGroupVo;
import com.laiketui.domain.vo.sec.AddSecondsVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 平台活动管理接口
 *
 * @author Trick
 * @date 2021/5/17 17:39
 */
public interface PluginsSecPlatFormService
{


    /**
     * 获取平台活动列表
     *
     * @param vo   -
     * @param name -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/17 17:49
     */
    Map<String, Object> index(MainVo vo, String name, String type) throws LaiKeAPIException;


    /**
     * 获取时段列表数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 10:16
     */
    Map<String, Object> getSecTimeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/删除秒杀时段
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 10:16
     */
    Map<String, Object> addSecTime(AddSecondsVo vo) throws LaiKeAPIException;


    /**
     * 删除秒杀时间段
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 10:16
     */
    Map<String, Object> delSecTime(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取 参团/开团 列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 10:56
     */
    Map<String, Object> canGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException;


    /**
     * 商品审核列表
     *
     * @param vo        -
     * @param plateId   -
     * @param id        -
     * @param status    - 审核状态1.提交审核，2.审核通过，3.审核失败
     * @param goodsName -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 16:01
     */
    Map<String, Object> groupExaminList(MainVo vo, Integer plateId, Integer id, Integer status, String goodsName) throws LaiKeAPIException;


    /**
     * 拼团商品审核
     *
     * @param vo        -
     * @param groupId   -
     * @param reason    -
     * @param groupCan  -
     * @param groupOpen -
     * @param groupMan  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/18 16:42
     */
    Map<String, Object> groupExamin(MainVo vo, int groupId, String reason, BigDecimal groupCan, BigDecimal groupOpen, Integer groupMan) throws LaiKeAPIException;


    /**
     * 批量删除活动
     *
     * @param vo  -
     * @param ids -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/19 9:25
     */
    Map<String, Object> delPlateForm(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 拼团明细
     *
     * @param vo     -
     * @param ptcode -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/19 9:43
     */
    Map<String, Object> groupDetailList(MainVo vo, String ptcode) throws LaiKeAPIException;
}
