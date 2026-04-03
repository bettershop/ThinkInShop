package com.laiketui.admins.admin.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.plugin.AdminSubtractionService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.BrandClassModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.SubtractionConfigModalMapper;
import com.laiketui.common.mapper.SubtractionModalMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
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
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.subtraction.SubtractionConfigModal;
import com.laiketui.domain.subtraction.SubtractionModal;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubConfigVo;
import com.laiketui.domain.vo.plugin.subtraction.AddSubtractionVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 满减后台实现
 *
 * @author Trick
 * @date 2021/5/12 17:12
 */
@Service
public class AdminSubtractionServiceImpl implements AdminSubtractionService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubtractionModalMapper subtractionModalMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private SubtractionConfigModalMapper subtractionConfigModalMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> index(MainVo vo, String title, Integer status, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("id", id);
            if (StringUtils.isEmpty(title))
            {
                parmaMap.put("title", title);
            }
            if (StringUtils.isEmpty(title))
            {
                parmaMap.put("status", status);
            }
            int                       total   = subtractionModalMapper.countDynamic(parmaMap);
            List<Map<String, Object>> subList = subtractionModalMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : subList)
            {
                int subId     = Integer.parseInt(map.get("id").toString());
                int subStatus = Integer.parseInt(map.get("status").toString());
                int subType   = StringUtils.stringParseInt(map.get("subtraction_type"));
                //活动如果被关闭则不管
                if (subStatus == 3)
                {
                    continue;
                }
                Date             startDate         = DateUtil.dateFormateToDate(map.get("starttime").toString(), GloabConst.TimePattern.YMDHMS);
                Date             endDate           = DateUtil.dateFormateToDate(map.get("endtime").toString(), GloabConst.TimePattern.YMDHMS);
                SubtractionModal subtractionUpdate = new SubtractionModal();
                subtractionUpdate.setId(subId);
                //判断活动是否已结束
                if (DateUtil.dateCompare(new Date(), endDate))
                {
                    //关闭活动
                    subtractionUpdate.setStatus(SubtractionModal.END);
                }
                else if (DateUtil.dateCompare(new Date(), startDate))
                {
                    //打开活动
                    subtractionUpdate.setStatus(SubtractionModal.STARTING);
                }
                else
                {
                    subtractionUpdate = null;
                }
                if (subtractionUpdate != null)
                {
                    subtractionModalMapper.updateByPrimaryKeySelective(subtractionUpdate);
                }
                //满减参数处理
                Map<String, Object> subMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("subtraction").toString(), Map.class));
                map.put("subtraction", subMap);
                String typeName = "未知";
                switch (subType)
                {
                    case 1:
                        typeName = "阶梯满减";
                        break;
                    case 2:
                        typeName = "循环满减";
                        break;
                    case 3:
                        typeName = "满赠";
                        break;
                    case 4:
                        typeName = "满件折扣";
                        break;
                    default:
                        break;
                }
                map.put("subtraction_type", typeName);
            }
            resultMap.put("list", subList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("满减活动列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> addSubtraction(AddSubtractionVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel       adminModel       = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int              row;
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(vo.getStoreId());
            SubtractionModal subtractionSave = new SubtractionModal();
            subtractionSave.setStore_id(vo.getStoreId());
            if (adminModel != null)
            {
                subtractionSave.setMch_id(adminModel.getShop_id());
            }
            SubtractionModal subtractionOld = new SubtractionModal();
            if (vo.getId() != null)
            {
                subtractionOld.setStore_id(vo.getStoreId());
                subtractionOld.setId(vo.getId());
                subtractionOld = subtractionModalMapper.selectOne(subtractionOld);
                if (subtractionOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJBCZ, "满减不存在");
                }
            }
            else
            {
                subtractionOld = null;
                if (StringUtils.isEmpty(vo.getTitle()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBTBNWK, "活动标题不能为空");
                }
                subtractionModal.setTitle(vo.getTitle());
                int count = subtractionModalMapper.selectCount(subtractionModal);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBTYCZ, "活动标题已存在");
                }
                if (StringUtils.isEmpty(vo.getName()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDMCBNWK, "活动名称不能为空");
                }
                subtractionModal.setTitle(null);
                subtractionModal.setName(vo.getName());
                count = subtractionModalMapper.selectCount(subtractionModal);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDMCYCZ, "活动名称已存在");
                }
                subtractionSave.setTitle(vo.getTitle());
                subtractionSave.setName(vo.getName());
            }

            if (StringUtils.isEmpty(vo.getImgurls()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJTPBNWK, "满减图片不能为空");
            }
            subtractionSave.setImage(vo.getImgurls());
            if (StringUtils.isEmpty(vo.getSubtractionRange()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJYYFWBNWK, "满减应用范围不能为空");
            }
            subtractionSave.setSubtraction_range(vo.getSubtractionRange() + "");
            if (StringUtils.isEmpty(vo.getSubtractionType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJYYFWBNWK, "满减应用范围不能为空");
            }
            subtractionSave.setSubtraction_type(vo.getSubtractionType());
            if (StringUtils.isEmpty(vo.getStartTime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJHDSXKSSJBNWK, "满减活动生效开始时间不能为空");
            }
            subtractionSave.setStarttime(DateUtil.dateFormateToDate(vo.getStartTime(), GloabConst.TimePattern.YMDHMS));
            if (StringUtils.isEmpty(vo.getEndTime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJHDSXJSSJBNWK, "满减活动生效结束时间不能为空");
            }
            subtractionSave.setEndtime(DateUtil.dateFormateToDate(vo.getEndTime(), GloabConst.TimePattern.YMDHMS));
            if (DateUtil.dateCompare(subtractionSave.getStarttime(), subtractionSave.getEndtime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJHDSXSJBZQ, "满减活动生效时间不正确");
            }
            if (DateUtil.dateCompare(new Date(), subtractionSave.getEndtime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSZMJHDSXJSSJ, "请设置满减活动生效结束时间");
            }
            //全场满减无需设置参数
            if (!vo.getSubtractionRange().equals(SubtractionRange.TWO))
            {
                if (StringUtils.isEmpty(vo.getMenuList()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJYYFWBNWK, "满减应用范围不能为空");
                }
                else
                {
                    String[]     ids    = vo.getMenuList().split(",");
                    List<String> idList = new ArrayList<>();
                    for (String id : ids)
                    {
                        if (vo.getSubtractionRange().equals(SubtractionRange.ONE))
                        {
                            //商品类别
                            idList.add(id);
                        }
                        else if (vo.getSubtractionRange().equals(SubtractionRange.THREE))
                        {
                            BrandClassModel brandClassModel = new BrandClassModel();
                            brandClassModel.setStore_id(vo.getStoreId());
                            brandClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                            brandClassModel.setBrand_name(id);
                            brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                            if (brandClassModel != null)
                            {
                                idList.add(brandClassModel.getBrand_id() + "");
                            }
                        }
                    }
                    subtractionSave.setSubtraction_parameter(StringUtils.stringImplode(idList, SplitUtils.DH));
                }
            }
            String subtraction = "";
            if (vo.getSubtractionType() == 1)
            {
                Map<Integer, Integer> reduceMap = JSON.parseObject(vo.getReduce(), new TypeReference<Map<Integer, Integer>>()
                {
                });
                for (Integer key : reduceMap.keySet())
                {
                    if (key <= 0 || reduceMap.get(key) <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJSZBZQ, "满减数值不正确");
                    }
                }
                subtraction = SerializePhpUtils.JavaSerializeByPhp(reduceMap);
            }
            subtractionSave.setSubtraction(subtraction);
            if (subtractionOld != null)
            {
                subtractionSave.setId(subtractionOld.getId());
                row = subtractionModalMapper.updateByPrimaryKeySelective(subtractionSave);
            }
            else
            {
                subtractionSave.setPosition_zfc("");
                row = subtractionModalMapper.insertSelective(subtractionSave);
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
            e.printStackTrace();
            logger.error("添加/编辑满减 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSubtraction");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> subtractionSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(vo.getStoreId());
            subtractionModal.setId(id);
            subtractionModal = subtractionModalMapper.selectOne(subtractionModal);
            if (subtractionModal == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            SubtractionModal subtractionUpdate = new SubtractionModal();
            subtractionUpdate.setId(id);
            if (subtractionModal.getStatus().equals(SubtractionModal.NOT_START))
            {
                subtractionUpdate.setStarttime(new Date());
                subtractionUpdate.setStatus(SubtractionModal.STARTING);
            }
            else
            {
                subtractionUpdate.setEndtime(new Date());
                subtractionUpdate.setStatus(SubtractionModal.END);
            }
            int row = subtractionModalMapper.updateByPrimaryKeySelective(subtractionUpdate);
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
            e.printStackTrace();
            logger.error("满减活动开始/结束 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "subtractionSwitch");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> del(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(vo.getStoreId());
            subtractionModal.setId(id);
            subtractionModal = subtractionModalMapper.selectOne(subtractionModal);
            if (subtractionModal == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在");
            }
            int row = subtractionModalMapper.deleteByPrimaryKey(id);
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
            e.printStackTrace();
            logger.error("删除满减活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "del");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getonfigInfo(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            SubtractionConfigModal subtractionConfigModal = new SubtractionConfigModal();
            subtractionConfigModal.setStore_id(vo.getStoreId());
            subtractionConfigModal = subtractionConfigModalMapper.selectOne(subtractionConfigModal);
            if (subtractionConfigModal != null)
            {
                resultMap.put("is_subtraction", subtractionConfigModal.getIs_subtraction());
                resultMap.put("range_zfc", SerializePhpUtils.getUnserializeObj(subtractionConfigModal.getRange_zfc(), Map.class));
                resultMap.put("pro_id", subtractionConfigModal.getPro_id());
                resultMap.put("is_shipping", subtractionConfigModal.getIs_shipping());
                resultMap.put("z_money", subtractionConfigModal.getZ_money());
                resultMap.put("address_id", subtractionConfigModal.getAddress_id());
                resultMap.put("mobile_terminal_image", subtractionConfigModal.getMobile_terminal_image());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取满减配置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getonfigInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getSubGoods(MainVo vo, Integer classId, Integer brandId, String title) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取之前商品id集
            SubtractionConfigModal subtractionConfigModal = new SubtractionConfigModal();
            subtractionConfigModal.setStore_id(vo.getStoreId());
            subtractionConfigModal = subtractionConfigModalMapper.selectOne(subtractionConfigModal);
            List<String> goodsIds = new ArrayList<>();
            if (subtractionConfigModal != null)
            {
                goodsIds = DataUtils.convertToList(subtractionConfigModal.getPro_id().split(","));
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE);
            parmaMap.put("stockNum", "stockNum");
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("status_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("addDate_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("product_title", title);
            parmaMap.put("notInGoodsId", goodsIds);
            parmaMap.put("classLeve", classId);
            parmaMap.put("brand_id", brandId);
            List<Map<String, Object>> goodsList = productListModelMapper.getProductListDynamic(parmaMap);
            int                       total     = productListModelMapper.countProductListDynamic(parmaMap);
            for (Map<String, Object> map : goodsList)
            {
                map.put("imgurl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
            }
            resultMap.put("product_list", goodsList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取满赠商品列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSubGoods");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addSubConfig(AddSubConfigVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                    row;
            SubtractionConfigModal subtractionSave = new SubtractionConfigModal();
            subtractionSave.setIs_subtraction(vo.getIsSubtraction());
            subtractionSave.setPro_id(vo.getProId());
            subtractionSave.setAdd_date(new Date());
            SubtractionConfigModal subtractionConfigOld = new SubtractionConfigModal();
            subtractionConfigOld.setStore_id(vo.getStoreId());
            subtractionConfigOld = subtractionConfigModalMapper.selectOne(subtractionConfigOld);
            if (subtractionConfigOld != null)
            {
                subtractionSave.setId(subtractionConfigOld.getId());
                row = subtractionConfigModalMapper.updateByPrimaryKeySelective(subtractionSave);
            }
            else
            {
                row = subtractionConfigModalMapper.insertSelective(subtractionSave);
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
            e.printStackTrace();
            logger.error("添加/编辑满减设置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSubConfig");
        }
        return resultMap;
    }
}

