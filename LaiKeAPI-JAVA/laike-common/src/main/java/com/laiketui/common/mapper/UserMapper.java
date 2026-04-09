package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.user.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author wangxian
 */
public interface UserMapper extends BaseMapper<User>
{

    /**
     * 取消订单时调用 反钱
     *
     * @param zPirce
     * @param storeId
     * @param userId
     * @return
     */
    @Update("update lkt_user set money = money + #{zPirce} where store_id = #{storeId} and user_id = #{userId} ")
    int updateUserMoney(BigDecimal zPirce, int storeId, String userId);

    /**
     * 更新用户登陆次数
     *
     * @param storeId
     * @param userId
     * @return
     */
    @Update("update lkt_user set login_num = 0 where store_id = #{storeId} and user_id = #{userId} ")
    int updateUserLoginnum(int storeId, String userId);

    /**
     * 余额支付更新账户
     *
     * @param storeId
     * @param userId
     * @return
     */
    @Update("update lkt_user set money = money-#{paymentMoney} where store_id = #{storeId} and user_id = #{userId} and money > 0")
    int walletPayUpdateUserAccount(BigDecimal paymentMoney, int storeId, String userId);

    /**
     * 积分支付
     */
    @Update("update lkt_user set score = score-#{paymentMoney} where store_id = #{storeId} and user_id = #{userId} and score > 0")
    int scorePayUpdateUserAccount(BigDecimal paymentMoney, int storeId, String userId);

    /**
     * 更新账户积分
     *
     * @param storeId
     * @param userId
     * @return
     */
    @Update(" update lkt_user set score = score+ #{score} where store_id = #{storeId} and user_id = #{userId}  ")
    int updateUserScore(int score, int storeId, String userId);



    @Select("SELECT count(1) value,case source  WHEN  2  then  \"H5端\"    when 1 then  \"小程序端\"   WHEN 11 then \"App\" when 6 then \"PC端\"\n" +
            "ELSE \"\" end  as name\n" +
            "From  lkt_user  where     source  in(1,2,6,11) and  store_id=#{storeId} GROUP BY source ")
    public List<Map<String, Object>> countBySource(int storeId);


    @Select("SELECT  count(1) num, DATE_FORMAT(Register_data,\"%Y-%m-%d\") as day FROM  lkt_user   where  store_id = #{storeId} and DATE_FORMAT( Register_data, \"%Y-%m-%d\" )=#{date} and source=#{source}  GROUP BY  DATE_FORMAT(Register_data,\"%Y-%m-%d\")")
    public Integer getAdditionUserByDay(@Param("storeId") int storeId, @Param("date") String date, @Param("source") Integer source);


}