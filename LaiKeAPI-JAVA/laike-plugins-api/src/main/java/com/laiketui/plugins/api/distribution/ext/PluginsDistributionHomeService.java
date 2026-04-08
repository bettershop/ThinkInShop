package com.laiketui.plugins.api.distribution.ext;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 分销 ：新的移动端接口商品相关 首页
 *
 * @date 2024/4/2
 */
public interface PluginsDistributionHomeService
{

    /**
     * 首页界面 非商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> homeIndex(MainVo vo, String fatherId) throws LaiKeAPIException;

    /**
     * 获取为你推荐商品列表
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> productListIndex(MainVo vo) throws LaiKeAPIException;

    /**
     * 首页用户获取佣金消息通知
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    public Map<String, Object> getUserTell(MainVo vo, Integer tellTime) throws LaiKeAPIException;
}
