package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.vo.MainVo;

import java.util.List;
import java.util.Map;

/**
 * 我的消息
 *
 * @author Trick
 * @date 2020/10/16 16:44
 */
public interface AppsCstrMessagesService
{


    /**
     * 消息列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 16:46
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;


    /**
     * 一键标记全读
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 16:49
     */
    boolean all(MainVo vo) throws LaiKeAPIException;


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
    Map<String, Object> oneindex(MainVo vo, int id) throws LaiKeAPIException;

    /**
     * 删除消息
     *
     * @param vo -
     * @param id - 消息id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/16 16:50
     */
    boolean del(MainVo vo, String id) throws LaiKeAPIException;


    /**
     * 获取微信模板
     *
     * @param storeId  -
     * @param language -
     * @param accessId -
     * @return NoticeModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/20 9:02
     */
    NoticeModel getWxTemplates(int storeId, String language, String accessId) throws LaiKeAPIException;


    /**
     * 加载后续消息
     *
     * @param vo     -
     * @param pageNo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/20 9:30
     */
    Map<String, Object> more(MainVo vo, int pageNo) throws LaiKeAPIException;

    /**
     * 小程序获取微信订阅消息模板id
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    List<String> getMessageIds(MainVo vo) throws LaiKeAPIException;

    /**
     * 是否客服有未读消息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> messageNotReade(MainVo vo) throws LaiKeAPIException;
}
