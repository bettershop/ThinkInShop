package com.laiketui.admins.mch.services;

import com.laiketui.admins.api.mch.MchUserFootprintService;
import com.laiketui.common.mapper.MchBrowseModelMapper;
import com.laiketui.common.mapper.UserBaseMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户管理
 *
 * @author Trick
 * @date 2021/5/26 17:19
 */
@Service
public class MchUserFootprintServiceImpl implements MchUserFootprintService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String phone, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("mobile", phone);
            parmaMap.put("startDate", startDate);
            parmaMap.put("endDate", endDate);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());

            List<Map<String, Object>> mchBrowseList = mchBrowseModelMapper.getMchBrowseListDynamic(parmaMap);
            for (Map<String, Object> map : mchBrowseList)
            {
                String userId    = map.get("user_id") + "";
                String userName  = "";
                String userPhone = "";
                if (!StringUtils.isEmpty(userId) && !"游客".equals(userId))
                {
                    User userClent = new User();
                    userClent.setStore_id(vo.getStoreId());
                    userClent.setUser_id(userId);
                    userClent = userBaseMapper.selectOne(userClent);
                    if (userClent != null)
                    {
                        userName = userClent.getUser_name();
                        userPhone = userClent.getMobile();
                    }
                }
                map.put("user_name", userName);
                map.put("mobile", userPhone);
            }
            int total = mchBrowseModelMapper.countMchBrowseListDynamic(parmaMap);

            resultMap.put("list", mchBrowseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("客户管理列表 异常 {}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> userPug(MainVo vo, String userId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                      user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            List<Map<String, Object>> mchBrowseList = mchBrowseModelMapper.getMchBrowseByUser(vo.getStoreId(), user.getMchId(), user.getUser_id());
            int                       total         = mchBrowseModelMapper.countMchBrowseByUser(vo.getStoreId(), user.getMchId(), user.getUser_id());

            resultMap.put("list", mchBrowseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取用户今日足迹明细 异常 {}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "userPug");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> del(MainVo vo, String userId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int  row  = mchBrowseModelMapper.delMchBrowseByUserId(vo.getStoreId(), user.getMchId(), userId);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除足迹 异常 {}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
        return resultMap;
    }
}

