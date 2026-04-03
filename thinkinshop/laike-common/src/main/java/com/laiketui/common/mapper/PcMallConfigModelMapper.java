package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.config.PcMallConfigModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PcMallConfigModelMapper extends BaseMapper<PcMallConfigModel>
{
    /**
     * 查询配置信息
     *
     * @param storeId
     * @param type
     * @return
     */
    @Select("select * from  lkt_pc_mall_config where store_id = #{storeId} and type = #{type}")
    PcMallConfigModel getConfigByStoreIdAndType(int storeId, int type);

    /**
     * 查询配置信息
     *
     * @param storeId
     * @return
     */
    @Select("select * from  lkt_pc_mall_config where store_id = #{storeId}")
    List<PcMallConfigModel> getConfigByStoreId(int storeId);
}
