package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.domain.auction.AuctionRemindModel;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 竞拍专场提醒表
 *
 * @author Trick
 * @date 2022/7/22 15:08
 */
public interface AuctionRemindModelMapper extends BaseMapper<AuctionRemindModel>
{

    //获取场次设置提醒人数
    @Select("select count(x.id) from lkt_auction_remind x where x.special_id=#{specialId} ")
    int getSpecialFollowNum(String specialId);


    //当前用户是否设置了提醒
    @Select("select count(x.id) from lkt_auction_remind x where x.special_id=#{specialId} and x.user_id=#{userId} ")
    int isSpecialRemindByUserId(String specialId, String userId);

    //获取关注某专场用户id集
    @Select("select user_id from lkt_auction_remind a,lkt_auction_special b where a.special_id=#{specialId} and a.special_id=b.id and b.status=1 and DATE_ADD(b.start_date,INTERVAL #{minute} minute) <=#{sysDate} and a.is_remind=0 ")
    List<String> getRemindUserIdBySpecialId(String specialId, Date sysDate, int minute);

    //标记已推送
    @Update("update lkt_auction_remind a set a.is_remind=1 where a.special_id=#{specialId} and a.is_remind=0")
    int specialRemind(String specialId);
}