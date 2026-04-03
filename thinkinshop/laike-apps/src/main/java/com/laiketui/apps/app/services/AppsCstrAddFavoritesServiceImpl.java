package com.laiketui.apps.app.services;

import com.laiketui.apps.api.app.services.AppsCstrAddFavoritesService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.Page;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.UserCollectionModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 收藏实现
 *
 * @author Trick
 * @date 2020/10/22 15:29
 */
@Service
public class AppsCstrAddFavoritesServiceImpl implements AppsCstrAddFavoritesService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrAddFavoritesServiceImpl.class);

    @Autowired
    UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    MchModelMapper mchModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;


    @Override
    public Map<String, Object> index(MainVo vo, int proId, int type) throws LaiKeAPIException
    {
        Map<String, Object> ret = new HashMap<>();
        try
        {
            User user;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user != null)
                {
                    //用户是否已收藏
                    UserCollectionModel userCollectionModel = new UserCollectionModel();
                    userCollectionModel.setStore_id(vo.getStoreId());
                    userCollectionModel.setUser_id(user.getUser_id());
                    userCollectionModel.setP_id(proId);
                    UserCollectionModel userCollection = userCollectionModelMapper.selectOne(userCollectionModel);
                    if (userCollection == null)
                    {
                        userCollectionModel.setAdd_time(new Date());
                        userCollectionModel.setType(type);
                        //收藏表添加一条记录
                        int id = userCollectionModelMapper.insertSelective(userCollectionModel);
                        if (id < 1)
                        {
                            logger.debug("收藏失败 userid = " + user.getUser_id() + "商品 id = " + proId);
                        }
                        else
                        {
                            ret.put("collection_id", userCollectionModel.getId());
                            return ret;
                        }
                    }
                    else
                    {
                        logger.debug("收藏失败 已收藏 userid = " + user.getUser_id() + "商品 id = " + proId);
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录", "index");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "index");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("收藏失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return ret;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeFavorites(MainVo vo, String collections, int type) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            String[] collectionList = StringUtils.trim(collections, ",").split(",");
            for (String collectionId : collectionList)
            {
                UserCollectionModel userCollectionModel = new UserCollectionModel();
                userCollectionModel.setId(Integer.parseInt(collectionId));
                userCollectionModel.setStore_id(vo.getStoreId());
                if (DictionaryConst.UserCollectionType.COLLECTIONTYPE2.equals(type))
                {
                    userCollectionModel.setType(type);
                }
                UserCollectionModel userCollection = userCollectionModelMapper.selectOne(userCollectionModel);
                if (userCollection != null)
                {
                    Integer mchId = userCollectionModel.getMch_id();
                    if (mchId != null && mchId != 0)
                    {
                        //店铺收藏数量-1
                        MchModel mchModel = new MchModel();
                        mchModel.setId(mchId);
                        mchModel.setStore_id(vo.getStoreId());
                        int count = mchModelMapper.cancelCollection(mchModel);
                        if (count < 1)
                        {
                            logger.debug("店铺收藏次数扣减失败 id = " + collectionId + " 店铺 id = " + mchId);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                    }
                    //删除收藏记录
                    int count = userCollectionModelMapper.deleteByPrimaryKey(userCollection.getId());
                    if (count < 1)
                    {
                        logger.debug("收藏删除失败 id = " + collectionId);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
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
            logger.error("收藏失败 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "removeFavorites");
        }
    }

    @Override
    public Map<String, Object> collection(MainVo vo, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //获取店铺插件是否开启
            boolean mchPlugin = publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.MCH, new HashMap<>(16));
            if (user != null)
            {

                UserCollectionModel userCollectionModel = new UserCollectionModel();
                userCollectionModel.setStore_id(vo.getStoreId());
                userCollectionModel.setUser_id(user.getUser_id());
                List<Map<String, Object>> userCollectionList;
                if (type == CollectionType.COMMODITY)
                {
                    boolean isGrade = true;
                    //获取会员折扣
                    BigDecimal gradeRate = new BigDecimal(publicMemberService.getMemberGradeRate(null, user.getUser_id(), vo.getStoreId()) + "");
                    if (BigDecimal.ONE.compareTo(gradeRate) == 0)
                    {
                        isGrade = false;
                        gradeRate = userGradeModelMapper.getGradeLow(vo.getStoreId()).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                    }
                    userCollectionList = userCollectionModelMapper.getUserGoodsCollection(userCollectionModel);
                    for (Map<String, Object> map : userCollectionList)
                    {
                        //商品id
                        int goodsId = (int) map.get("p_id");
                        //店铺id
                        int mchId = (int) map.get("mch_id");
                        //店铺名称
                        String mchName = "";
                        //商品价格
                        BigDecimal price = new BigDecimal("999999");
                        //商品原价格
                        BigDecimal yPrice = new BigDecimal("999999");
                        //折扣后的价格
                        BigDecimal zkPrice = new BigDecimal("0");

                        //查询是否有店铺
                        MchModel mchModel = new MchModel();
                        mchModel.setId(mchId);
                        mchModel = mchModelMapper.selectOne(mchModel);
                        if (mchModel != null)
                        {
                            mchName = mchModel.getName();
                        }
                        //查询商品详细信息
                        ConfiGureModel   confiGureModel   = confiGureModelMapper.getProductMinPriceAndMaxYprice(goodsId);
                        ProductListModel productListModel = new ProductListModel();
                        productListModel.setId(goodsId);
                        productListModel = productListModelMapper.selectByPrimaryKey(productListModel);
                        String imgUrl = "";
                        if (confiGureModel != null)
                        {
                            price = confiGureModel.getPrice();
                            yPrice = confiGureModel.getYprice();
                            //折扣价
                            zkPrice = price.multiply(gradeRate);
                            imgUrl = publiceService.getImgPath(productListModel.getImgurl(), vo.getStoreId());
                        }
                        //是否为预售
                        boolean           isPreGood         = false;
                        PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                        preSellGoodsModel.setProduct_id(goodsId);
                        preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                        if (!Objects.isNull(preSellGoodsModel))
                        {
                            isPreGood = true;
                        }
                        //获取商品标签
                        String sType = MapUtils.getString(map, "s_type");
                        map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));

                        map.put("mch_name", mchName);
                        map.put("price", price);
                        map.put("vip_yprice", price);
                        map.put("vip_price", zkPrice);
                        map.put("yprice", yPrice);
                        map.put("imgurl", imgUrl);
                        map.put("isPreGood", isPreGood);
                        resultMap.put("isGrade", isGrade);
                    }
                }
                else
                {
                    userCollectionList = userCollectionModelMapper.getMchGoodsCollection(userCollectionModel);
                    for (Map<String, Object> map : userCollectionList)
                    {
                        //店铺id
                        Integer shopId = MapUtils.getInteger(map, "shopid");
                        //获取店铺商品信息
                        Map<String, Object> mchGoodsInfo  = publiceService.commodityInformation(vo.getStoreId(), shopId,null);
                        Integer             collectionNum = Integer.parseInt(mchGoodsInfo.get("collection_num").toString());
                        map.put("collection_num", collectionNum);
                        String imgUrl = map.get("img").toString();
                        imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                        map.put("img", imgUrl);

                        map.putAll(publiceService.commodityInformation(vo.getStoreId(), shopId,null));
                    }
                }
                resultMap.put("grade", user.getGrade());
                resultMap.put("mch_status", mchPlugin);
                resultMap.put("list", userCollectionList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看收藏异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collection");
        }

        return resultMap;
    }


    @Override
    public Map<String, Object> similar(MainVo vo, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //是否享受折扣
        boolean isGrade = false;
        //相似商品信息
        List<Map<String, Object>> productList = new ArrayList<>();
        //相似商品id集,剔除相同商品
        List<Integer> goodsIdList = new ArrayList<>();
        goodsIdList.add(goodsId);
        //相似商品默认查询数量 每个关键字获取10条
        Page page = Page.newBuilder(GloabConst.PageEnum.TERMINAL_DEFAULT_PAGE, GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE, null);
        try
        {
            BigDecimal greadeRate = BigDecimal.ZERO;

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //获取会员折扣
                greadeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(null, user.getUser_id(), vo.getStoreId()));
            }
            //享受折扣
            if (greadeRate.compareTo(BigDecimal.ZERO) > 0 && new BigDecimal("1").compareTo(greadeRate) != 0)
            {
                isGrade = true;
            }
            else
            {
                //获取最低折扣
                greadeRate = userGradeModelMapper.getGradeLow(vo.getStoreId()).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            //获取商品信息
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", goodsId);
            parmaMap.put("volume_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> productLists = productListModelMapper.getProductListDynamic(parmaMap);
            for (Map<String, Object> productListMap : productLists)
            {
                //店铺id
                int mchId = (int) productListMap.get("mch_id");
                //店铺名称
                String mchName = "";
                //商品关键字
                String   keyWord  = productListMap.get("keyword") + "";
                String[] keyWords = keyWord.split(SplitUtils.DH);
                //商品图片
                String imgUrl = publiceService.getImgPath(productListMap.get("imgurl").toString(), vo.getStoreId());
                //查询店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setId(mchId);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    mchName = mchModel.getName();
                }
                productListMap.put("imgurl", imgUrl);
                productListMap.put("mch_name", mchName);


                //根据关键字获取
                parmaMap.clear();
                List<Integer> goodsStatus = new ArrayList<>();
                goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
//                goodsStatus.add(DictionaryConst.ProductStatus.UNSOLD_STATUS);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("GoodsStatus", goodsStatus);
//                parmaMap.put("pageNo", page.getPageNo());
//                parmaMap.put("pageSize", page.getPageSize());
//                parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("addDate_sort", DataUtils.Sort.DESC.toString());
                for (String key : keyWords)
                {
                    parmaMap.put("keyword", key);
                    List<Map<String, Object>> goodsList = productListModelMapper.getProductListDynamic(parmaMap);
                    for (Map<String, Object> map : goodsList)
                    {
                        int gId = Integer.parseInt(map.get("id") + "");
                        //店铺id
                        int mid = Integer.parseInt(map.get("mch_id") + "");
                        //店铺名称
                        String mname = "";
                        String img   = map.get("imgurl").toString();
                        img = publiceService.getImgPath(img, vo.getStoreId());

                        //剔除重复商品
                        if (goodsIdList.contains(gId))
                        {
                            continue;
                        }
                        //获取店铺名称
                        MchModel mch = new MchModel();
                        mch.setId(mid);
                        mch = mchModelMapper.selectOne(mch);
                        if (mch != null)
                        {
                            mname = mch.getName();
                        }
                        //商品价格处理
                        BigDecimal vipPrice = new BigDecimal(map.get("price").toString());
                        vipPrice = vipPrice.multiply(greadeRate);

                        map.put("imgurl", img);
                        map.put("mch_name", mname);
                        map.put("vip_yprice", new BigDecimal(map.get("yprice").toString()));
                        map.put("vip_price", vipPrice);
                        goodsIdList.add(gId);
                        productList.add(map);
                    }
                }
                resultMap.put("isGrade", isGrade);
                resultMap.put("product", productLists);
                resultMap.put("list", productList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("找相似 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "similar");
        }
        return resultMap;
    }


    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;
}

