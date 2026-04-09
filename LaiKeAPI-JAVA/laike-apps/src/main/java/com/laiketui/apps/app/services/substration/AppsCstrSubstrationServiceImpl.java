package com.laiketui.apps.app.services.substration;

import com.laiketui.apps.api.app.services.substration.AppsCstrSubstrationService;
import com.laiketui.common.api.plugin.substration.PubliceSubstrationService;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 满减
 *
 * @author Trick
 * @date 2021/4/16 14:25
 */
@Service
public class AppsCstrSubstrationServiceImpl implements AppsCstrSubstrationService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PubliceSubstrationService publiceSubstrationService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> getSubstrationGoodsList(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            resultMap = publiceSubstrationService.getSubstrationGoodsList(vo, user, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("进入购物车页面出错" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSubstrationGoodsList");
        }
        return resultMap;
    }
}

