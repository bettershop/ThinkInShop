package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.FreightModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 运费 sql
 *
 * @author Trick
 * @date 2020/11/16 11:03
 */
public interface FreightModelMapper extends BaseMapper<FreightModel>
{


    /**
     * 获取运费信息
     *
     * @param freightModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/16 11:05
     */
    List<Map<String, Object>> getFreightInfo(FreightModel freightModel) throws LaiKeAPIException;


    /**
     * 动态统计运费
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 15:08
     */
    int countFreightDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 设置默认
     *
     * @param storeId       -
     * @param mchId         -
     * @param updateDefault -
     * @param isDefault     -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 17:15
     */
    @Update("update lkt_freight set is_default = #{updateDefault} where store_id = #{storeId} and mch_id = #{mchId} and is_default = #{isDefault}")
    int setDefault(@Param("storeId") int storeId, @Param("mchId") int mchId, @Param("updateDefault") int updateDefault, @Param("isDefault") int isDefault) throws LaiKeAPIException;

    /**
     * 删除默认运费则将下一个运费设为默认
     *
     * @param storeId       -
     * @param mchId         -
     * @param updateDefault -
     * @param isDefault     -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 17:15
     */
    @Update("update lkt_freight set is_default = #{updateDefault} where store_id = #{storeId} and mch_id = #{mchId} and is_default = #{isDefault} ORDER BY add_time DEsc LIMIT 1")
    int setDefaultDel(@Param("storeId") int storeId, @Param("mchId") int mchId, @Param("updateDefault") int updateDefault, @Param("isDefault") int isDefault) throws LaiKeAPIException;

    /**
     * 获取运费商品信息动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 15:18
     */
    List<Map<String, Object>> getFreightInfoLeftGoodsDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取运费商品信息动态sql 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/3 13:49
     */
    int countFreightInfoLeftGoodsDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 运费信息动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 10:18
     */
    List<Map<String, Object>> getFreightInfoDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取供应商运费动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/11/7 15:18
     */
    List<Map<String, Object>> getSupplierFreight(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取供应商运费动态sql 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/11/7 15:18
     */
    int countSupplierFreight(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 供应商运费设置默认
     *
     * @param storeId
     * @param supplier
     * @param updateDefault
     * @param isDefault
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_freight set is_default = #{updateDefault} where store_id = #{storeId} and supplier_id = #{supplier} and is_default = #{isDefault}")
    int setSupplierDefault(int storeId, int supplier, int updateDefault, int isDefault) throws LaiKeAPIException;

}