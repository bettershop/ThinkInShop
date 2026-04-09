package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.group.PtGroupProductModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Trick
 */
public interface PtGroupProductModelMapper extends BaseMapper<PtGroupProductModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取店铺自己的拼团商品id
     *
     * @param storeId -
     * @param mchId   -
     * @param formId  -
     * @return List
     * @author Trick
     * @date 2021/4/25 17:56
     */
    @Select("select attr_id from lkt_pt_group_product where store_id = #{storeId} and mch_id = #{mchId} and platform_activities_id = #{formId} " +
            " and audit_status in (1,2)")
    List<Integer> selectMchGoodsId(int storeId, int mchId, int formId);

}
