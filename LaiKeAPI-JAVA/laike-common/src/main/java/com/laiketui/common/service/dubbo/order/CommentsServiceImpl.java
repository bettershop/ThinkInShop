package com.laiketui.common.service.dubbo.order;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.CommentsService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.CommentsImgModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.product.ReplyCommentsModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.CommentsInfoVo;
import com.laiketui.domain.vo.order.UpdateCommentsInfoVo;
import com.laiketui.domain.vo.query.GetCommentsDetailInfoVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 评论管理
 *
 * @author Trick
 * @date 2021/1/6 15:56
 */
@Service
public class CommentsServiceImpl implements CommentsService
{
    private final Logger logger = LoggerFactory.getLogger(CommentsServiceImpl.class);

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Override
    public Map<String, Object> getCommentsInfo(CommentsInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = publiceService.getRedisUserCache(vo);
            if (vo.getCid() != null && vo.getCid() > 0)
            {
                return getDetail(vo, vo.getCid());
            }
            AdminModel adminModel;
            try
            {
                adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            }
            catch (Exception l)
            {
                adminModel = new AdminModel();
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mchId", user.getMchId());
            parmaMap.put("status", 1);
            parmaMap.put("type", vo.getType());
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (StringUtils.isNotEmpty(vo.getMchName()))
            {
                parmaMap.put("mchName", vo.getMchName());
            }
            //订单类型
            if (StringUtils.isEmpty(vo.getOrderType()))
            {
                parmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            }
            else
            {
                parmaMap.put("orderType", vo.getOrderType());
            }
            parmaMap.put("keyword", vo.getOrderno());
            int count = commentsModelMapper.countCommentsOrderDynamic(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> commentsList = commentsModelMapper.getCommentsOrderDynamic(parmaMap);
            for (Map<String, Object> map : commentsList)
            {
                int          commentId    = MapUtils.getIntValue(map, "id");
                Integer      mchId        = MapUtils.getInteger(map, "mch_id");
                List<String> imgUrls      = commentsImgModelMapper.getCommentsImages(commentId);
                List<String> resultImgUrl = new ArrayList<>();
                for (String img : imgUrls)
                {
                    resultImgUrl.add(publiceService.getImgPath(img, vo.getStoreId()));
                }
                map.put("size", MapUtils.getString(map, "attrSize"));
                map.put("replyText", replyCommentsModelMapper.getMchReplyInfo(commentId, mchId));
                map.put("commentImgList", resultImgUrl);
                //自己才能回复自己的店铺的评论
                map.put("isMain", mchId.equals(adminModel.getShop_id()));
            }

            resultMap.put("total", count);
            resultMap.put("list", commentsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }

    private Map<String, Object> getDetail(MainVo vo, int cid)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", cid);
            //类型(0=全部,1=好评,2=中评,3=差评,4=有图)
            parmaMap.put("type", 0);
            List<Map<String, Object>> commentsMap = publiceService.getGoodsCommentList(parmaMap);

            resultMap.put("list", commentsMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getCommentReplyList(GetCommentsDetailInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        try
        {
            Map<String, Object> parmaMap = new Hashtable<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", vo.getCid());
            parmaMap.put("type", ReplyCommentsModel.Type.USER);
            parmaMap.put("sid_null", "sid_null");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            if (StringUtils.isNotEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            if (vo.getStartDate() != null)
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (vo.getEndDate() != null)
            {
                parmaMap.put("endDate", vo.getEndDate());
            }

            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = replyCommentsModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = replyCommentsModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
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
    public void delCommentReply(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            publiceService.delCommentsDetailInfoById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除回复 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delCommentReply");
        }
    }

    @Override
    public Map<String, Object> getCommentsDetailInfoById(GetCommentsDetailInfoVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("commentId", vo.getCid());
            if (StringUtils.isNotEmpty(vo.getKey()))
            {
                parmaMap.put("key", vo.getKey());
            }
            if (StringUtils.isNotEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (StringUtils.isNotEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDate", vo.getEndDate());

            }
            parmaMap.put("type", ReplyCommentsModel.Type.USER);
            parmaMap.put("sid_null", "sid_null");
            parmaMap.put("notMch", "notMch");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            resultMap = publiceService.getCommentsDetailInfoById(parmaMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取评论详细信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentsDetailInfoById(UpdateCommentsInfoVo vo) throws LaiKeAPIException
    {
        try
        {
            //获取评论信息
            CommentsModel commentsModel = new CommentsModel();
            commentsModel.setStore_id(vo.getStoreId());
            commentsModel.setId(vo.getCid());
            commentsModel = commentsModelMapper.selectOne(commentsModel);
            if (commentsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBCZ, "评论不存在");
            }
            //删除之前评论图片
            CommentsImgModel commentsImgModel = new CommentsImgModel();
            commentsImgModel.setComments_id(commentsModel.getId());
            commentsImgModelMapper.delete(commentsImgModel);
            //保存修改后的图片
            int count;
            if (StringUtils.isNotEmpty(vo.getCommentImgUrls()))
            {
                for (String img : vo.getCommentImgUrls())
                {
                    img = ImgUploadUtils.getUrlImgByName(img, true);
                    CommentsImgModel saveCommentsImgModel = new CommentsImgModel();
                    saveCommentsImgModel.setComments_id(commentsModel.getId());
                    saveCommentsImgModel.setType(0);
                    saveCommentsImgModel.setComments_url(img);
                    saveCommentsImgModel.setAdd_time(new Date());
                    count = commentsImgModelMapper.insertSelective(saveCommentsImgModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                }
            }
            //追评图
            if (StringUtils.isNotEmpty(vo.getReviewImgList()))
            {
                for (String img : vo.getReviewImgList())
                {
                    img = ImgUploadUtils.getUrlImgByName(img, true);
                    CommentsImgModel saveCommentsImgModel = new CommentsImgModel();
                    saveCommentsImgModel.setComments_id(commentsModel.getId());
                    saveCommentsImgModel.setType(1);
                    saveCommentsImgModel.setComments_url(img);
                    saveCommentsImgModel.setAdd_time(new Date());
                    count = commentsImgModelMapper.insertSelective(saveCommentsImgModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
                    }
                }
            }

            //修改评论
            CommentsModel updateComment = new CommentsModel();
            updateComment.setId(commentsModel.getId());
            if (StringUtils.isEmpty(vo.getCommentText()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBNWK, "评论不能为空");
            }
            updateComment.setContent(vo.getCommentText());
            if (!StringUtils.isEmpty(vo.getReview()))
            {
                updateComment.setReview_time(new Date());
                updateComment.setReview(vo.getReview());
            }
            updateComment.setCommentType(String.valueOf(vo.getCommentType()));

            count = commentsModelMapper.updateByPrimaryKeySelective(updateComment);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }

            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "修改了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改评论 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyComments(MainVo vo, int commentId, String commentText) throws LaiKeAPIException
    {
        try
        {
            User user = publiceService.getRedisUserCache(vo);

            ReplyCommentsModel replyCommentsModel = new ReplyCommentsModel();
            replyCommentsModel.setStore_id(vo.getStoreId());
            replyCommentsModel.setCid(String.valueOf(commentId));
            replyCommentsModel.setType(ReplyCommentsModel.Type.MCH);
            int count = replyCommentsModelMapper.selectCount(replyCommentsModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJHFGL, "已经回复过了");
            }

            replyCommentsModel.setUid(user.getUser_id());
            replyCommentsModel.setContent(commentText);
            replyCommentsModel.setAdd_time(new Date());
            count = replyCommentsModelMapper.insertSelective(replyCommentsModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HFSB, "回复失败");
            }

            CommentsModel     commentsModel     = commentsModelMapper.selectByPrimaryKey(commentId);
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "回复了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("回复 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delComments(MainVo vo, int commentId) throws LaiKeAPIException
    {
        try
        {
            CommentsModel commentsModel = commentsModelMapper.selectByPrimaryKey(commentId);
            if (commentsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLBCZ, "评论不存在");
            }
            //删除评论
            int count = commentsModelMapper.deleteByPrimaryKey(commentId);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //删除评论图片
            CommentsImgModel commentsImgModel = new CommentsImgModel();
            commentsImgModel.setComments_id(commentId);
            commentsImgModelMapper.delete(commentsImgModel);
            //删除回复
            ReplyCommentsModel replyCommentsModel = new ReplyCommentsModel();
            replyCommentsModel.setCid(String.valueOf(commentId));
            replyCommentsModelMapper.delete(replyCommentsModel);

            //获取订单ID
            OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(commentsModel.getOrder_detail_id());
            OrderModel        orderModel        = new OrderModel();
            orderModel.setsNo(orderDetailsModel.getR_sNo());
            orderModel = orderModelMapper.selectOne(orderModel);

            publiceService.addAdminRecord(vo.getStoreId(), "删除了订单ID：" + orderModel.getsNo() + " 的评论信息", AdminRecordModel.Type.DEL, vo.getAccessId());

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
    }

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private CommentsImgModelMapper commentsImgModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ReplyCommentsModelMapper replyCommentsModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;
}

