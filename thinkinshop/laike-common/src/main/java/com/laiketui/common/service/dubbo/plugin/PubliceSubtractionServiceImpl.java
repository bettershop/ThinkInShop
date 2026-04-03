package com.laiketui.common.service.dubbo.plugin;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.PubliceSubtractionService;
import com.laiketui.common.mapper.SubtractionConfigModalMapper;
import com.laiketui.common.mapper.SubtractionModalMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.subtraction.SubtractionConfigModal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 公共满减
 *
 * @author Trick
 * @date 2021/2/23 14:30
 */
@Service
public class PubliceSubtractionServiceImpl implements PubliceSubtractionService
{
    private final Logger logger = LoggerFactory.getLogger(PubliceSubtractionServiceImpl.class);

    @Autowired
    private SubtractionModalMapper subtractionModalMapper;

    @Autowired
    private SubtractionConfigModalMapper subtractionConfigModalMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public String getAppImage(int storeId) throws LaiKeAPIException
    {
        String result = "";
        try
        {
            SubtractionConfigModal subtractionConfigModal = new SubtractionConfigModal();
            subtractionConfigModal.setStore_id(storeId);
            subtractionConfigModal = subtractionConfigModalMapper.selectOne(subtractionConfigModal);
            if (subtractionConfigModal != null)
            {
                if (subtractionConfigModal.getIs_subtraction() != 0)
                {
                    result = subtractionConfigModal.getMobile_terminal_image();
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取满减移动端图片 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAppImage");
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSubtractionImage(int storeId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("status", 2);
            parmaMap.put("starttime", new Date());
            parmaMap.put("endtime", new Date());
            resultList = subtractionModalMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : resultList)
            {
                String imgUrl = publiceService.getImgPath(map.get("image").toString(), storeId);
                map.put("image", imgUrl);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取满减活动图片 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSubtractionImage");
        }
        return resultList;
    }
}

