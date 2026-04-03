package com.laiketui.apps.app.services.group;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.group.AppsCstrGroupBuyService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.consts.MchConst;
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
import com.laiketui.domain.group.GroupConfigModel;
import com.laiketui.domain.group.GroupProductModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.order.OrderConfigModal;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.plugin.group.GoGroupOrderDetailsModel;
import com.laiketui.domain.plugin.group.GoGroupOrderModel;
import com.laiketui.domain.product.CommentsModel;
import com.laiketui.domain.product.ProductImgModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.mch.FrontDeliveryVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.laiketui.core.lktconst.DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE;

/**
 * 拼团
 *
 * @author Trick
 * @date 2021/2/20 11:12
 */
@Service("groupBuyService")
public class AppsCstrGroupBuyServiceImpl implements AppsCstrGroupBuyService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GroupProductModelMapper groupProductModelMapper;

    @Autowired
    private GroupConfigModelMapper groupConfigModelMapper;

    @Autowired
    private GoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private GoGroupOrderDetailsModelMapper goGroupOrderDetailsModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private GroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;


    @Override
    public Map<String, Object> grouphome(MainVo vo, Integer navType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("addtime_sort", "desc");
            parmaMap.put("group_activity_no", "");
            //查询拼团活动
            if (navType != null && navType == GroupProductModel.GROUP_GOODS_STATUS_NO_START)
            {
                //只查询已经结束+进行中的活动
                parmaMap.put("not_no_start", GroupProductModel.GROUP_GOODS_STATUS_NO_START);
            }
            else
            {
                GroupConfigModel groupConfigModel = new GroupConfigModel();
                groupConfigModel.setStore_id(vo.getStoreId());
                groupConfigModel = groupConfigModelMapper.selectOne(groupConfigModel);
                if (groupConfigModel != null)
                {
                    //开团先知 单位/分
                    int heraldTime = groupConfigModel.getHerald_time();
                    if (heraldTime > 0)
                    {
                        //查询未开始的活动
                        parmaMap.put("g_status", GroupProductModel.GROUP_GOODS_STATUS_NO_START);
                        parmaMap.put("startTime_GT_HOUR", heraldTime);
                    }
                }
            }
            List<Map<String, Object>> groupList = groupProductModelMapper.getGroupProductIndex(parmaMap);
            for (int i = 0; i < groupList.size(); i++)
            {
                Map<String, Object> map = groupList.get(i);
                //拼团产品id
                int groupId = StringUtils.stringParseInt(map.get("activity_no"));
                //商品id
                int goodsId = StringUtils.stringParseInt(map.get("product_id"));
                //审核状态 审核状态 0待提交 1待审核 2审核通过 3审核拒绝
                int examineStatus = StringUtils.stringParseInt(map.get("audit_status"));
                //平台活动id
                int platFormId = StringUtils.stringParseInt(map.get("platform_activities_id"));
                if (platFormId > 0 && examineStatus != 2)
                {
                    logger.debug("平台活动id{},拼团产品编号{},未审核通过", platFormId, groupId);
                    groupList.remove(i);
                    i--;
                    continue;
                }
                //查询拼团活动已经被购买了多少
                Integer volume = goGroupOrderModelMapper.groupSumOrderByNum(groupId);
                if (volume == null)
                {
                    volume = 0;
                }
                map.put("sum", volume);
                //获取活动库存
                parmaMap.clear();
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("product_id", goodsId);
                Integer groupStockNum = groupProductModelMapper.getGroupProductNum(parmaMap);
                if (groupStockNum == null)
                {
                    groupStockNum = 0;
                }
                map.put("num", groupStockNum);
                //获取活动时间
                String              productData    = map.get("group_data").toString();
                Map<String, Object> productDataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(productData, Map.class));
                int                 productStatus  = 1;
                if (productDataMap == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                Date startDate = DateUtil.dateFormateToDate(productDataMap.get("starttime").toString(), GloabConst.TimePattern.YMDHMS);
                Date endDate   = DateUtil.dateFormateToDate(productDataMap.get("endtime").toString(), GloabConst.TimePattern.YMDHMS);
                //活动状态: 1.未开始 2.活动中 3.已结束
                int groupStatus = StringUtils.stringParseInt(map.get("g_status"));
                //判断是否有库存，并且未结束
                if (DateUtil.dateCompare(startDate, new Date()))
                {
                    //互动未开启
                    productStatus = 4;
                }
                else if (DateUtil.dateCompare(new Date(), endDate) || groupStatus == 3)
                {
                    //活动已经结束
                    productStatus = 3;
                }
                else if (groupStockNum == 0)
                {
                    //库存不足
                    productStatus = 2;
                }
                map.put("group_data", productDataMap);
                map.put("product_status", productStatus);

                //拼团等级
                String groupLevelStr = map.get("group_level").toString();
                //获取商品图片
                String goodsImg = publiceService.getImgPath(map.get("imageurl").toString(), vo.getStoreId());
                map.put("imgurl", goodsImg);
                //获取属性里最低的金额
                ConfiGureModel confiGureModel = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                BigDecimal     minPrice       = confiGureModel.getPrice();
                map.put("market_price", minPrice.toString());
                //获取拼团价格信息
                Map<String, BigDecimal> groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), groupLevelStr, minPrice, 0);
                map.put("kaiprice", groupPriceMap.get("groupOpenPrice"));
                //设置时间 初始值
                if (startDate != null)
                {
                    map.put("leftTime", startDate.getTime() - System.currentTimeMillis());
                }
                map.put("hour", "00");
                map.put("mniuate", "00");
                map.put("second", "00");

                map.put("level", "");
                map.put("min_price", groupPriceMap.get("groupOpenPrice"));
                map.put("groupnum", groupPriceMap.get("minMan"));
                map.put("min_man", groupPriceMap.get("minMan").intValue());
            }
            resultMap.put("list", groupList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团首页 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "grouphome");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getgoodsdetail(MainVo vo, int goodsId, int activityId, int platformActivityId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user == null)
            {
                user = new User();
            }
            //店铺信息集
            Map<String, Object> mchInfoMap = new HashMap<>(16);
            //当前商品拼团参数
            Map<String, Object> groupDataMap = new HashMap<>(16);
            //商品规格弹窗数据
            Map<String, Object> attrMap = new HashMap<>(16);
            //活动状态
            int productStatus = 1;
            //是否显示拼团商品
            int isShow = 0;
            //最多参团数量
            int maxGroupNum = 0;
            //最大开团数量
            int maxOpenNum = 0;
            //最小参团人数
            int miniManNum = 0;
            //拼团最小比例
            BigDecimal minBili = new BigDecimal("0");
            //开团价格
            BigDecimal openPrice = new BigDecimal("0");
            //查询商品
            Map<String, Object> goodsInfoMap = groupProductModelMapper.getGroupGoodsInfo(vo.getStoreId(), goodsId, activityId);
            if (goodsInfoMap != null)
            {
                //商品最低价
                BigDecimal goodsMiniPrice;
                //运费
                BigDecimal yunfei      = new BigDecimal("0");
                String     freightName = "免运费";
                //商品库存数量-取最大库存规格数量
                int groupStockNum = Integer.parseInt(goodsInfoMap.get("num").toString());
                //店铺id
                Integer mchId = StringUtils.stringParseInt(goodsInfoMap.get("mch_id"));
                isShow = Integer.parseInt(goodsInfoMap.get("is_show").toString());

                //是否免邮
                int isFrreFreight = Integer.parseInt(goodsInfoMap.get("free_freight").toString());
                if (isFrreFreight == 0)
                {
                    //获取商品运费信息
                    Map<String, Object> yunfeiMap  = productListModelMapper.selectGoodsFreightInfo(vo.getStoreId(), goodsId);
                    String              freight    = yunfeiMap.get("freight").toString();
                    String              supplierId = yunfeiMap.get("supplier_id").toString();
                    freightName = yunfeiMap.get("name") + "";
                    String           defaultFreight   = yunfeiMap.get("default_freight").toString();
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                    yunfei = new BigDecimal(defaultFreightVO.getNum2());
                }
                //获取商品最低价格
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setPid(goodsId);
                goodsMiniPrice = confiGureModelMapper.getActivityGoodsLowPrice(confiGureModel);
                //获取店铺信息
                if (mchId != null)
                {
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                    if (mchModel != null)
                    {
                        mchInfoMap.put("shop_id", mchModel.getId());
                        mchInfoMap.put("tel", mchModel.getTel());
                        mchInfoMap.put("cpc", mchModel.getCpc());
                        mchInfoMap.put("shop_name", mchModel.getName());
                        String shopLogo = mchModel.getLogo();
                        shopLogo = publiceService.getImgPath(shopLogo, vo.getStoreId());
                        mchInfoMap.put("shop_logo", shopLogo);
                        mchInfoMap.put("shop_id", mchModel.getId());
                        mchInfoMap.putAll(publiceService.commodityInformation(vo.getStoreId(), mchId,null));
                        //访问店铺记录
                        MchBrowseModel mchBrowseModel = new MchBrowseModel();
                        mchBrowseModel.setStore_id(vo.getStoreId());
                        mchBrowseModel.setToken(vo.getAccessId());
                        mchBrowseModel.setUser_id(user.getUser_id());
                        mchBrowseModel.setEvent("访问了店铺");
                        mchBrowseModel.setAdd_time(new Date());
                        mchBrowseModelMapper.insertSelective(mchBrowseModel);
                    }
                }
                //拼团等级参数
                String groupLevelStr = goodsInfoMap.get("group_level").toString();
                //默认没有开启团长优惠 默认参团折扣
                goodsInfoMap.put("freight", yunfei);
                goodsInfoMap.put("freight_name", freightName);
                goodsInfoMap.put("market_price", goodsMiniPrice);
                //获取拼团价格信息
                Map<String, BigDecimal> groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), groupLevelStr, goodsMiniPrice, 0);
                goodsInfoMap.put("kaiprice", openPrice = groupPriceMap.get("groupOpenPrice"));
                miniManNum = groupPriceMap.get("minMan").intValue();
                goodsInfoMap.put("groupnum", miniManNum);
                //获取该拼团商品销售数量
                Integer buyNum = goGroupOrderModelMapper.countGroupGoodsPayNum(vo.getStoreId(), goodsId);
                goodsInfoMap.put("buysum", buyNum);
                //获取拼团配置信息
                String           rule             = "";
                GroupConfigModel groupConfigModel = new GroupConfigModel();
                groupConfigModel.setStore_id(vo.getStoreId());
                groupConfigModel = groupConfigModelMapper.selectOne(groupConfigModel);
                //是否开启团长优惠
                if (groupConfigModel != null)
                {
                    //拼团规则
                    rule = groupConfigModel.getRule();
                    maxGroupNum = groupConfigModel.getCan_num();
                    maxOpenNum = groupConfigModel.getOpen_num();
                }
                goodsInfoMap.put("group_level", DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupLevelStr, Map.class)));
                goodsInfoMap.put("rule", rule);
                //获取商品轮播图
                List<String> imgUrlList = new ArrayList<>();
                String       goodsImg   = goodsInfoMap.get("imgurl").toString();
                goodsImg = publiceService.getImgPath(goodsImg, vo.getStoreId());
                goodsInfoMap.put("image", goodsImg);
                imgUrlList.add(goodsImg);
                //获取轮播图
                ProductImgModel productImgModel = new ProductImgModel();
                productImgModel.setProduct_id(goodsId);
                List<ProductImgModel> goodsImageList = productImgModelMapper.select(productImgModel);
                for (ProductImgModel productImg : goodsImageList)
                {
                    imgUrlList.add(publiceService.getImgPath(productImg.getProduct_url(), vo.getStoreId()));
                }
                goodsInfoMap.put("images", imgUrlList);
                //获取属性弹窗数据
                List<ConfiGureModel> confiGureModelList = confiGureModelMapper.selectGroupGureListByPid(goodsId, activityId);
                //处理图片地址
                for (ConfiGureModel confiGure : confiGureModelList)
                {
                    //剔除不在拼团活动里的规格
                    String imgUrl = confiGure.getImg();
                    imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                    confiGure.setImg(imgUrl);
                }
                //获取商品规格弹出窗口插件数据
                attrMap = GoodsDataUtils.getGoodsAttributeInfo(confiGureModelList);

                //拼团参数处理
                String groupData = goodsInfoMap.get("group_data").toString();
                groupDataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupData, Map.class));
                if (groupDataMap != null)
                {
                    //活动状态: 1.未开始 2.活动中 3.已结束
                    int  groupStatus = StringUtils.stringParseInt(goodsInfoMap.get("g_status"));
                    Date startDate   = DateUtil.dateFormateToDate(groupDataMap.get("starttime").toString(), GloabConst.TimePattern.YMDHMS);
                    Date endDate     = DateUtil.dateFormateToDate(groupDataMap.get("endtime").toString(), GloabConst.TimePattern.YMDHMS);
                    if (startDate != null && endDate != null)
                    {
                        groupDataMap.put("lefttime", endDate.getTime() / 1000 - DateUtil.getTime());
                        groupDataMap.put("righttime", startDate.getTime() / 1000 - DateUtil.getTime());
                    }
                    //判断是否有库存，并且未结束
                    if (DateUtil.dateCompare(startDate, new Date()))
                    {
                        //活动未开启
                        productStatus = 4;
                    }
                    else if (DateUtil.dateCompare(new Date(), endDate) || groupStatus == 3)
                    {
                        //活动已经结束
                        productStatus = 3;
                    }
                    else if (groupStockNum == 0)
                    {
                        //库存不足
                        productStatus = 2;
                    }
                }
            }
            //获取评论信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", goodsId);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            List<Map<String, Object>> goodsCommentList = publiceService.getGoodsCommentList(parmaMap);

            //获取该商品其它拼团信息
            List<Map<String, Object>> otherGroupInfoList = groupOpenModelMapper.getOterGroupInfo(vo.getStoreId(), goodsId, user.getUser_id(), new Date());
            for (Map<String, Object> map : otherGroupInfoList)
            {
                Date endDateTemp = DateUtil.dateFormateToDate(map.get("endtime").toString(), GloabConst.TimePattern.YMDHMS);
                if (endDateTemp == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                map.put("leftTime", endDateTemp.getTime() / 1000 - DateUtil.getTime());
                map.put("leftTimeStr", "00:00:00");
            }

            resultMap.putAll(attrMap);
            resultMap.put("islogin", user.getId() != null);
            resultMap.put("min_man", miniManNum);
            resultMap.put("min_price", openPrice);
            resultMap.putAll(publiceGroupService.validataGroup(vo.getStoreId(), user.getUser_id(), false));
            resultMap.put("user_can_can_num", maxGroupNum);
            resultMap.put("user_can_open_num", maxOpenNum);
            resultMap.put("kai_min_bili", minBili);
            resultMap.put("kai_min_man", miniManNum);
            resultMap.put("kai_price", openPrice);
            resultMap.put("kai_min_price", openPrice);
            resultMap.put("detail", goodsInfoMap);
            resultMap.put("shop_list", mchInfoMap);
            resultMap.put("control", groupDataMap);
            resultMap.put("isshow", isShow);
            resultMap.put("comments", goodsCommentList);
            resultMap.put("groupList", otherGroupInfoList);
            resultMap.put("isplug", 0);
            resultMap.put("product_status", productStatus);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品详情数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getgoodsdetail");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> grouporder(MainVo vo, String orderType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //拼团没有成功的在拼团订单表   查拼团成功的数据在总订单表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("orderType", orderType);
            parmaMap.put("id_group", "id_group");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> groupList = goGroupOrderModelMapper.getGrouporder(parmaMap);
            for (Map<String, Object> map : groupList)
            {
                //订单号
                String orderno = map.get("sNo").toString();
                //拼团订单状态
                int status = StringUtils.stringParseInt(map.get("status"));
                //拼团商品状态
                int goodsStatus = Integer.parseInt(map.get("g_status").toString());
                //获取拼团信息
                Map<String, Object> ptMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("group_data").toString(), Map.class));
                if (ptMap == null)
                {
                    logger.debug("拼团订单:{} group_data数据错误", orderno);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //拼团结束时间
                String endTime = ptMap.get("endtime").toString();
                //店铺id
                String mchIdStr = map.get("mch_id") + "";
                //订单金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());
                //折扣金额
                BigDecimal orderDiscount = new BigDecimal(map.get("offset_balance").toString());
                if (orderAmt.compareTo(orderDiscount) != 0)
                {
                    //计算订单真实金额
                    orderAmt = orderAmt.add(orderDiscount);
                    map.put("z_price", orderAmt);
                }

                //获取店铺信息
                int    shopId   = 0;
                String shopName = "";
                String shopLogo = "";
                if (!StringUtils.isEmpty(mchIdStr))
                {
                    Integer  mchId    = StringUtils.stringParseInt(StringUtils.trim(mchIdStr, ","));
                    MchModel mchModel = new MchModel();
                    mchModel.setStore_id(vo.getStoreId());
                    mchModel.setId(mchId);
                    mchModel = mchModelMapper.selectOne(mchModel);
                    if (mchModel != null)
                    {
                        shopId = mchModel.getId();
                        shopName = mchModel.getName();
                        shopLogo = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
                    }
                }
                map.put("shop_id", shopId);
                map.put("shop_name", shopName);
                map.put("shop_logo", shopLogo);

                //判断是否有订单售后未结束
                int saleType  = 0;
                int returnNum = returnOrderModelMapper.orderReturnIsNotEnd(vo.getStoreId(), orderno);
                if (returnNum > 0)
                {
                    saleType = 1;
                }
                //活动是否已经结束
                boolean isRefund = true;
                Date    endDate  = DateUtil.dateFormateToDate(endTime, GloabConst.TimePattern.YMDHMS);
                if (status == GoGroupOrderModel.OrderStatus.PT_ING && DateUtil.dateCompare(new Date(), endDate))
                {
                    //活动已结束
                    map.put("status", 11);
                    isRefund = false;
                }
                //获取拼团订单明细
                parmaMap.clear();
                parmaMap.put("orderno", orderno);
                parmaMap.put("store_id", vo.getStoreId());
                List<Map<String, Object>> goGroupOrderModelList = goGroupOrderDetailsModelMapper.selectDynamic(parmaMap);
                //运费统计
                BigDecimal freightAmt = new BigDecimal("0");
                for (Map<String, Object> detailsModel : goGroupOrderModelList)
                {
                    freightAmt = freightAmt.add(new BigDecimal(detailsModel.get("freight").toString()));
                    int goodsId = Integer.parseInt(detailsModel.get("p_id").toString());
                    detailsModel.put("pro_id", detailsModel.get("p_id"));
                    Integer attrId = Integer.parseInt(detailsModel.get("sid").toString());
                    detailsModel.put("attribute_id", attrId);
                    //获取商品图片
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                    if (productListModel != null)
                    {
                        String imgUrl = publiceService.getImgPath(productListModel.getImgurl(), vo.getStoreId());
                        detailsModel.put("imgurl", imgUrl);
                    }
                }
                if (status == GoGroupOrderModel.OrderStatus.NOTPAY)
                {
                    //判断当前商品拼团已结束/活动关闭
                    int canOpen = 1;
                    if (DateUtil.dateCompare(new Date(), endDate) || goodsStatus != GroupProductModel.GROUP_GOODS_STATUS_UNDER_WAY)
                    {
                        canOpen = 0;
                    }
                    map.put("can_open", canOpen);
                }
                map.put("list", goGroupOrderModelList);
                map.put("z_freight", freightAmt);
                map.put("sum", goGroupOrderModelList.size());

                map.put("refund", isRefund);
                map.put("sale_type", saleType);
            }

            resultMap.put("order", groupList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("查询订单 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "grouporder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> grouporderDetail(MainVo vo, String orderno) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        return resultMap;
    }


    @Override
    public Map<String, Object> orderDetails(MainVo vo, int orderId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = orderDetails(vo, orderId, user);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团订单选择支付方式 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "choicePaymentType");
        }
        return resultMap;
    }

    @Transactional
    @Override
    public boolean removeGroupOrder(MainVo vo, int orderId) throws LaiKeAPIException
    {
        try
        {
            int  count;
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取订单信息
            GoGroupOrderModel goGroupOrderModel = new GoGroupOrderModel();
            goGroupOrderModel.setUser_id(user.getUser_id());
            goGroupOrderModel.setStore_id(vo.getStoreId());
            goGroupOrderModel.setId(orderId);
            goGroupOrderModel = goGroupOrderModelMapper.selectOne(goGroupOrderModel);
            if (goGroupOrderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
            //拼团失败则直接删除订单,明细不删
            if (GoGroupOrderModel.OrderStatus.NOTPAY == goGroupOrderModel.getStatus())
            {
                GoGroupOrderModel goGroupOrderUpdate = new GoGroupOrderModel();
                goGroupOrderUpdate.setId(goGroupOrderModel.getId());
                goGroupOrderUpdate.setStatus(GoGroupOrderModel.OrderStatus.ORDER_CLOSE);
                count = goGroupOrderModelMapper.updateByPrimaryKeySelective(goGroupOrderUpdate);
            }
            else if (GoGroupOrderModel.OrderStatus.PT_FIAL_NOT_REFUND == goGroupOrderModel.getStatus())
            {
                count = goGroupOrderModelMapper.deleteByPrimaryKey(goGroupOrderModel.getId());
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGDDZTSB, "修改订单状态失败");
            }
            return publiceGroupService.reBackGoodsNum(vo.getStoreId(), goGroupOrderModel.getsNo(), goGroupOrderModel.getActivity_no(), "取消订单增加");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除拼团订单 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "choicePaymentType");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deliveryGood(MainVo vo, String sNo, Integer expressId, String courierNum) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setUserId(user.getUser_id());
            sendVo.setWxid(user.getWx_id());
            sendVo.setExpressId(expressId);
            sendVo.setsNo(sNo);
            sendVo.setCourierNum(courierNum);
            //发货
            publicOrderService.frontDelivery(sendVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("发货 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "send");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> okOrder(int storeId, String accessId, String orderno, Integer rType)
    {
        try
        {
            return publicOrderService.okOrder(storeId, accessId, orderno, rType);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "okOrder");
        }

    }


    /**
     * 订单明细
     *
     * @param vo   -
     * @param user -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/30 9:46
     */
    private Map<String, Object> orderDetails(MainVo vo, int orderId, User user) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        // 订单中商品信息
        List<Map<String, Object>> productList = new ArrayList<>();
        // 没有批量退货按钮
        int batchDel = 1;
        try
        {
            //时间单位
            String unitTime = "day";
            //订单失效时间
            int orderFailure = 2;
            //订单售后时限
            int orderAfter = 7;
            //获取最新数据
            user = userBaseMapper.selectByPrimaryKey(user.getId());

            //获取订单配置信息
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(vo.getStoreId());
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            if (orderConfigModal != null)
            {
                orderFailure = orderConfigModal.getOrder_failure();
                orderAfter = orderConfigModal.getOrder_after();
                if (!"天".equals(orderConfigModal.getCompany()))
                {
                    unitTime = "hour";
                }
            }

            // 根据微信id,查询用户id
            String userId = user.getUser_id();
            // 支付密码错误次数
            Integer loginNum = user.getLogin_num();
            // 支付密码验证时间
            Date verificationTime = user.getVerification_time();
            if (verificationTime != null)
            {
                verificationTime = DateUtil.getAddDate(verificationTime, 1);
            }
            //是否设置密码
            String user_password  = user.getPassword();
            int    passwordStatus = 0;
            if (!StringUtils.isEmpty(user_password))
            {
                passwordStatus = 1;
            }

            Date time = new Date();
            // 是否可以输入密码
            boolean enterless = true;
            if (loginNum == 5)
            {
                if (verificationTime != null && time.after(verificationTime))
                {
                    enterless = false;
                }
            }

            //获取订单信息
            GoGroupOrderModel orderModel = new GoGroupOrderModel();
            orderModel.setId(orderId);
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setUser_id(user.getUser_id());
            orderModel = goGroupOrderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "orderDetails");
            }
            // 订单号
            String sNo = orderModel.getsNo();
            // 总价
            BigDecimal zPrice = orderModel.getZ_price();
            //订单状态
            int status = orderModel.getStatus();
            // 余额抵扣
            BigDecimal offset_balance = orderModel.getOffset_balance();
            offset_balance = offset_balance == null ? BigDecimal.ZERO : offset_balance;
            if (zPrice.compareTo(offset_balance) != 0 && status != 0)
            {
                zPrice = zPrice.add(offset_balance);
            }
            //收到取消订单
            int hand_del = 0;
            // 订单时间
            Date   addTime   = orderModel.getAdd_time();
            String remarks   = "";
            String dbRemarks = orderModel.getRemarks();
            //处理订单备注
            if (!StringUtils.isEmpty(dbRemarks))
            {
                remarks = dbRemarks;
                if (SerializePhpUtils.isSerialized(dbRemarks))
                {
                    // 订单备注
                    Map<String, String> remarksMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(dbRemarks, Map.class));
                    if (remarksMap != null)
                    {
                        for (String val : remarksMap.values())
                        {
                            remarks = val;
                        }
                    }
                }
            }
            //联系人
            String name = orderModel.getName();
            //联系手机号
            String mobile1 = orderModel.getMobile();
            //脱敏操作
            mobile1 = StringUtils.desensitizedPhoneNumber(mobile1);

            String sheng = orderModel.getSheng();
            String shi   = orderModel.getShi();
            String xian  = orderModel.getXian();
            //联系地址
            String address     = orderModel.getAddress();
            String fullAddress = sheng + shi + xian + address;
            //订单类型
            String otype = orderModel.getOtype();
            //自动满减名称
            String couponActivityName = orderModel.getCoupon_activity_name();
            //满减金额
            BigDecimal reducePrice = orderModel.getReduce_price();
            if (DataUtils.equalBigDecimalZero(orderModel.getReduce_price()))
            {
                couponActivityName = "￥" + reducePrice;
            }
            // 优惠券ID
            String couponId = orderModel.getCoupon_id();
            // 优惠券金额
            BigDecimal couponPrice = orderModel.getCoupon_price();
            // 店铺ID
            String mchId = StringUtils.trim(orderModel.getMch_id(), SplitUtils.DH);
            // 会员等级折扣
            BigDecimal gradeRate = orderModel.getGrade_rate();
            //提醒状态
            Integer deliveryStatus = orderModel.getDelivery_status();
            // 是否自提
            Integer selfLifting = orderModel.getSelf_lifting();
            // 优惠金额
            BigDecimal preferentialAmount = orderModel.getPreferential_amount();
            String     couponName         = "";

            BigDecimal userMoney = BigDecimal.ZERO;
            if (status == 0)
            {
                // 未付款
                userMoney = user.getMoney();
            }

            BigDecimal productTotal = BigDecimal.ZERO;
            BigDecimal zFreight     = BigDecimal.ZERO;
            String     discountType = "";
            //类型：优惠券coupon、满减subtraction 0,12 或者  12,21 最后一个逗号后面的数字就是平台优惠券id
            String[] couponsId    = StringUtils.trim(couponId, SplitUtils.DH).split(SplitUtils.DH);
            String   platCouponId = couponsId[couponsId.length - 1];

            if (!"0".equals(platCouponId))
            {
                //平台优惠券优惠类型
                discountType = "优惠券";
            }
            Integer subtractionId = orderModel.getSubtraction_id();
            if (subtractionId != null && subtractionId == 0)
            {
                couponPrice = couponPrice.subtract(preferentialAmount);
            }
            // 会员优惠金额
            BigDecimal gradeRateAmount = BigDecimal.ZERO;
            // 根据订单号,查询订单详情
            GoGroupOrderDetailsModel orderDetailsInfo = new GoGroupOrderDetailsModel();
            orderDetailsInfo.setStore_id(vo.getStoreId());
            orderDetailsInfo.setR_sNo(sNo);
            List<GoGroupOrderDetailsModel> orderDetailsModelsList = goGroupOrderDetailsModelMapper.select(orderDetailsInfo);
            boolean                        userCanAfter           = true;
            String                         mchName                = "";
            if (!CollectionUtils.isEmpty(orderDetailsModelsList))
            {
                // 是否有批量申请
                List<Integer> batch = new ArrayList<Integer>();
                // 快递单号
                Map<String, Object> courier_num_arr = new HashMap<>(16);
                for (GoGroupOrderDetailsModel orderDetails : orderDetailsModelsList)
                {
                    // 商品售价
                    BigDecimal orderDetailsPPrice = orderDetails.getP_price();
                    // 商品数量
                    BigDecimal pNum = BigDecimal.valueOf(orderDetails.getNum());
                    // 优惠后的金额
                    BigDecimal afterDiscount = orderDetails.getAfter_discount();
                    // 总折扣
                    gradeRateAmount = gradeRateAmount.add(orderDetailsPPrice.subtract(orderDetailsPPrice.multiply(gradeRate)).multiply(pNum)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    // 产品id
                    int pId = orderDetails.getP_id();
                    // 属性id
                    String sid = orderDetails.getSid();
                    // 数据结构转换
                    Map<String, Object> jsonObject = JSON.parseObject(JSON.toJSONString(orderDetails), new TypeReference<Map<String, Object>>()
                    {
                    });
                    // 不显示
                    jsonObject.put("comments_type", 0);

                    //判断订单评论状态
                    CommentsModel commentsModel = new CommentsModel();
                    commentsModel.setStore_id(vo.getStoreId());
                    commentsModel.setPid(pId + "");
                    commentsModel.setUid(userId);
                    commentsModel.setOid(sNo);
                    commentsModel = commentsModelMapper.selectOne(commentsModel);
                    if (commentsModel != null)
                    {
                        //待追评
                        jsonObject.put("comments_type", 2);
                        if (!org.springframework.util.StringUtils.isEmpty(commentsModel.getReview()))
                        {
                            //评论完成
                            jsonObject.put("comments_type", 3);
                        }
                    }
                    else
                    {
                        int r_status = orderDetails.getR_status();
                        if (r_status == ORDERS_R_STATUS_COMPLETE)
                        {
                            //待评价
                            jsonObject.put("comments_type", 1);
                        }
                    }

                    //根据产品id 查询店铺id
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(pId);
                    productListModel.setStore_id(vo.getStoreId());
                    productListModel = productListModelMapper.selectOne(productListModel);
                    if (productListModel != null)
                    {
                        int      shop_id  = productListModel.getMch_id();
                        MchModel mchModel = new MchModel();
                        mchModel.setId(shop_id);
                        mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            jsonObject.put("shop_id", shop_id);
                            jsonObject.put("shop_name", mchModel.getName());
                            jsonObject.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                        }
                        else
                        {
                            jsonObject.put("shop_id", shop_id);
                            jsonObject.put("shop_name", "");
                            jsonObject.put("shop_logo", "");
                        }
                    }
                    //商品单价
                    orderDetailsPPrice = orderDetails.getP_price();
                    // 数量
                    BigDecimal num = BigDecimal.valueOf(orderDetails.getNum());
                    // 运费
                    BigDecimal freight = orderDetails.getFreight();
                    // 商品总价
                    productTotal = productTotal.add(orderDetailsPPrice.multiply(num));
                    //如果为竞拍商品，重新计算总价
                    // 运费总价
                    zFreight = zFreight.add(freight);
                    // 到货时间
                    Date arriveTime        = orderDetails.getArrive_time();
                    long order_after_times = orderAfter * 24 * 60 * 60;
                    //
                    if (arriveTime != null)
                    {
                        if ((DateUtil.getAddDate(new Date(), -1)).before(arriveTime))
                        {
                            userCanAfter = false;
                        }
                    }
                    //订单详情ID
                    int orderDetailsId = orderDetails.getId();
                    //售后标识
                    jsonObject.put("s_type", 0);
                    //判断申请售后按钮的显示
                    Map<String, Object> conditionMap = new HashMap<>(16);
                    conditionMap.put("storeId", vo.getStoreId());
                    conditionMap.put("userId", userId);
                    conditionMap.put("sNo", sNo);
                    conditionMap.put("orderDetailsId", orderDetailsId);
                    int unfinishOrderNum = returnOrderModelMapper.getUnFinishShouHouOrder(conditionMap);
                    if (unfinishOrderNum > 0)
                    {
                        jsonObject.put("s_type", 1);
                    }

                    //查询售后情况
                    ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                    returnOrderModel.setStore_id(vo.getStoreId());
                    returnOrderModel.setP_id(orderDetails.getId());
                    returnOrderModel.setsNo(sNo);
                    returnOrderModel = returnOrderModelMapper.selectOne(returnOrderModel);
                    if (returnOrderModel != null)
                    {
                        Integer rType = returnOrderModel.getR_type();
                        //退款转态
                        if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS))
                        {
                            jsonObject.put("prompt", "审核中");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK))
                        {
                            jsonObject.put("prompt", "审核通过");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS_AMT))
                        {
                            jsonObject.put("prompt", "拒绝退货退款");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED))
                        {
                            jsonObject.put("prompt", "审核通过");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_RECEIVED_REBAKGOODS))
                        {
                            jsonObject.put("prompt", "退货完成");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_REBACKGOODS))
                        {
                            jsonObject.put("prompt", "退货失败");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_REFUSE_AMT))
                        {
                            jsonObject.put("prompt", "退款失败");
                        }
                        else if (rType.equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK_AMT))
                        {
                            jsonObject.put("prompt", "退款成功");
                        }

                        if (returnOrderModel.getContent() != null)
                        {
                            batch.add(1);
                            // 申请退货（已经申请，不能再申请退货）
                            jsonObject.put("is_repro", 1);
                        }
                        else
                        {
                            batch.add(0);
                            // 没有申请退货（可以退货）
                            jsonObject.put("is_repro", 0);
                        }
                    }

                    // 换货次数
                    Integer exchangeNum = orderDetails.getExchange_num();
                    jsonObject.put("exchange_status", false);
                    exchangeNum = exchangeNum == null ? 0 : exchangeNum;
                    if (exchangeNum < 2)
                    {
                        jsonObject.put("exchange_status", true);
                    }
                    Date date = DateUtil.getAddDate(new Date(), -7);
                    if (arriveTime != null)
                    {
                        if (arriveTime.before(date))
                        {
                            // 到货时间少于7天
                            jsonObject.put("info", 1);
                        }
                        else
                        {
                            // 已经到货
                            jsonObject.put("info", 0);
                        }
                    }
                    else
                    {
                        jsonObject.put("info", 0);
                    }

                    // 根据产品id,查询产品列表 (产品图片)
                    String url = publiceService.getImgPath(productListModel.getImgurl(), vo.getStoreId());
                    jsonObject.put("imgurl", url);
                    jsonObject.put("sid", sid);
                    jsonObject.put("is_distribution", productListModel.getIs_distribution());
                    jsonObject.put("recycle", productListModel.getRecycle());
                    productList.add(jsonObject);

                    // 订单详情状态
                    Integer rStatus = orderDetails.getR_status();
                    if (rStatus != null)
                    {
                        //订单详情状态为4的订单
                        GoGroupOrderDetailsModel orderDetails4 = new GoGroupOrderDetailsModel();
                        orderDetails4.setStore_id(vo.getStoreId());
                        orderDetails4.setR_sNo(sNo);
                        orderDetails4 = goGroupOrderDetailsModelMapper.selectOne(orderDetails4);
                        // 如果订单下面的商品都处在同一状态,那就改订单状态为已完成
                        if (orderDetails4.getR_type().equals(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS) && orderDetails4.getR_status() == 4)
                        {
                            //如果订单数量相等 则修改父订单状态
                            GoGroupOrderModel goGroupOrderModelUpdate = new GoGroupOrderModel();
                            goGroupOrderModelUpdate.setId(orderModel.getId());
                            goGroupOrderModelUpdate.setStatus(rStatus);
                            goGroupOrderModelMapper.updateByPrimaryKeySelective(goGroupOrderModelUpdate);
                        }
                        else
                        {
                            status = rStatus;
                        }
                    }
                }
                for (Integer val : batch)
                {
                    if (val == 0)
                    {
                        // 有批量退货按钮
                        batchDel = 0;
                        break;
                    }
                }
            }
            else
            {
                batchDel = 0;
            }

            StringBuilder mchStoreAddress = new StringBuilder();
            if (selfLifting == 1)
            {
                MchStoreModel mchStoreModel = new MchStoreModel();
                mchStoreModel.setStore_id(vo.getStoreId());
                mchStoreModel.setMch_id(Integer.parseInt(mchId));
                mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                if (mchStoreModel != null)
                {
                    fullAddress = mchStoreAddress.append(mchStoreModel.getShi()).append(mchStoreModel.getXian()).append(mchStoreModel.getAddress()).toString();
                }
            }
            //拼团规则
            resultMap.putAll(publiceGroupService.validataGroup(vo.getStoreId(), user.getUser_id()));
            //刷新缓存
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, redisUtil);

            resultMap.put("allow", null);
            resultMap.put("address", fullAddress);
            resultMap.put("add_time", DateUtil.dateFormate(addTime, GloabConst.TimePattern.YMDHMS));
            resultMap.put("batch_del", batchDel);
            resultMap.put("couponActivityName", couponActivityName);
            resultMap.put("coupon_price", couponPrice);
            resultMap.put("couponName", couponName);
            resultMap.put("company", unitTime);
            resultMap.put("comm_discount", 1);
            resultMap.put("discount_type", discountType);
            resultMap.put("delivery_status", deliveryStatus);
            resultMap.put("gstatus", null);
            resultMap.put("enterless", enterless);
            resultMap.put("grade_rate", gradeRate);
            resultMap.put("grade_rate_amount", gradeRateAmount);
            resultMap.put("hand_del", hand_del);
            resultMap.put("is_end", null);
            resultMap.put("isinpt", false);
            resultMap.put("id", orderId);
            resultMap.put("jp", null);
            resultMap.put("logistics", null);
            resultMap.put("list", productList);
            resultMap.put("message", sNo);
            resultMap.put("mobile", mobile1);
            resultMap.put("mch_name", mchName);
            resultMap.put("sNo", sNo);
            resultMap.put("name", name);
            resultMap.put("otype", otype);
            resultMap.put("order_no", null);
            resultMap.put("omsg", orderModel);
            resultMap.put("order_failure", orderFailure);
            resultMap.put("offset_balance", offset_balance);
            resultMap.put("pro_id", null);
            resultMap.put("p_sNo", null);
            resultMap.put("pttype", null);
            resultMap.put("product_total", productTotal);
            resultMap.put("password_status", passwordStatus);
            resultMap.put("preferential_amount", preferentialAmount);
            resultMap.put("payment", publicOrderService.getPaymentConfig(vo.getStoreId()));
            resultMap.put("r_status", status);
            resultMap.put("remarks", remarks);
            resultMap.put("sale_type", 0);
            resultMap.put("subtraction_list", null);
            resultMap.put("self_lifting", selfLifting);
            resultMap.put("user_money", userMoney);
            resultMap.put("user_can_ms", true);
            resultMap.put("user_can_after", userCanAfter);
            resultMap.put("user_can_buy_ms", true);
            resultMap.put("status", status);
            resultMap.put("z_price", zPrice);
            resultMap.put("z_freight", zFreight);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团订单选择支付方式 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderDetails");
        }
        return resultMap;
    }

}

