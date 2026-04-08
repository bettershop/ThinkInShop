package com.laiketui.apps.app.services;

import com.alibaba.fastjson2.JSON;
import com.laiketui.apps.api.app.services.AppsCstrCodeService;
import com.laiketui.apps.app.common.consts.AppsCstrConst;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.okhttp.HttpUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.Config;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.ImageTool;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.group.GroupProductModel;
import com.laiketui.domain.mch.DistributionGradeModel;
import com.laiketui.domain.mch.MchConfigModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.goods.ShareVo;
import com.laiketui.root.common.BuilderIDTool;
import net.coobird.thumbnailator.Thumbnails;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 二维码处理实现
 *
 * @author Trick
 * @date 2020/12/17 11:23
 */
@Service
@RefreshScope
public class AppsCstrCodeServiceImpl implements AppsCstrCodeService
{

    private final Logger logger = LoggerFactory.getLogger(AppsCstrCodeServiceImpl.class);

    @Value("${client.mpwexin.env_version}")
    private String env_version;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private ConfigModelMapper configModelMapper;
    @Autowired
    private RedisUtil         redisUtil;
    @Autowired
    private UserMapper userMapper;


    @Override
    public String getShareQrcode(MainVo vo, String scene, Integer width, String path, String id) throws LaiKeAPIException
    {
        String imageUrl;
        try
        {
            if (path != null && path.charAt(0) == '/')
            {
                path = path.replaceFirst("/", "");
            }
            if (StringUtils.isEmpty(scene))
            {
                logger.error("getShareQrcode scene参数是必填!!!");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_EWMCSCW, "二维码参数错误");
            }
            //获取微信token
            String token = publiceService.getWeiXinToken(vo.getStoreId());
            logger.error("getShareQrcode-token{}", token);
            //图片名称
            String imageName = "_share_" + id;
            logger.error("getShareQrcode-imageName{}", imageName);
            //把参数存储到redis scene参数必须是32位内的字符!
            String sceneKey = BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 32);
            logger.error("getShareQrcode-sceneKey{}", sceneKey);
            redisUtil.set(GloabConst.RedisHeaderKey.WX_QR_KEY + sceneKey, scene, 259200);
            //装载图片参数
            Map<String, Object> imageParmaMap = new HashMap<>(16);
            imageParmaMap.put("scene", sceneKey);
            //需要发布正式版本的小程序路径
            imageParmaMap.put("page", path);
            imageParmaMap.put("env_version",env_version);
            logger.error("getShareQrcode-page{}", path);
            String postDataJson = JSON.toJSONString(imageParmaMap);
            logger.info("小程序二维码请求参数 {}", postDataJson);
            //请求url
            String apiUrl = String.format(GloabConst.WeiXinUrl.SHARE_B_GRCODE_GET_URL, token);
            logger.info("微信小程序二维码apiUrl {}", apiUrl);
            InputStream stream = HttpUtils.postFile(apiUrl, postDataJson);
            try
            {
                List<MultipartFile> fileList = new ArrayList<>();
                MultipartFile       file     = new MockMultipartFile(imageName, imageName + "." + GloabConst.UploadConfigConst.IMG_PNG, MediaType.IMAGE_PNG_VALUE, stream);
                fileList.add(file);
                logger.info("微信小程序二维码file {}", file.getSize());
                List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, vo.getStoreType(), vo.getStoreId(), true);
                imageUrl = imageUrlList.get(0);
            }
            catch (Exception e)
            {
                logger.error("getShareQrcode-error", e);
                imageUrl = null;
            }
            return imageUrl;
        }
        catch (Exception e)
        {
            logger.error("生成小程序二维码 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getShareQrcode");
        }
    }

    @Override
    public Map<String, Object> shareShop(MainVo vo, int shopId, String imageUrl) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取用户信息
            User                user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> imageInfo = new HashMap<>(16);
            //获取店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(shopId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "shareShop");
            }
            String mchLog = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
            if (StringUtils.isEmpty(mchLog))
            {
                mchLog = ImgUploadUtils.OOS_DEFAULT_IMAGE_NAME_LOGO;
            }
            mchModel.setLogo(mchLog + "?x-oss-process=image/resize,m_fixed,h_100,w_100/format,png");
            //店铺logo 调整大小
            logger.debug("店铺logo：{}", mchModel.getLogo());
            logger.debug("店铺logo：{}", mchLog);
            imageInfo.put("logo", mchModel.getLogo());
            //获取店铺在售数量、已售数量、收藏数量
            Map<String, Object> goodsNumInfo = publiceService.commodityInformation(vo.getStoreId(), shopId,null);
            imageInfo.putAll(goodsNumInfo);
            //获取前端域名
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            String path = "";
            if (configModel != null)
            {
                path = configModel.getH5_domain();
            }
            //url
            String url = path + "/#/pagesA/store/store?is_share=true&shop_id=" + shopId;
            //商城分享二维码
            String appImagImage = "";
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                logger.debug("准备获取小程序二维码");
                //获取小程序二维码
                appImagImage = getShareQrcode(vo, "is_share=true&shop_id=" + shopId, 50, "pagesB/store/store", user.getUser_id());
                logger.debug("小程序二维码下载路径{}", appImagImage);
            }
            else
            {
                //非小程序则系统生成二维码
                ByteArrayOutputStream stream = QRCode.from(url).to(ImageType.PNG).withCharset("utf-8").withSize(280, 280).stream();
                MultipartFile         file   = new MockMultipartFile("logo", shopId + "_appShare" + "." + GloabConst.UploadConfigConst.IMG_PNG, MediaType.IMAGE_PNG_VALUE, stream.toByteArray());
                stream.close();
                List<MultipartFile> fileList = new ArrayList<>();
                fileList.add(file);
                List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, vo.getStoreType(), vo.getStoreId());
                appImagImage = imageUrlList.get(0);
                logger.debug("非小程序则系统生成二维码,路径{}", appImagImage);
            }
            //将活动背景图片设置透明，然后和动态二维码图片合成一张图片
            File huoDongImageFile = new File(ImgUploadUtils.DEFAULT_UPLOAD_PATH + "shop_bg" + "." + GloabConst.UploadConfigConst.IMG_PNG.toLowerCase());
            if (!huoDongImageFile.exists())
            {
                logger.debug("底图片不存在! 路径:{}", huoDongImageFile.getPath());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常");
            }

            //下载图片到本地
            String              apiUrl   = String.valueOf(imageInfo.get("logo"));
            List<MultipartFile> fileList = new ArrayList<>();
            MultipartFile       file;
            try (InputStream stream = HttpUtils.getFile(apiUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN))
            {
                file = new MockMultipartFile("logo", shopId + "_logo" + "." + GloabConst.UploadConfigConst.IMG_JPEG, MediaType.IMAGE_JPEG_VALUE, stream);
            }
            fileList.add(file);
            List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, vo.getStoreType(), vo.getStoreId());
            String       logoImageUrl = imageUrlList.get(0);
            if (logoImageUrl == null)
            {
                logger.info("店铺logo下载失败!");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "shareShop");
            }

            //文字1
            Color  color1  = ImageTool.hexToRgb("#FEFEFE");
            String mchName = mchModel.getName();
            //文字2
            Color  color2       = ImageTool.hexToRgb("#FFFFFF");
            String mchOnSaleNum = String.valueOf(imageInfo.get("quantity_on_sale"));
            String mchSaleNum   = String.valueOf(imageInfo.get("quantity_sold"));
            String mchFollow    = String.valueOf(imageInfo.get("collection_num"));

            //文字3
            Color  color3 = ImageTool.hexToRgb("#AAAAAA");
            String text1  = "在售商品";
            String text2  = "已售";
            String text3  = "关注人数";

            //主图
            int           imgBackGroundX = 550;
            int           imgBackGroundY = 978;
            BufferedImage huoDongImage   = Thumbnails.of(huoDongImageFile).size(imgBackGroundX, imgBackGroundY).asBufferedImage();
            //logo图
            int           logoBackGroundX = 70;
            int           logoBackGroundY = 70;
            BufferedImage logoImage       = Thumbnails.of(logoImageUrl).size(logoBackGroundX, logoBackGroundY).asBufferedImage();
            //小程序码/店铺二维码
            BufferedImage rqCodeImage;
            try
            {
                rqCodeImage = Thumbnails.of(appImagImage).size(340, 340).asBufferedImage();
            }
            catch (LaiKeAPIException e)
            {
                //如果是小程序则检查token是否过期
                logger.error("小程序码/店铺logo图片获取失败 ", e);
                throw e;
            }

            //两条线条的x坐标,用于居中计算
            int x1 = 183, x2 = 368;
            //画两条分割线
            Graphics2D pen = (Graphics2D) huoDongImage.getGraphics();
            // 设置笔的颜色,即背景色
            pen.setColor(ImageTool.hexToRgb("#323232"));
            //定位点与点之间的位置
            pen.fillRect(x1, 243, 2, 60);
            pen.fillRect(x2, 243, 2, 60);
            //设置线的宽度
            pen.setStroke(new BasicStroke(2));
            //点动成线
            pen.drawLine(x1, 243, x1, 243);
            pen.drawLine(x2, 243, 368, 243);

            Font font1 = new Font("微软雅黑", Font.PLAIN, 26);
            Font font2 = new Font("微软雅黑", Font.PLAIN, 20);
            //店铺名称
            int fontLen = (imgBackGroundX - ImageTool.getWordWidth(font1, mchName)) / 2;
            ImageTool.imageWatermark(huoDongImage, mchName, fontLen, 170, color1, font1);

            //店铺在售商品数量
            int t1_num_x = (x1 - ImageTool.getWordWidth(font2, mchOnSaleNum)) / 2;
            ImageTool.imageWatermark(huoDongImage, mchOnSaleNum, t1_num_x - mchOnSaleNum.length(), 260, color2, font1);
            //店铺已售商品数量
            int t2_num_x = x1 + (x2 - x1 - ImageTool.getWordWidth(font2, mchSaleNum)) / 2;
            ImageTool.imageWatermark(huoDongImage, mchSaleNum, t2_num_x - mchSaleNum.length(), 260, color2, font1);
            //店铺关注人数数量
            int t3_num_x = imgBackGroundX - x1 + (x2 - x1 - ImageTool.getWordWidth(font2, mchFollow)) / 2;
            ImageTool.imageWatermark(huoDongImage, mchFollow, t3_num_x - mchFollow.length(), 260, color2, font1);

            //店铺在售商品文字 居中
            int t1_x = (x1 - ImageTool.getWordWidth(font2, text1)) / 2;
            ImageTool.imageWatermark(huoDongImage, text1, t1_x, 300, color3, font2);
            //店铺已售商品文字
            int t2_x = x1 + (x2 - x1 - ImageTool.getWordWidth(font2, text2)) / 2;
            ImageTool.imageWatermark(huoDongImage, text2, t2_x, 300, color3, font2);
            //店铺关注人数文字
            int t3_x = imgBackGroundX - x1 + (x2 - x1 - ImageTool.getWordWidth(font2, text3)) / 2;
            ImageTool.imageWatermark(huoDongImage, text3, t3_x, 300, color3, font2);

            String shareCodeName = BuilderIDTool.getGuid() + "_share.png";
            String uploadPath    = File.separator + "share" + File.separator + "mch" + File.separator;

            String         mchShareQrcodePath = config.getUploadPath() + uploadPath;
            ImgUploadUtils imgUploadUtils     = new ImgUploadUtils();
            imgUploadUtils.isDirectory(mchShareQrcodePath);
            Thumbnails
                    //打开一张图片作为底图
                    .of(huoDongImage)
                    //水印在底图上面的位置 第三个参数是图片的可视度百分比 居中
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(i / 2 - i2 / 2, 49), logoImage, 1f)
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(i / 2 - i2 / 2, 419), rqCodeImage, 1f)
                    //输出图片的压缩百分比
                    .outputQuality(0.8f)
                    //输出图片的大小
                    .size(imgBackGroundX, imgBackGroundY)
                    //输出文件的位置和名称
                    .toFile(mchShareQrcodePath + shareCodeName);
            ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, mchShareQrcodePath + shareCodeName);
            imageUrl = uploadPath;
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                if (configModel != null)
                {
                    imageUrl = configModel.getH5_domain() + uploadPath;
                }
            }
            resultMap.put("imgUrl", imageUrl + shareCodeName);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("店铺分享 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "shareShop");
        }
        return resultMap;
    }

    @Autowired
    private PublicMchService publicMchService;

    @Override
    public Map<String, Object> goodsShare(ShareVo vo, String url) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            //如果是积分商品 则根据积分id获取商品id
            if (vo.getShareType() == 2)
            {
                parmaMap.put("integralId", vo.getProId());
                List<Map<String, Object>> resultList = integralGoodsModelMapper.getGoodsInfo(parmaMap);
                vo.setProId(Integer.parseInt(resultList.get(0).get("goodsId").toString()));
            }
            //获取商品库信息
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pid", vo.getProId());
            parmaMap.put("attrPrice_sort", DataUtils.Sort.ASC.toString());
            List<Map<String, Object>> goodsInfoList = productListModelMapper.getProductListJoinConfigureDynamic(parmaMap);
            if (goodsInfoList == null || goodsInfoList.size() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "goodsShare");
            }
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWPZ, "商城未配置", "goodsShare");
            }
            //店铺头像
            String mchLogUrl = "";
            //获取店铺信息
            Integer mchId = MapUtils.getInteger(goodsInfoList.get(0), "mch_id");
            if (mchId != null)
            {
                MchModel mchModelOld = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModelOld != null)
                {
                    mchLogUrl = mchModelOld.getLogo();
                }
                if (StringUtils.isEmpty(mchLogUrl))
                {
                    MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), mchId);
                    if (mchConfigModel != null)
                    {
                        mchLogUrl = ImgUploadUtils.getUrlPure(publiceService.getImgPath(mchConfigModel.getLogo(), vo.getStoreId()), true);
                    }
                }
            }


            //生成二维码
            String qrCodeUrl = configModel.getH5_domain() + vo.getPath();

            //生成二维码
            String appImagImage = "";
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                //获取小程序二维码
                String pagePath  = vo.getPath().substring(0, vo.getPath().indexOf("?"));
                String parameter = vo.getPath().substring(vo.getPath().indexOf("?") + 1);
                appImagImage = getShareQrcode(vo, parameter, 50, pagePath, user.getUser_id());
                logger.debug("小程序二维码下载路径{}", appImagImage);
            }
            else
            {
                //非小程序则系统生成二维码
                ByteArrayOutputStream stream = QRCode.from(qrCodeUrl).to(ImageType.PNG).withCharset("utf-8").withSize(218, 218).stream();
                MultipartFile         file   = new MockMultipartFile("logo", configModel.getId() + "_appShare" + "." + GloabConst.UploadConfigConst.IMG_PNG, MediaType.IMAGE_PNG_VALUE, stream.toByteArray());
                stream.close();
                List<MultipartFile> fileList = new ArrayList<>();
                fileList.add(file);
                List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, vo.getStoreType(), vo.getStoreId());
                appImagImage = imageUrlList.get(0);
                logger.debug("非小程序则系统生成二维码,路径{}", appImagImage);
            }

            //当前商品规格最低价格
            BigDecimal attributeMinPrice = new BigDecimal(String.valueOf(goodsInfoList.get(0).get("price")));
            int        isDistribution    = Integer.parseInt(String.valueOf(goodsInfoList.get(0).get("is_distribution")));
            //商品类型 1正常商品 2拼团商品 3砍价商品 4竞拍商品
            int goodsType = Integer.parseInt(String.valueOf(goodsInfoList.get(0).get("active")));
            //拼团商品
            if (DictionaryConst.GoodsActive.GOODSACTIVE_SUPPORT_PT == goodsType)
            {
                //拼团等级参数
                String levelStr = "";
                //最小拼团人数
                int minBili = 100;
                //如果是拼团,查询出参团的最低价格
                if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equals(vo.getType().toUpperCase()))
                {
                    GroupProductModel groupProductModel = new GroupProductModel();
                    groupProductModel.setProduct_id(vo.getProId());
                    groupProductModel.setStore_id(vo.getStoreId());
                    groupProductModel.setActivity_no(vo.getActivityNo());
                    groupProductModel = groupProductModelMapper.selectOne(groupProductModel);
                    levelStr = groupProductModel.getGroup_level();
                }
                else
                {
                    Map<String, Object> map = groupProductModelMapper.getGroupProductGroupLevel(vo.getStoreId(), vo.getProId());
                    levelStr = map.get("group_level").toString();
                }
                Map<String, String> dataMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(levelStr, Map.class));
                if (dataMap == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常");
                }
                //计算拼团的最低价格 and 对应的参团人数
                if (DictionaryConst.OrdersType.ORDERS_HEADER_PT.equals(vo.getType().toUpperCase()))
                {
                    for (String key : dataMap.keySet())
                    {
                        String[] bili = dataMap.get(key).split("~");
                        if (minBili > Integer.parseInt(bili[0]))
                        {
                            minBili = Integer.parseInt(bili[0]);
                        }
                    }
                }
                else
                {
                    Map<String, Object> map = groupOpenModelMapper.getGroupOpen(vo.getStoreId(), vo.getOrderno());
                    if (map != null && map.containsKey("groupman"))
                    {
                        String   groupman = map.get("groupman").toString();
                        String[] bili     = dataMap.get(groupman).split("~");
                        minBili = Integer.parseInt(bili[0]);
                    }
                }
                //计算价格
                attributeMinPrice = attributeMinPrice.multiply(BigDecimal.valueOf(minBili)).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
            }

            //店铺logo
//            String logoImgUrl = publiceService.getImgPath(mchLogUrl, vo.getStoreId()) + "?x-oss-process=image/resize,m_fixed,h_55,w_55";
            //获取用户头像
            String userHeaderUrl = ImgUploadUtils.getUrlPure(user.getHeadimgurl(), true) + "?x-oss-process=image/resize,m_fixed,h_154,w_154/circle,r_100/format,png";
            //来客推小logo

            //合并图片 logo
            /*int imgLogoGroundX = 55;
            int imgLogoGroundY = 55;
            BufferedImage logoImage;
            try {
                logoImage = Thumbnails.of(HttpUtils.getFile(logoImgUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgLogoGroundX, imgLogoGroundY).asBufferedImage();
            } catch (Exception e) {
                logoImage = Thumbnails.of(HttpUtils.getFile(ImgUploadUtils.OOS_DEFAULT_IMAGE_NAME_LOGO, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgLogoGroundX, imgLogoGroundY).asBufferedImage();
            }*/

            //合并图片 用户头像
            int           imgUserGroundX = 100;
            int           imgUserGroundY = 100;
            BufferedImage userImage      = Thumbnails.of(HttpUtils.getFile(userHeaderUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgUserGroundX, imgUserGroundY).asBufferedImage();
            //合并图片 商城app二维码
            int           imgQrcodeGroundX = 218;
            int           imgQrcodeGroundY = 218;
            BufferedImage qrcodeImage;
            qrcodeImage = Thumbnails.of(appImagImage).size(imgQrcodeGroundX, imgQrcodeGroundY).asBufferedImage();
            //合并图片 商品图片
            int imgGoodsGroundX = 750;
            int imgGoodsGroundY = 749;
            //获取商品图片
            String goodsImageUrl = String.valueOf(goodsInfoList.get(0).get("imgurl"));
            goodsImageUrl = ImgUploadUtils.getUrlPure(publiceService.getImgPath(goodsImageUrl, vo.getStoreId()), true) + String.format("?x-oss-process=image/resize,m_fixed,h_%s,w_%s", imgGoodsGroundY, imgGoodsGroundX);
            BufferedImage goodsImage = Thumbnails.of(HttpUtils.getFile(goodsImageUrl, GloabConst.LktConfig.AUTHORIZE_DOMAIN)).size(imgGoodsGroundX, imgGoodsGroundY).asBufferedImage();


            //合并图片 生成底图
            int           imgBackGroundX = 700;
            int           imgBackGroundY = 1200;
            BufferedImage mainImage      = ImageTool.builderBaseMap(imgBackGroundX, imgBackGroundY, Color.WHITE);

            //字体高度偏移差
            int heightDeviation = 11;
            //文字1
            Font   unameFont = new Font("微软雅黑", Font.PLAIN, 33);
            String uname     = user.getUser_name() + "|推荐";
            //文字2
            String title     = goodsInfoList.get(0).get("product_title") + "";
            Font   titleFont = new Font("微软雅黑", Font.PLAIN, 35);
            //文字3
            String price     = "￥" + attributeMinPrice.toString();
            Font   priceFont = new Font("微软雅黑", Font.BOLD, 35);
            //文字4
            String text4     = "扫我查看购买";
            Font   text4Font = new Font("微软雅黑", Font.PLAIN, 33);
            //文字5
            String text5     = "值得购买的好物，推荐给你";
            Font   text5Font = new Font("微软雅黑", Font.PLAIN, 24);

            //用户名称
            ImageTool.imageWatermark(mainImage, uname, 154, 40 + ImageTool.getWordHeight(unameFont) - heightDeviation, ImageTool.hexToRgb("#020202"), unameFont);
            //tag文字
            ImageTool.imageWatermark(mainImage, text5, 154, 90 + ImageTool.getWordHeight(text5Font) - heightDeviation, ImageTool.hexToRgb("#B8B8B8"), text5Font);
            //价格
            ImageTool.imageWatermark(mainImage, price, 38, 920 + ImageTool.getWordHeight(priceFont) - heightDeviation, ImageTool.hexToRgb("#FF0000"), priceFont);
            //底部商品标题 格式化商品标题
            List<String> titleList = ImageTool.getWord(titleFont, title, 398);
            //二维码diy文字
            ImageTool.imageWatermark(mainImage, text4, 465, 1110 + ImageTool.getWordHeight(text4Font) - heightDeviation, ImageTool.hexToRgb("#999999"), text4Font);
            //行间距
            int lineNum = ImageTool.getWordHeight(titleFont);
            for (int i = 1; i <= titleList.size(); i++)
            {
                String titleTemp = titleList.get(i - 1);
                ImageTool.imageWatermark(mainImage, titleTemp, 39, 960 + lineNum * i, ImageTool.hexToRgb("#020202"), titleFont);
            }

            String shareCodeName = BuilderIDTool.getGuid() + "_share.png";
            //外网地址
            url += File.separator + shareCodeName;
            String goodsShareQrcodePath = config.getUploadPath() + File.separator + shareCodeName;
            Thumbnails
                    //打开一张图片作为底图
                    .of(mainImage)
                    //水印在底图上面的位置 第三个参数是图片的可视度百分比
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(0, 140), goodsImage, 1f)
                    //店铺logo
//                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(33, 31), logoImage, 1f)
                    //头像
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(30, 30), userImage, 1f)
                    //二维码
                    .watermark((i, i1, i2, i3, i4, i5, i6, i7) -> new Point(460, 898), qrcodeImage, 1f)
                    //输出图片的压缩百分比
                    .outputQuality(1f)
                    //输出图片的大小
                    .size(imgBackGroundX, imgBackGroundY)
                    //输出文件的位置和名称
                    .toFile(goodsShareQrcodePath);
            logger.debug("商品分享二维码路径：{}", goodsShareQrcodePath);
            ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, goodsShareQrcodePath);
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                url = configModel.getH5_domain() + url;
            }
            resultMap.put("imgUrl", url);
        }
        catch (LaiKeAPIException e)
        {
            logger.error("商品分享 自定义异常:", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("商品分享 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片生成失败", "goodsShare");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> rqCodeInfo(ShareVo vo, String url) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            user = userMapper.selectByPrimaryKey(user.getId());
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWPZ, "商城未配置", "goodsShare");
            }
            String h5_domain = configModel.getH5_domain();
            if (StringUtils.isNotEmpty(vo.getPlugin_url()))
            {
                h5_domain = vo.getPlugin_url();
            }
            String shareCodeName = BuilderIDTool.getGuid() + "_share.png";
            //外网地址
            url += File.separator + shareCodeName;
            //生成二维码，此处拼接前端传来的url
            String qrCodeUrl = h5_domain + vo.getPath();
            //禅道51688,图片路径后面不要#
            StringBuilder sb = new StringBuilder(h5_domain);
            //生成二维码
            String appImagImage = "";
            if (DictionaryConst.StoreSource.LKT_LY_001.equals(String.valueOf(vo.getStoreType())))
            {
                //获取小程序二维码
                String pagePath  = vo.getPath().substring(0, vo.getPath().indexOf("?"));
                String parameter = vo.getPath().substring(vo.getPath().indexOf("?") + 1);
                logger.error("小程序二维码下载路径-pagePath{}", pagePath);
                logger.error("小程序二维码下载路径-parameter{}", parameter);
                appImagImage = getShareQrcode(vo, parameter, 50, pagePath, user.getUser_id());
                logger.error("小程序二维码下载路径-appImagImage{}", appImagImage);
                url = ImgUploadUtils.getPathImgByName(appImagImage);
                logger.error("小程序二维码下载路径-url{}", url);
            }
            else
            {
                //非小程序则系统生成二维码
                ByteArrayOutputStream stream = QRCode.from(qrCodeUrl).to(ImageType.PNG).withCharset("utf-8").withSize(218, 218).stream();

                // 将 ByteArrayOutputStream 转换为 ByteArrayInputStream
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stream.toByteArray());

                // 使用 ImageIO 读取图像到 BufferedImage
                BufferedImage originalImage = ImageIO.read(byteArrayInputStream);

                // 裁剪图像，去掉四边的 margin 像素
                int           croppedWidth  = originalImage.getWidth() - 2 * 5;
                int           croppedHeight = originalImage.getHeight() - 2 * 5;
                BufferedImage croppedImage  = originalImage.getSubimage(5, 5, croppedWidth, croppedHeight);


                // 创建一个ByteArrayOutputStream来捕获图像数据
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                // 使用ImageIO将BufferedImage写入到ByteArrayOutputStream中
                ImageIO.write(croppedImage, "png", baos);

                // 从ByteArrayOutputStream中获取字节数组
                byte[] imageData = baos.toByteArray();

                // 关闭ByteArrayOutputStream（虽然对于ByteArrayOutputStream来说，这一步是可选的）
                baos.close();

                MultipartFile file = new MockMultipartFile("logo", configModel.getId() + "_appShare" + "." + GloabConst.UploadConfigConst.IMG_PNG, MediaType.IMAGE_PNG_VALUE, imageData);
                stream.close();
                List<MultipartFile> fileList = new ArrayList<>();
                fileList.add(file);
                List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, vo.getStoreType(), vo.getStoreId(), true);
                appImagImage = ImgUploadUtils.getUrlPure(imageUrlList.get(0), true);
                logger.debug("非小程序则系统生成二维码,路径{}", appImagImage);
                url = ImgUploadUtils.getPathImgByName(appImagImage);
            }
            if (h5_domain.contains(SplitUtils.JH))
            {
                h5_domain = sb.replace(h5_domain.lastIndexOf("#"), h5_domain.length(), "").toString();
            }
            url = h5_domain + "images/" + url;

            //获取openid
            // 1. 构建Redis键名（与存储时保持一致）
            String redisKey = "user:openid:" + user.getUser_id(); // 使用用户ID作为唯一标识

            // 2. 从Redis获取openId
            Object redisValue = redisUtil.get(redisKey);

            if (redisValue != null)
            {
                // 3. Redis中存在，直接使用
                String openId = redisValue.toString();
                resultMap.put("openId", openId);
            }

            logger.info("生成图片路径地址url：：：：{}",url);
            resultMap.put("qrCode", url);
            resultMap.put("userName", user.getUser_name());
            resultMap.put("doMain", h5_domain);
            resultMap.put("userHeadUrl", user.getHeadimgurl());
            resultMap.put("watermarkName", configModel.getWatermark_name());
            resultMap.put("watermarkUrl", configModel.getWatermark_url());
            resultMap.put("userId", user.getUser_id());
            resultMap.put("mobile", user.getMobile());
            resultMap.put("Headimgurl", user.getHeadimgurl());

            //获取会员分销信息
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            Integer mchIdMain = customerModelMapper.getStoreMchId(vo.getStoreId());
            //是否为总店
            boolean isMain = mchIdMain.equals(user.getMchId());
            if (userDistributionModel != null)
            {
                if (isMain || userDistributionModel.getLevel() > 0)
                {
                    //分销等级信息
                    DistributionGradeModel distributionGradeModel = distributionGradeModelMapper.selectByPrimaryKey(userDistributionModel.getLevel());
                    if (distributionGradeModel != null)
                    {
                        Map<String, Object> setsMap = SerializePhpUtils.getDistributionGradeBySets(distributionGradeModel.getSets());
                        //第一个分销等级id
                        Integer userTop1DistributionId = distributionGradeModelMapper.getUserTop1DistributionId(vo.getStoreId());
                        //等级名称
                        String distributionGradeName = setsMap.get("s_dengjiname").toString();
                        //等级排名
                        int distributionGrade = userTop1DistributionId - distributionGradeModel.getId() + 1;
                        resultMap.put("distributionGradeName", distributionGradeName);
                        resultMap.put("distributionGrade", distributionGrade);
                    }
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("商品分享 自定义异常:", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("商品分享 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片生成失败", "rqCodeInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getCodeParameter(MainVo vo, String key) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String parameter = "";
            if (redisUtil.hasKey(GloabConst.RedisHeaderKey.WX_QR_KEY + key))
            {
                parameter = redisUtil.get(GloabConst.RedisHeaderKey.WX_QR_KEY + key).toString();
            }
            logger.debug("key={} 获取到的小程序参数: {}", key, parameter);
            resultMap.put("parameter", parameter);
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取小程序二维码参数 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCodeParameter");
        }
        return resultMap;
    }

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private GroupProductModelMapper groupProductModelMapper;

    @Autowired
    private GroupOpenModelMapper groupOpenModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private Config config;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private DistributionGradeModelMapper distributionGradeModelMapper;
}

