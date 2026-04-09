package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.CustomerModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 商城 sql
 *
 * @author Trick
 * @date 2020/11/11 9:09
 */
public interface CustomerModelMapper extends BaseMapper<CustomerModel>
{

    /**
     * 获取商城信息动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 9:38
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商城信息动态sql - 统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 9:38
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 唯一字段验证
     *
     * @param email          -
     * @param name           -
     * @param customerNumber -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/28 13:52
     */
    @Select("select count(1) from lkt_customer where recycle = 0 and (email = #{email} OR name =#{name} OR customer_number = #{customerNumber})")
    int onlyVerification(String email, String name, String customerNumber) throws LaiKeAPIException;


    /**
     * 获取自营店id
     *
     * @param storeId -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/23 9:58
     */
    @Select("select b.shop_id from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id and b.store_id=a.id  where store_id = #{storeId}")
    Integer getStoreMchId(int storeId) throws LaiKeAPIException;

    /**
     * 获取自营店用户id
     *
     * @param storeId -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-06-30 16:01:58
     */
    @Select("select m.user_id from lkt_customer as a left join lkt_admin as b on a.admin_id = b.id and b.store_id=a.id inner join lkt_mch m on m.id=b.shop_id  where a.id = #{storeId}")
    String getStoreUserId(int storeId) throws LaiKeAPIException;


    /**
     * 清除所有默认
     *
     * @return Integer
     * @author Trick
     * @date 2021/6/11 14:53
     */
    @Update("update lkt_customer set is_default=0 where is_default=1")
    Integer removeDefault();


    /**
     * 设置一个为默认
     *
     * @return Integer
     * @author Trick
     * @date 2021/6/11 14:53
     */
    @Update("update lkt_customer a,(select id from lkt_customer x where x.recycle=0 order by add_date desc limit 1) b set a.is_default=1 where a.is_default=0 and a.id = b.id")
    Integer setDefault();


    /**
     * 获取商城列表
     *
     * @param sysDate -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/6 12:00
     */
    @Select("select id,name from lkt_customer where recycle =  0 and end_date > #{sysDate} " +
            " order by is_default desc,add_date desc ")
    List<Map<String, Object>> storeList(Date sysDate) throws LaiKeAPIException;


    /**
     * 获取默认商城
     *
     * @return Integer
     * @author Trick
     * @date 2022/6/6 19:36
     */
    @Select("select id from lkt_customer where recycle =  0 and is_default = 1 limit 1")
    Integer getDefaultStoreId();

    /**
     * 获取默认商城的管理员角色id
     *
     * @return Integer
     * @author Trick
     * @date 2022/6/6 19:36
     */
    @Select("select a.role from lkt_customer c left join lkt_admin a on c.admin_id=a.id where c.recycle =  0 and c.is_default = 1 limit 1")
    Integer getDefaultStoreRole();

    //获取所有商城id
    @Select("select id from lkt_customer where recycle=0 and status=0 and end_date>=#{sysdate}")
    List<Integer> getStoreIdAll(Date sysdate);

    @Update("update lkt_customer set is_default= 1 where id = #{id} ")
    int setDefaultById(Integer id);

    @Update("update lkt_customer set merchant_logo = #{storeLogo} where id = #{storeId}")
    void updateLogo(String storeLogo, int storeId);

}