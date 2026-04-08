package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.invoice.InvoiceHeaderModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 发票抬头 sql
 *
 * @author sunH
 * @date 2022/07/28 11:59
 */
public interface InvoiceHeaderModelMapper extends BaseMapper<InvoiceHeaderModel>
{

    /**
     * 获取我的发票抬头 -
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/07/28 14:04
     */
    List<Map<String, Object>> getList(Map<String, Object> map) throws LaiKeAPIException;

    Integer countList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询用户默认发票抬头
     *
     * @param storeId
     * @param userId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("SELECT * from lkt_invoice_header WHERE store_id = #{storeId} AND user_id = #{userId} AND is_default = 1 AND recovery = 0")
    Map<String, Object> getDefault(int storeId, String userId) throws LaiKeAPIException;

    /**
     * 删除默认发票抬头则将下一个发票抬头设为默认
     *
     * @param storeId       -
     * @param userId        -
     * @param updateDefault -
     * @param isDefault     -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/1 17:15
     */
    @Update("update lkt_invoice_header set is_default = #{updateDefault} where store_id = #{storeId} and user_id = #{userId} and is_default = #{isDefault} ORDER BY add_time DESC LIMIT 1")
    int setDefaultDel(@Param("storeId") int storeId, @Param("userId") String userId, @Param("updateDefault") int updateDefault, @Param("isDefault") int isDefault) throws LaiKeAPIException;
}