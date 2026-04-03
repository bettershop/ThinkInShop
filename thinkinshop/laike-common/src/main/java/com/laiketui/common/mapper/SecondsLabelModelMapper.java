package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.seckill.SecondsLabelModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 秒杀标签
 *
 * @author Trick
 * @date 2021/10/14 14:13
 */
public interface SecondsLabelModelMapper extends BaseMapper<SecondsLabelModel>
{
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取最大序号
     */
    @Select("SELECT COALESCE(MAX(sort), 0) + 1 FROM lkt_seconds_label WHERE store_id = #{storeId} AND recovery = 0;")
    int maxSort(int storeId);

    /**
     * 序号上下移动
     */
    @Update(" update lkt_seconds_label a,lkt_seconds_label b set a.sort=b.sort,b.sort=a.sort " +
            " where a.id=#{moveId} and b.id=#{moveId1}")
    int sortMove(String moveId, String moveId1);

    /**
     * 删除其它的秒杀商品
     */
    @Update("update lkt_seconds_pro set is_delete=1 where label_id=#{labelId} and pro_id not in (${goodsList}) and is_delete=0")
    int delGoods(String labelId, String goodsList);


    @Select("select pro_id from lkt_seconds_pro where id=#{labelId}")
    List<String> getSecondsGoodsIdList(String labelId);

    /**
     * 同步标签状态
     * 如果标签下所有商品都关闭则同步标签状态
     */
    @Update("update lkt_seconds_label a set a.is_show=0 where a.store_id=#{storeId} and (select count(1) from lkt_seconds_activity x where x.label_id=a.id and x.is_delete=0 and isshow=1) = 0 ")
    int updateLabelStatus(int storeId);

}