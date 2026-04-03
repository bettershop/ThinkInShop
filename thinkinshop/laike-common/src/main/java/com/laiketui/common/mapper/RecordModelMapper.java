package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.log.RecordModel;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 操作记录 sql
 *
 * @author Trick
 * @date 2020/11/2 16:54
 */
public interface RecordModelMapper extends BaseMapper<RecordModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取我的钱包
     * [提现成功的记录不显示了]
     *
     * @param recordModel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/2 16:11
     */
    @Select("<script>" +
            " select money,add_date,type from lkt_record where store_id = #{store_id} and user_id = #{user_id} and is_mch = 0 " +
            " and type in( 1,2 , 4 , 5,11 ,14,22,36,37,28,31,32,33,34, 12 , 1 , 5 , 6 , 13 , 14) " +
            " order by add_date desc " +
            "<if test=\"page!=null\">" +
            " LIMIT #{page.pageNo},#{page.pageSize} " +
            "</if>" +
            "</script>")
    List<Map<String, Object>> getUserWallet(RecordModel recordModel) throws LaiKeAPIException;

    @Select("<script>" +
            " select count(1) from lkt_record where store_id = #{store_id} and user_id = #{user_id} and is_mch = 0 " +
            " and (type =1 or type =2 or type =3 or type =4 or type =5 or type =11 or type =12 or type =13 or type =14 or type =19 " +
            " or type =20 or type =22 or type =23 or type =24 or type = 26 or type = 27 or type =30) " +
            "</script>")
    int countUserWallet(RecordModel recordModel) throws LaiKeAPIException;

    /**
     * 获取用户操作记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    List<Map<String, Object>> getUserWalletRecordInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计金额总和
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    BigDecimal sumUserWalletRecordInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计用户操作记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    int countUserWalletRecordInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户资金详情记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    List<Map<String, Object>> selectMoneyInfo_see(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计用户资金详情记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    int countMoneyInfo_see(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计用户资金详情记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    BigDecimal countMoney(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取用户积分详情记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    List<Map<String, Object>> selectIntegralInfo_see(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 统计用户积分详情记录
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/11 16:03
     */
    int countIntegralInfo_see(Map<String, Object> map) throws LaiKeAPIException;


}