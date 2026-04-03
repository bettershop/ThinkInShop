package com.laiketui.admins.admin.services;

import com.laiketui.admins.admin.consts.AdminStoreConst;
import com.laiketui.admins.api.admin.AdminIndexDubboService;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.AdminModelMapper;
import com.laiketui.common.mapper.ProLabelModelMapper;
import com.laiketui.common.mapper.ProductConfigModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ProductConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.admin.DefaultViewVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 首页
 *
 * @author Trick
 * @date 2020/12/28 14:05
 */
@Service
public class AdminIndexServiceImpl implements AdminIndexDubboService
{
    private final Logger logger = LoggerFactory.getLogger(AdminIndexServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PublicAdminService publicAdminService;

    @Override
    public Map<String, Object> home(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap.putAll(publicAdminService.index(vo, mchId));
        }
        catch (Exception e)
        {
            logger.error("商城首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "home");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> index(DefaultViewVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取商品信息
            Map<String, Object> goodsDataMap;
            Map<String, Object> map = new HashMap<>(16);
            map.put("page", vo.getPageNo());
            map.put("pagesize", vo.getPageSize());
            map.put("pageto", "");

            if (vo.getStatus() != null)
            {
                map.put("status", vo.getStatus());
            }
            else
            {
                //获取商品状态
                List<Integer> statusList = new ArrayList<>();
                statusList.add(DictionaryConst.GoodsStatus.NOT_GROUNDING);
                statusList.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
                statusList.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
                map.put("statusList", statusList);
            }
            if (vo.getCid() != null)
            {
                map.put("product_class", vo.getCid());
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                map.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                map.put("lang_code",language);
//            }

            if (vo.getBrandId() != null)
            {
                map.put("brand_id", vo.getBrandId());
            }
            if (StringUtils.isNotEmpty(vo.getProductTitle()) && StringUtils.isNotEmpty(vo.getMchName()))
            {
                map.put("mchNameOrGoodsName", vo.getMchName());
            }
            else
            {
                map.put("product_title", vo.getProductTitle());
                map.put("mch_name", vo.getMchName());
            }
            if (vo.getShowAdr() != null)
            {
                map.put("show_adr", vo.getShowAdr());
            }
            if (vo.getActive() != null)
            {
                map.put("active", vo.getActive());
            }
            if (vo.getGoodsTga() != null)
            {
                map.put("goodsTga", vo.getGoodsTga());
            }
            map.put("commodity_type", vo.getCommodityType());
            //只显示审核通过的
            map.put("mchStatus", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);

            //销量升序降序
            if (StringUtils.isNotEmpty(vo.getIsItDescendingOrder()))
            {
                map.put("IsItDescendingOrder", vo.getIsItDescendingOrder());
            }

            if (vo.getClassnotset() == 1)
            {
                map.put("classnotset", 1);
            }

            if (vo.getBrandnotset() == 1)
            {
                map.put("brandnotset", 1);
            }

            goodsDataMap = publicGoodsService.productList(vo.getStoreId(), user.getName(), 0, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT, map);

            if (vo.getExportType().equals(1))
            {
                exportGoodsData(DataUtils.cast(goodsDataMap.get("list")), response);
                return null;
            }
            // TODO: 2020/12/28 获取有那些插件暂时不做

            //获取字典信息
            Map<String, Object> goodsStatusMap = publicDictionaryService.getDictionaryByName("商品状态");
            Map<String, Object> goodsShowMap   = publicDictionaryService.getDictionaryByName("商品展示位置");

            //产品开关
            int                isOpen             = 0;
            int                isDisplaySellOut   = 0;
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null)
            {
                isOpen = productConfigModel.getIs_open();
                isDisplaySellOut = productConfigModel.getIs_display_sell_put();
            }
            //获取商城客户店铺
            Integer    mchId      = null;
            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(vo.getStoreId());
            adminModel.setType(AdminModel.TYPE_CLIENT);
            adminModel = adminModelMapper.selectOne(adminModel);
            if (adminModel != null)
            {
                mchId = adminModel.getShop_id();
            }

            resultMap.put("mch_id", mchId);
            resultMap.put("store_id", vo.getStoreId());
            resultMap.put("class_id", vo.getCid());
            resultMap.put("ctypes", mchId);
            resultMap.put("brand_id", vo.getBrandId());
            resultMap.putAll(goodsDataMap);
            resultMap.put("is_open", isOpen);
            resultMap.put("isDisplaySellOut", isDisplaySellOut);
            resultMap.put("select2", goodsStatusMap);
            resultMap.put("select3", goodsShowMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("后台首页", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    private void exportGoodsData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"商品编号", "商品标题", "商品分类", "品牌", "价格", "库存", "商品状态", "所属店铺", "销量", "显示位置", "上架时间", "发布时间"};
            //对应字段
            String[]     kayList = new String[]{"id", "product_title", "pname", "brand_name", "price", "num", "status_name", "name", "volume", "showName", "upper_shelf_time", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("商品列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(goodsList);
            vo.setResponse(response);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsData");
        }
    }

    @Override
    public Map<String, Object> goodsStatus(MainVo vo, String ids) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商城标签
            Integer proNewLabelId = proLabelModelMapper.getProLabelNew(vo.getStoreId());
            Integer proLabelTopId = proLabelModelMapper.getProLabelTop(vo.getStoreId());
            Integer proLabelHotId = proLabelModelMapper.getProLabelHot(vo.getStoreId());

            String[] goodsIds = ids.split(SplitUtils.DH);
            boolean  status   = true;
            boolean  xp       = true;
            boolean  rx       = true;
            boolean  tj       = true;
            for (String goodsId : goodsIds)
            {
                ProductListModel productListModel = new ProductListModel();
                productListModel.setStore_id(vo.getStoreId());
                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                productListModel.setId(Integer.parseInt(goodsId));
                productListModel = productListModelMapper.selectOne(productListModel);
                if (productListModel != null)
                {
                    if (DictionaryConst.ProductStatus.PRODUCTSTATUS_END_STATUS.toString().equals(productListModel.getStatus()))
                    {
                        status = false;
                    }
                    List<String> sTypes = Arrays.asList(productListModel.getS_type().split(SplitUtils.DH));
                    if (sTypes.contains(proNewLabelId + ""))
                    {
                        xp = false;
                    }
                    if (sTypes.contains(proLabelHotId + ""))
                    {
                        rx = false;
                    }
                    if (sTypes.contains(proLabelTopId + ""))
                    {
                        tj = false;
                    }
                }
            }
            resultMap.put("status", status);
            resultMap.put("xp", xp);
            resultMap.put("rx", rx);
            resultMap.put("tj", tj);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("商品状态信息" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "execute");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> isopen(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel         adminModel         = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int                status             = 0;
            int                count              = 0;
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            ProductConfigModel productConfigSave = new ProductConfigModel();
            productConfigSave.setStore_id(vo.getStoreId());
            if (productConfigModel != null)
            {
                productConfigSave.setId(productConfigModel.getId());
                productConfigSave.setIs_open(ProductConfigModel.NO_DISPLAY_BEAN_GOODS);
                if (ProductConfigModel.NO_DISPLAY_BEAN_GOODS.equals(productConfigModel.getIs_open()))
                {
                    productConfigSave.setIs_open(ProductConfigModel.DISPLAY_BEAN_GOODS);
                }
                count = productConfigModelMapper.updateByPrimaryKeySelective(productConfigSave);
            }
            else
            {
                productConfigSave.setIs_open(ProductConfigModel.DISPLAY_BEAN_GOODS);
                count = productConfigModelMapper.insertSelective(productConfigSave);
            }

            if (count > 0)
            {
                status = 1;
            }
            publiceService.addAdminRecord(vo.getStoreId(), "进行了是否显示已下架商品操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            resultMap.put("status", status);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否显示下架商品", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isopen");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void displaySellOut(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel         adminModel         = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int                count              = 0;
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            ProductConfigModel productConfigSave = new ProductConfigModel();
            productConfigSave.setStore_id(vo.getStoreId());
            if (productConfigModel != null)
            {
                productConfigSave.setId(productConfigModel.getId());
                productConfigSave.setIs_display_sell_put(ProductConfigModel.NO_DISPLAY_BEAN_GOODS);
                if (ProductConfigModel.NO_DISPLAY_BEAN_GOODS.equals(productConfigModel.getIs_display_sell_put()))
                {
                    productConfigSave.setIs_display_sell_put(ProductConfigModel.DISPLAY_BEAN_GOODS);
                }
                count = productConfigModelMapper.updateByPrimaryKeySelective(productConfigSave);
            }
            else
            {
                productConfigSave.setIs_display_sell_put(ProductConfigModel.DISPLAY_BEAN_GOODS);
                count = productConfigModelMapper.insertSelective(productConfigSave);
            }
            publiceService.addAdminRecord(vo.getStoreId(), "进行了显示已售罄商品操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否显示已售罄商品", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "displaySellOut");
        }
    }


    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private PubliceService publiceService;

}