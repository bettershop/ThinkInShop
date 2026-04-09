package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;

import java.util.Map;

public interface MchNoticeService
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
     * @param vo    -
     * @param id    -
     * @param types -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-12
     */
    void noticeRead(MainVo vo, Integer id, String types) throws LaiKeAPIException;

    /**
     * 标记已弹窗
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author gp
     * @date 2023-07-12
     */
    void noticePopup(MainVo vo, String ids) throws LaiKeAPIException;
}
