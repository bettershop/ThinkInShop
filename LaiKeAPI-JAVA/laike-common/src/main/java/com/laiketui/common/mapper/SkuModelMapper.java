package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.SkuModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * 属性 sql
 *
 * @author Trick
 * @date 2020/11/12 17:00
 */
public interface SkuModelMapper extends BaseMapper<SkuModel>
{


    /**
     * 获取属性名称
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/12 17:01
     */
    List<Map<String, Object>> getAttributeDynamic(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取属性+值
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2021/5/31 19:19
     */
    List<Map<String, Object>> getAttributeDynamicAll(Map<String, Object> map);

    @Select("<script>" +
            "select a.id,b.id sid,a.name,b.name sname,false as status from lkt_sku a,lkt_sku b where a.id=b.sid and a.status=1 and a.recycle=0 and a.is_examine = 1 " +
            "and b.status=1 and b.recycle=0 and b.is_examine = 1 " +
            "<if test='keyword != null '> " +
            " and( a.name like concat('%',#{keyword},'%') or b.name like concat('%',#{keyword},'%') )" +
            "</if> " +
            "<if test='lang_code != null '> " +
            " and a.lang_code = #{lang_code} " +
            "</if> " +
            "order by a.add_date limit #{pageStart},#{pageEnd}" +
            "</script>")
    List<Map<String, Object>> getSkuList(int pageStart, int pageEnd, String keyword, String lang_code);

    @Select("<script>" +
            "select count(1) from lkt_sku a,lkt_sku b where a.id=b.sid and a.status=1 and a.recycle=0 and a.is_examine = 1 " +
            "and b.status=1 and b.recycle=0 and b.is_examine = 1 " +
            "<if test='keyword != null'> " +
            " and( a.name like concat('%',#{keyword},'%') or b.name like concat('%',#{keyword},'%') )" +
            "</if> " +
            "<if test='lang_code != null'> " +
            " and a.lang_code = #{lang_code} " +
            "</if> " +
            "order by a.add_date" +
            "</script>")
    Integer countSkuList(String keyword, String lang_code);


    /**
     * 获取code
     *
     * @param sid -
     * @return Integer
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/21 14:08
     */
    @Select("select code from lkt_sku where recycle = 0 and status = 1 and sid = #{sid} order by id desc limit 1")
    String getAttributeByCode(int sid) throws LaiKeAPIException;


    /**
     * 获取属性动态sql
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/3 9:51
     */
    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取属性动态sql-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/3 9:51
     */
    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    //属性是否使用中
    @Select("select count(1) from lkt_configure where attribute like concat('%','_${skuId}\";','%') and recycle=0")
    int countSkuIsUse(Integer skuId);


    @Select("<script>" +
            "select a.id,b.id sid,a.name,b.name sname,'true' as status,a.name+':'+b.name as label from lkt_sku a,lkt_sku b where a.id=b.sid and a.status=1 and a.recycle=0 and a.is_examine = 1 " +
            "and b.status=1 and b.recycle=0 and b.is_examine = 1 " +
            "and b.id = #{id} " +
            "</script>")
    Map<String, Object> getSkuById(String id);

    @Select("select id from lkt_sku where  sid = #{id}")
    List<Integer> getChildSkuList(Integer id);
}