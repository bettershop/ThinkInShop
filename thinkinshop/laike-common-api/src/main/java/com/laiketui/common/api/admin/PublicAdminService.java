package com.laiketui.common.api.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.AddShopVo;

import java.util.Map;

/**
 * 公共后台方法
 *
 * @author Trick
 * @date 2021/8/13 17:23
 */
public interface PublicAdminService
{

    /**
     * 首页统计报表
     *
     * @param vo    -
     * @param mchId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/28 10:11
     */
    Map<String, Object> index(MainVo vo, Integer mchId) throws LaiKeAPIException;

    /**
     * 添加/编辑导航栏跳转地址
     *
     * @param vo       -
     * @param type     -跳转类型 JumpPathModel.JumpType
     * @param sid      - 上级id
     * @param jumpPath - 跳转路径
     * @param params   - 参数
     * @param mch_id   - 店铺id
     * @param lang_code 语种
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/15 10:21
     */
    void addJumpPath(MainVo vo, String sid, String name, int type, int sourceType, GloabConst.LaikeTuiUrl.JumpPath jumpPath, String[] params, Integer mch_id, String lang_code) throws LaiKeAPIException;

    /**
     * 删除导航栏跳转地址
     *
     * @param storeId    -
     * @param sid        -
     * @param name       -
     * @param type       -
     * @param sourceType -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/12/5 15:22
     */
    void delJumpPath(Integer storeId, String sid, String name, int type, int sourceType) throws LaiKeAPIException;

    /**
     * 系统推送消息
     *
     * @param vo         -
     * @param type       - 消息类型
     * @param tuiTitle   - 推送标题
     * @param tuiContext - 推送内容
     * @param tuiUserId  - 推送人
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/22 17:33
     */
    void systemMessageSend(MainVo vo, int type, String tuiTitle, String tuiContext, String tuiUserId) throws LaiKeAPIException;

    /**
     * 把删除管理员全部踢下线
     *
     * @param storeId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 18:28
     */
    void outLoginStoreAdminAll(int storeId) throws LaiKeAPIException;

    /**
     * 踢指定管理员下线
     *
     * @param storeId -
     * @param adminId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/7/12 19:12
     */
    void outLoginAdminById(int storeId, int adminId) throws LaiKeAPIException;

    /**
     * 初始化商城必要的数据
     *
     * @param storeId -
     * @param adminId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/11/14 10:47
     */
    void initialStoreData(int storeId, int adminId, AddShopVo vo) throws LaiKeAPIException;
}
