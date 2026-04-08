package com.laiketui.admins.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddFreihtVo;

import java.util.List;
import java.util.Map;

/**
 * 运费管理
 *
 * @author Trick
 * @date 2020/12/31 15:13
 */
public interface AdminFreightService
{


    /**
     * 查询品牌
     *
     * @param vo     -
     * @param fid    -
     * @param status -
     * @param mchId  -
     * @param name   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    Map<String, Object> getFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, String name) throws LaiKeAPIException;


    /**
     * 获取地区列表
     *
     * @param vo   -
     * @param type -
     * @param sid  - 上级id
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 10:28
     */
    List<AdminCgModel> getRegion(MainVo vo, Integer type, Integer sid) throws LaiKeAPIException;

    /**
     * 运费设置默认开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    void freightSetDefault(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 增加/修改品牌
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    boolean addFreight(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 删除品牌
     *
     * @param vo         -
     * @param freightIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 11:18
     */
    void delFreight(MainVo vo, String freightIds) throws LaiKeAPIException;


}
