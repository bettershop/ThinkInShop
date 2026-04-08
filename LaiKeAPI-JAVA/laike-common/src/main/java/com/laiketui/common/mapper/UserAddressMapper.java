package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.user.UserAddress;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author wx
 * @description 用户地址
 * @date 2019/10/19 11:49
 * @return
 */
public interface UserAddressMapper extends BaseMapper<UserAddress>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 修改用户地址动态sql
     *
     * @param userAddress -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/4 18:20
     */
    int updateById(UserAddress userAddress) throws LaiKeAPIException;


    /**
     * 设置默认地址
     * 最新的一条
     *
     * @param uid -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/5 13:56
     */
    @Update("update lkt_user_address set is_default = 1 where uid = #{uid} order by id desc limit 1")
    int setDefaultAddress(String uid) throws LaiKeAPIException;

    /**
     * 当用户没有默认地址时用最新的地址
     *
     * @param userId  -
     * @param storeId -
     * @return UserAddress
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/26 17:51
     */
    @Select("select * from lkt_user_address where store_id =#{storeId} and uid=#{userId} order by id desc limit 1")
    UserAddress getUserAddress(int storeId, String userId) throws LaiKeAPIException;
}