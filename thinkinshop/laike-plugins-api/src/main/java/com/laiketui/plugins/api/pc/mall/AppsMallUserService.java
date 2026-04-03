package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.UserLoginVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.user.EditResourcesVo;
import com.laiketui.domain.vo.user.ForgetPasswordVo;
import com.laiketui.domain.vo.user.PcRegisterVo;

import java.util.List;
import java.util.Map;

/**
 * pc店铺用户接口
 *
 * @author Trick
 * @date 2021/6/11 15:28
 */
public interface AppsMallUserService
{


    /**
     * 获取验证码
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/6/11 15:30
     */
    Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取协议
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 15:59
     */
    Map<String, Object> agreement(MainVo vo, int type) throws LaiKeAPIException;

    /**
     * 获取注册协议内容
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/3/1 10:13
     */
    Map<String, Object> agreementDetail(MainVo vo, int type) throws LaiKeAPIException;

    /**
     * 商城注册
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 16:05
     */
    Map<String, Object> register(PcRegisterVo vo) throws LaiKeAPIException;

    /**
     * 密码登陆
     *
     * @param user    -
     * @param imgCode -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/24 18:41
     */
    Map<String, Object> login(UserLoginVo user) throws LaiKeAPIException;

    /**
     * 自动登录
     *
     * @param vo        -
     * @param autoToken -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/5/26 17:33
     */
    Map<String, Object> autoLogin(MainVo vo, String autoToken) throws LaiKeAPIException;

    /**
     * 登录页数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 16:52
     */
    Map<String, Object> index(MainVo vo) throws LaiKeAPIException;

    /**
     * 注册页数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 16:52
     */
    Map<String, Object> registerPage(MainVo vo) throws LaiKeAPIException;


    /**
     * 短信登陆
     *
     * @param vo       -
     * @param phone    -
     * @param keyCode    - 验证码
     * @param imgCode  - 图形验证码
     * @param pid      - 推荐人
     * @param clientid - 推送客户端id
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/24 18:41
     */
    Map<String, Object> loginSms(MainVo vo, String phone, String keyCode, String imgCode, String pid, String clientid, String verificationCode, String cpc,Integer type,String e_mail) throws LaiKeAPIException;


    /**
     * 找回密码
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 9:24
     */
    void forgetPassword(ForgetPasswordVo vo) throws LaiKeAPIException;

    /**
     * 退出登录
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/17 10:07
     */
    void outLogin(MainVo vo) throws LaiKeAPIException;

    /**
     * 我的信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/23 15:55
     */
    Map<String, Object> myInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 关于我们
     *
     * @param vo   -
     * @param type -  AboutUsType
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 18:11
     */
    Map<String, Object> aboutUs(MainVo vo, String type) throws LaiKeAPIException;

    /**
     * 用户是否已经去完善过资料，修改是否使用的默认用户配置   1是 2否  默认否状态
     *
     * @param vo
     */
    Map<String, Object> isDefaultValue(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取区号
     * @param keyword
     * @return
     */
    List<Map<String, Object>> getItuList(String keyword);

    /**
     * 发送邮箱
     * @param email
     * @param vo
     * @throws LaiKeAPIException
     */
    void sendEmail(String email, MainVo vo) throws  LaiKeAPIException;

    /**
     * 关于说明
     */
    interface AboutUsType
    {
        /**
         * 关于我们
         */
        String ABOUTUS             = "aboutus";
        /**
         * 退款说明
         */
        String REFUND_INSTRUCTIONS = "refund_instructions";
        /**
         * 退款流程
         */
        String REFUND_PROCESS      = "refund_process";
        /**
         * 取消订单
         */
        String CANCELLATION_ORDER  = "cancellation_order";
        /**
         * 退货政策
         */
        String RETURN_POLICY       = "return_policy";
        /**
         * 支付方式
         */
        String PAYMENT_METHOD      = "payment_method";
        /**
         * 购物流程
         */
        String SHOPPING_PROCESS    = "shopping_process";
        /**
         * 支付问题
         */
        String PAYMENT_ISSUES      = "payment_issues";
        /**
         * 售后问题
         */
        String AFTER_SALES_ISSUES  = "after_sales_issues";

    }

    /**
     * 获取我的钱包
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-23 16:53:33
     */
    Map<String, Object> getUserWallet(MainVo vo, Integer type, Integer typeName, Integer day) throws LaiKeAPIException;


    /**
     * 我的钱包明细
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 9:33
     */
    Map<String, Object> getUserWalletDetail(MainVo vo, Integer type, Integer typeName, Integer day) throws LaiKeAPIException;


    /**
     * 我的提现页面数据
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 9:58
     */
    Map<String, Object> intoWallet(MainVo vo) throws LaiKeAPIException;


    /**
     * 发送短信
     *
     * @param vo      -
     * @param mobile  - 手机号
     * @param smsType - 短信类型
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 16:37
     */
    void sendSms(MainVo vo, String mobile, int smsType,String cpc) throws LaiKeAPIException;

    /**
     * 申请提现
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 12:09
     */
    void withdrawals(Withdrawals1Vo vo) throws LaiKeAPIException;

    /**
     * 个人资料
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/24 10:08
     */
    Map<String, Object> myResources(MainVo vo) throws LaiKeAPIException;


    /**
     * 编辑个人资料
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/24 10:08
     */
    void editResources(EditResourcesVo vo) throws LaiKeAPIException;

    /**
     * 账号安全
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/24 11:31
     */
    Map<String, Object> accountSecurity(MainVo vo) throws LaiKeAPIException;


    /**
     * 修改密码
     *
     * @param vo          -
     * @param imgToken    -
     * @param password    -
     * @param passwordOld -
     * @param keyCode     -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 17:13
     */
    void updatePassword(MainVo vo, String imgToken, String password, String passwordOld, String keyCode) throws LaiKeAPIException;


    /**
     * 修改支付密码
     *
     * @param vo       -
     * @param imgToken -
     * @param password -
     * @param pcode    -
     * @param keyCode  -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 17:13
     */
    void updatePayPassword(MainVo vo, String imgToken, String password, String pcode, String keyCode) throws LaiKeAPIException;


    /**
     * 修改手机号-验证身份
     *
     * @param vo       -
     * @param imgToken -
     * @param keyCode  -
     * @param pCode    -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/24 14:30
     */
    Map<String, Object> authentication(MainVo vo, String imgToken, String keyCode, String pCode) throws LaiKeAPIException;

    /**
     * 修改手机号
     *
     * @param vo                  -
     * @param authenticationToken - 身份token
     * @param newPhone            -
     * @param keyCode             -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/24 14:30
     */
    void updatePhone(MainVo vo, String authenticationToken, String newPhone, String imgToken, String keyCode, String pCode,String cpc,Integer country_num) throws LaiKeAPIException;

    /**
     * 获取支付方式
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/2/24 10:34
     */
    Map<String, Object> paymentTypeList(MainVo vo) throws LaiKeAPIException;

    /**
     * 设置密码
     *
     * @param vo       -
     * @param password -
     * @param phone    -
     * @param keyCode  -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 17:06
     */
    boolean setPassword(MainVo vo, String password, String phone, String keyCode) throws LaiKeAPIException;

    /**
     * 获取平台用户公告
     *
     * @param vo
     * @return
     */
    Map<String, Object> getUserTell(MainVo vo);

    /**
     * 标记公告以读
     *
     * @param vo
     * @param tell_id 公告id
     */
    void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException;
}
