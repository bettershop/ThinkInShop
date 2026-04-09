package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.distribution.UserDistributionModel;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 分销
 *
 * @author Trick
 * @date 2021/1/8 10:33
 */
public interface UserDistributionModelMapper extends BaseMapper<UserDistributionModel>
{


    /**
     * 动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 18:09
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 18:09
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 用户是否是分销员
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 10:35
     */
    @Select("select count(1) from lkt_user_distribution where user_id = #{userId} and store_id=#{storeId}")
    int counUserDistribution(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 用户是否是分销员并且有分销等级
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/8 10:35
     */
    @Select("select count(1) from lkt_user_distribution where user_id = #{userId} and store_id=#{storeId} and level > 0")
    int countUserDistributionAndHaveLevel(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 获取用户分销信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:25
     */
    List<Map<String, Object>> selectUserDistributionInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户分销信息-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:25
     */
    int countUserDistributionInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取未开通分销的用户列表
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:25
     */
    List<Map<String, Object>> selectUserDistribution(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取未开通分销的用户列表-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 17:25
     */
    int countUserDistribution(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 更具id动态修改
     *
     * @param model -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/5 10:55
     */
    int updateByPrimaryKeySelective1(UserDistributionModel model) throws LaiKeAPIException;


    /**
     * 修改树形结构
     *
     * @param storeId -
     * @param lt      -
     * @param rt      -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/5 15:49
     */
    @Update("UPDATE lkt_user_distribution SET lt=#{lt},rt=#{rt} where store_id=#{storeId} and user_id=#{userId}")
    int updateTree(int storeId, int lt, int rt, String userId) throws LaiKeAPIException;


    /**
     * 扩展树节点L
     *
     * @param storeId -
     * @param rt      -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 15:26
     */
    @Update("update lkt_user_distribution set lt = lt + 2 where store_id = #{storeId} and lt>=#{rt}")
    int updateTreeLt(int storeId, int rt) throws LaiKeAPIException;


    /**
     * 扩展整个树节点R
     *
     * @param storeId -
     * @param rt      -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 15:26
     */
    @Update("update lkt_user_distribution set rt = rt + 2 where store_id = #{storeId} and rt>=#{rt} ")
    int updateTreeRt(int storeId, int rt) throws LaiKeAPIException;

    /**
     * 扩展树节点R
     *
     * @param storeId -
     * @param userId  -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 15:26
     */
    @Update("update lkt_user_distribution SET rt=rt+2 WHERE store_id=#{storeId} and user_id =#{userId}")
    int updateThrreRtByUserId(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 动态查询
     *
     * @param model -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 10:04
     */
    int selectCount1(UserDistributionModel model) throws LaiKeAPIException;

    /**
     * 保存
     *
     * @param model -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 15:58
     */
    int save(UserDistributionModel model) throws LaiKeAPIException;


    /**
     * 保存
     *
     * @param storeId -
     * @param userId  -
     * @param sysDate -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 16:55
     */
    @Insert("insert into lkt_user_distribution(store_id,user_id,pid,add_date,level,lt,rt,uplevel) values (#{storeId},#{userId},#{userId},#{sysDate},0,0,1,0)")
    int save1(int storeId, String userId, Date sysDate) throws LaiKeAPIException;


    /**
     * 动态查询
     *
     * @param model -
     * @return UserDistributionModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 10:23
     */
    UserDistributionModel selectOne1(UserDistributionModel model) throws LaiKeAPIException;


    /**
     * 动态查询
     *
     * @param model -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 11:13
     */
    List<UserDistributionModel> select1(UserDistributionModel model) throws LaiKeAPIException;

    /**
     * 获取推荐人信息
     *
     * @param storeId -
     * @param userId  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/5 11:15
     */
    @Select("select b.pid,b.level,a.Referee,a.user_id from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id where a.store_id = #{storeId} and a.user_id = #{userId}")
    Map<String, Object> getReferencesinfo(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 获取用户等级信息
     *
     * @param storeId -
     * @param userId  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/18 17:48
     */
    @Select("select a.level,b.sets,b.discount from lkt_user_distribution as a left join lkt_distribution_grade as b on a.level = b.id " +
            "where a.store_id = #{storeId} and a.user_id = #{userId} and a.level > 0 limit 1")
    Map<String, Object> getUserGradeinfo(int storeId, String userId) throws LaiKeAPIException;


    /**
     * 获取用户下级分销信息
     *
     * @param storeId -
     * @param lt      -
     * @param rt      -
     * @param uplecel -
     * @param start   -
     * @param end     -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 12:12
     */
    @Select("select a.user_id,a.headimgurl,a.user_name,a.mobile,b.level,b.uplevel,b.lt,b.rt,b.add_date,father.user_name fatherName,father.user_id fatherUserId from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id" +
            " left join lkt_user father on father.user_id=b.pid and father.store_id=b.store_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0 and b.uplevel=#{uplecel} LIMIT #{start},#{end}")
    List<Map<String, Object>> getChildrenList(int storeId, int lt, int rt, int uplecel, int start, int end) throws LaiKeAPIException;

    @Select("SELECT user_id FROM lkt_user_distribution WHERE store_id = #{storeId} and (lt > #{lt} and rt < #{rt}) AND ( `level` >= #{level} AND `level` <= #{upLevel}) ")
    List<String> getAllChildrenList(int storeId, int lt, int rt, int level, int upLevel) throws LaiKeAPIException;


    /**
     * 获取用户下级分销信息数量
     */
    @Select("select count(*) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "left join lkt_user father on father.user_id=b.pid and father.store_id=b.store_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0 and b.uplevel=#{uplecel}")
    int countChildrenList(int storeId, int lt, int rt, int uplecel) throws LaiKeAPIException;


    /**
     * 获取用户下级分销信息 -增加下级筛选条件
     *
     * @param storeId -
     * @param lt      -
     * @param rt      -
     * @param uplecel -
     * @param start   -
     * @param end     -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 12:12
     */
    @Select("select a.user_id,a.headimgurl,a.user_name,a.mobile,b.level,b.uplevel,b.lt,b.rt,b.add_date,father.user_name fatherName from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "left join lkt_user father on father.user_id=b.pid and father.store_id=b.store_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0 and b.uplevel=#{uplecel} and (a.user_id = #{key} or a.mobile = #{key})  LIMIT #{start},#{end}")
    List<Map<String, Object>> getChildrenList2(int storeId, int lt, int rt, int uplecel, int start, int end, String key) throws LaiKeAPIException;

    /**
     * 获取用户下级分销信息数量 -增加下级筛选条件
     */
    @Select("select count(*) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "left join lkt_user father on father.user_id=b.pid and father.store_id=b.store_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0 and b.uplevel=#{uplecel} and (a.user_id = #{key} or a.mobile = #{key})")
    int countChildrenList2(int storeId, int lt, int rt, int uplecel, String key) throws LaiKeAPIException;

    /**
     * 团队人数
     *
     * @param storeId
     * @param lt
     * @param rt
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(distinct a.user_id) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0")
    int countChildrenNum(int storeId, int lt, int rt) throws LaiKeAPIException;

    /**
     * 团队直推人数
     *
     * @param storeId
     * @param lt
     * @param rt
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(distinct a.user_id) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "where b.store_id=#{storeId} and b.lt>#{lt} and b.rt<#{rt} and b.level>0 and pid =#{userId}")
    int countChildrenNum2(int storeId, int lt, int rt, String userId) throws LaiKeAPIException;

    /**
     * 团队业绩
     *
     * @param storeId
     * @param lt
     * @param rt
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select sum(b.allamount) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "where b.store_id=#{storeId} and b.lt>=#{lt} and b.rt<=#{rt} and b.level>0")
    BigDecimal sumChildrenAllAmount(int storeId, int lt, int rt) throws LaiKeAPIException;

    /**
     * 团队人数订单
     *
     * @param storeId
     * @param lt
     * @param rt
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(*) from lkt_user a left join lkt_user_distribution b on a.user_id=b.user_id " +
            "RIGHT JOIN lkt_order o ON o.user_id = b.user_id " +
            "where b.store_id=#{storeId} and b.lt>=#{lt} and b.rt<=#{rt} and b.level>0 " +
            "and o.otype = 'FX' and o.status in (1,2,5)")
    int countChildrenOrderNum(int storeId, int lt, int rt) throws LaiKeAPIException;

    /**
     * 统计下级数量
     *
     * @param storeId -
     * @param lt      -
     * @param rt      -
     * @param uplevel -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 13:33
     */
    @Select("select count(id) as count from lkt_user_distribution where store_id=#{storeId} and lt>#{lt} and rt<#{rt} and level>0 ")
    int childrenNum(int storeId, int lt, int rt, int uplevel) throws LaiKeAPIException;

    /**
     * 获取用户有效佣金明细信息
     *
     * @param storeId -
     * @param userId  -
     * @param start   -
     * @param end     -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 14:37
     */
    @Select("select a.money,a.add_date,a.from_id,a.status,a.sNo,b.z_price from lkt_distribution_record a left join lkt_order b on a.sNo=b.sNo " +
            " where a.store_id = #{storeId} and a.user_id = #{userId} and ((b.status in (1,2,5,7) and a.type=1 and b.pay_time is not null) or a.type=3) " +
            " and a.money>0 order by a.add_date desc LIMIT #{start},#{end}")
    List<Map<String, Object>> getCommissionDetail(int storeId, String userId, int start, int end) throws LaiKeAPIException;

    /**
     * 获取用户有效佣金明细信息 数量
     *
     * @param storeId -
     * @param userId  -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 14:37
     */
    @Select("select count(*) from lkt_distribution_record a left join lkt_order b on a.sNo=b.sNo " +
            " where a.store_id = #{storeId} and a.user_id = #{userId} and ((b.status in (1,2,5,7) and a.type=1 and b.pay_time is not null) or a.type=3) " +
            " and a.money>0 ")
    Integer countCommissionDetail(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 分佣提现申请
     *
     * @param storeId -
     * @param userId  -
     * @param txMoney -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 17:33
     */
    @Update("update lkt_user_distribution set tx_commission = tx_commission - #{txMoney} " +
            "where store_id = #{storeId} and user_id = #{userId} and commission>=#{txMoney} and tx_commission >= #{txMoney}")
    int updateCommission(int storeId, String userId, BigDecimal txMoney) throws LaiKeAPIException;

    /**
     * 增加分佣提现
     *
     * @param storeId -
     * @param userId  -
     * @param txMoney -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-02-09 14:28:54
     */
    @Update("update lkt_user_distribution set tx_commission = tx_commission + #{txMoney} " +
            "where store_id = #{storeId} and user_id = #{userId}")
    int updateCommission1(int storeId, String userId, BigDecimal txMoney) throws LaiKeAPIException;

    /**
     * 发放可提现佣金
     *
     * @param storeId    -
     * @param userId     -
     * @param commission -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/25 10:12
     */
    @Update("update lkt_user_distribution set tx_commission = tx_commission + #{commission} where store_id = #{storeId} and user_id = #{userId}")
    int grantTxCommission(int storeId, String userId, BigDecimal commission) throws LaiKeAPIException;

    /**
     * 更新用户佣金【结算】
     *
     * @param storeId    -
     * @param userId     -
     * @param commission -需要分佣的金额
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 9:31
     */
    @Update("update lkt_user_distribution set commission = commission + #{commission},accumulative = accumulative + #{commission},tx_commission = tx_commission + #{commission} " +
            "where store_id = #{storeId} and user_id = #{userId}")
    int updateUserCommission(int storeId, String userId, BigDecimal commission) throws LaiKeAPIException;


    /**
     * 更新用户业绩
     *
     * @param storeId     -
     * @param userId      -
     * @param achievement -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 9:55
     */
    @Update("update lkt_user_distribution set allamount = allamount + #{achievement} " +
            "where store_id = #{storeId} and user_id = #{userId}")
    int updateUserAchievement(int storeId, String userId, BigDecimal achievement) throws LaiKeAPIException;

    @Update("update lkt_user_distribution set onlyamount = onlyamount + #{achievement} " +
            "where store_id = #{storeId} and user_id = #{userId}")
    int updateUserOnlyAmount(int storeId, String userId, BigDecimal achievement) throws LaiKeAPIException;

    //onlyamount = onlyamount + #{achievement},
    /**
     * 更新用户业绩
     *
     * @param storeId     -
     * @param userId      -
     * @param achievement -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 9:55
     */
    @Update("update lkt_user_distribution set allamount = allamount + #{achievement} " +
            "where store_id = #{storeId} and user_id = #{userId}")
    int updateUserAchievement1(int storeId, String userId, BigDecimal achievement) throws LaiKeAPIException;

    /**
     * 获取第一个分销人 user_id
     *
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select user_id from lkt_user_distribution where store_id = #{storeId} order by rt desc limit 0,1")
    String getTheTopLevelUser(int storeId) throws LaiKeAPIException;

    /**
     * 获取用户的分销等级
     *
     * @param storeId
     * @param userId
     * @return
     */
    @Select("select level from lkt_user_distribution where store_id = #{storeId} and user_id = #{userId} ")
    int getUserDisLevel(int storeId, String userId);

    /**
     * 获取分销人 user_id
     *
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select * from lkt_user_distribution where store_id = #{storeId}  and user_id = #{userId} ")
    UserDistributionModel getParentUserInfo(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 预计用户佣金
     *
     * @param storeId -
     * @param userId  -
     * @return BigDecimal
     * @author Trick
     * @date 2022/1/11 17:40
     */
    @Select("select sum(a.money) as sum from lkt_distribution_record a inner join lkt_order b on a.sNo=b.sNo " +
            " where a.store_id=#{storeId} and a.type=1 and a.user_id=#{userId} and b.status in (1,2,5) and a.status=0 ")
    BigDecimal getDisPredictAmt(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 获取用户佣金排行
     *
     * @param storeId
     * @param storeId 排行数量
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select b.user_name,b.headimgurl,a.accumulative " +
            "from lkt_user_distribution a left join lkt_user b on a.user_id = b.user_id " +
            "where a.store_id = #{storeId} " +
            "and a.level != 0 " +
            "ORDER BY " +
            "a.accumulative desc " +
            "LIMIT 0,#{num} ")
    List<Map<String, Object>> getUserCommissionRanking(int storeId, int num) throws LaiKeAPIException;

    /**
     * 获取用户单日新增下级
     *
     * @param storeId
     * @param userId
     * @param time
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(*) as sum from lkt_user_distribution a  " +
            " where a.store_id=#{storeId} and a.pid=#{userId} and DATE_FORMAT(a.add_date,\"%Y-%m-%d\") = #{time} ")
    Integer getNewUserNumByAddtime(int storeId, String userId, String time) throws LaiKeAPIException;


    /**
     * 获取当日新增邀请 新增绑定临时关系
     *
     * @param storeId
     * @param userId
     * @param time
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select count(*) as sum from lkt_user a " +
            "where a.store_id=#{storeId} and a.Referee=#{userId} and DATE_FORMAT(a.Register_data,\"%Y-%m-%d\") = #{time} " +
            "and a.user_id not in (select user_id from lkt_user_distribution)")
    Integer getNewInvitationNumByAddtime(int storeId, String userId, String time) throws LaiKeAPIException;
}