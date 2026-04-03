package com.laiketui.common.mapper;


import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.flashsale.FlashsaleConfigModel;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface FlashsaleConfigModelMapper extends BaseMapper<FlashsaleConfigModel>
{
    /**
     * 获取平台配置
     *
     * @param storeId
     * @return
     */
    @Select("select * from lkt_flashsale_config where store_id = #{storeId} and mch_id = 0")
    Map<String, Object> getConfig(Integer storeId);
}
