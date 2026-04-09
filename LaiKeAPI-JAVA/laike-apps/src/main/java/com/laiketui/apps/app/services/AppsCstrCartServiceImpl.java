package com.laiketui.apps.app.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.laiketui.apps.api.app.services.AppsCstrCartService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicMemberService;
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
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.Page;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ProductConfigModel;
import com.laiketui.domain.mch.CartModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.root.license.Md5Util;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.*;

/**
 * 购物车实现
 *
 * @author Trick
 * @date 2020/10/20 9:55
 */
@Service
public class AppsCstrCartServiceImpl implements AppsCstrCartService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrCartServiceImpl.class);

    @Autowired
    ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserGradeModelMapper userGradeModelMapper;

    @Autowired
    CartModelMapper cartModelMapper;

    @Autowired
    PubliceService publicService;

    @Autowired
    ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    MchModelMapper mchModelMapper;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    CommentsModelMapper commentsModelMapper;

    @Autowired
    CommentsImgModelMapper commentsImgModelMapper;

    @Autowired
    ReplyCommentsModelMapper replyCommentsModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private Cache<String, Object> caffeineCache;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    /**
     * 购物车商品推荐列表缓存
     */
    String CART_INDEX_RECOMMONED_PROS_DATA_KEY = "com.laike.app.cartIndex_recommonedpro_";


    @Override
    public Map<String, Object> index(MainVo vo, Integer commodityType) throws LaiKeAPIException
    {
        int    storeId  = vo.getStoreId();
        String language = vo.getLanguage();
        String accessId = vo.getAccessId();
        int    pageNo   = vo.getPageNo();

        Map<String, Object> resultMap = new HashMap<>(16);
        //店铺信息
        List<MchModel> mchModelList = null;
        //商品信息
        List<Map<String, Object>> goodsList = new ArrayList<>();

        //是否享受会员折扣
        boolean isGrade = false;
        //是否登陆标识
        int loginStatus = 0;
        try
        {
            Page pageModel = Page.newBuilder((pageNo - 1) * GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE, GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE, null);

            User user = null;
            //会员折扣率
            BigDecimal gradeRate = new BigDecimal("1");
            //获取用户信息
            user = RedisDataTool.getRedisUserCache(accessId, redisUtil, false);
            if (user != null)
            {
                loginStatus = 1;
                //获取会员等级对应的折扣
                gradeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(null, user.getUser_id(), storeId));
                if (new BigDecimal("1").compareTo(gradeRate) > 0)
                {
                    isGrade = true;
                }
            }

            //商品状态 用于in查询
            List<Integer> goodsStatus = new ArrayList<>();
            //店铺id
            List<Integer> mchIdList = new ArrayList<>();
            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(storeId);
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                //已下架
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            //判断是否为游客
            if (user != null)
            {
                //会员
                parmaMap.put("user_id", user.getUser_id());
            }
            else
            {
                //游客
                parmaMap.put("yk_token", accessId);
                parmaMap.put("user_id_not_ull", "user_id_not_ull");
            }
            if (commodityType != null)
            {
                parmaMap.put("commodityType", commodityType.toString());
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //上架
            List<Map<String, Object>> cartList1 = new ArrayList<>();
            //下架、待上架、无库存、已删除
            List<Map<String, Object>> cartList2 = new ArrayList<>();
            if (!StringUtils.isEmpty(accessId))
            {
                //获取购物车商品数据
                List<Map<String, Object>> resultCartList = cartModelMapper.getUserShopCartList(parmaMap);
                for (Map<String, Object> map : resultCartList)
                {
                    int attrId = Integer.parseInt(map.get("Size_id").toString());
                    //商品状态
                    int status = Integer.parseInt(map.get("status").toString());
                    //商品是否回收
                    int recycle = Integer.parseInt(map.get("recycle").toString());
                    //商品总库存
                    int stockNumTotal = MapUtils.getIntValue(map, "num");
                    //店铺是否营业
                    String is_open = MapUtils.getString(map, "is_open");
                    //店铺营业时间
                    String business_hours = MapUtils.getString(map, "business_hours");
                    if (recycle == DictionaryConst.WhetherMaven.WHETHER_OK)
                    {
                        cartList2.add(map);
                    }
                    else
                    {
                        if (DictionaryConst.GoodsStatus.NEW_GROUNDING != status)
                        {
                            map.put("goodsStatus", 1);
                            cartList2.add(map);
                        }
                        else if (stockNumTotal <= 0)
                        {
                            map.put("goodsStatus", 0);
                            cartList2.add(map);
                        }
                        else
                        {
                            cartList1.add(map);
                        }
                    }
                    //产品信息
                    List<Map<String, Object>> skuBeanList = new ArrayList<>();
                    String                    price       = map.get("price") + "";
                    BigDecimal                vipPrice    = new BigDecimal(price);
                    //商品id
                    int pid = Integer.parseInt(map.get("pid") + "");
                    //库存数量
                    int stockNum = Integer.parseInt(map.get("num") + "");
                    int mchId    = Integer.parseInt(map.get("mch_id") + "");
                    if (!mchIdList.contains(mchId))
                    {
                        mchIdList.add(mchId);
                    }
                    String imgUrl = publicService.getImgPath(map.get("img") + "", storeId);

                    //商品属性
                    StringBuffer attribute = new StringBuffer(map.get("attribute") + "");
                    if (!StringUtils.isEmpty(attribute))
                    {
                        Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attribute.toString(), Map.class));
                        if (attributeMap != null)
                        {
                            for (String key : attributeMap.keySet())
                            {
                                String value = attributeMap.get(key) + "";
                                int    index = value.indexOf("_LKT_");
                                if (index > 0)
                                {
                                    String attribyteStr = value.substring(0, index);
                                    attribute.append(attribyteStr);
                                }
                            }
                        }
                    }
                    if (stockNum < 1)
                    {
                        stockNum = 0;
                    }
                    //商品配置信息
                    ConfiGureModel confiGure = confiGureModelMapper.selectByPrimaryKey(attrId);
                    //属性处理
                    String                    cfgAttribute = confiGure.getAttribute();
                    List<Map<String, Object>> attrList     = new ArrayList<>();
                    List<Map<String, Object>> attributes   = new ArrayList<>();
                    List<String>              arrayName    = new ArrayList<>();
                    StringBuilder             name         = new StringBuilder();
                    if (cfgAttribute != null)
                    {
                        Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(cfgAttribute, Map.class));
                        if (attributeMap != null)
                        {
                            for (String key : attributeMap.keySet())
                            {
                                List<Map<String, Object>> attrs = new ArrayList<>();
                                List<String>              alls  = new ArrayList<>();

                                //是否存在 不存在则添加
                                if (!arrayName.contains(key))
                                {
                                    Map<String, Object> attrMap = new HashMap<>(16);
                                    arrayName.add(key);
                                    String attribyteKey   = key;
                                    String attribyteValue = attributeMap.get(key) + "";

                                    int index = attribyteKey.indexOf("_LKT_");
                                    if (index > 0)
                                    {
                                        //属性名称
                                        attribyteKey = attribyteKey.substring(0, attribyteKey.indexOf("_LKT_"));
                                        //属性值
                                        attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT_"));
                                    }
                                    name.append(attribyteKey).append(":").append(attribyteValue).append(";");
                                    //属性集合
                                    Map<String, Object> attrArratMap = new HashMap<>(16);
                                    attrArratMap.put("attributeId", Md5Util.MD5endoce(attribyteKey));
                                    attrArratMap.put("attributeValId", Md5Util.MD5endoce(attribyteValue));
                                    attrArratMap.put("enable", false);
                                    attrArratMap.put("select", false);
                                    attrs.add(attrArratMap);
                                    //属性集合
                                    alls.add(attribyteValue + "");

                                    attrMap.put("attrName", attribyteKey);
                                    attrMap.put("attrType", "1");
                                    attrMap.put("id", Md5Util.MD5endoce(attribyteKey));
                                    attrMap.put("attr", attrs);
                                    attrMap.put("all", alls);
                                    attrList.add(attrMap);
                                    attrMap = new HashMap<>(16);
                                    attrMap.put("attributeId", Md5Util.MD5endoce(attribyteKey));
                                    attrMap.put("attributeValId", Md5Util.MD5endoce(attribyteValue));
                                    attributes.add(attrMap);
                                }
                            }
                        }
                    }
                    //获取产品图片
                    String cimgUrl = publicService.getImgPath(confiGure.getImg(), storeId);
                    //单位
                    String              unit         = confiGure.getUnit();
                    Map<String, Object> confiGureMap = new HashMap<>(16);
                    confiGureMap.put("name", name);
                    confiGureMap.put("imgurl", cimgUrl);
                    confiGureMap.put("cid", confiGure.getId());
                    confiGureMap.put("price", confiGure.getPrice());
                    confiGureMap.put("count", confiGure.getNum());
                    confiGureMap.put("unit", unit);
                    confiGureMap.put("attributes", attributes);
                    skuBeanList.add(confiGureMap);

                    //折扣 原来价格 * 折扣 / 10 = 优惠价
                    vipPrice = vipPrice.multiply(gradeRate);

                    map.put("id", map.get("id"));
                    map.put("attribute_id", map.get("attribute_id"));
                    map.put("mch_id", map.get("mch_id"));
                    map.put("pid", map.get("Goods_id"));
                    map.put("attribute", mchIdList);
                    map.put("price", vipPrice);
                    map.put("num", map.get("Goods_num"));
                    map.put("pro_name", map.get("product_title"));
                    map.put("imgurl", imgUrl);
                    map.put("stock", stockNum);
                    map.put("attrList", attrList);
                    map.put("skuBeanList", skuBeanList);
                    //虚拟商品
                    if (mchIdList.size() > 0)
                    {
                        parmaMap = new HashMap<>(16);
                        parmaMap.put("store_id", storeId);
                        parmaMap.put("mchList", mchIdList);
                        mchModelList = mchModelMapper.getMchDynamic(parmaMap);
                        for (MchModel mchModel : mchModelList)
                        {
                            mchModel.setHead_img(publicService.getImgPath(mchModel.getHead_img(), storeId));
                            mchModel.setLogo(publicService.getImgPath(mchModel.getLogo(), storeId));
                            mchModel.setIs_open(publicMchService.mchIsOpen(mchModel.getId()));
                        }
                    }
                }
            }

            // TODO: 2023/1/3 【优化】  获取【为您推荐】列表 建议抽出去单独api
            //获取【为您推荐】列表
            parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("GoodsStatus", goodsStatus);
//            parmaMap.put("is_open", MchModel.IS_OPEN_IN_BUSINESS);
            parmaMap.put("show_adr", DictionaryConst.GoodsShowAdr.GOODSSHOWADR_CART);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }
            parmaMap.put("country_num", vo.getCountry_num());
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap.put("stockNum", "stockNum");
            }

//            String cartIndexRecommondedKey = CART_INDEX_RECOMMONED_PROS_DATA_KEY + storeId;
//            caffeineCache.getIfPresent(cartIndexRecommondedKey);
//            goodsList = DataUtils.cast(caffeineCache.asMap().get(cartIndexRecommondedKey));
//            if (goodsList == null || goodsList.size() == 0 || pageNo != 1) {
            goodsList = productListModelMapper.getProductListDynamic(parmaMap);
            for (Map<String, Object> map : goodsList)
            {
                String imgUrl = map.get("imgurl") + "";
                imgUrl = publicService.getImgPath(imgUrl, storeId);
                int pid      = Integer.parseInt(map.get("pid") + "");
                int stockNum = confiGureModelMapper.countConfigGureNum(pid);
                //店铺信息
                String logoUrl = publicService.getImgPath(MapUtils.getString(map, "logo"), storeId);
                String mchName = map.get("mch_name").toString();
                //获取商品标签
                String sType = MapUtils.getString(map, "s_type");
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(storeId, DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));

                //原来价格
                BigDecimal vipYprice = new BigDecimal(map.get("price") + "");
                //打折后的价格
                BigDecimal vipPrice = new BigDecimal(map.get("price") + "");
                if (gradeRate.floatValue() != 1)
                {
                    //折扣 原来价格 * 折扣  = 优惠价
                    vipPrice = vipYprice.multiply(gradeRate);
                }
                map.put("vip_yprice", vipYprice.toString());
                map.put("vip_price", vipPrice.toString());
                map.put("num", stockNum);
                map.put("imgurl", imgUrl);
                map.put("logo", logoUrl);
                map.put("mch_name", mchName);
                int payPeople = orderDetailsModelMapper.payPeople(storeId, pid);
                map.put("payPeople", payPeople);
                //如果商品为需要预约时间的则不能加入购物车，为1是可以加入购物车
                if (!map.containsKey("is_appointment") || MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin)
                {
                    map.put("isAddCar", 1);
                }
                Integer writeOffSettings = MapUtils.getInteger(map, "write_off_settings");
                if (Objects.nonNull(writeOffSettings) && writeOffSettings == 1)
                {
                    map.put("isAddCar",2);
                    map.put("is_appointment",2);
                }

            }
//                caffeineCache.put(cartIndexRecommondedKey, goodsList);
//            }

            //正常上架的商品列表
            resultMap.put("data", cartList1);
            //下架、待上架、无库存
            resultMap.put("data0", cartList2);
            //【为您推荐】列表数据
            resultMap.put("list", goodsList);
            resultMap.put("login_status", loginStatus);
            resultMap.put("mch_list", mchModelList == null ? new ArrayList<>() : mchModelList);
            resultMap.put("grade", gradeRate);
            resultMap.put("isGrade", false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("进入购物车页面出错", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }



    @Override
    public boolean upCart(MainVo vo, String goods) throws LaiKeAPIException
    {
        try
        {
            JSONArray goodsMap;
            try
            {
                goodsMap = JSON.parseArray(goods);
            }
            catch (JSONException j)
            {
                j.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (goodsMap != null && !goodsMap.isEmpty())
            {
                for (int i = 0; i < goodsMap.size(); i++)
                {
                    JSONObject jsonObject = goodsMap.getJSONObject(i);
                    int        cartId     = Integer.parseInt(jsonObject.get("cart_id") + "");
                    int        num        = 1;
                    String     numObj     = jsonObject.get("num") + "";
                    if (StringUtils.isInteger(numObj))
                    {
                        num = Integer.parseInt(jsonObject.get("num") + "");
                    }
                    if (num < 1)
                    {
                        return false;
                    }
                    CartModel cartModel = new CartModel();
                    cartModel.setId(cartId);
                    if (user != null)
                    {
                        cartModel.setUser_id(user.getUser_id());
                    }
                    else
                    {
                        cartModel.setToken(vo.getAccessId());
                    }
                    cartModel.setStore_id(vo.getStoreId());
                    cartModel = cartModelMapper.selectOne(cartModel);
                    if (cartModel != null)
                    {
                        //属性id
                        int attributeId = Integer.parseInt(cartModel.getSize_id());
                        //根据属性获取库存
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setId(attributeId);
                        confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                        int stockNum = confiGureModel.getNum();

                        Map<String, Object> parmaMap = new HashMap<>(16);
                        //如果库存不充足则修改购物车数量为库存总数
                        parmaMap.put("cart_id", cartModel.getId());
                        if (user != null)
                        {
                            parmaMap.put("user_id", user.getUser_id());
                        }
                        else
                        {
                            parmaMap.put("access_id", vo.getAccessId());
                        }
                        parmaMap.put("Goods_num", Math.min(stockNum, num));
                        int count = cartModelMapper.updateCartById(parmaMap);
                        if (count < 1)
                        {
                            logger.debug("购物车修改失败 购物车id={}", cartModel.getId());
                        }
                        else
                        {
                            return true;
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDGWCSJ, "未找到购物车数据", "upCart");
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
            logger.error("编辑购物车 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upCart");
        }
        return false;
    }



    @Override
    public boolean delAllCart(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                User      user;
                Object    userObj   = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                CartModel cartModel = new CartModel();
                cartModel.setStore_id(vo.getStoreId());
                if (userObj != null)
                {
                    user = JSON.parseObject(userObj.toString(), User.class);
                    if (!StringUtils.isEmpty(user.getUser_id()))
                    {
                        cartModel.setUser_id(user.getUser_id());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    cartModel.setToken(vo.getAccessId());
                }
                int count = cartModelMapper.delete(cartModel);
                if (count < 1)
                {
                    logger.debug("购物车清空失败 accessId=" + vo.getAccessId());
                }
                else
                {
                    return true;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("清空购物车出错", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAllCart");
        }
        return false;
    }


    @Override
    public boolean modifyAttribute(MainVo vo, Integer cartId, Integer attributeId) throws LaiKeAPIException
    {
        try
        {
            if (cartId == null || attributeId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                User   user    = null;
                //获取当前购物车数据
                CartModel currentCart = new CartModel();
                currentCart.setId(cartId);
                currentCart = cartModelMapper.selectOne(currentCart);
                if (currentCart == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDGWCSJ, "未找到购物车数据");
                }
                //获取商品库存的信息
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(attributeId);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                if (confiGureModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDKCSJ, "未找到库存数据");
                }

                //获取用户购物车当前商品的所有规格数据
                CartModel cartModel = new CartModel();
                cartModel.setStore_id(vo.getStoreId());
                cartModel.setGoods_id(currentCart.getGoods_id());
                if (userObj != null)
                {
                    user = JSON.parseObject(userObj.toString(), User.class);
                    if (!StringUtils.isEmpty(user.getUser_id()))
                    {
                        cartModel.setUser_id(user.getUser_id());
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    cartModel.setToken(vo.getAccessId());
                }
                boolean isMerge = false;
                //sql参数
                Map<String, Object> parmaMap      = new HashMap<>(16);
                List<CartModel>     cartModelList = cartModelMapper.select(cartModel);
                for (CartModel cart : cartModelList)
                {
                    //剔除当前商品
                    if (cart.getId().equals(cartId))
                    {
                        continue;
                    }

                    //商品如果和原来规格相同则合并
                    if (!cart.getId().equals(cartId) && cart.getGoods_id().equals(currentCart.getGoods_id()))
                    {
                        if (cart.getSize_id().equals(attributeId + ""))
                        {
                            //虚拟商品相同规格不能合并
                            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(cart.getGoods_id());
                            if (productListModel.getCommodity_type().equals(ProductListModel.COMMODITY_TYPE.virtual))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_GWCZYCZGSPGG, "购物车中已存在该商品规格", "modifyAttribute");
                            }
                            //相同则合并
                            isMerge = true;
                            //合并数量
                            int goodsNum = cart.getGoods_num() + currentCart.getGoods_num();
                            //数量如果超出了总库存数量则强制修改为当前库存数量
                            if (goodsNum > confiGureModel.getNum())
                            {
                                goodsNum = confiGureModel.getNum();
                            }
                            parmaMap.put("cart_id", currentCart.getId());
                            parmaMap.put("Goods_num", goodsNum);
                            parmaMap.put("attributeId", attributeId);
                            cartModelMapper.updateCartById(parmaMap);
                            //删除原来商品
                            cartModel = new CartModel();
                            cartModel.setId(cart.getId());
                            int count = cartModelMapper.delete(cartModel);
                            if (count < 1)
                            {
                                logger.debug("购物车商品合并失败-删除失败 cartid=" + cart.getId());
                            }
                            else
                            {
                                return true;
                            }
                        }
                    }
                }
                if (!isMerge)
                {
                    //未作修改不动数据库
                    if (!currentCart.getSize_id().equals(attributeId + ""))
                    {
                        //不需要合并则直接修改商品属性
                        parmaMap = new HashMap<>(16);
                        parmaMap.put("cart_id", currentCart.getId());
                        parmaMap.put("attributeId", attributeId);
                        int count = cartModelMapper.updateCartById(parmaMap);
                        if (count < 1)
                        {
                            logger.debug("购物车商品属性修改失败 cartid=" + currentCart.getId());
                        }
                        else
                        {
                            return true;
                        }
                    }
                    else
                    {
                        logger.debug("数据为变化,无需操作 cartid=" + currentCart.getId());
                        return true;
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
            logger.error("修改购物车商品属性出错" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyAttribute");
        }
        return false;
    }



    @Override
    public List<Map<String, Object>> djAttribute(MainVo vo, int cartId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>(16);
        try
        {
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                if (cartId > 0)
                {
                    //会员折扣率
                    BigDecimal gradeRate = new BigDecimal("1");
                    if (!StringUtils.isEmpty(vo.getAccessId()))
                    {
                        //获取用户信息
                        user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                    }
                    //获取购物车数据
                    CartModel cartModel = new CartModel();
                    if (user == null)
                    {
                        cartModel.setToken(vo.getAccessId());
                    }
                    else
                    {
                        cartModel.setUser_id(user.getUser_id());
                    }
                    cartModel.setId(cartId);
                    cartModel = cartModelMapper.selectOne(cartModel);
                    if (cartModel != null)
                    {
                        //获取商品信息
                        ProductListModel productListModel = new ProductListModel();
                        productListModel.setId(cartModel.getGoods_id());
                        productListModel = productListModelMapper.selectByPrimaryKey(productListModel);
                        if (productListModel != null)
                        {
                            //获取商品配置信息
                            ConfiGureModel confiGureModel = new ConfiGureModel();
                            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                            confiGureModel.setPid(productListModel.getId());
                            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
                            //处理图片地址
                            for (ConfiGureModel confiGure : confiGureModelList)
                            {
                                String imgUrl = confiGure.getImg();
                                imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                                confiGure.setImg(imgUrl);
                            }
                            //获取商品规格弹出窗口插件数据
                            Map<String, Object> resultMap = GoodsDataUtils.getGoodsAttributeInfo(confiGureModelList, gradeRate);
                            resultList.add(resultMap);
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDSPSJ, "未找到商品数据", "djAttribute");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDGWCSJ, "未找到购物车数据", "djAttribute");
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "djAttribute");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录", "djAttribute");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("购物商品属性修改出错", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAllCart");
        }
        return resultList;
    }


    @Transactional(rollbackFor = Exception.class)

    @Override
    public boolean delcart(MainVo vo, String cartIds) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(vo.getAccessId()) && !StringUtils.isEmpty(cartIds))
            {
                String[] cartIdList = cartIds.split(",");
                //获取用户信息
                User   user = null;
                Object obj  = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                if (obj != null)
                {
                    user = JSON.parseObject(obj.toString(), User.class);
                }
                //循环删除购物车
                CartModel updateCartModel = new CartModel();
                for (String cartId : cartIdList)
                {
                    updateCartModel.setId(Integer.parseInt(cartId));
                    if (user != null)
                    {
                        updateCartModel.setUser_id(user.getUser_id());
                    }
                    else
                    {
                        updateCartModel.setToken(vo.getAccessId());
                    }
                    int count = cartModelMapper.delete(updateCartModel);
                    if (count < 1)
                    {
                        logger.debug("购物车删除失败 id = " + cartId);
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return false;
                    }
                }
                return true;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "delcart");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除购物车出错" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delcart");
        }
    }


}

