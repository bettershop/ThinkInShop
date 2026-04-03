package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddFreihtVo;

import java.util.Map;

/**
 * 运费管理
 *
 * @author Trick
 * @date 2021/6/8 10:20
 */
public interface MchFreightService
{

    /**
     * 运费列表
     *
     * @param vo     -
     * @param fid    -
     * @param status -
     * @return Map
     * @throws LaiKeAPIException -
     * @date 2021/6/8 10:23
     * @author Trick
     */
    Map<String, Object> index(MainVo vo, Integer fid, Integer status) throws LaiKeAPIException;

    interface IndexStatus
    {
        /**
         * 未使用
         */
        Integer NOT_USED = 0;
        /**
         * 使用中
         */
        Integer IN_USE   = 1;

    }


    /**
     * 添加/编辑运费
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/8 14:53
     */
    void addFreight(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 删除运费
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/8 17:44
     */
    void freightDel(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 设置默认运费
     *
     * @param vo -
     * @param id -
     * @return void
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/8 17:44
     */
    void setDefault(MainVo vo, int id) throws LaiKeAPIException;
}
