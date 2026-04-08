package com.laiketui.plugins.api.living.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.living.QueryLivingDataVo;

import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/6/18
 */
public interface MchLivingDataService
{
    /**
     * 直播数据
     *
     * @param vo
     * @return
     */
    Map<String, Object> queryLivingData(QueryLivingDataVo vo);

    /**
     * 直播数据详情
     *
     * @param vo
     * @return
     */
    Map<String, Object> queryLivingDataDetails(QueryLivingDataVo vo);

    /**
     * 移动店铺端-进入管理页面数据查询
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> manage(MainVo vo) throws LaiKeAPIException;
}
