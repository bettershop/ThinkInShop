package com.laiketui.apps.app.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.AppsCstrSearchService;
import com.laiketui.apps.api.products.AppsProductService;
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
import com.laiketui.domain.config.HotKeywordsModel;
import com.laiketui.domain.config.ProductConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.app.SearchCatVo;
import com.laiketui.domain.vo.app.SearchGoodsVo;
import com.laiketui.domain.vo.goods.GoodsSearchVo;
import com.laiketui.root.gateway.util.I18nUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 搜索实现
 *
 * @author Trick
 * @date 2020/10/15 12:51
 */
@Service
public class AppsCstrServiceImpl implements AppsCstrSearchService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrServiceImpl.class);

    @Autowired
    PubliceService publicService;

    @Autowired
    HotKeywordsModelMapper hotKeywordsModelMapper;

    @Autowired
    ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    MchModelMapper mchModelMapper;

    @Autowired
    ProductClassModelMapper productClassModelMapper;

    @Autowired
    UserBaseMapper userBaseMapper;

    @Autowired
    UserGradeModelMapper userGradeModelMapper;

    @Autowired
    ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private AppsProductService appsProductService;



    @Override
    public Map<String, Object> index(SearchCatVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //一级类目信息
        List<Map<String, Object>> oneList = new ArrayList<>();

        //查询商品并分类返回JSON
        ProductClassModel productClassModel = new ProductClassModel();

        // 搜索首页分类语种统一按前端 language 优先，其次 lang_code，最后回退中文
        String langCode = vo.getLanguage();
        if (StringUtils.isEmpty(langCode))
        {
            langCode = vo.getLang_code();
        }
        if (StringUtils.isEmpty(langCode))
        {
            langCode = GloabConst.Lang.CN;
        }
        vo.setLanguage(langCode);
        vo.setLang_code(langCode);
        productClassModel.setLang_code(langCode);

        if (vo.getCid() == null)
        {
            vo.setCid(0);
        }
        try
        {
            if (vo.getCid() != 0)
            {
                ProductClassModel productClassModelOld = new ProductClassModel();
                productClassModelOld.setCid(vo.getCid());
                productClassModelOld.setRecycle(0);
                productClassModelOld.setStore_id(vo.getStoreId());
                productClassModelOld.setLang_code(langCode);
                if (StringUtils.isNotEmpty(langCode))
                {
                    productClassModelOld.setLang_code(langCode);
                }
                productClassModelOld = productClassModelMapper.selectOne(productClassModelOld);
                resultMap.put("sid", productClassModelOld.getSid().toString());
            }
            else
            {
                //一级时，前端要求返空串
                resultMap.put("sid", "");
            }

            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setSid(vo.getCid());

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                productClassModel.setLang_code(language);
//            }

            //获取审核通过的类目
            productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            productClassModel.setNotset(0);
            List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
            //填装搜索商品所用的参数
            /*SearchGoodsVo searchGoodsVo = new SearchGoodsVo();
            BeanUtils.copyProperties(vo,searchGoodsVo);
            searchGoodsVo.setStoreId(vo.getStoreId());
            searchGoodsVo.setLanguage(vo.getLanguage());
            searchGoodsVo.setAccessId(vo.getAccessId());
            searchGoodsVo.setPageNo(1);
            searchGoodsVo.setPageSize(999999);*/
            //一级类目
            for (ProductClassModel productClass : productClassModelList)
            {
                //一级类目信息
                Map<String, Object>       oneMap      = new HashMap<>(16);
                List<Map<String, Object>> sonList     = new ArrayList<>(32);
                boolean                   ishaveChild = false;
                productClassModel = new ProductClassModel();
                productClassModel.setStore_id(productClass.getStore_id());
                productClassModel.setSid(productClass.getCid());
                productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                if (StringUtils.isNotEmpty(langCode))
                {
                    productClassModel.setLang_code(langCode);
                }
                List<ProductClassModel> productClassSecondList = productClassModelMapper.getProductClassLevel(productClassModel);

                if (productClassSecondList != null && !productClassSecondList.isEmpty())
                {

                    ishaveChild = true;
                    for (ProductClassModel productClassSecond : productClassSecondList)
                    {
                        //二级类目信息
                        Map<String, Object> sonMap = new HashMap<>(16);
                        //获取图片
                        String imgUrl = publicService.getImgPath(productClassSecond.getImg(), vo.getStoreId());
                        //类目id
                        sonMap.put("child_id", productClassSecond.getCid());
                        //分类名称
                        sonMap.put("name", productClassSecond.getPname());
                        //分类图片
                        sonMap.put("picture", imgUrl);
                        sonList.add(sonMap);
                    }
                }

                //一级类目图片
                String cimgurl = publicService.getImgPath(productClass.getImg(), vo.getStoreId());
                oneMap.put("cate_id", productClass.getCid());
                /*//需要根据id查找出一级类目下的商品，只有一级类目时使用这个
                if (productClass.getSid()==0) {
                    searchGoodsVo.setOnlyCid(productClass.getCid());
                }
                searchGoodsVo.setCid(productClass.getCid());
                oneMap.put("pro",this.searchDetail(searchGoodsVo).get("pro"));*/
                oneMap.put("cate_name", productClass.getPname());
                oneMap.put("ishaveChild", ishaveChild);
                oneMap.put("children", sonList);
                oneMap.put("cimgurl", cimgurl);
                oneList.add(oneMap);
            }
            //全部分类
            Map<String, Object> allClass = new HashMap<>(16);
            allClass.put("cate_id", "");
            allClass.put("cimgurl", "");
            //中英文
            String allClassName = I18nUtils.getRawMessage("app.search.class");
            allClass.put("cate_name", allClassName);
            allClass.put("children", oneList);
            boolean ishaveChild = false;
            if (oneList.size() > 0)
            {
                ishaveChild = true;
            }
            allClass.put("ishaveChild", ishaveChild);
            resultMap.put("allList", allClass);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("搜索首页出现异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSSYWLYC, "搜索首页网络异常", "index");
        }
        resultMap.put("List", oneList);
        return resultMap;
    }



    @Override
    public Map<String, Object> searchDetail(SearchGoodsVo vo)
    {
        Map<String, Object>       resultMap = new HashMap<>(16);
        List<Map<String, Object>> goodsList;
        //会员折扣
        BigDecimal gradeRate = new BigDecimal("1");
        //类目名称
        String pname = "";
        //商品品牌集
        List<BrandClassModel> brandClassList = new ArrayList<>();
        try
        {
            //产品状态
            List<Integer> goodsStatus = new ArrayList<>();
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            User user = null;
            if (!StringUtils.isEmpty(vo.getAccessId()))
            {
                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            }
            // 与 search 接口保持一致：登录用户取用户折扣，未登录取最低折扣
            gradeRate = publicService.getUserGradeRate(vo.getStoreId(), user, true);

            //获取产品配置 是否显示下架产品
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            //判断是否有店铺id
            if (vo.getShopId() != null) {
                parmaMap.put("shop_id", vo.getShopId());
            }
            //排序
//            if (!StringUtils.isEmpty(vo.getSortCriteria()))
//            {
//                String field = "";
//                switch (vo.getSortCriteria())
//                {
//                    case "price":
//                        field = "min(c.price)";
//                        break;
//                    case "comment_num":
//                        field = "a.comment_num";
//                        break;
//                    default:
//                        field = "volum";
//                }
//                parmaMap.put("sortCriteria_sort", field);
//                //排序类型
//                String sort = "asc";
//                if (!StringUtils.isEmpty(vo.getSort()))
//                {
//                    sort = vo.getSort();
//                }
//                parmaMap.put("sortCriteria_sort_type", sort);
//            }
            // 排序处理
            if (!StringUtils.isEmpty(vo.getSortCriteria())) {
                String sortField = "";
                String sortParam = "";  // 排序参数名

                switch (vo.getSortCriteria()) {
                    case "price":
                        sortField = "price";
                        sortParam = "sortCriteria_sort";
                        break;
                    case "comment_num":
                        sortField = "comment_num";
                        sortParam = "commentNum_sort";  // 关键修改：使用commentNum_sort参数
                        break;
                    default:
                        sortField = "volum";
                        sortParam = "sortCriteria_sort";
                }

                // 设置排序字段和类型
                parmaMap.put(sortParam, sortField);

                String sortType = vo.getSort() != null ? vo.getSort() : "asc";
                if ("comment_num".equals(vo.getSortCriteria())) {
                    // 评论数排序时，直接使用commentNum_sort参数传递排序类型
                    parmaMap.put("commentNum_sort", sortType);
                } else {
                    // 其他排序使用sortCriteria_sort_type
                    parmaMap.put("sortCriteria_sort_type", sortType);
                }
            }
            else
            {
                if (vo.getShopId() != null)
                {
                    parmaMap.put("mch_sort", DataUtils.Sort.DESC.toString());
                }
                else
                {
                    //默认排序
                    parmaMap.put("else_sort", DataUtils.Sort.DESC.toString());
                }
            }
            //价格区间
            if (!StringUtils.isEmpty(vo.getQueryCriteria()))
            {
                Map<String, Object> priceGroup = JSON.parseObject(vo.getQueryCriteria(), new TypeReference<Map<String, Object>>()
                {
                });
                String valueTemp = priceGroup.get("brand_id").toString();
                if (!StringUtils.isEmpty(valueTemp))
                {
                    parmaMap.put("brand_id", priceGroup.get("brand_id"));
                }
                valueTemp = priceGroup.get("min_price").toString();
                if (!StringUtils.isEmpty(valueTemp))
                {
                    parmaMap.put("min_price", priceGroup.get("min_price"));
                }
                valueTemp = priceGroup.get("max_price").toString();
                if (!StringUtils.isEmpty(valueTemp))
                {
                    parmaMap.put("max_price", priceGroup.get("max_price"));
                }
            }

            if (vo.getOnlyCid() != null)
            {
                parmaMap.put("onlyCid", vo.getCid());
            }
            parmaMap.put("cid", vo.getCid());
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("GoodsStatus", goodsStatus);
            parmaMap.put("keyword", vo.getKeyword());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("lang_code", vo.getLang_code());
//            parmaMap.put("country_num", vo.getCountry_num());
            parmaMap.put("notSupplierPro", "notSupplierPro");
            //只查询分类下的产品
            //parmaMap.put("show_adr",DictionaryConst.GoodsShowAdr.GOODSSHOWADR_CATEGORIES);
            if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
            {
                //不展示已售罄的商品
                parmaMap.put("stockNum", "stockNum");
            }
            goodsList = productListModelMapper.getProductListDynamic(parmaMap);
            for (Map<String, Object> map : goodsList)
            {
                int pid = Integer.parseInt(map.get("id") + "");
                Integer status = MapUtils.getInteger(map, "status");
                int stockNum;
                //获取商品库存
                stockNum = confiGureModelMapper.countConfigGureNum(pid);
                //获取商品标签
                String sType = MapUtils.getString(map, "s_type");
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));
                //如果商品为需要预约时间的则不能加入购物车，为1是可以加入购物车
                if (!map.containsKey("is_appointment") || MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin)
                {
                    map.put("isAddCar", 1);
                }

                Integer writeOffSettings = MapUtils.getInteger(map, "write_off_settings");
                if ((Objects.nonNull(writeOffSettings) && writeOffSettings == 1) || Objects.equals(status,DictionaryConst.GoodsStatus.OFFLINE_GROUNDING) || stockNum == 0)
                {
                    map.put("is_appointment", 2);
                    map.put("isAddCar", 2);
                }
                //图片url
                String imgUrl       = map.get("imgurl") + "";
                String logo         = map.get("logo") + "";
                String name         = map.get("name") + "";
                String mchName      = map.get("mch_name") + "";
                String color        = map.get("color") + "";
                String price        = map.get("price") + "";
                String ypriceStr    = map.get("yprice") + "";
                String productTitle = map.get("product_title") + "";

                BigDecimal yprice    = new BigDecimal(ypriceStr);
                BigDecimal salePrice = new BigDecimal(price);
                BigDecimal vipYprice = yprice;
                BigDecimal vipPrice  = salePrice.multiply(gradeRate);

                //获取图片
                imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                logo = publicService.getImgPath(logo, vo.getStoreId());
                //商品名称处理
                name = "null".equals(name) ? "" : name;
                color = "null".equals(color) ? "" : color;
                if (name.equals(color) || "默认".equals(name))
                {
                    name = "";
                }
                else
                {
                    name += color;
                }
                productTitle += name;

                map.put("name", productTitle);
                map.put("size", productTitle);
                map.put("mch_name", mchName);
                //原价
//                map.put("price", yprice);
                //售价
                map.put("price_yh", salePrice);
                map.put("vip_yprice", vipYprice);
                map.put("vip_price", vipPrice);
                map.put("imgurl", imgUrl);
                map.put("logo", logo);
                map.put("num", stockNum);
                map.put("contNum", stockNum);
                //商品标签
                //产品值属性 1：新品,2：热销，3：推荐
                String stype = MapUtils.getString(map, "s_type");
                map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(stype, SplitUtils.DH).split(SplitUtils.DH))));

                int payPeople = orderDetailsModelMapper.payPeople(vo.getStoreId(), pid);
                map.put("payPeople", payPeople);

                // 注入最高优先级营销活动参数
                Integer mchId = MapUtils.getInteger(map, "mchId", 0);
                String language = StringUtils.isNotEmpty(vo.getLanguage()) ? vo.getLanguage() : "zh_CN";
                Map<String, String> marketingParams = appsProductService.getTopMarketingParams(vo.getStoreId(), pid, language, mchId);
                if (marketingParams != null) {
                    map.put("marketingParams", marketingParams);
                }
            }
            Map<Integer, Object> tmpMap = new HashMap<>(16);
            parmaMap.remove("brand_id");
            //4152 【JAVA开发环境】分类：移动端--筛选--一开始是奶客，但是选中后再点进去变成了222
            // 获取所有品牌信息（不使用分页参数）
            Map<String, Object> brandParamMap = new HashMap<>(parmaMap);
            brandParamMap.remove("pageNo");     // 移除分页参数
            brandParamMap.remove("pageSize");   // 移除分页参数
            List<Map<String, Object>> brandGoodsList = productListModelMapper.getProductListDynamic(brandParamMap);
//            List<Map<String, Object>> brandGoodsList = productListModelMapper.getProductListDynamic(parmaMap);
            //填充品牌信息
            for (Map<String, Object> brandGoodsMap : brandGoodsList)
            {
                Integer brandId = null;
                Object brandIdObj = brandGoodsMap.get("brand_id");
                if (brandIdObj instanceof Number)
                {
                    brandId = ((Number) brandIdObj).intValue();
                }
                else
                {
                    String brandIdStr = MapUtils.getString(brandGoodsMap, "brand_id");
                    if (StringUtils.isNotEmpty(brandIdStr) && !"null".equalsIgnoreCase(brandIdStr))
                    {
                        try
                        {
                            brandId = Integer.parseInt(brandIdStr);
                        }
                        catch (Exception ignore)
                        {
                        }
                    }
                }
                if (brandId == null || brandId <= 0 || tmpMap.containsKey(brandId))
                {
                    continue;
                }
                //获取品牌信息
                BrandClassModel brandClassModel = new BrandClassModel();
                brandClassModel.setBrand_id(brandId);
                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                if (brandClassModel != null)
                {
                    brandClassList.add(brandClassModel);
                    tmpMap.put(brandId, brandClassModel);
                }
            }


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("搜索详情出现异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSXQWLYC, "搜索详情网络异常", "searchDetail");
        }

        resultMap.put("pro", goodsList);
        resultMap.put("grade", gradeRate);
        resultMap.put("brand_class_list", brandClassList);
        return resultMap;
    }



    @Override
    public Map<String, Object> hotSearch(MainVo vo, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //关键字集
        List<String> keyWordList = new ArrayList<>();
        if (type == null)
        {
            type = 0;
        }

        //关键词开关
        int isKeyWordOpen = 0;
        //产品配置上下架显示开关
        int isProductOpen = 0;
        //关键字上线个数
        int keyWordNum = 0;
        try
        {
            //商品状态 用于in
            List<Integer> goodsStatus = new ArrayList<>();

            //获取店铺关键词
            HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
            hotKeywordsModel.setStore_id(vo.getStoreId());
            hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
            if (hotKeywordsModel != null)
            {
                isKeyWordOpen = hotKeywordsModel.getIs_open();
                keyWordNum = hotKeywordsModel.getNum();
            }
            //获取店铺配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                isProductOpen = productConfigModel.getIs_open();
            }
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (isProductOpen > 0)
            {
                //需要显示下架商品
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }
            //判断热搜类型 0=商品关键词
            if (type == 0)
            {
                //是否开启了关键字
                if (isKeyWordOpen == 1)
                {
                    //提取关键字
                    keyWordList = Arrays.asList(hotKeywordsModel.getKeyword().split(","));
                    //判断关键字上限
                    if (keyWordList.size() > keyWordNum)
                    {
                        keyWordList = keyWordList.subList(0, keyWordNum);
                    }
                }
                else
                {
                    //获取被搜索最多的商品信息 显示6个
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("store_id", vo.getStoreId());
                    map.put("GoodsStatus", goodsStatus);
                    map.put("page", 0);
                    map.put("pageSize", 6);
                    List<ProductListModel> productListModels = productListModelMapper.getHotGoods(map);
                    for (ProductListModel productList : productListModels)
                    {
                        keyWordList.add(productList.getKeyword());
                    }
                }
            }
            else
            {
                //其它热搜类型 1=店铺关键词
                if (isKeyWordOpen == 1)
                {
                    //提取关键字
                    keyWordList = Arrays.asList(hotKeywordsModel.getMch_keyword().split(","));
                    //判断关键字上限
                    if (keyWordList.size() > keyWordNum)
                    {
                        keyWordList = keyWordList.subList(0, keyWordNum);
                    }
                }
                else
                {
                    //获取搜索最多的店铺信息 显示6个
                    Map<String, Object> map = new HashMap<>(16);
                    map.put("store_id", vo.getStoreId());
                    map.put("page", 0);
                    map.put("pageSize", 6);
                    List<MchModel> mchModelList = mchModelMapper.getHotMch(map);
                    for (MchModel mch : mchModelList)
                    {
                        keyWordList.add(mch.getName());
                    }
                }
            }

            //剔除重复的关键字 顺序不能发生变化
            List<String> keyList = new ArrayList<>();
            for (String key : keyWordList)
            {
                if (!keyList.contains(key))
                {
                    keyList.add(key);
                }
            }
            keyWordList = keyList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("热搜出现异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_RSWLYC, "热搜网络异常", "hotSearch");
        }

        resultMap.put("list", keyWordList);
        resultMap.put("is_open", isKeyWordOpen);
        return resultMap;
    }



    @Override
    public Map<String, Object> search(GoodsSearchVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);

        try
        {
            User user     = null;
            int  page     = (vo.getNum() - 1) * GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE;
            int  pageSize = GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE;
            //商品状态 用于in
            List<Integer> goodsStatus = new ArrayList<>();
            //产品配置上下架显示开关
            int isProductOpen = 0;
            //折扣
            BigDecimal gradeRate = new BigDecimal("1");

            //获取店铺配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                isProductOpen = productConfigModel.getIs_open();
            }
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (isProductOpen > 0)
            {
                //需要显示下架商品
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }
            //0=全部 1=热销 2=店铺
            if (vo.getType() == 0 || vo.getType() == 1)
            {
                //需登录的流程
                if (!StringUtils.isEmpty(vo.getAccessId()))
                {
                    //获取用户信息
                    user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
                }
                //获取未注册的折扣率 为了吸引顾客 用最低折扣
                gradeRate = publicService.getUserGradeRate(vo.getStoreId(), user, true);
                //获取搜索出来的品牌id
                Set<Integer> brandIdList = new HashSet<>();

                //处理前台传来的条件{"brand_id":"","min_price":"1","max_price":"2"}
                Map<String, Object> pcParmaMap = JSON.parseObject(vo.getQueryCriteria());
                pcParmaMap = pcParmaMap == null ? new HashMap<>(16) : pcParmaMap;
                if (vo.getType() == 1)
                {
                    //获取热销商品
                    pcParmaMap.put("s_type", proLabelModelMapper.getProLabelHot(vo.getStoreId()));
                }
                if (!StringUtils.isEmpty(vo.getQueryCriteria()))
                {
                    //品牌条件
                    String brandId  = "brand_id";
                    String minPrice = "min_price";
                    String maxPrice = "max_price";
                    if (pcParmaMap.containsKey(brandId))
                    {
                        pcParmaMap.put(brandId, pcParmaMap.get("brand_id").toString());
                    }
                    //金额条件
                    if (pcParmaMap.containsKey(minPrice))
                    {
                        pcParmaMap.put(minPrice, pcParmaMap.get("min_price").toString());
                    }
                    if (pcParmaMap.containsKey(maxPrice))
                    {
                        pcParmaMap.put(maxPrice, pcParmaMap.get("max_price").toString());
                    }
                }
                pcParmaMap.put("store_id", vo.getStoreId());
                pcParmaMap.put("keyword", vo.getKeyword());
                pcParmaMap.put("pageNo", page);
                pcParmaMap.put("pageSize", pageSize);
                if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
                {
                    pcParmaMap.put("stockNum", "stockNum");
                }
                //【排序】条件
                String volumeTj   = "volume";
                String price      = "price";
                String commentNum = "comment_num";
                if (volumeTj.equals(vo.getSortCriteria()))
                {
                    //销量
                    pcParmaMap.put("volume_sort", vo.getSort());
                }
                else if (price.equals(vo.getSortCriteria()))
                {
                    //价格
                    pcParmaMap.put("price_sort", vo.getSort());
                }
                else if (commentNum.equals(vo.getSortCriteria()))
                {
                    //评论数
                    pcParmaMap.put("commentNum_sort", vo.getSort());
                }
                else
                {
                    //默认
                    pcParmaMap.put("else_sort", "desc");
                }
                pcParmaMap.put("GoodsStatus", goodsStatus);
                pcParmaMap.put("notSupplierPro", "notSupplierPro");

                String langCode = vo.getLang_code();
                if (StringUtils.isNotEmpty(langCode))
                {
                    pcParmaMap.put("lang_code", langCode);
                }

                //获取符合条件的商品
                List<Map<String, Object>> searchGoodsList = productListModelMapper.getProductListDynamic(pcParmaMap);
                //商品搜索数量+1
                if (CollectionUtils.isNotEmpty(searchGoodsList))
                {
                    for (Map<String, Object> map : searchGoodsList)
                    {
                        int    goodsId = Integer.parseInt(map.get("id") + "");
                        String imgUrl  = map.get("imgurl").toString();
                        //店铺logo
                        String logoUrl = MapUtils.getString(map, "logo");
                        if (StringUtils.isNotEmpty(logoUrl))
                        {
                            logoUrl = publicService.getImgPath(logoUrl, vo.getStoreId());
                        }
                        //品牌id
                        Integer brandId = MapUtils.getInteger(map, "brand_id");
                        if (Objects.nonNull(brandId))
                        {
                            brandIdList.add(brandId);
                        }
                        //获取商品标签
                        String sType = MapUtils.getString(map, "s_type");
                        map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(sType, SplitUtils.DH).split(SplitUtils.DH))));
                        //原价格
                        BigDecimal vipYprice = new BigDecimal(map.get("yprice").toString());
                        //打折后的价格
                        BigDecimal vipPrice = new BigDecimal(map.get("price").toString());
                        //供应商商品划线价需要取建议零售价
                        String gongyingshang = MapUtils.getString(map, "gongyingshang");
                        if (!StringUtils.isEmpty(gongyingshang))
                        {
                            vipYprice = new BigDecimal(map.get("msrp").toString());
                        }
                        ProductListModel productListModel = new ProductListModel();
                        productListModel.setId(goodsId);
                        int count = productListModelMapper.updateAddSearchNum(productListModel);
                        if (count < 1)
                        {
                            logger.debug(goodsId + ">商品搜索量+1修改失败 search");
                        }
                        //获取图片url
                        imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                        //商品价格处理
                        if (gradeRate.intValue() != 1)
                        {
                            //折扣 原来价格 * 折扣 / 10 = 优惠价
                            vipPrice = vipPrice.multiply(gradeRate);
                        }
                        //获取商品库存数量
                        int stockNum = confiGureModelMapper.countConfigGureNum(goodsId);
                        //虚拟商品需要线下预约的商品不能加购物车
                        if (!map.containsKey("is_appointment") || (MapUtils.getIntValue(map, "is_appointment") != ProductListModel.IS_APPOINTMENT.isOpin))
                        {
                            map.put("isAddCar", 1);
                        }

                        Integer writeOffSettings = MapUtils.getInteger(map, "write_off_settings");
                        if (Objects.nonNull(writeOffSettings) && writeOffSettings == 1)
                        {
                            map.put("isAddCar", 1);
                            map.put("is_appointment",2);
                        }

                        map.put("num", stockNum);
                        //图片url
                        map.put("imgurl", imgUrl);
                        map.put("logo", logoUrl);
                        //原价
                        map.put("vip_yprice", vipYprice);
                        //折扣价
                        map.put("vip_price", vipPrice);

                        // 注入最高优先级营销活动参数
                        Integer mchId = MapUtils.getInteger(map, "mchId", 0);
                        String language = StringUtils.isNotEmpty(vo.getLanguage()) ? vo.getLanguage() : "zh_CN";
                        Map<String, String> marketingParams = appsProductService.getTopMarketingParams(vo.getStoreId(), goodsId, language, mchId);
                        if (marketingParams != null) {
                            map.put("marketingParams", marketingParams);
                        }
                    }
                }


                //查询出来的商品集
                resultMap.put("list", searchGoodsList);
                //查询出来的品牌信息
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("brandIdList", brandIdList);
                resultMap.put("brand_class_list", brandClassModelMapper.getBrandClassDynamic(parmaMap));
            }
            else
            {
                //店铺
                List<Map<String, Object>> mchTotalList = new ArrayList<>();
                String                    mchName      = vo.getKeyword();
                if (StringUtils.isEmpty(mchName))
                {
                    mchName = null;
                }
                //模糊获取店铺信息
                List<MchModel> mchModelList = mchModelMapper.getLikeMchByName(vo.getStoreId(), mchName, page, pageSize);
                for (MchModel mch : mchModelList)
                {
                    //获取店铺图片
                    String logo    = mch.getHead_img();
                    String logoUrl = publicService.getImgPath(logo, vo.getStoreId());
                    mch.setLogo(logoUrl);
                    //获取店铺商品信息
                    Map<String, Object> mchGoodsInfo      = publiceService.commodityInformation(vo.getStoreId(), mch.getId(),null);
                    int                 mchCollectionNum  = Integer.parseInt(mchGoodsInfo.get("collection_num").toString());
                    int                 quantityOnSale    = Integer.parseInt(mchGoodsInfo.get("quantity_on_sale").toString());
                    int                 mchGoodsSaleTotal = Integer.parseInt(mchGoodsInfo.get("quantity_sold").toString());

                    Map<String, Object> mchMap = JSON.parseObject(JSON.toJSONString(mch));
                    mchMap.put("quantity_on_sale", quantityOnSale);
                    mchMap.put("quantity_sold", mchGoodsSaleTotal);
                    mchMap.put("follow", mchCollectionNum);
                    mchTotalList.add(mchMap);
                }
                resultMap.put("list", mchTotalList);
            }

            //判断店铺插件是否开启
            boolean isOpenMch = publicService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.MCH, null);
            resultMap.put("mch_status", isOpenMch);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("热搜出现异常：", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSWLYC, "搜搜网络异常", "search");
        }
        return resultMap;
    }


    @Override
    public List<String> inputSearch(MainVo vo, Integer type, String keyword) throws LaiKeAPIException
    {
        //联想出来的商品名、商品类目、商品关键词
        List<String> searchNames = new ArrayList<>();
        try
        {
            type = type == null ? 0 : type;
            //产品配置开关
            int isProductOpen = 0;
            keyword = keyword.trim();

            //商品状态 用于in
            List<Integer> goodsStatus = new ArrayList<>();
            //获取店铺配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null && productConfigModel.getIs_open() != 0)
            {
                isProductOpen = productConfigModel.getIs_open();
            }
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (isProductOpen > 0)
            {
                //需要显示下架商品
                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            }
            //判断搜索类型 0 = 商品搜索
            if (type == 0)
            {
                //获取被搜索最多的商品名称信息 - 匹配商品名称
                Map<String, Object> map = new HashMap<>(16);
                map.put("store_id", vo.getStoreId());
                map.put("GoodsStatus", goodsStatus);
                map.put("product_title", keyword);
                //不包括供应商商品
                map.put("notSupplierPro", "notSupplierPro");
                String langCode = vo.getLang_code();
                if (StringUtils.isEmpty(langCode))
                {
                    map.put("lang_code", langCode);
                }
                List<ProductListModel> productListModels = productListModelMapper.getHotGoods(map);
                for (ProductListModel productList : productListModels)
                {
                    searchNames.add(productList.getProduct_title());
                }
                //获取被搜索最多的商品名称信息 - 匹配关键字
                map = new HashMap<>(16);
                map.put("store_id", vo.getStoreId());
                map.put("GoodsStatus", goodsStatus);
                map.put("keyword", keyword);
                //不包括供应商商品
                map.put("notSupplierPro", "notSupplierPro");
                productListModels = productListModelMapper.getHotGoods(map);
                for (ProductListModel productList : productListModels)
                {
                    searchNames.add(productList.getKeyword());
                }
                //获取被搜索最多的商品名称信息 - 匹配商品类目  不搜索这个  禅道 53891
/*                map = new HashMap<>(16);
                map.put("store_id", vo.getStoreId());
                map.put("pname", keyword);
                List<ProductClassModel> productClassModels = productClassModelMapper.getGoodsClass(map);
                for (ProductClassModel productClassModel : productClassModels) {
                    searchNames.add(productClassModel.getPname());
                }*/
            }
            else
            {
                //模糊商户搜索
                List<MchModel> mchModelList = mchModelMapper.getLikeMchByName(vo.getStoreId(), keyword, 0, 10);
                for (MchModel mch : mchModelList)
                {
                    searchNames.add(mch.getName());
                }
            }

            //去重
            searchNames = new ArrayList<>(new HashSet<>(searchNames));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("热搜出现异常：" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSLXWLYC, "搜索联想网络异常", "inputSearch");
        }

        return searchNames;
    }


}
