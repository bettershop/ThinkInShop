package com.laiketui.admins.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.admins.api.mch.MchService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
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
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.config.BannerModel;
import com.laiketui.domain.config.PrintSetupModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.mch.AddMchVo;
import com.laiketui.domain.vo.mch.MchPrintSetupVo;
import com.laiketui.domain.vo.pc.AddBannerInfoVo;
import com.laiketui.domain.vo.saas.CurrencyStoreVo;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.laiketui.core.lktconst.GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN;

/**
 * 店铺设置
 *
 * @author Trick
 * @date 2021/5/27 11:37
 */
@Service
public class MchServiceImpl implements MchService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

    @Autowired
    private BannerModelMapper bannerModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchClassModelMapper mchClassModelMapper;

    @Autowired
    private ProductImgModelMapper productImgModelMapper;

    @Autowired
    private StockModelMapper stockModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> mchMap = new HashMap<>(16);

            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setId(user.getMchId());
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null && StringUtils.isNotEmpty(mchModel.getSheng()) && StringUtils.isNotEmpty(mchModel.getShi()) && StringUtils.isNotEmpty(mchModel.getXian()))
            {
                mchModel.setHead_img(publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId()));
                mchModel.setPoster_img(publiceService.getImgPath(mchModel.getPoster_img(), vo.getStoreId()));
                mchMap = JSON.parseObject(JSON.toJSONString(mchModel), new TypeReference<Map<String, Object>>()
                {
                });
                mchMap.put("logo", publiceService.getImgPath(mchModel.getLogo(), vo.getStoreId()));
                Integer       cid           = MapUtils.getInteger(mchMap, "cid");
                MchClassModel mchClassModel = mchClassModelMapper.selectByPrimaryKey(cid);
                if (!Objects.isNull(mchClassModel))
                {
                    mchMap.put("className", mchClassModel.getName());
                }
                //营业状态
                String is_open = MapUtils.getString(mchMap, "is_open");
                //未营业
                if ("0".equals(is_open) || "2".equals(is_open))
                {
                    mchMap.put("is_open", is_open);
                }
                else if ("1".equals(is_open))
                {
                    String businessHoursValue = MapUtils.getString(mchMap, "business_hours");
                    if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                    {
                        mchMap.put("is_open", "2");
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
                                mchMap.put("is_open", "1");
                            }
                            else
                            {
                                //未营业
                                mchMap.put("is_open", "2");
                            }
                        }
                        else
                        {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                            if (!DateUtil.dateCompare(startTime, currentDate)
                                    && !DateUtil.dateCompare(currentDate, endTime))
                            {
                                mchMap.put("is_open", "1");
                            }
                            else
                            {
                                mchMap.put("is_open", "2");
                            }
                        }
                    }
                }
                else
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
                }

                AdminCgModel adminCgModel = new AdminCgModel();
                adminCgModel.setDistrictPid(0);
                adminCgModel.setDistrictName(mchModel.getSheng());
                AdminCgModel adminCg = adminCgModelMapper.selectOne(adminCgModel);
                if (adminCg != null)
                {
                    mchMap.put("sheng_id", adminCg.getId());
                    adminCgModel.setDistrictPid(adminCg.getId());
                    adminCgModel.setDistrictName(mchModel.getShi());
                    adminCg = adminCgModelMapper.selectOne(adminCgModel);
                    if (adminCg != null)
                    {
                        mchMap.put("shi_id", adminCg.getId());
                        adminCgModel.setDistrictPid(adminCg.getId());
                        adminCgModel.setDistrictName(mchModel.getXian());
                        adminCg = adminCgModelMapper.selectOne(adminCgModel);
                        if (adminCg != null)
                        {
                            mchMap.put("xian_id", adminCg.getId());
                        }
                    }
                }
                mchMap.put("user", user);
            }

            resultMap.put("res", mchMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺信息 异常  ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> setPassword(MainVo vo, String pwd, String pwdOld) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            MchModel mchModel = publicMchService.verificationMchExis(vo.getStoreId(), user.getUser_id(), user.getMchId());
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (StringUtils.isEmpty(pwd))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XMMBNWK, "新密码不能为空");
            }
            if (StringUtils.isEmpty(pwdOld))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMMBNWK, "原密码不能为空");
            }
            if (!pwdOld.equals(CryptoUtil.strDecode(user.getMima())))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YMMBZQ, "原密码不正确");
            }
            //修改当前用户登录密码
            User userUpdate = new User();
            userUpdate.setId(user.getId());
            userUpdate.setMima(CryptoUtil.strEncode(pwd));
            int row = userBaseMapper.updateByPrimaryKeySelective(userUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "修改了密码", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置店铺密码 异常  ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setPassword");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, Object> edit(AddMchVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            MchModel mchOld = new MchModel();
            mchOld.setStore_id(vo.getStoreId());
            mchOld.setId(user.getMchId());
            mchOld = mchModelMapper.selectOne(mchOld);
            if (mchOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在");
            }
            if (StringUtils.isEmpty(vo.getMchName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCBNWK, "店铺名称不能为空");
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
            if (StringUtils.isEmpty(vo.getLogo()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZ, "请选择logo");
            }
            if (Objects.isNull(vo.getCid()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择分类");
            }
            if (StringUtils.isEmpty(vo.getPosterImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "宣传图不能为空");
            }
            if (StringUtils.isEmpty(vo.getHeadImg()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "头像不能为空");
            }
            MchModel mchModelUpdate = new MchModel();
            if (StringUtils.isEmpty(vo.getIsOpen()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请输入营业状态");
            }
            mchModelUpdate.setIs_open(vo.getIsOpen().toString());
            //营业中-有营业时间
            if (vo.getIsOpen().equals(1))
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
                mchModelUpdate.setBusiness_hours(vo.getBusinessHours());
            }
            mchModelUpdate.setId(mchOld.getId());
            mchModelUpdate.setTel(vo.getTel());
            mchModelUpdate.setShi(vo.getShi());
            mchModelUpdate.setXian(vo.getXian());
            mchModelUpdate.setCpc(vo.getCpc());
            mchModelUpdate.setSheng(vo.getShen());
            mchModelUpdate.setAddress(vo.getAddress());
            mchModelUpdate.setShop_range(vo.getConfines());
            mchModelUpdate.setShop_information(vo.getMchInfo());
            mchModelUpdate.setLogo(ImgUploadUtils.getUrlImgByName(vo.getLogo(), true));
            mchModelUpdate.setPoster_img(ImgUploadUtils.getUrlImgByName(vo.getPosterImg(), true));
            mchModelUpdate.setHead_img(ImgUploadUtils.getUrlImgByName(vo.getHeadImg(), true));
            mchModelUpdate.setName(vo.getMchName());
            mchModelUpdate.setIs_invoice(vo.getIsInvoice());
            mchModelUpdate.setCid(vo.getCid());
            mchModelUpdate.setIs_self_delivery(vo.getIs_self_delivery());
            //校验店铺名称
            if (!mchOld.getName().equals(vo.getMchName()))
            {
                MchModel mchModel = new MchModel();
                mchModel.setStore_id(vo.getStoreId());
                mchModel.setName(vo.getMchName());
                mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                int count = mchModelMapper.selectCount(mchModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPMCYCZ, "店铺名称已存在");
                }
            }

            if (vo.getRoomid() != null && vo.getRoomid() > 0)
            {
                mchModelUpdate.setRoomid(vo.getRoomid());
                mchModelUpdate.setOld_roomid(mchOld.getRoomid());
            }
            if (!StringUtils.isEmpty(vo.getNature()))
            {
                mchModelUpdate.setShop_nature(vo.getNature());
            }
            DataCheckTool.checkMchDataFormate(mchModelUpdate);
            mchModelUpdate.setShop_range(vo.getConfines());
            int count = mchModelMapper.updateByPrimaryKeySelective(mchModelUpdate);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }

            //修改用户的喜好币种
            CurrencyStoreVo currencyStoreVo = new CurrencyStoreVo();
            currencyStoreVo.setCurrency_id(vo.getPreferred_currency());
            currencyStoreVo.setAccessId(vo.getAccessId());
            publiceService.changeCurrency(currencyStoreVo);

            //更新缓存中的用户
            user.setPreferred_currency(vo.getPreferred_currency());
            RedisDataTool.refreshRedisUserCache(vo.getAccessId(), user, LOGIN_ACCESS_MCH_TOKEN, redisUtil);

            publiceService.addAdminRecord(vo.getStoreId(), "修改了店铺设置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑店铺 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "edit");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bannerList(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            if (id != null)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("review_status", DictionaryConst.MchExameStatus.EXAME_PASS_STATUS);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = bannerModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                String imagUrl = String.valueOf(map.get("image"));
                map.put("image", publiceService.getImgPath(imagUrl, vo.getStoreId()));
            }
            int total = bannerModelMapper.countDynamic(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取轮播图列表 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> bannerPathList(MainVo vo, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mchId", user.getMchId());
            parmaMap.put("type0", type);
            parmaMap.put("status", 1);
            parmaMap.put("type", 1);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> list = jumpPathModelMapper.selectDynamic(parmaMap);

            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("轮播图路径分类 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bannerPathList");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBannerInfo(AddBannerInfoVo vo, Integer type) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            int  count;
            if (StringUtils.isEmpty(vo.getImageUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBNWK, "轮播图不能为空");
            }
            if (StringUtils.isEmpty(vo.getPath()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTTZLJBNWK, "轮播图跳转路径不能为空");
            }
            BannerModel bannerModelOld = null;
            if (vo.getId() != null && vo.getId() > 0)
            {
                bannerModelOld = bannerModelMapper.selectByPrimaryKey(vo.getId());
            }

            BannerModel bannerModelSave = new BannerModel();
            // &shop_id 设置指定店铺查询商品
            if (type == 1)
            {
                vo.setPath(vo.getPath() + "&shop_id=" + user.getMchId());
            }
            bannerModelSave.setUrl(vo.getPath());
            bannerModelSave.setImage(ImgUploadUtils.getUrlImgByName(vo.getImageUrl(), true));
            bannerModelSave.setType(String.valueOf(type));
            if (bannerModelOld != null)
            {
                bannerModelSave.setId(bannerModelOld.getId());
                count = bannerModelMapper.updateByPrimaryKeySelective(bannerModelSave);

                publiceService.addAdminRecord(vo.getStoreId(), "修改了轮播图ID：" + vo.getId(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                bannerModelSave.setSort(bannerModelMapper.getMaxSort(vo.getStoreId()));
                bannerModelSave.setMch_id(user.getMchId());
                bannerModelSave.setStore_id(vo.getStoreId());
                bannerModelSave.setAdd_date(new Date());
                count = bannerModelMapper.insertSelective(bannerModelSave);

                publiceService.addAdminRecord(vo.getStoreId(), "添加了轮播图ID：" + bannerModelSave.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }

            if (count < 1)
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
            logger.error("添加/编辑轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBannerInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBannerSort(MainVo vo, Integer id, Integer sort) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            int  row;
            if (id == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBNWK, "轮播图id不能为空");
            }
            if (sort == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTXHBNWK, "轮播图序号不能为空");
            }
            BannerModel bannerModelOld = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerModelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            BannerModel bannerUpdate = new BannerModel();
            bannerUpdate.setId(id);
            bannerUpdate.setSort(sort);
            row = bannerModelMapper.updateByPrimaryKeySelective(bannerUpdate);
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
            logger.error("添加/编辑轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addBannerInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBannerById(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            BannerModel bannerModel = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            if (bannerModelMapper.deleteByPrimaryKey(id) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了轮播图ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBannerById");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topBannerById(int id) throws LaiKeAPIException
    {
        try
        {
            BannerModel bannerModel = bannerModelMapper.selectByPrimaryKey(id);
            if (bannerModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBTBCZ, "轮播图不存在");
            }
            BannerModel bannerModelUpdate = new BannerModel();
            bannerModelUpdate.setId(id);
            bannerModelUpdate.setSort(bannerModelMapper.getMaxSort(bannerModel.getStore_id()));

            if (bannerModelMapper.updateByPrimaryKeySelective(bannerModelUpdate) < 1)
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
            logger.error("置顶轮播图 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "topBannerById");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delMchInfo(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            //注销店铺
            return publicMchService.cancellationShop(vo.getStoreId(), user.getMchId());
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
    public Map<String, Object> getMchConfig(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            MchConfigModel mchConfigModel    = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());
            String[]       uploadGoodsStatus = new String[]{};
            if (mchConfigModel != null)
            {
                uploadGoodsStatus = mchConfigModel.getCommodity_setup().split(SplitUtils.DH);
            }
            resultMap.put("uploadGoodsStatus", uploadGoodsStatus);
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

        return resultMap;
    }

    @Override
    public Map<String, Object> mchClassList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
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
            resultMap.put("total", i);
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
    public Map<String, Object> getMchPrintSetup(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            //订单打印配置
            PrintSetupModel printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(vo.getStoreId());
            printSetupModel.setMchId(user.getMchId());
            printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
            if (StringUtils.isEmpty(printSetupModel))
            {
                printSetupModel = new PrintSetupModel();
            }
            resultMap.put("printSetupConfig", printSetupModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取订单打印配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchPrintSetup");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMchPrintSetup(MchPrintSetupVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            int  count;
            //订单打印配置
            PrintSetupModel printSetupModel = new PrintSetupModel();
            printSetupModel.setStoreId(vo.getStoreId());
            printSetupModel.setMchId(user.getMchId());
            printSetupModel = printSetupModelMapper.selectOne(printSetupModel);
            if (printSetupModel != null)
            {
                printSetupModel.setSheng(vo.getSheng());
                printSetupModel.setShi(vo.getShi());
                printSetupModel.setXian(vo.getXian());
                printSetupModel.setAddress(vo.getAddress());
                printSetupModel.setPrintName(vo.getPrintName());
                printSetupModel.setPrintUrl(vo.getPrintUrl());
                printSetupModel.setPhone(vo.getPhone());
                count = printSetupModelMapper.updateByPrimaryKeySelective(printSetupModel);
            }
            else
            {
                printSetupModel = new PrintSetupModel();
                printSetupModel.setStoreId(vo.getStoreId());
                //管理后台店铺id为0
                printSetupModel.setMchId(user.getMchId());
                printSetupModel.setSheng(vo.getSheng());
                printSetupModel.setShi(vo.getShi());
                printSetupModel.setXian(vo.getXian());
                printSetupModel.setAddress(vo.getAddress());
                printSetupModel.setPrintName(vo.getPrintName());
                printSetupModel.setPrintUrl(vo.getPrintUrl());
                printSetupModel.setPhone(vo.getPhone());
                printSetupModel.setAddTime(new Date());
                count = printSetupModelMapper.insert(printSetupModel);
            }
            publiceService.addAdminRecord(vo.getStoreId(), "修改了打印配置信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            if (count < 1)
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
            logger.error("上传订单打印配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchPrintSetup");
        }
    }

    @Override
    public Map<String, Object> mchIndex(MainVo vo, String startTime, String endTime) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, LOGIN_ACCESS_MCH_TOKEN, true);
            Integer mchId = 0;
            if (user != null)
            {
                mchId = user.getMchId();
            }
            if (mchId == 0)
            {
                return resultMap;
            }
            //获取店铺信息
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);

            //获取店铺汇总信息
            MchStatisticsModel mchStatisticsModel = new MchStatisticsModel();
            mchStatisticsModel.setMch_id(mchId);
            mchStatisticsModel.setStore_id(vo.getStoreId());
            mchStatisticsModel.setRecycle(0);
            mchStatisticsModel = mchStatisticsModelMapper.selectOne(mchStatisticsModel);

            //库存预警数量
            Map<String,Object> stockParamMap = new HashMap<>();
            stockParamMap.put("store_id",vo.getStoreId());
            stockParamMap.put("mch_id",user.getMchId());
            stockParamMap.put("stockType",2);
            stockParamMap.put("group_attr_id",1);
            //库存大于预警库存不显示
            stockParamMap.put("ltNum","ltNum");
            //预售商品不显示
            stockParamMap.put("is_presell","is_presell");
            Integer stockLackNum = stockModelMapper.goodsStockInfoCountDynamic(stockParamMap);
            if (Objects.nonNull(mchStatisticsModel))
            {
                mchStatisticsModel.setCkbz_pro(stockLackNum);
            }else
            {
                mchStatisticsModel = new MchStatisticsModel();
                mchStatisticsModel.setYtx_money(BigDecimal.ZERO);
                mchStatisticsModel.setReturn_money(BigDecimal.ZERO);
                mchStatisticsModel.setAccess_user_num(0);
                mchStatisticsModel.setAttention_user_num(0);
                mchStatisticsModel.setNew_pay_user(0);
            }

            Map<String, Object> mchMap = new HashMap<>();
            mchMap.put("mchName", mchModel.getName());
            mchMap.put("tel", mchModel.getTel());
            mchMap.put("cpc", mchModel.getCpc());

            mchMap.put("img", publiceService.getImgPath(mchModel.getHead_img(), vo.getStoreId()));
            //营业状态
            String is_open = mchModel.getIs_open();
            //未营业
            if ("0".equals(is_open) || "2".equals(is_open))
            {
                mchMap.put("is_open", is_open);
            }
            else if ("1".equals(is_open))
            {
                String businessHoursValue = mchModel.getBusiness_hours();
                if (StringUtils.isEmpty(businessHoursValue) || !businessHoursValue.contains(SplitUtils.BL))
                {
                    mchMap.put("is_open", "2");
                }
                else
                {
                    //营业时间判断是否营业
                    String[] businessHours = businessHoursValue.split(SplitUtils.BL);
                    //开始时间
                    Date startDate = DateUtil.dateFormateToDate(businessHours[0], GloabConst.TimePattern.HM);
                    //结束时间
                    Date endDate = DateUtil.dateFormateToDate(businessHours[1], GloabConst.TimePattern.HM);
                    //当前时间
                    Date currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.HM);
                    //开始时间大于结束时间(跨天：例如 19：00~04：00 则当前时间 >= 19:00 || 当前时间 <= 04:00 -> 营业 )
                    if (DateUtil.dateCompare(startDate, endDate))
                    {
                        if (!DateUtil.dateCompare(startDate, currentDate)
                                || !DateUtil.dateCompare(currentDate, endDate))
                        {
                            mchMap.put("is_open", "1");
                        }
                        else
                        {
                            //未营业
                            mchMap.put("is_open", "2");
                        }
                    }
                    else
                    {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                        if (!DateUtil.dateCompare(startDate, currentDate)
                                && !DateUtil.dateCompare(currentDate, endDate))
                        {
                            mchMap.put("is_open", "1");
                        }
                        else
                        {
                            mchMap.put("is_open", "2");
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "店铺未设置营业状态");
            }
            mchMap.put("business_hours", mchModel.getBusiness_hours());
            mchMap.put("address",mchModel.getSheng() + mchModel.getShi() + mchModel.getXian() + mchModel.getAddress());
            mchMap.put("sy_money", String.format("%.2f", mchModel.getCashable_money()));
            mchMap.put("djs_money", String.format("%.2f", mchModel.getAccount_money()));
            mchMap.put("ytx_money", String.format("%.2f", mchStatisticsModel.getYtx_money()));
            mchMap.put("return_money", String.format("%.2f", mchStatisticsModel.getReturn_money()));
            mchStatisticsModel.setCustomer_num(mchStatisticsModel.getAccess_user_num() + mchStatisticsModel.getAttention_user_num() + mchStatisticsModel.getNew_pay_user());
            //店铺信息
            resultMap.put("mchMap", mchMap);
            //数据统计
            resultMap.put("mchData", mchStatisticsModel);

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("storeId", vo.getStoreId());
            paramMap.put("mchId", mchId);


            //获取店铺客单数据
            List<Map<String, Object>> mchBuyPowerList = mchModelMapper.getMchBuyPower(paramMap);
            resultMap.put("mchBuyPowerList", mchBuyPowerList);
            //获取店铺交易数据
            if (StringUtils.isNotEmpty(startTime))
            {
                paramMap.put("startTime", startTime);
            }
            if (StringUtils.isNotEmpty(endTime))
            {
                paramMap.put("startTime", endTime);
            }
//            List<Map<String, Object>> mchRecordList = mchModelMapper.getMchOrderRecordByDate(paramMap);

            Date       currentDate = DateUtil.dateFormateToDate(new Date(), GloabConst.TimePattern.YMD);
            List<Date> dateList    = new ArrayList<>();
            dateList.add(DateUtil.getAddDate(currentDate, -7));
            dateList.add(DateUtil.getAddDateByMonth(currentDate, -1));
            dateList.add(DateUtil.getAddDateByYear(currentDate, -1));
            List<List> resultList = new ArrayList<>();
            for (int i = 0; i < dateList.size(); i++)
            {
                List<String>     daystr     = new ArrayList<>();
                List<List>       dataList   = new ArrayList<>();
                List<BigDecimal> moneyList  = new ArrayList<>();
                List<BigDecimal> numberList = new ArrayList<>();
                if (i < 2)
                {
                    List<Date> days = DateUtil.createDays(dateList.get(i));
                    for (Date day : days)
                    {
                        String paramday = DateUtil.dateFormate(day, GloabConst.TimePattern.YMD);
                        daystr.add(paramday);
                        paramMap.put("date", paramday);
                        paramMap.put("index", i);
                        BigDecimal money = mchModelMapper.getMchOrderRecordByDateMoney(paramMap);
                        moneyList.add(money);
                        BigDecimal number = mchModelMapper.getMchOrderRecordByDateNumber(paramMap);
                        numberList.add(number);
                    }
                    dataList.add(daystr);
                    dataList.add(moneyList);
                    dataList.add(numberList);
                }
                else
                {
                    List<Date> months = DateUtil.createMonths(dateList.get(i));

                    for (Date month : months)
                    {
                        String paramday = DateUtil.dateFormate(month, GloabConst.TimePattern.YM);
                        daystr.add(paramday);
                        paramMap.put("date", paramday);
                        paramMap.put("index", i);
                        BigDecimal money = mchModelMapper.getMchOrderRecordByDateMoney(paramMap);
                        moneyList.add(money);
                        BigDecimal number = mchModelMapper.getMchOrderRecordByDateNumber(paramMap);
                        numberList.add(number);
                    }
                    dataList.add(daystr);
                    dataList.add(moneyList);
                    dataList.add(numberList);
                }
                resultList.add(dataList);
            }
            resultMap.put("mchRecordList", resultList);
            //获取商品信息
            List<Map<String, Object>> proList = mchModelMapper.getProList(paramMap);
            if (proList.size() > 0)
            {
                for (Map<String, Object> map : proList)
                {
                    if (MapUtils.getIntValue(map, "volume") >= 10000)
                    {
                        map.put("volume", MapUtils.getIntValue(map, "volume") / 10000 + "万");
                    }
                    map.put("imgurl", publiceService.getImgPath(productImgModelMapper.getProductImg(MapUtils.getIntValue(map, "pid")), vo.getStoreId()));
                }
            }
            resultMap.put("proList", proList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("首页信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchIndex");
        }
        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void statisticsMch(MainVo vo)
    {
        List<Integer> storeIdList = null;
        try
        {
            //获取所有的店铺id
//            storeIdList.add(0,vo.getStoreId());
            Integer storeId = vo.getStoreId();
            //根据商城id获取当前商城的所有店铺id
//            for (Integer storeId : storeIdList) {
            MchModel mchModel = new MchModel();
            mchModel.setRecovery(0);
            mchModel.setReview_status("1");
            mchModel.setStore_id(storeId);
            List<MchModel> mchList = mchModelMapper.select(mchModel);
            //遍历店铺id
            for (MchModel model : mchList)
            {

                Integer mchId = model.getId();
                //查看店铺是否有记录，有就修改，没有就新增
                MchStatisticsModel mchStatisticsModel = new MchStatisticsModel();
                mchStatisticsModel.setStore_id(storeId);
                mchStatisticsModel.setMch_id(mchId);
                mchStatisticsModel.setRecycle(0);
                mchStatisticsModel = mchStatisticsModelMapper.selectOne(mchStatisticsModel);
                //获取店铺下所有的商品信息
                Integer sjNum      = 0;
                Integer xjNum      = 0;
                Integer dshProNum  = 0;
                Integer classNum   = 0;
                Integer brandNum   = 0;
                Integer saleSkuNum = 0;
                Integer skuNum     = 0;
                Integer kcbzNum    = 0;
                //上架数
                sjNum = productListModelMapper.getNumByMchIdAndStatus(storeId, mchId, 2);
                //下架数
                xjNum = productListModelMapper.getNumByMchIdAndStatus(storeId, mchId, 3);
                //待审核商品数
                dshProNum = productListModelMapper.getDshNumByMchId(storeId, mchId);
                //商品分类数
                classNum = productListModelMapper.getClassNumByMchId(storeId, mchId);
                //商品品牌数
                brandNum = productListModelMapper.getBrandNumByMchId(storeId, mchId);
                //销售商品sku数量
                saleSkuNum = productListModelMapper.getSaleSkuNumByMchId(storeId, mchId);
                //sku数量
                skuNum = productListModelMapper.getSkuNumByMchId(storeId, mchId);
                //库存不足的商品
                kcbzNum = productListModelMapper.getKcbzNumByMchId(storeId, mchId);

                //获取店铺下所有的订单信息
                Integer dfhOrderNum = 0;
                Integer dfkOrderNum = 0;
                Integer tkOrderNum  = 0;
                Integer dshOrderNum = 0;
                Integer djsOrderNum = 0;
                Integer shOrderNum  = 0;
                //待发货订单
                dfhOrderNum = orderDetailsModelMapper.getDfhOrderNum(storeId, mchId);
                //待付款订单
                dfkOrderNum = orderDetailsModelMapper.getDfkOrderNum(storeId, mchId);
                //待收货订单
                shOrderNum = orderDetailsModelMapper.getShOrderNum(storeId, mchId);
                //退款数量
                tkOrderNum = orderDetailsModelMapper.getTkOrderNum(storeId, mchId);
                //退款-待审核订单
                dshOrderNum = orderDetailsModelMapper.getDshOrderNum(storeId, mchId);
                //待结算订单
                djsOrderNum = orderDetailsModelMapper.getDjsOrderNum(storeId, mchId);

                //获取店铺信息
                BigDecimal zhye  = new BigDecimal(0);
                BigDecimal djsje = new BigDecimal(0);
                BigDecimal ytxje = new BigDecimal(0);
                BigDecimal tkje  = new BigDecimal(0);

                //账户余额
                zhye = mchModel.getAccount_money();
                //待结算金额
//                    if (StringUtils.isNotEmpty(orderDetailsModelMapper.getDjsJeOrder(storeId, mchId))){
//                        djsje = orderDetailsModelMapper.getDjsJeOrder(storeId, mchId);
//                    }
                djsje = model.getAccount_money();
                //已提现金额
                ytxje = orderDetailsModelMapper.getTxJe(storeId, mchId);
                //退款金额
                tkje = orderDetailsModelMapper.getTkJe(storeId, mchId);
                //获取客户客单数据
                Integer zkd         = 0;
                Integer gzKh        = 0;
                Integer fwKh        = 0;
                Integer newOrderNum = 0;
                //总客单
                zkd = orderDetailsModelMapper.getZkdOrderNum(storeId, mchId);
                //关注客户
                gzKh = userCollectionModelMapper.getGzKhNum(storeId, mchId);
                //访问客户
                fwKh = mchBrowseModelMapper.getFwKhNum(storeId, mchId);
                //准备参数
                MchStatisticsModel saveMchStatisticsModel = new MchStatisticsModel();
                saveMchStatisticsModel.setAudit_order(dshOrderNum);
                saveMchStatisticsModel.setAudit_pro(dshProNum);
                saveMchStatisticsModel.setStore_id(storeId);
                saveMchStatisticsModel.setMch_id(mchId);
                saveMchStatisticsModel.setAccess_user_num(fwKh);
                saveMchStatisticsModel.setAttention_user_num(gzKh);
                saveMchStatisticsModel.setCkbz_pro(kcbzNum);
                saveMchStatisticsModel.setCustomer_num(zkd);
                saveMchStatisticsModel.setDjs_money(djsje);
                saveMchStatisticsModel.setDjs_order(djsOrderNum);
                saveMchStatisticsModel.setDsh_order(shOrderNum);
                saveMchStatisticsModel.setAdd_date(new Date());
                saveMchStatisticsModel.setObligation(dfkOrderNum);
                saveMchStatisticsModel.setPending_shipment(dfhOrderNum);
                saveMchStatisticsModel.setPro_brand(brandNum);
                saveMchStatisticsModel.setPro_class(classNum);
                saveMchStatisticsModel.setPro_sku(skuNum);
                saveMchStatisticsModel.setRefund_order(tkOrderNum);
                saveMchStatisticsModel.setReturn_money(tkje);
                saveMchStatisticsModel.setSale_pro_sku(saleSkuNum);
                saveMchStatisticsModel.setSj_pro(sjNum);
                saveMchStatisticsModel.setXj_pro(xjNum);
                saveMchStatisticsModel.setYtx_money(ytxje);

                if (mchStatisticsModel != null)
                {
                    saveMchStatisticsModel.setId(mchStatisticsModel.getId());
                    //修改
                    mchStatisticsModelMapper.updateByPrimaryKeySelective(saveMchStatisticsModel);
                }
                else
                {
                    //新增
                    mchStatisticsModelMapper.insertSelective(saveMchStatisticsModel);
                }

                //统计客户购买力表
                //先获取所有的客户
                List<Map<String, Object>> allUserList = orderDetailsModelMapper.getAllUserByMchId(storeId, mchId);
                if (allUserList.size() > 0)
                {
                    for (Map<String, Object> map : allUserList)
                    {
                        //获取客户下单的所有金额
                        BigDecimal allMoney = new BigDecimal(0);
                        String     userId   = MapUtils.getString(map, "user_id");
                        allMoney = orderDetailsModelMapper.getAllUserMoneyByUserId(storeId, mchId, userId);
                        //获取客户购买力表是否有记录
                        MchBuyPowerModel mchBuyPowerModel = new MchBuyPowerModel();
                        mchBuyPowerModel.setStore_id(storeId);
                        mchBuyPowerModel.setMch_id(mchId);
                        mchBuyPowerModel.setUser_id(userId);
                        mchBuyPowerModel.setRecycle(0);
                        mchBuyPowerModel = mchBuyPowerModelMapper.selectOne(mchBuyPowerModel);
                        //准备参数
                        MchBuyPowerModel saveMchBuyPowerModel = new MchBuyPowerModel();
                        saveMchBuyPowerModel.setRecycle(0);
                        saveMchBuyPowerModel.setAdd_date(new Date());
                        saveMchBuyPowerModel.setMoney(allMoney);
                        saveMchBuyPowerModel.setStore_id(storeId);
                        saveMchBuyPowerModel.setMch_id(mchId);
                        saveMchBuyPowerModel.setUser_id(userId);
                        if (mchBuyPowerModel != null)
                        {
                            //修改
                            saveMchBuyPowerModel.setId(mchBuyPowerModel.getId());
                            mchBuyPowerModelMapper.updateByPrimaryKeySelective(saveMchBuyPowerModel);
                        }
                        else
                        {
                            //新增
                            mchBuyPowerModelMapper.insertSelective(saveMchBuyPowerModel);
                        }
                    }
                }
                //统计订单表-按天
                //获取所有下单的日期
                List<Map<String, Object>> allDateList = orderDetailsModelMapper.getAllDateByMchId(storeId, mchId);
                if (allDateList.size() > 0)
                {
                    for (Map<String, Object> map : allDateList)
                    {
                        String     date       = MapUtils.getString(map, "add_time");
                        BigDecimal orderMoney = new BigDecimal(0);
                        Integer    orderNum   = 0;
                        orderMoney = orderDetailsModelMapper.getMoneyByMchId(storeId, mchId, date);
                        orderNum = orderDetailsModelMapper.getNumByMchId(storeId, mchId, date);
                        MchOrderRecordModel mchOrderRecordModel = new MchOrderRecordModel();
                        mchOrderRecordModel.setMch_id(mchId);
                        mchOrderRecordModel.setStore_id(storeId);
                        mchOrderRecordModel.setRecycle(0);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        Date count_day = sdf.parse(date);
                        mchOrderRecordModel.setCount_day(count_day);
                        mchOrderRecordModel = mchOrderRecordModelMapper.selectOne(mchOrderRecordModel);

                        MchOrderRecordModel saveMchOrderRecordModel = new MchOrderRecordModel();
                        saveMchOrderRecordModel.setCount_day(count_day);
                        saveMchOrderRecordModel.setOrder_num(orderNum);
                        saveMchOrderRecordModel.setMoney(orderMoney);
                        saveMchOrderRecordModel.setRecycle(0);
                        saveMchOrderRecordModel.setAdd_date(new Date());
                        saveMchOrderRecordModel.setStore_id(storeId);
                        saveMchOrderRecordModel.setMch_id(mchId);
                        if (mchOrderRecordModel != null)
                        {
                            saveMchOrderRecordModel.setId(mchOrderRecordModel.getId());
                            mchOrderRecordModelMapper.updateByPrimaryKeySelective(mchOrderRecordModel);
                        }
                        else
                        {
                            mchOrderRecordModelMapper.insertSelective(saveMchOrderRecordModel);
                        }
                    }
                }

            }
//            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "处理统计店铺定时任务 异常", "statisticsMch");
        }
        finally
        {

        }
    }

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private PrintSetupModelMapper printSetupModelMapper;

    @Autowired
    private MchOrderRecordModelMapper mchOrderRecordModelMapper;

    @Autowired
    private MchStatisticsModelMapper mchStatisticsModelMapper;

    @Autowired
    private MchBuyPowerModelMapper mchBuyPowerModelMapper;

    @Autowired
    private OrderDetailsModelMapper orderDetailsModelMapper;

    @Autowired
    private UserCollectionModelMapper userCollectionModelMapper;

    @Autowired
    private MchBrowseModelMapper mchBrowseModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;
}
