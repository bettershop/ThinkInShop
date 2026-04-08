package com.laiketui.admins.admin.services.order;

import com.google.common.collect.Maps;
import com.laiketui.admins.api.admin.order.AdminConfigService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.MchConfigModelMapper;
import com.laiketui.common.mapper.OrderConfigModalMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.order.OrderConfigModal;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.order.ConfigVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 订单设置
 *
 * @author wangxian
 */
@Service
public class AdminConfigServiceImpl implements AdminConfigService
{

    private final Logger logger = LoggerFactory.getLogger(AdminConfigServiceImpl.class);

    @Autowired
    private OrderConfigModalMapper orderConfigModalMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> configShow(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            OrderConfigModal orderConfigModal = new OrderConfigModal();
            orderConfigModal.setStore_id(vo.getStoreId());
            orderConfigModal = orderConfigModalMapper.selectOne(orderConfigModal);
            //发货时限
            int day  = 0;
            int hour = 0;
            //订单自动好评
            int autoGoodCommentDay = 1;
            //订单失效
            int orderFailure = 2;
            //订单售后失效时间
            int orderAfter = 7;
            //自动收货时间
            int autoTheGoods = 2;
            //发货时限
            int orderShip = 0;
            //提醒限制
            int remind = 1;
            //包邮设置 0.未开启 1.开启
            int packageSettings = 0;
            //同件
            int samePiece = 0;
            //同单
            int sameOrder = 0;
            //提醒限制(天)
            int remindDay = 0;
            //提醒限制(小时)
            int    remindHour         = 0;
            String autoCommentContent = "";
            //积分比例
            BigDecimal proportion = BigDecimal.ZERO;
            //发放状态
            int giveStatus = 0;
            //收货后多少天返回积分
            int amsTime = 0;

            //自动评价设置几后自动好评
            if (orderConfigModal != null)
            {
                packageSettings = orderConfigModal.getPackage_settings();
                samePiece = orderConfigModal.getSame_piece();
                sameOrder = orderConfigModal.getSame_order();
                orderFailure = orderConfigModal.getOrder_failure();
                orderAfter = orderConfigModal.getOrder_after();
                autoTheGoods = orderConfigModal.getAuto_the_goods();
                orderShip = orderConfigModal.getOrder_ship();
                remind = orderConfigModal.getRemind();
                autoGoodCommentDay = orderConfigModal.getAuto_good_comment_day();
                hour = orderShip;
                //发货提醒间隔处理
                if (remind > 0)
                {
                    //店主查看发货提醒后，买家多久后能再次提醒。0.表示只能提醒一次)
                    remindDay = new BigDecimal(remind).divide(new BigDecimal("24"), BigDecimal.ROUND_DOWN).intValue();
                    remindHour = remind % 24;
                }

                //发货时限处理
                if (orderShip > 24)
                {
                    day = new BigDecimal(orderShip).divide(new BigDecimal("24"), BigDecimal.ROUND_DOWN).intValue();
                    hour = orderShip % 24;
                }
                autoCommentContent = orderConfigModal.getAuto_good_comment_content();
                proportion = orderConfigModal.getProportion();
                giveStatus = orderConfigModal.getGive_status();
                amsTime = orderConfigModal.getAms_time();
            }

            retMap.put("same_order", sameOrder);
            retMap.put("same_piece", samePiece);
            retMap.put("package_settings", packageSettings);
            retMap.put("day", day);
            retMap.put("hour", hour);
            retMap.put("remind_hour", remindHour);
            retMap.put("remind_day", remindDay);
            retMap.put("auto_good_comment_day", autoGoodCommentDay);
            retMap.put("auto_the_goods", autoTheGoods);
            retMap.put("order_after", orderAfter);
            retMap.put("order_failure", orderFailure);
            retMap.put("autoCommentContent", autoCommentContent);
            retMap.put("proportion", proportion);
            retMap.put("giveStatus", giveStatus);
            retMap.put("amsTime", amsTime);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单设置 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "configShow");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(ConfigVo configVo) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(configVo.getAccessId(), redisUtil);
            //确认收货时间
            int autoTheGoods = configVo.getAutoTheGoods();
            //订单失效时间
            int orderFailure = configVo.getOrderFailure();
            //订单售后时间
            int orderAfter = configVo.getOrderAfter();
            //提醒限制 小时
            int remindHour = configVo.getRemindHour();
            //提醒限制 存储单位（小时）
            int remind = 0;
            //多少天默认好评
            int autoGoodCommentDay = configVo.getAutoGoodCommentDay();
            //同件
            int samePiece = configVo.getSamePiece();
            //同单
            int sameOrder = configVo.getSameOrder();

            if (configVo.getPackageSettings() == 1)
            {
                if (samePiece != 0 || sameOrder != 0)
                {
                    if (samePiece <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSLBNWFSHL, "同件数量不能为负数或零", "saveConfig");
                    }
                    if (sameOrder <= 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TDSLBNWFSHL, "同单数量不能为负数或零", "saveConfig");
                    }
                }
            }
            else
            {
                samePiece = 0;
                sameOrder = 0;
            }

            if (autoTheGoods != 0)
            {
                if (autoTheGoods <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSHSJBNWFSHL, "自动收货时间不能为负数或零", "saveConfig");
                }
            }

            if (orderFailure != 0)
            {
                if (orderFailure <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGQSCSJBNWFSHL, "订单过期删除时间不能为负数或零", "saveConfig");
                }
            }
            if (orderAfter != 0)
            {
                if (orderAfter <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDSHSJBNWFSHL, "订单售后时间不能为负数或零", "saveConfig");
                }
            }
            if (remindHour < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZQDTXXZSJ, "请输入正确的提醒限制时间", "saveConfig");
            }
            remind = remindHour;
            if (configVo.getProportion() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            else if (configVo.getProportion().compareTo(new BigDecimal("100")) > 0)
            {
                configVo.setProportion(new BigDecimal("100"));
            }
            int              row;
            OrderConfigModal orderConfigOld = new OrderConfigModal();
            orderConfigOld.setStore_id(configVo.getStoreId());
            orderConfigOld = orderConfigModalMapper.selectOne(orderConfigOld);

            OrderConfigModal orderConfigSave = new OrderConfigModal();
            orderConfigSave.setAuto_good_comment_day(autoGoodCommentDay);
            orderConfigSave.setOrder_failure(orderFailure);
            orderConfigSave.setOrder_after(orderAfter);
            orderConfigSave.setAuto_the_goods(autoTheGoods);
            orderConfigSave.setRemind(remind);
            orderConfigSave.setModify_date(new Date());
            orderConfigSave.setPackage_settings(configVo.getPackageSettings());
            orderConfigSave.setSame_piece(samePiece);
            orderConfigSave.setSame_order(sameOrder);
            orderConfigSave.setAuto_good_comment_content(configVo.getAutoCommentContent());
            orderConfigSave.setProportion(configVo.getProportion());
            orderConfigSave.setGive_status(configVo.getGiveStatus());
            //新增积分收货后x天后返回积分
            if (StringUtils.isNotEmpty(configVo.getAmsTime()))
            {
                orderConfigSave.setAms_time(configVo.getAmsTime());
            }
            if (orderConfigOld != null)
            {
                orderConfigSave.setId(orderConfigOld.getId());
                row = orderConfigModalMapper.updateByPrimaryKeySelective(orderConfigSave);
            }
            else
            {
                orderConfigSave.setStore_id(configVo.getStoreId());
                row = orderConfigModalMapper.insertSelective(orderConfigSave);
            }
            if (row < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZYYDDSZXGSB, "未知原因，订单设置修改失败！", "saveConfig");
            }

            publiceService.addAdminRecord(configVo.getStoreId(), "修改了订单设置信息", AdminRecordModel.Type.UPDATE, configVo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单设置失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveConfig");
        }
    }

    @Autowired
    private PublicMchService publicMchService;

    @Override
    public Map<String, Object> mchConfigShow(MainVo vo, int mchId, int isType) throws LaiKeAPIException
    {
        Map<String, Object> retMap = Maps.newHashMap();
        try
        {
            if (isType == 1)
            {
                RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            }
            else
            {
                RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            }
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(vo.getStoreId());
            mchConfigModel.setMch_id(mchId);
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);

            //包邮设置 0.未开启 1.开启
            int packageSettings = 0;
            //同件
            int samePiece = 0;
            //同单
            int sameOrder = 0;

            //自动评价设置几后自动好评
            if (mchConfigModel != null)
            {
                packageSettings = mchConfigModel.getPackage_settings();
                samePiece = mchConfigModel.getSame_piece();
                sameOrder = mchConfigModel.getSame_order();
            }

            retMap.put("same_order", sameOrder);
            retMap.put("same_piece", samePiece);
            retMap.put("package_settings", packageSettings);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单设置 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "configShow");
        }
        return retMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mchSaveConfig(ConfigVo configVo, int mchId, int isType) throws LaiKeAPIException
    {
        try
        {
            if (isType == 1)
            {
                RedisDataTool.getRedisUserCache(configVo.getAccessId(), redisUtil, true);
            }
            else
            {
                RedisDataTool.getRedisUserCache(configVo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            }

            //同件
            int samePiece = configVo.getSamePiece();
            //同单
            int sameOrder = configVo.getSameOrder();

            if (configVo.getPackageSettings() == 1)
            {
                if (samePiece != 0 || sameOrder != 0)
                {
                    if (samePiece < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSLBNWFSHL, "同件数量不能为负数或零", "saveConfig");
                    }
                    if (sameOrder < 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TDSLBNWFSHL, "同单数量不能为负数或零", "saveConfig");
                    }
                }
            }
            else
            {
                samePiece = 0;
                sameOrder = 0;
            }

            int            row;
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(configVo.getStoreId());
            mchConfigModel.setMch_id(mchId);
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
            MchConfigModel mchConfigModelSave = new MchConfigModel();
            mchConfigModelSave.setPackage_settings(configVo.getPackageSettings());
            mchConfigModelSave.setSame_piece(samePiece);
            mchConfigModelSave.setSame_order(sameOrder);

            if (mchConfigModel != null)
            {
                mchConfigModelSave.setId(mchConfigModel.getId());
                row = mchConfigModelMapper.updateByPrimaryKeySelective(mchConfigModelSave);
            }
            else
            {
                mchConfigModelSave.setStore_id(configVo.getStoreId());
                mchConfigModelSave.setMch_id(mchId);
                row = mchConfigModelMapper.insertSelective(mchConfigModelSave);
            }
            if (row < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZYYDDSZXGSB, "未知原因，订单设置修改失败！", "saveConfig");
            }
            publiceService.addAdminRecord(configVo.getStoreId(), "修改了订单设置信息", AdminRecordModel.Type.UPDATE, configVo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单设置失败 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveConfig");
        }
    }
}

