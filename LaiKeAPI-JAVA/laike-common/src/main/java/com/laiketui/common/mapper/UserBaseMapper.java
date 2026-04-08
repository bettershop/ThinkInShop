package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 用户dao层接口
 *
 * @author Trick
 * @date 2020/9/23 10:07
 */
public interface UserBaseMapper extends BaseMapper<User>
{


    /**
     * 重写
     *
     * @param user -
     * @return User
     * @author Trick
     * @date 2021/10/27 15:17
     */
    @Override
    User selectOne(User user);

    @Override
    @Select("select a.*,b.id mchId from lkt_user a left join lkt_mch b on a.user_id=b.user_id and b.recovery=0 where a.id=#{id}")
    User selectByPrimaryKey(Object id);

    @Select("select a.*,b.id mchId from lkt_user a left join lkt_mch b on a.user_id=b.user_id and b.recovery=0 where b.id=#{mchId}")
    User mchLoginByUser(Integer mchId);

    @Select("select a.*,b.id mchId from lkt_user a left join lkt_mch b on a.user_id=b.user_id and b.recovery=0 where a.user_id=#{userId} and a.store_id=#{storeId} ")
    User selectByUserId(int storeId, String userId);

    @Select("select a.* from lkt_user a  where a.user_id=#{userId} and a.store_id=#{storeId} ")
    User selectByUserIdOne(int storeId, String userId);

    /**
     * 修改用户资料
     *
     * @param user -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/28 17:30
     */
    int updateUserInfoById(User user) throws LaiKeAPIException;


    /**
     * 更新token
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/19 14:28
     */
    int updateUserAccessId(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 根据账号/手机号获取用户信息
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:45
     */
    @Select("select * from lkt_user where store_id = #{store_id} " +
            " and (zhanghao = #{zhanghao} or mobile = #{zhanghao}) " +
            " limit 1 ")
    User getUserByzhanghao(User user) throws LaiKeAPIException;


    /**
     * 根据账号/手机号获取用户信息
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:45
     */
    @Select("select * from lkt_user where store_id = #{store_id} " +
            " and (zhanghao = #{zhanghao} or e_mail = #{zhanghao}) " +
            " limit 1 ")
    User getUserByzhanghaoOrEmail(User user) throws LaiKeAPIException;

    /**
     * 根据账号/手机号获取用户信息
     * 移动端排除pc店铺管理员
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:45
     */
    @Select("select * from lkt_user where store_id = #{store_id} " +
            " and (zhanghao = #{zhanghao} or mobile = #{zhanghao}) " +
            " and user_id not in (select main_id from lkt_user_authority where type=1)" +
            " limit 1 ")
    User getUserByzhanghaoApp(User user) throws LaiKeAPIException;

    /**
     * 根据账号获取用户信息
     * 移动端排除pc店铺管理员
     *
     * @param user -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/6 10:45
     */
    /*@Select("select * from lkt_user where store_id = #{store_id} " +
            " and (e_mail = #{zhanghao} or zhanghao = #{zhanghao} or mobile = #{zhanghao}) " +
            " and user_id not in (select main_id from lkt_user_authority where type=1)" +
            " limit 1 ")*/
    User getUserByAccount(User user) throws LaiKeAPIException;



    /**
     * 验证账号是否被注册
     *
     * @param storeId  -
     * @param zhanghao -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/25 14:09
     */
    @Select("select count(1) from lkt_user where store_id = #{storeId}  and (zhanghao = #{zhanghao} or mobile = #{zhanghao}) ")
    int validataUserPhoneOrNoIsRegister(int storeId, String zhanghao) throws LaiKeAPIException;

    /**
     * 验证手机号是否被注册
     *
     * @param storeId -
     * @param phone   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/25 14:09
     */
    @Select("select count(1) from lkt_user where store_id = #{storeId}  and  zhanghao = #{phone} and id != #{id} ")
    int validataPhoneOrNoIsRegister(int storeId, String phone,Integer id) throws LaiKeAPIException;


    @Select("select count(1) from lkt_user where store_id = #{storeId} and  zhanghao = #{phone}")
    int checkPhoneIsRegister(int storeId, String phone) throws LaiKeAPIException;


    /**
     * 余额增减
     *
     * @param uid   -
     * @param price -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 17:19
     */
    @Update("update lkt_user set money = money + #{price} where id = #{uid}")
    int rechargeUserPrice(int uid, BigDecimal price) throws LaiKeAPIException;

    /**
     * 修改个人选择的币种
     *
     * @param uid
     * @param price
     * @return
     */
    @Update("update lkt_user set preferred_currency = #{currencyId} where id = #{uid}")
    int changePreferredCurrency(int uid, int currencyId);

    List<Map<String, Object>> getMember(Map<String, Object> map);

    Integer countMember(Map<String, Object> map);

    List<Map<String, Object>> getUser(Map<String, Object> map);

    Integer countUser(Map<String, Object> map);

    //根据userid获取用户主键
    @Select("select id from lkt_user where user_id=#{userId} and store_id=#{storeId}")
    Integer getUidByuserId(int storeId, String userId);


    /**
     * 积分增减
     *
     * @param id    -
     * @param price -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 17:19
     */
    @Update("update lkt_user set score = score + #{price} where id = #{id}")
    int rechargeUserByScore(int id, BigDecimal price) throws LaiKeAPIException;


    /**
     * 消费金额增减
     *
     * @param id    -
     * @param price -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/2 17:19
     */
    @Update("update lkt_user set consumer_money = consumer_money + #{price} where id = #{id}")
    int rechargeUserByConsumerMoney(int id, BigDecimal price) throws LaiKeAPIException;


    /**
     * 重置密码次数
     *
     * @param storeId -
     * @param time    -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/16 13:46
     */
    // Reset password-attempt count.
    // Narrow the update range to avoid full-store scan + massive locking.
    // We reset rows whose verification_time is before today (or null).
    @Update("update lkt_user set login_num = 0 where store_id = #{storeId} and login_num > 0 and (verification_time < CURDATE() or verification_time is null)")
    int resettingPwdNum(int storeId, String time) throws LaiKeAPIException;


    /**
     * 重置token
     *
     * @param storeId -
     * @param token   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/24 17:36
     */
    @Update("update lkt_user set access_id ='' where store_id = #{storeId} and access_id=#{token}")
    int resettingToken(int storeId, String token) throws LaiKeAPIException;


    /**
     * 重置token
     *
     * @param storeId -
     * @param token   -
     * @return int
     * @author Trick
     * @date 2021/5/26 10:16
     */
    @Update("update lkt_user set mch_token ='' where store_id = #{storeId} and mch_token=#{token}")
    int resettingMchToken(int storeId, String token);


    /**
     * 获取会员等级信息
     *
     * @param storeId -
     * @param userId  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/23 10:39
     */
    @Select("select  a.*,b.rate,b.money bymoney,b.money_j,b.money_n,b.id from lkt_user as a left join lkt_user_grade as b on a.grade = b.id " +
            "where a.store_id = #{storeId} and a.store_id = b.store_id and a.user_id = #{userId}")
    Map<String, Object> getUserGradeExpire(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 统计新注册用户-微信
     *
     * @param storId    -
     * @param startDate -
     * @param endDate   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 11:08
     */
    @Select("select COUNT(*) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = #{storId} " +
            "and source in (1,3,4,5) group by rdate " +
            "having rdate between #{startDate} " +
            "and #{endDate} " +
            "order by rdate desc")
    List<Map<String, Object>> getNewAddUserWxInfo(int storId, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 统计新注册用户 - app
     *
     * @param storId    -
     * @param startDate -
     * @param endDate   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 11:08
     */
    @Select("select COUNT(*) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = #{storId} " +
            "and source =2 group by rdate " +
            "having rdate between #{startDate} " +
            "and #{endDate} " +
            "order by rdate desc")
    List<Map<String, Object>> getNewAddUserAppInfo(int storId, String startDate, String endDate) throws LaiKeAPIException;


    /**
     * 统计新注册用户 - pc
     *
     * @param storId    -
     * @param startDate -
     * @param endDate   -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 11:08
     */
    @Select("select COUNT(*) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = #{storId} " +
            "and source = 6 group by rdate " +
            "having rdate between #{startDate} " +
            "and #{endDate} " +
            "order by rdate desc")
    List<Map<String, Object>> getNewAddUserPcInfo(int storId, String startDate, String endDate) throws LaiKeAPIException;

    @Select("select COUNT(*) as sum,DATE_FORMAT(Register_data,'%Y-%m-%d') as rdate from lkt_user where store_id = #{storId} " +
            "and source = 7 group by rdate " +
            "having rdate between #{startDate} " +
            "and #{endDate} " +
            "order by rdate desc")
    List<Map<String, Object>> getNewAddUserH5Info(int storId, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 会员消费报表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 16:37
     */
    List<Map<String, Object>> selectUserConsumptionReport(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 会员消费报表-统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/12 16:37
     */
    int countUserConsumptionReport(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户信息
     *
     * @param map -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:23
     */
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户信息 - 统计
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/7 11:23
     */
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取第一条用户信息
     *
     * @param storeId -
     * @return User
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 15:48
     */
    @Select("select * from lkt_user where store_id =#{storeId} order by Register_data asc limit 0,1")
    User getUserTop(int storeId) throws LaiKeAPIException;


    /**
     * 动态统计店铺营业额
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> sumMchTurnoverReport(Map<String, Object> map) throws LaiKeAPIException;

    int countMchTurnoverReport(Map<String, Object> map) throws LaiKeAPIException;

    @Select("<script>" +
            "SELECT DATE_FORMAT(register_data,'%Y-%m-%d') date,IFNULL(count(distinct id),0) num FROM `lkt_user` WHERE store_id = #{storeId} " +
            " AND DATE_FORMAT(Register_data,'%Y-%m-%d') <![CDATA[  >=  ]]> #{startDate} and DATE_FORMAT(Register_data,'%Y-%m-%d') <![CDATA[  <=  ]]> #{endDate} " +
            " group by DATE_FORMAT(Register_data,'%Y-%m-%d')" +
            " </script> ")
    List<Map<String, Object>> getNewUserByDate(int storeId, Integer mchId, String startDate, String endDate) throws LaiKeAPIException;

    /**
     * 查询手机号是否绑定其他账号
     *
     * @param accessId
     * @param mobile
     * @return
     */
    @Select("select id from lkt_user where store_id =  #{storeId} and (access_id != #{accessId} or access_id is null) and mobile = #{mobile} and is_lock = 0")
    User getUserByMobile(int storeId, String accessId, String mobile);

    /**
     * 查询用户是否存在
     *
     * @param storeId
     * @param userFromId
     * @return
     */
    @Select("select * from lkt_user where store_id =  #{storeId} and user_from_id = #{userFromId} and is_lock = 0")
    User getUserByUserFromID(int storeId, String userFromId);

    /**
     * 查询支付宝用户是否存在
     *
     * @param storeId
     * @param aliId
     * @return
     */
    @Select("select * from lkt_user where store_id = #{storeId} and zfb_id = #{aliId} and is_lock = 0")
    User getAliUser(int storeId, String aliId);

    /**
     * 同步用户信息
     *
     * @param accessId
     * @param mobile
     * @param storeId
     * @return
     */
    @Update("update lkt_user set mobile = #{mobile} where store_id = #{storeId} and access_id = #{accessId} ")
    int syncUserInfo(String accessId, String mobile, int storeId);

    List<String> getUserAllByUserId( @Param("storeId") int storeId,@Param("isAuth") Integer isAuth);


    List<Map<String, Object>> getUserList(Map<String, Object> map);

    int countUserList(Map<String, Object> map);

    @Update("UPDATE lkt_user SET grade = 0, is_out = 1 WHERE store_id = #{storeId} AND user_id = #{userId}")
    int memberExpirationProcessing(int storeId, String userId);

    @Update("update lkt_user set wx_id = null where store_id = #{storeId} and wx_id = #{wxId}")
    int emptyingWxId(int storeId, String wxId) throws LaiKeAPIException;

    /**
     * 查询未创建店铺用户
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getNotCreatedMchUser(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 条件查询未创建店铺用户数量
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int NotCreatedMchUser(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 日活跃用户
     *
     * @param storeId
     * @param date
     * @return
     */
    public List<Map<String, Object>> getActiveCountByHour(@Param("storeId") int storeId, @Param("date") String date) throws LaiKeAPIException;

    /**
     * 月活跃用户
     *
     * @param storeId
     * @param date
     * @return
     */
    @Select("SELECT  count(1) num, DATE_FORMAT(last_time,\"%y-%m-%d\") as day FROM  lkt_user   where DATE_FORMAT( last_time, '%Y-%m-%d' ) > #{date}  and store_id = #{storeId} GROUP BY day order by day")
    public List<Map<String, Object>> getActiveCountByDay(int storeId, String date) throws LaiKeAPIException;

    /**
     * 按日统计开通会员数
     *
     * @param storeId
     * @return
     */
    @Select("select COUNT(1) from lkt_user where  grade_end is NOT NULL\n" +
            "        and store_id = #{storeId} and grade = 1 and is_out = 0" +
            "        and DATE_FORMAT( grade_add, '%Y-%m-%d' ) = #{date} ")
    int getMembershipStatisticsByDay(int storeId, String date) throws LaiKeAPIException;

    /**
     * 按月统计开通会员数
     * <![CDATA[  >=  ]]>
     */
    @Select("select COUNT(1) from lkt_user where  grade_end is NOT NULL\n" +
            "        and store_id = #{storeId} and grade = 1 and is_out = 0" +
            "        and DATE_FORMAT( grade_add, '%Y-%m' ) <= #{date} and DATE_FORMAT(grade_end,'%Y-%m') >= #{date}")
    int getMembershipStatisticsByMonth(int storeId, String date) throws LaiKeAPIException;

    /**
     * 按日统计过期会员数
     *
     * @param storeId
     * @return
     */
    @Select("select COUNT(1) from lkt_user where  grade_end is NOT NULL\n" +
            "        and store_id = #{storeId} " +
            "        and  DATE_FORMAT( grade_end, '%Y-%m-%d' ) = #{date}")
    int getExpiredMemberByDay(int storeId, String date) throws LaiKeAPIException;

    /**
     * 按月统计过期会员数
     *
     * @param storeId
     * @return
     */
    @Select("select COUNT(1) from lkt_user where  grade_end is NOT NULL\n" +
            "        and store_id = #{storeId} " +
            "        and  DATE_FORMAT( grade_end, '%Y-%m' ) = #{date}")
    int getExpiredMemberByMonth(int storeId, String date) throws LaiKeAPIException;

    /**
     * 统计邮箱是否存在
     * @param storeId
     * @param eMail
     * @return
     */
    @Select("select count(1) from  lkt_user where store_id = #{storeId} and e_mail = #{eMail}")
    int countByEmail(int storeId, String eMail);

    @Select("select count(1) from lkt_user where store_id = #{storeId}  and mobile = #{phone} and cpc = #{cpc}")
    int getPhoneCount(Integer storeId, String phone, String cpc);

    @Select("select  count(1) from  lkt_user where e_mail = #{email} and store_id = #{storeId} and user_id <> #{userId}")
    int getEmailCount(@Param("email") String email,Integer storeId,String userId);

    @Select("select  count(1) from   lkt_user where e_mail = #{email} and store_id = #{storeId}")
    int emailCount(@Param("email") String email,Integer storeId);

    @Select("select count(1) from lkt_user where store_id = #{storeId}  and mobile = #{phone} and cpc = #{cpc} and id != #{id}")
    int getPhoneCountByUserId(int storeId, String phone, String cpc, Integer id);

}
