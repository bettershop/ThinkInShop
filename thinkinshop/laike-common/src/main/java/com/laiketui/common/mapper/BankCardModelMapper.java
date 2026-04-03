package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.BankCardModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 银行卡表
 *
 * @author Trick
 */
public interface BankCardModelMapper extends BaseMapper<BankCardModel>
{

    @Select("select id,Bank_name,Bank_card_number from lkt_bank_card where store_id = #{storeId} and mch_id = #{mchId} and recycle = 0 order by is_default desc")
    List<Map<String, Object>> selectBankcardByMch(int storeId, int mchId);

    @Select("select * from lkt_bank_card where store_id = #{storeId} and user_id=#{userId} and mch_id is null and recycle = 0 order by is_default desc")
    List<BankCardModel> selectBankcardByUser(int storeId, String userId);

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    //用户
    @Update("UPDATE lkt_bank_card SET is_default=0 WHERE user_id=#{userId} and mch_id is null and store_id=#{storeId} and recycle=0")
    int clearDefault(int storeId, String userId);

    //获取最新的一张银行卡
    @Select("select id from lkt_bank_card where recycle = 0 and user_id=#{userId} and mch_id is null order by add_date desc limit 1")
    Integer getNewCardOneByUserId(String userId);

    @Select("select id from lkt_bank_card where recycle = 0 and mch_id = #{mchId} order by add_date desc limit 1")
    Integer getNewCardOneByMchId(Integer mchId);

    //店铺
    @Update("UPDATE lkt_bank_card SET is_default=0 WHERE mch_id=#{mchId} and store_id=#{storeId} and recycle=0")
    int clearDefaultByMchId(int storeId, Integer mchId);


    //获取用户默认银行卡
    @Select("select * from lkt_bank_card where store_id = #{storeId} and user_id=#{userId} and mch_id is null and is_default=1 and recycle = 0 limit 1")
    BankCardModel getDefaultBankcardByUser(int storeId, String userId);

    //获取用户全部银行卡
    @Select("select * from lkt_bank_card where store_id = #{storeId} and user_id=#{userId} and mch_id is null and recycle = 0 ")
    List<BankCardModel> getAllDefaultBankcardByUser(int storeId, String userId);
}