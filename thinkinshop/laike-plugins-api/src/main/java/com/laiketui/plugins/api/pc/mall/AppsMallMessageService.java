package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * 我的消息
 *
 * @author Trick
 * @date 2021/6/17 10:21
 */
public interface AppsMallMessageService
{

    /**
     * 我的消息列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/16 16:52
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 删除消息
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 10:56
     */
    void del(MainVo vo, String ids) throws LaiKeAPIException;


    /**
     * 消息详情
     *
     * @param vo -
     * @param id - 消息id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 16:50
     */
    Map<String, Object> oneIndex(MainVo vo, int id) throws LaiKeAPIException;
}
