package com.laiketui.admins.admin.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.admin.AdminGoodsDubboService;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.exception.LaiKeApiWarnException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.config.PluginsModel;
import com.laiketui.domain.file.FileDeliveryModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.product.BrandClassModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.supplier.SupplierProClassModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.goods.GoodsClassVo;
import com.laiketui.domain.vo.goods.GoodsConfigureVo;
import com.laiketui.domain.vo.mch.ApplyShopVo;
import com.laiketui.domain.vo.mch.UploadMerchandiseVo;
import com.laiketui.root.common.BuilderIDTool;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 商品管理
 *
 * @author Trick
 * @date 2020/12/28 17:33
 */
@Service
public class AdminGoodsServiceImpl implements AdminGoodsDubboService
{
    private final Logger logger = LoggerFactory.getLogger(AdminIndexServiceImpl.class);


    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private SupplierProClassModelMapper supplierProClassModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;
    @Autowired
    private PublicGoodsClassService publicGoodsClassService;

    @Override
    public Map<String, Object> getClassifiedBrands(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo.getStoreId(), classId, brandId);
            resultMap.put("list", resultClassMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品类别以及品牌信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassifiedBrands");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo.getStoreId(), classId, brandId);
            resultMap.put("list", resultClassMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取商品类别 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClass");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> choiceClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo, classId, brandId);
            resultMap.put("list", resultClassMap);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("选择商品类别 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("选择商品类别 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "choiceClass");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAttributeName(MainVo vo, String attributes) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String[] attributeId = null;
            if (StringUtils.isNotEmpty(attributes))
            {
                attributeId = attributes.split(",");
            }
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            //获取所有属性
            List<Map<String, Object>> resultClassList = publicGoodsService.attribute1(vo.getStoreId(), pageModel, DataUtils.convertToList(attributeId),null);
            resultMap.put("attribute", resultClassList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取属性名称 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeName");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsConfigureList(GoodsConfigureVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取商品信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("active", DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE);
            parmaMap.put("goodsRecycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("goodsStatus", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            parmaMap.put("stockNum_gt", 0);
            parmaMap.put("is_presell", 0);
            //暂时不需要暂时虚拟商品
            parmaMap.put("commodity_type", "0");
            //获取自营店
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            parmaMap.put("mchId", mchId);

            if (vo.getCid() != null)
            {
                parmaMap.put("product_class", vo.getCid());
            }
            if (vo.getBrandId() != null)
            {
                parmaMap.put("brand_id", vo.getBrandId());
            }
            if (!StringUtils.isEmpty(vo.getProductTitle()))
            {
                parmaMap.put("product_title", vo.getProductTitle());
            }
            if (StringUtils.isNotEmpty(vo.getIsSupplier()) && vo.getIsSupplier() == 2)
            {
                parmaMap.put("isSupplier", "isSupplier");
            }
            List<Map<String, Object>> goodsList = confiGureModelMapper.getProductListLeftJoinMchDynamic(parmaMap);
            int                       total     = confiGureModelMapper.countProductListLeftJoinMchDynamic(parmaMap);
            for (Map<String, Object> goods : goodsList)
            {
                goods.put("imgurl", publiceService.getImgPath(MapUtils.getString(goods, "imgurl"), vo.getStoreId()));
                goods.put("attribute", GoodsDataUtils.getProductSkuValue(MapUtils.getString(goods, "attribute")));
            }

            resultMap.put("goodsList", goodsList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品列表-规格 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsConfigureList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAttributeValue(MainVo vo, String attributes, Integer attrId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> attributeNameMap = new HashMap<>(16);
            String[]            attributeIds     = attributes.split(",");
            if (attributeIds.length > 0)
            {
                for (String attributeId : attributeIds)
                {
                    attributeNameMap.putAll(publicGoodsService.attributeName1(vo.getStoreId(), attributeId, null,null));
                }
                resultMap.put("list", attributeNameMap);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取属性值 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeName");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> addMch(ApplyShopVo vo, String logo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(logo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBNWK, "店铺logo不能为空", "mch");
            }
            else if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCBNWK, "店铺名称不能为空");
            }
           /* else if (StringUtils.isEmpty(vo.getShop_range()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JYFWBNWK, "经营范围不能为空");
            }
            else if (StringUtils.isEmpty(vo.getRealname()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZSXMBNWK, "真实姓名不能为空");
            }
            else if (StringUtils.isEmpty(vo.getID_number()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SFZXHBNWK, "身份证信号不能为空");
            }
            else if (StringUtils.isEmpty(vo.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机号不能为空");
            }*/

            else if (StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XXDZBNWK, "详细地址不能为空");
            }

            String       headUrl = logo;
            logo = ImgUploadUtils.getUrlImgByName(logo, true);

            MchModel saveMchModel = new MchModel();
            saveMchModel.setStore_id(vo.getStoreId());
            saveMchModel.setName(vo.getName());
            saveMchModel.setShop_information(vo.getShop_information());
            saveMchModel.setLogo(logo);
            saveMchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            saveMchModel.setIs_open(MchModel.IS_OPEN_IN_BUSINESS.toString());
            saveMchModel.setBusiness_hours("00:00~23:59");
            saveMchModel.setRoomid(0);
            saveMchModel.setCpc(vo.getCpc());
            saveMchModel.setID_number(vo.getID_number());
            saveMchModel.setTel(vo.getTel());
            saveMchModel.setShop_range(vo.getShop_range());
            saveMchModel.setRealname(vo.getRealname());
            //获取默认店铺头像  禅道 39776
//            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), customerModelMapper.getStoreMchId(vo.getStoreId()));
//            if (mchConfigModel == null) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "找不到店铺配置,请先设置");
//            }
//            saveMchModel.setHead_img(mchConfigModel.getHead_img());

            saveMchModel.setSheng(vo.getSheng());
            saveMchModel.setShi(vo.getShi());
            saveMchModel.setXian(vo.getXian());
            saveMchModel.setAddress(vo.getAddress());


            int dataMax = 3;
            if (StringUtils.isNotEmpty(vo.getCity_all()))
            {
                List<String> citys = Arrays.asList(vo.getCity_all().split("-"));
                if (citys.size() == dataMax)
                {

                }
            }

            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(vo.getStoreId());
            adminModel.setType(AdminModel.TYPE_CLIENT);
            adminModel = adminModelMapper.selectOne(adminModel);
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHZHBCZ, "商户账号不存在", "mch");
            }
            if (adminModel.getShop_id() != null && adminModel.getShop_id() > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZYDYCZ, "自营店已存在");
            }

            //注册一个用户
            User user = new User();
            user.setMobile(vo.getTel());
            user.setMima("000000");
            user.setZhanghao(vo.getTel());
            user.setCpc(vo.getCpc());

            //校验数据格式
            user = DataCheckTool.checkUserDataFormate(user);
            //检查账户/手机号是否已存在
            if (userBaseMapper.validataUserPhoneOrNoIsRegister(vo.getStoreId(), user.getZhanghao()) > 0)
            {
                logger.debug("创建自营店的时候,手机/账号已被注册!");
                user.setZhanghao(BuilderIDTool.getNext(BuilderIDTool.Type.ALPHA, 10));
                user.setMobile(null);
            }
            user.setUser_name(vo.getName());
            user.setHeadimgurl(headUrl);
            publicUserService.register(vo, null, user);
            logger.debug("创建自营店-用户{}注册成功", user.getUser_id());
            //校验店铺数据
            saveMchModel = DataCheckTool.checkMchDataFormate(saveMchModel);
            if (mchModelMapper.verifyMchName(vo.getStoreId(), vo.getName()) > 0)
            {
                logger.debug("店铺名称已存在,重新生成一个");
                saveMchModel.setName(BuilderIDTool.getGuid());
            }

            //获取商城一条店铺分类数据
            Integer mchCid = mchModelMapper.getOneMchCIdByStoreId(vo.getStoreId());

            saveMchModel.setCid(mchCid);
            saveMchModel.setUser_id(user.getUser_id());
            saveMchModel.setAdd_time(new Date());
            saveMchModel.setHead_img(ImgUploadUtils.getUrlImgByName(vo.getHeadImg(), true));
            saveMchModel.setPoster_img(ImgUploadUtils.getUrlImgByName(vo.getPosterImg(), true));
            saveMchModel.setShop_nature(String.valueOf(vo.getShop_nature()));


            if (StringUtils.isNotEmpty(vo.getImgUrls()))
            {
                String[] imgUrls = vo.getImgUrls().split(SplitUtils.DH);

                if ("1".equals(saveMchModel.getShop_nature()) && imgUrls.length != 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
                }
                if ("0".equals(saveMchModel.getShop_nature()) && imgUrls.length != 2)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
                }

                //营业执照上传 or 个人身份证照
                if (imgUrls.length > 0)
                {
                    //上传营业执照
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < imgUrls.length; i++)
                    {
                        imgUrls[i] = ImgUploadUtils.getUrlImgByName(imgUrls[i], true);
                        stringBuilder.append(imgUrls[i]).append(SplitUtils.DH);
                    }
                    saveMchModel.setBusiness_license(StringUtils.trim(stringBuilder.toString(), SplitUtils.DH));
                }
            }
            int count = mchModelMapper.insertSelective(saveMchModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CJZYDPSB, "创建自营店铺失败", "mch");
            }
            //设置店铺
            count = adminModelMapper.setAdminMchId(vo.getStoreId(), saveMchModel.getId());
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SZZYDPSB, "设置自营店铺失败", "mch");
            }
            // 设置店铺默认配置
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(vo.getStoreId());
            mchConfigModel.setMch_id(saveMchModel.getId());
            mchConfigModel.setPoster_img(headUrl);
            mchConfigModel.setHead_img(headUrl);
            mchConfigModelMapper.insertSelective(mchConfigModel);
            //添加跳转路径
            publicAdminService.addJumpPath(vo, saveMchModel.getId() + "", saveMchModel.getName(), JumpPathModel.JumpType.JUMP_TYPE0_MCH, JumpPathModel.JumpType.JUMP_TYPE_APP,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{saveMchModel.getId() + ""}, saveMchModel.getId(), null);
            publicAdminService.addJumpPath(vo, saveMchModel.getId() + "", saveMchModel.getName(), JumpPathModel.JumpType.JUMP_TYPE0_MCH, JumpPathModel.JumpType.JUMP_TYPE_PC,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{saveMchModel.getId() + ""}, saveMchModel.getId(), null);
            resultMap.put("mchId", saveMchModel.getId());
            //添加默认售后地址  ->使用自营店地址
            ServiceAddressModel serviceAddressModelSave = new ServiceAddressModel();
            serviceAddressModelSave.setUid("admin");
            serviceAddressModelSave.setTel(vo.getTel());
            serviceAddressModelSave.setName(vo.getName());
            //邮政编码
            String code = "000000";
            if (StringUtils.isNotEmpty(vo.getCode()))
            {
                code = vo.getCode();
            }
            serviceAddressModelSave.setCode(code);
            serviceAddressModelSave.setCpc(vo.getCpc());
            serviceAddressModelSave.setType(ServiceAddressModel.TYPE_RETURN_GOODS);
            serviceAddressModelSave.setSheng(saveMchModel.getSheng());
            serviceAddressModelSave.setShi(saveMchModel.getShi());
            serviceAddressModelSave.setXian(saveMchModel.getXian());
            serviceAddressModelSave.setAddress(saveMchModel.getAddress());
            serviceAddressModelSave.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            serviceAddressModelSave.setAddress_xq(saveMchModel.getSheng() + saveMchModel.getShi() + saveMchModel.getXian() + saveMchModel.getAddress());
            serviceAddressModelSave.setStore_id(vo.getStoreId());
            count = serviceAddressModelMapper.insertSelective(serviceAddressModelSave);
            if (count < 1)
            {
                logger.error("创建自营店添加默认收货地址失败!商城id为:" + vo.getStoreId());
            }
            //刷新缓存
            adminModel = adminModelMapper.selectByPrimaryKey(adminModel.getId());
            RedisDataTool.refreshRedisAdminCache(vo.getAccessId(), adminModel, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, redisUtil);

            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加自营店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mch");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> getAddPage(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                resultMap = publicGoodsService.addPage(vo.getStoreId(), user.getName(), user.getShop_id(), GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载添加商品页面 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAddPage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addGoods(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        try
        {
           return addProduct(vo);
        } catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
    }

    /**
     * 添加商品公共方法
     * @param vo
     * @return
     */
    private Map<String,Object> addProduct(UploadMerchandiseVo vo)
    {
        Map<String,Object> res = new HashMap<>();
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //缓存原因，更新自营店id
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            publicGoodsService.addProduct(vo, user.getName(), mchId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addGoods1(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //缓存原因，更新自营店id
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            publicGoodsService.addProduct1(vo, user.getName(), mchId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadAddGoods(MainVo vo, List<MultipartFile> image, String productClassId, String brandId, String freightId) throws LaiKeAPIException
    {
        boolean flag = true;
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            ExcelAnalysisVo excelAnalysisVo = new ExcelAnalysisVo();
            excelAnalysisVo.setFile(image);
            //文件标题头
            String[] fileTitle = new String[]{"商品标题", "副标题", "关键词", "属性(属性名和属性值用冒号隔开，不同属性名与属性名用分号隔开)", "成本价", "原价"
                    , "售价", "库存", "单位", "销量", "条形码", "重量(kg)", "库存预警值", "详细内容"
                    , "封面图", "属性图", "商品展示图"};
            List<String> titleNames = new ArrayList<>(Arrays.asList(fileTitle));
            excelAnalysisVo.setTitleName(titleNames);
            //数据标题头
            String[] dataTitle = new String[]{"goodsTitle", "goodsLabel", "goodsKey", "attrStr", "costPrice", "goodsPrice"
                    , "attrPrice", "stockNum", "unit", "volume", "scan", "weight", "warningNum", "content"
                    , "coverImgUrl", "attrImg", "lunImgUrl"};
            List<String> valueNames = new ArrayList<>(Arrays.asList(dataTitle));
            excelAnalysisVo.setValueKey(valueNames);
            Map<String, Object>       resultMap     = EasyPoiExcelUtil.analysisExcel(excelAnalysisVo);
            List<Map<String, Object>> excelDataList = DataUtils.cast(resultMap.get("list"));
            if (excelDataList != null)
            {
                //商品规格合集
                Hashtable<String, List<Map<String, Object>>> attrArrMapList = new Hashtable<>(16);
                //商品集合
                Hashtable<String, UploadMerchandiseVo> addGoodsListMap = new Hashtable<>(16);
                //已经处理过了的规格
                Hashtable<String, Set<String>> attrOldListMap = new Hashtable<>();
                String                         fileName       = image.get(0).getOriginalFilename();
                StringBuilder                  errorText      = new StringBuilder();
                for (Map<String, Object> map : excelDataList)
                {
                    int x = MapUtils.getIntValue(map, "x");
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "errorText")))
                    {
                        errorText.append(MapUtils.getString(map, "errorText"));
                    }
                    if (map.containsKey("goodsTitle"))
                    {
                        //已经处理过的规格
                        Set<String> attrOldList;
                        //当前商品名称
                        String goodsName = MapUtils.getString(map, "goodsTitle");
                        //当前规格
                        String currentAttrStr = MapUtils.getString(map, "attrStr");
                        if (StringUtils.isEmpty(goodsName) || StringUtils.isEmpty(currentAttrStr)
                                || StringUtils.isEmpty(brandId) || StringUtils.isEmpty(freightId) || StringUtils.isEmpty(productClassId))
                        {
                            errorText.append(String.format("第%s行 %s", x, "数据错误")).append(SplitUtils.DH);
                            flag = false;
                            break;
                        }
                        //当前规格成本价
                        BigDecimal costPrice;
                        //当前规格原价
                        BigDecimal attrYPrice;
                        //当前规格售价
                        BigDecimal attrPrice;
                        //当前规格库存
                        BigDecimal stockNum;
                        //当前规格预警库存
                        BigDecimal warningNum;
                        //属性图片
                        String attrImg;
                        try
                        {
                            costPrice = new BigDecimal(MapUtils.getString(map, "costPrice", BigDecimal.ZERO.toString()));
                            attrYPrice = new BigDecimal(MapUtils.getString(map, "goodsPrice", BigDecimal.ZERO.toString()));
                            attrPrice = new BigDecimal(MapUtils.getString(map, "attrPrice", BigDecimal.ZERO.toString()));
                            stockNum = new BigDecimal(MapUtils.getString(map, "stockNum", BigDecimal.ZERO.toString()));
                            warningNum = new BigDecimal(MapUtils.getString(map, "warningNum", BigDecimal.ZERO.toString()));
                            attrImg = MapUtils.getString(map, "attrImg");
                        }
                        catch (Exception e)
                        {
                            errorText.append(String.format("第%s行 %s", x, "数据不能为空")).append(SplitUtils.DH);
                            flag = false;
                            break;
                        }

                        //当前规格单位
                        String unit = MapUtils.getString(map, "unit");
                        if (attrOldListMap.containsKey(goodsName))
                        {
                            attrOldList = attrOldListMap.get(goodsName);
                            if (attrOldList.contains(currentAttrStr))
                            {
                                logger.debug("{} 该规格重复", currentAttrStr);
                                continue;
                            }
                        }
                        else
                        {
                            attrOldListMap.put(goodsName, new HashSet<>(16));
                        }
                        //商品数据处理
                        UploadMerchandiseVo uploadVo = new UploadMerchandiseVo();
                        if (addGoodsListMap.containsKey(goodsName))
                        {
                            uploadVo = addGoodsListMap.get(goodsName);
                        }
                        else
                        {
                            uploadVo.setStoreId(vo.getStoreId());
                            uploadVo.setStoreType(vo.getStoreType());
                            uploadVo.setAccessId(vo.getAccessId());
                            uploadVo.setFreightId(Integer.parseInt(freightId));
                            uploadVo.setShopId(user.getShop_id());
                            uploadVo.setProductTitle(goodsName);
                            uploadVo.setSubtitle(MapUtils.getString(map, "goodsLabel"));
                            uploadVo.setKeyword(MapUtils.getString(map, "goodsKey"));
                            uploadVo.setUnit(MapUtils.getString(map, "unit"));
                            uploadVo.setScan(MapUtils.getString(map, "scan"));
                            uploadVo.setProductClassId(productClassId);
                            uploadVo.setBrandId(brandId);

                            //添加默认位置
                            uploadVo.setDisplayPosition("3");
                            //轮播图
                            String[] lunImgUrl = MapUtils.getString(map, "lunImgUrl").split(SplitUtils.DH);
                            if (lunImgUrl.length > 5)
                            {
                                errorText.append(String.format("第%s行 %s", x, "商品展示图数量不得大于五张")).append(SplitUtils.DH);
                            }
                            uploadVo.setShowImg(MapUtils.getString(map, "lunImgUrl"));
                            uploadVo.setCoverMap(MapUtils.getString(map, "coverImgUrl"));
                            uploadVo.setStockWarn(warningNum.intValue());
                            uploadVo.setContent(MapUtils.getString(map, "content"));
                            uploadVo.setX(x);
                            uploadVo.setVolume(MapUtils.getIntValue(map, "volume", 0));
                            uploadVo.setWeight(MapUtils.getString(map, "weight", "0"));
                            //cbj=1,yj=998,sj=188,kucun=9999,unit=%E7%AE%B1,stockWarn=99
                            String initialStr = "cbj=" + costPrice + ",yj=" + attrYPrice +
                                    ",sj=" + attrPrice + ",kucun=" + stockNum + ",unit=" + unit +
                                    ",stockWarn=" + warningNum + ",attrImg=" + attrImg;
                            uploadVo.setInitial(initialStr);
                        }
//                        //规格数据处理 颜色A:白色A;尺码A:M; [{"cbj":"1","yj":"12","sj":"11","kucun":"111","attr_list":[{"attr_id":"","attr_name":"蓝色","attr_group_name":"颜色"}]}]
//                        String[]                  attrStrList = StringUtils.trim(currentAttrStr, SplitUtils.FH).split(SplitUtils.FH);
//                        List<Map<String, String>> attrList    = new ArrayList<>();
//                        for (String skuStr : attrStrList)
//                        {
//                            String[]            skuList = skuStr.split(SplitUtils.MH);
//                            Map<String, String> attrMap = new HashMap<>(16);
//                            attrMap.put("attr_id", "");
//                            attrMap.put("attr_group_name", skuList[0]);
//                            attrMap.put("attr_name", skuList[1]);
//                            if (this.containsSpecialCharacters(attrMap.get("attr_name")))
//                            {
//                                errorText.append(String.format("第%s行 %s", x, "数据错误")).append(SplitUtils.DH);
//                            }
//                            attrList.add(attrMap);
//                        }
//                        //构造attrArr数据 例:[{"cbj":"1","yj":"12","sj":"11","kucun":"111","attr_list":[{"attr_id":"","attr_name":"蓝色","attr_group_name":"颜色"}]}]
//                        Map<String, Object> attrArr = new HashMap<>(16);
//                        attrArr.put("cbj", MapUtils.getString(map, "costPrice"));
//                        attrArr.put("yj", MapUtils.getString(map, "goodsPrice"));
//                        attrArr.put("sj", MapUtils.getString(map, "attrPrice"));
//                        attrArr.put("unit", MapUtils.getString(map, "unit"));
//                        attrArr.put("kucun", MapUtils.getString(map, "stockNum"));
//                        attrArr.put("image", MapUtils.getString(map, "lunImgUrl").split(SplitUtils.DH)[0]);
//                        attrArr.put("bar_code", MapUtils.getString(map, "scan"));
//                        attrArr.put("attr_list", attrList);
//                        attrArr.put("img", MapUtils.getString(map, "attrImg"));
//                        attrArr.put("cid", "");
//                        List<Map<String, Object>> attrListOld = new ArrayList<>();
//                        if (attrArrMapList.containsKey(goodsName))
//                        {
//                            attrListOld = attrArrMapList.get(goodsName);
//                        }
//                        else
//                        {
//                            attrArrMapList.put(goodsName, attrListOld);
//                        }
//                        attrListOld.add(attrArr);
//                        uploadVo.setAttrArr(JSON.toJSONString(attrListOld));
//                        uploadVo.setCountry_num(vo.getCountry_num());
//                        uploadVo.setLang_code(vo.getLang_code());
                        //3991 批量上传单个商品同属性名不同属性值会被覆盖
                        // 规格数据处理 - 生成属性组合
                        String[]                  attrStrList  = StringUtils.trim(currentAttrStr, SplitUtils.FH).split(SplitUtils.FH);
                        Map<String, List<String>> attrGroupMap = new HashMap<>(); // 按属性组名分组存储属性值

                        // 1. 分组处理属性值
                        for (String skuStr : attrStrList)
                        {
                            String[] skuList = skuStr.split(SplitUtils.MH);
                            if (skuList.length != 2)
                            {
                                errorText.append(String.format("第%s行 属性格式错误：%s", x, skuStr)).append(SplitUtils.DH);
                                flag = false;
                                continue;
                            }
                            String attrGroupName = skuList[0].trim();
                            String attrValue     = skuList[1].trim();
                            //生成分组属性map，并补充属性的值，如 颜色：白色、绿色
                            attrGroupMap.computeIfAbsent(attrGroupName, k -> new ArrayList<>()).add(attrValue);
                        }

                        // 2. 生成属性组合（支持单个属性组的情况）
                        List<String> attrGroups = new ArrayList<>(attrGroupMap.keySet()); //获取所有键

                        // 处理单个属性组的情况
                        if (attrGroups.size() == 1)
                        {
                            String       singleAttrGroupName = attrGroups.get(0);
                            List<String> singleAttrValues    = attrGroupMap.get(singleAttrGroupName);

                            for (String value : singleAttrValues)
                            {
                                Map<String, Object>       attrArr  = new HashMap<>(16);
                                List<Map<String, String>> attrList = new ArrayList<>();

                                Map<String, String> attrMap = new HashMap<>();
                                attrMap.put("attr_id", "");
                                attrMap.put("attr_group_name", singleAttrGroupName);
                                attrMap.put("attr_name", value);
                                attrList.add(attrMap);

                                // 填充规格公共信息
                                attrArr.put("cbj", MapUtils.getString(map, "costPrice"));
                                attrArr.put("yj", MapUtils.getString(map, "goodsPrice"));
                                attrArr.put("sj", MapUtils.getString(map, "attrPrice"));
                                attrArr.put("unit", unit);
                                attrArr.put("kucun", MapUtils.getString(map, "stockNum"));
                                attrArr.put("image", MapUtils.getString(map, "lunImgUrl").split(SplitUtils.DH)[0]);
                                attrArr.put("bar_code", MapUtils.getString(map, "scan"));
                                attrArr.put("attr_list", attrList);
                                attrArr.put("img", attrImg);
                                attrArr.put("cid", ""); // 分类ID，可后续处理

                                // 添加到商品的规格列表
                                List<Map<String, Object>> attrListOld = attrArrMapList.computeIfAbsent(goodsName, k -> new ArrayList<>());
                                // 添加到规格列表前检查是否已存在（避免重复）
                                if (!attrListOld.contains(attrArr))
                                {
                                    attrListOld.add(attrArr);
                                }
                                // 序列化规格列表并设置到 uploadVo
                                uploadVo.setAttrArr(JSON.toJSONString(attrListOld));
                            }
                        }
                        // 处理多个属性组的情况（生成组合）
                        else if (attrGroups.size() >= 2)
                        {
                            List<List<String>> allCombinations = generateCartesianProduct(attrGroupMap);

                            for (List<String> combination : allCombinations)
                            {
                                Map<String, Object>       attrArr  = new HashMap<>(16);
                                List<Map<String, String>> attrList = new ArrayList<>();

                                // 构建当前组合的属性列表
                                for (int i = 0; i < attrGroups.size(); i++)
                                {
                                    String              attrGroupName = attrGroups.get(i);
                                    String              attrValue     = combination.get(i);
                                    Map<String, String> attrMap       = new HashMap<>();
                                    attrMap.put("attr_id", "");
                                    attrMap.put("attr_group_name", attrGroupName);
                                    attrMap.put("attr_name", attrValue);
                                    attrList.add(attrMap);
                                }

                                // 填充规格公共信息
                                attrArr.put("cbj", MapUtils.getString(map, "costPrice"));
                                attrArr.put("yj", MapUtils.getString(map, "goodsPrice"));
                                attrArr.put("sj", MapUtils.getString(map, "attrPrice"));
                                attrArr.put("unit", unit);
                                attrArr.put("kucun", MapUtils.getString(map, "stockNum"));
                                attrArr.put("image", MapUtils.getString(map, "lunImgUrl").split(SplitUtils.DH)[0]);
                                attrArr.put("bar_code", MapUtils.getString(map, "scan"));
                                attrArr.put("attr_list", attrList);
                                attrArr.put("img", attrImg);
                                attrArr.put("cid", ""); // 分类ID，可后续处理

                                // 添加到商品的规格列表
                                List<Map<String, Object>> attrListOld = attrArrMapList.computeIfAbsent(goodsName, k -> new ArrayList<>());
                                // 添加到规格列表前检查是否已存在（避免重复）
                                if (!attrListOld.contains(attrArr))
                                {
                                    attrListOld.add(attrArr);
                                }
                                // 序列化规格列表并设置到 uploadVo
                                uploadVo.setAttrArr(JSON.toJSONString(attrListOld));
                            }
                        }
                        else
                        {
                            errorText.append(String.format("第%s行 规格不能为空", x)).append(SplitUtils.DH);
                            flag = false;
                            continue;
                        }

                        //规格处理结束
                        attrOldListMap.get(goodsName).add(currentAttrStr);
                        //需要上传的商品
                        addGoodsListMap.put(uploadVo.getProductTitle(), uploadVo);
                    }
                }

                try
                {
                    extracted(addGoodsListMap, errorText);
                }
                catch (LaiKeAPIException ignored)
                {
                    flag = false;
                }

                //记录
                FileDeliveryModel fileDeliveryModel = new FileDeliveryModel();
                fileDeliveryModel.setId(BuilderIDTool.getSnowflakeId() + "");
                fileDeliveryModel.setName(fileName);
                fileDeliveryModel.setAdd_date(new Date());
                int status = 1;
                if (StringUtils.isNotEmpty(errorText))
                {
                    status = 0;
                    fileDeliveryModel.setText(errorText.deleteCharAt(errorText.length() - 1).toString()
                    );
                }
                fileDeliveryModel.setStatus(status);
                fileDeliveryModel.setMch_id(user.getShop_id());
                int count = fileDeliveryModelMapper.insertSelective(fileDeliveryModel);
                logger.info("文件记录新增：：：：：：：{}",count);
            }
            publiceService.addAdminRecord(vo.getStoreId(), "进行了批量导入商品操作", AdminRecordModel.Type.ADD, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量上传商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadAddGoods");
        }
        return flag;
    }

    /**
     * 生成多个属性组值的笛卡尔积组合
     * 例如：颜色[白, 绿] × 尺码[M, S] → [[白,M], [白,S], [绿,M], [绿,S]]
     */
    private List<List<String>> generateCartesianProduct(Map<String, List<String>> attrGroupMap)
    {
        List<List<String>> groups       = new ArrayList<>(attrGroupMap.values());
        List<List<String>> combinations = new ArrayList<>();
        combinations.add(new ArrayList<>());

        for (List<String> group : groups)
        {
            List<List<String>> newCombinations = new ArrayList<>();
            for (List<String> combo : combinations)
            {
                for (String value : group)
                {
                    List<String> newCombo = new ArrayList<>(combo);
                    newCombo.add(value);
                    newCombinations.add(newCombo);
                }
            }
            combinations = newCombinations;
        }
        return combinations;
    }

    // 定义特殊字符集，可以根据需要修改
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+{}:\"<>?[];',./\\|~";

    // 使用正则表达式检查字符串是否包含特殊字符
    public static boolean containsSpecialCharacters(String str)
    {
        // 遍历特殊字符集，为每个字符创建一个正则表达式
        // 注意：在正则表达式中，某些字符（如点号.、星号*、问号?等）需要转义
        for (char ch : SPECIAL_CHARACTERS.toCharArray())
        {
            String regex = Pattern.quote(String.valueOf(ch)); // 使用Pattern.quote来确保特殊字符在正则表达式中被正确处理
            if (str.matches(".*" + regex + ".*"))
            { // 如果字符串中包含该特殊字符，则返回true
                return true;
            }
        }
        // 如果循环结束都没有返回true，则字符串不包含特殊字符
        return false;
    }

    //@Transactional(rollbackFor = Exception.class)
    public void extracted(Hashtable<String, UploadMerchandiseVo> addGoodsListMap, StringBuilder errorText)
    {
        int errorLineIndex = 1;
        try
        {
            if (StringUtils.isEmpty(errorText.toString()))
            {
                for (String key : addGoodsListMap.keySet())
                {
                    logger.debug("================== 当前商品:" + key + "========================");
                    UploadMerchandiseVo goodsInfo = addGoodsListMap.get(key);
                    logger.debug("规格:" + goodsInfo.getAttrArr());
                    try
                    {
                        this.addProduct(goodsInfo);
                    }
                    finally
                    {
                        logger.debug("================== 当前商品结束========================");
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("批量上传商品 异常", l);
            //回滚
            errorText.append(String.format("第%s行 %s", errorLineIndex, l.getMessage())).append(SplitUtils.DH);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadAddGoods");
        }
    }

    @Override
    public Map<String, Object> uploadRecordList(MainVo vo, String key, Integer status, String startDate, String endDate) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("mchId", user.getShop_id());
            paramMap.put("name", key);
            paramMap.put("status", status);
            paramMap.put("startDate", startDate);
            paramMap.put("endDate", endDate);
            paramMap.put("type", 0);
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());

            int                       total      = fileDeliveryModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> resultList = new ArrayList<>();
            if (total > 0)
            {
                resultList = fileDeliveryModelMapper.selectDynamic(paramMap);
            }

            resultMap.put("total", total);
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("批量上传记录列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadRecordList");
        }
        return resultMap;
    }

    @Override
    public void delUploadRecord(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            AdminModel        user              = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            FileDeliveryModel fileDeliveryModel = fileDeliveryModelMapper.selectByPrimaryKey(id);
            if (fileDeliveryModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
            }
            int row = fileDeliveryModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了文件id：" + id + " 的批量商品导入记录的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除发货记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
    }

    @Override
    public void editSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProductListModel productListOld = productListModelMapper.selectByPrimaryKey(id);
            if (productListOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            if (sort == null)
            {
                sort = 0;
            }
            ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(id);
            productListModel.setSort(sort);
            int row = productListModelMapper.updateByPrimaryKeySelective(productListModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑商品序号 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
    }

    @Override
    public Map<String, Object> getGoodsInfoById(MainVo vo, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            resultMap = publicGoodsService.editPage(vo.getStoreId(), user.getName(), 0, goodsId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsInfoById");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delGoodsById(MainVo vo, String pId) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(pId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            AdminModel          user           = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> resultMapOrder = null;
            //拼团商品不可删除
            PluginsModel groupConfig = pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.GOGROUP, vo.getStoreId());
            if (groupConfig != null && groupConfig.getStatus().equals(1))
            {
                HashMap<String, Object> map = new HashMap<>();
                map.put("vo", vo);
                map.put("goodsIds", pId);
                map.put("mchId", "");
                Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(map));
                try
                {
                    resultMapOrder = httpApiUtils.executeHttpApi("plugin.group.http.getExistenceOfGoods", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                }
                catch (Exception e)
                {
                    logger.error("删除商品,拼团商品远程调用异常:", e);
                }
                if (resultMapOrder != null && !(Boolean) resultMapOrder.get("whether"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PTHDWJS, "商品删除失败,商品加入拼团活动，对应拼团活动未结束");
                }
            }
            //竞拍商品不可删除
            PluginsModel auctionConfig = pluginsModelMapper.getPluginInfo(DictionaryConst.Plugin.AUCTION, vo.getStoreId());
            if (auctionConfig != null && auctionConfig.getStatus().equals(1))
            {
                Map<String, Object> paramApiMap = JSON.parseObject(JSON.toJSONString(vo));
                paramApiMap.put("goodsIds", pId);
                try
                {
                    resultMapOrder = httpApiUtils.executeHttpApi("plugin.admin.auction.goods.delProExamine", paramApiMap, MediaType.MULTIPART_FORM_DATA_VALUE);
                }
                catch (Exception e)
                {
                    logger.error("删除商品,竞拍商品远程调用异常:", e);
                }
                if (resultMapOrder != null && !MapUtils.getBoolean(resultMapOrder, "isDel"))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JPHDWJS, "商品删除失败,商品加入竞拍活动，对应竞拍活动进行中");
                }
            }
            if (user != null)
            {
                String[] pidList = pId.split(SplitUtils.DH);
                for (String goodsId : pidList)
                {
                    if (!publicGoodsService.delGoodsById(vo.getStoreId(), Integer.parseInt(goodsId), null))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败");
                    }
                    publiceService.addAdminRecord(vo.getStoreId(), user.getName(), "删除了商品ID：" + goodsId, AdminRecordModel.Type.DEL, AdminRecordModel.Source.PC_PLATFORM);

                }
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delGoodsById");
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void upperAndLowerShelves(MainVo vo, String goodsIds, Integer status) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicGoodsService.upperAndLowerShelves(vo.getStoreId(), goodsIds, null, status);
            String[] pidList = goodsIds.split(",");
            for (String goodsId : pidList)
            {
                if (status == 0)
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "将商品ID：" + goodsId + "进行了下架操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
                else
                {
                    publiceService.addAdminRecord(vo.getStoreId(), "将商品ID：" + goodsId + "进行了上架操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上下架商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upperAndLowerShelves");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean goodsByTop(MainVo vo, int goodsIds) throws LaiKeAPIException
    {
        try
        {
            int              maxSort          = productListModelMapper.getGoodsMaxSort(vo.getStoreId());
            ProductListModel productListModel = new ProductListModel();
            productListModel.setId(goodsIds);
            productListModel.setSort(maxSort);
            int count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSB, "置顶失败", "goodsByTop");
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("商品置顶 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "goodsByTop");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean goodsMovePosition(MainVo vo, int currentGoodsId, int moveGoodsId) throws LaiKeAPIException
    {
        try
        {
            //查询当前商品序号
            int currentSort = productListModelMapper.getGoodsSort(vo.getStoreId(), currentGoodsId);
            //查询被移动的商品序号
            int moveSort = productListModelMapper.getGoodsSort(vo.getStoreId(), moveGoodsId);

            //互换位置
            ProductListModel productListModel = new ProductListModel();
            productListModel.setId(currentGoodsId);
            productListModel.setSort(moveSort);
            int count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
            if (count > 0)
            {
                productListModel.setId(moveGoodsId);
                productListModel.setSort(currentSort);
                count = productListModelMapper.updateByPrimaryKeySelective(productListModel);
                if (count > 0)
                {
                    return true;
                }
            }
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YDSB, "移动失败", "goodsMovePosition");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("商品上下移动 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "goodsMovePosition");
        }
    }

    @Override
    public Map<String, Object> getClassInfo(GoodsClassVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel.setNotset(0);
            if (vo.getCountry_num() != null)
            {
                productClassModel.setCountry_num(vo.getCountry_num());
            }

            if (!StringUtils.isEmpty(vo.getLang_code()))
            {
                productClassModel.setLang_code(vo.getLang_code());
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                productClassModel.setLang_code(language);
//            }

            if (!StringUtils.isEmpty(vo.getClassName()))
            {
                productClassModel.setPname(vo.getClassName());
                if (vo.getLevel() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                }
                productClassModel.setLevel(vo.getLevel());
            }
            else
            {
                //1=查询下级,2=查询上级,3=根据类别Id查询 默认查询一级
                if (vo.getType() == null || vo.getType().equals(ClassType.FIRST_STAGE))
                {
                    //查询一级
                    productClassModel.setSid(0);
                }
                else if (vo.getType().equals(ClassType.SUBORDINATE))
                {
                    //查询下级
                    if (vo.getClassId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setSid(vo.getClassId());
                }
                else if (vo.getType().equals(ClassType.SUPERIOR))
                {
                    //查询上级
                    if (vo.getFatherId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setSid(vo.getFatherId());
                }
                else if (vo.getType().equals(ClassType.ID))
                {
                    //查询分类id
                    if (vo.getClassId() == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "getClassInfo");
                    }
                    productClassModel.setCid(vo.getClassId());
                }
            }
            PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize());
            productClassModel.setPageModel(pageModel);
            productClassModel.setExamine(Integer.valueOf(DictionaryConst.ExameStatus.EXAME_PASS_STATUS));
            int                     total                 = productClassModelMapper.getGoodsClassCount(productClassModel);
            List<ProductClassModel> productClassModelList = productClassModelMapper.getProductClassLevel(productClassModel);
            List<Map<String, Object>> productClassModelMap = JSON.parseObject(JSON.toJSONString(productClassModelList), new TypeReference<List<Map<String, Object>>>()
            {
            });
            //图片处理
            for (Map<String, Object> map : productClassModelMap)
            {
                int    level       = MapUtils.getIntValue(map, "level") + 1;
                String levelFormat = "%s级分类";
                switch (level)
                {
                    case 1:
                        levelFormat = String.format(levelFormat, "一");
                        break;
                    case 2:
                        levelFormat = String.format(levelFormat, "二");
                        break;
                    case 3:
                        levelFormat = String.format(levelFormat, "三");
                        break;
                    default:
                        levelFormat = String.format(levelFormat, level);
                        break;
                }
                map.put("levelFormat", levelFormat);
                String imgUrl = publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId());
                map.put("img", imgUrl);
                map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map,"add_date"), GloabConst.TimePattern.YMDHMS));
                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));
            }

            resultMap.put("classInfo", productClassModelMap);
            resultMap.put("total", total);
            if (vo.getExportType().equals(1))
            {
                if (productClassModelMap.size() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBBCZ, "当前没有分类信息", "getClassInfo");
                }
            }
            if (vo.getExportType().equals(1))
            {
                exportGoodsClassData(productClassModelMap, response);
                return null;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取类别信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassInfo");
        }
        return resultMap;
    }

    @Override
    public void batchSelectionOfLocations(MainVo vo, String goodsIds, String status)
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicGoodsService.batchSelectionOfLocations(vo, goodsIds, status);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置显示位置异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchSelectionOfLocations");
        }
    }

    @Override
    public void batchWarning(MainVo vo, String goodsIds, Integer minInventory)
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            publicGoodsService.batchWarning(vo, goodsIds, minInventory);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置预警异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchWarning");
        }
    }

    @Override
    public void batchSetShippingFees(MainVo vo, String goodsIds, Integer fid)
    {
        try
        {
            //商家后台登录判断
            if (vo.getStoreType() == 7)
            {
                RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            }else
            {
                RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            }
            publicGoodsService.batchSetShippingFees(vo, goodsIds, fid);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置运费异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchSetShippingFees");
        }
    }

    @Override
    public Map<String, Object> batchObtainShippingFees(MainVo vo, String goodsIds)
    {
        Map<String, Object> resultMap = new HashMap<>();
        try
        {
            if (StringUtils.isEmpty(goodsIds))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "batchObtainShippingFees");
            }
            ProductListModel productListModel = new ProductListModel();
            String[]         pids             = goodsIds.split(SplitUtils.DH);
            productListModel.setId(Integer.valueOf(pids[0]));
            productListModel = productListModelMapper.selectOne(productListModel);

            if (Objects.isNull(productListModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "batchObtainShippingFees");
            }

            PageModel pageModel = new PageModel(0, Integer.MAX_VALUE);
            resultMap = publicFreightService.GetFreightList(vo.getStoreId(), productListModel.getMch_id(), null, null, null, pageModel);
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置显示位置异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchObtainShippingFees");
        }
        return resultMap;
    }


    private void exportGoodsClassData(List<Map<String, Object>> goodsList, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"分类ID", "分类名称", "分类级别", "添加时间"};
            //对应字段
            String[] kayList = new String[]{"cid", "pname", "levelFormat", "add_date"};
            EasyPoiExcelUtil.excelExport("商品分类", headerList, kayList, goodsList, response);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出商品分类数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportGoodsClassData");
        }
    }

    @Override
    public Map<String, Object> getClassLevelTopAllInfo(MainVo vo, int classId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取当前类别信息
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setCid(classId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            if (vo.getCountry_num() == null)
            {
                productClassModel.setCountry_num(null);
            }
            else
            {
                productClassModel.setCountry_num(vo.getCountry_num());
            }

            productClassModel = productClassModelMapper.selectOne(productClassModel);
            Map<Integer, List<ProductClassModel>> resultDataMap =


                    new HashMap<>(16);
            if (productClassModel != null)
            {
                //图片处理
                String imgUrl = publiceService.getImgPath(productClassModel.getImg(), vo.getStoreId());
                productClassModel.setImg(imgUrl);
                //递归找上级
                publicGoodsService.getClassLevelAllInfo(vo.getStoreId(), classId, resultDataMap);
            }
            resultMap.put("classInfo", productClassModel);
            resultMap.put("levelInfoList", resultDataMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取当前类别所有上级 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelAllInfo");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    public void delClass(MainVo vo, int classId) throws LaiKeAPIException
    {
        try
        {
            AdminModel        adminModel      = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProductClassModel productClassOld = productClassModelMapper.selectByPrimaryKey(classId);
            if (productClassOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "数据不存在");
            }

            //查询系统自动生成且无法删除的品牌id 默认品牌已经没有了
//            String newBrand = brandClassModelMapper.getBrand(vo.getStoreId(), DictionaryConst.ProductRecycle.NOT_STATUS);
//            if (StringUtils.isEmpty(newBrand))
//            {
//                publicGoodsService.builderDefaultClassBrand(vo.getStoreId());
//                throw new LaiKeApiWarnException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "请稍后再试");
//            }

            //根据类别id查询商品id，循环修改商品的分类和品牌到默认分类和默认品牌
            //商品下架、商品分类置空
            List<Map<String, Object>> goodIds = productClassModelMapper.getGoodsByClass(vo.getStoreId(), classId);
            for (Map<String, Object> goods : goodIds)
            {
//                ProductListModel productListModel = new ProductListModel();
//                productListModel.setProduct_class(null);
//                //由于会将当前分类下面的所有品牌都同步到默认分类底下去 所有这里赋值商品品牌为默认品牌条件可有可无
//                productListModel.setBrand_id(null);
//                productListModel.setStatus( DictionaryConst.GoodsStatus.OFFLINE_GROUNDING + "" );
//                productListModel.setId(MapUtils.getInteger(goods, "id"));
//                productListModelMapper.updateByPrimaryKeySelective(productListModel);

                productListModelMapper.updateClassIdNullById(MapUtils.getInteger(goods, "id"));
            }

            //与该分类绑定的品牌 解绑
            List<Map<String, Object>> brandIds = productClassModelMapper.getclassIsBindBrand(vo.getStoreId(), classId);
            for (Map<String, Object> brand : brandIds)
            {
                BrandClassModel brandClassModel = new BrandClassModel();
                brandClassModel.setBrand_id(MapUtils.getInteger(brand, "brand_id"));
                brandClassModel = brandClassModelMapper.selectOne(brandClassModel);
                if (brandClassModel != null)
                {
                    //解绑
                    brandClassModel.setCategories(brandClassModel.getCategories().replaceAll("," + classId + ",", ""));
                    brandClassModelMapper.updateByPrimaryKeySelective(brandClassModel);
                }
            }

            //递归找下级
            List<Integer> classIDList = new ArrayList<>();
            getClassLevelLowAll(vo.getStoreId(), classId, classIDList);
            classIDList.add(classId);
            for (Integer cid : classIDList)
            {
                //二级，根据类别id查询商品id，循环修改商品的分类以及品牌到默认分类以及默认品牌
                List<Map<String, Object>> classLevelGoodIds = productClassModelMapper.getGoodsByClass(vo.getStoreId(), cid);
                for (Map<String, Object> levelGoods : classLevelGoodIds)
                {
                    ProductListModel productListLevelModel = new ProductListModel();
                    productListLevelModel.setProduct_class(null);
                    productListLevelModel.setBrand_id(null);
                    productListLevelModel.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING + "");
                    productListLevelModel.setId(MapUtils.getInteger(levelGoods, "id"));
                    int row = productListModelMapper.updateByPrimaryKeySelective(productListLevelModel);
                    if (row < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "修改商品分类以及默认品牌失败", "delClass");
                    }
                }

                //删除当前类别
                ProductClassModel productClassModel = new ProductClassModel();
                productClassModel.setCid(cid);
                productClassModel.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int count = productClassModelMapper.updateByPrimaryKeySelective(productClassModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败", "delClass");
                }

                //分类是否是一级分类,如果是一级分类则与关联的品牌接解除关系
                if (productClassOld.getLevel().equals(0))
                {
                    brandClassModelMapper.removeClassBrand(cid);
                    brandClassModelMapper.removeClassBrandClean();
                    //品牌分类是空的则绑定到默认分类下面
//                    brandClassModelMapper.bindDefaultClass(vo.getStoreId());
                }

                //删除跳转地址
                JumpPathModel jumpPathModel = new JumpPathModel();
                jumpPathModel.setSid(cid + "");
                jumpPathModel.setType0(JumpPathModel.JumpType.JUMP_TYPE0_GOODS_CLASS);
                jumpPathModelMapper.delete(jumpPathModel);
            }
            //同时删除供应商分类
            SupplierProClassModel supplierProClassModel = new SupplierProClassModel();
            supplierProClassModel.setStore_id(vo.getStoreId());
            supplierProClassModel.setPname(productClassOld.getPname());
            supplierProClassModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
            supplierProClassModel.setExamine(1);
            supplierProClassModel = supplierProClassModelMapper.selectOne(supplierProClassModel);
            if (supplierProClassModel != null)
            {
                supplierProClassModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_OK);
                supplierProClassModelMapper.updateByPrimaryKeySelective(supplierProClassModel);
                //分类是否是一级分类,如果是一级分类则与关联的品牌接解除关系
                if (supplierProClassModel.getLevel().equals(0))
                {
                    supplierBrandModelMapper.removeClassBrand(classId);
                    supplierBrandModelMapper.removeClassBrandClean();
                    //品牌分类是空的则绑定到默认分类下面
                    supplierBrandModelMapper.bindDefaultClass(vo.getStoreId());
                }
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了分类ID：" + classId + "的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除当前类别 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delClass");
        }
    }

    @Transactional(rollbackFor = Exception.class, noRollbackFor = LaiKeApiWarnException.class)
    @Override
    public void addClass(MainVo vo, Integer classId, String className, String ename, String img, int level, int fatherId) throws LaiKeAPIException
    {
        try
        {
            //type 这个字段是pc店铺端审核情况下用的 自营店可以不传这个参数
            publicGoodsClassService.addClass(vo, classId, className, ename, img, level, fatherId, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }


    /**
     * 当前下面的所有类别降级/升级处理
     *
     * @param cid      -
     * @param levelOld - 之前的等级
     * @param level    - 升级/降级后的等级
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022/2/22 18:09
     */
    private void classUpLevel(int cid, int levelOld, int level) throws LaiKeAPIException
    {
        try
        {
            //级差
            int levelDif = levelOld - level;
            if (levelDif == 0)
            {
                return;
            }
            logger.error("级别升级/降级 当前级别【{}】", level);
            ProductClassModel productClassOld = new ProductClassModel();
            productClassOld.setSid(cid);
            productClassOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<ProductClassModel> productClassModelList = productClassModelMapper.select(productClassOld);
            for (ProductClassModel productClass : productClassModelList)
            {
                //降级/升级处理
                ProductClassModel productClassUpdate = new ProductClassModel();
                productClassUpdate.setCid(productClass.getCid());
                productClassUpdate.setLevel(level + 1);
                //如果超过5级则删除
                if (productClassUpdate.getLevel() > 4)
                {
                    productClassUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                    //把下级也全部删除
                    productClassModelMapper.delClassBySid(productClass.getCid());
                }
                productClassModelMapper.updateByPrimaryKeySelective(productClassUpdate);
                this.classUpLevel(productClass.getCid(), productClass.getLevel(), productClassUpdate.getLevel());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("级别升级/降级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classUpLevel");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean classSortTop(MainVo vo, Integer classId) throws LaiKeAPIException
    {
        try
        {
            AdminModel        adminModel       = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProductClassModel updateClassModel = new ProductClassModel();
            updateClassModel.setCid(classId);
            //获取最新序号
            int maxSort = productClassModelMapper.getGoodsClassMaxSort(vo.getStoreId());
            updateClassModel.setSort(maxSort);

            //置顶
            int count = productClassModelMapper.updateByPrimaryKeySelective(updateClassModel);
            //操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "置顶了分类ID：" + classId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
            if (count > 0)
            {
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("类别置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "classSortTop");
        }
        return false;
    }


    /**
     * 递归找下级
     *
     * @param storeId -
     * @param classId -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/30 14:41
     */
    private void getClassLevelLowAll(int storeId, int classId, List<Integer> classIdList) throws LaiKeAPIException
    {
        try
        {
            //找下级
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(storeId);
            productClassModel.setSid(classId);
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<ProductClassModel> productClassModelList = productClassModelMapper.select(productClassModel);
            for (ProductClassModel productClass : productClassModelList)
            {
                classIdList.add(productClass.getCid());
                getClassLevelLowAll(storeId, productClass.getCid(), classIdList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("递归找下级 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getClassLevelLowAll");
        }
    }


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private PublicFreightService publicFreightService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private FileDeliveryModelMapper fileDeliveryModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;


    @Autowired
    private SupplierBrandModelMapper supplierBrandModelMapper;

}
