package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.supplier.SupplierBankCardModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface SupplierBankCardModelMapper extends BaseMapper<SupplierBankCardModel>
{
    @Select("select id,Bank_name,Bank_card_number from lkt_supplier_bank_card where store_id = #{storeId} and supplier_id = #{supplierId} and recycle = 0 order by is_default desc")
    List<Map<String, Object>> selectBank(int storeId, int supplierId);

    /**
     * 获取银行卡信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    List<Map<String, Object>> getBankCard(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计银行卡信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     */
    int countBankCard(Map<String, Object> map) throws LaiKeAPIException;

    //供应商银行卡默认
    @Update("UPDATE lkt_supplier_bank_card SET is_default = 0 WHERE supplier_id = #{supplierId} and store_id=#{storeId} and recycle=0")
    int clearDefaultByMchId(int storeId, Integer supplierId);
}