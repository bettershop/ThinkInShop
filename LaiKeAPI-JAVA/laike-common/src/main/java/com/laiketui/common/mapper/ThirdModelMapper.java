package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.home.ThirdModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;


/**
 * 第三方授权表
 *
 * @author Trick
 * @date 2021/2/4 10:29
 */
public interface ThirdModelMapper extends BaseMapper<ThirdModel>
{


    /**
     * 获取第三方授权信息
     *
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 10:30
     */
    @Select("select * from lkt_third order by id limit 1")
    Map<String, Object> selectThirdParmateOne() throws LaiKeAPIException;

    /**
     * 修改小程序地址
     *
     * @param h5Domain
     */
    @Update("UPDATE  `lkt_third` set work_domain = #{h5Domain}")
    Integer updateWorkDomain(String h5Domain);
}