package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierAccountLogModel;
import org.apache.ibatis.annotations.Insert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SupplierAccountLogModelMapper extends BaseMapper<SupplierAccountLogModel>
{

    /**
     * 查询出入账记录
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getAccountLog(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计出入账记录
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countAccountLog(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计出入账金额
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    BigDecimal countAccountLogMoney(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 保存一条供应商账户明细
     *
     * @param store_id
     * @param supplier_id
     * @param amount
     * @param account_money
     * @param status
     * @param type
     * @param addtime
     * @param remake
     * @return
     * @throws LaiKeAPIException
     */
    @Insert("insert into lkt_supplier_account_log(store_id,supplier_id,amount,account_money,status,type,add_time,remake,remark) values(#{store_id},#{supplier_id},#{amount},#{account_money},#{status},#{type},#{addtime},#{remake},#{remark})")
    int saveAccountLog(int store_id, int supplier_id, BigDecimal amount, BigDecimal account_money, int status, int type, Date addtime, String remake, String remark) throws LaiKeAPIException;

}