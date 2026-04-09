package com.laiketui.common.api;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 关于用户公共接口
 *
 * @author Trick
 * @date 2020/12/23 10:23
 */
public interface PublicUserService
{


    /**
     * 会员等级金额计算
     * 【php: rechargeService.orderTotal】
     *
     * @param storeId -
     * @param id      - 会员等级id
     * @param userId  -用户id
     * @param flag    - 开通-续费-升级 标识
     * @param method  - 开通时长
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 10:13
     */
    BigDecimal orderTotal(int storeId, int id, String userId, int flag, int method) throws LaiKeAPIException;


    /**
     * 余额支付
     *
     * @param accessId -
     * @param payPrice -
     * @param text     - 操作信息
     * @param type     - 操作类型 :RecordModel.BUYING_MEMBERS...
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 14:38
     */
    boolean balancePay(String accessId, BigDecimal payPrice, String text, int type) throws LaiKeAPIException;

    /**
     * 验证支付密码
     *
     * @param userId -
     * @param pwd    -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/28 16:27
     */
    void validatePayPwd(String userId, String pwd) throws LaiKeAPIException;


    /**
     * 给用户充值
     *
     * @param storeId -
     * @param id      - 用户主键id
     * @param money   - 充值/扣减
     * @param type    - 1=余额 2=消费金额 3=积分 4 = 代客下单扣款
     * @param sNo     - 代客下单订单号
     * @param remake  备注
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 17:09
     */
    boolean userRechargeMoney(int storeId, int id, BigDecimal money, int type, String sNo, String remake) throws LaiKeAPIException;

    /**
     * 获取店铺/用户提现数据
     *
     * @param vo     -
     * @param shopId - 传了店铺就是店铺提现 否则是会员提现
     * @param user   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/18 15:17
     */
    Map<String, Object> getIntoWallet(MainVo vo, Integer shopId, User user, String pluginType) throws LaiKeAPIException;

    /**
     * 注册用户公共方法
     *
     * @param vo   -
     * @param pid  -
     * @param user -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/30 17:20
     */
    void register(MainVo vo, String pid, User user) throws LaiKeAPIException;

    /**
     * 添加一条操作记录
     *
     * @param storeId -
     * @param userId  -
     * @param text    -
     * @param type    -
     * @return boolean -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/25 14:46
     */
    boolean saveUserRecord(int storeId, String userId, String text, int type) throws LaiKeAPIException;

    /**
     * 登录后统一操作
     *
     * @param loginUser -
     * @param token     - 登陆前的token,登陆前登陆后购物车合并
     * @param fatherUid -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/24 20:52
     */
    void loginSuccess(User loginUser, String token, String fatherUid) throws LaiKeAPIException;

    /**
     * 登录后统一操作
     *
     * @param loginUser -
     * @param token     -
     * @param fatherUid -
     * @param flagKey   - 模块对应token映射
     * @param loginKey  - 模块对应token key
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/2/15 17:34
     */
    void loginSuccess(User loginUser, String token, String fatherUid, String flagKey, String loginKey) throws LaiKeAPIException;

    /**
     * 登录后购物车处理
     *
     * @param token     -
     * @param storeId   -
     * @param storeType -
     * @param userId    -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/10 16:34
     */
    void loginCart(String token, int storeId, int storeType, String userId) throws LaiKeAPIException;

    /**
     * 获取移动端登录有效时长
     *
     * @param storeId -
     * @return Integer
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/6/10 10:58
     */
    Integer getUserLoginLife(int storeId) throws LaiKeAPIException;

    /**
     * 缓存所有平台新增用户信息
     *
     * @return
     */
    Map<String, Object> getAdditionUserData(Integer storeId) throws LaiKeAPIException;
}
