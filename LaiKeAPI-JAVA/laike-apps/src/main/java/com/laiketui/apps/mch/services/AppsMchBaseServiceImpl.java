package com.laiketui.apps.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Joiner;
import com.laiketui.apps.api.mch.AppsMchBaseService;
import com.laiketui.apps.api.mch.AppsMchExpressService;
import com.laiketui.apps.mch.common.consts.AppsMchConst;
import com.laiketui.common.api.*;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.redisGEO.GeoUtils;
import com.laiketui.common.utils.tool.BankTool;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.common.utils.tool.data.GoodsDataUtils;
import com.laiketui.common.utils.tool.jwt.JwtUtils;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.*;
import com.laiketui.domain.coupon.CouponConfigModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.living.LivingConfigModel;
import com.laiketui.domain.living.LivingRoomModel;
import com.laiketui.domain.log.SignRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.mch.son.MchAdminModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.order.OrderDetailsModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.order.ReturnOrderModel;
import com.laiketui.domain.order.StoreSelfDeliveryModel;
import com.laiketui.domain.plugin.DiyModel;
import com.laiketui.domain.presell.PreSellConfigModel;
import com.laiketui.domain.presell.PreSellGoodsModel;
import com.laiketui.domain.product.*;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.virtual.WriteRecordModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.goods.AddStockVo;
import com.laiketui.domain.vo.main.RefundVo;
import com.laiketui.domain.vo.mch.*;
import com.laiketui.domain.vo.order.OrderSettlementVo;
import com.laiketui.domain.vo.order.ShipDataVo;
import com.laiketui.domain.vo.user.AddBankVo;
import com.laiketui.root.annotation.HttpApiMethod;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.gateway.util.I18nUtils;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 店铺实现
 *
 * @author Trick
 * @date 2020/9/30 10:50
 */
@Service
public class AppsMchBaseServiceImpl implements AppsMchBaseService
{

    private final Logger logger = LoggerFactory.getLogger(AppsMchBaseServiceImpl.class);

    private final String NEARBY_KEY = "user_nearby";

    @Autowired
    MchModelMapper               lktMchModelMapper;
    @Autowired
    StoreSelfDeliveryModelMapper storeSelfDeliveryModelMapper;

    @Autowired
    UploadConfigModelMapper uploadConfigModelMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private AppsMchPromiseServiceImpl appsMchPromiseService;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private UserAuthorityModelMapper userAuthorityModelMapper;

    @Autowired
    private PublicStockService publicStockService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchStoreAccountModelMapper mchStoreAccountModelMapper;

    @Autowired
    private PublicExpressService publicExpressService;

    @Autowired
    private MchPromiseModelMapper    mchPromiseModelMapper;
    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ReturnOrderModelMapper returnOrderModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;


    @Autowired
    private PublicGoodsService publicGoodsService;
    @Autowired
    private PubliceService     publiceService;

    @Autowired
    private CouponConfigModelMapper couponConfigModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private BrandClassModelMapper brandClassModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private BankCardModelMapper bankCardModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductNumberModelMapper productNumberModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private ProductConfigModelMapper productConfigModelMapper;

    @Autowired
    private BannerModelMapper bannerModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private ExpressModelMapper expressModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private PublicRefundService publicRefundService;

    @Autowired
    private PreSellGoodsMapper preSellGoodsMapper;

    @Autowired
    private PreSellConfigModelMapper preSellConfigModelMapper;

    @Autowired
    private UserGradeModelMapper userGradeModelMapper;

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private MchAdminModelMapper mchAdminModelMapper;

    @Autowired
    private MchAdminRecordModelMapper mchAdminRecordModelMapper;
    @Autowired
    private MchClassModelMapper       mchClassModelMapper;
    @Autowired
    private MchStoreWriteModelMapper  mchStoreWriteModelMapper;
    @Autowired
    private SignRecordModelMapper     signRecordModelMapper;

    @Autowired
    private PublicMemberService    publicMemberService;
    @Autowired
    private WriteRecordModelMapper writeRecordModelMapper;

    @Autowired
    private AppsMchExpressService appsMchExpressService;

    @Autowired
    private LivingRoomModelMapper livingRoomModelMapper;

    @Autowired
    private LivingConfigModelMapper livingConfigModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private DiyModelMapper diyModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void applyShop(ApplyShopVo vo) throws LaiKeAPIException
    {
        try
        {
            //登录验证
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //地址
            String address = vo.getCity_all().replace("-", "");
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCBNWK, "店铺名称不能为空");
            }
            else if (StringUtils.isEmpty(vo.getShop_range()))
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
            }
            else if (StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XXDZBNWK, "详细地址不能为空");
            }
            else if (Objects.isNull(vo.getCid()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZDPFL, "请选择店铺分类");
            }
            MchModel mch = new MchModel();
            mch.setRoomid(0);
            mch.setTel(vo.getTel());
            mch.setName(vo.getName());
            mch.setAddress(vo.getAddress());
            mch.setStore_id(vo.getStoreId());
            mch.setUser_id(user.getUser_id());
            mch.setRealname(vo.getRealname());
            mch.setID_number(vo.getID_number());
            mch.setShop_range(vo.getShop_range());
            mch.setCpc(vo.getCpc());
            mch.setShop_nature(vo.getShop_nature() + "");
            mch.setShop_information(vo.getShop_information());
            mch.setCid(vo.getCid());
            if (StringUtils.isNotEmpty(vo.getCity_all()))
            {
                List<String> citys   = Arrays.asList(vo.getCity_all().split(SplitUtils.HG));
                int          dataMax = 3;
                if (citys.size() == dataMax)
                {
                    mch.setSheng(citys.get(0));
                    mch.setShi(citys.get(1));
                    mch.setXian(citys.get(2));
                }
            }

            //验证店铺名称
            publiceService.checkIsSensitiveWords(vo.getStoreId(), vo.getName());
            //是否重复提交数据检查校验
            MchModel checkOld = new MchModel();
            //校验店铺数据
            mch = DataCheckTool.checkMchDataFormate(mch);
            //获取默认店铺头像
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), customerModelMapper.getStoreMchId(vo.getStoreId()));
            String         logo           = mchConfigModel.getLogo();
            mch.setLogo(logo);
            mch.setHead_img(mchConfigModel.getHead_img());
            mch.setPoster_img(mchConfigModel.getPoster_img());
            //获取api key
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WPZDWFW, "未配置定位服务", "applyShop");
            }
            //默认生成所属商城的pc店铺地址
            if (StringUtils.isNotEmpty(configModel.getPc_mch_path()))
            {
                mch.setPc_mch_path(configModel.getPc_mch_path() + "/?storeId=" + mch.getStore_id());
            }
//            //获取地址经纬度
//            try {
//                Map<String, String> latAndLogMap = TengxunMapUtil.getlatAndLng(configModel.getTencent_key(), address);
//                mch.setLatitude(latAndLogMap.get("lat"));
//                mch.setLongitude(latAndLogMap.get("lng"));
//            } catch (Exception ee) {
//                logger.error("获取店铺位置信息失败{}", ee.getMessage());
//            }

            if (vo.getImgUrls() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "请上传图片");
            }
            String[] imgUrls = vo.getImgUrls().split(SplitUtils.DH);

            if ("1".equals(mch.getShop_nature()) && imgUrls.length != 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            if ("0".equals(mch.getShop_nature()) && imgUrls.length != 2)
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
                mch.setBusiness_license(StringUtils.trim(stringBuilder.toString(), SplitUtils.DH));
            }
            mch.setStore_id(vo.getStoreId());
            mch.setUser_id(user.getUser_id());
            String roleId = userAuthorityModelMapper.getRoleIdByUserId(user.getUser_id());
            if (StringUtils.isNotEmpty(roleId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NYJCWDPGLYBNSQ, "您已经成为店铺管理员,不能申请");
            }
            BeanUtils.copyProperties(mch, checkOld);
            //是否继续申请店铺
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            mch.setAdd_time(new Date());
            //禅道50252
            if (mchConfigModel.getAuto_examine() == 0)
            {
                mch.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            }
            if (mchModel != null)
            {
                //数据是否重复校验
                checkOld.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                if (mchModelMapper.selectCount(checkOld) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SQXXWGXQWCXTJ, "申请信息未更新,请勿重复提交", "verifyStoreName");
                }
                if (!mchModel.getName().equals(vo.getName()) && mchModelMapper.verifyMchName(vo.getStoreId(), vo.getName()) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在", "verifyStoreName");
                }
                //如果店铺已注销状态则恢复并且重新审核
                if (String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK).equals(mchModel.getIs_lock()))
                {
                    mch.setIs_lock(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_NO));
                }
                //继续申请
                mch.setId(mchModel.getId());
                mch.setReview_result("");
                mch.setReview_status(DictionaryConst.MchExameStatus.EXAME_WAIT_STATUS.toString());
                //禅道50252
                if (mchConfigModel.getAuto_examine() == 0)
                {
                    mch.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
                }
                int count = lktMchModelMapper.updateByPrimaryKeySelective(mch);
                if (count < 1)
                {
                    logger.info("继续申请开通店铺失败 参数:" + JSON.toJSONString(mch));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPSQSB, "店铺申请失败");
                }
            }
            else
            {
                if (mchModelMapper.verifyMchName(vo.getStoreId(), vo.getName()) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在", "verifyStoreName");
                }
                //新申请
                int id = lktMchModelMapper.insertSelective(mch);
                if (id < 1)
                {
                    logger.info("申请开通店铺失败 参数:" + JSON.toJSONString(mch));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPSQSB, "店铺申请失败");
                }
                //添加跳转路径
                publicAdminService.addJumpPath(vo, mch.getId() + "", mch.getName(), JumpPathModel.JumpType.JUMP_TYPE0_MCH, JumpPathModel.JumpType.JUMP_TYPE_APP,
                        GloabConst.LaikeTuiUrl.JumpPath.MCH_APP, new String[]{mch.getId() + ""}, mch.getId(), null);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("申请店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "applyShop");
        }
    }


    @Override
    public Map<String, Object>  index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> mchDataMap = new HashMap<>(16);
            //商户商品设置
            String[] goodsSetups = new String[0];
            //店铺id
            int mchId;
            //审核状态 0=未申请 1=审核中 2=审核通过 3=审核不通过
            int status = DictionaryConst.UserMchApply.NOT_APPLIED;
            //是否开启了优惠券插件
            boolean isOpencoupon = publiceService.frontPlugin(vo.getStoreId(), null, DictionaryConst.Plugin.COUPON, null,false);
            //是否安装活动管理插件
            boolean pgFlag = publiceService.frontPlugin(vo.getStoreId(), DictionaryConst.Plugin.PLATFORMACTIVITIES, null);
            //是否显示自选功能 只有非自营店才显示自选功能
            boolean isZx = false;
            boolean isFb = false;
            //店铺头像
            String headImg = "";
            //自动审核天数
            Integer autoDay = 1;
            //店铺默认头像
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), customerModelMapper.getStoreMchId(vo.getStoreId()));
            if (mchConfigModel != null)
            {
                String goodsSetup = mchConfigModel.getCommodity_setup();
                if (goodsSetup.contains("1"))
                {
                    isFb = true;
                }
                goodsSetups = goodsSetup.split(SplitUtils.DH);
                headImg = publiceService.getImgPath(mchConfigModel.getHead_img(), vo.getStoreId());
                autoDay = mchConfigModel.getAuto_examine();
            }

            //查询是否有店铺
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                if (mchModel.getIs_lock().equals(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK)))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "店铺已被自动注销,请联系管理员恢复");
                }
                //获取自营店商户配置信息
                if (StringUtils.isNotEmpty(mchModel.getHead_img()))
                {
                    headImg = publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId());
                }
                mchDataMap.put("headImg", headImg);
                mchId = mchModel.getId();
                if (DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString().equals(mchModel.getReview_status()))
                {
                    status = DictionaryConst.UserMchApply.ADOPT;
                }
                else if (DictionaryConst.MchExameStatus.EXAME_NOT_PASS_STATUS.toString().equals(mchModel.getReview_status()))
                {
                    status = DictionaryConst.UserMchApply.FAIL;
                }
                else
                {
                    status = DictionaryConst.UserMchApply.UNDER_REVIEW;
                }
                mchModel.setLast_login_time(new Date());
                mchModelMapper.updateByPrimaryKeySelective(mchModel);
            }
            else
            {
                mchDataMap.put("mch_status", status);
                resultMap.put("status", status);
                resultMap.put("mch_data", mchDataMap);
                return resultMap;
            }
            String startDay = DateUtil.getStartOfDay(new Date());
            String endDay   = DateUtil.getEndOfDay(new Date());

            Map<String, Object> parmaMap = new HashMap<>(16);
            //统计代发货订单数量
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            //统计待发货以及是商家自配和快递的订单数量
            parmaMap.put("statusType", 3);
            parmaMap.put("recycle1", DictionaryConst.ProductRecycle.NOT_STATUS);
            int dfhNum = orderModelMapper.countOrdersNumDynamic(parmaMap);
            //统计今日订单数量
            parmaMap.remove("orderType");
            parmaMap.put("startDay", startDay);
            parmaMap.put("endDay", endDay);
            parmaMap.put("statusType", 1);
            List<String> orderTypeList = new ArrayList<>();
            orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            orderTypeList.add(DictionaryConst.OrdersType.ORDERS_HEADER_VI);
            parmaMap.put("orderTypeList", orderTypeList);
            int toDayOrderNum = orderModelMapper.countOrdersNumDynamic(parmaMap);
            //统计售后订单
            List<Integer> rTypeList = new ArrayList<>();
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_EXAMEWAIT_STATUS);
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_AGREE_REBACK);
            rTypeList.add(DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_USER_DELIVERED);
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("rTypeList", rTypeList);
            parmaMap.put("recycle1", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("orderType", DictionaryConst.OrdersType.ORDERS_HEADER_GM);
            parmaMap.put("notgys","notgys");
            int returnOrderNum = returnOrderModelMapper.countRturnOrderNumDynamic(parmaMap);
            //获取访客数
            MchBrowseModel mchBrowseModel = new MchBrowseModel();
            mchBrowseModel.setStore_id(vo.getStoreId());
            mchBrowseModel.setMch_id(mchId + "");
            int mchBrowseNum = mchBrowseModelMapper.countMchBrowseNum(mchBrowseModel);
            //获取今日收入
            parmaMap.clear();
            BigDecimal income = orderModelMapper.mchToDayMoney(mchId, DateUtil.getStartOfDay(new Date()), DateUtil.getEndOfDay(new Date()));
            //是否存在门店
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(vo.getStoreId());
            mchStoreModel.setMch_id(mchId);
            int mchStatus = mchStoreModelMapper.selectCount(mchStoreModel);
            if (mchStatus > 0)
            {
                mchStatus = 1;
            }
            boolean isPromiseSwitch = false;
            if (mchConfigModel != null && mchConfigModel.getPromise_switch() == 1)
            {
                isPromiseSwitch = true;
                boolean isPromisePay = false;
                //是否缴纳保证金
                try
                {
                    isPromisePay = appsMchPromiseService.isPromisePay(vo);
                }
                catch (LaiKeAPIException l)
                {
                    logger.debug(l.getMessage());
                }
                mchDataMap.put("isPromisePay", isPromisePay);
                //判断保证金是否在退还中
                boolean examineStatus = appsMchPromiseService.examineStatus(mchModel.getId());
                mchDataMap.put("isPromiseExamine", examineStatus);
            }
            else if (mchConfigModel != null && mchConfigModel.getPromise_switch() == 0)
            {
                //可以直接操作
                mchDataMap.put("isPromiseExamine", true);
            }
            mchDataMap.put("isPromiseSwitch", isPromiseSwitch);
            //获取上架商品数量
            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("notSupplier", "notSupplier");
            parmaMap.put("status", DictionaryConst.GoodsStatus.NEW_GROUNDING);
            mchDataMap.put("goodsUpNum", productListModelMapper.countDynamic(parmaMap));


            mchDataMap.put("income", income == null ? 0 : income.toString());
            mchDataMap.put("shop_id", mchId);
            mchDataMap.put("order_num", toDayOrderNum);
            mchDataMap.put("mch_status", mchStatus);
            mchDataMap.put("name", mchModel.getName());
            mchDataMap.put("logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
            mchDataMap.put("visitor_num", mchBrowseNum);
            mchDataMap.put("order_num1", dfhNum);
            mchDataMap.put("order_num2", returnOrderNum);
            mchDataMap.put("cpc", mchModel.getCpc());
            mchDataMap.put("tel", mchModel.getTel());
            mchDataMap.put("order_num2", returnOrderNum);
            mchDataMap.put("roomid", mchModel.getRoomid());
            mchDataMap.put("commodity_setup", goodsSetups);
            mchDataMap.put("review_result", mchModel.getReview_result());
            mchDataMap.put("account_money", mchModel.getAccount_money().add(mchModel.getCashable_money()).toString());
            mchDataMap.put("shop_information", mchModel.getShop_information());
            mchDataMap.put("collection_code", publiceService.getImgPath(mchModel.getCollection_code(), vo.getStoreId()));
            //是否开启自选
            if (Arrays.asList(goodsSetups).contains("2"))
            {
                AdminModel adminModel = adminModelMapper.getAdminCustomer(vo.getStoreId());
                if (adminModel != null)
                {
                    if (!adminModel.getShop_id().equals(mchModel.getId()))
                    {
                        isZx = true;
                    }
                }
            }

            //订单(待发货)、订单(售后)、订单(提醒发货)、订单(订单关闭)、订单(新订单)、订单(收货)、商品(审核)、商品(补货)、h5店铺保证金审核消息通知、h5店铺商品审核消息通知
            List<Integer>       orderTypes = Arrays.asList(1, 2, 3, 4, 5, 15, 6, 9, 16, 17, 22, 23);
            Map<String, Object> map        = new HashMap<>(16);
            map.put("store_id", vo.getStoreId());
            map.put("mch_id", mchId);
            map.put("pageStart", vo.getPageNo());
            map.put("pageEnd", vo.getPageSize());
            map.put("orderTypes", orderTypes);
            map.put("read_or_not", 0);
            //未读消息
            int noread = messageLoggingModalMapper.countDynamic(map);

            //是否缴纳保证金
            boolean isPayment = publicMchService.judgeMchPromise(vo, user.getUser_id());

            resultMap.put("is_Payment", isPayment);
            resultMap.put("noread", noread);
            resultMap.put("zx", isZx);
            resultMap.put("isFb",isFb);
            resultMap.put("status", status);
            resultMap.put("is_lock", mchModel.getIs_lock());
            resultMap.put("pa_flag", pgFlag);
            resultMap.put("mch_data", mchDataMap);
            resultMap.put("auto_examine", autoDay);
            resultMap.put("coupon_status", isOpencoupon);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的店铺首页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bankList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> bankList = new ArrayList<>();
            User                      user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchModel                  mchModel = new MchModel();
            mchModel.setStore_id(user.getStore_id());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("mch_id", mchModel.getId());
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> bankCardModelList = bankCardModelMapper.selectDynamic(parmaMap);

            for (Map<String, Object> bankCard : bankCardModelList)
            {
                Map<String, Object> bankMap = new HashMap<>(16);
                bankMap.put("id", MapUtils.getIntValue(bankCard, "id"));
                bankMap.put("Bank_name", MapUtils.getString(bankCard, "Bank_name"));
                String bankNo = MapUtils.getString(bankCard, "Bank_card_number");
                bankMap.put("Bank_card_number", bankNo.substring(bankNo.length() - 4));
                bankMap.put("is_default", MapUtils.getInteger(bankCard, "is_default"));
                bankMap.put("branchName", MapUtils.getString(bankCard, "branch"));
                bankList.add(bankMap);
            }
            resultMap.put("list", bankList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bankList");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBank(AddBankVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取用户店铺
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(user.getMchId());
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }

            //验证银行卡信息
            verificationBank(vo, vo.getBankName(), vo.getBankCardNumber());

            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setStore_id(vo.getStoreId());
            bankCardModel.setMch_id(user.getMchId());
            bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            bankCardModel.setBank_card_number(vo.getBankCardNumber());
            if (bankCardModelMapper.selectCount(bankCardModel) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKYCZ, "银行卡已存在");
            }
            //查询是否存在默认银行卡,没有默认则这个银行卡为默认
            bankCardModel.setUser_id(user.getUser_id());
            bankCardModel.setBank_card_number(null);
            bankCardModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            BankCardModel bankCardModelTemp = bankCardModelMapper.selectOne(bankCardModel);
            if (bankCardModelTemp != null)
            {
                //判断此银行卡是否为默认
                if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
                {
                    //清空之前默认
                    BankCardModel bankCardModelUpdate = new BankCardModel();
                    bankCardModelUpdate.setId(bankCardModelTemp.getId());
                    bankCardModelUpdate.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                    if (bankCardModelMapper.updateByPrimaryKeySelective(bankCardModelUpdate) < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
                    }
                }
                else
                {
                    bankCardModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                }
            }
            bankCardModel.setBank_card_number(vo.getBankCardNumber());
            bankCardModel.setCardholder(vo.getCardholder());
            bankCardModel.setBank_name(vo.getBankName());
            bankCardModel.setBranch(vo.getBranchName());
            bankCardModel.setMch_id(mchModel.getId());
            bankCardModel.setAdd_date(new Date());
            //添加银行卡
            int count = bankCardModelMapper.insertSelective(bankCardModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBank");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBank(AddBankVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证银行卡信息
            verificationBank(vo, vo.getBankName(), vo.getBankCardNumber());
            BankCardModel bankCardModel = bankCardModelMapper.selectByPrimaryKey(vo.getId());
            if (!Objects.isNull(bankCardModel))
            {
                bankCardModel.setStore_id(vo.getStoreId());
                bankCardModel.setMch_id(user.getMchId());
                bankCardModel.setBank_card_number(vo.getBankCardNumber());
                bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                if (!bankCardModel.getBank_card_number().equals(vo.getBankCardNumber()) && bankCardModelMapper.selectCount(bankCardModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKYCZ, "银行卡已存在");
                }
                bankCardModel.setUser_id(user.getUser_id());
                bankCardModel.setCardholder(vo.getCardholder());
                bankCardModel.setBank_name(vo.getBankName());
                bankCardModel.setBranch(vo.getBranchName());
                bankCardModel.setIs_default(vo.getIsDefault());
                if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
                {
                    //查询是否存在默认银行卡,没有默认则这个银行卡为默认
                    BankCardModel oldBankCardModel = new BankCardModel();
                    oldBankCardModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                    oldBankCardModel.setMch_id(user.getMchId());
                    oldBankCardModel.setStore_id(vo.getStoreId());
                    oldBankCardModel.setUser_id(user.getUser_id());
                    oldBankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    oldBankCardModel = bankCardModelMapper.selectOne(oldBankCardModel);
                    if (!Objects.isNull(oldBankCardModel))
                    {
                        //清空之前默认
                        oldBankCardModel.setIs_default(0);
                        if (bankCardModelMapper.updateByPrimaryKeySelective(oldBankCardModel) < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }
            //编辑银行卡
            int count = bankCardModelMapper.updateByPrimaryKey(bankCardModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFMQSHZS, "网络繁忙,请稍后再试");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateBank");
        }
    }


    @Autowired
    private BankTool bankTool;

    private void verificationBank(MainVo vo, String bankName, String bankCardNumber) throws LaiKeAPIException
    {
        try
        {
            //是否为数字
            if (!bankTool.isNumber(bankCardNumber))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKGSCW, "银行卡格式错误");
            }
            //获取银行名称
            String name = bankTool.getBankNameByNo(bankCardNumber);
            if (StringUtils.isEmpty(name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKGSCW, "银行卡格式错误");
            }
            else
            {
                name = name.substring(0, name.indexOf("-"));
                if (!StringUtils.isEmpty(bankName))
                {
                    if (!name.equals(bankName))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKXXBPP, "银行卡信息不匹配");
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
            logger.error("校验银行卡异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationBank");
        }
    }

    @Override
    public Map<String, Object> mchBankInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> bankList      = new ArrayList<>();
            User                      user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            BankCardModel             bankCardModel = new BankCardModel();
            bankCardModel.setId(id);
            bankCardModel.setStore_id(vo.getStoreId());
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(user.getStore_id());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            bankCardModel.setMch_id(mchModel.getId());
            bankCardModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<BankCardModel> bankCardModelList = bankCardModelMapper.select(bankCardModel);
            if (id == null)
            {
                for (BankCardModel bankCard : bankCardModelList)
                {
                    Map<String, Object> bankMap = new HashMap<>(16);
                    bankMap.put("id", bankCard.getId());
                    bankMap.put("Bank_name", bankCard.getBank_name());
                    String bankNo = bankCard.getBank_card_number();
                    bankMap.put("Bank_card_number", bankNo.substring(bankNo.length() - 4));
                    bankList.add(bankMap);
                }
                resultMap.put("list", bankList);
            }
            else
            {
                resultMap.put("bank", bankCardModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据id获取店铺银行卡信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchBankInfo");
        }
        return resultMap;
    }

    @Override
    public boolean verifyStoreName(MainVo vo, String name) throws LaiKeAPIException
    {
        try
        {
            publiceService.checkIsSensitiveWords(vo.getStoreId(), name);
                RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
                Integer count = mchModelMapper.verifyMchName(vo.getStoreId(), name);
                if (count == null || count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在", "verifyStoreName");
                }
                return true;
//            }
//            else
//            {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCBHF, "店铺名称不合法", "verifyStoreName");
//            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证店铺名称 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verifyStoreName");
        }
    }

    @Override
    public Map<String, Object> continueApply(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            List<Object> imgUrls = new ArrayList<>();
            if (mchModel != null)
            {
                String imgUlr = mchModel.getLogo();
                imgUlr = publiceService.getImgPath(imgUlr, vo.getStoreId());
                mchModel.setLogo(imgUlr);
                if (StringUtils.isNotEmpty(mchModel.getBusiness_license()))
                {
                    String[] businessUrl = mchModel.getBusiness_license().split(SplitUtils.DH);
                    for (String img : businessUrl)
                    {
                        imgUrls.add(publiceService.getImgPath(img, vo.getStoreId()));
                    }
                }
                mchModel.setBusiness_license(StringUtils.stringImplode(imgUrls, SplitUtils.DH, false));

                resultMap.put("list", mchModel);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WSQJL, "无申请记录", "continueApply");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("继续申请店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "continueApply");
        }

        return resultMap;
    }



    @Override
    public Map<String, Object> addGoodsPage(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //两级类型集
        List<Map<String, Object>> productClassList = new ArrayList<>();
        //自选商品集
        List<Map<String, Object>> ziXuanGoodsList = new ArrayList<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //与分类绑定的品牌
            List<Map<String, Object>> brandClassModelList = new ArrayList<>();
            //所有一级分类id集
            Set<Integer> topClassIds = new HashSet<>();
            //获取两级产品类型
            ProductClassModel productClassModel = new ProductClassModel();
            productClassModel.setStore_id(vo.getStoreId());
            productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            productClassModel.setSid(0);
            //第一级
            List<ProductClassModel> productClassLeve1 = productClassModelMapper.select(productClassModel);
            for (ProductClassModel productTopClass : productClassLeve1)
            {
                Map<String, Object> leve1Map = new HashMap<>(16);
                leve1Map.put("cid", productTopClass.getCid());
                leve1Map.put("pname", productTopClass.getPname());
                //获取第二级
                List<Map<String, Object>> productClassList2 = new ArrayList<>();
                productClassModel.setSid(productTopClass.getCid());
                List<ProductClassModel> productClassLeve2 = productClassModelMapper.select(productClassModel);
                for (ProductClassModel productClass2 : productClassLeve2)
                {
                    Map<String, Object> leve2Map = new HashMap<>(16);
                    leve2Map.put("cid", productClass2.getCid());
                    leve2Map.put("pname", productClass2.getPname());
                    productClassList2.add(leve2Map);
                }
                topClassIds.add(productTopClass.getCid());
                leve1Map.put("res", productClassList2);
                productClassList.add(leve1Map);
            }
            //获取品牌信息
            Map<String, Object> bandMap = new HashMap<>(16);
            bandMap.put("store_id", vo.getStoreId());
            bandMap.put("categoriesList", topClassIds);
            brandClassModelList = brandClassModelMapper.getBrandClassDynamic(bandMap);
            //是否分佣
            boolean      freightStatus = false;
            FreightModel freightModel  = new FreightModel();
            freightModel.setStore_id(vo.getStoreId());
            freightModel.setMch_id(shopId);
            if (freightModelMapper.selectCount(freightModel) > 0)
            {
                freightStatus = true;
            }
            //获取当前商城自营店id
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            //当前店铺是否是自营店铺 自营店不能自选商品
            if (mchId != null && mchId != shopId)
            {
                Map<String, Object> parmaMap           = new HashMap<>(16);
                List<Integer>       checkedGoodsIdList = publiceService.getCheckedZiXuanGoodsList(vo.getStoreId(), shopId);
                //店铺不能自己选择自己的商品
                parmaMap.put("store_id", vo.getStoreId());
                List<Integer> goodsStatus = new ArrayList<>();
                goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
                parmaMap.put("GoodsStatus", goodsStatus);
                parmaMap.put("is_zixuan", 1);
                parmaMap.put("shop_id", mchId);
                parmaMap.put("not_checkZx_mchId", user.getMchId());
                parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("addDate_sort", DataUtils.Sort.DESC.toString());
                if (checkedGoodsIdList != null && checkedGoodsIdList.size() > 0)
                {
                    parmaMap.put("notInGoodsId", checkedGoodsIdList);
                }
                parmaMap.put("pageNo", vo.getPageNo());
                parmaMap.put("pageSize", vo.getPageSize());
                parmaMap.put("notSupplierPro", "notSupplierPro");
                //225 不展示虚拟商品
                parmaMap.put("commodity_type", "0");
                ziXuanGoodsList = productListModelMapper.getProductListDynamic(parmaMap);
                for (Map<String, Object> map : ziXuanGoodsList)
                {
                    String imgUrl = productImgModelMapper.getProductImg(MapUtils.getIntValue(map, "id"));
                    imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                    map.put("imgurl", imgUrl);
                }
            }

            resultMap.put("product_class_list", productClassList);
            resultMap.put("brand_class_list", brandClassModelList);
            resultMap.put("freight_status", freightStatus);
            resultMap.put("list", ziXuanGoodsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsPage");
        }
        return resultMap;
    }



    @Override
    public Map<String, Object> addGoodsPageLoad(AddGoodsPageLoadVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        //自选商品集
        List<Map<String, Object>> ziXuanGoodsList = new ArrayList<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //当前店铺是否是自营店铺 自营店不能自己选择自己的商品
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (mchId != null && mchId != vo.getShopId())
            {
                Map<String, Object> parmaMap           = new HashMap<>(16);
                List<Integer>       checkedGoodsIdList = publiceService.getCheckedZiXuanGoodsList(vo.getStoreId(), vo.getShopId());
                //当前店铺是否是自营店铺 自营店不能自己选择自己的商品
                parmaMap.put("store_id", vo.getStoreId());
                List<Integer> goodsStatus = new ArrayList<>();
                goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING);
                parmaMap.put("GoodsStatus", goodsStatus);
                parmaMap.put("is_zixuan", 1);
                parmaMap.put("shop_id", mchId);
                parmaMap.put("not_checkZx_mchId", user.getMchId());
                if (checkedGoodsIdList != null && checkedGoodsIdList.size() > 0)
                {
                    parmaMap.put("notInGoodsId", checkedGoodsIdList);
                }
                if (vo.getProductClassId() != null && vo.getProductClassId() != 0)
                {
                    //一级分类
                    parmaMap.put("classLeve", vo.getProductClassId());
                    if (vo.getProductClassId1() != null && vo.getProductClassId1() != 0)
                    {
                        //二级分类 如果传了二级分类则查找二级分类
                        parmaMap.put("classLeve", vo.getProductClassId1());
                    }
                }
                //品牌id
                if (vo.getBrandId() != null && vo.getBrandId() != 0)
                {
                    parmaMap.put("brand_id", vo.getBrandId());
                }
                if (!StringUtils.isEmpty(vo.getProName()))
                {
                    parmaMap.put("product_title", vo.getProName());
                }

                parmaMap.put("addDate_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
                parmaMap.put("pageNo", vo.getPageNo());
                parmaMap.put("pageSize", vo.getPageSize());
                parmaMap.put("notSupplierPro", "notSupplierPro");
                parmaMap.put("commodity_type", "0");
                ziXuanGoodsList = productListModelMapper.getProductListDynamic(parmaMap);
                for (Map<String, Object> map : ziXuanGoodsList)
                {
                    String imgUrl = productImgModelMapper.getProductImg(MapUtils.getIntValue(map, "id"));
                    imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                    map.put("imgurl", imgUrl);
                }
                //获取品牌信息
                Map<String, Object> bandMap = new HashMap<>(16);
                bandMap.put("store_id", vo.getStoreId());
                bandMap.put("categories", vo.getProductClassId());
                List<Map<String, Object>> brandClassDynamic = brandClassModelMapper.getBrandClassDynamic(bandMap);
                resultMap.put("brand_class_list", brandClassDynamic);
            }

            resultMap.put("list", ziXuanGoodsList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品页面-加载更多 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsPageLoad");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addGoods(MainVo vo, int shopId, String proId, String labbelId, Integer freightId) throws LaiKeAPIException
    {
        try
        {
            int count;
            //获取产品id集
            String[] proIdList = proId.split(",");
            User     user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            if (freightId == null || freightId == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYFMB, "请选择运费模板");
            }

            //获取店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setId(shopId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                AdminModel admin = adminModelMapper.getAdminCustomer(vo.getStoreId());
                if (admin.getShop_id().equals(mchModel.getId()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZYDBNZX, "自营店不能自选");
                }
                for (String goodsId : proIdList)
                {
                    //自选商品id
                    int goodsOldId;
                    //生成商品编码
                    String goodsNo = BuilderIDTool.getGuid();
                    //查询商品信息
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setStore_id(vo.getStoreId());
                    productListModel.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                    productListModel.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
                    productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    productListModel.setActive(DictionaryConst.GoodsActive.GOODSACTIVE_POSITIVE_PRICE.toString());
                    productListModel.setId(Integer.parseInt(goodsId));
                    productListModel = productListModelMapper.selectOne(productListModel);
                    if (productListModel != null)
                    {
                        //自选商品id
                        goodsOldId = productListModel.getId();
                        //获取运费id
                        FreightModel freightModel = new FreightModel();
                        freightModel.setId(freightId);
                        freightModel.setMch_id(shopId);
                        freightModel.setStore_id(vo.getStoreId());
                        freightModel = freightModelMapper.selectOne(freightModel);
                        if (freightModel == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFMBBCZ, "运费模板不存在");
                        }
                        //重置商品业务数据
                        productListModel.setZixuan_id(goodsOldId);
                        productListModel.setId(null);
                        productListModel.setFreight(freightModel.getId().toString());
                        productListModel.setProduct_number(goodsNo);
                        productListModel.setUpper_shelf_time(new Date());
                        productListModel.setAdd_date(new Date());
                        productListModel.setMch_id(shopId);
                        productListModel.setPublisher(mchModel.getName());
                        productListModel.setVolume(0);
                        productListModel.setSearch_num(0);
                        productListModel.setSort(productListModelMapper.getGoodsMaxSortByMch(vo.getStoreId(), mchModel.getId()));
                        if (StringUtils.isEmpty(labbelId))
                        {
                            labbelId = "";
                        }
                        productListModel.setLabel(labbelId);
                        int gid = productListModelMapper.insertSelective(productListModel);
                        if (gid < 1)
                        {
                            logger.info("商品保存失败 参数:" + JSON.toJSONString(productListModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                        gid = productListModel.getId();

                        //查询自选商品轮播图
                        ProductImgModel productImgModel = new ProductImgModel();
                        productImgModel.setProduct_id(goodsOldId);
                        List<ProductImgModel> productImgModelList = productImgModelMapper.select(productImgModel);
                        for (ProductImgModel productImg : productImgModelList)
                        {
                            //添加新得商品轮播图
                            productImg.setId(null);
                            productImg.setAdd_date(new Date());
                            productImg.setProduct_id(gid);
                            count = productImgModelMapper.insertSelective(productImg);
                            if (count < 1)
                            {
                                logger.info("商品轮播图添加失败 参数:" + JSON.toJSONString(productImg));
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                            }
                        }
                        //获取自选商品属性信息
                        ConfiGureModel confiGureModel = new ConfiGureModel();
                        confiGureModel.setPid(goodsOldId);
                        confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                        List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
                        for (ConfiGureModel confiGure : confiGureModelList)
                        {
                            //复制商品属性信息
                            confiGure.setPid(gid);
                            confiGure.setId(null);
                            confiGure.setAttribute_str("");
                            confiGure.setCtime(new Date());

                            int attributeId = confiGureModelMapper.insertSelective(confiGure);
                            if (attributeId < 1)
                            {
                                logger.info("添加商品属性失败 参数:" + JSON.toJSONString(confiGure));
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                            }
                            attributeId = confiGure.getId();
                            //添加库存记录信息
                            String     content    = user.getUser_id() + "增加商品总库存:" + confiGure.getNum();
                            StockModel stockModel = new StockModel();
                            stockModel.setStore_id(vo.getStoreId());
                            stockModel.setProduct_id(gid);
                            stockModel.setAttribute_id(attributeId);
                            stockModel.setTotal_num(confiGure.getNum());
                            stockModel.setFlowing_num(confiGure.getNum());
                            stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                            stockModel.setContent(content);
                            stockModel.setAdd_date(new Date());
                            count = stockModelMapper.insertSelective(stockModel);
                            if (count < 1)
                            {
                                logger.info("库存入库记录失败 参数:" + JSON.toJSONString(stockModel));
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                            }
                            //库存是否达到阀值
                            if (productListModel.getMin_inventory() >= confiGure.getNum())
                            {
                                //记录一条库存预警信息
                                content = "预警";
                                stockModel.setId(null);
                                stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                                stockModel.setContent(content);
                                stockModel.setAdd_date(new Date());
                                count = stockModelMapper.insertSelective(stockModel);
                                if (count < 1)
                                {
                                    logger.info("库存预警记录失败 参数:" + JSON.toJSONString(stockModel));
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSPBFHTJ, "该商品不符合条件", "addGoods");
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
            logger.error("添加商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoods");
        }
    }


    @Override
    public Map<String, Object> getZxGoodsInfoById(MainVo vo, int shopId, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> editPageMap = new HashMap<>(16);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                editPageMap = publicGoodsService.editPage(vo.getStoreId(), user.getZhanghao(), shopId, goodsId, GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH);
                //插件流程暂时不做...
            }
            resultMap.put("attr", editPageMap.get("attr_group_list"));
            resultMap.put("attrList", editPageMap.get("checked_attr_list"));
            resultMap.put("richList", editPageMap.get("richList"));
            resultMap.put("content", editPageMap.get("content"));
            resultMap.put("brand_list", editPageMap.get("brand_class"));
            resultMap.put("freight_list", editPageMap.get("freight_list"));
            resultMap.put("unit", editPageMap.get("unit"));
            resultMap.put("s_type", editPageMap.get("sp_type"));
            resultMap.put("plugin_list", editPageMap.get("Plugin_arr"));
            resultMap.put("distributors", editPageMap.get("distributors"));
            resultMap.put("list", editPageMap.get("list"));
            resultMap.put("product_class_list1", editPageMap.get("ctypes"));
            resultMap.put("brand_class_list1", editPageMap.get("brand_class_list1"));
            resultMap.put("freight_list1", editPageMap.get("freight_list1"));
            resultMap.put("video", editPageMap.get("video"));
            resultMap.put("proVideo", editPageMap.get("proVideo"));
            resultMap.put("distributors1", editPageMap.get("distributors1"));
            resultMap.put("show_adr", editPageMap.get("show_adr"));
            resultMap.put("imgurls", editPageMap.get("imgurls"));
            resultMap.put("initial", editPageMap.get("initial"));
            resultMap.put("status", editPageMap.get("status"));
            resultMap.put("cover_map", editPageMap.get("cover_map"));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取自选商品信息 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getZxGoodsInfoById");
        }
        return resultMap;
    }

    @Override
    @HttpApiMethod(urlMapping = "mch.app.upload_merchandise_page")
    public Map<String, Object> uploadMerchandisePage(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //添加虚拟商品时，判断是否有门店
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setMch_id(shopId);
            List<MchStoreModel> mch_store = mchStoreModelMapper.select(mchStoreModel);
            //为0时表示无门店
            int haveStore = 0;
            if (mch_store != null && mch_store.size() > 0)
            {
                haveStore = 1;
            }
            resultMap = publicGoodsService.addPage(vo, user.getZhanghao(), shopId, GloabConst.LktConfig.LKT_CONFIG_TYPE_PC);
            resultMap.put("haveStore", haveStore);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传商品页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "uploadMerchandisePage");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getClass(MainVo vo, Integer classId, Integer brandId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> resultClassMap = publicGoodsService.getClassifiedBrands(vo, classId, brandId);
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
            logger.error("选择商品类别 异常" + e.getMessage());
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
            Integer sid = 0;
            //获取所有属性
            List<Map<String, Object>> resultClassList = publicGoodsService.attribute1(vo, null, DataUtils.convertToList(attributeId), sid);
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
    public Map<String, Object> getAttributeValue(MainVo vo, String attributes, String attrValues) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Integer sid = 0;
            Map<String, Object> attributeNameMap = new HashMap<>(16);
            String[]            attributeNames   = attributes.split(SplitUtils.DH);
            if (attributeNames.length > 0)
            {
                for (String attributeName : attributeNames)
                {
                    if (!StringUtils.isEmpty(attrValues))
                    {
                        //获取属性json
                        List<Map<String, Object>> attrValueMap = JSON.parseObject(attrValues, new TypeReference<List<Map<String, Object>>>()
                        {
                        });
                        //获取当前属性对应的值
                        for (Map<String, Object> map : attrValueMap)
                        {
                            String attrKey = map.get("attr_group_name") + "";
                            if (!StringUtils.isEmpty(attrKey))
                            {
                                if (attrKey.equals(attributeName))
                                {
                                    attributeNameMap.putAll(publicGoodsService.attributeName1(vo.getStoreId(), attributeName, map,sid));
                                }
                            }
                        }
                    }
                    else
                    {
                        attributeNameMap.putAll(publicGoodsService.attributeName1(vo.getStoreId(), attributeName, null,sid));
                    }
                }
            }
            resultMap.put("list", attributeNameMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取属性值 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAttributeName");
        }
        return resultMap;
    }


    /**
     * @param vo     -
     * @param shopId -
     * @param type   -//type为2时实物商品的上下架，为1，3实物商品的待审核
     * @param status -为不同状态的商品
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Map<String, Object> myMerchandise(MainVo vo, Integer shopId, String type, Integer status, Integer commodity_type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> rmap = new HashMap<>(16);
            if (user != null)
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("page", vo.getPageNo());
                map.put("pagesize", vo.getPageSize());
                map.put("pageto", "");
                List<Integer> mchStatusList = new ArrayList<>();
                String[]      split         = type.split(SplitUtils.DH);
                for (String mchStatus : split)
                {
                    mchStatusList.add(Integer.valueOf(mchStatus));
                }
                map.put("mchStatusList", mchStatusList);
                if (!Objects.isNull(status))
                {
                    map.put("status", status);
                }
                map.put("mch_id", user.getMchId());
                map.put("commodity_type", commodity_type);

                String langCode = vo.getLang_code();
                if (StringUtils.isNotEmpty(langCode))
                {
                    map.put("lang_code", langCode);
                }

                rmap = publicGoodsService.productList(vo.getStoreId(), user.getZhanghao(), shopId, GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH, map);
//                rmap = publicGoodsService.productList(vo, user.getZhanghao(), shopId, GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH, map);
            }
            //是否缴纳保证金
            boolean isPayment = publicMchService.judgeMchPromise(vo, user.getUser_id());
            if (user.getMchId() != null)
            {
                boolean examineStatus = appsMchPromiseService.examineStatus(user.getMchId());
                resultMap.put("isPromiseExamine", examineStatus);
            }
            resultMap.put("is_Payment", isPayment);
            resultMap.put("list", rmap.get("list"));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("我的商品列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "myMerchandise");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> modify(MainVo vo, int shopId, int pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> editPageMap = new HashMap<>(16);

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                editPageMap = publicGoodsService.editPage(vo, user.getZhanghao(), shopId, pid, GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH);
                //插件流程暂时不做...
            }
            //店铺门店信息
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(vo.getStoreId());
            mchStoreModel.setMch_id(shopId);
            List<MchStoreModel> mchStoreModels = mchStoreModelMapper.select(mchStoreModel);
            if (mchStoreModels != null && mchStoreModels.size() > 0)
            {
                resultMap.put("mchStoreModels", mchStoreModels);
            }
            resultMap.put("attr", editPageMap.get("attr_group_list"));
            resultMap.put("attrList", editPageMap.get("checked_attr_list"));
            resultMap.put("richList", editPageMap.get("richList"));
            resultMap.put("content", editPageMap.get("content"));
            resultMap.put("brand_list", editPageMap.get("brand_class"));
            resultMap.put("freight_list", editPageMap.get("freight_list"));
            resultMap.put("unit", editPageMap.get("unit"));
            resultMap.put("s_type", editPageMap.get("sp_type"));
            resultMap.put("plugin_list", editPageMap.get("Plugin_arr"));
            resultMap.put("distributors", editPageMap.get("distributors"));
            resultMap.put("list", editPageMap.get("list"));
            resultMap.put("product_class_list1", editPageMap.get("ctypes"));
            resultMap.put("brand_class_list1", editPageMap.get("brand_class_list1"));
            resultMap.put("freight_list1", editPageMap.get("freight_list1"));
            resultMap.put("video", editPageMap.get("video"));
            resultMap.put("proVideo", editPageMap.get("proVideo"));
            resultMap.put("distributors1", editPageMap.get("distributors1"));
            resultMap.put("show_adr", editPageMap.get("show_adr"));
            resultMap.put("imgurls", editPageMap.get("imgurls"));
            resultMap.put("initial", editPageMap.get("initial"));
            resultMap.put("status", editPageMap.get("status"));
            resultMap.put("cover_map", editPageMap.get("cover_map"));
            resultMap.put("write_off_mch_names", editPageMap.get("writeOffMchName"));
            resultMap.put("writeOffMchIds", editPageMap.get("writeOffMchIds"));

            String langCode = MapUtils.getString(editPageMap, "lang_code");
            String langName = publiceService.getLangName(langCode);
            resultMap.put("lang_code", langCode);
            resultMap.put("lang_name", langName);

            Integer country_num  = MapUtils.getInteger(editPageMap, "country_num");
            String  country_name = publiceService.getCountryName(country_num);
            resultMap.put("country_num", country_num);
            resultMap.put("country_name", country_name);


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑商品加载数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modify");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveGoods(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //校验店铺信息
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());

            publicGoodsService.addProduct(vo, user.getUser_id(), vo.getShopId(), GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存商品 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveGoods");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean reEdit(UploadMerchandiseVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());

                return publicGoodsService.editProduct(vo, user.getZhanghao(), vo.getShopId(), GloabConst.LktConfig.LKT_CONFIG_TYPE_MCH);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("重新编辑商品 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "reEdit");
        }
        return false;
    }

    @Override
    public Map<String, Object> upStockPage(MainVo vo, int shopId, int pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //校验店铺信息
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            //属性详细信息
            List<List<Map<String, Object>>> attrubiteInfoList = new ArrayList<>();
            //属性集 [key]:[value]...
            List<Map<String, Object>> attrGroupList = new ArrayList<>();
            //属性一对多 key->[value]
            Map<String, Set<String>> attrbuiteValueMap = new LinkedHashMap<>(16);

            //获取商品+属性信息
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            parmaMap.put("pid", pid);
            parmaMap.put("no_status", "no_status");

            List<Map<String, Object>> goodsInfoMap = productListModelMapper.getProductListJoinConfigureDynamic(parmaMap);
            for (Map<String, Object> map : goodsInfoMap)
            {
                //属性详细信息
                Map<String, Object> info = new HashMap<>(16);
                //当前规格库存数量
                int stockNum = StringUtils.stringParseInt(map.get("stockNum").toString());
                //当前规格库存预警
                int attrMinInventory = StringUtils.stringParseInt(map.get("attrMinInventory").toString());
                //当前规格id
                int cid = StringUtils.stringParseInt(map.get("attrId").toString());

                info.put("stock", stockNum);
                info.put("stockWarn", attrMinInventory);
                info.put("cid", cid);
                info.put("addStockNum", 0);

                String attrbuiteStr = map.get("attribute").toString();
                int    index        = attrbuiteStr.indexOf("_LKT_");
                //处理属性
                if (index >= 0)
                {
                    Map<String, Object> attrbuiteMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attrbuiteStr, Map.class));
                    assert attrbuiteMap != null;
                    List<Map<String, Object>> attrGroupSunList = new ArrayList<>();
                    //列出当前属性 ->{"颜色_LKT_1":"白色_LKT_124","尺码_LKT_8":"M_LKT_9"}
                    for (String key : attrbuiteMap.keySet())
                    {
                        Map<String, Object> tempAttrInfoMap = new HashMap<>(16);
                        tempAttrInfoMap.putAll(info);
                        String value = attrbuiteMap.get(key).toString();
                        //颜色
                        key = key.substring(0, key.indexOf("_LKT"));
                        //白色
                        value = value.substring(0, value.indexOf("_LKT"));
                        tempAttrInfoMap.put("name", key);
                        tempAttrInfoMap.put("value", value);

                        //属性值集(属性值可能会有重复,需要去重) 如果规格存在，则在当前规格上添加对应属性值
                        Set<String> valueNameList = new HashSet<>();
                        if (!attrbuiteValueMap.containsKey(key))
                        {
                            attrbuiteValueMap.put(key, valueNameList);
                        }
                        attrbuiteValueMap.get(key).add(value);
                        attrGroupSunList.add(tempAttrInfoMap);
                    }
                    attrubiteInfoList.add(attrGroupSunList);
                }
            }

            //组装数据 -> [{"attrName":["颜色"],"attrValue":[]},{"attrName":["尺码"],"attrValue":[]}]
            for (String key : attrbuiteValueMap.keySet())
            {
                //一对多属性集 attrValue:[key],attrName:[value]
                Map<String, Object> attrbuiteMapTemp = new HashMap<>(16);
                attrbuiteMapTemp.put("attrName", key);
                attrbuiteMapTemp.put("attrValue", attrbuiteValueMap.get(key));
                attrGroupList.add(attrbuiteMapTemp);
            }

            resultMap.put("attr", attrGroupList);
            resultMap.put("attrList", attrubiteInfoList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载商品规格库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upStockPage");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void upStock(MainVo vo, int shopId, String confiGureModels) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //校验店铺信息
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            //参数处理 [{"id":3095,"pid":755,num:1},{"id":3096,"pid":755,num:1},...]
            List<ConfiGureModel> confiGureModelList = JSON.parseArray(confiGureModels, ConfiGureModel.class);
            //当前商品信息
            if (confiGureModelList.size() > 0)
            {
                int              pid              = confiGureModelList.get(0).getPid();
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pid);
                productListModel = productListModelMapper.selectOne(productListModel);
                if (productListModel != null)
                {
                    for (ConfiGureModel confiGureModel : confiGureModelList)
                    {

                        if (confiGureModel.getNum() == null)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSRZJKCZ, "请输入增加库存值", "upStock");
                        }
                        if (confiGureModel.getNum() == 0)
                        {
                            continue;
                        }
                        AddStockVo addStockVo = new AddStockVo();
                        addStockVo.setStoreId(vo.getStoreId());
                        addStockVo.setId(confiGureModel.getId());
                        addStockVo.setPid(confiGureModel.getPid());
                        addStockVo.setAddNum(confiGureModel.getNum());
                        addStockVo.setText("店铺操作库存库存" + confiGureModel.getNum());
                        publicStockService.addGoodsStock(addStockVo, user.getUser_id());
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSBZQ, "参数不正确", "upStock");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修改商品规格库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upStock");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean submitAudit(MainVo vo, int shopId, int pId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                //获取商品信息
                ProductListModel productListModel = new ProductListModel();
                productListModel.setId(pId);
                productListModel.setStore_id(vo.getStoreId());
                productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                productListModel = productListModelMapper.selectByPrimaryKey(pId);
                if (productListModel != null)
                {
                    ProductListModel updateGoods = new ProductListModel();
                    updateGoods.setId(productListModel.getId());
                    updateGoods.setCommodity_type(productListModel.getCommodity_type());
                    if (DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS.toString().equals(productListModel.getMch_status()))
                    {
                        //提交审核需要验证品牌、分类是否被删除
                        String[] classIdList = StringUtils.trim(productListModel.getProduct_class(), SplitUtils.HG).split(SplitUtils.HG);
                        for (String classId : classIdList)
                        {
                            ProductClassModel productClassCount = new ProductClassModel();
                            productClassCount.setCid(Integer.parseInt(classId));
                            productClassCount.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                            if (productClassModelMapper.selectCount(productClassCount) > 0)
                            {
                                logger.debug("分类id[{}]已被删除,无法提交审核,请重新编辑!", classId);
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB_QCXBJSP, "提交失败,请重新编辑商品", "submitAudit");
                            }
                        }
                        BrandClassModel brandClassCount = new BrandClassModel();
                        brandClassCount.setBrand_id(productListModel.getBrand_id());
                        brandClassCount.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                        if (brandClassModelMapper.selectCount(brandClassCount) > 0)
                        {
                            logger.debug("品牌id[{}]已被删除,无法提交审核,请重新编辑!", productListModel.getBrand_id());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB_QCXBJSP, "提交失败,请重新编辑商品", "submitAudit");
                        }

                        //重新提交审核
                        updateGoods.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString());
                        //通知后台消息
                        MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                        messageLoggingSave.setStore_id(vo.getStoreId());
                        messageLoggingSave.setMch_id(productListModel.getMch_id());
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_GOODS_EXAMINE);
                        messageLoggingSave.setParameter(productListModel.getId() + "");
                        messageLoggingSave.setContent("商品id为:" + productListModel.getId() + "商品名称为: " + productListModel.getProduct_title() + "的商品需要管理员审核");
                        messageLoggingSave.setAdd_date(new Date());
                        messageLoggingSave.setTo_url(GoodsDataUtils.MCH_GOODS_URL);
                        messageLoggingModalMapper.insertSelective(messageLoggingSave);
                    }
                    else if (DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS.toString().equals(productListModel.getMch_status()))
                    {
                        //撤销审核
                        updateGoods.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_STOP_STATUS.toString());
                    }
                    else
                    {
                        logger.info("该商品商品状态为：" + productListModel.getMch_status() + "无需修改");
                        return false;
                    }
                    int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                    if (count < 1)
                    {
                        logger.info("商品审核 提交/撤销失败  参数:" + JSON.toJSONString(updateGoods));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "submitAudit");
                    }
                    return true;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("提交/撤销商品审核 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "submitAudit");
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delMyMerchandise(MainVo vo, int shopId, int pId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);

                if (!publicGoodsService.delGoodsById(vo.getStoreId(), pId, shopId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败");
                }
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除商品 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMyMerchandise");
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean myMerchandiseStatus(MainVo vo, int shopId, String pId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //校验店铺信息
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);

                String[] pidList = pId.split(",");
                for (String pid : pidList)
                {
                    int id = StringUtils.stringParseInt(pid);
                    //获取商品信息
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setId(id);
                    productListModel = productListModelMapper.selectByPrimaryKey(id);
                    if (productListModel != null)
                    {
                        ProductListModel updateGoods = new ProductListModel();
                        updateGoods.setId(id);
                        if (DictionaryConst.GoodsStatus.NEW_GROUNDING.toString().equals(productListModel.getStatus()))
                        {
                            //下架
                            updateGoods.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                            //上架时间置空
                            productListModelMapper.setUpperTimeNull(id);
                        }
                        else
                        {
                            //判断店铺是否缴纳保证金
                            boolean mchPromise = publicMchService.judgeMchPromise(vo, user.getUser_id());
                            if (!mchPromise)
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JNJWJ, "缴纳金未缴");
                            }
                            //上架
                            updateGoods.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                            updateGoods.setUpper_shelf_time(new Date());
                        }
                        //虚拟商品处理
                        if (Objects.equals(productListModel.getCommodity_type(), ProductListModel.COMMODITY_TYPE.virtual))
                        {
                            updateGoods.setCommodity_type(1);
                        }
                        int count = productListModelMapper.updateByPrimaryKeySelective(updateGoods);
                        if (count < 1)
                        {
                            logger.info("商品上下架状态修改失败  参数:" + JSON.toJSONString(updateGoods));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "myMerchandiseStatus");
                        }
                        return true;
                    }
                    else
                    {
                        logger.info("商品不存在  id:" + pid);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBZYSPBCZ, "列表中有商品不存在", "myMerchandiseStatus");
                    }
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("我的商品上下架 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "myMerchandiseStatus");
        }
        return false;
    }


    @Override
    public Map<String, Object> storeHomepage(MainVo vo, int shopId, Integer shopListId, String longitude, String latitude, int type, String lang_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //店铺主页轮播图
            List<Map<String, Object>> bannerModelList = new ArrayList<>();
            //收藏状态
            int collectionStatus = 0;
            //用户坐标和店铺坐标距离
            String distance = "0.00";

            //省市县
            String sheng   = "";
            String shi     = "";
            String xian    = "";
            String address = "";

            //收藏数量
            int collectionNum;
            //在售数量
            int quantityOnSale;
            //已售数量
            int quantitySold;

            DiyModel diyModel = new DiyModel();
            diyModel.setMch_id(shopId);
            diyModel.setIs_del(DictionaryConst.ProductRecycle.NOT_STATUS);
            diyModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
            diyModel = diyModelMapper.selectOne(diyModel);

            boolean hasDiy = Objects.nonNull(diyModel);

            if (user != null)
            {
                //获取用户是否有收藏该店铺
                UserCollectionModel userCollectionModel = new UserCollectionModel();
                userCollectionModel.setStore_id(vo.getStoreId());
                userCollectionModel.setUser_id(user.getUser_id());
                userCollectionModel.setMch_id(shopId);
                int count = userCollectionModelMapper.selectCount(userCollectionModel);
                if (count > 0)
                {
                    collectionStatus = 1;
                }
                if (StringUtils.isNotEmpty(longitude) &&  StringUtils.isNotEmpty(latitude))
                {
                    //上传用户经纬度信息
                    redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(Double.valueOf(longitude), Double.valueOf(latitude)), user.getUser_id());
                }
            }
            //获取当前店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(shopId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                if (mchModel.getIs_lock().equals(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK)))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "该店铺已被注销");
                }
                //获取商城配置信息
                ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null)
                {
                    //距离
                    if (user != null && StringUtils.isNotEmpty(longitude) && StringUtils.isNotEmpty(latitude))
                    {
                        RedisGeoCommands.GeoRadiusCommandArgs geoRadiusArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeDistance().includeCoordinates()//查询返回结果包括距离和坐标
                                .sortAscending();//按查询出的坐标距离中心坐标的距离进行排序
                        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate
                                .opsForGeo()
                                .radius(NEARBY_KEY, new Circle(new Point(Double.valueOf(longitude), Double.valueOf(latitude)), new Distance(10000, RedisGeoCommands.DistanceUnit.KILOMETERS)), geoRadiusArgs);
                        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = geoResults != null ? geoResults.getContent() : null;
                        for (GeoResult<RedisGeoCommands.GeoLocation<String>> mchId : content)
                        {
                            if (!mchId.getContent().getName().contains("user") && mchId.getContent().getName().equals(String.valueOf(mchModel.getId())))
                            {
                                Distance redisDistance = redisTemplate.opsForGeo().distance(NEARBY_KEY, user.getUser_id(), String.valueOf(mchModel.getId()), RedisGeoCommands.DistanceUnit.METERS);
                                if (!Objects.isNull(redisDistance))
                                {
                                    distance = String.valueOf(redisDistance.getValue());
                                }
                            }
                        }
                    }
                    //获取店铺商品信息
                    Map<String, Object> mchGoodsInfo = publiceService.commodityInformation(vo.getStoreId(), mchModel.getId(),lang_code);
                    collectionNum = Integer.parseInt(mchGoodsInfo.get("collection_num").toString());
                    quantityOnSale = Integer.parseInt(mchGoodsInfo.get("quantity_on_sale").toString());
                    quantitySold = Integer.parseInt(mchGoodsInfo.get("quantity_sold").toString());
                    //1=推荐,2=全部商品,3=商品分类
                    if (type == 3)
                    {
                        //获取当前店铺所有商品类别
                        List<ProductClassModel> productClassModelList = productClassModelMapper.getGoodsClassByMch1(shopId, vo.getLanguage());
                        resultMap.put("list", productClassModelList);
                    }
                    else
                    {
                        List<Map<String, Object>> goodsInfoList = getMchIndexGoodsInfo(vo, shopId, type, lang_code);
                        resultMap.put("list", goodsInfoList);
                    }
                    //获取店铺地址信息
                    List<MchStoreModel> mchStoreModels = new ArrayList<>();
                    MchStoreModel       mchStoreModel  = new MchStoreModel();
                    mchStoreModel.setStore_id(vo.getStoreId());
                    mchStoreModel.setMch_id(shopId);
                    if (shopListId != null)
                    {
                        mchStoreModel.setId(shopListId);
                    }
                    else
                    {
                        mchStoreModel.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                    }
                    mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                    if (mchStoreModel != null)
                    {
                        mchStoreModels.add(mchStoreModel);
                    }
                    if (mchStoreModel != null)
                    {
                        sheng = mchStoreModel.getSheng();
                        shi = mchStoreModel.getShi();
                        xian = mchStoreModel.getXian();
                        address = mchStoreModel.getAddress();
                    }

                    //轮播图处理
                    List<BannerModel> bannerModels = bannerModelMapper.getBannersByMch(vo.getStoreId(), mchModel.getId());
                    for (BannerModel banner : bannerModels)
                    {
                        Map<String, Object> bannerMap = new HashMap<>(16);
                        String              imageUrl  = publiceService.getImgPath(banner.getImage(), vo.getStoreId());
                        String              url       = banner.getUrl() == null ? "" : banner.getUrl();
                        //获取图片中最后的一个参数
                        String parameter = url.substring(url.lastIndexOf("=") + 1);

                        if (url.contains("tabBar"))
                        {
                            bannerMap.put("type", "switchTab");
                        }
                        else
                        {
                            bannerMap.put("type", "navigate");
                        }
                        bannerMap.put("id", banner.getId());
                        bannerMap.put("image", imageUrl);
                        bannerMap.put("parameter", parameter);
                        bannerMap.put("url", url);
                        bannerModelList.add(bannerMap);
                    }
                    //判断商城是否开启预售插件
                    boolean            isPreSell          = false;
                    PreSellConfigModel preSellConfigModel = new PreSellConfigModel();
                    preSellConfigModel.setStore_id(vo.getStoreId());
                    preSellConfigModel = preSellConfigModelMapper.selectOne(preSellConfigModel);
                    if (!Objects.isNull(preSellConfigModel))
                    {
                        if (preSellConfigModel.getIs_open() == DictionaryConst.WhetherMaven.WHETHER_OK)
                        {
                            isPreSell = true;
                        }
                    }
                    //判断商城是否开启优惠劵插件
                    int               isOpenCoupon;
                    CouponConfigModel couponConfigModel = new CouponConfigModel();
                    couponConfigModel.setStore_id(vo.getStoreId());
                    couponConfigModel.setMch_id(0);
                    couponConfigModel = couponConfigModelMapper.selectOne(couponConfigModel);
                    //商城和店铺同时开启
                    if (couponConfigModel != null && couponConfigModel.getIs_status() == 1 && mchModel.getIsOpenCoupon() == 1 && couponConfigModel.getIs_show() == 1)
                    {
                        isOpenCoupon = 1;
                    }
                    else
                    {
                        isOpenCoupon = 0;
                    }
                    //未营业
                    String mchOpenStatus = mchModel.getIs_open();
                    if ("0".equals(mchOpenStatus) || "2".equals(mchOpenStatus))
                    {
                        resultMap.put("is_open", mchModel.getIs_open());
                        //不显示营业时间
//                        resultMap.put("isShowTime", 1);
                        //营业时间
                        resultMap.put("business_hours", mchModel.getBusiness_hours());
                    }
                    else if ("1".equals(mchOpenStatus))
                    {
                        //营业时间
                        String businessHoursValue = mchModel.getBusiness_hours();
                        resultMap.put("business_hours", businessHoursValue);
                        //显示营业时间
//                        resultMap.put("isShowTime", 0);
                        if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                        {
                            resultMap.put("is_open", "2");
                        }
                        else
                        {
                            //营业时间判断是否营业
                            String[] businessHours = businessHoursValue.split(SplitUtils.BL);
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
                                    resultMap.put("is_open", "1");
                                }
                                else
                                {
                                    //未营业
                                    resultMap.put("is_open", "2");
                                }
                            }
                            else
                            {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                                if (!DateUtil.dateCompare(startTime, currentDate)
                                        && !DateUtil.dateCompare(currentDate, endTime))
                                {
                                    resultMap.put("is_open", "1");
                                    resultMap.put("business_hours", mchModel.getBusiness_hours());
                                }
                                else
                                {
                                    resultMap.put("is_open", "2");
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
                    }
                    //判断店铺是否在直播中
                    LivingRoomModel livingRoomModel = new LivingRoomModel();
                    livingRoomModel.setLiving_status(LivingRoomModel.STATUS_LIVING_STREAMING);
                    livingRoomModel.setUser_id(mchModel.getUser_id());
                    livingRoomModel.setRecycle(LivingRoomModel.RECYCLE_SHOW);
                    int count = livingRoomModelMapper.selectCount(livingRoomModel);

                    //true为店铺正在直播
                    if (count > 0)
                    {
                        resultMap.put("livingStatus", true);
                    }
                    else
                    {
                        resultMap.put("livingStatus", false);
                    }
                    //判断店铺插件是否开启
                    LivingConfigModel livingConfigModel = new LivingConfigModel();
                    livingConfigModel.setStore_id(vo.getStoreId());
                    livingConfigModel.setRecycle(0);
                    livingConfigModel = livingConfigModelMapper.selectOne(livingConfigModel);

                    if (StringUtils.isEmpty(livingConfigModel))
                    {
                        resultMap.put("mch_is_open", 0);
                    }
                    else
                    {
                        resultMap.put("mch_is_open", livingConfigModel.getMch_is_open());
                    }
                    resultMap.put("shop_name", mchModel.getName());
                    resultMap.put("shop_logo", publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId()));
                    resultMap.put("poster_img", publiceService.getImgPath(mchModel.getPoster_img(), vo.getStoreId()));
                    resultMap.put("roomid", mchModel.getRoomid());
                    resultMap.put("collection_num", collectionNum);
                    resultMap.put("collection_status", collectionStatus);
                    resultMap.put("quantity_on_sale", quantityOnSale);
                    resultMap.put("quantity_sold", quantitySold);
                    resultMap.put("shop_list", mchStoreModels);
                    resultMap.put("banner", bannerModelList);
                    resultMap.put("distance", distance);
                    resultMap.put("sheng", mchModel.getSheng());
                    resultMap.put("shi", mchModel.getShi());
                    resultMap.put("xian", mchModel.getXian());
                    resultMap.put("address", mchModel.getAddress());
                    resultMap.put("isPreSell", isPreSell);
                    resultMap.put("isOpenCoupon", isOpenCoupon);
                    resultMap.put("mobile", mchModel.getTel());
                    resultMap.put("cpc", mchModel.getCpc());
                    resultMap.put("hasDiy",hasDiy);
                    resultMap.put("shop_information", mchModel.getShop_information());
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHWPZXX, "商户未配置信息", "storeHomepage");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "storeHomepage");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("加载店铺主页自定义 异常", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载店铺主页 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeHomepage");
        }
        return resultMap;
    }

    @Override
    public void browseRecord(MainVo vo, int shopId) throws LaiKeAPIException
    {
        try
        {

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);

            //获取当前店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(shopId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                if (mchModel.getIs_lock().equals(String.valueOf(DictionaryConst.WhetherMaven.WHETHER_OK)))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "该店铺已被注销");
                }
            }

            String token = vo.getAccessId();
            if (StringUtils.isEmpty(token))
            {
                //没有token则获取一个token返回出去
                token = JwtUtils.getToken(publicUserService.getUserLoginLife(vo.getStoreId()));
            }
            StringBuilder userid = new StringBuilder("游客").append(DateUtil.timeStamp());

            if (user != null)
            {
                userid = new StringBuilder(user.getUser_id());
            }
            //添加一条商品浏览记录
            MchBrowseModel mchBrowseModel = new MchBrowseModel();
            mchBrowseModel.setStore_id(vo.getStoreId());
            mchBrowseModel.setToken(token);
            mchBrowseModel.setMch_id(mchModel.getId().toString());
            mchBrowseModel.setUser_id(userid.toString());
            mchBrowseModel.setEvent("访问了店铺");
            mchBrowseModel.setAdd_time(new Date());
            int id = mchBrowseModelMapper.saveBrowse(mchBrowseModel);
            if (id < 1)
            {
                logger.info(userid + " 用户店铺浏览记录插入失败 店铺id= " + mchModel.getId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加浏览记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "browseRecord");
        }
    }



    @Override
    public Map<String, Object> storeHomepageLoad(MainVo vo, int shopId, int type, String lang_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (type == 3)
            {
                //获取当前店铺所有商品类别
                List<ProductClassModel> productClassModelList = productClassModelMapper.getGoodsClassByMch1(shopId, vo.getLanguage());
                resultMap.put("list", productClassModelList);
            }
            else
            {
                List<Map<String, Object>> goodsInfoList = getMchIndexGoodsInfo(vo, shopId, type, lang_code);
                resultMap.put("list", goodsInfoList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载店铺主页-加载更多 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeHomepageLoad");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean collectionShop(MainVo vo, int shopId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setId(shopId);

                UserCollectionModel userCollectionModel = new UserCollectionModel();
                userCollectionModel.setStore_id(vo.getStoreId());
                userCollectionModel.setUser_id(user.getUser_id());
                userCollectionModel.setMch_id(shopId);
                UserCollectionModel userCollection = userCollectionModelMapper.selectOne(userCollectionModel);
                if (userCollection != null)
                {
                    //用户已收藏,删除记录
                    int count = userCollectionModelMapper.delete(userCollection);
                    if (count < 1)
                    {
                        logger.info("用户取消收藏失败  收藏id:" + userCollection.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionShop");
                    }
                    //收藏数-1
                    count = mchModelMapper.cancelCollection(mchModel);
                    if (count < 1)
                    {
                        logger.info("收藏数-1失败  收藏id:" + userCollection.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionShop");
                    }
                }
                else
                {
                    //用户未收藏,添加记录
                    userCollectionModel.setAdd_time(new Date());
                    int count = userCollectionModelMapper.insertSelective(userCollectionModel);
                    if (count < 1)
                    {
                        logger.info("用户取消收藏失败  参数:" + JSON.toJSONString(userCollectionModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionShop");
                    }
                    //收藏数+1
                    count = mchModelMapper.addCollection(mchModel);
                    if (count < 1)
                    {
                        logger.info("收藏数+1失败  收藏id:" + userCollectionModel.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionShop");
                    }
                }
                return true;
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("收藏店铺 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionShop");
        }
        return false;
    }


    @Override
    public Map<String, Object> intoSetShop(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                      user      = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            List<Map<String, Object>> list      = new ArrayList<>();
            Map<String, Object>       map       = new HashMap<>(16);
            MchModel                  resultMch = new MchModel();
            //校验店铺
            publicMchService.verificationMchExits(vo.getStoreId(), user.getUser_id(), shopId);
            resultMch.setStore_id(vo.getStoreId());
            resultMch.setUser_id(user.getUser_id());
            resultMch.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            resultMch.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            resultMch = mchModelMapper.selectOne(resultMch);
            if (resultMch != null)
            {
                String logoUlr = resultMch.getLogo();
                logoUlr = publiceService.getImgPath(logoUlr, vo.getStoreId());
                map.put("id", resultMch.getId());
                map.put("logo", logoUlr);
                map.put("name", resultMch.getName());
                map.put("shop_information", resultMch.getShop_information());
                map.put("shop_range", resultMch.getShop_range());
                map.put("realname", resultMch.getRealname());
                //身份证脱敏
                map.put("ID_number", StringUtils.desensitizedIdNumber(resultMch.getID_number()));
                //手机号脱敏
                map.put("tel", StringUtils.desensitizedPhoneNumber(resultMch.getTel()));
                map.put("sheng", resultMch.getSheng());
                map.put("cpc", resultMch.getCpc());
                map.put("shi", resultMch.getShi());
                map.put("xian", resultMch.getXian());
                map.put("address", resultMch.getAddress());
                //未营业
                String resultMchOpenStatus = resultMch.getIs_open();
                if ("0".equals(resultMchOpenStatus) || "2".equals(resultMchOpenStatus))
                {
                    map.put("is_open", resultMch.getIs_open());
                    if (resultMch.getBusiness_hours() != null)
                    {
                        map.put("business_hours", resultMch.getBusiness_hours());
                    }
                }
                else if ("1".equals(resultMchOpenStatus))
                {
                    //营业时间
                    String businessHoursValue = resultMch.getBusiness_hours();
                    map.put("business_hours", businessHoursValue);
                    if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                    {
                        map.put("is_open", "2");
                    }
                    else
                    {
                        //营业时间判断是否营业
                        String[] businessHours = businessHoursValue.split(SplitUtils.BL);
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
                                map.put("is_open", "1");
                            }
                            else
                            {
                                //未营业
                                map.put("is_open", "2");
                            }
                        }
                        else
                        {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                            if (!DateUtil.dateCompare(startTime, currentDate)
                                    && !DateUtil.dateCompare(currentDate, endTime))
                            {
                                map.put("is_open", "1");
                            }
                            else
                            {
                                map.put("is_open", "2");
                            }
                        }
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
                }
                map.put("account_money", resultMch.getAccount_money());
                map.put("cashable_money", resultMch.getCashable_money());
                map.put("isInvoice", resultMch.getIs_invoice());
                map.put("cid", resultMch.getCid());
                map.put("is_self_delivery", resultMch.getIs_self_delivery());
                MchClassModel mchClassModel = mchClassModelMapper.selectByPrimaryKey(resultMch.getCid());
                if (mchClassModel != null)
                {
                    map.put("cname", mchClassModel.getName());
                }

                map.put("posterImg", publiceService.getImgPath(resultMch.getPoster_img(), vo.getStoreId()));
                map.put("headImg", publiceService.getImgPath(resultMch.getHead_img(), vo.getStoreId()));
            }
            list.add(map);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("进入设置店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "intoSetShop");
        }
        return resultMap;
    }

    @Override
    public boolean setShop(SetShopVo vo) throws LaiKeAPIException
    {
        try
        {
            User     user   = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchModel mchOld = new MchModel();
            mchOld.setId(vo.getShopId());
            mchOld.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchOld = mchModelMapper.selectByPrimaryKey(mchOld);
            if (mchOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }
            MchModel mchModel = new MchModel();
            if (StringUtils.isNotEmpty(vo.getCityAll()))
            {
                String[] ssxList = vo.getCityAll().split("-");
                if (ssxList.length < 3)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZSJGSBZQ, "地址数据格式不正确");
                }
                mchModel.setSheng(ssxList[0]);
                mchModel.setShi(ssxList[1]);
                mchModel.setXian(ssxList[2]);
            }
            mchModel.setId(vo.getShopId());
            mchModel.setName(vo.getName());
            mchModel.setShop_information(vo.getShopInformation());
            mchModel.setShop_range(vo.getShopRange());
            if (!StringUtils.isEmpty(vo.getIsOpen()))
            {
                mchModel.setIs_open(vo.getIsOpen());
                //营业中-有营业时间
                if (vo.getIsOpen().equals("1"))
                {
                    if (StringUtils.isEmpty(vo.getBusinessHours()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入营业时间");
                    }
                    //营业时间判断是否营业
                    String[] businessHours = vo.getBusinessHours().split(SplitUtils.BL);
                    //开始时间
                    Date startTime = DateUtil.dateFormateToDate(businessHours[0], GloabConst.TimePattern.HM);
                    //结束时间
                    Date endTime = DateUtil.dateFormateToDate(businessHours[1], GloabConst.TimePattern.HM);
                    if (Objects.equals(startTime, endTime))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "营业时间开始时间不能等于结束时间");
                    }
                    mchModel.setBusiness_hours(vo.getBusinessHours());
                }
            }
            mchModel.setAddress(vo.getAddress());
            mchModel.setTel(vo.getTel());
            mchModel.setCpc(vo.getCpc());
            mchModel.setIs_invoice(vo.getIsInvoice());
            mchModel.setIs_self_delivery(vo.getIs_self_delivery());
            mchModel.setCid(vo.getCid());
            //上传店铺logo
            if (vo.getFile() != null && !vo.getFile().isEmpty())
            {
                List<MultipartFile> files = new ArrayList<>();
                files.add(vo.getFile());
                List<String> imgUrls   = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, vo.getStoreType(), vo.getStoreId(), mchOld.getId());
                String       imageName = ImgUploadUtils.getUrlImgByName(imgUrls.get(0), true);
                if (StringUtils.isNotEmpty(vo.getHeadImg()))
                {
                    mchModel.setHead_img(imageName);
                }
                else if (StringUtils.isNotEmpty(vo.getPosterImg()))
                {
                    mchModel.setPoster_img(imageName);
                }
                else
                {
                    mchModel.setLogo(imageName);
                }
            }
            String addressInfo = vo.getCityAll() + vo.getAddress();
            //获取api key
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WPZDWFW, "未配置定位服务", "applyShop");
            }
//            //获取地址经纬度
//            try {
//                Map<String, String> latAndLogMap = TengxunMapUtil.getlatAndLng(configModel.getTencent_key(), addressInfo);
//                mchModel.setLatitude(latAndLogMap.get("lat"));
//                mchModel.setLongitude(latAndLogMap.get("lng"));
//            } catch (Exception ee) {
//                logger.error("获取店铺位置信息失败{}", ee.getMessage());
//            }
            mchModel = DataCheckTool.checkMchDataFormate(mchModel);
            //验证店铺名称
            if (StringUtils.isNotEmpty(mchModel.getName()))
            {
                if (!mchOld.getName().equals(vo.getName()) && mchModelMapper.verifyMchName(vo.getStoreId(), vo.getName()) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在", "verifyStoreName");
                }
            }
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPXXXGSB, "店铺信息修改失败", "setShop");
            }
            //审核通过上传商铺经纬度
            if (!Objects.isNull(mchModel.getLongitude()) && !Objects.isNull(mchModel.getLatitude()))
            {
                //经度
                Double longitude = Double.valueOf(mchModel.getLongitude());
                //纬度
                Double latitude = Double.valueOf(mchModel.getLatitude());
                if (!GeoUtils.isAvailablePointInRedis(longitude, latitude))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXWZ, "Invalid position(无效位置)");
                }
                redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(longitude, latitude), String.valueOf(mchModel.getId()));
            }

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setShop");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancellationShop(MainVo vo, int shopId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            //验证店铺信息
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            //注销店铺
            if (!publicMchService.cancellationShop(vo.getStoreId(), shopId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXSB, "注销失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("注销 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "cancellationShop");
        }
    }


    @Override
    public Map<String, Object> shopCustomer(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(shopId);
            //店铺logo
            String logoUrl = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
            //参数
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            parmaMap.put("pageNo", vo.getPageNo());
            //今天+昨天+前天+更早 = page
            int dataNum = 0;
            //客户信息,减少数据库访问
            Map<String, Map<String, Object>> clientInfoMap = new HashMap<>(16);

            //店铺浏览记录集 根据需求，按照 今天，昨天，前天，更早进行分组
            Map<String, Object> groupDataMap = new HashMap<>(16);
            for (int i = 0; i < 4; i++)
            {
                String startDate = null, endDate = null;
                if (dataNum == vo.getPageSize())
                {
                    break;
                }
                String time = "更早";
                //为true=更早标识
                boolean flag = false;
                //判断今天的客户是否够n条，不足其它来凑
                int num = vo.getPageSize() - dataNum;
                parmaMap.put("pageSize", num);
                switch (i)
                {
                    case 0:
                        //今天
                        startDate = DateUtil.getStartOfDay(new Date());
                        endDate = DateUtil.getEndOfDay(new Date());
                        break;
                    case 1:
                        //昨天
                        Date yesterDay = DateUtil.getAddDate(new Date(), -1);
                        startDate = DateUtil.getStartOfDay(yesterDay);
                        endDate = DateUtil.getEndOfDay(yesterDay);
                        break;
                    case 2:
                        //前天
                        Date beforeDay = DateUtil.getAddDate(new Date(), -2);
                        startDate = DateUtil.getStartOfDay(beforeDay);
                        endDate = DateUtil.getEndOfDay(beforeDay);
                        break;
                    default:
                        //更早
                        flag = true;
                        beforeDay = DateUtil.getAddDate(new Date(), -2);
                        startDate = DateUtil.getStartOfDay(beforeDay);
                        break;
                }
                if (!flag)
                {
                    parmaMap.put("startDate", startDate);
                    parmaMap.put("endDate", endDate);
                    time = DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD);
                }
                else
                {
                    //更早
                    parmaMap.remove("startDate");
                    parmaMap.remove("endDate");
                    parmaMap.put("startDate_lt", startDate);
                }
                //时间分组
                Map<String, Object> currentDataMap = new HashMap<>(16);
                //按人分组
                Map<String, List<Map<String, Object>>> clientListMain = new LinkedHashMap<>();
                List<List<Map<String, Object>>>        list           = new ArrayList<>();
                ConfigModel                            configModel    = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                //默认头像
                String defaultImgUrl = "";
                if (configModel != null)
                {
                    defaultImgUrl = publiceService.getImgPath(configModel.getWx_headimgurl(), vo.getStoreId());
                }

                //先获取 今天 昨天 前天 更早的主数据
                List<Map<String, Object>> mchBrowseList = mchBrowseModelMapper.getMchBrowseListDynamic(parmaMap);
                for (Map<String, Object> map : mchBrowseList)
                {
                    String                    userId   = map.get("user_id") + "";
                    List<Map<String, Object>> listTemp = new ArrayList<>();
                    //当前客户信息
                    Map<String, Object> clientInfo = new HashMap<>(16);
                    //获取客户信息
                    if (clientListMain.containsKey(userId))
                    {
                        listTemp = clientListMain.get(userId);
                    }
                    if (clientInfoMap.containsKey(userId))
                    {
                        clientInfo = clientInfoMap.get(userId);
                    }
                    else
                    {
                        //获取客户头像
                        User client = new User();
                        client.setUser_id(userId);
                        client = userBaseMapper.selectOne(client);
                        String imgUrl;
                        String zhangHao = "游客";
                        if (client != null)
                        {
                            imgUrl = client.getHeadimgurl();
                            if (StringUtils.isNotEmpty(client.getZhanghao()))
                            {
                                zhangHao = client.getZhanghao();
                            }
                            else
                            {
                                zhangHao = client.getUser_id();
                            }
                        }
                        else
                        {
                            //显示店铺logo
                            imgUrl = publiceService.getImgPath(logoUrl, vo.getStoreId());
                        }
                        if (StringUtils.isEmpty(imgUrl))
                        {
                            imgUrl = defaultImgUrl;
                        }
                        clientInfo.put("headimgurl", imgUrl);
                        clientInfo.put("zhanghao", zhangHao);
                    }
                    map.putAll(clientInfo);
                    listTemp.add(map);
                    clientListMain.put(userId, listTemp);
                    clientInfoMap.put(userId, clientInfo);
                }
                //按照人分组 获取每个人访问明细时间点 防止数据膨大，每个人只显示最新的十条
                for (String userId : clientListMain.keySet())
                {
                    //获取当前客户访问数据
                    Map<String, Object> parmaMap1 = new HashMap<>(16);
                    parmaMap1.put("store_id", vo.getStoreId());
                    parmaMap1.put("mch_id", shopId);
                    parmaMap1.put("user_id", userId);
                    if (!flag)
                    {
                        parmaMap1.put("startDate", startDate);
                        parmaMap1.put("endDate", endDate);
                    }
                    else
                    {
                        //更早
                        parmaMap.put("startDate_lt", startDate);
                    }
                    parmaMap1.put("pageStart", 0);
                    parmaMap1.put("pageEnd", 10);
                    parmaMap1.put("add_time_sort", DataUtils.Sort.DESC.toString());
                    List<Map<String, Object>> userBrowseList = mchBrowseModelMapper.selectDynamic(parmaMap1);

                    //获取客户信息
                    for (Map<String, Object> map : userBrowseList)
                    {
                        String event = I18nUtils.getRawMessage("app.mch.visitor");
                        map.put("event", event);
                        String uid = map.get("user_id") + "";
                        map.putAll(clientInfoMap.get(uid));
                        //更新访问时间
                        map.put("add_time", DateUtil.dateFormate(map.get("add_time") + "", GloabConst.TimePattern.YMDHMS));
                    }
                    list.add(userBrowseList);
                }
                currentDataMap.put("res", list);
                currentDataMap.put("time", time);
                groupDataMap.put("list" + (i + 1), currentDataMap);
                //计算分页
                dataNum += mchBrowseList.size();
            }

            resultMap.put("list", groupDataMap);
            resultMap.put("num", vo.getPageSize());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的顾客 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "shopCustomer");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> shopFans(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(shopId);
            //店铺logo
            String logoUrl = publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId());
            //参数
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            parmaMap.put("collectionMch", "collectionMch");
            parmaMap.put("pageStart", vo.getPageNo());
            //今天+昨天+前天+更早 = page
            int dataNum = 0;
            //客户信息,减少数据库访问
            Map<String, Map<String, Object>> clientInfoMap = new HashMap<>(16);

            //店铺浏览记录集 根据需求，按照 今天，昨天，前天，更早进行分组
            Map<String, Object> groupDataMap = new HashMap<>(16);
            for (int i = 0; i < 4; i++)
            {
                String startDate = null, endDate = null;
                if (dataNum == vo.getPageSize())
                {
                    break;
                }
                String time = "更早";
                //为true=更早标识
                boolean flag = false;
                //判断今天的客户是否够n条，不足其它来凑
                int num = vo.getPageSize() - dataNum;
                parmaMap.put("pageEnd", num);
                switch (i)
                {
                    case 0:
                        //今天
                        startDate = DateUtil.getStartOfDay(new Date());
                        endDate = DateUtil.getEndOfDay(new Date());
                        break;
                    case 1:
                        //昨天
                        Date yesterDay = DateUtil.getAddDate(new Date(), -1);
                        startDate = DateUtil.getStartOfDay(yesterDay);
                        endDate = DateUtil.getEndOfDay(yesterDay);
                        break;
                    case 2:
                        //前天
                        Date beforeDay = DateUtil.getAddDate(new Date(), -2);
                        startDate = DateUtil.getStartOfDay(beforeDay);
                        endDate = DateUtil.getEndOfDay(beforeDay);
                        break;
                    default:
                        //更早
                        flag = true;
                        beforeDay = DateUtil.getAddDate(new Date(), -2);
                        startDate = DateUtil.getStartOfDay(beforeDay);
                        break;
                }
                if (!flag)
                {
                    parmaMap.put("startDate", startDate);
                    parmaMap.put("endDate", endDate);
                    time = DateUtil.dateFormate(startDate, GloabConst.TimePattern.YMD);
                }
                else
                {
                    //更早
                    parmaMap.remove("startDate");
                    parmaMap.remove("endDate");
                    parmaMap.put("startDate_lt", startDate);
                }
                //时间分组
                Map<String, Object>       currentDataMap = new HashMap<>(16);
                List<Map<String, Object>> list           = new ArrayList<>();

                //先获取 今天 昨天 前天 更早的主数据
                List<Map<String, Object>> mchBrowseList = userCollectionModelMapper.selectMchDynamic(parmaMap);
                for (Map<String, Object> map : mchBrowseList)
                {
                    Map<String, Object> userMap = new HashMap<>(16);
                    String              userId  = map.get("user_id") + "";
                    String              cid     = map.get("cid") + "";
                    //获取客户头像
                    User client = new User();
                    client.setUser_id(userId);
                    client = userBaseMapper.selectOne(client);
                    String headImg  = "";
                    String userName = "";
                    if (client != null)
                    {
                        headImg = client.getHeadimgurl();
                        userName = client.getUser_name();
                    }
                    userMap.put("cid", cid);
                    userMap.put("headImg", headImg);
                    userMap.put("userName", userName);
                    userMap.put("userId", userId);
                    list.add(userMap);
                }
                currentDataMap.put("res", list);
                currentDataMap.put("time", time);
                groupDataMap.put("list" + (i + 1), currentDataMap);
                //计算分页
                dataNum += mchBrowseList.size();
            }

            resultMap.put("list", groupDataMap);
            resultMap.put("num", vo.getPageSize());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的粉丝 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "shopFans");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFans(MainVo vo, Integer cid) throws LaiKeAPIException
    {
        try
        {
            User                user                = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            UserCollectionModel userCollectionModel = userCollectionModelMapper.selectByPrimaryKey(cid);
            if (!Objects.isNull(userCollectionModel))
            {
                userCollectionModelMapper.deleteByPrimaryKey(userCollectionModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("移除粉丝 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "removeFans");
        }
    }

    @Override
    public Map<String, Object> myOrder(MchOrderIndexVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());
            vo.setUserId(user.getUser_id());
            resultMap = publicOrderService.aMchOrderIndex(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "myOrder");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> deliverShow(MainVo vo, String orderno) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                List<Map<String, Object>> goodsList = new ArrayList<>();

                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("store_id", vo.getStoreId());
                parmaMap.put("orderno", orderno);
                parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CONSIGNMENT);
                //获取订单信息
                List<Map<String, Object>> orderInfoList = orderDetailsModelMapper.selectOrderInfoListDynamce(parmaMap);
                for (Map<String, Object> map : orderInfoList)
                {
                    //订单详情id
                    int detailId = MapUtils.getInteger(map, "id");
                    //订单号
                    String orderNo = MapUtils.getString(map, "r_sNo");
                    //售后中的商品不显示在发货列表
                    if (returnOrderModelMapper.orderDetailReturnIsNotEnd(vo.getStoreId(), orderNo, detailId) > 0)
                    {
                        logger.debug("售后中的商品不显示再发货列表 订单明细id{}", detailId);
                        continue;
                    }

                    String imgUrl = map.get("img") + "";
                    imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                    map.put("imgurl", imgUrl);
                    //商品类别id集
                    String   goodsClassIdStr  = map.get("product_class").toString();
                    String[] goodsClassIdList = StringUtils.trim(goodsClassIdStr, "-").split("-");
                    //获取当前商品最小类别
                    String            minClassId        = goodsClassIdList[goodsClassIdList.length - 1];
                    ProductClassModel productClassModel = new ProductClassModel();
                    productClassModel.setCid(StringUtils.stringParseInt(minClassId));
                    productClassModel = productClassModelMapper.selectOne(productClassModel);
                    if (productClassModel != null)
                    {
                        map.put("class_name", productClassModel.getPname());
                    }
                    //原来显示成本价 现在显示售价
                    map.put("costprice", map.get("price"));
                    //可发货数量
                    map.put("deliverNum", MapUtils.getIntValue(map, "num") - MapUtils.getIntValue(map, "deliver_num"));
                    resultMap.put("self_lifting", DataUtils.getIntegerVal(orderInfoList.get(0), "self_lifting"));
                    goodsList.add(map);
                }
                resultMap.put("goods", goodsList);
                resultMap.put("logistics_type", publicExpressService.getMchHaveExpressSubtableByMchId(vo, user.getMchId()));

            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载发货列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deliverShow");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> intoSend(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //验证店铺
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                Map<String, Object> paramMap = new HashMap<>(16);
                paramMap.put("is_open", DictionaryConst.WhetherMaven.WHETHER_OK);
                paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());

                int                       total            = expressModelMapper.countDynamic(paramMap);
                List<Map<String, Object>> expressModelList = new ArrayList<>();
                if (total > 0)
                {
                    expressModelList = expressModelMapper.selectDynamic(paramMap);
                }
                resultMap.put("total", total);
                resultMap.put("list", expressModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取物流列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "intoSend");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void send(MainVo vo, int shopId, String sNo, Integer expressId, String courierNum, String orderListId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            if (expressId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZWLGS, "请选择物流公司");
            }
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setUserId(user.getUser_id());
            sendVo.setWxid(user.getWx_id());
            sendVo.setExpressId(expressId);
            sendVo.setsNo(sNo);
            sendVo.setOrderDetailsId(orderListId);
            sendVo.setCourierNum(courierNum);
            //发货
            publicOrderService.frontDelivery(sendVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "send");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean closingOrder(MainVo vo, int shopId, String orderno) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //验证店铺
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                Map<String, Object> parmaMap = new HashMap<>(16);
                //关闭订单-订单明细
                parmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                parmaMap.put("orderno", orderno);
                int count = orderDetailsModelMapper.updateByOrdernoDynamic(parmaMap);
                if (count < 1)
                {
                    logger.info("订单明细关闭失败 参数" + JSON.toJSONString(parmaMap));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                }
                //关闭订单 - 主订单
                parmaMap.clear();
                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE);
                parmaMap.put("orderno", orderno);
                //取消方式为手动取消
                parmaMap.put("cancel_method", 1);
                count = orderModelMapper.updateByOrdernoDynamic(parmaMap);
                if (count < 1)
                {
                    logger.info("订单关闭失败 参数" + JSON.toJSONString(parmaMap));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                }
                //商品库存回滚
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setStore_id(vo.getStoreId());
                orderDetailsModel.setR_sNo(orderno);
                List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                for (OrderDetailsModel orderDetails : orderDetailsModelList)
                {
                    //回滚商品主表库存
                    count = productListModelMapper.addGoodsStockNum(orderDetails.getP_id(), orderDetails.getNum());
                    if (count < 1)
                    {
                        logger.info("关闭订单时,商品库存回滚失败 参数 订单明细id" + orderDetails.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                    }
                    //回滚属性库存
                    count = confiGureModelMapper.addGoodsAttrStockNumByPid(orderDetails.getNum(), Integer.parseInt(orderDetails.getSid()));
                    if (count < 1)
                    {
                        logger.info("关闭订单时,属性库存回滚失败 参数 订单明细id" + orderDetails.getId());
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDGBSB, "订单关闭失败");
                    }
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
            e.printStackTrace();
            logger.error("关闭订单 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "closingOrder");
        }
        return false;
    }

    @Override
    public boolean upOrder(MainVo vo, int shopId, String orderno, String orderDetail) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //验证店铺
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                //{"address":"嘻嘻嘻","mobile":"18474432142","name":"一下","z_price":"20.00","area":"北京市-市辖区-丰台区","remarks":""}
                Map<String, String> orderMap = JSON.parseObject(orderDetail, new TypeReference<Map<String, String>>()
                {
                });
                //验证订单是否存在
                OrderModel orderModel = new OrderModel();
                orderModel.setsNo(orderno);
                orderModel = orderModelMapper.selectOne(orderModel);
                if (orderModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在", "upOrder");
                }
                String   ssx     = orderMap.get("area");
                String[] address = ssx.split("-");
                if (address.length < 3)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZGSBZQ, "地址格式不正确");
                }
                BigDecimal orderAmt = new BigDecimal(orderMap.get("z_price"));
                if (orderAmt.doubleValue() < 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDJEYW, "订单金额有误");
                }
                if (orderAmt.compareTo(orderModel.getZ_price()) != 0)
                {
                    //如果是修改订单金额 若订单有多个商品 则均摊金额
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setStore_id(vo.getStoreId());
                    orderDetailsModel.setR_sNo(orderno);
                    List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetails : orderDetailsModelList)
                    {
                        //订单差额 原订单金额-修改的金额
                        BigDecimal chaAmt = orderModel.getZ_price().subtract(orderAmt);
                        //(优惠后的价格+运费)/原订单价格 * 订单差额
                        BigDecimal currentAmt = orderDetails.getAfter_discount().add(orderDetails.getFreight());
                        currentAmt = currentAmt.divide(orderModel.getZ_price(), 2, BigDecimal.ROUND_HALF_UP).multiply(chaAmt);
                        int row = orderDetailsModelMapper.updateOrderPrice(orderDetails.getId(), currentAmt);
                        if (row < 1)
                        {
                            logger.debug("订单明细id:{} 修改金额失败", orderDetails.getId());
                        }
                    }

                }
                if (orderMap.containsKey("delivery_time") || orderMap.containsKey("delivery_period"))
                {
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setR_sNo(orderno);
                    List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
                    for (OrderDetailsModel orderDetails : orderDetailsModels)
                    {
                        if (orderDetails.getStore_self_delivery() != null)
                        {
                            StoreSelfDeliveryModel storeSelfDeliveryModel = storeSelfDeliveryModelMapper.selectByPrimaryKey(orderDetails.getStore_self_delivery());
                            storeSelfDeliveryModel.setDelivery_time(DateUtil.dateFormateToDate(orderMap.get("delivery_time"), GloabConst.TimePattern.YMD));
                            storeSelfDeliveryModel.setDelivery_period(orderMap.get("delivery_period"));
                            storeSelfDeliveryModelMapper.updateByPrimaryKeySelective(storeSelfDeliveryModel);
                        }
                    }
                }

                //只能修改详细地址、所在地区、订单备注
                OrderModel updateOder = new OrderModel();
                updateOder.setId(orderModel.getId());
                updateOder.setSheng(address[0]);
                updateOder.setShi(address[1]);
                updateOder.setXian(address[2]);
                updateOder.setAddress(orderMap.get("address"));
                updateOder.setRemark(orderMap.get("remarks"));
                updateOder.setName(orderMap.get("name"));
                updateOder.setMobile(orderMap.get("mobile"));
                updateOder.setZ_price(orderAmt);
                int count = orderModelMapper.updateByPrimaryKeySelective(updateOder);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDXGSB, "订单修改失败", "upOrder");
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
            logger.error("修改订单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "upOrder");
        }
        return false;
    }

    @Override
    public Map<String, Object> orderDetails(MainVo vo, int shopId, String sNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            MchOrderDetailVo orderVo = new MchOrderDetailVo();
            orderVo.setStoreId(vo.getStoreId());
            orderVo.setShopId(shopId);
            orderVo.setsNo(sNo);
            resultMap = publicOrderService.mchOrderDetails(orderVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> returnOrderDetails(MainVo vo, int shopId, String sNo, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //售后订单详情信息
            Map<String, Object> infoMap = new HashMap<>(16);
            //商品信息
            Map<String, Object> goodsMap = new HashMap<>(16);

            List<Map<String, Object>> returnOrderInfos = returnOrderModelMapper.selectReturnOrderInfo(vo.getStoreId(), id);
            if (returnOrderInfos == null || returnOrderInfos.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            Map<String, Object> map = returnOrderInfos.get(0);
            //售后id
            Integer returnId = MapUtils.getIntValue(map, "id");
            //凭证
            String rePhotoStr = map.get("re_photo") + "";
            //退货信息
            Map<String, Object> returnInfo = new HashMap<>(16);
            //回寄信息
            Map<String, Object> sendInfo = new HashMap<>(16);
            //商品图片
            String imgUrl = map.get("img").toString();
            map.put("img", publiceService.getImgPath(imgUrl, vo.getStoreId()));
            int orderId = Integer.parseInt(map.get("p_id").toString());
            //获取回寄信息
            List<Map<String, Object>> backList = returnOrderModelMapper.selectReturnGoodsInfo(orderId, returnId, vo.getStoreId());
            if (backList != null && backList.size() > 0)
            {
                //回寄信息
                returnInfo = backList.get(0);
                if (backList.size() > 1)
                {
                    //退换信息
                    sendInfo = backList.get(1);
                }
            }
            //处理凭证
            List<String> rePhotoUrlList = new ArrayList<>();
            if (!StringUtils.isEmpty(rePhotoStr))
            {
                Map<Integer, Object> rePhotoMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(rePhotoStr, Map.class));
                if (rePhotoMap != null)
                {
                    for (Integer key : rePhotoMap.keySet())
                    {
                        String rePhotoUr = rePhotoMap.get(key) + "";
                        rePhotoUrlList.add(publiceService.getImgPath(rePhotoUr, vo.getStoreId()));
                    }
                }
            }
            //售后信息
            infoMap.put("re_time", DateUtil.dateFormate(map.get("re_time") + "", GloabConst.TimePattern.YMDHMS));
            infoMap.put("p_price", map.get("real_money"));
            infoMap.put("r_content", map.get("r_content"));
            infoMap.put("re_apply_money", map.get("re_apply_money"));
            infoMap.put("real_money", map.get("real_money"));
            infoMap.put("re_type", map.get("re_type"));
            infoMap.put("p_name", map.get("p_name"));
            infoMap.put("r_sNo", map.get("sNo"));
            infoMap.put("type", map.get("r_type"));
            infoMap.put("content", map.get("content"));
            infoMap.put("re_photo", rePhotoUrlList);
            //商品信息
            goodsMap.put("img", map.get("img"));
            goodsMap.put("p_name", map.get("p_name"));
            goodsMap.put("p_price", map.get("p_price"));
            goodsMap.put("num", map.get("num"));
            goodsMap.put("size", map.get("size"));

            resultMap.put("info", infoMap);
            if (sendInfo != null)
            {
                sendInfo.put("add_data", DateUtil.dateFormate(MapUtils.getString(sendInfo, "add_data"), GloabConst.TimePattern.YMDHMS));
            }
            //退货退款，用户寄回商品给供应商，供应商端同意退款后，在pc店铺这边应该是没有拒绝操作的 ，此处对于供应商商品单独处理
            ReturnOrderModel returnOrderModel = returnOrderModelMapper.selectByPrimaryKey(id);
            if (DictionaryConst.ReturnOrderStatus.RETURNORDERSTATUS_QUICK_SUPPLIER_AGRESS.equals(returnOrderModel.getR_type()))
            {
                resultMap.put("refuseButton", false);
            }
            else
            {
                resultMap.put("refuseButton", true);
            }
            resultMap.put("send_info", sendInfo);
            resultMap.put("return_info", returnInfo);
            resultMap.put("goods_list", goodsMap);
            resultMap.put("r_type", map.get("r_type"));
            resultMap.put("re_type", map.get("re_type"));
            resultMap.put("audit_time", DateUtil.dateFormate(map.get("audit_time") + "", GloabConst.TimePattern.YMDHMS));

            //货币信息
            OrderModel orderModel = new OrderModel();
            orderModel.setsNo(sNo);
            orderModel = orderModelMapper.selectOne(orderModel);
            resultMap.put("exchange_rate", orderModel.getExchange_rate());
            resultMap.put("currency_code", orderModel.getCurrency_code());
            resultMap.put("currency_symbol", orderModel.getCurrency_symbol());
            //订单类型和积分
            resultMap.put("oType", orderModel.getOtype());
            resultMap.put("allow", orderModel.getAllow());

            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后订单详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnOrderDetails");
        }
    }

    @Override
    public Map<String, Object> withdrawalDetails(MainVo vo, int shopId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("id", id);
            List<Map<String, Object>> detailList = withdrawModelMapper.getWithdrawLeftUserBankAndMch(parmaMap);
            Map<String, Object>       detailMap  = null;
            if (detailList != null && detailList.size() > 0)
            {
                detailMap = detailList.get(0);
                String addTime  = DateUtil.dateFormate(MapUtils.getString(detailMap, "addtime"), GloabConst.TimePattern.YMDHMS);
                String bankCard = MapUtils.getString(detailMap, "Bank_card_number");
                if (StringUtils.isNotEmpty(bankCard))
                {
                    bankCard = bankCard.substring(bankCard.length() - 4);
                }
                else
                {
                    bankCard = "";
                }
                //提现的方式或者是类型
                String withdraw_status = MapUtils.getString(detailMap, "withdraw_status");
                if (MapUtils.getIntValue(detailMap, "withdraw_status") == (WithdrawModel.WITHDRAW_STATUS.YHK))
                {
                    String bankCardName = MapUtils.getString(detailMap, "Bank_name");
                    detailMap.put("withdrawalMethod", bankCardName + " 尾号(" + bankCard + ")");
                }
                else if (MapUtils.getIntValue(detailMap, "withdraw_status") == (WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    detailMap.put("withdrawalMethod", "微信余额");
                }

                detailMap.put("add_date", addTime);
                detailMap.put("Bank_card_number", bankCard);
                detailMap.put("examine_date", DateUtil.dateFormate(MapUtils.getString(detailMap, "examine_date"), GloabConst.TimePattern.YMDHMS));
                detailMap.put("withdrawStatus", withdraw_status);
                detailMap.put("wxStatus", MapUtils.getString(detailMap, "wx_status"));
            }

            resultMap.put("list", detailMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现详情 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawalDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> delWithdrawalDetails(MainVo vo, int shopId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);

            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setStore_id(vo.getStoreId());
            withdrawModel.setId(id);
            withdrawModel = withdrawModelMapper.selectOne(withdrawModel);
            if (withdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXJLBCZ, "提现记录不存在");
            }
            WithdrawModel withdrawUpdate = new WithdrawModel();
            withdrawUpdate.setId(withdrawModel.getId());
            withdrawUpdate.setRecovery(DictionaryConst.ProductRecycle.RECOVERY.toString());

            withdrawModelMapper.updateByPrimaryKeySelective(withdrawUpdate);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除提现明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "returnOrderDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> myWallet(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            MchModel mchModel = publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            //商户余额
            BigDecimal accountAmt = mchModel.getAccount_money();
            //商户可提现金额
            BigDecimal cashAmt = mchModel.getCashable_money();
            //商户积分
            BigDecimal integralAmt = mchModel.getIntegral_money();
            //店铺总收入
            BigDecimal incomeAmtAll;
            //提现说明
            String illustrate = "";
            //提现提示
            String PopUpContent = "";
            //获取商户配置信息
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());
            if (mchConfigModel != null)
            {
                illustrate = mchConfigModel.getIllustrate();
                //提现时间
                String withdrawal_time = mchConfigModel.getWithdrawal_time();
                //当前时间日  没有前置0
                Integer dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME))
                {
                    if (!dayOfMonth.equals(Integer.valueOf(withdrawal_time)))
                    {
                        PopUpContent = "每月" + withdrawal_time + "日可提现，请提现日再操作";
                    }
                }
                else if (mchConfigModel.getWithdrawal_time_open().equals(MchConfigModel.SPECIFY_TIME_PERIOD))
                {
                    String[] split = withdrawal_time.split("-");
                    if (dayOfMonth < Integer.parseInt(split[0]) || dayOfMonth > Integer.parseInt(split[1]))
                    {
                        PopUpContent = "每月" + split[0] + "日至" + split[1] + "日可提现，请提现日再操作";
                    }
                }
            }
            //获取店铺总收入
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            parmaMap.put("type", DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME.toString());
            incomeAmtAll = mchAccountLogModelMapper.sumMchAccountLogDynamic(parmaMap);


            //是否缴纳保证金
            boolean isPayment = publicMchService.judgeMchPromise(vo, user.getUser_id());
            //是否存在保证金审核
            boolean examineStatus = appsMchPromiseService.examineStatus(user.getMchId());
            resultMap.put("isPromiseExamine", examineStatus);

            resultMap.put("is_Payment", isPayment);

            resultMap.put("account_money", accountAmt);
            resultMap.put("cashAmt", cashAmt);
            resultMap.put("integral_money", integralAmt);
            resultMap.put("all_money", incomeAmtAll);
            resultMap.put("illustrate", illustrate);
            resultMap.put("PopUpContent", PopUpContent);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("我的提现 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "myWallet");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> accountDetails(MainVo vo, int shopId, int type, int tabIndex, String startDay, String endDay, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            if (StringUtils.isNotEmpty(startDay))
            {
                parmaMap.put("startDay", startDay);
            }
            if (StringUtils.isNotEmpty(endDay))
            {
                parmaMap.put("endDay", endDay);
            }
            if (StringUtils.isNotEmpty(orderNo))
            {
                parmaMap.put("orderNo", orderNo);
            }
            //明细
            List<Map<String, Object>> list;
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            //type 1=售后明细 2=提现明细 3=收入明细 4=支出明细
            if (type == 1)
            {
                //售后明细
                parmaMap.put("mch_id", shopId);
                parmaMap.put("status", DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
                parmaMap.put("type", DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND);
                list = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);
            }
            else if (type == 2)
            {
                //店铺提现明细
                parmaMap.put("user_id", user.getUser_id());
                if (tabIndex != DictionaryConst.MchExameStatus.EXAME_WAIT_STATUS)
                {
                    tabIndex--;
                    parmaMap.put("status", tabIndex);
                }
                list = withdrawModelMapper.getWithdrawLeftUserBankAndMch(parmaMap);
                BigDecimal outcome = withdrawModelMapper.getWithdrawLeftUserBankAndMchMoney(parmaMap);
                resultMap.put("outcome", outcome);
            }
            else if (type == 3)
            {
                //收入明细
                parmaMap.put("mch_id", shopId);
                parmaMap.put("status", DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_INCOME);
                list = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);
                BigDecimal income = mchAccountLogModelMapper.countMchAccountMoney(parmaMap);
                resultMap.put("income", income);
            }
            else
            {
                //支出明细
                parmaMap.put("mch_id", shopId);
                parmaMap.put("status", DictionaryConst.MchAccountLogStatus.MCHACCOUNTLOG_STATUS_EXPENDITURE);
                list = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);
            }
            list.forEach(map ->
            {
                //类型：1.订单 2.退款 3.提现
                int    type1    = MapUtils.getIntValue(map, "type");
                String typeName = "提现";
                if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER.equals(type1))
                {
                    typeName = "订单";
                }
                else if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND.equals(type1))
                {
                    typeName = "退款";
                }
                else if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_PROMISE.equals(type1))
                {
                    typeName = "保证金";
                }
                map.put("type_name", typeName);
                //备注
                String remake = MapUtils.getString(map, "remake");
                map.put("remake", remake);
                map.put("addtime", DateUtil.dateFormate(MapUtils.getString(map, "addtime"), GloabConst.TimePattern.YMDHMS));
                map.put("examine_date", DateUtil.dateFormate(MapUtils.getString(map, "examine_date"), GloabConst.TimePattern.YMDHMS));
                map.put("price", new BigDecimal(MapUtils.getString(map, "price")).setScale(2, RoundingMode.HALF_DOWN).toString());
            });
//            resultMap.put("list", walletDetailData(list));
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("账户明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "accountDetails");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> walletDetails(MainVo vo, int shopId, int status, String keyWord) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            if (StringUtils.isNotEmpty(keyWord))
            {
                parmaMap.put("keyWord", keyWord);
            }
            //1=收入 2=支出
            parmaMap.put("status", status);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            //如果根据时间来分组,分页只能分时间,默认只展示7天
            parmaMap.put("startDay", DateUtil.getAddDate(-7));
            parmaMap.put("endDay", new Date());
            //明细
            List<Map<String, Object>> list;
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            list = mchAccountLogModelMapper.getMchAccountLogDynamic(parmaMap);

            resultMap.put("list", walletDetailData(list));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("账户收入/支出 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletDetails");
        }
        return resultMap;
    }

    //数据时间分组处理
    private List<Map<String, Object>> walletDetailData(List<Map<String, Object>> list)
    {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try
        {
            //暂存订单信息 时间分组
            Map<String, List<Map<String, Object>>> groupDateMap = new LinkedHashMap<>(16);
            //数据处理
            for (Map<String, Object> map : list)
            {
                String typeName;
                String time = map.get("addtime").toString();
                map.put("date", DateUtil.dateFormate(time, GloabConst.TimePattern.HMS));
                time = DateUtil.dateFormate(time, GloabConst.TimePattern.YMD2);
                map.put("time", time);
                map.put("addtime", time);
                //类型：1.订单 2.退款 3.提现
                int type = MapUtils.getIntValue(map, "type");
                if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER.equals(type))
                {
                    typeName = "订单";
                }
                else if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_REFUND.equals(type))
                {
                    typeName = "退款";
                }
                else if (DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_PROMISE.equals(type))
                {
                    typeName = "保证金";
                }
                else
                {
                    typeName = "提现";
                }
                map.put("type_name", typeName);
                //备注
                String remake = MapUtils.getString(map, "remake");
                map.put("remake", remake);
                //同一个时段合并
                List<Map<String, Object>> tempMap = new ArrayList<>();
                if (groupDateMap.containsKey(time))
                {
                    tempMap = groupDateMap.get(time);
                }
                tempMap.add(map);
                groupDateMap.put(time, tempMap);
            }
            //存储数据结构
            for (String key : groupDateMap.keySet())
            {
                Map<String, Object>       map      = new HashMap<>(16);
                List<Map<String, Object>> tempList = groupDateMap.get(key);
                map.put("res", tempList);
                map.put("time", key);
                resultList.add(map);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("账户收入/支出 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletDetails");
        }
        return resultList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addStore(AddStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            int           count;
            MchStoreModel mchStoreOld = null;
            if (vo.getId() != null)
            {
                mchStoreOld = new MchStoreModel();
                mchStoreOld.setStore_id(vo.getStoreId());
                mchStoreOld.setId(vo.getId());
                mchStoreOld = mchStoreModelMapper.selectOne(mchStoreOld);
                if (mchStoreOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
                }
            }
            //验证店铺名称
            if (mchStoreOld == null || !mchStoreOld.getName().equals(vo.getName()))
            {
                MchStoreModel mchStore = new MchStoreModel();
                mchStore.setStore_id(vo.getStoreId());
                mchStore.setMch_id(vo.getShopId());
                mchStore.setName(vo.getName());
                count = mchStoreModelMapper.selectCount(mchStore);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在");
                }
            }
           /* if (!MobileUtils.isMobile(vo.getMobile()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
            }*/
            //保存数据
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setStore_id(vo.getStoreId());
            mchStoreModel.setMch_id(vo.getShopId());
            mchStoreModel.setName(vo.getName());
            mchStoreModel.setMobile(vo.getMobile());
            mchStoreModel.setCpc(vo.getCpc());
            mchStoreModel.setBusiness_hours(vo.getBusinessHours());

            String sheng = "";
            String shi = "";
            String xian = "";
            if (vo.getCpc().equals("86") || vo.getCpc().equals("852"))
            {
                String ssx = vo.getCityAll();
                if (StringUtils.isEmpty(ssx))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSXBNWK, "省市县不能为空");
                }
                String[] ssxList = ssx.split("-");
                if (ssxList.length != 3)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SSXBNWK, "省市县不能为空");
                }
                sheng =  ssxList[0];
                shi =  ssxList[1];
                xian =  ssxList[2];
            }
            else
            {
                sheng = vo.getProvince();
                shi = vo.getCity();
                xian = "";
            }

            mchStoreModel.setSheng(sheng);
            mchStoreModel.setShi(shi);
            mchStoreModel.setXian(xian);
            mchStoreModel.setAddress(vo.getAddress());
            mchStoreModel.setIs_default(vo.getIsDefault());
           /* String apiKey  = "";
            String address = mchStoreModel.getSheng() + mchStoreModel.getShi() + mchStoreModel.getXian();
            //获取商城配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                apiKey = configModel.getTencent_key();
            }
           try {
               //更具地址获取坐标
               Map<String, String> latAndLng = TengxunMapUtil.getlatAndLng(apiKey, address);
               mchStoreModel.setLongitude(latAndLng.get("lng"));
               mchStoreModel.setLatitude(latAndLng.get("lat"));
           } catch (Exception e) {
               logger.debug("获取地址坐标错误", e);
           }*/
            //取消默认
            if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                mchStoreModelMapper.updateNotDefault(vo.getStoreId(), vo.getShopId());
            }

            if (vo.getId() != null && vo.getId() > 0)
            {
                //修改
                mchStoreModel.setId(vo.getId());
                //如果是取消默认店铺,并且没有默认店铺了,则默认给一个门店为默认
                MchStoreModel updateMchStore = new MchStoreModel();
                updateMchStore.setId(vo.getId());
                if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_NO)
                {
                    updateMchStore.setStore_id(vo.getStoreId());
                    updateMchStore.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                    count = mchStoreModelMapper.updateByPrimaryKeySelective(updateMchStore);
                    if (count < 1)
                    {
                        logger.info("修改默认店铺失败 参数:" + JSON.toJSONString(updateMchStore));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                }
                count = mchStoreModelMapper.updateByPrimaryKeySelective(mchStoreModel);
            }
            else
            {
                mchStoreModel.setAdd_date(new Date());
                count = mchStoreModelMapper.insertSelective(mchStoreModel);

                if (vo.getWriteList() != null && vo.getWriteList().size() > 0)
                {
                    List<MchStoreWriteModel> mchStoreWriteModels = new ArrayList<>();

                    for (AddStoreVo.write_time write_time : vo.getWriteList())
                    {
                        //添加核销时间
                        if (StringUtils.isEmpty(write_time.getWrite_time()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                        else if (StringUtils.isEmpty(write_time.getWrite_off_num()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }
                        else if (StringUtils.isEmpty(write_time.getWrite_date()))
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                        }

                        String   date       = write_time.getWrite_date();
                        String   time       = write_time.getWrite_time();
                        String[] split_date = date.split(SplitUtils.BL);
                        String[] split_time = time.split(SplitUtils.BL);
                        String   start_time = split_date[0] + split_time[0];
                        String   end_time   = split_date[1] + split_time[1];
                        Date     start      = DateUtil.dateFormateToDate(start_time, GloabConst.TimePattern.YMDHMS);
                        Date     end        = DateUtil.dateFormateToDate(end_time, GloabConst.TimePattern.YMDHMS);

                        //判断核销时间是否重复
                        // 获取日期范围内的所有日期
                        List<String> intervalDate = DateUtil.getIntervalDate(split_date[0], split_date[1]);
                        int          length       = intervalDate.size();
                        String       off_num      = "";
                        for (int i = 0; i < length; i++)
                        {
                            if (i == 1)
                            {
                                off_num = "0";
                            }
                            else
                            {
                                off_num = off_num + ",0";
                            }
                        }

                        MchStoreWriteModel mchStoreWriteModel = new MchStoreWriteModel();
                        mchStoreWriteModel.setStart_time(start);
                        mchStoreWriteModel.setStart_time(end);
                        mchStoreWriteModel.setWrite_off_num(write_time.getWrite_off_num());
                        mchStoreWriteModel.setStore_id(vo.getStoreId());
                        mchStoreWriteModel.setMch_id(vo.getShopId());
                        mchStoreWriteModel.setOff_num(off_num);
                        mchStoreWriteModel.setMch_store_id(mchStoreModel.getId());
                        mchStoreWriteModel.setAdd_time(new Date());
                        mchStoreWriteModels.add(mchStoreWriteModel);
                    }

                    int i = mchStoreWriteModelMapper.insertList(mchStoreWriteModels);
                    if (i < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "添加核销时间失败", "editStorePage");
                    }
                }

            }
            if (count < 1)
            {
                logger.info("门店添加/修改失败/添加核销信息失败 参数:" + JSON.toJSONString(mchStoreModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
            }
            if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_NO)
            {
                //是否有默认店铺,没有则给最旧的门店设置默认
                MchStoreModel mchStore = new MchStoreModel();
                mchStore.setStore_id(vo.getStoreId());
                mchStore.setMch_id(vo.getShopId());
                mchStore.setIs_default(1);
                int num = mchStoreModelMapper.selectCount(mchStore);
                if (num < 1)
                {
                    mchStoreModelMapper.setDefaultStore(mchStore.getStore_id(), mchStore.getMch_id());
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
            logger.error("添加店铺 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStore");
        }
    }

    @Override
    public Map<String, Object> editStorePage(MainVo vo, Integer shopId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //验证店铺
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                MchStoreModel mchStoreModel = new MchStoreModel();
                mchStoreModel.setStore_id(vo.getStoreId());
                mchStoreModel.setMch_id(shopId);
                mchStoreModel.setId(id);
                mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                Map<String, Object> mchStoreMap = new HashMap<>(16);
                if (mchStoreModel != null)
                {
                    String   businessHours     = mchStoreModel.getBusiness_hours();
                    String[] businessHoursList = businessHours.split(",");
                    mchStoreModel.setProvince(mchStoreModel.getSheng());
                    mchStoreModel.setCity(mchStoreModel.getShi());
                    mchStoreMap = JSON.parseObject(JSON.toJSONString(mchStoreModel), new TypeReference<Map<String, Object>>()
                    {
                    });
                    mchStoreMap.put("business_hours", businessHoursList);
                }
                resultMap.put("list", mchStoreMap);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> seeMyStore(MainVo vo, int shopId, Integer proId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            MchStoreModel mchStoreModel = new MchStoreModel();
            mchStoreModel.setMch_id(shopId);
            mchStoreModel.setStore_id(vo.getStoreId());
            Map<String, Object>       parmaMap     = new HashMap<>(16);
            List<Map<String, Object>> mchStoreList = new ArrayList<>();
            if (StringUtils.isNotEmpty(proId))
            {
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(proId);
                if (productListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在", "seeMyStore");
                }
                String write_off_mch_ids = productListModel.getWrite_off_mch_ids();
                if (StringUtils.isNotEmpty(write_off_mch_ids) && !write_off_mch_ids.equals("0"))
                {
                    List<String> ids = Arrays.asList(write_off_mch_ids.split(SplitUtils.DH));
                    parmaMap.put("idList", ids);
                    List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);

                    for (MchStoreModel storeModel : mchStoreModelList)
                    {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", storeModel.getName());
                        map.put("phone", storeModel.getMobile());
                        map.put("business_hours", storeModel.getBusiness_hours());
                        map.put("address", storeModel.getSheng() + " " + storeModel.getShi() + " " + storeModel.getXian() + " " + storeModel.getAddress());
                        mchStoreList.add(map);
                    }
                }
            }
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());

            List<Map<String, Object>> mchStoreModelList = mchStoreModelMapper.selectDynamic(parmaMap);
            resultMap.put("list", mchStoreModelList);
            resultMap.put("mchStoreList", mchStoreList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查看我的门店 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "seeMyStore");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverySave(MainVo vo, String orderDetailIds) throws LaiKeAPIException
    {
        try
        {
            User       user         = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            ShipDataVo shippingData = JSON.parseObject(orderDetailIds, ShipDataVo.class);


            //1普通发货 2电子面单 3商家配送
            if (shippingData.getType() == 2)
            {
                appsMchExpressService.FaceSheetSend(vo, shippingData.getExpressId(), JSON.toJSONString(shippingData.getOrderList()));
            }
            else if (shippingData.getType() == 3)
            {
                selfSend(vo, user.getMchId(), orderDetailsModelMapper.selectByPrimaryKey(shippingData.getOrderList().get(0).getDetailId()).getR_sNo(), shippingData.getPsyInfo().getTel(), shippingData.getPsyInfo().getName());
            }
            else
            {
                // 请选择快递公司
                if (shippingData.getExpressId() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZKDGS, "请选择快递公司");
                }
                if (StringUtils.isEmpty(shippingData.getCourierNumber()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRKDDH, "请输入快递单号");
                }
                int count = orderDetailsModelMapper.getDeliverNumByExIdAndExNo(shippingData.getExpressId().toString(), shippingData.getCourierNumber());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KDDHYCZ, "快递单号已存在");
                }
                send(vo, user.getMchId(), orderDetailsModelMapper.selectByPrimaryKey(shippingData.getOrderList().get(0).getDetailId()).getR_sNo(), shippingData.getExpressId(), shippingData.getCourierNumber(), JSON.toJSONString(shippingData.getOrderList()));
            }


        }
        catch (LaiKeAPIException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error("订单发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderPrint");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean editStore(AddStoreVo vo) throws LaiKeAPIException
    {
        try
        {
            return addStore(vo);
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("编辑我的门店 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStore");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delStore(MainVo vo, int shopId, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                String[] idList = ids.split(",");
                for (String id : idList)
                {
                    MchStoreModel mchStoreModel = new MchStoreModel();
                    mchStoreModel.setStore_id(vo.getStoreId());
                    mchStoreModel.setMch_id(shopId);
                    mchStoreModel.setId(Integer.parseInt(id));
                    //虚拟商品，当门店下还有订单未关闭，则不能删除门店
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setMch_store_write_id(Integer.parseInt(id));
                    List<OrderDetailsModel> orderDetailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    if (orderDetailsModelList != null && orderDetailsModelList.size() > 0)
                    {
                        for (OrderDetailsModel orderDetailsModel1 : orderDetailsModelList)
                        {
                            if (orderDetailsModel1.getR_status() != DictionaryConst.OrdersStatus.ORDERS_R_STATUS_CLOSE)
                            {
                                logger.info("门店下有订单未关闭，不能删除门店");
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_GMDXHYDDWGB, "门店下有订单未关闭，不能删除门店", "delStore");
                            }
                        }
                    }
                    //处理商品表中和核销门店的对应删除
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("mch_id", shopId);
                    map.put("store_id", user.getStore_id());
                    List<ProductListModel> products = productListModelMapper.selectForStore(map);
                    for (ProductListModel product : products)
                    {
                        String join = "";
                        if (product.getWrite_off_mch_ids().equals("0"))
                        {
                            //查询该店铺下的所有门店，除了该删除门店
                            MchStoreModel mchStoreModel1 = new MchStoreModel();
                            mchStoreModel1.setStore_id(user.getStore_id());
                            mchStoreModel1.setMch_id(shopId);
                            List<MchStoreModel> select = mchStoreModelMapper.select(mchStoreModel1);
                            List<Integer> mchIds = select.stream()
                                    .filter(x -> x.getId() != Integer.parseInt(id))
                                    .map(MchStoreModel::getId)
                                    .collect(Collectors.toList());
                            join = Joiner.on(SplitUtils.DH).join(mchIds);
                        }
                        else
                        {
                            String[]     split = product.getWrite_off_mch_ids().split(SplitUtils.DH);
                            List<String> s_ids = new ArrayList<>(Arrays.asList(split));
                            if (!s_ids.isEmpty() && s_ids.contains(id))
                            {
                                if (s_ids.size() > 1)
                                {
                                    s_ids.remove(id);
                                }
                                else
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_YJSSPDZHYGMDBKSC, "已经是商品的最后一个门店不可删除", "delStore");
                                }
                            }
                            else
                            {
                                continue;
                            }
                            join = Joiner.on(SplitUtils.DH).join(s_ids);
                        }
                        product.setWrite_off_mch_ids(join);
                        productListModelMapper.updateByPrimaryKey(product);
                    }
                    int count = mchStoreModelMapper.deleteByPrimaryKey(mchStoreModel);
                    if (count < 1)
                    {
                        logger.info("删除门店失败 参数:" + JSON.toJSONString(mchStoreModel));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MDBCZ, "门店不存在", "delStore");
                    }
                }
                //是否还有默认店铺,没有则给最旧的门店设置默认
                MchStoreModel mchStore = new MchStoreModel();
                mchStore.setStore_id(vo.getStoreId());
                mchStore.setMch_id(shopId);
                mchStore.setIs_default(1);
                int num = mchStoreModelMapper.selectCount(mchStore);
                if (num < 1)
                {
                    mchStoreModelMapper.setDefaultStore(mchStore.getStore_id(), mchStore.getMch_id());
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
            e.printStackTrace();
            logger.error("删除我的门店 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delStore");
        }
        return false;
    }

    @Override
    public Map<String, Object> orderInfoForCode(MainVo vo, int shopId, Integer orderId, String extractionCode, Integer writeShopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            if (extractionCode != null)
            {
                if (extractionCode.contains(SplitUtils.DH))
                {
                    parmaMap.put("extraction_code1", extractionCode);
                }
                else
                {
                    parmaMap.put("extraction_code", extractionCode);
                }
            }
            if (orderId != null)
            {
                parmaMap.put("id", orderId);
            }
            List<Map<String, Object>> orderInfo = orderModelMapper.getOrdersNumDynamic(parmaMap);
            //String pId=MapUtils.getString();
            if (orderId == null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            if (orderInfo.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            /*if (!MapUtils.getInteger(orderInfo.get(0),"id").equals(orderId)){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }*/
            Map<String, Object> map = new HashMap<>();
            if (orderInfo != null && orderInfo.size() > 0)
            {
                map = orderInfo.get(0);
                //无需预约的且没有核销门店的，需要提供核销门店
                if (writeShopId == null && map.containsKey("is_appointment") && DataUtils.getIntegerVal(map, "is_appointment") == 1)
                {
                    List<Map<String, Object>> mchStoreList      = new ArrayList<>();
                    String                    write_off_mch_ids = DataUtils.getStringVal(map, "write_off_mch_ids");
                    if (StringUtils.isNotEmpty(write_off_mch_ids) && !write_off_mch_ids.equals("0"))
                    {
                        List<String> ids = Arrays.asList(write_off_mch_ids.split(SplitUtils.DH));
                        parmaMap.put("idList", ids);
                        List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);

                        for (MchStoreModel storeModel : mchStoreModelList)
                        {
                            Map<String, Object> mchMap = new HashMap<>();
                            mchMap.put("id", storeModel.getId());
                            mchMap.put("name", storeModel.getName());
                            mchMap.put("phone", storeModel.getMobile());
                            mchMap.put("business_hours", storeModel.getBusiness_hours());
                            mchMap.put("address", storeModel.getSheng() + " " + storeModel.getShi() + " " + storeModel.getXian() + " " + storeModel.getAddress());
                            mchStoreList.add(mchMap);
                        }
                    }
                    else if (StringUtils.isNotEmpty(write_off_mch_ids) && write_off_mch_ids.equals("0"))
                    {
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("mch_id", shopId);
                        params.put("is_default_sort", DataUtils.Sort.DESC.toString());
                        params.put("add_date_sort", DataUtils.Sort.DESC.toString());
                        mchStoreList = mchStoreModelMapper.selectDynamic(params);
                    }
                    resultMap.put("hxShop", mchStoreList);
                }
                //订单状态
                orderId = MapUtils.getInteger(map, "id");
                int status = Integer.parseInt(map.get("status").toString());
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED || status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
                {
                    //给map中填入show_write_time表示有预约信息需要展示
                    if (MapUtils.getString(map, "write_time") != null && !MapUtils.getString(map, "write_time").equals(""))
                    {
                        map.put("show_write_time", 1);
                    }

                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }

            parmaMap.clear();
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_id", MapUtils.getString(orderInfo.get(0), "user_id"));
            parmaMap.put("id", orderId);
            List<Map<String, Object>> orderCodesInfo = orderModelMapper.seeExtractionCode(parmaMap);
            List<Map<String, Object>> products       = new ArrayList<>();
            for (int i = 0; i < orderCodesInfo.size(); i++)
            {
                Map<String, Object> productMap = orderCodesInfo.get(i);
                // 商品ID
                int proId = MapUtils.getInteger(productMap, "pid");
                // 商品规格id
                int                 productSid = MapUtils.getInteger(productMap, "sid");
                Map<String, Object> params     = new HashMap<>();
                params.put("store_id", vo.getStoreId());
                params.put("sid", productSid);
                params.put("p_id", proId);
                List<Map<String, Object>> productInfoList = productListModelMapper.getGoodsTitleAndImg(params);
                if (CollectionUtils.isEmpty(productInfoList))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YWYC, "业务异常", "seeExtractionCode");
                }
                Map<String, Object> productInfoMap = productInfoList.get(0);
                // 商品名称
                String product_title = DataUtils.getStringVal(productInfoMap, "product_title");
                // 拼图片路径
                String img = publiceService.getImgPath(DataUtils.getStringVal(productInfoMap, "img"), vo.getStoreId());
                //判断是否是核销商品，核销商品还需要返回待核销次数，write_off_num
                Map<String, Object> productRetMap   = new HashMap<>();
                Map<String, Object> write_time_info = new HashMap<>();
                if (productMap.containsKey("write_off_num"))
                {
                    productRetMap.put("write_off_num", DataUtils.getIntegerVal(productMap, "write_off_num"));
                }
                if (productMap.containsKey("write_time"))
                {
                    write_time_info.put("time", DataUtils.getStringVal(productMap, "write_time"));
                }
                if (productMap.containsKey("mch_store_write_id"))
                {
                    MchStoreModel mchStoreModel = mchStoreModelMapper.selectByPrimaryKey(DataUtils.getIntegerVal(productMap, "mch_store_write_id"));
                    write_time_info.put("mch_store", mchStoreModel.getName());
                    write_time_info.put("mch_store_write_id", DataUtils.getIntegerVal(productMap, "mch_store_write_id"));
                    map.put("write_time_info", write_time_info);
                }
                //无需预约的商品选择的门店id//如果选择的门店位于商品的可核销门店中，则正常展示信息，否则不返回信息给前端
                if (writeShopId != null)
                {
                    map.put("write_shop_id", writeShopId);
                    //mch_store_write_id
                    for (Map<String, Object> m : productInfoList)
                    {
                        //取出商品可核销门店，多门店用，隔开的，为0则为全部门店，33，44，55
                        String   mchIds = MapUtils.getString(m, "write_off_mch_ids");
                        String[] split  = mchIds.split(SplitUtils.DH);
                        for (int j = 0; j < split.length; j++)
                        {
                            if (writeShopId.toString().equals(split[j]) || "0".equals(split[j]))
                            {
                                //不是所选的门店核销商品的不显示
                                resultMap.put("orderInfo", map);
                                productRetMap.put("p_id", proId);
                                productRetMap.put("product_title", product_title);
                                productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                                productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                                productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                                productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                                productRetMap.put("img", img);
                                products.add(productRetMap);
                            }
                        }
                    }
                }
                else
                {
                    resultMap.put("orderInfo", map);
                    productRetMap.put("p_id", proId);
                    productRetMap.put("product_title", product_title);
                    productRetMap.put("p_price", DataUtils.getBigDecimalVal(productMap, "p_price", BigDecimal.ZERO));
                    productRetMap.put("num", MapUtils.getInteger(productMap, "num"));
                    productRetMap.put("sid", MapUtils.getInteger(productMap, "sid"));
                    productRetMap.put("size", DataUtils.getStringVal(productMap, "size"));
                    productRetMap.put("img", img);
                    products.add(productRetMap);
                }
            }
            if (products.size() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
            }
            resultMap.put("por_list", products);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证码扫码订单信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "orderInfoForCode");
        }
        return resultMap;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> verificationExtractionCode(MainVo vo, int shopId, Integer orderId, String extractionCode, Integer mch_store_id, Integer pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", shopId);
            if (orderId == null)
            {
                if (extractionCode.contains(SplitUtils.DH))
                {
                    parmaMap.put("extraction_code1", extractionCode);
                }
                else
                {
                    parmaMap.put("extraction_code", extractionCode);
                }
            }
            parmaMap.put("id", orderId);
            List<Map<String, Object>> orderInfo = orderModelMapper.getOrdersNumDynamic(parmaMap);
            if (orderId == null && (orderInfo == null || orderInfo.size() < 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
            }
            if (orderInfo != null && orderInfo.size() > 0)
            {
                Map<String, Object> map = orderInfo.get(0);
                //订单号
                String orderno = map.get("sNo").toString();
                //订单状态
                int status = Integer.parseInt(map.get("status").toString());
                //订单金额
                BigDecimal orderAmt = new BigDecimal(map.get("z_price").toString());
                //积分
                BigDecimal allow = new BigDecimal("0");
                if (map.containsKey("allow"))
                {
                    allow = new BigDecimal(map.get("allow").toString());
                }
                if (status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_DISPATCHED || status == DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED)
                {
                    //取货代码 E9ds5B,1601349348,1601351148
                    String keyCode = MapUtils.getString(map, "extraction_code");
                    if (StringUtils.isEmpty(keyCode))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QHMBCZ, "取货码不存在");
                    }
                    String[] codeStr = keyCode.split(SplitUtils.DH);
                    if (codeStr.length < 3 || StringUtils.isEmpty(codeStr[0]) || !StringUtils.isInteger(codeStr[2]))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                    }
                    //验证码
                    String code = codeStr[0];
                    //失效时间
                    long endTime = Long.parseLong(codeStr[2]);
                    //系统时间
                    long sysTime = Long.parseLong(DateUtil.timeStamp());
                    if (sysTime > endTime)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMYSX, "验证码已失效");
                    }
                    String   validateCodeParam = extractionCode == null ? "" : extractionCode;
                    String[] validateCode      = validateCodeParam.split(SplitUtils.DH);
                    String   validateCodeNo    = validateCode.length > 0 ? validateCode[0] : "";
                    if (!code.equals(validateCodeNo))
                    {
                        logger.debug("参数{} 校验码{}", validateCodeNo, code);
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMCW, "验证码错误");
                    }
                    //标记订单已完成，如果商品为虚拟商品则需要判断是否进入已完成状态
                    parmaMap.clear();
                    parmaMap.put("orderno", orderno);

                    //订单详情的更新参数
                    Map<String, Object> detailParmaMap = new HashMap<>();
                    //核销完成标志
                    boolean writeOver = false;
                    //虚拟订单特殊处理
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                       /* ReturnOrderModel returnOrderModel = new ReturnOrderModel();
                        returnOrderModel.setsNo(orderno);
                        List<ReturnOrderModel> select = returnOrderModelMapper.select(returnOrderModel);
                        if (select.size()>0){
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_HXCSYW, "核销次数已用完");
                        }*/
                        //核销记录录入
                        WriteRecordModel writeRecordModel = new WriteRecordModel();
                        //待核销次数
                        int write_off_num = MapUtils.getIntValue(map, "write_off_num");
                        //已预约核销次数
                        int after_write_off_num = MapUtils.getIntValue(map, "after_write_off_num");
                        if (write_off_num <= 0)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_HXCSYW, "核销次数已用完");
                        }
                        //更新订单详情中的核销次数
                        detailParmaMap.put("write_off_num", write_off_num - 1);
                        if (write_off_num - 1 < 1)
                        {
                            detailParmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                        }
                        detailParmaMap.put("after_write_off_num", after_write_off_num + 1);

                        //多商品核销处理
                        if (orderInfo.size() > 1)
                        {
                            OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                            orderDetailsModel.setR_sNo(orderno);
                            orderDetailsModel.setP_id(pid);
                            OrderDetailsModel orderDetailsModel_result = orderDetailsModelMapper.selectOne(orderDetailsModel);
                            writeRecordModel.setP_id(orderDetailsModel_result.getId());
                            //详单中对于具体哪个商品来进行处理
                            detailParmaMap.put("p_id", pid);
                            //订单表中当详单中所有的商品都核销完后再变成完成状态
                            Integer num = 0;
                            for (Map<String, Object> o_map : orderInfo)
                            {
                                num += DataUtils.getIntegerVal(o_map, "write_off_num");
                            }
                            if (num > 1)
                            {
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
                            }
                            else
                            {
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                                parmaMap.put("arrive_time", new Date());
                            }
                        }
                        else
                        {
                            writeRecordModel.setP_id(MapUtils.getInteger(map, "dId"));
                            if (write_off_num - 1 < 1)
                            {
                                parmaMap.put("arrive_time", new Date());
                                parmaMap.put("status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                                detailParmaMap.put("arrive_time", new Date());
                                writeOver = true;
                            }
                        }
                        //添加核销记录
                        writeRecordModel.setS_no(orderno);
                        writeRecordModel.setWrite_code(extractionCode);
                        writeRecordModel.setWrite_time(new Date());
                        //如果是线下核销且无需预约，则使用核销时用户选择的门店信息，有预约的详单表中有数据
                        if (MapUtils.getIntValue(map, "is_appointment") == ProductListModel.IS_APPOINTMENT.noOpin)
                        {
                            writeRecordModel.setWrite_store_id(mch_store_id);
                        }
                        else
                        {
                            writeRecordModel.setWrite_store_id(DataUtils.getIntegerVal(map, "mch_store_write_id"));
                        }
                        if (DataUtils.getIntegerVal(detailParmaMap, "write_off_num") > 0)
                        {
                            writeRecordModel.setStatus(WriteRecordModel.status.continueWrite);
                        }
                        else
                        {
                            writeRecordModel.setStatus(WriteRecordModel.status.isWrite);
                        }
                        int i = writeRecordModelMapper.insertSelective(writeRecordModel);
                        if (i < 1)
                        {
                            logger.info("添加核销记录失败 参数:" + JSON.toJSONString(writeRecordModel));
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                        }

                    }
                    //订单的状态处理
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")) && DataUtils.getIntegerVal(detailParmaMap, "write_off_num") > 0)
                    {
                        detailParmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_TOBEVERIFIED);
                    }
                    else
                    {
                        detailParmaMap.put("r_status", DictionaryConst.OrdersStatus.ORDERS_R_STATUS_COMPLETE);
                    }
                    //店主核销
                    parmaMap.put("VerifiedBy_type", 1);
                    int count = orderModelMapper.updateByOrdernoDynamic(parmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + JSON.toJSONString(parmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //标记明细状态
                    parmaMap.clear();
                    detailParmaMap.put("orderno", orderno);
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(MapUtils.getString(map, "otype")))
                    {
                        detailParmaMap.put("arrive_time", new Date());
                    }
                    count = orderDetailsModelMapper.updateByOrdernoDynamic(detailParmaMap);
                    if (count < 1)
                    {
                        logger.info("订单标记已完成失败 参数:" + JSON.toJSONString(detailParmaMap));
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常");
                    }
                    //卖家收入,虚拟商品需要等到全部核销次数完后才记录
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        if (writeOver)
                        {
                            publicMchService.clientConfirmReceipt(vo.getStoreId(), shopId, orderno, orderAmt, allow);
                        }
                    }
                    else
                    {
                        publicMchService.clientConfirmReceipt(vo.getStoreId(), shopId, orderno, orderAmt, allow);
                    }
                    //确认收货 普通商品赠送积分(只有普通订单可以自提，if判断可以不用)  250613新增虚拟商品自提赠送积分
                    if (DictionaryConst.OrdersType.ORDERS_HEADER_GM.equals(MapUtils.getString(map, "otype")) ||
                            DictionaryConst.OrdersType.ORDERS_HEADER_VI.equals(MapUtils.getString(map, "otype")))
                    {
                        //如果订单之前已经获得了一笔积分则不再获取
                        SignRecordModel signRecordCount = new SignRecordModel();
                        signRecordCount.setStore_id(vo.getStoreId());
                        signRecordCount.setUser_id(MapUtils.getString(map, "user_id"));
                        signRecordCount.setsNo(orderno);
                        if (signRecordModelMapper.selectCount(signRecordCount) == 0)
                        {
                            publicMemberService.memberSettlement(vo.getStoreId(), MapUtils.getString(map, "user_id"),
                                    orderno, new BigDecimal(MapUtils.getString(map, "z_price", "0")), IntegralConfigModel.GiveStatus.RECEIVING);
                        }
                        else
                        {
                            logger.debug("订单{}已经获得过一笔【会员购物积分】积分,此次不获嘚积分.", orderno);
                        }
                    }
                    resultMap.put("sNo", orderno);
                    resultMap.put("p_price", orderAmt);
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZMWX, "验证码无效");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DDBCZ, "订单不存在");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证提货码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationExtractionCode");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> freightList(MainVo vo, int shopId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
                PageModel pageModel = new PageModel(vo.getPageNo(), vo.getPageSize(), vo.getPageNum());
                resultMap = publicMchService.freightList(vo, shopId, null, null, pageModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightList");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addFreight(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());

            if (!publicMchService.freightAdd(vo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJYFSB, "添加运费失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addFreight");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void freightDel(MainVo vo, String ids, int shopId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                //验证数据
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);

                publicMchService.freightDel(vo.getStoreId(), ids,shopId);
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除运费 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCYF, "删除运费", "freightDel");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefault(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getShopId());

                publicMchService.setDefault(vo);
            }
        }
        catch (LaiKeAPIException l)
        {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("设置默认运费 异常" + e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
        }
    }

    @Override
    public Map<String, Object> freightModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), mchId);
                resultMap = publicMchService.freightModifyShow(vo, mchId, id);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载编辑运费页面 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightModifyShow");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void examine(RefundVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), vo.getMchId());
            if (!publicRefundService.refund(vo))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHSB_001, "审核失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("售后流程 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examine");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBank(MainVo vo, int bankId) throws LaiKeAPIException
    {
        try
        {
            User          user          = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            BankCardModel bankCardModel = new BankCardModel();
            bankCardModel.setMch_id(user.getMchId());
            bankCardModel.setId(bankId);
            bankCardModel = bankCardModelMapper.selectOne(bankCardModel);
            if (bankCardModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXKBCZ, "银行卡不存在");
            }
            BankCardModel bankCardUpdate = new BankCardModel();
            bankCardUpdate.setId(bankCardModel.getId());
            bankCardUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            int row = bankCardModelMapper.updateByPrimaryKeySelective(bankCardUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }

            //当前是否是默认银行卡,如果是则默认另外一张,否则不处理
            if (bankCardModel.getIs_default() == 1)
            {
                Integer id = bankCardModelMapper.getNewCardOneByMchId(user.getMchId());
                if (id != null)
                {
                    BankCardModel bankCardDefault = new BankCardModel();
                    bankCardDefault.setId(id);
                    bankCardDefault.setIs_default(1);
                    bankCardModelMapper.updateByPrimaryKeySelective(bankCardDefault);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("解绑银行卡 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBank");
        }
    }

    @Override
    public Map<String, Object> getSettlementOrderList(OrderSettlementVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            vo.setMchId(user.getMchId());
            resultMap = publicOrderService.getSettlementOrderList(vo);
            //结算金额
            /*BigDecimal settlementPrice = BigDecimal.ZERO;
            List<Map<String, Object>> list= (List<Map<String, Object>>) resultMap.get("allOrderList");
            for (Map<String, Object> map : list) {
                settlementPrice = settlementPrice.add(DataUtils.getBigDecimalVal(map,"z_price"));
            }*/
            resultMap.put("settlementPrice", DataUtils.getBigDecimalVal(resultMap, "allSettlementPrice"));
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("订单结算列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMchStoreAccount(MchStoreAccountVo vo) throws LaiKeAPIException
    {
        try
        {
            User                 user       = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchStoreAccountModel oldAccount = null;
            if (vo.getId() != null)
            {
                oldAccount = mchStoreAccountModelMapper.selectByPrimaryKey(vo.getId());
            }
            MchStoreAccountModel mchStoreAccountModel = new MchStoreAccountModel();
            if (oldAccount == null || !oldAccount.getAccount_number().equals(vo.getAccountNumber()))
            {
                mchStoreAccountModel.setStore_id(vo.getStoreId());
                mchStoreAccountModel.setAccount_number(vo.getAccountNumber());
                mchStoreAccountModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
                int i = mchStoreAccountModelMapper.selectCount(mchStoreAccountModel);
                if (i > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHYCZ, "账号已存在");
                }
            }
            mchStoreAccountModel = new MchStoreAccountModel();
            mchStoreAccountModel.setAccount_number(vo.getAccountNumber());
            mchStoreAccountModel.setPassword(CryptoUtil.strEncode(vo.getPassWord()));
            if (oldAccount == null)
            {
                mchStoreAccountModel.setStore_id(vo.getStoreId());
                mchStoreAccountModel.setMch_id(user.getMchId());
                mchStoreAccountModel.setMch_store_id(vo.getMchStoreId());
                mchStoreAccountModel.setAdd_date(new Date());
                mchStoreAccountModelMapper.insertSelective(mchStoreAccountModel);
            }
            else
            {
                mchStoreAccountModel.setId(oldAccount.getId());
                mchStoreAccountModelMapper.updateByPrimaryKeySelective(mchStoreAccountModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加门店账户 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMchStoreAccount(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            User                 user                 = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchStoreAccountModel mchStoreAccountModel = mchStoreAccountModelMapper.selectByPrimaryKey(id);
            if (mchStoreAccountModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "账户不存在");
            }
            mchStoreAccountModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_OK);
            mchStoreAccountModelMapper.updateByPrimaryKeySelective(mchStoreAccountModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除门店账户 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
    }

    @Override
    public Map<String, Object> mchStoreAccountList(MchStoreAccountVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (vo.getId() != null)
            {
                paramMap.put("id", vo.getId());
            }
            if (vo.getMchStoreId() != null)
            {
                paramMap.put("mch_store_id", vo.getMchStoreId());
            }
            int                       i    = mchStoreAccountModelMapper.countList(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = mchStoreAccountModelMapper.selectList(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    String lastLogin = MapUtils.getString(map, "last_login");
                    if (StringUtils.isNotEmpty(lastLogin))
                    {
                        map.put("last_login", DateUtil.dateFormate(lastLogin, GloabConst.TimePattern.YMDHMS));
                    }
                    map.put("password", CryptoUtil.strDecode(MapUtils.getString(map, "password")));
                }
            }
            resultMap.put("list", list);
            resultMap.put("total", i);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("门店账户列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectionCode(MainVo vo, Integer mchId, String code) throws LaiKeAPIException
    {
        try
        {
            User     user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "collectionCode");
            }
            String urlImgByName = ImgUploadUtils.getUrlImgByName(code, true);
            mchModel.setCollection_code(urlImgByName);
            mchModelMapper.updateByPrimaryKeySelective(mchModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传收款二维码 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "collectionCode");
        }
    }


    /**
     * 店铺主页加载商品信息
     *
     * @param vo     -
     * @param shopId -
     * @param type   -
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/23 17:24
     */
    private List<Map<String, Object>> getMchIndexGoodsInfo(MainVo vo, int shopId, int type, String lang_code) throws LaiKeAPIException
    {
        //商品信息集
        List<Map<String, Object>> goodsInfoList;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            //sql参数列表
            Map<String, Object> parmaMap = new HashMap<>(16);

            //会员等级
            BigDecimal grade = new BigDecimal("0");
            //用户折扣
            BigDecimal gradeRate = new BigDecimal("1");

            //当前店铺可显示的商品状态
            List<String> goodsStatus = new ArrayList<>();
            goodsStatus.add(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());

            boolean isGrade = false;
            if (user != null)
            {
                grade = new BigDecimal(user.getGrade());
                //获取会员折扣
                gradeRate = publiceService.getUserGradeRate(vo.getStoreId(), user);
            }
            //享受折扣
            if (gradeRate.compareTo(BigDecimal.ZERO) > 0 && new BigDecimal("1").compareTo(gradeRate) != 0)
            {
                isGrade = true;
            }
            else
            {
                //获取最低折扣
                gradeRate = userGradeModelMapper.getGradeLow(vo.getStoreId()).divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
            }
            //获取该店铺是否显示下架商品
            ProductConfigModel productConfigModel = new ProductConfigModel();
            productConfigModel.setStore_id(vo.getStoreId());
            productConfigModel = productConfigModelMapper.selectOne(productConfigModel);
            if (productConfigModel != null)
            {
                if (productConfigModel.getIs_open() != null && productConfigModel.getIs_open() == 1)
                {
                    goodsStatus.add(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                }
                if (productConfigModel.getIs_display_sell_put() == 0)
                {
                    parmaMap.put("stockNum", "stockNum");
                }
            }
            //获取当前店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(shopId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                //获取商城配置信息
                ConfigModel configModel = new ConfigModel();
                configModel.setStore_id(vo.getStoreId());
                configModel = configModelMapper.selectOne(configModel);
                if (configModel != null)
                {
                    //1=推荐,2=全部商品,3=商品分类
                    parmaMap.put("store_id", vo.getStoreId());
                    parmaMap.put("shop_id", mchModel.getId());
                    parmaMap.put("GoodsStatus", goodsStatus);
                    parmaMap.put("mch_status", DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS);
                    parmaMap.put("mch_sort", DataUtils.Sort.DESC.toString());
                    parmaMap.put("pageNo", vo.getPageNo());
                    parmaMap.put("pageSize", vo.getPageSize());
                    parmaMap.put("lang_code", lang_code);
                    if (type == 1)
                    {
                        //获取推荐商品
                        parmaMap.put("volume_sort", DataUtils.Sort.DESC.toString());
                        parmaMap.put("s_type", proLabelModelMapper.getProLabelTop(vo.getStoreId()));
                        parmaMap.put("sTypeNotNull", "sTypeNotNull");
                    }
                    else if (type == 2)
                    {
                        //获取全部商品
                        parmaMap.put("addDate_sort", DataUtils.Sort.DESC.toString());
                    }
                    goodsInfoList = productListModelMapper.getProductListDynamic(parmaMap);
                    //折扣处理
                    for (Map<String, Object> map : goodsInfoList)
                    {
                        //产品值属性 1：新品,2：热销，3：推荐
                        String stype = map.get("s_type") + "";
                        //获取商品标签
                        map.put("s_type_list", publicGoodsService.getGoodsLabelList(vo.getStoreId(), DataUtils.convertToList(StringUtils.trim(stype, SplitUtils.DH).split(SplitUtils.DH))));
                        String imgUrl = map.get("imgurl").toString();
                        imgUrl = publiceService.getImgPath(imgUrl, vo.getStoreId());
                        String isOpen = mchModel.getIs_open();
                        //需要核销的商品不可加入购物车
                        Integer writeOffSettings = MapUtils.getInteger(map, "write_off_settings");
                        if (Objects.nonNull(writeOffSettings) && writeOffSettings == 1)
                        {
                            map.put("is_appointment",2);
                        }
                        //未营业
                        if ("0".equals(isOpen) || "2".equals(isOpen))
                        {
                            map.put("is_open", mchModel.getIs_open());
                        }
                        else if ("1".equals(mchModel.getIs_open()))
                        {
                            String businessHoursValue = mchModel.getBusiness_hours();
                            if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                            {
                                map.put("is_open", "2");
                            }
                            else
                            {
                                //营业时间判断是否营业
                                String[] businessHours = businessHoursValue.split(SplitUtils.BL);
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
                                        map.put("is_open", "1");
                                    }
                                    else
                                    {
                                        //未营业
                                        map.put("is_open", "2");
                                    }
                                }
                                else
                                {
                                    //开始时间小于结束时间（当天）则当前时间 >= 开始时间 && 当前时间 <= 结束时间 -> 营业
                                    if (!DateUtil.dateCompare(startTime, currentDate)
                                            && !DateUtil.dateCompare(currentDate, endTime))
                                    {
                                        map.put("is_open", "1");
                                    }
                                    else
                                    {
                                        map.put("is_open", "2");
                                    }
                                }
                            }
                        }
                        else
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
                        }

                        //商品价格处理
                        BigDecimal price     = new BigDecimal(map.get("price").toString());
                        BigDecimal vipYprice = new BigDecimal(price.toString());
                        //折扣 原来价格 * 折扣 / 10 = 优惠价
                        vipYprice = vipYprice.multiply(gradeRate);

                        //预售商品信息
                        Integer           pid               = MapUtils.getInteger(map, "id");
                        PreSellGoodsModel preSellGoodsModel = new PreSellGoodsModel();
                        preSellGoodsModel.setProduct_id(pid);
                        preSellGoodsModel = preSellGoodsMapper.selectOne(preSellGoodsModel);
                        if (!Objects.isNull(preSellGoodsModel))
                        {
                            map.put("isPreSell", 1);
                            if (!Objects.isNull(preSellGoodsModel.getDeposit()))
                            {
                                map.put("deposit", preSellGoodsModel.getDeposit());
                            }
                        }
                        map.put("isGrade", isGrade);
                        map.put("vip_price", vipYprice);
                        map.put("vip_yprice", price);
                        map.put("imgurl", imgUrl);
                        map.put("grade", 0);
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHWPZXX, "商户未配置信息", "storeHomepageLoad");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "storeHomepageLoad");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载店铺主页-加载商品信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "storeHomepageLoad");
        }
        return goodsInfoList;
    }

    @Override
    public Map<String, Object> StoreAdminList(MainVo vo, Integer mch_store_id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(mch_store_id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            HashMap<String, Object> parmaMap = new HashMap<>();
            parmaMap.put("storeId", vo.getStoreId());
            parmaMap.put("mchId", user.getMchId());
            parmaMap.put("mch_store_id", mch_store_id);
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            int                       total = mchAdminModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = mchAdminModelMapper.selectDynamic(parmaMap);
                list.forEach(map ->
                {
                    try
                    {
                        map.put("password", CryptoUtil.strDecode(MapUtils.getString(map, "password")));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                    if (!StringUtils.isEmpty(MapUtils.getString(map, "last_time")))
                    {
                        map.put("last_time", DateUtil.dateFormate(MapUtils.getString(map, "last_time"), GloabConst.TimePattern.YMDHMS));
                    }
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
            e.printStackTrace();
            logger.error("获取门店管理员列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAdmin(MainVo vo, Integer mch_store_id, String account_number, String password, Integer id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(mch_store_id) || StringUtils.isEmpty(account_number) || StringUtils.isEmpty(password))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRZHMM, "参数错误");
            }
            int lab;
            password = CryptoUtil.strEncode(password);
            MchAdminModel mchAdminModel = new MchAdminModel();
            if (id != null)
            {
                mchAdminModel = mchAdminModelMapper.selectByPrimaryKey(id);
                if (mchAdminModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYBCZ, "管理员不存在");
                }
                mchAdminModel.setPassword(password);
                lab = mchAdminModelMapper.updateByPrimaryKeySelective(mchAdminModel);
            }
            else
            {
                mchAdminModel.setStore_id(vo.getStoreId());
                mchAdminModel.setMch_id(user.getMchId());
                mchAdminModel.setMch_store_id(mch_store_id);
                mchAdminModel.setAccount_number(account_number);
                mchAdminModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
                if (mchAdminModelMapper.selectCount(mchAdminModel) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYZHCF, "管理员账号重复");
                }
                mchAdminModel.setPassword(password);
                mchAdminModel.setAdd_date(new Date());
                lab = mchAdminModelMapper.insertSelective(mchAdminModel);
            }
            if (lab <= 0)
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
            e.printStackTrace();
            logger.error("添加管理员 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAdmin(MainVo vo, Integer mch_store_id, Integer id) throws LaiKeAPIException
    {
        try
        {
//            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            if (StringUtils.isEmpty(mch_store_id) || StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            int           lab;
            MchAdminModel mchAdminModel = new MchAdminModel();
            mchAdminModel.setId(id);
            mchAdminModel.setRecycle(DictionaryConst.WhetherMaven.WHETHER_OK);
            lab = mchAdminModelMapper.updateByPrimaryKeySelective(mchAdminModel);
            if (lab <= 0)
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
            e.printStackTrace();
            logger.error("添加管理员 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
    }

    @Override
    public Map<String, Object> getAppointmenTime(MainVo vo, Integer mchStoreId, Integer w_id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("storeId", vo.getStoreId());
                parmaMap.put("mchId", user.getMchId());
                parmaMap.put("mch_store_id", mchStoreId);
                parmaMap.put("id", w_id);
                parmaMap.put("pageNo", vo.getPageNo());
                parmaMap.put("pageSize", vo.getPageSize());
                parmaMap.put("recycle", 0);
                Map<String, List<Map<String, Object>>> HashMap  = new HashMap<>();
                List<Map<String, Object>>              list     = mchStoreWriteModelMapper.selectDynamic(parmaMap);
                List<Map<String, Object>>              dataInfo = new ArrayList<>();
                /*for (Map<String, Object> map : list) {
                    //可预约核销次数
                    Integer write_off_num = MapUtils.getInteger(map, "write_off_num");
                    //已预约核销次数
                    Integer off_num = MapUtils.getInteger(map, "off_num");
                    //可预约
                    map.put("is_off", 1);
                    if (write_off_num > 0 && off_num >= write_off_num){
                        //不可预约
                        map.put("is_off", 2);
                    }
                    //时间格式修改
                    //日期：2024-07-10至2024-07-20
                    //时间：9:00~10:00
                    //开始时间
                    String start_time = MapUtils.getString(map, "start_time");
                    //结束时间
                    String end_time = MapUtils.getString(map, "end_time");
                    map.put("start_time", DateUtil.dateFormate(start_time, GloabConst.TimePattern.YMD));
                    map.put("end_time", DateUtil.dateFormate(end_time, GloabConst.TimePattern.YMD));
                    String start_time_home = DateUtil.dateFormate(start_time, GloabConst.TimePattern.HM);
                    String end_time_home = DateUtil.dateFormate(end_time, GloabConst.TimePattern.HM);
                    map.put("start_time_home", start_time_home);
                    map.put("end_time_home", end_time_home);
                    String time = start_time_home + SplitUtils.BL + end_time_home;
                    map.put("time", time);

                    List<String> intervalDate = DateUtil.getIntervalDate(start_time, end_time);
                    List<Map<String, Object>> list1;
                    for (String s : intervalDate) {
                        //s = DateUtil.dateFormate(s, GloabConst.TimePattern.MD2);
                        if (HashMap.containsKey(s)){
                            list1 = HashMap.get(s);
                            list1.add(map);
                        }else {
                            list1 = new ArrayList<>();
                            list1.add(map);
                            HashMap.put(s, list1);
                        }
                    }
                }*/
                for (Map<String, Object> map : list)
                {
                    Map<String, Object> map1       = new HashMap<>();
                    String              start_time = MapUtils.getString(map, "start_time");
                    //结束时间
                    String end_time = MapUtils.getString(map, "end_time");

                    String start_time_home = DateUtil.dateFormate(start_time, GloabConst.TimePattern.HM);
                    String end_time_home   = DateUtil.dateFormate(end_time, GloabConst.TimePattern.HM);

                    map1.put("startTime", start_time_home);
                    map1.put("endTime", end_time_home);
                    start_time = DateUtil.dateFormate(start_time, GloabConst.TimePattern.YMD);
                    end_time = DateUtil.dateFormate(end_time, GloabConst.TimePattern.YMD);
                    map1.put("startDate", start_time);
                    map1.put("endDate", end_time);
                    //核销上限
                    map1.put("num", MapUtils.getString(map, "write_off_num"));
                    map1.put("id", MapUtils.getIntValue(map, "w_id"));
                    //添加是否可删除
                    //拿门店的id去orderDetail中查找该门店下的所有订单的核销时间，2024-06-26 9:00-10:00，拆成2024-06-26 9:00和2024-06-26 10:00
                    //为0可删除，为1不可删除
                    Integer            notDelete = 0;
                    MchStoreWriteModel old       = mchStoreWriteModelMapper.selectByPrimaryKey(MapUtils.getIntValue(map, "w_id"));
                    if (old == null)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
                    }
                    Date    startTime    = old.getStart_time();
                    Date    endTime      = old.getEnd_time();
                    Integer mch_store_id = old.getMch_store_id();
                    // 格式化时间
                    String start_time_ymd = DateUtil.dateFormate(startTime, GloabConst.TimePattern.YMD);
                    String end_time_ymd   = DateUtil.dateFormate(endTime, GloabConst.TimePattern.YMD);
                    String start_time_hm  = DateUtil.dateFormate(startTime, GloabConst.TimePattern.HM);
                    String end_time_hm    = DateUtil.dateFormate(endTime, GloabConst.TimePattern.HM);

                    // 构造时间范围
                    String       time_range   = start_time_hm + SplitUtils.HG + end_time_hm;
                    List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                    List<String> newTime      = new ArrayList<>();
                    for (String s : intervalDate)
                    {
                        s = s + " " + time_range;
                        newTime.add(s);
                    }
                    OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                    orderDetailsModel.setMch_store_write_id(mch_store_id);
                    List<OrderDetailsModel> detailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                    //查询订单详单表中该门店id下的所有的预约时间看是否有时间在删除的时间区间内
                    for (String s : newTime)
                    {
                        for (OrderDetailsModel detailsModel : detailsModelList)
                        {
                            if (detailsModel.getWrite_time().equals(s))
                            {
                                notDelete = 1;
                            }
                        }
                    }
                    map1.put("notDelete", notDelete);
                    dataInfo.add(map1);
                }
                resultMap.put("total", dataInfo.size());
                resultMap.put("list", dataInfo);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载核销管理页面 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean editAppointmentTime(EditAppointVo vo, Integer w_id) throws LaiKeAPIException
    {

        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (StringUtils.isEmpty(vo.getWrite_time()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (StringUtils.isEmpty(vo.getWrite_off_num()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (StringUtils.isEmpty(vo.getWrite_date()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (w_id == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                MchStoreWriteModel mchStoreWriteModelOld = mchStoreWriteModelMapper.selectByPrimaryKey(w_id);
                if (mchStoreWriteModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "原数据异常", "editStorePage");
                }

                String   date       = vo.getWrite_date();
                String   time       = vo.getWrite_time();
                String[] split_date = date.split(SplitUtils.DH);
                String[] split_time = time.split(SplitUtils.DH);
                String   start_time = split_date[0] + " " + split_time[0];
                String   end_time   = split_date[1] + " " + split_time[1];
                Date     start      = DateUtil.dateFormateToDate(start_time, GloabConst.TimePattern.YMDHMS);
                Date     end        = DateUtil.dateFormateToDate(end_time, GloabConst.TimePattern.YMDHMS);

                MchStoreWriteModel mchStoreWriteModel = new MchStoreWriteModel();
                mchStoreWriteModel.setId(w_id);
                mchStoreWriteModel.setStart_time(start);
                mchStoreWriteModel.setEnd_time(end);
                mchStoreWriteModel.setWrite_off_num(vo.getWrite_off_num());
                int i = mchStoreWriteModelMapper.updateByPrimaryKeySelective(mchStoreWriteModel);
                if (i < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "操作失败", "editStorePage");
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
            logger.error("加载编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAppointmentTime(AddAppointVo vo, Integer store_id)
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (StringUtils.isEmpty(vo.getWrite_time()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (StringUtils.isEmpty(vo.getWrite_date()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (StringUtils.isEmpty(vo.getMch_store_id()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                else if (StringUtils.isEmpty(vo.getMch_id()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //220 防止连续点击
                if (redisUtil.incr(GloabConst.RedisHeaderKey.WRITE_TIME_KEY + store_id, 1) > 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CFCZ, "重复操作", "addAppointmentTime");
                }
                redisUtil.expire(GloabConst.RedisHeaderKey.WRITE_TIME_KEY + store_id, 300);
                String   date       = vo.getWrite_date();
                String   time       = vo.getWrite_time();
                String[] split_date = date.split(SplitUtils.DH);//2024-07-03,2024-07-04
                String[] split_time = time.split(SplitUtils.DH);//14:00,15:00
                String   start_time = split_date[0] + " " + split_time[0];
                String   end_time   = split_date[1] + " " + split_time[1];
                Date     start      = DateUtil.dateFormateToDate(start_time, GloabConst.TimePattern.YMDHM);
                Date     end        = DateUtil.dateFormateToDate(end_time, GloabConst.TimePattern.YMDHM);

                List<String> newIntervalDate = DateUtil.getIntervalDate(split_date[0], split_date[1]);//旧的所有日期

                //验证时间参数是否正确
                if (DateUtil.dateCompare(split_time[0], split_time[1], GloabConst.TimePattern.HM) || DateUtil.dateCompare(split_date[0], split_date[1], GloabConst.TimePattern.YMD))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCSBZQ, "时间参数错误");
                }
                //判断时间是否重叠
                MchStoreWriteModel oldMchStoreWriteModel = new MchStoreWriteModel();
                oldMchStoreWriteModel.setRecycle(0);
                oldMchStoreWriteModel.setMch_store_id(vo.getMch_store_id());
                oldMchStoreWriteModel.setMch_id(vo.getMch_id());
                oldMchStoreWriteModel.setStore_id(vo.getStore_id());
                List<String> oldIntervalDate = new ArrayList<>();

                String off_num = "";
                //找出当前该门店下还在使用的核销时间，将时间与新时间进行对比，避免有重复的时间段
                List<MchStoreWriteModel> select = mchStoreWriteModelMapper.select(oldMchStoreWriteModel);
                if (select != null && select.size() > 0)
                {
                    for (MchStoreWriteModel mchStoreWriteModel : select)
                    {
                        Date   startTime = mchStoreWriteModel.getStart_time();
                        Date   endTime   = mchStoreWriteModel.getEnd_time();
                        String startYear = DateUtil.dateFormate(startTime, GloabConst.TimePattern.YMD);
                        String startHour = DateUtil.dateFormate(startTime, GloabConst.TimePattern.HM);
                        String endYear   = DateUtil.dateFormate(endTime, GloabConst.TimePattern.YMD);
                        String endHour   = DateUtil.dateFormate(endTime, GloabConst.TimePattern.HM);
                        oldIntervalDate = DateUtil.getIntervalDate(startYear, endYear);//旧的所有日期
                        for (String s : oldIntervalDate)
                        {
                            String s_time = s + " " + startHour;
                            String e_time = s + " " + endHour;
                            String s1     = DateUtil.dateFormate(s_time, GloabConst.TimePattern.YMDHM);//旧的开始时间
                            String s2     = DateUtil.dateFormate(e_time, GloabConst.TimePattern.YMDHM);//旧的结束时间
                            for (String newTime : newIntervalDate)
                            {
                                //newTime:2024-06-24         01:00
                                //新添加的时间的开始时间和结束时间
                                //1.新添加的开始时间小于旧的开始时间且新添加的结束时间大于旧的开始时间报错
                                //2.新添加的开始时间或者结束时间夹在旧时间的中间报错
                                //3.新添加的结束时间大于旧的开始时间且新添加的结束时间小于于旧的开始时间报错
                                //4.新添加时间完全包围住旧时间
                                String c_start_time = newTime + " " + split_time[0];
                                String s3           = DateUtil.dateFormate(c_start_time, GloabConst.TimePattern.YMDHM);
                                String c_end_time   = newTime + " " + split_time[1];//
                                String s4           = DateUtil.dateFormate(c_end_time, GloabConst.TimePattern.YMDHM);
                                if (timeRepeat(s1, s2, s3) || timeRepeat(s1, s2, s4))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_TJDSJCD, "添加的核销时间重叠");
                                }
                                //单独解决第四种情况 ,第一个参数大于第二个参数时返回true
                                if (DateUtil.dateCompare(s1, s3, GloabConst.TimePattern.YMDHM) && DateUtil.dateCompare(s4, s2, GloabConst.TimePattern.YMDHM))
                                {
                                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_TJDSJCD, "添加的核销时间重叠");
                                }

                            }
                        }
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < newIntervalDate.size(); i++)
                {
                    sb.append("0");
                    if (i < newIntervalDate.size() - 1)
                    { // 不是最后一个元素时添加逗号
                        sb.append(",");
                    }
                }
                off_num = sb.toString();


                MchStoreWriteModel mchStoreWriteModel = new MchStoreWriteModel();
                mchStoreWriteModel.setStore_id(vo.getStore_id());
                mchStoreWriteModel.setMch_id(vo.getMch_id());
                mchStoreWriteModel.setMch_store_id(vo.getMch_store_id());
                mchStoreWriteModel.setStart_time(start);
                mchStoreWriteModel.setEnd_time(end);
                mchStoreWriteModel.setWrite_off_num(vo.getWrite_off_num());
                mchStoreWriteModel.setAdd_time(new Date());
                //可预约核销次数的设置
                mchStoreWriteModel.setOff_num(off_num);
                int i = mchStoreWriteModelMapper.insertSelective(mchStoreWriteModel);
                if (i < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "操作失败", "editStorePage");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.LOGIN_INVALID, "未登录", "editStorePage");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("加载编辑我的门店页面数据 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        finally
        {
            //防止连续点击
            redisUtil.decr(GloabConst.RedisHeaderKey.WRITE_TIME_KEY + store_id, 1);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteAppointmentTime(EditAppointVo vo)
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            if (user != null)
            {
                if (StringUtils.isEmpty(vo.getId()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }
                //拿门店的id去orderDetail中查找该门店下的所有订单的核销时间，2024-06-26 9:00-10:00，拆成2024-06-26 9:00和2024-06-26 10:00
                //为0可删除，为1不可删除
                Integer            notDelete = 1;
                MchStoreWriteModel old       = mchStoreWriteModelMapper.selectByPrimaryKey(vo.getId());
                if (old == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JLBCZ, "记录不存在");
                }
                Date    start_time = old.getStart_time();
                Date    end_time   = old.getEnd_time();
                Integer mchStoreId = old.getMch_store_id();
                // 格式化时间
                String start_time_ymd = DateUtil.dateFormate(start_time, GloabConst.TimePattern.YMD);
                String end_time_ymd   = DateUtil.dateFormate(end_time, GloabConst.TimePattern.YMD);
                String start_time_hm  = DateUtil.dateFormate(start_time, GloabConst.TimePattern.HMS);
                String end_time_hm    = DateUtil.dateFormate(end_time, GloabConst.TimePattern.HMS);

                // 构造时间范围
                String       time_range   = start_time_hm + SplitUtils.HG + end_time_hm;
                List<String> intervalDate = DateUtil.getIntervalDate(start_time_ymd, end_time_ymd);
                List<String> newTime      = new ArrayList<>();
                for (String s : intervalDate)
                {
                    s = s + " " + time_range;
                    newTime.add(s);
                }
                OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
                orderDetailsModel.setMch_store_write_id(mchStoreId);
                List<OrderDetailsModel> detailsModelList = orderDetailsModelMapper.select(orderDetailsModel);
                //查询订单详单表中该门店id下的所有的预约时间看是否有时间在删除的时间区间内
                for (String s : newTime)
                {
                    for (OrderDetailsModel detailsModel : detailsModelList)
                    {
                        if (!detailsModel.getWrite_time().equals(s))
                        {
                            notDelete = 0;
                        }
                    }
                }
                result.put("notDelete", notDelete);
                //逻辑删除
                MchStoreWriteModel mchStoreWriteModel = new MchStoreWriteModel();
                mchStoreWriteModel.setId(vo.getId());
                mchStoreWriteModel.setRecycle(1);
                int i = mchStoreWriteModelMapper.updateByPrimaryKeySelective(mchStoreWriteModel);
                if (i < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "操作失败", "editStorePage");
                }

            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WDL, "未登录", "editStorePage");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除核销时间 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "editStorePage");
        }
        return result;
    }

    /**
     * 编辑我的门店预约时间段
     *
     * @param mchId
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public List<Map<String, Object>> getWriteStore(Integer mchId, Integer storeId)
    {
        if (mchId == null || storeId == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object>       params     = new HashMap<String, Object>();
        params.put("store_id", storeId);
        params.put("mch_id", mchId);

        List<Map<String, Object>> maps = mchStoreModelMapper.selectDynamic(params);
        for (Map<String, Object> map : maps)
        {
            HashMap<String, Object> result = new HashMap<>();
            result.put("id", map.get("id"));
            result.put("name", map.get("name"));
            resultList.add(result);
        }
        return resultList;
    }


    /**
     * 虚拟商品根据orderId来判断是否有预约
     *
     * @param dId
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public Integer getWrite(Integer dId)
    {
        if (dId == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
        }
        OrderDetailsModel orderDetailsModel = orderDetailsModelMapper.selectByPrimaryKey(dId);
        if (orderDetailsModel.getMch_store_write_id() != null && StringUtils.isNotEmpty(orderDetailsModel.getWrite_time()))
        {
            //有预约
            return 1;
        }
        else
        {
            return 2;
        }


    }

    /**
     * 虚拟商品根据商品id来查询商品可用门店信息
     *
     * @param sNo
     * @return
     * @throws LaiKeAPIException
     */
    @Override
    public List<Map<String, Object>> get_write_shop(String sNo)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (sNo == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
        }
        OrderDetailsModel orderDetailsModel = new OrderDetailsModel();
        orderDetailsModel.setR_sNo(sNo);
        List<OrderDetailsModel> orderDetailsModels = orderDetailsModelMapper.select(orderDetailsModel);
        List<Integer>           pIds               = new ArrayList<>();
        for (OrderDetailsModel detailsModel : orderDetailsModels)
        {
            pIds.add(detailsModel.getP_id());
        }
        List<ProductListModel> productListModels = productListModelMapper.selectByIds(pIds);
        if (productListModels == null || productListModels.size() < 1)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不能为空");
        }
        for (ProductListModel productListModel : productListModels)
        {
            if (productListModel.getWrite_off_mch_ids().equals("0"))
            {
                MchStoreModel mchStoreModel = new MchStoreModel();
                mchStoreModel.setMch_id(productListModel.getMch_id());
                mchStoreModel.setStore_id(productListModel.getStore_id());
                List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.select(mchStoreModel);
                for (MchStoreModel storeModel : mchStoreModelList)
                {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", storeModel.getId());
                    map.put("name", storeModel.getName());
                    result.add(map);
                }
                break;
            }
            else
            {
                String[]            split             = productListModel.getWrite_off_mch_ids().split(SplitUtils.DH);
                List<String>        ids               = Arrays.asList(split);
                List<MchStoreModel> mchStoreModelList = mchStoreModelMapper.selectByIds(ids);
                for (MchStoreModel mchStoreModel : mchStoreModelList)
                {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", mchStoreModel.getId());
                    map.put("name", mchStoreModel.getName());
                    result.add(map);
                }
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void selfSend(MainVo vo, int shopId, String sNo, String phone, String courier_name) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //验证店铺
            publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), shopId);
            if (com.google.common.base.Strings.isNullOrEmpty(courier_name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_PSYXMBNWK, "配送员姓名不能为空");
            }
            else if (com.google.common.base.Strings.isNullOrEmpty(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHBNWK, "手机不能为空");
            }
           /* else if (!MobileUtils.isMobile(phone))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机格式不正确");
            }*/
            //装载参数
            FrontDeliveryVo sendVo = new FrontDeliveryVo();
            sendVo.setStoreId(vo.getStoreId());
            sendVo.setStoreType(vo.getStoreType());
            sendVo.setUserId(user.getUser_id());
            sendVo.setWxid(user.getWx_id());
            sendVo.setsNo(sNo);
            sendVo.setPhone(phone);
            sendVo.setCourier_name(courier_name);
            //发货
            publicOrderService.selfSend(sendVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("发货 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "send");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> quickRefund(RefundVo vo)
    {
        return publicRefundService.adminQuickRefund(vo);
    }

    @Override
    public Map<String, Object> getMchDiyHomePage(MainVo vo, int shopId, String longitude, String latitude) {
        Map<String, Object> result = new HashMap<>();
        try {
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(shopId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (Objects.nonNull(mchModel)) {
                DiyModel diyModel = new DiyModel();
                diyModel.setMch_id(shopId);
                diyModel.setIs_del(DictionaryConst.ProductRecycle.NOT_STATUS);
                diyModel.setStatus(DictionaryConst.WhetherMaven.WHETHER_OK);
                diyModel = diyModelMapper.selectOne(diyModel);
                if (Objects.nonNull(diyModel)) {
                    result.put("pageJson", diyModel.getValue());
                }
            }
        } catch (LaiKeAPIException l) {
            throw l;
        } catch (Exception e) {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchDiyHomePage");
        }
        return result;
    }

    /**
     * 判断添加的时间是否重复 ,s3是否在s1和s2的中间
     *
     * @return
     */
    public boolean timeRepeat(String oldStartTime, String oldEndTime, String newTime)
    {
        //添加2024-06-24 01:00~2024-06-26 00:00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime     start1    = LocalDateTime.parse(oldStartTime, formatter);
        LocalDateTime     end1      = LocalDateTime.parse(oldEndTime, formatter);
        LocalDateTime     addTime   = LocalDateTime.parse(newTime, formatter);

        // 只要新添加的时间夹在旧时间中间就不行，新时间为添加时间的所有日期的开始时刻和结束时刻分别循环来判断
        // 则认为它们重合
        return ((addTime.isBefore(end1) && addTime.isAfter(start1)) || (start1.equals(addTime) && end1.equals(addTime)));


    }

}
