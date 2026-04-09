package com.laiketui.admins.admin.services.supplier;

import com.laiketui.admins.api.admin.supplier.AdminSupplierBrandService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 供应商商品分类管理
 *
 * @Author: sunH_
 * @Date: Create in 16:27 2022/9/15
 */
@Service
public class AdminSupplierBrandServiceImpl implements AdminSupplierBrandService
{

    private final Logger logger = LoggerFactory.getLogger(AdminSupplierBrandServiceImpl.class);

    @Autowired
    private SupplierBrandModelMapper supplierBrandModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private SupplierProClassModelMapper supplierProClassModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    /*@Override
    public Map<String, Object> auditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException {
        Map<String, Object> resultMap = new HashMap<>(16);
        try {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("page", vo.getPageNo());
            paramMap.put("pageSize", vo.getPageSize());
            if (!Objects.isNull(id)){
                paramMap.put("id", id);
            }
            if (StringUtils.isNotEmpty(condition)){
                paramMap.put("condition", condition);
            }
            if (!Objects.isNull(status)){
                paramMap.put("status", status);
            }
            if (StringUtils.isNotEmpty(startTime)){
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime)){
                paramMap.put("endTime", endTime);
            }
            int i = supplierBrandModelMapper.countCondition(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0){
                list = supplierBrandModelMapper.selectCondition(paramMap);
                for (Map<String, Object> map : list){
                    map.put("brand_time", DateUtil.dateFormate(MapUtils.getString(map, "brand_time"), GloabConst.TimePattern.YMDHMS));
                    String examine = MapUtils.getString(map, "examine");
                    String examineDesc = "";
                    if (examine.equals(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS)){
                        examineDesc = "待审核";
                    }else if (examine.equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS)){
                        examineDesc = "审核通过";
                    }else {
                        examineDesc = "审核不通过";
                    }
                    map.put("examineDesc", examineDesc);
                    map.put("brand_pic", publiceService.getImgPath(MapUtils.getString(map, "brand_pic"), vo.getStoreId()));
                    map.put("brand_image", publiceService.getImgPath(MapUtils.getString(map, "brand_image"), vo.getStoreId()));
                    //获取分类
                    String[] cidList = StringUtils.trim(MapUtils.getString(map, "categories"), ",").split(",");
                    List<String> classNames = new ArrayList<>();
                    for (String cid : cidList) {
                        ProductClassModel productClassModel = new ProductClassModel();
                        productClassModel.setCid(Integer.parseInt(cid));
                        productClassModel = productClassModelMapper.selectOne(productClassModel);
                        if (productClassModel != null) {
                            classNames.add(productClassModel.getPname());
                        }
                    }
                    map.put("categories", classNames);
                    map.put("categoriesName", StringUtils.stringImplode(classNames, SplitUtils.DH));
                }
            }
            resultMap.put("total", i);
            resultMap.put("list", list);
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }

        return resultMap;
    }*/

    @Override
    public Map<String, Object> auditList(MainVo vo, Integer id, String condition, Integer status, String startTime, String endTime) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("operator", "pt");
            if (!Objects.isNull(id))
            {
                paramMap.put("id", id);
            }
            if (StringUtils.isNotEmpty(condition))
            {
                paramMap.put("condition", condition);
            }
            if (!Objects.isNull(status))
            {
                paramMap.put("examine", status);
            }
            if (StringUtils.isNotEmpty(startTime))
            {
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                paramMap.put("endTime", endTime);
            }
            if (vo.getCountry_num() != null)
            {
                paramMap.put("country_num", vo.getCountry_num());
            }

            if (StringUtils.isNotEmpty(vo.getLang_code()))
            {
                paramMap.put("lang_code", vo.getLang_code());
            }

            int                       i    = brandClassModelMapper.countBrandClassInfo(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = brandClassModelMapper.getBrandClassInfo(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("brand_time", DateUtil.dateFormate(MapUtils.getString(map, "brand_time"), GloabConst.TimePattern.YMDHMS));
                    String examine     = MapUtils.getString(map, "examine", "");
                    String examineDesc = "";
                    if (examine.equals(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS))
                    {
                        examineDesc = "待审核";
                    }
                    else if (examine.equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
                    {
                        examineDesc = "审核通过";
                    }
                    else
                    {
                        examineDesc = "审核不通过";
                    }
                    map.put("examineDesc", examineDesc);
                    map.put("brand_pic", publiceService.getImgPath(MapUtils.getString(map, "brand_pic"), vo.getStoreId()));
                    map.put("brand_image", publiceService.getImgPath(MapUtils.getString(map, "brand_image"), vo.getStoreId()));
                    //获取分类
                    String[]     cidList    = StringUtils.trim(MapUtils.getString(map, "categories"), ",").split(",");
                    List<String> classNames = new ArrayList<>();
                    for (String cid : cidList)
                    {
                        if (StringUtils.isNotEmpty(cid))
                        {
                            ProductClassModel productClassModel = new ProductClassModel();
                            productClassModel.setCid(Integer.parseInt(cid));
                            productClassModel = productClassModelMapper.selectOne(productClassModel);
                            if (productClassModel != null)
                            {
                                classNames.add(productClassModel.getPname());
                            }
                        }
                    }
                    map.put("categories", classNames);
                    map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                    map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));
                    map.put("categoriesName", StringUtils.stringImplode(classNames, SplitUtils.DH));
                }
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
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }

        return resultMap;
    }

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    public void examine(MainVo vo, Integer id, Integer status, String remark) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel      = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String          text            = "";
            String          event           = "";
            BrandClassModel brandClassModel = brandClassModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(brandClassModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "供应商商品品牌信息不存在", "examine");
            }
            brandClassModel.setExamine(status);
            if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS))
            {
                if (StringUtils.isEmpty(remark))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "备注不能为空", "examine");
                }
                brandClassModel.setRemark(remark);
            }
            if (status.toString().equals(DictionaryConst.ExameStatus.EXAME_PASS_STATUS))
            {
                //判断品牌名称是否存在，如果已经有相同的名称的，直接改转为拒绝，并填充拒绝原因为为：品牌名称已存在
                BrandClassModel brandClassOld = new BrandClassModel();
                brandClassOld.setStore_id(vo.getStoreId());
                brandClassOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                brandClassOld.setBrand_name(brandClassModel.getBrand_name());
                brandClassOld.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
                int count = brandClassModelMapper.selectCount(brandClassOld);
                if (count > 0)
                {
                    brandClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS));
                    brandClassModel.setRemark("品牌名称重复，请勿重复添加");
                    brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
                    //消息通知
                    MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                    messageLoggingSave.setStore_id(vo.getStoreId());
                    messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_BRAND);
                    messageLoggingSave.setParameter(brandClassModel.getBrand_id() + "");
                    messageLoggingSave.setContent("品牌名称已存在");
                    messageLoggingSave.setAdd_date(new Date());
                    if (StringUtils.isNotEmpty(brandClassModel.getMch_id()))
                    {
                        messageLoggingSave.setMch_id(brandClassModel.getMch_id());
                    }
                    else
                    {
                        messageLoggingSave.setSupplier_id(brandClassModel.getSupplier_id());
                    }
                    messageLoggingModalMapper.insertSelective(messageLoggingSave);
                    throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_XTJCGPPMCYCZ, "系统检测该品牌名称已存在，已自动拒绝", "examine");
                }
                //判断所属分类是否存在
                String[] cidList = StringUtils.trim(brandClassModel.getCategories(), ",").split(",");
                for (String cid : cidList)
                {
                    ProductClassModel productClassModel = new ProductClassModel();
                    productClassModel.setCid(Integer.parseInt(cid));
                    productClassModel = productClassModelMapper.selectOne(productClassModel);
                    if (productClassModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PPSSFLBCZ, "品牌所属分类不存在", "examine");
                    }
                }
                //brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
                text = "商品品牌" + brandClassModel.getBrand_name() + "审核通过";
                event = "通过了品牌ID：" + brandClassModel.getBrand_id() + "，并且名称为：" + brandClassModel.getBrand_name() + "的审核";
            }
            else
            {
                text = "商品品牌" + brandClassModel.getBrand_name() + "审核拒绝";
                event = "拒绝了品牌ID：" + brandClassModel.getBrand_id() + "，并且名称为：" + brandClassModel.getBrand_name() + "的审核";
            }
            brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            //消息通知
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_BRAND);
            messageLoggingSave.setParameter(brandClassModel.getBrand_id() + "");
            messageLoggingSave.setContent(text);
            messageLoggingSave.setAdd_date(new Date());
            if (StringUtils.isNotEmpty(brandClassModel.getMch_id()))
            {
                messageLoggingSave.setMch_id(brandClassModel.getMch_id());
            }
            else
            {
                messageLoggingSave.setSupplier_id(brandClassModel.getSupplier_id());
            }
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("审核供应商商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }
    }

}
