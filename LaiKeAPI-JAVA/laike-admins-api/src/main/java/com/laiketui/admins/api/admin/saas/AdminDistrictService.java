package com.laiketui.admins.api.admin.saas;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.saas.DistrictVo;

import java.util.Map;

/**
 * 地区信息服务类
 */
public interface AdminDistrictService
{
    /**
     * 获取地区列表信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> districtList(DistrictVo vo) throws LaiKeAPIException;

    /**
     * 保存地区信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void saveOrEditDistrict(DistrictVo vo) throws LaiKeAPIException;

    /**
     * 删除地区信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    void deleteDistrict(DistrictVo vo) throws LaiKeAPIException;

    Map<String, Object> allDistrict() throws LaiKeAPIException;

}
