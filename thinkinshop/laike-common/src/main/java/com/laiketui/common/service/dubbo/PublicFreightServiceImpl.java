package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicFreightService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.mapper.CurrencyStoreModelMapper;
import com.laiketui.common.mapper.MchFreightModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.freight.FreightRuleVO;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * 关于运费
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
@Service
public class PublicFreightServiceImpl implements PublicFreightService
{

    private final Logger                logger = LoggerFactory.getLogger(PublicFreightServiceImpl.class);
    @Autowired
    private MchFreightModelMapper mchFreightModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> GetFreightList(int storeId, int mchId, String name, Integer isUse, Integer otype, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费数量
            int total;
            //运费信息集
            List<Map<String, Object>> freightList;
            Map<String, Object>       parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("pageNo", pageModel.getPageNo());
            parmaMap.put("pageSize", pageModel.getPageSize());
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("likeName", name);
            }
            if (otype != null)
            {
                parmaMap.put("otype", otype);
            }
            if (isUse != null)
            {
                //使用状态
                if (isUse == 1)
                {
                    parmaMap.put("isNotNullId", "isNotNullId");
                }
                else if (isUse == 0)
                {
                    parmaMap.put("isNullId", "isNullId");
                }
            }
            total = mchFreightModelMapper.countFreightInfoLeftGoodsDynamic(parmaMap);
            freightList = mchFreightModelMapper.getFreightInfoLeftGoodsDynamic(parmaMap);
            for (Map<String, Object> map : freightList)
            {
                //运费规则
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "freight")))
                {
                    List<FreightRuleVO> freightRuleVos = JSON.parseArray(MapUtils.getString(map, "freight"), FreightRuleVO.class);
                    map.put("freight", freightRuleVos);
                }
                //不配送区域
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "no_delivery")))
                {
                    JSONArray objects = JSONArray.parseArray(MapUtils.getString(map, "no_delivery"));
                    map.put("no_delivery", objects);
                }
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                //包邮设置
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "package_settings")))
                {
                    resultMap.put("package_settings", MapUtils.getString(map, "package_settings"));
                }
                else
                {
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                String defaultFreight = MapUtils.getString(map, "default_freight");
                if (StringUtils.isNotEmpty(defaultFreight))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                    StringBuilder sb = new StringBuilder();
                    Integer type = MapUtils.getInteger(map, "type");
                    String rule= null;
                    String rule1 = null;
                    if (type == 0)
                    {
                        rule = String.format("%s件内%s元",defaultFreightVO.getNum1(),defaultFreightVO.getNum2());
                        rule1 = String.format("每加%s件，加%s元",defaultFreightVO.getNum3(),defaultFreightVO.getNum4());

                    }else if (type == 1)
                    {
                        rule = String.format("%s千克内%s元",defaultFreightVO.getNum1(),defaultFreightVO.getNum2());
                        rule1 = String.format("每加%s千克，加%s元",defaultFreightVO.getNum3(),defaultFreightVO.getNum4());
                    }
                    map.put("rule",rule);
                    map.put("rule1",rule1);
                    map.put("default_freight", defaultFreightVO);
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", freightList);
            resultMap.put("start", pageModel.getPageNum());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getFreightList(MainVo vo, int mchId, String name, Integer isUse, Integer otype, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费数量
            int total;
            //运费信息集
            List<Map<String, Object>> freightList;
            Map<String, Object>       parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("pageNo", pageModel.getPageNo());
            parmaMap.put("pageSize", pageModel.getPageSize());
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("likeName", name);
            }
            if (otype != null)
            {
                parmaMap.put("otype", otype);
            }
            if (isUse != null)
            {
                //使用状态
                if (isUse == 1)
                {
                    parmaMap.put("isNotNullId", "isNotNullId");
                }
                else if (isUse == 0)
                {
                    parmaMap.put("isNullId", "isNullId");
                }
            }

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

            total = mchFreightModelMapper.countFreightInfoLeftGoodsDynamic(parmaMap);
            freightList = mchFreightModelMapper.getFreightInfoLeftGoodsDynamic(parmaMap);
            for (Map<String, Object> map : freightList)
            {
                //运费规则
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "freight")))
                {
                    List<FreightRuleVO> freightRuleVos = JSON.parseArray(MapUtils.getString(map, "freight"), FreightRuleVO.class);
                    map.put("freight", freightRuleVos);
                }
                //不配送区域
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "no_delivery")))
                {
                    JSONArray objects = JSONArray.parseArray(MapUtils.getString(map, "no_delivery"));
                    map.put("no_delivery", objects);
                }
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));
                //包邮设置
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "package_settings")))
                {
                    resultMap.put("package_settings", MapUtils.getString(map, "package_settings"));
                }
                else
                {
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                String defaultFreight = MapUtils.getString(map, "default_freight");
                if (StringUtils.isNotEmpty(defaultFreight))
                {
                    DefaultFreightVO defaultFreightVO   = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                    StringBuilder    sb                 = new StringBuilder();
                    Integer          type               = MapUtils.getInteger(map, "type");
                    String           rule               = null;
                    String           rule1              = null;
                    Map              defaultCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
                    String           currencySymbol     = MapUtils.getString(defaultCurrencyMap, "currency_symbol");
                    if (type == 0)
                    {
                        rule = String.format("%s件内%s%s", defaultFreightVO.getNum1(), defaultFreightVO.getNum2(), currencySymbol);
                        rule1 = String.format("每加%s件，加%s%S", defaultFreightVO.getNum3(), defaultFreightVO.getNum4(), currencySymbol);

                    }
                    else if (type == 1)
                    {
                        rule = String.format("%s千克内%s%s", defaultFreightVO.getNum1(), defaultFreightVO.getNum2(), currencySymbol);
                        rule1 = String.format("每加%s千克，加%s%s", defaultFreightVO.getNum3(), defaultFreightVO.getNum4(), currencySymbol);
                    }
                    map.put("rule", rule);
                    map.put("rule1", rule1);
                    map.put("default_freight", defaultFreightVO);
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", freightList);
            resultMap.put("start", pageModel.getPageNum());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightList");
        }
        return resultMap;
    }

    @Override
    public boolean FreightToAdd(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            int          count;
            boolean      isUpdate        = false;
            FreightModel freightModelOld = new FreightModel();
            if (vo.getFid() != null && vo.getFid() > 0)
            {
                isUpdate = true;
                freightModelOld.setId(vo.getFid());
                freightModelOld = mchFreightModelMapper.selectOne(freightModelOld);
            }
            FreightModel freightModel = new FreightModel();
            freightModel.setIs_default(vo.getIsDefault());
            freightModel.setName(vo.getName());
            freightModel.setThreeIdsList(vo.getThreeIdsList());

            if (vo.getCountry_num() != null)
            {
                freightModel.setCountry_num(vo.getCountry_num());
            }

            if (StringUtils.isNotEmpty(vo.getLang_code()))
            {
                freightModel.setLang_code(vo.getLang_code());
            }

            //计算方式
            freightModel.setType(vo.getType());
            String yfRule = null;
            if (StringUtils.isNotEmpty(vo.getHiddenFreight()))
            {
                yfRule = URLDecoder.decode(vo.getHiddenFreight(), CharEncoding.UTF_8);
            }
            freightModel.setFreight(yfRule);
            if (vo.getSupplierId() != null)
            {
                freightModel.setSupplier_id(vo.getSupplierId());
            }
            //验证数据
            freightModel = DataCheckTool.checkFreightDataFormate(freightModel);
            if (!isUpdate || !freightModelOld.getName().equals(vo.getName()))
            {
                //验证规则名称是否已存在
                FreightModel freight = new FreightModel();
                freight.setStore_id(vo.getStoreId());
                if (vo.getSupplierId() != null)
                {
                    freight.setSupplier_id(vo.getSupplierId());
                }
                if (vo.getShopId() != null)
                {
                    freight.setMch_id(vo.getShopId());
                }
                freight.setName(freightModel.getName());
                count = mchFreightModelMapper.selectCount(freight);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZMCYCZQZXXZLYGMC, "模板名称重复,请重新输入", "freightAdd");
                }
            }
            //不配送区域
            String bpsRule = null;
            //不配送区域
            if (StringUtils.isNotEmpty(vo.getNoDelivery()))
            {
                List<Map<String, Object>> freightList = JSON.parseObject(yfRule, new TypeReference<List<Map<String, Object>>>()
                {
                });
                List<String> cityList = new ArrayList<>();
                freightList.stream().forEach(freight ->
                {
                    String[] names = MapUtils.getString(freight, "name").split(SplitUtils.DH);
                    cityList.addAll(Arrays.asList(names));
                });
                bpsRule = URLDecoder.decode(vo.getNoDelivery(), CharEncoding.UTF_8);
                //二维数组遍历[["1"],["3"],["2"]]
                JSONArray objects = JSONArray.parseArray(bpsRule);
                for (Object array : objects)
                {
                    if (cityList.contains(array.toString()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDYFHBFDQCF, "不配送区域与运费规则中有重复省份", "freightAdd");
                    }
                }
                freightModel.setNo_delivery(bpsRule);
            }
            //默认运费规则
            if (StringUtils.isNotEmpty(vo.getDefaultFreight()))
            {
                String defaultFreight = URLDecoder.decode(vo.getDefaultFreight(), CharEncoding.UTF_8);
                freightModel.setDefault_freight(defaultFreight);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRMRYFGZ, "请输入默认运费规则", "freightAdd");
            }
            //包邮设置
            if (StringUtils.isNotEmpty(vo.getPackageSettings()))
            {
                freightModel.setPackage_settings(vo.getPackageSettings());
            }
            //判断是否是设置默认,如果是设置默认,则取消原来默认,修改当前为默认。
            if (vo.getIsDefault() != null && vo.getIsDefault() == 1)
            {
                mchFreightModelMapper.setSupplierDefault(vo.getStoreId(), vo.getSupplierId(), 0, 1);
            }
            //添加运费
            if (isUpdate)
            {
                freightModel.setId(freightModelOld.getId());
                //是否是包邮设置 0.未开启 1.开启
                freightModel.setIs_package_settings(vo.getIsPackageSettings());
                //是否不配送 0.未开启 1.开启
                freightModel.setIs_no_delivery(vo.getIsNoDelivery());
                count = mchFreightModelMapper.updateByPrimaryKeySelective(freightModel);

                publiceService.addAdminRecord(vo.getStoreId(), "修改了运费模板ID：" + freightModel.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                freightModel.setStore_id(vo.getStoreId());
                freightModel.setMch_id(vo.getShopId());
                freightModel.setAdd_time(new Date());
                //是否是包邮设置 0.未开启 1.开启
                freightModel.setIs_package_settings(vo.getIsPackageSettings());
                //是否不配送 0.未开启 1.开启
                freightModel.setIs_no_delivery(vo.getIsNoDelivery());
                //第一条默认为默认
                FreightModel first = new FreightModel();
                first.setStore_id(vo.getStoreId());
                if (vo.getSupplierId() != null)
                {
                    freightModel.setSupplier_id(vo.getSupplierId());
                    first.setSupplier_id(vo.getSupplierId());
                }
                else
                {
                    first.setMch_id(vo.getShopId());
                    //不同语种的第一条为默认运费
                    if (StringUtils.isNotEmpty(vo.getLang_code()))
                    {
                        first.setLang_code(vo.getLang_code());
                    }
                }
                int i = mchFreightModelMapper.selectCount(first);
                if (i < 1)
                {
                    freightModel.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                count = mchFreightModelMapper.insertSelective(freightModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了运费模板ID：" + freightModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (count < 1)
            {
                logger.debug("运费添加/修改失败 参数:{}", JSON.toJSONString(freightModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFTJXGSB, "运费添加/修改失败", "freightAdd");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightAdd");
        }
    }

    @Override
    public Map<String, Object> FreightAndModifyShow(int storeId, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费id数组
            List selCityArr = new ArrayList();
            //运费id字符串集  返回String数组使用
//            StringBuilder selCityArr = new StringBuilder();
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            if (mchId > 0)
            {
                freightModel.setMch_id(mchId);
            }
            freightModel.setId(id);
            freightModel = mchFreightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //运费规则
                List<FreightRuleVO> freightRuleVos = new ArrayList<>();
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    freightRuleVos = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                }
                resultMap.put("list", freightRuleVos);
                resultMap.put("freight", freightRuleVos);
                //不配送区域
                if (StringUtils.isNotEmpty(freightModel.getNo_delivery()))
                {
                    JSONArray objects = JSONArray.parseArray(freightModel.getNo_delivery());
                    resultMap.put("no_delivery", objects);
                }
                //默认运费规则
                if (StringUtils.isNotEmpty(freightModel.getDefault_freight()))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                    resultMap.put("default_freight", defaultFreightVO);
                }
                //是否包邮设置
                resultMap.put("is_package_settings", freightModel.getIs_package_settings().toString());
                //包邮设置
                resultMap.put("package_settings", freightModel.getPackage_settings());
                //是否不配送
                resultMap.put("is_no_delivery", freightModel.getIs_no_delivery().toString());

                resultMap.put("is_default", freightModel.getIs_default());
                resultMap.put("name", freightModel.getName());
                resultMap.put("type", freightModel.getType().toString());
                //运费id字符串集  返回String数组使用
                resultMap.put("sel_city_arr", selCityArr);
                resultMap.put("lang_code", freightModel.getLang_code());
                resultMap.put("country_num", freightModel.getCountry_num());
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "freightModifyShow");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载编辑运费页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightModifyShow");
        }
    }

    @Override
    public Map<String, Object> freightAndModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费id数组
            List selCityArr = new ArrayList();
            //运费id字符串集  返回String数组使用
//            StringBuilder selCityArr = new StringBuilder();
            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(vo.getStoreId());
            if (mchId > 0)
            {
                freightModel.setMch_id(mchId);
            }
            freightModel.setId(id);
            freightModel = mchFreightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //运费规则
                List<FreightRuleVO> freightRuleVos = new ArrayList<>();
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    freightRuleVos = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                }
                resultMap.put("list", freightRuleVos);
                resultMap.put("freight", freightRuleVos);
                //不配送区域
                if (StringUtils.isNotEmpty(freightModel.getNo_delivery()))
                {
                    JSONArray objects = JSONArray.parseArray(freightModel.getNo_delivery());
                    resultMap.put("no_delivery", objects);
                }
                //默认运费规则
                if (StringUtils.isNotEmpty(freightModel.getDefault_freight()))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                    resultMap.put("default_freight", defaultFreightVO);
                }
                //是否包邮设置
                resultMap.put("is_package_settings", freightModel.getIs_package_settings().toString());
                //包邮设置
                resultMap.put("package_settings", freightModel.getPackage_settings());
                //是否不配送
                resultMap.put("is_no_delivery", freightModel.getIs_no_delivery().toString());

                resultMap.put("is_default", freightModel.getIs_default());
                resultMap.put("name", freightModel.getName());
                resultMap.put("lang_name", publiceService.getLangName(vo.getLang_code()));
                resultMap.put("lang_code", freightModel.getLang_code());
                resultMap.put("country_name", publiceService.getCountryName(freightModel.getCountry_num()));
                resultMap.put("country_num", freightModel.getCountry_num());
                resultMap.put("type", freightModel.getType().toString());
                //运费id字符串集  返回String数组使用
//                resultMap.put("sel_city_arr", StringUtils.trim(selCityArr.toString(), ",").split(","));
                resultMap.put("sel_city_arr", selCityArr);
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "freightModifyShow");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载编辑运费页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightModifyShow");
        }
    }

    @Override
    public void FreightToDel(int storeId, String ids) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(ids))
            {
                int      supplierId;
                String[] fidList = ids.split(",");
                for (String id : fidList)
                {
                    //如果有商品应用了该运费,则不能删除
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setStore_id(storeId);
                    productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    productListModel.setFreight(id);
                    int count = productListModelMapper.selectCount(productListModel);
                    if (count > 0)
                    {
                        logger.info("含有商品设置该运费规则,不能删除 参数:" + JSON.toJSONString(productListModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYSPSZGYFGZBNSC, "此运费规则已绑定商品不能删除", "freightDel");
                    }
                    FreightModel freightMch   = mchFreightModelMapper.selectByPrimaryKey(id);
                    FreightModel freightModel = new FreightModel();
                    freightModel.setId(Integer.parseInt(id));
                    count = mchFreightModelMapper.deleteByPrimaryKey(freightModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙,请重试", "freightDel");
                    }
                    FreightModel defaultFreight = new FreightModel();
                    if (freightMch.getSupplier_id() != null)
                    {
                        //删除默认运费则将下一个运费设为默认
                        supplierId = freightMch.getSupplier_id();
                        defaultFreight.setSupplier_id(supplierId);
                    }
                    else
                    {
                        supplierId = 0;
                    }
                    defaultFreight.setStore_id(storeId);
                    defaultFreight.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                    count = mchFreightModelMapper.selectCount(defaultFreight);
                    if (count == 0)
                    {
                        mchFreightModelMapper.setSupplierDefault(storeId, supplierId, 1, 0);
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFCS, "非法参数", "freightDel");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightDel");
        }
    }

    @Override
    public void setDefaultFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getFid() == null)
            {
                logger.debug("运费id不能为空");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据");
            }
            FreightModel freightModel = new FreightModel();
            freightModel.setId(vo.getFid());
            //获取当前运费信息
            freightModel = mchFreightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //之前默认修改成非默认
                mchFreightModelMapper.setDefault(vo.getStoreId(), freightModel.getMch_id(), 0, 1);

                FreightModel updateFreight = new FreightModel();
                updateFreight.setId(freightModel.getId());
                updateFreight.setIs_default(1);
                int count = mchFreightModelMapper.updateByPrimaryKeySelective(updateFreight);
                if (count < 1)
                {
                    logger.info("设置运费默认失败 参数" + JSON.toJSONString(updateFreight));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "setDefault");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("设置默认运费 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
        }
    }

    @Override
    public Map<String, Object> relatedProducts(AddFreihtVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int count = mchFreightModelMapper.getFreightRelationGoodCount(vo.getStoreId(),vo.getFid());
            List<Map<String,Object>> results = new ArrayList<>();
            if( count > 0 )
            {
                results = mchFreightModelMapper.getFreightRelationGoods(vo.getStoreId(),vo.getFid(),vo.getPageNo(),vo.getPageSize());
                results.forEach(goods ->
                {
                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(MapUtils.getString(goods, "initial"), Map.class));
                    if (goodsInfoMap != null)
                    {
                        goods.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                        goods.put("imgurl", publiceService.getImgPath(MapUtils.getString(goods, "imgurl"), vo.getStoreId()));
                    }
                });
            }
            resultMap.put("total",count);
            resultMap.put("list",results);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("设置默认运费 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
        }
        return resultMap;
    }

}

