package com.laiketui.apps.products.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.products.AppsProductService;
import com.laiketui.apps.products.consts.AppsProductConst;
import com.laiketui.common.api.*;
import com.laiketui.common.api.distribution.PubliceDistributionService;
import com.laiketui.common.api.plugin.seconds.PublicSecondsService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.SensitiveWordTool;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.orders.OrdersUtils;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.Page;
import com.laiketui.domain.auction.AuctionProductModel;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.distribution.DistributionGoodsModel;
import com.laiketui.domain.flashsale.FlashsaleActivityModel;
import com.laiketui.domain.group.GroupGoodsModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.plugin.member.MemberProModel;
import com.laiketui.domain.presell.PreSellConfigModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.product.*;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.seckill.SecondsProModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserFootprintModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.ProductIndexVo;
import com.laiketui.domain.vo.cart.AddCartVo;
import com.laiketui.domain.vo.goods.AddCommentVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;


/**
 * 商品
 *
 * @author Trick
 * @date 2020/10/26 8:37
 */
@Service
public class AppsProductServiceImpl implements AppsProductService
{
    private final Logger logger = LoggerFactory.getLogger(AppsProductServiceImpl.class);

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private FlashsaleRecordModelMapper flashsaleRecordModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addCart(AddCartVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        int                 cartId    = 0;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //未登录则看token是否存在,token不存在则生成token 返回前台
            if (StringUtils.isEmpty(vo.getAccessId()))
            {
                vo.setAccessId(JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId())));
            }

            //判断商品是否下架
            ProductListModel productListModel = new ProductListModel();
            productListModel.setId(vo.getGoodsId());
            productListModel = productListModelMapper.selectProduct(vo.getGoodsId());
            if (productListModel != null)
            {
                if (DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString().equals(productListModel.getStatus()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPYXJ, "该商品已下架", "addCart");
                }
                String isOpen = publicMchService.mchIsOpen(productListModel.getMch_id());
                //判断店铺是否已打烊
                if (!isOpen.equals("1"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPYDY, "店铺已打烊,无法加入购物车", "addCart");
                }

                //线下核销或需预约商品不可加入购物车
                if (productListModel.getCommodity_type() == 1)
                {
                    if (productListModel.getWrite_off_settings() == 1 || productListModel.getIs_appointment() == 2)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BKJRGWC, "不可加入购物车", "addCart");
                    }
                }

                //根据商品规格获取库存信息
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(vo.getAttributeId());
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                int stockNum = confiGureModel.getNum();
                //判断库存是否充足
                if (stockNum >= vo.getNum())
                {
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    //判断购物车是否存在相同商品
                    CartModel myCart = new CartModel();
                    myCart.setStore_id(vo.getStoreId());
                    //如果用户登陆了则用userid
                    if (user != null)
                    {
                        myCart.setUser_id(user.getUser_id());
                    }
                    else
                    {
                        myCart.setToken(vo.getAccessId());
                    }
                    myCart.setGoods_id(vo.getGoodsId());
                    myCart.setSize_id(vo.getAttributeId() + "");
                    myCart = cartModelMapper.selectOne(myCart);
                    if (myCart != null)
                    {
                        int needNum = vo.getNum();
                        if ("addcart".equals(vo.getType()))
                        {
                            //修改数量
                            needNum += myCart.getGoods_num();
                            if (needNum > stockNum)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "加入购物车数量已超库存数量", "addCart");
                            }
                            //虚拟商品需要核销的商品不能添加重复添加购物车
                            if (productListModel.getCommodity_type() == ProductListModel.COMMODITY_TYPE.virtual && productListModel.getWrite_off_settings() == ProductListModel.WRITE_OFF_SETTINGS.offline)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_XXHXSPBNCFTJGWC, "线下核销商品不能重复添加购物车", "addCart");
                            }
                        }
                        parmaMap.put("cart_id", myCart.getId());
                        parmaMap.put("Goods_num", needNum);
                        int count = cartModelMapper.updateCartById(parmaMap);
                        if (count < 1)
                        {
                            logger.debug("添加购物车失败-修改购物车 id = " + myCart.getId());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJGWCSB, "添加购物车失败", "addCart");
                        }
                        cartId = myCart.getId();
                    }
                    else
                    {
                        //添加购物车数据
                        CartModel insertCart = new CartModel();
                        insertCart.setStore_id(vo.getStoreId());
                        insertCart.setGoods_id(vo.getGoodsId());
                        insertCart.setGoods_num(vo.getGoodsId());
                        //如果用户登陆了则用userid
                        if (user != null)
                        {
                            insertCart.setUser_id(user.getUser_id());
                        }
                        else
                        {
                            insertCart.setUser_id("");
                        }
                        insertCart.setToken(vo.getAccessId());
                        insertCart.setGoods_num(vo.getNum());
                        insertCart.setCreate_time(new Date());
                        insertCart.setSize_id(vo.getAttributeId() + "");
                        int count = cartModelMapper.insert(insertCart);
                        if (count < 1)
                        {
                            logger.debug("添加购物车失败 goodsid = " + vo.getGoodsId());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJGWCSB, "添加购物车失败", "addCart");
                        }
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "addCart");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "addCart");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加购物车失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addCart");
        }
        resultMap.put("cart_id", cartId);
        resultMap.put("type", vo.getType());
        resultMap.put("access_id", vo.getAccessId());
        return resultMap;
    }


    @Override
    public Map<String, Object> getcomment(int storeId, String language, String accessId, int goodsId, String isbargain, int type, int pageNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new Hashtable<>(16);
        //评论数量
        int commentsTotal = 0;
        //好评数量
        int commentsHao = 0;
        //中评数量
        int commentsZhong = 0;
        //差评数量
        int commentsCha = 0;
        //评论图片数量
        int commentsImage = 0;
        try
        {
            Page pageModel = Page.newBuilder((pageNo - 1) * GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE, GloabConst.PageEnum.TERMINAL_DEFAULT_PAGESIZE, null);
            //根据条件获取评论信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("pid", goodsId);
            parmaMap.put("pageNo", pageModel.getPageNo());
            parmaMap.put("pageSize", pageModel.getPageSize());
            parmaMap.put("group_commentId", "group_commentId");
            //类型(0=全部,1=好评,2=中评,3=差评,4=有图)
            parmaMap.put("type", type);
            List<Map<String, Object>> commentsMap = publicService.getGoodsCommentList(parmaMap);

            parmaMap.clear();
            parmaMap.put("store_id", storeId);
            parmaMap.put("pid", goodsId);
            commentsTotal = commentsModelMapper.countCommentsUserDynamicByPid(parmaMap);

            Map<String, Object> haoPinMap = new HashMap<>(16);
            haoPinMap.putAll(parmaMap);
            haoPinMap.put("type", 1);
            commentsHao = commentsModelMapper.countCommentsUserDynamicByPid(haoPinMap);

            Map<String, Object> zhongPinMap = new HashMap<>(16);
            zhongPinMap.putAll(parmaMap);
            zhongPinMap.put("type", 2);
            commentsZhong = commentsModelMapper.countCommentsUserDynamicByPid(zhongPinMap);

            Map<String, Object> chaPinMap = new HashMap<>(16);
            chaPinMap.putAll(parmaMap);
            chaPinMap.put("type", 3);
            commentsCha = commentsModelMapper.countCommentsUserDynamicByPid(chaPinMap);

            Map<String, Object> imageMap = new HashMap<>(16);
            imageMap.putAll(parmaMap);
            imageMap.put("type", 4);
            commentsImage = commentsModelMapper.countCommentsUserDynamicByPid(imageMap);

            resultMap.put("data", commentsMap);
            resultMap.put("comments_total", commentsTotal);
            resultMap.put("comments_hao", commentsHao);
            resultMap.put("comments_zhong", commentsZhong);
            resultMap.put("comments_cha", commentsCha);
            resultMap.put("comments_image", commentsImage);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载评论异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getcomment");
        }
        return resultMap;
    }


    @Override
    public boolean immediatelyCart(int storeId, String language, String accessId, String productJson, String orderType) throws LaiKeAPIException
    {
        boolean                   result      = false;
        List<Map<String, Object>> productList = null;
        try
        {
            try
            {
                productList = JSON.parseObject(productJson, new TypeReference<List<Map<String, Object>>>()
                {
                });
            }
            catch (JSONException j)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSGSBZQ, productJson + ">参数格式不正确", "immediatelyCart");
            }
            Map<String, Object> productMap = null;
            if (StringUtils.isEmpty(orderType))
            {
                orderType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
            }
            List<Map<String, Object>> ret = publicService.productsList(productList, "", 0, orderType);
            if (!CollectionUtils.isEmpty(ret))
            {
                //商品详情页的立即购买按钮 最多只有一条数据
                productMap = ret.get(0);
            }
            if (productMap != null && productMap.size() > 0)
            {
                result = true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("立即购买异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "immediatelyCart");
        }
        return result;
    }

    @Override
    public String builderOrderNo() throws LaiKeAPIException
    {
        String orderNo = "";
        try
        {
            orderNo = OrdersUtils.builderOrderNo();
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("生成订单异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "builderOrderNo");
        }
        return orderNo;
    }


    @Override
    public Map<String, Object> getOrderDetailInfo(int storeId, String language, String accessId, int orderDetailsId,Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        List<Map<String, Object>> commList = getCommList(storeId, accessId, orderDetailsId,type);
        resultMap.put("commentList", commList);
        return resultMap;
    }



    @Override
    public boolean base64ImageContent(String base64Str, String path) throws LaiKeAPIException
    {
        try
        {
            ImageBase64ConverterUtils.convertBase64ToFile(base64Str, path);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("base64图片上传异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "base64ImageContent");
        }
        return true;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addComment(MainVo vo, int anonymous, int orderDetailsId,
                                          Integer start, String comment, Integer imgIndex, Integer imgNum, MultipartFile image) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(comment))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLNRBNWK, "评论内容不能为空", "addComment");
            }
            //是否追评标识
            boolean isReview = false;
            User    user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取订单明细信息
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setId(orderDetailsId);
            orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
            if (orderDetailsModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDGDDXX, "未找到该订单信息", "addComment");
            }
            if (!user.getUser_id().equals(orderDetailsModel.getUser_id()))
            {
                logger.warn("用户 user = " + user.getUser_id() + " 正在请求订单id=" + orderDetailsId + ";被拦截!");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "addComment");
            }
            if (start == null || start == 0)
            {
                isReview = true;
            }
            if (!isReview)
            {
                if (start < 1 || start > 5)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PXCGFW, "评星超过范围");
                }
            }
            String orderNo = orderDetailsModel.getR_sNo();
            //获取商品规格信息
            ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(orderDetailsModel.getSid());
            if (confiGureModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGSJCW, "商品规格数据错误");
            }

            //检查是否已经评论过了
            CommentsModel commentsModel = new CommentsModel();
            commentsModel.setOid(orderNo);
            commentsModel.setOrder_detail_id(orderDetailsId);
            commentsModel = commentsModelMapper.selectOne(commentsModel);
            if (!isReview)
            {
                if (commentsModel != null)
                {
                    logger.warn("用户 user = " + user.getUser_id() + " 正在重复评论,订单号=" + orderNo + ";被拦截!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YPLGL, "已评论过了");
                }
            }
            else
            {
                if (commentsModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXPLHZP, "请先评论后追评");
                }
                logger.debug(user.getUser_id() + " 开始追加评论");
            }

            logger.debug(user.getUser_id() + "的评论,原内容:" + comment);
            //评论脱敏处理
            comment = sensitiveWordTool.filterInfo(comment);
            //评论xss防范
            comment = XssUtil.stripXSS(comment);
            logger.debug(user.getUser_id() + "的评论,处理后的内容:" + comment);

            int           count;
            CommentsModel commentsSave = new CommentsModel();
            if (isReview)
            {
                if (imgNum != null)
                {
                    //上传图片 只有到最后一张图片才会真正保存到数据库中
                    imgComment(vo, commentsModel.getId(), image, imgIndex, imgNum, 1);
                }
                if (imgNum == null || (imgIndex + 1) == imgNum)
                {
                    //追加评论
                    commentsSave.setId(commentsModel.getId());
                    commentsSave.setReview(comment);
                    commentsSave.setReview_time(new Date());
                    count = commentsModelMapper.updateByPrimaryKeySelective(commentsSave);
                }
                else
                {
                    count = 1;
                }
            }
            else
            {
                //添加评论
                commentsSave.setStore_id(vo.getStoreId());
                commentsSave.setOid(orderNo);
                commentsSave.setUid(user.getUser_id());
                commentsSave.setPid(confiGureModel.getPid() + "");
                commentsSave.setAttribute_id(confiGureModel.getId());
                commentsSave.setContent(comment);
                commentsSave.setCommentType(start + "");
                commentsSave.setAnonymous(anonymous + "");
                commentsSave.setOrder_detail_id(orderDetailsId);
                commentsSave.setAdd_time(new Date());
                count = commentsModelMapper.insertSelective(commentsSave);
            }

            if (count > 0)
            {
                if (!isReview)
                {
                    //增加商品评论的数量
                    count = productListModelMapper.updateAddCommentNum(confiGureModel.getPid());
                }
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
                }
                resultMap.put("cid", commentsSave.getId());
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品评论异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addComment");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> imgComment(MainVo vo, int cid, MultipartFile file, int imgIndex, int imgNum, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (type == null)
                {
                    type = 0;
                }
                String key = user.getUser_id() + cid;
                //商品图片集
                List<String> imgUlrs = new ArrayList<>();
                if (file != null)
                {
                    if (imgIndex == 0)
                    {
                        //清空缓存
                        redisUtil.del(GloabConst.RedisHeaderKey.COMMENTS_UPLOAD_KEY + key);
                    }
                    //获取之前的图片
                    Object obj = redisUtil.get(GloabConst.RedisHeaderKey.COMMENTS_UPLOAD_KEY + key);
                    if (obj != null)
                    {
                        imgUlrs = DataUtils.cast(obj);
                        if (imgUlrs == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
                        }
                    }
                    List<MultipartFile> files = new ArrayList<>();
                    files.add(file);
                    //默认使用oos
                    String uploadType = GloabConst.UploadConfigConst.IMG_UPLOAD_OSS;
                    //获取店铺信息
                    ConfigModel configModel = new ConfigModel();
                    configModel.setStore_id(vo.getStoreId());
                    configModel = configModelMapper.selectOne(configModel);
                    if (configModel != null)
                    {
                        uploadType = configModel.getUpserver();
                    }
                    //图片上传
                    List<String> urls = publiceService.uploadImage(files, uploadType, vo.getStoreType(), vo.getStoreId());
                    if (urls.size() != files.size())
                    {
                        logger.info(String.format("图片上传失败 需上传:%s 实际上传:%s", files.size(), imgUlrs.size()));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "returnData");
                    }
                    imgUlrs.add(ImgUploadUtils.getUrlImgByName(urls.get(0), true));
                    redisUtil.set(GloabConst.RedisHeaderKey.COMMENTS_UPLOAD_KEY + key, imgUlrs, 30);
                    if (imgNum != imgIndex + 1)
                    {
                        resultMap.put("comment_id", cid);
                        return resultMap;
                    }
                }
                //清空缓存
                redisUtil.del(GloabConst.RedisHeaderKey.RETURN_UPLOAD_KEY + key);

                //记录图片
                for (String url : imgUlrs)
                {
                    CommentsImgModel commentsImgModel = new CommentsImgModel();
                    commentsImgModel.setComments_url(ImgUploadUtils.getUrlImgByName(url, true));
                    commentsImgModel.setComments_id(cid);
                    commentsImgModel.setType(type);
                    commentsImgModel.setAdd_time(new Date());
                    int count = commentsImgModelMapper.insertSelective(commentsImgModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败");
                    }
                }
                return resultMap;
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZXDL, "请重新登录");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("评论图片上传 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "imgComment");
        }
    }

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private GroupGoodsModelMapper groupGoodsModelMapper;
    @Autowired
    private BbsPostModelMapper bbsPostModelMapper;
    @Autowired
    private AuctionProductModelMapper auctionProductModelMapper;


    @Override
    public Map<String, Object> index(ProductIndexVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //商品类型跟订单类型类似
            String              proType  = vo.getType();
            Map<String, Object> parmaMap = new HashMap<>(16);
            User                user     = null;
            String              token    = vo.getAccessId();
            StringBuilder       userid   = new StringBuilder("游客").append(DateUtil.timeStamp());

            //商品折扣价格
            BigDecimal vipPrice;
            //折扣
            BigDecimal gradeRate = new BigDecimal("1");
            //商品规格
            String unit = "";

            //是否是会员
            int isVip = 0;
            //是否收藏 0 = 未收藏
            int type = 0;
            //收藏id
            Integer collectionId = null;
            //店铺图标
            String logo = "";
            //商品信息
            Map<String, Object> goodsMap = new HashMap<>(16);
            //预售商品信息
            Map<String, Object> sellGoodMap = new HashMap<>(16);
            //店铺信息
            Map<String, Object> shopMap;
            //评论信息
            List<Map<String, Object>> commentsList;
            // 总评论数目
            int commentsTotal;
            //商品图片集
            List<String> imgList = new ArrayList<>();
            //分类名称
            String pname = "";
            //产品活动类型
            String active = "";
            //优惠卷状态
            int couponStatus = 0;
            //限时折扣折扣值
            BigDecimal discount = null;
            if (!StringUtils.isEmpty(token))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId());
                if (userObj != null)
                {
                    user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                    if (user.getId() != null)
                    {
                        user = userBaseMapper.selectByPrimaryKey(user.getId());
                    }
                    userid = new StringBuilder(user.getUser_id());
                    if (user.getGrade() != null)
                    {
                        isVip = user.getGrade() > 0 ? 1 : 0;
                    }
                    //获取用户收藏列表
                    UserCollectionModel userCollectionModel = new UserCollectionModel();
                    userCollectionModel.setStore_id(vo.getStoreId());
                    userCollectionModel.setUser_id(user.getUser_id());
                    userCollectionModel.setP_id(vo.getPro_id());
                    userCollectionModel = userCollectionModelMapper.selectOne(userCollectionModel);
                    if (userCollectionModel != null)
                    {
                        type = 1;
                        collectionId = userCollectionModel.getId();
                    }
                    //插入一条足迹记录
                    UserFootprintModel userFootprintModel = new UserFootprintModel();
                    userFootprintModel.setStore_id(vo.getStoreId());
                    userFootprintModel.setUser_id(user.getUser_id());
                    userFootprintModel.setP_id(vo.getPro_id());
                    userFootprintModel.setAdd_time(new Date());
                    int fid = userFootprintModelMapper.saveUserFootprint(userFootprintModel);
                    if (fid < 1)
                    {
                        logger.info(user.getUser_id() + " 用户足迹插入失败 商品id= " + vo.getPro_id());
                    }
                }
            }
            else
            {
                //没有token则获取一个token返回出去
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            //获取产品数据
            /*ProductListModel productListModel = new ProductListModel();
            productListModel.setId(vo.getPro_id());
            productListModel.setCommodity_type(null);
            productListModel = productListModelMapper.selectOne(productListModel);*/
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(vo.getPro_id());
            if (productListModel != null)
            {
                //商品id
                int goodsId = productListModel.getId();
                //商品订单类型
                String orderGoodsType = vo.getType();
                //店铺id
                Integer mchId = productListModel.getMch_id();
                //商品状态
                String status = productListModel.getStatus();
                //商品标题
                String productTitle = productListModel.getProduct_title();
                //产品活动类型
                active = productListModel.getActive();
                //商品封面图
                String imgUrl = productListModel.getImgurl();
                //商品类别
                String goodsClass = productListModel.getProduct_class();
                //商品详情内容
                String content = productListModel.getContent();
                //商品销量
                int volume = productListModel.getVolume();
                //商品真实销量
                Integer realVolume = productListModel.getReal_volume();
                //展示视频
                String video = productListModel.getVideo();
                //商品视频
                String proVideo = productListModel.getProVideo();
                //商品类型 0.实物商品 1.虚拟商品
                Integer commodity_type = productListModel.getCommodity_type();
                //可以加入购物车
                resultMap.put("isAddCar", 1);
                resultMap.put("commodity_type", commodity_type);

                //虚拟商品特殊处理
                if (commodity_type != null && commodity_type.equals(ProductListModel.COMMODITY_TYPE.virtual))
                {
                    //是否是需要预约时间的虚拟商品，不可加入购物车
                    //核销设置 1.线下核销 2.无需核销
                    Integer write_off_settings = productListModel.getWrite_off_settings();
                    //预约时间设置 1.无需预约下单 2.需要预约下单
                    Integer                   is_appointment = productListModel.getIs_appointment();
                    List<Map<String, Object>> mchStoreList   = new ArrayList<>();
                    List<Map<String, Object>> writeStore     = new ArrayList<>();
                    //需要线下核销的商品，不可加入购物车
                    if (write_off_settings.equals(ProductListModel.WRITE_OFF_SETTINGS.offline))
                    {
                        resultMap.put("isAddCar", 2);
                        //获取核销门店列表
                        Map<String, Object> paraMap = new HashMap<>();
                        paraMap.put("store_id", vo.getStoreId());
                        paraMap.put("mch_id", mchId);
                        String write_off_mch_ids = productListModel.getWrite_off_mch_ids();
                        //55984 显示商品可核销的所有门店而不是其中有可用预约时间的门店
                        Integer mchStoreListNum = 0;
                        if (write_off_mch_ids != null && !write_off_mch_ids.equals("0"))
                        {
                            paraMap.put("idList", Arrays.asList(write_off_mch_ids.split(SplitUtils.DH)));
                            int num = mchStoreModelMapper.countDynamic(paraMap);
                            mchStoreListNum = num;
                        }
                        else if (write_off_mch_ids != null && write_off_mch_ids.equals("0"))
                        {
                            mchStoreListNum = mchStoreModelMapper.countDynamic(paraMap);
                        }
                        resultMap.put("mchStoreListNum", mchStoreListNum);
                        //排除逻辑删除
                        paraMap.put("recycle", 0);
                        paraMap.put("order_id", "order_id");
                        int writeNumStatus = 1;
                        writeStore = mchStoreWriteModelMapper.selectDynamic(paraMap);

                        if (Objects.equals(is_appointment, ProductListModel.IS_APPOINTMENT.noOpin))
                        {
                            writeNumStatus = 1;
                        }else
                        {
                            if (CollectionUtils.isEmpty(writeStore))
                            {
                                writeNumStatus = 2;
                            }
                            else
                            {
                                int i = 0;
                                int j = 0;
                                for (Map<String, Object> map : writeStore)
                                {
                                    if (MapUtils.getIntValue(map, "write_off_num") == 0)
                                    {
                                        //当有一个门店设置为0，则为可无限预约的门店，该商品则不存在无预约排期的情况
                                        break;
                                    }
                                    String   off_num       = MapUtils.getString(map, "off_num");
                                    Integer  write_off_num = MapUtils.getIntValue(map, "write_off_num");
                                    String[] split         = off_num.split(SplitUtils.DH);
                                    for (String s : split)
                                    {
                                        if (Integer.valueOf(s) >= write_off_num)
                                        {
                                            i++;
                                        }
                                    }
                                    if (i == split.length)
                                    {
                                        j++;
                                    }
                                }
                                if (j == writeStore.size())
                                {
                                    //预约排期已满
                                    writeNumStatus = 2;
                                }
                                else
                                {
                                    //可以购买
                                    writeNumStatus = 1;
                                }
                            }
                        }
                        resultMap.put("writeNumStatus",writeNumStatus);
                        //string放日期
                        Map<String, List<Map<String, Object>>> HashMap = new HashMap<>();
                        List<String>                           ids     = new ArrayList<>();
                        for (Map<String, Object> originalMap : writeStore)
                        {
                            String id = MapUtils.getString(originalMap, "id");
                            if (ids != null && ids.size() > 0 && !ids.contains(id))
                            {
                                Map<String, List<Map<String, Object>>> TempMap = new HashMap<>();
                                HashMap = TempMap;
                            }
                            ids.add(MapUtils.getString(originalMap, "id"));
                            // 提取原始数据
                            String start_time    = MapUtils.getString(originalMap, "start_time");
                            String end_time      = MapUtils.getString(originalMap, "end_time");
                            int    write_off_num = MapUtils.getInteger(originalMap, "write_off_num", 0); // 假设默认为0
                            String w_id          = MapUtils.getString(originalMap, "w_id");
                            int    writeStatus   = 0;
                            if (write_off_num == 0 || MapUtils.getIntValue(originalMap, "write_off_num") <= MapUtils.getIntValue(originalMap, "off_num"))
                            {
                                writeStatus = 1;
                            }
                            // 格式化时间
                            String start_time_ymd = DateUtil.dateFormate(start_time, GloabConst.TimePattern.YMD);
                            String end_time_ymd   = DateUtil.dateFormate(end_time, GloabConst.TimePattern.YMD);
                            String start_time_hm  = DateUtil.dateFormate(start_time, GloabConst.TimePattern.HM);
                            String end_time_hm    = DateUtil.dateFormate(end_time, GloabConst.TimePattern.HM);

                            // 构造时间字符串，但不作为map的键使用
                            String time_range = start_time_hm + SplitUtils.BL + end_time_hm; // 假设SplitUtils.BL是时间分隔符，如"-"

                            // 创建一个新的map来存储格式化后的数据和核销次数
                            Map<String, Object> formattedMap = new HashMap<>();
                            formattedMap.put("start_time", start_time_ymd);
                            formattedMap.put("end_time", end_time_ymd);
                            formattedMap.put("start_time_home", start_time_hm);
                            formattedMap.put("end_time_home", end_time_hm);
                            formattedMap.put("time_range", time_range);
                            formattedMap.put("w_id", w_id);
                            formattedMap.put("write_status", writeStatus);

                            // 获取日期范围内的所有日期
                            List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                            //对日期进行排序

                            List<Map<String, Object>> list1;
                            for (String s : intervalDate)
                            {
                                //s = DateUtil.dateFormate(s, GloabConst.TimePattern.MD2);
                                if (HashMap.containsKey(s))
                                {
                                    list1 = HashMap.get(s);
                                    list1.add(formattedMap);
                                }
                                else
                                {
                                    list1 = new ArrayList<>();
                                    list1.add(formattedMap);
                                    HashMap.put(s, list1);
                                }
                            }

                            /*Map<String, Map<String,Integer>> list1 = dataMap.computeIfAbsent(w_id, k -> new HashMap<>());
                            list1.add(m); // 这里添加的是同一个formattedMap对象到每个日期的列表中*/


                            originalMap.put("date", HashMap);
                        }
                        /*Set<String> seenIds = new HashSet<>();
                        writeStore.removeIf(map -> !seenIds.add(map.get("id").toString()));*/
                        //writeStore.add(dataMap);

                        /*if (mchStoreWriteModelMapper.countDynamic(paraMap) > 0){
                            //可以购买
                            resultMap.put("writeNumStatus", 1);
                        }else {
                            //预约排期已满
                            resultMap.put("writeNumStatus", 2);
                        }*/
                    }
                    resultMap.put("write_off_settings", write_off_settings);
                }

                //是否能购买
                int canbuy = 1;

                imgUrl = publicService.getImgPath(imgUrl, vo.getStoreId());
                //获取商品统计信息
                shopMap = publicService.commodityInformation(vo.getStoreId(), mchId,null);
                //获取店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setId(mchId);
                mchModel = mchModelMapper.selectOne(mchModel);
                if (mchModel != null)
                {
                    shopMap.put("shop_id", mchId);
                    shopMap.put("shop_name", mchModel.getName());
                    //未营业
                    if (mchModel.getIs_open().equals("0") || mchModel.getIs_open().equals("2"))
                    {
                        shopMap.put("is_open", mchModel.getIs_open());
                    }
                    else if (mchModel.getIs_open().equals("1"))
                    {
                        //营业时间判断是否营业
                        String[] businessHours = mchModel.getBusiness_hours().split(SplitUtils.BL);
                        //开始时间
                        Date startTime = DateUtil.dateFormateToDate(businessHours[0], GloabConst.TimePattern.HM);
                        //结束时间
                        Date endTime = DateUtil.dateFormateToDate(businessHours[1], GloabConst.TimePattern.HM);
                        //当前时间
                        Date currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.HM);
                        //开始时间大于结束时间(跨天：例如 19：00~04：00 则当前时间 >= 19:00 || 当前时间 <= 04:00 -> 营业 )
                        if (DateUtil.dateCompare(startTime, endTime))
                        {
                            if (!DateUtil.dateCompare(startTime, currentDate)
                                    || !DateUtil.dateCompare(currentDate, endTime))
                            {
                                //营业
                                shopMap.put("is_open", "1");
                            }
                            else
                            {
                                //未营业
                                shopMap.put("is_open", "2");
                            }
                        }
                        else
                        {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                            if (!DateUtil.dateCompare(startTime, currentDate)
                                    && !DateUtil.dateCompare(currentDate, endTime))
                            {
                                //营业
                                shopMap.put("is_open", "1");
                            }
                            else
                            {
                                //未营业
                                shopMap.put("is_open", "2");
                            }
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
                    }
                    String logoUrl = publicService.getImgPath(mchModel.getLogo(), vo.getStoreId());
                    shopMap.put("shop_logo", logoUrl);
                    String headImg = publicService.getImgPath(mchModel.getHead_img(), vo.getStoreId());
                    shopMap.put("shop_head", headImg);
                    shopMap.put("is_lock", mchModel.getIs_lock());
                }
                //添加一条商品浏览记录
                MchBrowseModel mchBrowseModel = new MchBrowseModel();
                mchBrowseModel.setStore_id(vo.getStoreId());
                mchBrowseModel.setToken(token);
                mchBrowseModel.setMch_id(mchId.toString());
                mchBrowseModel.setUser_id(userid.toString());
                mchBrowseModel.setEvent("访问了店铺");
                mchBrowseModel.setAdd_time(new Date());
                int id = mchBrowseModelMapper.saveBrowse(mchBrowseModel);
                if (id < 1)
                {
                    logger.info(userid + " 用户店铺浏览记录插入失败 店铺id= " + mchId);
                }
                //展示视频
                if (StringUtils.isNotEmpty(video))
                {
                    imgList.add(publiceService.getImgPath(video, vo.getStoreId()));
                }
                //获取该商品图片集
                ProductImgModel productImgModel = new ProductImgModel();
                productImgModel.setProduct_id(vo.getPro_id());
                List<ProductImgModel> productImgList = productImgModelMapper.select(productImgModel);
                for (ProductImgModel productImg : productImgList)
                {
                    String goodsImgUrl = productImg.getProduct_url();
                    goodsImgUrl = publicService.getImgPath(goodsImgUrl, vo.getStoreId());
                    imgList.add(goodsImgUrl);
                }
                //产品类型处理
                String[] goodsClassList = goodsClass.trim().split("-");
                for (String cid : goodsClassList)
                {
                    if (StringUtils.isEmpty(cid))
                    {
                        continue;
                    }
                    //获取分类名称 如果一级没查到则网上查直到查到为止
                    ProductClassModel productClassModel = new ProductClassModel();
                    productClassModel.setCid(Integer.parseInt(cid));
                    productClassModel = productClassModelMapper.selectOne(productClassModel);
                    if (productClassModel != null)
                    {
                        pname = productClassModel.getPname();
                        break;
                    }
                }
                //产品内容xss拦截处理
                //content = XssUtil.stripXSS(content);

                //价格处理
                ProductListModel productListParma = new ProductListModel();
                productListParma.setId(productListModel.getId());
                productListParma.setStore_id(productListModel.getStore_id());
                //获取商品最低/最高价格
                Map<String, BigDecimal> goodsPricMap = productListModelMapper.getGoodsPriceMinAndMax(productListParma);
                BigDecimal              minPrice     = goodsPricMap.get("minPrice");
                BigDecimal              maxPrice     = goodsPricMap.get("maxPrice");
                BigDecimal              minYprice    = goodsPricMap.get("minYprice");
                BigDecimal              maxYprice    = goodsPricMap.get("maxYprice");
                //商品成本价
                BigDecimal costprice = goodsPricMap.get("costprice");
                //出售价格
                goodsMap.put("price", minPrice);
                //原来价格
                goodsMap.put("yprice", minYprice);
                goodsMap.put("vip_yprice", minPrice);
                //计算折扣价
                BigDecimal showGradeRate = new BigDecimal("1");
                if (user != null)
                {
                    gradeRate = showGradeRate = BigDecimal.valueOf(publicMemberService.getMemberGradeRate(orderGoodsType, user.getUser_id(), vo.getStoreId()));
                }
                //未登录则用商城最低折扣来计算显示价
                if (gradeRate.compareTo(new BigDecimal("1")) == 0)
                {
                    showGradeRate = userGradeModelMapper.getGradeLow(vo.getStoreId()).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                }
                vipPrice = minPrice.multiply(showGradeRate);
                goodsMap.put("vip_price", vipPrice);
                if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equals(proType))
                {
                    goodsMap.put("is_distribution", 1);
                    goodsMap.put("vip_price", 0);
                }
                else
                {
                    goodsMap.put("is_distribution", 0);
                }
                //获取运费信息
                BigDecimal yunfei = publicGoodsService.getGoodsFreight(user, goodsId);
                //品牌处理
                String  brandName = "无";
                Integer brandId   = productListModel.getBrand_id();
                if (brandId != null)
                {
                    BrandClassModel brandClassModel = new BrandClassModel();
                    brandClassModel.setBrand_id(brandId);
                    brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                    if (brandClassModel != null)
                    {
                        brandName = brandClassModel.getBrand_name();
                    }
                }
                //多规格的情况修正库存
                Map<Integer, Integer> otherStockNumMap = null;

                //其它活动规格id
                List<Integer> otherAttrIds = new ArrayList<>();
                //是否需要重新计算价格 比如 秒杀的百分比
                BigDecimal rePriceScale = null;
                //修订后的价格
                BigDecimal price = null;
                //修订后的规格价格
                Map<Integer, BigDecimal> priceReviseMap = new HashMap<>(16);
                //修订后的积分商品兑换所需积分
                Map<Integer, Integer> integralMap = null;
                //如果是秒杀则获取秒杀价格、库存、秒杀状态
                if (vo.getId() != null && DictionaryConst.OrdersType.ORDERS_HEADER_MS.equalsIgnoreCase(orderGoodsType))
                {
                    //秒杀详情不显示会员价
                    gradeRate = new BigDecimal("1");
                    //2021-10-21 14:36:41 新版秒杀
                    //获取秒杀活动信息
                    SecondsActivityModel secondsActivityModel = secondsActivityModelMapper.selectByPrimaryKey(vo.getId());
                    if (secondsActivityModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MSSPBCZ, "秒杀商品不存在");
                    }

                    //获取规格价格最小的价格,所有插件不应展示商品售价计算的价格,而应展示规格金额最低计算后的价格
                    ConfiGureModel attrTemp = confiGureModelMapper.getProductMaxPriceAndMaxPrice(productListModel.getId());
                    //秒杀商品售价
                    BigDecimal goodsPrice = attrTemp.getPrice();

                    //商品原价
                    goodsMap.put("yprice", attrTemp.getYprice());
                    //秒杀价格
                    price = secondsActivityModel.getSeconds_price();
                    if (secondsActivityModel.getPrice_type() == 0)
                    {
                        //如果设置的是百分比则每个规格的价格重新计算
                        rePriceScale = price;
                        price = goodsPrice.multiply(price).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                        //禅道 56632 如果金额低于0.01 设置为0.01
                        if (price.compareTo(new BigDecimal("0.01")) < 0)
                        {
                            price = new BigDecimal("0.01");
                        }
                    }
                    goodsMap.put("price", price);
                    goodsMap.put("num", secondsActivityModel.getNum());
                    goodsMap.put("max_num", secondsActivityModel.getMax_num());
                    //获取秒杀限购 默认不限购
                    int                buyNum             = 0;
                    SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                    secondsConfigModel.setStore_id(vo.getStoreId());
                    secondsConfigModel.setMch_id(productListModel.getMch_id());
                    secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                    if (secondsConfigModel != null && secondsConfigModel.getBuy_num() > 0)
                    {
                        buyNum = secondsConfigModel.getBuy_num();
                    }
                    if (user != null && buyNum > 0)
                    {
                        //是否超过限购数量
                        int secNum = secondsProModelMapper.getUserSecNum(secondsActivityModel.getId(), user.getUser_id());
                        buyNum -= secNum;
                        if (buyNum <= 0)
                        {
                            //超过限购数量
                            buyNum = -1;
                        }
                    }
                    goodsMap.put("buyNum", buyNum);
                    //获取秒杀商品规格信息
                    /*SecondsProModel secondsProModel = new SecondsProModel();
                    secondsProModel.setStore_id(vo.getStoreId());
                    secondsProModel.setActivity_id(secondsActivityModel.getId());
                    secondsProModel.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
                    List<SecondsProModel> secondsProModelList = secondsProModelMapper.select(secondsProModel);*/
                    //修正库存数量
                    otherStockNumMap = new HashMap<>(16);
                    if (Objects.nonNull(secondsActivityModel.getAttrId())){
                        otherStockNumMap.put(secondsActivityModel.getAttrId(),secondsActivityModel.getNum());
                    }
                    //秒杀状态
                    int secStatus = publicSecondsService.getSecondsStatus(vo.getStoreId(), secondsActivityModel.getId(), secondsActivityModel.getNum(), secondsActivityModel.getStarttime(), secondsActivityModel.getEndtime(), true);
                    goodsMap.put("secStatus", secStatus);

                    //倒计时
                    long remainingTime = secondsActivityModel.getEndtime().getTime();
                    //如果是预告则为开始时间
                    if (SecondsProModel.SecondsStatus.SECKILL_STATUS_HERALD == secStatus)
                    {
                        remainingTime = secondsActivityModel.getStarttime().getTime();
                    }
                    goodsMap.put("remainingTime", remainingTime);
                }
                else if (DictionaryConst.OrdersType.PTHD_ORDER_PM.equalsIgnoreCase(vo.getType()))
                {
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_FX.equals(vo.getType()))
                {
                    //获取分销商品集合otherAttrIds
                    DistributionGoodsModel distributionGoodsModel = distributionGoodsModelMapper.selectByPrimaryKey(vo.getId());
                    if (distributionGoodsModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPBCZ, "分销商品不存在");
                    }
                    if (DictionaryConst.ProductRecycle.RECOVERY.equals(distributionGoodsModel.getRecycle()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPYBSC, "分销商品已被删除");
                    }
                    Map<String, Object> disUserInfo = null;
                    if (user != null)
                    {
                        //获取用户分销商信息
                        disUserInfo = userDistributionModelMapper.getUserGradeinfo(vo.getStoreId(), user.getUser_id());
                    }
                    //单位
                    BigDecimal directMtype = BigDecimal.ZERO;
                    //直推金额
                    BigDecimal directM = BigDecimal.ZERO;
                    //分销价格
                    BigDecimal fxPrice;
                    if (disUserInfo == null)
                    {
                        //不是分销身份则获取商城top1分销人
                        DistributionGradeModel userTop1Distribution = distributionGradeModelMapper.getUserTop1Distribution(vo.getStoreId());
                        if (userTop1Distribution == null)
                        {
                            logger.error("商城【{}】分销配置有问题：未配置分销等级!", vo.getStoreId());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSZFXDJ, "请设置分销等级", "getGoodsList");
                        }
                        String              gradeStr = userTop1Distribution.getSets();
                        Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(gradeStr);
                        if (gradeMap.containsKey("direct_m_type"))
                        {
                            directMtype = new BigDecimal(gradeMap.get("direct_m_type").toString());
                        }
                        if (gradeMap.containsKey(DistributionGoodsModel.DistributionRuleKey.DIRECT_M))
                        {
                            directM = new BigDecimal(gradeMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M).toString());
                        }
                    }
                    else
                    {
                        Map<String, Object> gradeMap = SerializePhpUtils.getDistributionGradeBySets(MapUtils.getString(disUserInfo, "sets"));
                        if (gradeMap.containsKey("direct_m_type"))
                        {
                            directMtype = new BigDecimal(gradeMap.get("direct_m_type").toString());
                        }
                        if (gradeMap.containsKey(DistributionGoodsModel.DistributionRuleKey.DIRECT_M))
                        {
                            directM = new BigDecimal(gradeMap.get(DistributionGoodsModel.DistributionRuleKey.DIRECT_M).toString());
                        }
                    }

                    //分销商品对应的等级折扣 1 为无折扣
                    BigDecimal fxGoodsLevelDiscount = new BigDecimal(100);
                    List<Map<String, Object>> goodsDisDiyRules     = null;
                    //是否是自定义规则
                    if (distributionGoodsModel.getDistribution_rule().equals(DistributionGoodsModel.DISTRIBUTION_RULE_CUSTOM))
                    {
                        /////新增开始
                        goodsDisDiyRules = SerializePhpUtils.getUnserializeToList(distributionGoodsModel.getRules_set());
                        if (Objects.isNull(goodsDisDiyRules))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FXSPWSZDIYRULE, "分销商品未设置自定义分销规则", "orderDistribution");
                        }

                        //当前用户分销等级ID
                        if (disUserInfo != null && disUserInfo.get("level") != null)
                        {
                            int fxLevelId = MapUtils.getIntValue(disUserInfo, "level");
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
                                    logger.info("商品->{}对应的自定义分销折扣->{}", goodsId, gradeDiydisCount);
                                    break;
                                }
                            }
                        }
                    }

                    //计算分销商折扣价
                    BigDecimal disPrice = BigDecimal.ZERO;

                    if (user != null)
                    {
                        if (!Objects.isNull(goodsDisDiyRules) && fxGoodsLevelDiscount.compareTo(BigDecimal.ZERO) > 0)
                        {
                            disPrice = fxGoodsLevelDiscount.multiply(minPrice).multiply(new BigDecimal(0.01));
                        }
                        else
                        {
                            disPrice = publiceDistributionService.getGoodsPrice(vo.getStoreId(), user.getUser_id(), minPrice);
                        }
                    }
                    else
                    {
                        disPrice = minPrice;
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
                        BigDecimal pv = distributionGoodsModel.getPv();
                        //获取分润基值
                        BigDecimal profit = publiceDistributionService.getProfit(vo.getStoreId(), disPrice, costprice, null, BigDecimal.ONE, pv);
                        logger.error("profit:" + profit);
                        //百分比
                        logger.error("directM:" + directM);
                        fxPrice = profit.multiply(directM.multiply(new BigDecimal("0.01")));
                    }
                    if (BigDecimal.ZERO.compareTo(fxPrice) >= 0)
                    {
                        fxPrice = BigDecimal.ZERO;
                    }
                    goodsMap.put("directM", fxPrice.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    goodsMap.put("distributionPrice", directM);
                    /*otherAttrIds.add(distributionGoodsModel.getS_id());
                    ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(distributionGoodsModel.getS_id());
                    if (confiGureModel == null) {
                        throw new LaiKeAPIException(com.laiketui.core.lktconst.ErrorCode.BizErrorCode.ERROR_CODE_SPGGSJCW, "商品规格数据错误");
                    }
                    vipPrice = confiGureModel.getPrice();
                    //获取分销折扣
                    if (user != null) {
                        vipPrice = publiceDistributionService.getGoodsPrice(vo.getStoreId(), user.getUser_id(), vipPrice);
                    }
                    goodsMap.put("vip_yprice", price = vipPrice);*/
                    //优化(31065):同一商品的不同规格需要在一个商品中进行展示。 获取所有规格（规格多组合的情况下，少一种组合前端报错？？？） 2022-09-07 17:51:49
                    List<Integer> attrIdList = distributionGoodsModelMapper.selectDistributionGoodsAtrrIds(distributionGoodsModel.getP_id());
                    for (Integer attrId : attrIdList)
                    {
                        otherAttrIds.add(attrId);
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(attrId);
                        if (confiGureModel == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPGGSJCW, "商品规格数据错误");
                        }
                        vipPrice = confiGureModel.getPrice();
                        //获取分销折扣
                        if (user != null)
                        {
                            vipPrice = publiceDistributionService.getGoodsPrice(vo.getStoreId(), user.getUser_id(), vipPrice);
                        }
                        priceReviseMap.put(attrId, vipPrice);
                        if (attrId.equals(distributionGoodsModel.getS_id()))
                        {
                            goodsMap.put("vip_yprice", price = vipPrice);
                        }
                    }
                    //end
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(vo.getType()))
                {
                    //积分商品详情不显示会员价
                    gradeRate = new BigDecimal("1");
                    IntegralGoodsModel integralGoodsOld = integralGoodsModelMapper.selectByPrimaryKey(vo.getId());
                    if (integralGoodsOld == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JFSPBCZ, "积分商品不存在");
                    }
                    //商品售价价格
                    Map<String, Object> goodsInfo  = DataUtils.cast(SerializePhpUtils.getUnserializeObj(productListModel.getInitial(), Map.class));
                    BigDecimal          goodsPrice = new BigDecimal(Objects.requireNonNull(MapUtils.getString(goodsInfo, "sj")));
                    //商品原价
                    goodsMap.put("yprice", goodsPrice);
                    //积分商品价格
                    price = integralGoodsOld.getMoney();
                    goodsMap.put("price", price);
                    goodsMap.put("integralNum", integralGoodsOld.getIntegral());
                    //获取积分商品规格信息--已上架商品
                    integralGoodsOld = new IntegralGoodsModel();
                    integralGoodsOld.setStore_id(vo.getStoreId());
                    integralGoodsOld.setGoods_id(vo.getPro_id());
                    integralGoodsOld.setIs_delete(0);
                    integralGoodsOld.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING);
                    List<IntegralGoodsModel> allAttrIdGoods = integralGoodsModelMapper.select(integralGoodsOld);

                    if (allAttrIdGoods == null || allAttrIdGoods.size() <= 0)
                    {
                        //该商品无已上架规格--商品状态已下架
                        status = DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString();
                    }
                    else
                    {
                        //商品上架
                        status = DictionaryConst.GoodsStatus.NEW_GROUNDING.toString();
                        int num    = 0;
                        int maxNum = 0;
                        //修正库存数量
                        otherStockNumMap = new HashMap<>(16);
                        //修正价格
                        priceReviseMap = new HashMap<>();
                        //不同规格兑换所需积分
                        integralMap = new HashMap<>();
                        for (IntegralGoodsModel integralGoodsModel : allAttrIdGoods)
                        {
                            otherStockNumMap.put(integralGoodsModel.getAttr_id(), integralGoodsModel.getNum());
                            num += integralGoodsModel.getNum();
                            maxNum += integralGoodsModel.getMax_num();
                            priceReviseMap.put(integralGoodsModel.getAttr_id(), integralGoodsModel.getMoney());
                            integralMap.put(integralGoodsModel.getAttr_id(), integralGoodsModel.getIntegral());

                            //如果商品非所有规格已下架，将已下架的规格排除不显示
                            otherAttrIds.add(integralGoodsModel.getAttr_id());

                        }
                        //库存
                        goodsMap.put("num", num);
                        goodsMap.put("max_num", maxNum);
                    }
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_FS.equals(vo.getType()))
                {
                    FlashsaleActivityModel flashsaleActivityModel = flashsaleActivityModelMapper.selectByPrimaryKey(vo.getId());
                    if (flashsaleActivityModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
                    }
                    //获取默认规格
                    BigDecimal productDefaultPrice = confiGureModelMapper.getProductDefaultPrice(vo.getPro_id());
                    //截止时间秒级时间戳
                    goodsMap.put("remainingTime", flashsaleActivityModel.getEndtime().getTime());
                    //出售价格
                    discount = flashsaleActivityModel.getDiscount().divide(new BigDecimal(10));
                    goodsMap.put("price", productDefaultPrice
                            .multiply(discount)
                            .setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    //原来价格
                    goodsMap.put("yprice", productDefaultPrice.toString());
                    Integer buylimit = flashsaleActivityModel.getBuylimit();
                    //查询用户已购买数量
                    if (user != null)
                    {
                        Integer userPayNum = flashsaleRecordModelMapper.getUserPayNum(vo.getStoreId(), user.getUser_id(), vo.getId());
                        if (userPayNum < buylimit)
                        {
                            buylimit = buylimit - userPayNum;
                        }
                        else
                        {
                            buylimit = 0;
                        }
                    }
                    //限购数量
                    goodsMap.put("buyNum", buylimit);
                }
                //pro节点
                goodsMap.put("pro_id", goodsId);
                goodsMap.put("name", productTitle);
                goodsMap.put("grade_rate", gradeRate);
                goodsMap.put("vip_price", vipPrice);
                goodsMap.put("unit", unit);
                goodsMap.put("photo_x", imgUrl);
                goodsMap.put("content", content);
                goodsMap.put("cat_name", pname);
                goodsMap.put("img_arr", imgList);
                goodsMap.put("status", status);
                goodsMap.put("user_id", userid);
                goodsMap.put("freight", yunfei.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
                goodsMap.put("freight_name", yunfei.compareTo(new BigDecimal("0")) == 0 ? "免运费" : yunfei.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
                goodsMap.put("canbuy", canbuy);
                goodsMap.put("brand_name", brandName);
                goodsMap.put("volume", volume + realVolume);
                goodsMap.put("yunfei", yunfei.setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
                goodsMap.put("coverImage", publiceService.getImgPath(productListModel.getCover_map(), vo.getStoreId()));
                goodsMap.put("video", publiceService.getImgPath(video, vo.getStoreId()));
                goodsMap.put("proVideo", publiceService.getImgPath(proVideo, vo.getStoreId()));
                //获取评论 首页只获取最新的一条
                parmaMap.clear();
                Page pageModel = Page.newBuilder(0, 1, null);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("pid", goodsId);
                parmaMap.put("pageNo", pageModel.getPageNo());
                parmaMap.put("pageSize", pageModel.getPageSize());
                commentsList = publicService.getGoodsCommentList(parmaMap);

                CommentsModel commentsModel = new CommentsModel();
                commentsModel.setPid(goodsId + "");
                commentsTotal = commentsModelMapper.selectCount(commentsModel);


                //获取弹出商品规格插件信息
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setPid(productListModel.getId());
                confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                //商品总库存
                int attrStockTotalNum = 0;
                //处理图片地址、库存
                List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
                for (int i = 0; i < confiGureModelList.size(); i++)
                {
                    ConfiGureModel confiGure = confiGureModelList.get(i);
                    attrStockTotalNum += confiGure.getNum();
                    //是否只显示选中的规格
                    if (otherAttrIds.size() > 0 && !otherAttrIds.contains(confiGure.getId()))
                    {
                        confiGureModelList.remove(confiGure);
                        i--;
                        continue;
                    }
                    String goodsConfigureUrl = confiGure.getImg();
                    goodsConfigureUrl = publicService.getImgPath(goodsConfigureUrl, productListModel.getStore_id());
                    confiGure.setImg(goodsConfigureUrl);
                    //是否需要修正库存数量
                    if (otherStockNumMap != null)
                    {
                        Integer currentStockNum = otherStockNumMap.get(confiGure.getId());
                        if (currentStockNum == null)
                        {
                            currentStockNum = 0;
                        }
                        confiGure.setNum(currentStockNum);
                    }
                    if (priceReviseMap.size() < 1)
                    {
                        //修正价格
                        if (price != null)
                        {
                            if (rePriceScale != null)
                            {
                                //百分比重新计算价格
                                confiGure.setPrice(confiGure.getPrice().multiply(rePriceScale).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));
                                //禅道 56632 如果金额低于0.01 设置为0.01
                                if (confiGure.getPrice().compareTo(new BigDecimal("0.01")) < 0)
                                {
                                    confiGure.setPrice(new BigDecimal("0.01"));
                                }
                            }
                            else
                            {
                                //固定值
                                confiGure.setPrice(price);
                            }
                        }
                    }
                    else
                    {
                        if (priceReviseMap.get(confiGure.getId()) != null)
                        {
                            confiGure.setPrice(priceReviseMap.get(confiGure.getId()));
                        }

                    }
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(vo.getType()) && integralMap != null)
                    {
                        if (integralMap.get(confiGure.getId()) != null)
                        {
                            confiGure.setIntegralPriceNum(integralMap.get(confiGure.getId()));
                        }
                        else
                        {
                            confiGure.setIntegralPriceNum(0);
                        }
                    }
                    //限时折扣价格修改
                    if (discount != null)
                    {
                        confiGure.setPrice(confiGure.getPrice().multiply(discount).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                }
                if (otherStockNumMap == null && !DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(vo.getType()))
                {
                    //普通商品
                    goodsMap.put("num", attrStockTotalNum);
                    logger.debug("当前商品总库存{},当前规格库存{}", productListModel.getNum(), attrStockTotalNum);
                    if (productListModel.getNum() != attrStockTotalNum)
                    {
                        //修正库存
                        ProductListModel productListUpdate = new ProductListModel();
                        productListUpdate.setId(goodsId);
                        productListUpdate.setNum(attrStockTotalNum);
                        productListModelMapper.updateByPrimaryKeySelective(productListUpdate);
                        logger.debug("商品id:{}的总库存已经修正", goodsId);
                    }
                }
                //获取商品规格弹出窗口插件数据
                Map<String, Object> goodsAttributeInfo = GoodsDataUtils.getGoodsAttributeInfo(confiGureModelList, gradeRate);
                resultMap.putAll(goodsAttributeInfo);
                //会员价(会员商品才会享有)
                if (vo.getVipSource() == null)
                {
                    vo.setVipSource(DictionaryConst.WhetherMaven.WHETHER_NO);
                }
                if (vo.getVipSource().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                {
                    MemberProModel memberProModel = new MemberProModel();
                    memberProModel.setPro_id(goodsId);
                    memberProModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                    memberProModel = memberProModelMapper.selectOne(memberProModel);
                    if (!Objects.isNull(memberProModel) && memberProModel.getId() != null)
                    {
                        if (!Objects.isNull(user) && user.getGrade() != null && user.getGrade().equals(User.MEMBER))
                        {
                            Map<String, Object> config = memberConfigMapper.getConfig(vo.getStoreId());
                            if (MapUtils.getInteger(config, "id") != null)
                            {
                                String                    memberDiscount = MapUtils.getString(config, "member_discount");
                                List<Map<String, Object>> mapList        = (List<Map<String, Object>>) resultMap.get("attribute_list");
                                mapList.stream().forEach(map ->
                                {
                                    String     amount   = MapUtils.getString(map, "Price");
                                    BigDecimal multiply = new BigDecimal(amount).multiply(new BigDecimal(memberDiscount));
                                    BigDecimal divide   = multiply.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
                                    map.put("Price", divide);
                                });
                            }
                        }
                    }
                }
                //优惠卷流程
                couponStatus = publicCouponService.index(productListModel.getStore_id());
                //商品价格小数点处理
                String vip_price  = new BigDecimal(MapUtils.getString(goodsMap, "vip_price")).setScale(2, BigDecimal.ROUND_DOWN).toString();
                String vip_yprice = new BigDecimal(MapUtils.getString(goodsMap, "vip_yprice")).setScale(2, BigDecimal.ROUND_DOWN).toString();
                goodsMap.put("vip_price", vip_price);
                goodsMap.put("vip_yprice", vip_yprice);
                resultMap.put("logo", logo);
                resultMap.put("pro", goodsMap);
                resultMap.put("shop_list", shopMap);
                resultMap.put("cs_price", minPrice);
                resultMap.put("collection_id", collectionId);
                resultMap.put("comments", commentsList);
                resultMap.put("commentsTotal", commentsTotal);
                resultMap.put("type", type);
                resultMap.put("access_id", token);
                resultMap.put("login_status", user != null ? 1 : 0);
                resultMap.put("active", active);

                resultMap.put("activity_type_codes", buildMarketingTypeCodes(vo.getStoreId(), goodsId, vo.getLanguage(), mchId));
                resultMap.put("is_grade", 0);
                resultMap.put("coupon_status", couponStatus);
                // 为null前端
                resultMap.put("coupon_str", "");
                //预售商品信息
                PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                preSellGoodsModel.setProduct_id(vo.getPro_id());
                preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                if (!Objects.isNull(preSellGoodsModel))
                {
                    //预售配置信息
                    PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                    preSellConfigModel.setStore_id(vo.getStoreId());
                    preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                    if (Objects.nonNull(preSellConfigModel))
                    {
                        sellGoodMap.put("depositDesc", preSellConfigModel.getDeposit_desc());
                        sellGoodMap.put("balanceDesc", preSellConfigModel.getBalance_desc());
                    }
                    sellGoodMap.put("sellType", preSellGoodsModel.getSell_type());
                    if (preSellGoodsModel.getSell_type().equals(PreSellGoodsModel.DEPOSIT_PATTERN))
                    {
                        sellGoodMap.put("depositType", preSellGoodsModel.getPay_type());
                        sellGoodMap.put("deposit", preSellGoodsModel.getDeposit());
                        sellGoodMap.put("depositStart", DateUtil.dateFormate(preSellGoodsModel.getDeposit_start_time(), GloabConst.TimePattern.YMDHMS));
                        sellGoodMap.put("depositEnd", DateUtil.dateFormate(preSellGoodsModel.getDeposit_end_time(), GloabConst.TimePattern.YMDHMS));
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(preSellGoodsModel.getBalance_pay_time());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Date start = calendar.getTime();
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        calendar.add(Calendar.SECOND, -1);
                        Date end = calendar.getTime();
                        sellGoodMap.put("startTime", DateUtil.dateFormate(start, GloabConst.TimePattern.YMDHMS));
                        sellGoodMap.put("endTime", DateUtil.dateFormate(end, GloabConst.TimePattern.YMDHMS));
                        sellGoodMap.put("balance", minPrice.subtract(preSellGoodsModel.getDeposit()));
                    }
                    else
                    {
                        sellGoodMap.put("sellNum", preSellGoodsModel.getSell_num());
                        sellGoodMap.put("surplusNum", preSellGoodsModel.getSurplus_num());
                        sellGoodMap.put("endTime", DateUtil.dateFormate(preSellGoodsModel.getDeadline(), GloabConst.TimePattern.YMDHMS));
                    }
                    sellGoodMap.put("deliveryTime", preSellGoodsModel.getDelivery_time());
                    resultMap.put("sellGoodInfo", sellGoodMap);
                    //库存处理

                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "index");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("获取商品详情 错误", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品详情异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPXXBWZ, "商品信息不完整", "index");
        }
        return resultMap;
    }

    private List<Map<String, String>> buildMarketingTypeCodes(int storeId, int goodsId, String language, int mchId)
    {
        List<Map<String, String>> result = new ArrayList<>();
        // 只返回一个包含全部活动类型的 map，避免重复
        Map<String, String>    map               = new HashMap<>();
        String                 urlparam          = "";
        DistributionGoodsModel distributionGoods = isDistributionGoods(storeId, goodsId);

        if (distributionGoods != null)
        {
            //pagesC/goods/goodsDetailed?pro_id=1&fx_id=1&toback=true&language=zh_CN
            urlparam = "?isDistribution=true&toback=true&pro_id=" + goodsId + "&fx_id=" + distributionGoods.getId() + "&language=" + language;
            map.put(DictionaryConst.Plugin.DISTRIBUTION, urlparam);
        }

        Integer mshdid = isSecondsGoods(storeId, goodsId);
        if (mshdid != null)
        {
            //pagesB/seckill/seckill_detail?pro_id=1&navType=1&id=148&type=MS&language=zh_CN
            urlparam = "pro_id=" + goodsId + "&navType=1&id=" + mshdid + "&type=MS&language=" + language;
            map.put(DictionaryConst.Plugin.SECONDS, urlparam);
        }
        GroupGoodsModel groupGoods = isGroupGoods(goodsId);
        if (groupGoods != null)
        {
            //pagesA/group/groupDetailed?pro_id=368&goodsId=368&acId=2010519152007053312&language=zh_CN
            urlparam = "pro_id=" + goodsId + "&goodsId=" + goodsId + "&acId=" + groupGoods.getActivity_id() + "&type=MS&language=" + language;
            map.put(DictionaryConst.Plugin.GOGROUP, urlparam);
        }

        if (isPreSellGoods(goodsId))
        {
            //pagesC/preSale/goods/goodsDetailed?toback=true&pro_id=181&language=zh_CN
            urlparam = "toback=true&pro_id=" + goodsId + "&language=" + language;
            map.put(DictionaryConst.Plugin.PRESELL, urlparam);
        }

        Integer zcid = isGrassGoods(storeId, goodsId);
        if (zcid != null && zcid > 0)
        {
            //pagesE/forumPost/postDetail/index?id=291&language=zh_CN
            urlparam = "id=" + zcid + "&language=" + language;
            map.put(DictionaryConst.Plugin.ZC, urlparam);
        }
        AuctionProductModel auctionGoods = isAuctionGoods(goodsId);
        if (auctionGoods != null)
        {
            if (auctionProductModelMapper.getGoodsDetail(auctionGoods.getId()) != null)
            {
                //pagesD/OrderBidding/ProductDetails?specialId=2026575881853468672&acId=157&language=zh_CN
                urlparam = "specialId=" + auctionGoods.getSession_id() + "&acId=" + auctionGoods.getId() + "&language=" + language;
                map.put(DictionaryConst.Plugin.AUCTION, urlparam);
            }
        }
        FlashsaleActivityModel flashsaleGoods = isFlashsaleGoods(storeId, goodsId);
        if (flashsaleGoods != null)
        {
            //pagesC/discount/discount_detail?toback=true&pro_id=145&fsid=53&type=FS&mch_id=25&language=zh_CN
            urlparam = "toback=true&type=FS&pro_id=" + goodsId + "&fsid=" + flashsaleGoods.getId() + "&mch_id=" + mchId + "&language=" + language;
            map.put(DictionaryConst.Plugin.FLASHSALE, urlparam);
        }
        IntegralGoodsModel integralGoods = isIntegralGoods(storeId, goodsId);
        if (integralGoods != null)
        {
            //pagesB/integral/integral_detail?pro_id=62&goodsId=294&integral=900&num=2&language=zh_CN
            urlparam = "goodsId=" + integralGoods.getGoods_id() + "&pro_id=" + integralGoods.getId() + "&integral=" + integralGoods.getIntegral() + "&num=" + integralGoods.getNum() + "&language=" + language;
            map.put(DictionaryConst.Plugin.INTEGRAL, urlparam);
        }
        if (isMemberGoods(storeId, goodsId))
        {
            //pagesC/goods/goodsDetailed?pro_id=346&is_hy=true&language=zh_CN
            //界面需要获取
            // vip_price : 12.60
            // vip_yprice :  14.00
            urlparam = "is_hy=true&pro_id=" + goodsId + "&language=" + language;
            map.put(DictionaryConst.Plugin.MEMBER, urlparam);
        }
        if (!map.isEmpty())
        {
            result.add(map);
        }
        return result;
    }

    /**
     * 获取商品最高优先级的营销活动参数。
     * 规则：按当前store_id的插件排序，从高到低选择商品实际参加的第一个“非排除插件”活动。
     */
    @Override
    public Map<String, String> getTopMarketingParams(int storeId, int goodsId, String language, int mchId)
    {
        List<Map<String, String>> allMarketing = buildMarketingTypeCodes(storeId, goodsId, language, mchId);
        if (CollectionUtils.isEmpty(allMarketing))
        {
            return null;
        }

        Map<String, String> activityMap = allMarketing.get(0);
        if (CollectionUtils.isEmpty(activityMap))
        {
            return null;
        }

        List<PluginsModel> plugins = pluginsModelMapper.selectPluginsByStoreIdOrderBySort(storeId);
        if (CollectionUtils.isEmpty(plugins))
        {
            return null;
        }

        Set<String> excludeTypes = new HashSet<>(Arrays.asList(
                DictionaryConst.Plugin.COUPON,
                DictionaryConst.Plugin.LIVING,
                DictionaryConst.Plugin.MCH,
                DictionaryConst.Plugin.WALLET,
                DictionaryConst.Plugin.SIGN,
                "sigin",
                DictionaryConst.Plugin.DIY
        ));

        PluginsModel topPlugin = null;
        for (PluginsModel plugin : plugins)
        {
            if (plugin == null || StringUtils.isEmpty(plugin.getPlugin_code()))
            {
                continue;
            }

            String pluginCode = plugin.getPlugin_code();
            if (excludeTypes.contains(pluginCode))
            {
                continue;
            }

            topPlugin = plugin;
            break;
        }

        if (topPlugin == null)
        {
            return null;
        }

        String activityCode = normalizeMarketingActivityCode(topPlugin.getPlugin_code());
        String topParam = activityMap.get(activityCode);
        if (StringUtils.isEmpty(topParam))
        {
            return null;
        }

        Map<String, String> result = new HashMap<>(4);
        result.put("type", activityCode);
        result.put("name", topPlugin.getPlugin_name());
        result.put("param", topParam);
        return result;
    }

    private String normalizeMarketingActivityCode(String pluginCode)
    {
        if (StringUtils.isEmpty(pluginCode))
        {
            return pluginCode;
        }
        if ("advertising".equals(pluginCode))
        {
            return DictionaryConst.Plugin.ZC;
        }
        return pluginCode;
    }

    private DistributionGoodsModel isDistributionGoods(int storeId, int goodsId)
    {
        DistributionGoodsModel query = new DistributionGoodsModel();
        query.setStore_id(storeId);
        query.setP_id(goodsId);
        query.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
        List<DistributionGoodsModel> distributionGoodsModelList = distributionGoodsModelMapper.select(query);
        return !CollectionUtils.isEmpty(distributionGoodsModelList) ? distributionGoodsModelList.get(0) : null;
    }

    private Integer isSecondsGoods(int storeId, int goodsId)
    {
        SecondsActivityModel secondsQuery = new SecondsActivityModel();
        secondsQuery.setStore_id(storeId);
        secondsQuery.setGoodsId(goodsId);
        secondsQuery.setIs_delete(DictionaryConst.ProductRecycle.NOT_STATUS);
        secondsQuery.setIsshow(SecondsActivityModel.IS_SHOW_OK);
        List<SecondsActivityModel> secondsList = secondsActivityModelMapper.select(secondsQuery);
        if (!CollectionUtils.isEmpty(secondsList))
        {
            for (SecondsActivityModel model : secondsList)
            {
                if (model.getStatus() == null || model.getStatus() != SecondsActivityModel.SecondsStatus.SECKILL_STATUS_END)
                {
                    return model.getId();
                }
            }
        }
        return null;
    }

    private GroupGoodsModel isGroupGoods(int goodsId)
    {
        GroupGoodsModel selectByGoodsId = groupGoodsModelMapper.selectByGoodsId(goodsId);
        return selectByGoodsId;
    }

    private boolean isPreSellGoods(int goodsId)
    {
        PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
        preSellGoodsModel.setProduct_id(goodsId);
        preSellGoodsModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
        preSellGoodsModel.setIs_display(DictionaryConst.WhetherMaven.WHETHER_OK);
        return !CollectionUtils.isEmpty(preSellGoodsMapper.select(preSellGoodsModel));
    }

    private Integer isGrassGoods(int storeId, int goodsId)
    {
        Integer id = bbsPostModelMapper.countByGoodsId(storeId, goodsId);
        return id == null ? 0 : id;
    }

    private AuctionProductModel isAuctionGoods(int goodsId)
    {
        AuctionProductModel auctionProductModel = new AuctionProductModel();
        auctionProductModel.setGoods_id(goodsId);
        auctionProductModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
        auctionProductModel.setStatus(AuctionProductModel.Status.ONGOING);
        auctionProductModel.setIs_show(DictionaryConst.WhetherMaven.WHETHER_OK);
        List<AuctionProductModel> auctionProductModelList = auctionProductModelMapper.select(auctionProductModel);
        return !CollectionUtils.isEmpty(auctionProductModelList) ? auctionProductModelList.get(0) : null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addBatchComment(MainVo vo,String commentVoList)
    {
        Map<String,Object> res = new HashMap<>();
        List<AddCommentVo> addCommentVoList = JSON.parseArray(commentVoList, AddCommentVo.class);
        for (AddCommentVo addCommentVo : addCommentVoList)
        {
            Map<String, Object> map = saveBathComment(addCommentVo,vo);
            Integer cid = MapUtils.getInteger(map, "cid");
            res.put("cid",cid);
        }
        return res;
    }

    private Map<String,Object> saveBathComment(AddCommentVo vo,MainVo mainVo){
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(mainVo.getAccessId(), redisUtil, true);
            //获取订单明细信息
            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
            orderDetailsModel.setId(vo.getOrder_details_id());
            orderDetailsModel = orderDetailsModelMapper.selectOne(orderDetailsModel);
            if (orderDetailsModel != null)
            {
                boolean isReview = false;
                Integer start = vo.getStart();
                if (start == null || start == 0)
                {
                    isReview = true;
                }
                if (user.getUser_id().equals(orderDetailsModel.getUser_id()))
                {
                    if (StringUtils.isNotEmpty(vo.getComment()))
                    {
                        if (!isReview)
                        {
                            if (vo.getStart() < 1 || vo.getStart() > 5)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PXCGFW, "评星超过范围");
                            }
                        }
                        String orderNo = orderDetailsModel.getR_sNo();
                        //检查是否已经评论过了
                        CommentsModel commentsModel = new CommentsModel();
                        commentsModel.setOid(orderNo);
                        commentsModel.setOrder_detail_id(vo.getOrder_details_id());
                        commentsModel = commentsModelMapper.selectOne(commentsModel);
                        if (!isReview)
                        {
                            if (commentsModel != null)
                            {
                                logger.warn("用户 user = " + user.getUser_id() + " 正在重复评论,订单号=" + orderNo + ";被拦截!");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YPLGL, "已评论过了");
                            }
                        }
                        else
                        {
                            if (commentsModel == null)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXPLHZP, "请先评论后追评");
                            }
                            logger.debug(user.getUser_id() + " 开始追加评论");
                        }
                        logger.debug(user.getUser_id() + "的评论,原内容:" + vo.getComment());
                        //评论脱敏处理
                        vo.setComment(sensitiveWordTool.filterInfo(vo.getComment()));
                        //评论xss防范
                        vo.setComment(XssUtil.stripXSS(vo.getComment()));
                        logger.debug(user.getUser_id() + "的评论,处理后的内容:" + vo.getComment());

                        int sid = Integer.parseInt(orderDetailsModel.getSid());
                        ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(sid);
                        int           count;
                        CommentsModel commentsSave = new CommentsModel();
                        if (isReview)
                        {
                            if (StringUtils.isNotEmpty(commentsModel.getReview()))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YPLGL, "已评论过了");
                            }
                            //追加评论
                            commentsSave.setId(commentsModel.getId());
                            commentsSave.setReview(vo.getComment());
                            commentsSave.setReview_time(new Date());
                            count = commentsModelMapper.updateByPrimaryKeySelective(commentsSave);
                        }
                        else
                        {
                            //添加评论
                            commentsSave.setStore_id(mainVo.getStoreId());
                            commentsSave.setOid(orderNo);
                            commentsSave.setUid(user.getUser_id());
                            commentsSave.setPid(confiGureModel.getPid() + "");
                            commentsSave.setAttribute_id(sid);
                            commentsSave.setContent(vo.getComment());
                            commentsSave.setCommentType(vo.getStart() + "");
                            commentsSave.setAnonymous(vo.getAnonymous() + "");
                            commentsSave.setOrder_detail_id(vo.getOrder_details_id());
                            commentsSave.setAdd_time(new Date());
                            count = commentsModelMapper.insertSelective(commentsSave);
                        }

                        if (StringUtils.isNotEmpty(vo.getImgUrls()))
                        {
                            for (String imgUrl : vo.getImgUrls())
                            {
                                //添加图片
                                CommentsImgModel commentsImgSave = new CommentsImgModel();
                                commentsImgSave.setComments_url(ImgUploadUtils.getUrlImgByName(imgUrl, true));
                                commentsImgSave.setComments_id(commentsSave.getId());
                                commentsImgSave.setAdd_time(new Date());
                                commentsImgSave.setType(isReview ? CommentsImgModel.Type.REVIEW : CommentsImgModel.Type.COMMENT);
                                count = commentsImgModelMapper.insertSelective(commentsImgSave);
                                if (count < 1)
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败");
                                }
                            }
                        }

                        if (count > 0)
                        {
                            if (!isReview)
                            {
                                //增加商品评论的数量
                                count = productListModelMapper.updateAddCommentNum(confiGureModel.getPid());
                            }
                            if (count < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
                            }
                            resultMap.put("cid", commentsSave.getId());
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PLNRBNWK, "评论内容不能为空", "addComment");
                    }
                }
                else
                {
                    logger.warn("用户 user = " + user.getUser_id() + " 正在请求订单id=" + vo.getOrder_details_id() + ";被拦截!");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "addComment");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WZDGDDXX, "未找到该订单信息", "addComment");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品评论异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addComment");
        }
        return resultMap;
    }
    @Override
    public Map<String, Object> getDetailCommList(Integer storeId, String language, String accessId, String orderNo,Integer type,Integer order_details_id)
    {
        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String, Object>> allList = new ArrayList<>();
        List<Integer> idList = new ArrayList<>();
        if (Objects.nonNull(order_details_id))
        {
            idList.add(order_details_id);
        }
        else
        {
            idList = orderDetailsModelMapper.getOrderDetailsIdByNO(orderNo);
        }
        for (Integer id : idList)
        {
            List<Map<String, Object>> commList = getCommList(storeId, accessId, id,type);
            allList.addAll(commList);
        }
        resultMap.put("commentList",allList);
        return resultMap;
    }

    private List<Map<String, Object>> getCommList(Integer storeId, String accessId,Integer orderDetailsId,Integer type){
        List<Map<String, Object>> goodsDetailInfoList = new ArrayList<>();
        try
        {
            if (!StringUtils.isEmpty(accessId))
            {
                Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + accessId);
                if (userObj != null)
                {
                    List<Integer> orderStatusList = new ArrayList<>();
                    orderStatusList.add(DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    Map<String, Object> parmaMap = new HashMap<>(16);
                    parmaMap.put("store_id", storeId);
                    parmaMap.put("oderDetailId", orderDetailsId);
                    parmaMap.put("r_statusList", orderStatusList);
                    //获取订单详情明细信息
                    List<Map<String, Object>> orderDetailByGoodsInfo = orderDetailsModelMapper.getOrderDetailByGoodsInfo(parmaMap);
                    for (Map<String, Object> map : orderDetailByGoodsInfo)
                    {

                        if (Objects.nonNull(type))
                        {
                            String userId = MapUtils.getString(map, "user_id");
                            String orderNo = MapUtils.getString(map, "r_sNo");
                            Integer sid = MapUtils.getInteger(map, "attr_id");
                            Integer status = MapUtils.getInteger(map, "r_status");
                            int orderCommentType = publicOrderService.orderCommentType(storeId, userId, orderNo, orderDetailsId, sid, status);
                            Integer detailReturnIsNotEnd = returnOrderModelMapper.orderDetailReturnIsNotEnd(storeId, orderNo, orderDetailsId);
                            //判断订单是否有售后未结束
                            if (detailReturnIsNotEnd > 0)continue;
                            //显示立即评价订单
                            if (type == 0)
                            {
                                if (orderCommentType != 1) continue;
                                //显示追加评价
                            }else if (type == 1)
                            {
                                if (orderCommentType != 2)continue;
                            }
                        }
                        String productTitle = map.get("product_title") + "";
                        String imgUrl       = map.get("img") + "";
                        imgUrl = publicService.getImgPath(imgUrl, storeId);
                        map.put("product_title", productTitle);
                        map.put("commodityIcon", imgUrl);
                        goodsDetailInfoList.add(map);
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDL, "请登录", "getOrderDetailInfo");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getOrderDetailInfo");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取订单商品数据异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getOrderDetailInfo");
        }
        return goodsDetailInfoList;
    }


    @Autowired
    private ProductModelMapper productModelMapper;

    @Autowired
    private DistributionGoodsModelMapper distributionGoodsModelMapper;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    PublicCouponService publicCouponService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    BrandClassModelMapper brandClassModelMapper;

    @Autowired
    UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    DistributionGradeModelMapper distributionGradeModelMapper;

    @Autowired
    ProductClassModelMapper productClassModelMapper;

    @Autowired
    ProductImgModelMapper productImgModelMapper;

    @Autowired
    MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    MchModelMapper mchModelMapper;

    @Autowired
    UserFootprintModelMapper userFootprintModelMapper;

    @Autowired
    UploadConfigModelMapper uploadConfigModelMapper;

    @Autowired
    UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SensitiveWordTool sensitiveWordTool;

    @Autowired
    PubliceService publicService;

    @Autowired
    ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    ConfigModelMapper configModelMapper;

    @Autowired
    CartModelMapper cartModelMapper;

    @Autowired
    ProductListModelMapper productListModelMapper;

    @Autowired
    CommentsModelMapper commentsModelMapper;

    @Autowired
    CommentsImgModelMapper commentsImgModelMapper;

    @Autowired
    ReplyCommentsModelMapper replyCommentsModelMapper;

    @Autowired
    SecondsProModelMapper secondsProModelMapper;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

    @Autowired
    PtSecondsProModelMapper ptsecondsProModelMapper;

    @Autowired
    private PublicMemberService publicMemberService;

    @Autowired
    private PublicSecondsService publicSecondsService;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private SecondsConfigModelMapper secondsConfigModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private PubliceDistributionService publiceDistributionService;

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private MemberProModelMapper memberProModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private FlashsaleActivityModelMapper flashsaleActivityModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    private FlashsaleActivityModel isFlashsaleGoods(int storeId, int goodsId)
    {
        FlashsaleActivityModel flashsaleQuery = new FlashsaleActivityModel();
        flashsaleQuery.setStoreId(storeId);
        flashsaleQuery.setGoodsId(goodsId);
        flashsaleQuery.setIsDelete(DictionaryConst.WhetherMaven.WHETHER_NO);
        flashsaleQuery.setIsshow(SecondsActivityModel.IS_SHOW_OK);
        List<FlashsaleActivityModel> flashsaleList = flashsaleActivityModelMapper.select(flashsaleQuery);
        return !CollectionUtils.isEmpty(flashsaleList) ? flashsaleList.get(0) : null;
    }

    private IntegralGoodsModel isIntegralGoods(int storeId, int goodsId)
    {
        IntegralGoodsModel integralGoodsModel = new IntegralGoodsModel();
        integralGoodsModel.setStore_id(storeId);
        integralGoodsModel.setGoods_id(goodsId);
        integralGoodsModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
        integralGoodsModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING);
        List<IntegralGoodsModel> integralGoodsModelList = integralGoodsModelMapper.select(integralGoodsModel);
        return !CollectionUtils.isEmpty(integralGoodsModelList) ? integralGoodsModelList.get(0) : null;
    }

    private boolean isMemberGoods(int storeId, int goodsId)
    {
        MemberProModel memberProModel = new MemberProModel();
        memberProModel.setStore_id(storeId);
        memberProModel.setPro_id(goodsId);
        memberProModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
        return !CollectionUtils.isEmpty(memberProModelMapper.select(memberProModel));
    }

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private MchStoreWriteModelMapper mchStoreWriteModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;
}

