package com.laiketui.apps.app.services.integral;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.app.integral.AppsCstrIntegralService;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.order.OrderDubboService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.IntegralConfigModel;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.OrderVo;
import com.laiketui.domain.vo.dic.DicVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 积分商城实现
 *
 * @author Trick
 * @date 2021/4/14 10:12
 */
@Service
public class AppsCstrIntegralServiceImpl implements AppsCstrIntegralService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;

    @Autowired
    private CommentsModelMapper commentsModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public Map<String, Object> getSignInfo(MainVo vo, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("sign_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            //类型: 0:签到 1:消费 2:首次关注得积分 3:转积分给好友 4:好友转积分 5:系统扣除 6:系统充值 7:抽奖 8:会员购物积分 9:分销升级奖励积分 10:积分过期
            if (type != null)
            {
                Integer[] typeList = null;
                if (type == 1)
                {
                    //获得积分明细
                    typeList = new Integer[]{0, 2, 4, 6, 8, 11};
                }
                else if (type == 2)
                {
                    //使用明细
                    typeList = new Integer[]{1, 3, 5, 7, 10};
                }
                parmaMap.put("typeList", typeList);
                List<Map<String, Object>> integralListTemp = signRecordModelMapper.selectDynamic(parmaMap);
                resultMap.put("list", getIntegralList(integralListTemp));
            }
            else
            {
                //获取全部
                List<Map<String, Object>> whole = signRecordModelMapper.selectDynamic(parmaMap);
                if (vo.getPageNo() == 0)
                {
                    //使用明细
                    parmaMap.put("typeList", new Integer[]{1, 3, 5, 7});
                    List<Map<String, Object>> consumption = signRecordModelMapper.selectDynamic(parmaMap);
                    //获取明细
                    parmaMap.put("typeList", new Integer[]{0, 2, 4, 6, 8, 11});
                    List<Map<String, Object>> sign = signRecordModelMapper.selectDynamic(parmaMap);

                    resultMap.put("consumption", getIntegralList(consumption));
                    resultMap.put("sign", getIntegralList(sign));
                }


                resultMap.put("score", user.getScore());
                resultMap.put("whole", getIntegralList(whole));
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的积分列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSignInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> instructions(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(vo.getStoreId());
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);

            String text = "";
            if (integralConfigModel != null)
            {
                text = integralConfigModel.getContent();
            }
            resultMap.put("text", text);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取我的积分使用说明 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "instructions");
        }
        return resultMap;
    }

    private List<Map<String, Object>> getIntegralList(List<Map<String, Object>> integralListTemp)
    {
        //获取积分类型字典
        Map<String, String> integralNameMap = new HashMap<>(16);
        DicVo               dicVo           = new DicVo();
        dicVo.setName("积分类型");
        Map<String, Object> dicType = publicDictionaryService.getDictionaryByName(dicVo);
        if (dicType != null)
        {
            if (dicType.containsKey("value"))
            {
                List<DictionaryListModel> dicValueList = DataUtils.cast(dicType.get("value"));
                if (dicValueList != null)
                {
                    for (DictionaryListModel dicValue : dicValueList)
                    {
                        integralNameMap.put(dicValue.getValue(), dicValue.getCtext());
                    }
                }
            }
        }

        //消费积分分类
        List<String>              consumptionList = Arrays.asList("1", "3", "5", "7", "10");
        List<Map<String, Object>> integralList    = new ArrayList<>();
        for (Map<String, Object> map : integralListTemp)
        {
            String typeTemp = MapUtils.getString(map, "type");
            //false 代表负数
            boolean             status  = !consumptionList.contains(typeTemp);
            Map<String, Object> dataMap = new HashMap<>(16);
            dataMap.put("status", status);
            dataMap.put("name", integralNameMap.get(typeTemp));
            dataMap.put("sNo", MapUtils.getString(map, "sNo"));
            dataMap.put("sign_score", MapUtils.getString(map, "sign_score"));
            dataMap.put("sign_time", MapUtils.getString(map, "sign_time"));
            dataMap.put("type", typeTemp);
            integralList.add(dataMap);
        }
        return integralList;
    }

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            //积分商城首页图
            String imgUrlList = "";
            //积分商品集
            List<Map<String, Object>> resultList = new ArrayList<>();
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            int total = integralGoodsModelMapper.countGoodsInfo(parmaMap);
            if (total > 0)
            {
                resultList = integralGoodsModelMapper.getGoodsInfo(parmaMap);
            }
            for (Map<String, Object> map : resultList)
            {
                map.put("imgurl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
                map.put("sales", map.get("volume"));
                //售价
                String              goodsInfoStr = MapUtils.getString(map, "initial");
                Map<String, Object> goodsInfoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(goodsInfoStr, Map.class));
                if (goodsInfoMap != null)
                {
                    map.put("price", new BigDecimal(MapUtils.getString(goodsInfoMap, "sj")));
                }
            }
            //获取积分商城轮播图
//            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
//            integralConfigModel.setStore_id(vo.getStoreId());
//            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            imgUrlList = "";
            //获取用户积分
            int userScore = 0;
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (user != null)
            {
                userScore = user.getScore();
            }
            //签到模块是否开启
            int isOpen = publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.SIGN, null) ? 1 : 0;

            resultMap.put("go_sign", isOpen);
            resultMap.put("score", userScore);
            resultMap.put("list", resultList);
            resultMap.put("total", total);
            resultMap.put("bg_img", imgUrlList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取积分商城商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> rule(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            IntegralConfigModel integralConfigModel = new IntegralConfigModel();
            integralConfigModel.setStore_id(vo.getStoreId());
            integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
            resultMap.put("content", integralConfigModel.getContent());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取积分规则 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "rule");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> goodsdetail(MainVo vo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //积分商城模块是否开启
            boolean isOpen = publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.INTEGRAL, null);
            //商品评论集
            List<Map<String, Object>> goodsCommentList = new ArrayList<>();
            //评论总数量
            int commentNum = 0;
            //店铺信息
            Map<String, Object> shopInfo = new HashMap<>(16);
            //获取商品信息
            Map<String, Object> goodsDetailInfo = integralGoodsModelMapper.getGoodsDetailInfoById(id);
            if (goodsDetailInfo != null)
            {
                goodsDetailInfo.put("imgurl", publiceService.getImgPath(goodsDetailInfo.get("imgurl").toString(), vo.getStoreId()));
                //商品售价价格
                Map<String, Object> goodsInfo  = DataUtils.cast(SerializePhpUtils.getUnserializeObj(MapUtils.getString(goodsDetailInfo, "initial"), Map.class));
                BigDecimal          goodsPrice = new BigDecimal(Objects.requireNonNull(MapUtils.getString(goodsInfo, "sj")));
                goodsDetailInfo.put("price", goodsPrice);
                //获取店铺信息
                Integer mchId = StringUtils.stringParseInt(goodsDetailInfo.get("mch_id"));
                if (mchId != null)
                {
                    MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                    if (mchModel != null)
                    {
                        shopInfo.putAll(publiceService.commodityInformation(vo.getStoreId(), mchId,null));
                        shopInfo.put("shop_id", mchId);
                        shopInfo.put("shop_name", mchModel.getName());
                        shopInfo.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                    }
                }
                //获取商品评论数据 只加载一条评论数据
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("pid", goodsDetailInfo.get("goodsId"));
                parmaMap.put("pageNo", 0);
                parmaMap.put("pageSize", 1);
                goodsCommentList = commentsModelMapper.getCommentsUserDynamicByPid(parmaMap);
                commentNum = commentsModelMapper.countCommentsUserDynamicByPid(parmaMap);
            }

            resultMap.put("shop_list", shopInfo);
            resultMap.put("isopen", isOpen ? 1 : 0);
            resultMap.put("goods", goodsDetailInfo);
            resultMap.put("comm_count", commentNum);
            resultMap.put("comments", goodsCommentList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取积分商品明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "goodsdetail");
        }
        return resultMap;
    }

    @Autowired
    @Qualifier("integralOrderHandler")
    private OrderDubboService integralOrderServiceImpl;

    @Override
    public Map<String, Object> integralOrder(MainVo vo, String orderType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            OrderVo orderVo = JSON.parseObject(JSON.toJSONString(vo), new TypeReference<OrderVo>()
            {
            });
            orderVo.setStoreId(vo.getStoreId());
            orderVo.setType(orderType);
            resultMap = integralOrderServiceImpl.orderList(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        return resultMap;
    }
}

