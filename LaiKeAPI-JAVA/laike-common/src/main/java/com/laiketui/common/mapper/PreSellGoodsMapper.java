package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.presell.PreSellGoodsModel;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 预售商品信息表sql映射
 *
 * @author sunH_
 * @date 2021/12/20 16:00
 */
public interface PreSellGoodsMapper extends BaseMapper<PreSellGoodsModel>
{

    /**
     * 获取预售商品信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2021/12/21 11:00
     */
    List<Map<String, Object>> getGoodsList(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取预售商品统计
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2021/12/21 11:00
     */
    int getTotal(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取预售商品信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     * @author sunH_
     * @date 2022/01/04 11:00
     */
//    @Select("SELECT p.* FROM lkt_pre_sell_goods s LEFT JOIN lkt_product_list p ON s.product_id = p.id WHERE s.is_display = 1 AND s.is_delete = 0 AND p.`status` = 2 AND p.store_id = #{storeId} ORDER BY p.add_date limit #{pageModel.pageNo},#{pageModel.pageSize}")
    List<Map<String, Object>> getPreGoods(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 增库存
     *
     * @param num -
     * @param pid -
     * @throws LaiKeAPIException -
     * @author sunH_
     * @date 2022/01/18 16:47
     */
    @Update("update lkt_pre_sell_goods set surplus_num = surplus_num + #{num} where product_id = #{pid}")
    void addGoodNum(int num, int pid) throws LaiKeAPIException;
}