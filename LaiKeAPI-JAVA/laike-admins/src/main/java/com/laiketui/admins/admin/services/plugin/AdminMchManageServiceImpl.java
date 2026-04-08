package com.laiketui.admins.admin.services.plugin;

import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.plugin.AdminMchManageService;
import com.laiketui.common.api.PublicGoodsService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.HttpApiUtils;
import com.laiketui.common.utils.tool.ImgUploadUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
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
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.home.SystemMessageModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductImgModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.WithdrawModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.main.AddStoreConfigVo;
import com.laiketui.domain.vo.mch.AddIMchVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.user.WithdrawalVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 店铺管理实现
 *
 * @author Trick
 * @date 2021/1/26 11:31
 */
@Service
public class AdminMchManageServiceImpl implements AdminMchManageService
{
    private final Logger logger = LoggerFactory.getLogger(AdminMchManageServiceImpl.class);

    private final String NEARBY_KEY = "user_nearby";

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchClassModelMapper mchClassModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private SystemMessageModelMapper systemMessageModelMapper;

    @Override
    public Map<String, Object> getMchInfo(MainVo vo, Integer id, Integer isOpen, String name, Integer promiseStatus, Integer cid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (id == null)
            {
                //只显示已经审核通过的店铺
                parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);
            }
            if (promiseStatus != null)
            {
                //1=已缴
                parmaMap.put("promiseStatus", promiseStatus);
            }

            if (id != null)
            {
                parmaMap.put("id", id);
            }
            if (isOpen != null)
            {
                parmaMap.put("is_open", isOpen);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("mchName1", name);
            }
            if (!Objects.isNull(cid))
            {
                parmaMap.put("cid", cid);
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            /*parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());*/
            int                       total   = mchModelMapper.countMchUserInfo(parmaMap);
            List<Map<String, Object>> listMap = new ArrayList<>();

            List<Map<String, Object>> listProMap = new ArrayList<>();
            if (total > 0)
            {
                listMap = mchModelMapper.getMchUserInfo(parmaMap);
                for (Map<String, Object> map : listMap)
                {
                    maskIdNumber(map, true);
                    maskPhone(map, true);

                    ConfigModel configModel = new ConfigModel();
                    configModel.setStore_id(Integer.valueOf(map.get("store_id").toString()));
                    configModel = configModelMapper.selectOne(configModel);

                    if (configModel == null || null == configModel.getIsAccounts())
                    {
                        map.put("isAccounts", 0);
                    }
                    else
                    {
                        map.put("isAccounts", configModel.getIsAccounts());
                    }


                    String logoUrl = MapUtils.getString(map, "logo");
                    map.put("logo", publiceService.getImgPath(logoUrl, vo.getStoreId()));
                    //店铺营业执照和身份证正反面
                    List<String> businessLicense = new ArrayList<>();
                    if (StringUtils.isNotEmpty(MapUtils.getString(map, "business_license")))
                    {
                        String[] businessLicenses = MapUtils.getString(map, "business_license").split(SplitUtils.DH);
                        for (String imgurl : businessLicenses)
                        {
                            String imgPath = publiceService.getImgPath(imgurl, vo.getStoreId());
                            businessLicense.add(imgPath);
                        }
                        map.put("business_license", businessLicense);
                    }
                    map.put("headimgurl", publiceService.getImgPath(MapUtils.getString(map, "head_img"), vo.getStoreId()));
                    map.put("poster_img", publiceService.getImgPath(MapUtils.getString(map, "poster_img"), vo.getStoreId()));
                    String promise = MapUtils.getString(map, "promiseStatus");
                    if (StringUtils.isNotEmpty(promise) && MchPromiseModel.PromiseConstant.STATUS_PAY.toString().equals(promise))
                    {
                        promise = "已缴";
                    }
                    else
                    {
                        promise = "未缴";
                    }
                    map.put("promiseStatus", promise);
                    Integer       classId       = MapUtils.getInteger(map, "cid");
                    MchClassModel mchClassModel = mchClassModelMapper.selectByPrimaryKey(classId);
                    if (!Objects.isNull(mchClassModel))
                    {
                        map.put("className", mchClassModel.getName());
                    }
                    String lastLoginTime = MapUtils.getString(map, "last_login_time");
                    if (StringUtils.isNotEmpty(lastLoginTime))
                    {
                        map.put("last_login_time", DateUtil.dateFormate(lastLoginTime, GloabConst.TimePattern.YMDHMS));
                    }
                    Integer mchId = MapUtils.getInteger(map, "id");
                    //商品数
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setMch_id(mchId);
                    productListModel.setCommodity_type(null);
                    productListModel.setRecycle(String.valueOf(DictionaryConst.ProductRecycle.NOT_STATUS));
                    int count = productListModelMapper.selectCount(productListModel);
                    map.put("goodsNum",count);
                    //订单数
                    Map<String, Object> paramMap = new HashMap<>(16);
                    paramMap.put("mch_id", mchId);
                    paramMap.put("store_id", vo.getStoreId());
                    map.put("ordersNum", orderModelMapper.countbMchOrderIndex(paramMap));
                    String is_open = MapUtils.getString(map, "is_open");
                    if ("0".equals(is_open) || "2".equals(is_open))
                    {
                        map.put("is_open", is_open);
                    }
                    else if ("1".equals(is_open))
                    {
                        String businessHoursValue = MapUtils.getString(map, "business_hours");
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
                    if (isOpen != null && isOpen == 1 && map.get("is_open").equals("1"))
                    {
                        listProMap.add(map);
                    }
                    else if (isOpen != null && isOpen == 2 && map.get("is_open").equals("2"))
                    {
                        listProMap.add(map);
                    }
                }

                if (isOpen == null)
                {
                    listProMap = listMap;
                }
                //拿出来再筛选的数据是分页后的数据，导致total的数据不对，无法正常分页
                /*if(isOpen != null ){
                    total = listProMap.size();
                }*/
                total = listProMap.size();
                listProMap = getPage(listProMap, vo.getPageNum(), vo.getPageSize());
                ;

            }
            resultMap.put("total", total);
            resultMap.put("list", listProMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺列表信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchInfo");
        }
        return resultMap;
    }

    // 分页方法，根据页码和页大小返回对应页的数据
    public static List<Map<String, Object>> getPage(List<Map<String, Object>> list, int pageNumber, int pageSize)
    {
        if (pageNumber == 0)
        {
            pageNumber = 1;
        }
        if (list == null || pageSize <= 0 || pageNumber <= 0)
        {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        int fromIndex = (pageNumber - 1) * pageSize;
        if (fromIndex >= list.size())
        {
            // 如果起始索引超出列表大小，返回空列表
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addMchInfo(AddIMchVo vo) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(vo.getCity_all()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPDZBNWK, "店铺地址不能为空");
            }
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
            //管理平台添加店铺须创建账号
            String userId = "";
            if (!Objects.isNull(vo.getAccount()) && !Objects.isNull(vo.getPassword()))
            {
                User user = new User();
                user.setMobile(vo.getTel());
                user.setMima(vo.getPassword());
                user.setZhanghao(vo.getAccount());
                //校验数据格式
                user = DataCheckTool.checkUserDataFormate(user);
                //检查账户/手机号是否已存在
                User userTemp = new User();
                userTemp.setZhanghao(user.getZhanghao());
                int accountIsExist = userBaseMapper.validataUserPhoneOrNoIsRegister(vo.getStoreId(), user.getZhanghao());
                int phoneIsExist   = userBaseMapper.validataUserPhoneOrNoIsRegister(vo.getStoreId(), user.getMobile());
                int emailCount          = userBaseMapper.emailCount(vo.getAccount(), vo.getStoreId());
                if (accountIsExist == 0 && phoneIsExist == 0 && emailCount == 0)
                {
                    //注册 默认使用pc端
                    vo.setStoreType(GloabConst.StoreType.STORE_TYPE_PC_MALL);
                    publicUserService.register(vo, null, user);
                    userId = user.getUser_id();
                }
                if (accountIsExist > 0)
                {
                    logger.debug("账号{}已被注册!", user.getZhanghao());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZHYBZC, "该账号已被注册", "addMchInfo");
                }
                if (phoneIsExist > 0)
                {
                    logger.debug("手机号{}已被注册!", user.getMobile());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSJHMYJZC, "该手机号已被注销", "addMchInfo");
                }
                if (emailCount > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERR0R_CODE_YXYBZC,"邮箱已被注册","addMchInfo");
                }
            }
            else
            {
                if (vo.getUserId() == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHWK, "用户Id不能为空");
                }
                User user = userBaseMapper.selectByUserId(vo.getStoreId(), vo.getUserId());
                if (user == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
                }
                userId = user.getUser_id();
            }
            MchModel mch = new MchModel();
            //商城id
            mch.setStore_id(vo.getStoreId());
            //用户id
            mch.setUser_id(userId);
            //申请，审核时间
            mch.setAdd_time(new Date());
            //审核通过
            mch.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            //营业状态
            mch.setIs_open(MchModel.IS_OPEN_PROOFING.toString());
            mch.setCid(vo.getCid());
            mch.setRoomid(0);
            mch.setTel(vo.getTel());
            mch.setName(vo.getName());
            mch.setAddress(vo.getAddress());
            mch.setUser_id(userId);
            mch.setCpc(vo.getCpc());
            mch.setRealname(vo.getRealname());
            mch.setID_number(vo.getID_number());
            mch.setShop_range(vo.getShop_range());
            mch.setShop_nature(vo.getShop_nature() + "");
            mch.setShop_information(vo.getShop_information());
            if (StringUtils.isNotEmpty(vo.getCity_all()))
            {
                List<String> citys   = Arrays.asList(vo.getCity_all().split("-"));
                int          dataMax = 3;
                if (citys.size() == dataMax)
                {
                    mch.setSheng(citys.get(0));
                    mch.setShi(citys.get(1));
                    mch.setXian(citys.get(2));
                }
            }

            //校验店铺数据
            mch = DataCheckTool.checkMchDataFormate(mch);
            //验证名称是否存在
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(userId);
            if (mchModelMapper.selectOne(mchModel) != null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DUPLICATE, "用户已经创建店铺", "TheUserCreatedAStore");
            }
            mchModel.setUser_id(null);
            mchModel.setName(vo.getName());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在", "verifyStoreName");
            }
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
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误", "applyShop");
            }
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
            int insert = mchModelMapper.insertSelective(mch);
//            redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(Double.parseDouble(mch.getLongitude()), Double.parseDouble(mch.getLatitude())), String.valueOf(mch.getId()));
            //添加跳转路径
            publicAdminService.addJumpPath(vo, mch.getId() + "", mch.getName(), JumpPathModel.JumpType.JUMP_TYPE0_MCH, JumpPathModel.JumpType.JUMP_TYPE_APP,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_APP, new String[]{mch.getId() + ""}, mch.getId(), null);
            publicAdminService.addJumpPath(vo, mch.getId() + "", mch.getName(), JumpPathModel.JumpType.JUMP_TYPE0_MCH, JumpPathModel.JumpType.JUMP_TYPE_PC,
                    GloabConst.LaikeTuiUrl.JumpPath.GOODS_PC, new String[]{mch.getId() + ""}, mch.getId(), null);

            publiceService.addAdminRecord(vo.getStoreId(), "添加了店铺ID：" + mch.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            return insert > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("新增商户信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyMchInfo");
        }
    }

    @Override
    public Map<String, Object> addMchGetUserInfo(MainVo vo, String queryUser)
    {
        try
        {
            HashMap<String, Object> resultMap = new HashMap<>(16);
            HashMap<String, Object> map       = new HashMap<>();
            map.put("storeId", vo.getStoreId());
            map.put("pageStart", vo.getPageNo());
            map.put("pageEnd", vo.getPageSize());
            map.put("queryUser", queryUser);
            map.put("Register_data_sort", DataUtils.Sort.DESC.toString());
            int total = userBaseMapper.NotCreatedMchUser(map);
            resultMap.put("total", total);
            resultMap.put("list", userBaseMapper.getNotCreatedMchUser(map));
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("查询未创建店铺用户信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyMchInfo");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean modifyMchInfo(AddMchVo vo) throws LaiKeAPIException
    {
        try
        {
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(vo.getId());
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHBCZ, "商户不存在");
            }
            if (StringUtils.isEmpty(vo.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXDHBNWK, "联系电话不能为空");
            }
            if (StringUtils.isEmpty(vo.getConfines()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JYFWBNWK, "经营范围不能为空");
            }
            if (StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXDZBNWK, "详细地址不能为空");
            }
            if (StringUtils.isEmpty(vo.getLicense()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCSFZZFMYYZZ, "请上传身份证正反面/营业执照");
            }
            if (Objects.isNull(vo.getCid()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择分类");
            }
            //校验店铺名称
            if (!mchModel.getName().equals(vo.getMchName()))
            {
                MchModel mchName = new MchModel();
                mchName.setStore_id(vo.getStoreId());
                mchName.setName(vo.getMchName());
                mchName.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                int count = mchModelMapper.selectCount(mchName);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在");
                }
            }

            MchModel mchModelUpdate = new MchModel();
            mchModelUpdate.setId(mchModel.getId());
            mchModelUpdate.setTel(vo.getTel());
            mchModelUpdate.setCpc(vo.getCpc());
            mchModelUpdate.setShi(vo.getShi());
            mchModelUpdate.setXian(vo.getXian());
            mchModelUpdate.setSheng(vo.getShen());
            mchModelUpdate.setAddress(vo.getAddress());
            mchModelUpdate.setShop_range(vo.getConfines());
            mchModelUpdate.setShop_information(vo.getMchInfo());
            mchModelUpdate.setShop_nature(vo.getNature());
            mchModelUpdate.setCid(vo.getCid());
            if (vo.getRoomid() != null && vo.getRoomid() > 0)
            {
                mchModelUpdate.setRoomid(vo.getRoomid());
                mchModelUpdate.setOld_roomid(mchModel.getRoomid());
            }
            mchModelUpdate.setIs_open(String.valueOf(vo.getIsOpen()));
            mchModelUpdate.setName(vo.getMchName());
            mchModelUpdate.setRealname(vo.getRealName());
            mchModelUpdate.setID_number(vo.getIdNumber());
            mchModelUpdate.setPoster_img(ImgUploadUtils.getUrlImgByName(vo.getPosterImg(), true));
            mchModelUpdate.setLogo(ImgUploadUtils.getUrlImgByName(vo.getLogo(), true));
            mchModelUpdate.setHead_img(ImgUploadUtils.getUrlImgByName(vo.getHeadImg(), true));
            StringBuilder license = new StringBuilder();
            String[]      split   = vo.getLicense().split(SplitUtils.DH);
            for (String img : split)
            {
                license.append(ImgUploadUtils.getUrlImgByName(img, true)).append(SplitUtils.DH);
            }
            String trim = StringUtils.trim(license.toString(), SplitUtils.DH);
            mchModelUpdate.setBusiness_license(trim);
            String apiKey  = "";
            String address = vo.getShen() + vo.getShi() + vo.getXian() + vo.getAddress();
            //获取商城配置信息
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);
            if (configModel != null)
            {
                apiKey = configModel.getTencent_key();
            }
            //更具地址获取坐标
//            try {
//                Map<String, String> latAndLng = TengxunMapUtil.getlatAndLng(apiKey, address);
//                mchModelUpdate.setLongitude(latAndLng.get("lng"));
//                mchModelUpdate.setLatitude(latAndLng.get("lat"));
//                //经度
//                double longitude = Double.parseDouble(mchModelUpdate.getLongitude());
//                //纬度
//                double latitude = Double.parseDouble(mchModelUpdate.getLatitude());
//                if (!GeoUtils.isAvailablePointInRedis(longitude, latitude)) {
//                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXWZ, "Invalid position(无效位置)");
//                }
//                //上传店铺新的地址经纬度信息
////                redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(longitude, latitude), String.valueOf(mchModel.getId()));
//            } catch (LaiKeAPIException l) {
//                logger.error("获取地址经纬度错误 ", l);
//            }
            DataCheckTool.checkMchDataFormate(mchModelUpdate);
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModelUpdate);
            publiceService.addAdminRecord(vo.getStoreId(), "修改了店铺ID：" + vo.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("修改商户信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "modifyMchInfo");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delMchInfo(MainVo vo, int shopId) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //记录日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了店铺ID：" + shopId, AdminRecordModel.Type.DEL, vo.getAccessId());
            //注销店铺
            return publicMchService.cancellationShop(vo.getStoreId(), shopId);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("注销 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMchInfo");
        }
    }

    @Override
    public Map<String, Object> getMchExamineInfo(MainVo vo, Integer id, Integer reviewStatus, String name, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("recovery", DictionaryConst.ProductRecycle.NOT_STATUS);
            if (id != null)
            {
                parmaMap.put("id", id);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("mchName1", name);
            }
            if (reviewStatus != null)
            {
                parmaMap.put("review_status", reviewStatus);
            }
            else
            {
                parmaMap.put("not_examine", "not_examine");
            }
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total   = mchModelMapper.countMchUserInfo(parmaMap);
            List<Map<String, Object>> listMap = mchModelMapper.getMchUserInfo(parmaMap);
            boolean maskIdNumber = vo.getExportType() == null || vo.getExportType() != 1;
            boolean maskPhone    = vo.getExportType() == null || vo.getExportType() != 1;

            for (Map<String, Object> map : listMap)
            {
                maskIdNumber(map, maskIdNumber);
                maskPhone(map, maskPhone);

                int    examine = MapUtils.getIntValue(map, "review_status");
                String examineName;
                switch (examine)
                {
                    //0.待审核 1.审核通过 2.审核不通过
                    case 0:
                        examineName = "待审核";
                        break;
                    case 1:
                        examineName = "审核通过";
                        break;
                    case 2:
                        examineName = "审核不通过";
                        break;
                    default:
                        examineName = "-";
                }
                map.put("examineName", examineName);
                map.put("logo", publiceService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId()));
                map.put("headimgurl", publiceService.getImgPath(MapUtils.getString(map, "head_img"), vo.getStoreId()));
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                Integer       cid           = MapUtils.getInteger(map, "cid");
                MchClassModel mchClassModel = mchClassModelMapper.selectByPrimaryKey(cid);
                if (!Objects.isNull(mchClassModel))
                {
                    map.put("className", mchClassModel.getName());
                }
            }

            if (vo.getExportType() == 1)
            {
                exportMchExamineData(listMap, response);
                return null;
            }

            resultMap.put("total", total);
            resultMap.put("list", listMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商户审核信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchExamineInfo");
        }
        return resultMap;
    }

    private void maskIdNumber(Map<String, Object> map, boolean mask) throws LaiKeAPIException
    {
        if (!mask || map == null || map.isEmpty())
        {
            return;
        }
        if (map.containsKey("ID_number"))
        {
            map.put("ID_number", StringUtils.desensitizedIdNumber(MapUtils.getString(map, "ID_number")));
        }
        if (map.containsKey("id_number"))
        {
            map.put("id_number", StringUtils.desensitizedIdNumber(MapUtils.getString(map, "id_number")));
        }
    }

    private void maskPhone(Map<String, Object> map, boolean mask) throws LaiKeAPIException
    {
        if (!mask || map == null || map.isEmpty())
        {
            return;
        }
        if (map.containsKey("tel"))
        {
            map.put("tel", StringUtils.desensitizedPhoneNumber(MapUtils.getString(map, "tel")));
        }
        if (map.containsKey("phone"))
        {
            map.put("phone", StringUtils.desensitizedPhoneNumber(MapUtils.getString(map, "phone")));
        }
        if (map.containsKey("mobile"))
        {
            map.put("mobile", StringUtils.desensitizedPhoneNumber(MapUtils.getString(map, "mobile")));
        }
        if (map.containsKey("contact_phone"))
        {
            map.put("contact_phone", StringUtils.desensitizedPhoneNumber(MapUtils.getString(map, "contact_phone")));
        }
    }

    //导出 店铺审核列表
    private void exportMchExamineData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"店铺ID", "店铺名称", "联系人", "联系电话", "申请/审核时间", "审核状态"};
            //对应字段
            String[]     kayList = new String[]{"id", "name", "realname", "tel", "add_time", "examineName"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("店铺审核列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(false);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出 店铺审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportMchExamineData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean examineMch(MainVo vo, int mchId, Integer reviewStatus, String text) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String     content    = "";
            //获取商城信息
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), adminModel.getShop_id());
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "找不到店铺配置");
            }
            //获取商户信息
            MchModel mchModel = new MchModel();
            mchModel.setId(mchId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHBCZ, "商户不存在");
            }
            //发送系统消息给店铺的管理员
            SystemMessageModel systemMessageSave = new SystemMessageModel();
            systemMessageSave.setType(1);
            systemMessageSave.setSenderid("admin");
            systemMessageSave.setStore_id(vo.getStoreId());
            systemMessageSave.setRecipientid(mchModel.getUser_id());
            systemMessageSave.setTitle("系统通知");
            systemMessageSave.setTime(new Date());
            MchModel mchModelUpdate = new MchModel();
            mchModelUpdate.setId(mchModel.getId());
            mchModelUpdate.setLogo(mchConfigModel.getLogo());
            if (reviewStatus != null && reviewStatus.equals(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS))
            {
                mchModelUpdate.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
                //审核通过变成已打烊
                mchModelUpdate.setIs_open(MchModel.IS_OPEN_PROOFING.toString());
                //审核通过上传商铺经纬度
//                if (!Objects.isNull(mchModel.getLongitude()) && !Objects.isNull(mchModel.getLatitude())) {
//                    //经度
//                    Double longitude = Double.valueOf(mchModel.getLongitude());
//                    //纬度
//                    Double latitude = Double.valueOf(mchModel.getLatitude());
//                    if (!GeoUtils.isAvailablePointInRedis(longitude, latitude)) {
//                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXWZ, "Invalid position(无效位置)");
//                    }
////                    redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(longitude, latitude), String.valueOf(mchModel.getId()));
//                }
                systemMessageSave.setContent("您的店铺【" + mchModel.getName() + "】已经通过审核，快去看看吧!");
                content = "通过了店铺ID：" + mchId + " 的申请审核";
            }
            else if (reviewStatus != null && reviewStatus.equals(DictionaryConst.MchExameStatus.EXAME_NOT_PASS_STATUS))
            {
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJLYBNWK, "拒绝理由不能为空");
                }
                mchModelUpdate.setReview_result(text);
                mchModelUpdate.setReview_status(DictionaryConst.MchExameStatus.EXAME_NOT_PASS_STATUS.toString());
                systemMessageSave.setContent("您的店铺【" + mchModel.getName() + "】未通过审核！驳回原因：" + text);
                content = "拒绝了了店铺ID：" + mchId + " 的申请审核";
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            mchModelUpdate.setAdd_time(new Date());
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            //发送结果系统消息给店铺的管理员
            systemMessageModelMapper.insertSelective(systemMessageSave);
            //记录日志
            publiceService.addAdminRecord(vo.getStoreId(), content, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());

            // TODO: 2021/1/26 审核结果推送
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商户审核/拒绝 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "网络异常", "examineMch");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMchClass(MainVo vo, Integer id, String name, String img, Integer sort, Integer isDisplay) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QSRDPMC, "请输入分类名称", "addMchClass");
            }
            if (Objects.isNull(sort))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QTXPXZ, "请填写排序值", "addMchClass");
            }
            if (Objects.isNull(isDisplay))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_QXZSFXX, "请选择是否显示", "addMchClass");
            }
            if (!Objects.isNull(id))
            {
                MchClassModel mchClassOld = mchClassModelMapper.selectByPrimaryKey(id);
                if (Objects.isNull(mchClassOld))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "分类不存在", "addMchClass");
                }
            }
            MchClassModel mchClassModel = new MchClassModel();
            mchClassModel.setStore_id(vo.getStoreId());
            mchClassModel.setName(name);
            if (StringUtils.isNotEmpty(img))
            {
                mchClassModel.setImg(ImgUploadUtils.getUrlImgByName(img, true));
            }
            mchClassModel.setSort(sort);
            mchClassModel.setIs_display(isDisplay);
            if (!Objects.isNull(id))
            {
                mchClassModel.setId(id);
                int i = mchClassModelMapper.countByName(vo.getStoreId(), name, id);
                if (i > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPFLYCZ, "店铺分类已存在", "addMchClass");
                }
                mchClassModelMapper.updateByPrimaryKeySelective(mchClassModel);

                publiceService.addAdminRecord(vo.getStoreId(), "修改了店铺分类ID：" + id + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                int i = mchClassModelMapper.countByName(vo.getStoreId(), name, null);
                if (i > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPFLYCZ, "店铺分类已存在", "addMchClass");
                }
                mchClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                mchClassModel.setAdd_date(new Date());
                mchClassModelMapper.insertSelective(mchClassModel);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了店铺分类ID：" + mchClassModel.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改店铺分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addMchClass");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void isDisplay(MainVo vo, Integer id, Integer isDisplay) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            MchClassModel mchClassOld = mchClassModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(mchClassOld))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "分类不存在", "addMchClass");
            }
            mchClassOld.setIs_display(isDisplay);
            mchClassModelMapper.updateByPrimaryKeySelective(mchClassOld);

            publiceService.addAdminRecord(vo.getStoreId(), "将店铺分类ID：" + id + " 进行了是否显示操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否显示店铺分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMchClass");
        }
    }

    @Override
    public Map<String, Object> mchClassList(MainVo vo, Integer id, String name, Integer isDisPlay) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
//            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
//            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            if (!Objects.isNull(id))
            {
                paramMap.put("id", id);
            }
            if (StringUtils.isNotEmpty(name))
            {
                paramMap.put("name", name);
            }
            if (!Objects.isNull(isDisPlay))
            {
                paramMap.put("isDisplay", isDisPlay);
            }
            int                       i    = mchClassModelMapper.countCondition(paramMap);
            List<Map<String, Object>> list = new ArrayList<>();
            if (i > 0)
            {
                list = mchClassModelMapper.selectCondition(paramMap);
                list.stream().forEach(map ->
                {
                    map.put("img", publiceService.getImgPath(MapUtils.getString(map, "img"), vo.getStoreId()));
                    map.put("add_date", DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS));
                });
            }
            int maxSort = mchClassModelMapper.selectMaxSort(vo.getStoreId());
            resultMap.put("total", i);
            resultMap.put("maxSort", maxSort);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取店铺分类列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchClassList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMchClass(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            MchClassModel mchClassOld = mchClassModelMapper.selectByPrimaryKey(id);
            if (Objects.isNull(mchClassOld))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MBBCZ, "分类不存在", "addMchClass");
            }
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setCid(id);
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            int i = mchModelMapper.selectCount(mchModel);

            if (i > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPFLZZSY, "该分类已绑定店铺,请解绑后操作", "addMchClass");
            }
            mchClassOld.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
            mchClassModelMapper.updateByPrimaryKeySelective(mchClassOld);
            publiceService.addAdminRecord(vo.getStoreId(), "删除了店铺分类ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除店铺分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMchClass");
        }
    }

    @Override
    public Map<String, Object> getGoodsExamineInfo(MainVo vo, String mchName, String goodsName, Integer goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("page", vo.getPageNo());
            parmaMap.put("pagesize", vo.getPageSize());
            parmaMap.put("pageto", "");
            parmaMap.put("mchStatus", DictionaryConst.GoodsMchExameStatus.EXAME_WAIT_STATUS);
            if (!StringUtils.isEmpty(goodsName))
            {
                parmaMap.put("product_title", goodsName);
            }
            if (!StringUtils.isEmpty(mchName))
            {
                parmaMap.put("mch_name", mchName);
            }
            if (goodsId != null)
            {
                parmaMap.put("goodsId", goodsId);
            }
            resultMap = publicGoodsService.productList(vo.getStoreId(), null, 0, GloabConst.LktConfig.LKT_CONFIG_TYPE_PT, parmaMap);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商户审核信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsExamineInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getGoodsDetailInfo(MainVo vo, int goodsId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取商品信息
            List<Map<String, Object>> goodsInfoList = productListModelMapper.selectGoodsDetailInfo(vo.getStoreId(), goodsId);
            for (Map<String, Object> map : goodsInfoList)
            {
                map.put("cover_map", publiceService.getImgPath(MapUtils.getString(map, "cover_map"), vo.getStoreId()));
                map.put("video", publiceService.getImgPath(MapUtils.getString(map, "video"), vo.getStoreId()));
                map.put("proVideo", publiceService.getImgPath(MapUtils.getString(map, "pro_video"), vo.getStoreId()));
                //商品类别
                List<String>        nameList    = new ArrayList<>();
                String[]            classIdList = StringUtils.trim(MapUtils.getString(map, "product_class"), SplitUtils.HG).split(SplitUtils.HG);
                Map<String, Object> parmaMap    = new HashMap<>(16);
                parmaMap.put("classIdList", classIdList);
                List<Map<String, Object>> classMapList = productClassModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> classMap : classMapList)
                {
                    nameList.add(MapUtils.getString(classMap, "pname"));
                }
                map.put("className", StringUtils.stringImplode(nameList, SplitUtils.FXG));
            }
            //获取商品图片
            ProductImgModel productImgModel = new ProductImgModel();
            productImgModel.setProduct_id(goodsId);
            List<ProductImgModel> productImgModelList = productImgModelMapper.select(productImgModel);
            List<String>          goodsImageUrls      = new ArrayList<>();
            for (ProductImgModel productImg : productImgModelList)
            {
                String imgUrl = publiceService.getImgPath(productImg.getProduct_url(), vo.getStoreId());
                goodsImageUrls.add(imgUrl);
            }
            //获取规格信息
            List<Map<String, Object>> confiGureList = new ArrayList<>();
            //获取所有规格
            List<String> attrAllList = new ArrayList<>();


            //规格值集
            List<Map<String, Object>> strArrList      = new ArrayList<>();
            List<Map<String, Object>> checkedAttrList = new ArrayList<>();
            List<Map<String, Object>> attrGroupList   = new ArrayList<>();

            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(goodsId);
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
            if (confiGureModelList != null && confiGureModelList.size() > 0)
            {
                //规格处理
                String attribute = confiGureModelList.get(0).getAttribute();
                if (!StringUtils.isEmpty(attribute))
                {
                    Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attribute, Map.class));
                    if (attributeMap != null)
                    {
                        for (String key : attributeMap.keySet())
                        {
                            Map<String, Object> dataMap        = new HashMap<>(16);
                            String              attribyteKey   = key;
                            String              attribyteValue = attributeMap.get(key).toString();
                            int                 index          = attribyteKey.indexOf("_LKT_");
                            if (index > 0)
                            {
                                //属性名称
                                attribyteKey = attribyteKey.substring(0, attribyteKey.indexOf("_LKT_"));
                                attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));
                            }
                            dataMap.put("attr_group_name", attribyteKey);
                            //默认规格不展示不回显
                            if (attribyteKey.equals("默认") && attribyteValue.equals("默认"))
                            {
                                continue;
                            }
                            attrGroupList.add(dataMap);
                        }
                    }
                    //属性名称集合,去重
                    List<String> tempList = new ArrayList<>();
                    for (ConfiGureModel confiGure : confiGureModelList)
                    {
                        //当前属性信息
                        List<Map<String, Object>> attrLists       = new ArrayList<>();
                        String                    attributeStr    = confiGure.getAttribute();
                        Map<String, Object>       attributeStrMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attributeStr, Map.class));
                        if (attributeStrMap != null)
                        {
                            for (String key : attributeStrMap.keySet())
                            {
                                String attributeKey   = key;
                                String attribyteValue = attributeStrMap.get(key).toString();
                                int    index          = attributeKey.indexOf("_LKT_");
                                if (index > 0)
                                {
                                    //获取id (尺码_LKT_8) 左边为名称 最后一个是id
                                    int                 keyId       = attributeKey.lastIndexOf("-") + 1;
                                    int                 valueId     = attribyteValue.indexOf("_") + 1;
                                    String              keyIdTemp   = attributeKey.substring(keyId);
                                    String              valueIdTemp = attribyteValue.substring(0, valueId);
                                    Map<String, Object> dataMap     = new HashMap<>(16);
                                    dataMap.put("id0", keyIdTemp);
                                    dataMap.put("id1", valueIdTemp);
                                    strArrList.add(dataMap);
                                    //获取名称
                                    attributeKey = attributeKey.substring(0, attributeKey.indexOf("_LKT"));
                                    attribyteValue = attribyteValue.substring(0, attribyteValue.indexOf("_LKT"));
                                }
                                //默认规格不展示不回显
                                if (attributeKey.equals("默认") && attribyteValue.equals("默认"))
                                {
                                    continue;
                                }
                                for (Map<String, Object> map : attrGroupList)
                                {
                                    //规格集 (attr_name:value,status:false)
                                    List<Map<String, Object>> attrList = new ArrayList<>();
                                    if (map.containsKey("attr_list"))
                                    {
                                        attrList = DataUtils.cast(map.get("attr_list"));
                                    }
                                    if (attributeKey.equals(map.get("attr_group_name").toString()))
                                    {
                                        //判断当前规格值是否存在数组中，不存在则添加
                                        if (!tempList.contains(attribyteValue))
                                        {
                                            Map<String, Object> attrListMap = new HashMap<>(16);
                                            attrListMap.put("attr_name", attribyteValue);
                                            attrList.add(attrListMap);
                                            map.put("attr_list", attrList);
                                            tempList.add(attribyteValue);
                                        }
                                    }
                                }
                                Map<String, Object> attrListsMap = new HashMap<>(16);
                                attrListsMap.put("attr_id", "");
                                attrListsMap.put("attr_group_name", attributeKey);
                                attrListsMap.put("attr_name", attribyteValue);
                                attrLists.add(attrListsMap);
                            }
                        }
                        //默认规格不返回
                        if (attrLists.size() == 0)
                        {
                            continue;
                        }
                        Map<String, Object> checkedAttrListMap = new HashMap<>(16);
                        checkedAttrListMap.put("attr_list", attrLists);
                        checkedAttrListMap.put("cbj", confiGure.getCostprice());
                        checkedAttrListMap.put("yj", confiGure.getYprice());
                        checkedAttrListMap.put("sj", confiGure.getPrice());
                        checkedAttrListMap.put("kucun", confiGure.getNum());
                        checkedAttrListMap.put("unit", confiGure.getUnit());
                        checkedAttrListMap.put("bar_code", confiGure.getBar_code());
                        checkedAttrListMap.put("img", publiceService.getImgPath(confiGure.getImg(), vo.getStoreId()));
                        checkedAttrListMap.put("cid", confiGure.getId());
                        //加入可核销次数
                        checkedAttrListMap.put("write_off_num", confiGure.getWrite_off_num());
                        checkedAttrList.add(checkedAttrListMap);
                    }
                }
            }

            resultMap.put("goodsInfo", goodsInfoList);
            resultMap.put("goodsImageUrls", goodsImageUrls);
            //动态字段名称
            resultMap.put("attrList", attrAllList);
            resultMap.put("attr_group_list", attrGroupList);
            resultMap.put("checked_attr_list", checkedAttrList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品详情信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getGoodsDetailInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean goodsExamine(MainVo vo, int goodsId, int status, String text) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String     content    = "";
            //获取商品信息
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(vo.getStoreId());
            productListModel.setId(goodsId);
            productListModel = productListModelMapper.selectProductByIdAndStoreId(goodsId, vo.getStoreId());
            if (productListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBCZ, "商品不存在");
            }
            //商品sku信息
            Set<Integer>   attrIdList     = new HashSet<>();
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setPid(productListModel.getId());
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> confiGureModelList = confiGureModelMapper.select(confiGureModel);
            for (ConfiGureModel confiGure : confiGureModelList)
            {
                String              attributeStr = confiGure.getAttribute();
                Map<String, Object> attributeMap = DataUtils.cast(SerializePhpUtils.getUnserializeObj(attributeStr, Map.class));
                if (attributeMap != null)
                {
                    //a:2:{s:12:"颜色_LKT_1";s:14:"白色_LKT_124";s:12:"尺码_LKT_8";s:7:"M_LKT_9";}
                    for (String key : attributeMap.keySet())
                    {
                        String attribyteKeyId   = key;
                        String attribyteValueId = attributeMap.get(key) + "";
                        int    indexKey         = attribyteKeyId.lastIndexOf("_") + 1;
                        int    indexValue       = attribyteValueId.lastIndexOf("_") + 1;
                        if (indexKey > 0)
                        {
                            //属性id
                            attribyteKeyId = attribyteKeyId.substring(indexKey);
                            attrIdList.add(Integer.parseInt(attribyteKeyId));
                        }
                        if (indexValue > 0)
                        {
                            //属性值id
                            attribyteValueId = attribyteValueId.substring(indexValue);
                            attrIdList.add(Integer.parseInt(attribyteValueId));
                        }
                    }
                }
            }
            //修改商品状态
            ProductListModel productListModelUpdate = new ProductListModel();
            productListModelUpdate.setId(productListModel.getId());
            //添加商品类型标识
            productListModelUpdate.setCommodity_type(productListModel.getCommodity_type());
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            if (status == ExamineStatus.ADOPT)
            {
                //获取未生效的属性
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("attrIdList", attrIdList);
                //根据skuId修改，新增的时候会判断表有没有数据，有skuId会把状态改为生效
//                parmaMap.put("status", SkuModel.SKU_STATUS_INVALID);
                List<Map<String, Object>> notTakeEffectSkuList = skuModelMapper.getAttributeDynamic(parmaMap);
                //属性生效
                for (Map<String, Object> map : notTakeEffectSkuList)
                {
                    Integer  attrId   = Integer.parseInt(String.valueOf(map.get("id")));
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(attrId);
                    skuModel.setStatus(SkuModel.SKU_STATUS_TAKE_EFFECT);
                    skuModel.setIs_examine(DictionaryConst.WhetherMaven.WHETHER_OK);
                    skuModelMapper.updateByPrimaryKeySelective(skuModel);
                }
                logger.debug("生效属性数量:{}", notTakeEffectSkuList.size());
                productListModelUpdate.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
                productListModelUpdate.setUpper_shelf_time(new Date());
                productListModelUpdate.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_PASS_STATUS.toString());
                logger.debug("商品id:{} 审核通过", goodsId);
                content = "通过了商品ID：" + goodsId + " 审核";
                //pc店铺商品审核通过消息通知
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.COMMODITY_APPROVAL_URL);
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_COMMODITY_APPROVAL);
                messageLoggingSave.setParameter(goodsId + "");
                messageLoggingSave.setContent("商品ID为:" + goodsId + "的审核申请通过。");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //H5店铺商品审核通过消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_COMMODITY_APPROVAL);
                messageLoggingSave.setParameter("商品审核申请通过");
                messageLoggingSave.setContent("商品ID为:" + goodsId + "的审核申请通过。");
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
            }
            else
            {
                //审核拒绝
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJLYBNWK, "拒绝理由不能为空");
                }
                productListModelUpdate.setStatus(DictionaryConst.GoodsStatus.OFFLINE_GROUNDING.toString());
                productListModelUpdate.setMch_status(DictionaryConst.GoodsMchExameStatus.EXAME_NOT_PASS_STATUS.toString());
                productListModelUpdate.setRefuse_reasons(text);
                logger.debug("商品id:{} 审核拒绝", goodsId);
                content = "拒绝了商品ID：" + goodsId + " 审核";
                //pc店铺商品审核失败消息通知
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.COMMODITY_REJECTION_URL);
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_COMMODITY_APPROVAL);
                messageLoggingSave.setParameter(goodsId + "");
                messageLoggingSave.setContent("商品ID为:" + goodsId + "的审核申请被拒绝。" + "拒接理由为:" + text);
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //H5店铺商品审核失败消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setMch_id(productListModel.getMch_id());
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_COMMODITY_APPROVAL);
                messageLoggingSave.setParameter("商品审核申请通过");
                messageLoggingSave.setContent("商品ID为:" + goodsId + "的审核申请被拒绝。" + "拒接理由为:" + text);
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //删除未审核通过的sku
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("attrIdList", attrIdList);
                parmaMap.put("is_examine", DictionaryConst.WhetherMaven.WHETHER_NO);
                List<Map<String, Object>> notTakeEffectSkuList = skuModelMapper.getAttributeDynamic(parmaMap);
                //属性生效
                for (Map<String, Object> map : notTakeEffectSkuList)
                {
                    Integer  attrId   = Integer.parseInt(String.valueOf(map.get("id")));
                    SkuModel skuModel = new SkuModel();
                    skuModel.setId(attrId);
                    skuModelMapper.deleteByPrimaryKey(skuModel);
                }
            }
            int count = productListModelMapper.updateByPrimaryKeySelective(productListModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            //记录日志
            publiceService.addAdminRecord(vo.getStoreId(), content, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "goodsExamine");
        }
    }

    @Override
    public Map<String, Object> getWithdrawalInfo(WithdrawalVo vo, Integer status, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);

            if (status != null)
            {
                parmaMap.put("status", status);
            }
            else
            {
                parmaMap.put("not_status", WithdrawModel.EXAME_WAIT_STATUS);
            }
            parmaMap.put("is_mch", 1);

            if (vo.getStatus() != null)
            {
                parmaMap.put("status", vo.getStatus());
            }
            if (vo.getWid() != null && vo.getWid() > 0)
            {
                parmaMap.put("wid", vo.getWid());
            }
            if (!StringUtils.isEmpty(vo.getMchName()))
            {
                parmaMap.put("like_mchName", vo.getMchName());
            }
            if (!StringUtils.isEmpty(vo.getPhone()))
            {
                parmaMap.put("like_mobile", vo.getPhone());
            }
            if (StringUtils.isNotEmpty(vo.getUserNameAndPhone()))
            {
                parmaMap.put("like_mch_name_mobile", vo.getUserNameAndPhone());
            }
            if (StringUtils.isNotEmpty(vo.getWithdrawStatus()))
            {
                parmaMap.put("withdrawStatus", vo.getWithdrawStatus());
            }
            if (StringUtils.isNotEmpty(vo.getWxStatus()))
            {
                parmaMap.put("wxStatus", vo.getWxStatus());
            }
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total    = withdrawModelMapper.countWithdrawLeftMchBank(parmaMap);
            List<Map<String, Object>> dataList = withdrawModelMapper.getWithdrawLeftMchBank(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                int    examine = MapUtils.getIntValue(map, "status");
                String examineName;
                switch (examine)
                {
                    //0：审核中 1：审核通过 2：拒绝
                    case 0:
                        examineName = "审核中";
                        break;
                    case 1:
                        examineName = "审核通过";
                        break;
                    case 2:
                        examineName = "拒绝";
                        break;
                    default:
                        examineName = "-";
                }
                map.put("examineName", examineName);
                String bankCardNumber = MapUtils.getString(map, "Bank_card_number");
                String email = MapUtils.getString(map, "email","");

                map.put("card",StringUtils.isNotEmpty(bankCardNumber) ? bankCardNumber : email);
                String add_date = DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS);
                map.put("add_date", add_date);
                map.put("logo", publiceService.getImgPath(MapUtils.getString(map, "logo"), vo.getStoreId()));
                map.put("headimgurl", publiceService.getImgPath(MapUtils.getString(map, "head_img"), vo.getStoreId()));
                if (MapUtils.getInteger(map, "withdraw_status").equals(WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    map.put("Bank_card_number", MapUtils.getString(map, "wx_name"));
                }
            }
            if (vo.getExportType() == 1)
            {
                exportWithdrawalData(dataList, status == null, response);
                return null;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getWithdrawalExamineInfo");
        }
        return resultMap;
    }

    //导出 提现审核列表
    private void exportWithdrawalData(List<Map<String, Object>> list, boolean flag, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            list.forEach(map ->
            {
                Integer withdraw_status = MapUtils.getInteger(map, "withdraw_status");
                Integer wx_status       = MapUtils.getInteger(map, "wx_status");
                if (withdraw_status.equals(WithdrawModel.WITHDRAW_STATUS.YHK))
                {
                    map.put("withdraw_status", "银行卡");
                }
                else
                {
                    map.put("withdraw_status", "微信余额");
                }
                if (wx_status != null)
                {
                    if (wx_status.equals(WithdrawModel.WX_STATUS.WAIT_PAY))
                    {
                        map.put("wx_status", "进行中");
                    }
                    if (wx_status.equals(WithdrawModel.WX_STATUS.SUCCESS))
                    {
                        map.put("wx_status", "已完成");
                    }
                    if (wx_status.equals(WithdrawModel.WX_STATUS.FAIL))
                    {
                        map.put("wx_status", "提现失败");
                    }
                }
                else
                {
                    map.put("wx_status", "");
                }
            });
            //表头
            String[] headerList = new String[]{"店铺", "联系电话", "状态", "申请时间", "提现金额", "提现手续费", "持卡人姓名", "银行名称", "支行名称", "卡号", "提现方式", "打款状态"};
            //对应字段
            String[] kayList = new String[]{"mch_name", "mobile", "examineName", "add_date", "money", "s_charge", "Cardholder", "Bank_name", "branch", "Bank_card_number", "withdraw_status", "wx_status"};
            if (flag)
            {
                List<String> headers = new ArrayList<>();
                Collections.addAll(headers, headerList);
                headers.add("备注");
                headerList = headers.toArray(new String[0]);

                List<String> kay = new ArrayList<>();
                Collections.addAll(kay, kayList);
                kay.add("refuse");
                kayList = kay.toArray(new String[0]);
            }

            ExcelParamVo vo = new ExcelParamVo();
            vo.setTitle("提现审核列表");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("提现审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportWithdrawalData");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean withdrawalExamineMch(MainVo vo, int id, int status, String text) throws LaiKeAPIException
    {
        try
        {
            String logoText = "店铺%s提现了%s";
            String logoText1;
            //获取提现申请信息
            WithdrawModel withdrawModel = new WithdrawModel();
            withdrawModel.setId(id);
            withdrawModel.setStore_id(vo.getStoreId());
            withdrawModel.setStatus(WithdrawModel.EXAME_WAIT_STATUS.toString());
            withdrawModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            withdrawModel = withdrawModelMapper.selectOne(withdrawModel);
            if (withdrawModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSQBCZ, "提现申请不存在");
            }
            //系统推送
            String tuiTitle   = "店铺提现成功!";
            String tuiContext = String.format("您提现的%s元已到账,快去看看吧!", withdrawModel.getMoney());
            //获取提现用户店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setUser_id(withdrawModel.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHMXWDP, "用户名下无店铺");
            }
            //操作记录
            RecordModel recordModel = new RecordModel();
            recordModel.setStore_id(vo.getStoreId());
            recordModel.setUser_id(withdrawModel.getUser_id());
            recordModel.setMoney(withdrawModel.getMoney());
            recordModel.setOldmoney(mchModel.getCashable_money());
            recordModel.setEvent(String.format(logoText, withdrawModel.getUser_id(), withdrawModel.getMoney()));
            recordModel.setType(RecordModel.RecordType.WITHDRAWAL_SUCCEED);
            recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_OK);
            recordModel.setAdd_date(new Date());
            int count = recordModelMapper.insertSelective(recordModel);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
            }
            //入驻商户账户收支记录表
            if (status == DictionaryConst.MchExameStatus.EXAME_PASS_STATUS)
            {
                MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
                mchAccountLogModel.setStore_id(vo.getStoreId());
                mchAccountLogModel.setMch_id(mchModel.getId().toString());
                mchAccountLogModel.setPrice(withdrawModel.getMoney());
                mchAccountLogModel.setAccount_money(mchModel.getAccount_money());
                mchAccountLogModel.setStatus(MchAccountLogModel.AccountLogStatus.OUT_ACCOUNT);
                mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_WITHDRAWAL);
                mchAccountLogModel.setAddtime(new Date());
                count = mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                }
            }
            //提现状态修改
            WithdrawModel withdrawModelUpdate = new WithdrawModel();
            withdrawModelUpdate.setId(withdrawModel.getId());
            if (WithdrawModel.EXAME_PASS_STATUS.equals(status))
            {
                withdrawModelUpdate.setStatus(WithdrawModel.EXAME_PASS_STATUS.toString());
                logoText1 = "提现id为" + withdrawModel.getId() + "的提现已通过";
                //判断是否为微信零钱提现
                if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.WX))
                {
                    withdrawModelUpdate.setWxStatus(WithdrawModel.WX_STATUS.WAIT_PAY);
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("app.v3.MerchantTransfersToChange", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
                //贝宝提现
                if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.PAYPAL))
                {
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        BigDecimal money    = withdrawModel.getMoney();
                        BigDecimal s_charge = withdrawModel.getS_charge();
                        money = money.subtract(s_charge);
                        params.put("money", money);
//                        params.put("money", withdrawModel.getMoney());
                        params.put("email", withdrawModel.getEmail());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("v1.payouts", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }
                //stripe提现
                if (withdrawModel.getWithdrawStatus().equals(WithdrawModel.WITHDRAW_STATUS.STRIPE))
                {
                    try
                    {
                        Map<String, Object> params = new HashMap<>(16);
                        params.put("storeId", vo.getStoreId());
                        params.put("withdrawId", withdrawModel.getId());
                        BigDecimal money    = withdrawModel.getMoney();
                        BigDecimal s_charge = withdrawModel.getS_charge();
                        money = money.subtract(s_charge);
                        params.put("money", money);
//                        params.put("money", withdrawModel.getMoney());
                        params.put("stripe_account_id", withdrawModel.getStripeAccountId());
                        Map<String, Object> paramMap1 = new HashMap<>(16);
                        paramMap1.put("paramJson", JSON.toJSONString(params));
                        httpApiUtils.executeHttpApi("comps.stripe.payouts", paramMap1, MediaType.MULTIPART_FORM_DATA_VALUE);
                    }
                    catch (Exception e)
                    {
                        //提现失败修改 todo
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                    }
                }

                publiceService.addAdminRecord(vo.getStoreId(), "通过了店铺ID：" + mchModel.getId() + " 的提现审核", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            else
            {
                if (StringUtils.isEmpty(text))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JJYYBNWK, "拒绝原因不能为空");
                }
                withdrawModelUpdate.setStatus(WithdrawModel.EXAME_NOT_PASS_STATUS.toString());
                withdrawModelUpdate.setRefuse(text);
                logoText1 = "提现id为" + withdrawModel.getId() + "的提现已拒绝";
                tuiTitle = "店铺提现失败!";
                tuiContext = String.format("您%s申请的提现被驳回!驳回原因:%s", DateUtil.dateFormate(withdrawModel.getAdd_date(), GloabConst.TimePattern.YMDHMS), text);
                //拒绝提现,还到钱包
                count = mchModelMapper.refuseWithdraw(mchModel.getId(), withdrawModel.getMoney());
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
                }
                publiceService.addAdminRecord(vo.getStoreId(), "拒绝了店铺ID：" + mchModel.getId() + " 的提现审核", AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
            }
            count = withdrawModelMapper.updateByPrimaryKeySelective(withdrawModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TXSB, "提现失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), withdrawModel.getUser_id(), logoText1, 6, AdminRecordModel.Source.PC_PLATFORM);

            //站内推送退款信息  禅道50080
//            publicAdminService.systemMessageSend(vo, 1, tuiTitle, tuiContext, withdrawModel.getUser_id());
            //h5店铺消息通知
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(mchModel.getId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url("");
            messageLoggingSave.setParameter(tuiTitle);
            messageLoggingSave.setContent(tuiContext);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            //pc店铺消息通知
            messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(mchModel.getId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_MCH_WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setTo_url(MessageLoggingModal.PcMchUrl.WITHDRAWAL_APPLICATION_SUCCESSFULLY_SUBMITTED);
            messageLoggingSave.setParameter(withdrawModelUpdate.getId() + "");
            messageLoggingSave.setContent(tuiContext);
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺提现审核 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "withdrawalExamineMch");
        }
    }

    @Override
    public Map<String, Object> getStoreConfigInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel     adminModel     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), customerModelMapper.getStoreMchId(vo.getStoreId()));
            if (mchConfigModel != null)
            {
                String logoUrl = publiceService.getImgPath(mchConfigModel.getLogo(), vo.getStoreId());
                mchConfigModel.setLogo(logoUrl);
                mchConfigModel.setHead_img(publiceService.getImgPath(mchConfigModel.getHead_img(), vo.getStoreId()));
                mchConfigModel.setPoster_img(publiceService.getImgPath(mchConfigModel.getPoster_img(), vo.getStoreId()));
            }

            resultMap.put("data", mchConfigModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商城设置信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreConfigInfo");
        }
        return resultMap;
    }

    @Override
    public boolean setStoreConfigInfo(AddStoreConfigVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(vo.getLogiUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRLGBNWK, "默认logo不能为空");
            }
//            if (vo.getOutDayDel() == null || vo.getOutDayDel() == 0) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCTSBNWK, "删除天数不能为空");
//            }
//            if (vo.getOutDayDel() < 0) {
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCTSBNXY0, "删除天数不能小于0");
//            }
            if (StringUtils.isEmpty(vo.getHeadImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRTXBNWK, "默认头像不能为空");
            }
            if (StringUtils.isEmpty(vo.getPosterImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMRXCTBNWK, "宣传图不能为空");
            }
            if (vo.getAutoExamine() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入入驻审核时间");
            }
            if (vo.getMinWithdrawalMoney() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNWK, "最小提现金额不能为空");
            }
            if (vo.getMinWithdrawalMoney().doubleValue() < 0.01)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEZSXY, "最小提现金额至少需要0.01");
            }
            if (vo.getMaxWithdrawalMoney() == null || vo.getMaxWithdrawalMoney().doubleValue() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDTXJEBNWK, "最大提现金额不能为空");
            }
            else if (vo.getServiceCharge().doubleValue() >= 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXFWDYXYDXS, "手续费为大于0小于1的小数");
            }
            //只保留两位小数
//            vo.setServiceCharge(new BigDecimal(vo.getServiceCharge() + "").setScale(2, BigDecimal.ROUND_FLOOR));
            if (vo.getMinWithdrawalMoney().doubleValue() > vo.getMaxWithdrawalMoney().doubleValue())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZXTXJEBNDYZDTXJE, "最小提现金额不能大于最大提现金额");
            }
            if (StringUtils.isEmpty(vo.getUploadType()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCFSBNWK, "上传方式不能为空");
            }
            if (Objects.isNull(vo.getAutoLogOff()) || vo.getAutoLogOff() <= 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_SDSZBNWK, "锁定设置不能为空");
            }
            if (Objects.isNull(vo.getWithdrawalTimeOpen()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择提现时间设置");
            }
            if (vo.getWithdrawalTimeOpen() > 0 && StringUtils.isEmpty(vo.getWithdrawalTime()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请填写每月提现时间");
            }
            if (vo.getWithdrawalTimeOpen().equals(MchConfigModel.SPECIFY_TIME_PERIOD))
            {
                String[] split = vo.getWithdrawalTime().split("-");
                if (split.length != 2)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请填写每月提现时间");
                }
                if (Integer.parseInt(split[0]) >= Integer.parseInt(split[1]))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请填写每月提现时间");
                }
            }
            int            count;
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(vo.getStoreId());
            mchConfigModel.setMch_id(adminModel.getShop_id());
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);

            MchConfigModel mchConfigModelSave = new MchConfigModel();
            mchConfigModelSave.setStore_id(vo.getStoreId());
            mchConfigModelSave.setIs_display(vo.getIsOpenPlugin());
            mchConfigModelSave.setLogo(ImgUploadUtils.getUrlImgByName(vo.getLogiUrl(), true));
            mchConfigModelSave.setHead_img(ImgUploadUtils.getUrlImgByName(vo.getHeadImg(), true));
            mchConfigModelSave.setPoster_img(ImgUploadUtils.getUrlImgByName(vo.getPosterImg(), true));
            mchConfigModelSave.setMin_charge(vo.getMinWithdrawalMoney());
            mchConfigModelSave.setMax_charge(vo.getMaxWithdrawalMoney());
            mchConfigModelSave.setService_charge(vo.getServiceCharge());
            mchConfigModelSave.setCommodity_setup(vo.getUploadType());
            mchConfigModelSave.setIllustrate(vo.getIllustrate());
            mchConfigModelSave.setPromise_switch(vo.getPromiseSwitch());
            mchConfigModelSave.setDelete_settings(vo.getOutDayDel());
            mchConfigModelSave.setAuto_examine(vo.getAutoExamine());
            mchConfigModelSave.setAuto_log_off(vo.getAutoLogOff());
            mchConfigModelSave.setWithdrawal_time_open(vo.getWithdrawalTimeOpen());
            mchConfigModelSave.setWithdrawal_time(vo.getWithdrawalTime());
            if (StringUtils.isEmpty(vo.getWithdrawalTime()))
            {
                mchConfigModelSave.setWithdrawal_time("");
            }
            //保证金
            if (vo.getPromiseSwitch().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
            {
                if (vo.getPromiseAmt() == null || BigDecimal.ZERO.compareTo(vo.getPromiseAmt()) >= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRBZJ, "请输入保证金");
                }
                mchConfigModelSave.setPromise_amt(vo.getPromiseAmt());
                mchConfigModelSave.setPromise_text(vo.getPromiseText());
            }


            if (mchConfigModel != null)
            {
                mchConfigModelSave.setId(mchConfigModel.getId());
                count = mchConfigModelMapper.updateByPrimaryKeySelective(mchConfigModelSave);
            }
            else
            {
                mchConfigModelSave.setMch_id(adminModel.getShop_id());
                count = mchConfigModelMapper.insertSelective(mchConfigModelSave);
            }
            //记录日志
            publiceService.addAdminRecord(vo.getStoreId(), "修改了店铺插件的配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改 商城配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getStoreConfigInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void location(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setReview_status(DictionaryConst.ExameStatus.EXAME_PASS_STATUS);
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
//            List<MchModel> mchModels = mchModelMapper.select(mchModel);
//            mchModels.stream().forEach(mch -> {
//                if (StringUtils.isNotEmpty(mch.getLongitude()) && StringUtils.isNotEmpty(mch.getLatitude())) {
//                    if (!GeoUtils.isAvailablePointInRedis(Double.parseDouble(mch.getLongitude()), Double.parseDouble(mch.getLatitude()))) {
//                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WXWZ, "Invalid position(无效位置)", "location");
//                    } else {
//                        redisTemplate.opsForGeo().add(NEARBY_KEY, new Point(Double.parseDouble(mch.getLongitude()), Double.parseDouble(mch.getLatitude())), String.valueOf(mch.getId()));
//                    }
//                }
//            });
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上传商城位置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "location");
        }
    }

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private WithdrawModelMapper withdrawModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private HttpApiUtils httpApiUtils;

}
