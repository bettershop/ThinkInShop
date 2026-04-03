package com.laiketui.apps.api.user;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAuthorizeUserInfoVo;
import com.laiketui.domain.vo.user.AlipayUserVo;
import com.laiketui.domain.vo.user.AppletsVo;
import com.laiketui.domain.vo.user.WxAuthPhoneVo;

import java.util.Map;

/**
 * 登陆
 *
 * @author Trick
 * @date 2020/10/19 10:12
 */
public interface AppsUserLoginService
{


    /**
     * 获取token
     *
     * @param vo -
     * @return Map -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 10:21
     */
    Map<String, Object> token(MainVo vo) throws LaiKeAPIException;


    /**
     * 判断是否需要注册
     *
     * @param vo-
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 14:24
     */
    Map<String, Object> isRegister(MainVo vo) throws LaiKeAPIException;


    /**
     * 更新授权信息
     *
     * @param vo     -
     * @param openid -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 14:51
     */
    Map<String, Object> loginAccess(MainVo vo, String openid) throws LaiKeAPIException;


    /**
     * 存储用户授权信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 15:28
     */
    @Deprecated
    Map<String, Object> saveAuthorizeUserInfo(SaveAuthorizeUserInfoVo vo) throws LaiKeAPIException;


    /**
     * 初始化登陆页面
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 9:03
     */
    Map<String, Object> loginIndex(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取旧密码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:27
     */
    Map<String, Object> getOldPassword(MainVo vo) throws LaiKeAPIException;


    /**
     * 忘记密码
     *
     * @param vo       -
     * @param zhanghao -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:37
     */
    Map<String, Object> forgetZhanghao(MainVo vo, String zhanghao,Integer type,String cpc,Integer country_num) throws LaiKeAPIException;


    /**
     * 校验验证码是否正确
     *
     * @param vo    -
     * @param phone -
     * @param pcode -
     * @param type  - 短信类型
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 11:28
     */
    boolean validatePcode(MainVo vo, String phone, String pcode, int type) throws LaiKeAPIException;


    /**
     * 重置密码
     *
     * @param vo       -
     * @param phone    -
     * @param pcode    -
     * @param password -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 13:49
     */
    boolean forgotpassword(MainVo vo, String phone, String pcode, String password) throws LaiKeAPIException;


    /**
     * 退出登陆
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 16:33
     */
    boolean quit(MainVo vo) throws LaiKeAPIException;


    /**
     * 修改用户推荐人
     * 【php login.chang_pid】
     *
     * @param vo       -
     * @param fatherId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/2 16:13
     */
    void changPid(MainVo vo, String fatherId) throws LaiKeAPIException;

    /**
     * 授权参数
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> appletsParam(AppletsVo vo) throws LaiKeAPIException;

    /**
     * 小程序授权登录
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     */
    Map<String, Object> applets(AppletsVo vo) throws LaiKeAPIException;

    /**
     * 微信公众号授权登录
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/13 18:01
     */
    Map<String, Object> appletsWx(AppletsVo vo) throws LaiKeAPIException;

    /**
     * 小程序授权获取用户手机号
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> getWxPhone(WxAuthPhoneVo vo) throws LaiKeAPIException;

    /**
     * 小程序验证Token是否过期
     *
     * @param vo
     * @param openid
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> tokenAccess(MainVo vo, String openid) throws LaiKeAPIException;

    /**
     * 新增、修改阿里用户
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliUser(AlipayUserVo vo) throws LaiKeAPIException;

    /**
     * 阿里用户登陆
     *
     * @param vo
     * @param alimp_auth_code
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> aliUserLogin(MainVo vo, String alimp_auth_code, boolean flag) throws LaiKeAPIException;

    /**
     * 阿里用户登陆APP(非小程序场景)
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/10 11:38
     */
    Map<String, Object> aliUserLoginByApp(MainVo vo) throws LaiKeAPIException;

    /**
     * 阿里用户登陆H5(非小程序场景)
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/10 11:38
     */
    Map<String, Object> aliUserLoginByWeb(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取微信appid
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/1/13 14:23
     */
    Map<String, Object> getWxAppId(MainVo vo) throws LaiKeAPIException;

    /**
     * 【微信公众号,APP】 用户绑定微信
     *
     * @param vo
     * @throws LaiKeAPIException
     */
    void bindWechat(AppletsVo vo) throws LaiKeAPIException;

    /**
     * 忘记密码并重置密码
     * @param vo
     * @param tel 手机号
     * @param keyCode 验证码
     * @param type 类型 0.手机号 1.邮箱
     * @param email 邮箱
     * @param password 密码
     * @param cpc 区号
     * @param countryNum 国家代码
     * @param currencyCode 模板
     */
    void validatePcodeAndUpdatePassword(MainVo vo, String tel, String keyCode, Integer type, String email, String password, String cpc, Integer countryNum, int currencyCode);

}
