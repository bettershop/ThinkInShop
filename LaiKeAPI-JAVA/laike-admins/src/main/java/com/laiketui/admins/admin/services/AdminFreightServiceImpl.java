package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminFreightService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
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


/**
 * 运费管理
 *
 * @author Trick
 * @date 2020/12/31 15:12
 */
@Service
public class AdminFreightServiceImpl implements AdminFreightService
{
    private final Logger logger = LoggerFactory.getLogger(AdminBrandServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getFreightInfo(MainVo vo, Integer mchId, Integer fid, Integer status, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize(), vo.getPageNum());
            if (fid != null)
            {
                //更具id获取
                resultMap = publicMchService.freightModifyShow(vo.getStoreId(), mchId, fid);
            }
            else if (status != null || !StringUtils.isEmpty(name))
            {
                //更具条件获取模板列表
                resultMap = publicMchService.freightList(vo, mchId, name, status, pageModel);
            }
            else
            {
                //获取全部
                resultMap = publicMchService.freightList(vo, mchId, null, null, pageModel);
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
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                vo.setShopId(user.getShop_id());
                return publicMchService.freightAdd(vo);
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
            AdminModel  user        = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            AddFreihtVo addFreihtVo = new AddFreihtVo();
            addFreihtVo.setFid(id);
            addFreihtVo.setStoreId(vo.getStoreId());
            addFreihtVo.setShopId(user.getShop_id());
            publicMchService.setDefault(addFreihtVo);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将运费模板ID：" + id + "设为默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
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
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicMchService.freightDel(vo.getStoreId(), freightIds,adminModel.getShop_id());
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
            logger.error("添加/修改运费信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
    }


    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

}

