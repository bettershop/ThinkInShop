package com.laiketui.apps.app.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.laiketui.apps.api.app.AppsCstrIndexService;
import com.laiketui.apps.api.products.AppsProductService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.*;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.plugin.PublicAuctionService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.api.plugin.PubliceIntegralService;
import com.laiketui.common.api.plugin.PubliceSubtractionService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.*;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.living.LivingConfigModel;
import com.laiketui.domain.living.LivingRoomModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.UserCollectionModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

/**
 * 首页实现
 *
 * @author Trick
 * @date 2020/10/10 9:22
 */
@Service
public class AppsCstrIndexServiceImpl implements AppsCstrIndexService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrIndexServiceImpl.class);

    private final String NEARBY_KEY = "user_nearby";

    @Autowired
    private Cache<String, Object> caffeineCache;

    /**
     * 首页分类商品 ：30秒更新一次
     */
    private final Integer HOME_PAGE_CLASS_CACHE_TIME = 30;

    @Override
    public boolean hasDiy(Integer storeId) throws LaiKeAPIException
    {
        try
        {
            return publicService.frontPlugin(storeId, DictionaryConst.Plugin.DIY, null);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "hasDiy");
        }
    }

    @Override
    public Map<String, Object> pluginStatus(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //插件状态
            Map<String, Object> pluginMap      = new HashMap<>(16);
            List<String>         pluginCodeList = pluginsModelMapper.getPluginsCodeAll(vo.getStoreId());
            for (String pluginCode : pluginCodeList)
            {
                publicService.frontPlugin(vo.getStoreId(), mchId, pluginCode, pluginMap,false);
            }
            resultMap.put("plugin", pluginMap);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "pluginStatus");
        }
        return resultMap;
    }


    //会员标志
    String MEMBER_SHIP_STATUS_CACHE_KEY = "com.laike.app.get_membership_status_cache_";


    @Override
    public Map<String, Object> getMembershipStatus(int storeId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            boolean status = false;
            String  key    = MEMBER_SHIP_STATUS_CACHE_KEY + storeId;
            caffeineCache.getIfPresent(key);
            Object cacheStatus = caffeineCache.asMap().get(key);
            if (cacheStatus == null)
            {
                BigDecimal userGrate = publicService.getUserGradeRate(storeId, null);
                if (userGrate != null && userGrate.compareTo(new BigDecimal("1")) != 0)
                {
                    status = true;
                    caffeineCache.put(key, status);
                }
            }
            else
            {
                status = (Boolean) cacheStatus;
            }
            resultMap.put("membership_status", status);
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMembershipStatus");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> classList(MainVo vo, Integer shop_id, String sort_criteria, String sort) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //小程序商品分类集
            List<Map<String, Object>> appList = new ArrayList<>();
            User                      user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            //获取会员等级
            BigDecimal gradeRate = BigDecimal.ONE;
//            if (user != null) {
            gradeRate = getMemberGrade(vo.getStoreId(), user);
//            }
            //产品配置 是否显示下架商品
            List<Integer> GoodsStatus = new ArrayList<>();
            GoodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            ProductModel productModel = new ProductModel();
            productModel.setStore_id(vo.getStoreId());
            productModel = productModelMapper.selectOne(productModel);
            if (productModel != null)
            {
                if (ProductConfigModel.DISPLAY_BEAN_GOODS.equals(productModel.getIs_open()))
                {
                    //显示已下架商品
                    GoodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
                }
            }

          /*  if (Objects.nonNull(shop_id)) {
                //获取自营店
                Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                if (Objects.equals(storeMchId, shop_id)) {
                    shop_id = null;
                }
            }*/

            Map<String, Object> sortMap = new HashMap<>(16);
            goodSort(sort_criteria, sort, sortMap);

            //查询商品并分类显示返回JSON至小程序
            List<ProductClassModel> productClassModels = productClassModelMapper.getProduct(vo.getStoreId(), vo.getLang_code());
            if (CollectionUtils.isNotEmpty(productClassModels))
            {
                for (ProductClassModel productClass : productClassModels)
                {
                    Map<String, Object> appMap = new HashMap<>(16);
                    //获取商品
                    Map<String, Object> parmaMap1 = new HashMap<>(16);
                    parmaMap1.put("store_id", vo.getStoreId());
                    parmaMap1.put("GoodsStatus", GoodsStatus);
                    parmaMap1.put("cid", productClass.getCid());
                    parmaMap1.putAll(sortMap);

                    if (productModel != null && productModel.getIs_display_sell_put() == 0)
                    {
                        //不展示已售罄的商品
                        parmaMap1.put("stockNum", "stockNum");
                    }
                    if (Objects.nonNull(shop_id))
                    {
                        parmaMap1.put("mch_id", shop_id);
                    }
                    parmaMap1.put("page", 0);
                    parmaMap1.put("pageSize", 10);
                    parmaMap1.put("lang_code",vo.getLang_code());
                    List<Map<String, Object>> productListModels = productListModelMapper.getProductList(parmaMap1);
                    //商品信息逻辑处理
                    List<Map<String, Object>> resultProductMap = getGoodsInfo(vo.getStoreId(), gradeRate, productListModels,vo.getLanguage());
                    appMap.put("list", resultProductMap);
                    appMap.put("cid", productClass.getCid());
                    appMap.put("english_name", productClass.getEnglish_name());
                    appMap.put("pname", productClass.getPname());
                    appList.add(appMap);
                }
            }
            resultMap.put("list", appList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classList");
        }
        return resultMap;
    }

    @Autowired
    private LivingConfigModelMapper livingConfigModelMapper;
    
    @Autowired
    private AppsProductService appsProductService;

    //换成商城店铺配置信息
    String MCH_CONFIG_CACHE_KEY = "MCH_CONFIG_INFO_";
    //如果是相同的经纬度则从缓存中拿不去调用接口
    String USER_LON_LAT         = "USER_LON_LAT_";


    @Override
    public Map<String, Object> index(MainVo vo, @NotNull String longitude, @NotNull String latitude) throws LaiKeAPIException
    {
        User                user;
        Map<String, Object> resultMap = new HashMap<>(16);
        //系统消息未读数量
        int messageNum = 0;
        //会员折扣
        BigDecimal gradeRate = BigDecimal.ONE;
        //是否享受折扣
        BigDecimal isGradeRate = BigDecimal.ZERO;
        //小程序商品分类集
        List<Map<String, Object>> appList = new ArrayList<>();
        int                       islogin = 0;
        int    storeId  = vo.getStoreId();
        String accessId = vo.getAccessId();
        try
        {
            //获配置
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "请先配置商城");
            }

            //获取位置
            Map<String, Object> currentMap = new HashMap<>(16);
//            try {
//                //获取当前位置
//                String latLongCacheKey = USER_LON_LAT + accessId;
//                String region = (String) caffeineCache.asMap().get(latLongCacheKey);
//                if (region == null) {
//                    region = TengxunMapUtil.getAddress(configModel.getTencent_key(), latitude, longitude);
//                    logger.info("接口获取地区信息：{}", region);
//                    caffeineCache.put(latLongCacheKey, region);
//                }
//
//                if (StringUtils.isNotEmpty(region)) {
//                    logger.info("地区信息：{}", region);
//                    JSONObject resultGpsMap = JSONObject.parseObject(region);
//                    AdminCgModel adminCgModel = new AdminCgModel();
//                    String shen = resultGpsMap.getJSONObject("address_component").get("province") + "";
//                    adminCgModel.setG_ParentID(0);
//                    adminCgModel.setG_Level(2);
//                    adminCgModel.setG_CName(shen);
//                    AdminCgModel cgShen = adminCgModelMapper.selectOne(adminCgModel);
//                    String shi = resultGpsMap.getJSONObject("address_component").get("city") + "";
//
//                    if (cgShen != null) {
//                        logger.info("地区信息 省：{}", cgShen.getG_CName());
//                        adminCgModel.setG_ParentID(cgShen.getGroupID());
//                    }
//                    adminCgModel.setG_Level(3);
//                    adminCgModel.setG_CName(shi);
//                    AdminCgModel cgShi = adminCgModelMapper.selectOne(adminCgModel);
//                    //可能为空AuctionPayServiceImpl
//                    String qu = resultGpsMap.getJSONObject("address_component").get("district") + "";
//                    AdminCgModel cgQu = new AdminCgModel();
//                    if (StringUtils.isNotEmpty(region)) {
//                        if (cgShi != null) {
//                            logger.info("地区信息 市、县、区：{}", cgShen.getG_CName());
//                            adminCgModel.setG_ParentID(cgShi.getGroupID());
//                        }
//                        adminCgModel.setG_Level(4);
//                        adminCgModel.setG_CName(qu);
//                        cgQu = adminCgModelMapper.selectOne(adminCgModel);
//                    }
//
//                    if (cgShen != null) {
//                        currentMap.put("province", cgShen.getG_CName());
//                        currentMap.put("province_GroupID", cgShen.getGroupID());
//                    }
//                    if (cgShi != null) {
//                        currentMap.put("city", cgShi.getG_CName());
//                        currentMap.put("city_GroupID", cgShi.getGroupID());
//                    } else {
//                        if (cgShen != null) {
//                            currentMap.put("city", cgShen.getG_CName());
//                            currentMap.put("city_GroupID", cgShen.getGroupID());
//                        }
//                    }
//
//                    if (cgQu != null) {
//                        currentMap.put("district", cgQu.getG_CName() == null ? "" : cgQu.getG_CName());
//                        currentMap.put("district_GroupID", cgQu.getGroupID() == null ? "" : cgQu.getGroupID());
//                    }
//
//                }
//            } catch (LaiKeAPIException le) {
//                le.printStackTrace();
//            }

            //需登录的流程
            user = RedisDataTool.getRedisUserCache(accessId, redisUtil, false);
            gradeRate = getMemberGrade(storeId, user);
            if (user != null)
            {
                islogin = 1;
                //获取会员等级
//                gradeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(null, user.getUser_id(), storeId));
                //对token进行签名认证
                boolean isTokenInvalid = DataCheckTool.verifyToken(configModel, user);
                if (isTokenInvalid)
                {
                    //未过期 获取该会员未读的系统消息
                    SystemMessageModel systemMessageModel = new SystemMessageModel();
                    systemMessageModel.setStore_id(storeId);
                    systemMessageModel.setRecipientid(user.getUser_id());
                    systemMessageModel.setType(GloabConst.LktConfig.SYSMESSAGE_NOT_READ);
                    messageNum = systemMessageModelMapper.selectCount(systemMessageModel);
                }
            }

            //未登录则获取最低折扣用于显示
            if (gradeRate.compareTo(new BigDecimal("1")) == 0)
            {
                gradeRate = getMemberGrade(storeId, user);
            }
            else
            {
                //享受折扣
                isGradeRate = isGradeRate.add(gradeRate);
            }
            Integer                   userMchId       = null;
            if (user != null)
            {
                userMchId = user.getMchId();
            }
            //获取店铺图片
            String imgLogoUrl = publicService.getImgPath(configModel.getLogo1(), storeId);
            //查询轮播图,根据排序、轮播图id顺序排列
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("type_not", 4);
            parmaMap.put("mch_id", 0);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> resultBannerModels = bannerModelMapper.selectDynamic(parmaMap);
            //获取轮播图完整路径
            for (Map<String, Object> map : resultBannerModels)
            {
                String bannerImgUrl = publicService.getImgPath(MapUtils.getString(map, "image"), storeId);
                map.put("image", bannerImgUrl);
                map.put("type", "switchTab");
            }
            //产品配置 是否显示下架商品
            List<Integer> GoodsStatus = new ArrayList<>();
            GoodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            ProductModel productModel = new ProductModel();
            productModel.setStore_id(storeId);
            productModel = productModelMapper.selectOne(productModel);
            if (productModel != null)
            {
                if (ProductConfigModel.DISPLAY_BEAN_GOODS.equals(productModel.getIs_open()))
                {
                    //显示已下架商品
                    GoodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
                }
            }
            // 首页分类语种统一按前端 language 读取，未传时回退 lang_code，再回退中文
            String indexLangCode = vo.getLanguage();
            if (StringUtils.isEmpty(indexLangCode))
            {
                indexLangCode = vo.getLang_code();
            }
            if (StringUtils.isEmpty(indexLangCode))
            {
                indexLangCode = GloabConst.Lang.CN;
            }
            vo.setLanguage(indexLangCode);
            vo.setLang_code(indexLangCode);
            //查询商品并分类显示返回JSON至小程序
            //首页分类数据缓存
//            String                  proClassesKey      = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_PRO_CLASSDATA_CONDITION_, storeId);
//            Object                  proClassesVales    = redisUtil.get(proClassesKey);
            List<ProductClassModel> productClassModels = null;
//            if (proClassesVales == null)
//            {
            productClassModels = productClassModelMapper.getProduct(storeId, indexLangCode);
//                redisUtil.set(proClassesKey, productClassModels, HOME_PAGE_CLASS_CACHE_TIME);
//            }
//            else
//            {
//                productClassModels = DataUtils.cast(proClassesVales);
//            }
            //首页分类商品数据缓存
            String indexCacheCondition = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_CLASS_CONDITION_, storeId);
            Object indexData           = redisUtil.get(indexCacheCondition);
//            if (indexData == null)
//            {
//                if (productClassModels != null)
//                {
                    for (ProductClassModel productClass : productClassModels)
                    {
                        Map<String, Object> appMap = new HashMap<>(16);
                        //获取商品
                        Map<String, Object> parmaMap1 = new HashMap<>(16);
                        parmaMap1.put("store_id", storeId);
                        parmaMap1.put("GoodsStatus", GoodsStatus);
                        parmaMap1.put("cid", productClass.getCid());
                        parmaMap1.put("sort_sort", DataUtils.Sort.DESC.toString());
                        parmaMap1.put("page", 0);
                        parmaMap1.put("pageSize", 10);

                        parmaMap1.put("lang_code", indexLangCode);

//                        if (vo.getCountry_num() == null || vo.getCountry_num() == 0)
//                        {
//                            vo.setCountry_num(156);
//                        }
//                        parmaMap1.put("country_num", vo.getCountry_num());

                        //只展示首页的产品
                        parmaMap1.put("show_adr", DictionaryConst.GoodsShowAdr.GOODSSHOWADR_INDEX);
                        if (productModel != null && productModel.getIs_display_sell_put() == 0)
                        {
                            //不展示已售罄的商品
                            parmaMap1.put("stockNum", "stockNum");
                        }

                        List<Map<String, Object>> productListModels = productListModelMapper.getProductList(parmaMap1);
                        //商品信息逻辑处理
                        List<Map<String, Object>> resultProductMap = getGoodsInfo(storeId, gradeRate, productListModels, indexLangCode);
                        appMap.put("list", resultProductMap);
                        appMap.put("cid", productClass.getCid());
                        appMap.put("english_name", productClass.getEnglish_name());
                        appMap.put("pname", productClass.getPname());
                        appList.add(appMap);
                    }
                    //缓存 5分钟 更新一次
//                    redisUtil.set(indexCacheCondition, appList, HOME_PAGE_CLASS_CACHE_TIME);
//                }
//            }
//            else
//            {
//                appList = DataUtils.cast(indexData);
//            }
            //获取首页活动信息
            List<Map<String, Object>> MarketingList = getMarketingList(storeId, gradeRate, user);
            //获取首页导航栏
            List<Map<String, Object>> navList = uiNavigationBarModelMapper.getNavigationInfo(storeId);
            for (int i = 0; i < navList.size(); i++)
            {
                Map<String, Object> map    = navList.get(i);
                String              appImg = publicService.getImgPath(map.get("image").toString(), storeId);
                String              url    = String.valueOf(map.get("url"));
                //是否不属于插件
                boolean      isPlugin = true;
                PluginsModel plugins  = new PluginsModel();
                if (url.contains("/pagesA/OrderBidding/StoreBidding") || url.contains("/pagesA/OrderBidding/OrderBidding"))
                {
                    //竞拍
                    plugins.setPlugin_code(DictionaryConst.Plugin.AUCTION);
                }
                else if (url.contains("/pagesA/shop/sign"))
                {
                    //签到
                    plugins.setPlugin_code(DictionaryConst.Plugin.SIGN);
                }
                else if (url.contains("/pagesA/group/group"))
                {
                    //拼团
                    plugins.setPlugin_code(DictionaryConst.Plugin.GOGROUP);
                }
                else if (url.contains("/pagesB/seckill/seckill"))
                {
                    //秒杀
                    plugins.setPlugin_code(DictionaryConst.Plugin.SECONDS);
                }
                else if (url.contains("/pagesC/preSale/goods/goods"))
                {
                    //预售
                    plugins.setPlugin_code(DictionaryConst.Plugin.PRESELL);
                }
                else if (url.contains("/pagesB/integral/integral"))
                {
                    //积分
                    plugins.setPlugin_code(DictionaryConst.Plugin.INTEGRAL);
                }
                else if (url.contains("/pagesA/shop/coupon"))
                {
                    //个人中心入口是否显示 优惠券入口特殊处理 36190
                    CouponConfigModel couponConfigModel = new CouponConfigModel();
                    couponConfigModel.setStore_id(storeId);
                    couponConfigModel.setMch_id(0);
                    couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                    if (couponConfigModel != null)
                    {
                        if (couponConfigModel.getIs_show().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                        {
                            plugins.setPlugin_code(DictionaryConst.Plugin.COUPON);
                        }
                    }
                }
                else if (url.contains("/pagesA/bargain/bargain"))
                {
                    //砍价
                    plugins.setPlugin_code(DictionaryConst.Plugin.BARGAIN);
                }
                else if (url.contains("/pagesB/home/mchList"))
                {
                    //店铺
                    plugins.setPlugin_code(DictionaryConst.Plugin.MCH);
                }
                else if (url.contains("/pagesB/userVip/memberCenter"))
                {
                    //会员
                    plugins.setPlugin_code(DictionaryConst.Plugin.MEMBER);
                }
                else if (url.contains("/pagesA/distribution/fxProduct"))
                {
                    //分销
                    plugins.setPlugin_code(DictionaryConst.Plugin.DISTRIBUTION);
                }
                else if (url.contains("/pagesB/liveStreaming/liveRecommended"))
                {
                    plugins.setPlugin_code(DictionaryConst.Plugin.LIVING);
                }
                else
                {
                    //不是插件
                    isPlugin = false;
                }
                if (isPlugin && !DictionaryConst.Plugin.COUPON.equals(plugins.getPlugin_code()))
                {
                    if (!publicService.frontPlugin(storeId, plugins.getPlugin_code(), new HashMap<>(16)))
                    {
                        //如果插件不存在则不显示该导航栏
                        navList.remove(i);
                        i--;
                    }
                }
                if (!isPlugin && MapUtils.getInteger(map, "isshow").equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                {
                    //如果不是插件则判断是否显示
                    navList.remove(i);
                    i--;
                }
                // 设置插件是否需要登录
                Integer flag     = MapUtils.getInteger(map, "is_login");
                boolean is_login = DictionaryConst.WhetherMaven.WHETHER_OK == flag;
                map.put("is_login", is_login);
                map.put("appimg", appImg);
                map.put("isPlugin", isPlugin);
            }
            //会员插件是否开启
            boolean memberPlugin = false;
            //会员提醒
            boolean             remind = false;
            Map<String, Object> config = memberConfigMapper.getConfig(storeId);
            if (MapUtils.getInteger(config, "id") != null && MapUtils.getInteger(config, "is_open") > 0)
            {
                memberPlugin = true;
                if (user != null && StringUtils.isNotEmpty(user.getUser_id()))
                {
                    if (Integer.parseInt(user.getIs_box()) == DictionaryConst.WhetherMaven.WHETHER_OK && user.getGrade().equals(User.MEMBER))
                    {
                        if (config != null && MapUtils.getInteger(config, "is_open") > 0)
                        {
                            Integer renewOpen = MapUtils.getInteger(config, "renew_open");
                            if (renewOpen.equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                            {
                                if (user.getGrade_end() != null)
                                {
                                    Integer renewDay    = MapUtils.getInteger(config, "renew_day");
                                    long    end         = user.getGrade_end().getTime() / 1000;
                                    long    now         = new Date().getTime() / 1000;
                                    int     betweenDate = DateUtil.getBetweenDate(now, end);
                                    if (betweenDate < renewDay)
                                    {
                                        remind = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //判断直播插件开关状态
            boolean           livingStatus      = false;
            LivingConfigModel livingConfigModel = new LivingConfigModel();
            livingConfigModel.setStore_id(storeId);
            livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);
            if (livingConfigModel == null)
            {
                livingStatus = false;
            }
            else
            {
                if (livingConfigModel.getIs_open() == 0)
                {
                    livingStatus = false;
                }
                else
                {
                    livingStatus = true;
                }
            }
            resultMap.put("livingStatus", livingStatus);
            //TODO 会员插件开关状态(diy组件可以添加就删除此逻辑)
            resultMap.put("memberPlugin", memberPlugin);
            //店铺图片
            resultMap.put("logo", imgLogoUrl);
            //消息数量
            resultMap.put("xxnum", messageNum);
            //地理位置
            resultMap.put("region", currentMap);
            //轮播图
            resultMap.put("banner", resultBannerModels);
            //导航栏图标
            resultMap.put("nav_list", navList);
            //获取签到标识 is_sign_status、sign_status
//            resultMap.putAll(publicService.sign(user));

            //会员等级
            resultMap.put("grade", isGradeRate);
            //会员提醒
            resultMap.put("grade_remind", remind);
            //是否登陆 0未登录
            resultMap.put("login_status", islogin);
            //商城活动商品列表
            resultMap.put("Marketing_list", MarketingList);
            //查询商品并分类显示返回JSON至小程序
            resultMap.put("list2", appList);
            //获取满减活动图片列表
//            resultMap.put("subtraction_list", publiceSubtractionService.getSubtractionImage(storeId));
            //TODO 暂时不用这个满减
            resultMap.put("subtraction_list", new ArrayList<>());
            //首页标题
            resultMap.put("appTitle", configModel.getApp_title());
            // showMchflag = true的时候隐藏店铺跟小程序钱包一同隐藏
            // hideMchFlag = true的时候隐藏店铺跟小程序钱包一同隐藏
            boolean mpwechathidesthflag = configModel.getHide_your_wallet() == 1;
            resultMap.put("hideMchFlag", mpwechathidesthflag);
            resultMap.put("hideWalletFlag", mpwechathidesthflag);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("首页接口出错 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }

        return resultMap;
    }

    /**
     * 获取商城首页活动数据
     *
     * @param storeId   -
     * @param gradeRate - 当前会员折扣
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/22 12:13
     */
    private List<Map<String, Object>> getMarketingList(int storeId, BigDecimal gradeRate, User user) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = null;
        try
        {
            //默认获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            //获取商城活动列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("is_display", 1);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            // 活动商品里会计算会员价，缓存需按折扣维度隔离，避免不同用户折扣串数据
            String gradeRateKey      = gradeRate.stripTrailingZeros().toPlainString();
            String homeMarketListKey = GloabConst.RedisHeaderKey.HOME_MARKET_LIST_KEY + storeId + "_" + gradeRateKey;
            String data              = (String) redisUtil.get(homeMarketListKey);

            if (!StringUtils.isEmpty(data))
            {
                resultList = JSON.parseObject(data, new TypeReference<List<Map<String, Object>>>()
                {
                });
            }

            if (resultList != null && resultList.size() > 0)
            {
                return resultList;
            }

            resultList = new ArrayList<>();
            List<Map<String, Object>> activityModelList = activityModelMapper.selectDynamic(parmaMap);
            if (activityModelList != null)
            {
                //获取
                tag:
                for (Map<String, Object> activityMap : activityModelList)
                {
                    List<Map<String, Object>> goodsList = null;
                    //活动类型
                    int acType = Integer.parseInt(activityMap.get("plug_type").toString());
                    //活动id
                    int id = Integer.parseInt(activityMap.get("id").toString());
                    if (acType == ActivityModel.ACTIVITY_TYPE_SPECIAL)
                    {
                        //获取正在活动的商品
                        List<Integer> goodsStatusList = publicGoodsService.getProductSettings(storeId);
                        goodsList = activityProModelMapper.getActivityGoodsList(storeId, id, goodsStatusList);
                        if (goodsList != null)
                        {
                            for (Map<String, Object> map : goodsList)
                            {
                                int          goodsId   = Integer.parseInt(map.get("id").toString());
                                List<String> sType     = null;
                                String       goodsType = map.get("s_type") + "";
                                if (StringUtils.isNotEmpty(goodsType))
                                {
                                    sType = DataUtils.convertToList(map.get("s_type").toString().split(","));
                                }
                                //会员价
                                BigDecimal vipPrice = new BigDecimal(map.get("price").toString());
                                vipPrice = vipPrice.multiply(gradeRate);
                                //商品图片
                                String imageUrl = publicService.getImgPath(map.get("imgurl").toString(), storeId);
                                map.put("imgurl", imageUrl);
                                //产品主图
                                imageUrl = publicService.getImgPath(map.get("cover_map").toString(), storeId);
                                map.put("cover_map", imageUrl);
                                //获取库存数量
                                int stockNum = confiGureModelMapper.countConfigGureNum(goodsId);
                                map.put("num", stockNum);

                                //新品、热销、推荐标识
                                boolean xp = false, rx = false, tj = false;
                                if (sType != null)
                                {
                                    if (sType.contains(DictionaryConst.LKT_SPLX_001))
                                    {
                                        xp = true;
                                    }
                                    else if (sType.contains(DictionaryConst.LKT_SPLX_002))
                                    {
                                        rx = true;
                                    }
                                    else if (sType.contains(DictionaryConst.LKT_SPLX_003))
                                    {
                                        tj = true;
                                    }
                                }
                                map.put("xp", xp);
                                map.put("rx", rx);
                                map.put("tj", tj);
                                map.put("vip_price", vipPrice);
                            }
                        }
                    }
                    else
                    {
                        //营销插件
                        switch (acType)
                        {
                            case 2:
                                //拼团
//                                goodsList = publiceGroupService.getGroupGoodsInfo(storeId, 0, 3);
                                break;
                            case 3:
                                //砍价
//                                goodsList = publiceBargainService.bargainGoodsInfo(storeId, 0, 10, user);
                                break;
                            case 4:
                                //竞拍
//                                goodsList = publiceAuctionService.getAuctionGoodsInfo(storeId, user, 0, 3);
                                break;
                            case 7:
                                //积分商城
                                IntegralConfigModel integralConfigModel = new IntegralConfigModel();
                                integralConfigModel.setStore_id(storeId);
                                integralConfigModel.setMch_id(storeMchId);
                                integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
                                if (integralConfigModel != null)
                                {
                                    if (integralConfigModel.getStatus().equals(0))
                                    {
                                        continue tag;
                                    }
                                }
                                else
                                {
                                    continue tag;
                                }
                                goodsList = publiceIntegralService.getIntegralGoodsInfo(storeId, 0, 3);
                                break;
                            case 8:
                                //秒杀
//                                Integer zyMchId = customerModelMapper.getStoreMchId(storeId);
//                                goodsList = this.seconds(storeId, zyMchId);
                                break;
                            default:
                                break;
                        }
                    }
                    if (goodsList == null)
                    {
                        goodsList = new ArrayList<>();
                    }
                    activityMap.put("list", goodsList);
                    activityMap.put("title", MapUtils.getString(activityMap, "name"));
                    resultList.add(activityMap);
                }
                redisUtil.set(homeMarketListKey, JSON.toJSONString(resultList), 300);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商城首页活动数据", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMarketingList");
        }
        return resultList;
    }



    @Override
    public List<Map<String, Object>> getMore(MainVo vo, int cid, String sort_criteria, String sort) throws LaiKeAPIException
    {
        try
        {
            //商品状态 用于in查询
            List<Integer> GoodsStatus = new ArrayList<>();
            //商品集
            List<Map<String, Object>> resultProductMap;


            //获取用户信息
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            //会员折扣率
            BigDecimal gradeRate = getMemberGrade(vo.getStoreId(), user);

            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            GoodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                GoodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }
            //根据条件获取商品列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("GoodsStatus", GoodsStatus);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("cid", cid);
            goodSort(sort_criteria, sort, parmaMap);
            parmaMap.put("show_adr", DictionaryConst.GoodsShowAdr.GOODSSHOWADR_INDEX);
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("lang_code", vo.getLanguage());
            parmaMap.put("country_num", vo.getCountry_num());
            List<Map<String, Object>> productListModels = productListModelMapper.getProductList(parmaMap);
            resultProductMap = getGoodsInfo(vo.getStoreId(), gradeRate, productListModels,vo.getLanguage());

            return resultProductMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("更多商品加载失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GDSPJZSBWLYC, "更多商品加载失败,网络异常", "getMore");
        }

    }

    /**
     * 商品排序
     * @param sort_criteria
     * @param sort
     * @param parmaMap
     */
    private void goodSort(String sort_criteria, String sort, Map<String, Object> parmaMap) {
        if (StringUtils.isNotEmpty(sort_criteria)) {
            String volumeTj = "volume";
            String price = "price";
            if (volumeTj.equals(sort_criteria)) {
                //销量
                parmaMap.put("volume_sort", sort);
            } else if (price.equals(sort_criteria)) {
                //价格
                parmaMap.put("price_sort", sort);
            }
        } else {
            //默认
            parmaMap.put("else_sort", "desc");
        }
    }

    String HOME_MEMBER_GRADE_KEY = "home_member_grade_key_";

    /**
     * 获取会员等级信息
     * 用户登录了，则直接去数据获取
     * 用户没有登录则从缓存获取没有再从数据库查询获取
     *
     * @param storeId
     * @param user
     * @return
     */
    private BigDecimal getMemberGrade(int storeId, User user)
    {
        //
        if (user != null)
        {
            return BigDecimal.valueOf(publicMemberService.getMemberGradeRate(null, user.getUser_id(), storeId));
        }

        String memberGardeKey = HOME_MEMBER_GRADE_KEY + storeId;
        caffeineCache.getIfPresent(memberGardeKey);
        Object gradeCache = caffeineCache.asMap().get(memberGardeKey);
        if (gradeCache == null)
        {
            gradeCache = userGradeModelMapper.getGradeLow(storeId).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
            caffeineCache.put(memberGardeKey, gradeCache);
        }
        return new BigDecimal(String.valueOf(gradeCache));
    }



    @Override
    public Map<String, Object> getLocation(MainVo vo, Integer groupId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //排序后的结果集  按照每个市首字母进行键位排序
        Map<String, List<Map<String, Object>>> adminCgMap = new TreeMap<>(Comparator.naturalOrder());
        //获取县
        List<Map<String, Object>> adminCgModelList = new ArrayList<>();
        try
        {
            //获取市区
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("G_Level", DictionaryConst.Position.LEVEL_3);
            List<Map<String, Object>> adminCgModels = adminCgModelMapper.getAdminCgInfoDynamic(parmaMap);
            for (Map<String, Object> adminCg : adminCgModels)
            {
                String cgName = adminCg.get("G_CName").toString();
                //处理市辖区
                if ("市辖区".equals(cgName))
                {
                    int          pid          = Integer.parseInt(adminCg.get("G_ParentID").toString());
                    AdminCgModel adminCgModel = new AdminCgModel();
                    adminCgModel.setId(pid);
                    adminCgModel = adminCgModelMapper.selectOne(adminCgModel);
                    if (adminCgModel != null)
                    {
                        cgName = adminCgModel.getDistrictName();
                        adminCg.put("G_CName", cgName);
                    }
                    else
                    {
                        logger.debug("未获取到市辖区 G_ParentID :" + cgName);
                    }
                }
                //获取市名称每个字拼音首字母
                String ypHead = PinyinUtils.getPinYinHeadChar(cgName);
                //截取第一个
                String                    first                = ypHead.substring(0, 1).toUpperCase();
                List<Map<String, Object>> adminCgModelListTemp = new ArrayList<>();
                if (adminCgMap.containsKey(first))
                {
                    adminCgModelListTemp = adminCgMap.get(first);
                }
                adminCgModelListTemp.add(adminCg);
                adminCgMap.put(first, adminCgModelListTemp);
            }
            //获取县级
            if (groupId != null)
            {
                parmaMap.clear();
                parmaMap.put("G_ParentID", groupId);
                adminCgModelList = adminCgModelMapper.getAdminCgInfoDynamic(parmaMap);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("位置获取失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZHQSBWLYC, "位置获取失败,网络异常", "getLocation");
        }
        resultMap.put("list", adminCgMap);
        resultMap.put("xian", adminCgModelList);
        return resultMap;
    }



    @Override
    public List<GuideModel> guidedGraph(int storeId, String accessId, String language, int storeType) throws LaiKeAPIException
    {
        List<GuideModel> guideModelList;
        try
        {
            GuideModel guideModel = new GuideModel();
            guideModel.setStore_id(storeId);
            guideModel.setSource(storeType);
            guideModel.setType(DictionaryConst.GuideType.GUIDETYPE_00);
            guideModelList = guideModelMapper.getGuidedGraph(guideModel);
            for (GuideModel guide : guideModelList)
            {
                String imgUrl = guide.getImage();
                imgUrl = publicService.getImgPath(imgUrl, storeId);
                guide.setImage(imgUrl);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("开机图获取失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "guidedGraph");
        }

        return guideModelList;
    }

    /**
     * 新品上市数据
     */
    String HOME_INDEX_NEWARRIVAL_KEY = "home_app_index_new_arrival_";


    @Override
    public Map<String, Object> newArrival(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //当前登录会员享受的折扣
        BigDecimal gradeRate = BigDecimal.ONE;
        //只有登录才享受折扣值
        BigDecimal gradeValue = BigDecimal.ZERO;
        //商品信息
        List<Map<String, Object>> goodsList;
        //是否享受会员折扣
        boolean isGrade = false;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            //商品状态 用于in查询
            List<Integer>       goodsStatus = new ArrayList<>();
            Map<String, Object> parmaMap    = new HashMap<>(16);
            gradeRate = getMemberGrade(vo.getStoreId(), user);
            gradeValue = gradeRate;
            //获取折扣
            if (user != null)
            {
                if (BigDecimal.ONE.compareTo(gradeRate) > 0)
                {
                    isGrade = true;
                }
            }

            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap.put("stockNum", "stockNum");
            }


            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("GoodsStatus", goodsStatus);

            //商品标签
            int proId = proLabelModelMapper.getProLabelNew(vo.getStoreId());
            parmaMap.put("s_type", proId);
            parmaMap.put("else_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());

            String appIndexArrivalKey = HOME_INDEX_NEWARRIVAL_KEY + vo.getStoreId();
            caffeineCache.getIfPresent(appIndexArrivalKey);
            goodsList = (List<Map<String, Object>>) caffeineCache.asMap().get(appIndexArrivalKey);

            if (goodsList == null || goodsList.size() == 0 || vo.getPageSize() >= 10)
            {
                goodsList = productListModelMapper.getProductListDynamic(parmaMap);
                for (Map<String, Object> map : goodsList)
                {
                    String imgUrl = map.get("imgurl") + "";
                    imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                    Integer productId = MapUtils.getInteger(map, "id");
                    //获取付款人数
                    int payPeople = orderDetailsModelMapper.payPeople(vo.getStoreId(), productId);
                    map.put("volume",payPeople);
                    map.put("imgurl", imgUrl);
                    //获取商品标签
                    String sType = MapUtils.getString(map, "s_type");
                    map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));
                    //如果商品为需要预约时间的则不能加入购物车，为1是可以加入购物车
                    if (!map.containsKey("is_appointment") || MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin)
                    {
                        map.put("isAddCar", 1);
                    }
                }
                GoodsDataUtils.getGoodsInfo(goodsList, gradeRate);
                caffeineCache.put(appIndexArrivalKey, goodsList);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("新品上市获取失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "newArrival");
        }
        resultMap.put("list", goodsList);
        resultMap.put("grade", gradeValue);
        resultMap.put("isGrade", isGrade);
        return resultMap;
    }



    @Override
    public Map<String, Object> recommendStores(MainVo vo, String longitude, String latitude, Integer cid,String lang_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> mchMapList = new ArrayList<>();
            //38558禅道优化
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            paramMap.put("review_status", DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
            paramMap.put("is_lock", String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO));
            paramMap.put("collection_num_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            if (cid != null)
            {
                paramMap.put("cid", cid);
            }
            List<Map<String, Object>> mchUserInfos = mchModelMapper.getMchUserInfo(paramMap);
            if (mchUserInfos != null && mchUserInfos.size() > 0)
            {
                for (Map<String, Object> mchUserInfo : mchUserInfos)
                {
                    Map<String, Object> mchMap = new HashMap<>(16);
                    //获取商户logo图片地址
                    String logo       = MapUtils.getString(mchUserInfo, "logo");
                    String imgLogoUrl = publicService.getImgPath(logo, vo.getStoreId());
                    mchMap.put("logo", imgLogoUrl);
                    //店铺头像
                    String headImg = MapUtils.getString(mchUserInfo, "head_img");
                    mchMap.put("headimgurl", publicService.getImgPath(headImg, vo.getStoreId()));
                    //获取商户所属人信息
                    mchMap.put("user_name", MapUtils.getString(mchUserInfo, "user_name"));
                    mchMap.put("user_id", MapUtils.getString(mchUserInfo, "user_id"));
                    mchMap.put("source", MapUtils.getString(mchUserInfo, "source"));
                    mchMap.put("shop_id", MapUtils.getInteger(mchUserInfo, "id"));
                    mchMap.put("name", MapUtils.getString(mchUserInfo, "name"));
                    //宣传图
                    String mchBackImgUrl = publicService.getImgPath(MapUtils.getString(mchUserInfo, "poster_img"), vo.getStoreId());
                    if (StringUtils.isEmpty(mchBackImgUrl))
                    {
                        MchConfigModel mchConfig = new MchConfigModel();
                        mchConfig.setStore_id(vo.getStoreId());
                        mchConfig.setMch_id(customerModelMapper.getStoreMchId(vo.getStoreId()));
                        mchConfig = mchConfigModelMapper.selectOne(mchConfig);
                        if (mchConfig != null)
                        {
                            mchBackImgUrl = publicService.getImgPath(mchConfig.getPoster_img(), vo.getStoreId());
                        }
                    }
                    mchMap.put("backImgUrl", mchBackImgUrl);
                    mchMap.put("shop_information", MapUtils.getString(mchUserInfo, "shop_information"));
                    mchMap.putAll(publicService.commodityInformation(vo.getStoreId(), MapUtils.getInteger(mchUserInfo, "id"),lang_code));
                    mchMap.put("collection_num", MapUtils.getInteger(mchUserInfo, "collection_num", 0));

                    //判断店铺是否正在直播
                    LivingRoomModel livingRoomModel = new LivingRoomModel();
                    livingRoomModel.setLiving_status(LivingRoomModel.STATUS_LIVING_STREAMING);
                    livingRoomModel.setUser_id(MapUtils.getString(mchUserInfo, "user_id").toString());
                    livingRoomModel.setRecycle(LivingRoomModel.RECYCLE_SHOW);
                    int count = livingRoomModelMapper.selectCount(livingRoomModel);

                    //true为店铺正在直播
                    if (count > 0)
                    {
                        mchMap.put("livingStatus", true);
                    }
                    else
                    {
                        mchMap.put("livingStatus", false);
                    }
                    //判断店铺插件是否开启
                    LivingConfigModel livingConfigModel = new LivingConfigModel();
                    livingConfigModel.setStore_id(vo.getStoreId());
                    livingConfigModel.setRecycle(0);
                    livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);

                    if (StringUtils.isEmpty(livingConfigModel))
                    {
                        mchMap.put("mch_is_open", 0);
                    }
                    else
                    {
                        mchMap.put("mch_is_open", livingConfigModel.getMch_is_open());
                    }

                    mchMapList.add(mchMap);
                }
            }
            resultMap.put("list", mchMapList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("推荐门店获取失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "recommendStores");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> mchClass(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("isDisplay", DictionaryConst.WhetherMaven.WHETHER_OK);
            int                       i    = mchClassModelMapper.countCondition(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = mchClassModelMapper.selectCondition(paramMap);
                list.stream().forEach(map ->
                {
                    map.put("img", publicService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                });
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchClassList");
        }
        return resultMap;
    }



    @Override
    public boolean selectLanguage(int storeId, String accessId, String language) throws LaiKeAPIException
    {
        boolean result = false;
        try
        {
                //获取用户信息
                User user = RedisDataTool.getRedisUserCache(accessId, redisUtil, true);
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", storeId);
                parmaMap.put("access_id", user.getAccess_id());
                parmaMap.put("language", language);
                int count = userBaseMapper.updateUserAccessId(parmaMap);
                if (count < 1)
                {
                    logger.debug("用户修改语言失败 userid=" + user.getUser_id());
                }
                else
                {
                    result = true;
                }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("更改语言异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "selectLanguage");
        }

        return result;
    }

    //首页推荐和上新缓存数据
    String HOME_RECOMMONED_PROS_KEY = "home_recommoned_pros_info_";
    //商品配置信息
    String PRO_CONFIG_INFO_KEY      = "pro_config_info_key_";

    @Override
    public Map<String, Object> recommend(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object>       resultMap          = new HashMap<>(16);
        List<Map<String, Object>> goodsList;
        BigDecimal                grade              = BigDecimal.ZERO;
        BigDecimal                gradeRate          = BigDecimal.ONE;
        List<Integer>             goodsStatus        = new ArrayList<>();
        ProductConfigModel        productConfigModel = null;
        //是否享受会员折扣
        boolean isGrade = false;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            //获取商户产品配置
            String proConfigInfoKey = PRO_CONFIG_INFO_KEY + vo.getStoreId();
            caffeineCache.getIfPresent(proConfigInfoKey);
            productConfigModel = (ProductConfigModel) caffeineCache.asMap().get(proConfigInfoKey);
            if (productConfigModel == null)
            {
                productConfigModel = new ProductConfigModel();
                productConfigModel.setStore_id(vo.getStoreId());
                productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
                if (productConfigModel != null)
                {
                    caffeineCache.put(proConfigInfoKey, productConfigModel);
                }
            }

            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }

            gradeRate = getMemberGrade(vo.getStoreId(), user);
            //获取用户信息
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                //获取用户信息
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                if (user != null)
                {
                    grade = new BigDecimal(user.getGrade());
//                    gradeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(null, user.getUser_id(), vo.getStoreId()));
                    if (BigDecimal.ONE.compareTo(gradeRate) > 0)
                    {
                        isGrade = true;
                    }
                }
            }


            //获取匹配到的推荐商品
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("GoodsStatus", goodsStatus);
            parmaMap.put("else_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("s_type", proLabelModelMapper.getProLabelTop(vo.getStoreId()));
            parmaMap.put("sTypeNotNull", "sTypeNotNull");
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap.put("stockNum", "stockNum");
            }
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());


            String homeRecommonedProInfosKey = HOME_RECOMMONED_PROS_KEY + vo.getStoreId();
            goodsList = (List<Map<String, Object>>) caffeineCache.asMap().get(homeRecommonedProInfosKey);
            if (goodsList == null || goodsList.size() == 0 || vo.getPageSize() >= 10)
            {
                goodsList = productListModelMapper.getProductListDynamic(parmaMap);
                //处理商品信息
                for (Map<String, Object> map : goodsList)
                {
                    Integer productId    = MapUtils.getInteger(map, "id");
                    //获取付款人数
                    int payPeople = orderDetailsModelMapper.payPeople(vo.getStoreId(), productId);
                    map.put("volume",payPeople);
                    String imgUrl = map.get("imgurl") + "";
                    imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                    map.put("imgurl", imgUrl);//获取商品标签
                    String sType = MapUtils.getString(map, "s_type");
                    map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));
                    //虚拟商品需要线下预约的商品不能加购物车，为1是可以加
                    if (!map.containsKey("is_appointment") || (MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin))
                    {
                        map.put("isAddCar", 1);
                    }
                }
                GoodsDataUtils.getGoodsInfo(goodsList, gradeRate);
                caffeineCache.put(homeRecommonedProInfosKey, goodsList);
            }

            resultMap.put("list", goodsList);
            resultMap.put("grade", grade);
            resultMap.put("isGrade", isGrade);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("推荐商品异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "recommend");
        }
        return resultMap;
    }

    /**
     * 获取商品信息
     *
     * @param productListModels -
     * @return List
     * @throws LaiKeAPIException -
     */
    private List<Map<String, Object>> getGoodsInfo(Integer storeId, BigDecimal grade_rate, List<Map<String, Object>> productListModels,String language) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultMap = new ArrayList<>();
        try
        {
            //商品信息逻辑处理
            for (Map<String, Object> map : productListModels)
            {
                map.put("isAddCar", 1);
                int pid = Integer.parseInt(map.get("id") + "");
                //销量
                int volume = Integer.parseInt(map.get("volume") + "");
                //真实销量
                int realVolume = Integer.parseInt(map.get("real_volume") + "");
                //商品类型 0.实物商品 1.虚拟商品
                Integer commodity_type = MapUtils.getInteger(map, "commodity_type", 0);
                //虚拟商品处理
                if (commodity_type.equals(ProductListModel.COMMODITY_TYPE.virtual))
                {
                    //需要线下核销的商品，不可加入购物车
                    //核销设置 1.线下核销 2.无需核销
                    Integer write_off_settings = MapUtils.getInteger(map, "write_off_settings", 2);
                    //预约时间设置 1.无需预约下单 2.需要预约下单
                    Integer is_appointment = MapUtils.getInteger(map, "is_appointment", 1);
                    if (write_off_settings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                    {
                        map.put("isAddCar", 2);
                    }
                }
                //产品值属性 1：新品,2：热销，3：推荐
                String stype = map.get("s_type") + "";
                //图片url
                String image = map.get("imgurl") + "";
                //获取商品标签
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(storeId, DataUtils.convertToList(StringUtils.trim(stype, SplitUtils.DH).split(SplitUtils.DH))));
                if (volume < 0)
                {
                    volume = 0;
                }
                //判断会员等级 等级不同，优惠不同`
                //原来价格
                BigDecimal vipYprice = new BigDecimal(map.get("price") + "");
                //打折后的价格 折扣 原来价格 * 折扣  = 优惠价
                BigDecimal vipPrice = vipYprice.multiply(grade_rate);
                if (grade_rate.compareTo(new BigDecimal("1")) > 0)
                {
                    vipPrice = vipPrice.divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                }
                //获取轮播图完整路径
                String imagUrl = publicService.getImgPath(image, storeId);
                //商品封面图
                String coverUrl = publicService.getImgPath(String.valueOf(map.get("cover_map")), storeId);
                //店铺logo
                String logoUrl = publicService.getImgPath(String.valueOf(map.get("logo")), storeId);
                //库存
                int countNum = confiGureModelMapper.countConfigGureNum(pid);
                map.put("vip_yprice", vipYprice);
                map.put("vip_price", vipPrice);
                map.put("imgurl", imagUrl);
                map.put("logo", logoUrl);
                map.put("cover_map", coverUrl);
                map.put("contNum", countNum);
                map.put("volume", volume + realVolume);
                //付款人数   禅道46291
                int payPeople = orderDetailsModelMapper.payPeople(storeId, pid);
                map.put("payPeople", payPeople);
                // 注入最高优先级营销活动参数
                Integer mchId = MapUtils.getInteger(map, "mchId", 0);
                Map<String, String> marketingParams = appsProductService.getTopMarketingParams(storeId, pid, language, mchId);
                if (marketingParams != null) {
                    map.put("marketingParams", marketingParams);
                }
                resultMap.add(map);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("首页加载商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQSPXXSB, "获取商品信息失败", "getGoodsInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserTell(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //公告标题
            String systemMsgTitle = "";
            //系统公告
            String systemMsg = "";
            //公告类型 1=系统维护 2=升级公告 3--普通公告
            int systemMsgType = 0;
            //维护开始、结束时间
            String              systemMsgStartDate = "";
            String              systemMsgEndDate   = "";
            String              info               = "";
            Map<String, Object> parmaMap           = new HashMap<>(16);
            parmaMap.put("store_id", 0);
            parmaMap.put("startDate_lt", new Date());
            parmaMap.put("endDate_gt", new Date());
            parmaMap.put("type_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("user_tell", SystemTellModel.TELL.YES);
            List<Map<String, Object>> systemList = systemTellModelMapper.selectDynamic(parmaMap);
            if (systemList.size() > 0)
            {
                Map<String, Object> systemMap = systemList.get(0);
                systemMsgType = MapUtils.getIntValue(systemMap, "type");
                systemMsgTitle = MapUtils.getString(systemMap, "title");
                systemMsg = MapUtils.getString(systemMap, "content");
                systemMsgEndDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "enddate"), GloabConst.TimePattern.YMDHMS);
                systemMsgStartDate = DateUtil.dateFormate(MapUtils.getString(systemMap, "startdate"), GloabConst.TimePattern.YMDHMS);
                if (systemMsgType == 1)
                {
                    info = "系统维护中";
                }
            }
            //系统公告
            resultMap.put("systemMsgTitle", systemMsgTitle);
            resultMap.put("systemMsg", systemMsg);
            resultMap.put("systemMsgType", systemMsgType);
            resultMap.put("systemMsgEndDate", systemMsgEndDate);
            resultMap.put("systemMsgStartDate", systemMsgStartDate);
            resultMap.put("info", info);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取平台用户公告");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SYPTSPLCCC, "获取平台用户公告 出错", "goGroup");
        }
        return resultMap;
    }

    @Override
    public void markToRead(MainVo vo, Integer tell_id) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(tell_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "markToRead");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicSystemTellService.markToRead(vo, user.getUser_id(), tell_id, false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记公告以读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "markToRead");
        }
    }

    @Autowired
    private LivingRoomModelMapper livingRoomModelMapper;

    @Override
    public Map<String, Object> queryLiving(MainVo vo)
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int pageSize = 4;
            if (Objects.nonNull(vo.getLimit_num()))
            {
                pageSize = vo.getLimit_num();
            }
            //获取正在直播的主播信息
            List<Map<String, Object>> list = livingRoomModelMapper.queryLiving(vo.getStoreId(),pageSize);

            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取正在直播的主播");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SYPTSPLCCC, "获取正在直播的主播", "goGroup");
        }
        return resultMap;
    }

    @Override
    public void changeCurrency(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            User   user     = null;
            String accessId = vo.getAccessId();
            if (!StringUtils.isEmpty(accessId))
            {
                //获取用户信息
                Object obj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + accessId);
                if (obj != null)
                {
                    user = JSON.parseObject(obj.toString(), User.class);
                    int uid   = user.getId();
                    int count = userBaseMapper.changePreferredCurrency(uid, vo.getCurrency_id());

                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "changeCurrency");
                    }

                    //刷新用户信息
                    User userInfo = new User();
                    userInfo.setId(uid);
                    userInfo = userBaseMapper.selectOne(userInfo);
                    RedisDataTool.refreshRedisUserCache(accessId, userInfo, redisUtil);

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
            logger.error("更改币种异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "changeCurrency");
        }

    }

    @Override
    public Map<String, Object> distributionList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            int pageSize = 3;
            if (Objects.nonNull(vo.getLimit_num()))
            {
                pageSize = vo.getLimit_num();
            }
            vo.setPageNo(0);
            vo.setPageNum(pageSize);
            Map<String,Object> goodsRsultMap = publiceDistributionService.getGoodsList(vo,null,null,1,null,user);

            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(vo.getStoreId());
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (distributionConfigModel != null && distributionConfigModel.getAdvertising() == 1)
            {
                goodsRsultMap.put("ad_image", distributionConfigModel.getAd_image());
            }

            List<Map<String, Object>> goodsList = (List<Map<String, Object>>) goodsRsultMap.get("pro");
            goodsRsultMap.put("list", goodsList);

            resultMap.put("distribution_list", goodsRsultMap);


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分销商品异常{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "distributionList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getMchList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        int storeId = vo.getStoreId();
        try
        {
            List<Map<String, Object>> mchStoreAllList = new ArrayList<>();
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, false);
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(storeId);
            mchConfigModel.setMch_id(customerModelMapper.getStoreMchId(storeId));
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
            if (mchConfigModel != null && mchConfigModel.getIs_display() == DictionaryConst.WhetherMaven.WHETHER_OK)
            {
                //38558禅道优化
                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("store_id", storeId);
                paramMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
                paramMap.put("review_status", DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
                paramMap.put("is_lock", String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO));
                paramMap.put("collection_num_sort", DataUtils.Sort.DESC.toString());
                paramMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
                if (Objects.nonNull(vo.getLimit_num()))
                {
                    paramMap.put("pageStart",0);
                    paramMap.put("pageEnd",vo.getLimit_num());
                }
                List<Map<String, Object>> mchUserInfos = mchModelMapper.getMchUserInfo(paramMap);
                if (mchUserInfos != null && mchUserInfos.size() > 0)
                {
                    for (Map<String, Object> mchUserInfo : mchUserInfos)
                    {
                        Map<String, Object> mchMap = new HashMap<>(16);
                        //获取商户logo图片地址
                        String logo       = MapUtils.getString(mchUserInfo, "logo");
                        String imgLogoUrl = publicService.getImgPath(logo, storeId);
                        mchMap.put("logo", imgLogoUrl);
                        //店铺头像
                        String headImg = MapUtils.getString(mchUserInfo, "head_img");
                        mchMap.put("headimgurl", publicService.getImgPath(headImg, storeId));
                        //获取商户所属人信息
                        mchMap.put("user_name", MapUtils.getString(mchUserInfo, "user_name"));
                        mchMap.put("user_id", MapUtils.getString(mchUserInfo, "user_id"));
                        mchMap.put("source", MapUtils.getString(mchUserInfo, "source"));
                        mchMap.put("shop_id", MapUtils.getInteger(mchUserInfo, "id"));
                        mchMap.put("name", MapUtils.getString(mchUserInfo, "name"));
                        //宣传图
                        String mchBackImgUrl = publicService.getImgPath(MapUtils.getString(mchUserInfo, "poster_img"), storeId);
                        if (StringUtils.isEmpty(mchBackImgUrl))
                        {
                            MchConfigModel mchConfig = new MchConfigModel();
                            mchConfig.setStore_id(storeId);
                            mchConfig.setMch_id(customerModelMapper.getStoreMchId(storeId));
                            mchConfig = mchConfigModelMapper.selectOne(mchConfig);
                            if (mchConfig != null)
                            {
                                mchBackImgUrl = publicService.getImgPath(mchConfig.getPoster_img(), storeId);
                            }
                        }
                        mchMap.put("backImgUrl", mchBackImgUrl);
                        //是否关注
                        boolean collectionStatus = false;
                        if (user != null)
                        {
                            UserCollectionModel userCollectionModel = new UserCollectionModel();
                            userCollectionModel.setUser_id(user.getUser_id());
                            userCollectionModel.setMch_id(MapUtils.getInteger(mchUserInfo, "id"));
                            int i = userCollectionModelMapper.selectCount(userCollectionModel);
                            if (i > 0)
                            {
                                collectionStatus = true;
                            }
                        }
                        mchMap.put("collection_status", collectionStatus);

                        //判断店铺是否正在直播
                        LivingRoomModel livingRoomModel = new LivingRoomModel();
                        livingRoomModel.setLiving_status(LivingRoomModel.STATUS_LIVING_STREAMING);
                        livingRoomModel.setUser_id(MapUtils.getString(mchUserInfo, "user_id").toString());
                        livingRoomModel.setRecycle(LivingRoomModel.RECYCLE_SHOW);
                        int count = livingRoomModelMapper.selectCount(livingRoomModel);

                        //true为店铺正在直播
                        if (count > 0)
                        {
                            mchMap.put("livingStatus", true);
                        }
                        else
                        {
                            mchMap.put("livingStatus", false);
                        }

                        //判断店铺插件是否开启
                        LivingConfigModel livingConfigModel = new LivingConfigModel();
                        livingConfigModel.setStore_id(storeId);
                        livingConfigModel.setRecycle(0);
                        livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);

                        if (StringUtils.isEmpty(livingConfigModel))
                        {
                            mchMap.put("mch_is_open", 0);
                        }
                        else
                        {
                            mchMap.put("mch_is_open", livingConfigModel.getMch_is_open());
                        }
                        mchStoreAllList.add(mchMap);
                    }
                }
            }
            resultMap.put("r_mch", mchStoreAllList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取推荐商家异常{}", e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchList");
        }
        return resultMap;
    }


    @Autowired
    private PublicSystemTellService publicSystemTellService;

    @Autowired
    PublicCouponService publicCouponService;

    @Autowired
    PubliceService publicService;

    @Autowired
    PluginsModelMapper pluginsModelMapper;

    @Autowired
    UserGradeModelMapper userGradeModelMapper;

    @Autowired
    ConfigModelMapper configModelMapper;

    @Autowired
    FilesRecordModelMapper filesRecordModelMapper;

    @Autowired
    UploadConfigModelMapper uploadConfigModelMapper;

    @Autowired
    BannerModelMapper bannerModelMapper;

    @Autowired
    ProductModelMapper productModelMapper;

    @Autowired
    ProductClassModelMapper productClassModelMapper;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    MchModelMapper mchModelMapper;

    @Autowired
    MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    GroupProductModelMapper groupProductModelMapper;

    @Autowired
    SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    SecondsProModelMapper secondsProModelMapper;

    @Autowired
    SignConfigModelMapper signConfigModelMapper;

    @Autowired
    SignRecordModelMapper signRecordModelMapper;


    @Autowired
    ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    AdminCgModelMapper adminCgModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    GuideModelMapper guideModelMapper;

    @Autowired
    private ActivityModelMapper activityModelMapper;

    @Autowired
    private ActivityProModelMapper activityProModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private PublicAuctionService publiceAuctionService;

    @Autowired
    private PubliceIntegralService publiceIntegralService;

    @Autowired
    private UiNavigationBarModelMapper uiNavigationBarModelMapper;

    @Autowired
    private PubliceSubtractionService publiceSubtractionService;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private MemberConfigMapper  memberConfigMapper;
    @Autowired
    private MchClassModelMapper mchClassModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private SystemTellModelMapper systemTellModelMapper;

}
