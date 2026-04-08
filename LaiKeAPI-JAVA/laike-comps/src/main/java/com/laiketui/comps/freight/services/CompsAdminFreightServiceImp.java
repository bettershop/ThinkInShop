package com.laiketui.comps.freight.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.laiketui.common.api.PublicFreightService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.freight.CompsAdminFreightService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompsAdminFreightServiceImp implements CompsAdminFreightService
{

    private final Logger logger = LoggerFactory.getLogger(CompsAdminFreightServiceImp.class);

    @Autowired
    private RedisUtil            redisUtil;
    @Autowired
    private PublicFreightService publicFreightService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> AdminGetFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, Integer otype, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, true);

            if (user != null)
            {
                PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize(), vo.getPageNum());
                if (fid != null)
                {
                    if (mchId != null)
                    {
                        //根据id获取
                        resultMap = publicFreightService.freightAndModifyShow(vo, mchId, fid);
                    }
                    else
                    {
                        resultMap = publicFreightService.freightAndModifyShow(vo, 0, fid);
                    }
                }
                else if (status != null || otype != null || !StringUtils.isEmpty(name))
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addAdminFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, true);
            if (user != null)
            {
                Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                vo.setShopId(mchId);
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
    public void AdminFreightSetDefault(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            User        user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, true);
            AddFreihtVo addFreihtVo = new AddFreihtVo();
            addFreihtVo.setFid(id);
            addFreihtVo.setStoreId(vo.getStoreId());
            addFreihtVo.setShopId(user.getMchId());
            publicFreightService.setDefaultFreight(addFreihtVo);
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
    public void delAdminFreight(MainVo vo, String freightIds) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicFreightService.FreightToDel(vo.getStoreId(), freightIds);
            if (!StringUtils.isEmpty(freightIds))
            {
                String[] fidList = freightIds.split(",");
                for (String id : fidList)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "删除了运费模板ID：" + id + "的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
                }
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


    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Override
    public Map<String, Object> cityInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Object citys        = redisUtil.get("cityChildren");
            String cityChildren = citys == null ? null : citys + "";
            if (StringUtils.isEmpty(cityChildren))
            {
                Integer                   minLevel           = adminCgModelMapper.getMinLevel();
                List<Map<String, Object>> adminCgInfoOneList = new ArrayList<>();
                if (minLevel != null)
                {
                    Map<String, Object> paramMap = new HashMap<>(16);
                    paramMap.put("G_Level", minLevel);
                    adminCgInfoOneList = adminCgModelMapper.getAdminCgInfoDynamic(paramMap);
                    adminCgInfoOneList.stream().forEach(adminCgInfoOne ->
                    {
                        Integer groupId = MapUtils.getInteger(adminCgInfoOne, "id");
                        paramMap.clear();
                        paramMap.put("G_ParentID", groupId);
                        List<Map<String, Object>> adminCgParent = adminCgModelMapper.getAdminCgInfoDynamic(paramMap);
                        adminCgInfoOne.put("children", adminCgParent);
                        //递归
                        getTree(adminCgParent);
                    });
                }
                redisUtil.set("cityChildren", JSON.toJSONString(adminCgInfoOneList));
                resultMap.put("list", adminCgInfoOneList);
            }
            else
            {
                List<Map> maps = JSONArray.parseArray(cityChildren, Map.class);
                resultMap.put("list", maps);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("城市 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "cityInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> relatedProducts(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            return publicFreightService.relatedProducts(vo);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
    }

    //递归
    private void getTree(List<Map<String, Object>> dictList)
    {
        List<Map<String, Object>> retList = new ArrayList<>();
        for (Map<String, Object> dict : dictList)
        {
            Integer groupId = MapUtils.getInteger(dict, "id");
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("G_ParentID", groupId);
            retList = adminCgModelMapper.getAdminCgInfoDynamic(paramMap);
            if (retList.size() > 0)
            {
                dict.put("children", retList);
                getTree(retList);
            }
        }
    }


}
