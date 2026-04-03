package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.common.base.Strings;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicMemberService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.kaptcha.PropertiesConfig;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.MailUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.common.utils.weixin.Jssdk;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.config.Config;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.okhttp.HttpUtils;
import com.laiketui.core.utils.tool.*;
import com.laiketui.domain.CurrencyStoreModel;
import com.laiketui.domain.LangModel;
import com.laiketui.domain.Page;
import com.laiketui.domain.SmsMessageModel;
import com.laiketui.domain.config.*;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.dictionary.CountryModel;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.dictionary.DictionaryNameModel;
import com.laiketui.domain.dictionary.MessageModel;
import com.laiketui.domain.distribution.DistributionConfigModel;
import com.laiketui.domain.distribution.DistributionWithdrawModel;
import com.laiketui.domain.distribution.UserDistributionModel;
import com.laiketui.domain.diy.DiyPageModel;
import com.laiketui.domain.flashsale.FlashsaleConfigModel;
import com.laiketui.domain.group.GroupOrderConfigModel;
import com.laiketui.domain.living.LivingConfigModel;
import com.laiketui.domain.living.LivingProductModel;
import com.laiketui.domain.log.*;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.BuyAgainModal;
import com.laiketui.domain.payment.PaymentConfigModel;
import com.laiketui.domain.payment.PaymentModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.plugin.DiyPageBindModel;
import com.laiketui.domain.plugin.bbs.BbsConfigModel;
import com.laiketui.domain.plugin.member.MemberProModel;
import com.laiketui.domain.plugin.seckill.PtSecondsProModel;
import com.laiketui.domain.presell.PreSellConfigModel;
import com.laiketui.domain.product.CommentsImgModel;
import com.laiketui.domain.product.ProductImgModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.ReplyCommentsModel;
import com.laiketui.domain.seckill.SecondsActivityModel;
import com.laiketui.domain.seckill.SecondsConfigModel;
import com.laiketui.domain.subtraction.SubtractionConfigModal;
import com.laiketui.domain.upload.ImgGroupModel;
import com.laiketui.domain.upload.UploadConfigModel;
import com.laiketui.domain.upload.UploadImagModel;
import com.laiketui.domain.user.FinanceConfigModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserGradeModel;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Withdrawals1Vo;
import com.laiketui.domain.vo.diy.DiyVo;
import com.laiketui.domain.vo.files.DelFilesVo;
import com.laiketui.domain.vo.files.UploadFileVo;
import com.laiketui.domain.vo.order.MemberPriceVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import com.laiketui.domain.vo.systems.SystemInfoVo;
import com.laiketui.root.common.BuilderIDTool;
import io.jsonwebtoken.Claims;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 公共服务
 *
 * @author Trick
 * @date 2020/10/15 17:07
 */
@Service
@RefreshScope
public class PublicServiceImpl implements PubliceService
{

    private final Logger                    logger = LoggerFactory.getLogger(PublicServiceImpl.class);

    @Value("${client.mpwexin.env_version}")
    private String env_version;

    @Autowired
    private       PublicMemberService       publicMemberService;
    @Autowired
    private       CartModelMapper           cartModelMapper;
    @Autowired
    private       SignConfigModelMapper     signConfigModelMapper;
    @Autowired
    private       SignRecordModelMapper     signRecordModelMapper;
    @Autowired
    private       CommentsImgModelMapper    commentsImgModelMapper;
    @Autowired
    private       ReplyCommentsModelMapper  replyCommentsModelMapper;
    @Autowired
    private       AgreementModelMapper      agreementModelMapper;
    @Autowired
    private       CommentsModelMapper       commentsModelMapper;
    @Autowired
    private       UserCollectionModelMapper userCollectionModelMapper;
    @Autowired
    private       FilesRecordModelMapper    filesRecordModelMapper;
    @Autowired
    private       UploadConfigModelMapper   uploadConfigModelMapper;
    @Autowired
    private       ProductConfigModelMapper  productConfigModelMapper;
    @Autowired
    private       ProductListModelMapper    productListModelMapper;
    @Autowired
    private       ConfigModelMapper         configModelMapper;
    @Autowired
    private       UserGradeModelMapper      userGradeModelMapper;
    @Autowired
    private       UserBaseMapper            userBaseMapper;
    @Autowired
    private       MchModelMapper            mchModelMapper;
    @Autowired
    private       ConfiGureModelMapper      confiGureModelMapper;
    @Autowired
    private       RedisUtil                 redisUtil;
    @Autowired
    private       AdminModelMapper          adminModelMapper;
    @Autowired
    private       BuyAgainModalMapper       buyAgainModalMapper;

    @Autowired
    private SensitiveWordsModelMapper sensitiveWordsModelMapper;

    @Autowired
    private SecondsProModelMapper secondsProModelMapper;

    @Autowired
    private PtSecondsProModelMapper ptSecondsProModelMapper;

    @Autowired
    private ImgGroupModelMapper imgGroupModelMapper;

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Autowired
    private Config config;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private IntegralGoodsModelMapper integralGoodsModelMapper;

    @Autowired
    private GroupOrderConfigModelMapper groupOrderConfigModelMapper;

    @Autowired
    private DefaultKaptcha defaultKaptcha;

    @Autowired
    private LivingProductModelMapper livingProductModelMapper;

    @Autowired
    private LivingConfigModelMapper livingConfigModelMapper;

    @Autowired
    private SecondsActivityModelMapper secondsActivityModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private DiyPageBindModelMapper diyPageBindModelMapper;

    @Autowired
    private DiyPageModelMapper diyPageModelMapper;

    @Autowired
    private BbsConfigModelMapper bbsConfigModelMapper;


    private static final String CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


    public Map<String, Object> getCode(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object>   resultMap        = new HashMap<>(16);
        ByteArrayOutputStream jpegOutputStream = null;
        String                imgUrl           = "";
        try
        {
            jpegOutputStream = new ByteArrayOutputStream();
            String createText = "";
            String uuid       = BuilderIDTool.getGuid();
            //禅道47310--验证码样式修改
            com.google.code.kaptcha.util.Config config     = defaultKaptcha.getConfig();
            Properties                          properties = config.getProperties();
            //自定义配置
            config = new com.google.code.kaptcha.util.Config(PropertiesConfig.getPropertiesConfig(properties));
            if (vo.getStoreType() == GloabConst.StoreType.STORE_TYPE_PC_MALL)
            {
                config = new com.google.code.kaptcha.util.Config(PropertiesConfig.getPropertiesConfig02(properties));
            }
            defaultKaptcha.setConfig(config);

            // 生产验证码
            createText = defaultKaptcha.createText().toLowerCase();
            imgUrl = uuid + createText + SplitUtils.XSD + GloabConst.UploadConfigConst.IMG_JPG;
            // 使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = defaultKaptcha.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
            MultipartFile       file  = new MockMultipartFile("file", imgUrl, MediaType.IMAGE_JPEG_VALUE, jpegOutputStream.toByteArray());
            List<MultipartFile> files = new ArrayList<>();
            files.add(file);
            List<String> imagUrls = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO, vo.getStoreType(), vo.getStoreId(), true);
            if (imagUrls == null || imagUrls.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMHQSB, "验证码获取失败");
            }
            logger.info("验证码url:{}", imagUrls.get(0));
            imgUrl = imagUrls.get(0);
            //缓存验证码
            redisUtil.set(uuid + createText, createText, GloabConst.LktConfig.IMG_CODE_EXISTENCE_TIME);
            redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_CODE_KEY + uuid, ImgUploadUtils.getUrlImgByName(imgUrl, true), GloabConst.LktConfig.IMG_CODE_EXISTENCE_TIME);
            resultMap.put("code", uuid);
            resultMap.put("code_img", imgUrl);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMHQSB, "验证码获取失败");
        }
        finally
        {
            if (jpegOutputStream != null)
            {
                try
                {
                    jpegOutputStream.close();
                }
                catch (IOException e)
                {
                    logger.error(e.getMessage());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMHQSB, "验证码获取失败");
                }
            }
        }
        return resultMap;
    }

    @Override
    public String getImgPath(String imgName, String storeId) throws LaiKeAPIException
    {
        String imageUrl = "";
        try
        {
            //获取图片完整路径
            FilesRecordModel filesRecordModel = new FilesRecordModel();
            filesRecordModel.setStore_id(storeId);
            filesRecordModel.setImage_name(imgName);
            filesRecordModel = filesRecordModelMapper.getImageUrlOne(filesRecordModel);
            if (filesRecordModel != null)
            {
                //获取文件上传配置
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(filesRecordModel.getUpload_mode());
                List<UploadConfigModel> uploadConfigModels = uploadConfigModelMapper.select(uploadConfigModel);
                //获取图片完整路径
                imageUrl = ImgUploadUtils.getImgPath(filesRecordModel, uploadConfigModels);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取图片路径失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPLJHQSB, "图片路径获取失败", "getImgPath");
        }

        return imageUrl;
    }

    @Override
    public String getImgPath(String imgName, Integer storeId) throws LaiKeAPIException {
        if (storeId == null || StringUtils.isEmpty(imgName)) {
            return "";
        }

        // 1. 先查 Redis 缓存
        String cacheKey = "img_path:" + storeId + ":" + imgName;
        String cachedUrl = (String) redisUtil.get(cacheKey);
        if (StringUtils.isNotEmpty(cachedUrl)) {
            return cachedUrl;
        }

        // 2. Redis 未命中 → 查库
        FilesRecordModel record = new FilesRecordModel();
        record.setStore_id(storeId.toString());
        record.setImage_name(imgName);
        record = filesRecordModelMapper.getImageUrlOne(record);

        if (record == null) {
            logger.warn("图片记录不存在: storeId={}, imgName={}", storeId, imgName);
            return "";
        }

        // 3. 查询上传配置（这个表数据极少，可缓存）
        UploadConfigModel configQuery = new UploadConfigModel();
        configQuery.setUpserver(record.getUpload_mode());
        List<UploadConfigModel> configs = uploadConfigModelMapper.select(configQuery);

        // 4. 获取完整路径
        String imageUrl = ImgUploadUtils.getImgPath(record, configs);

        // 5. 存入 Redis（缓存 1 小时）
        redisUtil.set(cacheKey, imageUrl, 3600);

        return imageUrl;
    }

    @Override
    public User getRedisUserCache(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User       userCache = null;
            AdminModel adminModel;
            if (StringUtils.isEmpty(vo.getAccessId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请登录");
            }
            String h5Token = GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN;
            //获取当前登录用户数据
            Object cacheValue = redisUtil.get(h5Token + vo.getAccessId());
            if (cacheValue == null)
            {
                h5Token = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN;
                cacheValue = redisUtil.get(h5Token + vo.getAccessId());
            }
            if (cacheValue == null)
            {
                h5Token = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN;
                if (redisUtil.hasKey(h5Token + vo.getAccessId()))
                {
                    //转换登录
                    logger.info("后台登录插件,自动转换token登录");
                    //校验管理后台账号登录凭证
                    adminModel = getRedisAdminUserCache(vo.getAccessId(), redisUtil);
                    //如果是后台则自动登录商城自营店h5账号
                    Integer mchId = adminModel.getShop_id();
                    if (adminModel.getShop_id() == null || adminModel.getShop_id() == 0)
                    {
                        //获取商城自营
                        mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
                    }
                    userCache = userBaseMapper.mchLoginByUser(mchId);
                    if (userCache == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTJZYD, "请添加自营店");
                    }
                    userCache.setAdminId(adminModel.getId());
                    //保存登录信息
                    redisUtil.set(userCache.getAccess_token(), JSON.toJSONString(userCache), GloabConst.LktConfig.LOGIN_EXISTENCE_TIME);
                    redisUtil.set(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG + userCache.getUser_id(), GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + vo.getAccessId(), GloabConst.LktConfig.LOGIN_EXISTENCE_TIME);
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "请重新登录");
                }
            }
            try
            {
                if (userCache == null)
                {
                    userCache = JSON.parseObject(cacheValue.toString(), User.class);
                }
                return userCache;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取已经登陆的用户! 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQYHXXYC, "获取用户信息异常", "getRedisUserCache");
        }
    }

    @Override
    public List<String> uploadFiles(UploadFileVo vo) throws LaiKeAPIException
    {
        List<String> imgUrls = null;
        try {
            //获取默认商城配置
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
            uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
            if (StringUtils.isEmpty(uploadConfigModel)) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
            }
            String attrvalue = uploadConfigModel.getAttrvalue();
            //使用商城默认图片配置替换
            vo.setUploadType(attrvalue);
            UploadImagModel imagModel = new UploadImagModel();
            imagModel.setUploadType(vo.getUploadType());
            if (vo.getGroupId() != null && vo.getGroupId() > 0) {
                ImgGroupModel imgGroupModel = imgGroupModelMapper.selectByPrimaryKey(vo.getGroupId());
                if (imgGroupModel == null) {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPFZBCZ, "图片分组不存在");
                }
                //指定上传路径
                imagModel.setPath(PinyinUtils.getPinYin(imgGroupModel.getName()));
            }
            //从数据库获取上传配置key
            uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(imagModel.getUploadType());
            List<UploadConfigModel> uploadConfigs = uploadConfigModelMapper.select(uploadConfigModel);
            //装载参数
            imagModel.setUploadConfigs(uploadConfigs);
            imagModel.setMultipartFiles(DataUtils.convertToList(vo.getImage()));
            if (Objects.nonNull(vo.getDiyId())) {
                imagModel.setDiyId(vo.getDiyId());
            }
            if (Objects.nonNull(vo.getDiy_img_type())) {
                imagModel.setDiy_img_type(vo.getDiy_img_type());
            }
            //图片上传
            ImgUploadUtils imgUpload = new ImgUploadUtils();
            imgUrls = imgUpload.imgUpload(imagModel, vo.getStoreId(), vo.getStoreType());

            if (imgUrls != null && imgUrls.size() > 0)
            {
                //添加上传记录信息
                for (int i = 0; i < imgUrls.size(); i++)
                {
                    FilesRecordModel filesRecordModel = new FilesRecordModel();
                    if (GloabConst.UploadConfigConst.IMG_UPLOAD_OSS.equals(vo.getUploadType())
                            || GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO.equals(vo.getUploadType()))
                    {
                        filesRecordModel.setImage_name(ImgUploadUtils.getUrlImgByName(imgUrls.get(i), true));
                    }
                    else
                    {
                        filesRecordModel.setImage_name(ImgUploadUtils.getPathImgByName(imgUrls.get(i)));
                    }
                    filesRecordModel.setStore_id(vo.getStoreId() + "");
                    filesRecordModel.setStore_type(vo.getStoreType() + "");
                    filesRecordModel.setGroup(vo.getGroupId() + "");
                    filesRecordModel.setUpload_mode(vo.getUploadType());
                    String urlImgBySuffix = ImgUploadUtils.getUrlImgBySuffix(imgUrls.get(i));
                    if (StringUtils.isEmpty(urlImgBySuffix))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.IMAGE_TYPE_NOT_CHECK, "图片文件类型有误", "uploadImage");
                    }
                    if (urlImgBySuffix.equals(".mp4") || urlImgBySuffix.equals(".avi") || urlImgBySuffix.equals(".webg") || urlImgBySuffix.equals(".mov"))
                    {
                        if (vo.getType() != null && !"2".equals(vo.getType()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.IMAGE_TYPE_NOT_CHECK, "图片文件类型有误", "uploadImage");
                        }
                        // 认为是视频
                        filesRecordModel.setType(2);
                    }
                    else
                    {
                        if (vo.getType() != null && !"1".equals(vo.getType()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.IMAGE_TYPE_NOT_CHECK, "图片文件类型有误", "uploadImage");
                        }
                    }
                    List<MultipartFile> files = DataUtils.convertToList(vo.getImage());
                    if (files != null && StringUtils.isNotEmpty(files.get(i).getOriginalFilename()))
                    {
                        filesRecordModel.setName(files.get(i).getOriginalFilename());
                        i++;
                    }
                    if (StringUtils.isNotEmpty(vo.getTitle()))
                    {
                        filesRecordModel.setTitle(vo.getTitle());
                    }
                    if (StringUtils.isNotEmpty(vo.getExplain()))
                    {
                        filesRecordModel.setTitle(vo.getExplain());
                    }
                    if (StringUtils.isNotEmpty(vo.getAdd_user()))
                    {
                        filesRecordModel.setAdd_user(vo.getAdd_user());
                    }
                    if (StringUtils.isNotEmpty(vo.getAlternativeText()))
                    {
                        filesRecordModel.setTitle(vo.getAlternativeText());
                    }
                    if (StringUtils.isNotEmpty(vo.getDescribe()))
                    {
                        filesRecordModel.setTitle(vo.getDescribe());
                    }
                    if (vo.getMchId() != null)
                    {
                        filesRecordModel.setMch_id(vo.getMchId());
                    }
                    else
                    {
                        //获取商城自营店id 禅道53963
                        filesRecordModel.setMch_id(customerModelMapper.getStoreMchId(vo.getStoreId()));
                    }
                    if (vo.getSupplierId() != null)
                    {
                        filesRecordModel.setSupplier_id(vo.getSupplierId());
                    }

                    if (Objects.nonNull(vo.getImg_type())) {
                        filesRecordModel.setImg_type(vo.getImg_type());
                    }
                    if (Objects.nonNull(vo.getDiy_img_type())) {
                        filesRecordModel.setDiy_img_type(vo.getDiy_img_type());
                    }
                    if (Objects.nonNull(vo.getDiyId())) {
                        filesRecordModel.setDiy_id(vo.getDiyId());
                    }
                    filesRecordModel.setAdd_time(new Date());
                    int count = filesRecordModelMapper.insertSelective(filesRecordModel);
                    if (count < 1)
                    {
                        logger.info("图片上传失败,图片记录失败 参数：" + JSON.toJSONString(filesRecordModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
                    }
                }
                return imgUrls;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "uploadImage");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取图片路径失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "uploadImage");
        }
    }

    @Override
    public List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId, Integer mchId) throws LaiKeAPIException
    {
        return getImagesUrls(multipartFiles, uploadType, storeType, storeId, mchId, false);
    }

    /**
     * 公共上传方法，加了一个参数 iscode 是否为获取验证码图片 true 是验证码 false 不是验证码
     *
     * @param multipartFiles
     * @param uploadType
     * @param storeType
     * @param storeId
     * @param mchId
     * @param iscode
     * @return
     * @throws LaiKeAPIException
     */
    private List<String> getImagesUrls(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId, Integer mchId, boolean iscode) throws LaiKeAPIException
    {
        List<String> imgUrls;
        try
        {
            //文件上传方式特殊处理,上传方式为oos,minIo获取商城上传默认配置(gp 2023-08-04)
            switch (uploadType)
            {
                case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
                case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1:
                case GloabConst.UploadConfigConst.IMG_UPLOAD_TXY:
                case GloabConst.UploadConfigConst.IMG_UPLOAD_QNY:
                    break;
                case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO:
                    //获取商城上传默认配置
                    UploadConfigModel uploadConfigModel = new UploadConfigModel();
                    uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
                    uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
                    if (StringUtils.isEmpty(uploadConfigModel))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
                    }
                    uploadType = uploadConfigModel.getAttrvalue();
                    break;
                default:
                    logger.error("上传类型不存在{}", uploadType);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "imgUpload");
            }
            UploadImagModel imagModel = new UploadImagModel();
            imagModel.setIsCode(iscode);
            imagModel.setUploadType(uploadType);
            //从数据库获取上传配置key
            UploadConfigModel uploadConfigModel = new UploadConfigModel();
            uploadConfigModel.setUpserver(imagModel.getUploadType() + "");
            List<UploadConfigModel> uploadConfigs = uploadConfigModelMapper.select(uploadConfigModel);

            //装载参数
            imagModel.setUploadConfigs(uploadConfigs);
            imagModel.setMultipartFiles(multipartFiles);
            //如果是本地则获取配置信息里的路径
            if (uploadType.equals(GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1))
            {
                imagModel.setUploadPath(config.getUploadPath());
            }
            //图片上传
            ImgUploadUtils imgupload = new ImgUploadUtils();
            imgUrls = imgupload.imgUpload(imagModel, storeId, storeType);


            if (imgUrls != null && imgUrls.size() > 0)
            {
                //非验证码才插入lkt_files_record表 验证码不插入这个表，虚拟商品和自提商品的提货二维码也不插入此表
                if (!iscode)
                {
                    //添加上传记录信息
                    for (String imgUrl : imgUrls)
                    {
                        FilesRecordModel filesRecordModel = new FilesRecordModel();
                        filesRecordModel.setImage_name(ImgUploadUtils.getUrlImgByName(imgUrl, true));
                        filesRecordModel.setStore_id(storeId + "");
                        filesRecordModel.setStore_type(storeType + "");
                        filesRecordModel.setGroup("-1");
                        filesRecordModel.setUpload_mode(uploadType);
                        if (mchId != null)
                        {
                            filesRecordModel.setMch_id(mchId);
                        }
                        filesRecordModel.setAdd_time(new Date());
                        int count = filesRecordModelMapper.insertSelective(filesRecordModel);
                        if (count < 1)
                        {
                            logger.info("图片上传失败,图片记录失败 参数：" + JSON.toJSONString(filesRecordModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadImage");
                        }
                    }
                }
                return imgUrls;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "uploadImage");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传图片异常! ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB, "图片上传失败", "uploadImage");
        }
    }

    @Override
    public List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId) throws LaiKeAPIException
    {
        try
        {
            //获取商城默认文件上传方式
            if (StringUtils.isEmpty(uploadType))
            {
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
                uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
                if (StringUtils.isEmpty(uploadConfigModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
                }
                uploadType = uploadConfigModel.getAttrvalue();
            }
            return uploadImage(multipartFiles, uploadType, storeType, storeId, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取图片路径失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPLJHQSB, "图片路径获取失败", "getImgPath");
        }
    }

    @Override
    public List<String> uploadImage(List<MultipartFile> multipartFiles, String uploadType, int storeType, int storeId, boolean iscode) throws LaiKeAPIException
    {
        try
        {
            //获取商城默认文件上传方式
            if (StringUtils.isEmpty(uploadType))
            {
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_FANGSHI);
                uploadConfigModel = uploadConfigModelMapper.selectOne(uploadConfigModel);
                if (StringUtils.isEmpty(uploadConfigModel))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPSCSB_001, "平台图片配置未配置!", "uploadImage");
                }
                uploadType = uploadConfigModel.getAttrvalue();
            }
            return getImagesUrls(multipartFiles, uploadType, storeType, storeId, null, iscode);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取图片路径失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPLJHQSB, "图片路径获取失败", "getImgPath");
        }
    }

    @Override
    public String uploadImage(String url, String uploadType, int storeType, int storeId, Integer mchId) throws LaiKeAPIException
    {
        InputStream stream   = null;
        String      imageUrl = null;
        try
        {
            //获取图片信息
            File                openFile    = new File(url);
            String              imageSuffix = ImgUploadUtils.getUrlImgBySuffix(openFile.getName());
            List<MultipartFile> fileList    = new ArrayList<>();
            //加载图片
            stream = HttpUtils.getFile(url);
            MultipartFile file = new MockMultipartFile("temp", BuilderIDTool.getGuid() + imageSuffix, ImgUploadUtils.getUrlImgByMimeType(imageSuffix), stream);
            fileList.add(file);
            //上传图片
            List<String> imageUrlList = uploadImage(fileList, uploadType, storeType, storeId, mchId);
            for (String imgUrl : imageUrlList)
            {
                imageUrl = imgUrl;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传外链图片异常! ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPWLSCSB, "图片外链上传失败", "uploadImage");
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException io)
                {
                    io.printStackTrace();
                    logger.error("上传外链图片异常!流关闭异常 " + io.getMessage());
                }
            }
        }
        return imageUrl;
    }

    @Override
    public void addRotationImage(List<String> imageNameList, int pid, boolean isUpdate) throws LaiKeAPIException
    {
        try
        {
            if (!isUpdate)
            {
                //删除之前商品的轮播图
                ProductImgModel productImg = new ProductImgModel();
                productImg.setProduct_id(pid);
                productImgModelMapper.delete(productImg);
            }
            for (String imageName : imageNameList)
            {
                ProductImgModel productImgModel = new ProductImgModel();
                productImgModel.setProduct_url(imageName);
                productImgModel.setProduct_id(pid);
                productImgModel.setAdd_date(new Date());
                int count = productImgModelMapper.insertSelective(productImgModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTTJSB, "轮播图添加失败", "addRotationImage");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品轮播图失败 异常! ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSPLBTSB, "添加商品轮播图失败", "addRotationImage");
        }
    }

    /**
     * 获取后台用户信息
     *
     * @param accessId  -
     * @param redisUtil -
     * @return AdminModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/31 15:30
     */
    private AdminModel getRedisAdminUserCache(String accessId, RedisUtil redisUtil) throws LaiKeAPIException
    {
        //获取当前登录用户数据
        Object     cacheValue = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + accessId);
        AdminModel userCache  = null;
        if (cacheValue != null)
        {
            try
            {
                userCache = JSON.parseObject(cacheValue.toString(), AdminModel.class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试", "getRedisUserCache");
            }
        }
        else
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZXDL, "请重新登录");
        }

        return userCache;
    }

    @Override
    public boolean verifyToken(String token) throws LaiKeAPIException
    {
        boolean flag = false;
        try
        {
            try
            {
                if (StringUtils.isEmpty(token))
                {
                    return true;
                }
                //验证token的合法性
                JwtUtils.verifyJwt(token);
            }
            catch (LaiKeAPIException l)
            {
                //过期或者不存在
                l.printStackTrace();
                flag = true;
            }
        }
        catch (Exception e)
        {
            logger.error("获取Token失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQSB, "获取Token失败", "verifyToken");
        }
        return flag;
    }

    @Override
    public Map<String, Object> verifyToken(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        boolean             flag      = false;
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                String isRegister = configModel.getIs_register();
                //只有app才支持免注册
                if (GloabConst.LktConfig.REGISTER_TYPE2.equals(isRegister) && DictionaryConst.StoreSource.LKT_LY_001.equals(vo.getStoreType() + ""))
                {
                    // 当注册为免注册，并且来源为小程序
                    flag = true;
                }
                else
                {
                    try
                    {
                        //验证token的合法性
                        Claims claims = JwtUtils.verifyJwt(vo.getAccessId());
                        resultMap.put("tokenMap", claims);
                    }
                    catch (LaiKeAPIException l)
                    {
                        //过期或者不存在
                        l.printStackTrace();
                        flag = false;
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("获取Token失败!", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQSB, "获取Token失败", "verifyToken");
        }
        resultMap.put("msg", flag);
        return resultMap;
    }

    @Override
    public BigDecimal getUserGradeRate(int storeId, User user) throws LaiKeAPIException
    {
        BigDecimal gradeRate = BigDecimal.ONE;
        try
        {
            //会员折扣率
            if (user != null && user.getGrade() != null && user.getGrade().doubleValue() != 0)
            {
                //获取会员等级对应的折扣
                UserGradeModel userGradeModel = userGradeModelMapper.getUserGradeInfo(storeId, user.getUser_id(), new Date());
                if (userGradeModel != null && gradeRate.compareTo(userGradeModel.getRate()) != 0 && BigDecimal.ZERO.compareTo(userGradeModel.getRate()) != 0)
                {
                    gradeRate = userGradeModel.getRate().divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                }
            }
            else
            {
                //如果不是会员则获取最低折扣价
                gradeRate = userGradeModelMapper.getGradeLow(storeId).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取折扣失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZKHQSB, "折扣获取失败", "getUserGradeRate");
        }
        return gradeRate;
    }

    @Override
    public BigDecimal getUserGradeRate(int storeId, User user, boolean isLowGrate) throws LaiKeAPIException
    {
        BigDecimal gradeRate = new BigDecimal("1");
        try
        {
            //会员折扣率
            if (user != null)
            {
                gradeRate = getUserGradeRate(storeId, user);
            }
            else if (isLowGrate)
            {
                //获取未注册的折扣率 为了吸引顾客 用最低折扣
                gradeRate = userGradeModelMapper.getGradeLow(storeId).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取折扣失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZKHQSB, "折扣获取失败", "getUserGradeRate_1");
        }
        return gradeRate;
    }

    @Override
    public User getUserInfo(String accessId) throws LaiKeAPIException
    {
        User user = null;
        try
        {
            Object userObj = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN + accessId);
            if (userObj != null)
            {
                user = JSON.parseObject(userObj.toString(), User.class);
                user.setIsVip(GloabConst.UserIdentity.USER_VIP);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WDL, "未登录", "getUserInfo");
            }
            return user;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户信息失败", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQYHXXSB, "获取用户信息失败", "getUserInfo");
        }
    }

    @Override
    public List<Map<String, Object>> getNearbyShops(String tencentKey, int storeId, String latitude, String longitude, Page pageModel) throws LaiKeAPIException
    {
        //优选店铺集
        List<Map<String, Object>> mchStoreList = new ArrayList<>();
        try
        {
            //判断店铺开关
            if (!this.frontPlugin(storeId, DictionaryConst.Plugin.MCH, null))
            {
                return mchStoreList;
            }

            //所有商户下的店铺位置信息集
            List<String> longitudeAndLatitudes = new ArrayList<>();

            //获取所有审核通过的店铺
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setReview_status(DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
            List<MchModel> mchModels = mchModelMapper.select(mchModel);
            for (MchModel mch : mchModels)
            {
                Map<String, Object> mchMap = new HashMap<>(16);
                //获取商户logo图片地址
                String imgLogoUrl = this.getImgPath(mch.getLogo(), mchModel.getStore_id());
                //获取商户所属人信息
                User configUser = new User();
                configUser.setUser_id(mch.getUser_id());
                configUser.setStore_id(mch.getStore_id());
                configUser = userBaseMapper.selectOne(configUser);
                //获取商户下的店铺信息
                if (configUser == null)
                {
                    continue;
                }
                //店铺经纬度处理
                StringBuffer longitudeAndLatitude = new StringBuffer();
                String       configLatitude       = mch.getLatitude();
                String       configLongitude      = mch.getLongitude();
                if (StringUtils.isNotEmpty(configLatitude) && StringUtils.isNotEmpty(configLongitude))
                {
                    longitudeAndLatitude.append(configLatitude).append(",").append(configLongitude);
                    longitudeAndLatitudes.add(longitudeAndLatitude.toString());
                }
                else
                {
                    logger.debug("当前店铺id{} 没有位置,不进行推荐 ", mch.getId());
                    continue;
                }
                //计算离当前店铺距离
//                String to = String.join(";", longitudeAndLatitudes);
//                String addressStoreJson = TengxunMapUtil.getStoreDstance(tencentKey, to, latitude + "," + longitude);
//                Map<String, List<Map<String, String>>> rowsMap = JSON.parseObject(addressStoreJson, new TypeReference<Map<String, List<Map<String, String>>>>() {
//                });
//                if (rowsMap != null) {
//                    for (Map<String, String> elementsMap : rowsMap.get(GloabConst.ManaValue.MANA_VALUE_ROWS)) {
//                        List<Map<String, Object>> distanceList = JSON.parseObject(elementsMap.get("elements"), new TypeReference<List<Map<String, Object>>>() {
//                        });
//
//                        for (Map<String, Object> distanceMap : distanceList) {
//                            //距离 米
//                            BigDecimal distance = new BigDecimal(distanceMap.get("distance") + "");
//                            //换算千米
//                            mchMap.put("distance", distance.divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP));
//                        }
//                    }
//                }
                mchMap.put("LongitudeAndLatitude", longitudeAndLatitude);
                mchMap.put("user_name", configUser.getUser_name());
                mchMap.put("user_id", configUser.getUser_id());
                mchMap.put("logo", imgLogoUrl);
                mchMap.put("headimgurl", configUser.getHeadimgurl());
                mchMap.put("source", configUser.getSource());
                mchMap.put("shop_id", mch.getId());
                mchMap.put("name", mch.getName());
                mchStoreList.add(mchMap);
            }
            //升序
            mchStoreList.sort((o1, o2) ->
            {
                if (o1.containsKey("distance") && o2.containsKey("distance"))
                {
                    Double name1 = Double.parseDouble(o1.get("distance") + "");//name1是从你list里面拿出来的一个
                    Double name2 = Double.parseDouble(o2.get("distance") + ""); //name1是从你list里面拿出来的第二个name
                    return name1.compareTo(name2);
                }
                else
                {
                    return 0;
                }
            });
            List<Map<String, Object>> mchStoreListTemp = new ArrayList<>();
            int                       maxNum           = Math.min(pageModel.getPageSize(), mchStoreList.size());
            for (int i = pageModel.getPageNo(); i < maxNum; i++)
            {
                mchStoreListTemp.add(mchStoreList.get(i));
            }
            mchStoreList = mchStoreListTemp;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取附近店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQFJDPSB, "获取附近店铺失败", "getNearbyShops");
        }

        return mchStoreList;
    }

    /**
     * @param goodsInfoList -[{"pid":"979"},{"cid":"5648"},{"num":1},{"sec_id":"6"}--秒杀id,{}] 商品信息 ， 1.单个商品传入，2.购物车结算传入，3.支付方式 ， 4.商品类型
     * @param cartIds       - 购物车
     * @param buyType       - 购买类型 默认0 再次购买1
     * @param orderHead     - 订单头部
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public List<Map<String, Object>> productsList(List<Map<String, Object>> goodsInfoList, String cartIds, int buyType, String orderHead) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //
        List<Map<String, Object>> ret = new ArrayList<>();
        try
        {
            //当购物车为空时，此时为购买单个商品
            if (StringUtils.isEmpty(cartIds))
            {
                int     pid    = 0;
                int     cid    = 0;
                int     num    = 0;
                int     roomId = 0;
                Integer secId  = null;
                for (Map<String, Object> map : goodsInfoList)
                {
                    if (map.containsKey("pid"))
                    {
                        pid = Integer.parseInt(map.get("pid").toString());
                    }
                    else if (map.containsKey("cid"))
                    {
                        cid = Integer.parseInt(map.get("cid").toString());
                    }
                    else if (map.containsKey("num"))
                    {
                        num = Integer.parseInt(map.get("num").toString());
                    }
                    else if (map.containsKey("sec_id"))
                    {
                        secId = Integer.parseInt(map.get("sec_id").toString());
                        resultMap.put("sec_id", secId);
                        orderHead = DictionaryConst.OrdersType.ORDERS_HEADER_MS;
                    }
                    else if (map.containsKey("roomId") && !(map.get("roomId").toString().equals("")))
                    {
                        roomId = Integer.parseInt(map.get("roomId").toString());
                    }
                }
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pid);
                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                if (orderHead.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    productListModel.setCommodity_type(1);
                }
                productListModel = productListModelMapper.selectOne(productListModel);
                //获取商品规格信息
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(cid);
                confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                if (productListModel != null)
                {
                    //判断商品是否上架-积分商品排除
                    //当其为下架商品且不为积分商品时抛出异常
                    if (!DictionaryConst.GoodsStatus.NEW_GROUNDING.equals((Integer.parseInt(productListModel.getStatus()))) && !DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(orderHead))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWSJ, "商品未上架", "productsList");
                    }
                    //砍价订单不需要判断库存,因为在砍价的时候就吧库存扣除了
                    //订单头部用于判断商品的类型，下面开始判断各类商品的库存信息
                    if (!DictionaryConst.OrdersType.ORDERS_HEADER_KJ.equals(orderHead))
                    {
                        int stockNum;
                        //判断库存是否充足
                        if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(orderHead) && secId != null)
                        {
                            //获取秒杀商品库存
                          /*  SecondsProModel secondsProModel = new SecondsProModel();
                            secondsProModel.setActivity_id(secId);
                            secondsProModel.setAttr_id(cid);
                            secondsProModel = secondsProModelMapper.selectOne(secondsProModel);*/
                            SecondsActivityModel secondsActivityModel = secondsActivityModelMapper.selectByPrimaryKey(secId);
                            if (secondsActivityModel != null)
                            {
                                stockNum = secondsActivityModel.getNum();
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                            }
                        }
                        else if (DictionaryConst.OrdersType.PTHD_ORDER_PP.equals(orderHead) && secId != null)
                        {
                            //获取秒杀商品库存
                            PtSecondsProModel secondsProModel = ptSecondsProModelMapper.selectByPrimaryKey(secId);
                            if (secondsProModel != null)
                            {
                                stockNum = secondsProModel.getNum();
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                            }
                        }
                        else if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(orderHead))
                        {
                            //获取积分库存
                            IntegralGoodsModel integralGoodsModel = new IntegralGoodsModel();
                            integralGoodsModel.setGoods_id(pid);
                            integralGoodsModel.setAttr_id(cid);
                            integralGoodsModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_NO);
                            integralGoodsModel = integralGoodsModelMapper.selectOne(integralGoodsModel);
                            if (integralGoodsModel != null)
                            {
                                stockNum = integralGoodsModel.getNum();
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                            }

                        }
                        else if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(orderHead))
                        {
                            LivingProductModel livingProductModel = new LivingProductModel();
                            livingProductModel.setLiving_id(roomId);
                            livingProductModel.setConfig_id(cid);
                            livingProductModel.setRecycle(0);
                            livingProductModel = livingProductModelMapper.selectOne(livingProductModel);

                            stockNum = livingProductModel.getNum() - livingProductModel.getXl_num();
                            //判断库存
                        }
                        else
                        {
                            if (confiGureModel != null)
                            {
                                stockNum = confiGureModel.getNum();
                            }
                            else
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                            }
                        }
                        if (stockNum < num)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "productsList");
                        }
                    }
                    //虚拟商品处理
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(orderHead))
                    {
                        Integer is_appointment     = productListModel.getIs_appointment();
                        Integer write_off_settings = productListModel.getWrite_off_settings();
                        if (write_off_settings != null && is_appointment != null)
                        {
                            resultMap.put("is_appointment", is_appointment);
                            resultMap.put("write_off_settings", write_off_settings);
                        }
                    }
                    resultMap.put("pid", pid);
                    resultMap.put("cid", cid);
                    resultMap.put("num", num);
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(orderHead))
                    {
                        resultMap.put("roomId", roomId);
                    }
                    ret.add(resultMap);
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效", "productsList");
                }
            }
            else
            //当购物车中有东西，在购物车中结算
            {
                ConfiGureModel attrCount = new ConfiGureModel();
                //设置是否回收
                attrCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                //购物车数据不为空
                String[] cartIdsArray = cartIds.split(SplitUtils.DH);
                for (String cartId : cartIdsArray)
                {
                    //当购物车中的商品为再次购买时
                    if (buyType != 0)
                    {
                        Example          example  = new Example(BuyAgainModal.class);
                        Example.Criteria criteria = example.createCriteria();
                        criteria.andGreaterThan("goods_num", 0);
                        criteria.andEqualTo("id", cartId);
                        List<BuyAgainModal> buyAgainModalList = buyAgainModalMapper.selectByExample(example);
                        for (BuyAgainModal buyAgainModal : buyAgainModalList)
                        {
                            //判断商品是否存在
                            attrCount.setId(StringUtils.stringParseInt(buyAgainModal.getSize_id()));
                            if (confiGureModelMapper.selectCount(attrCount) < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效");
                            }
                            resultMap = new HashMap<>(16);
                            //虚拟商品处理
                            if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(orderHead))
                            {
                                ProductListModel productListModel   = productListModelMapper.selectByPrimaryKey(buyAgainModal.getGoods_id());
                                Integer          is_appointment     = productListModel.getIs_appointment();
                                Integer          write_off_settings = productListModel.getWrite_off_settings();
                                if (write_off_settings != null && is_appointment != null)
                                {
                                    resultMap.put("is_appointment", is_appointment);
                                    resultMap.put("write_off_settings", write_off_settings);
                                }
                            }
                            resultMap.put("pid", buyAgainModal.getGoods_id());
                            resultMap.put("num", buyAgainModal.getGoods_num());
                            resultMap.put("cid", buyAgainModal.getSize_id());
                            ret.add(resultMap);
                        }
                    }
                    else
                    {
                        Example          example  = new Example(CartModel.class);
                        Example.Criteria criteria = example.createCriteria();
                        criteria.andGreaterThan("goods_num", 0);
                        criteria.andEqualTo("id", cartId);
                        List<CartModel> cartModels = cartModelMapper.selectByExample(example);
                        for (CartModel cartModel : cartModels)
                        {
                            //判断商品是否存在
                            attrCount.setId(StringUtils.stringParseInt(cartModel.getSize_id()));
                            if (confiGureModelMapper.selectCount(attrCount) < 1)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效");
                            }
                            //判断库存是否充足
                            if (confiGureModelMapper.sumConfigGureNum(Integer.parseInt(cartModel.getSize_id())) < cartModel.getGoods_num())
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBYSPGGKCBZ, "购物车中有商品库存不足");
                            }
                            resultMap = new HashMap<>(16);
                            //虚拟商品处理
                            if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(orderHead))
                            {
                                ProductListModel productListModel   = productListModelMapper.selectByCartId(Integer.valueOf(cartId));
                                Integer          is_appointment     = productListModel.getIs_appointment();
                                Integer          write_off_settings = productListModel.getWrite_off_settings();
                                if (write_off_settings != null && is_appointment != null)
                                {
                                    resultMap.put("is_appointment", is_appointment);
                                    resultMap.put("write_off_settings", write_off_settings);
                                }
                            }
                            resultMap.put("pid", cartModel.getGoods_id());
                            resultMap.put("num", cartModel.getGoods_num());
                            resultMap.put("cid", cartModel.getSize_id());
                            ret.add(resultMap);
                        }
                    }

                }
            }


        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "productsList");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("处理立即购买异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "productsList");
        }
        return ret;
    }

    /**
     * @param goodsInfoList -[{"pid":"979"},{"cid":"5648"},{"num":1},{"sec_id":"6"}--秒杀id,{}] 商品信息 ， 1.单个商品传入，2.购物车结算传入，3.支付方式 ， 4.商品类型
     * @param cartIds       - 购物车
     * @param buyType       - 购买类型 默认0 再次购买1
     * @param orderHead     - 订单头部
     * @return
     * @throws LaiKeAPIException
     */

    public List<Map<String, Object>> productsList_demo(List<Map<String, Object>> goodsInfoList, String cartIds, int buyType, String orderHead) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //
        List<Map<String, Object>> ret = new ArrayList<>();
        try
        {
            //当购物车为空时，此时为购买单个商品
            if (StringUtils.isEmpty(cartIds))
            {
                String pid    = "";
                String cid    = "";
                String num    = "";
                String sec_id = "";
                for (Map<String, Object> stringObjectMap : goodsInfoList)
                {
                    if (stringObjectMap.containsKey("pid"))
                    {
                        pid = stringObjectMap.get("pid").toString();
                    }
                    else if (stringObjectMap.containsKey("cid"))
                    {
                        cid = stringObjectMap.get("cid").toString();
                    }
                    else if (stringObjectMap.containsKey("num"))
                    {
                        num = stringObjectMap.get("num").toString();
                    }
                }
                //获得商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(Integer.valueOf(pid));
                ProductListModel retail = productListModelMapper.selectOne(productListModel);

                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(Integer.valueOf(cid));
                ConfiGureModel sku = confiGureModelMapper.selectOne(confiGureModel);

                if (retail == null || sku == null)
                {
                    throw new ClassCastException();
                }
                //1.判断商品是否上架
                if (!retail.getStatus().equals(DictionaryConst.GoodsStatus.NEW_GROUNDING) && !DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(orderHead))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "商品已下架", "productsList");
                }
                //2、判断库存，分普通商品、积分商品或者是秒杀商品
                int stock = 0;
                if (DictionaryConst.OrdersType.ORDERS_HEADER_IN.equals(orderHead))
                {
                    IntegralGoodsModel integralGoodsModel = new IntegralGoodsModel();
                    integralGoodsModel.setGoods_id(Integer.valueOf(pid));
                    integralGoodsModel.setAttr_id(Integer.valueOf(cid));
                    integralGoodsModel.setIs_delete(DictionaryConst.WhetherMaven.WHETHER_OK);
                    IntegralGoodsModel integralGoods = integralGoodsModelMapper.selectOne(integralGoodsModel);
                    if (integralGoods != null && integralGoods.getNum() != null)
                    {
                        stock = integralGoods.getNum();
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                    }
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_FS.equals(orderHead) && sec_id != null)
                {
                   /* SecondsProModel secondsProModel = new SecondsProModel();
                    secondsProModel.setActivity_id(Integer.valueOf(sec_id));
                    secondsProModel.setAttr_id(Integer.valueOf(cid));
                    SecondsProModel secondsPro = secondsProModelMapper.selectOne(secondsProModel);*/
                    SecondsActivityModel secondsActivityModel = secondsActivityModelMapper.selectByPrimaryKey(Integer.valueOf(sec_id));
                    if (secondsActivityModel != null && secondsActivityModel.getNum() != null)
                    {
                        stock = secondsActivityModel.getNum();
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPWKCXX, "商品无库存信息", "productsList");
                    }
                }
                else
                {
                    //普通商品
                    stock = retail.getNum();
                }

                if (stock < 0 || Integer.parseInt(num) > stock)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "productsList");
                }

                //List<Map<String, Object>>
                resultMap.put("pid", pid);
                resultMap.put("num", num);
                resultMap.put("cid", cid);

                ret.add(resultMap);
            }
            else
            {
                ConfiGureModel confiGureModel = new ConfiGureModel();
                //购物车处理
                if (Strings.isNullOrEmpty(cartIds))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "productsList");
                }
                String[]         split    = cartIds.split(",");
                Example          example  = new Example(CartModel.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andIn("id", Arrays.asList(split));
                criteria.andGreaterThan("num", 0);
                List<CartModel> cartModels = cartModelMapper.selectByExample(example);
                for (CartModel cartModel : cartModels)
                {
                    //判断商品是否存在，此处只会是普通商品
                    confiGureModel.setId(Integer.valueOf(cartModel.getSize_id()));
                    ConfiGureModel sku = confiGureModelMapper.selectByPrimaryKey(confiGureModel);
                    //判断库存是否充足
                    if (sku == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "商品sku信息不存在", "productsList");
                    }
                    else if (sku.getNum() < cartModel.getGoods_num())
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "库存不足", "productsList");
                    }
                }
            }

        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "productsList");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("处理立即购买异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "productsList");
        }
        return ret;
    }

    @Override
    public Map<String, Object> commodityInformation(int storeId, int mchId,String lang_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //在售商品数量
            int goodsNum;
            //已售数量
            int quantitySold;
            //收藏数量
            int collectionNum;

            Map<String, Object> parmaMap = new HashMap<>(16);

            List<Integer> goodsStatus = new ArrayList<>();
            //获取商户产品配置
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(storeId);
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            //已上架
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
            //            if (productConfigModel != null && productConfigModel.getIs_open() != 0) {
            //                //已下架
            //                goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING);
            //            }
            //获取匹配到的商品
            parmaMap.put("store_id", storeId);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("GoodsStatus", goodsStatus);
            if (StringUtils.isNotEmpty(lang_code))
            {
                parmaMap.put("lang_code", lang_code);
            }
            //在售商品数量
            goodsNum = productListModelMapper.countProductListDynamic(parmaMap);
            //获取商品销售信息
            quantitySold = productListModelMapper.sumDynamic(parmaMap).intValue();

            //获取收藏数量
            UserCollectionModel userCollectionModel = new UserCollectionModel();
            userCollectionModel.setStore_id(storeId);
            userCollectionModel.setMch_id(mchId);
            collectionNum = userCollectionModelMapper.selectCount(userCollectionModel);
            //是否商品上新
            boolean isGroundingNew = false;
            Date    newDate        = productListModelMapper.isUploadNewDate(storeId, mchId, DictionaryConst.GoodsStatus.NEW_GROUNDING);
            if (newDate != null)
            {
                Date date = DateUtil.getAddDate(-7);
                if (DateUtil.dateCompare(newDate, date))
                {
                    isGroundingNew = true;
                }
            }
            resultMap.put("isUploadNewDate", isGroundingNew);

            resultMap.put("quantity_on_sale", goodsNum);
            resultMap.put("quantity_sold", quantitySold);
            resultMap.put("collection_num", collectionNum);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "commodityInformation");
        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getGoodsCommentList(Map<String, Object> parmaMap) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList;
        try
        {
            int storeId = MapUtils.getIntValue(parmaMap, "store_id");

            resultList = commentsModelMapper.getCommentsUserDynamicByPid(parmaMap);
            for (Map<String, Object> map : resultList)
            {
                int id          = Integer.parseInt(map.get("id") + "");
                int attributeId = Integer.parseInt(map.get("attribute_id") + "");
                int anonymous   = Integer.parseInt(map.get("anonymous") + "");
                //购买多少天后追平
                long reviewDay = 0;
                //用户头像
                String headimgurl = map.get("headimgurl") + "";
                //评论时间
                String addTime = DateUtil.dateFormate(map.get("add_time").toString(), GloabConst.TimePattern.YMDHMS);
                //追平时间
                String reviewTime = "";
                if (map.containsKey("review_time"))
                {
                    reviewTime = DateUtil.dateFormate(map.get("review_time").toString(), GloabConst.TimePattern.YMDHMS);
                    String arriveTime = MapUtils.getString(map, "arrive_time");
                    if (StringUtils.isNotEmpty(arriveTime))
                    {
                        Date reviewEndDate  = DateUtil.dateFormateToDate(reviewTime, GloabConst.TimePattern.YMD);
                        Date reviewStartEnd = DateUtil.dateFormateToDate(arriveTime, GloabConst.TimePattern.YMD);
                        reviewDay = DateUtil.dateConversion(reviewEndDate.getTime() / 1000, reviewStartEnd.getTime() / 1000, DateUtil.TimeType.DAY);
                    }
                }
                map.put("review_day", reviewDay);
                //时间处理
                String time = addTime.substring(0, 10);
                //评论图片
                List<Map<String, String>> imgUrls = new ArrayList<>();
                //追平图片
                List<Map<String, String>> reviewImages = new ArrayList<>();
                //回复
                String replyAdmin = "";
                //商品店铺id
                int goodsMchId = MapUtils.getInteger(map, "mch_id");
                //评论id
                int commentId = MapUtils.getIntValue(map, "id");

                //统计图片数量
                CommentsImgModel commentsImgModel = new CommentsImgModel();
                commentsImgModel.setComments_id(id);
                List<CommentsImgModel> commentsImgModelList = commentsImgModelMapper.select(commentsImgModel);
                if (commentsImgModelList != null)
                {
                    for (CommentsImgModel commentsImg : commentsImgModelList)
                    {
                        Map<String, String> commentMap = new HashMap<>(16);
                        //获取图片路径
                        String imgUrl = getImgPath(commentsImg.getComments_url(), storeId);
                        commentMap.put("url", imgUrl);
                        if (commentsImg.getType() == 0)
                        {
                            //评论图片
                            imgUrls.add(commentMap);
                        }
                        else
                        {
                            //追评
                            reviewImages.add(commentMap);
                        }
                    }
                }
                //获取商品属性
                ConfiGureModel confiGureModel = new ConfiGureModel();
                confiGureModel.setId(attributeId);
                confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                //属性处理
                Map<String, String> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(confiGureModel.getAttribute(), Map.class));
                if (attributeMap == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
                }
                StringBuilder attribute = new StringBuilder();
                for (String key : attributeMap.keySet())
                {
                    String attribyteKey   = key;
                    String attribyteValue = attributeMap.get(key) + "";

                    int index = key.indexOf("_LKT_");
                    if (index > 0)
                    {
                        attribyteKey = key.substring(0, attribyteKey.indexOf("_LKT"));
                        //属性值
                        attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT_"));
                    }
                    attribute.append(attribyteKey);
                    attribute.append(":");
                    attribute.append(attribyteValue);
                    attribute.append(",");
                }
                String attributeString = attribute.substring(0, attribute.lastIndexOf(SplitUtils.DH));

                //是否匿名处理
                if (anonymous == 1)
                {
                    map.put("user_name", "匿名");
                }
                else
                {
                    String userName = "匿名";
                    if (map.containsKey("user_name"))
                    {
                        userName = map.get("user_name") + "";
                    }
                    map.put("user_name", userName);
                }
                //查询该条评论的商家回复
                replyAdmin = replyCommentsModelMapper.getMchReplyInfo(commentId, goodsMchId);
                if (StringUtils.isEmpty(replyAdmin))
                {
                    replyAdmin = "";
                }

                map.put("time", time);
                map.put("add_time", addTime);
                map.put("review_time", reviewTime);
                map.put("headimgurl", headimgurl);
                map.put("images", imgUrls);
                map.put("review_images", reviewImages);
                map.put("attribute_str", attributeString);
                //商家回复内容
                map.put("replyAdmin", replyAdmin);
                //回复数量
                map.put("replyNum", replyCommentsModelMapper.countReplyNum(commentId));
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getGoodsCommentList");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品评论信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsCommentList");
        }
        return resultList;
    }

    @Override
    public Map<String, Object> getCommentsDetailInfoById(Map<String, Object> parmaMap) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            int                       total = replyCommentsModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = replyCommentsModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    map.put("id", MapUtils.getIntValue(map, "id"));
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                });
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品回复明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCommentsDetailInfoById");
        }
        return resultMap;
    }

    @Override
    public void delCommentsDetailInfoById(MainVo vo, int cid) throws LaiKeAPIException
    {
        try
        {
            ReplyCommentsModel replyCommentsOld = replyCommentsModelMapper.selectByPrimaryKey(cid);
            if (replyCommentsOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "数据不存在");
            }
            replyCommentsModelMapper.deleteByPrimaryKey(cid);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品评论信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsCommentList");
        }
    }

    @Override
    public boolean validatePhoneCode(String header, String phone, String keyCode) throws LaiKeAPIException
    {
        try
        {
            //校验验证码
            Object pcodeObj = redisUtil.get(header + phone);
            if (pcodeObj == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZXHQYZM, "请重新获取验证码", "setPassword");
            }
            String pcode = pcodeObj.toString();
            if (!pcode.equals(keyCode))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMBZQ, "验证码不正确", "setPassword");
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "validateSMS");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("校验短信异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validateSMS");
        }
        return true;
    }

    @Override
    public Map<String, Integer> sign(User user) throws LaiKeAPIException
    {
        Map<String, Integer> resultMap = new HashMap<>(16);
        //签到状态 0 未签 1 签过了
        int signStatus = 0;
        //签到插件是否启用
        int isStatus = 0;
        try
        {
            //当前时间
            Date sysDate = DateUtil.dateFormateToDate(DateUtil.getEndOfDay(new Date()), GloabConst.TimePattern.YMDHMS);
            //今天开始时间
            Date toDayDate = DateUtil.dateFormateToDate(DateUtil.getStartOfDay(new Date()), GloabConst.TimePattern.YMDHMS);
            if (user != null)
            {
                //查询签到活动
                SignConfigModel signConfigModel = new SignConfigModel();
                signConfigModel.setStore_id(user.getStore_id());
                signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
                if (signConfigModel != null)
                {
                    isStatus = signConfigModel.getIs_status();
                    //是否提醒
                    int isRemind = signConfigModel.getIs_remind();
                    //签到有效开始时间
                    Date starttime = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).parse(signConfigModel.getStarttime());
                    //签到结束时间
                    Date endtime = FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS).parse(signConfigModel.getEndtime());
                    //是否允许多次
                    int isManyTime = signConfigModel.getIs_many_time();
                    //间隔时间
                    String reset = signConfigModel.getReset();
                    //签到次数
                    int scoreNum = signConfigModel.getScore_num();

                    //签到插件是否开启
                    if (signConfigModel.getIs_status() > 0)
                    {
                        // 今天开始时间大于签到结束时间 或者 今天开始时间小于签到开始时间 签到还没进行
                        if (sysDate == null || endtime.getTime() <= sysDate.getTime() || starttime.getTime() >= sysDate.getTime())
                        {
                            //关闭插件
                            signConfigModel = new SignConfigModel();
                            signConfigModel.setIs_status(0);
                            signConfigModelMapper.updateStatusByStoreId(signConfigModel);
                            isStatus = 0;
                        }
                        else
                        {
                            //需要提醒
                            if (isRemind > 0)
                            {
                                SignRecordModel signRecordModel = new SignRecordModel();
                                signRecordModel.setStore_id(user.getStore_id());
                                signRecordModel.setUser_id(user.getUser_id());
                                signRecordModel.setSign_time(toDayDate);
                                List<SignRecordModel> signRecordModels = signRecordModelMapper.getSiginRecord(signRecordModel);
                                if (signRecordModels != null && signRecordModels.size() > 0)
                                {
                                    //是否允许多次签到
                                    if (isManyTime > 0)
                                    {
                                        //是否签够了次数
                                        if (signRecordModels.size() < scoreNum)
                                        {
                                            Date signTime  = signRecordModels.get(0).getSign_time();
                                            long signTimeL = (signTime.getTime() - System.currentTimeMillis()) / 1000;
                                            if (signTimeL >= Long.parseLong(reset))
                                            {
                                                //签过了
                                                signStatus = 1;
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    //签过了
                                    signStatus = 1;
                                }
                            }
                        }

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
            e.printStackTrace();
            logger.error("签到流程异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QDLCWLYC, "签到流程网络异常", "sigin");
        }
        resultMap.put("is_sign_status", isStatus);
        resultMap.put("sign_status", signStatus);
        return resultMap;
    }

    @Override
    public boolean frontPlugin(int storeId, String pluginCode, Map<String, Object> pluginMap) throws LaiKeAPIException
    {
        try
        {
            return frontPlugin(storeId, null, pluginCode, pluginMap,false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证插件是否开启 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontPlugin");
        }
    }

    @Override
    public boolean frontPlugin(int storeId, Integer mchId, String pluginCode, Map<String, Object> pluginMap,boolean isShow) throws LaiKeAPIException
    {
        try
        {
            PluginsModel pluginsModel = pluginsModelMapper.getPluginInfo(pluginCode, storeId);
            if (DictionaryConst.Plugin.PLATFORMACTIVITIES.equals(pluginCode))
            {
                if (pluginsModel != null)
                {
                    return true;
                }
                return true;
            }
            //获取自营店id
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            int     status     = 0;
            Integer zyMchId    = 0;
            if (pluginsModel != null)
            {
                //店铺授权开关
                Integer pluginStatus = pluginsModel.getStatus();
                //以下这三行不确定 todo
//                Integer mchIdTemp = mchId;
//                if (mchId == null) {
//                    mchId = customerModelMapper.getStoreMchId(storeId);
//                }
                switch (pluginCode.toLowerCase())
                {
                    case DictionaryConst.Plugin.SIGN:
                        //验证签到是否开启
                        SignConfigModel signConfigModel = new SignConfigModel();
                        signConfigModel.setStore_id(storeId);
                        signConfigModel = signConfigModelMapper.selectOne(signConfigModel);
                        if (signConfigModel != null)
                        {
                            status = signConfigModel.getIs_status();
                        }
                        break;
                    case DictionaryConst.Plugin.SUBTRACTION:
                        SubtractionConfigModal subtractionConfigModal = new SubtractionConfigModal();
                        subtractionConfigModal.setStore_id(storeId);
                        subtractionConfigModal = subtractionConfigModalMapper.selectOne(subtractionConfigModal);
                        if (subtractionConfigModal != null)
                        {
                            status = subtractionConfigModal.getIs_subtraction();
                        }
                        break;
                    case DictionaryConst.Plugin.DISTRIBUTION:
                        //分销插件是否开启
                        DistributionConfigModel distributionConfigModel = new DistributionConfigModel();
                        distributionConfigModel.setStore_id(storeId);
                        distributionConfigModel = distributionConfigModelMapper.selectOne(distributionConfigModel);
                        if (distributionConfigModel != null)
                        {
                            status = distributionConfigModel.getStatus();
                        }
                        break;
                    case DictionaryConst.Plugin.GOGROUP:
                        //禅道 48292
                        GroupOrderConfigModel groupOrderConfig = new GroupOrderConfigModel();
                        groupOrderConfig.setStore_id(storeId);
                        groupOrderConfig = groupOrderConfigModelMapper.selectOne(groupOrderConfig);
                        if (groupOrderConfig != null)
                        {
                            status = groupOrderConfig.getIsOpen();
                        }
                        break;
                    case DictionaryConst.Plugin.SECONDS:
                        zyMchId = customerModelMapper.getStoreMchId(storeId);
                        SecondsConfigModel secondsConfigModel = new SecondsConfigModel();
                        secondsConfigModel.setStore_id(storeId);
                        secondsConfigModel.setMch_id(zyMchId);
                        secondsConfigModel = secondsConfigModelMapper.selectOne(secondsConfigModel);
                        if (secondsConfigModel != null && secondsConfigModel.getIs_open().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                        {
                            status = 1;
                        }
                        break;
                    case DictionaryConst.Plugin.PRESELL:
                        PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                        preSellConfigModel.setStore_id(storeId);
                        preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                        if (preSellConfigModel != null)
                        {
                            status = preSellConfigModel.getIs_open();
                        }
                        break;
                    case DictionaryConst.Plugin.AUCTION:
                        AuctionConfigModel auctionConfigModel = new AuctionConfigModel();
                        auctionConfigModel.setStore_id(storeId);
                        auctionConfigModel = auctionConfigModelMapper.selectOne(auctionConfigModel);
                        if (auctionConfigModel != null)
                        {
                            status = auctionConfigModel.getIs_open();
                        }
                        break;
                    case DictionaryConst.Plugin.COUPON:
                        //平台关闭店铺优惠劵 只展示自营店店铺优惠卷人口
                        if (pluginStatus.equals(PluginsModel.PluginStatus.STATUS_FAIL))
                        {
                            if (mchId != null && mchId.equals(storeMchId))
                            {
                                status = 1;
                            }
                            break;
                        }
                        //首先看平台是否开启了优惠券,如果关闭了其它店铺都跟着关闭
                        CouponConfigModel couponConfigModel = new CouponConfigModel();
                        couponConfigModel.setStore_id(storeId);
                        couponConfigModel.setMch_id(0);
                        couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                        //店铺id不为空 店铺主页特殊处理
                        if (StringUtils.isNotEmpty(mchId))
                        {
                            MchModel mchModel = new MchModel();
                            mchModel.setId(mchId);
                            mchModel = mchModelMapper.selectOne(mchModel);
                            if (mchModel.getIsOpenCoupon() == 0)
                            {
                                break;
                            }
                        }
                        if (couponConfigModel != null && DictionaryConst.WhetherMaven.WHETHER_OK == couponConfigModel.getIs_status())
                        {
                            status = 1;
                        }
                        break;
                    case DictionaryConst.Plugin.INTEGRAL:
                        //积分商城
                        zyMchId = customerModelMapper.getStoreMchId(storeId);
                        IntegralConfigModel integralConfigModel = new IntegralConfigModel();
                        integralConfigModel.setStore_id(storeId);
                        integralConfigModel.setMch_id(zyMchId);
                        integralConfigModel = integralConfigModelMapper.selectOne(integralConfigModel);
                        if (integralConfigModel != null)
                        {
                            status = integralConfigModel.getStatus();
                        }
                        break;
                    case DictionaryConst.Plugin.WALLET:
                        //钱包
                        ConfigModel configModel = new ConfigModel();
                        configModel.setStore_id(storeId);
                        configModel = configModelMapper.selectOne(configModel);
                        if (configModel != null)
                        {
                            //钱包没有隐藏
                            if (configModel.getHide_your_wallet() == 0)
                            {
                                PaymentModel paymentModel = new PaymentModel();
                                paymentModel.setClass_name(DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY);
                                paymentModel = paymentModelMapper.selectOne(paymentModel);
                                if (paymentModel == null)
                                {
                                    break;
                                }
                                //钱包没有被禁用
                                PaymentConfigModel paymentConfigModel = new PaymentConfigModel();
                                paymentConfigModel.setStore_id(storeId);
                                paymentConfigModel.setPid(paymentModel.getId());
                                paymentConfigModel = paymentConfigModelMapper.selectOne(paymentConfigModel);
                                if (paymentConfigModel == null)
                                {
                                    break;
                                }
                                if (paymentConfigModel.getStatus() == 1)
                                {
                                    status = paymentConfigModel.getStatus();
                                }
                            }
                        }
                        break;
                    case DictionaryConst.Plugin.MCH:
                        MchConfigModel mchConfigModel = new MchConfigModel();
                        mchConfigModel.setStore_id(storeId);
                        if (StringUtils.isEmpty(mchId))
                        {
                            mchConfigModel.setMch_id(customerModelMapper.getStoreMchId(storeId));
                        }
                        else
                        {
                            mchConfigModel.setMch_id(mchId);
                        }
                        mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                        if (mchConfigModel != null)
                        {
                            status = mchConfigModel.getIs_display();
                        }
                        break;
                    case DictionaryConst.Plugin.DIY:
                        //diy如果启用则判断是否应用了diy模板
                        DiyModel diyModel = new DiyModel();
                        diyModel.setStore_id(storeId);
                        diyModel.setMch_id(0);
                        diyModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                        int count = diyModelMapper.selectCount(diyModel);
                        if (count > 0)
                        {
                            status = DictionaryConst.WhetherMaven.WHETHER_OK;
                        }
                        break;
                    case DictionaryConst.Plugin.MEMBER:
                        Map<String, Object> config = memberConfigMapper.getConfig(storeId);
                        Integer isOpen = MapUtils.getInteger(config, "is_open");
                        if (isOpen != null && isOpen > 0)
                        {
                            status = isOpen;
                        }
                        break;
                    case DictionaryConst.Plugin.FLASHSALE:
                        FlashsaleConfigModel flashsaleConfigModel = new FlashsaleConfigModel();
                        flashsaleConfigModel.setStoreId(storeId);
                        flashsaleConfigModel.setMchId(0);
                        flashsaleConfigModel = flashsaleConfigModelMapper.selectOne(flashsaleConfigModel);
                        if (flashsaleConfigModel != null && flashsaleConfigModel.getIsOpen() == 1)
                        {
                            status = flashsaleConfigModel.getIsOpen();
                        }
                        break;
                    case DictionaryConst.Plugin.LIVING:
                        LivingConfigModel livingConfigModel = new LivingConfigModel();
                        livingConfigModel.setStore_id(storeId);
                        livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);
                        if (livingConfigModel != null && livingConfigModel.getIs_open() == 1)
                        {
                            status = livingConfigModel.getIs_open();
                        }
                        break;
                    case DictionaryConst.Plugin.ZC:
                        BbsConfigModel bbsConfigModel = new BbsConfigModel();
                        bbsConfigModel.setStore_id(storeId);
                        bbsConfigModel = bbsConfigModelMapper.selectOne(bbsConfigModel);
                        if (Objects.nonNull(bbsConfigModel))
                        {
                            status = bbsConfigModel.getIs_status();
                        }
                        break;
                    default:
                        status = 1;
                        break;
                }
            }
            if (pluginMap != null)
            {
                if (isShow)
                {
                    if (status == 1)
                    {
                        pluginMap.put(pluginsModel.getPlugin_code(),pluginsModel.getPlugin_name());
                    }
                }
                else
                {
                    pluginMap.put(pluginCode, status);
                }
            }
            return status == 1;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证插件是否开启 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "frontPlugin");
        }
    }

    @Override
    public List<Map<String, Object>> getGoodsActive(int storeId) throws LaiKeAPIException
    {
        try
        {
            return getGoodsActive(storeId, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品支持的活动类型 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsActive");
        }
    }

    @Override
    public List<Map<String, Object>> getGoodsActive(int storeId, Integer active) throws LaiKeAPIException
    {
        List<Map<String, Object>> pluginArr = new ArrayList<>();
        try
        {
            //获取所有商品活动类型(插件)
            List<Map<String, Object>> goodsTagList = dictionaryNameModelMapper.getDicByName(DictionaryConst.DicName.DIC_GOODS_ACTIVE);
            for (Map<String, Object> goodsTagMap : goodsTagList)
            {
                Map<String, Object> dataMap = new HashMap<>(16);
                int                 value   = StringUtils.stringParseInt(goodsTagMap.get("value"));
                //是否支持活动标识
                boolean isOpen = false;
                if (!"会员".equals(goodsTagMap.get("text")))
                {
                    dataMap.put("name", goodsTagMap.get("text"));
                    dataMap.put("value", value);
                    if (DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE.equals(value) || DictionaryConst.GoodsActive.GOODSACTIVE_VIP_DISCOUNT.equals(value))
                    {
                        //正价、会员特惠
                        isOpen = true;
                    }/* else if (DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_JP.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.AUCTION, null)) {
                        //竞拍
                        isOpen = true;
                    } else if (DictionaryConst.GoodsActive.GOODSACTIVE_SECONDS.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.SECONDS, null)) {
                    //秒杀
                    isOpen = true;
                } else if (DictionaryConst.GoodsActive.GOODSACTIVE_INTEGRAL.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.INTEGRAL, null)) {
                    //积分
                    isOpen = true;
                } else if (DictionaryConst.GoodsActive.GOODSACTIVE_SUPPORT_PT.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.GOGROUP, null)) {
                    //拼团是否开启
                    isOpen = true;
                } else if (DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_KJ.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.BARGAIN, null)) {
                    //砍价
                    isOpen = true;
                } else if (DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_JP.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.AUCTION, null)) {
                    //竞拍
                    isOpen = true;
                } else if (DictionaryConst.GoodsActive.GOODSACTIVE_SECONDS.equals(value) && this.frontPlugin(storeId, DictionaryConst.Plugin.SECONDS, null)) {
                    //秒杀
                    isOpen = true;
                }*/
                    if (active != null)
                    {
                        isOpen = isOpen && active.equals(value);
                    }
                    dataMap.put("status", isOpen);
                    pluginArr.add(dataMap);
                }

            }
            return pluginArr;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品支持的活动类型 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsActive");
        }
    }

    @Override
    public void arrangeUserCart(MainVo vo, String userid) throws LaiKeAPIException
    {
        try
        {
            //获取未登录前用户购物车信息
            CartModel cartModel = new CartModel();
            cartModel.setStore_id(vo.getStoreId());
            cartModel.setToken(vo.getAccessId());
            if (StringUtils.isEmpty(vo.getAccessId()))
            {
                return;
            }
            List<CartModel> cartModelList = cartModelMapper.select(cartModel);
            if (cartModelList != null)
            {
                for (CartModel cart : cartModelList)
                {
                    //规格id
                    int sizeId = Integer.parseInt(cart.getSize_id());
                    //购物车数量(未登录前)
                    int goodsNum = cart.getGoods_num();
                    //查询登陆后的用户购物车是否拥有这个商品,有则合并 无则绑定购物车
                    cartModel.setUser_id(userid);
                    cartModel.setGoods_id(cart.getGoods_id());
                    cartModel.setSize_id(cart.getSize_id());
                    CartModel userCart = cartModelMapper.selectOne(cartModel);
                    if (userCart != null)
                    {
                        //当前购物车是自己的则用不用做处理
                        if (userid.equals(userCart.getUser_id()))
                        {
                            continue;
                        }
                        //购物车数量(未登录后)
                        int myGoodsNum = userCart.getGoods_num();
                        //获取该商品某规格库存信息
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setId(sizeId);
                        confiGureModel = confiGureModelMapper.selectOne(confiGureModel);
                        if (confiGureModel != null)
                        {
                            int stokNum = confiGureModel.getNum();
                            // 没登录时购物车数量 + 登入后已存在的购物车数量 >= 库存剩余数量
                            if ((goodsNum + myGoodsNum) >= stokNum)
                            {
                                myGoodsNum = stokNum;
                            }
                            else
                            {
                                myGoodsNum += goodsNum;
                            }
                            //合并数量
                            CartModel updateCart = new CartModel();
                            updateCart.setId(userCart.getId());
                            updateCart.setGoods_num(myGoodsNum);
                            int count = cartModelMapper.updateByPrimaryKeySelective(updateCart);
                            if (count < 1)
                            {
                                logger.info("合并购物车数量失败 参数" + JSON.toJSONString(updateCart));
                            }
                            //删除未登录之前的购物车数据
                            CartModel delCart = new CartModel();
                            delCart.setId(cart.getId());
                            count = cartModelMapper.delete(delCart);
                            if (count < 1)
                            {
                                logger.info("删除购物车(未登录前)失败 参数" + JSON.toJSONString(delCart));
                            }
                        }
                    }
                    else
                    {
                        //登陆用户没有购物车数据,则绑定购物车userid
                        CartModel updateCart = new CartModel();
                        updateCart.setId(cart.getId());
                        updateCart.setUser_id(userid);
                        int count = cartModelMapper.updateByPrimaryKeySelective(updateCart);
                        if (count < 1)
                        {
                            logger.info("用户登陆绑定未登录前的购物车失败 参数" + JSON.toJSONString(updateCart));
                        }
                    }
                }
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "arrangeUserCart");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("整理用户购物车 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "arrangeUserCart");
        }
    }

    @Override
    public Map<String, Object> getAgreement(int storeId, int type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AgreementModel agreementModel = new AgreementModel();
            agreementModel.setStore_id(storeId);
            agreementModel.setType(type);
            agreementModel = agreementModelMapper.selectOne(agreementModel);
            if (agreementModel == null)
            {
                agreementModel = new AgreementModel();
            }

            resultMap.put("agreement", agreementModel.getContent());
            resultMap.put("content", agreementModel.getContent());
            resultMap.put("name", agreementModel.getName());
            return resultMap;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getAgreement");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取协议 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAgreement");
        }
    }

    @Override
    public Map<String, Object> getMchStore(MainVo vo, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (StringUtils.isEmpty(mchId))
            {
                //获取自营店
                mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            }
            List<Map<String, Object>> list = new ArrayList<>();
            if (StringUtils.isNotEmpty(mchId))
            {
                list = mchStoreModelMapper.getStoreList(mchId);
            }
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取店铺门店列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchStore");
        }
        return resultMap;
    }

    @Override
    public List<LangModel> getAdminLangs(MainVo vo) throws LaiKeAPIException
    {
        AdminModel      adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
        List<LangModel> resultList    = new ArrayList<LangModel>();
        List<LangModel> tmpResultList = null;
        try
        {
            //resultList 理论上这个不可能会为空
            resultList = langModelMapper.selectAll();
            int storeId = vo.getStoreId();
            //超管
            if (adminModel.getAdmin_type() != 0)
            {
                CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(storeId);
                String        langs         = customerModel.getStore_langs();

                if (StringUtils.isNotEmpty(langs))
                {
                    Set<String> langSet = Arrays.stream(langs.split(",")).collect(Collectors.toCollection(HashSet::new));
                    tmpResultList = new ArrayList<LangModel>();
                    for (LangModel langModel : resultList)
                    {
                        int id = langModel.getId();
                        for (String langId : langSet)
                        {
                            if (id == Integer.parseInt(langId))
                            {
                                tmpResultList.add(langModel);
                            }
                        }
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
            e.printStackTrace();
            logger.error("获取语种列表异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getLangs");
        }

        return (tmpResultList != null && tmpResultList.size() > 0) ? tmpResultList : resultList;
    }

    @Override
    public List<LangModel> getAppLangs(MainVo vo) throws LaiKeAPIException
    {
        List<LangModel> resultList    = new ArrayList<LangModel>();
        List<LangModel> tmpResultList = null;
        try
        {
            //resultList 理论上这个不可能会为空
            resultList = langModelMapper.selectAll();
            int           storeId       = vo.getStoreId();
            CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(storeId);
            String        langs         = customerModel.getStore_langs();

            if (StringUtils.isEmpty(langs))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWSZYZ, "商城未配置语种", "getAppLangs");
            }

            Set<String> langSet = Arrays.stream(langs.split(",")).collect(Collectors.toCollection(HashSet::new));
            tmpResultList = new ArrayList<LangModel>();
            for (LangModel langModel : resultList)
            {
                int id = langModel.getId();
                for (String langId : langSet)
                {
                    if (id == Integer.parseInt(langId))
                    {
                        tmpResultList.add(langModel);
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
            e.printStackTrace();
            logger.error("获取语种列表异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getLangs");
        }

        return (tmpResultList != null && tmpResultList.size() > 0) ? tmpResultList : resultList;
    }

    @Override
    public List<LangModel> getMchLangs(MainVo vo) throws LaiKeAPIException
    {
        return this.getAppLangs(vo);
    }

    @Override
    public List<Map<String, Object>> getAppCurencys(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            vo.setPageNo(1);
            vo.setPageSize(500);
            vo.setRecycle(0);
            resultList = currencyStoreModelMapper.queryCurrencyStoreList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取币种异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAppCurencys");
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getPcMchCurencys(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            vo.setPageNo(1);
            vo.setPageSize(500);
            vo.setRecycle(0);
            resultList = currencyStoreModelMapper.queryCurrencyStoreList(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取币种异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAppCurencys");
        }
        return resultList;
    }

    @Override
    public Map<String, Object> getStoreDefaultCurrency(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            resultMap = currencyStoreModelMapper.getDefaultCurrency(vo.getStoreId());
            logger.info("商城:{} 默认货币信息：{}", vo.getStoreId(), JSON.toJSONString(resultMap));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商城默认币种异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreDefaultCurrency");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getStoreDefaultLang(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWPZ, "商城未配置", "goodsShare");
            }

            if (StringUtils.isEmpty(configModel.getDefault_lang_code()))
            {
                //商城未设置默认语种
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWSZYZ, " 商城未设置语种", "getStoreDefaultLang");
            }

            LangModel langModel = langModelMapper.getStoreDefaultLangByLangCode(configModel.getDefault_lang_code());
            resultMap.put("storeDefaultLanguage", langModel);
            logger.info("商城:{} 默认语言信息：{}", vo.getStoreId(), JSON.toJSONString(resultMap));

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商城默认语言异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreDefaultLang");
        }
        return resultMap;
    }

    @Override
    public List<CountryModel> getCountryList(MainVo vo) throws LaiKeAPIException
    {
        List<CountryModel> countryModelList;
        try
        {
            CountryModel countryModel = new CountryModel();
            countryModel.setIs_show(1);
            countryModelList = countryModelMapper.select(countryModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取国家列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCountry");
        }
        return countryModelList;
    }


    @Override
    public String getLangName(String langCode) throws LaiKeAPIException
    {
        LangModel result = null;
        try
        {
            if (!StringUtils.isEmpty(langCode))
            {
                result = new LangModel();
                result.setLang_code(langCode);
                result = langModelMapper.selectOne(result);
                if (Objects.nonNull(result))
                {
                    return result.getLang_name();
                }
            }
            return langCode;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取语种名称异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getLangName");
        }
    }

    @Override
    public String getCountryName(int countryNum) throws LaiKeAPIException
    {
        CountryModel result = new CountryModel();
        try
        {
            result.setNum3(countryNum);
            result = countryModelMapper.selectOne(result);
            if (result == null)
            {
                return "" + countryNum;
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "getCountryName");
            }
            return result.getName();
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取语种名称异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getCountryName");
        }
    }
    @Override
    public String generateAccount(Integer length)
    {
       return "lkt" + generateWithRandom(length);
    }

    @Override
    public Map<String, Object> addOrUpdateDiy(DiyVo vo) throws LaiKeAPIException {

        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            DiyModel diyOld = null;
            int      row;
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBMCBNWK, "模板名称不能为空");
            }
            if (vo.getId() != null)
            {
                diyOld = diyModelMapper.selectByPrimaryKey(vo.getId());
                if (diyOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
                }
            }
            if (StringUtils.isEmpty(vo.getValue()) && vo.getMchId() != 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBNWK, "配置不能为空");
            }

            DiyModel diySave = new DiyModel();
            diySave.setCover(vo.getCover());
            diySave.setName(vo.getName());
            diySave.setMch_id(vo.getMchId());
            diySave.setValue(vo.getValue());
            diySave.setUpdate_time(DateUtil.getTime());
            diySave.setAdd_time(DateUtil.getTime());
            diySave.setTheme_dict_code(vo.getTheme_type_code());
            diySave.setTheme_type(vo.getTheme_type());
            diySave.setRemark(vo.getRemark());
            diySave.setTab_bar(vo.getTabBar());
            diySave.setTabber_info(vo.getTabberinfo());

            if (diyOld == null)
            {
                if (StringUtils.isEmpty(vo.getValue()) && vo.getMchId() != 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBNWK, "diy配置不能为空");
                }
                diySave.setStore_id(vo.getStoreId());
                diySave.setVersion("1.0");
                diySave.setStatus(DictionaryConst.WhetherMaven.WHETHER_NO);
                diySave.setType(1);
                diySave.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            }
            else
            {
                diySave.setId(vo.getId());
            }

            if (diyOld == null || !diyOld.getName().equals(vo.getName()))
            {
                DiyModel diyModel = new DiyModel();
                diyModel.setStore_id(vo.getStoreId());
                diyModel.setMch_id(vo.getMchId());
                diyModel.setName(vo.getName());
                diyModel.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
                int count = diyModelMapper.selectCount(diyModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBMCYCZ, "模板名称已存在");
                }
            }

            if (diyOld == null)
            {
                diySave.setLang_code(vo.getLang_code());
                row = diyModelMapper.insertSelective(diySave);
                resultMap.put("id",diySave.getId());
                publiceService.addAdminRecord(vo.getStoreId(), "添加了名称为：" + vo.getName() + "，的DIY模板", AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                if (Objects.nonNull(vo.getTheme_type()) && vo.getTheme_type() == 1)
                {
                    if (redisUtil.hasKey("diy_json_key:" + diySave.getId()))
                    {
                        redisUtil.del("diy_json_key:" + diySave.getId());
                    }
                }
                //页面数据，解绑/绑定
                if (StringUtils.isNotEmpty(vo.getPageInfo()))
                {
                    Integer diyId = diySave.getId();
                    int storeId = vo.getStoreId();
                    JSONArray jsonArray = JSON.parseArray(vo.getPageInfo());
                    for (Object object : jsonArray)
                    {
                        JSONObject jsonObject = (JSONObject) object;

                        // 提取页面信息
                        PageInfo pageInfo = extractPageInfo(jsonObject);
                        //大组件删除
                        if (StringUtils.isNotEmpty(pageInfo.getBigDelLink()))
                        {
                            handleBigComponentDeletion(pageInfo, diyId, diySave.getValue());
                            continue;
                        }

                        // 获取新旧页面
                        DiyPageModel newPageModel = diyPageModelMapper.getOneByLink(pageInfo.getNewLink(), storeId);
                        DiyPageModel oldPageModel = null;

                        //解绑久页面绑定数据
                        if (StringUtils.isNotEmpty(pageInfo.getOldLinkKey()))
                        {
                            oldPageModel = diyPageModelMapper.getOneByLink(pageInfo.getOldLinkKey(), storeId);
                        }
                        if (Objects.nonNull(oldPageModel))
                        {
                            Integer bindId = diyPageBindModelMapper.getBindId(diySave.getId(), oldPageModel.getId(), pageInfo.getLink(), pageInfo.unit);
                            if (Objects.nonNull(bindId))
                            {
                                delBindUnit(oldPageModel.getId(), diySave.getId(), pageInfo.getUnit(), pageInfo.getLink(), bindId, diySave.getValue(), false);
                            }
                        }
                        // 绑定新页面（如果未删除）
                        if (!pageInfo.isDelete())
                        {
                            bindPage(newPageModel, vo.getTheme_type(), diyId, pageInfo.getLink(), pageInfo.getUnit());
                        }
                    }
                }
                row = diyModelMapper.updateByPrimaryKeySelective(diySave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了名称为：" + vo.getName() + "，的DIY模板", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBTJSB, "模板添加失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "actGoodsMove");
        }
        return resultMap;
    }



    @Data
    private static class PageInfo
    {
        private String newLink;
        private String unit;
        private String oldLinkKey;
        private String link;
        private boolean isDelete;
        private String bigDelLink;
    }

    // 提取页面信息到对象
    private PageInfo extractPageInfo(JSONObject jsonObject)
    {
        PageInfo info = new PageInfo();
        info.setNewLink(jsonObject.getString("value"));
        info.setUnit(jsonObject.getString("unit"));
        info.setOldLinkKey(jsonObject.getString("lodValue"));
        info.setLink(jsonObject.getString("link"));
        info.setDelete(jsonObject.getBooleanValue("isDelete"));
        info.setBigDelLink(jsonObject.getString("delLink"));
        return info;
    }

    // 处理大组件删除
    private void handleBigComponentDeletion(PageInfo pageInfo, Integer diyId, String diyJson) throws LaiKeAPIException
    {
        try {
            List<Integer> bindList = diyPageBindModelMapper.getBindByLinkAndDiyId(diyId, pageInfo.getBigDelLink());
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(bindList))
            {
                for (Integer bindId : bindList)
                {
                    DiyPageBindModel bindModel = diyPageBindModelMapper.selectByPrimaryKey(bindId);
                    delBindUnit(bindModel.getDiy_page_id(), diyId, bindModel.getUnit(), pageInfo.getBigDelLink(), bindId, diyJson, false);
                }
            }
        } catch (LaiKeAPIException l)
        {
            throw l;
        } catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "handleBigComponentDeletion");
        }
    }

    /**
     * 页面绑定主题
     */
    private void bindPage(DiyPageModel pageModel, Integer theme_type, Integer diyId, String link_key, String unit) throws LaiKeAPIException
    {
        try
        {
            if (Objects.nonNull(pageModel))
            {
                //系统主题只能绑定系统页面，自定义主题只能绑定自定义页面
                boolean flag = (theme_type == 1 && pageModel.getType() == 1) || (theme_type == 2 && pageModel.getType() == 2);

                //防止组件重复关联
                int i = diyPageBindModelMapper.checkCount(diyId, pageModel.getId(), link_key, unit);
                if (i == 0 && flag)
                {
                    pageModel.setStatus(1);
                    int count = diyPageModelMapper.updateByPrimaryKey(pageModel);
                    if (count < 1) {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }

                    DiyPageBindModel diyPageBindModel = new DiyPageBindModel();
                    diyPageBindModel.setDiy_page_id(pageModel.getId());
                    diyPageBindModel.setLink_key(link_key);
                    diyPageBindModel.setUnit(unit);
                    diyPageBindModel.setDiy_id(diyId);
                    diyPageBindModel.setBind_time(new Date());
                    diyPageBindModelMapper.insertSelective(diyPageBindModel);
                }
            }
        } catch (LaiKeAPIException l)
        {
            throw l;
        } catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bindPage");
        }
    }

    @Override
    public void diyStatus(MainVo vo, Integer id,Integer mchId) throws LaiKeAPIException {
        try
        {
            int row;
            if (id != null)
            {
                DiyModel diyOld = new DiyModel();
                diyOld.setId(id);
                diyOld.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
                diyOld = diyModelMapper.selectOne(diyOld);
                if (diyOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
                }
                //移除之前默认模板
                if (mchId != 0)
                {
                    row = diyModelMapper.removeMchDefault(mchId);
                }
                else
                {
                    row = diyModelMapper.removeDefault(vo.getStoreId());
                }
                if (diyOld.getStatus() == 0)
                {
                    //设置该模板为默认
                    DiyModel diyUpdate = new DiyModel();
                    diyUpdate.setId(diyOld.getId());
                    diyUpdate.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                    row = diyModelMapper.updateByPrimaryKeySelective(diyUpdate);
                    publiceService.addAdminRecord(vo.getStoreId(), "应用了名称为：" + diyOld.getName() + "，的DIY模板", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
            }
            else
            {
                if (mchId != 0)
                {
                    row = diyModelMapper.removeMchDefault(mchId);
                }
                else
                {
                    row = diyModelMapper.removeDefault(vo.getStoreId());
                }
                publiceService.addAdminRecord(vo.getStoreId(), "应用了默认模板", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBSZSB, "diy模板设置失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDiy");
        }
    }

    @Override
    public void delDiy(MainVo vo, int id) throws LaiKeAPIException {
        try {
            int row;
            DiyModel diyOld = new DiyModel();
            diyOld.setId(id);
            diyOld.setIs_del(DictionaryConst.WhetherMaven.WHETHER_NO);
            diyOld = diyModelMapper.selectOne(diyOld);
            if (diyOld == null) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "diy模板不存在");
            }
            DiyModel diyUpdate = new DiyModel();
            diyUpdate.setId(diyOld.getId());
            diyUpdate.setIs_del(DictionaryConst.WhetherMaven.WHETHER_OK);

            //查看当前主题是否绑定了页面
            List<Integer> pageIdList = diyPageBindModelMapper.getPageIdByDiyId(id);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(pageIdList))
            {
                diyPageBindModelMapper.deleteByDiyId(id);

                for (Integer pageId : pageIdList)
                {
                   int i = diyPageBindModelMapper.countByDiyId(pageId);
                    if (i == 0)
                    {
                        DiyPageModel diyPageModel = new DiyPageModel();
                        diyPageModel.setId(pageId);
                        diyPageModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_NO);
                        diyPageModelMapper.updateByPrimaryKeySelective(diyPageModel);
                    }
                }
            }



            publiceService.addAdminRecord(vo.getStoreId(), "删除了名称为：" + diyOld.getName() + "，的DIY模板", AdminRecordModel.Type.DEL, vo.getAccessId());
            row = diyModelMapper.updateByPrimaryKeySelective(diyUpdate);

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBSCSB, "diy模板删除失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除diy模板 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDiy");
        }
    }

    @Autowired
    private LangModelMapper langModelMapper;

    @Autowired
    private CountryModelMapper countryModelMapper;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;


    /**
     * 此处还处理了运费，特殊商品只能单个
     *
     * @param products    -  商品pid，num，cid的集合，购物车下则有数个，单个商品则只有一个
     * @param storeId     -  商城id
     * @param productType -  商品类型
     * @return
     */
    @Override
    public Map<String, Object> settlementProductsInfo(List<Map<String, Object>> products, int storeId, String productType)
    {
        //商品信息
        Map<String, Object> resutlProducts = new HashMap<>();
        try
        {
            //商品总价
            BigDecimal orderProductTotal = new BigDecimal(0);
            if (CollectionUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "settlementProductsInfo");
            }
            //店铺商品信息
            Map<String, List<Map<String, Object>>> mchProductsResult = new HashMap<>();
            //店铺运费处理 products_freight
            Map<String, List<Map<String, Object>>> mchReightResult = new HashMap<>();

            //根据每一个商品来判断，每一个map中包含sec_id（秒杀）,num,cid,pid
            for (Map<String, Object> map : products)
            {
                map.put("store_id", storeId);
                //
                Map<String, Object> tmpRet;
                //是否免运费
                boolean isFreeFreight = false;
                //秒杀为平台秒杀或者为店铺秒杀
                if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType) || DictionaryConst.OrdersType.PTHD_ORDER_PM.equals(productType))
                {
                    //秒杀商品信息
                    //secId活动id，needNum为购买的数量
                    int secId   = Integer.parseInt(map.get("sec_id").toString());
                    int needNum = Integer.parseInt(map.get("num").toString());
                    //秒杀商品
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType))
                    {
                        //秒杀的该商品及其属性的所有信息
                        tmpRet = secondsProModelMapper.settlementProductsInfo(storeId, needNum, secId, MapUtils.getIntValue(map, "cid"));
                    }
                    else
                    {
                        //平台暂时无用 2021-10-29 17:32:40
                        tmpRet = ptSecondsProModelMapper.settlementProductsInfo(storeId, needNum, secId);
                    }
                    //包邮商品，此处必不免邮
                    //todo 检查此处
                    if ("1".equals(tmpRet.get("free_freight")))
                    {
                        isFreeFreight = true;
                    }
                }
                else if (DictionaryConst.OrdersType.ORDERS_HEADER_ZB.equals(productType))
                {
                    tmpRet = productListModelMapper.settlementLiveProductsInfo(map);
                }
                else
                //普通商品
                {
                    tmpRet = productListModelMapper.settlementProductsInfo(map);
                }


                if (!CollectionUtils.isEmpty(tmpRet))
                {
                    //商品信息，再将storeId，pid，num，cid等数据传入
                    tmpRet.putAll(map);
                    // 产品单价 变成分
                    BigDecimal productPrice = new BigDecimal(String.valueOf(tmpRet.get("price")));
                    //如果是秒杀,则获取秒杀价格
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType))
                    {
                        //秒杀商品为百分比或者是固定值
                        int type = MapUtils.getIntValue(tmpRet, "price_type");
                        //规格价格即出售价格
                        BigDecimal attrPrice = new BigDecimal(MapUtils.getString(tmpRet, "attrPrice"));
                        if (type == 0)
                        {
                            //百分比
                            //秒杀价格=出售价格*秒杀价格/100，当按百分比结算时，productPrice中的则为百分比数需要除以100，例如99/100
                            productPrice = attrPrice.multiply(productPrice).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                            //如果秒杀的价格低于0.01则为0.01
                            if (productPrice.compareTo(new BigDecimal("0.01")) < 0)
                            {
                                productPrice = new BigDecimal("0.01");
                            }
                        }
                        tmpRet.put("secPrice", productPrice);
                    }


                    BigDecimal num    = new BigDecimal(String.valueOf(map.get("num")));
                    int        pid    = Integer.parseInt(String.valueOf(map.get("pid")));
                    String     weight = String.valueOf(tmpRet.get("weight"));
                    //价格*数量，订单总价累加
                    BigDecimal delta = productPrice.multiply(num);
                    //orderProductTotal第一次加
                    orderProductTotal = orderProductTotal.add(delta);
                    //规格处理
                    String key = "attribute";
                    //tmpRet从数据库中取出来的
                    String attrs = (String) tmpRet.get(key);
                    //对字符串解析
                    String sku = GoodsDataUtils.getProductSkuValue(attrs);
                    tmpRet.put("size", sku);
                    //结果移除规格信息，使用size来表示sku
                    tmpRet.remove(key);
                    //图片处理
                    key = "img";
                    String imgPath = getImgPath((String) tmpRet.get(key), storeId);
                    tmpRet.put(key, imgPath);

                    //校验商品是否已下架或者删除
                    int count = productListModelMapper.isBuyGoods(pid, DictionaryConst.GoodsStatus.NEW_GROUNDING);
                    if (count < 1)
                    {
                        logger.debug("购买商品{} 时,商品被下架或者被删除", pid);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效!", "settlementProductsInfo");
                    }

                    //运费处理
                    if (!isFreeFreight)
                    {
                        //店铺id
                        String mch_key = String.valueOf(tmpRet.get("mch_id"));
                        //运费id
                        String     fidStr = MapUtils.getString(tmpRet, "freight", "");
                        BigDecimal freight;

                        //店铺商品运费处理
                        Map<String, Object> freightInfo = new HashMap<>();
                        freightInfo.put("num", num);
                        freightInfo.put("pid", pid);
                        if (!org.apache.commons.lang3.StringUtils.isEmpty(fidStr))
                        {
                            freight = new BigDecimal(fidStr);
                            //运费id，还需要到运费表中去查询，但不在此方法中处理
                            freightInfo.put("freight_id", freight);
                        }
                        freightInfo.put("weight", weight);

                        //Map<String, List<Map<String, Object>>> mchReightResult = new HashMap<>();运费处理
                        //map中key为店铺的id，value值为num数量、pid商品id、weight重量、freight运费id分别为map组成的list
                        //在for循环中首先验证是否存在以商店id为key的map，防止同一商店id只有一个key
                        //此处先不去表里找，到后面再找
                        if (mchReightResult.containsKey(mch_key))
                        {
                            mchReightResult.get(mch_key).add(freightInfo);
                        }
                        else
                        {
                            List<Map<String, Object>> mchProductInfo = new ArrayList<>();
                            mchProductInfo.add(freightInfo);
                            mchReightResult.put(mch_key, mchProductInfo);
                        }

                        //按照店铺分类，存储商品信息，下面运行完后结果集中则有了运费的信息和商品的信息，都是以店铺的id来分组的
                        //Map<String, List<Map<String, Object>>> mchProductsResult = new HashMap<>();
                        if (mchProductsResult.containsKey(mch_key))
                        {
                            mchProductsResult.get(mch_key).add(tmpRet);
                        }
                        else
                        {
                            List<Map<String, Object>> mchProductInfo = new ArrayList<>();
                            mchProductInfo.add(tmpRet);
                            mchProductsResult.put(mch_key, mchProductInfo);
                        }
                    }

                }
                else
                {
                    logger.debug("购买商品时,商品被下架或者被删除");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效!", "settlementProductsInfo");
                }
            }

            //所有店铺id集合
            Set<String> mchids = mchProductsResult.keySet();
            //多店铺信息和商品信息集合
            List<Map<String, Object>> returnMchInfoAndProducts = new ArrayList<>();
            //店铺信息和店铺对应的商品信息已经店铺商品总价

            //结算界面按店铺结算的基本信息
            //针对多商店的购物车下单for处理
            for (String mchid : mchids)
            {
                Map<String, Object> mchInfoAndProducts = new HashMap<>();
                //该店铺的商品
                List<Map<String, Object>> thisMchProducts = mchProductsResult.get(mchid);
                //商品信息
                mchInfoAndProducts.put("list", thisMchProducts);
                //店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setId(Integer.parseInt(mchid));
                mchModel = mchModelMapper.selectOne(mchModel);
                mchInfoAndProducts.put("shop_id", mchModel.getId());
                mchInfoAndProducts.put("shop_name", mchModel.getName());
                mchInfoAndProducts.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
                mchInfoAndProducts.put("head_img", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                //店铺商品总价
                AtomicInteger mchProductTotal = new AtomicInteger(0);
                for (Map<String, Object> product : thisMchProducts)
                {
                    int productPrice = BigDecimal.valueOf(Double.parseDouble(String.valueOf(product.get("price"))) * 100).intValue();
                    int num          = Integer.parseInt(String.valueOf(product.get("num")));
                    int delta        = productPrice * num;
                    mchProductTotal = new AtomicInteger(mchProductTotal.addAndGet(delta));
                }
                mchInfoAndProducts.put("product_total", mchProductTotal.doubleValue() / 100.0);
                //List<Map<String, Object>> returnMchInfoAndProducts = new ArrayList<>();
                //最终返回这个list，该list中为map1，map1中存放了商店商品信息、商店基本信息、每个店铺商品的总价
                returnMchInfoAndProducts.add(mchInfoAndProducts);
            }

            //订单商品总额
            resutlProducts.put("products_total", orderProductTotal);
            //按店铺区分的商品信息
            resutlProducts.put("products", returnMchInfoAndProducts);
            //按店铺分类的商品运费信息
            resutlProducts.put("products_freight", mchReightResult);

            //TODO 以下这两个key没有使用到 ，php中使用的是第一个商品的信息
            resutlProducts.put("product_class", "==");
            resutlProducts.put("product_id", "111");

            return resutlProducts;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("获取订单确认信息 失败 ", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取订单确认信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDDQRXXSB, "获取订单确认信息失败", "settlementProductsInfo");
        }
    }


    /**
     * 此处还处理了运费，运费为商品表中的运费
     *
     * @param products    （storeId，num，pid，cid） -  商品pid，num，cid的集合，购物车下则有数个此类循环，单个商品则只有一个此类循环
     * @param storeId     -  商城id
     * @param productType -  商品类型
     * @return 商品订单总额；按店铺区分的商品信息；按店铺区分的运费信息
     */
    @Override
    public Map<String, Object> settlementProductsInfoVI(List<Map<String, Object>> products, int storeId, String productType)
    {
        //商品信息
        Map<String, Object> resutlProducts = new HashMap<>();
        try
        {
            //商品总价
            BigDecimal orderProductTotal = new BigDecimal(0);
            if (CollectionUtils.isEmpty(products))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "settlementProductsInfo");
            }
            //店铺商品信息
            Map<String, List<Map<String, Object>>> mchProductsResult = new HashMap<>();
            //店铺运费处理 products_freight
            Map<String, List<Map<String, Object>>> mchReightResult = new HashMap<>();

            //根据每一个商品来判断，每一个map中包含sec_id（秒杀）,num,cid,pid
            for (Map<String, Object> map : products)
            {
                map.put("store_id", storeId);
                //
                Map<String, Object> tmpRet;
                //是否免运费，虚拟商品无运费
                boolean isFreeFreight = false;
                if (productType.equals(DictionaryConst.OrdersType.ORDERS_HEADER_VI))
                {
                    if (map.get("write_off_settings") == (ProductListModel.WRITE_OFF_SETTINGS.offline))
                    {
                        if (map.get("is_appointment") == (ProductListModel.IS_APPOINTMENT.isOpin))
                        {
                            //需要预约下单
                            resutlProducts.put("address_status", 2);
                        }
                        else
                        {
                            //无需预约，不需要显示地址给
                            resutlProducts.put("address_status", 1);
                        }
                    }
                    else
                    {
                        resutlProducts.put("address_status", 1);
                        resutlProducts.put("write_off_settings", ProductListModel.WRITE_OFF_SETTINGS.dispenseWith);
                    }
                    //isFreeFreight = true;
                }
                //秒杀为平台秒杀或者为店铺秒杀
                if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType) || DictionaryConst.OrdersType.PTHD_ORDER_PM.equals(productType))
                {
                    //秒杀商品信息
                    //secId活动id，needNum为购买的数量
                    int secId   = Integer.parseInt(map.get("sec_id").toString());
                    int needNum = Integer.parseInt(map.get("num").toString());
                    //秒杀商品
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType))
                    {
                        //秒杀的该商品及其属性的所有信息
                        tmpRet = secondsProModelMapper.settlementProductsInfo(storeId, needNum, secId, MapUtils.getIntValue(map, "cid"));
                    }
                    else
                    {
                        //平台暂时无用 2021-10-29 17:32:40
                        tmpRet = ptSecondsProModelMapper.settlementProductsInfo(storeId, needNum, secId);
                    }
                    //包邮商品，此处必不免邮
                    //todo 检查此处
                    if ("1".equals(tmpRet.get("free_freight")))
                    {
                        isFreeFreight = true;
                    }
                }
                else
                //普通商品
                {
                    tmpRet = productListModelMapper.settlementProductsInfo(map);
                    if (tmpRet.containsKey("write_off_settings") && DataUtils.getIntegerVal(tmpRet, "write_off_settings") == ProductListModel.WRITE_OFF_SETTINGS.dispenseWith)
                    {
                        resutlProducts.put("write_off_settings", ProductListModel.WRITE_OFF_SETTINGS.dispenseWith);
                    }
                }

                if (!CollectionUtils.isEmpty(tmpRet))
                {
                    //商品信息，再将storeId，pid，num，cid等数据传入
                    tmpRet.putAll(map);
                    // 产品单价 变成分
                    BigDecimal productPrice = new BigDecimal(String.valueOf(tmpRet.get("price")));
                    //如果是秒杀,则获取秒杀价格
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_MS.equals(productType))
                    {
                        //秒杀商品为百分比或者是固定值
                        int type = MapUtils.getIntValue(tmpRet, "price_type");
                        //规格价格即出售价格
                        BigDecimal attrPrice = new BigDecimal(MapUtils.getString(tmpRet, "attrPrice"));
                        if (type == 0)
                        {
                            //百分比
                            //秒杀价格=出售价格*秒杀价格/100，当按百分比结算时，productPrice中的则为百分比数需要除以100，例如99/100
                            productPrice = attrPrice.multiply(productPrice).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
                        }
                        tmpRet.put("secPrice", productPrice);
                    }


                    BigDecimal num    = new BigDecimal(String.valueOf(map.get("num")));
                    int        pid    = Integer.parseInt(String.valueOf(map.get("pid")));
                    String     weight = String.valueOf(tmpRet.get("weight"));
                    //价格*数量，订单总价累加
                    BigDecimal delta = productPrice.multiply(num);
                    //orderProductTotal第一次加
                    orderProductTotal = orderProductTotal.add(delta);
                    //规格处理
                    String key = "attribute";
                    //tmpRet从数据库中取出来的
                    String attrs = (String) tmpRet.get(key);
                    //对字符串解析
                    String sku = GoodsDataUtils.getProductSkuValue(attrs);
                    tmpRet.put("size", sku);
                    //结果移除规格信息，使用size来表示sku
                    tmpRet.remove(key);
                    //图片处理
                    key = "img";
                    String imgPath = getImgPath((String) tmpRet.get(key), storeId);
                    tmpRet.put(key, imgPath);

                    //校验商品是否已下架或者删除
                    int count = productListModelMapper.isBuyGoods(pid, DictionaryConst.GoodsStatus.NEW_GROUNDING);
                    if (count < 1)
                    {
                        logger.debug("购买商品{} 时,商品被下架或者被删除", pid);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效!", "settlementProductsInfo");
                    }


                    //店铺id
                    String mch_key = String.valueOf(tmpRet.get("mch_id"));


                    //按照店铺分类，存储商品信息，下面运行完后结果集中则有了运费的信息和商品的信息，都是以店铺的id来分组的
                    //Map<String, List<Map<String, Object>>> mchProductsResult = new HashMap<>();
                    if (mchProductsResult.containsKey(mch_key))
                    {
                        mchProductsResult.get(mch_key).add(tmpRet);
                    }
                    else
                    {
                        List<Map<String, Object>> mchProductInfo = new ArrayList<>();
                        mchProductInfo.add(tmpRet);
                        mchProductsResult.put(mch_key, mchProductInfo);
                    }


                }
                else
                {
                    logger.debug("购买商品时,商品被下架或者被删除");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPYSX, "商品已失效!", "settlementProductsInfo");
                }
            }

            //所有店铺id集合
            Set<String> mchids = mchProductsResult.keySet();
            //多店铺信息和商品信息集合
            List<Map<String, Object>> returnMchInfoAndProducts = new ArrayList<>();
            //店铺信息和店铺对应的商品信息已经店铺商品总价

            //结算界面按店铺结算的基本信息
            //针对多商店的购物车下单for处理
            for (String mchid : mchids)
            {
                Map<String, Object> mchInfoAndProducts = new HashMap<>();
                //该店铺的商品
                List<Map<String, Object>> thisMchProducts = mchProductsResult.get(mchid);
                //商品信息
                mchInfoAndProducts.put("list", thisMchProducts);
                //店铺信息
                MchModel mchModel = new MchModel();
                mchModel.setId(Integer.parseInt(mchid));
                mchModel = mchModelMapper.selectOne(mchModel);
                mchInfoAndProducts.put("shop_id", mchModel.getId());
                mchInfoAndProducts.put("shop_name", mchModel.getName());
                mchInfoAndProducts.put("shop_logo", publiceService.getImgPath(mchModel.getLogo(), storeId));
                mchInfoAndProducts.put("head_img", publiceService.getImgPath(mchModel.getHead_img(), storeId));
                //店铺商品总价
                AtomicInteger mchProductTotal = new AtomicInteger(0);
                for (Map<String, Object> product : thisMchProducts)
                {
                    int productPrice = BigDecimal.valueOf(Double.parseDouble(String.valueOf(product.get("price"))) * 100).intValue();
                    int num          = Integer.parseInt(String.valueOf(product.get("num")));
                    int delta        = productPrice * num;
                    mchProductTotal = new AtomicInteger(mchProductTotal.addAndGet(delta));
                }
                mchInfoAndProducts.put("product_total", mchProductTotal.doubleValue() / 100.0);
                //List<Map<String, Object>> returnMchInfoAndProducts = new ArrayList<>();
                //最终返回这个list，该list中为map1，map1中存放了商店商品信息、商店基本信息、每个店铺商品的总价
                returnMchInfoAndProducts.add(mchInfoAndProducts);
            }

            //订单商品总额
            resutlProducts.put("products_total", orderProductTotal);
            //按店铺区分的商品信息
            resutlProducts.put("products", returnMchInfoAndProducts);
            //按店铺分类的商品运费信息
            resutlProducts.put("products_freight", mchReightResult);

            //TODO 以下这两个key没有使用到 ，php中使用的是第一个商品的信息
            resutlProducts.put("product_class", "==");
            resutlProducts.put("product_id", "111");

            return resutlProducts;
        }
        catch (LaiKeAPIException e)
        {
            logger.error("获取订单确认信息 失败 ", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("获取订单确认信息 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDDQRXXSB, "获取订单确认信息失败", "settlementProductsInfo");
        }
    }

    @Override
    public List<Integer> getCheckedZiXuanGoodsList(int storeId, int shopId) throws LaiKeAPIException
    {
        List<Integer> goodsIds = new ArrayList<>();
        try
        {
            //获取商城自营店铺信息
            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(storeId);
            adminModel.setType(1);
            adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            adminModel = adminModelMapper.selectOne(adminModel);
            //获取当前店铺非自选商品集,当前商品不能和平台自选商品一样,一样则剔除
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setMch_id(shopId);
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS + "");
            productListModel.setIs_zixuan("0");
            List<ProductListModel> productListModelList = productListModelMapper.select(productListModel);
            for (ProductListModel productList : productListModelList)
            {
                //自选商品名称
                String zxName = productList.getProduct_title();
                //查询平台自选商品
                productListModel.setMch_id(adminModel.getShop_id());
                productListModel.setIs_zixuan("1");
                List<ProductListModel> ptGoodsList = productListModelMapper.select(productListModel);
                for (ProductListModel ptGoods : ptGoodsList)
                {
                    if (!StringUtils.isEmpty(ptGoods.getCommodity_str()))
                    {
                        //获取商品id集 选中后都会往这个字段新增一个id
                        List<Integer> checkedGoodsIds = DataUtils.cast(SerializePhpUtils.getUnserializeArray1(ptGoods.getCommodity_str(), List.class));
                        if (checkedGoodsIds != null)
                        {
                            //是否已经选中了
                            if (checkedGoodsIds.contains(productList.getId()))
                            {
                                goodsIds.add(productList.getId());
                            }
                        }
                        //商品名称是否相同
                        if (ptGoods.getProduct_title().equals(zxName))
                        {
                            goodsIds.add(productList.getId());
                        }
                    }
                }
            }
            return goodsIds;
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "getZiXuanGoodsList");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取自选商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getZiXuanGoodsList");
        }
    }

    @Override
    public Map<String, Object> getMemberPrice(List<Map<String, Object>> params, String userId, int storeId) throws LaiKeAPIException
    {
        try
        {
            if (CollectionUtils.isEmpty(params))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "getMemberPrice");
            }
            //默认没有折扣
            BigDecimal       gradeRate        = new BigDecimal("1");
            ProductListModel productListModel = new ProductListModel();
            int              pid              = (Integer) ((List<Map<String, Object>>) (params.get(0).get("list"))).get(0).get("pid");
            productListModel.setId(pid);
            productListModel = productListModelMapper.selectOne(productListModel);
            String productType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
            String activeStr   = productListModel.getActive();
            //特惠商品
            if ("6".equals(activeStr))
            {
                productType = DictionaryConst.OrdersType.ORDERS_HEADER_TH;
            }
            gradeRate = new BigDecimal(String.valueOf(publicMemberService.getMemberGradeRate(productType, userId, storeId)));
            //订单产品总价
            BigDecimal orderProductTotal = new BigDecimal(0);
            //按照店铺计算
            for (Map<String, Object> mchAllInfo : params)
            {
                //店铺商品信息
                List<Map<String, Object>> mchProductsInfo = (List<Map<String, Object>>) mchAllInfo.get("list");
                if (CollectionUtils.isEmpty(mchProductsInfo))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "getMemberPrice");
                }
                //店铺商品总价
                BigDecimal mchSellProductTotal = new BigDecimal(0);
                //会员商品总价
                BigDecimal mchMemberProductTotal = new BigDecimal(0);
                for (Map<String, Object> productInfo : mchProductsInfo)
                {
                    //产品原始售价
                    BigDecimal productPrice = new BigDecimal(MapUtils.getString(productInfo, "price"));
                    //产品数量
                    BigDecimal num = new BigDecimal(String.valueOf(productInfo.get("num")));
                    //会员价
                    BigDecimal memberPrice = productPrice.multiply(gradeRate);
                    //设置会员价
                    productInfo.put("membership_price", memberPrice);
                    //会员单个产品总价
                    BigDecimal memberTotalPrice = memberPrice.multiply(num);
                    //售价总价
                    BigDecimal delta = productPrice.multiply(num);
                    //单个产品售价总价
                    mchSellProductTotal = mchSellProductTotal.add(delta);
                    //单个产品会员价总价
                    mchMemberProductTotal = mchMemberProductTotal.add(memberTotalPrice);
                    // 优惠后金额 如果没有优惠则原价
                    productInfo.put("amount_after_discount", memberTotalPrice);
                    //订单所有产品总价
                    orderProductTotal = orderProductTotal.add(memberTotalPrice);
                }
                //会员优惠价格
                mchAllInfo.put("grade_rate_amount", DoubleFormatUtil.format((mchSellProductTotal.subtract(mchMemberProductTotal)).doubleValue()));
                //店铺总产品价格
                mchAllInfo.put("product_total", DoubleFormatUtil.format(mchMemberProductTotal.doubleValue()));
            }

            Map<String, Object> result = new HashMap<>();
            //会员优惠
            result.put("grade_rate", gradeRate);
            result.put("products", params);
            //订单商品总价
            result.put("products_total", DoubleFormatUtil.format(orderProductTotal.doubleValue()));
            return result;
        }
        catch (Exception e)
        {
            logger.error("会员优惠计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYYHJSSB, "会员优惠计算失败", "getMemberPrice");
        }

    }

    @Override
    public Map<String, Object> getMemberPrice(MemberPriceVo vo, Integer vipSource) throws LaiKeAPIException
    {
        try
        {
            if (CollectionUtils.isEmpty(vo.getMchProductList()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "getMemberPrice");
            }
            String productType = DictionaryConst.OrdersType.ORDERS_HEADER_GM;
            //订单产品总价
            BigDecimal orderProductTotal = new BigDecimal(0);
            //获取折扣
            BigDecimal gradeRate = BigDecimal.TEN;
            //根据商城的id来搜索每个商城的会员的设置
            Map<String, Object> config = memberConfigMapper.getConfig(vo.getStoreId());
            //商城会员设置有且已打开
            if (MapUtils.getInteger(config, "id") != null && MapUtils.getInteger(config, "is_open") > 0)
            {
                //获得会员打折率，如8.8
                String memberDiscount = MapUtils.getString(config, "member_discount");
                gradeRate = new BigDecimal(memberDiscount);
            }
            User user = new User();
            user.setUser_id(vo.getUserId());
            user.setStore_id(vo.getStoreId());
            user = userBaseMapper.selectOne(user);
            //该用户没有会员则为10折，
            if (user.getGrade() == null || user.getGrade().equals(User.USER))
            {
                gradeRate = BigDecimal.TEN;
            }
            //按照店铺计算
            for (Map<String, Object> mchAllInfo : vo.getMchProductList())
            {
                //店铺商品信息
                List<Map<String, Object>> mchProductsInfo = DataUtils.cast(mchAllInfo.get("list"));
                if (CollectionUtils.isEmpty(mchProductsInfo))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "getMemberPrice");
                }
                //店铺商品总价
                BigDecimal mchSellProductTotal = new BigDecimal(0);
                //会员商品总价
                BigDecimal mchMemberProductTotal = new BigDecimal(0);
                //遍历店铺商品
                for (Map<String, Object> productInfo : mchProductsInfo)
                {
                    int            pid            = MapUtils.getIntValue(productInfo, "pid");
                    MemberProModel memberProModel = new MemberProModel();
                    memberProModel.setPro_id(pid);
                    memberProModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                    memberProModel = memberProModelMapper.selectOne(memberProModel);
                    //如果此商品不是会员折扣商品则不打折
                    if (Objects.isNull(memberProModel) || memberProModel.getId() == null)
                    {
                        gradeRate = BigDecimal.TEN;
                    }
                    if (vipSource == null || vipSource.equals(DictionaryConst.WhetherMaven.WHETHER_NO))
                    {
                        gradeRate = BigDecimal.TEN;
                    }
                    //产品原始售价
                    BigDecimal productPrice = new BigDecimal(MapUtils.getString(productInfo, "price"));
                    //产品数量
                    BigDecimal num = new BigDecimal(String.valueOf(productInfo.get("num")));
                    //会员价
                    BigDecimal memberPrice = productPrice.multiply(gradeRate).divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
                    //设置会员价
                    productInfo.put("membership_price", memberPrice);
                    //会员单个产品总价
                    BigDecimal memberTotalPrice = memberPrice.multiply(num);
                    //售价总价
                    BigDecimal delta = productPrice.multiply(num);
                    //单个产品售价总价
                    mchSellProductTotal = mchSellProductTotal.add(delta);
                    //单个产品会员价总价
                    mchMemberProductTotal = mchMemberProductTotal.add(memberTotalPrice);
                    // 优惠后金额 如果没有优惠则原价
                    productInfo.put("amount_after_discount", memberTotalPrice);
                    //订单所有产品总价
                    //使用商品价格--禅道46507
                    orderProductTotal = orderProductTotal.add(delta);
                }
                //会员优惠价格
                mchAllInfo.put("grade_rate_amount", DoubleFormatUtil.format((mchSellProductTotal.subtract(mchMemberProductTotal)).doubleValue()));
                //店铺总产品价格
                mchAllInfo.put("product_total", DoubleFormatUtil.format(mchMemberProductTotal.doubleValue()));
            }

            Map<String, Object> result = new HashMap<>();
            //会员优惠
            result.put("grade_rate", gradeRate);
            result.put("products", vo.getMchProductList());
            //订单商品总价
            result.put("products_total", DoubleFormatUtil.format(orderProductTotal.doubleValue()));
            return result;
        }
        catch (Exception e)
        {
            logger.error("会员优惠计算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HYYHJSSB, "会员优惠计算失败", "getMemberPrice");
        }
    }

    private ActivityModelMapper activityModelMapper;

    @Override
    public boolean activityPlugin(int storeId, String pluginCode) throws LaiKeAPIException
    {
        try
        {
            boolean switchFlag = false;
            if (this.frontPlugin(storeId, null, pluginCode, null,false))
            {
                int pluginType;
                switch (pluginCode.toLowerCase())
                {
                    case DictionaryConst.Plugin.INTEGRAL:
                        pluginType = DictionaryConst.GoodsActive.GOODSACTIVE_INTEGRAL;
                        break;
                    case DictionaryConst.Plugin.SECONDS:
                        pluginType = DictionaryConst.GoodsActive.GOODSACTIVE_SECONDS;
                        break;
                    default:
                        return true;
                }
                switchFlag = activityModelMapper.pluginSwitch(storeId, pluginType) > 0;
            }
            return switchFlag;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("插件活动是否开启 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "activityPlugin");
        }
    }

    @Override
    public boolean sendSms(int storeId, String phone, int type, int smsType, Map<String, String> smsParmaMap) throws LaiKeAPIException
    {
        try
        {
            StringBuilder redistKey = new StringBuilder();
            //模板名称
            String template;
            //验证码超时时间
            int outTime = 60;
            //注册、登录验证码、通用模板 为五分钟
            /*if (GloabConst.VcodeCategory.LOGIN_CODE == smsType || GloabConst.VcodeCategory.REGISTER_CODE == smsType || GloabConst.VcodeCategory.CURRENCY_CODE == smsType)
            {
                outTime *= 5;
            }*/
            String header = RedisDataTool.getSmsHeade(smsType);

            //区号
            String cpc = MapUtils.getString(smsParmaMap, "cpc","86");


            //检查改手机号是否有发送给短信
            redistKey.append(header);
            redistKey.append(phone);
            if (redisUtil.hasKey(redistKey.toString()) && type == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZPFQSHZS, "操作频繁,请稍后重试", "sendSms");
            }

            //如果区号不是国内或港澳台就用国际短信模板
            int international = (cpc.equals("86") || cpc.equals("852")) ? 0 : 1;

            //验证短信模板是否存在
            MessageModel messageModel = new MessageModel();
            messageModel.setInternational(international);
            messageModel.setType1(smsType);
            messageModel.setStore_id(storeId);
            messageModel = messageModelMapper.selectOne(messageModel);
            //给定场景的短信模板为空则获取商城通用模版
            if (messageModel == null || StringUtils.isEmpty(messageModel.getTemplateCode()))
            {
                logger.error("短信模板不存在,使用通用模板");
                messageModel = new MessageModel();
                messageModel.setType1(GloabConst.VcodeCategory.CURRENCY_CODE);
                messageModel.setStore_id(storeId);
                messageModel.setInternational(international);
                messageModel = messageModelMapper.selectOne(messageModel);
                if (messageModel == null)
                {
                    logger.error("通用模板不存在");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DXMBBCZ, "短信模板不存在", "sendSms");
                }
            }
            template = messageModel.getTemplateCode();
            //获取短信服务api密钥
            MessageConfigModel messageConfigModel = new MessageConfigModel();
            messageConfigModel.setStore_id(storeId);
            messageConfigModel = messageConfigModelMapper.selectOne(messageConfigModel);
            if (messageConfigModel != null)
            {

                if (type == GloabConst.VcodeCategory.TYPE_VERIFICATION)
                {
                    phone = cpc + phone;
                    //生成x位验证码
                    String pcode = String.format("%06d", new java.security.SecureRandom().nextInt(999999));
                    logger.info("短信验证码：：：：：：：{}",pcode);
                    smsParmaMap = new HashMap<>(16);
                    smsParmaMap.put("code", pcode);
                }

                //发送短信
                SmsMessageModel smsmodel = MobileUtils.sendSms(phone, template, messageModel.getSignName(), messageConfigModel.getAccessKeyId(), messageConfigModel.getAccessKeySecret(), smsParmaMap);
                String          result   = "OK";
                boolean         flag     = result.equals(smsmodel.getCode());
                //测试
                /*SmsMessageModel smsmodel = new SmsMessageModel();
                smsmodel.setMessage("测试");
                boolean flag = true;*/
                if (flag)
                {
                    if (type == GloabConst.VcodeCategory.TYPE_VERIFICATION)
                    {
                        flag = redisUtil.set(redistKey.toString(), smsParmaMap.get("code"), outTime);
                        if (!flag)
                        {
                            logger.error(smsParmaMap.get("code") + " 验证码保存失败");
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZQSHZS, "网络故障,请稍后再试");
                        }
                    }
                }
                else
                {
                    logger.info("短信发送失败 参数:{},原因:{}", JSON.toJSONString(smsParmaMap), smsmodel.getMessage());
                    // TODO  短信发送失败 参数:{"code":"635728"},原因:触发天级流控Permits:5
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE, smsmodel.getMessage(), "sendSms");
                }
                return true;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTWPZDXFW, "平台未配置短信服务", "sendSms");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.info("短信发送失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sendSms");
        }
    }

    @Override
    public boolean sendValidatorSmsTemplate(int storeId, String phone, String code, String template, Map<String, String> smsParmaMap,StringBuilder stringBuilder) throws LaiKeAPIException
    {
        try
        {
            //获取短信服务api密钥
            MessageConfigModel messageConfigModel = new MessageConfigModel();
            messageConfigModel.setStore_id(storeId);
            messageConfigModel = messageConfigModelMapper.selectOne(messageConfigModel);
            if (messageConfigModel != null)
            {
                //发送短信
                SmsMessageModel smsmodel = MobileUtils.sendSms(phone, code, template, messageConfigModel.getAccessKeyId(), messageConfigModel.getAccessKeySecret(), smsParmaMap);
                String          result   = "OK";
                boolean         flag     = result.equals(smsmodel.getCode());
                if (flag)
                {
                    return true;
                }
                else
                {
                    stringBuilder.append(smsmodel.getMessage());
                    logger.debug("短信发送失败 参数:{},原因:{}", JSON.toJSONString(smsParmaMap), smsmodel.getMessage());
                    return false;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.info("短信发送失败:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sendValidatorSmsTemplate");
        }
        return false;
    }

    @Override
    public String getWeiXinToken(int storeId) throws LaiKeAPIException
    {
        String token = "";
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null && (StringUtils.isNotEmpty(configModel.getAppid()) && StringUtils.isNotEmpty(configModel.getAppsecret())))
            {
                logger.error("appId:{}, appSecret:{}", configModel.getAppid(), configModel.getAppsecret());
                token = jssdk.getAccessToken(configModel.getAppid(), configModel.getAppsecret());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.info("获取微信token 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWeiXinToken");
        }
        return token;
    }


    @Autowired
    private PubliceService publiceService;

    @Override
    public String getWeiXinAppQrcode(int storeId, int storeType, String scene, String path, int width) throws LaiKeAPIException
    {
        String imageUrl;
        try
        {
            //获取微信token
            String token = publiceService.getWeiXinToken(storeId);
            //图片名称
            String imageName = "_share_" + BuilderIDTool.getSnowflakeId();
            //把参数存储到redis scene参数必须是32位内的字符!
            String sceneKey = BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 32);
            redisUtil.set(GloabConst.RedisHeaderKey.WX_QR_KEY + sceneKey, scene, 259200);
            //装载图片参数
            Map<String, Object> imageParmaMap = new HashMap<>(16);
            imageParmaMap.put("scene", sceneKey);
            //需要发布正式版本的小程序路径
            imageParmaMap.put("page", path);
            imageParmaMap.put("env_version",env_version);
            String postDataJson = JSON.toJSONString(imageParmaMap);
            logger.debug("小程序二维码请求参数 {}", postDataJson);
            //请求url
            String apiUrl = String.format(GloabConst.WeiXinUrl.SHARE_B_GRCODE_GET_URL, token);
            logger.debug("微信小程序二维码apiUrl {}", apiUrl);
            try (InputStream stream = HttpUtils.postFile(apiUrl, postDataJson))
            {
                List<MultipartFile> fileList = new ArrayList<>();
                MultipartFile       file     = new MockMultipartFile(imageName, imageName + "." + GloabConst.UploadConfigConst.IMG_PNG, MediaType.IMAGE_PNG_VALUE, stream);
                fileList.add(file);
                List<String> imageUrlList = publiceService.uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST, storeType, storeId);
                imageUrl = imageUrlList.get(0);
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
    public String getStoreQrcode(int storeId, int storeType, String qrCodeUrl) throws LaiKeAPIException
    {
        InputStream stream = null;
        String      imageUrl;
        try
        {
            //图片名称
            String              fileName = BuilderIDTool.getGuid() + "." + GloabConst.UploadConfigConst.IMG_JPEG;
            List<MultipartFile> fileList = new ArrayList<>();

            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(storeId);
            configModel = configModelMapper.selectOne(configModel);
            String logUrl = "";
            if (configModel != null)
            {
                logUrl = getImgPath(configModel.getLogo(), storeId);
            }
            File rqcodeFile = ImageTool.builderQrcode(logUrl, null, qrCodeUrl, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1 + fileName);
            if (rqcodeFile == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }
            stream = new FileInputStream(rqcodeFile);
            MultipartFile file = new MockMultipartFile(fileName, fileName, ImgUploadUtils.getUrlImgByMimeType(fileName), stream);
            fileList.add(file);
            List<String> imageUrlList = uploadImage(fileList, GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1, storeType, storeType);
            imageUrl = imageUrlList.get(0);
            logger.debug("商户logo:" + imageUrl);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.info("获取商户logo 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreQrcode");
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException io)
                {
                    logger.error("流关闭异常 ", io);
                }
            }
        }
        return imageUrl;
    }

    @Override
    public boolean addAdminRecord(int storeId, String userId, String text, int type, int source) throws LaiKeAPIException
    {
        try
        {
            return addAdminRecord(storeId, userId, text, type, source, 0);
        }
        catch (Exception e)
        {
            logger.info("添加操作记录 异常:", e);
        }
        return false;
    }

    @Override
    public boolean addAdminRecord(int storeId, String userId, String text, int type, int source, int mchId, int adminId) throws LaiKeAPIException
    {
        try
        {
            AdminRecordModel adminRecordModel = new AdminRecordModel();
            adminRecordModel.setStore_id(storeId);
            adminRecordModel.setAdmin_name(userId);
            adminRecordModel.setEvent(text);
            adminRecordModel.setType(type);
            adminRecordModel.setAdd_date(new Date());
            adminRecordModel.setSource(source);
            adminRecordModel.setMchId(mchId);
            adminRecordModel.setOperator_id(adminId);
            int count = adminRecordModelMapper.insertSelective(adminRecordModel);
            if (count > 0)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.info("添加操作记录 异常:", e);
        }
        return false;
    }

    @Override
    public boolean addAdminRecord(int storeId, String userId, String text, int type, int source, int mchId) throws LaiKeAPIException
    {
        try
        {
            AdminRecordModel adminRecordModel = new AdminRecordModel();
            adminRecordModel.setStore_id(storeId);
            adminRecordModel.setAdmin_name(userId);
            adminRecordModel.setEvent(text);
            adminRecordModel.setType(type);
            adminRecordModel.setAdd_date(new Date());
            adminRecordModel.setSource(source);
            adminRecordModel.setMchId(mchId);
            int count = adminRecordModelMapper.insertSelective(adminRecordModel);
            if (count > 0)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.info("添加操作记录 异常:", e);
        }
        return false;
    }

    @Override
    public List<DictionaryListModel> getDictionaryList(String name)
    {
        List<DictionaryListModel> retList = new ArrayList<>();
        try
        {
            DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
            dictionaryNameModel.setName(name);
            dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
            if (dictionaryNameModel != null)
            {
                int                 id                  = dictionaryNameModel.getId();
                DictionaryListModel dictionaryListModel = new DictionaryListModel();
                dictionaryListModel.setSid(id);
                retList = dictionaryListModelMapper.select(dictionaryListModel);
            }
            return retList;
        }
        catch (Exception e)
        {
            logger.info("获取字典 异常:", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getJoinCityCounty(int level, int groupId, String sel_city) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictPid(groupId);
            adminCgModel.setDistrictLevel(level);
            if (StringUtils.isNotEmpty(sel_city))
            {
                Map<String, Object> resultMap     = new HashMap<>(16);
                String[]            selCityString = sel_city.split(SplitUtils.DH);
                int[]               selCity       = new int[selCityString.length];
                for (int i = 0; i < selCityString.length; i++)
                {
                    selCity[i] = Integer.parseInt(selCityString[i]);
                }
                resultMap.put("GroupID", groupId);
                resultMap.put("G_Level", level);
                resultMap.put("selCity", selCity);
                List<Map<String, Object>> adminCgModelList = adminCgModelMapper.selectNoSheng(resultMap);
                for (Map<String, Object> adminCg : adminCgModelList)
                {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("GroupID", MapUtils.getString(adminCg, "GroupID"));
                    map.put("G_CName", MapUtils.getString(adminCg, "G_CName"));
                    resultList.add(map);
                }
            }
            else
            {
                List<AdminCgModel> adminCgModelList = adminCgModelMapper.select(adminCgModel);
                for (AdminCgModel adminCg : adminCgModelList)
                {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("GroupID", adminCg.getId());
                    map.put("G_CName", adminCg.getDistrictName());
                    resultList.add(map);
                }
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("省市级联异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getJoinCityCounty");
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getJoinCityCounty(int level, int groupId) throws LaiKeAPIException
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictPid(groupId);
            adminCgModel.setDistrictLevel(level);
            List<AdminCgModel> adminCgModelList = adminCgModelMapper.select(adminCgModel);
            for (AdminCgModel adminCg : adminCgModelList)
            {
                Map<String, Object> map = new HashMap<>(2);
                map.put("GroupID", adminCg.getId());
                map.put("G_CName", adminCg.getDistrictName());
                resultList.add(map);
            }


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("省市级联异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getJoinCityCounty");
        }
        return resultList;
    }

    @Override
    public void messageUpdate(int storeId, int mchId, int type, String parma, MessageLoggingModal messageLoggingUpdate) throws LaiKeAPIException
    {
        try
        {
            MessageLoggingModal messageLoggingModal = new MessageLoggingModal();
            messageLoggingModal.setStore_id(storeId);
            messageLoggingModal.setMch_id(mchId);
            messageLoggingModal.setParameter(parma);
            messageLoggingModal.setType(type);
            messageLoggingModal = messageLoggingModalMapper.selectOne(messageLoggingModal);
            if (messageLoggingModal != null)
            {
                messageLoggingUpdate.setId(messageLoggingModal.getId());
                int row = messageLoggingModalMapper.updateByPrimaryKeySelective(messageLoggingUpdate);
                if (row < 1)
                {
                    logger.debug("后台消息修改失败 参数:{}", JSON.toJSONString(messageLoggingUpdate));
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改后台消息表信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "messageUpdate");
        }
    }

    @Override
    public boolean withdrawals(Withdrawals1Vo vo, User user) throws LaiKeAPIException
    {
        try
        {
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (StringUtils.isEmpty(vo.getWithdrawStatus()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "参数错误");
            }
            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setIs_mch(0);
            int count;
            //当前余额
            BigDecimal accountMoney;
            //可提现金额
            BigDecimal cashableMoney;
            //提现金额
            BigDecimal amoney = new BigDecimal(vo.getAmoney());
            //最低提现金额
            BigDecimal minCharge;
            //最大体现金额
            BigDecimal maxCharge;
            //提现手续费比例
            BigDecimal serviceCharge;
            //实际手续费
            BigDecimal costAmt = BigDecimal.ZERO;
            //用户余额提现详情id
            Integer detailsId = null;
            //提现类型 1银行卡  2微信余额
            Integer withdrawStatus = vo.getWithdrawStatus();
            //提现单号
            String txSno = "";
            //提示
            String event = "%s%s申请提现:%s元";
            //提现方式
            String withdrawalMethod = "";
            if (withdrawStatus.equals(WithdrawModel.WITHDRAW_STATUS.YHK))
            {
                BankCardModel bankCardModel = bankCardModelMapper.selectByPrimaryKey(vo.getBankId());
                String        str           = "%s 尾号(%s)";
                String        bankNum       = bankCardModel.getBank_card_number();
                withdrawalMethod = String.format(str, bankCardModel.getBank_name(), bankNum.substring(bankNum.length() - 4));
                withdrawModel.setWithdrawStatus(WithdrawModel.WITHDRAW_STATUS.YHK);
            }
            else if (withdrawStatus.equals(WithdrawModel.WITHDRAW_STATUS.WX))
            {
                //判断用户是否绑定微信
                if (StringUtils.isEmpty(user.getWx_id()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXJXWXBD, "请先进行微信绑定");
                }
                //微信零钱提现金额大于2000须有用户的真实姓名
                if (amoney.compareTo(new BigDecimal(2000)) >= 0)
                {
                    if (StringUtils.isEmpty(vo.getUserName()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QTXZSXM, "提现金额大于2000请填写真实姓名");
                    }
                }
                vo.setBankId(null);
                withdrawModel.setRealname(vo.getUserName());
                withdrawalMethod = "微信零钱";
                withdrawModel.setWithdrawStatus(WithdrawModel.WITHDRAW_STATUS.WX);
            }
            else if (withdrawStatus.equals(WithdrawModel.WITHDRAW_STATUS.PAYPAL))
            {
                vo.setBankId(null);
                withdrawModel.setRealname(vo.getUserName());
                withdrawalMethod = "贝宝余额";
                withdrawModel.setWithdrawStatus(WithdrawModel.WITHDRAW_STATUS.PAYPAL);
                withdrawModel.setEmail(vo.getEmail());
            }
            else if (withdrawStatus.equals(WithdrawModel.WITHDRAW_STATUS.STRIPE))
            {
                vo.setBankId(null);
                withdrawModel.setRealname(vo.getUserName());
                withdrawalMethod = "stripe余额";
                withdrawModel.setWithdrawStatus(WithdrawModel.WITHDRAW_STATUS.STRIPE);
                withdrawModel.setStripeAccountId(vo.getStripeAccountId());
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "参数错误");
            }
            if (StringUtils.isNotEmpty(vo.getPluginType()))
            {
                //插件提现处理
                if (vo.getPluginType().equals(DictionaryConst.OrdersType.ORDERS_HEADER_FX))
                {
                    //校验数据
                    BigDecimal txAmt = withdrawalsVerification(user, amoney);
                    //开始提现 更新佣金
                    count = userDistributionModelMapper.updateCommission(vo.getStoreId(), user.getUser_id(), amoney);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJGXSB, "佣金更新失败");
                    }
                    //记录提现操作
                    DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
                    distributionWithdrawModel.setStore_id(vo.getStoreId());
                    distributionWithdrawModel.setUser_id(user.getUser_id());
                    distributionWithdrawModel.setName(user.getUser_name());
                    distributionWithdrawModel.setMobile(user.getMobile());
                    distributionWithdrawModel.setBank_id(vo.getBankId());
                    distributionWithdrawModel.setZ_money(txAmt);
                    distributionWithdrawModel.setMoney(amoney);
                    distributionWithdrawModel.setS_charge(amoney.subtract(txAmt));
                    distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
                    distributionWithdrawModel.setIs_mch(0);
                    distributionWithdrawModel.setWithdrawStatus(vo.getWithdrawStatus());
                    distributionWithdrawModel.setWxStatus(DistributionWithdrawModel.WX_STATUS.WAIT_PAY);
                    distributionWithdrawModel.setAdd_date(new Date());
                    if (StringUtils.isNotEmpty(vo.getUserName()))
                    {
                        distributionWithdrawModel.setRealname(vo.getUserName());
                    }
                    count = distributionWithdrawModelMapper.insertSelective(distributionWithdrawModel);
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
            }
            else
            {
                //记录
                RecordModel recordSave = new RecordModel();
                recordSave.setStore_id(vo.getStoreId());
                recordSave.setUser_id(user.getUser_id());
                //店铺和用户每天只能提现一次
                String startTime = DateUtil.getStartOfDay(new Date());
                String endTime   = DateUtil.getEndOfDay(new Date());

                Map<String, Object> parmaMap = new HashMap<>(16);//提现验证规则
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("user_id", user.getUser_id());
                parmaMap.put("is_mch", vo.getShopId() != null ? 1 : 0);
                parmaMap.put("status", "0");
                count = withdrawModelMapper.countDynamic(parmaMap);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDSBSQHWSHQSHZS, "您的上笔申请还未审核，请稍后再试");
                }
                //提现申请是否已达上限 每天可以提现一次
                parmaMap.put("startTime", startTime);
                parmaMap.put("endTime", endTime);
                parmaMap.put("status", "1");
                count = withdrawModelMapper.countDynamic(parmaMap);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSQCSYDSX, "提现申请次数已达上限！");
                }
                if (vo.getShopId() != null)
                {
                    //判断店铺设置是否在提现日期
                    String         day            = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.D);
                    Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
                    MchConfigModel mchConfigModel = new MchConfigModel();
                    mchConfigModel.setStore_id(vo.getStoreId());
                    mchConfigModel.setMch_id(storeMchId);
                    mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                    if (mchConfigModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WHQDDPPZXX, "未获取到店铺配置信息");
                    }
                    if (mchConfigModel != null && mchConfigModel.getWithdrawal_time_open() > 0)
                    {
                        if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME) && !day.equals(mchConfigModel.getWithdrawal_time()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "当前未到平台指定提现时间");
                        }
                        else if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME_PERIOD))
                        {
                            String withdrawalTimeOne = mchConfigModel.getWithdrawal_time().split(SplitUtils.HG)[0];
                            String withdrawalTimeTwo = mchConfigModel.getWithdrawal_time().split(SplitUtils.HG)[1];
                            if (day.compareTo(withdrawalTimeOne) < 0 || day.compareTo(withdrawalTimeTwo) > 0)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "当前未到平台指定提现时间段");
                            }
                        }
                    }
                    txSno = this.createTxSno("MCH");
                    logger.debug("店主{}进行提现", user.getUser_id());
                    recordSave.setIs_mch(1);
                    withdrawModel.setIs_mch(1);
                    //获取店铺信息
                    MchModel mchModel = new MchModel();
                    mchModel.setStore_id(vo.getStoreId());
                    mchModel.setUser_id(user.getUser_id());
                    mchModel.setReview_status(DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
                    mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                    mchModel = mchModelMapper.selectOne(mchModel);
                    if (mchModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
                    }
                    int mchId = mchModel.getId();

                    accountMoney = mchModel.getAccount_money();
                    cashableMoney = mchModel.getCashable_money();
                    if (mchId != vo.getShopId())
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHYDPBPP, "用户与店铺不匹配");
                    }

                    minCharge = mchConfigModel.getMin_charge();
                    maxCharge = mchConfigModel.getMax_charge();
                    serviceCharge = mchConfigModel.getService_charge();
                    //金额验证
                    validateWithdrawalsAmt(amoney, minCharge, maxCharge);
                    //提现金额不能大于最大可提现金额
                    if (amoney.compareTo(cashableMoney) > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEBNDYKTXJE, "提现金额不能大于可提现金额！");
                    }
                    if (serviceCharge.compareTo(BigDecimal.ZERO) > 0)
                    {
                        //计算手续费
                        costAmt = amoney.multiply(serviceCharge);
                    }
                    //扣减店铺可提现金额
                    parmaMap.clear();
                    parmaMap.put("t_money", amoney);
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("mch_id", mchModel.getId());
                    count = mchModelMapper.withdrawal(parmaMap);
                    if (count < 1)
                    {
                        logger.warn("店铺金额扣减失败 mchModelMapper.withdrawal 参数:{}", JSON.toJSONString(parmaMap));
                        return false;
                    }
                    //提现成功
                    event = String.format(event, "店主", user.getUser_id(), amoney);
                }
                else
                {
                    logger.debug("用户{}进行提现", user.getUser_id());
                    txSno = this.createTxSno("USE");
                    recordSave.setIs_mch(0);
                    //获取钱包信息
                    FinanceConfigModel financeConfigModel = new FinanceConfigModel();
                    financeConfigModel.setStore_id(vo.getStoreId());
                    financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
                    if (financeConfigModel == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJYC, "数据异常!！");
                    }
                    minCharge = financeConfigModel.getMin_amount();
                    maxCharge = financeConfigModel.getMax_amount();
                    serviceCharge = financeConfigModel.getService_charge();
                    //提现手续费
                    String withdrawalFees = "";
                    if (serviceCharge.compareTo(BigDecimal.ZERO) > 0)
                    {
                        //实际手续费
                        costAmt = amoney.multiply(serviceCharge).setScale(2, BigDecimal.ROUND_HALF_UP);
                        withdrawalFees = costAmt + "(" + serviceCharge.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString() + "%)";
                    }
                    else
                    {
                        withdrawalFees = costAmt.toString();
                    }
                    //获取会员信息
                    User client = userBaseMapper.selectByPrimaryKey(user.getId());
                    accountMoney = client.getMoney();
                    //会员提现不能提高出自己当前余额
                    if (amoney.compareTo(accountMoney) > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEDYXYJE, "提现金额大于现有金额!！");
                    }
                    //金额验证
                    validateWithdrawalsAmt(amoney, minCharge, maxCharge);
                    //扣除余额
                    count = userBaseMapper.rechargeUserPrice(user.getId(), amoney.negate());
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
                    }
                    //提现成功
                    event = String.format(event, "", user.getUser_id(), amoney);
                    user = userBaseMapper.selectByPrimaryKey(user.getId());
                    //添加提现记录详情
                    RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                    recordDetailsModel.setStore_id(vo.getStoreId());
                    recordDetailsModel.setMoney(amoney.negate().abs());
                    recordDetailsModel.setUserMoney(user.getMoney());
                    recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.EXPENDITURE);
                    recordDetailsModel.setType(RecordDetailsModel.type.WITHDRAWAL_OF_BALANCE);
                    recordDetailsModel.setsNo(txSno);
                    recordDetailsModel.setWithdrawalFees(withdrawalFees);
                    recordDetailsModel.setWithdrawalMethod(withdrawalMethod);
                    recordDetailsModel.setRecordTime(new Date());
                    recordDetailsModel.setAddTime(new Date());

                    Map<String, Object> userCurrecyMap = currencyStoreModelMapper.getCurrencyInfo(vo.getStoreId(), user.getPreferred_currency());
                    recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code"));
                    recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol"));
                    recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate"));

                    recordDetailsModelMapper.insert(recordDetailsModel);
                    recordSave.setDetails_id(recordDetailsModel.getId());

                }
                withdrawModel.setStore_id(vo.getStoreId());
                withdrawModel.setUser_id(user.getUser_id());
                withdrawModel.setName(user.getUser_name());
                withdrawModel.setMobile(vo.getMobile());
                withdrawModel.setBank_id(vo.getBankId());
                withdrawModel.setMoney(amoney);
                withdrawModel.setZ_money(accountMoney);
                withdrawModel.setS_charge(costAmt);
                withdrawModel.setStatus("0");
                withdrawModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS + "");
                withdrawModel.setTxsno(txSno);
                withdrawModel.setAdd_date(new Date());
                if (vo.getId() == null)
                {
                    //添加一条提现记录
                    count = withdrawModelMapper.insert(withdrawModel);
                    if (count < 1)
                    {
                        logger.warn("提现记录添加失败 withdrawModelMapper.insert 参数:{}", JSON.toJSONString(withdrawModel));
                        return false;
                    }
                }
                else
                {
                    //修改提现列表金额
                    withdrawModel.setId(vo.getId());
                    count = withdrawModelMapper.updateByPrimaryKeySelective(withdrawModel);
                    if (count < 1)
                    {
                        logger.warn("提现列表修改金额 参数:{}", JSON.toJSONString(withdrawModel));
                        return false;
                    }
                }

                recordSave.setMoney(amoney);
                recordSave.setOldmoney(user.getMoney());
                recordSave.setEvent(event);
                recordSave.setType(2);
                recordSave.setAdd_date(new Date());
                count = recordModelMapper.insertSelective(recordSave);
                if (count < 1)
                {
                    logger.warn("提现记录失败 recordModelMapper.insert 参数:" + JSON.toJSONString(recordSave));
                    return false;
                }
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请提现 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
        }
    }

    @Override
    public boolean jurisdiction(int storeId, AdminModel admin, String url) throws LaiKeAPIException
    {
        try
        {
            if (admin != null && admin.getType() != 1)
            {
                int count = roleMenuModelMapper.countButtonRole(admin.getRole(), url);
                if (count > 0)
                {
                    return true;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取后台权限按钮 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "jurisdiction");
        }

        return false;
    }

    /**
     * 提现金额验证
     *
     * @param amoney    -
     * @param minCharge -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/3/8 16:36
     */
    private void validateWithdrawalsAmt(BigDecimal amoney, BigDecimal minCharge, BigDecimal maxCharge) throws LaiKeAPIException
    {
        try
        {
            if (amoney.floatValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEBNXYDY, "提现金额不能小于等于0");
            }
            //提现金额是否小于最低提现金额
            if (minCharge.compareTo(amoney) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEBNDYZDTXJE, "提现金额不能低于最低提现金额！");
            }
            if (amoney.compareTo(maxCharge) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEDYZDTXJE, "提现金额大于最大提现金额！");
            }
        }
        catch (LaiKeAPIException l)
        {
            l.printStackTrace();
            logger.error("提现金额验证 异常 " + l.getMessage());
            throw l;
        }
    }

    /**
     * 删除图片和对应的文件
     */
    public void delFileRecoderModel(FilesRecordModel filesRecordOld) throws LaiKeAPIException
    {
        int uploadType = 0;
        try
        {
            if (filesRecordOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TPBCZ, "图片不存在");
            }
            //删除数据库数据
            int row = filesRecordModelMapper.deleteByPrimaryKey(filesRecordOld.getId());
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WJSJSCSB, "文件数据删除失败");
            }
            //是否有目录
            ImgGroupModel imgGroupModel = imgGroupModelMapper.selectByPrimaryKey(filesRecordOld.getGroup());

            DelFilesVo delFilesVo = new DelFilesVo();
            delFilesVo.setStoreId(Integer.parseInt(filesRecordOld.getStore_id()));
            delFilesVo.setStoreType(Integer.parseInt(filesRecordOld.getStore_type()));
            delFilesVo.setUploadType(filesRecordOld.getUpload_mode());
            delFilesVo.setFileName(filesRecordOld.getImage_name());
            delFilesVo.setAddTime(filesRecordOld.getAdd_time());
            if (imgGroupModel != null)
            {
                delFilesVo.setCatalogue(imgGroupModel.getName());
            }
            UploadImagModel imagModel = new UploadImagModel();
            //oss参数
            if (GloabConst.UploadConfigConst.IMG_UPLOAD_OSS.equals(filesRecordOld.getUpload_mode()))
            {
                //从数据库获取上传配置key
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_OSS);
                List<UploadConfigModel> uploadConfigs = uploadConfigModelMapper.select(uploadConfigModel);
                //装载参数
                imagModel.setUploadConfigs(uploadConfigs);
                uploadType = 1;
            }
            else if (GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO.equals(filesRecordOld.getUpload_mode()))
            {
                //从数据库获取上传配置key
                UploadConfigModel uploadConfigModel = new UploadConfigModel();
                uploadConfigModel.setUpserver(GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO);
                List<UploadConfigModel> uploadConfigs = uploadConfigModelMapper.select(uploadConfigModel);
                //装载参数
                imagModel.setUploadConfigs(uploadConfigs);
            }
            delFilesVo.setUploadImagModel(imagModel);
            //执行删除
            ImgUploadUtils imgUploadUtils = new ImgUploadUtils();
            imgUploadUtils.imgDel(delFilesVo);
            //数据库删除
            filesRecordModelMapper.delFileRecordByName(filesRecordOld.getImage_name());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除验证码图片异常：{}", e.getMessage());
            throw new LaiKeAPIException(uploadType == 0 ? ErrorCode.BizErrorCode.ERROR_CODE_SCSB : ErrorCode.BizErrorCode.ERROR_CODE_OOSWSCQX, "删除失败", "delFileRecoderModel");
        }
    }

    /**
     * 验证图像验证码
     *
     * @param token -
     * @param code  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/16 17:08
     */
    public void validateImgCode(String token, String code) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isNotEmpty(code))
            {
                Object imgUrl = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_CODE_KEY + token);
                token += code.toLowerCase();
                Object obj = redisUtil.get(token);
                if (obj != null && imgUrl != null)
                {
                    String url = obj.toString();
                    //从redis中删除code
                    redisUtil.del(token);
                    redisUtil.del(GloabConst.RedisHeaderKey.LOGIN_CODE_KEY + token);
                    return;
                }
            }
        }
        catch (Exception e)
        {
            logger.error("验证图像验证码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "validateImgCode");
        }
        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXYZMBZQ, "图形验证码不正确");
    }

    @Override
    public boolean addAdminRecord(int storeId, String text, int type, String token) throws LaiKeAPIException
    {

        try
        {

            int              count            = 0;
            AdminRecordModel adminRecordModel = new AdminRecordModel();
            adminRecordModel.setStore_id(storeId);
            adminRecordModel.setEvent(text);
            adminRecordModel.setType(type);
            adminRecordModel.setAdd_date(new Date());
            //判读是哪个平台
            // 管理后台
            Object     cacheValue = redisUtil.get(com.laiketui.core.lktconst.GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN + token);
            AdminModel adminModel = new AdminModel();
            User       user       = new User();
            if (cacheValue != null)
            {
                adminModel = JSON.parseObject(cacheValue.toString(), AdminModel.class);
                adminRecordModel.setAdmin_name(adminModel.getName());
                adminRecordModel.setSource(AdminRecordModel.Source.PC_PLATFORM);
                adminRecordModel.setMchId(-1);
                adminRecordModel.setOperator_id(adminModel.getId());
                count = adminRecordModelMapper.insertSelective(adminRecordModel);
                if (count > 0)
                {
                    return true;
                }
            }
            else if (cacheValue == null)
            {
                //店铺后台
                cacheValue = redisUtil.get(com.laiketui.core.lktconst.GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN + token);
                if (cacheValue != null)
                {
                    user = JSON.parseObject(cacheValue.toString(), User.class);
                    adminRecordModel.setAdmin_name(user.getUser_id());
                    adminRecordModel.setSource(AdminRecordModel.Source.PC_SHOP);
                    adminRecordModel.setMchId(user.getMchId());
                    adminRecordModel.setOperator_id(user.getId());
                    count = adminRecordModelMapper.insertSelective(adminRecordModel);
                    if (count > 0)
                    {
                        return true;
                    }
                }
            }
            else if (cacheValue == null)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error("添加记录异常 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
        }
        return false;
    }

    //生成提现订单号
    public String createTxSno(String Type)
    {
        try
        {
            StringBuilder sNo    = new StringBuilder();
            Random        random = new Random();
            sNo.append(Type).append(FastDateFormat.getInstance(GloabConst.TimePattern.YMDHMS2).format(new Date())).append(random.nextInt(9))
                    .append(random.nextInt(9)).append(random.nextInt(9)).append(random.nextInt(9));
            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setTxsno(sNo.toString());

            if (withdrawModelMapper.selectCount(withdrawModel) <= 0)
            {
                return sNo.toString();
            }
            else
            {
                return createTxSno(Type);
            }
        }
        catch (Exception e)
        {
            logger.error("创建订单号 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJDDHSB, "创建订单号失败", "createOrderNo");
        }
    }

    /**
     * 分销数据验证
     *
     * @param user  -
     * @param txAmt -
     * @return BigDecimal - 最终可以提现的金额
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/9 17:21
     */
    private BigDecimal withdrawalsVerification(User user, BigDecimal txAmt) throws LaiKeAPIException
    {
        try
        {
            int count;
            //获取金额单位
            FinanceConfigModel financeConfigModel = new FinanceConfigModel();
            financeConfigModel.setStore_id(user.getStore_id());
            financeConfigModel = financeConfigModelMapper.selectOne(financeConfigModel);
            if (financeConfigModel == null)
            {
                financeConfigModel = new FinanceConfigModel();
                financeConfigModel.setUnit("元");
                financeConfigModel.setMin_amount(new BigDecimal("0"));
                financeConfigModel.setMax_amount(new BigDecimal("999"));
                financeConfigModel.setService_charge(new BigDecimal("0"));
            }
            //查询可提现金额
            UserDistributionModel userDistributionModel = new UserDistributionModel();
            userDistributionModel.setStore_id(user.getStore_id());
            userDistributionModel.setUser_id(user.getUser_id());
            userDistributionModel = userDistributionModelMapper.selectOne1(userDistributionModel);
            if (userDistributionModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MYKTXJE, "没有可提现金额");
            }
            //提现金额是否>0或者大于可提现金额
            if (txAmt.doubleValue() <= 0 || txAmt.doubleValue() > userDistributionModel.getTx_commission().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QKJESFXYHDYHDYXYJE, "取款金额是否小于或等于0或大于现有金额");
            }
            //提现金额小于最小提现金额
            if (txAmt.doubleValue() < financeConfigModel.getMin_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYZXTXJE, "提现金额小于最小提现金额");
            }
            // 提现金额大于最大提现金额
            if (txAmt.doubleValue() > financeConfigModel.getMax_amount().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEDYZDTXJE, "提现金额大于最大提现金额");
            }
            // 提现金额小于等于手续费
            if (txAmt.doubleValue() <= financeConfigModel.getService_charge().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJEXYDYSXF, "提现金额小于等于手续费");
            }
            //查询用户之前是否有未完成的提现申请
            DistributionWithdrawModel distributionWithdrawModel = new DistributionWithdrawModel();
            distributionWithdrawModel.setStore_id(user.getStore_id());
            distributionWithdrawModel.setUser_id(user.getUser_id());
            distributionWithdrawModel.setStatus(DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_UNDER);
            count = distributionWithdrawModelMapper.selectCount(distributionWithdrawModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NHYYBTXZZSHZ, "您还有一笔提现正在审核中");
            }
            //一天只允许提现一次
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", user.getStore_id());
            parmaMap.put("user_id", user.getUser_id());
            parmaMap.put("status", DistributionWithdrawModel.WITHDRAWMODEL_EXAMIN_PASS);
            parmaMap.put("startDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            parmaMap.put("endDate", DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD));
            count = distributionWithdrawModelMapper.countDynamic(parmaMap);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YTZYXTXYC, "一天只允许提现一次");
            }
            //获取手续费
            BigDecimal serverAmt = txAmt.multiply(financeConfigModel.getService_charge());
            //四舍五入保留两位小数
            serverAmt = serverAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
            //扣除手续费
            txAmt = txAmt.subtract(serverAmt);
            return txAmt;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("提现数据验证 异常:" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawals");
        }
    }

    private static String generateWithRandom(int length) {
        if (length < 1) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ);
        }
        Random random = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    @Override
    public void changeCurrency(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            User   user     = null;
            String accessId = vo.getAccessId();
            if (!StringUtils.isEmpty(accessId))
            {
                user = RedisDataTool.getLktUser(vo.getAccessId(), redisUtil);
                //获取用户信息
                if (user != null)
                {
                    int uid   = user.getId();
                    int count = userBaseMapper.changePreferredCurrency(uid, vo.getCurrency_id());

                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "changeCurrency");
                    }

                    //刷新用户信息
                    User userInfo = new User();
                    userInfo.setId(uid);
                    userInfo = userBaseMapper.selectOne(userInfo);
                    RedisDataTool.refreshRedisUserCache(accessId, userInfo, redisUtil);

                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("更改币种异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "changeCurrency");
        }

    }

    @Override
    public CurrencyStoreModel fetchUserCurrencyInfo(CurrencyStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            User               user               = null;
            String             accessId           = vo.getAccessId();
            CurrencyStoreModel currencyStoreModel = null;
            if (!StringUtils.isEmpty(accessId))
            {
                user = RedisDataTool.getLktUser(vo.getAccessId(), redisUtil);
                Integer currencyId = user.getPreferred_currency();
                currencyStoreModel = new CurrencyStoreModel();
                currencyStoreModel.setCurrency_id(currencyId);
                currencyStoreModel.setStore_id(vo.getStoreId());
                currencyStoreModel = currencyStoreModelMapper.selectOne(currencyStoreModel);
                if (currencyStoreModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HBBCZ, "货币不存在", "fetchUserCurrencyInfo");
                }
                logger.info("货币信息:{}", JSON.toJSONString(currencyStoreModel));
                return currencyStoreModel;
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WDL, "未登录", "fetchUserCurrencyInfo");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取币种最新信息异常,{}" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "fetchUserCurrencyInfo");
        }
    }

    @Override
    public void checkIsSensitiveWords(int storeId, String keyword) throws LaiKeAPIException {
        try {
            List<String> wordList;
            if (redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + storeId)) {
                wordList = DataUtils.cast(redisUtil.get(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + storeId));
            } else {
                wordList = sensitiveWordsModelMapper.getWordList(storeId);
                redisUtil.set(GloabConst.RedisHeaderKey.LKT_SENSITIVE_WORDS + storeId, wordList);
            }
            if (!CollectionUtils.isEmpty(wordList)) {
                if (wordList.contains(keyword)) {
                    throw new LaiKeAPIException("该关键字属于敏感词");
                }
            }
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "checkIsSensitiveWords");
        }
    }

    @Override
    public String delBindUnit(Integer diyPageId, Integer diyId, String unit, String linkKey, Integer bindId, String diyValue, boolean isUnbind) throws LaiKeAPIException {
        {
            String json = null;
            //清空diy绑定页面链接
            if (StringUtils.isNotEmpty(diyValue) && isUnbind)
            {
                //diy页面数据
                JSONArray jsonArray = JSON.parseArray(diyValue);
                boolean modified = false;
                for (Object object : jsonArray)
                {
                    JSONObject jsonObject = (JSONObject) object;
                    JSONObject defaultArray = jsonObject.getJSONObject("defaultArray");
                    if (defaultArray != null)
                    {
                        //获取绑定的大组件value
                        JSONObject linkValue = defaultArray.getJSONObject(linkKey);
                        if (Objects.isNull(linkValue)) continue;
                        JSONObject swiperConfig = linkValue.getJSONObject("swiperConfig");
                        if (Objects.isNull(swiperConfig)) continue;
                        //对应的子组件
                        JSONArray list = swiperConfig.getJSONArray("list");
                        if (Objects.isNull(list)) continue;
                        for (Object o : list)
                        {
                            JSONObject listValue = (JSONObject) o;
                            JSONArray infoKey = listValue.getJSONArray("info");
                            for (Object infoValues : infoKey)
                            {
                                JSONObject infoValue = (JSONObject) infoValues;
                                //unit表示：链接地址对应的平级，匹配则清空
                                String unitKey = infoValue.getString("unit");
                                if (StringUtils.isNotEmpty(unitKey) && unitKey.equals(unit))
                                {
                                    infoValue.put("value", "");
                                    infoValue.put("lodValue", "");
                                    modified = true;
                                }
                            }
                        }
                    }
                }
                if (modified)
                {
                    json = jsonArray.toJSONString();
                }
            }

            int i = diyPageBindModelMapper.deleteByPrimaryKey(bindId);
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB,"删除失败");
            }
            //查询当前页面是否绑定了其他主题,为0修改状态为未启用
            i = diyPageBindModelMapper.countByDiyId(diyPageId);
            if (i == 0)
            {
                DiyPageModel diyPageModel = new DiyPageModel();
                diyPageModel.setId(diyPageId);
                diyPageModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_NO);
                diyPageModelMapper.updateByPrimaryKeySelective(diyPageModel);
            }
            return json;
        }
    }

    @Override
    public Map<String,Object> getEmailConfig(MainVo vo)
    {
        Map<String,Object> map = new HashMap<>();
        try
        {
            String config = "";
            Map<String,Object> emailConfig = configModelMapper.getEmailConfig(vo.getStoreId());
            if (MapUtils.isNotEmpty(emailConfig) && StringUtils.isNotEmpty(MapUtils.getString(emailConfig,"mail_config")))
            {
                config = MapUtils.getString(emailConfig,"mail_config");
                config = maskEmailConfigSecrets(config);
            }
            map.put("mail_config",config);
            map.put("id",MapUtils.getInteger(emailConfig,"id"));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getEmailConfig");
        }
        return map;
    }

    private String maskEmailConfigSecrets(String config) throws LaiKeAPIException
    {
        if (StringUtils.isEmpty(config))
        {
            return config;
        }
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(config);
            if (jsonObject == null || jsonObject.isEmpty())
            {
                return config;
            }
            maskSensitiveConfig(jsonObject);
            return jsonObject.toJSONString();
        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.warn("邮箱配置脱敏失败，返回原配置: {}", e.getMessage());
            return config;
        }
    }

    @SuppressWarnings("unchecked")
    private void maskSensitiveConfig(Map<String, Object> config) throws LaiKeAPIException
    {
        if (config == null || config.isEmpty())
        {
            return;
        }
        for (Map.Entry<String, Object> entry : config.entrySet())
        {
            String key   = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map)
            {
                maskSensitiveConfig((Map<String, Object>) value);
                continue;
            }
            if (value instanceof List)
            {
                maskSensitiveConfigList((List<Object>) value, key);
                continue;
            }
            if (value instanceof String && isSensitiveConfigKey(key))
            {
                entry.setValue(StringUtils.desensitizedSecret((String) value));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void maskSensitiveConfigList(List<Object> list, String parentKey) throws LaiKeAPIException
    {
        if (list == null || list.isEmpty())
        {
            return;
        }
        for (int i = 0; i < list.size(); i++)
        {
            Object item = list.get(i);
            if (item instanceof Map)
            {
                maskSensitiveConfig((Map<String, Object>) item);
                continue;
            }
            if (item instanceof List)
            {
                maskSensitiveConfigList((List<Object>) item, parentKey);
                continue;
            }
            if (item instanceof String && isSensitiveConfigKey(parentKey))
            {
                list.set(i, StringUtils.desensitizedSecret((String) item));
            }
        }
    }

    private boolean isSensitiveConfigKey(String key)
    {
        if (StringUtils.isEmpty(key))
        {
            return false;
        }
        String lower = key.toLowerCase(Locale.ROOT);
        return lower.contains("key")
                || lower.contains("secret")
                || lower.contains("private")
                || lower.contains("cert")
                || lower.contains("pem")
                || lower.contains("password")
                || lower.contains("token");
    }

    @Override
    public void addOrUpdateEmailConfig(MainVo vo, String config,Integer id) throws LaiKeAPIException
    {
        try
        {
            int i = 0;
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(configModel))
            {
                configModel.setMail_config(config);
                configModel.setId(configModel.getId());
                i = configModelMapper.updateByPrimaryKeySelective(configModel);
            }

            if (i == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB,"添加失败","addOrUpdateEmailConfig");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addOrUpdateEmailConfig");
        }
    }

    @Override
    public Map<String, Object> getSystemIconAndName(MainVo vo) throws LaiKeAPIException
    {
        Map<String,Object> result = new HashMap<>();
        try
        {
            String store_name = "";
            String html_icon = "";
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(configModel))
            {
                if (StringUtils.isNotEmpty(configModel.getStore_name()))
                {
                    store_name = configModel.getStore_name();
                }
                if (StringUtils.isNotEmpty(configModel.getHtml_icon()))
                {
                    html_icon = configModel.getHtml_icon();
                }
            }
            result.put("store_name",store_name);
            result.put("html_icon",html_icon);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSystemIconAndName");
        }
        return result;
    }

    @Override
    public void sendEmail(String email, MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> emailConfig = configModelMapper.getEmailConfig(vo.getStoreId());
        String mailConfig = MapUtils.getString(emailConfig, "mail_config");
        if (StringUtils.isEmpty(mailConfig))
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXPZYXFW,"请先配置邮箱服务");
        }
        JSONObject config = JSONObject.parseObject(mailConfig);
        String host = config.getString("host");
        String username = config.getString("username");
        String password = config.getString("password");
        Boolean sslEnable = config.getBoolean("sslEnable");
        String store_name = MapUtils.getString(config, "store_name", "来客推");

        mailUtils.sendEmailCode(host,username,password,sslEnable,email,store_name);
    }

    /**
     * 批量获取图片路径（性能最佳）
     * @param imgNames  图片名集合
     * @param storeId   商城ID
     * @return Map<imgName, url>
     */
    @Override
    public Map<String, String> batchGetImgPath(Collection<String> imgNames, Integer storeId) throws LaiKeAPIException {
        if (storeId == null || CollectionUtils.isEmpty(imgNames)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>(imgNames.size());

        // 1. 批量从 Redis 获取（miss 的再查库）
        List<String> missKeys = new ArrayList<>();
        Map<String, String> redisBatch = redisUtil.mget(imgNames.stream()
                .map(name -> "img_path:" + storeId + ":" + name)
                .collect(Collectors.toList()));

        for (String imgName : imgNames) {
            String key = "img_path:" + storeId + ":" + imgName;
            String url = redisBatch.get(key);
            if (StringUtils.isNotEmpty(url)) {
                result.put(imgName, url);
            } else {
                missKeys.add(imgName);
            }
        }

        // 2. 没有命中的去查库
        if (!missKeys.isEmpty()) {
            // 一次性查所有缺失的记录
            List<FilesRecordModel> records = filesRecordModelMapper.batchGetByImageNames(storeId.toString(), missKeys);
            Map<String, FilesRecordModel> recordMap = records.stream()
                    .collect(Collectors.toMap(FilesRecordModel::getImage_name, r -> r));

            // 批量查配置（配置很少，可缓存或直接查）
            Set<String> uploadModes = records.stream()
                    .map(FilesRecordModel::getUpload_mode)
                    .collect(Collectors.toSet());
            List<String> uploadModesList = new ArrayList<>(uploadModes);
            List<UploadConfigModel> configs = uploadConfigModelMapper.batchGetByUpservers(uploadModesList);

            Map<String, List<UploadConfigModel>> configMap = configs.stream()
                    .collect(Collectors.groupingBy(UploadConfigModel::getUpserver));

            for (String imgName : missKeys) {
                FilesRecordModel record = recordMap.get(imgName);
                if (record == null) {
                    result.put(imgName, "");
                    continue;
                }

                List<UploadConfigModel> configList = configMap.getOrDefault(record.getUpload_mode(), Collections.emptyList());
                String url = ImgUploadUtils.getImgPath(record, configList);

                // 存 Redis
                redisUtil.set("img_path:" + storeId + ":" + imgName, url, 3600);
                result.put(imgName, url);
            }
        }

        return result;
    }

    @Autowired
    private HotKeywordsModelMapper hotKeywordsModelMapper;

    @Autowired
    private MchConfigModelMapper     mchConfigModelMapper;
    @Autowired
    private FinanceConfigModelMapper financeConfigModelMapper;
    @Autowired
    private WithdrawModelMapper      withdrawModelMapper;
    @Autowired
    private RecordModelMapper        recordModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private DictionaryNameModelMapper dictionaryNameModelMapper;

    @Autowired
    private MessageModelMapper messageModelMapper;

    @Autowired
    private MessageConfigModelMapper messageConfigModelMapper;

    @Autowired
    private AdminRecordModelMapper adminRecordModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;
    @Autowired
    private Jssdk                 jssdk;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private SubtractionConfigModalMapper  subtractionConfigModalMapper;
    @Autowired
    private DistributionConfigModelMapper distributionConfigModelMapper;
    @Autowired
    private GroupConfigModelMapper        groupConfigModelMapper;
    @Autowired
    private SecondsConfigModelMapper      secondsConfigModelMapper;

    @Autowired
    private SecondsLabelModelMapper   secondsLabelModelMapper;
    @Autowired
    private AuctionConfigModelMapper  auctionConfigModelMapper;
    @Autowired
    private IntegralConfigModelMapper integralConfigModelMapper;
    @Autowired
    private CouponConfigModelMapper   couponConfigModelMapper;
    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private MemberConfigMapper memberConfigMapper;

    @Autowired
    private MemberProModelMapper memberProModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;
    @Autowired
    private PrintSetupModelMapper printSetupModelMapper;

    @Override
    public Map<String, Object> getFrontConfig(MainVo vo, Integer type)
    {
        Map<String, Object> resultMap    = new HashMap<>(16);
        SystemInfoVo        systemInfoVo = new SystemInfoVo();
        try
        {
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (Objects.nonNull(configModel))
            {
                switch (type)
                {
                    case 1:
                        //1：基础信息配置
                        systemInfoVo.setMessageSaveDay(configModel.getMessage_day());
                        systemInfoVo.setLogon_logo(configModel.getLogon_logo());
                        systemInfoVo.setAppLoginValid(configModel.getExp_time());
                        systemInfoVo.setCopyright_information(configModel.getCopyright_information());
                        systemInfoVo.setRecord_information(configModel.getRecord_information());
                        systemInfoVo.setLink_to_landing_page(configModel.getLink_to_landing_page());
                        systemInfoVo.setAdmin_default_portrait(configModel.getAdmin_default_portrait());
                        systemInfoVo.setWatermark_url(configModel.getWatermark_url());
                        systemInfoVo.setWatermark_name(configModel.getWatermark_name());
                        //h5地址 支付后跳转使用
                        systemInfoVo.setH5_domain(configModel.getH5_domain());
                        systemInfoVo.setApp_logo(configModel.getApp_logo());
                        systemInfoVo.setHtml_icon(configModel.getHtml_icon());
                        systemInfoVo.setStore_name(configModel.getStore_name());

                        CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(vo.getStoreId());
                        systemInfoVo.setStore_logo(customerModel.getMerchant_logo());

                        break;
                    case 2:
                        //2：快递100配置
                        systemInfoVo.setExpressAddress(configModel.getExpress_address());
                        systemInfoVo.setExpressKey(configModel.getExpress_key());
                        systemInfoVo.setExpress_secret(configModel.getExpress_secret());
                        systemInfoVo.setExpress_tempId(configModel.getExpress_tempId());
                        systemInfoVo.setExpressNumber(configModel.getExpress_number());
                        systemInfoVo.setSiid(configModel.getSiid());
                        systemInfoVo.setCloud_notify(configModel.getCloud_notify());
                        systemInfoVo.setIs_open_cloud(configModel.getIs_open_cloud());
                        systemInfoVo.setTrack_secret(configModel.getTrack_secret());
                        //订单打印配置
                        PrintSetupModel printSetupModel = new PrintSetupModel();
                        printSetupModel.setStoreId(vo.getStoreId());
                        printSetupModel.setMchId(0);
                        printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
                        if (Objects.nonNull(printSetupModel))
                        {
                            systemInfoVo.setPrintName(printSetupModel.getPrintName());
                            systemInfoVo.setPrintUrl(printSetupModel.getPrintUrl());
                            systemInfoVo.setSheng(printSetupModel.getSheng());
                            systemInfoVo.setShi(printSetupModel.getShi());
                            systemInfoVo.setXian(printSetupModel.getXian());
                            systemInfoVo.setAddress(printSetupModel.getAddress());
                            systemInfoVo.setPhone(printSetupModel.getPhone());
                        }
                        break;
                    case 3:
                        //敏感词
                        HotKeywordsModel hotKeywordsModel = new HotKeywordsModel();
                        hotKeywordsModel.setStore_id(vo.getStoreId());
                        hotKeywordsModel = hotKeywordsModelMapper.selectOne(hotKeywordsModel);
                        if (Objects.nonNull(hotKeywordsModel))
                        {
                            resultMap.put("list", hotKeywordsModel);
                            return resultMap;
                        }
                        break;
                    case 4:
                        //国际化配置
                        //h5地址 支付后跳转使用
                        systemInfoVo.setDefault_lang_code(configModel.getDefault_lang_code());
                        break;
                    case 5:
                        //分账
                        systemInfoVo.setIs_accounts(Objects.isNull(configModel.getIsAccounts()) ? 0 : configModel.getIsAccounts());
                        systemInfoVo.setAccounts_set(configModel.getAccountsSet());
                        break;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取系统基本配置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFrontMsgAndLoginConfig");
        }
        resultMap.put("list", systemInfoVo);
        return resultMap;
    }

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private FlashsaleConfigModelMapper flashsaleConfigModelMapper;

    @Autowired
    private UserDistributionModelMapper userDistributionModelMapper;

    @Autowired
    private DistributionWithdrawModelMapper distributionWithdrawModelMapper;

    @Autowired
    private RoleMenuModelMapper roleMenuModelMapper;

    @Autowired
    MailUtils mailUtils;
}
