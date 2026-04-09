package com.laiketui.plugins.api.distribution.ext;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 用户 我的-分销中心
 *
 * @date 2024/4/2
 */
public interface PluginsDistributionMyService
{

    /**
     * 我的界面首页
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> MyIndex(MainVo vo, String mobile) throws LaiKeAPIException;

    /**
     * 获取我的团队
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> myGroup(MainVo vo, String userId, String kye) throws LaiKeAPIException;

    /**
     * 收益报表
     *
     * @param vo
     * @param timeStatus 今日 today, 昨日 yesterday，本月 toMonth, 上月 yesMonth, 自定义 custom
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> myGroupIncomeStatement(MainVo vo, String timeStatus, String startTime, String endTime) throws LaiKeAPIException;
}
