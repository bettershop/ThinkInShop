package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

/**
 * 排序\移动 接口
 *
 * @author Trick
 * @date 2021/10/14 11:36
 */
public interface PublicSortService
{

//    @Deprecated
//    default void sortMove(String moveId, String moveId1) throws LaiKeAPIException {
//    }

    /**
     * 上下移动
     *
     * @param vo      -
     * @param moveId  -
     * @param moveId1 -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:37
     */
    default void sortMove(MainVo vo, String moveId, String moveId1) throws LaiKeAPIException
    {
    }

//    @Deprecated
//    default void top(String id) throws LaiKeAPIException {
//    }

    /**
     * 置顶
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:39
     */
    default void top(MainVo vo, String id) throws LaiKeAPIException
    {
    }

    /**
     * 重新排序
     *
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:39
     */
    default void reSort() throws LaiKeAPIException
    {
    }
}
