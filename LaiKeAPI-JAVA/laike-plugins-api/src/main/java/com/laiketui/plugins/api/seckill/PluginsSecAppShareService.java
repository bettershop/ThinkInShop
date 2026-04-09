package com.laiketui.plugins.api.seckill;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

/**
 * 秒杀共用方法
 *
 * @author Trick
 * @date 2021/10/18 15:59
 */
public interface PluginsSecAppShareService
{

    /**
     * 秒杀活动有效日期处理
     * 【以前是根据商品来定义有效期.现在是标签有效期来决定秒杀是否结束】
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/10/18 15:55
     */
    void secondsTermDate(MainVo vo, String id) throws LaiKeAPIException;
}
