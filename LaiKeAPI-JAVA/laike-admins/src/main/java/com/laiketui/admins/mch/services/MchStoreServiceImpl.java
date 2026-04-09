package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Joiner;
import com.laiketui.admins.api.mch.MchStoreService;
import com.laiketui.common.api.PublicMchService;
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
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.MchStoreModel;
import com.laiketui.domain.mch.son.MchAdminModel;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddStoreVo;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 门店管理
 *
 * @author Trick
 * @date 2021/5/27 16:25
 */
@Service
public class MchStoreServiceImpl implements MchStoreService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchStoreModelMapper     mchStoreModelMapper;
    @Autowired
    private ProductListModelMapper  productListModelMapper;
    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;
    @Autowired
    private ConfigModelMapper       configModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private MchAdminModelMapper mchAdminModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, Integer mchStoreId, String mchName) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", mchStoreId);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", user.getMchId());
            if (StringUtils.isNotEmpty(mchName))
            {
                parmaMap.put("name", mchName);
            }
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total         = mchStoreModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> mchBrowseList = new ArrayList<>();
            if (total > 0)
            {
                mchBrowseList = mchStoreModelMapper.selectDynamic(parmaMap);
            }

            resultMap.put("list", mchBrowseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStore(AddStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            int           count;
            MchStoreModel mchStoreOld = null;
            User          user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            vo.setShopId(user.getMchId());
            if (vo.getId() != null)
            {
                mchStoreOld = new MchStoreModel();
                mchStoreOld.setStore_id(vo.getStoreId());
                mchStoreOld.setId(vo.getId());
                mchStoreOld = mchStoreModelMapper.selectOne(mchStoreOld);
                if (mchStoreOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
                }
            }
            //验证店铺名称
            if (mchStoreOld == null || !mchStoreOld.getName().equals(vo.getName()))
            {
                MchStoreModel mchStore = new MchStoreModel();
                mchStore.setStore_id(vo.getStoreId());
                mchStore.setMch_id(user.getMchId());
                mchStore.setName(vo.getName());
                count = mchStoreModelMapper.selectCount(mchStore);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在");
                }
            }
            //保存数据
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(vo.getStoreId());
            mchStoreModel.setMch_id(user.getMchId());
            mchStoreModel.setName(vo.getName());
            mchStoreModel.setCpc(vo.getCpc());
            mchStoreModel.setMobile(vo.getMobile());
            mchStoreModel.setBusiness_hours(vo.getBusinessHours());

            //国外不需要这个参数验证
            if (StringUtils.isNotEmpty(vo.getCityAll()))
            {
                String[] ssxList = vo.getCityAll().split("-");
                if (ssxList.length < 3)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZSJGSBZQ, "地址数据格式不正确");
                }
                mchStoreModel.setSheng(ssxList[0]);
                mchStoreModel.setShi(ssxList[1]);
                mchStoreModel.setXian(ssxList[2]);
            }

            mchStoreModel.setAddress(vo.getAddress());
            mchStoreModel.setIs_default(vo.getIsDefault());
            //取消所有默认店铺
            mchStoreModelMapper.updateNotDefault(vo.getStoreId(), vo.getShopId());

//            String apiKey  = "";
//            String address = mchStoreModel.getSheng() + mchStoreModel.getShi() + mchStoreModel.getXian();
            //获取商城配置信息
//            ConfigModel configModel = new ConfigModel();
//            configModel.setStore_id(vo.getStoreId());
//            configModel = configModelMapper.selectOne(configModel);
//            if (configModel != null)
//            {
//                apiKey = configModel.getTencent_key();
//            }
            //更具地址获取坐标
//            Map<String, String> latAndLng = TengxunMapUtil.getlatAndLng(apiKey, address);
//            mchStoreModel.setLongitude(latAndLng.get("lng"));
//            mchStoreModel.setLatitude(latAndLng.get("lat"));
            //取消默认
            if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                mchStoreModelMapper.updateNotDefault(vo.getStoreId(), vo.getShopId());
            }
            if (vo.getId() != null && vo.getId() > 0)
            {
                //修改
                mchStoreModel.setId(vo.getId());
                //如果是取消默认店铺,并且没有默认店铺了,则默认给一个门店为默认
                MchStoreModel updateMchStore = new MchStoreModel();
                updateMchStore.setId(vo.getId());
                if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_NO)
                {
                    updateMchStore.setStore_id(vo.getStoreId());
                    updateMchStore.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                    count = mchStoreModelMapper.updateByPrimaryKeySelective(updateMchStore);
                    if (count < 1)
                    {
                        logger.info("修改默认店铺失败 参数:" + JSON.toJSONString(updateMchStore));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                }
                count = mchStoreModelMapper.updateByPrimaryKeySelective(mchStoreModel);

                publiceService.addAdminRecord(vo.getStoreId(), "修改了门店ID：" + mchStoreModel.getId(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                mchStoreModel.setAdd_date(new Date());
                count = mchStoreModelMapper.insertSelective(mchStoreModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了门店ID：" + mchStoreModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            if (count < 1)
            {
                logger.info("门店添加/修改失败 参数:" + JSON.toJSONString(mchStoreModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }

            if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_NO)
            {
                //是否还有默认店铺,没有则给最旧的门店设置默认
                MchStoreModel mchStore = new MchStoreModel();
                mchStore.setStore_id(vo.getStoreId());
                mchStore.setMch_id(vo.getShopId());
                mchStore.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                int num = mchStoreModelMapper.selectCount(mchStore);
                if (num < 1)
                {
                    mchStoreModelMapper.setDefaultStore(mchStore.getStore_id(), mchStore.getMch_id());
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStore");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultStore(MainVo vo, Integer mchStoreId) throws LaiKeAPIException
    {
        try
        {
            User          user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            MchStoreModel mchStoreOld = mchStoreModelMapper.selectByPrimaryKey(mchStoreId);
            if (mchStoreOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MDBCZ, "门店不存在");
            }
            if (mchStoreOld.getIs_default() != DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                //取消所有默认店铺
                mchStoreModelMapper.updateNotDefault(vo.getStoreId(), user.getMchId());
                //设置置顶店铺为默认店铺
                MchStoreModel mchStoreUpdate = new MchStoreModel();
                mchStoreUpdate.setId(mchStoreOld.getId());
                mchStoreUpdate.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                int row = mchStoreModelMapper.updateByPrimaryKeySelective(mchStoreUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZSBLYGMRMD, "至少保留一个默认门店");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "将门店ID：" + mchStoreId + " 设为默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认门店 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefaultStore");
        }
    }

    @Override
    public Map<String, Object> editStorePage(MainVo vo, int shopId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (user != null)
            {
                //验证店铺
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                MchStoreModel mchStoreModel = new MchStoreModel();
                mchStoreModel.setStore_id(vo.getStoreId());
                mchStoreModel.setMch_id(shopId);
                mchStoreModel.setId(id);
                mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                Map<String, Object> mchStoreMap = new HashMap<>(16);
                if (mchStoreModel != null)
                {
                    String   businessHours     = mchStoreModel.getBusiness_hours();
                    String[] businessHoursList = businessHours.split(",");
                    mchStoreMap = JSON.parseObject(JSON.toJSONString(mchStoreModel), new TypeReference<Map<String, Object>>()
                    {
                    });
                    //获取省市县
                    AdminCgModel adminCgModel = new AdminCgModel();
                    adminCgModel.setDistrictPid(0);
                    adminCgModel.setDistrictName(mchStoreModel.getSheng());
                    AdminCgModel adminCg = adminCgModelMapper.selectOne(adminCgModel);
                    if (adminCg != null)
                    {
                        resultMap.put("sheng_id", adminCg.getId());
                        adminCgModel.setDistrictPid(adminCg.getId());
                        adminCgModel.setDistrictName(mchStoreModel.getShi());
                        adminCg = adminCgModelMapper.selectOne(adminCgModel);
                        if (adminCg != null)
                        {
                            resultMap.put("shi_id", adminCg.getId());
                            adminCgModel.setDistrictPid(adminCg.getId());
                            adminCgModel.setDistrictName(mchStoreModel.getXian());
                            adminCg = adminCgModelMapper.selectOne(adminCgModel);
                            if (adminCg != null)
                            {
                                resultMap.put("xian_id", adminCg.getId());
                            }
                        }
                    }
                    mchStoreMap.put("business_hours", businessHoursList);
                }
                resultMap.put("list", mchStoreMap);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delStore(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            String[] idList = ids.split(",");
            for (String id : idList)
            {
                MchStoreModel mchStoreModel = new MchStoreModel();
                mchStoreModel.setStore_id(vo.getStoreId());
                mchStoreModel.setMch_id(user.getMchId());
                mchStoreModel.setId(Integer.parseInt(id));
                //虚拟商品，当门店下还有订单未关闭，则不能删除门店
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setMch_store_write_id(Integer.parseInt(id));
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                if (orderDetailsModelList != null && orderDetailsModelList.size() > 0)
                {
                    for (OrderDetailsModel orderDetailsModel1 : orderDetailsModelList)
                    {
                        if (orderDetailsModel1.getR_status() != DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
                        {
                            logger.info("门店下有订单未关闭，不能删除门店");
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_GMDXHYDDWGB, "门店下有订单未关闭，不能删除门店", "delStore");
                        }
                    }
                }
                //处理商品表中和核销门店的对应删除
                HashMap<String, Object> map = new HashMap<>();
                map.put("mch_id", user.getMchId());
                map.put("store_id", user.getStore_id());
                List<ProductListModel> products = productListModelMapper.selectForStore(map);
                for (ProductListModel product : products)
                {
                    String join = "";
                    if (product.getWrite_off_mch_ids().equals("0"))
                    {
                        //查询该店铺下的所有门店，除了该删除门店
                        MchStoreModel mchStoreModel1 = new MchStoreModel();
                        mchStoreModel1.setStore_id(user.getStore_id());
                        mchStoreModel1.setMch_id(user.getMchId());
                        List<MchStoreModel> select = mchStoreModelMapper.select(mchStoreModel1);
                        List<Integer> mchIds = select.stream()
                                .filter(x -> x.getId() != Integer.parseInt(id))
                                .map(MchStoreModel::getId)
                                .collect(Collectors.toList());
                        join = Joiner.on(SplitUtils.DH).join(mchIds);
                    }
                    else
                    {
                        String[]     split = product.getWrite_off_mch_ids().split(SplitUtils.DH);
                        List<String> s_ids = new ArrayList<>(Arrays.asList(split));
                        if (!s_ids.isEmpty() && s_ids.contains(id))
                        {
                            if (s_ids.size() > 1)
                            {
                                s_ids.remove(id);
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_YJSSPDZHYGMDBKSC, "已经是商品的最后一个门店不可删除", "delStore");
                            }
                        }
                        else
                        {
                            continue;
                        }
                        join = Joiner.on(SplitUtils.DH).join(s_ids);
                    }
                    product.setWrite_off_mch_ids(join);
                    productListModelMapper.updateByPrimaryKey(product);
                }
                int count = mchStoreModelMapper.deleteByPrimaryKey(mchStoreModel);
                if (count < 1)
                {
                    logger.info("删除门店失败 参数:" + JSON.toJSONString(mchStoreModel));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MDBCZ, "门店不存在", "delStore");
                }

                publiceService.addAdminRecord(vo.getStoreId(), "删除了门店ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
            }
            //是否还有默认店铺,没有则给最旧的门店设置默认
            MchStoreModel mchStore = new MchStoreModel();
            mchStore.setStore_id(vo.getStoreId());
            mchStore.setMch_id(user.getMchId());
            mchStore.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            int num = mchStoreModelMapper.selectCount(mchStore);
            if (num < 1)
            {
                mchStoreModelMapper.setDefaultStore(mchStore.getStore_id(), mchStore.getMch_id());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除我的门店 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delStore");
        }
    }

    @Override
    public Map<String, Object> getAdminList(MainVo vo, Integer mch_store_id, String account_number) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(mch_store_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            HashMap<String, Object> parmaMap = new HashMap<>();
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("mchId", user.getMchId());
            parmaMap.put("mch_store_id", mch_store_id);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            if (StringUtils.isEmpty(account_number))
            {
                parmaMap.put("key", account_number);
            }
            int                       total = mchAdminModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = mchAdminModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    try
                    {
                        map.put("password", CryptoUtil.strDecode(MapUtils.getString(map, "password")));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    if (!StringUtils.isEmpty(MapUtils.getString(map, "last_time")))
                    {
                        map.put("last_time", DateUtil.dateFormate(MapUtils.getString(map, "last_time"), GloabConst.TimePattern.YMDHMS));
                    }
                    else
                    {
                        map.put("last_time", "");
                    }
                });
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取门店管理员列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(mch_store_id) || StringUtils.isEmpty(account_number) || StringUtils.isEmpty(password))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int lab;
            password = CryptoUtil.strEncode(password);
            MchAdminModel mchAdminModel = new MchAdminModel();
            if (id != null)
            {
                mchAdminModel = mchAdminModelMapper.selectByPrimaryKey(id);
                if (mchAdminModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYBCZ, "管理员不存在");
                }
/*                HashMap<String, Object> parmaMap = new HashMap<>();
                parmaMap.put("storeId", vo.getStoreId());
                parmaMap.put("mchId", user.getMchId());
                parmaMap.put("mch_store_id", mch_store_id);
                parmaMap.put("account_number", account_number);
                parmaMap.put("notId", id);
                if (mchAdminModelMapper.countDynamic(parmaMap) > 0){
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHCF, "管理员账号重复");
                }*/
                mchAdminModel.setPassword(password);
                lab = mchAdminModelMapper.updateByPrimaryKeySelective(mchAdminModel);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了门店ID：" + mch_store_id + " 的管理员账号：" + account_number, AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                mchAdminModel.setStore_id(vo.getStoreId());
                mchAdminModel.setMch_id(user.getMchId());
                mchAdminModel.setMch_store_id(mch_store_id);
                mchAdminModel.setAccount_number(account_number);
                mchAdminModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
                if (mchAdminModelMapper.selectCount(mchAdminModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHCF, "管理员账号重复");
                }
                mchAdminModel.setPassword(password);
                mchAdminModel.setAdd_date(new Date());
                lab = mchAdminModelMapper.insertSelective(mchAdminModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了门店ID：" + mch_store_id + " 的管理员账号：" + account_number, AdminRecordModel.Type.ADD, vo.getAccessId());

            }
            if (lab <= 0)
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
            logger.error("添加管理员 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAdmin(MainVo vo, Integer mch_store_id, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(mch_store_id) || StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int           lab;
            MchAdminModel mchAdminModel = new MchAdminModel();
            mchAdminModel.setId(id);
            mchAdminModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_OK);
            lab = mchAdminModelMapper.updateByPrimaryKeySelective(mchAdminModel);
            if (lab <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //强制管理员退出
            if (redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_APP_TOKEN + id) != null)
            {
                redisUtil.del(redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_APP_TOKEN + id).toString());
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_APP_TOKEN + id);
            }
            if (redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_PC_TOKEN + id) != null)
            {
                redisUtil.del(redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_PC_TOKEN + id).toString());
                redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_AUTO_LOGIN_MCH_SON_PC_TOKEN + id);
            }
            MchAdminModel mchAdminModel1 = mchAdminModelMapper.selectByPrimaryKey(id);
            publiceService.addAdminRecord(vo.getStoreId(), "删除了门店ID：" + mch_store_id + " 的管理员账号：" + mchAdminModel1.getAccount_number(), AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("添加管理员 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
    }
}

