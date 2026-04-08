package com.laiketui.comps.task.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.algorithm.DataAlgorithmTool;
import com.laiketui.common.utils.txun.KCPlayerUtils;
import com.laiketui.comps.api.task.CompsTaskService;
import com.laiketui.comps.api.task.plugin.CompsTaskSignService;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.distribution.DistributionIncomeModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.plugin.bbs.BbsConfigModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.user.User;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 所有商城定时任务
 *
 * @author Trick
 * @date 2020/12/14 15:29
 */
@Service
public class CompsTaskServiceImpl implements CompsTaskService
{

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CompsTaskSignService signTaskService;

    private static final int PAGE_SIZE = 10000;  // 每次处理的最大条数
    private static final int BATCH_SIZE = 20;   // 每次调用的最大 URL 数量


    @Override
    public void taskStoreAll() throws LaiKeAPIException
    {
        List<ConfigModel> configModel   = null;
        CustomerModel     customerModel = null;
        try
        {
            XxlJobHelper.log("缓存商城id 开始执行!");
            if (redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
                List<Integer> storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
                {
                });
                XxlJobHelper.log("有效商城id：：：：{}",storeIdList);
                XxlJobHelper.log("商城id已存在缓存 获取缓存 下次刷新商城id剩余:{}!", redisUtil.getExpire(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST));
            }
            else
            {
                XxlJobHelper.log("商城id已存在缓存 开始初始化缓存!");
                //获取所有商城
                configModel = configModelMapper.getConfigAllInfo();
                Set<Integer> storeIdList = new HashSet<>();
                for (ConfigModel config : configModel)
                {
                    //只有正常的店铺才走流程
                    customerModel = new CustomerModel();
                    customerModel.setId(config.getStore_id());
                    customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    customerModel.setStatus(0);
                    customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_OPEN);
                    customerModel = customerModelMapper.selectOne(customerModel);
                    if (customerModel != null)
                    {
                        storeIdList.add(customerModel.getId());
                    }
                    else
                    {
                        XxlJobHelper.log("商城被锁定或者已到期 商城id:{}", config.getId());
                    }
                }
                //缓存storeId集
                redisUtil.set(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST, JSON.toJSONString(storeIdList), GloabConst.LktConfig.LOGIN_STORE_ID_EXISTENCE_TIME);
            }

            XxlJobHelper.log("缓存商城id 执行完毕!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("商城定时任务 异常: " + e.getMessage());
        }
        finally
        {
            configModel = null;
            customerModel = null;
        }
    }

    @Override
    public List<Integer> getStoreIdAll() throws LaiKeAPIException
    {
        try
        {
            XxlJobHelper.log("缓存所有商城id 开始执行!");
            if (!redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {

                taskStoreAll();
            }
            String json = redisUtil.get(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST).toString();
            List<Integer> storeIdList = JSON.parseObject(json, new TypeReference<List<Integer>>()
            {
            });
            XxlJobHelper.log("获取到商城id数量{}", storeIdList.size());
            return storeIdList;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("获取商城id 异常: " + e.getMessage());
        }
        XxlJobHelper.log("商城id缓存为空");
        throw new LaiKeAPIException("商城id缓存为空");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMessageDay() throws LaiKeAPIException
    {
        ConfigModel   configModel = null;
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("定时清空消息 开始执行!");
            storeIdList = getStoreIdAll();

            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("===== 商城id{} 开始清空消息 ======", storeId);
                //获取商城配置信息
                configModel = new ConfigModel();
                configModel.setStore_id(storeId);
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null)
                {
                    Integer day = configModel.getMessage_day();
                    if (day != null && day > 0)
                    {
                        //到期时间
                        Date endDate = DateUtil.getAddDate(-day);
                        XxlJobHelper.log("该商城消息保留的天数:{},消息过期时间{}", day, DateUtil.dateFormate(endDate, GloabConst.TimePattern.YMDHMS));
                        int count = systemMessageModelMapper.delMessageExpire(storeId, endDate);
                        XxlJobHelper.log("【总共清除{}条消息】", count);
                    }
                    else
                    {
                        XxlJobHelper.log("【该商城不删除消息】");
                    }
                }
                else
                {
                    XxlJobHelper.log("【该商城未配置信息】");
                }
                XxlJobHelper.log("===== 商城id{} 清空消息完成 ======", storeId);
            }
            XxlJobHelper.log("定时清空消息 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("定时清空消息 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
            configModel = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resettingPwdNum() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("定时重置密码次数 开始执行!");
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("===== 商城id{} 开始重置密码次数 ======", storeId);
                int count = userBaseMapper.resettingPwdNum(storeId, DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
                XxlJobHelper.log(">>>> 重置成功,重置数量:{} <<<<", count);
            }
            XxlJobHelper.log("定时重置密码次数 执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.log("定时重置密码次数 异常: " + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC,e.getMessage());
        }
        finally
        {
            storeIdList = null;
        }
    }


    /**
     * 获取商品明细
     *
     * @param dom -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/6 10:58
     */
    private String getGoodsContext(Document dom) throws LaiKeAPIException
    {
        StringBuilder content = new StringBuilder();
        try
        {
            String              pattern   = "(?s)apiImgInfo(.*?):(.*?),";
            Pattern             r         = Pattern.compile(pattern);
            Matcher             m         = r.matcher(dom.getElementsByTag("script").html());
            String              imgUrlApi = "https://gd1.alicdn.com/imgextra/%s";
            Map<String, Object> imgUrlMap = new HashMap<>(16);
            while (m.find())
            {
                String              imgUrlApiJson = "{" + StringUtils.trim(m.group()).replace(",", "}");
                Map<String, String> imgUrlApiMap  = new HashMap<>(16);
                imgUrlApiMap = JSON.parseObject(imgUrlApiJson, new TypeReference<Map<String, String>>()
                {
                });
                String goodsImgUrlApi = GloabConst.HttpProtocol.HTTPS + ":" + imgUrlApiMap.get("apiImgInfo");
                String resultJson     = HttpUtils.get(goodsImgUrlApi);
                resultJson = resultJson.replace("$callback(", "");
                resultJson = resultJson.substring(0, resultJson.length() - 1);
                imgUrlMap = JSON.parseObject(resultJson, new TypeReference<Map<String, Object>>()
                {
                });
            }
            content.append("<p>");
            for (String key : imgUrlMap.keySet())
            {
                if (key.length() > 10)
                {
                    content.append("<img src=\"").append(String.format(imgUrlApi, key)).append("\">");
                }
            }
            content.append("</p>");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail(">>>>>>>抓取商品详情 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGKCBCSB, "商品规格库存保存失败", "getGoodsContext");
        }
        return content.toString();
    }

    public static void main(String[] args) throws IOException
    {

        String   url      = String.format(GloabConst.OtherUrl.API_TAOBAO_GOODSDATA_URL, "601086843322");
        Document document = Jsoup.connect(url).get();

        Elements goodsAttrDom = document.select("#J_isku>.tb-skin>dl>dd>ul");
        //获取库存规格信息
        String  pattern = "(?s)skuMap(.*?):(.*?)}\\);";
        Pattern r       = Pattern.compile(pattern);
        Matcher m       = r.matcher(document.html());
        while (m.find())
        {
            XxlJobHelper.log(">>>>>网页爬取开始<<<<<");
            String skuMapStr = "{" + StringUtils.trim(m.group());
            skuMapStr = skuMapStr.replace("skuMap", "\"skuMap\"");
            skuMapStr = skuMapStr.replace("propertyMemoMap", "\"propertyMemoMap\"");
            skuMapStr = skuMapStr.substring(0, skuMapStr.length() - 3);
            //爬取的数据结构 - 所有规格排列组合信息
            Map<String, Map<String, Object>> skuJsonMap = JSON.parseObject(skuMapStr, new TypeReference<Map<String, Map<String, Object>>>()
            {
            });
            //商品规格信息 结构: {;节点;节点;节点... : {价格信息}}
            Map<String, Object> skuMap = skuJsonMap.get("skuMap");

            String[][] groupNodes = new String[goodsAttrDom.size()][];
            int        x          = 0;
            for (Element attrNameAll : goodsAttrDom)
            {
                XxlJobHelper.log(">>>网页父节点{}爬取开始<<<", x);
                //属性名称
                String attrName = attrNameAll.attr("data-property");
                //属性值节点
                Elements goodsAttrValueDom = attrNameAll.select("li");
                //属性集合
                Map<String, Object> attrMap = new HashMap<>(16);
                //节点id集合
                List<String> nodeIdAllList = new ArrayList<>();
                //属性名称_LKT_属性id
                String attributeStr = "%s_LKT_%s";

                //准备添加属性
                SkuModel saveSkuModel = new SkuModel();
                saveSkuModel.setAdmin_name("task");
                saveSkuModel.setStore_id(1);
                //添加属性值
                int i = 0;
                for (Element attrValueAll : goodsAttrValueDom)
                {
                    XxlJobHelper.log(">>当前网页节点{}爬取开始<<", i);
                    String attrValue = attrValueAll.select("a>span").text();
                    //节点id
                    String nodeId = attrValueAll.attr("data-value");
                    //子集 属性在sku表里是否存在
                    nodeIdAllList.add(nodeId + JSON.toJSONString(attrMap));
                    XxlJobHelper.log(">>当前网页节点{}爬取完成<<", i);
                    i++;
                }
                //解决组合少一个的情况
                if (x == 0)
                {
                    nodeIdAllList.add("");
                }
                groupNodes[x] = nodeIdAllList.toArray(new String[0]);
                XxlJobHelper.log(">>>网页父节点{}爬取完成<<<", x);
                x++;
            }
            XxlJobHelper.log(">>>>>网页爬取完成<<<<<");
            XxlJobHelper.log(">>>>>商品发布开始<<<<<");

            //获取所有可能的组合价格信息
            List<List<String>> resultList = new ArrayList<>();
            DataAlgorithmTool.combination(groupNodes, new String[groupNodes.length], 0, 0, resultList);
            if (resultList.size() > 0)
            {
                resultList.remove(resultList.size() - 1);
            }
            //添加规格库存信息
            for (List<String> groupNodeId : resultList)
            {
                StringBuilder nodeIdstr = new StringBuilder();
                //因为网页里是用‘;’来分隔做key
                nodeIdstr.append(";");
                for (String nodeId : groupNodeId)
                {
                    nodeIdstr.append(nodeId).append(";");
                }
                //拆分
                Map<String, String> nodeIdMap = StringUtils.getSplit(nodeIdstr.toString(), "{", "}", ",");
                for (String key : nodeIdMap.keySet())
                {
                    String   attrImgId = StringUtils.trim(key, ";");
                    String[] tempIds   = attrImgId.split(";");
                    attrImgId = tempIds[1];
                    String tempUrl = document.select(String.format("#J_isku>.tb-skin>dl>dd>ul>li[data-value='%s']>a", attrImgId)).attr("style");
                    System.out.println(tempUrl.trim());

                    String  pattern1 = "\\((.*?)\\)";
                    Pattern r1       = Pattern.compile(pattern1);
                    Matcher m1       = r1.matcher(tempUrl);
                    while (m1.find())
                    {
                        String path           = m1.group();
                        String attrImgUrlPath = GloabConst.HttpProtocol.HTTPS + ":" + path.substring(1, path.length() - 1);
                        attrImgUrlPath = attrImgUrlPath.replace(".jpg_30x30", ".jpg_200x200");
                        System.out.println(attrImgUrlPath);
                    }

                    String valueJson = "[" + nodeIdMap.get(key) + "]";
                    XxlJobHelper.log(">>>添加规格库存开始 {}<<<", nodeIdMap.get(key));
                    //找到当前id价格信息
                    if (skuMap.containsKey(key))
                    {
                        //获取属性
                        List<Map<String, String>> attributeList = JSON.parseObject(valueJson, new TypeReference<List<Map<String, String>>>()
                        {
                        });
                        Map<String, String> attributeMap = new HashMap<>(16);
                        for (Map<String, String> map : attributeList)
                        {
                            attributeMap.putAll(map);
                        }
                        //获取当前属性节点价格信息
                        Map<String, Object> goodsStockMap = JSON.parseObject(String.valueOf(skuMap.get(key)), new TypeReference<Map<String, Object>>()
                        {
                        });
                        BigDecimal price = new BigDecimal(String.valueOf(goodsStockMap.get("price")));
                        String     num   = String.valueOf(goodsStockMap.get("stock"));
                        //添加属性
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setCostprice(price);
                        confiGureModel.setPrice(price);
                        confiGureModel.setYprice(price);
                        confiGureModel.setImg("图片url");
                        confiGureModel.setMin_inventory(10);
                        confiGureModel.setTotal_num(Integer.parseInt(num));
                        confiGureModel.setNum(Integer.parseInt(num));
                        confiGureModel.setUnit("件");
                        confiGureModel.setBargain_price(price);
                        confiGureModel.setStatus("0");
                        confiGureModel.setColor("");
                        confiGureModel.setAttribute(SerializePhpUtils.JavaSerializeByPhp(attributeMap));
                        confiGureModel.setCtime(new Date());
                        //添加库存
                        StockModel stockModel = new StockModel();
                        stockModel.setAttribute_id(confiGureModel.getId());
                        stockModel.setFlowing_num(confiGureModel.getNum());
                        stockModel.setTotal_num(confiGureModel.getTotal_num());
                        stockModel.setContent("添加库存");
                        stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                        if (confiGureModel.getMin_inventory() >= confiGureModel.getNum())
                        {
                            stockModel.setContent("库存预警");
                            stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                        }
                        stockModel.setAdd_date(new Date());

                        XxlJobHelper.log(">>>添加规格库存成功 id={}<<<", confiGureModel.getId());
                    }
                    else
                    {
                        XxlJobHelper.log(">>>未获取到当前节点价格信息 节点id={}<<<", key);
                    }
                }
                XxlJobHelper.log(">>>>添加规格库存结束<<<<");
            }
        }

    }

    @Override
    public void signTask() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("定时处理签到 开始执行!");
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("===== 商城id{} 定时处理签到 ======", storeId);
                signTaskService.task(storeId);
                XxlJobHelper.log(">>>> 签到活动处理完毕 <<<<");
            }
            XxlJobHelper.log("定时处理签到 执行完毕!");
            XxlJobHelper.handleSuccess();
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail(">>>>>>>定时处理签到 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSCLQDYC, "定时处理签到 异常", "getGoodsContext");
        }
        finally
        {
            storeIdList = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settlementIntegral() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        Date          sysDate     = null;
        try
        {
            XxlJobHelper.log("定时结算积分 开始执行!");
            int row;
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("==================== 商城id={} 定时结算积分 ====================", storeId);
                sysDate = new Date();
                //积分解冻结算
                row = signRecordModelMapper.setFrozenIntegral(storeId, sysDate, SignRecordModel.ScoreType.INTEGRAL_FROZEN, SignRecordModel.ScoreType.VIP_BUY);
                XxlJobHelper.log("积分解冻结算 执行结果:{}", row);
                //积分解冻-状态更改
                //row = signRecordModelMapper.setFrozenIntegralStatus(storeId, sysDate, SignRecordModel.ScoreType.INTEGRAL_FROZEN, SignRecordModel.ScoreType.VIP_BUY);
                //4129 【JAVA开发环境】积分：移动端--收货后没有售后正常解冻的积分应该有3条记录，但是现在只有1条用户购物的记录
                List<SignRecordModel> frozenIntegralRecords = signRecordModelMapper.getFrozenIntegralRecords(storeId, sysDate, SignRecordModel.ScoreType.INTEGRAL_FROZEN);
//                for (SignRecordModel signRecordModel : frozenIntegralRecords)
//                {
//                    //新增积分解冻记录
//                    signRecordModel.setId(null);
//                    signRecordModel.setRecord("冻结积分解冻");
//                    signRecordModel.setType(SignRecordModel.ScoreType.INTEGRAL_UNFREEZE);
//                    signRecordModel.setSign_time(sysDate);
//                    signRecordModelMapper.insert(signRecordModel);
//                    //新增用户购物记录
//                    signRecordModel.setId(null);
//                    signRecordModel.setRecord("用户购物");
//                    signRecordModel.setType(SignRecordModel.ScoreType.VIP_BUY);
//                    signRecordModel.setSign_time(sysDate);
//                    signRecordModelMapper.insert(signRecordModel);
//                }
                for (SignRecordModel source : frozenIntegralRecords)
                {
                    // 积分解冻记录（新对象）
                    SignRecordModel thawRecord = new SignRecordModel();
                    BeanUtils.copyProperties(source, thawRecord);
                    thawRecord.setId(null);
                    thawRecord.setRecord("冻结积分解冻");
                    thawRecord.setType(SignRecordModel.ScoreType.INTEGRAL_UNFREEZE);
                    thawRecord.setSign_time(sysDate);
                    signRecordModelMapper.insert(thawRecord);

                    // 用户购物记录（新对象）
                    SignRecordModel shoppingRecord = new SignRecordModel();
                    BeanUtils.copyProperties(source, shoppingRecord);
                    shoppingRecord.setId(null);
                    shoppingRecord.setRecord("用户购物");
                    shoppingRecord.setType(SignRecordModel.ScoreType.VIP_BUY);
                    shoppingRecord.setSign_time(sysDate);
                    signRecordModelMapper.insert(shoppingRecord);
                }
                row = signRecordModelMapper.clearFrozenTime(storeId, sysDate, SignRecordModel.ScoreType.INTEGRAL_FROZEN);
                XxlJobHelper.log("积分解冻-更改状态 执行结果:{}", row);
                //结算扣除用户过期积分
                row = signRecordModelMapper.settlementIntegral(storeId, sysDate);
                XxlJobHelper.log("扣除用户过期积分 执行结果:{}", row);
                //失效积分
                row = signRecordModelMapper.userInvalidScore(sysDate);
                XxlJobHelper.log("失效积分 执行结果:{}", row);
                XxlJobHelper.log("==================== 定时结算积分执行完成 ====================");
            }
        }
        catch (Exception e)
        {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>定时结算积分 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "定时结算积分 异常", "settlementIntegral");
        }
        finally
        {
            sysDate = null;
            storeIdList = null;
        }
    }


    @Override
    public void cancellationShop() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("定时清理店铺 开始执行!");
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("==================== 商城id={} 定时清理店铺 ====================", storeId);
                //获取商城设置的删除时间 0 代表永不删除
                MchConfigModel mchConfigModel = null;
                try
                {
                    mchConfigModel = publicMchService.getMchConfig(storeId, customerModelMapper.getStoreMchId(storeId));
                }
                catch (LaiKeAPIException l)
                {
                    XxlJobHelper.log("====== 商城id={} 自营店设置不存在 =====", storeId);
                }
                if (mchConfigModel != null && mchConfigModel.getDelete_settings() != 0)
                {
                    XxlJobHelper.log("====== 商城id={} 设置的时间为{}个月未登录的店铺 =====", storeId, mchConfigModel.getDelete_settings());
                    //获取当前商城除了自营店其它所有店铺
                    List<Integer> mchIdList = mchModelMapper.getMchIdListByStoreId(storeId, mchConfigModel.getDelete_settings());
                    for (Integer mchId : mchIdList)
                    {
                        XxlJobHelper.log("= 店铺{}主人已经{}个月没登陆了,开始清理... =", mchId, mchConfigModel.getDelete_settings());
                        String result = GloabConst.ManaValue.MANA_VALUE_SUCCESS;
                        try
                        {
                            publicMchService.cancellationShop(storeId, mchId);
                        }
                        catch (Exception e)
                        {
                            result = e.getMessage();
                            XxlJobHelper.log("清理失败,原因:{}", result);
                        }
                        XxlJobHelper.log("定时删除店铺 店铺id{} 执行结果:{}", mchId, result);
                        result = null;
                    }
                    mchIdList = null;
                }
                else
                {
                    XxlJobHelper.log("商城{} 没有设置定时删除店铺", storeId);
                }
                mchConfigModel = null;
                XxlJobHelper.log("==================== 定时清理店铺 执行完成 ====================");
            }

        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail(">>>>>>>定时清理店铺 【异常失败】<<<<<<<", e);
            XxlJobHelper.handleFail(">>>>>>>定时清理店铺 【异常失败】<<<<<<<");
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSQLDPYC, "定时清理店铺 异常", "cancellationShop");
        }
        finally
        {
            storeIdList = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void memberExpirationProcessing() throws LaiKeAPIException
    {
        User       user   = null;
        List<User> select = null;
        try
        {
            XxlJobHelper.log("会员过期处理定时任务 开始执行!");
            user = new User();
            user.setGrade(User.MEMBER);
            select = userBaseMapper.select(user);
            select.stream().forEach(userBase ->
            {
                XxlJobHelper.log("==================== 正在处理会员用户{} ====================", userBase.getUser_id());
                boolean compare = DateUtil.dateCompare(new Date(), userBase.getGrade_end());
                if (compare)
                {
                    XxlJobHelper.log("==================== 会员用户{}已过期 ====================", userBase.getUser_id());
                    int                i                 = userBaseMapper.memberExpirationProcessing(userBase.getStore_id(), userBase.getUser_id());
                    SystemMessageModel systemMessageSave = new SystemMessageModel();
                    systemMessageSave.setType(SystemMessageModel.ReadType.UNREAD);
                    systemMessageSave.setSenderid("admin");
                    systemMessageSave.setStore_id(userBase.getStore_id());
                    systemMessageSave.setRecipientid(userBase.getUser_id());
                    systemMessageSave.setTitle("系统消息");
                    systemMessageSave.setContent("您的会员已到期，续费后可继续享受会员权益！");
                    systemMessageSave.setTime(new Date());
                    systemMessageModelMapper.insertSelective(systemMessageSave);
                    XxlJobHelper.log("会员用户{} 执行结果:{}", userBase.getUser_id(), i);
                }
                else
                {
                    XxlJobHelper.log("==================== 会员用户{}未过期 ====================", userBase.getUser_id());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>会员过期处理定时任务 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "会员过期处理定时任务 异常", "memberExpirationProcessing");
        }
        finally
        {
            user = null;
            select = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mchAutoExamine() throws LaiKeAPIException
    {
        List<Integer>  storeIdList    = null;
        MchConfigModel mchConfigModel = null;
        try
        {
            XxlJobHelper.log("店铺自动审核成功处理定时任务 开始执行!");
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("==================== 商城id={} 店铺自动审核开始 ====================", storeId);
                //获取商城设置的删除时间 0 代表永不删除
                mchConfigModel = new MchConfigModel();
                mchConfigModel.setStore_id(storeId);
                mchConfigModel.setMch_id(customerModelMapper.getStoreMchId(storeId));
                mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                if (mchConfigModel != null && mchConfigModel.getAuto_examine() != 0)
                {
                    XxlJobHelper.log("====== 商城id={} 设置的时间为{}天店铺自动审核成功 =====", storeId, mchConfigModel.getAuto_examine());
                    Date expireDate = DateUtil.getAddDate(-mchConfigModel.getAuto_examine());
                    //获取当前商城除了自营店其它所有店铺
                    List<Integer> mchIdList = mchModelMapper.getAutoExamineMchId(storeId, expireDate);
                    for (Integer mchId : mchIdList)
                    {
                        XxlJobHelper.log("--------店铺id{}申请时间已超过{}天,系统将自动审核成功---------", mchId, mchConfigModel.getAuto_examine());
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                        mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
                        mchModelMapper.updateByPrimaryKeySelective(mchModel);
                        XxlJobHelper.log("--------店铺id{}自动审核成功--------", mchId);
                        mchModel = null;
                    }
                }
                else
                {
                    XxlJobHelper.log("商城{} 没有设置店铺自动审核天数", storeId);
                }

                XxlJobHelper.log("==================== 店铺自动审核成功 执行完成 ====================");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>店铺自动审核成功处理定时任务 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "店铺自动审核成功处理定时任务 异常", "mchAutoExamine");
        }
        finally
        {
            storeIdList = null;
            mchConfigModel = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mchAutoLogOff() throws LaiKeAPIException
    {
        List<Integer>    storeIdList      = null;
        MchConfigModel   mchConfigModel   = null;
        JumpPathModel    jumpPathModel    = null;
        ProductListModel productListModel = null;
        try
        {
            XxlJobHelper.log("店铺自动注销处理定时任务 开始执行!");
            storeIdList = getStoreIdAll();


            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("==================== 商城id={} 店铺自动注销开始 ====================", storeId);
                Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
                //获取商城设置的自动注销时间 为0代表无限制
                mchConfigModel = new MchConfigModel();
                mchConfigModel.setStore_id(storeId);
                mchConfigModel.setMch_id(storeMchId);
                mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                if (mchConfigModel != null && mchConfigModel.getAuto_log_off() > 0)
                {
                    XxlJobHelper.log("====== 商城id={} 设置的时间为{}个月店铺未登录自动注销 =====", storeId, mchConfigModel.getAuto_log_off());
                    //天数
                    int    autoLogOff = mchConfigModel.getAuto_log_off() * 30;
                    String logOffDate = DateUtil.dateFormate(DateUtil.getAddDate(-autoLogOff), GloabConst.TimePattern.YMDHMS);
                    //获取当前商城除了自营店其它所有店铺
                    List<Integer> mchIdList = mchModelMapper.getAutoLogOffMchId(storeId, logOffDate);
                    for (Integer mchId : mchIdList)
                    {
                        //自营店无需注销
                        if (mchId.equals(storeMchId))
                        {
                            continue;
                        }
                        XxlJobHelper.log("--------店铺id{}已超过{}个月未登录,系统将自动注销---------", mchId, mchConfigModel.getAuto_log_off());
                        MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                        mchModel.setIs_lock(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK));
                        mchModelMapper.updateByPrimaryKeySelective(mchModel);
                        //删除跳转地址
                        jumpPathModel = new JumpPathModel();
                        jumpPathModel.setSid(mchModel.getId() + "");
                        jumpPathModel.setType0(JumpPathModel.JumpType.JUMP_TYPE0_MCH);
                        jumpPathModelMapper.delete(jumpPathModel);
                        //删除店铺商品
                        productListModel = new ProductListModel();
                        productListModel.setStore_id(storeId);
                        productListModel.setMch_id(mchId);
                        List<ProductListModel> productListModelList = productListModelMapper.select(productListModel);
                        for (ProductListModel goods : productListModelList)
                        {
                            goods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                            productListModelMapper.updateByPrimaryKeySelective(goods);
                        }
                        //删除对应优惠券
                        couponActivityModelMapper.delCouponByMchId(storeId, mchId);
                    }

                    logOffDate = null;

                }
                else
                {
                    XxlJobHelper.log("=======商城{}没有设置店铺自动注销跳过======", storeId);
                    continue;
                }

                XxlJobHelper.log("==================== 商城id={}店铺自动注销执行完成 ====================", storeId);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>店铺自动注销处理定时任务 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "店铺自动注销处理定时任务 异常", "mchAutoLogOff");
        }
        finally
        {
            storeIdList = null;
            mchConfigModel = null;
            jumpPathModel = null;
            productListModel = null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeExpiration() throws LaiKeAPIException
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("处理商城过期定时任务 开始执行!");
            storeIdList = getStoreIdAll();
            CustomerModel customerModel;
            boolean       isExpire = false;
            for (Integer storeId : storeIdList)
            {
                customerModel = customerModelMapper.selectByPrimaryKey(storeId);
                if (customerModel != null && customerModel.getEnd_date() != null)
                {
                    Date endDate = DateUtil.dateFormateToDate(customerModel.getEnd_date(), GloabConst.TimePattern.YMDHMS);
                    //过期延期7天,超过7天则回收商城
                    Date delayedTime = DateUtil.getAddDate(endDate, 7);
                    //判断是否过期
                    if (DateUtil.dateCompare(new Date(), endDate))
                    {
                        //标记过期
                        isExpire = true;
                        customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_EXPIRE);
                        XxlJobHelper.log("=======商城id={}已过期======", storeId);
                    }
                    //判断是否需要回收商城
                    if (DateUtil.dateCompare(new Date(), delayedTime))
                    {
                        //标记过期
                        customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_EXPIRE);
                        XxlJobHelper.log("=======商城id={}超时未缴费,回收商城======", storeId);
                    }
                    if (isExpire)
                    {
                        int count = customerModelMapper.updateByPrimaryKeySelective(customerModel);
                        XxlJobHelper.log("=======商城已过期 处理结果{}======", count > 0);
                        //强制踢出该商城已登录管理员
                        AdminModel adminModel = new AdminModel();
                        adminModel.setStore_id(storeId);
                        adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                        List<AdminModel> adminModels = adminModelMapper.select(adminModel);
                        adminModels.forEach(adminModel2 ->
                        {
                            String sidLogKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel2.getId();
                            String sidTokenOld = redisUtil.get(sidLogKey) + "";
                            if (StringUtils.isNotEmpty(sidTokenOld))
                            {
                                //踢人
                                XxlJobHelper.log("=======商城id={}已过期,管理员【{}】踢出登录======", storeId, adminModel2.getName());
                                redisUtil.del(sidLogKey);
                                redisUtil.del(sidTokenOld);
                            }
                            sidLogKey = null;
                            sidTokenOld = null;
                        });
                        adminModel = null;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>处理商城过期定时任务 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "处理商城过期定时任务 异常", "storeExpiration");
        }
        finally
        {
            storeIdList = null;
            XxlJobHelper.handleSuccess("处理商城过期定时任务 开始执行!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distributionIncome()
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("分销系统收益统计报表 开始执行!");
            //获取当前时间
            String                      dayTime                 = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD);
            List<UserDistributionModel> userDistributionModels  = userDistributionModelMapper.selectAll();
            DistributionIncomeModel     distributionIncomeModel = new DistributionIncomeModel();
            for (UserDistributionModel userDistributionModel : userDistributionModels)
            {
                XxlJobHelper.log("分销商id：" + userDistributionModel.getId());
                if (userDistributionModel.getUplevel() > 0)
                {
                    String  user_id  = userDistributionModel.getUser_id();
                    Integer store_id = userDistributionModel.getStore_id();
                    //获取记录id
                    Integer id = distributionIncomeModelMapper.getIdByUserIdAndAddTime(store_id, user_id, dayTime);
                    distributionIncomeModel = new DistributionIncomeModel();
                    distributionIncomeModel.setStore_id(store_id);
                    distributionIncomeModel.setUser_id(user_id);
                    //获取当日预估收益
                    distributionIncomeModel.setEstimated_income(distributionRecordModelMapper.sumUserEstimateAmtByAddDate(store_id, user_id, dayTime));
                    //获取当日有效订单
                    distributionIncomeModel.setOrder_num(distributionRecordModelMapper.sumUserOrderNumByAddDate(store_id, user_id, dayTime));
                    //获取有效订单金额
                    distributionIncomeModel.setOrder_price(distributionRecordModelMapper.sumUserOrderTotalByAddDate(store_id, user_id, dayTime));
                    //获取用户单日新增下级
                    distributionIncomeModel.setNew_customer(userDistributionModelMapper.getNewUserNumByAddtime(store_id, user_id, dayTime));
                    //获取当日新增邀请 新增绑定临时关系
                    distributionIncomeModel.setNew_invitation(userDistributionModelMapper.getNewInvitationNumByAddtime(store_id, user_id, dayTime));
                    //全为0不记录
                    if (distributionIncomeModel.getEstimated_income().compareTo(BigDecimal.ZERO) <= 0
                            && distributionIncomeModel.getOrder_num() <= 0
                            && distributionIncomeModel.getOrder_price().compareTo(BigDecimal.ZERO) <= 0
                            && distributionIncomeModel.getNew_customer() <= 0
                            && distributionIncomeModel.getNew_invitation() <= 0)
                    {
                        continue;
                    }
                    distributionIncomeModel.setAdd_time(DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD));
                    if (id != null)
                    {
                        distributionIncomeModel.setId(id);
                        distributionIncomeModelMapper.updateByPrimaryKeySelective(distributionIncomeModel);
                    }
                    else
                    {
                        distributionIncomeModelMapper.insertSelective(distributionIncomeModel);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>分销系统收益统计报表 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DSJSJFYC, "分销系统收益统计报表 异常", "storeExpiration");
        }
        finally
        {
            storeIdList = null;
            XxlJobHelper.handleSuccess("分销系统收益统计报表 开始执行!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void statisticsMch()
    {
        List<Integer> storeIdList = null;
        try
        {
            XxlJobHelper.log("统计店铺同进信息 开始执行!");
            //获取所有的商城id
            storeIdList = getStoreIdAll();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //根据商城id获取当前商城的所有店铺id
            for (Integer storeId : storeIdList)
            {
                MchModel mchModel = new MchModel();
                mchModel.setRecovery(0);
                mchModel.setReview_status("1");
                mchModel.setStore_id(storeId);
                List<MchModel> mchList = mchModelMapper.select(mchModel);
                XxlJobHelper.log("=======获取到商城id={}的所有店铺id======", storeId, mchList);
                //遍历店铺id
                for (MchModel model : mchList)
                {
                    Integer mchId = model.getId();
                    XxlJobHelper.log("=======开始处理店铺id======", mchId);
                    //查看店铺是否有记录，有就修改，没有就新增
                    MchStatisticsModel mchStatisticsModel = new MchStatisticsModel();
                    mchStatisticsModel.setStore_id(storeId);
                    mchStatisticsModel.setMch_id(mchId);
                    mchStatisticsModel.setRecycle(0);
                    mchStatisticsModel = mchStatisticsModelMapper.selectOne(mchStatisticsModel);
                    //获取店铺下所有的商品信息
                    Integer sjNum      = 0;
                    Integer xjNum      = 0;
                    Integer dshProNum  = 0;
                    Integer classNum   = 0;
                    Integer brandNum   = 0;
                    Integer saleSkuNum = 0;
                    Integer skuNum     = 0;
                    Integer kcbzNum    = 0;

                    //上架数   select count(1) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and status = #{status} and recycle=0 and mch_status in (2,4)
                    sjNum = productListModelMapper.getNumByMchIdAndStatus(storeId, mchId, 2);

                    //下架数  select count(1) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and status = #{status} and recycle=0 and mch_status in (2,4)
                    xjNum = productListModelMapper.getNumByMchIdAndStatus(storeId, mchId, 3);

                    //待审核商品数 select count(1) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and mch_status =1 and recycle=0
                    dshProNum = productListModelMapper.getDshNumByMchId(storeId, mchId);

                    //商品分类数 select count(1) from (select count(1) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and recycle=0 group by product_class) a
                    classNum = productListModelMapper.getClassNumByMchId(storeId, mchId);

                    //商品品牌数 select count(1) from (select count(1) as num from lkt_product_list a left join lkt_brand_class b on a.brand_id = b.brand_id where a.store_id=#{storeId} and a.mch_id=#{mchId} and a.recycle=0 and b.recycle=0 group by a.brand_id) a
                    brandNum = productListModelMapper.getBrandNumByMchId(storeId, mchId);

                    //销售商品sku数量 select count(1) as num from lkt_configure c left join lkt_product_list a on a.id=c.pid where a.store_id=#{storeId} and a.mch_id=#{mchId} and a.mch_status =2 and a.recycle=0 and a.status=2
                    saleSkuNum = productListModelMapper.getSaleSkuNumByMchId(storeId, mchId);

                    //sku数量  select count(1) as num from lkt_configure c left join lkt_product_list a on a.id=c.pid where a.store_id=#{storeId} and a.mch_id=#{mchId} and a.mch_status =2 and a.recycle=0
                    skuNum = productListModelMapper.getSkuNumByMchId(storeId, mchId);

                    //库存不足的商品  select count(1) as num from lkt_product_list where store_id=#{storeId} and mch_id=#{mchId} and recycle=0 and num <= min_inventory
                    kcbzNum = productListModelMapper.getKcbzNumByMchId(storeId, mchId);

                    //获取店铺下所有的订单信息
                    Integer dfhOrderNum = 0;
                    Integer dfkOrderNum = 0;
                    Integer tkOrderNum  = 0;
                    Integer dshOrderNum = 0;
                    Integer djsOrderNum = 0;
                    Integer shOrderNum  = 0;
                    //待发货订单 select count(1) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=1
                    dfhOrderNum = orderDetailsModelMapper.getDfhOrderNum(storeId, mchId);

                    //待付款订单 select count(1) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=0
                    dfkOrderNum = orderDetailsModelMapper.getDfkOrderNum(storeId, mchId);

                    //待收货订单 select count(1) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and status=2
                    shOrderNum = orderDetailsModelMapper.getShOrderNum(storeId, mchId);

                    //退款数量 select count(1) from lkt_invoice_info d where d.store_id=#{storeId} and d.mch_id=#{mchId} and d.recovery=0 and d.invoice_status=1
                    tkOrderNum = orderDetailsModelMapper.getTkOrderNum(storeId, mchId);

                    //退款-待审核订单 select count(1) from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id=#{storeId} and d.mch_id=CONCAT(',',#{mchId},',') and d.recycle=0 and r.r_type in (0,3,16) and r.re_type in (1,2)
                    dshOrderNum = orderDetailsModelMapper.getDshOrderNum(storeId, mchId);

                    //待结算订单 select count(1) from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and settlement_status=0 and status=5
                    djsOrderNum = orderDetailsModelMapper.getDjsOrderNum(storeId, mchId);

                    //获取店铺信息
                    BigDecimal zhye  = new BigDecimal(0);
                    BigDecimal djsje = new BigDecimal(0);
                    BigDecimal ytxje = new BigDecimal(0);
                    BigDecimal tkje  = new BigDecimal(0);

                    //账户余额
                    zhye = model.getCashable_money();
                    //待结算金额
//                    if (StringUtils.isNotEmpty(orderDetailsModelMapper.getDjsJeOrder(storeId, mchId))){
//                        djsje = orderDetailsModelMapper.getDjsJeOrder(storeId, mchId);
//                    }
                    djsje = model.getAccount_money();
                    //已提现金额 select sum(w.money) from lkt_withdraw as w  left join lkt_mch as m on m.user_id = w.user_id where m.store_id=#{storeId} and m.id=#{mchId} and is_mch=1  and w.status=1
                    ytxje = orderDetailsModelMapper.getTxJe(storeId, mchId);

                    //退款金额 select sum(r.real_money) from lkt_return_order r left join  lkt_order d on d.sNo = r.sNo where d.store_id=#{storeId} and d.mch_id=CONCAT(',',#{mchId},',') and d.recycle=0 and r.r_type in (4,9,13,15)
                    tkje = orderDetailsModelMapper.getTkJe(storeId, mchId);

                    //获取客户客单数据
                    Integer zkd         = 0;
                    Integer gzKh        = 0;
                    Integer fwKh        = 0;
                    Integer newOrderNum = 0;

                    //总客单
                    zkd = orderDetailsModelMapper.getZkdOrderNum(storeId, mchId);

                    //关注客户 select count(1) from (select count(1) from lkt_user_collection where store_id = #{storeId} and mch_id = #{mchId} group by user_id) a
                    gzKh = userCollectionModelMapper.getGzKhNum(storeId, mchId);

                    //访问客户  select count(1) from (select count(1) from lkt_mch_browse where store_id = #{storeId} and mch_id = #{mchId} group by user_id) a
                    fwKh = mchBrowseModelMapper.getFwKhNum(storeId, mchId);

                    //下单就算
                    newOrderNum = mchModel.getNew_user_order_num();
                    //准备参数
                    MchStatisticsModel saveMchStatisticsModel = new MchStatisticsModel();
                    saveMchStatisticsModel.setAudit_order(dshOrderNum);
                    saveMchStatisticsModel.setAudit_pro(dshProNum);
                    saveMchStatisticsModel.setStore_id(storeId);
                    saveMchStatisticsModel.setMch_id(mchId);
                    saveMchStatisticsModel.setAccess_user_num(fwKh);
                    saveMchStatisticsModel.setAttention_user_num(gzKh);
                    saveMchStatisticsModel.setCkbz_pro(kcbzNum);
                    saveMchStatisticsModel.setCustomer_num(zkd);
                    saveMchStatisticsModel.setDjs_money(djsje);
                    saveMchStatisticsModel.setDjs_order(djsOrderNum);
                    saveMchStatisticsModel.setDsh_order(shOrderNum);
                    saveMchStatisticsModel.setAdd_date(new Date());
                    saveMchStatisticsModel.setObligation(dfkOrderNum);
                    saveMchStatisticsModel.setPending_shipment(dfhOrderNum);
                    saveMchStatisticsModel.setPro_brand(brandNum);
                    saveMchStatisticsModel.setPro_class(classNum);
                    saveMchStatisticsModel.setPro_sku(skuNum);
                    saveMchStatisticsModel.setRefund_order(tkOrderNum);
                    saveMchStatisticsModel.setReturn_money(tkje);
                    saveMchStatisticsModel.setSale_pro_sku(saleSkuNum);
                    saveMchStatisticsModel.setSj_pro(sjNum);
                    saveMchStatisticsModel.setXj_pro(xjNum);
                    saveMchStatisticsModel.setYtx_money(ytxje);
                    saveMchStatisticsModel.setNew_pay_user(newOrderNum);

                    if (mchStatisticsModel != null)
                    {
                        saveMchStatisticsModel.setId(mchStatisticsModel.getId());
                        //修改
                        mchStatisticsModelMapper.updateByPrimaryKeySelective(saveMchStatisticsModel);
                    }
                    else
                    {
                        //新增
                        mchStatisticsModelMapper.insertSelective(saveMchStatisticsModel);
                    }

                    //统计客户购买力表
                    //先获取所有的客户 select user_id from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 group by user_id
                    List<Map<String, Object>> allUserList = orderDetailsModelMapper.getAllUserByMchId(storeId, mchId);
                    if (allUserList.size() > 0)
                    {
                        for (Map<String, Object> map : allUserList)
                        {
                            //获取客户下单的所有金额
                            BigDecimal allMoney = new BigDecimal(0);
                            String     userId   = MapUtils.getString(map, "user_id");
                            // select ifnull(sum(z_price),0) as sum  from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and user_id=#{userId} and status in (1,2,5)
                            allMoney = orderDetailsModelMapper.getAllUserMoneyByUserId(storeId, mchId, userId);

                            //获取客户购买力表是否有记录
                            MchBuyPowerModel mchBuyPowerModel = new MchBuyPowerModel();
                            mchBuyPowerModel.setStore_id(storeId);
                            mchBuyPowerModel.setMch_id(mchId);
                            mchBuyPowerModel.setUser_id(userId);
                            mchBuyPowerModel.setRecycle(0);
                            mchBuyPowerModel = mchBuyPowerModelMapper.selectOne(mchBuyPowerModel);

                            //准备参数
                            MchBuyPowerModel saveMchBuyPowerModel = new MchBuyPowerModel();
                            saveMchBuyPowerModel.setRecycle(0);
                            saveMchBuyPowerModel.setAdd_date(new Date());
                            saveMchBuyPowerModel.setMoney(allMoney);
                            saveMchBuyPowerModel.setStore_id(storeId);
                            saveMchBuyPowerModel.setMch_id(mchId);
                            saveMchBuyPowerModel.setUser_id(userId);
                            if (mchBuyPowerModel != null)
                            {
                                //修改
                                saveMchBuyPowerModel.setId(mchBuyPowerModel.getId());
                                mchBuyPowerModelMapper.updateByPrimaryKeySelective(saveMchBuyPowerModel);
                            }
                            else
                            {
                                //新增
                                mchBuyPowerModelMapper.insertSelective(saveMchBuyPowerModel);
                            }
                        }
                    }
                    //统计订单表-按天
                    //获取所有下单的日期 select date_format(add_time,'%Y-%m-%d') as add_time from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 group by date_format(add_time,'%Y-%m-%d')
                    List<Map<String, Object>> allDateList = orderDetailsModelMapper.getAllDateByMchId(storeId, mchId);
                    if (allDateList.size() > 0)
                    {
                        for (Map<String, Object> map : allDateList)
                        {
                            String     date       = MapUtils.getString(map, "add_time");
                            BigDecimal orderMoney = new BigDecimal(0);
                            Integer    orderNum   = 0;

                            //select sum(z_price) as money from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and date_format(add_time,'%Y-%m-%d')=#{date}
                            orderMoney = orderDetailsModelMapper.getMoneyByMchId(storeId, mchId, date);

                            //select count(1) as num  from lkt_order where store_id=#{storeId} and mch_id=CONCAT(',',#{mchId},',') and recycle=0 and date_format(add_time,'%Y-%m-%d')=#{date}
                            orderNum = orderDetailsModelMapper.getNumByMchId(storeId, mchId, date);
                            MchOrderRecordModel mchOrderRecordModel = new MchOrderRecordModel();
                            mchOrderRecordModel.setMch_id(mchId);
                            mchOrderRecordModel.setStore_id(storeId);
                            mchOrderRecordModel.setRecycle(0);

                            Date count_day = sdf.parse(date);
                            mchOrderRecordModel.setCount_day(count_day);
                            mchOrderRecordModel = mchOrderRecordModelMapper.selectOne(mchOrderRecordModel);

                            MchOrderRecordModel saveMchOrderRecordModel = new MchOrderRecordModel();
                            saveMchOrderRecordModel.setCount_day(count_day);
                            saveMchOrderRecordModel.setOrder_num(orderNum);
                            saveMchOrderRecordModel.setMoney(orderMoney);
                            saveMchOrderRecordModel.setRecycle(0);
                            saveMchOrderRecordModel.setAdd_date(new Date());
                            saveMchOrderRecordModel.setStore_id(storeId);
                            saveMchOrderRecordModel.setMch_id(mchId);
                            if (mchOrderRecordModel != null)
                            {
                                saveMchOrderRecordModel.setId(mchOrderRecordModel.getId());
                                mchOrderRecordModelMapper.updateByPrimaryKeySelective(mchOrderRecordModel);
                            }
                            else
                            {
                                mchOrderRecordModelMapper.insertSelective(saveMchOrderRecordModel);
                            }
                        }
                    }
                    XxlJobHelper.log("=======结束处理店铺id======", model.getId());
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail(">>>>>>>处理统计店铺定时任务 【异常失败】<<<<<<<", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "处理统计店铺定时任务 异常", "statisticsMch");
        }
        finally
        {
            XxlJobHelper.handleSuccess("处理统计店铺定时任务 开始执行!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noticeInvalidIntegral()
    {
        //获取当前商城所有用户
        List<String>              userIdList      = null;
        List<Map<String, Object>> secondsListInfo = null;
        String[]                  remindList      = null;
        Date                      heraldDate      = null;
        String                    noticeTime      = null;
        Map<String, Object>       insertParam     = null;
        String                    goodsName       = null;
        List<Integer>             storeIdList     = null;
        try
        {
            int row = 0;
            XxlJobHelper.log("积分过期通知定时任务 开始执行!");
            storeIdList = getStoreIdAll();
            for (int storeId : storeIdList)
            {
                XxlJobHelper.log("商城id{}--->积分过期通知 开始执行!", storeId);
                //获取商城自营店
                Integer zyMchId = customerModelMapper.getStoreMchId(storeId);
                //获取当前商城所有用户
                userIdList = userBaseMapper.getUserAllByUserId(storeId,null);
                if (userIdList == null || userIdList.size() < 1)
                {
                    continue;
                }
                //获取即将过期且未提醒过的积分记录id
                List<Map<String, Object>> userInvalidScoreInfo = signRecordModelMapper.getUserInvalidScore(storeId, DateUtil.getAddDate(1));

                for (Map<String, Object> scoreInfo : userInvalidScoreInfo)
                {
                    XxlJobHelper.log("开始积分过期通知推送,当前推送时间段:{}!", DateUtil.dateFormate(heraldDate, GloabConst.TimePattern.YMDHMS));
                    //开始批量推送
                    SystemMessageModel systemMessageModel = new SystemMessageModel();
                    systemMessageModel.setType(1);
                    systemMessageModel.setStore_id(storeId);
                    systemMessageModel.setSenderid("task");
                    systemMessageModel.setRecipientid(DataUtils.getStringVal(scoreInfo, "user_id"));
                    systemMessageModel.setTime(new Date());
                    systemMessageModel.setTitle("您有积分即将过期，请尽快使用！");
                    systemMessageModel.setContent(String.format("您好，您有%s积分将于%s过期，请尽快使用！", DataUtils.getIntegerVal(scoreInfo, "sign_score"), DateUtil.formatDate(DateUtil.dateFormate(DataUtils.getStringVal(scoreInfo, "score_invalid"), GloabConst.TimePattern.YMD))));
                    row = systemMessageModelMapper.insertSelective(systemMessageModel);
                    XxlJobHelper.log("商城id{}--->积分过期通知 一共通知{}", storeId, row);
                }

                XxlJobHelper.log("======== 积分过期通知定时任务  开始执行! ========");
            }
            XxlJobHelper.log("积分过期通知任务执行完毕!");
        }
        catch (Exception e)
        {
            XxlJobHelper.handleFail("积分过期通知任务 异常堆栈信息: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, e.getMessage());
        }
        finally
        {
            userIdList = null;
            secondsListInfo = null;
            remindList = null;
            heraldDate = null;
            noticeTime = null;
            insertParam = null;
            goodsName = null;
            storeIdList = null;
        }
    }

    @Override
    public void httpGetJob() throws LaiKeAPIException
    {
        try
        {
            // 获取参数
            String url = XxlJobHelper.getJobParam();
            XxlJobHelper.log("接收参数,:{}", url);
            if (url == null)
            {
                XxlJobHelper.log("参数为空，调度结束。");
                return;
            }
            XxlJobHelper.log("结果：" + HttpUtils.get(url));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("分销订单确认收货佣金结算特殊处理 异常: " + e.getMessage());
        }
    }

    @Override
    public void httpPostJob() throws LaiKeAPIException
    {
        try
        {
            // 获取参数
            String url = XxlJobHelper.getJobParam();
            XxlJobHelper.log("接收参数,:{}", url);
            if (url == null)
            {
                XxlJobHelper.log("参数为空，调度结束。");
                return;
            }
            XxlJobHelper.log("结果：" + HttpUtils.post(url));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("分销订单确认收货佣金结算特殊处理 异常: " + e.getMessage());
        }
    }

    @Override
    public void videoTask() throws LaiKeAPIException
    {
        try
        {
            List<Integer> storeIdAll = getStoreIdAll();
            if (CollectionUtils.isNotEmpty(storeIdAll))
            {
                for (Integer store_id : storeIdAll)
                {
                    Map<String, Object> param = new HashMap<>();
                    param.put("store_id", store_id);
                    BbsConfigModel bbsConfigModel = new BbsConfigModel();
                    bbsConfigModel.setStore_id(store_id);
                    bbsConfigModel = bbsConfigModelMapper.selectOne(bbsConfigModel);

                    List<String> urlList = new ArrayList<>();

                    if (Objects.nonNull(bbsConfigModel))
                    {
                        //视频过期时间
                        Integer expireTime = bbsConfigModel.getExpire_time();
                        expireTime = expireTime * 60 * 60;

                        List<Map<String, Object>> list = bbsVideoModelMapper.selectDynamic(param);

                        if (CollectionUtils.isNotEmpty(list))
                        {
                            for (Map<String, Object> map : list)
                            {
                                String fileId = MapUtils.getString(map, "file_id");

                                Integer status = MapUtils.getInteger(map, "status");

                                String url = MapUtils.getString(map, "url");
                                if (!redisUtil.hasKey(fileId))
                                {
                                    //回调完成，再给url加防盗链
                                    if (status == 2)
                                    {
                                        url = kcPlayerUtils.generateSecurityUrl(bbsConfigModel, url);
                                        urlList.add(url);
                                        redisUtil.set(fileId,url,expireTime);
                                    }
                                }
                                else
                                {
                                    long expire = redisUtil.getExpire(fileId);
                                    //过期时间小于5分钟时，刷新缓存过期时间
                                    if (expire < 300)
                                    {
                                        url = kcPlayerUtils.generateSecurityUrl(bbsConfigModel, url);
                                        urlList.add(url);
                                        redisUtil.set(fileId,url,expireTime);
                                    }
                                }
                            }
                        }


                        //视频预热
                        if (CollectionUtils.isNotEmpty(urlList))
                        {
                            //一次20个
                            List<List<String>> listList = splitListIntoBatches(urlList, BATCH_SIZE);
                            for (List<String> urls : listList)
                            {
                                kcPlayerUtils.pushUrl(bbsConfigModel, urls);
                            }
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            XxlJobHelper.log("腾讯云防盗链视频过期时间延长 异常: " + e.getMessage());
        }
    }

    // 分割列表为多个批次
    public static List<List<String>> splitListIntoBatches(List<String> list, int batchSize)
    {
        List<List<String>> batches = new ArrayList<>();
        int totalSize = list.size();

        for (int i = 0; i < totalSize; i += batchSize)
        {
            // 获取当前批次的结束索引
            int end = Math.min(i + batchSize, totalSize);
            batches.add(list.subList(i, end));
        }

        return batches;
    }

    @Autowired
    private KCPlayerUtils kcPlayerUtils;

    @Autowired
    private BbsConfigModelMapper bbsConfigModelMapper;

    @Autowired
    private BbsVideoModelMapper bbsVideoModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private SignRecordModelMapper signRecordModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private DistributionIncomeModelMapper distributionIncomeModelMapper;

    @Autowired
    private DistributionRecordModelMapper distributionRecordModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private MchStatisticsModelMapper mchStatisticsModelMapper;

    @Autowired
    private MchBuyPowerModelMapper mchBuyPowerModelMapper;

    @Autowired
    private MchOrderRecordModelMapper mchOrderRecordModelMapper;
}

