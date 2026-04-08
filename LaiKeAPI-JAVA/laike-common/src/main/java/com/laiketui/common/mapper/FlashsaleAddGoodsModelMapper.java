package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.flashsale.FlashsaleAddGoodsModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface FlashsaleAddGoodsModelMapper extends BaseMapper<FlashsaleAddGoodsModel>
{
    /**
     * 获取商品信息
     * LEFT JOIN lkt_configure
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 10:27
     */
    List<Map<String, Object>> getProductListJoinConfigureDynamic(Map<String, Object> map) throws LaiKeAPIException;


    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 逻辑删除
     *
     * @param storeId
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_flashsale_addgoods set is_delete = 1 where store_id = #{storeId} and mch_id = #{mchId} and is_delete = 0")
    int deleteByMchId(Integer storeId, Integer mchId) throws LaiKeAPIException;
}
