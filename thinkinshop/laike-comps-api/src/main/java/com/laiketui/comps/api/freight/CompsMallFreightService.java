package com.laiketui.comps.api.freight;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddFreihtVo;

import java.util.Map;

/**
 * 运费管理
 *
 * @author Trick
 * @date 2020/12/31 15:13
 */
public interface CompsMallFreightService
{

    /**
     * 查询运费
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
    Map<String, Object> getMallFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer otype, String name) throws LaiKeAPIException;


    /**
     * 增加/修改运费
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    boolean addMallFreight(AddFreihtVo vo) throws LaiKeAPIException;

    /**
     * 运费设置默认开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 17:00
     */
    void MallFreightSetDefault(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除运费
     *
     * @param vo         -
     * @param freightIds -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 11:18
     */
    void delMallFreight(MainVo vo, String freightIds) throws LaiKeAPIException;
}
