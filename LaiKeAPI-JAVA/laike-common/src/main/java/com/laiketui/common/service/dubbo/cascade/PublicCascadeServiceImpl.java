package com.laiketui.common.service.dubbo.cascade;

import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.cascade.PublicCascadeService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.mapper.MessageListModelMapper;
import com.laiketui.common.mapper.MessageModelMapper;
import com.laiketui.common.mapper.UserGradeModelMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.dictionary.MessageModel;
import com.laiketui.domain.user.UserGradeModel;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共级联查询
 *
 * @author Trick
 * @date 2021/1/7 15:16
 */
@Service
public class PublicCascadeServiceImpl implements PublicCascadeService
{
    private final Logger logger = LoggerFactory.getLogger(PublicCascadeServiceImpl.class);

    @Autowired
    private MessageModelMapper messageModelMapper;

    @Autowired
    private MessageListModelMapper messageListModelMapper;

    @Override
    public List<UserGradeModel> getGradeList(int storeId) throws LaiKeAPIException
    {
        List<UserGradeModel> userGradeModelList;
        try
        {
            //获取会员等级下拉列表
            UserGradeModel userGradeModel = new UserGradeModel();
            userGradeModel.setStore_id(storeId);
            userGradeModelList = userGradeModelMapper.select(userGradeModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取会员等级列表 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGradeList");
        }
        return userGradeModelList;
    }

    @Override
    public Map<String, Object> getSourceList(int storeId) throws LaiKeAPIException
    {
        Map<String, Object> resultData;
        try
        {
            //获取来源下拉
            resultData = publicDictionaryService.getDictionaryByName("来源");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取来源列表 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGradeList");
        }
        return resultData;
    }

    @Override
    public Map<String, Object> getSmsTypeList(MainVo vo, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultData = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> dictionaryListModelList = new ArrayList<>();
            if (!StringUtils.isEmpty(name))
            {
                //获取类别
                dictionaryListModelList = publicDictionaryService.getDictionaryById("短信模板类别", name);
            }
            else
            {
                dictionaryListModelList.add(publicDictionaryService.getDictionaryByName("短信模板类型"));
            }
            resultData.put("list", dictionaryListModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取短信类型 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSmsTypeList");
        }
        return resultData;
    }

    @Override
    public Map<String, Object> getSmsTemplateList(MainVo vo, Integer type, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultData = new HashMap<>(16);
        try
        {
            if (type == null)
            {
                type = SmsType.VERIFICATION;
            }
            //获取短信模板 类型 0:验证码 1:短信通知
            List<MessageModel> messageListModelList;
            MessageModel       messageModel = new MessageModel();
            messageModel.setStore_id(vo.getStoreId());
            if (id != null)
            {
                messageModel.setType1(id);
            }
            else
            {
                messageModel.setType(type);
            }
            messageListModelList = messageModelMapper.select(messageModel);
            resultData.put("templateList", messageListModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取短信模板 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSmsTemplateList");
        }
        return resultData;
    }

    @Override
    public List<Map<String, Object>> getJoinCityCounty(String groupName, int groupId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictPid(groupId);
            adminCgModel.setDistrictName(groupName);
            List<AdminCgModel> adminCgModelList = adminCgModelMapper.select(adminCgModel);
            for (AdminCgModel adminCg : adminCgModelList)
            {
                Map<String, Object> map = new HashMap<>(2);
                map.put("GroupID", adminCg.getId());
                map.put("G_CName", adminCg.getDistrictName());
                resultList.add(map);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("省市级联异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getJoinCityCounty");
        }
        return resultList;
    }

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private PublicDictionaryService publicDictionaryService;
}

