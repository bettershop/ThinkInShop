package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


/**
 * 运费 sql
 *
 * @author Trick
 * @date 2020/11/16 11:03
 */
public interface MchFreightModelMapper extends BaseMapper<FreightModel>
{


    /**
     * 获取运费信息
     *
     * @return List
     * @throws LaiKeAPIException -
     * @ param freightModel -
     * @author Trick
     * @date 2020/11/16 11:05
     */
    @Select("select id,name from lkt_freight where store_id = #{store_id} and mch_id = #{mch_id} order by id")
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

    /**
     *  运费关联的商品
     * @param storeId  商城id
     * @param id 运费id
     * @return
     */
    @Select("select ifnull(count(a.id),0) as total from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = #{storeId} and a.recycle = 0 and a.freight = #{id} ")
    int getFreightRelationGoodCount(int storeId,int id) throws LaiKeAPIException ;


    @Select("select a.id,a.imgurl,a.product_title,a.initial,m.name as mch_name from lkt_product_list as a left join lkt_mch as m on a.mch_id = m.id where a.store_id = #{storeId}  and a.recycle = 0 and a.freight = #{id} order by (a.volume + a.real_volume) desc limit #{pageNo},#{pageSize} ")
    List<Map<String, Object>> getFreightRelationGoods(int storeId,int id,int pageNo,int pageSize) throws LaiKeAPIException ;

}