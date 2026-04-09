package com.laiketui.common.service.dubbo.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.*;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.api.plugin.PublicAuctionService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.service.dubbo.third.PublicPaypalServiceImpl;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.cache.RedisLockTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.auction.*;
import com.laiketui.domain.config.AuctionConfigModel;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.mch.MchBrowseModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.UserCollectionModel;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.auction.AddGoodsVo;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.pay.PaymentVo;
import com.laiketui.domain.vo.plugin.auction.PromiseOrderVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 公共竞拍方法
 *
 * @author Trick
 * @date 2022/7/1 17:58
 */
@Service
public class PublicAuctionServiceImpl implements PublicAuctionService
{

    private final Logger logger = LoggerFactory.getLogger(PublicAuctionServiceImpl.class);

    @Autowired
    private AuctionSpecialModelMapper auctionSpecialModelMapper;

    @Autowired
    private AuctionSessionModelMapper auctionSessionModelMapper;

    @Autowired
    private AuctionProductModelMapper auctionProductModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private AuctionPromiseModelMapper auctionPromiseModelMapper;

    @Autowired
    private AuctionRemindModelMapper auctionRemindModelMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private AuctionRecordModelMapper auctionRecordModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private AuctionConfigModelMapper auctionConfigModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private RedisLockTool redisLockTool;

    @Override
    public int getStatus(String id) throws LaiKeAPIException
    {
        int status = AuctionSpecialModel.AuctionStatus.STATUS_NOT_STARTED;
        try
        {
            AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(id);
            if (auctionSpecialOld == null || auctionSpecialOld.getRecovery().equals(DictionaryConst.ProductRecycle.RECOVERY))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            //当前时间大于截至时间 未开始
            if (DateUtil.dateCompare(new Date(), auctionSpecialOld.getStart_date()))
            {
                //当前时间大于结束时间 已结束
                if (!DateUtil.dateCompare(auctionSpecialOld.getEnd_date(), new Date()))
                {
                    logger.debug("专场活动id:{} 已标记结束", id);
                    status = AuctionSpecialModel.AuctionStatus.STATUS_END_STARTED;
                }
                else
                {
                    logger.debug("专场活动id:{} 已标记开始", id);
                    status = AuctionSpecialModel.AuctionStatus.STATUS_STARTED;
                }
            }
            else
            {
                logger.debug("专场活动id:{} 已标记未开始", id);
            }
            AuctionSpecialModel auctionSpecialUpdate = new AuctionSpecialModel();
            auctionSpecialUpdate.setId(id);
            auctionSpecialUpdate.setStatus(status);
            logger.debug("专场活动id:{} 状态是否变更:{}", id, auctionSpecialModelMapper.updateByPrimaryKeySelective(auctionSpecialUpdate) > 0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取专场活动状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStatus");
        }
        return status;
    }

    @Override
    public int getStatusBySession(String id) throws LaiKeAPIException
    {
        int status = AuctionSpecialModel.AuctionStatus.STATUS_NOT_STARTED;
        try
        {
            AuctionSessionModel auctionSessionOld = auctionSessionModelMapper.selectByPrimaryKey(id);
            if (auctionSessionOld == null || auctionSessionOld.getRecovery().equals(DictionaryConst.ProductRecycle.RECOVERY))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            //判断专场是否开始,只有专场开始了场次才做判断
            int specialStatus = getStatus(auctionSessionOld.getSpecial_id());
            if (AuctionSpecialModel.AuctionStatus.STATUS_STARTED.equals(specialStatus))
            {
                //当前时间大于结束时间 已结束
                if (!DateUtil.dateCompare(auctionSessionOld.getEnd_date(), new Date()))
                {
                    logger.debug("场次活动id:{} 已标记结束", id);
                    status = AuctionSessionModel.AuctionStatus.STATUS_END_STARTED;
                }
                else if (DateUtil.dateCompare(new Date(), auctionSessionOld.getStart_date()))
                {
                    logger.debug("场次活动id:{} 已标记开始", id);
                    status = AuctionSessionModel.AuctionStatus.STATUS_STARTED;
                }
                else
                {
                    logger.debug("场次活动id:{} 已标记未开始", id);
                }
            }
            else if (AuctionSpecialModel.AuctionStatus.STATUS_END_STARTED.equals(specialStatus))
            {
                status = AuctionSessionModel.AuctionStatus.STATUS_END_STARTED;
            }
            AuctionSessionModel auctionSessionUpdate = new AuctionSessionModel();
            auctionSessionUpdate.setId(id);
            auctionSessionUpdate.setStatus(status);
            logger.debug("场次活动id:{} 状态是否变更:{}", id, auctionSessionModelMapper.updateByPrimaryKeySelective(auctionSessionUpdate) > 0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取场次活动状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStatusBySession");
        }
        return status;
    }

    @Override
    public String getStatusName(int status) throws LaiKeAPIException
    {
        String statusName = "未开始";
        try
        {
            if (AuctionSpecialModel.AuctionStatus.STATUS_STARTED.equals(status))
            {
                statusName = "进行中";
            }
            else if (AuctionSpecialModel.AuctionStatus.STATUS_END_STARTED.equals(status))
            {
                statusName = "已结束";
            }
            return statusName;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取活动状态名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStatusName");
        }
    }

    @Override
    public String getStatusNameBySession(int status) throws LaiKeAPIException
    {
        String statusName = "未开始";
        try
        {
            if (AuctionSessionModel.AuctionStatus.STATUS_STARTED.equals(status))
            {
                statusName = "进行中";
            }
            else if (AuctionSessionModel.AuctionStatus.STATUS_END_STARTED.equals(status))
            {
                statusName = "已结束";
            }
            return statusName;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取场次活动状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStatusNameBySession");
        }
    }

    @Override
    public String getGoodsStatusName(int status) throws LaiKeAPIException
    {
        String statusName = "待拍卖";
        try
        {
            if (AuctionProductModel.GoodsStatus.STATUS_SOLD_IN_PROGRESS.equals(status))
            {
                statusName = "拍卖中";
            }
            else if (AuctionProductModel.GoodsStatus.STATUS_SOLD.equals(status))
            {
                statusName = "已拍卖";
            }
            else if (AuctionProductModel.GoodsStatus.STATUS_UNSOLD.equals(status))
            {
                statusName = "已流拍";
            }
            return statusName;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品状态 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsStatusName");
        }
    }

    @Override
    public Map<String, Object> getSessionGoods(MainVo vo, String specialId, String sessionId) throws LaiKeAPIException
    {
        try
        {
            return getSessionGoods(vo, specialId, sessionId, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据场次id获取下面的商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSessionGoodsList");
        }
    }

    @Override
    public Map<String, Object> getSessionGoods(MainVo vo, String specialId, String sessionId, Map<String, Object> sessionParmaMap) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> sessionResultList = new ArrayList<>();
            Map<String, Object>       parmaSessionMap   = new HashMap<>(16);
            if (sessionParmaMap != null && !sessionParmaMap.isEmpty())
            {
                parmaSessionMap.putAll(sessionParmaMap);
            }
            if (StringUtils.isNotEmpty(specialId))
            {
                parmaSessionMap.put("special_id", specialId);
            }
            if (StringUtils.isNotEmpty(sessionId))
            {
                parmaSessionMap.put("id", sessionId);
            }
            parmaSessionMap.put("store_id", vo.getStoreId());
            parmaSessionMap.put("is_show", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaSessionMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> sessionList = auctionSessionModelMapper.selectAuctionSessionList(parmaSessionMap);
            for (Map<String, Object> sessionMap : sessionList)
            {
                Map<String, Object> sessionResultMap = new HashMap<>(16);
                Integer status = MapUtils.getInteger(sessionMap, "status");
                sessionResultMap.put("sessionId", MapUtils.getString(sessionMap, "id"));
                //场次名称
                sessionResultMap.put("sessionName", MapUtils.getString(sessionMap, "sessionName"));
                //获取场次下的商品
                List<Map<String, Object>> goodsResultList = new ArrayList<>();
                int                       total           = auctionProductModelMapper.countGoodsJoinConfigureById(vo.getStoreId(), MapUtils.getString(sessionMap, "id"));
                List<Map<String, Object>> goodsList;
                if (total > 0)
                {
                    String priceSort = DataUtils.Sort.DESC.toString();
                    if (sessionParmaMap != null && !sessionParmaMap.isEmpty())
                    {
                        if (parmaSessionMap.containsKey("priceSort"))
                        {
                            priceSort = MapUtils.getString(sessionParmaMap, "priceSort");
                        }
                    }
                    goodsList = auctionProductModelMapper.getGoodsJoinConfigureById(vo.getStoreId(), MapUtils.getString(sessionMap, "id"), priceSort, vo.getPageNo(), vo.getPageSize());
                    for (Map<String, Object> goodsMap : goodsList)
                    {
                        Map<String, Object> goodsResultMap = new HashMap<>(16);
                        goodsResultMap.put("img", publiceService.getImgPath(MapUtils.getString(goodsMap, "img"), vo.getStoreId()));
                        goodsResultMap.put("id", MapUtils.getString(goodsMap, "id"));
                        goodsResultMap.put("acId", MapUtils.getInteger(goodsMap, "acId"));
                        goodsResultMap.put("mchId", MapUtils.getInteger(goodsMap, "mchId"));
                        goodsResultMap.put("outNum", MapUtils.getInteger(goodsMap, "outNum",0));
                        //如果当前出价为空则获取起拍价
                        BigDecimal currentPrice = new BigDecimal(MapUtils.getString(goodsMap, "price"));
                        BigDecimal starting_amt = new BigDecimal(MapUtils.getString(goodsMap, "starting_amt"));
                        goodsResultMap.put("price", currentPrice);
                        goodsResultMap.put("status",status);
                        goodsResultMap.put("starting_amt", starting_amt);
                        goodsResultMap.put("goodsName", MapUtils.getString(goodsMap, "product_title"));
                        goodsResultMap.put("attribute", GoodsDataUtils.getProductSkuValue(MapUtils.getString(goodsMap, "attribute")));
                        //商品是否收藏过
                        goodsResultMap.put("isCollection", MapUtils.getInteger(goodsMap, "collectionId") != null);
                        goodsResultList.add(goodsResultMap);
                    }
                }
                sessionResultMap.put("goods", goodsResultList);
                sessionResultMap.put("total", total);
                sessionResultList.add(sessionResultMap);
            }
            resultMap.put("sessionList", sessionResultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取专场下所有商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSessionGoodsList");
        }
        return resultMap;
    }

    @Override
    public boolean isOutAmt(int auctionGoodsId) throws LaiKeAPIException
    {
        boolean isOutAmt = false;
        try
        {
            AuctionProductModel auctionProductOld = auctionProductModelMapper.selectByPrimaryKey(auctionGoodsId);
            if (auctionProductOld != null)
            {
                //更新场次状态
                int status = getStatusBySession(auctionProductOld.getSession_id());
                if (AuctionSessionModel.AuctionStatus.STATUS_STARTED.equals(status))
                {
                    isOutAmt = true;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("当前竞拍商品是否可以出价 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isOutAmt");
        }
        return isOutAmt;
    }

    @Override
    public void delSpecial(MainVo vo, String id, User user) throws LaiKeAPIException
    {
        try
        {
            int row;
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int status = this.getStatus(id);
            //进行中的活动不能操作
            if (AuctionSessionModel.AuctionStatus.STATUS_STARTED.equals(status))
            {
                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_JXZDHDBNCZ, "进行中的活动不能操作");
            }
            AuctionSpecialModel auctionSpecialOld = new AuctionSpecialModel();
            auctionSpecialOld.setStore_id(vo.getStoreId());
            auctionSpecialOld.setId(id);
            auctionSpecialOld = auctionSpecialModelMapper.selectOne(auctionSpecialOld);
            if (auctionSpecialOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }

            AuctionSpecialModel auctionSpecialUpdate = new AuctionSpecialModel();
            auctionSpecialUpdate.setId(id);
            auctionSpecialUpdate.setUpdate_date(new Date());
            auctionSpecialUpdate.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);

            row = auctionSpecialModelMapper.updateByPrimaryKeySelective(auctionSpecialUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //回滚所有竞拍商品库存
            AuctionSessionModel auctionSessionModel = new AuctionSessionModel();
            auctionSessionModel.setSpecial_id(auctionSpecialOld.getId());
            auctionSessionModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
            List<AuctionSessionModel> sessionModels = auctionSessionModelMapper.select(auctionSessionModel);
            for (AuctionSessionModel sessionModel : sessionModels)
            {
                AuctionProductModel auctionProductModel = new AuctionProductModel();
                auctionProductModel.setSession_id(sessionModel.getId());
                auctionProductModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
                List<AuctionProductModel> productModels = auctionProductModelMapper.select(auctionProductModel);
                for (AuctionProductModel productModel : productModels)
                {
                    //回滚商品库存
                    AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(vo.getStoreId());
                    addStockVo.setId(productModel.getAttr_id());
                    addStockVo.setPid(productModel.getGoods_id());
                    addStockVo.setAddNum(1);
                    addStockVo.setText("删除竞拍商品返还" + 1);
                    publicStockService.addGoodsStock(addStockVo, null);
                }
            }
            //删除场次
            logger.debug("删除专场id={},删除{}个场次", id, auctionSessionModelMapper.delSessionByAuctionId(id));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除专场 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSpecial");
        }
    }

    @Override
    public void addGoodsBySessionId(int storeId, String attrJson, String specialId, String sessionId, boolean isDelOldGoods) throws LaiKeAPIException
    {
        try
        {
            addGoodsBySessionId(storeId, attrJson, specialId, sessionId, isDelOldGoods, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除专场 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSpecial");
        }
    }

    @Override
    public void addGoodsBySessionId(int storeId, String attrJson, String specialId, String sessionId, boolean isDelOldGoods, Integer mchId) throws LaiKeAPIException
    {
        try
        {
            int row;
            //删除之前场次商品
            if (isDelOldGoods)
            {
                //回滚竞拍商品库存
                AuctionProductModel auctionProductModel = new AuctionProductModel();
                auctionProductModel.setSession_id(sessionId);
                auctionProductModel.setRecovery(DictionaryConst.WhetherMaven.WHETHER_NO);
                List<AuctionProductModel> select = auctionProductModelMapper.select(auctionProductModel);
                for (AuctionProductModel model : select)
                {
                    //回滚商品库存
                    AddStockVo addStockVo = new AddStockVo();
                    addStockVo.setStoreId(storeId);
                    addStockVo.setId(model.getAttr_id());
                    addStockVo.setPid(model.getGoods_id());
                    addStockVo.setAddNum(1);
                    addStockVo.setText("删除竞拍商品返还" + 1);
                    publicStockService.addGoodsStock(addStockVo, null);
                }
                if (mchId != null)
                {
                    row = auctionProductModelMapper.delAuctionGoodsBySessionIdByMchId(sessionId, mchId);
                }
                else
                {
                    row = auctionProductModelMapper.delAuctionGoodsBySessionId1(sessionId);
                }
                logger.debug("删除之前场次id:{} 商品数量:{}", sessionId, row);
            }
            List<AuctionProductModel> auctionProductSaveList = new ArrayList<>();
            List<AddGoodsVo> attrList = JSON.parseObject(attrJson, new TypeReference<List<AddGoodsVo>>()
            {
            });
            AuctionSpecialModel auctionSpecialOld = null;
            if (StringUtils.isNotEmpty(specialId))
            {
                auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
                if (auctionSpecialOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
            }
            for (AddGoodsVo attr : attrList)
            {
                int attrId = attr.getAttrId();
                //商品是否参加了其它竞拍场次
                if (auctionSessionModelMapper.isAuctionGoods(new Date(), specialId, sessionId, attrId) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_SPYCJHD, "商品已参加活动");
                }
                Integer pid = confiGureModelMapper.getGoodsId(attrId);
                if (pid == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                }
                //商品价格
                BigDecimal goodsPrice = confiGureModelMapper.getGoodsPirce(attrId);

                if (auctionSpecialOld != null)
                {
//                    if (BigDecimal.ZERO.compareTo(new BigDecimal(attr.getStartingAmt())) >= 0) {
//                        attr.setStartingAmt(auctionSpecialOld.getNumber().toString());
//                    }
                    //起拍价
                    if (StringUtils.isNotEmpty(attr.getMarkUpAmt()) && BigDecimal.ZERO.compareTo(new BigDecimal(attr.getMarkUpAmt())) >= 0)
                    {
                        attr.setMarkUpAmt(auctionSpecialOld.getMark_up_amt().toString());
                    }

                    //起拍价
                    BigDecimal startingAmt = BigDecimal.ZERO;
                    if (StringUtils.isNotEmpty(attr.getStartingAmt()))
                    {
                        startingAmt = new BigDecimal(attr.getStartingAmt());
                    }

                    //加价幅度
                    if (StringUtils.isEmpty(attr.getMarkUpAmt()))
                    {
                        throw new LaiKeAPIException("加价幅度不能为空");
                    }
                    BigDecimal markUpAmt = new BigDecimal(attr.getMarkUpAmt());
                    if (BigDecimal.ZERO.compareTo(startingAmt) >= 0)
                    {
                        logger.debug("添加商品时,起拍价为空 获取专场默认设置");
                        //是否百分比
                        boolean isPercentage = auctionSpecialOld.getUnit().equals(AuctionSpecialModel.SpecialUnit.UNIT_PERCENTAGE);
                        if (isPercentage)
                        {
                            attr.setStartingAmt(goodsPrice.multiply(auctionSpecialOld.getNumber().divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)).toString());
                        }
                        else
                        {
                            attr.setStartingAmt(startingAmt.toString());
                        }
                    }
                    if (BigDecimal.ZERO.compareTo(markUpAmt) >= 0)
                    {
                        logger.debug("添加商品时,加价幅度为空 获取专场默认设置");
                        attr.setMarkUpAmt(markUpAmt.toString());
                    }
                }
                //加价幅度最小0.01
                if (new BigDecimal("0.01").compareTo(new BigDecimal(attr.getMarkUpAmt())) > 0)
                {
                    attr.setMarkUpAmt("0.01");
                }

                AuctionProductModel auctionProductSave = new AuctionProductModel();
                auctionProductSave.setSession_id(sessionId);
                auctionProductSave.setAttr_id(attrId);
                auctionProductSave.setGoods_id(pid);
                auctionProductSave.setGoods_price(goodsPrice);
                auctionProductSave.setStarting_amt(new BigDecimal(attr.getStartingAmt()));
                auctionProductSave.setMark_up_amt(new BigDecimal(attr.getMarkUpAmt()));
                //默认起拍价
                auctionProductSave.setPrice(auctionProductSave.getStarting_amt());
                auctionProductSave.setStatus(AuctionProductModel.GoodsStatus.STATUS_SOLD_WAIT);
                auctionProductSave.setIs_show(DictionaryConst.WhetherMaven.WHETHER_OK);
                auctionProductSave.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                auctionProductSave.setAdd_date(new Date());
                auctionProductSaveList.add(auctionProductSave);
                //商品出库
                publicStockService.outStockNum(storeId, pid, attrId, 1, true);
            }
            if (auctionProductSaveList.size() > 0)
            {
                if (auctionProductModelMapper.insertList(auctionProductSaveList) < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品到场次中 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSpecial");
        }
    }

    @Override
    public void paymentPromise(PromiseOrderVo vo, String orderNo) throws LaiKeAPIException
    {
        try
        {
            int row;
            row = auctionPromiseModelMapper.updateStatus(vo.getStoreId(), orderNo, OrderDataModel.PayStatus.PAYMENT);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("竞拍保证金支付回调 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "paymentPromise");
        }
    }

    @Override
    public Map<String, Object> getSoonInfo(String specialId,User user) throws LaiKeAPIException
    {
        Map<String,Object> resuleMap = new HashMap<>();

        AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
        if (auctionSpecialOld == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "专场不存在");
        }

        boolean isRemind = false;
        if (user != null)
        {
            isRemind = auctionRemindModelMapper.isSpecialRemindByUserId(auctionSpecialOld.getId(), user.getUser_id()) > 0;
        }
        //关注数
        AuctionRemindModel  auctionRemind = new AuctionRemindModel();
        auctionRemind.setSpecial_id(specialId);
        int followNum = auctionRemindModelMapper.selectCount(auctionRemind);
        resuleMap.put("followNum",followNum);

        //拍品数
        int goodsTotal = auctionSpecialModelMapper.sumSpecialGoodsNum(specialId);
        resuleMap.put("goodsTotal",goodsTotal);


        //this.getMainImg(resuleMap,auctionSpecialOld.getStore_id(),specialId);
        resuleMap.put("isRemind", isRemind);
        resuleMap.put("id", auctionSpecialOld.getId());
        resuleMap.put("name", auctionSpecialOld.getName());
        resuleMap.put("startDate", DateUtil.dateFormate(auctionSpecialOld.getStart_date(), GloabConst.TimePattern.YMDHMS));
        resuleMap.put("endDate", DateUtil.dateFormate(auctionSpecialOld.getEnd_date(), GloabConst.TimePattern.YMDHMS));
        resuleMap.put("content", auctionSpecialOld.getContent());
        String img = auctionSpecialOld.getImg();
        img = publiceService.getImgPath(img,auctionSpecialOld.getStore_id());

        MchModel mchModel = mchModelMapper.selectByPrimaryKey(auctionSpecialOld.getMch_id());
        String headImg = mchModel.getHead_img();
        resuleMap.put("mch_logo",publiceService.getImgPath(headImg,auctionSpecialOld.getStore_id()));
        resuleMap.put("mch_name",mchModel.getName());
        resuleMap.put("mch_id",mchModel.getId());

        resuleMap.put("img", img);
        return resuleMap;
    }

    @Override
    public void setRemind(String specialId, User user) throws LaiKeAPIException
    {
        int row = 0;
        boolean             isRemind       = auctionRemindModelMapper.isSpecialRemindByUserId(specialId, user.getUser_id()) > 0;
        AuctionSpecialModel auctionSpecial = new AuctionSpecialModel();
        auctionSpecial.setId(specialId);
        if (auctionSpecialModelMapper.selectCount(auctionSpecial) < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "专场不存在");
        }
        AuctionRemindModel auctionRemindSave = new AuctionRemindModel();
        auctionRemindSave.setUser_id(user.getUser_id());
        auctionRemindSave.setSpecial_id(specialId);

        if (isRemind)
        {
            auctionRemindSave = auctionRemindModelMapper.selectOne(auctionRemindSave);
            if (auctionRemindSave != null)
            {
                //已经设置了提醒再一次则是取消提醒
                row = auctionRemindModelMapper.deleteByPrimaryKey(auctionRemindSave.getId());
            }
        }
        else
        {
            auctionRemindSave.setId(BuilderIDTool.getSnowflakeId() + "");
            auctionRemindSave.setUser_id(user.getUser_id());
            auctionRemindSave.setSpecial_id(specialId);
            auctionRemindSave.setAdd_date(new Date());
            row = auctionRemindModelMapper.insertSelective(auctionRemindSave);
        }
        if (row < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
        }
    }

    @Override
    public Map<String, Object> getGoodsDetail(User user, Integer acid,Integer store_id) throws LaiKeAPIException
    {

        Map<String,Object> resultMap = new HashMap<>();

        Map<String, Object> goodsInfo = auctionProductModelMapper.getGoodsDetail(acid);
        if (goodsInfo == null || goodsInfo.isEmpty())
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSJCW, "商品不存在");
        }
        //店铺信息
        Map<String,Object> shopMap = new HashMap<>();
        Integer mchId = MapUtils.getInteger(goodsInfo, "mchId");
        MchModel mchModel = new MchModel();
        mchModel.setStore_id(store_id);
        mchModel.setId(mchId);
        mchModel = mchModelMapper.selectOne(mchModel);
        if (mchModel != null)
        {
            shopMap.put("shop_id", mchId);
            shopMap.put("shop_name", mchModel.getName());
            shopMap.put("is_open", publicMchService.mchIsOpen(mchId));
            String logoUrl = publiceService.getImgPath(mchModel.getHead_img(), store_id);
            shopMap.put("shop_logo", logoUrl);

            //获取店铺成交数量
            int turnoverNum = auctionSpecialModelMapper.getNumByStatus(mchId,2);
            shopMap.put("quantity_sold", turnoverNum);

            //获取店铺收藏数量
            int collection_num = userCollectionModelMapper.getAuctionMchCollectionNum(mchId);
            shopMap.put("collection_num",collection_num);

            //获取店铺拍品数
            Map<String,Object> param = new HashMap<>();
            param.put("mch_id",mchId);
            int mchGoodsTotal = auctionProductModelMapper.countGoodsInfoList(param);
            shopMap.put("mchGoodsTotal", mchGoodsTotal);
        }

        resultMap.put("shop_list", shopMap);

        //获取商品运费信息
        BigDecimal yuFeiPrice = BigDecimal.ZERO;
        //禅道34092
        if (user != null)
        {
            UserAddress userAddress = new UserAddress();
            userAddress.setUid(user.getUser_id());
            userAddress.setIs_default(1);
            userAddress.setStore_id(store_id);
            userAddress = userAddressMapper.selectOne(userAddress);
            if (userAddress != null)
            {
                String           freight          = MapUtils.getString(goodsInfo, "freight");
                String           defaultFreight   = MapUtils.getString(goodsInfo, "default_freight");
                Integer          supplierId       = MapUtils.getInteger(goodsInfo, "supplier_id");
                DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                yuFeiPrice = new BigDecimal(defaultFreightVO.getNum2());
            }
        }
        goodsInfo.put("freight", yuFeiPrice);
        //是否缴纳过保证金
        boolean isPayPromise = false;
        //当前用户是否已收藏该商品
        boolean isCollection = false;
        //是否是得主
        boolean isMain = false;
        if (user != null)
        {
            UserCollectionModel userCollectionCount = new UserCollectionModel();
            userCollectionCount.setUser_id(user.getUser_id());
            userCollectionCount.setP_id(acid);
            userCollectionCount.setStore_id(store_id);
            userCollectionCount.setType(UserCollectionModel.CollectionType.TYPE_JP);
            isCollection = userCollectionModelMapper.selectCount(userCollectionCount) > 0;
            String userId = MapUtils.getString(goodsInfo, "user_id");
            isMain = user.getUser_id().equals(userId);
        }
        if (user != null)
        {
            isPayPromise = auctionPromiseModelMapper.isPayPromiseByAcId(store_id, MapUtils.getString(goodsInfo, "special_id"), user.getUser_id()) > 0;
        }
        //商品id
        Integer goodsId = MapUtils.getInteger(goodsInfo, "goodsId");
        //商品图片
        String imgurl = MapUtils.getString(goodsInfo, "imgurl");
        imgurl = publiceService.getImgPath(imgurl,store_id);
        goodsInfo.put("imgurl", imgurl);
        //规格id
        Integer        attrId = MapUtils.getInteger(goodsInfo, "attr_id");
        ConfiGureModel attr   = confiGureModelMapper.selectByPrimaryKey(attrId);
        if (attr != null)
        {
            goodsInfo.put("attribute", GoodsDataUtils.getProductSkuValue(attr.getAttribute()));
        }
        //轮播图
        List<String> imgList    = new ArrayList<>();
        List<String> imgUrlList = productImgModelMapper.getBannerImgByGoodsId(goodsId);
        for (String imgUrl : imgUrlList)
        {
            imgList.add(publiceService.getImgPath(imgUrl, store_id));
        }
        resultMap.put("imgList", imgList);
        //获取商品的当前竞拍价
        BigDecimal currentPrice = new BigDecimal(MapUtils.getString(goodsInfo, "price"));
        goodsInfo.put("price", currentPrice);
        //场次开始时间
        resultMap.put("startDate", DateUtil.dateFormate(MapUtils.getString(goodsInfo, "start_date"), GloabConst.TimePattern.YMDHMS));
        //保证金
        //场次结束时间
        resultMap.put("endDate", DateUtil.dateFormate(MapUtils.getString(goodsInfo, "end_date"), GloabConst.TimePattern.YMDHMS));
        //是否缴纳保证金
        resultMap.put("isPayPromise", isPayPromise);
        //是否可以出价
        resultMap.put("isOutAmt", this.isOutAmt(acid));
        //是否收藏
        resultMap.put("isCollection", isCollection);
        //商品信息
        resultMap.put("goodsInfo", goodsInfo);
        //是否是得主
        resultMap.put("isMain", isMain);

        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsOutAmtRecord(MainVo vo, Integer acid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> outRecordList = new ArrayList<>();

        Map<String, Object> parmaMap = new HashMap<>(16);
        parmaMap.put("store_id", vo.getStoreId());
        parmaMap.put("auction_id", acid);
        parmaMap.put("pageStart", vo.getPageNo());
        parmaMap.put("pageEnd", vo.getPageSize());
        parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
        int total = auctionRecordModelMapper.countAuctionRecordInfo(parmaMap);
        if (total > 0)
        {
            List<Map<String, Object>> resBid = auctionRecordModelMapper.selectAuctionRecordInfo(parmaMap);
            for (Map<String, Object> map : resBid)
            {
                Map<String, Object> recordMap = new HashMap<>(16);
                recordMap.put("price", MapUtils.getString(map, "price"));
                recordMap.put("user_name", MapUtils.getString(map, "user_name"));
                recordMap.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                outRecordList.add(recordMap);
            }
        }
        resultMap.put("list", outRecordList);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public void lookSpecial(MainVo vo, String specialId) throws LaiKeAPIException
    {
        AuctionSpecialModel auctionSpecialOld = new AuctionSpecialModel();
        auctionSpecialOld.setId(specialId);
        auctionSpecialOld.setStore_id(vo.getStoreId());
        auctionSpecialOld.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
        if (auctionSpecialModelMapper.selectCount(auctionSpecialOld) < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "专场不存在");
        }
        auctionSpecialModelMapper.lookSpecial(specialId);
    }

    @Override
    public Map<String, Object> payPromisePage(Integer store_id, String specialId, User user,Integer type) throws LaiKeAPIException
    {
        Map<String,Object> resultMap = new HashMap<>();
        //是否已经支付了保证金
        boolean ifPayPromise = auctionPromiseModelMapper.isPayPromiseByAcId(store_id, specialId, user.getUser_id()) > 0;
        if (type == 1 && ifPayPromise)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_YJNGBZJ, "已缴纳过保证金");
        }
        AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
        if (auctionSpecialOld == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "场次不存在");
        }
        AuctionConfigModel auctionConfigOld = new AuctionConfigModel();
        auctionConfigOld.setStore_id(store_id);
        auctionConfigOld = auctionConfigModelMapper.selectOne(auctionConfigOld);
        if (auctionConfigOld == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJWPZ, "插件未配置");
        }
        if (auctionConfigOld.getIs_open() == 0)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJWKQ, "插件未开启");
        }

        //获取支付方式
        Map<String, Object> paramQueryMap = new HashMap<>();
        paramQueryMap.put("store_id", store_id);
        //支付相关配置的信息
        List<Map<String, Object>> paymentStatus = paymentConfigModelMapper.getPaymentConfigDynamic(paramQueryMap);
        Map<String, Integer>      payment       = new HashMap<>(16);
        for (Map<String, Object> paymentConf : paymentStatus)
        {
            String payName = DataUtils.getStringVal(paymentConf, "class_name");
            if (StringUtils.isEmpty(payName))
            {
                continue;
            }
            int isOpen = DataUtils.getIntegerVal(paymentConf, "statusSwitch", 0);
            payment.put(DataUtils.getStringVal(paymentConf, "class_name"), isOpen);
        }
        user = userBaseMapper.selectByPrimaryKey(user.getId());
        RedisDataTool.refreshRedisUserCache(user, redisUtil);

        //是否设置了支付密码
        int passwordStatus = 1;
        if (StringUtils.isEmpty(user.getPassword()))
        {
            passwordStatus = 0;
        }
        resultMap.put("isPayPromise", ifPayPromise);
        resultMap.put("password_status", passwordStatus);
        //支持的支付方式
        resultMap.put("payment", payment);
        //插件规则
        resultMap.put("auctionRule", auctionConfigOld.getContent());
        //场次名称
        resultMap.put("specialName", auctionSpecialOld.getName());
        //账户余额
        resultMap.put("money", user.getMoney());
        //所需支付的金额
        resultMap.put("total", auctionSpecialOld.getPromise_amt());
        //协议标题
        resultMap.put("agreeTitle", auctionConfigOld.getAgree_title());
        //竞拍协议
        resultMap.put("agreeContent", auctionConfigOld.getAgree_content());

        return resultMap;
    }

    @Override
    public Map<String, Object> payPromise(Integer store_id, String specialId, User user,String payType) throws LaiKeAPIException
    {
        Map<String,Object> resultMap = new HashMap<>();
        int row = 0;

        //是否已经支付了保证金
        if (auctionPromiseModelMapper.isPayPromiseByAcId(store_id, specialId, user.getUser_id()) > 0)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_YJNGBZJ, "已缴纳过保证金");
        }
        AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
        if (auctionSpecialOld == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "场次不存在");
        }
        OrderDataModel orderDataSave = new OrderDataModel();
        //临时订单表数据
        Map<String, Object> dataMap = new HashMap<>(16);
        dataMap.put("paymentAmt", auctionSpecialOld.getPromise_amt());
        dataMap.put("storeId", auctionSpecialOld.getStore_id());
        dataMap.put("specialId", auctionSpecialOld.getId());
        dataMap.put("pay", payType);
        dataMap.put("user_id", user.getUser_id());
        //是否有生成过订单,如果生成过则修改记录
        AuctionPromiseModel auctionPromiseOld = new AuctionPromiseModel();
        auctionPromiseOld.setStore_id(store_id);
        auctionPromiseOld.setUser_id(user.getUser_id());
        auctionPromiseOld.setIs_pay(OrderDataModel.PayStatus.NOT_PAY);
        auctionPromiseOld.setSpecial_id(auctionSpecialOld.getId());
        auctionPromiseOld = auctionPromiseModelMapper.selectOne(auctionPromiseOld);
        if (auctionPromiseOld != null)
        {
            auctionPromiseOld.setPromise(auctionSpecialOld.getPromise_amt());
            auctionPromiseOld.setAdd_time(new Date());
            row = auctionPromiseModelMapper.updateByPrimaryKeySelective(auctionPromiseOld);
            OrderDataModel orderDataOld = new OrderDataModel();
            orderDataOld.setTrade_no(auctionPromiseOld.getTrade_no());
            orderDataOld.setOrder_type(DictionaryConst.OrdersType.ORDERS_HEADER_JB);
            orderDataSave = orderDataModelMapper.selectOne(orderDataOld);
            if (orderDataSave == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            orderDataSave.setData(JSON.toJSONString(dataMap));
            orderDataSave.setAddtime(new Date());
            orderDataSave.setPay_type(payType);
            orderDataModelMapper.updateByPrimaryKeySelective(orderDataSave);
        }
        else
        {
            String sno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_JB);
            orderDataSave.setOrder_type(DictionaryConst.OrdersType.ORDERS_HEADER_JB);
            //下单操作
            orderDataSave.setStatus(OrderDataModel.PayStatus.NOT_PAY);
            //记录竞拍保证金
            AuctionPromiseModel auctionPromiseSave = new AuctionPromiseModel();
            auctionPromiseSave.setTrade_no(sno);
            auctionPromiseSave.setSpecial_id(auctionSpecialOld.getId());
            auctionPromiseSave.setPromise(auctionSpecialOld.getPromise_amt());
            auctionPromiseSave.setStore_id(store_id);
            auctionPromiseSave.setUser_id(user.getUser_id());
            auctionPromiseSave.setIs_pay(OrderDataModel.PayStatus.NOT_PAY);
            auctionPromiseSave.setAdd_time(new Date());
            row = auctionPromiseModelMapper.insertSelective(auctionPromiseSave);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSB, "下单失败");
            }
            orderDataSave.setData(JSON.toJSONString(dataMap));
            orderDataSave.setTrade_no(sno);
            orderDataSave.setPay_type(payType);
            orderDataSave.setOrder_type(DictionaryConst.OrdersType.ORDERS_HEADER_JB);
            orderDataSave.setAddtime(new Date());
            row = orderDataModelMapper.insertSelective(orderDataSave);
        }
        if (row < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
        }

        resultMap.put("sNo", orderDataSave.getTrade_no());
        resultMap.put("total", auctionSpecialOld.getPromise_amt());
        resultMap.put("specialId", auctionSpecialOld.getId());
        resultMap.put("orderTime", DateUtil.dateFormate(orderDataSave.getAddtime(), GloabConst.TimePattern.YMDHMS));

        return resultMap;
    }

    @Override
    public Map<String, Object> getSessionGoodsList(MainVo vo, String sessionId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<Map<String, Object>> goodsResultList = new ArrayList<>();
        //获取专场下的商品
        int                       total = auctionSpecialModelMapper.countSpecialGoodsList(sessionId);
        List<Map<String, Object>> goodsList;
        if (total > 0)
        {
            goodsList = auctionSpecialModelMapper.selectSpecialGoodsList(sessionId, 0, 10);
            for (Map<String, Object> goodsMap : goodsList)
            {
                Map<String, Object> goodsResultMap = new HashMap<>(16);
                goodsResultMap.put("img", publiceService.getImgPath(MapUtils.getString(goodsMap, "img"), vo.getStoreId()));
                goodsResultMap.put("id", MapUtils.getString(goodsMap, "id"));
                goodsResultList.add(goodsResultMap);
            }
        }
        resultMap.put("goods", goodsResultList);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public void getMainImg(Map<String, Object> map, Integer store_id, String specialId) throws LaiKeAPIException
    {
        String main_img = "";
        List<String> auctionProImgList = new ArrayList<>();
        List<String> imgList = auctionProductModelMapper.getImgListById(specialId);
        if (CollectionUtils.isNotEmpty(imgList))
        {
            for (String img : imgList)
            {
                img = publiceService.getImgPath(img,store_id);
                auctionProImgList.add(img);
            }
        }
        if (CollectionUtils.isNotEmpty(auctionProImgList))
        {
            main_img = auctionProImgList.get(0);
            auctionProImgList.remove(0);
        }
        map.put("main_img", main_img);
        map.put("auctionProImgList", auctionProImgList);
        map.put("auctionProImgList_size", auctionProImgList.size());
    }

    @Override
    public List<Map<String, Object>> sortByGoodsTotal(List<Map<String, Object>> list, String sort, String sort_criteria) throws LaiKeAPIException
    {
        // 处理空值和默认排序
        if (org.apache.commons.lang3.StringUtils.isEmpty(sort))
        {
            sort = "desc"; // 默认倒序
        }
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                map ->
                {
                    // 根据不同字段获取对应的值（处理空值，默认0）
                    if ("total_amount".equals(sort_criteria))
                    {
                        return MapUtils.getDoubleValue(map, "specialDealAmt", 0.0);
                    }
                    else
                    {
                        return (double) MapUtils.getIntValue(map, "goodsTotal", 0);
                    }
                }
        );

        if ("desc".equalsIgnoreCase(sort))
        {
            //倒序
            comparator = comparator.reversed();
        }
        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public void collection(MainVo vo, Integer acId, User user) throws LaiKeAPIException
    {
        int  row;
        if (StringUtils.isEmpty(acId))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
        }
        AuctionProductModel auctionProductOld = new AuctionProductModel();
        auctionProductOld.setId(acId);
        auctionProductOld.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
        auctionProductOld = auctionProductModelMapper.selectOne(auctionProductOld);
        if (Objects.isNull(auctionProductOld))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
        }

        //获取店铺id
        Integer goodsId = auctionProductOld.getGoods_id();
        ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);

        UserCollectionModel userCollectionSave = new UserCollectionModel();
        userCollectionSave.setStore_id(vo.getStoreId());
        userCollectionSave.setP_id(acId);
        userCollectionSave.setUser_id(user.getUser_id());
        userCollectionSave.setType(UserCollectionModel.CollectionType.TYPE_JP);
        userCollectionSave.setMch_id(productListModel.getMch_id());
        UserCollectionModel userCollectionOld = userCollectionModelMapper.selectOne(userCollectionSave);
        userCollectionSave.setAdd_time(new Date());
        if (userCollectionOld == null)
        {
            row = userCollectionModelMapper.insertSelective(userCollectionSave);
        }
        else
        {
            row = userCollectionModelMapper.deleteByPrimaryKey(userCollectionOld.getId());
        }
        if (row < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
        }
    }

    @Override
    public void offerAmt(MainVo vo, Integer acGoodsId, BigDecimal price, User user) throws LaiKeAPIException
    {
        String lockToken = "";
        String lockKey   = GloabConst.RedisHeaderKey.PLUGIN_AUCTION_LOCK + acGoodsId;
        String timeKey   = "";
        try
        {
            int  row;
            //优化(31063):只有出价的用户才会等待一段时间才可以再次出价，其他用户不需要等待一段时间。
            timeKey = GloabConst.RedisHeaderKey.PLUGIN_AUCTION_OUT_AMT + user.getUser_id() + SplitUtils.MH + acGoodsId;
            //锁
            lockToken = redisLockTool.lock(lockKey, 5000, 10000);
            //当前是否可以出价
            if (redisUtil.hasKey(timeKey))
            {
                logger.debug("{} 出价:{} 失败,还有{}s才可以出价", user.getUser_id(), price, redisUtil.getExpire(timeKey));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            if (StringUtils.isEmpty(lockToken))
            {
                logger.debug("{} 出价:{} 失败,有人正在出价中", user.getUser_id(), price);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            //获取当前最高价
            BigDecimal maxOfferAmt = auctionProductModelMapper.getGoodsMaxPrice(acGoodsId);
            if (maxOfferAmt == null)
            {
                maxOfferAmt = BigDecimal.ZERO;
            }
            if (maxOfferAmt.compareTo(price) >= 0)
            {
                logger.debug("{} 出价:{} 失败,最高价:{}", user.getUser_id(), price, maxOfferAmt);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            AuctionConfigModel auctionConfigOld = new AuctionConfigModel();
            auctionConfigOld.setStore_id(vo.getStoreId());
            auctionConfigOld = auctionConfigModelMapper.selectOne(auctionConfigOld);
            if (auctionConfigOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJWPZ, "插件未配置");
            }
            if (auctionConfigOld.getIs_open() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJWKQ, "插件未开启");
            }
            //获取竞拍信息
            Map<String, Object> goodsInfo = auctionProductModelMapper.getGoodsInfo(acGoodsId);
            if (goodsInfo == null || goodsInfo.isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "竞拍商品不存在");
            }
            //专场id
            String specialId = MapUtils.getString(goodsInfo, "special_id");
            //场次id
            String sessionId = MapUtils.getString(goodsInfo, "sessionId");
            //是否已经支付了保证金
            if (auctionPromiseModelMapper.isPayPromiseByAcId(vo.getStoreId(), specialId, user.getUser_id()) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_WJNGBZJ, "未缴纳过保证金");
            }
            int status = this.getStatusBySession(sessionId);
            if (AuctionSessionModel.AuctionStatus.STATUS_END_STARTED.equals(status))
            {
                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_YJP, "已截拍");
            }
            else if (AuctionSessionModel.AuctionStatus.STATUS_NOT_STARTED.equals(status))
            {
                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_HDHWKS, "活动还未开始");
            }
            //当前竞拍价格
            BigDecimal currentPrice = new BigDecimal(MapUtils.getString(goodsInfo, "price"));
            //起拍价
            BigDecimal startingAmt = new BigDecimal(MapUtils.getString(goodsInfo, "starting_amt"));
            //加价幅度
            BigDecimal markUpAmt = new BigDecimal(MapUtils.getString(goodsInfo, "mark_up_amt"));
            if (startingAmt.compareTo(price) >= 0)
            {
                logger.debug("{} 出价:{} 失败,当前起拍价:{}元,当前出价必须>=起拍价", user.getUser_id(), price, startingAmt);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            //当前出的价是否是设置的加价幅度(+起拍价【第一次出价的情况】) 的倍数
            BigDecimal[] bei;
            //加价幅度+起拍价【第一次出价的情况】 起拍价可为0,第一次出价需要 起拍价+加价幅度
            if (BigDecimal.ZERO.compareTo(currentPrice) == 0)
            {
                bei = price.divideAndRemainder(markUpAmt.add(startingAmt));
            }
            else
            {
                bei = price.subtract(startingAmt).divideAndRemainder(markUpAmt);
            }
            if (startingAmt.compareTo(price) != 0 && BigDecimal.ZERO.compareTo(bei[1]) != 0)
            {
                logger.debug("{} 出价:{} 失败,当前幅度:{}元,当前出的价不是设置的加价幅度的倍数", user.getUser_id(), price, markUpAmt);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }

            //出价
            AuctionRecordModel auctionRecordSave = new AuctionRecordModel();
            auctionRecordSave.setStore_id(vo.getStoreId());
            auctionRecordSave.setStore_id(vo.getStoreId());
            auctionRecordSave.setUser_id(user.getUser_id());
            auctionRecordSave.setPrice(price);
            auctionRecordSave.setAuction_id(acGoodsId);
            auctionRecordSave.setAdd_time(new Date());
            row = auctionRecordModelMapper.insertSelective(auctionRecordSave);
            if (row < 1)
            {
                logger.debug("{} 出价:{} 失败,记录失败", user.getUser_id(), price);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            //出价成功
            AuctionProductModel auctionProductUpdate = new AuctionProductModel();
            auctionProductUpdate.setId(acGoodsId);
            auctionProductUpdate.setStatus(AuctionProductModel.GoodsStatus.STATUS_SOLD_IN_PROGRESS);
            auctionProductUpdate.setPrice(price);
            row = auctionProductModelMapper.updateByPrimaryKeySelective(auctionProductUpdate);
            if (row < 1)
            {
                logger.debug("{} 出价:{} 失败,出价失败", user.getUser_id(), price);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PluginAuctionCode.ERROR_CODE_CJSB, "出价失败");
            }
            redisUtil.set(timeKey, user.getUser_id(), auctionConfigOld.getWait_time());
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        finally
        {
            if (com.laiketui.core.utils.tool.StringUtils.isNotEmpty(lockToken))
            {
                //释放锁
                logger.debug("【竞拍】释放锁{} 执行结果:{}", lockKey, redisLockTool.unlock(lockKey, lockToken));
            }
        }
    }

    @Override
    public Map<String, Object> specialDetail(MainVo vo, String specialId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //获取专场信息
        AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
        if (auctionSpecialOld == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "专场数据不存在");
        }
        resultMap.put("specialImg", publiceService.getImgPath(auctionSpecialOld.getImg(), vo.getStoreId()));
        resultMap.put("specialId", auctionSpecialOld.getId());
        resultMap.put("specialName", auctionSpecialOld.getName());
        resultMap.put("endDate", DateUtil.dateFormate(auctionSpecialOld.getEnd_date(), GloabConst.TimePattern.YMDHMS));
        resultMap.put("startDate", DateUtil.dateFormate(auctionSpecialOld.getStart_date(), GloabConst.TimePattern.YMDHMS));
        resultMap.put("commission", auctionSpecialOld.getCommission());
        resultMap.put("lookNum", auctionSpecialOld.getLook_count());
        resultMap.put("auctionNum", auctionSpecialModelMapper.sumSpecialGoodsNum(auctionSpecialOld.getId()));
        return resultMap;
    }

    @Override
    public Map<String, Object> getSessionGoodsList(MainVo vo, String sessionId, Integer sortType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<Map<String, Object>> goodsList = new ArrayList<>();
        Map<String, Object>       paramMap  = new HashMap<>(16);
        if (Objects.nonNull(sortType))
        {
            paramMap.put("priceSort", sortType == 1 ? DataUtils.Sort.DESC : DataUtils.Sort.ASC);
        }


        Integer                   total      = 0;
        Map<String, Object>       map        = this.getSessionGoods(vo, null, sessionId, paramMap);
        List<Map<String, Object>> list       = DataUtils.cast(map.get("sessionList"));
        Map<String, Object>       resultData;
        if (list != null && list.size() > 0)
        {
            resultData = DataUtils.cast(list.get(0));
            if (resultData != null)
            {
                goodsList = DataUtils.cast(resultData.get("goods"));
                total = DataUtils.cast(resultData.get("total"));
            }
        }
        resultMap.put("list", goodsList);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public Map<String, Object> getMchSpecialList(MainVo vo,User user,Map<String,Object> parmaMap,Integer status,String sort,String sort_criteria) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //1=即将开始 2=竞拍中 3=已结束
        parmaMap.put("status", status);
        //1.优化(31060):如果是即将开始的专场，专场中没有拍品，那么该专场可以在移动端展示。
        if (status != 1)
        {
            //2.优化(31060):如果专场中没有拍品且状态为竞拍中或者已结束，那么该专场将不在用户移动端进行显示。
            parmaMap.put("gt_goods_num", 0);
        }
            if (parmaMap.containsKey("type"))
        {
            parmaMap.put("type", MapUtils.getInteger(parmaMap, "type"));
        }
        parmaMap.put("isShow", DictionaryConst.WhetherMaven.WHETHER_OK);
        parmaMap.put("pageStart", vo.getPageNo());
        parmaMap.put("pageEnd", vo.getPageSize());
        List<Map<String, Object>> specialList = new ArrayList<>();
        int                       total       = auctionSpecialModelMapper.countDynamic(parmaMap);
        if (total > 0)
        {
            List<Map<String, Object>> list = auctionSpecialModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : list)
            {
                Map<String, Object> tempMap = new HashMap<>(16);
                String              id      = MapUtils.getString(map, "id");
                String mch_logo = MapUtils.getString(map, "mch_logo");
                mch_logo = publiceService.getImgPath(mch_logo, vo.getStoreId());
                tempMap.put("specialId", id);
                tempMap.put("mch_logo", mch_logo);
                this.getMainImg(map,vo.getStoreId(),id);
                tempMap.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                if (status == 1)
                {
                        tempMap.put("followNum",MapUtils.getIntValue(map, "focus_num"));
                    //当前用户是否已经提醒过
                    boolean isRemind = false;
                    if (user != null)
                    {
                        isRemind = auctionRemindModelMapper.isSpecialRemindByUserId(id, user.getUser_id()) > 0;
                    }
                    tempMap.put("isRemind", isRemind);
                }
                else if (status == 2)
                {
                    //竞拍中获取专场下面的场次+商品,最多只展示两个场次
                    tempMap.putAll(this.getSessionGoods(vo, id, null));
                }
                else
                {
                    //交易结束获取专场总成交价
                    String amt = auctionSpecialModelMapper.getSpecialDealAmt(id);
                    if (StringUtils.isEmpty(amt))
                    {
                        amt = "0";
                    }
                    BigDecimal dealAmt = new BigDecimal(amt);
                    tempMap.put("dealAmt", dealAmt);
                    //做排序用
                    tempMap.put("specialDealAmt",dealAmt);
                    //获取专场下所有商品
                    tempMap.putAll(this.getSessionGoodsList(vo, id));
                }
                //围观人数
                tempMap.put("lookNum", MapUtils.getInteger(map, "look_count"));
                //获取专场商品数量
                int goodsNum = auctionSpecialModelMapper.sumSpecialGoodsNum(id);
                tempMap.put("goodsNum",goodsNum);
                //做排序用
                tempMap.put("goodsTotal", goodsNum);
                tempMap.put("name", MapUtils.getString(map, "name"));
                tempMap.put("startDate", DateUtil.dateFormate(MapUtils.getString(map, "start_date"), GloabConst.TimePattern.YMDHMS));
                tempMap.put("endDate", DateUtil.dateFormate(MapUtils.getString(map, "end_date"), GloabConst.TimePattern.YMDHMS));
                tempMap.put("status", MapUtils.getIntValue(map, "status"));
                tempMap.put("promiseAmt", MapUtils.getString(map, "promise_amt"));
                tempMap.put("mchId", MapUtils.getString(map, "mch_id"));
                tempMap.put("type", MapUtils.getString(map, "type"));
                tempMap.put("mch_name", MapUtils.getString(map, "mch_name"));
                tempMap.put("content", MapUtils.getString(map, "content"));

                specialList.add(tempMap);
            }

            //拍品数和成交金额排序
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(sort) && (Objects.equals(sort_criteria, "num") || sort_criteria.equals("total_amount")))
            {
                specialList = sortByGoodsTotal(specialList,sort,sort_criteria);
            }
        }
        resultMap.put("list", specialList);
        resultMap.put("total", total);
        return resultMap;
    }

    @Override
    public Map<String, Object> execute(Integer storeId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        StringBuilder       loggerStr = new StringBuilder();
        String              lockToken = "";
        String              lockKey   = GloabConst.RedisHeaderKey.PLUGIN_AUCTION_TASK_LOCK;
        out:
        try
        {
            if (storeId == null)
            {
                logger.debug(loggerStr.append("商城id为空,不执行竞拍任务!").toString());
                break out;
            }

            //锁
            lockToken = redisLockTool.lock(lockKey, 30000, 500000);
            if (StringUtils.isEmpty(lockToken))
            {
                logger.debug(loggerStr.append("异步任务正在执行中,等待下次执行!").toString());
                break out;
            }
            //获取竞拍设置
            AuctionConfigModel configModel = new AuctionConfigModel();
            configModel.setStore_id(storeId);
            configModel = auctionConfigModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                logger.debug(loggerStr.append("未获得商城竞拍设置,等待下次执行!").toString());
                break out;
            }
            int count;
            loggerStr.append(String.format("=========== 商城id:%s 正在执行竞拍异步任务 ===========\n", storeId));
            //执行专场过期
            count = auctionMapper.invalidSpecial(storeId, new Date(), AuctionSpecialModel.AuctionStatus.STATUS_END_STARTED);
            loggerStr.append(String.format(" 执行专场过期 数量：%s \n", count));
            //专场进行中
            count = auctionMapper.startedSpecial(storeId, new Date(), AuctionSpecialModel.AuctionStatus.STATUS_STARTED);
            loggerStr.append(String.format(" 专场进行中 数量：%s \n", count));
            //专场未开始
            count = auctionMapper.notStartedSpecial(storeId, new Date(), AuctionSpecialModel.AuctionStatus.STATUS_NOT_STARTED);
            loggerStr.append(String.format(" 专场未开始 数量：%s \n", count));

            //执行场次过期
            count = auctionMapper.invalidSession(storeId, new Date(), AuctionSessionModel.AuctionStatus.STATUS_END_STARTED);
            loggerStr.append(String.format(" 场次过期 数量：%s \n", count));
            //场次进行中
            count = auctionMapper.startedSession(storeId, new Date(), AuctionSessionModel.AuctionStatus.STATUS_STARTED);
            loggerStr.append(String.format(" 场次进行中 数量：%s \n", count));
            //同步竞拍商品状态为进行中
            count = auctionMapper.startedGoodsBySession(storeId, new Date(),
                    AuctionSessionModel.AuctionStatus.STATUS_STARTED,
                    AuctionProductModel.GoodsStatus.STATUS_SOLD_WAIT,
                    AuctionProductModel.GoodsStatus.STATUS_SOLD_IN_PROGRESS);
            loggerStr.append(String.format(" 竞拍商品状态更新(待拍卖->拍卖中) 数量：%s \n", count));
            //场次未开始
            count = auctionMapper.notStartedSession(storeId, new Date(), AuctionSessionModel.AuctionStatus.STATUS_NOT_STARTED);
            loggerStr.append(String.format(" 场次未开始 数量：%s \n", count));

            //已经提醒过的专场
            Set<String> remindSpecialList = new HashSet<>();
            //已经结算的专场
            Set<String> settlementSpecialList = new HashSet<>();

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("recovery", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap.put("is_settlement", DictionaryConst.WhetherMaven.WHETHER_NO);
            //获取商城所有没有结算的专场商品
            List<Map<String, Object>> auctionGoodsList = auctionProductModelMapper.pluginSelectAuctionGoodsList(parmaMap);
            for (Map<String, Object> map : auctionGoodsList)
            {
                //专场id
                String specialId = MapUtils.getString(map, "specialId");
                //场次id
                String sessionId = MapUtils.getString(map, "sessionId");
                //专场状态
                Integer specialStatus = MapUtils.getInteger(map, "specialStatus");
                //场次状态
                Integer sessionStatus = MapUtils.getInteger(map, "sessionStatus");
                //竞拍商品id
                Integer acId = MapUtils.getInteger(map, "acId");
                //竞拍商品状态
                Integer goodsStatus = MapUtils.getInteger(map, "goodsStatus");

                //专场标记结算需要 最后结束的场次结束时间(有竞拍得主)+后台设置的订单过期时间 过了之后再能结算；所有场次都没有得主则直接结算
                if (AuctionSpecialModel.AuctionStatus.STATUS_END_STARTED.equals(specialStatus))
                {
                    settlementSpecialList.add(specialId);
                }

                loggerStr.append(String.format("------ 场次id:%s  正在处理 ------\n", sessionId));
                //已结束场次
                if (AuctionSessionModel.AuctionStatus.STATUS_END_STARTED.equals(sessionStatus))
                {
                    loggerStr.append(String.format("竞拍商品id:%s 已结束,正在结算... \n", acId));
                    AuctionProductModel auctionProductUpdate = new AuctionProductModel();
                    auctionProductUpdate.setId(acId);
                    auctionProductUpdate.setStatus(AuctionProductModel.GoodsStatus.STATUS_UNSOLD);
                    //获取最终得主
                    String mainUserId = auctionProductModelMapper.getGoodsMaxUserId(acId);
                    //没有得主直接流拍
                    if (StringUtils.isEmpty(mainUserId))
                    {
                        count = auctionProductModelMapper.updateByPrimaryKeySelective(auctionProductUpdate);
                        loggerStr.append(String.format("竞拍商品id:%s 没有得主,直接流拍... 流拍状态:%s \n", acId, count > 0));
                        continue;
                    }
                    //更新最终得主 这个
                    count = auctionProductModelMapper.setGoodsMaxUserId(acId, mainUserId, new Date());
                    loggerStr.append(String.format("最终得主为:%s 更新状态:%s \n", mainUserId, count > 0));
                    if (count < 1)
                    {
                        continue;
                    }
                    if (AuctionProductModel.GoodsStatus.STATUS_SOLD_IN_PROGRESS.equals(goodsStatus))
                    {
                        loggerStr.append("当前商品状态:拍卖中\n");
                        //如果竞拍人数没达到预计则流拍
                        int num = auctionRecordModelMapper.getRecordNumByUserId(storeId, acId);
                        // 流拍需要看是未支付流拍还是人数没达标流拍,人数没达标则退保证金,否则不退
                        if (configModel.getLow_pepole() > num)
                        {
                            //流拍类型  1未付款流拍  2未达到人数流拍
                            auctionProductUpdate.setPassed_status(AuctionProductModel.passedStatus.STATUS_2);
                            //未得标
                            loggerStr.append("当前商品状态:未得标\n");
                            count = auctionProductModelMapper.updateByPrimaryKeySelective(auctionProductUpdate);
                            loggerStr.append(String.format("系统自动下单失败 原因:%s 流拍状态:%s \n", String.format("竞拍人数需要达到【%s】人,实际只有【%s】人,已流拍!", configModel.getLow_pepole(), num), count > 0));
                            continue;
                        }
                        //判断当前精品专场是否已经结束,如果结束则直接下单
                        loggerStr.append("当前竞拍专场是否已经结束,已结束则下单");
                        AuctionSpecialModel auctionSpecialModel = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
                        loggerStr.append("系统开始自动下单... \n");
                        if (auctionSpecialModel != null)
                        {
                            //专场结束时间
                            Date endDate = auctionSpecialModel.getEnd_date();
                            if (!DateUtil.dateCompare(endDate, new Date()))
                            {
                                //记录可以下单的信息
                                loggerStr.append("系统开始自动下单... \n");
                                OrderVo orderVo = new OrderVo();
                                orderVo.setMainId(acId);
                                orderVo.setStoreId(storeId);
                                orderVo.setUid(userBaseMapper.getUidByuserId(storeId, mainUserId));
                                orderVo.setIsPlugin(true);
                              /*  Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(orderVo));
                                Map<String, Object> paramMap    = new HashMap<>(16);
                                paramMap.put("vo", orderVo);*/
                                try
                                {
                                    loggerStr.append(String.format("系统自动下单请求参数:%s \n", JSON.toJSONString(orderVo)));
                                    //httpApiUtils.executeHttpApi(ApiKey.PLACE_ORDER_API, paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                                    Map<String, Object> result = this.placeOrder(orderVo);
                                    loggerStr.append(String.format("系统自动下单成功 订单号:%s \n", result.get("sNo")));
                                }
                                catch (LaiKeAPIException l)
                                {
                                    loggerStr.append(String.format("系统自动下单失败 原因:%s \n", l.getMessage()));
                                    continue;
                                }
                            }
                        }
                    }
                    else if (AuctionProductModel.GoodsStatus.STATUS_SOLD.equals(goodsStatus))
                    {
                        loggerStr.append("当前商品状态:已拍卖\n");
                        //已拍卖则判断是否超时支付,超时支付也流拍
                        String orderNo = MapUtils.getString(map, "sNo");
                        if (StringUtils.isEmpty(orderNo))
                        {
                            loggerStr.append(String.format("商品id:%s 数据错误,原因:已经竞拍,但是没有订单号?  \n", acId));
                            continue;
                        }
                        //获取订单信息
                        OrderModel orderOld =   new OrderModel();
                        orderOld.setStore_id(storeId);
                        orderOld.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_JP);
                        orderOld.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
                        orderOld.setsNo(orderNo);
                        orderOld = orderModelMapper.selectOne(orderOld);
                        if (orderOld != null)
                        {
                            //计算是否超时支付
                            Date validateDate = DateUtil.getAddDateBySecond(orderOld.getAdd_time(), Integer.parseInt(configModel.getOrder_failure() + ""));
                            loggerStr.append(String.format("订单超时支付 超时时间为：%s \n", DateUtil.dateFormate(validateDate, GloabConst.TimePattern.YMDHMS)));
                            if (!DateUtil.dateCompare(validateDate, new Date()))
                            {
                                auctionProductUpdate.setPassed_status(AuctionProductModel.passedStatus.STATUS_1);
                                auctionProductUpdate.setStatus(AuctionProductModel.GoodsStatus.STATUS_UNSOLD);
                                //超时流拍
                                count = auctionProductModelMapper.updateByPrimaryKeySelective(auctionProductUpdate);
                                orderModelMapper.closeOrder(storeId, orderNo);
                                loggerStr.append(String.format("订单超时支付 原因:%s 流拍状态:%s \n", String.format("订单:%s 超时时间:%s 支付时间已超时,已流拍!", orderNo, DateUtil.dateFormate(validateDate, GloabConst.TimePattern.YMDHMS)), count > 0));
                                continue;
                            }
                        }
                    }
                }
                else if (AuctionSessionModel.AuctionStatus.STATUS_NOT_STARTED.equals(sessionStatus))
                {
                    loggerStr.append("当前商品状态:未开始\n");
                    if (!remindSpecialList.contains(specialId))
                    {
                        int row;
                        loggerStr.append(String.format("商品id:%s 未开始,提前五分钟开始推送提醒已关注专场的人 ... \n", acId));
                        //提前五分钟开始推送提醒已关注专场的人
                        List<String> userIdList = auctionRemindModelMapper.getRemindUserIdBySpecialId(specialId, new Date(), -5);
                        loggerStr.append(String.format("总共需要推送%s人 ... \n", userIdList != null ? userIdList.size() : 0));
                        if (userIdList != null && userIdList.size() > 0)
                        {
                            String              name        = MapUtils.getString(map, "specialName");
                            Map<String, Object> insertParam = new HashMap<>(16);
                            insertParam.put("userList", userIdList);
                            insertParam.put("store_id", storeId);
                            insertParam.put("senderid", "task");
                            insertParam.put("title", "您有关注的消息！");
                            insertParam.put("content", String.format("专场:【%s】 即将开始!", name));
                            insertParam.put("date", new Date());
                            insertParam.put("type", 1);
                            //批量发送系统推送
                            row = systemMessageModelMapper.noticeUserAll(insertParam);

                            loggerStr.append(String.format("竞拍专场【%s】即将开始 一共通知%s 个用户\n", name, row));
                            //标记竞拍已推送
                            auctionRemindModelMapper.specialRemind(specialId);
                        }
                        remindSpecialList.add(specialId);
                    }
                }

                loggerStr.append(String.format("------ 场次id:%s 执行结束 ------\n", sessionId));
            }
            //专场结算
            loggerStr.append(String.format("------ 需结算专场的数量%s ------\n", settlementSpecialList.size()));
            for (String specialId : settlementSpecialList)
            {
                loggerStr.append(String.format("------ 专场id:%s 结算中 ------\n", specialId));
                loggerStr.append(this.specialSettlement(storeId, specialId, configModel));
            }

            //超过n天删除专场
            loggerStr.append(String.format("专场保留%s天\n", configModel.getDays()));
            count = auctionSpecialModelMapper.delSpecialInvalidate(new Date(), configModel.getDays());
            loggerStr.append(String.format("专场删除 删除数量: %s \n", count));
        }
        catch (Exception e)
        {
            logger.error("竞拍任务 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC);
        }
        finally
        {
            if (StringUtils.isNotEmpty(lockToken))
            {
                //释放锁
                logger.debug("【竞拍-异步任务】释放锁{} 执行结果:{}", lockKey, redisLockTool.unlock(lockKey, lockToken));
            }
        }
        resultMap.put("logo", loggerStr.toString());
        return resultMap;
    }




    @Override
    public Map<String, Object> backPromise(PaymentVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int row;
            if (StringUtils.isEmpty(vo.getsNo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "订单不能为空");
            }
            OrderDataModel orderDataOld = new OrderDataModel();
            orderDataOld.setTrade_no(vo.getsNo());
            orderDataOld.setStatus(OrderDataModel.PayStatus.PAYMENT);
            orderDataOld = orderDataModelMapper.selectOne(orderDataOld);
            if (orderDataOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            Map<String, Object> auctionInfo = JSON.parseObject(orderDataOld.getData(), new TypeReference<Map<String, Object>>()
            {
            });
            //参数准备
            String     pay       = MapUtils.getString(auctionInfo, "pay");
            Integer    storeId   = MapUtils.getInteger(auctionInfo, "storeId");
            String     userId    = MapUtils.getString(auctionInfo, "user_id");
            Integer    oid       = orderDataOld.getId();
            String     orderNo   = orderDataOld.getTrade_no();
            BigDecimal refundAmt = new BigDecimal(MapUtils.getString(auctionInfo, "paymentAmt"));
            switch (pay)
            {
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                    //钱包支付全额退款
                    publicMemberService.returnUserMoney(storeId, userId, refundAmt, oid, true);
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                    //发起退款
                    Map<String, String> map = publicAlipayService.refundOrder(storeId, 0, pay, orderNo, refundAmt, refundAmt);
                    String code = map.get("code");
                    if (DictionaryConst.AliApiCode.ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH.equals(code))
                    {
                        logger.error("支付宝退款失败 卖家余额不足 code:{}", code);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                    }
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                    //微信退款
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", pay);
                    logger.info("退款参数realOrderno-{},", orderNo);
                    map = publicWechatService.refundOrder(storeId, oid, pay, orderNo, refundAmt, true, refundAmt);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                //贝宝退款
                case DictionaryConst.OrderPayType.PAYPAL_PAY:
                    logger.error("进入贝宝退竞拍保证金逻辑");
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", pay);
                    logger.info("退款参数realOrderno-{},", orderNo);
                    map = publicPaypalService.refundOrder(storeId, oid, pay, orderNo, refundAmt, true, refundAmt);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                    break;
                case DictionaryConst.OrderPayType.STRIPE_PAY:
                    logger.error("退款参数-{}-,", JSON.toJSONString(vo));
                    logger.info("退款参数orderId-{},", oid);
                    logger.info("退款参数payType-{}-,", pay);
                    map = publicStripeServiceImpl.refundOrder(vo.getStoreId(), orderNo, oid, pay,null, refundAmt, Boolean.FALSE, null, null);
                    logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在", "refund");
            }
//            httpApiUtils.executeHttpApi("pay.refund.refundPay", paramMap, MediaType.MULTIPART_FORM_DATA_VALUE);
            //标记已退款
            row = auctionPromiseModelMapper.refundPromise(MapUtils.getInteger(auctionInfo, "storeId"), MapUtils.getString(auctionInfo, "specialId"), orderDataOld.getTrade_no(), new Date());
            //重新生成一条已退回的记录
            AuctionPromiseModel auctionPromiseModel = new AuctionPromiseModel();
            auctionPromiseModel.setPromise(DataUtils.getBigDecimalVal(auctionInfo, "paymentAmt"));
            auctionPromiseModel.setIs_pay(1);
            auctionPromiseModel.setAdd_time(new Date());
            auctionPromiseModel.setBack_time(new Date());
            auctionPromiseModel.setUser_id(DataUtils.getStringVal(auctionInfo, "user_id"));
            auctionPromiseModel.setTrade_no(orderDataOld.getTrade_no());
            auctionPromiseModel.setStore_id(MapUtils.getInteger(auctionInfo, "storeId"));
            auctionPromiseModel.setSpecial_id(MapUtils.getString(auctionInfo, "specialId"));
            auctionPromiseModel.setIs_back(1);

            auctionPromiseModelMapper.insertSelective(auctionPromiseModel);
            if (row < 1)
            {
                logger.error("竞拍保证金退款失败,标记已退款失败 订单id{}", orderDataOld.getTrade_no());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
            systemMessageSave.setSenderid("admin");
            systemMessageSave.setStore_id(MapUtils.getInteger(auctionInfo, "storeId"));
            systemMessageSave.setRecipientid(MapUtils.getString(auctionInfo, "user_id"));
            systemMessageSave.setTitle("系统消息");
            systemMessageSave.setContent("您的竞拍保证金已退回！");
            systemMessageSave.setTime(new Date());
            systemMessageModelMapper.insertSelective(systemMessageSave);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("竞拍保证金退款 错误 ", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("竞拍保证金退款 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "backPromise");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> placeOrder(OrderVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user;
            if (vo.getUid() != null)
            {
                user = userBaseMapper.selectByPrimaryKey(vo.getUid());
            }
            else
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            //刷新缓存
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            //获取商品信息
            Map<String, Object> goodsDetailInfo = auctionMapper.getGoodsDetailInfo(StringUtils.stringParseInt(vo.getMainId()));
            if (goodsDetailInfo == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }

            if (vo.getMainId() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int     acId  = MapUtils.getInteger(goodsDetailInfo, "id");
            Integer mchId = StringUtils.stringParseInt(goodsDetailInfo.get("mch_id"));
            //商品id
            int goodsId = MapUtils.getInteger(goodsDetailInfo, "goods_id");
            //获取商品规格信息
            int attrId = MapUtils.getInteger(goodsDetailInfo, "attr_id");
            //所需数量
            int needNum = 1;
            //最终得主是否是当前登录人
            String mainUserId = MapUtils.getString(goodsDetailInfo, "user_id");
            if (!user.getUser_id().equals(mainUserId))
            {
                logger.debug("当前竞拍商品id:{},当前最新出价人:{},当前登录人:{}", acId, mainUserId, user.getUser_id());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }

            //获取系统自动生成的订单
            vo.setOrderNo(auctionProductModelMapper.getAuctionOrder(vo.getMainId(), user.getUser_id()));
            //获取用户地址
            UserAddress userAddress = publicAddressService.findAddress(vo.getStoreId(), user.getUser_id(), vo.getAddressId());
            if (StringUtils.isNotEmpty(vo.getOrderNo()) && userAddress == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZSHDZ, "请选择收货地址");
            }
            //获取商品信息
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误", "payment");
            }
            FreightModel freightModel = freightModelMapper.selectByPrimaryKey(productListModel.getFreight());
            //插件订单主动下订单不判断
            if (freightModel != null && userAddress != null && !vo.getIsPlugin())
            {
                //用户地址 省-市-区对应运费模板里的格式
                String address = userAddress.getSheng() + "-" + userAddress.getCity() + "-" + userAddress.getQuyu();
                //不配送区域参数列表
                String    bpsRule = URLDecoder.decode(freightModel.getNo_delivery(), CharEncoding.UTF_8);
                JSONArray objects = JSONArray.parseArray(bpsRule);
                if (objects.contains(address))
                {
                    logger.debug("地址超出配送范围");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZCCPSFW, "地址超出配送范围", "payment");
                }
            }
            BigDecimal freightPrice = this.getFreight(vo.getStoreId(), mchId, needNum, StringUtils.stringParseInt(goodsDetailInfo.get("freight").toString()), userAddress, new BigDecimal(MapUtils.getString(goodsDetailInfo, "weight")));
            //处理订单备注
            String remarks = "";
            if (StringUtils.isNotEmpty(vo.getRemarks()))
            {
                Map<String, String> mchRemarks = new HashMap<>(16);
                mchRemarks.put(mchId + "", vo.getRemarks());
                remarks = SerializePhpUtils.JavaSerializeByPhp(mchRemarks);
            }

            //商品名称
            String goodsName = MapUtils.getString(goodsDetailInfo, "product_title");
            //所需金额(原价)
            BigDecimal goodsPrice = auctionMapper.getGoodsMaxPrice(acId);
            //获取规格信息
            ConfiGureModel attrOld = confiGureModelMapper.selectByPrimaryKey(attrId);
            //规格处理
            String attribute = GoodsDataUtils.getProductSkuValue(attrOld.getAttribute());

            //获取用户信息
            user = userBaseMapper.selectByPrimaryKey(user.getId());
//            if (user.getMoney().compareTo(goodsPrice.multiply(new BigDecimal(needNum))) < 0) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YEBZ, "余额不足");
//            }
            //生成订单号
            String            orderNo         = vo.getOrderNo();
            OrderModel        orderOld        = null;
            OrderDetailsModel orderDetailsOld = null;
            //添加一条购买记录
            if (StringUtils.isEmpty(vo.getOrderNo()))
            {
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setMch_id(mchId + "");
                mchBrowseModel.setStore_id(vo.getStoreId());
                mchBrowseModel.setUser_id(user.getUser_id());
                mchBrowseModel.setEvent("购买了竞拍商品");
                mchBrowseModel.setAdd_time(new Date());
                mchBrowseModelMapper.insertSelective(mchBrowseModel);
                orderNo = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_JP);
            }
            else
            {
                orderOld = new OrderModel();
                orderOld.setStore_id(vo.getStoreId());
                orderOld.setsNo(vo.getOrderNo());
                orderOld = orderModelMapper.selectOne(orderOld);
                if (orderOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
                }
                orderDetailsOld = new OrderDetailsModel();
                orderDetailsOld.setStore_id(vo.getStoreId());
                orderDetailsOld.setR_sNo(vo.getOrderNo());
                orderDetailsOld = orderDetailsModelMapper.selectOne(orderDetailsOld);
                if (orderDetailsOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDSJCW, "订单数据错误");
                }
            }
            //生成订单明细
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setStore_id(vo.getStoreId());
            orderDetailsModel.setUser_id(user.getUser_id());
            //插件商品id
            orderDetailsModel.setP_id(vo.getMainId());
            orderDetailsModel.setP_name(goodsName);
            orderDetailsModel.setP_price(goodsPrice);
            orderDetailsModel.setAfter_discount(goodsPrice);
            orderDetailsModel.setNum(needNum);
            orderDetailsModel.setUnit(attrOld.getUnit());
            orderDetailsModel.setR_sNo(orderNo);
            orderDetailsModel.setR_status(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
            orderDetailsModel.setSize(attribute);
            orderDetailsModel.setSid(attrId + "");
            orderDetailsModel.setMch_id(mchId);
            orderDetailsModel.setFreight(freightPrice);
            orderDetailsModel.setSettlement_type(OrderDetailsModel.SETTLEMENT_TYPE_UNSETTLED);
            orderDetailsModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            orderDetailsModel.setAdd_time(new Date());
            int row;
            if (orderDetailsOld == null)
            {
                row = orderDetailsModelMapper.insertSelective(orderDetailsModel);
            }
            else
            {
                orderDetailsModel.setId(orderDetailsOld.getId());
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsModel);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试");
            }
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(vo.getStoreId());
            orderModel.setUser_id(user.getUser_id());
            orderModel.setNum(needNum);
            orderModel.setZ_price(goodsPrice.multiply(new BigDecimal(needNum)).add(freightPrice));
            orderModel.setOld_total(orderModel.getZ_price());
            orderModel.setsNo(orderNo);
            //系统自动下单用户地址可能是空的
            if (userAddress != null)
            {
                //如果是修改则表示
                orderModel.setName(userAddress.getName());
                orderModel.setMobile(userAddress.getTel());
                orderModel.setSheng(userAddress.getSheng());
                orderModel.setShi(userAddress.getCity());
                orderModel.setXian(userAddress.getQuyu());
                orderModel.setAddress(userAddress.getAddress());
                orderModel.setZ_freight(freightPrice);
                orderModel.setOld_freight(freightPrice);
            }
            orderModel.setSpz_price(goodsPrice);
            orderModel.setStatus(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_UNPAID);
            orderModel.setSource(vo.getStoreType());
            orderModel.setOtype(DictionaryConst.OrdersType.ORDERS_HEADER_JP);
            orderModel.setMch_id(String.format("%s%s%s", SplitUtils.DH, mchId, SplitUtils.DH));
            orderModel.setP_sNo(vo.getMainId() + "");
            orderModel.setRemarks(remarks);
            orderModel.setSubtraction_id(0);
            orderModel.setReal_sno(publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_JP));
            orderModel.setAdd_time(new Date());


            //货币信息,竞拍订单使用商城默认币种
            Map storeCurrencyMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
            //货币code
            String currency_code = MapUtils.getString(storeCurrencyMap, "currency_code");
            //货币符号
            String currency_symbol = MapUtils.getString(storeCurrencyMap, "currency_symbol");
            //汇率
            BigDecimal exchange_rate = BigDecimal.valueOf(MapUtils.getDouble(storeCurrencyMap, "exchange_rate"));

            orderModel.setCurrency_code(currency_code);
            orderModel.setCurrency_symbol(currency_symbol);
            orderModel.setExchange_rate(exchange_rate);

            if (orderOld == null)
            {
                row = orderModelMapper.insertSelective(orderModel);
            }
            else
            {
                orderModel.setsNo(null);
                orderModel.setId(orderOld.getId());
                row = orderModelMapper.updateByPrimaryKeySelective(orderModel);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试");
            }
            //标记竞拍商品已经有竞拍得主了
            AuctionProductModel auctionProductUpdate = new AuctionProductModel();
            auctionProductUpdate.setId(orderDetailsModel.getP_id());
            auctionProductUpdate.setsNo(orderNo);
            auctionProductUpdate.setStatus(AuctionProductModel.GoodsStatus.STATUS_SOLD);
            auctionProductUpdate.setUpdate_date(new Date());
            auctionProductUpdate.setPassed_status(AuctionProductModel.passedStatus.STATUS_1);
            row = auctionProductModelMapper.updateByPrimaryKeySelective(auctionProductUpdate);
            if (row < 1)
            {
                logger.debug("标记竞拍商品已被竞拍失败!");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XDSBQSHZS, "下单失败,请稍后再试");
            }

            resultMap.put("sNo", orderNo);
            resultMap.put("total", orderModel.getZ_price());
            resultMap.put("order_id", orderModel.getId());
            //下单时间
            resultMap.put("orderTime", DateUtil.dateFormate(orderModel.getAdd_time(), GloabConst.TimePattern.YMDHMS));
        }
        catch (LaiKeAPIException e)
        {
            logger.error("竞拍下单 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("竞拍下单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", e.getMessage());
        }
        return resultMap;
    }

    @Override
    public BigDecimal getFreight(int storeId, Integer mchId, Integer num, Integer freightId, UserAddress userAddress, BigDecimal weight) throws LaiKeAPIException
    {
        //运费
        BigDecimal freightPrice = BigDecimal.ZERO;
        try
        {
            FreightModel freightModel = new FreightModel();
            if (freightId != null)
            {
                //根据运费模版id
                freightModel.setId(freightId);
            }
            else
            {
                //如果没有默认模板则获取默认模板
                freightModel.setStore_id(storeId);
                freightModel.setMch_id(mchId);
                freightModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            }
            //运费信息
            freightModel = freightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //获取运费
                BigDecimal goodsYunFei = publicOrderService.getFreight(freightModel.getId(), userAddress, num, weight);
                //计算总运费
                freightPrice = freightPrice.add(goodsYunFei);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("计算竞拍运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPXXBWZ, "商品信息不完整", "getFreight");
        }
        return freightPrice;
    }

    /**
     * 专场结算
     * 退还保证金的条件 :
     * 1.只有专场结束了才会退保证金;
     * 2.没有竞拍到的退还;
     * 3.竞拍得到了且支付了的退还;
     * 4.如果参与了多个场次,只要有流拍的情况就扣除保证金
     *
     * @param storeId       - 商城id
     * @param specialId     - 专场id
     * @param auctionConfig - 专场配置
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/8/18 9:45
     */
    private String specialSettlement(int storeId, String specialId, AuctionConfigModel auctionConfig) throws LaiKeAPIException
    {
        StringBuilder loggerStr = new StringBuilder();
        out:
        try
        {
            //专场标记结算需要 最后结束的场次结束时间(有竞拍得主)+后台设置的订单过期时间 过了之后再能结算；所有场次都没有得主则直接结算
            int row;
            //获取当前专场有竞拍得主且最后结束的下单时间 如果是空的则表示都可以退还保证金
            String endDateTemp = auctionMapper.getGoodsOrderTime(specialId);
            if (StringUtils.isNotEmpty(endDateTemp))
            {
                Date endDate         = DateUtil.dateFormateToDate(endDateTemp, GloabConst.TimePattern.YMDHMS);
                Date validateEndDate = DateUtil.getAddDateBySecond(endDate, Integer.parseInt(auctionConfig.getOrder_failure() + ""));
                if (DateUtil.dateCompare(validateEndDate, new Date()))
                {
                    loggerStr.append(String.format("竞拍专场【%s】有订单未支付不能结算 结算时间为:%s \n", specialId, DateUtil.dateFormate(validateEndDate, GloabConst.TimePattern.YMDHMS)));
                    break out;
                }
            }
            //获取自营店id
            Integer  zyMchId = customerModelMapper.getStoreMchId(storeId);
            MchModel mchOld  = mchModelMapper.selectByPrimaryKey(zyMchId);
            //获取用户支付的专场保证金信息
            List<AuctionPromiseModel> auctionPromiseOlds = auctionPromiseModelMapper.selectAuctionPromiseUserIdList(storeId, specialId);
            if (auctionPromiseOlds != null)
            {
                for (AuctionPromiseModel auctionPromise : auctionPromiseOlds)
                {
                    String refundPromiseUserId = null;
                    loggerStr.append(String.format("userid:%s,开始退还/扣除专场:%s 保证金,订单:%s \n", auctionPromise.getUser_id(), auctionPromise.getSpecial_id(), auctionPromise.getTrade_no()));
                    //是否需要扣除保证金 专场保证金扣除：只有超时未支付的情况下才扣除保证金
                    Map<String, Object> promiseMap = new HashMap<>(16);
                    //是否有未支付的订单,如果有则扣除保证金,否则退还
                    if (auctionPromiseModelMapper.isRefundPromise(storeId, specialId, auctionPromise.getUser_id()) > 0)
                    {
                        promiseMap = auctionPromiseModelMapper.isDeductionPromise(specialId);
                        if (promiseMap != null)
                        {
                            refundPromiseUserId = MapUtils.getString(promiseMap, "user_id");
                        }
                    }
                    else
                    {
                        loggerStr.append("不扣除保证金,开始退还 \n");
                    }
                    if (StringUtils.isNotEmpty(refundPromiseUserId))
                    {
                        //扣除保证金
                        //row = auctionPromiseModelMapper.deductionPromise(auctionPromise.getId());
                        row = auctionPromiseModelMapper.deductionPromise(auctionPromise.getId(), new Date());
                        loggerStr.append(String.format("订单:%s 扣除保证金状态%s \n", auctionPromise.getTrade_no(), row > 0));
                        //重新生成一条已扣除的记录
                        AuctionPromiseModel auctionPromiseModel = new AuctionPromiseModel();
                        auctionPromiseModel.setPromise(auctionPromise.getPromise());
                        auctionPromiseModel.setIs_pay(auctionPromise.getIs_pay());
                        auctionPromiseModel.setAdd_time(new Date());
                        auctionPromiseModel.setBack_time(new Date());
                        auctionPromiseModel.setUser_id(auctionPromise.getUser_id());
                        auctionPromiseModel.setTrade_no(auctionPromise.getTrade_no());
                        auctionPromiseModel.setStore_id(auctionPromise.getStore_id());
                        auctionPromiseModel.setSpecial_id(auctionPromise.getSpecial_id());
                        auctionPromiseModel.setIs_deduction(1);

                        auctionPromiseModelMapper.insertSelective(auctionPromiseModel);

                        AuctionSpecialModel auctionSpecialOld = auctionSpecialModelMapper.selectByPrimaryKey(specialId);
                        //增加对应店铺收入
                        MchAccountLogModel mchAccountLogSave = new MchAccountLogModel();
                        mchAccountLogSave.setStore_id(storeId);
                        mchAccountLogSave.setRemake(auctionPromise.getTrade_no());
                        mchAccountLogSave.setPrice(auctionPromise.getPromise());
                        mchAccountLogSave.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_PROMISE);
                        mchAccountLogSave.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);
                        mchAccountLogSave.setAddtime(new Date());
                        if (AuctionSpecialModel.SpecialType.TYPE_MCH == auctionSpecialOld.getType())
                        {
                            //增加店铺收入
                            mchAccountLogSave.setMch_id(auctionSpecialOld.getMch_id().toString());
                            MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchAccountLogSave.getMch_id());
                            mchAccountLogSave.setAccount_money(mchModel.getAccount_money());
                            logger.info("店铺收入：：：：{}",mchModel.getAccount_money());
                        }
                        else
                        {
                            //增加自营店收入
                            mchAccountLogSave.setMch_id(Integer.toString(zyMchId));
                            mchAccountLogSave.setAccount_money(mchOld.getAccount_money());
                            logger.info("自营店收入：：：：：{}",mchOld.getAccount_money());
                        }
                        mchAccountLogModelMapper.insertSelective(mchAccountLogSave);
                        row = mchModelMapper.settlementMchCash(storeId, Integer.parseInt(mchAccountLogSave.getMch_id()), mchAccountLogSave.getPrice());
                        loggerStr.append(String.format("店铺id:%s 扣除保证金,增加店铺收入到提现 状态 %s！ \n", mchAccountLogSave.getMch_id(), row > 0));
                    }
                    else
                    {
                        Map<String, Object> paramMap = new HashMap<>(16);
                        paramMap.put("sNo", auctionPromise.getTrade_no());
                        try
                        {
                            PaymentVo paymentVo = new PaymentVo();
                            paymentVo.setsNo(auctionPromise.getTrade_no());
                            this.backPromise(paymentVo);
                           /* httpApiUtils.executeHttpApi("plugin.auction.backPromise", paramMap, MediaType.MULTIPART_FORM_DATA_VALUE);*/
                            loggerStr.append(String.format("保证金退款【%s】成功 \n", auctionPromise.getPromise()));
                        }
                        catch (LaiKeAPIException l)
                        {
//                            l.printStackTrace();
                            loggerStr.append(String.format("竞拍保证金退还失败 参数%s,原因:%s 【该专场不结算,等待下次任务执行退保证金】\n", JSON.toJSONString(paramMap), l.getMessage()));
                            break out;
                        }
                    }

                }
            }

            //结算佣金
            loggerStr.append(String.format("专场id:%s 开始结算佣金 \n", specialId));
            List<Map<String, Object>> settlementList = auctionMapper.getGoodsOrderPrice(specialId);
            for (Map<String, Object> map : settlementList)
            {
                String orderNo = map.get("sNo").toString();
                loggerStr.append(String.format("订单:%s 开始结算 \n", orderNo));
                //订单id
                int orderId = Integer.parseInt(map.get("orderId").toString());
                //明细id
                int detailId = Integer.parseInt(map.get("detailId").toString());
                //专场类型
                int specialType = MapUtils.getIntValue(map, "type");
                //运费
                BigDecimal freightPrice = new BigDecimal(MapUtils.getString(map, "freight"));
                //订单金额
                BigDecimal orderPrice = new BigDecimal(MapUtils.getString(map, "zPrice"));
                //订单实际支付金额 不含运费
                BigDecimal payPrice = new BigDecimal(MapUtils.getString(map, "price"));
                //当前结算金额
                BigDecimal settlementPrice = new BigDecimal(MapUtils.getString(map, "price"));
                settlementPrice = settlementPrice.add(freightPrice);
                //佣金比例
                BigDecimal commissionPrice = new BigDecimal(MapUtils.getString(map, "commission", "0"));
                int        mchId           = Integer.parseInt(StringUtils.trim(MapUtils.getString(map, "mchId"), SplitUtils.DH));
                //订单结算
                loggerStr.append(String.format("正在结算订单: %s \n 订单金额:%s,优惠后的金额:%s,运费:%s,最后结算金额%s 佣金比例:%s \n", orderNo, orderPrice, settlementPrice, freightPrice, settlementPrice, commissionPrice));
                //只有报名专场才需要结算佣金
                outSettlementMch:
                if (BigDecimal.ZERO.compareTo(commissionPrice) < 0 && specialType == AuctionSpecialModel.SpecialType.TYPE_SIGN)
                {
                    //自营店佣金结算
                    if (BigDecimal.ZERO.compareTo(settlementPrice) < 0)
                    {
                        if (zyMchId == null || mchOld == null)
                        {
                            loggerStr.append(String.format("商城id:%s 自营店不存在！ \n", storeId));
                            break outSettlementMch;
                        }
                        settlementPrice = settlementPrice.multiply(commissionPrice.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        if (settlementPrice.compareTo(new BigDecimal("0.01")) < 0)
                        {
                            //如果佣金小于0.01则不结算
                            loggerStr.append("佣金小于0.01 不予结算 \n");
                            settlementPrice = new BigDecimal("0.01");
                            break outSettlementMch;
                        }
                        loggerStr.append(String.format("结算佣金给自营店id:%s 金额:%s \n", zyMchId, settlementPrice));
                        row = mchModelMapper.settlementCashableMch(storeId, zyMchId, settlementPrice);
                        if (row < 1)
                        {
                            loggerStr.append(String.format("店铺id:%s 佣金结算失败 结算金额%s！ \n", zyMchId, settlementPrice));
                            break outSettlementMch;
                        }
                        //添加一条收入
                        MchAccountLogModel mchAccountLogSave = new MchAccountLogModel();
                        mchAccountLogSave.setStore_id(storeId);
                        mchAccountLogSave.setRemake(orderNo);
                        mchAccountLogSave.setMch_id(Integer.toString(zyMchId));
                        mchAccountLogSave.setPrice(settlementPrice);
                        mchAccountLogSave.setAccount_money(mchOld.getAccount_money());
                        mchAccountLogSave.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER);
                        mchAccountLogSave.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);
                        mchAccountLogSave.setAddtime(new Date());
                        mchAccountLogModelMapper.insertSelective(mchAccountLogSave);
                    }
                    payPrice = orderPrice.subtract(settlementPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
                }else
                {
                    payPrice = payPrice.add(freightPrice);
                }
                loggerStr.append(String.format("==== 店铺%s 开始结算订单%s 结算金额%s ==== \n", mchId, orderNo, payPrice));
                mchModelMapper.settlementMchCash(storeId, mchId, payPrice);
                MchModel mch = mchModelMapper.selectByPrimaryKey(mchId);
                if (mch != null)
                {
                    //添加一条收入
                    MchAccountLogModel mchAccountLogSave = new MchAccountLogModel();
                    mchAccountLogSave.setStore_id(storeId);
                    mchAccountLogSave.setRemake(orderNo);
                    mchAccountLogSave.setMch_id(Integer.toString(mchId));
                    mchAccountLogSave.setPrice(payPrice);//不包含运费
                    mchAccountLogSave.setAccount_money(mch.getAccount_money());
                    mchAccountLogSave.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER);
                    mchAccountLogSave.setStatus(DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);
                    mchAccountLogSave.setAddtime(new Date());
                    mchAccountLogModelMapper.insertSelective(mchAccountLogSave);
                }
                OrderModel orderUpdate = new OrderModel();
                orderUpdate.setId(orderId);
                //结算时间
                orderUpdate.setArrive_time(new Date());
                orderUpdate.setSettlement_status(DictionaryConst.WhetherMaven.WHETHER_OK);
                row = orderModelMapper.updateByPrimaryKeySelective(orderUpdate);
                OrderDetailsModel orderDetailsUpdate = new OrderDetailsModel();
                orderDetailsUpdate.setId(detailId);
                orderDetailsUpdate.setSettlement_type(DictionaryConst.WhetherMaven.WHETHER_OK);
                row = orderDetailsModelMapper.updateByPrimaryKeySelective(orderDetailsUpdate);
            }

            AuctionSpecialModel auctionSpecialUpdate = new AuctionSpecialModel();
            auctionSpecialUpdate.setId(specialId);
            auctionSpecialUpdate.setIs_settlement(DictionaryConst.WhetherMaven.WHETHER_OK);
            row = auctionSpecialModelMapper.updateByPrimaryKeySelective(auctionSpecialUpdate);
            loggerStr.append(String.format("------ 专场id:%s 标记结算 结算状态:%s ------\n", specialId, row > 0));
        }
        catch (Exception e)
        {
            logger.error("竞拍任务 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC);
        }
        return loggerStr.toString();
    }

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private AuctionMapper auctionMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService publicAlipayService;
    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicWechatService;

    @Autowired
    @Qualifier("publicStripeServiceImpl")
    private PublicPaymentService publicStripeServiceImpl;

    @Autowired
    private PublicPaypalServiceImpl publicPaypalService;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private  FreightModelMapper freightModelMapper;

    @Autowired
    private PublicAddressService publicAddressService;
}
