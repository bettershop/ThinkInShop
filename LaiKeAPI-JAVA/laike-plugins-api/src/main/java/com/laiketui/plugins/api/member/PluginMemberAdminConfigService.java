package com.laiketui.plugins.api.member;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.member.MemberConfigVo;

import java.util.Map;

/**
 * 管理平台会员设置管理
 *
 * @Author: sunH_
 * @Date: Create in 16:00 2022/6/29
 */
public interface PluginMemberAdminConfigService
{

    /**
     * 获取配置列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加商城设置
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void addOrUpdate(MemberConfigVo vo) throws LaiKeAPIException;


}
