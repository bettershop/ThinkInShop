package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CurrencyStoreModelMapper extends BaseMapper<CurrencyStoreModel>
{

    public int countCurrencyStore(CurrencyStoreVo vo);

    public List<Map<String, Object>> queryCurrencyStoreList(CurrencyStoreVo vo);

    @Update("UPDATE lkt_currency_store set exchange_rate = #{currency_rata} where store_id = #{storeId} and currency_id = #{currencyID} ")
    public int updateCurrencyExchangerate(int currencyID, int storeId, BigDecimal currency_rata);

    @Update("UPDATE lkt_currency_store set default_currency = #{default_currency},exchange_rate = #{exchange_rate} where store_id = #{storeId} and currency_id = #{currencyID} ")
    public int updateDefaultCurrency(int currencyID, int storeId, int default_currency, BigDecimal exchange_rate);

    @Select(" select a.*,b.* from lkt_currency_store as a ,lkt_currency as b where a.currency_id = b.id and a.default_currency = 1 and a.store_id = #{storeId} ")
    public Map getDefaultCurrency(int storeId);

    @Select(" select a.*,b.* from lkt_currency_store as a ,lkt_currency as b where a.currency_id = b.id and a.store_id = #{storeId} and a.currency_id = #{currencyId}")
    public Map getCurrencyInfo(int storeId, int currencyId);

    int deleteByBatchParams(List<CurrencyStoreModel> currencyStoreModels);

    @Select({
            "<script>",
            "select COUNT(1) from lkt_currency_store where currency_id in",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int selectByCurrencyIds(@Param("ids") List<Integer> ids);


    List<CurrencyStoreModel> selectByStoreIds(List<Integer> storeIds);

    List<CurrencyStoreModel> selectDefaultCurrencies(List<Integer> storeIds);


}