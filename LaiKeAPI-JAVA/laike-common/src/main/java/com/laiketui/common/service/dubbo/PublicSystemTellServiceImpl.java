package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicSystemTellService;
import com.laiketui.common.mapper.SystemTellModelMapper;
import com.laiketui.common.mapper.SystemTellUserModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.systems.SystemTellUserModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城公告通用类
 */
@Service
public class PublicSystemTellServiceImpl implements PublicSystemTellService
{
    private final Logger logger = LoggerFactory.getLogger(PublicSystemTellServiceImpl.class);

    @Autowired
    private SystemTellUserModelMapper systemTellUserModelMapper;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markToRead(MainVo vo, String read_id, Integer tell_id, boolean isSupplier) throws LaiKeAPIException
    {
        try
        {
            SystemTellUserModel systemTellUserModel = new SystemTellUserModel();
            systemTellUserModel.setRead_id(read_id);
            systemTellUserModel.setTell_id(tell_id);
            systemTellUserModel.setStore_id(vo.getStoreId());
            systemTellUserModel.setStore_type(vo.getStoreType());
            if (isSupplier)
            {
                systemTellUserModel.setIsSupplier(SystemTellUserModel.IS_SUPPLIER.ok);
            }
            else
            {
                systemTellUserModel.setIsSupplier(SystemTellUserModel.IS_SUPPLIER.no);
            }
            //查询是否重复标记
            int i = systemTellUserModelMapper.selectCount(systemTellUserModel);
            if (i > 0)
            {
                return;
            }
            systemTellUserModel.setAdd_time(new Date());
            if (systemTellUserModelMapper.insertSelective(systemTellUserModel) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记公告以读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderTotal");
        }
    }

    @Override
    public Map<String, Object> getUserTell(MainVo vo, String type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //公告标题
            String systemMsgTitle = "";
            //系统公告
            String systemMsg = "";
            //公告类型 1=系统维护 2=升级公告 3--普通公告
            int systemMsgType = 0;
            //维护开始、结束时间
            String              systemMsgStartDate = "";
            String              systemMsgEndDate   = "";
            String              info               = "";
            Map<String, Object> parmaMap           = new HashMap<>(16);
            parmaMap.put("store_id", 0);
            parmaMap.put("startDate_lt", new Date());
            parmaMap.put("endDate_gt", new Date());
            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put(type, SystemTellModel.TELL.YES);
            List<Map<String, Object>> systemList = systemTellModelMapper.selectDynamic(parmaMap);
            if (systemList.size() > 0)
            {
                Map<String, Object> systemMap = systemList.get(0);
                systemMsgType = MapUtils.getIntValue(systemMap, "type");
                systemMsgTitle = MapUtils.getString(systemMap, "title");
                systemMsg = MapUtils.getString(systemMap, "content");
                systemMsgEndDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "enddate"), GloabConst.TimePattern.YMDHMS);
                systemMsgStartDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "startdate"), GloabConst.TimePattern.YMDHMS);
                if (systemMsgType == 1)
                {
                    info = "系统维护中";
                }
            }
            //系统公告
            resultMap.put("systemMsgTitle", systemMsgTitle);
            resultMap.put("systemMsg", systemMsg);
            resultMap.put("systemMsgType", systemMsgType);
            resultMap.put("systemMsgEndDate", systemMsgEndDate);
            resultMap.put("systemMsgStartDate", systemMsgStartDate);
            resultMap.put("info", info);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取平台用户公告");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SYPTSPLCCC, "获取平台用户公告 出错", "goGroup");
        }
        return resultMap;
    }
}
