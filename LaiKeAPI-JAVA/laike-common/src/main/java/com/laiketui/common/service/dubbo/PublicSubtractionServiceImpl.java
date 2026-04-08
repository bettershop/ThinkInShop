package com.laiketui.common.service.dubbo;

import com.google.common.collect.Ordering;
import com.laiketui.common.api.PublicSubtractionService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.subtraction.SubtractionConfigModal;
import com.laiketui.domain.subtraction.SubtractionModal;
import com.laiketui.domain.subtraction.SubtractionRecordModal;
import org.apache.commons.beanutils.BeanUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 满减通用服务类
 *
 * @author wangxian
 */
@Service
public class PublicSubtractionServiceImpl implements PublicSubtractionService
{

    private final Logger logger = LoggerFactory.getLogger(PublicSubtractionServiceImpl.class);

    @Autowired
    private SubtractionConfigModalMapper subtractionConfigModalMapper;
    @Autowired
    private SubtractionModalMapper       subtractionModalMapper;
    @Autowired
    private SubtractionRecordModalMapper subtractionRecordModalMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Override
    public Map<String, Object> autoSubtraction(int storeId, List<Map<String, Object>> products, int subtractionId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                    isSubtraction          = 0;
            SubtractionConfigModal subtractionConfigModal = new SubtractionConfigModal();
            subtractionConfigModal.setStore_id(storeId);
            subtractionConfigModal = subtractionConfigModalMapper.selectOne(subtractionConfigModal);
            if (subtractionConfigModal != null)
            {
                // 是否开启满减 0.否 1.是
                isSubtraction = subtractionConfigModal.getIs_subtraction();
                if (isSubtraction == 1)
                {
                    resultMap = subtractionList(storeId, subtractionId, products);
                }
            }
            resultMap.put("is_subtraction", isSubtraction);
        }
        catch (LaiKeAPIException e)
        {
            e.printStackTrace();
            throw e;
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> subtractionList(int storeId, int subtractionId, List<Map<String, Object>> products)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            // 店铺ID数组
            List<String> shopIdList = new ArrayList<>();
            // 商品分类ID数组
            List<String[]> productClassIdList = new ArrayList<>();
            // 品牌ID数组
            List<String> brandIdList = new ArrayList<>();
            // 可用满减活动
            List<Map<String, String>> subtractionList = new ArrayList<>();
            // 返回的可用满减活动
            List<Map<String, String>> returnSubtractionList = new ArrayList<>();
            //
            String reduceNameArray = "";
            //
            BigDecimal reduceMoney = BigDecimal.ZERO;
            //
            String reduceName = "";
            //
            BigDecimal zong = BigDecimal.ZERO;
            BigDecimal num  = BigDecimal.ZERO;
            //赠送商品id
            int giveId = 0;
            for (Map<String, Object> mchProductInfo : products)
            {
                shopIdList.add(DataUtils.getStringVal(mchProductInfo, "shop_id"));
                List<Map<String, Object>> oneMchProducts = (List<Map<String, Object>>) mchProductInfo.get("list");
                for (Map<String, Object> product : oneMchProducts)
                {
                    BigDecimal price = DataUtils.getBigDecimalVal(product, "price");
                    num = DataUtils.getBigDecimalVal(product, "num");
                    zong = zong.add(price).multiply(num);
                    String productClassStr = DataUtils.getStringVal(product, "product_class");
                    productClassIdList.add(StringUtils.trim(productClassStr, SplitUtils.HG).split(SplitUtils.HG));
                    brandIdList.add(DataUtils.getStringVal(product, "brand_id"));
                }
            }

            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(storeId);
            adminModel.setType(AdminModel.TYPE_CLIENT);
            adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            adminModel = adminModelMapper.selectOne(adminModel);
            int mchId = adminModel.getShop_id();

            /* 根据满减范围挑选可使用的满减活动 开始 */
            // 查询自动满减设置
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(storeId);
            //满减进行中状态 1.未开始 2.开启 3.关闭 4.已结束
            subtractionModal.setStatus(SubtractionModal.STARTING);
            List<SubtractionModal> subtractionModalList = subtractionModalMapper.select(subtractionModal);

            //返回结果
            List<SubtractionModal> retSubtractionModalList = null;

            if (!CollectionUtils.isEmpty(subtractionModalList))
            {
                retSubtractionModalList = new ArrayList<>(16);
                // 当前有满减活动
                for (SubtractionModal subtraction : subtractionModalList)
                {
                    // 满减应用范围
                    String subtractionRange      = subtraction.getSubtraction_range();
                    String subtractionParameters = subtraction.getSubtraction_parameter();
                    // 满足满减
                    boolean subtractionStatus = true;
                    if (SubtractionModal.SUBTRACTION_RANG_ZDFL.equals(subtractionRange))
                    {
                        // 指定分类
                        for (String[] productClasses : productClassIdList)
                        {
                            for (String proClass : productClasses)
                            {
                                // 判断店铺名称，是否存在满减范围参数数组
                                if (subtractionParameters.contains(proClass))
                                {
                                    // 满足满减
                                    subtractionStatus = true;
                                }
                                else
                                {
                                    // 不满足满减
                                    subtractionStatus = false;
                                }
                            }
                            if (!subtractionStatus)
                            {
                                break;
                            }
                        }
                    }
                    else if (SubtractionModal.SUBTRACTION_RANG_ZDPP.equals(subtractionRange))
                    {
                        // 品牌
                        for (String brandId : brandIdList)
                        {
                            if (!subtractionParameters.contains(brandId))
                            {
                                // 判断店铺名称，是否存在满减范围参数数组 不满足满减
                                subtractionStatus = false;
                                break;
                            }
                        }
                    }
                    if (subtractionStatus)
                    {
                        retSubtractionModalList.add(subtraction);
                    }
                }
            }
            /* 根据满减范围挑选可使用的满减活动 结束 */

            /* 根据满减类型计算优惠的价格 开始 */
            if (!CollectionUtils.isEmpty(retSubtractionModalList) && retSubtractionModalList.size() > 0)
            {
                Map<String, String> tmpSubtractionMap = new HashMap<>();
                for (SubtractionModal subtractionModalTmp : retSubtractionModalList)
                {
                    tmpSubtractionMap = BeanUtils.describe(subtractionModalTmp);
                    // 满减类型 1.阶梯满减 2.循环满减 3.满赠 4.满件折扣
                    int subtractionType = subtractionModalTmp.getSubtraction_type();
                    // 满减参数
                    Map<String, String> subtractionParamsMap = SerializePhpUtils.getUnserializeObj(subtractionModalTmp.getSubtraction());
                    // todo
                    if (subtractionParamsMap == null)
                    {
                        continue;
                    }
                    //参数 100-999 200-1000
                    Set<String>  keys = subtractionParamsMap.keySet();
                    List<String> vals = new ArrayList<String>(subtractionParamsMap.values());
                    if (SubtractionModal.SUBTRACTION_JT == subtractionType)
                    {
                        //阶梯满减
                        for (Object key : keys)
                        {
                            //阶梯价格
                            BigDecimal jtPrice = new BigDecimal(String.valueOf(key));
                            if (zong.compareTo(jtPrice) >= 0)
                            {
                                tmpSubtractionMap.put("discount", subtractionParamsMap.get(key));
                                subtractionList.add(tmpSubtractionMap);
                            }
                        }
                    }
                    else if (SubtractionModal.SUBTRACTION_XH == subtractionType)
                    {
                        //循环满减 只会有一个价格
                        BigDecimal jtPrice = new BigDecimal((String) keys.toArray()[0]);
                        if (zong.compareTo(jtPrice) >= 0)
                        {
                            int    number = Integer.parseInt(String.valueOf(zong.divide(jtPrice)));
                            double val    = Double.parseDouble(String.valueOf(vals.get(0)));
                            tmpSubtractionMap.put("discount", String.valueOf(number * val));
                            subtractionList.add(tmpSubtractionMap);
                        }
                    }
                    else if (SubtractionModal.SUBTRACTION_MZ == subtractionType)
                    {
                        // 满赠
                        int shopId = Integer.parseInt(shopIdList.get(0));
                        if (shopIdList.size() == 1 && mchId == shopId)
                        {
                            for (Object key : keys)
                            {
                                BigDecimal jtPrice = new BigDecimal(String.valueOf(key));
                                if (zong.compareTo(jtPrice) >= 0)
                                {
                                    ProductListModel productListModel = new ProductListModel();
                                    productListModel.setStore_id(storeId);
                                    productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                                    productListModel.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
                                    productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                                    productListModel.setId(Integer.parseInt(subtractionParamsMap.get(key)));
                                    productListModel = productListModelMapper.selectOne(productListModel);
                                    if (productListModel != null)
                                    {
                                        int num1 = productListModel.getNum();
                                        //
                                        tmpSubtractionMap.put("give_id", String.valueOf(productListModel.getId()));
                                        tmpSubtractionMap.put("product_title", productListModel.getProduct_title());
                                        tmpSubtractionMap.put("num", String.valueOf(num1));

                                        ConfiGureModel configModel = new ConfiGureModel();
                                        configModel.setPid(productListModel.getId());
                                        List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(configModel);
                                        if (!CollectionUtils.isEmpty(confiGureModelList))
                                        {
                                            List<BigDecimal> priceList = new ArrayList<>();
                                            for (ConfiGureModel confiGureModel : confiGureModelList)
                                            {
                                                priceList.add(confiGureModel.getPrice());
                                            }
                                            //排序获取最小的规格价格升序
                                            priceList = new Ordering<BigDecimal>()
                                            {
                                                @Override
                                                public int compare(@Nullable BigDecimal bigDecimal, @Nullable BigDecimal t1)
                                                {
                                                    return bigDecimal.compareTo(t1);
                                                }
                                            }.immutableSortedCopy(priceList);
                                            BigDecimal min = priceList.get(0);
                                            tmpSubtractionMap.put("discount", String.valueOf(min));

                                            for (Map<String, Object> mchProductsInfo : products)
                                            {
                                                List<Map<String, Object>> productsInfo = (List<Map<String, Object>>) mchProductsInfo.get("list");
                                                for (Map<String, Object> product : productsInfo)
                                                {
                                                    //
                                                    int pid  = (Integer) product.get("pid");
                                                    int num2 = (Integer) product.get("num");
                                                    //满赠商品id
                                                    int mzProductId = Integer.parseInt(subtractionParamsMap.get(key));
                                                    if (pid == mzProductId)
                                                    {
                                                        num1 = num1 - num2;
                                                    }
                                                }
                                            }
                                            if (num1 > 0)
                                            {
                                                subtractionList.add(tmpSubtractionMap);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        //满件折扣
                        if (num.intValue() >= Integer.valueOf(String.valueOf(keys.toArray()[0])))
                        {
                            tmpSubtractionMap.put("discount", String.valueOf(zong.subtract(zong.multiply(new BigDecimal(String.valueOf(vals.get(0)))).divide(BigDecimal.TEN))));
                            subtractionList.add(tmpSubtractionMap);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(subtractionList))
                {
                    Collections.sort(subtractionList, new Comparator<Map<String, String>>()
                    {
                        @Override
                        public int compare(Map<String, String> o1, Map<String, String> o2)
                        {
                            Double sort1 = Double.parseDouble(String.valueOf(o1.get("discount")));
                            Double sort2 = Double.parseDouble(String.valueOf(o2.get("discount")));
                            return sort2.compareTo(sort1);
                        }
                    });
                    Map<String, String> retSubtractionMap = null;
                    for (Map<String, String> subtractionMap : subtractionList)
                    {
                        retSubtractionMap = new HashMap<>();
                        returnSubtractionList.add(retSubtractionMap);
                        String subtractionIdTmp   = subtractionMap.get("id");
                        String subtractionNameTmp = subtractionMap.get("name");

                        retSubtractionMap.put("coupon_id", subtractionIdTmp);
                        retSubtractionMap.put("coupon_name", "满减活动" + subtractionNameTmp);
                        retSubtractionMap.put("coupon_status", "false");
                        retSubtractionMap.put("discount_type", "subtraction");
                        BigDecimal discount        = new BigDecimal(String.valueOf(subtractionMap.get("discount")));
                        byte       subtractionType = Byte.parseByte(String.valueOf(subtractionMap.get("subtraction_type")));
                        BigDecimal numTmp          = BigDecimal.ZERO;
                        if (subtractionMap.get("num") != null)
                        {
                            numTmp = new BigDecimal(subtractionMap.get("num"));
                        }

                        if (subtractionId != 0)
                        {
                            if (subtractionIdTmp.equals(subtractionId + ""))
                            {
                                retSubtractionMap.put("coupon_status", "true");
                                reduceNameArray = "满减活动:" + subtractionNameTmp;

                                if (subtractionType == SubtractionModal.SUBTRACTION_MZ)
                                {
                                    if (numTmp.intValue() > 0)
                                    {
                                        giveId = Integer.parseInt(subtractionMap.get("give_id"));
                                        reduceName = "赠送商品(" + subtractionList.get(0).get("product_title") + ")";
                                    }
                                    else
                                    {
                                        reduceName = "赠送商品（已赠完）";
                                    }
                                }
                                else
                                {
                                    reduceMoney = discount;
                                    reduceNameArray += "(优惠金额￥" + discount + ")";
                                    retSubtractionMap.put("reduce_name", reduceNameArray);
                                    for (Map<String, Object> mchProducts : products)
                                    {
                                        List<Map<String, Object>> onlyProductsInfo = (List<Map<String, Object>>) mchProducts.get("list");
                                        for (Map<String, Object> product : onlyProductsInfo)
                                        {
                                            BigDecimal amountAfterDiscountTmp = DataUtils.getBigDecimalVal(product, "amount_after_discount");
                                            product.put("amount_after_discount", amountAfterDiscountTmp.subtract(amountAfterDiscountTmp.divide(zong, 2, RoundingMode.HALF_UP).multiply(discount)));
                                        }
                                    }
                                }
                            }
                        }
                        if (subtractionType != SubtractionModal.SUBTRACTION_MZ)
                        {
                            retSubtractionMap.put("coupon_name", retSubtractionMap.get("coupon_name") + "(优惠金额￥" + discount + ")");
                        }
                        else
                        {
                            if (numTmp.intValue() > 0)
                            {
                                subtractionList.get(0).get("product_title");
                                retSubtractionMap.put("coupon_name", retSubtractionMap.get("coupon_name") + "赠送商品(" + subtractionList.get(0).get("product_title") + ")");
                            }
                            else
                            {
                                retSubtractionMap.put("coupon_name", retSubtractionMap.get("coupon_name") + "赠送商品（已赠完）");
                            }
                        }
                    }
                }
            }
            /* 根据满减类型计算优惠的价格 结束 */
            reduceNameArray = StringUtils.trim(reduceNameArray, SplitUtils.DH);
            resultMap.put("subtraction_id", subtractionId);
            resultMap.put("reduce_money", reduceMoney);
            resultMap.put("products", products);
            resultMap.put("reduce_name_array", reduceNameArray);
            resultMap.put("reduce_name", reduceName);
            resultMap.put("give_id", giveId);
            resultMap.put("subtraction_list", returnSubtractionList);
            return resultMap;
        }
        catch (Exception e)
        {
            logger.error("订单满减计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDMJJSSB, "订单满减计算失败", "subtractionList");
        }
    }

    @Override
    public int subtractionRecord(int storeId, String userId, String sNo, int giveId)
    {
        int ret = 0;
        try
        {
            OrderModel orderModel = new OrderModel();
            orderModel.setStore_id(storeId);
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            String couponActivityName = orderModel.getCoupon_activity_name();
            int    pos                = couponActivityName.lastIndexOf(":");
            couponActivityName = couponActivityName.substring(pos + 1, couponActivityName.length());
            pos = couponActivityName.indexOf("(");
            if (pos != -1)
            {
                couponActivityName = couponActivityName.substring(0, pos);
            }
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(storeId);
            subtractionModal.setName(couponActivityName);
            subtractionModal = subtractionModalMapper.selectOne(subtractionModal);

            if (subtractionModal != null)
            {
                int                    id                     = subtractionModal.getId();
                String                 content                = "会员" + userId + "的订单号为" + sNo + "参与满减活动" + couponActivityName;
                SubtractionRecordModal subtractionRecordModal = new SubtractionRecordModal();
                subtractionRecordModal.setH_id(id);
                subtractionRecordModal.setP_id(giveId);
                subtractionRecordModal.setsNo(sNo);
                subtractionRecordModal.setUser_id(userId);
                subtractionRecordModal.setContent(content);
                subtractionRecordModal.setAdd_date(new Date());
                int row = subtractionRecordModalMapper.insert(subtractionRecordModal);
                if (row <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJMJJLSB, " 添加满减记录失败", "subtractionRecord");
                }
            }
        }
        catch (Exception e)
        {
            logger.error("添加满减记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJMJJLSB, " 添加满减记录失败", "subtractionRecord");
        }
        return ret;
    }

}

