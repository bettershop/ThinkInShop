package com.laiketui.admins.mch.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.mch.plugin.MchGroupService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.PubliceGroupService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.group.GroupConfigModel;
import com.laiketui.domain.group.GroupOpenModel;
import com.laiketui.domain.group.GroupProductModel;
import com.laiketui.domain.plugin.group.GoGroupOrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.group.AddGroup;
import com.laiketui.domain.vo.plugin.group.AddGroupConfigVo;
import com.laiketui.domain.vo.plugin.group.QueryOpenGroupVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼团后台管理实现
 *
 * @author Trick
 * @date 2021/5/8 11:46
 */
@Service
public class MchGroupServiceImpl implements MchGroupService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GoGroupOrderModelMapper goGroupOrderModelMapper;

    @Autowired
    private GroupProductModelMapper groupProductModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PubliceGroupService publiceGroupService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private GroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private GroupConfigModelMapper groupConfigModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String goodsName, Integer status) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取拼团活动列表信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("g_status", status);
            parmaMap.put("product_title", goodsName);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("group_activity_no", "group_activity_no");
            parmaMap.put("g_status_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> groupList = groupProductModelMapper.getGroupActivityGoodsInfo1(parmaMap);
            for (Map<String, Object> map : groupList)
            {
                int        goodsId  = Integer.parseInt(map.get("product_id").toString());
                BigDecimal minPrice = new BigDecimal(map.get("price").toString());
                //商品没有进行中的活动则可以手动开始
                int               have              = 1;
                GroupProductModel groupProductModel = new GroupProductModel();
                groupProductModel.setProduct_id(goodsId);
                groupProductModel.setStore_id(vo.getStoreId());
                groupProductModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
                groupProductModel.setPlatform_activities_id(0);
                groupProductModel.setG_status(GroupProductModel.GROUP_GOODS_STATUS_UNDER_WAY);
                int count = groupProductModelMapper.selectCount(groupProductModel);
                if (count > 0)
                {
                    have = 0;
                }
                //查询商品库存
                int                 goodsStockNum  = confiGureModelMapper.countConfigGureNum(goodsId);
                Map<String, String> productDataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("group_data").toString(), Map.class));
                //获取拼团价格信息
                Map<String, BigDecimal> groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), map.get("group_level").toString(), minPrice, 0);

                map.put("min_man", groupPriceMap.get("minMan"));
                map.put("min_price", groupPriceMap.get("groupPrice"));
                if (productDataMap != null)
                {
                    map.put("starttime", productDataMap.get("starttime"));
                    map.put("endtime", productDataMap.get("endtime"));
                }
                else
                {
                    //未设置
                    map.put("g_status", 1);
                }
                map.put("num", goodsStockNum);
                map.put("no_have", have);
                map.put("imgurl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
            }

            resultMap.put("list", groupList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团管理首页 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> openGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //拼团没有成功的在拼团订单表   查拼团成功的数据在总订单表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (vo.isOpen())
            {
                parmaMap.put("pid", GoGroupOrderModel.OrderPid.KAITUAN);
            }
            else
            {
                parmaMap.put("pid", GoGroupOrderModel.OrderPid.CANTUAN);
            }
            parmaMap.put("ptstatus", vo.getGroupStatus());
            parmaMap.put("p_name", vo.getGoodsName());
            parmaMap.put("userName", vo.getGroupManName());
            parmaMap.put("groupman", vo.getGroupType());
            parmaMap.put("id_group", "id_group");
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> groupList = goGroupOrderModelMapper.getGrouporder(parmaMap);
            int                       count     = goGroupOrderModelMapper.countGrouporder(parmaMap);

            //获取拼团时限
            int              limitTime        = 10;
            GroupConfigModel groupConfigModel = new GroupConfigModel();
            groupConfigModel.setStore_id(vo.getStoreId());
            groupConfigModel = groupConfigModelMapper.selectOne(groupConfigModel);
            if (groupConfigModel != null)
            {
                if (!StringUtils.isEmpty(groupConfigModel.getGroup_time()))
                {
                    limitTime = groupConfigModel.getGroup_time();
                }
            }
            for (Map<String, Object> map : groupList)
            {
                int    goodsId = Integer.parseInt(map.get("p_id").toString());
                int    attrId  = Integer.parseInt(map.get("sid").toString());
                Date   addTime = (Date) map.get("add_time");
                String ptcode  = map.get("ptcode").toString();
                //参团人数
                int groupMan = Integer.parseInt(map.get("groupman").toString());
                //查询商品库存
                int                 goodsStockNum  = confiGureModelMapper.countConfigGureNum(goodsId);
                Map<String, String> productDataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("group_data").toString(), Map.class));
                Date                startDate      = null;
                Date                endDate        = null;
                if (productDataMap != null)
                {
                    startDate = DateUtil.dateFormateToDate(productDataMap.get("starttime"), GloabConst.TimePattern.YMDHMS);
                    endDate = DateUtil.dateFormateToDate(productDataMap.get("starttime"), GloabConst.TimePattern.YMDHMS);
                }
                map.put("starttime", startDate);
                map.put("end_time", endDate);
                //获取拼团配置信息
                GroupOpenModel groupOpenModel = new GroupOpenModel();
//                groupOpenModel.setStore_id(vo.getStoreId());
//                groupOpenModel.setPtcode(ptcode);
                groupOpenModel = groupOpenModelMapper.selectOne(groupOpenModel);
                //获取库存信息
                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                BigDecimal     minPrice       = new BigDecimal("0");
                if (confiGureModel != null)
                {
                    minPrice = confiGureModel.getPrice();
                    map.put("price", confiGureModel.getPrice());
                    map.put("imgurl", publiceService.getImgPath(confiGureModel.getImg(), vo.getStoreId()));
                    map.put("pt_end_time", DateUtil.getAddDateBySecond(addTime, DateUtil.dateConversion(limitTime, DateUtil.TimeType.TIME)));
                }
                map.put("num", goodsStockNum);
                //获取拼团价格信息 todo-20240805
//                Map<String, BigDecimal> groupPriceMap;
//                if (groupOpenModel != null) {
//                    groupPriceMap = publiceGroupService.getGroupDiscountPrice(vo.getStoreId(), groupOpenModel.getGroup_level(), minPrice, groupMan);
//                    map.put("openmoney", groupPriceMap.get("groupOpenPrice"));
//                    map.put("canmoney", groupPriceMap.get("groupPrice"));
//                }
            }
            resultMap.put("list", groupList);
            resultMap.put("total", count);
            //获取拼团所有人数
            List<Integer> groupManList = groupOpenModelMapper.selectGroupOpenByAll(vo.getStoreId());
            resultMap.put("man_arr", groupManList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团管理首页 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> canGroupList(QueryOpenGroupVo vo) throws LaiKeAPIException
    {
        vo.setOpen(false);
        return openGroupList(vo);
    }

    @Override
    public Map<String, Object> groupDetailList(MainVo vo, String ptcode) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取拼团活动列表信息
            List<Map<String, Object>> groupList = goGroupOrderModelMapper.groupDetailList(ptcode);
            for (Map<String, Object> map : groupList)
            {
                map.put("imgurl", publiceService.getImgPath(map.get("img").toString(), vo.getStoreId()));
                int            attrId         = Integer.parseInt(map.get("sid").toString());
                ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                map.put("price", confiGureModel.getPrice());
                map.put("can_price", map.get("z_price"));
            }
            resultMap.put("list", groupList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("拼团明细 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "groupDetailList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGroupById(MainVo vo, Integer goodsId, Integer activityNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取拼团活动商品信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("product_id", goodsId);
            parmaMap.put("activity_no", activityNo);
            List<Map<String, Object>> groupList     = groupProductModelMapper.getGroupGoodsList(parmaMap);
            String                    groupLevelStr = "";
            String                    startDate     = "";
            String                    endDate       = "";
            for (Map<String, Object> map : groupList)
            {
                groupLevelStr = map.get("group_level") + "";
                startDate = map.get("starttime").toString();
                endDate = map.get("endtime").toString();
                map.put("imgurl", publiceService.getImgPath(map.get("img").toString(), vo.getStoreId()));
            }
            //拼团参数处理
            Map<Integer, String> groupLevelMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupLevelStr, Map.class));
            resultMap.put("group_level", groupLevelMap);
            resultMap.put("startDate", startDate);
            resultMap.put("endDate", endDate);

            resultMap.put("goodsList", groupList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取编辑拼团页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGroupById");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addGroup(AddGroup vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(vo.getGlevel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CTRSCSBZQ, "参团人数参数不正确");
            }
            Map<Integer, String> levelMap = JSON.parseObject(vo.getGlevel(), new TypeReference<Map<Integer, String>>()
            {
            });
            for (Integer key : levelMap.keySet())
            {
                if (key < 2)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTRSBNXY, "拼团人数不能小于2");
                }
            }
            if (StringUtils.isEmpty(vo.getGdata()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCSBZQ, "时间参数不正确");
            }
            Map<String, String> dateMap = JSON.parseObject(vo.getGdata(), new TypeReference<Map<String, String>>()
            {
            });
            if (dateMap == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCSBZQ, "时间参数不正确");
            }
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(vo.getGoodsId());
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (groupProductModelMapper.isGroupOldGoodsName(vo.getStoreId(), productListModel.getProduct_title(), vo.getId()) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPMCYCZ, "商品名称已存在");
            }
            Date endDate;
            if ("changqi".equals(dateMap.get("endtime")))
            {
                endDate = DateUtil.getAddMonth(new Date(), 12);
            }
            else
            {
                endDate = DateUtil.dateFormateToDate(dateMap.get("endtime"), GloabConst.TimePattern.YMD);
            }

            Date startDate = DateUtil.dateFormateToDate(dateMap.get("starttime"), GloabConst.TimePattern.YMD);
            //查询老活动和复制活动的时间有没有相交
            List<Map<String, Object>> groupMap = groupProductModelMapper.getGroupOldGoodsList(vo.getStoreId(), vo.getGoodsId(), vo.getId());
            for (Map<String, Object> map : groupMap)
            {
                Date startDateOld = DateUtil.dateFormateToDate(map.get("starttime").toString(), GloabConst.TimePattern.YMD);
                Date endDateOld   = DateUtil.dateFormateToDate(map.get("endtime").toString(), GloabConst.TimePattern.YMD);
                if (DateUtil.dateCompare(endDate, startDateOld) && DateUtil.dateCompare(endDateOld, startDate))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYLHDXJ, "时间与老活动相交");
                }
            }

            //获取商品所有规格
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(vo.getGoodsId());
            List<ConfiGureModel> configList = confiGureModelMapper.select(confiGureModel);
            if (configList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGBCZ, "商品规格不存在");
            }
            GroupProductModel groupProductMdel = new GroupProductModel();
            groupProductMdel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
            groupProductMdel.setStore_id(vo.getStoreId());
            groupProductMdel.setProduct_id(vo.getGoodsId());
            groupProductMdel.setActivity_no(vo.getId());
            //获取之前活动信息
            List<GroupProductModel> groupProductModelList = groupProductModelMapper.select(groupProductMdel);
            Integer                 gStatus               = null;
            Integer                 isCopy                = null;
            Integer                 isShow                = null;
            if (groupProductModelList.size() > 0)
            {
                gStatus = groupProductModelList.get(0).getG_status();
                isCopy = groupProductModelList.get(0).getIs_copy();
                isShow = groupProductModelList.get(0).getIs_show();
            }
            //删除之前的活动
            int row = groupProductModelMapper.delete(groupProductMdel);
            if (groupProductModelList.size() > 0 && row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
            }
            //把所有规格列入拼团活动中
            for (ConfiGureModel confiGure : configList)
            {
                GroupProductModel groupProductSave = new GroupProductModel();
                groupProductSave.setActivity_no(vo.getId());
                groupProductSave.setStore_id(vo.getStoreId());
                groupProductSave.setAttr_id(confiGure.getId());
                groupProductSave.setProduct_id(confiGure.getPid());
                groupProductSave.setGroup_level(SerializePhpUtils.JavaSerializeByPhp(levelMap));
                groupProductSave.setGroup_data(SerializePhpUtils.JavaSerializeByPhp(dateMap));
                groupProductSave.setGroup_title(productListModel.getProduct_title());
                groupProductSave.setG_status(gStatus);
                groupProductSave.setIs_copy(isCopy);
                groupProductSave.setIs_show(isShow);
                groupProductSave.setStarttime(startDate);
                groupProductSave.setEndtime(endDate);
                groupProductSave.setAddtime(new Date());
                int count = groupProductModelMapper.insertSelective(groupProductSave);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
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
            logger.error("添加/编辑拼团活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGroup");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> copyGroup(MainVo vo, int activityNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取被复制的拼团活动信息
            GroupProductModel groupProductModel = new GroupProductModel();
            groupProductModel.setStore_id(vo.getStoreId());
            groupProductModel.setActivity_no(activityNo);
            List<GroupProductModel> groupProductModelList = groupProductModelMapper.select(groupProductModel);
            if (groupProductModelList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            int activNo = groupProductModelMapper.getMaxActivityNo();
            for (GroupProductModel groupProduct : groupProductModelList)
            {
                groupProduct.setId(null);
                Map<String, String> dataMap   = DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupProduct.getGroup_data(), Map.class));
                Date                startDate = DateUtil.dateFormateToDate(GloabConst.TimePattern.TIME, GloabConst.TimePattern.YMD);
                startDate = DateUtil.getAddDate(startDate, 1);
                if (dataMap != null)
                {
                    dataMap.put("starttime", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMDHMS));
                    dataMap.put("endtime", DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMDHMS));
                }
                groupProduct.setIs_delete(0);
                groupProduct.setActivity_no(activNo++);
                groupProduct.setGroup_data(SerializePhpUtils.JavaSerializeByPhp(dataMap));
                groupProduct.setStarttime(startDate);
                groupProduct.setEndtime(startDate);
                groupProduct.setG_status(GroupProductModel.GROUP_GOODS_STATUS_NO_START);
                groupProduct.setIs_copy(DictionaryConst.WhetherMaven.WHETHER_OK);
                int count = groupProductModelMapper.insertSelective(groupProduct);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS_001, "请稍后重试");
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
            logger.error("复制拼团活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "copyGroup");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> switchGroup(MainVo vo, int activityNo, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取当前拼团活动信息
            GroupProductModel groupProductModel = new GroupProductModel();
            groupProductModel.setStore_id(vo.getStoreId());
            groupProductModel.setActivity_no(activityNo);
            List<GroupProductModel> groupProductModelList = groupProductModelMapper.select(groupProductModel);
            if (groupProductModelList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HDBCZ, "活动不存在");
            }
            GroupProductModel groupProductUpdate = new GroupProductModel();
            groupProductUpdate.setStore_id(vo.getStoreId());
            groupProductUpdate.setActivity_no(activityNo);
            groupProductUpdate.setIs_show(DictionaryConst.WhetherMaven.WHETHER_OK);
            groupProductUpdate.setG_status(type);
            for (GroupProductModel groupProduct : groupProductModelList)
            {
                groupProductUpdate.setProduct_id(groupProduct.getProduct_id());
                Map<String, String> dataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(groupProduct.getGroup_data(), Map.class));
                if (dataMap != null)
                {
                    if (type == GroupProductModel.GROUP_GOODS_STATUS_UNDER_WAY)
                    {
                        //开启活动
                        String startDate = DateUtil.dateFormate(dataMap.get("starttime"), GloabConst.TimePattern.YMD);
                        if (startDate == null)
                        {
                            continue;
                        }
                        if (GloabConst.TimePattern.TIME.trim().contains(DateUtil.dateFormate(dataMap.get("starttime"), GloabConst.TimePattern.YM)))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBJSZHDSP, "请先编辑设置活动商品");
                        }
                        groupProductUpdate.setStarttime(new Date());
                        dataMap.put("starttime", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
                    }
                    else
                    {
                        //结束
                        groupProductUpdate.setEndtime(new Date());
                        dataMap.put("endtime", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS));
                        groupOpenModelMapper.endGroup(vo.getStoreId(), dataMap.get("endtime"), activityNo);
                    }
                    groupProductUpdate.setGroup_data(SerializePhpUtils.JavaSerializeByPhp(dataMap));
                }
                break;
            }
            int count = groupProductModelMapper.updateGroup(groupProductUpdate);
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
            e.printStackTrace();
            logger.error("拼团活动开始/结束 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "switchGroup");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> switchGroupShow(MainVo vo, int activityNo, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            GroupProductModel groupProductModel = new GroupProductModel();
            groupProductModel.setStore_id(vo.getStoreId());
            groupProductModel.setActivity_no(activityNo);
            groupProductModel.setIs_show(type);
            int row = groupProductModelMapper.groupShowSwitch(groupProductModel);
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
            logger.error("拼团活动是否显示开始/结束 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "switchGroupShow");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> delGroup(MainVo vo, String activityNos) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(activityNos))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] activityNoList = activityNos.split(",");
            int      row            = groupProductModelMapper.delGroup(vo.getStoreId(), DataUtils.convertToList(activityNoList));
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
            logger.error("删除拼团活动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delGroup");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGroupConfig(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            GroupConfigModel groupConfigOld = new GroupConfigModel();
            groupConfigOld.setStore_id(vo.getStoreId());
            groupConfigOld = groupConfigModelMapper.selectOne(groupConfigOld);
            resultMap.put("res", groupConfigOld);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取拼团配置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGroupConfig");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> addGroupConfig(AddGroupConfigVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int              row;
            GroupConfigModel groupConfigSave = new GroupConfigModel();
            groupConfigSave.setStore_id(vo.getStoreId());
            groupConfigSave.setHerald_time(vo.getHeraldTime());
            groupConfigSave.setIs_open(vo.getIsOpen());
            groupConfigSave.setGroup_time(vo.getGroupTime());
            groupConfigSave.setOpen_num(vo.getOpenNum());
            groupConfigSave.setCan_num(vo.getCanNum());
            groupConfigSave.setCan_again(vo.getCanAgain());
            groupConfigSave.setOpen_discount(vo.getOpenDiscount());
            groupConfigSave.setRule(vo.getRule());

            GroupConfigModel groupConfigOld = new GroupConfigModel();
            groupConfigOld.setStore_id(vo.getStoreId());
            groupConfigOld = groupConfigModelMapper.selectOne(groupConfigOld);
            if (groupConfigOld != null)
            {
                //修改
                groupConfigSave.setId(groupConfigOld.getId());
                row = groupConfigModelMapper.updateByPrimaryKeySelective(groupConfigSave);
            }
            else
            {
                //添加
                groupConfigSave.setRefunmoney(0);
                row = groupConfigModelMapper.insertSelective(groupConfigSave);
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
            logger.error("添加/编辑配置信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGroupConfig");
        }
        return resultMap;
    }
}

