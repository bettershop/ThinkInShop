package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.living.LivingProductModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author zhuqingyu
 * @create 2024/5/31
 */
public interface LivingProductModelMapper extends BaseMapper<LivingProductModel>
{
    /**
     * 根据直播间ID查询产品信息
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> selectProByRooId(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 根据直播间ID查询产品信息-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    int countProByRooId(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商品池
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getGoodsPool(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 主播中心-商品管理
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> getLivingGoodById(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 查询直播间商品-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> countLivingGoodById(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 商品池-计数
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> countGoodsPool(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 移动店铺端查询产品有没有设置
     *
     * @param pid
     * @return
     */
    @Select("select count(1) from lkt_product_list a left join lkt_configure b on a.id=b.pid WHERE a.id=#{pid}  and b.commission > 0;")
    int countIsSetUp(Integer pid) throws LaiKeAPIException;

    /**
     * 查询是否设置了直播间商品
     *
     * @param pid
     * @param roomId
     * @return
     */
    @Select("select count(1) from lkt_living_product a left join lkt_configure b on a.id=b.pid WHERE a.pro_id=#{pid}  and b.commission > 0 and a.living_id=#{roomId};")
    int countIsSetUpByRoomId(Integer pid, String roomId) throws LaiKeAPIException;

    /**
     * 根据直播间ID置顶
     *
     * @param living_id
     * @param sort
     * @return
     */
    @Update("update lkt_living_product set sort = 1 where living_id = #{living_id}")
    int updateSortByLivingId(Integer living_id, Integer sort) throws LaiKeAPIException;

    /**
     * 根据商品和直播间ID置顶
     *
     * @param living_id
     * @param sort
     * @param pId
     * @return
     */
    @Update("update lkt_living_product set sort = #{sort} where living_id = #{living_id} and pro_id =#{pId} ")
    int updateSortByLivingIdAndPid(Integer living_id, Integer sort, Integer pId) throws LaiKeAPIException;

    /**
     * 查询销量信息
     *
     * @param roomId
     * @param pId
     * @return
     */
    @Select("select IFNULL(sum(num),0) as num,IFNULL(sum(xl_num),0) as xl_num  from lkt_living_product where living_id = #{roomId} and pro_id =#{pId} and recycle=0")
    Map<String, Object> queryLivingProNum(String roomId, Integer pId) throws LaiKeAPIException;


    /**
     * 查询这个规格下的所有销量
     *
     * @param pId
     * @return
     */
    @Select("select IFNULL(sum(num),0) as num,IFNULL(sum(xl_num),0) as xl_num from lkt_living_product where pro_id =#{pId} and recycle=0")
    Map<String, Object> queryAllLivingProNum(Integer pId) throws LaiKeAPIException;

    /**
     * 修改商品为不讲解
     *
     * @param roomId
     * @param store_id
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_living_product set represent=0 where store_id=#{store_id} and living_id = #{roomId}")
    int updateProByRoomId(Integer roomId, Integer store_id) throws LaiKeAPIException;

    /**
     * 修改商品为讲解中
     *
     * @param roomId
     * @param store_id
     * @param proId
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_living_product set represent=1 where store_id=#{store_id} and living_id = #{roomId} and pro_id =#{proId}")
    int updateProExplainByRoomId(Integer roomId, Integer store_id, Integer proId) throws LaiKeAPIException;

    /**
     * 查询直播间的商品
     *
     * @param map
     * @return
     * @throws LaiKeAPIException
     */
    List<Map<String, Object>> queryLivingPro(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 根据直播间ID查询主播头像和用户名
     *
     * @param roomId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select ifnull(u.headimgurl,'') as live_img,u.user_name from lkt_living_room r left join lkt_user u on u.user_id = r.user_id where r.id = #{roomId}")
    Map<String, Object> selectLivingById(String roomId) throws LaiKeAPIException;

    /**
     * 获取直播间商品排序的最大值
     *
     * @param roomId
     * @return
     */
    @Select("select max(sort_num) from lkt_living_product where living_id =#{roomId} and recycle=0")
    int getProSortNumByRoomId(Integer roomId);

    /**
     * 修改产品的序号值
     *
     * @param proId
     * @param roomId
     * @param sortNum
     * @return
     */
    @Update("update lkt_living_product set sort_num = #{sortNum} where living_id=#{roomId} and pro_id = #{proId}")
    int updateSortByProAndRoomId(Integer proId, Integer roomId, Integer sortNum);

    /**
     * 获取设置过分佣比例的产品
     *
     * @param mchId
     * @return
     * @throws LaiKeAPIException
     */
    @Select("select b.* from lkt_product_list a left join lkt_configure b on a.id = b.pid where a.mch_id=#{mchId} and ifnull(b.commission,'') != '' ")
    List<ConfiGureModel> getConfigureByMchId(String mchId) throws LaiKeAPIException;
}
