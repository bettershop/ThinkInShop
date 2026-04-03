package com.laiketui.admins.mch.services.plugin;

import com.laiketui.admins.api.mch.plugin.MchManageService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.mch.MchPromiseModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductImgModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.main.AddStoreConfigVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.user.WithdrawalVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 店铺管理实现
 *
 * @author Trick
 * @date 2021/1/26 11:31
 */
@Service
public class MchManageServiceImpl implements MchManageService
{
    private final Logger logger = LoggerFactory.getLogger(MchManageServiceImpl.class);

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> getMchInfo(MainVo vo, Integer id, Integer isOpen, String name, Integer promiseStatus) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (id == null)
            {
                //只显示已经审核通过的店铺
                parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);
            }
            if (promiseStatus != null)
            {
                //1=已缴
                parmaMap.put("promiseStatus", promiseStatus);
            }

            if (id != null)
            {
                parmaMap.put("id", id);
            }
            if (isOpen != null)
            {
                parmaMap.put("is_open", isOpen);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("mchName1", name);
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total   = mchModelMapper.countMchUserInfo(parmaMap);
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (total > 0)
            {
                listMap = mchModelMapper.getMchUserInfo(parmaMap);
                for (Map<String, Object> map : listMap)
                {
                    String logoUrl = MapUtils.getString(map, "logo");
                    map.put("logo", publiceService.getImgPath(logoUrl, vo.getStoreId()));

                    String businessLicense = MapUtils.getString(map, "business_license");
                    map.put("business_license", publiceService.getImgPath(businessLicense, vo.getStoreId()));

                    String promise = MapUtils.getString(map, "promiseStatus");
                    if (StringUtils.isNotEmpty(promise) && MchPromiseModel.PromiseConstant.STATUS_PAY.toString().equals(promise))
                    {
                        promise = "已交";
                    }
                    else
                    {
                        promise = "未交";
                    }
                    map.put("promiseStatus", promise);
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", listMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺列表信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyMchInfo(AddMchVo vo) throws LaiKeAPIException
    {
        try
        {
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(vo.getId());
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHBCZ, "商户不存在");
            }
            if (StringUtils.isEmpty(vo.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXDHBNWK, "联系电话不能为空");
            }
            if (StringUtils.isEmpty(vo.getConfines()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JYFWBNWK, "经营范围不能为空");
            }
            if (StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXDZBNWK, "详细地址不能为空");
            }
            MchModel mchModelUpdate = new MchModel();
            mchModelUpdate.setId(mchModel.getId());
            mchModelUpdate.setTel(vo.getTel());
            mchModelUpdate.setShi(vo.getShi());
            mchModelUpdate.setCpc(vo.getCpc());
            mchModelUpdate.setXian(vo.getXian());
            mchModelUpdate.setSheng(vo.getShen());
            mchModelUpdate.setAddress(vo.getAddress());
            mchModelUpdate.setShop_range(vo.getConfines());
            mchModelUpdate.setShop_information(vo.getMchInfo());
            mchModelUpdate.setShop_nature(vo.getNature());
            if (vo.getRoomid() != null && vo.getRoomid() > 0)
            {
                mchModelUpdate.setRoomid(vo.getRoomid());
                mchModelUpdate.setOld_roomid(mchModel.getRoomid());
            }
//            String apiKey = "";
//            String address = vo.getShen() + vo.getShi() + vo.getXian() + vo.getAddress();
//            //获取商城配置信息
//            ConfigModel configModel = new ConfigModel();
//            configModel.setStore_id(vo.getStoreId());
//            configModel = configModelMapper.selectOne(configModel);
//            if (configModel != null) {
//                apiKey = configModel.getTencent_key();
//            }
//            //更具地址获取坐标
//            try {
//                Map<String, String> latAndLng = TengxunMapUtil.getlatAndLng(apiKey, address);
//                mchModelUpdate.setLongitude(latAndLng.get("lng"));
//                mchModelUpdate.setLatitude(latAndLng.get("lat"));
//            } catch (LaiKeAPIException l) {
//                logger.error("获取地址经纬度错误 ", l);
//            }
            DataCheckTool.checkMchDataFormate(mchModelUpdate);
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModelUpdate);
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改商户信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyMchInfo");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delMchInfo(MainVo vo, int shopId) throws LaiKeAPIException
    {
        try
        {
            //注销店铺
            return publicMchService.cancellationShop(vo.getStoreId(), shopId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("注销 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMchInfo");
        }
    }

    @Override
    public Map<String, Object> getMchExamineInfo(MainVo vo, Integer id, Integer reviewStatus, String name, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (id != null)
            {
                parmaMap.put("id", id);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("mchName1", name);
            }
            if (reviewStatus != null)
            {
                parmaMap.put("review_status", reviewStatus);
            }
            else
            {
                parmaMap.put("not_examine", "not_examine");
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total   = mchModelMapper.countMchUserInfo(parmaMap);
            List<Map<String, Object>> listMap = mchModelMapper.getMchUserInfo(parmaMap);

            for (Map<String, Object> map : listMap)
            {
                int    examine = MapUtils.getIntValue(map, "review_status");
                String examineName;
                switch (examine)
                {
                    //0.待审核 1.审核通过 2.审核不通过
                    case 0:
                        examineName = "待审核";
                        break;
                    case 1:
                        examineName = "审核通过";
                        break;
                    case 2:
                        examineName = "审核不通过";
                        break;
                    default:
                        examineName = "-";
                }
                map.put("examineName", examineName);
                map.put("logo", publiceService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId()));
            }

            if (vo.getExportType() == 1)
            {
                exportMchExamineData(listMap, response);
                return null;
            }

            resultMap.put("total", total);
            resultMap.put("list", listMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商户审核信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchExamineInfo");
        }
        return resultMap;
    }

    //导出 店铺审核列表
    private void exportMchExamineData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"店铺ID", "店铺信息", "联系人", "联系电话", "申请/审核时间", "审核状态"};
            //对应字段
            String[]     kayList = new String[]{"id", "shop_information", "realname", "tel", "add_time", "examineName"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("店铺审核列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出 店铺审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportMchExamineData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean examineMch(MainVo vo, int mchId, Integer reviewStatus, String text) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //获取商城信息
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCBCZ, "商城不存在");
            }
            //获取商户信息
            MchModel mchModel = new MchModel();
            mchModel.setId(mchId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHBCZ, "商户不存在");
            }
            MchModel mchModelUpdate = new MchModel();
            mchModelUpdate.setId(mchModel.getId());
            mchModelUpdate.setLogo(mchConfigModel.getLogo());
            if (reviewStatus != null && reviewStatus.equals(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS))
            {
                mchModelUpdate.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            }
            else if (reviewStatus != null && reviewStatus.equals(DictionaryConst.MchExameStatus.EXAME_NOT_PASS_STATUS))
            {
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJLYBNWK, "拒绝理由不能为空");
                }
                mchModelUpdate.setReview_result(text);
                mchModelUpdate.setReview_status(DictionaryConst.MchExameStatus.EXAME_NOT_PASS_STATUS.toString());
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            // TODO: 2021/1/26 审核结果推送

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商户审核/拒绝 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examineMch");
        }
    }

    @Override
    public Map<String, Object> getGoodsExamineInfo(MainVo vo, String mchName, String goodsName, Integer goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pagesize", vo.getPageSize());
            parmaMap.put("pageto", "");
            parmaMap.put("mchStatus", DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
            if (!StringUtils.isEmpty(goodsName))
            {
                parmaMap.put("product_title", goodsName);
            }
            if (!StringUtils.isEmpty(mchName))
            {
                parmaMap.put("mch_name", mchName);
            }
            if (goodsId != null)
            {
                parmaMap.put("goodsId", goodsId);
            }
            resultMap = publicGoodsService.productList(vo.getStoreId(), null, 0, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT, parmaMap);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商户审核信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsExamineInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsDetailInfo(MainVo vo, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商品信息
            List<Map<String, Object>> goodsInfoList = productListModelMapper.selectGoodsDetailInfo(vo.getStoreId(), goodsId);
            //获取商品图片
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            List<ProductImgModel> productImgModelList = productImgModelMapper.select(productImgModel);
            List<String>          goodsImageUrls      = new ArrayList<>();
            for (ProductImgModel productImg : productImgModelList)
            {
                String imgUrl = publiceService.getImgPath(productImg.getProduct_url(), vo.getStoreId());
                goodsImageUrls.add(imgUrl);
            }
            //获取规格信息
            List<Map<String, Object>> confiGureList = new ArrayList<>();
            //获取所有规格
            List<String> attrAllList = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsId);
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
            for (ConfiGureModel confiGure : confiGureModelList)
            {
                Map<String, Object> confiGureMap = new HashMap<>(16);
                //属性处理
                String              cfgAttribute = confiGure.getAttribute();
                Map<String, Object> attributeMap = SerializePhpUtils.getUnserializeObj(cfgAttribute, Map.class);
                if (attributeMap != null)
                {
                    //a:2:{s:12:"颜色_LKT_1";s:14:"白色_LKT_124";s:12:"尺码_LKT_8";s:7:"M_LKT_9";}
                    for (String key : attributeMap.keySet())
                    {
                        String attribyteKey   = key;
                        String attribyteValue = attributeMap.get(key) + "";
                        int    index          = attribyteKey.indexOf("_LKT_");
                        if (index > 0)
                        {
                            //属性名称
                            attribyteKey = attribyteKey.substring(0, attribyteKey.indexOf("_LKT_"));
                            //属性值
                            attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT_"));
                        }
                        if (!attrAllList.contains(attribyteKey))
                        {
                            attrAllList.add(attribyteKey);
                        }
                        confiGureMap.put(attribyteKey, attribyteValue);
                    }
                }
                confiGureMap.put("imgurl", publiceService.getImgPath(confiGure.getImg(), vo.getStoreId()));
                confiGureMap.put("cid", confiGure.getId());
                confiGureMap.put("costprice", confiGure.getCostprice());
                confiGureMap.put("yprice", confiGure.getYprice());
                confiGureMap.put("price", confiGure.getPrice());
                confiGureMap.put("stockNum", confiGure.getNum());
                confiGureMap.put("unit", confiGure.getUnit());
                confiGureList.add(confiGureMap);
            }

            resultMap.put("goodsInfo", goodsInfoList);
            resultMap.put("goodsImageUrls", goodsImageUrls);
            //动态字段名称
            resultMap.put("attrList", attrAllList);
            resultMap.put("configureList", confiGureList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品详情信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsDetailInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean goodsExamine(MainVo vo, int goodsId, int status, String text) throws LaiKeAPIException
    {
        try
        {
            //获取商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(vo.getStoreId());
            productListModel.setId(goodsId);
            productListModel = productListModelMapper.selectOne(productListModel);
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //修改商品状态
            ProductListModel productListModelUpdate = new ProductListModel();
            productListModelUpdate.setId(productListModel.getId());
            if (status == 1)
            {
                //获取属性信息,启用属性
                Set<Integer>   attrIdList     = new HashSet<>();
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setPid(productListModel.getId());
                confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
                for (ConfiGureModel confiGure : confiGureModelList)
                {
                    String              attributeStr = confiGure.getAttribute();
                    Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attributeStr, Map.class));
                    if (attributeMap != null)
                    {
                        //a:2:{s:12:"颜色_LKT_1";s:14:"白色_LKT_124";s:12:"尺码_LKT_8";s:7:"M_LKT_9";}
                        for (String key : attributeMap.keySet())
                        {
                            String attribyteKeyId   = key;
                            String attribyteValueId = attributeMap.get(key) + "";
                            int    indexKey         = attribyteKeyId.lastIndexOf("_") + 1;
                            int    indexValue       = attribyteValueId.lastIndexOf("_") + 1;
                            if (indexKey > 0)
                            {
                                //属性id
                                attribyteKeyId = attribyteKeyId.substring(indexKey);
                                attrIdList.add(Integer.parseInt(attribyteKeyId));
                            }
                            if (indexValue > 0)
                            {
                                //属性值id
                                attribyteValueId = attribyteValueId.substring(indexValue);
                                attrIdList.add(Integer.parseInt(attribyteValueId));
                            }
                        }
                    }
                }
                //获取未生效的属性
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("attrIdList", attrIdList);
                parmaMap.put("status", SkuModel.SKU_STATUS_INVALID);
                List<Map<String, Object>> notTakeEffectSkuList = skuModelMapper.getAttributeDynamic(parmaMap);
                //属性生效
                for (Map<String, Object> map : notTakeEffectSkuList)
                {
                    Integer  attrId   = Integer.parseInt(String.valueOf(map.get("id")));
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(attrId);
                    skuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    skuModelMapper.updateByPrimaryKeySelective(skuModel);
                }
                logger.debug("生效属性数量:{}", notTakeEffectSkuList.size());
                productListModelUpdate.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                productListModelUpdate.setUpper_shelf_time(new Date());
                productListModelUpdate.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
                logger.debug("商品id:{} 审核通过", goodsId);
            }
            else
            {
                //审核拒绝
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJLYBNWK, "拒绝理由不能为空");
                }
                productListModelUpdate.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                productListModelUpdate.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_NOT_PASS_STATUS.toString());
                productListModelUpdate.setRefuse_reasons(text);
                logger.debug("商品id:{} 审核拒绝", goodsId);
            }
            int count = productListModelMapper.updateByPrimaryKeySelective(productListModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "goodsExamine");
        }
    }

    @Override
    public Map<String, Object> getWithdrawalInfo(WithdrawalVo vo, Integer status, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);
            if (status != null)
            {
                parmaMap.put("status", status);
            }
            parmaMap.put("is_mch", 1);

            if (vo.getStatus() != null)
            {
                parmaMap.put("status", vo.getStatus());
            }
            if (vo.getWid() != null && vo.getWid() > 0)
            {
                parmaMap.put("wid", vo.getWid());
            }
            if (!StringUtils.isEmpty(vo.getMchName()))
            {
                parmaMap.put("like_mchName", vo.getMchName());
            }
            if (!StringUtils.isEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total    = withdrawModelMapper.countWithdrawLeftMchBank(parmaMap);
            List<Map<String, Object>> dataList = withdrawModelMapper.getWithdrawLeftMchBank(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                int    examine = MapUtils.getIntValue(map, "status");
                String examineName;
                switch (examine)
                {
                    //0：审核中 1：审核通过 2：拒绝
                    case 0:
                        examineName = "审核中";
                        break;
                    case 1:
                        examineName = "审核通过";
                        break;
                    case 2:
                        examineName = "拒绝";
                        break;
                    default:
                        examineName = "-";
                }
                map.put("examineName", examineName);
            }
            if (vo.getExportType() == 1)
            {
                exportWithdrawalData(dataList, status == null, response);
                return null;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWithdrawalExamineInfo");
        }
        return resultMap;
    }

    //导出 提现审核列表
    private void exportWithdrawalData(List<Map<String, Object>> list, boolean flag, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"店铺", "联系电话", "状态", "申请时间", "提现金额", "提现手续费", "持卡人姓名", "银行名称", "支行名称", "卡号"};
            //对应字段
            String[] kayList = new String[]{"mch_name", "mobile", "examineName", "add_date", "money", "s_charge", "Cardholder", "Bank_name", "branch", "Bank_card_number"};
            if (flag)
            {
                List<String> headers = new ArrayList<>();
                Collections.addAll(headers, headerList);
                headers.add("备注");
                headerList = headers.toArray(new String[0]);

                List<String> kay = new ArrayList<>();
                Collections.addAll(kay, kayList);
                kay.add("refuse");
                kayList = kay.toArray(new String[0]);
            }

            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("提现审核列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportWithdrawalData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean withdrawalExamineMch(MainVo vo, int id, int status, String text) throws LaiKeAPIException
    {
        try
        {
            User   user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            String logoText = "店铺%s提现了%s";
            String logoText1;
            //获取提现申请信息
            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setId(id);
            withdrawModel.setStore_id(vo.getStoreId());
            withdrawModel.setStatus(WithdrawModel.EXAME_WAIT_STATUS.toString());
            withdrawModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            withdrawModel = withdrawModelMapper.selectOne(withdrawModel);
            if (withdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSQBCZ, "提现申请不存在");
            }
            //系统推送
            String tuiTitle   = "店铺提现成功!";
            String tuiContext = String.format("您提现的%s元已到账,快去看看吧!", withdrawModel.getMoney());
            //获取提现用户店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setUser_id(withdrawModel.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHMXWDP, "用户名下无店铺");
            }
            //操作记录
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(vo.getStoreId());
            recordModel.setUser_id(withdrawModel.getUser_id());
            recordModel.setMoney(withdrawModel.getMoney());
            recordModel.setOldmoney(mchModel.getCashable_money());
            recordModel.setEvent(String.format(logoText, withdrawModel.getUser_id(), withdrawModel.getMoney()));
            recordModel.setType(21);
            recordModel.setIs_mch(1);
            recordModel.setAdd_date(new Date());
            int count = recordModelMapper.insertSelective(recordModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            //入驻商户账户收支记录表
            MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
            mchAccountLogModel.setStore_id(vo.getStoreId());
            mchAccountLogModel.setMch_id(mchModel.getId().toString());
            mchAccountLogModel.setPrice(withdrawModel.getMoney());
            mchAccountLogModel.setAccount_money(mchModel.getAccount_money());
            mchAccountLogModel.setStatus(MchAccountLogModel.AccountLogStatus.OUT_ACCOUNT);
            mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_WITHDRAWAL);
            mchAccountLogModel.setAddtime(new Date());
            count = mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            //提现状态修改
            WithdrawModel withdrawModelUpdate = new WithdrawModel();
            withdrawModelUpdate.setId(withdrawModel.getId());
            if (WithdrawModel.EXAME_PASS_STATUS.equals(status))
            {
                withdrawModelUpdate.setStatus(WithdrawModel.EXAME_PASS_STATUS.toString());
                logoText1 = "提现id为" + withdrawModel.getId() + "的提现已通过";
            }
            else
            {
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJYYBNWK, "拒绝原因不能为空");
                }
                withdrawModelUpdate.setStatus(WithdrawModel.EXAME_NOT_PASS_STATUS.toString());
                withdrawModelUpdate.setRefuse(text);
                logoText1 = "提现id为" + withdrawModel.getId() + "的提现已拒绝";
                tuiTitle = "店铺提现失败!";
                tuiContext = String.format("您%s申请的提现被驳回!驳回原因:%s", DateUtil.dateFormate(withdrawModel.getAdd_date(), GloabConst.TimePattern.YMDHMS), text);
                //拒绝提现,还到钱包
                count = mchModelMapper.refuseWithdraw(mchModel.getId(), withdrawModel.getMoney());
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                }
            }
            count = withdrawModelMapper.updateByPrimaryKeySelective(withdrawModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), withdrawModel.getUser_id(), logoText1, AdminRecordModel.Type.PASS_OR_REFUSE, AdminRecordModel.Source.PC_SHOP, user.getMchId());

            //h5店铺消息通知
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(mchModel.getId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url("");
            messageLoggingSave.setParameter(tuiTitle);
            messageLoggingSave.setContent(tuiContext);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //pc店铺消息通知
            messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(mchModel.getId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setParameter(withdrawModelUpdate.getId() + "");
            messageLoggingSave.setContent(tuiContext);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺提现审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawalExamineMch");
        }
    }

    @Override
    public Map<String, Object> getStoreConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User           user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());
            if (mchConfigModel != null)
            {
                String logoUrl = publiceService.getImgPath(mchConfigModel.getLogo(), vo.getStoreId());
                mchConfigModel.setLogo(logoUrl);
                mchConfigModel.setHead_img(publiceService.getImgPath(mchConfigModel.getHead_img(), vo.getStoreId()));
                mchConfigModel.setPoster_img(publiceService.getImgPath(mchConfigModel.getPoster_img(), vo.getStoreId()));
            }

            resultMap.put("data", mchConfigModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商城设置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreConfigInfo");
        }
        return resultMap;
    }

    @Override
    public boolean setStoreConfigInfo(AddStoreConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(vo.getLogiUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRTXBNWK, "默认头像不能为空");
            }
            if (vo.getOutDayDel() == null || vo.getOutDayDel() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCTSBNWK, "删除天数不能为空");
            }
            if (vo.getMinWithdrawalMoney() == null || vo.getMinWithdrawalMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNWK, "最小提现金额不能为空");
            }
            if (StringUtils.isEmpty(vo.getHeadImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "头像不能为空");
            }
            if (StringUtils.isEmpty(vo.getPosterImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "宣传图不能为空");
            }
            if (vo.getMaxWithdrawalMoney() == null || vo.getMaxWithdrawalMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDTXJEBNWK, "最大提现金额不能为空");
            }
            else if (vo.getServiceCharge().doubleValue() >= 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXFWDYXYDXS, "手续费为大于0小于1的小数");
            }
            if (vo.getMinWithdrawalMoney().doubleValue() > vo.getMaxWithdrawalMoney().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNDYZDTXJE, "最小提现金额不能大于最大提现金额");
            }
            if (StringUtils.isEmpty(vo.getUploadType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCFSBNWK, "上传方式不能为空");
            }
            int            count;
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());

            MchConfigModel mchConfigModelSave = new MchConfigModel();
            mchConfigModelSave.setStore_id(vo.getStoreId());
            mchConfigModelSave.setIs_display(vo.getIsOpenPlugin());
            mchConfigModelSave.setLogo(ImgUploadUtils.getUrlImgByName(vo.getLogiUrl(), true));
            mchConfigModelSave.setHead_img(ImgUploadUtils.getUrlImgByName(vo.getHeadImg(), true));
            mchConfigModelSave.setPoster_img(ImgUploadUtils.getUrlImgByName(vo.getPosterImg(), true));
            mchConfigModelSave.setMin_charge(vo.getMinWithdrawalMoney());
            mchConfigModelSave.setMax_charge(vo.getMaxWithdrawalMoney());
            mchConfigModelSave.setService_charge(vo.getServiceCharge());
            mchConfigModelSave.setCommodity_setup(vo.getUploadType());
            mchConfigModelSave.setIllustrate(vo.getIllustrate());
            mchConfigModelSave.setPromise_switch(vo.getPromiseSwitch());
            //保证金
            if (vo.getPromiseSwitch().equals(1))
            {
                if (BigDecimal.ZERO.compareTo(vo.getPromiseAmt()) >= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRBZJ, "请输入保证金");
                }
                mchConfigModelSave.setPromise_amt(vo.getPromiseAmt());
                mchConfigModelSave.setPromise_text(vo.getPromiseText());
            }


            if (mchConfigModel != null)
            {
                mchConfigModelSave.setId(mchConfigModel.getId());
                count = mchConfigModelMapper.updateByPrimaryKeySelective(mchConfigModelSave);
            }
            else
            {
                mchConfigModelSave.setMch_id(user.getMchId());
                count = mchConfigModelMapper.insertSelective(mchConfigModelSave);
            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改 商城配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreConfigInfo");
        }
    }

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

}

