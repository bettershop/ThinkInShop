package com.laiketui.admins.mch.services.plugin;

import com.laiketui.admins.api.mch.plugin.MchIntegralService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.IntegralGoodsModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.integral.AddIntegralConfigVo;
import com.laiketui.domain.vo.plugin.integral.AddIntegralVo;
import com.laiketui.domain.vo.sec.QueryProVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 积分商品列表【积分插件已拆分 2022-01-13 11:16:33】
 *
 * @author Trick
 * @date 2021/5/12 9:58
 */
@Service
@Deprecated
public class MchIntegralServiceImpl implements MchIntegralService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> index(MainVo vo, String goodsName, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //是否设置了积分商城,没有设置积分商城则默认添加一个
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(vo.getStoreId());
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            if (integralConfigModel == null)
            {
                integralConfigModel = new IntegralConfigModel();
                integralConfigModel.setStore_id(vo.getStoreId());
                integralConfigModelMapper.insertSelective(integralConfigModel);
            }
            //获取列表数据
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("product_title", goodsName);
            parmaMap.put("integralId", id);
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total      = integralGoodsModelMapper.countGoodsInfo1(parmaMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (total > 0)
            {
                resultList = integralGoodsModelMapper.getGoodsInfo1(parmaMap);
                for (Map<String, Object> map : resultList)
                {
                    map.put("imgurl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
                    //售价
                    String              goodsInfoStr = MapUtils.getString(map, "initial");
                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(goodsInfoStr, Map.class));
                    if (goodsInfoMap != null)
                    {
                        map.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                    }
                }
            }

            resultMap.put("list", resultList);
            resultMap.put("num", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("积分商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getProList(QueryProVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //必须要有库存
            parmaMap.put("stockNumGt", 0);
            //添加过的商品不显示
            parmaMap.put("not_exists_integral", "not_exists_integral");
            parmaMap.put("goodsStatus", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (!StringUtils.isEmpty(vo.getMyClass()))
            {
                parmaMap.put("product_class", vo.getMyClass());
            }
            if (!StringUtils.isEmpty(vo.getMyBrand()))
            {
                parmaMap.put("brand_id", vo.getMyBrand());
            }
            if (!StringUtils.isEmpty(vo.getProName()))
            {
                parmaMap.put("product_title", vo.getProName());
            }
            parmaMap.put("diy_sort", "diy_sort");
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());

            List<Map<String, Object>> resultGoodsList = new ArrayList<>();
            int                       count           = productListModelMapper.getProductListLeftJoinMchCountDynamic(parmaMap);
            List<Map<String, Object>> list            = new ArrayList<>();
            if (count > 0)
            {
                list = productListModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
                for (Map<String, Object> map : list)
                {
                    Map<String, Object> goodsMap = new HashMap<>(16);
                    int                 stockNum = Integer.parseInt(map.get("num").toString());
                    int                 goodsId  = MapUtils.getIntValue(map, "id");
                    int                 brandId  = MapUtils.getIntValue(map, "brand_id");
                    goodsMap.put("id", goodsId);
                    goodsMap.put("stockNum", stockNum);
                    goodsMap.put("imgUrl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
                    //获取分类名称
                    goodsMap.put("className", productClassModelMapper.getGoodsClassName(goodsId));
                    //获取品牌名称
                    BrandClassModel brandClassModel = brandClassModelMapper.selectByPrimaryKey(brandId);
                    goodsMap.put("brandName", brandClassModel.getBrand_name());
                    //售价
                    String              goodsInfoStr = MapUtils.getString(map, "initial");
                    Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(goodsInfoStr, Map.class));
                    if (goodsInfoMap != null)
                    {
                        goodsMap.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                    }
                    goodsMap.put("mchName", MapUtils.getString(map, "name"));
                    goodsMap.put("goodsName", MapUtils.getString(map, "product_title"));

                    resultGoodsList.add(goodsMap);
                }
            }

            resultMap.put("res", resultGoodsList);
            resultMap.put("total", count);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getProList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addIntegral(AddIntegralVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            int row;
            if (vo.getGoodsid() == null || vo.getGoodsid() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZZQDSP, "请选择正确的商品");
            }
            if (vo.getIntegral() == null || vo.getIntegral().compareTo(new BigDecimal("0")) <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHSXJFXDYL, "兑换所需积分需大于零");
            }
            IntegralGoodsModel integralGoodsOld = null;
            if (vo.getId() != null)
            {
                integralGoodsOld = integralGoodsModelMapper.selectByPrimaryKey(vo.getId());
                if (integralGoodsOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFSPBCZ, "积分商品不存在");
                }
            }
            ProductListModel productListOld = new ProductListModel();
            productListOld.setId(vo.getGoodsid());
            productListOld.setMch_id(admin.getShop_id());
            productListOld.setStore_id(vo.getStoreId());
            productListOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            productListOld = productListModelMapper.selectOne(productListOld);
            if (productListOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (!DictionaryConst.GoodsStatus.NEW_GROUNDING.toString().equals(productListOld.getStatus()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWSJ, "商品未上架");
            }

            //获取商品金额
            Map<String, Object> goodsInfo  = DataUtils.cast(SerializePhpUtils.getUnserializeObj(productListOld.getInitial(), Map.class));
            BigDecimal          goodsPrice = new BigDecimal(Objects.requireNonNull(MapUtils.getString(goodsInfo, "sj")));
            if (vo.getMoney().compareTo(goodsPrice) >= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DHJGXXYSPYBSJ, "兑换价格需小于商品原本售价");
            }
            IntegralGoodsModel integralGoodsSave = new IntegralGoodsModel();
            integralGoodsSave.setIntegral(vo.getIntegral().intValue());
            integralGoodsSave.setMoney(vo.getMoney());
            if (integralGoodsOld != null)
            {
                integralGoodsSave.setId(integralGoodsOld.getId());
                integralGoodsSave.setUpdate_time(new Date());
                row = integralGoodsModelMapper.updateByPrimaryKeySelective(integralGoodsSave);
            }
            else
            {
                IntegralGoodsModel integralGoodsModel = new IntegralGoodsModel();
                integralGoodsModel.setStore_id(vo.getStoreId());
                integralGoodsModel.setGoods_id(vo.getGoodsid());
                integralGoodsModel.setIs_delete(0);
                int count = integralGoodsModelMapper.selectCount(integralGoodsModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYTJ, "商品已添加");
                }
                //获取当前商品的预库存量
                int goodsNum = integralGoodsModelMapper.getGoodsStockNum(vo.getGoodsid(), integralGoodsModel.getAttr_id());
                int needNum  = vo.getStockNum() + goodsNum;
                //判断库存是否小于库存
                if (needNum > productListOld.getNum())
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPKCBZ, "列表有商品库存不足");
                }
                integralGoodsSave.setStore_id(vo.getStoreId());
                integralGoodsSave.setGoods_id(vo.getGoodsid());
                integralGoodsSave.setMax_num(needNum);
                integralGoodsSave.setNum(needNum);
                integralGoodsSave.setAdd_time(new Date());
                int maxSort = integralGoodsModelMapper.getMaxSort(vo.getStoreId());
                integralGoodsSave.setSort(maxSort + 1);
                row = integralGoodsModelMapper.insertSelective(integralGoodsSave);
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑积分商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addIntegral");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> top(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            IntegralGoodsModel integralGoodsUpdate = new IntegralGoodsModel();
            integralGoodsUpdate.setId(id);
            int maxSort = integralGoodsModelMapper.getMaxSort(vo.getStoreId());
            integralGoodsUpdate.setSort(maxSort + 1);
            integralGoodsModelMapper.updateByPrimaryKeySelective(integralGoodsUpdate);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "top");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> del(MainVo vo, String ids) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            List<String> idList = DataUtils.convertToList(ids.split(","));
            if (idList != null)
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("idList", idList);
                List<Map<String, Object>> goodsList = integralGoodsModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : goodsList)
                {
                    int id = Integer.parseInt(map.get("id").toString());
                    //判断该商品是否存在订单
                    int count = orderModelMapper.countUserUnfinishedOrderByGoodsId(vo.getStoreId(), id);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSPHYDDWWCBKSC, "此商品还有订单未完成,不可删除！");
                    }
                    IntegralGoodsModel integralGoodsModel = new IntegralGoodsModel();
                    integralGoodsModel.setId(id);
                    integralGoodsModel.setIs_delete(1);
                    count = integralGoodsModelMapper.updateByPrimaryKeySelective(integralGoodsModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
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
            logger.error("删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(vo.getStoreId());
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            resultMap.put("config", integralConfigModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取积分商城配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getConfigInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addConfigInfo(AddIntegralConfigVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                 row;
            IntegralConfigModel integralConfigSave = new IntegralConfigModel();
            integralConfigSave.setStore_id(vo.getStatus());
            integralConfigSave.setStatus(vo.getStatus());
            integralConfigSave.setBg_img(vo.getImgUrls());
            integralConfigSave.setContent(vo.getContent());
            if (vo.getIsFreeShipping() == 1)
            {
                if (vo.getGoodsNum() == null || vo.getGoodsNum() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRSPSL, "请输入商品数量");
                }
                integralConfigSave.setSame_piece(vo.getGoodsNum());
            }
            integralConfigSave.setPackage_settings(vo.getIsFreeShipping());

            if (vo.getAutoReceivingGoodsDay() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZDSHSJ, "请输入自动收货时间");
            }

            //发货提醒限制
            integralConfigSave.setDeliver_remind(vo.getDeliverRemind().intValue());
            //订单售后时间
            integralConfigSave.setOrder_after(DateUtil.dateConversion(vo.getReturnDay(), DateUtil.TimeType.DAY).intValue());
            //订单失效
            integralConfigSave.setOrder_failure(DateUtil.dateConversion(vo.getOrderInvalidTime(), DateUtil.TimeType.TIME).intValue());
            //自动收货
            integralConfigSave.setAuto_the_goods(DateUtil.dateConversion(vo.getAutoReceivingGoodsDay(), DateUtil.TimeType.DAY).intValue());
            //自动平评价
            integralConfigSave.setAuto_good_comment_day(DateUtil.dateConversion(vo.getAutoCommentDay(), DateUtil.TimeType.DAY).intValue());
            //设置购物赠送积分
            integralConfigSave.setProportion(vo.getProportion());
            integralConfigSave.setGive_status(vo.getGiveStatus());
            integralConfigSave.setOverdue_time(vo.getOverdueTime());

            IntegralConfigModel integralConfigOld = new IntegralConfigModel();
            integralConfigOld.setStore_id(vo.getStoreId());
            integralConfigOld = integralConfigModelMapper.selectOne(integralConfigOld);
            if (integralConfigOld != null)
            {
                integralConfigSave.setId(integralConfigOld.getId());
                row = integralConfigModelMapper.updateByPrimaryKeySelective(integralConfigSave);
            }
            else
            {
                row = integralConfigModelMapper.insertSelective(integralConfigSave);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取积分商城配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getConfigInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(MainVo vo, int proId, int num) throws LaiKeAPIException
    {
        try
        {
            IntegralGoodsModel integralGoodsOld = new IntegralGoodsModel();
            integralGoodsOld.setId(proId);
            integralGoodsOld.setStore_id(vo.getStoreId());
            integralGoodsOld.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
            integralGoodsOld = integralGoodsModelMapper.selectOne(integralGoodsOld);
            if (integralGoodsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFSPBCZ, "积分商品不存在");
            }

            //获取商品总库存
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(integralGoodsOld.getGoods_id());
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (DictionaryConst.ProductRecycle.RECOVERY.toString().equals(productListModel.getRecycle()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }

            //获取当前商品的预库存量
            int goodsNum = integralGoodsModelMapper.getGoodsStockNum(integralGoodsOld.getGoods_id(), integralGoodsOld.getAttr_id());
            //判断库存是否小于库存
            if (num > (productListModel.getNum() - goodsNum))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
            }
            //增减库存
            int count = integralGoodsModelMapper.addStockNum1(proId, num);

            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
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
    }
}

