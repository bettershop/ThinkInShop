package com.laiketui.apps.products.services;

import com.laiketui.apps.api.products.AppsCommentService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.CommentsImgModelMapper;
import com.laiketui.common.mapper.CommentsModelMapper;
import com.laiketui.common.mapper.ReplyCommentsModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.product.CommentsImgModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.product.ReplyCommentsModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 评论
 *
 * @author Trick
 * @date 2023/3/6 11:02
 */
@Service
public class AppsCommentServiceImpl implements AppsCommentService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private CommentsImgModelMapper commentsImgModelMapper;

    @Autowired
    private ReplyCommentsModelMapper replyCommentsModelMapper;

    @Override
    public Map<String, Object> getComment(MainVo vo, int goodsId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        //评论数量
        int commentsTotal = 0;
        //好评数量
        int commentsHao = 0;
        //中评数量
        int commentsZhong = 0;
        //差评数量
        int commentsCha = 0;
        //评论图片数量
        int commentsImage = 0;
        try
        {
            //根据条件获取评论信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", goodsId);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("group_commentId", "group_commentId");
            //类型(0=全部,1=好评,2=中评,3=差评,4=有图)
            parmaMap.put("type", type);
            //只显示商品的主评论
            parmaMap.put("goodsCommentList", "goodsCommentList");
            List<Map<String, Object>> commentsMap = publiceService.getGoodsCommentList(parmaMap);
            for (Map<String, Object> map : commentsMap)
            {
                int commentId = MapUtils.getIntValue(map, "id");
                //评论回复数量
                Map<String, Object> replyMap = new HashMap<>(16);
                replyMap.put("commentId", commentId);
                replyMap.put("sid_null", "sid_null");
                replyMap.put("type", ReplyCommentsModel.Type.USER);
                map.put("replyNum", replyCommentsModelMapper.countDynamic(replyMap));
            }

            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", goodsId);
            commentsTotal = commentsModelMapper.countCommentsUserDynamicByPid(parmaMap);

            Map<String, Object> haoPinMap = new HashMap<>(16);
            haoPinMap.putAll(parmaMap);
            haoPinMap.put("type", 1);
            commentsHao = commentsModelMapper.countCommentsUserDynamicByPid(haoPinMap);

            Map<String, Object> zhongPinMap = new HashMap<>(16);
            zhongPinMap.putAll(parmaMap);
            zhongPinMap.put("type", 2);
            commentsZhong = commentsModelMapper.countCommentsUserDynamicByPid(zhongPinMap);

            Map<String, Object> chaPinMap = new HashMap<>(16);
            chaPinMap.putAll(parmaMap);
            chaPinMap.put("type", 3);
            commentsCha = commentsModelMapper.countCommentsUserDynamicByPid(chaPinMap);

            Map<String, Object> imageMap = new HashMap<>(16);
            imageMap.putAll(parmaMap);
            imageMap.put("type", 4);
            commentsImage = commentsModelMapper.countCommentsUserDynamicByPid(imageMap);

            resultMap.put("data", commentsMap);
            resultMap.put("comments_total", commentsTotal);
            resultMap.put("comments_hao", commentsHao);
            resultMap.put("comments_zhong", commentsZhong);
            resultMap.put("comments_cha", commentsCha);
            resultMap.put("comments_image", commentsImage);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载评论异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getComment");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getCommentDetail(MainVo vo, int commentId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        try
        {
            Map<String, Object> detailInfo = commentsModelMapper.getCommentsDetailByCid(commentId);
            if (detailInfo == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "数据不存在");
            }
            detailInfo.put("imgurl", publiceService.getImgPath(MapUtils.getString(detailInfo, "imgurl"), vo.getStoreId()));
            detailInfo.put("addTime", DateUtil.dateFormate(MapUtils.getString(detailInfo, "add_time"), GloabConst.TimePattern.YMD));
            //获取评论图片
            List<String> commentImgList = commentsImgModelMapper.getCommentsImagesByType(commentId, CommentsImgModel.Type.COMMENT);
            for (int i = 0; i < commentImgList.size(); i++)
            {
                commentImgList.set(i, publiceService.getImgPath(commentImgList.get(i), vo.getStoreId()));
            }
            //获取追平图片
            List<String> replyImgList = commentsImgModelMapper.getCommentsImagesByType(commentId, CommentsImgModel.Type.REVIEW);
            for (int i = 0; i < replyImgList.size(); i++)
            {
                replyImgList.set(i, publiceService.getImgPath(replyImgList.get(i), vo.getStoreId()));
            }
            //查询该条评论的商家回复
            String replyAdmin = replyCommentsModelMapper.getMchReplyInfo(commentId, MapUtils.getIntValue(detailInfo, "mch_id"));
            if (StringUtils.isEmpty(replyAdmin))
            {
                replyAdmin = "";
            }
            detailInfo.put("replyAdmin", replyAdmin);
            //追平时间
            String reviewTime = "";
            long   reviewDay  = 0;
            if (detailInfo.containsKey("review_time"))
            {
                reviewTime = DateUtil.dateFormate(detailInfo.get("review_time").toString(), GloabConst.TimePattern.YMDHMS);
                String arriveTime = MapUtils.getString(detailInfo, "arrive_time");
                if (StringUtils.isNotEmpty(arriveTime))
                {
                    Date reviewEndDate  = DateUtil.dateFormateToDate(reviewTime, GloabConst.TimePattern.YMD);
                    Date reviewStartEnd = DateUtil.dateFormateToDate(arriveTime, GloabConst.TimePattern.YMD);
                    reviewDay = DateUtil.dateConversion(reviewEndDate.getTime() / 1000, reviewStartEnd.getTime() / 1000, DateUtil.TimeType.DAY);
                }
            }
            detailInfo.put("size", MapUtils.getString(detailInfo, "size"));
            detailInfo.put("reviewDay", reviewDay);
            detailInfo.put("replyImgList", replyImgList);
            detailInfo.put("review", MapUtils.getString(detailInfo, "review"));
            //是否匿名处理
            if (MapUtils.getInteger(detailInfo, "anonymous") == 1)
            {
                detailInfo.put("user_name", "匿名");
            }
            resultMap.put("detailInfo", detailInfo);
            resultMap.put("commentImgList", commentImgList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCommentDetail");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getCommentReplyList(MainVo vo, int commentId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        try
        {
            Map<String, Object> parmaMap = new Hashtable<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", commentId);
            parmaMap.put("type", ReplyCommentsModel.Type.USER);
            parmaMap.put("sid_null", "sid_null");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = replyCommentsModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = replyCommentsModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
                    int                 id          = MapUtils.getIntValue(map, "id");
                    Map<String, Object> childrenMap = new Hashtable<>(16);
                    childrenMap.put("sid", id);
                    childrenMap.put("commentId", commentId);
                    childrenMap.put("type", ReplyCommentsModel.Type.USER);
                    childrenMap.put("add_time_sort", DataUtils.Sort.ASC.toString());
                    int count = replyCommentsModelMapper.countDynamic(childrenMap);
                    if (count > 0)
                    {
                        List<Map<String, Object>> childrenList = replyCommentsModelMapper.selectDynamic(childrenMap);
                        childrenList.forEach(children ->
                        {
                            children.put("dateTime", DateUtil.dateFormate(MapUtils.getString(children, "add_time"), GloabConst.TimePattern.YMD));
                        });
                        map.put("children", childrenList);
                    }

                    map.put("addTime", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMD));
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情回复列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCommentReplyList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendComment(MainVo vo, int commentId, Integer sid, String text) throws LaiKeAPIException
    {
        try
        {
            User user;
            if (DictionaryConst.StoreSource.LKT_LY_006.equals(vo.getStoreType() + ""))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_PC_SHOP_TOKEN, true);
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            if (StringUtils.isEmpty(text))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBNWK, "发表内容不能为空");
            }
            //获取评论信息
            CommentsModel commentsOld = commentsModelMapper.selectByPrimaryKey(commentId);
            if (commentsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "评论已被删除");
            }
            //保存回复记录
            ReplyCommentsModel replyCommentsSave = new ReplyCommentsModel();
            replyCommentsSave.setUid(user.getUser_id());
            replyCommentsSave.setContent(text);
            replyCommentsSave.setType(ReplyCommentsModel.Type.USER);
            replyCommentsSave.setCid(commentId + "");
            replyCommentsSave.setStore_id(vo.getStoreId());
            if (sid != null && sid > 0)
            {
                //获取之前评论信息
                ReplyCommentsModel replyCommentsOld = replyCommentsModelMapper.selectByPrimaryKey(sid);
                if (replyCommentsOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "之前的评论已被删除");
                }
                replyCommentsSave.setTo_uid(replyCommentsOld.getUid());
                replyCommentsSave.setSid(replyCommentsOld.getId());
            }
            replyCommentsSave.setAdd_time(new Date());
            int row = replyCommentsModelMapper.insertSelective(replyCommentsSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "发表失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发表回复 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sendComment");
        }
    }


}
