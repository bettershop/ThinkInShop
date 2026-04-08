package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.systems.SystemTellModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 系统公告
 *
 * @author Trick
 */
public interface SystemTellModelMapper extends BaseMapper<SystemTellModel>
{


    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 11:48
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 11:48
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Update("<script>" +
            " update lkt_system_tell set is_read = 1 where " +
            " <if test='ids != null '> " +
            "   <foreach collection=\"ids\" item=\"id\" separator=\",\" open=\"id in(\" close=\")\"> " +
            "        #{id}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int readSysNotice(@Param("ids") List<String> ids);
}