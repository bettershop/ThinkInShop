package com.laiketui.plugins.api.pc.mall;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.ProductIndexVo;
import com.laiketui.domain.vo.cart.AddCartVo;
import com.laiketui.domain.vo.goods.AddCommentVo;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品接口
 *
 * @author Trick
 * @date 2021/6/22 17:22
 */
public interface AppsMallGoodsService
{


    /**
     * 获取商品详细信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/26 17:26
     */
    Map<String, Object> getGoodsDetailInfo(ProductIndexVo vo) throws LaiKeAPIException;

    /**
     * 显示评论详情显示
     *
     * @param vo   -
     * @param pid  -
     * @param type - 类型 (0=全部,1=好评,2=中评,3=差评,4=有图)
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-23 10:43:24
     */
    Map<String, Object> getComment(MainVo vo, int pid, Integer type) throws LaiKeAPIException;

    /**
     * 评论明细
     *
     * @param vo  -
     * @param sNo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/2/24 17:08
     */
    Map<String, Object> getCommentDetailInfo(MainVo vo, String sNo) throws LaiKeAPIException;

    /**
     * 添加购物车
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-23 11:04:51
     */
    Map<String, Object> addCart(AddCartVo vo) throws LaiKeAPIException;

    /**
     * 为你推荐
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 9:52
     */
    Map<String, Object> recommend(MainVo vo) throws LaiKeAPIException;

    public static void main(String[] args)
    {
        BigDecimal maxNum = new BigDecimal("10");
        maxNum = maxNum.divide(new BigDecimal(4), 0, BigDecimal.ROUND_UP);
        System.out.println(maxNum);
        BigDecimal roundStartNo = new BigDecimal(DataUtils.random(0, maxNum.intValue()));

        System.out.println(roundStartNo);
    }

    /**
     * 为你推荐
     *
     * @param vo       -
     * @param isReview -是否追评标识
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-23 15:21:25
     */
    Map<String, Object> addComment(AddCommentVo vo, boolean isReview) throws LaiKeAPIException;


    /**
     * 上传图片
     *
     * @param vo    -
     * @param image -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-06-23 15:36:12
     */
    Map<String, Object> uploadImgs(MainVo vo, MultipartFile image) throws LaiKeAPIException;
}
