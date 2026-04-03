package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.user.UserRuleModel;
import org.apache.ibatis.annotations.Select;

/**
 * 会员等级制配置
 *
 * @author Administrator
 */
public interface UserRuleModelMapper extends BaseMapper<UserRuleModel>
{

    //自定义查询,只能增量式加字段,不能加规则字段
    @Select("SELECT method FROM lkt_user_rule where store_id=#{storeId}")
    UserRuleModel getUserRule(int storeId);
}