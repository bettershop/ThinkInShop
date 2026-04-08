package com.laiketui.apps.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

/**
 * h5店铺消息通知
 * gp
 * 2023-08-31
 */
public interface AppsMchNoticeService
{
    /**
     * 商城消息通知
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-12
     */
    Map<String, Object> noticeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 标记消息已读
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-12
     */
    Map<String, Object> noticeRead(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 店铺消息通知获取未读消息条数
     *
     * @param vo
     * @return
     * @author gp
     * @date 2023-07-12
     */
    Integer messageNum(MainVo vo) throws LaiKeAPIException;

    /**
     * 标记全部以读
     *
     * @param vo
     * @author gp
     * @date 2023-07-12
     */
    void allMessage(MainVo vo) throws LaiKeAPIException;

    /**
     * 删除消息通知
     *
     * @param vo
     * @param ids
     * @author gp
     * @date 2023-07-12
     */
    void delMessage(MainVo vo, String ids) throws LaiKeAPIException;
}
