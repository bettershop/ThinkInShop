package com.laiketui.comps.api.task;

import com.laiketui.core.exception.LaiKeAPIException;

/**
 * 优惠卷定时任务接口
 *
 * @author vvx
 * @date 2023/12/04 12:01
 */
public interface CompsTaskApiCheckService
{

    /**
     * 检测api节点是否可用
     *
     * @throws LaiKeAPIException
     */
    void checkApis() throws LaiKeAPIException;

}
