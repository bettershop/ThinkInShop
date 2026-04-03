package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.SignConfigModel;
import org.apache.ibatis.annotations.Update;


/**
 * 签到配置 sql
 *
 * @author Trick
 * @date 2020/10/14 15:10
 */
public interface SignConfigModelMapper extends BaseMapper<SignConfigModel>
{


    /**
     * 根据店铺id修改插件状态
     *
     * @param signConfigModel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 15:11
     */
    @Update("update lkt_sign_config set is_status = #{is_status} where store_id = #{store_id}")
    int updateStatusByStoreId(SignConfigModel signConfigModel) throws LaiKeAPIException;

}