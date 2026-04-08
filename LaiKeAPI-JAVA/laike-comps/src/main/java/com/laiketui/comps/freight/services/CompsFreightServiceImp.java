package com.laiketui.comps.freight.services;

import com.laiketui.common.api.PublicFreightService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.freight.CompsFreightService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompsFreightServiceImp implements CompsFreightService
{

    private final Logger logger = LoggerFactory.getLogger(CompsFreightServiceImp.class);

    @Autowired
    private RedisUtil            redisUtil;
    @Autowired
    private PublicFreightService publicFreightService;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer otype, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);

            if (user != null)
            {
                PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize(), vo.getPageNum());
                if (fid != null)
                {
                    //根据id获取
                    resultMap = publicFreightService.FreightAndModifyShow(vo.getStoreId(), mchId, fid);
                }
                else if (status != null || !StringUtils.isEmpty(name) || otype != null)
                {
                    //根据条件获取模板列表
                    resultMap = publicFreightService.getFreightList(vo, mchId, name, status, otype, pageModel);
                }
                else
                {
                    //获取全部
                    resultMap = publicFreightService.getFreightList(vo, mchId, null, null, null, pageModel);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFreightInfo");
        }
        return resultMap;
    }

    @Override
    public List<AdminCgModel> getRegion(MainVo vo, Integer level, Integer sid) throws LaiKeAPIException
    {
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictLevel(level);
            if (sid != null)
            {
                adminCgModel.setDistrictPid(sid);
            }
            return adminCgModelMapper.select(adminCgModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取地区信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRegion");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user != null)
            {
                vo.setShopId(user.getMchId());
                return publicFreightService.FreightToAdd(vo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
        return false;
    }

    @Override
    public void freightSetDefault(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User        user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            AddFreihtVo addFreihtVo = new AddFreihtVo();
            addFreihtVo.setFid(id);
            addFreihtVo.setStoreId(vo.getStoreId());
            addFreihtVo.setShopId(user.getMchId());
            publicFreightService.setDefaultFreight(addFreihtVo);

            publiceService.addAdminRecord(vo.getStoreId(), "将运费模板ID：" + id + " 设为默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费设置默认开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightSetDefault");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFreight(MainVo vo, String freightIds) throws LaiKeAPIException
    {
        try
        {
            publicFreightService.FreightToDel(vo.getStoreId(), freightIds);

            String[] fidList = freightIds.split(",");
            for (String id : fidList)
            {
                publiceService.addAdminRecord(vo.getStoreId(), "删除了运费模板ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
    }
}