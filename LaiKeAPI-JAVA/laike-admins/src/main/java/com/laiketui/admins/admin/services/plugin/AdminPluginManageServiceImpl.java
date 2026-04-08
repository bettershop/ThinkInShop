package com.laiketui.admins.admin.services.plugin;

import com.laiketui.admins.api.admin.plugin.AdminPluginManageService;
import com.laiketui.common.api.PublicOrderService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.config.AddPluginConfigVo;
import com.laiketui.domain.vo.config.AddPluginOrderConfigVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件管理
 *
 * @author Trick
 * @date 2023/3/17 10:16
 */
@Service
public class AdminPluginManageServiceImpl implements AdminPluginManageService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSort(MainVo vo, int pluginId, int pluginSort) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            PluginsModel pluginsModel = pluginsModelMapper.selectByPrimaryKey(pluginId);
            if (pluginsModel == null || !pluginsModel.getStore_id().equals(vo.getStoreId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "插件不存在");
            }
            pluginsModel.setPlugin_sort(pluginSort);
            int row = pluginsModelMapper.updateByPrimaryKeySelective(pluginsModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "保存排序失败");
            }
            // 排序变更后清理首页相关缓存，避免前端活动优先级不更新
            clearHomeIndexCache(vo.getStoreId());
            // 操作日志
            String event = "修改插件排序：" + pluginsModel.getPlugin_name() + " -> " + pluginSort;
            publiceService.addAdminRecord(vo.getStoreId(), userCache.getName(), event, AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM, 0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存插件排序 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveSort");
        }
    }

    /**
     * 清理商城首页相关缓存（Redis）
     */
    private void clearHomeIndexCache(int storeId)
    {
        String indexCacheKey = String.format(GloabConst.RedisHeaderKey.JAVA_INDEX_CACHE, storeId);
        String classCacheKey = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_CLASS_CONDITION_, storeId);
        String proClassCacheKey = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_PRO_CLASSDATA_CONDITION_, storeId);
        // 删除固定 key
        redisUtil.del(indexCacheKey, classCacheKey, proClassCacheKey);
        // 删除按用户维度的首页缓存 key
        String indexCacheConditionPattern = String.format(GloabConst.RedisHeaderKey.INDEX_CACHE_CONDITION_, storeId, "*");
        redisUtil.delByPattern(indexCacheConditionPattern);
        // 删除首页营销市场缓存（含会员折扣维度）
        String homeMarketListPattern = GloabConst.RedisHeaderKey.HOME_MARKET_LIST_KEY + storeId + "_*";
        redisUtil.delByPattern(homeMarketListPattern);
    }

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private AuctionConfigModelMapper auctionConfigModelMapper;

    @Autowired
    private GroupConfigModelMapper groupConfigModelMapper;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private ActivityModelMapper activityModelMapper;

    @Autowired
    private MenuModelMapper menuModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            // 按 plugin_sort 降序排序，越大越靠前
            List<PluginsModel> pluginsModels = pluginsModelMapper.selectPluginsByStoreIdOrderBySort(vo.getStoreId());
//            pluginsModels.forEach(model -> {
//                //获取通用菜单表(pc店铺菜单显示)
//                String menuId = model.getMenuId();
//                if (StringUtils.isNotEmpty(menuId)) {
//                    MenuModel menuModel = menuModelMapper.selectByPrimaryKey(menuId);
//                    model.setStatus(menuModel.getIs_display() == 0 ? 1 : 0);
//                }
//            });
            //商城-插件管理，英文适配问题
            if (vo.getLanguage().equals("en_US"))
            {
                for (PluginsModel pluginsModel : pluginsModels)
                {
                    String name = pluginsModel.getPlugin_code();
                    pluginsModel.setPlugin_name(name);
                }
            }

            resultMap.put("list", pluginsModels);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取插件列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pluginSwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int        row;
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //默认获取自营店id
            Integer      storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            PluginsModel pluginsOld = pluginsModelMapper.selectByPrimaryKey(id);
            if (pluginsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "插件不存在");
            }
            //插件开关
            int switchStatus = DictionaryConst.WhetherMaven.WHETHER_NO;
            if (pluginsOld.getStatus().equals(switchStatus))
            {
                switchStatus = DictionaryConst.WhetherMaven.WHETHER_OK;
            }
            pluginsOld.setStatus(switchStatus);
            row = pluginsModelMapper.updateByPrimaryKeySelective(pluginsOld);
            //添加操作日志
            String event = "将插件名称：" + pluginsOld.getPlugin_name() + "进行了授权店铺操作";
            publiceService.addAdminRecord(vo.getStoreId(), userCache.getName(), event, AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM, 0);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
            }
            //修改通用菜单表(pc店铺菜单显示)
//            if (StringUtils.isNotEmpty(menuId)) {
//                Integer isDisplay = menuModelMapper.selectByPrimaryKey(menuId).getIs_display();
//                if (isDisplay.equals(DictionaryConst.WhetherMaven.WHETHER_NO)) {
//                    isDisplay = DictionaryConst.WhetherMaven.WHETHER_OK;
//                } else {
//                    isDisplay = DictionaryConst.WhetherMaven.WHETHER_NO;
//                }
//                MenuModel menuModel = new MenuModel();
//                menuModel.setId(menuId);
//                menuModel.setIs_display(isDisplay);
//                row = menuModelMapper.updateByPrimaryKeySelective(menuModel);
//                if (row < 1) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
//                }
//            }
            // 该开关只控制pc店铺插件显示
            // 2023-10-09 禅道：46922
/*
            PluginsModel pluginsUpdate = new PluginsModel();
            pluginsUpdate.setId(id);
            pluginsUpdate.setStatus(switchStatus);
            pluginsUpdate.setOptime(new Date());
            row = pluginsModelMapper.updateByPrimaryKeySelective(pluginsUpdate);
            if (row < 1) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
            }
            String type = pluginsOld.getPlugin_code();
            switch (type) {
                case DictionaryConst.Plugin.COUPON:
                    CouponConfigModel couponConfigModel = new CouponConfigModel();
                    couponConfigModel.setStore_id(vo.getStoreId());
                    //优惠券插件开关 特殊处理
                    couponConfigModel.setMch_id(0);
                    couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                    if (couponConfigModel != null){
                        couponConfigModel.setIs_show(switchStatus);
                        if (couponConfigModelMapper.updateByPrimaryKeySelective(couponConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    break;
                case DictionaryConst.Plugin.MCH:
                    MchConfigModel mchConfigModel = new MchConfigModel();
                    mchConfigModel.setStore_id(vo.getStoreId());
                    mchConfigModel.setMch_id(storeMchId);
                    mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                    if (mchConfigModel != null){
                        mchConfigModel.setIs_display(switchStatus);
                        if (mchConfigModelMapper.updateByPrimaryKeySelective(mchConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    break;
                case DictionaryConst.Plugin.MEMBER:
                    MemberConfig memberConfig = new MemberConfig();
                    memberConfig.setStore_id(vo.getStoreId());
                    memberConfig = memberConfigMapper.selectOne(memberConfig);
                    if (!Objects.isNull(memberConfig)){
                        memberConfig.setIs_open(switchStatus);
                        memberConfigMapper.updateByPrimaryKeySelective(memberConfig);
                    }
                    break;
                case DictionaryConst.Plugin.SECONDS:
                    SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                    secondsConfigModel.setStore_id(vo.getStoreId());
                    secondsConfigModel.setMch_id(storeMchId);
                    secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                    if (secondsConfigModel != null){
                        secondsConfigModel.setIs_open(switchStatus);
                        if (secondsConfigModelMapper.updateByPrimaryKeySelective(secondsConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    break;
                case DictionaryConst.Plugin.PRESELL:
                    PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                    preSellConfigModel.setStore_id(vo.getStoreId());
                    preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                    if (!Objects.isNull(preSellConfigModel)){
                        preSellConfigModel.setIs_open(switchStatus);
                        preSellConfigModelMapper.updateByPrimaryKeySelective(preSellConfigModel);
                    }
                    break;
                case DictionaryConst.Plugin.SIGN:

                    break;
                case DictionaryConst.Plugin.GOGROUP:

                    break;
                case DictionaryConst.Plugin.BARGAIN:

                    break;
                case DictionaryConst.Plugin.DISTRIBUTION:
                    DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                    distributionConfigModel.setStore_id(vo.getStoreId());
                    distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                    if (distributionConfigModel != null){
                        distributionConfigModel.setStatus(switchStatus);
                        if (distributionConfigModelMapper.updateByPrimaryKeySelective(distributionConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    break;
                case DictionaryConst.Plugin.INTEGRAL:
                    IntegralConfigModel integralConfigModel = new IntegralConfigModel();
                    integralConfigModel.setStore_id(vo.getStoreId());
                    integralConfigModel.setMch_id(storeMchId);
                    integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
                    if (integralConfigModel != null){
                        integralConfigModel.setStatus(switchStatus);
                        if (integralConfigModelMapper.updateByPrimaryKeySelective(integralConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    //积分商品h5首页是否显示积分商品推荐框
//                    ActivityModel activityModel = new ActivityModel();
//                    activityModel.setStore_id(vo.getStoreId());
//                    activityModel.setPlug_type(7);
//                    activityModel = activityModelMapper.selectOne(activityModel);
//                    activityModel.setIs_display(activityModel.getIs_display() == 1 ? 0 : 1);
//                    activityModelMapper.updateByPrimaryKeySelective(activityModel);
                    break;
                case DictionaryConst.Plugin.DIY:

                    break;
                case DictionaryConst.Plugin.AUCTION:
                    AuctionConfigModel auctionConfigModel = new AuctionConfigModel();
                    auctionConfigModel.setStore_id(vo.getStoreId());
                    auctionConfigModel = auctionConfigModelMapper.selectOne(auctionConfigModel);
                    if (auctionConfigModel != null){
                        auctionConfigModel.setIs_open(switchStatus);
                        if (auctionConfigModelMapper.updateByPrimaryKeySelective(auctionConfigModel) < 1){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJBJSB, "编辑失败");
                        }
                    }
                    break;
                default:
                    break;
            }*/
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("插件开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "pluginSwitch");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPlugin(AddPluginConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel   userCache  = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int          row;
            PluginsModel pluginsOld = pluginsModelMapper.selectByPrimaryKey(vo.getId());
            if (pluginsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "插件不存在");
            }

            PluginsModel pluginsUpdate = new PluginsModel();
            pluginsUpdate.setId(vo.getId());
            pluginsUpdate.setContent(vo.getContent());
            if (vo.getPluginSwitch() != null && vo.getPluginSwitch() == 1)
            {
                pluginsUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
            }
            else
            {
                pluginsUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_NO);
            }
            pluginsUpdate.setOptime(new Date());
            row = pluginsModelMapper.updateByPrimaryKeySelective(pluginsUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了：" + pluginsOld.getPlugin_name() + "插件的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            //修改通用菜单表(pc店铺菜单显示)
//            if (StringUtils.isNotEmpty(menuId) && StringUtils.isNotEmpty(vo.getPluginSwitch())) {
//                Integer pluginSwitch = vo.getPluginSwitch();
//                if (pluginSwitch.equals(DictionaryConst.WhetherMaven.WHETHER_NO)) {
//                    pluginSwitch = DictionaryConst.WhetherMaven.WHETHER_OK;
//                } else {
//                    pluginSwitch = DictionaryConst.WhetherMaven.WHETHER_NO;
//                }
//                MenuModel menuModel = new MenuModel();
//                menuModel.setId(menuId);
//                menuModel.setIs_display(pluginSwitch);
//                row = menuModelMapper.updateByPrimaryKeySelective(menuModel);
//                if (row < 1) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.OPERATION_FAILED, "操作失败");
//                }
//            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("插件配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPlugin");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPluginConfig(AddPluginOrderConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            publicOrderService.addOrderConfig(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单设置失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPluginConfig");
        }
    }

    @Override
    public Map<String, Object> getOrderConfig(MainVo vo, String pluginCode) throws LaiKeAPIException
    {
        try
        {
            return publicOrderService.getOrderConfig(vo.getStoreId(), null, pluginCode);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单设置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addPluginConfig");
        }
    }

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;
}
