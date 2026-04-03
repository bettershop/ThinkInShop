package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.seckill.SecondsActivityModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动/商品表
 *
 * @author Trick
 * @date 2021/11/17 14:40
 */
@Deprecated
public interface SecondsActivityModelMapper extends BaseMapper<SecondsActivityModel>
{

    /**
     * 增减库存
     *
     * @param acId -
     * @param num  -
     * @return int
     * @author Trick
     * @date 2021/4/12 15:09
     */
    @Update("update lkt_seconds_activity set num = num + #{num} where id=#{acId} and is_delete  = 0")
    int addStockNum(int acId, int num);

    /**
     * 扣减库存（防超卖）
     *
     * @param acId 活动id
     * @param num  扣减数量（正数）
     * @return 影响行数
     */
    @Update("update lkt_seconds_activity set num = num - #{num} " +
            "where id = #{acId} and is_delete = 0 and num >= #{num}")
    int deductStockNum(@Param("acId") int acId, @Param("num") int num);

    //获取已结束的秒杀活动id
    @Select("select id  from lkt_seconds_activity where endtime <= #{sysDate} and is_delete=0 and status != 3")
    List<Integer> getSecondsEndStatusId(Date sysDate);

    //状态修改---已结束
    @Update("update lkt_seconds_activity  set status=3,isshow=0,is_delete=1,delete_method=2 where id = #{id}")
    int secondsEndStatusById(Integer id);

    //状态修改---已结束
    @Update("update lkt_seconds_activity as a set a.`status`=3,a.isshow=0 where a.endtime <= #{sysDate} and a.is_delete=0")
    int secondsEndStatus(Date sysDate);

    //状态修改---未开始
    @Update("update lkt_seconds_activity a set status=1 where a.starttime > #{sysDate} and a.endtime > #{sysDate} and is_delete=0 ")
    int secondsNotOpenStatus(Date sysDate);

    //状态修改---进行中
    @Update("update lkt_seconds_activity a set status=2 where a.starttime < #{sysDate} and a.endtime > #{sysDate} and status!=4 and is_delete=0 ")
    int secondsOpenStatus(Date sysDate);

    //状态修改---已抢光
    @Update("update lkt_seconds_activity a set status=5 where a.num<1 and is_delete=0 ")
    int secondsTootAllStatus();

    //状态修改---预告
    @Update("update lkt_seconds_activity a,lkt_seconds_label b set status=4 where a.label_id=b.id and b.recovery=0 and b.mch_id=#{mchId} " +
            " and a.starttime <=#{heraldDate} and a.starttime > #{sysDate} and a.endtime > #{sysDate} and a.is_delete=0")
    int secondsHeraldStatus(Integer mchId, Date heraldDate, Date sysDate);

    /**
     * 获取秒杀预告
     *
     * @param heraldDate - 预告时间
     * @param sysDate    - 系统时间
     * @return Map
     * @author Trick
     * @date 2021/4/13 17:18
     */
    @Select("SELECT a.*,c.product_title FROM lkt_seconds_activity a LEFT JOIN lkt_product_list c ON a.goodsId = c.id " +
            " inner join lkt_seconds_label lab on a.label_id=lab.id and lab.recovery=0 and lab.mch_id=#{mchId} " +
            " where a.starttime <= #{heraldDate} " +
            " and a.starttime < #{sysDate} " +
            " and a.endtime > #{sysDate} " +
            " and a.isNotice = 0 ")
    List<Map<String, Object>> getSecondsByHerald(Integer mchId, Date heraldDate, Date sysDate);

    /**
     * 获取全商城秒杀预告
     *
     * @param heraldDate - 预告时间
     * @param sysDate    - 系统时间
     * @return Map
     * @author Trick
     * @date 2021/4/13 17:18
     */
    @Select("SELECT a.*,c.product_title FROM lkt_seconds_activity a LEFT JOIN lkt_product_list c ON a.goodsId = c.id " +
            " inner join lkt_seconds_label lab on a.label_id=lab.id and lab.recovery=0 and lab.store_id=#{storeId} " +
            " where a.starttime <= #{heraldDate} " +
            " and a.endtime > #{sysDate} " +
            " and a.store_id = #{storeId} " +
            " and a.is_delete = 0 " +
            " and a.isNotice = 0 ")
    List<Map<String, Object>> getSecondsByStoreIdAndHeraldDate(Integer storeId, Date heraldDate, Date sysDate);

    /**
     * 获取商品规格信息-后台添加商品规格弹窗
     *
     * @param goodsId -
     * @param acId    -
     * @return List
     * @author Trick
     * @date 2021/11/17 19:59
     */
    @Select(" select a.id,a.pid goodsId,a.attribute,a.num,a.price,IFNULL(c.num,0) secNum,IFNULL(c.max_num,0) secMaxNum from lkt_configure a " +
            " left join lkt_seconds_activity b on b.id = #{acId} and b.is_delete=0 " +
            " left JOIN lkt_seconds_pro c on b.id=c.activity_id and c.is_delete=0 and c.attr_id=a.id and c.num>0 " +
            " where a.recycle=0 " +
            " and a.pid=#{goodsId} ")
    List<Map<String, Object>> getSecondsAttrList(int goodsId, int acId);


    /**
     * 获取秒杀活动所属店铺
     *
     * @param acId -
     * @return Integer
     * @author Trick
     * @date 2022/1/17 19:38
     */
    @Select("select distinct b.mch_id from lkt_seconds_activity a,lkt_seconds_label b where a.id =#{acId} and a.label_id=b.id ")
    Integer getSecMchId(Integer acId);

    @Select("select * from lkt_seconds_activity a inner JOIN lkt_seconds_pro c on a.id=c.activity_id and c.is_delete=0 and c.num >0\n" +
            "where a.is_delete=0  and a.status=#{status}")
    List<SecondsActivityModel> secondsActivityList(int storeId, int status);

    /**
     * 统计秒杀商品
     * @param parmaMap
     * @return
     */
    int countSecondsGoodsListByLabelId(Map<String, Object> parmaMap);

    /**
     * 获取秒杀商品
     * @param map
     * @return
     */
    List<Map<String, Object>> getSecondsGoodsListByLabelId(Map<String, Object> map);
}
