package com.laiketui.apps.app.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.apps.api.app.AppsCstrMessagesService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.NoticeModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 我的消息实现
 *
 * @author Trick
 * @date 2020/10/16 16:44
 */
@Service
public class AppsCstrMessagesServiceImpl implements AppsCstrMessagesService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrJssdkServiceImpl.class);

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    MessageReadRecordModelMapper messageReadRecordModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private NoticeModelMapper noticeModelMapper;

    @Autowired
    private OnlineMessageModelMapper onlineMessageModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //消息总数
        int mesgNum = 0;
        //未读数量
        int notMesgNum = 0;
        try
        {
            //获取用户信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取该用户消息
            resultMap = more(vo, 0);

            SystemMessageModel systemMessageModel = new SystemMessageModel();
            systemMessageModel.setStore_id(vo.getStoreId());
            systemMessageModel.setRecipientid(user.getUser_id());
            //获取消息总数
            mesgNum = systemMessageModelMapper.selectCount(systemMessageModel);
            //获取未读数量
            systemMessageModel.setType(DictionaryConst.SystemMessageType.MESSAGE_READE_NO);
            notMesgNum = systemMessageModelMapper.selectCount(systemMessageModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("读取我的消息异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQXXSBWLYC, "读取消息失败,网络异常", "index");
        }
        resultMap.put("total", mesgNum);
        resultMap.put("noread", notMesgNum);
        return resultMap;
    }

    @Override
    public Map<String, Object> more(MainVo vo, int pageNo) throws LaiKeAPIException
    {
        Map<String, Object>       resultMap              = new HashMap<>(16);
        List<Map<String, Object>> systemMessageModelList = new ArrayList<>();
        try
        {
            PageModel pageModel = new PageModel(pageNo);
            Object    obj       = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
            if (obj != null)
            {
                User               user               = JSON.parseObject(obj.toString(), User.class);
                SystemMessageModel systemMessageModel = new SystemMessageModel();
                systemMessageModel.setStore_id(vo.getStoreId());
                systemMessageModel.setRecipientid(user.getUser_id());
                systemMessageModel.setPageModel(pageModel);
                systemMessageModelList = systemMessageModelMapper.getMessageList(systemMessageModel);
                for (Map<String, Object> map : systemMessageModelList)
                {
                    String time = DateUtil.dateFormate(map.get("time").toString(), GloabConst.TimePattern.YMDHMS);
                    map.put("time", time);
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录", "more");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载后续消息异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JZHXXXWLYC, "加载后续消息,网络异常", "more");
        }
        resultMap.put("message", systemMessageModelList);
        return resultMap;
    }

    @Override
    public List<String> getMessageIds(MainVo vo) throws LaiKeAPIException
    {
        List<String> templateId = new ArrayList<>();
        try
        {
            NoticeModel noticeModel = new NoticeModel();
            noticeModel.setStore_id(vo.getStoreId());
            noticeModel = noticeModelMapper.selectOne(noticeModel);
            if (Objects.isNull(noticeModel))
            {
                logger.error("该商城暂无模板");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSCZWMB, "该商城暂无模板");
            }
            // 禅道 51079 修改返回值结构为Array
//            result.put("storeId", noticeModel.getStore_id());
//            List<String> templateId = new ArrayList<>();
            if (!Objects.isNull(noticeModel.getPay_success()))
            {
                templateId.add(noticeModel.getPay_success());
            }
            if (!Objects.isNull(noticeModel.getDelivery()))
            {
                templateId.add(noticeModel.getDelivery());
            }
            if (!Objects.isNull(noticeModel.getRefund_res()))
            {
                templateId.add(noticeModel.getRefund_res());
            }
//            result.put("templates", templateId);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("获取微信模板异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQWXMBYC, "获取微信模板异常", "getWxTemplates");
        }
        return templateId;
    }

    @Override
    public boolean all(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            //获取用户信息
            User   user;
            Object obj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
            if (obj != null)
            {
                user = JSON.parseObject(obj.toString(), User.class);
                //标记已读
                SystemMessageModel systemMessageModel = new SystemMessageModel();
                systemMessageModel.setType(DictionaryConst.SystemMessageType.MESSAGE_READE_OK);
                systemMessageModel.setStore_id(vo.getStoreId());
                systemMessageModel.setRecipientid(user.getUser_id());
                int count = systemMessageModelMapper.updateMessage(systemMessageModel);
                if (count < 1)
                {
                    logger.debug("user:" + user.getUser_id() + "一键标记已读失败!");
                }
                return true;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WDL, "未登录");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("一键标记已读异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJBJYDSBWLYC, "一键标记已读失败,网络异常", "all");
        }
    }


    @Override
    public Map<String, Object> oneindex(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object>      resultMap   = new HashMap<>(16);
        List<SystemMessageModel> sysMesgList = new ArrayList<>();
        try
        {
            //获取用户信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取该用户消息
            SystemMessageModel systemMessageModel = new SystemMessageModel();
            systemMessageModel.setId(id);
            systemMessageModel.setRecipientid(user.getUser_id());
            SystemMessageModel sysMesg = systemMessageModelMapper.selectOne(systemMessageModel);

            //标记已读
            systemMessageModel = new SystemMessageModel();
            systemMessageModel.setId(sysMesg.getId());
            systemMessageModel.setType(DictionaryConst.SystemMessageType.MESSAGE_READE_OK);
            int count = systemMessageModelMapper.updateByPrimaryKeySelective(systemMessageModel);
            if (count < 1)
            {
                logger.debug("消息id:" + id + "标记已读失败!");
            }
            sysMesgList.add(sysMesg);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取消息详情异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQXXXQSBWLYC, "获取消息详情失败,网络异常", "oneindex");
        }
        resultMap.put("message", sysMesgList);
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean del(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            //获取用户信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            //id为null则代表全选，否则批量删除
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("store_id",vo.getStoreId());
            paramMap.put("recipientId",user.getUser_id());
            if (StringUtils.isNotEmpty(id))
            {
                String[] ids = id.split(SplitUtils.DH);
                paramMap.put("ids",ids);
            }
            systemMessageModelMapper.delBatchByIds(paramMap);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除消息详情异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XXSCSBWLYC, "消息删除失败,网络异常", "del");
        }
    }

    @Override
    public NoticeModel getWxTemplates(int storeId, String language, String accessId) throws LaiKeAPIException
    {
        NoticeModel noticeModel = new NoticeModel();
        try
        {
            noticeModel.setStore_id(storeId);
            noticeModel = noticeModelMapper.selectOne(noticeModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取微信模板异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQWXMBSBWLYC, "获取微信模板失败,网络异常", "getWxTemplates");
        }
        return noticeModel;
    }

    @Override
    public Map<String, Object> messageNotReade(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //用户是否有客服未读消息
            int userOnlineMessageNotRead = 0;
            //店铺是否有客服未读消息
            int mchOnlineMessageNotRead = 0;
            //获取会员信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (user != null)
            {
                user = userBaseMapper.selectByUserIdOne(vo.getStoreId(), user.getUser_id());
                userOnlineMessageNotRead = onlineMessageModelMapper.countUserMessageNotRead(vo.getStoreId(), user.getUser_id());
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setUser_id(user.getUser_id());
                mchModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    mchOnlineMessageNotRead = onlineMessageModelMapper.countMchMessageNotRead(vo.getStoreId(), mchModel.getId().toString());

                }
            }
            resultMap.put("userOnlineMessageNotRead", userOnlineMessageNotRead);
            resultMap.put("mchOnlineMessageNotRead", mchOnlineMessageNotRead);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除消息详情异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XXSCSBWLYC, "消息删除失败,网络异常", "del");
        }
        return resultMap;
    }
}

