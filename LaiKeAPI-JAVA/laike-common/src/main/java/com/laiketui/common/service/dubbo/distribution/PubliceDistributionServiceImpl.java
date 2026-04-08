package com.laiketui.common.service.dubbo.distribution;

import com.alibaba.fastjson2.JSON;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ActivityModel;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ProductConfigModel;
import com.laiketui.domain.config.ProductModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.DistributionGoodsModel;
import com.laiketui.domain.distribution.DistributionRecordModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.plugin.LevelUpdateModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 分销公共
 *
 * @author Trick
 * @date 2021/2/19 16:56
 */
@Service
public class PubliceDistributionServiceImpl implements PubliceDistributionService
{
    private final Logger logger = LoggerFactory.getLogger(PubliceDistributionServiceImpl.class);

    @Autowired
    private UserDistributionModelMapper  userDistributionModelMapper;
    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;

    @Autowired
    private DistributionGoodsModelMapper distributionGoodsModelMapper;

    @Autowired
    private LevelUpdateModelMapper levelUpdateModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private ProductConfigModelMapper productConfigModelMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uplevel(int storeId, String upId, String orderNo) throws LaiKeAPIException
    {
        try
        {
            logger.debug("【分销】订单号:{},当前升级userid:{}", orderNo, upId);
            //获取自营店id
            MchModel mchModel = new MchModel();
            Integer  mchId    = customerModelMapper.getStoreMchId(storeId);
            if (mchId != null)
            {
                mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    mchModel = new MchModel();
                }
            }
            //获取所有分销等级
            Map<Integer, Map<String, String>> gradeMap = new HashMap<>(16);
            Map<String, Object>               parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("id_sort", DataUtils.Sort.ASC.toString());
            List<Map<String, Object>> gradeInfList = distributionGradeModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : gradeInfList)
            {
                //获取升级条件
                Map<String, String> uplevelMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(map.get("uplevel_obj").toString(), Map.class));
                if (uplevelMap != null)
                {
                    int gradeId = Integer.parseInt(map.get("id").toString());
                    gradeMap.put(gradeId, uplevelMap);
                }
            }
            if (gradeMap.size() > 0)
            {
                int uplevel = 0;
                //获取当前用户分销信息
                UserDistributionModel userDistributionModel = new UserDistributionModel();
                userDistributionModel.setStore_id(storeId);
                userDistributionModel.setUser_id(upId);
                userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                if (userDistributionModel == null)
                {
                    logger.debug("该用户非分销会员");
                    return;
                }
                //获取上级信息
                UserDistributionModel userDistributionFather = new UserDistributionModel();
                userDistributionFather.setStore_id(storeId);
                userDistributionFather.setUser_id(userDistributionModel.getPid());
                userDistributionFather = userDistributionModelMapper.selectOne1(userDistributionFather);
                if (userDistributionFather == null)
                {
                    logger.error("分销数据发生错误,分销商{}的上级{}已不再是分销商", userDistributionModel.getUser_id(), userDistributionModel.getPid());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                //获取分销配置信息
                Map<String, Object>     configMap               = new HashMap<>(16);
                DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                distributionConfigModel.setStore_id(storeId);
                distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                if (distributionConfigModel != null)
                {
                    configMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                    if (configMap == null || configMap.isEmpty())
                    {
                        logger.debug("分销配置信息不存在");
                        return;
                    }
                }

                //是否走礼包流程 如果是礼包商品 则先晋升礼包，然后在看其它等级条件晋升(其它等级必须>礼包等级)
                if (StringUtils.isNotEmpty(orderNo))
                {
                    //礼包流程 获取分销商品信息
                    Map<String, Object> map = distributionGradeModelMapper.getGoodsInfoByOrder(storeId, orderNo);
                    //是否是礼包商品
                    int uplevelId = MapUtils.getIntValue(map, "uplevel");
                    if (uplevelId > 0)
                    {
                        //直接升级
                        DistributionGradeModel distributionGradeModel = new DistributionGradeModel();
                        distributionGradeModel.setId(uplevelId);
                        if (distributionGradeModelMapper.selectCount(distributionGradeModel) < 1)
                        {
                            logger.error("订单{} 购买的是礼包商品,晋升id {} 失败,等级不存在", orderNo, uplevelId);
                        }
                        else
                        {
                            uplevel = uplevelId;
                        }
                    }
                }


                //第一次循环升级当前购买人,第二次循环升级上级
                for (int i = 0; i < 2; i++)
                {
                    Integer level = userDistributionModel.getLevel();
                    //累计消费金额
                    BigDecimal onlyAmount = userDistributionModel.getOnlyamount();
                    //累积业绩
                    BigDecimal allAmount = userDistributionModel.getAllamount();
                    if (i == 1)
                    {
                        logger.debug("上级开始循环规则");
                        upId = userDistributionFather.getUser_id();
                        level = userDistributionFather.getLevel();
                        //55918 没有清空礼包带来的uplevel，导致上级没有满足条件，下级购买了礼包商品升级带着上级也因为礼包而升级了，其他导致上级升级情况也在这做处理
                        uplevel = 0;
                        onlyAmount = userDistributionFather.getOnlyamount();
                        allAmount = userDistributionFather.getAllamount();
                    }
                    //获取当前可升级的等级
                    List<Map<String, Object>> gradeInfoList = this.getGradeLevel(storeId, level);

                    //分销等级循环判断规则，gradeInfoList为可升级成的等级，循环判断是否满足条件
                    for (Map<String, Object> map : gradeInfoList)
                    {
                        //升级条件满足数
                        int ok = 0;
                        //获取下一等级配置信息
                        int id = Integer.parseInt(map.get("id").toString());
                        //a:2:{s:6:"onebuy";s:3:"500";s:6:"recomm";s:4:"2,14";}
                        Map<String, String> nextGardeMap = gradeMap.get(id);
                        logger.debug("当前用户{}循环等级{}", upId, id);

                        //一次性消费
                        if (nextGardeMap.containsKey(DistributionGradeModel.Uplevel_Obj.ONE_BUY))
                        {
                            logger.debug("--- 一次性消费规则执行中 ---");
                            List<Integer> statusList = new ArrayList<>();
                            statusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                            BigDecimal price = orderModelMapper.getDistributionPrice(storeId, upId, orderNo, statusList);
                            if (price == null)
                            {
                                price = BigDecimal.ZERO;
                            }
                            BigDecimal oneBuy = new BigDecimal(nextGardeMap.get(DistributionGradeModel.Uplevel_Obj.ONE_BUY));
                            logger.debug("一次性消费{}元,当前消费{}元", oneBuy, price);
                            if (price.compareTo(oneBuy) >= 0)
                            {
                                logger.debug("【一次性消费规则】 条件满足");
                                ok++;
                            }
                            logger.debug("--- 一次性消费规则执行完成 ---");
                        }
                        //推荐指定等级会员(满足条件上级升级)
                        if (nextGardeMap.containsKey(DistributionGradeModel.Uplevel_Obj.RECOMM))
                        {
                            logger.debug("--- 推荐指定等级规则 ---");
                            //获取当前等级规则 {"recomm":"2(人数),18(等级id)"} 需要满足邀请 2个id为18的等级
                            List<String> recommList = DataUtils.convertToList(nextGardeMap.get(DistributionGradeModel.Uplevel_Obj.RECOMM).split(SplitUtils.DH));
                            //获取当前人邀请了多少个规则等级
                            if (recommList != null && recommList.size() > 1)
                            {
                                //查看应用该等级的数量
                                parmaMap.clear();
                                parmaMap.put("store_id", storeId);
                                parmaMap.put("pid", upId);
                                parmaMap.put("level", recommList.get(1));
                                int count = userDistributionModelMapper.countDynamic(parmaMap);
                                logger.debug("需要满足邀请 {}个id为{}的等级 {}当前邀请数量{}", recommList.get(0), recommList.get(1), userDistributionFather.getUser_id(), count);
                                if (count >= Integer.parseInt(recommList.get(0)))
                                {
                                    ok++;
                                }
                            }
                            logger.debug("--- 推荐指定等级规则执行完成 ---");
                        }
                        //累计消费升级
                        if (nextGardeMap.containsKey(DistributionGradeModel.Uplevel_Obj.MANY_BUY))
                        {
                            int manyBuy = MapUtils.getIntValue(nextGardeMap, DistributionGradeModel.Uplevel_Obj.MANY_BUY);
                            //当该用户的累计购买大于商家设定的值，则升级
                            if (onlyAmount.intValue() >= manyBuy)
                            {
                                ok++;
                            }
                        }
                        //累计业绩升级
                        if (nextGardeMap.containsKey(DistributionGradeModel.Uplevel_Obj.MANYYEJI))
                        {
                            int manyBuy = MapUtils.getIntValue(nextGardeMap, DistributionGradeModel.Uplevel_Obj.MANYYEJI);
                            //当该用户的销售业绩大于等于商家设定的累计业绩就升级
                            if (allAmount.intValue() >= manyBuy)
                            {
                                ok++;
                            }
                        }
                        //团队人数升级
                        if (nextGardeMap.containsKey(DistributionGradeModel.Uplevel_Obj.MANYPEOPLE))
                        {
                            String[] manypeopleList = MapUtils.getString(nextGardeMap, DistributionGradeModel.Uplevel_Obj.MANYPEOPLE).split(SplitUtils.DH);
                            if (manypeopleList.length > 0)
                            {
                                //直推人数
                                parmaMap.clear();
                                parmaMap.put("store_id", storeId);
                                parmaMap.put("levelGT", 0);
                                parmaMap.put("pid", upId);
                                int count = userDistributionModelMapper.countDynamic(parmaMap);
                                //团队人数
                                parmaMap.put("ltGT1", userDistributionModel.getLt());
                                parmaMap.put("rtGT1", userDistributionModel.getRt());
                                int num = userDistributionModelMapper.countDynamic(parmaMap);
                                if (count >= Integer.parseInt(manypeopleList[0]) && num >= Integer.parseInt(manypeopleList[1]))
                                {
                                    ok++;
                                }
                            }
                        }

                        //如果升级条件数大于0
                        if (nextGardeMap.size() > 0)
                        {
                            //分销等级晋升设置满足任意一项 等级晋升设置（1.满足任意一项升级，2.满足所有项升级）
                            int cuplevel = Integer.parseInt(configMap.get(DistributionConfigModel.SetsKey.C_UPLEVEL).toString());
                            if (cuplevel == 1 && ok > 0)
                            {
                                uplevel = id;
                                //从等级大到小排序,只要一升级则跳出循环,其它等级不用做判断了
                                break;
                            }
                            else if (cuplevel == 2 && ok == nextGardeMap.size())
                            {
                                uplevel = id;
                                //从等级大到小排序,只要一升级则跳出循环,其它等级不用做判断了
                                break;
                            }
                        }
                    }
                    SystemMessageModel systemMessageSave = new SystemMessageModel();
                    //升级
                    if (uplevel > userDistributionModel.getLevel())
                    {
                        UserDistributionModel userDistributionOld = userDistributionModelMapper.selectByPrimaryKey(userDistributionModel.getId());
                        //修改用户分销等级
                        UserDistributionModel userDistributionModelUpdate = new UserDistributionModel();
                        userDistributionModelUpdate.setId(userDistributionModel.getId());
                        userDistributionModelUpdate.setLevel(uplevel);
                        if (i == 1)
                        {
                            //上级升级
                            if ("1000".equals(userDistributionFather.getUser_id()) || mchModel.getUser_id().equals(userDistributionFather.getUser_id()))
                            {
                                logger.debug("userid={} 上级是 系统/自营店分销角色不升级!", userDistributionFather.getUser_id());
                                continue;
                            }
                            if (uplevel <= userDistributionFather.getLevel())
                            {
                                logger.debug("userid={} 是上级 等级未发生变化,不升级!", userDistributionFather.getUser_id());
                                continue;
                            }
                            userDistributionModelUpdate.setId(userDistributionFather.getId());
                            if (userDistributionFather.getLevel() > uplevel)
                            {
                                break;
                            }
                            //站内推送发货信息
                            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                            systemMessageSave.setSenderid("admin");
                            systemMessageSave.setStore_id(userDistributionFather.getStore_id());
                            systemMessageSave.setRecipientid(userDistributionFather.getUser_id());
                            systemMessageSave.setTitle("系统消息");
                            systemMessageSave.setContent("恭喜！您的分销等级提升了！");
                            systemMessageSave.setTime(new Date());
                            systemMessageModelMapper.insertSelective(systemMessageSave);
                        }
                        else
                        {
                            //站内推送发货信息
                            systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                            systemMessageSave.setSenderid("admin");
                            systemMessageSave.setStore_id(userDistributionModel.getStore_id());
                            systemMessageSave.setRecipientid(userDistributionModel.getUser_id());
                            systemMessageSave.setTitle("系统消息");
                            systemMessageSave.setContent("恭喜！您的分销等级提升了！");
                            systemMessageSave.setTime(new Date());
                            systemMessageModelMapper.insertSelective(systemMessageSave);
                        }
                        if ("1000".equals(userDistributionOld.getUser_id()) || mchModel.getUser_id().equals(userDistributionOld.getUser_id()))
                        {
                            logger.debug("userid={} 系统/自营店分销角色不升级!", userDistributionOld.getUser_id());
                            continue;
                        }

                        //晋升时间
                        userDistributionModelUpdate.setAdd_date(new Date());
                        int count = userDistributionModelMapper.updateByPrimaryKeySelective1(userDistributionModelUpdate);
                        if (count < 1)
                        {
                            logger.debug("分销统一升级失败: 参数 {}", JSON.toJSONString(userDistributionModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                        //添加一条记录
                        LevelUpdateModel levelUpdateModelSave = new LevelUpdateModel();
                        levelUpdateModelSave.setUser_id(upId);
                        levelUpdateModelSave.setType(LevelUpdateModel.TYPE_RULE);
                        levelUpdateModelSave.setOld_level(userDistributionModelUpdate.getLevel());
                        levelUpdateModelSave.setUp_level(uplevel);
                        levelUpdateModelMapper.insertSelective(levelUpdateModelSave);
                        logger.debug("修改用户分销等级成功: 参数 {}", JSON.toJSONString(levelUpdateModelSave));
                        logger.debug("会员【{}】,分销统一升级到等级【{}】成功", upId, uplevel);
                    }
                    else
                    {
                        logger.debug("会员【{}】,分销统一升级到等级【{}】失败,等级未发生变化/等级低于当前等级", upId, uplevel);
                    }
                }

            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("分销统一升级异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uplevel");
        }
    }


    /**
     * 获取可以升级的分销等级
     * 【php Commission.get_nextlevel】
     *
     * @param storeId    -
     * @param gradeLevel -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/19 12:25
     */
    private List<Map<String, Object>> getGradeLevel(int storeId, int gradeLevel) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultMap = new ArrayList<>();
        try
        {
            //获取可升级的分销等级
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("levelGT", gradeLevel);
            parmaMap.put("id_sort", DataUtils.Sort.DESC.toString());
            resultMap = distributionGradeModelMapper.selectDynamic(parmaMap);
        }
        catch (Exception e)
        {
            logger.error("获取可以升级的分销等级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGradeLevel");
        }
        return resultMap;
    }

    @Override
    public void straightUp(int storeId, String userId, int level) throws LaiKeAPIException
    {
        try
        {
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(storeId);
            userDistributionModel.setUser_id(userId);
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                int                   oldLevel                  = userDistributionModel.getLevel();
                UserDistributionModel userDistributionModelSave = new UserDistributionModel();
                userDistributionModelSave.setId(userDistributionModel.getId());
                userDistributionModelSave.setStore_id(storeId);
                userDistributionModelSave.setUser_id(userId);
                userDistributionModelSave.setLevel(level);
                //升级级别必须大于之前等级，且该用户不能为自营店初始用户
                MchModel mchModel = new MchModel();
                Integer  mchId    = customerModelMapper.getStoreMchId(storeId);
                if (mchId != null)
                {
                    mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                }
                if (mchModel.getUser_id().equals(userDistributionModel.getUser_id()))
                {
                    logger.debug("userid={} 系统/自营店分销角色不升级!", userDistributionModel.getUser_id());
                    return;
                }
                if (level > oldLevel)
                {
                    userDistributionModelSave.setAdd_date(new Date());
                    int count = userDistributionModelMapper.updateByPrimaryKeySelective1(userDistributionModelSave);
                    if (count < 1)
                    {
                        logger.debug("会员【{}】分销直升到等级【{}】失败！", userId, level);
                    }
                    //添加一条记录
                    LevelUpdateModel levelUpdateModelSave = new LevelUpdateModel();
                    levelUpdateModelSave.setUser_id(userId);
                    levelUpdateModelSave.setType(LevelUpdateModel.TYPE_RULE);
                    levelUpdateModelSave.setOld_level(userDistributionModel.getLevel());
                    levelUpdateModelSave.setUp_level(userDistributionModelSave.getLevel());
                    levelUpdateModelMapper.insertSelective(levelUpdateModelSave);
                    logger.debug("修改用户分销等级成功: 参数 {}", JSON.toJSONString(levelUpdateModelSave));
                    logger.debug("会员【{}】,分销统一升级到等级【{}】成功", userId, level);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("礼包升级 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBSJYC, "礼包升级异常", "straightUp");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLevel(int storeId, String userId, int level, String fatherId) throws LaiKeAPIException
    {
        try
        {
            int count   = 0;
            int lt      = 0;
            int rt      = 1;
            int uplevel = 0;
            //检查当前用户是否已经开通了分销
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setUser_id(userId);
            userDistributionModel.setStore_id(storeId);
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel != null)
            {
                if (userDistributionModel.getLevel() > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYHYJKTLFX, "该用户已经开通了分销");
                }
                //已经有分销商身份，但是没有分销商等级（已经绑定永久关系的情况），只修改分销商等级不修改推荐人
                userDistributionModel.setLevel(level);
                userDistributionModel.setAdd_date(new Date());
                userDistributionModelMapper.updateByPrimaryKeySelective(userDistributionModel);
                return;
            }
            if (StringUtils.isEmpty(fatherId))
            {
                fatherId = "";
            }
            //获取最顶级
            UserDistributionModel userDistributionTop = new UserDistributionModel();
            userDistributionTop.setLevel(0);
            // uplevel 第几代 0 是最顶级分销用户一个商城只有一个 rt 最大 且 pid 的id是自己的 user_id
            userDistributionTop.setUplevel(0);
            userDistributionTop.setStore_id(storeId);
            userDistributionTop = userDistributionModelMapper.selectOne1(userDistributionTop);
            if (userDistributionTop == null)
            {
                String storeUserId = customerModelMapper.getStoreUserId(storeId);
                if (StringUtils.isEmpty(storeUserId))
                {
                    logger.info("商城id:{} 没有创建自营店或者自营店用户不存在", storeId);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXCJZYD, "请先创建自营店");
                }
                //生成顶级分销商
                User userTop = userBaseMapper.getUserTop(storeId);
                userDistributionModel = new UserDistributionModel();
                userDistributionModel.setStore_id(storeId);
                userDistributionModel.setUser_id(storeUserId);
                //上级id使用自己
                userDistributionModel.setPid(userId);
                logger.info("商城id:{} 没有分销顶级分销商,系统现在创建一个 userid:{}", storeId, storeUserId);
                count = userDistributionModelMapper.save1(storeId, userTop.getUser_id(), new Date());
                //获取最顶级
                userDistributionTop = new UserDistributionModel();
                userDistributionTop.setLevel(0);
                userDistributionTop.setUplevel(0);
                userDistributionTop.setStore_id(storeId);
                userDistributionTop = userDistributionModelMapper.selectOne1(userDistributionTop);
            }

            UserDistributionModel userDistributionModelSave = new UserDistributionModel();
            userDistributionModelSave.setStore_id(storeId);
            userDistributionModelSave.setUser_id(userId);
            userDistributionModelSave.setTx_commission(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setAccumulative(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setTeam_put(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setOne_put(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setAllamount(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setOnlyamount(new BigDecimal(BigInteger.ZERO));
            userDistributionModelSave.setCommission(new BigDecimal(BigInteger.ZERO));
            //获取上级信息
            UserDistributionModel fatherDistribution = null;
            if (StringUtils.isNotEmpty(fatherId) && !fatherId.equals(userDistributionTop.getUser_id()))
            {
                fatherDistribution = new UserDistributionModel();
                fatherDistribution.setUser_id(fatherId);
                fatherDistribution.setStore_id(storeId);
                fatherDistribution = userDistributionModelMapper.selectOne1(fatherDistribution);
                if (fatherDistribution != null)
                {
                    logger.debug("子节【{}】点插入父节点【{}】当中", userId, fatherId);

                    //子节点插入父节点当中
                    rt = fatherDistribution.getRt() + 1;
                    lt = fatherDistribution.getRt();
                    uplevel = fatherDistribution.getUplevel() + 1;

                    //扩展父节点lt值： update lkt_user_distribution set lt = lt + 2 where store_id = #{storeId} and lt>=#{rt}
                    count = userDistributionModelMapper.updateTreeLt(storeId, fatherDistribution.getRt());
                    logger.debug("扩展父节点 lt 数量:{}", count);

                    //扩展父节点rt值：  update lkt_user_distribution set rt = rt + 2 where store_id = #{storeId} and rt>=#{rt}
                    count = userDistributionModelMapper.updateTreeRt(storeId, fatherDistribution.getRt());

                    if (count < 1)
                    {
                        logger.debug("子节点插入父节点当中 失败");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                    }

                }
            }
            if (fatherDistribution == null)
            {
                //没有推荐人,找最顶级做推荐人
                fatherId = userDistributionTop.getUser_id();
                rt = userDistributionTop.getRt() + 1;
                lt = userDistributionTop.getRt();
                uplevel = userDistributionTop.getUplevel() + 1;
                //扩展顶级节点
                userDistributionModelMapper.updateTree(storeId, 0, rt + 1, userDistributionTop.getUser_id());
            }
            userDistributionModelSave.setPid(fatherId);
            userDistributionModelSave.setLevel(level);
            userDistributionModelSave.setLt(lt);
            userDistributionModelSave.setRt(rt);
            userDistributionModelSave.setUplevel(uplevel);
            userDistributionModelSave.setAdd_date(new Date());
            count = userDistributionModelMapper.save(userDistributionModelSave);
            if (count < 1)
            {
                logger.debug("子节点插入父节点当中 失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("创建分销商信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJFXSYC, "创建分销商异常", "createLevel");
        }
    }

    @Override
    public void putcomm(int storeId, String orderno, BigDecimal achievement) throws LaiKeAPIException
    {
        try
        {
            //规则结算方式  1.付款后 2.收货后
            int ruleType = 2;
            //获取分销配置信息
            Map<String, Object>     configMap               = new HashMap<>(16);
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(storeId);
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (distributionConfigModel != null)
            {
                configMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                if (configMap == null || configMap.isEmpty())
                {
                    logger.info("分销配置信息不存在");
                    return;
                }
                //获取规则结算方式
                if (configMap.containsKey(DistributionConfigModel.SetsKey.C_PAY))
                {
                    ruleType = Integer.parseInt(configMap.get(DistributionConfigModel.SetsKey.C_PAY).toString());
                }
            }

            int count;
            //获取订单信息
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(orderno);
            orderModel = orderModelMapper.selectOne(orderModel);
            if (orderModel == null)
            {
                logger.info("订单{}不存在", orderno);
                return;
            }
            if (ruleType == 1)
            {
                //付款后
                if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT != orderModel.getStatus())
                {
                    logger.info("当前分销设置为【付款后】结算 订单{}状态为{} 不予结算", orderno, orderModel.getStatus());
                    return;
                }
            }
            else
            {
                //收货后
                if (DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE != orderModel.getStatus())
                {
                    logger.info("当前分销设置为【收货后】结算 订单{}状态为{} 不予结算", orderno, orderModel.getStatus());
                    return;
                }
            }

            //获取下单人信息
            User user = new User();
            user.setStore_id(storeId);
            user.setUser_id(orderModel.getUser_id());
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                logger.info("用户{}不存在", orderModel.getUser_id());
                return;
            }
            //获取未发放的分销日志信息
            DistributionRecordModel distributionRecordModel = new DistributionRecordModel();
            distributionRecordModel.setStore_id(storeId);
            distributionRecordModel.setsNo(orderno);
            distributionRecordModel.setStatus(0);
            List<DistributionRecordModel> distributionRecordModelList = distributionRecordModelMapper.select(distributionRecordModel);
            SystemMessageModel            systemMessageSave;
            //开始发放
            for (DistributionRecordModel distributionRecord : distributionRecordModelList)
            {
                //修改用户佣金
                count = userDistributionModelMapper.updateUserCommission(storeId, distributionRecord.getUser_id(), distributionRecord.getMoney());
                if (count < 1)
                {
                    logger.debug("佣金[{}]发放【{}】失败,佣金修改失败", distributionRecord.getMoney(), distributionRecord.getUser_id());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //修改佣金表已发放状态
                DistributionRecordModel distributionRecordUpdate = new DistributionRecordModel();
                distributionRecordUpdate.setId(distributionRecord.getId());
                distributionRecordUpdate.setStatus(1);
                count = distributionRecordModelMapper.updateByPrimaryKeySelective(distributionRecordUpdate);
                if (count < 1)
                {
                    logger.debug("佣金[{}]发放【{}】失败,佣金表状态修改失败", distributionRecord.getMoney(), distributionRecord.getUser_id());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //操作记录
                String      event       = distributionRecord.getEvent() + "[" + orderno + "]";
                RecordModel recordModel = new RecordModel(storeId, user.getUser_id(), distributionRecord.getMoney(), new BigDecimal("0"), new Date(), event, 7);
                count = recordModelMapper.insertSelective(recordModel);
                if (count < 1)
                {
                    logger.debug("佣金[{}]发放【{}】失败,记录失败", distributionRecord.getMoney(), user.getUser_id());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                logger.debug("佣金[{}]发放【{}】成功", distributionRecord.getMoney(), distributionRecord.getUser_id());
                //微信推送
                String text = "您通过订单【%s】获得佣金%s元！";
                text = String.format(text, orderModel.getsNo(), distributionRecord.getMoney());
                logger.debug(text);
                // TODO: 2021/2/20 微信推送
                //站内推送发货信息
                systemMessageSave = new SystemMessageModel();
                systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                systemMessageSave.setSenderid("admin");
                systemMessageSave.setStore_id(storeId);
                systemMessageSave.setRecipientid(distributionRecord.getUser_id());
                systemMessageSave.setTitle("系统消息");
                systemMessageSave.setContent(text);
                systemMessageSave.setTime(new Date());
                systemMessageModelMapper.insertSelective(systemMessageSave);
            }
            if (orderModel.getIs_put() == 0)
            {
                    //获取分佣配置信息
                    int rank = 0;
                    if (distributionConfigModel != null)
                    {
                        Map<String, Object> setsConfigMap = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
                        if (setsConfigMap.containsKey(DistributionConfigModel.SetsKey.C_CENGJI))
                        {
                            rank = Integer.parseInt(String.valueOf(setsConfigMap.get(DistributionConfigModel.SetsKey.C_CENGJI)));
                        }
                    }
                //增加个人消费 - [累计消费]
                    userDistributionModelMapper.updateUserOnlyAmount(storeId,user.getUser_id(),achievement);
                    count = userDistributionModelMapper.updateUserAchievement(storeId, user.getUser_id(), achievement);
                    if (count < 1)
                    {
                        logger.debug("业绩[{}]发放【{}】失败,记录失败", achievement, user.getUser_id());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                    }
                    //上级业绩增加
                    String fatherId = user.getUser_id();
                    int    i        = 0;
                    while (i < rank)
                    {
                        UserDistributionModel userDistributionModel = new UserDistributionModel();
                        userDistributionModel.setStore_id(storeId);
                        userDistributionModel.setUser_id(fatherId);
                        userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
                        if (userDistributionModel != null && !StringUtils.isEmpty(userDistributionModel.getPid()))
                        {
                            fatherId = userDistributionModel.getPid();
                            //更新业绩
                            count = userDistributionModelMapper.updateUserAchievement1(storeId, fatherId, achievement);
                            if (count < 1)
                            {
                                logger.debug("业绩[{}]发放【{}】失败,上级业绩增加失败", achievement, user.getUser_id());
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                            }
                        }
                        i++;
                    }


                //修改佣金发放状态
                OrderModel orderModelUpdate = new OrderModel();
                orderModelUpdate.setId(orderModel.getId());
                orderModelUpdate.setIs_put(1);
                count = orderModelMapper.updateByPrimaryKeySelective(orderModelUpdate);
                if (count < 1)
                {
                    logger.debug("发放【{}】失败,订单佣金发放状态修改失败", orderno);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
            }
            uplevel(storeId, user.getUser_id(), orderno);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("佣金发放 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "putcomm");
        }
    }

    @Override
    public void commSettlement(int storeId, String userId, String orderno) throws LaiKeAPIException
    {
        try
        {
            List<Map<String, Object>> orderInfo = distributionGoodsModelMapper.selectOrderInfo(storeId, orderno);
            for (Map<String, Object> map : orderInfo)
            {
                //分销商品等级
                int goodsLevel = StringUtils.stringParseInt(map.get("uplevel"));
                //实际支付金额
                BigDecimal price = new BigDecimal(map.get("after_discount").toString());
                //修改分佣等级
                uplevel(storeId, userId, orderno);
                //发放佣金
                putcomm(storeId, orderno, price);
                //只有分销商品设置的等级大于0才可以升级
                if (goodsLevel > 0)
                {
                    //升级会员
                    straightUp(storeId, userId, goodsLevel);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("佣金结算 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "commSettlement");
        }
    }

    @Autowired
    private ActivityModelMapper activityModelMapper;

    @Autowired
    ProductModelMapper productModelMapper;

    @Override
    public Map<String, Object> getGoodsInfo(int storeId, User user, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取分销配置信息
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(storeId);
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            if (distributionConfigModel != null && distributionConfigModel.getStatus() == 1)
            {
                //是否开启内购广告设置
                String img = "";
                if (distributionConfigModel.getAdvertising() == 1)
                {
                    if (StringUtils.isNotEmpty(distributionConfigModel.getAd_image()))
                    {
                        img = distributionConfigModel.getAd_image();
                    }
                }
                Map<String, Object> parmaMap      = new HashMap<>(16);
                Map<String, Object> userGradeinfo = new HashMap<>(16);
                if (user != null)
                {
                    //如果登录了则获取当前登录人能最高赚取的金额
                    userGradeinfo = userDistributionModelMapper.getUserGradeinfo(storeId, user.getUser_id());
                }
                //如果没有获取到会员等级信息,则获取最大的直推金额
                if (userGradeinfo == null || userGradeinfo.isEmpty())
                {
                    //获取默认等级信息
                    parmaMap.put("store_id", storeId);
                    parmaMap.put("id_sort", DataUtils.Sort.DESC.toString());
                    parmaMap.put("pageStart ", 0);
                    parmaMap.put("pageEnd", 1);
                    List<Map<String, Object>> mapList = userGradeModelMapper.selectDynamic(parmaMap);
                    if (!Objects.isNull(mapList) && mapList.size() > 0)
                    {
                        userGradeinfo = mapList.get(0);
                    }
                }
                //等级信息
                Map<String, Object> gradeMap;
                //直推金额
                BigDecimal directM = BigDecimal.ZERO;
                //单位
                BigDecimal directmType = BigDecimal.ONE;
                //组装数据
                if (userGradeinfo != null && !userGradeinfo.isEmpty())
                {
                    gradeMap = SerializePhpUtils.getDistributionGradeBySets(String.valueOf(userGradeinfo.get("sets")));
                    directmType = new BigDecimal(String.valueOf(gradeMap.get("direct_m_type")));
                    directM = new BigDecimal(String.valueOf(gradeMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
                }
                //获取分销商品信息
                parmaMap.clear();
                parmaMap.put("store_id", storeId);
                parmaMap.put("uplevel1", "0");
                parmaMap.put("pid_group", DataUtils.Sort.DESC.toString());
                parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("pageStart", pageModel.getPageNo());
                parmaMap.put("pageEnd", pageModel.getPageSize());
                ProductModel productModel = new ProductModel();
                productModel.setStore_id(storeId);
                productModel = productModelMapper.selectOne(productModel);
                if (productModel != null && productModel.getIs_display_sell_put() == 0)
                {
                    //不展示已售罄的商品
                    parmaMap.put("stockNum", "stockNum");
                }
                List<Map<String, Object>> goodsList = distributionGoodsModelMapper.selectGoodsInfo(parmaMap);
                List<Map<String, Object>> goodsDisDiyRules = null;
                for (Map<String, Object> map : goodsList)
                {
                    //是否是自定义规则
                    if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM))
                    {
                        goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(map.get("rules_set").toString());
                        if (Objects.isNull(goodsDisDiyRules))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                        }

                        //当前用户分销等级ID
                        if (userGradeinfo != null && userGradeinfo.get("level") != null)
                        {
                            int fxLevelId = MapUtils.getInteger(userGradeinfo, "level");
                            for (int i = 0; i < goodsDisDiyRules.size(); i++)
                            {
                                Map setsMap = (Map) goodsDisDiyRules.get(i);
                                int tempGid = MapUtils.getInteger(setsMap, "id");
                                if (tempGid == fxLevelId)
                                {
                                    directmType = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_MODE_TYPE)));
                                    directM = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
                                    break;
                                }
                            }
                        }
                    }

                    Integer attrId = MapUtils.getInteger(map, "s_id");
                    String  imgUrl = publiceService.getImgPath(MapUtils.getString(map, "imgurl"), storeId);
                    map.put("imgurl", imgUrl);
                    //获取当前商品最低价格
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                    if (confiGureModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSJCW, "商品数据错误");
                    }
                    BigDecimal goodsPrice = confiGureModel.getPrice();
                    //分销价格
                    BigDecimal fxPrice = goodsPrice;
                    if (user != null)
                    {
                        //获取会员折扣价
                        fxPrice = this.getGoodsPrice(storeId, user.getUser_id(), goodsPrice);
                    }
                    //最高可获取的金额
                    BigDecimal maxEarnPrice;
                    if (directmType.compareTo(BigDecimal.ONE) == 0)
                    {
                        //固定值
                        maxEarnPrice = directM;
                    }
                    else
                    {
                        //商品pv值
                        BigDecimal pv = new BigDecimal(MapUtils.getString(map, "pv"));
                        //商品成本价
                        BigDecimal costPrice = new BigDecimal(MapUtils.getString(map, "costprice"));
                        //获取分润基值
                        BigDecimal profit = this.getProfit(storeId, fxPrice, costPrice, null, BigDecimal.ONE, pv);
                        //百分比
                        maxEarnPrice = profit.multiply(directM.multiply(new BigDecimal("0.01")));
                    }

                    map.put("price", fxPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
                    map.put("fx_price", maxEarnPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                resultMap.put("ad_image", img);
                resultMap.put("list", goodsList);
                //分销活动标题
                ActivityModel activityModel = activityModelMapper.getActivityModel(storeId, DictionaryConst.GoodsActive.GOODSACTIVE_DISTRIBUTION);
                if (activityModel != null)
                {
                    resultMap.put("title", activityModel.getName());
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分销商品 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsInfo");
        }
        return resultMap;
    }

    @Override
    public BigDecimal getGoodsPrice(int storeId, String userId, BigDecimal goodsPrice) throws LaiKeAPIException
    {
        BigDecimal price = goodsPrice;
        try
        {
            if (goodsPrice == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            BigDecimal discount = this.getGoodsDiscount(storeId, userId);
            if (BigDecimal.ONE.compareTo(discount) > 0)
            {
                //获取分销折扣价
                price = price.multiply(discount);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户分销商品价格 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsPrice");
        }
        return price;
    }

    @Override
    public BigDecimal getGoodsDiscount(int storeId, String userId) throws LaiKeAPIException
    {
        BigDecimal discount = BigDecimal.ONE;
        try
        {
            Map<String, Object> disUserInfo = userDistributionModelMapper.getUserGradeinfo(storeId, userId);
            if (disUserInfo == null)
            {
                return discount;
            }
            //获取折扣开关
            String              gradeStr     = MapUtils.getString(disUserInfo, "sets");
            Map<String, Object> setsMap      = SerializePhpUtils.getDistributionGradeBySets(gradeStr);
            Integer             zheKouSwitch = MapUtils.getInteger(setsMap, "zhekou");
            if (zheKouSwitch == 1)
            {
                //获取折扣值
                if (disUserInfo.containsKey("discount"))
                {
                    discount = new BigDecimal(MapUtils.getString(disUserInfo, "discount"));
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户分销折扣值 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsDiscount");
        }
        return discount;
    }

    @Override
    public BigDecimal getProfit(int storeId, BigDecimal goodsPrice, BigDecimal costPrice, BigDecimal orderPrice, BigDecimal num, BigDecimal pv) throws LaiKeAPIException
    {
        BigDecimal profit = BigDecimal.ONE;
        try
        {
            //获取分销设置
            DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
            distributionConfigModel.setStore_id(storeId);
            distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
            //反序列化
            Map<String, Object> config = SerializePhpUtils.getDistributionConfigBySets(distributionConfigModel.getSets());
            //佣金计算方式
            int calculation = MapUtils.getInteger(config, DistributionConfigModel.SetsKey.C_YJJISUAN);
            //计算分润基值
            switch (calculation)
            {
                case DistributionConfigModel.BY_SPLR:
                    //利润基值 商品售价-商品成本价*数量
//                    profit = goodsPrice.subtract(costPrice).multiply(num);
                    profit = goodsPrice.subtract(costPrice.multiply(num));
                    break;
                case DistributionConfigModel.BY_SPSJ:
                    //售价基值 商品售价*数量
//                    profit = goodsPrice.multiply(num);
                    profit = goodsPrice;
                    break;
                case DistributionConfigModel.BY_PV:
                    //PV基值 PV*数量
                    profit = pv.multiply(num);
                    break;
                default:
                    //默认订单成交价
                    if (orderPrice != null)
                    {
                        profit = orderPrice;
                    }
                    else
                    {
                        //如果订单价是空的则为预算,使用商品金额
                        profit = goodsPrice;
                    }
                    break;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取分润基值 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getProfit");
        }
        return profit;
    }

    @Override
    public Map<String, Object> getGoodsList(MainVo vo, String productTitle, String sortKey, int queue, Integer cid,User user) throws LaiKeAPIException
    {
        Map<String,Object> resultMap = new HashMap<>();
        //单位
        BigDecimal directMtype = BigDecimal.ZERO;
        //直推金额
        BigDecimal directM = BigDecimal.ZERO;
        //等级直推佣金
        BigDecimal levelDirectM = BigDecimal.ZERO;
        //当前分销身份信息
        DistributionGradeModel distributionGrade = null;
        if (user != null)
        {
            //获取当前用户分销信息
            distributionGrade = distributionGradeModelMapper.getUserDistribution(user);
        }
        if (distributionGrade == null)
        {
            //不是分销身份则获取商城top1分销人
            distributionGrade = distributionGradeModelMapper.getUserTop1Distribution(vo.getStoreId());
        }
        if (distributionGrade == null)
        {
            logger.error("商城【{}】分销配置有问题：未配置分销等级!", vo.getStoreId());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSZFXDJ, "请设置分销等级", "getGoodsList");
        }
        String              sets     = distributionGrade.getSets();
        Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(sets);
        if (gradeMap.containsKey("direct_m_type"))
        {
            directMtype = new BigDecimal(gradeMap.get("direct_m_type").toString());
        }
        if (gradeMap.containsKey(DistributionGoodsModel.DistributionRuleKey.DIRECT_M))
        {
            directM = new BigDecimal(gradeMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M).toString());
            levelDirectM = directM;
        }
        //获取分销商品信息
        Map<String, Object> parmaMap = new HashMap<>(16);
        parmaMap.put("store_id", vo.getStoreId());
        parmaMap.put("goodsStatus", DictionaryConst.GoodsStatus.NEW_GROUNDING);
        parmaMap.put("goodsRecycle", DictionaryConst.ProductRecycle.NOT_STATUS);
        /*parmaMap.put("uplevel1", 0);*/
        if (!StringUtils.isEmpty(productTitle))
        {
            parmaMap.put("goodsName", productTitle);
        }
        if (cid != null && cid > 0)
        {
            parmaMap.put("goodsClassId", cid);
        }
        String sortType = queue == 1 ? DataUtils.Sort.DESC.toString() : DataUtils.Sort.ASC.toString();
        String sort     = "any_key";
        if ("price".equals(sortKey))
        {
            sort = "price_sort";
            parmaMap.put(sort, sortType);
        }
        else
        {
            parmaMap.put("any_sort", sortType);
            parmaMap.put(sort, sortKey);
        }
        parmaMap.put("pid_group", "pid_group");
        parmaMap.put("pageStart", vo.getPageNo());
        parmaMap.put("pageEnd", vo.getPageSize());
        ProductConfigModel productConfigModel = new ProductConfigModel();
        productConfigModel.setStore_id(vo.getStoreId());
        productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
        if (productConfigModel != null && productConfigModel.getIs_display_sell_put() == 0)
        {
            //不展示已售罄的商品
            parmaMap.put("stockNum", "stockNum");
        }
        List<Map<String, Object>> goodsList = distributionGoodsModelMapper.selectGoodsInfo(parmaMap);

        //分销商品对应的等级折扣 100 为无折扣
        BigDecimal                fxGoodsLevelDiscount = new BigDecimal(100);
        List<Map<String, Object>> goodsDisDiyRules     = null;

        for (Map<String, Object> map : goodsList)
        {
            //将直推佣金恢复成等级规则的直推佣金
            directM = levelDirectM;
            //是否是自定义规则
            if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM)) {
                goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(map.get("rules_set").toString());
                if (Objects.isNull(goodsDisDiyRules)) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                }

                //当前用户分销等级ID
                if (distributionGrade.getId() != null) {
                    int fxLevelId = distributionGrade.getId();
                    for (int i = 0; i < goodsDisDiyRules.size(); i++) {
                        Map setsMap = (Map) goodsDisDiyRules.get(i);
                        int tempGid = MapUtils.getInteger(setsMap, "id");
                        if (tempGid == fxLevelId) {
                            Double gradeDiydisCount = MapUtils.getDouble(setsMap, "diy_discount");
                            directMtype = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_MODE_TYPE)));
                            directM = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
                            fxGoodsLevelDiscount = BigDecimal.valueOf(gradeDiydisCount);
                            logger.info("商品->{} 对应的自定义分销折扣->{}", JSON.toJSONString(map), gradeDiydisCount);
                            break;
                        }
                    }
                }
            }
            //分销等级规则
            else if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_LEVEL)) {
                fxGoodsLevelDiscount = distributionGrade.getDiscount();
            }
            //分销价格
            BigDecimal fxPrice;
            //商品价格
            BigDecimal goodsPrice = new BigDecimal(map.get("price").toString());
            BigDecimal price      = BigDecimal.ZERO;
            //计算分销商折扣价
            if (user != null) {
//                    if (!Objects.isNull(goodsDisDiyRules) && fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0)
                if (fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0) {
                    price = fxGoodsLevelDiscount.multiply(goodsPrice).multiply(new BigDecimal(0.01));
                } else {
                    price = this.getGoodsPrice(vo.getStoreId(), user.getUser_id(), goodsPrice);
                }
            }
            else
            {
                price = goodsPrice;
            }
            //最高可获取的金额
            if (directMtype.compareTo(BigDecimal.ONE) == 0)
            {
                //固定值
                fxPrice = directM;
            }
            else
            {
                //商品pv值
                BigDecimal pv = new BigDecimal(MapUtils.getString(map, "pv"));
                //商品成本价
                BigDecimal costPrice = new BigDecimal(MapUtils.getString(map, "costprice"));
                //获取分润基值
                BigDecimal profit = this.getProfit(vo.getStoreId(), price, costPrice, null, BigDecimal.ONE, pv);
                //百分比
                fxPrice = profit.multiply(directM.multiply(new BigDecimal("0.01")));
            }
            if (BigDecimal.ZERO.compareTo(fxPrice) >= 0)
            {
                fxPrice = BigDecimal.ZERO;
            }
            //图片处理
            String imgUrl = publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId());
            map.put("imgurl", imgUrl);
            map.put("goodsPrice", goodsPrice);
            map.put("fx_price", fxPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("price", price.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        //4269 【JAVA开发环境】分销：移动端--商品部分规格设置自定义，部分规格设置分销等级，则哪个分享赚大用哪个
        // 同一个商品存在多个分销规格时，取最高的“分享赚”金额
        parmaMap.remove("pid_group");
        // 取消分组查询分销商品列表
        List<Map<String, Object>> moreGoodsList = distributionGoodsModelMapper.selectGoodsInfo(parmaMap);
        // 存储每个商品(p_id)的最高fx_price及其对应的price
        Map<Integer, Map<String, BigDecimal>> maxFxPriceMap = new HashMap<>();
        for (Map<String, Object> map : moreGoodsList)
        {
            //将直推佣金恢复成等级规则的直推佣金
            directM = levelDirectM;
            //是否是自定义规则
            if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM))
            {
                goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(map.get("rules_set").toString());
                if (Objects.isNull(goodsDisDiyRules))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                }

                //当前用户分销等级ID
                if (distributionGrade.getId() != null)
                {
                    int fxLevelId = distributionGrade.getId();
                    for (int i = 0; i < goodsDisDiyRules.size(); i++)
                    {
                        Map setsMap = (Map) goodsDisDiyRules.get(i);
                        int tempGid = MapUtils.getInteger(setsMap, "id");
                        if (tempGid == fxLevelId)
                        {
                            Double gradeDiydisCount = MapUtils.getDouble(setsMap, "diy_discount");
                            directMtype = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_MODE_TYPE)));
                            directM = new BigDecimal(String.valueOf(setsMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M)));
                            fxGoodsLevelDiscount = BigDecimal.valueOf(gradeDiydisCount);
                            logger.info("商品->{} 对应的自定义分销折扣->{}", JSON.toJSONString(map), gradeDiydisCount);
                            break;
                        }
                    }
                }
            }
            //分销等级规则
            else if (MapUtils.getInteger(map, "distribution_rule").equals(DistributionGoodsModel.DISTRIBUTION_RULE_LEVEL))
            {
                fxGoodsLevelDiscount = distributionGrade.getDiscount();
            }
            //分销价格
            BigDecimal fxPrice;
            //商品价格
            BigDecimal goodsPrice = new BigDecimal(map.get("price").toString());
            BigDecimal price      = BigDecimal.ZERO;
            //计算分销商折扣价
            if (user != null)
            {
//                    if (!Objects.isNull(goodsDisDiyRules) && fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0)
                if (fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0)
                {
                    price = fxGoodsLevelDiscount.multiply(goodsPrice).multiply(new BigDecimal(0.01));
                }
                else
                {
                    price = this.getGoodsPrice(vo.getStoreId(), user.getUser_id(), goodsPrice);
                }
            }
            else
            {
                price = goodsPrice;
            }
            //最高可获取的金额
            if (directMtype.compareTo(BigDecimal.ONE) == 0)
            {
                //固定值
                fxPrice = directM;
            }
            else
            {
                //商品pv值
                BigDecimal pv = new BigDecimal(MapUtils.getString(map, "pv"));
                //商品成本价
                BigDecimal costPrice = new BigDecimal(MapUtils.getString(map, "costprice"));
                //获取分润基值
                BigDecimal profit = this.getProfit(vo.getStoreId(), price, costPrice, null, BigDecimal.ONE, pv);
                //百分比
                fxPrice = profit.multiply(directM.multiply(new BigDecimal("0.01")));
            }
            if (BigDecimal.ZERO.compareTo(fxPrice) >= 0)
            {
                fxPrice = BigDecimal.ZERO;
            }
            map.put("goodsPrice", goodsPrice);
            map.put("fx_price", fxPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
            map.put("price", price.setScale(2, BigDecimal.ROUND_HALF_UP));

            // 获取当前规格的p_id
            Integer pId = MapUtils.getInteger(map, "p_id");
            if (pId == null) continue;

            // 获取当前计算的fx_price和price
            BigDecimal currentFxPrice = fxPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal currentPrice   = price.setScale(2, BigDecimal.ROUND_HALF_UP);

            // 检查是否已存在该商品的记录
            Map<String, BigDecimal> priceInfo = maxFxPriceMap.get(pId);
            if (priceInfo == null)
            {
                // 首次记录
                priceInfo = new HashMap<>();
                priceInfo.put("fx_price", currentFxPrice);
                priceInfo.put("price", currentPrice);
                maxFxPriceMap.put(pId, priceInfo);
            }
            else
            {
                // 比较并保留最高fx_price
                BigDecimal existingFxPrice = priceInfo.get("fx_price");
                if (currentFxPrice.compareTo(existingFxPrice) > 0)
                {
                    priceInfo.put("fx_price", currentFxPrice);
                    priceInfo.put("price", currentPrice);
                }
            }
        }
        // 遍历goodsList，替换为最高fx_price对应的值
        for (Map<String, Object> map : goodsList)
        {
            Integer pId = MapUtils.getInteger(map, "p_id");
            if (pId != null)
            {
                Map<String, BigDecimal> priceInfo = maxFxPriceMap.get(pId);
                if (priceInfo != null)
                {
                    // 替换为同一商品的最高fx_price对应的值
                    map.put("fx_price", priceInfo.get("fx_price"));
                    map.put("price", priceInfo.get("price"));

                    // 更新商品价格显示（如果需要）
//                        map.put("goodsPrice", new BigDecimal(map.get("price").toString()));
                }
            }
        }
        //4269 结束 ===

        resultMap.put("pro", goodsList);
        return resultMap;
    }
}

