package com.laiketui.apps.app.services.activitie;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.activitie.AppsCstrPlatformActivitiesService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
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
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.plugin.activitie.PlatformActivitiesDelModel;
import com.laiketui.domain.plugin.activitie.PlatformActivitiesModel;
import com.laiketui.domain.plugin.group.PtGroupProductModel;
import com.laiketui.domain.plugin.seckill.PtSecondsProModel;
import com.laiketui.domain.plugin.seckill.SecondsTimeModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 平台活动管理
 *
 * @author Trick
 * @date 2021/4/25 10:16
 */
@Service
public class AppsCstrPlatformActivitiesServiceImpl implements AppsCstrPlatformActivitiesService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PlatformActivitiesModelMapper platformActivitiesModelMapper;

    @Autowired
    private PtGroupProductModelMapper ptGroupProductModelMapper;

    @Autowired
    private PtSecondsProModelMapper ptSecondsProModelMapper;

    @Autowired
    private PlatformActivitiesDelModelMapper platformActivitiesDelModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private SecondsTimeModelMapper secondsTimeModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<Map<String, Object>> index(MainVo vo, int mchId) throws LaiKeAPIException
    {
        List<Map<String, Object>> platformActivitiesList = new ArrayList<>();
        try
        {
            PlatformActivitiesModel platformActivitiesModel = new PlatformActivitiesModel();
            platformActivitiesModel.setStore_id(vo.getStoreId());
            platformActivitiesModel.setIsdelete(DictionaryConst.WhetherMaven.WHETHER_NO);
            List<PlatformActivitiesModel> platformActivitiesModelList = platformActivitiesModelMapper.select(platformActivitiesModel);
            for (PlatformActivitiesModel platformActivities : platformActivitiesModelList)
            {
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(platformActivities), new TypeReference<Map<String, Object>>()
                {
                });
                map.put("image", publiceService.getImgPath(platformActivities.getImage(), vo.getStoreId()));
                //0=添加活动 1=活动详情
                int status = 0;
                int count  = 0;
                if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.toLowerCase().equals(platformActivities.getType()))
                {
                    //获取拼团活动商品
                    PtGroupProductModel groupProductModel = new PtGroupProductModel();
                    groupProductModel.setPlatform_activities_id(platformActivities.getId());
                    groupProductModel.setStore_id(vo.getStoreId());
                    groupProductModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
                    groupProductModel.setMch_id(mchId);
                    count = ptGroupProductModelMapper.selectCount(groupProductModel);
                }
                else
                {
                    PtSecondsProModel secondsProModel = new PtSecondsProModel();
                    secondsProModel.setPlatform_activities_id(platformActivities.getId());
                    secondsProModel.setStore_id(vo.getStoreId());
                    secondsProModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
                    secondsProModel.setMch_id(mchId);
                    count = ptSecondsProModelMapper.selectCount(secondsProModel);
                }
                if (count > 0)
                {
                    status = 1;
                }
                else
                {
                    if (platformActivities.getStatus() > 1)
                    {
                        continue;
                    }
                    if (platformActivities.getStatus() == 0 && DateUtil.dateCompare(new Date(), platformActivities.getJoin_endtime()))
                    {
                        continue;
                    }
                    if (platformActivities.getStatus() == 0 && DateUtil.dateCompare(platformActivities.getJoin_starttime(), new Date()))
                    {
                        continue;
                    }
                }
                //查询商家是否删除了该活动
                PlatformActivitiesDelModel platformActivitiesDelModel = new PlatformActivitiesDelModel();
                platformActivitiesDelModel.setPlatform_activities_id(platformActivities.getId());
                platformActivitiesDelModel.setMch_id(mchId);
                count = platformActivitiesDelModelMapper.selectCount(platformActivitiesDelModel);
                if (count > 0)
                {
                    continue;
                }
                map.put("status1", status);
                map.put("starttime", DateUtil.dateFormate(platformActivities.getStarttime(), GloabConst.TimePattern.YMDHMS));
                map.put("addtime", DateUtil.dateFormate(platformActivities.getAddtime(), GloabConst.TimePattern.YMDHMS));
                map.put("endtime", DateUtil.dateFormate(platformActivities.getEndtime(), GloabConst.TimePattern.YMDHMS));
                map.put("join_starttime", DateUtil.dateFormate(platformActivities.getJoin_starttime(), GloabConst.TimePattern.YMDHMS));
                map.put("join_endtime", DateUtil.dateFormate(platformActivities.getJoin_endtime(), GloabConst.TimePattern.YMDHMS));
                platformActivitiesList.add(map);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("平台活动管理 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return platformActivitiesList;
    }

    @Override
    public List<Map<String, Object>> activityDetails(MainVo vo, int id) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object>       resultMap  = new HashMap<>(16);
        try
        {
            PlatformActivitiesModel platformActivitiesModel = new PlatformActivitiesModel();
            platformActivitiesModel.setStore_id(vo.getStoreId());
            platformActivitiesModel.setIsdelete(DictionaryConst.WhetherMaven.WHETHER_NO);
            platformActivitiesModel.setId(id);
            platformActivitiesModel = platformActivitiesModelMapper.selectOne(platformActivitiesModel);
            if (platformActivitiesModel != null)
            {
                resultMap = JSON.parseObject(JSON.toJSONString(platformActivitiesModel), new TypeReference<Map<String, Object>>()
                {
                });
                resultMap.put("image", publiceService.getImgPath(platformActivitiesModel.getImage(), vo.getStoreId()));
                //秒杀时段
                String timeStr = platformActivitiesModel.getBuy_time();
                if (!StringUtils.isEmpty(timeStr))
                {
                    List<String> timeIds = DataUtils.convertToList(timeStr.split(","));
                    if (timeIds != null)
                    {
                        List<String> buyTime1s = new ArrayList<>();
                        for (String time : timeIds)
                        {
                            SecondsTimeModel secondsTimeModel = new SecondsTimeModel();
                            secondsTimeModel.setStore_id(vo.getStoreId());
                            secondsTimeModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
                            secondsTimeModel.setId(Integer.parseInt(time));
                            secondsTimeModel = secondsTimeModelMapper.selectOne(secondsTimeModel);
                            if (secondsTimeModel != null)
                            {
                                String startTime = DateUtil.dateFormate(secondsTimeModel.getStarttime(), GloabConst.TimePattern.HMS);
                                String endTime   = DateUtil.dateFormate(secondsTimeModel.getEndtime(), GloabConst.TimePattern.HMS);
                                buyTime1s.add(startTime + "~" + endTime);
                            }
                        }
                        resultMap.put("buy_time1", StringUtils.stringImplode(buyTime1s, SplitUtils.DH));
                        resultMap.put("starttime", DateUtil.dateFormate(platformActivitiesModel.getStarttime(), GloabConst.TimePattern.YMDHMS));
                        resultMap.put("addtime", DateUtil.dateFormate(platformActivitiesModel.getAddtime(), GloabConst.TimePattern.YMDHMS));
                        resultMap.put("endtime", DateUtil.dateFormate(platformActivitiesModel.getEndtime(), GloabConst.TimePattern.YMDHMS));
                        resultMap.put("join_starttime", DateUtil.dateFormate(platformActivitiesModel.getJoin_starttime(), GloabConst.TimePattern.YMDHMS));
                        resultMap.put("join_endtime", DateUtil.dateFormate(platformActivitiesModel.getJoin_endtime(), GloabConst.TimePattern.YMDHMS));
                    }
                }
            }
            resultList.add(resultMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("活动详情 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityDetails");
        }
        return resultList;
    }

    @Override
    public Map<String, Object> proList(MainVo vo, int id, int mchId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //活动商品集
            List<Map<String, Object>> productList = new ArrayList<>();
            //获取活动信息
            PlatformActivitiesModel platformActivitiesModel = new PlatformActivitiesModel();
            platformActivitiesModel.setStore_id(vo.getStoreId());
            platformActivitiesModel.setIsdelete(DictionaryConst.WhetherMaven.WHETHER_NO);
            platformActivitiesModel.setId(id);
            platformActivitiesModel = platformActivitiesModelMapper.selectOne(platformActivitiesModel);
            if (platformActivitiesModel != null)
            {
                //参数列表
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("platform_activities_id", id);
                if (type == ProListType.REVIEWED)
                {
                    parmaMap.put("audit_not_status", 2);
                }
                else
                {
                    parmaMap.put("audit_status", 2);
                }
                if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.toLowerCase().equals(platformActivitiesModel.getType()))
                {
                    productList = ptGroupProductModelMapper.selectDynamic(parmaMap);
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.toLowerCase().equals(platformActivitiesModel.getType()))
                {
                    productList = ptSecondsProModelMapper.selectDynamic(parmaMap);
                }
                for (Map<String, Object> map : productList)
                {
                    int attrId = Integer.parseInt(map.get("attr_id").toString());
                    //活动价格
                    BigDecimal goodsPrice;
                    if (map.containsKey("seconds_price"))
                    {
                        goodsPrice = new BigDecimal(map.get("seconds_price").toString());
                    }
                    else
                    {
                        goodsPrice = new BigDecimal(map.get("price").toString());
                    }
                    //获取商品信息
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(confiGureModel.getPid());
                    if (productListModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    map.put("img", publiceService.getImgPath(confiGureModel.getImg(), vo.getStoreId()));
                    map.put("names", GoodsDataUtils.getProductSkuValue(confiGureModel.getAttribute()));
                    map.put("costprice", confiGureModel.getCostprice());
                    map.put("num1", confiGureModel.getNum());
                    map.put("price", goodsPrice);
                }
            }
            resultMap.put("code", 200);
            resultMap.put("pro", productList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("活动商品列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "proList");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> modifyPro(MainVo vo, int id, String activityType, Integer type, Integer num) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int row = 0;
            if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.toLowerCase().equals(activityType))
            {
                PtGroupProductModel ptGroupProductUpdate = new PtGroupProductModel();
                ptGroupProductUpdate.setId(id);
                if (type != null)
                {
                    if (type.equals(ModifyProType.ADDITIONAL))
                    {
                        //追加库存
                        ptGroupProductUpdate.setNum(num);
                    }
                    else
                    {
                        ptGroupProductUpdate.setAudit_status(type);
                        ptGroupProductUpdate.setAddtime(new Date());
                    }
                }
                else
                {
                    ptGroupProductUpdate.setIs_delete(1);
                }
                row = ptGroupProductModelMapper.updateByPrimaryKeySelective(ptGroupProductUpdate);
            }
            else if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.toLowerCase().equals(activityType))
            {
                PtSecondsProModel ptSecondsProUpdate = new PtSecondsProModel();
                ptSecondsProUpdate.setId(id);
                if (type != null)
                {
                    if (type.equals(ModifyProType.ADDITIONAL))
                    {
                        //追加库存
                        ptSecondsProUpdate.setNum(num);
                    }
                    else
                    {
                        ptSecondsProUpdate.setNum(num);
                        ptSecondsProUpdate.setAudit_status(type);
                        ptSecondsProUpdate.setAdd_time(new Date());
                    }
                }
                else
                {
                    ptSecondsProUpdate.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                row = ptSecondsProModelMapper.updateByPrimaryKeySelective(ptSecondsProUpdate);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改活动商品 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyPro");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> listdetail(MainVo vo, Integer id, Integer mchId, Integer cid, Integer brandId, String goodsName, String type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //商品集
            List<Map<String, Object>> productList     = new ArrayList<>();
            Map<String, Object>       goodsCascadeMap = publicGoodsService.getClassifiedBrands(vo.getStoreId(), cid, brandId);
            //参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("is_distribution", 0);
            parmaMap.put("mch_id", mchId);
            //自己店铺商品id
            List<Integer> attrIdNotList = new ArrayList<>();
            if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.toLowerCase().equals(type))
            {
                attrIdNotList = ptGroupProductModelMapper.selectMchGoodsId(vo.getStoreId(), mchId, id);
            }
            else if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.toLowerCase().equals(type))
            {
                attrIdNotList = ptSecondsProModelMapper.selectMchGoodsId(vo.getStoreId(), mchId, id);
            }
            //剔除已经拥有的活动商品
            parmaMap.put("attrIdNotList", attrIdNotList);
            if (!StringUtils.isEmpty(goodsName))
            {
                parmaMap.put("product_title", goodsName);
            }
            if (cid != null)
            {
                parmaMap.put("product_class", cid);
            }
            if (brandId != null)
            {
                parmaMap.put("brand_id", brandId);
            }
            parmaMap.put("add_date_sort", DataUtils.Sort.ASC);
            parmaMap.put("status_sort", DataUtils.Sort.DESC);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       count     = productListModelMapper.countProductListJoinConfigureDynamic(parmaMap);
            List<Map<String, Object>> goodsList = productListModelMapper.getProductListJoinConfigureDynamic(parmaMap);

            for (Map<String, Object> map : goodsList)
            {
                Map<String, Object> tmepMap = new HashMap<>(16);
                tmepMap.put("img", publiceService.getImgPath(map.get("img").toString(), vo.getStoreId()));
                tmepMap.put("attr", GoodsDataUtils.getProductSkuValue(map.get("attribute").toString()));
                tmepMap.put("id", map.get("id"));
                tmepMap.put("name", map.get("product_title"));
                tmepMap.put("costprice", map.get("costprice"));
                tmepMap.put("price", map.get("price"));
                tmepMap.put("yprice", map.get("yprice"));
                tmepMap.put("num", map.get("stockNum"));
                tmepMap.put("attr_id", map.get("attrId"));
                productList.add(tmepMap);
            }
            resultMap.put("code", 200);
            resultMap.put("attr", goodsCascadeMap.get("class_list"));
            resultMap.put("brand_class_list", goodsCascadeMap.get("brand_list"));
            resultMap.put("count_num", count);
            resultMap.put("pro", productList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("活动动商品展示页 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "listdetail");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addSecondGoods(MainVo vo, int id, int mchId, String goodsData) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int row;
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //解析商品参数 [{"id":"16601","price":0.008,"num":"3","pid":"1260"},{"id":"16602","price":0.008,"num":"3","pid":"1260"}]
            List<Map<String, String>> goodsInfoList = JSON.parseObject(goodsData, new TypeReference<List<Map<String, String>>>()
            {
            });
            if (goodsInfoList != null)
            {
                //获取平台活动信息
                PlatformActivitiesModel platformActivitiesModel = new PlatformActivitiesModel();
                platformActivitiesModel.setStore_id(vo.getStoreId());
                platformActivitiesModel.setId(id);
                platformActivitiesModel = platformActivitiesModelMapper.selectOne(platformActivitiesModel);
                if (platformActivitiesModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
                }
                //计算价格,平台发布的活动,商家参与统一八折
                BigDecimal zhekou = new BigDecimal("0.8");
                //最低价格
                BigDecimal minPrice = new BigDecimal("0.01");
                for (Map<String, String> map : goodsInfoList)
                {
                    int attrId = Integer.parseInt(map.get("id"));
                    //秒杀数量
                    int num = Integer.parseInt(map.get("num"));
                    //获取规格信息
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    BigDecimal price = confiGureModel.getPrice().multiply(zhekou);
                    if (price.compareTo(minPrice) < 0)
                    {
                        price = minPrice;
                    }
                    //添加平台秒杀商品
                    PtSecondsProModel ptSecondsProSave = new PtSecondsProModel();
                    ptSecondsProSave.setStore_id(vo.getStoreId());
                    ptSecondsProSave.setPro_id(confiGureModel.getPid());
                    ptSecondsProSave.setAttr_id(confiGureModel.getId());
                    ptSecondsProSave.setSeconds_price(price);
                    ptSecondsProSave.setNum(num);
                    ptSecondsProSave.setMax_num(num);
                    ptSecondsProSave.setBuy_num(1);
                    ptSecondsProSave.setActivity_id(0);
                    ptSecondsProSave.setTime_id(0);
                    ptSecondsProSave.setEndtime(platformActivitiesModel.getEndtime());
                    ptSecondsProSave.setStarttime(platformActivitiesModel.getStarttime());
                    ptSecondsProSave.setPlatform_activities_id(platformActivitiesModel.getId());
                    ptSecondsProSave.setFree_freight(platformActivitiesModel.getFree_freight());
                    ptSecondsProSave.setMch_id(mchId);
                    ptSecondsProSave.setAudit_status(1);
                    ptSecondsProSave.setReason("");
                    ptSecondsProSave.setIs_show(1);
                    ptSecondsProSave.setIs_delete(0);
                    ptSecondsProSave.setAdd_time(new Date());
                    row = ptSecondsProModelMapper.insertSelective(ptSecondsProSave);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJMSHDSB, "添加秒杀活动失败");
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("添加秒杀商品 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSecondGoods");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addGroupGoods(MainVo vo, int id, int mchId, String goodsData) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int row;
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //解析商品参数 [{"id":"16601","price":0.008,"num":"3","pid":"1260"},{"id":"16602","price":0.008,"num":"3","pid":"1260"}]
            List<Map<String, String>> goodsInfoList = JSON.parseObject(goodsData, new TypeReference<List<Map<String, String>>>()
            {
            });
            if (goodsInfoList != null)
            {
                //获取平台活动信息
                PlatformActivitiesModel platformActivitiesModel = new PlatformActivitiesModel();
                platformActivitiesModel.setStore_id(vo.getStoreId());
                platformActivitiesModel.setId(id);
                platformActivitiesModel = platformActivitiesModelMapper.selectOne(platformActivitiesModel);
                if (platformActivitiesModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
                }
                //计算价格,平台发布的活动,商家参与统一八折
                BigDecimal zhekou = new BigDecimal("0.8");
                //最低价格
                BigDecimal minPrice = new BigDecimal("0.01");
                //拼团数据
                int groupMan = platformActivitiesModel.getGroup_man();
                int groupCan = platformActivitiesModel.getGroup_can();
                int groupKai = platformActivitiesModel.getGroup_kai();
                //拼团参数
                Map<String, String> groupData = new HashMap<>(16);
                groupData.put("starttime", DateUtil.dateFormate(platformActivitiesModel.getStarttime(), GloabConst.TimePattern.YMDHMS));
                groupData.put("endtime", DateUtil.dateFormate(platformActivitiesModel.getEndtime(), GloabConst.TimePattern.YMDHMS));
                groupData.put("overtype", "2");
                //拼团等级参数
                Map<Integer, Object> groupLevelMap = new HashMap<>(16);
                groupLevelMap.put(groupMan, String.format("%s~%s", groupCan, groupKai));

                for (Map<String, String> map : goodsInfoList)
                {
                    int attrId = Integer.parseInt(map.get("id"));
                    //拼团数量
                    int num = Integer.parseInt(map.get("num"));
                    //获取规格信息
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    BigDecimal price = confiGureModel.getPrice().multiply(zhekou);
                    if (price.compareTo(minPrice) < 0)
                    {
                        price = minPrice;
                    }
                    //获取商品信息
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(confiGureModel.getPid());
                    if (productListModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    //添加平台拼团商品
                    PtGroupProductModel ptGroupProductModel = new PtGroupProductModel();
                    ptGroupProductModel.setStore_id(vo.getStoreId());
                    ptGroupProductModel.setProduct_id(confiGureModel.getPid());
                    ptGroupProductModel.setAttr_id(confiGureModel.getId());
                    ptGroupProductModel.setGroup_level(SerializePhpUtils.JavaSerializeByPhp(groupLevelMap));
                    ptGroupProductModel.setGroup_data(SerializePhpUtils.JavaSerializeByPhp(groupData));
                    ptGroupProductModel.setNum(num);
                    ptGroupProductModel.setPrice(price);
                    ptGroupProductModel.setGroup_title(productListModel.getProduct_title());
                    ptGroupProductModel.setEndtime(platformActivitiesModel.getEndtime());
                    ptGroupProductModel.setStarttime(platformActivitiesModel.getStarttime());
                    ptGroupProductModel.setPlatform_activities_id(platformActivitiesModel.getId());
                    ptGroupProductModel.setFree_freight(platformActivitiesModel.getFree_freight());
                    ptGroupProductModel.setMch_id(mchId);
                    ptGroupProductModel.setAudit_status(1);
                    ptGroupProductModel.setG_status(1);
                    ptGroupProductModel.setReason("");
                    ptGroupProductModel.setIs_show(1);
                    ptGroupProductModel.setIs_delete(0);
                    ptGroupProductModel.setActivity_no(ptGroupProductModelMapper.selectCount(new PtGroupProductModel()) + 1);
                    ptGroupProductModel.setAddtime(new Date());
                    row = ptGroupProductModelMapper.insertSelective(ptGroupProductModel);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJMSHDSB, "添加秒杀活动失败");
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("添加拼团商品 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGroupGoods");
        }
        return resultMap;
    }


}

