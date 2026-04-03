package com.laiketui.comps.invoice.services;


import com.laiketui.common.mapper.InvoiceHeaderModelMapper;
import com.laiketui.common.mapper.UserBaseMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.comps.api.invoice.CompsInvoiceHeaderService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.domain.invoice.InvoiceHeaderModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.invoice.InvoiceHeaderVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;

/**
 * 会员制实现
 *
 * @author Trick
 * @date 2020/12/21 17:49
 */
@Service
public class CompsInvoiceHeaderServiceImpl implements CompsInvoiceHeaderService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private InvoiceHeaderModelMapper invoiceHeaderModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;


    @Override
    public Map<String, Object> getList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> list     = new ArrayList<>();
            User                      user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object>       paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            paramMap.put("addTimeSort", DataUtils.Sort.DESC.toString());
            paramMap.put("userId", user.getUser_id());
            if (id != null)
            {
                paramMap.put("id", id);
            }
            Integer integer = invoiceHeaderModelMapper.countList(paramMap);
            if (integer != null && integer > 0)
            {
                list = invoiceHeaderModelMapper.getList(paramMap);
                int i = 0;
                for (Map<String, Object> map : list)
                {
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    //默认发票抬头始终放在第一位
                    Integer isDefault = MapUtils.getInteger(map, "is_default");
                    if (isDefault == DictionaryConst.WhetherMaven.WHETHER_OK && i != 0)
                    {
                        Collections.swap(list, i, 0);
                    }
                    i++;
                }
            }
            resultMap.put("total", integer);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询我的发票抬头 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(InvoiceHeaderVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (Objects.isNull(vo.getType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "抬头类型不得为空", "addOrUpdate");
            }
            if (Objects.isNull(vo.getIsDefault()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择是否为默认选项", "addOrUpdate");
            }
            if (vo.getType().equals(InvoiceHeaderModel.ENTERPRISE))
            {
                if (StringUtil.isEmpty(vo.getCompanyName()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入公司名称", "addOrUpdate");
                }
                if (StringUtil.isEmpty(vo.getCompanyTaxNumber()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入公司税号", "addOrUpdate");
                }
            }
            else
            {
                if (StringUtil.isEmpty(vo.getCompanyName()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入发票抬头名称", "addOrUpdate");
                }
            }

            InvoiceHeaderModel invoiceHeaderModel = new InvoiceHeaderModel();
            invoiceHeaderModel.setStore_id(vo.getStoreId());
            invoiceHeaderModel.setUser_id(user.getUser_id());
            invoiceHeaderModel.setType(vo.getType());
            invoiceHeaderModel.setCompany_name(vo.getCompanyName());
            invoiceHeaderModel.setCompany_tax_number(vo.getCompanyTaxNumber());
            invoiceHeaderModel.setRegister_address(vo.getRegisterAddress());
            invoiceHeaderModel.setRegister_phone(vo.getRegisterPhone());
            invoiceHeaderModel.setDeposit_bank(vo.getDepositBank());
            invoiceHeaderModel.setBank_number(vo.getBankNumber());
            invoiceHeaderModel.setIs_default(vo.getIsDefault());
            //每个用户至少需要一个默认发票抬头
            Map<String, Object> aDefault = invoiceHeaderModelMapper.getDefault(vo.getStoreId(), user.getUser_id());
            if (!Objects.isNull(vo.getId()))
            {
                InvoiceHeaderModel old = invoiceHeaderModelMapper.selectByPrimaryKey(vo.getId());
                if (Objects.isNull(old))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "发票抬头信息不存在", "addOrUpdate");
                }
                Integer id = MapUtils.getInteger(aDefault, "id");
                if (vo.getIsDefault().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    if (id != null)
                    {
                        InvoiceHeaderModel oldInvoiceHeader = invoiceHeaderModelMapper.selectByPrimaryKey(id);
                        oldInvoiceHeader.setIs_default(DictionaryConst.WhetherMaven.WHETHER_NO);
                        invoiceHeaderModelMapper.updateByPrimaryKeySelective(oldInvoiceHeader);
                    }
                }
                else
                {
                    if (id.equals(vo.getId()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "用户需要至少一个默认抬头信息", "addOrUpdate");
                    }
                }
                invoiceHeaderModel.setId(vo.getId());
                invoiceHeaderModelMapper.updateByPrimaryKeySelective(invoiceHeaderModel);
            }
            else
            {
                Integer id = MapUtils.getInteger(aDefault, "id");
                if (id == null)
                {
                    invoiceHeaderModel.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                else
                {
                    if (vo.getIsDefault().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                    {
                        InvoiceHeaderModel defaultHeader = invoiceHeaderModelMapper.selectByPrimaryKey(id);
                        defaultHeader.setIs_default(DictionaryConst.WhetherMaven.WHETHER_NO);
                        invoiceHeaderModelMapper.updateByPrimaryKeySelective(defaultHeader);
                    }
                }
                invoiceHeaderModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                invoiceHeaderModel.setAdd_time(new Date());
                invoiceHeaderModelMapper.insertSelective(invoiceHeaderModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改发票抬头信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addOrUpdate");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void del(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtil.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "发票抬头id不得为空", "del");
            }
            String[] split = ids.split(SplitUtils.DH);
            for (String id : split)
            {
                InvoiceHeaderModel invoiceHeaderModel = invoiceHeaderModelMapper.selectByPrimaryKey(id);
                if (Objects.isNull(invoiceHeaderModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "id为" + id + "的发票抬头信息不存在", "del");
                }
                invoiceHeaderModel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
                invoiceHeaderModelMapper.updateByPrimaryKeySelective(invoiceHeaderModel);
            }
            InvoiceHeaderModel isDefault = new InvoiceHeaderModel();
            isDefault.setStore_id(vo.getStoreId());
            isDefault.setUser_id(user.getUser_id());
            isDefault.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
            isDefault.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            int i = invoiceHeaderModelMapper.selectCount(isDefault);
            if (i == 0)
            {
                invoiceHeaderModelMapper.setDefaultDel(vo.getStoreId(), user.getUser_id(), 1, 0);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改发票抬头信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
    }

    @Override
    public Map<String, Object> getDefault(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> map = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> aDefault = invoiceHeaderModelMapper.getDefault(vo.getStoreId(), user.getUser_id());
            Integer             id       = MapUtils.getInteger(aDefault, "id");
            if (id != null)
            {
                map = aDefault;
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取默认发票抬头信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
        return map;
    }
}

