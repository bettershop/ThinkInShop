package com.laiketui.plugins.api.seckill.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.sec.AddProVo;
import com.laiketui.domain.vo.sec.AddSecondsVo;
import com.laiketui.domain.vo.sec.QueryProVo;
import com.laiketui.domain.vo.sec.QuerySecRecordVo;

import java.util.Map;

/**
 * 秒杀管理接口 弃用
 *
 * @author Trick
 * @date 2021/5/6 9:54
 */
public interface PluginsSecAdminService
{


    /**
     * 获取秒杀活动列表
     * 【php Index.getDefaultView】
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 9:55
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取秒杀详情
     *
     * @param vo       -
     * @param activeNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 10:14
     */
    Map<String, Object> activeDayDetail(MainVo vo, Integer activeNo) throws LaiKeAPIException;


    /**
     * 删除秒杀详情
     *
     * @param vo    -
     * @param secId -
     * @param day   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 11:34
     */
    Map<String, Object> delActive(MainVo vo, Integer secId, String day) throws LaiKeAPIException;


    /**
     * 获取秒杀时段下拉
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 12:01
     */
    Map<String, Object> getSecTimeList(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取秒杀时段列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 18:03
     */
    Map<String, Object> getSecTimeInfoList(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/删除秒杀时段
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 15:16
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
     * @date 2021/5/6 16:20
     */
    Map<String, Object> delSecTime(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取秒杀配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 16:42
     */
    Map<String, Object> getSecConfigInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑秒杀配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 16:50
     */
//    Map<String, Object> addSecConfigInfo(AddSecConfigVo vo) throws LaiKeAPIException;


    /**
     * 获取秒杀插件首页数据
     * <p>
     * 【php Activity.axios】
     *
     * @param vo        -
     * @param timeId    -
     * @param secStatus -
     * @param name      -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/6 17:31
     */
    Map<String, Object> homeIndex(MainVo vo, Integer timeId, Integer secStatus, String name) throws LaiKeAPIException;


    /**
     * 添加/编辑秒杀商品
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 9:43
     */
    void addPro(AddProVo vo) throws LaiKeAPIException;


    /**
     * 获取秒杀商品信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 14:52
     */
    Map<String, Object> getProList(QueryProVo vo) throws LaiKeAPIException;


    /**
     * 删除秒杀活动商品
     *
     * @param vo     -
     * @param proIds - 活动id,支持多个
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 15:47
     */
    Map<String, Object> delPro(MainVo vo, String proIds) throws LaiKeAPIException;


    /**
     * 获取秒杀记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 17:09
     */
    Map<String, Object> getSecRecord(QuerySecRecordVo vo) throws LaiKeAPIException;

    /**
     * 删除秒杀记录
     *
     * @param vo  -
     * @param rid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 17:02
     */
    Map<String, Object> delSecRecord(MainVo vo, int rid) throws LaiKeAPIException;

}
