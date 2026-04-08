package com.laiketui.admins.api.admin.order;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.ConfigVo;

import java.util.Map;

/**
 * 订单设置
 *
 * @author wangxian
 */
public interface AdminConfigService
{

    /**
     * 获取订单设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author wangxian
     * @date 2021/7/26 16:41
     * @see Trick
     */
    Map<String, Object> configShow(MainVo vo) throws LaiKeAPIException;

    /**
     * 保存订单设置
     *
     * @param configVo -
     * @throws LaiKeAPIException-
     * @author wangxian
     * @date 2021/7/26 18:31
     * @see Trick
     */
    void saveConfig(ConfigVo configVo) throws LaiKeAPIException;

    /**
     * 获取店铺订单设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author wangxian
     * @date 2021/7/26 16:41
     * @see Trick
     */
    Map<String, Object> mchConfigShow(MainVo vo, int mchId, int isType) throws LaiKeAPIException;

    /**
     * 保存店铺订单设置
     *
     * @param configVo -
     * @throws LaiKeAPIException-
     * @author wangxian
     * @date 2021/7/26 18:31
     * @see Trick
     */
    void mchSaveConfig(ConfigVo configVo, int mchId, int isType) throws LaiKeAPIException;

}
