package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminExpressService;
import com.laiketui.common.api.PublicExpressService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.ExpressModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ExpressModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddExpressSubtableVo;
import com.laiketui.domain.vo.config.GetExpressSubtableListVo;
import com.laiketui.domain.vo.goods.ExpressSaveVo;
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
 * 物流公司管理
 *
 * @author Trick
 * @date 2021/7/6 16:48
 */
@Service
public class AdminExpressServiceImpl implements AdminExpressService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, Integer id, String keyWord, Integer sortType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (StringUtils.isNotEmpty(keyWord))
            {
                parmaMap.put("kuaidi_name", keyWord);
            }
            parmaMap.put("id", id);
            String sort = DataUtils.Sort.DESC.toString();
            if (sortType != null && sortType.equals(SortType.DESC))
            {
                sort = DataUtils.Sort.ASC.toString();
            }
            parmaMap.put("sort_sort", sort);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total = expressModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = expressModelMapper.selectDynamic(parmaMap);

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("物流公司列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public void expressSwitch(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (id == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            ExpressModel expressOld = expressModelMapper.selectByPrimaryKey(id);
            if (expressOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBCZ, "物流不存在");
            }
            ExpressModel expressUpdate = new ExpressModel();
            expressUpdate.setId(expressOld.getId());
            int isOpen = 1;
            if (expressOld.getIs_open() == isOpen)
            {
                isOpen = 2;
            }
            expressUpdate.setIs_open(isOpen);
            int row = expressModelMapper.updateByPrimaryKeySelective(expressUpdate);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KGSB, "开关失败");
            }

            publiceService.addAdminRecord(vo.getStoreId(), "将物流公司名称：" + expressOld.getKuaidi_name() + " 进行了开关操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("物流开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressSwitch");
        }
    }

    @Override
    public void expressSave(ExpressSaveVo vo) throws LaiKeAPIException
    {
        try
        {
            int          row;
            AdminModel   adminModel  = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ExpressModel expressSave = new ExpressModel();
            ExpressModel expressOld  = null;
            if (vo.getId() != null)
            {
                expressOld = expressModelMapper.selectByPrimaryKey(vo.getId());
                if (expressOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGSBCZ, "物流公司不存在");
                }
            }
            expressSave.setKuaidi_name(vo.getName());
            expressSave.setType(vo.getCode());
            expressSave.setSort(vo.getSort());
            expressSave.setIs_open(vo.getSwitchse());
            expressSave.setAdd_date(new Date());

            ExpressModel expressModel = new ExpressModel();
            expressModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            expressModel.setKuaidi_name(vo.getName());
            expressSave.setRecycle(0);
            if (expressOld == null || !expressOld.getKuaidi_name().equals(vo.getName()))
            {
                if (expressModelMapper.selectCount(expressModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGSYCZ, "物流公司已存在");
                }
            }
            if (expressOld == null || !expressOld.getType().equals(vo.getCode()))
            {
                expressModel.setKuaidi_name(null);
                expressModel.setType(vo.getCode());
                if (expressModelMapper.selectCount(expressModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLBMYCZ, "物流编码已存在");
                }
            }
            if (expressOld != null)
            {
                expressSave.setId(expressOld.getId());
                row = expressModelMapper.updateByPrimaryKeySelective(expressSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了物流ID：" + expressSave.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                row = expressModelMapper.insertSelective(expressSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了物流ID：" + expressSave.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }

            if (row < 1)
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
            logger.error("添加/编辑物流 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressSave");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void expressDel(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            int        row;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] idList = ids.split(",");
            for (String id : idList)
            {
                ExpressModel expressUpdate = new ExpressModel();
                expressUpdate.setId(Integer.parseInt(id));
                expressUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                row = expressModelMapper.updateByPrimaryKeySelective(expressUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
                }
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "删除了物流ID：" + expressUpdate.getId() + " 的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除物流 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "expressDel");
        }
    }

    @Override
    public void addAndUpdateExpressSubtable(AddExpressSubtableVo vo) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //默认获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            vo.setMch_id(storeMchId);
            publicExpressService.addAndUpdateExpressSubtable(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加修改快递公司子表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> getExpressSubtableList(GetExpressSubtableListVo vo) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //默认获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (StringUtils.isEmpty(storeMchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTJZYD, "请添加自营店");
            }
            vo.setMch_id(storeMchId);
            return publicExpressService.getExpressSubtableList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }


    @Override
    public Map<String, Object> getExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            return publicExpressService.getExpressSubtableById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取快递公司子表详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }


    @Override
    public void delExpressSubtableById(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicExpressService.delExpressSubtableById(vo, id);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除快递公司子表详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> getExpressInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //默认获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            return publicExpressService.getExpressInfoByMchId(vo, storeMchId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有物流公司信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Override
    public Map<String, Object> GetLogistics(MainVo vo, String sNo) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            return publicExpressService.getExpressInfoBySNo(vo, sNo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取所有物流公司信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAndUpdateExpressSubtable");
        }
    }

    @Autowired
    private PublicExpressService publicExpressService;

    @Autowired
    private CustomerModelMapper customerModelMapper;
}

