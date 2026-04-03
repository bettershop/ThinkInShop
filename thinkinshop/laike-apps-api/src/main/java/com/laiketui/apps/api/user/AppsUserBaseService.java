package com.laiketui.apps.api.user;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.UserLoginVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.domain.vo.user.SycnUserVo;
import com.laiketui.domain.vo.user.UserRegisterVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户登陆注册接口
 *
 * @author Trick
 * @date 2020/9/23 9:31
 */
public interface AppsUserBaseService
{

    /**
     * 获取用户信息
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException
     * @author Trick
     * @date 2020/9/23 9:50
     */
    User getUser(User user) throws LaiKeAPIException;


    /**
     * 新增用户
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:48
     */
    Map<String, Object> insertUser(UserRegisterVo vo) throws LaiKeAPIException;


    /**
     * 验证手机号/账号是否已被注册
     *
     * @param stroeId  -
     * @param zhanghao -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 9:49
     */
    boolean isRegister(int stroeId, String zhanghao) throws LaiKeAPIException;


    /**
     * 发送短信
     *
     * @param mobile  - 手机号
     * @param smsType - 短信类型
     * @param storeId - 商城id
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/23 16:37
     */
    boolean sendSms(String mobile, int smsType, int storeId,String cpc) throws LaiKeAPIException;


    /**
     * 密码登陆
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/24 18:41
     */
    Map<String, Object> login(UserLoginVo user) throws LaiKeAPIException;

    /**
     * 短信登陆
     *
     * @param vo       -
     * @param phone    -
     * @param pcode    - 验证码
     * @param pid      - 推荐人
     * @param clientid - 推送客户端id
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/24 18:41
     */
    Map<String, Object> loginSms(MainVo vo, String phone, String pcode, String pid, String clientid,String cpc ,Integer country_num,Integer type) throws LaiKeAPIException;


    /**
     * 修改用户资料
     *
     * @param vo       -
     * @param sex      -
     * @param birthday -
     * @param nickname -
     * @param file     - 文件流
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/25 16:25
     */
    boolean updateUser(MainVo vo, Integer sex, String birthday, String nickname, MultipartFile file,String e_mail,String keyCode) throws LaiKeAPIException;


    /**
     * 图片上传
     *
     * @param storeId    -
     * @param storeType  -
     * @param uploadType - 上传方式
     * @param files      -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/28 18:25
     */
    List<String> uploadImage(int storeId, int storeType, String uploadType, MultipartFile[] files) throws LaiKeAPIException;


    /**
     * 修改密码
     *
     * @param vo       -
     * @param password -
     * @param newPwd   -
     * @param confirm  -
     * @return
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 17:13
     * @author sunH(优化)
     * @date 2022/11/01 10:00
     */
    boolean updatePassword(MainVo vo, String password, String newPwd, String confirm) throws LaiKeAPIException;


    /**
     * 设置支付密码
     *
     * @param vo       -
     * @param password -
     * @param keyCode  -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/30 18:25
     */
    void setPaymentPassword(MainVo vo, String password, String keyCode) throws LaiKeAPIException;


    /**
     * 修改支付密码
     *
     * @param vo       -
     * @param password -
     * @param phone    -
     * @param keyCode  -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 8:58
     */
    boolean updatePayPassword(MainVo vo, String password, String phone, String keyCode) throws LaiKeAPIException;

    /**
     * 获取设置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 14:14
     */
    Map<String, Object> set(MainVo vo) throws LaiKeAPIException;


    /**
     * 验证支付密码
     *
     * @param vo       -
     * @param password -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 14:45
     */
    boolean paymentPassword(MainVo vo, String password) throws LaiKeAPIException;


    /**
     * 获取我的钱包
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 15:49
     */
    Map<String, Object> getUserWallet(MainVo vo) throws LaiKeAPIException;


    /**
     * 我的钱包明细加载更多
     *
     * @param vo   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 9:33
     */
    Map<String, Object> getUserWalletDetail(MainVo vo, Integer type) throws LaiKeAPIException;


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
     * 修改手机号
     *
     * @param vo       -
     * @param phoneNew - 新密码
     * @param keyCode  -
     * @return LaiKeAPIException
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 17:27
     */
    boolean updatePhone(MainVo vo, String phoneNew, String keyCode,String cpc,Integer country_num) throws LaiKeAPIException;


    /**
     * 关于
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 18:11
     */
    Map<String, Object> aboutUs(MainVo vo) throws LaiKeAPIException;


    /**
     * 店主进入提现页面
     *
     * @param vo         -
     * @param shopId     -
     * @param pluginType 插件提现
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 9:58
     */
    Map<String, Object> intoWallet1(MainVo vo, Integer shopId, String pluginType) throws LaiKeAPIException;


    /**
     * 申请提现
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 12:09
     */
    Map<String, Object> withdrawals1(Withdrawals1Vo vo) throws LaiKeAPIException;


    /**
     * 验证银行卡与银行名称是否匹配
     *
     * @param vo             -
     * @param bankName       -
     * @param bankCardNumber -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/3 18:21
     */
    Map<String, Object> verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException;


    /**
     * 获取我的银行卡列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author List
     * @date 2021/5/19 17:57
     */
    List<Map<String, Object>> bankList(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取银行卡详情
     *
     * @param vo     -
     * @param bankId -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/10 14:53
     */
    Map<String, Object> getBankDetail(MainVo vo, int bankId) throws LaiKeAPIException;

    /**
     * 添加银行卡
     * 【php MchBank.add_bank】
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/5 15:42
     */
    boolean addBank(AddBankVo vo) throws LaiKeAPIException;

    /**
     * 设置默认银行卡
     *
     * @param vo     -
     * @param bankId -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2023/4/10 11:11
     */
    void setDefaultBank(MainVo vo, int bankId) throws LaiKeAPIException;

    /**
     * 解绑银行卡
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/8/27 19:21
     */
    void delBank(MainVo vo, int bankId) throws LaiKeAPIException;

    /**
     * 请求我的数据
     *
     * @param vo     -
     * @param mobile -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 9:13
     */
    Map<String, Object> index(MainVo vo, String mobile) throws LaiKeAPIException;

    /**
     * 小程序用户绑定/合并
     *
     * @param vo
     * @param phone
     * @throws LaiKeAPIException
     */
    void bindPhone(MainVo vo, String phone) throws LaiKeAPIException;

    /**
     * 微信小程序用户合并
     *
     * @param vo
     * @param mobile
     * @throws LaiKeAPIException
     */
    void synchronizeAccount(MainVo vo, String mobile, Integer source) throws LaiKeAPIException;


    /**
     * 第三方用户信息同步
     *
     * @param sycnUserVo
     * @return
     * @throws LaiKeAPIException
     */
    Map<String, Object> synuser(SycnUserVo sycnUserVo) throws LaiKeAPIException;

    /**
     * 我的钱包记录详情
     *
     * @param vo
     * @param id
     * @return gp
     */
    Map<String, Object> getRecordDetails(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 用户解除与微信的绑定
     *
     * @param vo
     */
    void wxUnbind(MainVo vo) throws LaiKeAPIException;

    /**
     * 用户绑定微信
     *
     * @param vo
     * @param openId
     * @param wxName
     */
    void wxBind(MainVo vo, String openId, String wxName) throws LaiKeAPIException;

    Map<String, Object> getMyRecommendation(MainVo vo);


    /**
     * 用户是否已经去完善过资料，修改是否使用的默认用户配置   1是 2否  默认否状态
     *
     * @param vo
     */
    void isDefaultValue(MainVo vo) throws LaiKeAPIException;

    /**
     * 获取区号
     * @param keyword
     * @return
     */
    List<Map<String, Object>> getItuList(String keyword);

    /**
     * 获取用户当前选择的货币信息
     *
     * @param vo
     * @return
     * @throws LaiKeAPIException
     */
    Map getUserCurrnetCurrencyInfo(MainVo vo) throws LaiKeAPIException;


    /**
     * 发送邮箱验证码
     * @param email
     * @param vo
     */
    void sendEmail(String email, MainVo vo) throws LaiKeAPIException;

    /**
     * 获取系统html icon 商城名称
     * @param vo
     * @return
     */
    Map<String,Object> getSystemIconAndName(MainVo vo) throws LaiKeAPIException;
}
