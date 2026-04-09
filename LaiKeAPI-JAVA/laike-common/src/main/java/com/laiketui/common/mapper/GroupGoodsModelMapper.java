package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.group.GroupGoodsModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 拼团商品
 *
 * @author Trick
 * @date 2023/3/20 16:17
 */
public interface GroupGoodsModelMapper extends BaseMapper<GroupGoodsModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 后台 获取正在活动中的拼团商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 14:30
     */
    List<Map<String, Object>> getGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int delDynamic(Map<String, Object> map);


    /**
     * 获取拼团活动商品信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/3/30 10:28
     */
    List<Map<String, Object>> selectGroupGoodsDetailInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countGoodsDetailInfo(Map<String, Object> map) throws LaiKeAPIException;

//    int countGoodsDetailInfo(Map<String, Object> map) throws LaiKeAPIException;

    @Select("select goods_id from lkt_group_goods where activity_id = #{id}")
    List<Map<String, Object>> selectGoodsIdByActivityId(String id);

    @Select("SELECT * FROM lkt_group_goods WHERE goods_id = #{goodsId} and recycle = 0 ORDER BY id desc  LIMIT 1 ")
    GroupGoodsModel selectByGoodsId(Integer goodsId);
}