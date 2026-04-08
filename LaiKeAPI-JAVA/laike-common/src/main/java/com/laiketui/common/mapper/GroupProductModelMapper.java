package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.group.GroupProductModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 拼团
 *
 * @author Trick
 * @date 2020/10/13 9:31
 */
public interface GroupProductModelMapper extends BaseMapper<GroupProductModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;


    List<Map<String, Object>> selectGoodsDynamic(Map<String, Object> map);

    int countGoodsDynamic(Map<String, Object> map);



    /**
     * 获取产品活动数量-最小商品规格库存
     *
     * @param map -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/13 10:58
     */
    @Select("select c.num from " +
            " ( " +
            "select min(id) as id,attr_id,product_id " +
            "from lkt_group_product " +
            "where store_id = #{store_id} " +
            "and product_id=#{product_id}" +
            ") as m " +
            "left join lkt_configure as c on m.attr_id=c.id"
    )
    Integer getGroupProductNum(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取拼团等级参数
     *
     * @param storeId -
     * @param goodsId -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/18 14:33
     */
    @Select("select group_level from lkt_group_product " +
            " where product_id = #{goodsId} and store_id = #{storeId} and is_delete = 0 " +
            " and g_status != 1 order by id desc LIMIT 1 ")
    Map<String, Object> getGroupProductGroupLevel(int storeId, int goodsId) throws LaiKeAPIException;


    /**
     * 拼团首页数据
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 12:08
     */
    List<Map<String, Object>> getGroupProductIndex(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取拼团商品数据
     *
     * @param storeId    -
     * @param goodsId    -
     * @param activityId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/20 17:43
     */

    Map<String, Object> getGroupGoodsInfo(@Param("storeId") int storeId, @Param("goodsId") int goodsId, @Param("activityId") int activityId) throws LaiKeAPIException;


    /**
     * 获取正在活动中的拼团商品
     *
     * @param storeId   -
     * @param pageStart -
     * @param pageEnd   -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 15:42
     */
    List<Map<String, Object>> getGroupActivityGoodsInfo(@Param("storeId") int storeId, @Param("pageStart") int pageStart, @Param("pageEnd") int pageEnd) throws LaiKeAPIException;


    /**
     * 后台 获取正在活动中的拼团商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 14:30
     */
    List<Map<String, Object>> getGroupActivityGoodsInfo1(Map<String, Object> map) throws LaiKeAPIException;

    List<Map<String, Object>> countGroupActivityGoodsInfo1(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 增加拼团活动销量
     *
     * @param storeId    -
     * @param activityNo -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/4/1 17:00
     */
    @Update("update lkt_group_product set sales_volume = sales_volume + 1 where store_id = #{storeId}  and activity_no = #{activityNo}")
    int addVolume(int storeId, int activityNo) throws LaiKeAPIException;


    /**
     * 结束拼团
     *
     * @param storeId    -
     * @param activityNo -
     * @return int
     * @author Trick
     * @date 2021/4/7 9:53
     */
    @Update("update lkt_group_product set g_status=2 where g_status=1 and is_delete = 0  and activity_no = #{activityNo} and store_id=#{storeId}")
    int closeGroupProduct(int storeId, int activityNo);


    /**
     * 打开未过期的活动
     *
     * @param storeId    -
     * @param activityNo -
     * @param sysDate    -
     * @return int
     * @author Trick
     * @date 2021/4/7 9:58
     */
    @Update("update lkt_group_product set g_status=2 where g_status=1 and is_delete = 0 " +
            "and store_id=#{storeId} and endtime>#{sysDate} and starttime<=#{sysDate}")
    int startGroupProduct(int storeId, Date sysDate);


    /**
     * 获取拼团商品信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 9:37
     */
    List<Map<String, Object>> getGroupGoodsList(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 查询老活动的信息
     *
     * @param storeId    -
     * @param goodsId    -
     * @param activityNo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 11:10
     */
    @Select("select * from lkt_group_product where product_id = #{goodsId} and store_id = #{storeId}   " +
            "and starttime != '0000-00-00 00:00:00' and starttime != '0000-00-00 00:00:00' " +
            "and activity_no != #{activityNo} and is_delete = 0 and (g_status = 1 || g_status = 2) GROUP by activity_no")
    List<Map<String, Object>> getGroupOldGoodsList(int storeId, int goodsId, int activityNo) throws LaiKeAPIException;


    /**
     * 是否有重复名称
     *
     * @param storeId    -
     * @param goodsName  -
     * @param activityNo -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/10 11:10
     */
    @Select("select count(distinct activity_no) from lkt_group_product " +
            " where store_id = #{storeId} and activity_no != #{activityNo} and is_delete = 0 and (g_status = 1 or g_status = 2) " +
            " and group_title = #{goodsName}")
    Integer isGroupOldGoodsName(int storeId, String goodsName, int activityNo) throws LaiKeAPIException;

    /**
     * 获取最新活动id
     *
     * @return int
     */
    @Select("SELECT max(activity_no) as activity_no FROM lkt_group_product")
    int getMaxActivityNo();


    /**
     * 修改拼团活动信息
     *
     * @param groupProductModel -
     * @return int
     * @author Trick
     * @date 2021/5/10 16:34
     */
    @Update("update lkt_group_product set g_status=#{g_status}  ,is_show = 1,group_data = #{group_data} where product_id=#{product_id} and store_id=#{store_id} " +
            " and is_delete = 0 and activity_no = #{activity_no}")
    int updateGroup(GroupProductModel groupProductModel);


    /**
     * 活动是否显示开关
     *
     * @param groupProductModel -
     * @return int
     * @author Trick
     * @date 2021/5/10 17:26
     */
    @Update("update lkt_group_product set is_show=#{is_show} where product_id=#{product_id} and store_id=#{store_id} and activity_no = #{activity_no}")
    int groupShowSwitch(GroupProductModel groupProductModel);


    /**
     * 删除拼团活动
     *
     * @param storeId        -
     * @param activityNoList -
     * @return int
     * @author Trick
     * @date 2021/5/10 17:38
     */
    @Update("<script>" +
            "update lkt_group_product set is_delete=1 where " +
            "store_id=#{storeId} " +
            " <if test='activityNoList != null '> " +
            "   <foreach collection=\"activityNoList\" item=\"activityNo\" separator=\",\" open=\"and activity_no in(\" close=\")\"> " +
            "        #{activityNo,jdbcType=INTEGER}" +
            "   </foreach> " +
            "</if> " +
            "</script>")
    int delGroup(int storeId, List<String> activityNoList);

    /**
     * 后台 获取正在活动中的拼团商品
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/8 14:30
     */
    List<Map<String, Object>> getGroupActivityGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

    int countGroupActivityGoodsInfo(Map<String, Object> map) throws LaiKeAPIException;

}