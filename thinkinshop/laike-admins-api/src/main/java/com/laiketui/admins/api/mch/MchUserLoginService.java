package com.laiketui.admins.api.mch;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.user.ForgetPasswordVo;
import com.laiketui.domain.vo.user.LoginVo;

import java.util.Map;

/**
 * 登录接口
 *
 * @author Trick
 * @date 2021/5/26 9:40
 */
public interface MchUserLoginService
{


    /**
     * 登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 9:41
     */
    Map<String, Object> login(LoginVo vo) throws LaiKeAPIException;

    /**
     * 退出登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 9:50
     */
    Map<String, Object> loginOut(MainVo vo) throws LaiKeAPIException;

    /**
     * 发送短信
     *
     * @param mobile  - 手机号
     * @param storeId - 商城id
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 15:41
     */
    boolean sendSms(String mobile, int storeId) throws LaiKeAPIException;

    /**
     * 忘记密码发送短信
     *
     * @param vo     -
     * @param mobile - 手机号
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 15:41
     */
    boolean sendSmsForgetPwd(MainVo vo, String mobile) throws LaiKeAPIException;

    /**
     * 手机验证码登录
     *
     * @param vo    -
     * @param phone -
     * @param pcode -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 9:46
     */
    Map<String, Object> loginBySms(MainVo vo, String phone, String pcode, String imgCode, String imgCodeToken) throws LaiKeAPIException;

    /**
     * 获取图形验证码
     *
     * @param vo      -
     * @param httpUrl -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/2 16:59
     */
    Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;

    /**
     * 修改密码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/26 15:56
     */
    Map<String, Object> forgetPwd(ForgetPasswordVo vo) throws LaiKeAPIException;

    /**
     * 商城查看店铺端所需token
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author sunH
     * @date 2023/06/26 20:00
     */
    Map<String, Object> storeLookMch(MainVo vo, Integer mchId) throws LaiKeAPIException;

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
}
