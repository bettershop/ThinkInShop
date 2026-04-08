package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.product.ProLabelModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品标签
 *
 * @author Trick
 * @date 2021/6/25 18:12
 */
public interface ProLabelModelMapper extends BaseMapper<ProLabelModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 新品上市
     *
     * @param storeId
     * @return
     */
    @Select("select id from lkt_pro_label where store_id=#{storeId} and name ='新品' order by add_time desc limit 1")
    Integer getProLabelNew(int storeId);

    /**
     * 好物优选
     *
     * @param storeId
     * @return
     */
    @Select("select id from lkt_pro_label where store_id=#{storeId} and name ='推荐' order by add_time desc limit 1")
    Integer getProLabelTop(int storeId);

    /**
     * 热销
     *
     * @param storeId
     * @return
     */
    @Select("select id from lkt_pro_label where store_id=#{storeId} and name ='热销' order by add_time desc limit 1")
    Integer getProLabelHot(int storeId);

    /**
     * 获取核心标签
     *
     * @return
     */
    @Select("select id from lkt_pro_label where store_id=#{storeId} and name in('新品','推荐','热销') order by add_time desc limit 1")
    List<Integer> getProCoreLabel(int storeId);
}