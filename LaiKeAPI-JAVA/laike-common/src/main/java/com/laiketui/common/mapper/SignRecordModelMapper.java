package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.SignRecordModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 签到记录 sql
 *
 * @author Trick
 * @date 2020/10/14 15:31
 */
public interface SignRecordModelMapper extends BaseMapper<SignRecordModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取会员签到记录
     * 【可能会存在允许多次签到的情况】
     *
     * @param signRecordModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/14 15:32
     */
    @Select("select * from lkt_sign_record where store_id = #{store_id} and user_id = #{user_id} and sign_time > #{sign_time} and type = 0 order by sign_time desc")
    List<SignRecordModel> getSiginRecord(SignRecordModel signRecordModel) throws LaiKeAPIException;


    /**
     * 用户签到列表动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 17:39
     */
    List<Map<String, Object>> selectUserDynamic(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> selectUserDynamic1(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 用户签到列表动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 17:39
     */
    int countUserDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 根据时间统计用户签到次数
     *
     * @param storeId   -
     * @param userId    -
     * @param startTime -
     * @param endTime   -
     * @return Integer
     * @author Trick
     * @date 2021/4/7 15:56
     */
    @Select("select count(1) from lkt_sign_record where store_id = #{storeId} and user_id = #{userId} and sign_time > #{startTime} and sign_time < #{endTime} and type = 0 ")
    Integer countSiginNum(int storeId, String userId, Date startTime, Date endTime);


    /**
     * 统计签到获取的积分
     *
     * @param storeId -
     * @param userId  -
     * @return Integer
     * @author Trick
     * @date 2021/5/11 11:39
     */
    @Select("select sum(sign_score) from lkt_sign_record where store_id = #{storeId} and user_id = #{userId} and type = 0")
    Integer sumSiginScore(int storeId, String userId);


    /**
     * 统计某类型的积分
     *
     * @param storeId -
     * @param userId  -
     * @param type    -
     * @return Integer
     * @author Trick
     * @date 2021/5/11 11:39
     */
    @Select("select sum(sign_score) from lkt_sign_record where store_id = #{storeId} and user_id = #{userId} and type=#{type} ")
    Integer sumSignScoreByType(int storeId, String userId, Integer type);

    /**
     * 查询当前冻结积分
     *
     * @param storeId 店铺ID
     * @param userId  用户ID
     * @param type    积分类型（如冻结积分的类型值）
     * @return 符合条件的冻结积分总和
     */
    @Select({
            "SELECT COALESCE(SUM(sign_score), 0) ",
            "FROM lkt_sign_record ",
            "WHERE store_id = #{storeId} ",
            "AND user_id = #{userId} ",
            "AND type = #{type} ",
            "AND frozen_time IS NOT NULL"
    })
    Integer getFrozenScore(int storeId, String userId, Integer type);

    /**
     * 结算用户积分
     *
     * @param storeId -
     * @param sysDate -
     * @return Integer
     * @author Trick
     * @date 2021/11/10 19:54
     */
    @Update(" update lkt_user a,(select x.user_id,ifnull(sum(x.sign_score),0) num  FROM lkt_sign_record x " +
            " WHERE  x.type in(0,2,4,6,7,8,9) and x.score_invalid < #{sysDate} and x.score_invalid is not null group by x.user_id ) b set a.score = CAST(a.score - b.num AS decimal(24,0))  " +
            " WHERE a.store_id = #{storeId} " +
            " and a.user_id=b.user_id and a.score >0 AND (a.score - b.num)>=0 ")
    Integer settlementIntegral(int storeId, Date sysDate);

    /**
     * 积分失效【商城所有】
     *
     * @param sysDate -
     * @return Integer
     * @author Trick
     * @date 2021/11/10 19:54
     */
    @Update(" update lkt_sign_record a set a.type=10 " +
            " where type in(0,2,4,6,7,8,9) and score_invalid < #{sysDate} ")
    int userInvalidScore(Date sysDate);

    /**
     * 积分失效【商城所有】
     *
     * @param sysDate -
     * @return Integer
     * @author Trick
     * @date 2021/11/10 19:54
     */
    @Select(" SELECT id from lkt_sign_record where type in(0,2,4,6,7,8,9) and  score_invalid < #{sysDate} and notice=0 and store_id = #{storeId}")
    List<Map<String, Object>> getUserInvalidScore(int storeId, Date sysDate);

    /**
     * 结算用户积分
     *
     * @param storeId -
     * @param sysDate -
     * @param userId  -
     * @return Integer
     * @author Trick
     * @date 2021/11/10 19:54
     */
    @Update(" update lkt_user a,(select x.user_id,ifnull(sum(x.sign_score),0) num  FROM lkt_sign_record x " +
            " WHERE  x.type in(0,2,4,6,7,8,9) and x.score_invalid < #{sysDate} and x.score_invalid is not null and x.user_id=#{userId} group by x.user_id ) b set a.score = CAST(a.score - b.num AS decimal(24,0))  " +
            " WHERE a.store_id = #{storeId} " +
            " and a.user_id=b.user_id and a.score >0 ")
    Integer settlementIntegralByUserId(int storeId, String userId, Date sysDate);

    /**
     * 积分失效
     *
     * @param userId  -
     * @param sysDate -
     * @return Integer
     * @author Trick
     * @date 2021/11/10 19:54
     */
    @Update(" update lkt_sign_record a set a.type=10 " +
            " where type in(0,2,4,6,7,8,9) and score_invalid < #{sysDate} and a.user_id=#{userId} ")
    int userInvalidScoreByUserId(String userId, Date sysDate);

    /**
     * 积分解冻结算
     *
     * @param storeId -
     * @param sysDate -
     * @param type    - 解冻类型
     * @return Integer
     * @author Trick
     * @date 2022-10-24 17:39:16
     */
    @Update(" update lkt_user a,(select x.user_id,ifnull(sum(x.sign_score),0) num  FROM lkt_sign_record x " +
            " WHERE  x.type =#{type} and x.frozen_time<=#{sysDate} and x.frozen_time is not null group by x.user_id ) b set a.score = CAST(a.score + b.num AS decimal(24,0))  " +
            " WHERE a.store_id = #{storeId} " +
            " and a.user_id=b.user_id ")
    Integer setFrozenIntegral(int storeId, Date sysDate, Integer type, Integer newType);

    /**
     * 积分解冻--查询符合条件的记录列表
     *
     * @param storeId - 店铺ID
     * @param sysDate - 系统日期
     * @param type    - 解冻类型
     * @return List<SignRecordModel> 符合条件的记录列表
     * @author Trick
     * @date 2023-11-10
     */
    @Select("SELECT b.* FROM lkt_sign_record b " +
            "WHERE b.store_id = #{storeId} AND b.frozen_time <= #{sysDate} AND b.type = #{type}")
    List<SignRecordModel> getFrozenIntegralRecords(int storeId, Date sysDate, Integer type);

    /**
     * 积分解冻--将符合条件的记录的 frozen_time 设置为 null
     *
     * @param storeId - 店铺ID
     * @param sysDate - 系统日期
     * @param type    - 解冻类型（原冻结状态）
     * @return 更新的记录数量
     */
    @Update("UPDATE lkt_sign_record b " +
            "SET b.frozen_time = NULL " +
            "WHERE b.store_id = #{storeId} AND b.frozen_time <= #{sysDate} AND b.type = #{type}")
    int clearFrozenTime(int storeId, Date sysDate, Integer type);
}
