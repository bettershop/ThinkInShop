package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.AdminRecordModel;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface AdminRecordModelMapper extends BaseMapper<AdminRecordModel>
{


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取管理员日志信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/13 17:25
     */
    List<Map<String, Object>> selectAdminLoggerInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取管理员日志信息 - 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/13 17:32
     */
    int countAdminLoggerInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 删除管理员日志
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/13 17:43
     */
    int delAdminLoggerInfo(Map<String, Object> map) throws LaiKeAPIException;
}