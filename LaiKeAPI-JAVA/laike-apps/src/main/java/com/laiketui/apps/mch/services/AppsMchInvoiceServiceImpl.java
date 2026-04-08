package com.laiketui.apps.mch.services;

import com.alibaba.fastjson2.JSONObject;
import com.laiketui.apps.api.mch.AppsMchInvoiceService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.InvoiceHeaderModelMapper;
import com.laiketui.common.mapper.InvoiceInfoModelMapper;
import com.laiketui.common.mapper.UserBaseMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.invoice.InvoiceHeaderModel;
import com.laiketui.domain.invoice.InvoiceInfoModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.invoice.AdminInvoiceVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 发票管理
 *
 * @author 朱庆宇
 * @date 2024/04/25 14:27
 */
@Service
public class AppsMchInvoiceServiceImpl implements AppsMchInvoiceService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private InvoiceInfoModelMapper invoiceInfoModelMapper;

    @Autowired
    private InvoiceHeaderModelMapper invoiceHeaderModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getList(AdminInvoiceVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            paramMap.put("addTimeSort", DataUtils.Sort.DESC.toString());
            paramMap.put("mchId", user.getMchId());
            if (StringUtil.isNotEmpty(vo.getCondition()))
            {
                paramMap.put("condition", vo.getCondition());
            }
            if (!Objects.isNull(vo.getInvoiceStatus()) && vo.getInvoiceStatus() != 0)
            {
                paramMap.put("invoiceStatus", vo.getInvoiceStatus());
            }
            if (!Objects.isNull(vo.getType()))
            {
                paramMap.put("type", vo.getType());
            }
            Integer                   integer = invoiceInfoModelMapper.countList(paramMap);
            List<Map<String, Object>> list    = new ArrayList<>();
            if (integer != null && integer > 0)
            {
                list = invoiceInfoModelMapper.getList(paramMap);
                list.stream().forEach(map ->
                {
                    Integer type = MapUtils.getInteger(map, "type");
                    if (type.equals(InvoiceHeaderModel.ENTERPRISE))
                    {
                        map.put("typeDesc", "企业");
                    }
                    else
                    {
                        map.put("typeDesc", "个人");
                    }
                    Integer invoiceStatus = MapUtils.getInteger(map, "invoice_status");
                    switch (invoiceStatus)
                    {
                        case 1:
                            map.put("invoiceStatusDesc", "申请中");
                            break;
                        case 2:
                            map.put("invoiceStatusDesc", "已完成");
                            break;
                        case 3:
                            map.put("invoiceStatusDesc", "已撤销");
                            break;
                        default:
                            break;
                    }
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                    String fileTime = MapUtils.getString(map, "file_time");
                    if (StringUtils.isNotEmpty(fileTime))
                    {
                        map.put("file_time", DateUtil.dateFormate(fileTime, GloabConst.TimePattern.YMDHMS));
                    }
                    map.put("file", publiceService.getImgPath(MapUtils.getString(map, "file"), vo.getStoreId()));
                });
            }
            resultMap.put("total", integer);
            resultMap.put("list", list);
            if (vo.getExportType() == 1)
            {
                exportListData(DataUtils.cast(resultMap.get("list")), response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询发票列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (Objects.isNull(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入发票id", "getInfo");
            }
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            paramMap.put("addTimeSort", DataUtils.Sort.DESC.toString());
            paramMap.put("id", id);
            List<Map<String, Object>> list = invoiceInfoModelMapper.getList(paramMap);
            if (list.size() > 0)
            {
                resultMap = list.get(0);
                Integer type = MapUtils.getInteger(resultMap, "type");
                if (type.equals(InvoiceHeaderModel.ENTERPRISE))
                {
                    resultMap.put("typeDesc", "企业");
                }
                else
                {
                    resultMap.put("typeDesc", "个人");
                }
                Integer invoiceStatus = MapUtils.getInteger(resultMap, "invoice_status");
                switch (invoiceStatus)
                {
                    case 1:
                        resultMap.put("invoiceStatusDesc", "申请中");
                        break;
                    case 2:
                        resultMap.put("invoiceStatusDesc", "已完成");
                        break;
                    case 3:
                        resultMap.put("invoiceStatusDesc", "已撤销");
                        break;
                    default:
                        break;
                }
                resultMap.put("add_time", DateUtil.dateFormate(MapUtils.getString(resultMap, "add_time"), GloabConst.TimePattern.YMDHMS));
                String fileTime = MapUtils.getString(resultMap, "file_time");
                if (StringUtils.isNotEmpty(fileTime))
                {
                    resultMap.put("file_time", DateUtil.dateFormate(fileTime, GloabConst.TimePattern.YMDHMS));
                }
                String file    = MapUtils.getString(resultMap, "file");
                String imgPath = publiceService.getImgPath(file, vo.getStoreId());
                resultMap.put("file", imgPath);
                String             invoiceHeader      = MapUtils.getString(resultMap, "invoice_header");
                InvoiceHeaderModel invoiceHeaderModel = JSONObject.parseObject(invoiceHeader, InvoiceHeaderModel.class);
                resultMap.put("invoice_header", invoiceHeaderModel);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "目标不存在", "getInfo");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看发票详情 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadInvoice(MainVo vo, Integer id, String filePath) throws LaiKeAPIException
    {
        try
        {
            if (Objects.isNull(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择需要上传文件的发票", "uploadInvoice");
            }
            if (StringUtils.isEmpty(filePath))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "上传文件不得为空", "uploadInvoice");
            }
            User             user             = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            InvoiceInfoModel invoiceInfoModel = invoiceInfoModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(invoiceInfoModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "目标不存在", "getInfo");
            }
            invoiceInfoModel.setFile(ImgUploadUtils.getUrlImgByName(filePath, true));
            invoiceInfoModel.setFile_time(new Date());
            invoiceInfoModel.setInvoice_status(InvoiceInfoModel.COMPLETED);
            invoiceInfoModelMapper.updateByPrimaryKeySelective(invoiceInfoModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传发票文件 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadInvoice");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delInvoice(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择需要删除的发票信息", "delInvoice");
            }
            String[] split = ids.split(SplitUtils.DH);
            for (String id : split)
            {
                InvoiceInfoModel invoiceInfoModel = invoiceInfoModelMapper.selectByPrimaryKey(id);
                if (Objects.isNull(invoiceInfoModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败！id为" + id + "的发票信息不存在", "delInvoice");
                }
                invoiceInfoModel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
                invoiceInfoModelMapper.updateByPrimaryKeySelective(invoiceInfoModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除发票信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delInvoice");
        }
    }

    private void exportListData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"发票id", "昵称", "抬头类型", "发票抬头", "税号", "电子邮箱", "开票状态", "订单号", "发票金额", "提交时间", "上传时间"};
            //对应字段
            String[] kayList = new String[]{"id", "user_name", "typeDesc", "company_name", "company_tax_number", "email", "invoiceStatusDesc", "s_no", "invoice_amount", "add_time", "file_time"};
            //对应字段
            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("发票列表信息");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出订单列表数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportOrderData");
        }

    }
}

