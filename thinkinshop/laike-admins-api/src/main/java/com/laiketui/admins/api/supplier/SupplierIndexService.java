package com.laiketui.admins.api.supplier;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.supplier.SupplierPrintSetupVo;
import com.laiketui.domain.vo.supplier.UpdateSupplierVo;
import com.laiketui.domain.vo.user.AdminLoginVo;
import com.laiketui.domain.vo.user.SupplierColorVo;

import java.util.Map;

/**
 * @Author: sunH_
 * @Date: Create in 10:33 2022/9/21
 */
public interface SupplierIndexService
{

    /**
     * 后台登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> login(AdminLoginVo vo) throws LaiKeAPIException;

    /**
     * 供应商信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> getSupplierInfo(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取验证码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-09-21
     */
    Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;

    /**
     * 自动登录
     *
     * @param vo        -
     * @param autoToken -
     * @return Map
     * @throws LaiKeAPIException-
     */
    Map<String, Object> autoLogin(MainVo vo, String autoToken) throws LaiKeAPIException;

    /**
     * 供应商首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 退出登录
     *
     * @param vo -
     * @throws LaiKeAPIException-
     */
    void logout(MainVo vo) throws LaiKeAPIException;

    /**
     * 修改密码
     *
     * @param vo
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * @param pwd    确认密码
     * @throws LaiKeAPIException
     */
    void updatePwd(MainVo vo, String oldPwd, String newPwd, String pwd) throws LaiKeAPIException;

    /**
     * 修改基本信息
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void updateInfo(UpdateSupplierVo vo) throws LaiKeAPIException;

    /**
     * 获取商城列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     */
    Map<String, Object> storeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 供应商消息通知
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
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
     * 获取订单打印配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023/11/21 10:20
     */
    Map<String, Object> getSupplierPrintSetup(MainVo vo) throws LaiKeAPIException;

    /**
     * 上传订单打印配置
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author gp
     * @date 2023/11/21 10:20
     */
    void setSupplierPrintSetup(SupplierPrintSetupVo vo) throws LaiKeAPIException;


    /**
     * 标记公告以读
     *
     * @param vo
     * @param tell_id 公告id
     */
    void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException;

    /**
     * 获取平台维护公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo);

    void updateSupplierColor(SupplierColorVo vo);
}
