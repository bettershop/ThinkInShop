package com.laiketui.common.api;


import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;

import java.util.Map;

/**
 * 关于商户后台运费公共类
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
public interface PublicFreightService
{


    /**
     * 运费列表
     * 【php freight.freight_list】
     *
     * @param storeId -
     * @param mchId   -
     * @param name    -
     * @param isUse   - 是否只显示被使用的运费信息
     * @param page    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 14:51
     */
    Map<String, Object> GetFreightList(int storeId, int mchId, String name, Integer isUse, Integer otype, PageModel page) throws LaiKeAPIException;

    /**
     * 运费列表
     * 【php freight.freight_list】
     *
     * @param storeId -
     * @param mchId   -
     * @param name    -
     * @param isUse   - 是否只显示被使用的运费信息
     * @param page    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 14:51
     */
    Map<String, Object> getFreightList(MainVo vo, int mchId, String name, Integer isUse, Integer otype, PageModel page) throws LaiKeAPIException;


    /**
     * 添加/修改运费
     * 【php freight.freight_add】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:15
     */
    boolean FreightToAdd(AddFreihtVo vo) throws LaiKeAPIException;


    /**
     * 编辑运费显示页面
     * 【php freight.freight_modify_show】
     *
     * @param storeId -
     * @param mchId   -
     * @param id      -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 9:10
     */
    Map<String, Object> FreightAndModifyShow(int storeId, int mchId, int id) throws LaiKeAPIException;

    Map<String, Object> freightAndModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException;


    /**
     * 删除运费
     * 【php freight.freight_del】
     *
     * @param storeId -
     * @param ids     -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/1 17:25
     */
    void FreightToDel(int storeId, String ids) throws LaiKeAPIException;


    /**
     * 修改默认运费
     * 【php freight.set_default】
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 16:15
     */
    void setDefaultFreight(AddFreihtVo vo) throws LaiKeAPIException;

    /**
     * 关联商品
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> relatedProducts(AddFreihtVo vo) throws LaiKeAPIException;

}
