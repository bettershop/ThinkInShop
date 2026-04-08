package com.laiketui.apps.api.app;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.ProductIndexVo;
import com.laiketui.domain.vo.cart.AddCartVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 商品接口
 *
 * @author Trick
 * @date 2020/10/26 8:40
 */
public interface AppsCstrProductService
{
    /**
     * 添加购物车
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 10:41
     */
    Map<String, Object> addCart(AddCartVo vo) throws LaiKeAPIException;


    /**
     * 显示评论详情显示
     *
     * @param storeId   -
     * @param language  -
     * @param accessId  -
     * @param goodsId   -
     * @param type      - 类型 (0=全部,1=好评,2=中评,3=差评,4=有图)
     * @param isbargain - 是否交易
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/23 16:04
     */
    Map<String, Object> getcomment(int storeId, String language, String accessId, int goodsId, String isbargain, int type, int pageNo) throws LaiKeAPIException;


    /**
     * 立即购买
     *
     * @param storeId     -
     * @param language    -
     * @param accessId    -
     * @param productJson - 商品信息集
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 11:14
     */
    boolean immediatelyCart(int storeId, String language, String accessId, String productJson) throws LaiKeAPIException;

    /**
     * 生成订单号
     *
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 15:41
     */
    String builderOrderNo() throws LaiKeAPIException;


    /**
     * 获取评论数据
     * (购买完后立即评论,获取一次商品信息)
     *
     * @param storeId        -
     * @param language       -
     * @param accessId       -
     * @param orderDetailsId - 订单详情id
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 15:49
     */
    Map<String, Object> getOrderDetailInfo(int storeId, String language, String accessId, int orderDetailsId) throws LaiKeAPIException;


    /**
     * base64转图片
     *
     * @param base64Str - base64图片字符串
     * @param path      - 路径
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/27 18:18
     */
    boolean base64ImageContent(String base64Str, String path) throws LaiKeAPIException;


    /**
     * 添加评论
     *
     * @param vo             -
     * @param anonymous      -
     * @param orderDetailsId -
     * @param start          -
     * @param comment        -
     * @param imgIndex       -
     * @param imgNum         -
     * @param image          -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/28 11:02
     */
    Map<String, Object> addComment(MainVo vo, int anonymous, int orderDetailsId, Integer start, String comment,
                                   Integer imgIndex, Integer imgNum, MultipartFile image) throws LaiKeAPIException;


    /**
     * 评论图片上传
     *
     * @param vo       -
     * @param cid      -
     * @param file     -
     * @param imgIndex -
     * @param imgNum   -
     * @param type     - 评论类型
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/17 14:38
     */
    Map<String, Object> imgComment(MainVo vo, int cid, MultipartFile file, int imgIndex, int imgNum, Integer type) throws LaiKeAPIException;

    /**
     * 获取商品详情首页
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/29 9:35
     */
    Map<String, Object> index(ProductIndexVo vo) throws LaiKeAPIException;
}
