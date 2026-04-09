package com.laiketui.admins.admin.services.notie;

import com.laiketui.admins.api.admin.notie.AdminPublicNoticeService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.systems.SystemTellModel;
import com.laiketui.domain.systems.SystemTellStoreModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddNoticeVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 公告管理
 *
 * @author Trick
 * @date 2021/1/19 15:44
 */
@Service
public class AdminPublicNoticeServiceImpl implements AdminPublicNoticeService
{
    private final Logger logger = LoggerFactory.getLogger(AdminPublicNoticeServiceImpl.class);
    @Autowired
    private SystemTellModelMapper      systemTellModelMapper;
    @Autowired
    private RedisUtil                  redisUtil;
    @Autowired
    private MessageLoggingModalMapper  messageLoggingModalMapper;
    @Autowired
    private PublicAdminService         publicAdminService;
    @Autowired
    private CustomerModelMapper        customerModelMapper;
    @Autowired
    private OrderModelMapper           orderModelMapper;
    @Autowired
    private SystemTellStoreModelMapper systemTellStoreModelMapper;
    @Autowired
    private PubliceService             publiceService;

    @Override
    public Map<String, Object> getPublicNoticeInfo(MainVo vo, Integer id, Integer isStore) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            //商户端获取商城公告，不展示该商场创建时间之前的公告  禅道 51552
            if (StringUtils.isNotEmpty(isStore) && isStore == 1)
            {
                CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(vo.getStoreId());
                if (customerModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误", "getPublicNoticeInfo");
                }
                parmaMap.put("storeTime", DateUtil.dateFormate(customerModel.getAdd_date(), GloabConst.TimePattern.YMDHMS));
            }
            parmaMap.put("store_id", 0);
            if (id != null && id > 0)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> systemTellModelList = systemTellModelMapper.selectDynamic(parmaMap);
            List<Integer>             storeTypes;
            StringBuilder             storeTypesName;
            int                       total               = systemTellModelMapper.countDynamic(parmaMap);
            for (Map<String, Object> map : systemTellModelList)
            {
                storeTypes = new ArrayList<>();
                storeTypesName = new StringBuilder();
                int    type      = MapUtils.getIntValue(map, "type");
                int    tellId    = MapUtils.getIntValue(map, "id");
                String status;
                Date   startDate = DateUtil.dateFormateToDate(MapUtils.getString(map, "startdate"), GloabConst.TimePattern.YMDHMS);
                Date   endDate   = DateUtil.dateFormateToDate(MapUtils.getString(map, "enddate"), GloabConst.TimePattern.YMDHMS);
                if (DateUtil.dateCompare(new Date(), endDate))
                {
                    status = "已失效";
                }
                else if (DateUtil.dateCompare(startDate, new Date()))
                {
                    status = "未生效";
                }
                else
                {
                    status = "生效中";
                    //只有系统维护才踢人
                    if (type == 1)
                    {
                        //获取所有商城
                        List<Integer> storeIdList = customerModelMapper.getStoreIdAll(new Date());
                        for (Integer storeId : storeIdList)
                        {
                            publicAdminService.outLoginStoreAdminAll(storeId);
                        }
                    }
                }
                map.put("status", status);
                //判断商城是否查阅
                SystemTellStoreModel systemTellStoreModel = new SystemTellStoreModel();
                systemTellStoreModel.setStore_id(vo.getStoreId());
                systemTellStoreModel.setTell_id(tellId);
                systemTellStoreModel = systemTellStoreModelMapper.selectOne(systemTellStoreModel);
                if (systemTellStoreModel != null)
                {
                    map.put("is_read", DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                else
                {
                    map.put("is_read", DictionaryConst.WhetherMaven.WHETHER_NO);
                }
                for (int i = 0; i < 5; i++)
                {
                    switch (i)
                    {
                        case 0:
                            if (MapUtils.getInteger(map, "store_tell").equals(SystemTellModel.TELL.YES))
                            {
                                storeTypes.add(i);
                                storeTypesName.append("管理后台").append(SplitUtils.DH);
                            }
                            break;
                        case 1:
                            if (MapUtils.getInteger(map, "user_tell").equals(SystemTellModel.TELL.YES))
                            {
                                storeTypes.add(i);
                                storeTypesName.append("用户端").append(SplitUtils.DH);
                            }
                            break;
                        case 2:
                            if (MapUtils.getInteger(map, "mch_tell").equals(SystemTellModel.TELL.YES))
                            {
                                storeTypes.add(i);
                                storeTypesName.append("商家端").append(SplitUtils.DH);
                            }
                            break;
                        case 3:
                            if (MapUtils.getInteger(map, "supplier_tell").equals(SystemTellModel.TELL.YES))
                            {
                                storeTypes.add(i);
                                storeTypesName.append("供应商端").append(SplitUtils.DH);
                            }
                            break;
                        case 4:
                            if (MapUtils.getInteger(map, "mch_son_tell").equals(SystemTellModel.TELL.YES))
                            {
                                storeTypes.add(i);
                                storeTypesName.append("门店端").append(SplitUtils.DH);
                            }
                            break;
                    }
                }
                map.put("storeTypes", storeTypes);
                map.put("storeTypesName", StringUtils.trim(storeTypesName.toString(), SplitUtils.DH));
            }

            resultMap.put("list", systemTellModelList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统公告 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPublicNoticeInfo");
        }
        return resultMap;
    }

    @Override
    public boolean addSysNoticeInfo(AddNoticeVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel         = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int             count;
            SystemTellModel systemTellModelOld = null;
            if (vo.getId() != null)
            {
                systemTellModelOld = systemTellModelMapper.selectByPrimaryKey(vo.getId());
                if (systemTellModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GGBCZ_001, "公告不存在");
                }
            }
            if (StringUtils.isEmpty(vo.getTitle()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BTBNWK, "标题不能为空");
            }
            if (vo.getType() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GGLXBNWK, "公告类型不能为空");
            }
            if (systemTellModelOld == null)
            {
                if (StringUtils.isEmpty(vo.getStartDate()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KSSJBNWK, "开始时间不能为空");
                }
                else if (StringUtils.isEmpty(vo.getEndDate()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSSJBNWK, "结束时间不能为空");
                }
            }
            if (!DateUtil.dateCompare(vo.getStartDate(), DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS)))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KSSJBNXYDQSJ, "开始时间不能小于当前时间");
            }
            if (!DateUtil.dateCompare(vo.getEndDate(), vo.getStartDate()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KSSJBNDYJSSJ, "开始时间必须小于结束时间");
            }
            if (StringUtils.isEmpty(vo.getContent()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GGNRBNWK, "公告内容不能为空");
            }
            if (StringUtils.isEmpty(vo.getStoreTypes()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSFBNWK, "接收方不能为空");
            }
            String[]        StoreTypeList       = vo.getStoreTypes().split(SplitUtils.DH);
            SystemTellModel systemTellModelSave = new SystemTellModel();
            systemTellModelSave.setType(vo.getType());
            systemTellModelSave.setTitle(vo.getTitle());
            systemTellModelSave.setContent(vo.getContent());
            systemTellModelSave.setTimetype(vo.getIsTime());
            systemTellModelSave.setStartdate(vo.getStartDate());
            systemTellModelSave.setEnddate(vo.getEndDate());
            systemTellModelSave.setStoreTell(SystemTellModel.TELL.No);
            systemTellModelSave.setUserTell(SystemTellModel.TELL.No);
            systemTellModelSave.setMchTell(SystemTellModel.TELL.No);
            systemTellModelSave.setSupplierTell(SystemTellModel.TELL.No);
            systemTellModelSave.setMchSonTell(SystemTellModel.TELL.No);
            for (String s : StoreTypeList)
            {
                switch (s)
                {
                    case "0":
                        systemTellModelSave.setStoreTell(SystemTellModel.TELL.YES);
                        break;
                    case "1":
                        systemTellModelSave.setUserTell(SystemTellModel.TELL.YES);
                        break;
                    case "2":
                        systemTellModelSave.setMchTell(SystemTellModel.TELL.YES);
                        break;
                    case "3":
                        systemTellModelSave.setSupplierTell(SystemTellModel.TELL.YES);
                        break;
                    case "4":
                        systemTellModelSave.setMchSonTell(SystemTellModel.TELL.YES);
                        break;
                }
            }
            //判断时间段中是否存在交集数据
            Date                start    = DateUtil.dateFormateToDate(vo.getStartDate(), GloabConst.TimePattern.YMDHMS);
            Date                end      = DateUtil.dateFormateToDate(vo.getEndDate(), GloabConst.TimePattern.YMDHMS);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("type", vo.getType());
//            paramMap.put("startDate", new Date());
            if (vo.getId() != null)
            {
                paramMap.put("idNot", vo.getId());
            }
            List<Map<String, Object>> mapList = systemTellModelMapper.selectDynamic(paramMap);
            if (mapList.size() > 0)
            {
                mapList.stream().forEach(map ->
                {
                    Date startDate = DateUtil.dateFormateToDate(MapUtils.getString(map, "startdate"), GloabConst.TimePattern.YMDHMS);
                    Date endDate   = DateUtil.dateFormateToDate(MapUtils.getString(map, "enddate"), GloabConst.TimePattern.YMDHMS);
                    boolean b = DateUtil.hasOverlap(start, end, startDate, endDate);
                    if (b)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLXZGSJDZYCZSXDGG, "该类型在该时间段中已存在生效中的公告");
                    }
                });
            }
            if (systemTellModelOld != null)
            {
                systemTellModelSave.setId(systemTellModelOld.getId());
                count = systemTellModelMapper.updateByPrimaryKeySelective(systemTellModelSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了公告：" + vo.getTitle() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                systemTellModelSave.setAdd_time(new Date());
                count = systemTellModelMapper.insertSelective(systemTellModelSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了公告：" + vo.getTitle(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统公告 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addSysNoticeInfo");
        }
    }

    @Override
    public boolean delSysNoticeInfo(String token, int storeId, Integer id) throws LaiKeAPIException
    {
        try
        {
            AdminModel      adminModel      = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            SystemTellModel systemTellModel = new SystemTellModel();
            systemTellModel.setId(id);
            systemTellModel = systemTellModelMapper.selectOne(systemTellModel);
            if (systemTellModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XTGGBCZ, "系统公告不存在");
            }
            int count = systemTellModelMapper.deleteByPrimaryKey(id);
            //添加操作日志
            publiceService.addAdminRecord(storeId, "删除了公告：" + systemTellModel.getTitle(), AdminRecordModel.Type.DEL, token);
            return count > 0;
        }
        catch (Exception e)
        {
            logger.error("删除系统公告 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delSysNoticeInfo");
        }
    }

    @Override
    public Map<String, Object> noticeList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            // 所有需要查询的通知类型
            // 订单
            int[] orderTypes = {1, 2, 3, 4, 5, 6};
            // 商品
            int[] goodsTypes = {7, 9};
            // 账单
            int[]         checkTypes = {19, 20, 21};
            List<Integer> allTypes   = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 19, 20, 21);

            // 获取自营店ID（用于订单过滤）
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());

            // 1. 一次性查出所有未读通知
            Map<String, Object> paramMap = new HashMap<>(8);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("read_or_not", 0);
            // MyBatis 支持 List 传入 IN
            paramMap.put("types", allTypes);
            List<Map<String, Object>> allNotices = messageLoggingModalMapper.selectByTypes(paramMap);

            // 2. 按 type 分组
            Map<Integer, List<Map<String, Object>>> groupedNotices  = new HashMap<>();
            Set<Integer>                            orderIdsToFetch = new HashSet<>();

            for (Map<String, Object> notice : allNotices)
            {
                Integer type = MapUtils.getInteger(notice, "type");
                if (type == null) continue;

                // 如果是订单类，且包含"订单"字样，才需要查 Order（保持原逻辑）
                String content = MapUtils.getString(notice, "content", "");
                if (Arrays.stream(orderTypes).anyMatch(t -> t == type) && content.contains("订单"))
                {
                    Integer orderId = MapUtils.getInteger(notice, "parameter");
                    if (orderId != null)
                    {
                        orderIdsToFetch.add(orderId);
                    }
                }

                groupedNotices.computeIfAbsent(type, k -> new ArrayList<>()).add(notice);
            }

            // 3. 批量查 Order（仅订单类）
            Map<Integer, OrderModel> orderMap = new HashMap<>();
            if (!orderIdsToFetch.isEmpty())
            {
                List<OrderModel> orders = orderModelMapper.selectByIds(new ArrayList<>(orderIdsToFetch));
                for (OrderModel order : orders)
                {
                    orderMap.put(order.getId(), order);
                }
            }

            // 4. 构建三类结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            resultList.add(buildNoticeGroup(groupedNotices, orderTypes, storeMchId, orderMap));
            resultList.add(buildNoticeGroup(groupedNotices, goodsTypes, storeMchId, orderMap));
            resultList.add(buildNoticeGroup(groupedNotices, checkTypes, storeMchId, orderMap));

            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商城消息通知 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeList");
        }
        return resultMap;
    }

    // 辅助方法：构建某一类通知组
    private Map<String, Object> buildNoticeGroup(
            Map<Integer, List<Map<String, Object>>> groupedNotices,
            int[] types,
            Integer storeMchId,
            Map<Integer, OrderModel> orderMap)
    {

        List<Map<String, Object>> mainList  = new ArrayList<>();
        int                       totalMain = 0;

        for (int type : types)
        {
            List<Map<String, Object>> notices      = groupedNotices.getOrDefault(type, new ArrayList<>());
            List<Map<String, Object>> filteredList = new ArrayList<>();
            int                       validCount   = 0;

            for (Map<String, Object> notice : notices)
            {
                Map<String, Object> noticeMap = new HashMap<>(16);
                boolean             skip      = false;

                // 原逻辑：订单类需判断是否自营
                String content = MapUtils.getString(notice, "content", "");
                if (content.contains("订单"))
                {
                    Integer mchIdInNotice = MapUtils.getInteger(notice, "mch_id");
                    // 自营店过滤
                    if (storeMchId != null && !storeMchId.equals(mchIdInNotice))
                    {
                        skip = true;
                    }
                    else
                    {
                        Integer    orderId = MapUtils.getInteger(notice, "parameter");
                        OrderModel order   = orderId != null ? orderMap.get(orderId) : null;
                        if (order == null)
                        {
                            skip = true; // 订单不存在，跳过
                        }
                        else
                        {
                            noticeMap.put("self_lifting", order.getSelf_lifting() != null && order.getSelf_lifting() == 1 ? 1 : 0);
                        }
                    }
                }

                if (!skip)
                {
                    noticeMap.put("id", MapUtils.getInteger(notice, "id"));
                    noticeMap.put("parameter", MapUtils.getString(notice, "parameter"));
                    noticeMap.put("toUrl", MapUtils.getString(notice, "to_url"));
                    noticeMap.put("content", content);
                    noticeMap.put("is_popup", MapUtils.getString(notice, "is_popup"));
                    noticeMap.put("add_date", DateUtil.dateFormate(MapUtils.getString(notice, "add_date"), GloabConst.TimePattern.YMDHMS));
                    filteredList.add(noticeMap);
                    validCount++;
                }
            }

            totalMain += validCount;
            Map<String, Object> group = new HashMap<>(8);
            group.put("list", filteredList);
            group.put("total", validCount);
            group.put("type", type);
            mainList.add(group);
        }

        Map<String, Object> result = new HashMap<>(8);
        result.put("list", mainList);
        result.put("total", totalMain);
        result.put("type", Arrays.toString(types));
        return result;
    }

    @Override
    public void noticeRead(MainVo vo, Integer id, String types) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user                = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String[]            typeList            = null;
            MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
            messageLoggingModal.setStore_id(vo.getStoreId());
            messageLoggingModal.setRead_or_not(DictionaryConst.WhetherMaven.WHETHER_OK);
            if (id != null)
            {
                messageLoggingModal.setId(id);
                messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingModal);
            }
            if (types != null)
            {
                typeList = types.split(SplitUtils.DH);
            }
            if (typeList != null)
            {
                for (String type : typeList)
                {
                    messageLoggingModal.setType(Integer.parseInt(type));
                    messageLoggingModalMapper.noticeRead(vo.getStoreId(), null, type);
                }
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记消息已读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeRead");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noticePopup(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] idList = ids.split(SplitUtils.DH);
            int      count  = messageLoggingModalMapper.updateMessLogPopup(idList);
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
            logger.error("标记已弹窗 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "noticeRead");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readSysNotice(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] idList = ids.split(SplitUtils.DH);
            for (String id : idList)
            {
                SystemTellStoreModel systemTellStoreModel = new SystemTellStoreModel();
                systemTellStoreModel.setStore_id(vo.getStoreId());
                systemTellStoreModel.setTell_id(Integer.valueOf(id));
                systemTellStoreModel = systemTellStoreModelMapper.selectOne(systemTellStoreModel);
                if (systemTellStoreModel == null)
                {
                    SystemTellStoreModel save = new SystemTellStoreModel();
                    save.setStore_id(vo.getStoreId());
                    save.setTell_id(Integer.valueOf(id));
                    save.setAdd_time(new Date());
                    systemTellStoreModelMapper.insertSelective(save);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("标记系统公告-已读 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "readSysNotice");
        }
    }
}

