package com.laiketui.admins.admin.services.saas;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laiketui.admins.api.admin.saas.AdminDataDictionaryManageService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.SkuModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 数据字典管理
 *
 * @author Trick
 * @date 2021/2/3 9:29
 */
@Service
public class AdminDataDictionaryManageServiceImpl implements AdminDataDictionaryManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminDataDictionaryManageServiceImpl.class);

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getSkuInfo(MainVo vo, Integer id, Integer sid, String dataCode, String dataName, String key, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (id != null)
            {
                parmaMap.put("id", id);
            }
            else
            {
                parmaMap.put("pageStart", vo.getPageNo());
                parmaMap.put("pageEnd", vo.getPageSize());
            }
            if (!StringUtils.isEmpty(dataCode))
            {
                parmaMap.put("code", dataCode);
            }
            if (!StringUtils.isEmpty(dataName))
            {
                parmaMap.put("nameLike", dataName);
            }
            if (sid != null)
            {
                parmaMap.put("sid", sid);
            }
            if (StringUtils.isNotEmpty(key))
            {
                parmaMap.put("name", key);
            }
            parmaMap.put("add_date_sort", "desc");

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                parmaMap.put("lang_code", language);
//            }

            int                       total    = skuModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> dataList = skuModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                //获取明细
                if (id != null)
                {
                    //获取属性值集
                    Map<String, Object> parmaSkuMap = new HashMap<>(16);
                    parmaSkuMap.put("sid", id);
                    parmaSkuMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                    parmaSkuMap.put("pageStart", vo.getPageNo());
                    parmaSkuMap.put("pageEnd", vo.getPageSize());
                    if (StringUtils.isNotEmpty(langCode))
                    {
                        parmaSkuMap.put("lang_code", langCode);
                    }
                    int                       sunSkustotal = skuModelMapper.countDynamic(parmaSkuMap);
                    List<Map<String, Object>> sunSkusList  = skuModelMapper.selectDynamic(parmaSkuMap);
                    sunSkusList.forEach(model ->
                    {
                        model.put("lang_name", publiceService.getLangName(MapUtils.getString(model, "lang_code")));
                        model.put("country_name", publiceService.getCountryName(MapUtils.getInteger(model, "country_num")));
                    });
                    map.put("sunSkusTotal", sunSkustotal);
                    map.put("sunSkus", sunSkusList);
                }
                //是否生效 0:不是 1:是 status
                String statusName = "失效";
                if (MapUtils.getIntValue(map, "status") == 1)
                {
                    statusName = "生效";
                }
                map.put("statusName", statusName);
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                Integer countryNum = MapUtils.getIntValue(map, "country_num");
                logger.error("countryNum:{}", countryNum);
                logger.error("map:{}", JSON.toJSONString(map));
                if (countryNum == null)
                {
                    //中国
                    countryNum = 156;
                }
                map.put("country_name", publiceService.getCountryName(countryNum));
            }
            if (vo.getExportType().equals(1))
            {
                exportSkuData(dataList, response);
                return null;
            }

            resultMap.put("total", total);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品属性信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSkuInfo");
        }
        return resultMap;
    }

    //导出sku
    private void exportSkuData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"属性编码", "属性名称", "添加人", "添加时间", "是否生效"};
            //对应字段
            String[]     kayList = new String[]{"code", "name", "admin_name", "add_date", "statusName"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("sku列表");
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
            logger.error("导出[sku]数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportSkuData");
        }
    }

    @Override
    public Map<String, Object> getSkuAttributeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("type", SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                parmaMap.put("lang_code", language);
//            }
            int                       total    = skuModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (total > 0)
            {
                dataList = skuModelMapper.selectDynamic(parmaMap);
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性名称下拉 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSkuAttributeList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getSkuList(MainVo vo, String keyword, String jsonStrArr) throws LaiKeAPIException
    {
        Map<String, Object>       resultMap = new HashMap<>(16);
        ObjectMapper              mapper    = new ObjectMapper();
        List<Map<String, Object>> strArr    = new ArrayList<>();
        try
        {
            if (StringUtils.isEmpty(keyword))
            {
                keyword = null;
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isEmpty(langCode))
            {
                langCode = GloabConst.Lang.CN;
            }

            int total = skuModelMapper.countSkuList(keyword, langCode);
            if (total > 0)
            {
                List<Map<String, Object>> dataList = skuModelMapper.getSkuList(vo.getPageNo(), vo.getPageSize(), keyword, langCode);
                if (!StringUtils.isEmpty(jsonStrArr))
                {
                    JsonNode jsonNode = mapper.readTree(jsonStrArr);

                    for (JsonNode jsonElement : jsonNode)
                    {
                        // 将每个JsonNode转换为Map
                        Map<String, Object> map = mapper.convertValue(jsonElement, Map.class);
                        // 将Map添加到List中
                        strArr.add(map);
                    }
                }
                for (Map<String, Object> map : dataList)
                {
                    strArr.add(map);
                }
            }

            resultMap.put("list", strArr);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品属性/属性值列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSkuList");
        }
        return resultMap;
    }

    @Override
    public boolean setSkuSwitch(String token, Integer id) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            SkuModel   skuModel   = new SkuModel();
            skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            skuModel.setId(id);
            skuModel = skuModelMapper.selectOne(skuModel);
            if (skuModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCZ, "属性不存在");
            }
            Integer status = SkuModel.SKU_STATUS_TAKE_EFFECT;
            if (status.equals(skuModel.getStatus()))
            {
                status = SkuModel.SKU_STATUS_INVALID;
            }

            //获取子级sku id
            List<Integer> skuIdList = skuModelMapper.getChildSkuList(id);
            skuIdList.add(id);

            boolean flag = false;
            for (Integer skuId : skuIdList)
            {
                SkuModel skuModelUpdate = new SkuModel();
                skuModelUpdate.setId(skuId);
                skuModelUpdate.setStatus(status);
                int count = skuModelMapper.updateByPrimaryKeySelective(skuModelUpdate);
                flag = count > 0;
            }
            //添加操作日志
            publiceService.addAdminRecord(adminModel.getStore_id(), "将SKU属性：" + skuModel.getName() + "进行了是否生效操作", AdminRecordModel.Type.OPEN_OR_CLOSE, token);
            return flag;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("商品属性生效开关 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setSkuSwitch");
        }
    }

    @Override
    public boolean addSkuName(Integer id, String skuName, int isOpen, String token) throws LaiKeAPIException
    {
        try
        {
            int        count      = 0;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            if (adminModel != null)
            {
                if (StringUtils.isEmpty(skuName))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCBNWK, "属性名称不能为空");
                }

                SkuModel skuModelOld = null;
                if (id != null)
                {
                    skuModelOld = new SkuModel();
                    skuModelOld.setId(id);
                    skuModelOld.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    skuModelOld = skuModelMapper.selectOne(skuModelOld);
                    if (skuModelOld == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCZ, "属性不存在");
                    }
                    //只有未生效的情况下才能修改
                    if (SkuModel.SKU_STATUS_TAKE_EFFECT.equals(skuModelOld.getStatus()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXSXZJZXG, "属性生效中,禁止修改");
                    }
                }
                SkuModel skuModelSave = new SkuModel();
                skuModelSave.setAdd_date(new Date());
                if (skuModelOld == null || !skuModelOld.getName().equals(skuName))
                {
                    SkuModel skuModel = new SkuModel();
                    skuModel.setName(skuName);
                    skuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    count = skuModelMapper.selectCount(skuModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCYCZ, "属性名称已存在");
                    }
                    skuModelSave.setName(skuName);
                }
                if (skuModelOld != null)
                {
                    skuModelSave.setId(id);
                    count = skuModelMapper.updateByPrimaryKeySelective(skuModelSave);
                }
                else
                {
                    String code = publicGoodsService.getSkuCode(adminModel.getStore_id(), skuName, null);
                    skuModelSave.setCode(code);
                    skuModelSave.setAdmin_name(adminModel.getName());
                    skuModelSave.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModelSave.setStatus(isOpen);
                    skuModelSave.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    count = skuModelMapper.insertSelective(skuModelSave);
                }
            }
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改数据名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSkuName");
        }
    }

    @Override
    public boolean addSkuName(Integer id, String skuName, int isOpen, MainVo vo) throws LaiKeAPIException
    {
        try
        {
            int        count      = 0;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (adminModel != null)
            {
                if (StringUtils.isEmpty(skuName))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCBNWK, "属性名称不能为空");
                }

                SkuModel skuModelOld = null;
                if (id != null)
                {
                    skuModelOld = new SkuModel();
                    skuModelOld.setId(id);
                    skuModelOld.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    skuModelOld = skuModelMapper.selectOne(skuModelOld);
                    if (skuModelOld == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCZ, "属性不存在");
                    }
                    //只有未生效的情况下才能修改
                    if (SkuModel.SKU_STATUS_TAKE_EFFECT.equals(skuModelOld.getStatus()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXSXZJZXG, "属性生效中,禁止修改");
                    }
                }
                SkuModel skuModelSave = new SkuModel();

                String langCode = vo.getLang_code();
                if (StringUtils.isNotEmpty(langCode))
                {
                    skuModelSave.setLang_code(langCode);
                }

                //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//                String language = vo.getLanguage();
//                if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//                {
//                    logger.info("默认语种:{}",language);
//                    skuModelSave.setLang_code(language);
//                }


                Integer countryNum = vo.getCountry_num();
                if (countryNum != null)
                {
                    skuModelSave.setCountry_num(countryNum);
                }

                skuModelSave.setAdd_date(new Date());
                if (skuModelOld == null || !skuModelOld.getName().equals(skuName))
                {
                    SkuModel skuModel = new SkuModel();
                    skuModel.setName(skuName);
                    skuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    count = skuModelMapper.selectCount(skuModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCYCZ, "属性名称已存在");
                    }
                    skuModelSave.setName(skuName);
                }
                if (skuModelOld != null)
                {
                    skuModelSave.setId(id);
                    count = skuModelMapper.updateByPrimaryKeySelective(skuModelSave);
                }
                else
                {
                    String code = publicGoodsService.getSkuCode(adminModel.getStore_id(), skuName, null);
                    skuModelSave.setCode(code);
                    skuModelSave.setAdmin_name(adminModel.getName());
                    skuModelSave.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
                    skuModelSave.setStatus(isOpen);
                    skuModelSave.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    count = skuModelMapper.insertSelective(skuModelSave);
                }
            }
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改数据名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSkuName");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSku(int sid, List<String> attributeList, String token, int type) throws LaiKeAPIException
    {
        try
        {
            int        count;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            if (attributeList == null || attributeList.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZBNWK, "属性值不能为空");
            }
            SkuModel skuModelOld = new SkuModel();
            skuModelOld.setId(sid);
            skuModelOld.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            skuModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            skuModelOld = skuModelMapper.selectOne(skuModelOld);
            if (skuModelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCBCZ, "属性名称不存在");
            }
            //标记属性是否使用中,如果使用中则不能删除,但是可以添加
            boolean isUse = false;
            //获取当前属性下全部的值
            SkuModel skuModel = new SkuModel();
            skuModel.setSid(skuModelOld.getId());
            skuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
            skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<SkuModel> skuModelSunList = new ArrayList<>();
            if (type == 1)
            {
                //只有编辑的是时候需要
                skuModelSunList = skuModelMapper.select(skuModel);
                //判断该属性是否使用中,使用中则不能删除
                if (skuModelMapper.countSkuIsUse(skuModelOld.getId()) > 0)
                {
                    isUse = true;
                }
            }

            //添加/修改
            List<String> skuNameTemp = new ArrayList<>();
            for (String value : attributeList)
            {
//                if (StringUtils.isEmpty(value)) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZBNWK, "属性值不能为空");
//                } else if (!DataCheckTool.checkLength(value, 1, 20)) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值长度为20个字符以内");
//                } else
                if (skuNameTemp.contains(value))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值:" + value + "重复");
                }
                skuNameTemp.add(value);
                SkuModel skuModelSave = new SkuModel();
                skuModelSave.setName(value);
                //获取属性信息
                SkuModel skuModelSunOld = null;
                for (SkuModel sku : skuModelSunList)
                {
                    if (sku.getName().equals(value))
                    {
                        skuModelSunOld = sku;
                        //每次处理一个属性则移除一个,剩下的则删除
                        skuModelSunList.remove(sku);
                        break;
                    }
                }
                if (skuModelSunOld != null)
                {
                    skuModelSave.setId(skuModelSunOld.getId());
                    count = skuModelMapper.updateByPrimaryKeySelective(skuModelSave);
                    //添加操作日志
                    publiceService.addAdminRecord(adminModel.getStore_id(), "修改了SKU属性：" + skuModelOld.getName() + "的信息", AdminRecordModel.Type.UPDATE, token);
                }
                else
                {
                    if (type == 0)
                    {
                        SkuModel skuCount = new SkuModel();
                        skuCount.setSid(skuModelOld.getId());
                        skuCount.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                        skuCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        skuCount.setName(value);
                        count = skuModelMapper.selectCount(skuCount);
                        if (count > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值:" + value + "重复");
                        }
                    }
                    skuModelSave.setSid(skuModelOld.getId());
                    skuModelSave.setStore_id(skuModelOld.getStore_id());
                    skuModelSave.setAdmin_name(adminModel.getName());
                    skuModelSave.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    skuModelSave.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                    String code = publicGoodsService.getSkuCode(adminModel.getStore_id(), null, skuModelOld.getId());
                    skuModelSave.setCode(code);
                    skuModelSave.setAdd_date(new Date());
                    skuModelSave.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    count = skuModelMapper.insertSelective(skuModelSave);
                    //添加操作日志
                    publiceService.addAdminRecord(adminModel.getStore_id(), "添加了SKU属性：" + skuModelSave.getName(), AdminRecordModel.Type.ADD, token);
                }
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                }
            }

            //删除剩下的属性值
            List<Integer> delAttributeList = new ArrayList<>();
            for (SkuModel sku : skuModelSunList)
            {
                delAttributeList.add(sku.getId());
                if (isUse)
                {
                    logger.debug("属性操作失败【{}】", sku.getName());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXSYZWFSC, "属性使用中,操作失败");
                }
            }
            try
            {
                delSku(token, delAttributeList);
            }
            catch (LaiKeAPIException l)
            {
                logger.debug("sku 删除失败 {}", l.getMessage());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改商品属性 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSku");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSku(int sid, List<String> attributeList, MainVo vo, int type) throws LaiKeAPIException
    {
        try
        {
            int        count;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (attributeList == null || attributeList.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZBNWK, "属性值不能为空");
            }
            SkuModel skuModelOld = new SkuModel();
            skuModelOld.setId(sid);
            skuModelOld.setType(SkuModel.SKU_TYPE_ATTRIBUTE_NAME);
            skuModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            skuModelOld = skuModelMapper.selectOne(skuModelOld);
            if (skuModelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXMCBCZ, "属性名称不存在");
            }
            //标记属性是否使用中,如果使用中则不能删除,但是可以添加
            boolean isUse = false;
            //获取当前属性下全部的值
            SkuModel skuModel = new SkuModel();
            skuModel.setSid(skuModelOld.getId());
            skuModel.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
            skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<SkuModel> skuModelSunList = new ArrayList<>();
            if (type == 1)
            {
                //只有编辑的是时候需要
                skuModelSunList = skuModelMapper.select(skuModel);
                //判断该属性是否使用中,使用中则不能删除
                if (skuModelMapper.countSkuIsUse(skuModelOld.getId()) > 0)
                {
                    isUse = true;
                }
            }

            //添加/修改
            List<String> skuNameTemp = new ArrayList<>();
            for (String value : attributeList)
            {
//                if (StringUtils.isEmpty(value)) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZBNWK, "属性值不能为空");
//                } else if (!DataCheckTool.checkLength(value, 1, 20)) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值长度为20个字符以内");
//                } else
                if (skuNameTemp.contains(value))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值:" + value + "重复");
                }
                skuNameTemp.add(value);
                SkuModel skuModelSave = new SkuModel();
                skuModelSave.setName(value);

                String langCode = vo.getLang_code();
                if (StringUtils.isNotEmpty(langCode))
                {
                    skuModelSave.setLang_code(langCode);
                }

                Integer countryNum = vo.getCountry_num();
                if (countryNum != null)
                {
                    skuModelSave.setCountry_num(countryNum);
                }

                //获取属性信息
                SkuModel skuModelSunOld = null;
                for (SkuModel sku : skuModelSunList)
                {
                    if (sku.getName().equals(value))
                    {
                        skuModelSunOld = sku;
                        //每次处理一个属性则移除一个,剩下的则删除
                        skuModelSunList.remove(sku);
                        break;
                    }
                }
                if (skuModelSunOld != null)
                {
                    skuModelSave.setId(skuModelSunOld.getId());
                    count = skuModelMapper.updateByPrimaryKeySelective(skuModelSave);
                    //添加操作日志
                    publiceService.addAdminRecord(adminModel.getStore_id(), "修改了SKU属性：" + skuModelOld.getName() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
                else
                {
                    if (type == 0)
                    {
                        SkuModel skuCount = new SkuModel();
                        skuCount.setSid(skuModelOld.getId());
                        skuCount.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                        skuCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        skuCount.setName(value);
                        count = skuModelMapper.selectCount(skuCount);
                        if (count > 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZZF, "属性值:" + value + "重复");
                        }
                    }
                    skuModelSave.setSid(skuModelOld.getId());
                    skuModelSave.setStore_id(skuModelOld.getStore_id());
                    skuModelSave.setAdmin_name(adminModel.getName());
                    skuModelSave.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    skuModelSave.setType(SkuModel.SKU_TYPE_ATTRIBUTE_VALUE);
                    String code = publicGoodsService.getSkuCode(adminModel.getStore_id(), null, skuModelOld.getId());
                    skuModelSave.setCode(code);
                    skuModelSave.setAdd_date(new Date());
                    skuModelSave.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    count = skuModelMapper.insertSelective(skuModelSave);
                    //添加操作日志
                    publiceService.addAdminRecord(adminModel.getStore_id(), "添加了SKU属性：" + skuModelSave.getName(), AdminRecordModel.Type.ADD, vo.getAccessId());
                }
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                }
            }

            //删除剩下的属性值
            List<Integer> delAttributeList = new ArrayList<>();
            for (SkuModel sku : skuModelSunList)
            {
                delAttributeList.add(sku.getId());
                if (isUse)
                {
                    logger.debug("属性操作失败【{}】", sku.getName());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXSYZWFSC, "属性使用中,操作失败");
                }
            }
            try
            {
                delSku(vo.getAccessId(), delAttributeList);
            }
            catch (LaiKeAPIException l)
            {
                logger.debug("sku 删除失败 {}", l.getMessage());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改商品属性 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSku");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delSku(String token, List<Integer> idList) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            int        count;
            if (idList != null)
            {
                for (Integer id : idList)
                {
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(id);
                    skuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    skuModel = skuModelMapper.selectOne(skuModel);
                    if (skuModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXBCZ, "属性不存在");
                    }
                    //还需要删除他的子id属性
                    SkuModel childSku = new SkuModel();
                    childSku.setSid(id);
                    List<SkuModel> childSkuIds = skuModelMapper.select(childSku);
                    for (SkuModel childSkuId : childSkuIds)
                    {
                        if (SkuModel.SKU_STATUS_TAKE_EFFECT.equals(childSkuId.getStatus()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBZSXSXZSCSB, "列表中属性生效中,删除失败");
                        }
                        SkuModel skuModelUpdate = new SkuModel();
                        skuModelUpdate.setId(childSkuId.getId());
                        skuModelUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                        count = skuModelMapper.updateByPrimaryKeySelective(skuModelUpdate);
                        if (count < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                        }
                    }
                    if (SkuModel.SKU_STATUS_TAKE_EFFECT.equals(skuModel.getStatus()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBZSXSXZSCSB, "列表中属性生效中,删除失败");
                    }

                    SkuModel skuModelUpdate = new SkuModel();
                    skuModelUpdate.setId(skuModel.getId());
                    skuModelUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                    count = skuModelMapper.updateByPrimaryKeySelective(skuModelUpdate);

                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                    }
                    //添加操作日志
                    publiceService.addAdminRecord(adminModel.getStore_id(), "删除了SKU属性：" + skuModel.getName(), AdminRecordModel.Type.DEL, token);
                }

            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量删除商品属性 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSku");
        }
    }

}

