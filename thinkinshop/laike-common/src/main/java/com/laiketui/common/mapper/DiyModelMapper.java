package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.plugin.DiyModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * diy
 *
 * @author Trick
 * @date 2021/7/2 18:36
 */
public interface DiyModelMapper extends BaseMapper<DiyModel>
{

    @Update("UPDATE lkt_diy SET status = 0 WHERE store_id = #{storeId} ")
    int removeDefault(int storeId);

    @Update("UPDATE lkt_diy SET status = 0 WHERE mch_id = #{mchId} ")
    int removeMchDefault(int mchId);

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<DiyModel> selectList(@Param("model") DiyModel diyModel);
}