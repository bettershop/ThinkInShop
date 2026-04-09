package com.laiketui.comps.task.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.laiketui.common.annotation.HandlerOrderType;
import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.api.order.PublicTaskService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 秒杀任务
 *
 * @author Trick
 * @date 2021/4/13 16:25
 */
@Service
@HandlerOrderType(type = DictionaryConst.OrdersType.ORDERS_HEADER_MS + DictionaryConst.TaskType.TASK)
public class CompsTaskSecondsSerciceImpl implements PublicTaskService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CompsTaskService taskServer;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

    @Autowired
    private SecondsLabelModelMapper secondsLabelModelMapper;

    @Autowired
    private SecondsProModelMapper secondsProModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute() throws LaiKeAPIException
    {
        //获取当前商城所有用户
        List<String>              userIdList            = null;
        List<Map<String, Object>> secondsListInfo       = null;
        SecondsConfigModel        secondsConfigModel    = null;
        String[]                  remindList            = null;
        Date                      heraldDate            = null;
        String                    noticeTime            = null;
        Map<String, Object>       insertParam           = null;
        SecondsActivityModel      secondsActivityUpdate = null;
        String                    goodsName             = null;
        List<Integer>             storeIdList           = null;
        try
        {
            int row = 0;
            XxlJobHelper.log("秒杀定时任务 开始执行!");
            storeIdList = taskServer.getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("商城id{}--->秒杀通知 开始执行!", storeId);
                //获取商城自营店
                Integer zyMchId = customerModelMapper.getStoreMchId(storeId);
                //获取当前商城所有用户
                userIdList = userBaseMapper.getUserAllByUserId(storeId,null);
                List<Integer> secondsEndStatusId = secondsActivityModelMapper.getSecondsEndStatusId(new Date());
                for (Integer secId : secondsEndStatusId)
                {
                    secondsActivityModelMapper.secondsEndStatusById(secId);
                    row++;
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("activityId", secId);
                    //更新秒杀缓存信息
                    Map<String, Object> resultMap = httpApiUtils.executeHttpApi("sec.http.cacheByActivityId", map, MediaType.MULTIPART_FORM_DATA_VALUE);
                    map = null;

                    XxlJobHelper.log("sec.http.cacheByActivityId远程调用返回结果: " + JSON.toJSONString(resultMap));
                    resultMap = null;
                }

//                row = secondsActivityModelMapper.secondsEndStatus(new Date());
                XxlJobHelper.log("商城id{} 一共{} 个秒杀已经结束", storeId, row);
                row = secondsActivityModelMapper.secondsNotOpenStatus(new Date());
                XxlJobHelper.log("商城id{} 一共{} 个秒杀未开始", storeId, row);

                XxlJobHelper.log("======== 秒杀定时任务  开始执行! ========");
                //优化51964 【JAVA开发环境】秒杀（PC店铺）：店铺不需要有单独的秒杀设置，取插件配置-秒杀配置里面的公共配置即可
                secondsConfigModel = new SecondsConfigModel();
                secondsConfigModel.setStore_id(storeId);
                secondsConfigModel.setMch_id(zyMchId);
                secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                if (secondsConfigModel == null)
                {
                    XxlJobHelper.log("未找到秒杀配置信息!");
                    continue;
                }
                //秒杀消息推送
                if (StringUtils.isEmpty(secondsConfigModel.getRemind()))
                {
                    XxlJobHelper.log("未开消息推送!");
                    continue;
                }

                remindList = secondsConfigModel.getRemind().split(SplitUtils.DH);
                for (int i = 0; i < remindList.length; i++)
                {
                    noticeTime = remindList[i];
                    //获取秒杀推送时间，数据库中存的是秒数，后台设置的是分钟，需要转换
                    heraldDate = DateUtil.getAddDateBySecond(new Date(), Integer.parseInt(noticeTime) * 60);
                    XxlJobHelper.log("开始秒杀消息推送,当前推送时间段:{}!", DateUtil.dateFormate(heraldDate, GloabConst.TimePattern.YMDHMS));
                    //获取即将开始得活动信息
                    secondsListInfo = secondsActivityModelMapper.getSecondsByStoreIdAndHeraldDate(storeId, heraldDate, new Date());
                    if (secondsListInfo == null || secondsListInfo.size() < 1)
                    {
                        XxlJobHelper.log("没有即将开始的秒杀!");
                        continue;
                    }
                    if (userIdList == null || userIdList.size() < 1)
                    {
                        continue;
                    }

                    for (Map<String, Object> map : secondsListInfo)
                    {
                        //需要推送的秒杀活动id
                        int id = MapUtils.getIntValue(map, "id");
                        //当前活动推送的进度
                        JSONObject tuiMap = JSON.parseObject(MapUtils.getString(map, "remind_json"));
                        if (tuiMap == null)
                        {
                            tuiMap = new JSONObject();
                        }
                        XxlJobHelper.log("当前秒杀活动id:{} 当前推送进度:{}", id, i + 1);
                        //当前时间段是否推送过
                        if (!tuiMap.containsKey(noticeTime))
                        {
                            //开始批量推送
                            goodsName = MapUtils.getString(map, "product_title");
                            insertParam = new HashMap<>(16);
                            insertParam.put("userList", userIdList);
                            insertParam.put("store_id", storeId);
                            insertParam.put("senderid", "task");
                            insertParam.put("title", "有秒杀活动开始啦！");
                            insertParam.put("content", String.format("商品:【%s】 秒杀马上要开始了", goodsName));
                            insertParam.put("date", new Date());
                            insertParam.put("type", 1);
                            //批量发送系统推送
                            row = systemMessageModelMapper.noticeUserAll(insertParam);
                            tuiMap.put(noticeTime, true);
                            XxlJobHelper.log("商品【{}】即将开始 一共通知{} 个用户", goodsName, row);
                            //记录这一次的推送记录
                            secondsActivityUpdate = new SecondsActivityModel();
                            secondsActivityUpdate.setId(id);
                            secondsActivityUpdate.setIsNotice(i == noticeTime.length() ? DictionaryConst.WhetherMaven.WHETHER_OK : DictionaryConst.WhetherMaven.WHETHER_NO);
                            secondsActivityUpdate.setRemind_json(JSON.toJSONString(tuiMap));
                            secondsActivityModelMapper.updateByPrimaryKeySelective(secondsActivityUpdate);
                        }
                        else
                        {
                            XxlJobHelper.log("当前进度已通知过!");
                        }

                    }

                }

                row = secondsActivityModelMapper.secondsOpenStatus(new Date());
                XxlJobHelper.log("店铺id{} 一共{} 个秒杀进行中", storeId, row);
                //抢光了
                row = secondsActivityModelMapper.secondsTootAllStatus();
                XxlJobHelper.log("店铺id{} 一共{} 个秒杀已被抢光", storeId, row);
                //同步标签状态
                row = secondsLabelModelMapper.updateLabelStatus(storeId);
                XxlJobHelper.log("店铺id{} 一共{} 个秒杀标签被关闭了", storeId, row);

                XxlJobHelper.log("商城{},秒杀任务执行完毕!", storeId);
            }
            XxlJobHelper.log("秒杀任务执行完毕!");
        }
        catch (Exception e)
        {
            logger.error("秒杀任务异常 ", e);
            XxlJobHelper.handleFail("秒杀任务 异常堆栈信息: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            userIdList = null;
            secondsListInfo = null;
            secondsConfigModel = null;
            remindList = null;
            heraldDate = null;
            noticeTime = null;
            insertParam = null;
            secondsActivityUpdate = null;
            goodsName = null;
            storeIdList = null;
        }

    }

    @Override
    public void stock() throws LaiKeAPIException
    {
        List<Integer>              storeIdList              = null;
        AddStockVo                 addStockVo               = null;
        List<SecondsActivityModel> secondsActivityModelList = null;
        List<Map<String, Object>>  attrModelList            = null;
        try
        {
            XxlJobHelper.log("秒杀处理库存 开始执行!");
            storeIdList = taskServer.getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("商城id{}--->秒杀处理库存 开始执行!", storeId);
                //获取所有已经结束了的秒杀,把剩余库存回滚
                secondsActivityModelList = secondsActivityModelMapper.secondsActivityList(storeId, SecondsActivityModel.SecondsStatus.SECKILL_STATUS_END);
                if (secondsActivityModelList == null)
                {
                    secondsActivityModelList = new ArrayList<>();
                }
                XxlJobHelper.log("一共有{}个结束活动", secondsActivityModelList.size());
                for (SecondsActivityModel secondsActivity : secondsActivityModelList)
                {
                    XxlJobHelper.log("==== 正在处理秒杀活动id{}库存 ====", secondsActivity.getId());
                    attrModelList = secondsActivityModelMapper.getSecondsAttrList(secondsActivity.getGoodsId(), secondsActivity.getId());

                    for (Map<String, Object> attrMap : attrModelList)
                    {
                        int attrId  = MapUtils.getIntValue(attrMap, "id");
                        int goodsId = MapUtils.getIntValue(attrMap, "goodsId");
                        int secNum  = MapUtils.getIntValue(attrMap, "secNum");
                        //如果是修改则回滚部分库存
                        addStockVo = new AddStockVo();
                        addStockVo.setStoreId(storeId);
                        addStockVo.setText("秒杀结束,回滚库存");
                        addStockVo.setPid(goodsId);
                        addStockVo.setId(attrId);
                        addStockVo.setAddNum(secNum);
                        publicStockService.addGoodsStock(addStockVo, "task");
                        XxlJobHelper.log("规格id{} 回滚库存:{}", attrId, secNum);
                    }
                    //库存回滚后,活动库存置空
                    secondsProModelMapper.resettingStockByAcId(secondsActivity.getId());
                }
                XxlJobHelper.log("商城{},秒杀处理库存执行完毕!", storeId);
            }
            XxlJobHelper.log("秒杀处理库存任务执行完毕!");
        }
        catch (Exception e)
        {
            logger.error("秒杀处理库存异常 ", e);
            XxlJobHelper.log("秒杀处理库存 异常堆栈信息: ", e);
        }
        finally
        {
            addStockVo = null;
            storeIdList = null;
            secondsActivityModelList = null;
            attrModelList = null;
        }
    }


    @Override
    public void Cache() throws LaiKeAPIException
    {
        Map<String, Object> resultMap = null;
        try
        {
            taskServer.getStoreIdAll();
            //缓存秒杀活动信息
            resultMap = httpApiUtils.executeHttpApi("sec.http.cache", new HashMap<>(), MediaType.MULTIPART_FORM_DATA_VALUE);
            XxlJobHelper.log("sec.http.cache远程调用返回结果: " + JSON.toJSONString(resultMap));
        }
        catch (Exception e)
        {
            logger.error("缓存秒杀活动信息 ", e);
            XxlJobHelper.log("缓存秒杀活动信息: ", e);
        }
        finally
        {
            resultMap = null;
        }

    }
}
