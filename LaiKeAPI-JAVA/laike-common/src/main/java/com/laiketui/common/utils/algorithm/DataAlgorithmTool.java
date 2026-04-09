package com.laiketui.common.utils.algorithm;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;

import java.math.BigDecimal;
import java.util.*;


/**
 * 数据算法
 *
 * @author Trick
 * @date 2020/12/22 9:43
 */
public class DataAlgorithmTool
{

    public static void main(String[] args)
    {

        //订单金额
        BigDecimal priceTotal = new BigDecimal("200");
        //优惠金额
        BigDecimal avgPrice = new BigDecimal("20");
        //假如有四个商品
        List<BigDecimal> goodsPrice = new ArrayList<>();
        goodsPrice.add(new BigDecimal("150"));
        goodsPrice.add(new BigDecimal("10"));
        goodsPrice.add(new BigDecimal("40"));
        for (BigDecimal currentPrice : goodsPrice)
        {
            System.out.println("当前商品价格:" + currentPrice);
            currentPrice = orderPriceAverage(priceTotal, currentPrice, avgPrice);
            System.out.println("均摊优惠价格:" + currentPrice);
        }

//        System.out.println("均摊优惠后价格:" + new BigDecimal("163").subtract(orderPriceAverage(new BigDecimal("162"), new BigDecimal("162"), new BigDecimal("32.4"))));
//
//        System.out.println("均摊优惠后价格:" + new BigDecimal("163").subtract(orderPriceAverage(new BigDecimal("163"), new BigDecimal("163"), new BigDecimal("32.4"))));
//
//        System.out.println("均摊优惠后价格:" + orderPriceAverage(new BigDecimal("182.9"), new BigDecimal("19.9"), new BigDecimal("34.380")));
//        System.out.println("均摊优惠后价格:" + orderPriceAverage(new BigDecimal("182.9"), new BigDecimal("163"), new BigDecimal("34.380")));
//
//
//        System.out.println("均摊优惠后价格:" + orderPriceAverage(new BigDecimal("104.4"), new BigDecimal("104.4"), new BigDecimal("10")));
//        System.out.println("均摊优惠后价格:" + orderPriceAverage(new BigDecimal("171.9"), new BigDecimal("162"), new BigDecimal("34.380")));

    }


    /**
     * 金额均摊算法
     * 【场景1:
     * 一个店铺多个商品，使用平台优惠券后，每个商品均衡分配优惠金额
     * 优惠前订单金额: 769 (优惠后价格:676.72/8.8折) 优惠了:92.28
     * 商品金额分别: 398,180,180,11
     * 公式[商品金额/优惠前订单金额*92.28]
     * 得出每个商品均摊金额:350.24(-47.76),158.4(-21.6),158.4(-21.6),9.68(-1.32)
     * 】
     *
     * @param priceTotal   - 优惠前总金额
     * @param currentPrice - 当前商品金额
     * @param avgPrice     - 订单总优惠金额(需均摊的金额)
     * @return BigDecimal - 当前商品均摊金额
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/9/2 19:39
     */
    public static BigDecimal orderPriceAverage(BigDecimal priceTotal, BigDecimal currentPrice, BigDecimal avgPrice) throws LaiKeAPIException
    {
        BigDecimal resultAvgPrice;
        try
        {
            resultAvgPrice = currentPrice.divide(priceTotal, 6, BigDecimal.ROUND_HALF_UP).multiply(avgPrice);
            return resultAvgPrice;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.COUPON_PLAFORM_SCREENING_ERROR, "金额均摊算法异常", "orderPriceAverage");
        }
    }


    /**
     * @param priceTotal      - 优惠前总金额(去除运费)
     * @param currentPriceMap - {key:订单明细id/唯一id,value:所有商品金额集(去除运费)}
     * @param avgPrice        - 订单总优惠金额(需均摊的金额)
     * @return Map - {key:订单明细id,value:当前商品均摊金额}
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022-11-15 17:54:40
     */
    public static Map<Integer, BigDecimal> orderPriceAverage(BigDecimal priceTotal, Map<Integer, BigDecimal> currentPriceMap, BigDecimal avgPrice) throws LaiKeAPIException
    {
        Map<Integer, BigDecimal> resultMap = new HashMap<>(16);
        try
        {
            //当前商品数量
            int i = 1;
            //当前优惠总金额
            BigDecimal yhTotalMoney = BigDecimal.ZERO;
            for (Integer orderId : currentPriceMap.keySet())
            {
                BigDecimal resultAvgPrice = BigDecimal.ZERO;
                if (BigDecimal.ZERO.compareTo(avgPrice) < 0)
                {
                    //店铺小计（减去了店铺优惠金额）
                    BigDecimal currentPrice = currentPriceMap.get(orderId);
                    resultAvgPrice = currentPrice.divide(priceTotal, 6, BigDecimal.ROUND_HALF_UP).multiply(avgPrice).setScale(6, BigDecimal.ROUND_HALF_UP);
                    yhTotalMoney = yhTotalMoney.add(resultAvgPrice.setScale(2, BigDecimal.ROUND_DOWN));
                    if (currentPriceMap.size() == i)
                    {
                        avgPrice = avgPrice.subtract(yhTotalMoney);
                        if (BigDecimal.ZERO.compareTo(avgPrice) < 0)
                        {
                            resultAvgPrice = resultAvgPrice.add(avgPrice);
                        }
                    }
                }
                resultMap.put(orderId, resultAvgPrice.setScale(2, BigDecimal.ROUND_DOWN));
                i++;
            }

            return resultMap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.COUPON_PLAFORM_SCREENING_ERROR, "金额均摊算法异常", "orderPriceAverage");
        }
    }

    /**
     * 二维数组组合算法
     *
     * @param array      - 二维数组 为了防止少一个,请在数组最后增加一组空数据
     * @param temp       - 临时数组
     * @param i          -
     * @param j          -
     * @param returnList - 返回的结果
     * @author Trick
     * @date 2020/12/22 9:43
     */
    public static void combination(String[][] array, String[] temp, int i, int j, List<List<String>> returnList)
    {
        for (; i < array.length; i++)
        {
            if (StringUtils.isEmpty(array[i]))
            {
                continue;
            }
            for (; j < array[i].length; j++)
            {
                temp[i] = array[i][j];
                combination(array, temp, i + 1, 0, returnList);
                //递归出口
                if (j + 1 < array[i].length)
                {
                    if (StringUtils.isEmpty(temp[0]))
                    {
                        continue;
                    }
                    returnList.add(new ArrayList<>(Arrays.asList(temp)));
                }

            }
        }
    }

}
