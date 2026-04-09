package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.auction.AuctionSpecialModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 竞拍专场
 *
 * @author Trick
 * @date 2022/7/4 11:10
 */
public interface AuctionSpecialModelMapper extends BaseMapper<AuctionSpecialModel>
{

    @Override
    List<Map<String, Object>> selectDynamic(Map<String, Object> map) throws LaiKeAPIException;

    @Override
    int countDynamic(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取竞拍专场列表
     *
     * @author Trick
     * @date 2022/7/4 11:15
     */
    List<Map<String, Object>> selectAuctionSpecialList(Map<String, Object> map);

    int countAuctionSpecialList(Map<String, Object> map);

    //获取专场下拉
    @Select("select id,name,start_date,end_date,mark_up_amt,unit,number from lkt_auction_special a where a.recovery=0 and type=#{type} and a.store_id=#{storeId} and a.end_date>=#{sysDate} ")
    List<Map<String, Object>> getSpecials(int storeId, int type, Date sysDate);

    /**
     * 获取已经参与竞拍的商品列表
     *
     * @param map -
     * @return List
     * @author Trick
     * @date 2022/7/4 17:47
     */
    List<Map<String, Object>> selectAuctionGoodsList(Map<String, Object> map);

    int countAuctionGoodsList(Map<String, Object> map);


    //获取专场下所有商品
    String selectSpecialGoodsListSQL = " from lkt_auction_special a,lkt_auction_session b,lkt_auction_product c,lkt_configure d inner join lkt_product_list e on e.id=d.pid where a.id=#{specialId} and a.id=b.special_id and b.id=c.session_id and a.is_show=1 and a.is_show=b.is_show and a.is_show=c.is_show and c.attr_id=d.id and a.recovery=0 and a.recovery=b.recovery and a.recovery=c.recovery and a.recovery = e.recycle ";


    /**
     * 获取专场下所有商品
     */
    @Select("select c.id,d.id attrId,d.attribute,d.img,e.product_title,c.price,c.mark_up_amt,c.starting_amt,b.status sessionStatus,c.starting_amt,d.price goodsPrice " + selectSpecialGoodsListSQL + " limit #{start},#{end}")
    List<Map<String, Object>> selectSpecialGoodsList(String specialId, int start, int end);

    @Select("select c.id,d.id attrId,d.attribute,d.img,e.product_title,c.price,c.mark_up_amt,c.starting_amt,b.status sessionStatus,c.starting_amt " + selectSpecialGoodsListSQL + " order by c.price ${sort}  limit #{start},#{end}")
    List<Map<String, Object>> selectSpecialGoodsList1(String specialId, String sort, int start, int end);

    @Select("select count(1) " + selectSpecialGoodsListSQL)
    int countSpecialGoodsList(String specialId);

    @Select("select c.id,d.id attrId,d.attribute,d.img,e.product_title,c.price,c.mark_up_amt,c.starting_amt,b.status sessionStatus,d.price goodsPrice " + selectSpecialGoodsListSQL + " and e.mch_id=#{mchId} limit #{start},#{end}")
    List<Map<String, Object>> selectSpecialMyGoodsList(String specialId, Integer mchId, int start, int end);

    @Select("select count(1) " + selectSpecialGoodsListSQL + " and e.mch_id=#{mchId}")
    int countSpecialMyGoodsList(String specialId, Integer mchId);


    //获取专场有多少商品
    @Select("select ifnull(count(distinct c.id),0) from lkt_auction_special a,lkt_auction_session b,lkt_auction_product c where a.id=#{specialId} and a.id=b.special_id and b.id=c.session_id and a.recovery=0 and a.recovery=b.recovery and a.recovery=c.recovery ")
    int sumSpecialGoodsNum(String specialId);

    /**
     * 获取专场总成交价
     */
    @Select("select IFNULL(sum(x.price),0) from (select ifnull(max(d.price),0) price  from lkt_auction_special a,lkt_auction_session b,lkt_auction_product c,lkt_auction_record d " +
            "where a.id=#{specialId} and a.id=b.special_id and b.id=c.session_id and c.id=d.auction_id and a.recovery=0 and a.recovery=b.recovery and a.recovery=c.recovery and c.status=2 " +
            " group by d.auction_id) x")
    String getSpecialDealAmt(String specialId);


    /**
     * 获取专场下所有场次
     */
    @Select("select b.id sessionId,b.name,b.status,DATE_FORMAT(b.end_date,'%Y-%m-%d %H:%i:%s') end_date,DATE_FORMAT(b.start_date,'%Y-%m-%d %H:%i:%s') start_date from lkt_auction_special a,lkt_auction_session b where a.store_id=#{storeId} and a.id =#{specialId} and a.id=b.special_id and a.recovery=0 and a.recovery=b.recovery and b.is_show=1")
    List<Map<String, Object>> selectSessionListBySpecial(Integer storeId, String specialId);

    /**
     * 获取店铺专场明细
     */
    @Select("select a.name,a.start_date,a.end_date,a.commission,a.img specialImg,a.content,e.product_title goodsName,d.attribute,c.starting_amt,c.mark_up_amt,c.price from lkt_auction_special a,lkt_auction_session b,lkt_auction_product c,lkt_configure d,lkt_product_list e " +
            "where a.id=#{specialId} and a.id=b.special_id and b.id=c.session_id and c.attr_id = d.id and d.pid=e.id and a.recovery=0 and a.recovery=b.recovery and a.recovery=c.recovery and a.recovery=d.recycle and a.recovery=e.recycle ")
    List<Map<String, Object>> selectSpecialGoodsInfo(String specialId);


    int getNumByStatus(@Param("mchId") Integer mchId,@Param("status") Integer status);

    /**
     * 专场报名动态sql
     */
    List<Map<String, Object>> selectSpecialSignList(Map<String, Object> map);

    int countSpecialSign(Map<String, Object> map);

    //围观专场
    @Update("update lkt_auction_special a set a.look_count=a.look_count+1 where id=#{specialId} and status!=3 ")
    int lookSpecial(String specialId);

    //删除过期的专场
    @Update("update lkt_auction_special a,lkt_auction_session b set a.recovery=1 where a.id=b.special_id and #{sysdate}>= DATE_ADD(a.end_date,INTERVAL #{day} day ) and a.status=3 and a.recovery=0 ")
    int delSpecialInvalidate(Date sysdate, int day);


    //根据竞拍id获取店铺信息
    @Select("select b.id mchId,b.account_money money from lkt_auction_special a inner join lkt_mch b on a.mch_id=b.id where a.id=#{specialId}")
    Map<String, Object> getMchInfoBySpecialId(String specialId);

    /**
     * 获取4个热门专场列表
     * @param storeId
     * @return
     */
    @Select("select * from lkt_auction_special where store_id = #{storeId} and status = 2 order by sort desc,start_date desc limit 4")
    List<Map<String, Object>> getHotSpecialList(int storeId);

}