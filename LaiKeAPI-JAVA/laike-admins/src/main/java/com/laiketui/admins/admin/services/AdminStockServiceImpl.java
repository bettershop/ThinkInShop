package com.laiketui.admins.admin.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.AdminStockService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ConfiGureModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.StockModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataSerializeHelp;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.goods.StockInfoVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 库存管理
 *
 * @author Trick
 * @date 2021/1/4 9:14
 */
@Service
public class AdminStockServiceImpl implements AdminStockService
{
    private final Logger logger = LoggerFactory.getLogger(AdminStockServiceImpl.class);


    @Override
    public Map<String, Object> getStockInfo(StockInfoVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_number", vo.getProductNumber());
            }
            if (!StringUtils.isEmpty(vo.getMchName()))
            {
                parmaMap.put("mch_name_like", vo.getMchName());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_title_like", vo.getProductTitle());
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            Integer total = stockModelMapper.stockInfoCountDynamic(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            if (vo.getExportType() == 1)
            {
                List<Map<String, Object>> maps = stockModelMapper.exportQuery(parmaMap);
                for (Map<String, Object> map : maps)
                {
                    //属性处理
                    String attribute = map.get("attribute") + "";
                    attribute = DataSerializeHelp.getAttributeStr(attribute);
                    map.put("specifications", attribute);
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    map.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(map, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
                }
                exportStockData(maps, response);
                return null;
            }
            else
            {
                List<Map<String, Object>> list = stockModelMapper.getStorckInfoDynamic(parmaMap);
                data(vo.getStoreId(), list);
                resultMap.put("list", list);
                resultMap.put("total", total);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品库存信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockInfo");
        }
        return resultMap;
    }

    //导出库存列表
    private void exportStockData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品总库存", "剩余库存", "供货商", "上架时间"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "total_num", "num", "shop_name", "upper_shelf_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品库存数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockData");
        }
    }

    @Override
    public Map<String, Object>  getStockDetailInfo(StockInfoVo vo, Integer attrId, Integer pid, Integer type, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("product_id", pid);
            parmaMap.put("attribute_id", attrId);
            if (type != null)
            {
                if (type == 3)
                {
                    List<Integer> types = new ArrayList<>();
                    types.add(0);
                    types.add(1);
                    parmaMap.put("stockTypeList", types);
                }
                else if (type == 2)
                {
                    parmaMap.put("stockType", type);
                    parmaMap.put("group_attr_id", "group_attr_id");
                    //库存大于库存预警不显示
                    parmaMap.put("ltNum","ltNum");
                    //预售商品不显示
                    parmaMap.put("is_presell","is_presell");
                }
                else
                {
                    parmaMap.put("stockType", type);
                }
            }
            if (!StringUtils.isEmpty(vo.getMchName()))
            {
                parmaMap.put("mch_name", vo.getMchName());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_title_like", vo.getProductTitle());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
            }
            if (!StringUtils.isEmpty(vo.getEndDate()))
            {
                parmaMap.put("endDate", vo.getEndDate());
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            int total = stockModelMapper.goodsStockInfoCountDynamic(parmaMap);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> list = stockModelMapper.getGoodsStorckInfoDynamic(parmaMap);
            //排序
            String sortCriteria = vo.getSortCriteria();
            String sortOrder    = vo.getSort();
            if (Objects.nonNull(sortCriteria) && Objects.nonNull(sortOrder))
            {
                Comparator<Map<String, Object>> comparator = getMapComparator(sortCriteria, sortOrder);
                list.sort(comparator);
            }
            data(vo.getStoreId(), list);

            if (vo.getExportType() == 1 && type != null)
            {
                //0.入库 1.出库 2.预警
                if (type == 0)
                {
                    exportStockAddData(list, response);
                }
                else if (type == 1)
                {
                    exportStockOutData(list, response);
                }
                else if (type == 2)
                {
                    exportStockWarningData(list, response);
                }
                return null;
            }


            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品详细库存信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStockDetailInfo");
        }
        return resultMap;
    }

    /**
     * 排序
     * @param sortCriteria
     * @param sortOrder
     * @return
     */
    private Comparator<Map<String, Object>> getMapComparator(String sortCriteria, String sortOrder)
    {
        Comparator<Map<String, Object>> comparator = null;
        if (sortCriteria.equals("num"))
        {
            comparator = Comparator.comparing(o -> MapUtils.getIntValue(o, "num"));
        }
        else
        {
            comparator = Comparator.comparing(o -> MapUtils.getString(o, "upper_shelf_time"),Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (sortOrder.equals("desc"))
        {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    //导出库存预警列表
    private void exportStockWarningData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "商品总库存", "剩余库存", "店铺", "上架时间"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "num", "name", "upper_shelf_time"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存预警列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品库存数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockData");
        }
    }

    //导出库存入库列表
    private void exportStockAddData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "商品总库存", "入库数量", "入库时间", "店铺"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "flowing_num", "add_date", "name"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存入库列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品库存数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockAddData");
        }
    }

    //导出库存出库列表
    private void exportStockOutData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品名称", "商品售价", "商品规格", "商品状态", "剩余库存", "出库数量", "店铺", "出库时间"};
            //对应字段
            String[]     kayList = new String[]{"product_title", "price", "specifications", "statusName", "total_num", "flowing_num", "name", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("库存出库列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出库存出库列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportStockOutData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addStock(AddStockVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user  = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count = productListModelMapper.addGoodsStockNum(vo.getPid(), vo.getAddNum());
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败", "addStock");
            }
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setTotal_num(vo.getAddNum());
            confiGureModel.setNum(vo.getAddNum());
            confiGureModel.setId(vo.getId());
            confiGureModel.setPid(vo.getPid());
            count = confiGureModelMapper.addGoodsAttrStockNum(confiGureModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败", "addStock");
            }
            //获取库存信息
            confiGureModel = new ConfiGureModel();
            confiGureModel.setId(vo.getId());
            confiGureModel.setPid(vo.getPid());
            confiGureModel = confiGureModelMapper.selectOne(confiGureModel);


            String     text       = "";
            StockModel stockModel = new StockModel();
            if (vo.getAddNum() >= 0)
            {
                stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                text = user.getName() + "增加商品库存" + vo.getAddNum();
                publiceService.addAdminRecord(vo.getStoreId(), "添加了商品ID：" + vo.getPid() + "," + vo.getAddNum() + "个库存", AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                vo.setAddNum(new BigDecimal(vo.getAddNum()).negate().intValue());
                stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
                text = user.getName() + "减少商品库存" + vo.getAddNum();
                publiceService.addAdminRecord(vo.getStoreId(), "扣除了商品ID：" + vo.getPid() + "," + vo.getAddNum() + "个库存", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            stockModel.setStore_id(vo.getStoreId());
            stockModel.setProduct_id(vo.getPid());
            stockModel.setAttribute_id(vo.getId());
            stockModel.setTotal_num(confiGureModel.getTotal_num());
            stockModel.setFlowing_num(vo.getAddNum());
            stockModel.setContent(text);
            stockModel.setAdd_date(new Date());
            count = stockModelMapper.insertSelective(stockModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败", "addStock");
            }
            //是否需要库存添加库存预警
            if (!publicStockService.addStockWarning(vo.getStoreId(), vo.getId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败", "addStock");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStock");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> batchAddStock(AddStockVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel    user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String        cids     = vo.getIds();
            List<Integer> pidsList = JSON.parseArray(cids, Integer.class);
            if (Objects.isNull(cids) || Objects.isNull(pidsList))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败", "addStock");
            }
            AddStockVo     tempVo         = null;
            ConfiGureModel confiGureModel = null;
            for (Integer cid : pidsList)
            {
                tempVo = new AddStockVo();
                BeanUtils.copyProperties(vo, tempVo);
                confiGureModel = confiGureModelMapper.selectByPrimaryKey(cid);
                tempVo.setPid(confiGureModel.getPid());
                tempVo.setId(confiGureModel.getId());
                this.addStock(tempVo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量添加库存异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStock");
        }
        return resultMap;
    }


    /**
     * 数据处理
     *
     * @param storeId               -
     * @param outExcelStockInfoList -
     * @author Trick
     * @date 2021/1/4 13:08
     */
    private void data(int storeId, List<Map<String, Object>> outExcelStockInfoList)
    {
        for (Map<String, Object> map : outExcelStockInfoList)
        {
            //图片处理
            String imgUrl = map.get("imgurl").toString();
            imgUrl = publiceService.getImgPath(imgUrl, storeId);
            map.put("imgurl", imgUrl);
            //属性处理
            String attribute = map.get("attribute") + "";
            attribute = DataSerializeHelp.getAttributeStr(attribute);
            map.put("specifications", attribute);

            int status = Integer.parseInt(map.get("status").toString());
            map.put("statusName", GoodsDataUtils.getGoodsStatusStr(status));

            map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
            map.put("upper_shelf_time", DateUtil.dateFormate(MapUtils.getString(map, "upper_shelf_time"), GloabConst.TimePattern.YMDHMS));
        }
    }

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PublicStockService publicStockService;
}

