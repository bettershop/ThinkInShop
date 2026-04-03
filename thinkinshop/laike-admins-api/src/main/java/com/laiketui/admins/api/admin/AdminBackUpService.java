package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/8/19
 */
public interface AdminBackUpService
{

    /**
     * 查询配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> queryConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加数据备份配置表
     *
     * @param vo
     * @param is_open       是否开启，0关闭，1开启
     * @param execute_cycle 执行周期
     * @param url           路径
     * @throws LaiKeAPIException
     */
    void addConfig(MainVo vo, Integer is_open, String execute_cycle, String url, Integer selectDate) throws LaiKeAPIException;

    /**
     * 查询数据备份记录
     *
     * @param vo
     * @return
     */
    Map<String, Object> backUpRecord(MainVo vo) throws LaiKeAPIException;

    /**
     * 删除数据备份记录
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void delBackUpRecord(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 还原
     *
     * @param vo
     * @param id
     * @throws LaiKeAPIException
     */
    void reduction(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 立即备份
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void immediately(MainVo vo) throws LaiKeAPIException;

    /**
     * 根据商城id获取对应的执行定时任务参数
     *
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    String getNewCronByStoreId(Integer storeId) throws LaiKeAPIException;

    /**
     * 定时任务执行备份
     *
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    Runnable immediatelyTask(Integer storeId) throws LaiKeAPIException;

    /**
     * 缓存搜有商城id
     */
    void taskStoreAll();
}
