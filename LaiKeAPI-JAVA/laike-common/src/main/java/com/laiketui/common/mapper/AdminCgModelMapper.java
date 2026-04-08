package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.vo.saas.DistrictVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 省市县
 *
 * @author Trick
 * @date 2020/12/2 10:53
 */
public interface AdminCgModelMapper extends BaseMapper<AdminCgModel>
{


    /**
     * 获取省市县动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 10:53
     */
    List<Map<String, Object>> getAdminCgInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取未选中省市县动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 10:53
     */
    List<Map<String, Object>> selectNoSheng(Map<String, Object> map) throws LaiKeAPIException;


    //获取所有省份信息
    @Select("select id,district_name name from lkt_map where district_level = 2")
    List<Map<String, Object>> getShenInfList();


    @Select("SELECT MIN(district_level) district_level FROM lkt_map")
    Integer getMinLevel() throws LaiKeAPIException;

    int countDistrict(DistrictVo vo);

    List<Map<String, Object>> queryDistrictList(DistrictVo vo);

    /**
     * 目前中国国内只查询省和市级别的上级地区 区级和县级的目前不支持添加下级
     *
     * @param vo
     */
    @Select("SELECT * FROM lkt_map where district_level < 4")
    List<Map<String, Object>> allDistrict();
}