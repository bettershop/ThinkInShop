package com.laiketui.admins.api.admin.notie;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddNoticeVo;

import java.util.Map;

/**
 * 公告管理
 *
 * @author Trick
 * @date 2021/1/19 15:44
 */
public interface AdminPublicNoticeService
{

    /**
     * 获取公告列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/19 15:45
     */
    Map<String, Object> getPublicNoticeInfo(MainVo vo, Integer id, Integer isStore) throws LaiKeAPIException;


    /**
     * 添加/编辑系统公告
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 10:51
     */
    boolean addSysNoticeInfo(AddNoticeVo vo) throws LaiKeAPIException;


    /**
     * 删除系统公告
     *
     * @param storeId -
     * @param id      -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/1 11:23
     */
    boolean delSysNoticeInfo(String token, int storeId, Integer id) throws LaiKeAPIException;

    /**
     * 商城消息通知
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/7 16:27
     */
    Map<String, Object> noticeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 标记消息已读
     *
     * @param vo    -
     * @param id    -
     * @param types -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/7 20:13
     */
    void noticeRead(MainVo vo, Integer id, String types) throws LaiKeAPIException;

    /**
     * 标记已弹窗
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/7 9:41
     */
    void noticePopup(MainVo vo, String ids) throws LaiKeAPIException;

    /**
     * 标记系统公告-已读
     *
     * @param vo  -
     * @param ids -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/11/14 18:36
     */
    void readSysNotice(MainVo vo, String ids) throws LaiKeAPIException;
}
