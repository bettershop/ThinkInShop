package com.laiketui.common.service.dubbo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.*;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.JumpPathModel;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.log.MchAccountLogModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.order.OrderDataModel;
import com.laiketui.domain.order.OrderModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.PageModel;
import com.laiketui.domain.vo.freight.DefaultFreightVO;
import com.laiketui.domain.vo.freight.FreightRuleVO;
import com.laiketui.domain.vo.mch.AddFreihtVo;
import com.laiketui.root.common.BuilderIDTool;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.*;

/**
 * 关于店铺
 *
 * @author Trick
 * @date 2020/11/13 9:11
 */
@Service
public class PublicMchServiceImpl implements PublicMchService
{

    private final Logger logger = LoggerFactory.getLogger(PublicMchServiceImpl.class);

    @Autowired
    private UserAuthorityModelMapper userAuthorityModelMapper;

    @Override
    public MchModel verificationMchExits(int storeId, String userId, Integer shopId) throws LaiKeAPIException
    {
        try
        {
            if (shopId == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ILLEGAL_INVASION, "非法入侵");
            }
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setUser_id(userId);
            mchModel.setReview_status(DictionaryConst.MchExameStatus.EXAME_PASS_STATUS.toString());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel != null)
            {
                if (!mchModel.getId().equals(shopId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "verificationMchExits");
                }
                return mchModel;
            }
            else
            {
                //没有则查看是否有权限
                String mchId = userAuthorityModelMapper.getMchIdByUserId(userId);
                if (mchId == null || !mchId.equals(shopId.toString()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "verificationMchExits");
                }
                return mchModelMapper.selectByPrimaryKey(mchId);
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "verificationMchExits");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("验证店铺是否存在 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationMchExits");
        }
    }

    @Override
    public MchModel verificationMchExis(int storeId, String userId, Integer shopId) throws LaiKeAPIException
    {
        return verificationMchExits(storeId, userId, shopId);
    }

    @Override
    public Map<String, Object> settlement(int storeId, Integer shopId, String res, int shopAddressId, int storeType) throws LaiKeAPIException
    {

        Map<String, Object> resultMap     = new HashMap<>(16);
        MchStoreModel       mchStoreModel = new MchStoreModel();
        try
        {
            int    shopStatus        = 0;
            String sheng             = "";
            String shi               = "";
            String xian              = "";
            String address           = "";
            String extractionCode    = "";
            String extractionCodeImg = "";
            String cpc = "";
            if (shopId != 0)
            {
                mchStoreModel.setStore_id(storeId);
                mchStoreModel.setMch_id(shopId);
                if (shopAddressId != 0)
                {
                    mchStoreModel.setId(shopAddressId);
                }
                else
                {
                    mchStoreModel.setIs_default(1);
                    mchStoreModel = mchStoreModelMapper.selectOne(mchStoreModel);
                    if (mchStoreModel == null)
                    {
                        mchStoreModel = new MchStoreModel();
                        mchStoreModel.setStore_id(storeId);
                        mchStoreModel.setMch_id(shopId);
                        mchStoreModel.setIs_default(null);
                    }
                }
                List<MchStoreModel> mchStoreModels = mchStoreModelMapper.select(mchStoreModel);
                if (mchStoreModels != null && mchStoreModels.size() > 0)
                {
                    mchStoreModel = mchStoreModels.get(0);
                    sheng = mchStoreModel.getSheng();
                    shi = mchStoreModel.getShi();
                    xian = mchStoreModel.getXian();
                    address = mchStoreModel.getAddress();
                    shopStatus = 1;
                    cpc = mchStoreModel.getCpc();
                }
                else
                {
                    shopStatus = 0;
                }
            }
            //
            String flag = "payment";
            if (StringUtils.isNotEmpty(res) && flag.equals(res))
            {
                extractionCode = extractionCode();
                if (StringUtils.isNotEmpty(extractionCode))
                {
                    extractionCodeImg = createQRCodeImg(extractionCode, storeId, storeType);
                }
            }
            resultMap.put("cpc",cpc);
            resultMap.put("extraction_code_img", extractionCodeImg);
            resultMap.put("extraction_code", extractionCode);
            resultMap.put("shop_status", shopStatus);
            resultMap.put("sheng", sheng);
            resultMap.put("shi", shi);
            resultMap.put("xian", xian);
            resultMap.put("address", address);
            //门店信息
            resultMap.put("mch_store_info", mchStoreModel);

        }
        catch (Exception e)
        {
            logger.error("自提结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "settlement");
        }
        return resultMap;
    }

    @Override
    public void clientConfirmReceipt(int storeId, int shopId, String orderno, BigDecimal paymentMoney, BigDecimal allow) throws LaiKeAPIException
    {
        try
        {
            int row = orderModelMapper.isSettement(storeId, orderno);
            if (row < 1)
            {
                logger.error("买家确认收货,增加卖家收入失败,订单已结算 订单号:" + orderno);
                return;
            }
            if (orderno.contains(DictionaryConst.OrdersType.ORDERS_HEADER_JP))
            {
                logger.error("竞拍订单不予结算 订单号:" + orderno);
                return;
            }

            MchAccountLogModel queryDto = new MchAccountLogModel();
            queryDto.setMch_id(String.valueOf(shopId));
            queryDto.setAccount_money(paymentMoney);
            queryDto.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER);
            queryDto.setRemake(orderno);
            queryDto = mchAccountLogModelMapper.selectOne(queryDto);
            logger.info("确认收货订单【" + orderno + "】，店铺【" + shopId + "】入账记录，标记值={}", (queryDto != null ? "已入账" : "未入账"));
            if (queryDto != null)
            {
                logger.error("此订单金额已入账，不予重复入账 订单号:" + orderno);
                return;
            }
            //入到店铺的账户
            int count = mchModelMapper.clientConfirmReceipt(shopId, paymentMoney, allow);
            if (count < 1)
            {
                logger.error("买家确认收货,增加卖家收入失败 订单号:" + orderno);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常");
            }
            //获取增加收入后的店铺信息
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setId(shopId);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据", "clientConfirmReceipt");
            }
            //记录店铺收支信息
            MchAccountLogModel mchAccountLogModel = new MchAccountLogModel();
            mchAccountLogModel.setStore_id(mchModel.getStore_id());
            mchAccountLogModel.setMch_id(mchModel.getId().toString());
            mchAccountLogModel.setRemake(orderno);
            mchAccountLogModel.setPrice(paymentMoney);
            mchAccountLogModel.setIntegral(mchModel.getIntegral_money().intValue());
            mchAccountLogModel.setAccount_money(mchModel.getAccount_money());
            mchAccountLogModel.setType(DictionaryConst.MchAccountLogType.MCHACCOUNTLOG_TYPE_ORDER);
            mchAccountLogModel.setAddtime(new Date());
            count = mchAccountLogModelMapper.insertSelective(mchAccountLogModel);
            if (count < 1)
            {
                logger.error("记录店铺收支信息失败 参数:" + JSON.toJSONString(mchAccountLogModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "clientConfirmReceipt");
            }
            logger.info("订单：{}确认收货，店铺:{} ，入账金额：{}。积分：{}", orderno, shopId, paymentMoney, allow);
        }
        catch (LaiKeAPIException l)
        {
            logger.error("确认收货予店铺结算 失败", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("确认收货予店铺结算 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "clientConfirmReceipt");
        }
    }

    @Override
    public Map<String, Object> freightList(int storeId, int mchId, String name, Integer isUse, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费数量
            int total;
            //运费信息集
            List<Map<String, Object>> freightList;

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", storeId);
            parmaMap.put("mch_id", mchId);
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("pageNo", pageModel.getPageNo());
            parmaMap.put("pageSize", pageModel.getPageSize());
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("likeName", name);
            }
            if (isUse != null)
            {
                if (isUse == 1)
                {
                    parmaMap.put("isNotNullId", "isNotNullId");
                }
                else if (isUse == 0)
                {
                    parmaMap.put("isNullId", "isNullId");
                }
            }
            total = freightModelMapper.countFreightInfoLeftGoodsDynamic(parmaMap);
            freightList = freightModelMapper.getFreightInfoLeftGoodsDynamic(parmaMap);
            for (Map<String, Object> map : freightList)
            {
                //运费规则
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "freight")))
                {
                    List<FreightRuleVO> freightRuleVOS = JSON.parseArray(MapUtils.getString(map, "freight"), FreightRuleVO.class);
                    map.put("freight", freightRuleVOS);
                }
                //不配送区域
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "no_delivery")))
                {
                    JSONArray objects = JSONArray.parseArray(MapUtils.getString(map, "no_delivery"));
                    map.put("no_delivery", objects);
                }
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                //包邮设置
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "package_settings")))
                {
                    resultMap.put("package_settings", MapUtils.getString(map, "package_settings"));
                }
                else
                {
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                String defaultFreight = MapUtils.getString(map, "default_freight");
                if (StringUtils.isNotEmpty(defaultFreight))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                    map.put("default_freight", defaultFreightVO);
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", freightList);
            resultMap.put("start", pageModel.getPageNum());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> freightList(MainVo vo, int mchId, String name, Integer isUse, PageModel pageModel) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费数量
            int total;
            //运费信息集
            List<Map<String, Object>> freightList;

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("mch_id", mchId);
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("add_time_sort", DataUtils.Sort.ASC.toString());
            parmaMap.put("pageNo", pageModel.getPageNo());
            parmaMap.put("pageSize", pageModel.getPageSize());

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                parmaMap.put("lang_code", language);
//            }

            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("likeName", name);
            }
            if (isUse != null)
            {
                if (isUse == 1)
                {
                    parmaMap.put("isNotNullId", "isNotNullId");
                }
                else if (isUse == 0)
                {
                    parmaMap.put("isNullId", "isNullId");
                }
            }
            total = freightModelMapper.countFreightInfoLeftGoodsDynamic(parmaMap);
            freightList = freightModelMapper.getFreightInfoLeftGoodsDynamic(parmaMap);
            for (Map<String, Object> map : freightList)
            {
                //运费规则
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "freight")))
                {
                    List<FreightRuleVO> freightRuleVOS = JSON.parseArray(MapUtils.getString(map, "freight"), FreightRuleVO.class);
                    map.put("freight", freightRuleVOS);
                }
                //不配送区域
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "no_delivery")))
                {
                    JSONArray objects = JSONArray.parseArray(MapUtils.getString(map, "no_delivery"));
                    map.put("no_delivery", objects);
                }
                map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));
                //包邮设置
                if (StringUtils.isNotEmpty(MapUtils.getString(map, "package_settings")))
                {
                    resultMap.put("package_settings", MapUtils.getString(map, "package_settings"));
                }
                else
                {
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                String defaultFreight = MapUtils.getString(map, "default_freight");
                if (StringUtils.isNotEmpty(defaultFreight))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(defaultFreight, DefaultFreightVO.class);
                    map.put("default_freight", defaultFreightVO);
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", freightList);
            resultMap.put("start", pageModel.getPageNum());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取运费列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightList");
        }
        return resultMap;
    }

    @Override
    public boolean freightAdd(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            int          count;
            boolean      isUpdate        = false;
            FreightModel freightModelOld = new FreightModel();
            if (vo.getFid() != null && vo.getFid() > 0)
            {
                isUpdate = true;
                freightModelOld.setId(vo.getFid());
                freightModelOld = freightModelMapper.selectOne(freightModelOld);
            }
            FreightModel freightModel = new FreightModel();
            freightModel.setIs_default(vo.getIsDefault());
            freightModel.setName(vo.getName());
            freightModel.setType(vo.getType());
            String yfRule = null;
            if (StringUtils.isNotEmpty(vo.getHiddenFreight()))
            {
                yfRule = URLDecoder.decode(vo.getHiddenFreight(), CharEncoding.UTF_8);
            }
            freightModel.setFreight(yfRule);
            //验证数据
            freightModel = DataCheckTool.checkFreightDataFormate(freightModel);
            if (!isUpdate || !freightModelOld.getName().equals(vo.getName()))
            {
                //验证规则名称是否已存在
                FreightModel freight = new FreightModel();
                freight.setStore_id(vo.getStoreId());
                freight.setMch_id(vo.getShopId());
                freight.setName(freightModel.getName());
                count = freightModelMapper.selectCount(freight);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZMCYCZQZXXZLYGMC, "模板名称重复,请重新输入", "freightAdd");
                }
            }
            //不配送区域  //根据PHP新增
            String bpsRule = null;
            //不配送区域
            if (StringUtils.isNotEmpty(vo.getNoDelivery()))
            {
                List<Map<String, Object>> freightList = JSON.parseObject(yfRule, new TypeReference<List<Map<String, Object>>>()
                {
                });
                List<String> cityList = new ArrayList<>();
                freightList.stream().forEach(freight ->
                {
                    String[] names = MapUtils.getString(freight, "name").split(SplitUtils.DH);
                    cityList.addAll(Arrays.asList(names));
                });
                bpsRule = URLDecoder.decode(vo.getNoDelivery(), CharEncoding.UTF_8);
                //二维数组遍历[["1"],["3"],["2"]]
                JSONArray objects = JSONArray.parseArray(bpsRule);
                for (Object array : objects)
                {
                    if (cityList.contains(array.toString()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZMCYCZQZXXZLYGMC, "不配送区域与运费规则中有重复省份", "freightAdd");
                    }
                }
                freightModel.setNo_delivery(bpsRule);
            }
            //默认运费规则
            if (StringUtils.isNotEmpty(vo.getDefaultFreight()))
            {
                String defaultFreight = URLDecoder.decode(vo.getDefaultFreight(), CharEncoding.UTF_8);
                freightModel.setDefault_freight(defaultFreight);
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSRMRYFGZ, "请输入默认运费规则", "freightAdd");
            }
            //是否是包邮设置 0.未开启 1.开启
            freightModel.setIs_package_settings(vo.getIsPackageSettings());
            //包邮设置
            if (StringUtils.isNotEmpty(vo.getPackageSettings()))
            {
                freightModel.setPackage_settings(vo.getPackageSettings());
            }

            //是否不配送 0.未开启 1.开启
            freightModel.setIs_no_delivery(vo.getIsNoDelivery());

            //国家和语种
            freightModel.setLang_code(vo.getLang_code());
            freightModel.setCountry_num(vo.getCountry_num());

            //添加运费
            if (isUpdate)
            {
                freightModel.setId(freightModelOld.getId());
                count = freightModelMapper.updateByPrimaryKeySelective(freightModel);
            }
            else
            {
                freightModel.setStore_id(vo.getStoreId());
                freightModel.setMch_id(vo.getShopId());
                freightModel.setAdd_time(new Date());
                //第一条默认为默认
                FreightModel first = new FreightModel();
                first.setStore_id(vo.getStoreId());
                first.setMch_id(vo.getShopId());
                int i = freightModelMapper.selectCount(first);
                if (i < 1)
                {
                    freightModel.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                }
                count = freightModelMapper.insertSelective(freightModel);
            }

            //判断是否是设置默认,如果是设置默认,则取消原来默认,修改当前为默认。
            if (vo.getIsDefault() != null && vo.getIsDefault() == 1)
            {
                //设置当前运费为默认
                vo.setFid(freightModel.getId());
                this.setDefault(vo);
            }

            if (count < 1)
            {
                logger.debug("运费添加/修改失败 参数:{}", JSON.toJSONString(freightModel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YFTJXGSB, "运费添加/修改失败", "freightAdd");
            }

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightAdd");
        }
    }

    @Override
    public Map<String, Object> freightModifyShow(int storeId, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //运费id字符串集
            StringBuilder selCityArr = new StringBuilder();

            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            freightModel.setId(id);
            freightModel = freightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //运费规则
                List<FreightRuleVO> freightRuleVOS = new ArrayList<>();
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    freightRuleVOS = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                }
                resultMap.put("list", freightRuleVOS);
                resultMap.put("freight", freightRuleVOS);
                //不配送区域
                if (StringUtils.isNotEmpty(freightModel.getNo_delivery()))
                {
                    JSONArray objects = JSONArray.parseArray(freightModel.getNo_delivery());
                    resultMap.put("no_delivery", objects);
                }
                //是否包邮设置
                resultMap.put("is_package_settings", freightModel.getIs_package_settings().toString());
                if (StringUtils.isNotEmpty(freightModel.getPackage_settings()))
                {
                    //包邮设置
                    resultMap.put("package_settings", freightModel.getPackage_settings());
                }
                else
                {
                    //包邮设置
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                if (StringUtils.isNotEmpty(freightModel.getDefault_freight()))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                    resultMap.put("default_freight", defaultFreightVO);
                }
                //是否不配送
                resultMap.put("is_no_delivery", freightModel.getIs_no_delivery().toString());
                resultMap.put("is_default", freightModel.getIs_default());
                resultMap.put("name", freightModel.getName());
                resultMap.put("type", freightModel.getType());
                resultMap.put("sel_city_arr", StringUtils.trim(selCityArr.toString(), ",").split(","));
                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "freightModifyShow");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载编辑运费页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightModifyShow");
        }
    }

    @Override
    public Map<String, Object> freightModifyShow(MainVo vo, int mchId, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        int                 storeId   = vo.getStoreId();
        try
        {
            //运费id字符串集
            StringBuilder selCityArr = new StringBuilder();

            FreightModel freightModel = new FreightModel();
            freightModel.setStore_id(storeId);
            freightModel.setMch_id(mchId);
            freightModel.setId(id);
            freightModel = freightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //运费规则
                List<FreightRuleVO> freightRuleVOS = new ArrayList<>();
                if (StringUtils.isNotEmpty(freightModel.getFreight()))
                {
                    freightRuleVOS = JSON.parseArray(freightModel.getFreight(), FreightRuleVO.class);
                }
                resultMap.put("list", freightRuleVOS);
                resultMap.put("freight", freightRuleVOS);
                //不配送区域
                if (StringUtils.isNotEmpty(freightModel.getNo_delivery()))
                {
                    JSONArray objects = JSONArray.parseArray(freightModel.getNo_delivery());
                    resultMap.put("no_delivery", objects);
                }
                //是否包邮设置
                resultMap.put("is_package_settings", freightModel.getIs_package_settings().toString());
                if (StringUtils.isNotEmpty(freightModel.getPackage_settings()))
                {
                    //包邮设置
                    resultMap.put("package_settings", freightModel.getPackage_settings());
                }
                else
                {
                    //包邮设置
                    resultMap.put("package_settings", "");
                }
                //默认运费规则
                if (StringUtils.isNotEmpty(freightModel.getDefault_freight()))
                {
                    DefaultFreightVO defaultFreightVO = JSON.parseObject(freightModel.getDefault_freight(), DefaultFreightVO.class);
                    resultMap.put("default_freight", defaultFreightVO);
                }
                //是否不配送
                resultMap.put("is_no_delivery", freightModel.getIs_no_delivery().toString());
                resultMap.put("is_default", freightModel.getIs_default());
                resultMap.put("name", freightModel.getName());
                resultMap.put("type", freightModel.getType());

                resultMap.put("lang_name", publiceService.getLangName(freightModel.getLang_code()));
                resultMap.put("lang_code", freightModel.getLang_code());

                resultMap.put("country_name", publiceService.getCountryName(freightModel.getCountry_num()));
                resultMap.put("country_num", freightModel.getCountry_num());

                resultMap.put("sel_city_arr", StringUtils.trim(selCityArr.toString(), ",").split(","));


                return resultMap;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "freightModifyShow");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("加载编辑运费页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightModifyShow");
        }
    }

    @Override
    public void freightDel(int storeId, String ids,Integer mch_id) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(ids))
            {
                List<ProductListModel> productListModelList = new ArrayList<>();
                Integer mchId = null;
                boolean flag = false;
                String[] fidList = ids.split(",");
                for (String id : fidList)
                {
                    FreightModel freightMch   = freightModelMapper.selectByPrimaryKey(id);
                    if (Objects.isNull(freightMch))continue;
                    int count = freightModelMapper.deleteByPrimaryKey(Integer.parseInt(id));
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙", "freightDel");
                    }
                    mchId = freightMch.getMch_id();
                    //删除默认运费则将下一个运费设为默认
                    if (freightMch.getIs_default().equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                    {
                        freightModelMapper.setDefaultDel(storeId, mchId, 1, 0);
                    }
                    //获取绑定运费商品
                    ProductListModel productListModel = new ProductListModel();
                    productListModel.setStore_id(storeId);
                    productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
                    productListModel.setFreight(id);
                    List<ProductListModel> list = productListModelMapper.select(productListModel);
                    if (CollectionUtils.isNotEmpty(list))
                    {
                        flag = true;
                        productListModelList.addAll(list);
                    }
                }
                if (flag)
                {
                    //如果有商品应用了该运费,则使用默认运费模板
                    FreightModel defaultFreight = new FreightModel();
                    defaultFreight.setStore_id(storeId);
                    defaultFreight.setMch_id(mch_id);
                    defaultFreight.setIs_default(DictionaryConst.WhetherMaven.WHETHER_OK);
                    FreightModel freightModel = freightModelMapper.selectOne(defaultFreight);
                    if (CollectionUtils.isNotEmpty(productListModelList))
                    {
                        for (ProductListModel productListModel : productListModelList)
                        {
                            productListModelMapper.updateFreight(productListModel.getId(), freightModel.getId().toString());
                        }
                    }
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFCS, "非法参数", "freightDel");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除运费 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "freightDel");
        }
    }

    @Override
    public void setDefault(AddFreihtVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getFid() == null)
            {
                logger.debug("运费id不能为空");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFSJ, "非法数据");
            }
            FreightModel freightModel = new FreightModel();
            freightModel.setId(vo.getFid());
            //获取当前运费信息
            freightModel = freightModelMapper.selectOne(freightModel);
            if (freightModel != null)
            {
                //之前默认修改成非默认
                freightModelMapper.setDefault(vo.getStoreId(), freightModel.getMch_id(), 0, 1);

                FreightModel updateFreight = new FreightModel();
                updateFreight.setId(freightModel.getId());
                updateFreight.setIs_default(1);
                int count = freightModelMapper.updateByPrimaryKeySelective(updateFreight);
                if (count < 1)
                {
                    logger.info("设置运费默认失败 参数" + JSON.toJSONString(updateFreight));
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
                }
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJBCZ, "数据不存在", "setDefault");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("设置默认运费 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefault");
        }
    }

    /**
     * 生成提取码
     *
     * @return
     */
    @Override
    public String extractionCode()
    {
        try
        {
            String        str           = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            StringBuilder stringBuilder = new StringBuilder();
            int           codeLen       = 6;
            for (int i = 0; i < codeLen; i++)
            {
                stringBuilder.append(str.charAt(new java.security.SecureRandom().nextInt(62)));
            }

            long time    = System.currentTimeMillis() / 1000;//秒
            long timeEnd = System.currentTimeMillis() / 1000 + 30 * 60 * 1000;//

            stringBuilder.append(SplitUtils.DH).append(time).append(SplitUtils.DH).append(timeEnd);
            Example          example  = new Example(OrderModel.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andCondition(" status in (0,2) ");
            criteria.andEqualTo("extraction_code", stringBuilder.toString());
            OrderModel orderModel = orderModelMapper.selectOneByExample(example);
            if (orderModel != null)
            {
                return extractionCode();
            }
            else
            {
                return stringBuilder.toString();
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public String createQRCodeImg(String extractionCode, int storeId, int storeType) throws LaiKeAPIException
    {
        String      retImgUrl   = "";
        InputStream inputStream = null;
        try
        {
            File file = QRCode.from(extractionCode).to(ImageType.PNG).withCharset("utf-8").withSize(250, 250).file();
            inputStream = new FileInputStream(file);
            String              fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + ImageType.PNG.toString().toLowerCase();
            MultipartFile       mfile    = new MockMultipartFile(extractionCode, fileName, MediaType.IMAGE_PNG_VALUE, inputStream);
            List<MultipartFile> files    = new ArrayList<>();
            files.add(mfile);
            List<String> imageUrlList = publiceService.uploadImage(files, GloabConst.UploadConfigConst.IMG_UPLOAD_OSS, storeType, storeId, true);
            retImgUrl = imageUrlList.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("生成二维码图片出错：{}", e.getMessage());
            return null;
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return retImgUrl;
    }

    @Override
    public boolean cancellationShop(int storeId, int mchId) throws LaiKeAPIException
    {
        try
        {
            //店铺是否有待处理的订单，如果有待处理的订单则不能注销
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(storeId);
            mchModel.setId(mchId);
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPBCZ, "店铺不存在", "cancellationShop");
            }
            //店铺余额
//            if (mchModel.getAccount_money().compareTo(BigDecimal.ZERO) > 0 || mchModel.getCashable_money().compareTo(BigDecimal.ZERO) > 0){
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "店铺余额未清空");
//            }
            //是否缴纳保证金
            MchPromiseModel mchPromiseCount = new MchPromiseModel();
            mchPromiseCount.setMch_id(mchModel.getId());
            mchPromiseCount.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            if (mchPromiseModelMapper.selectCount(mchPromiseCount) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQDPYWTHDBZJ, "当前店铺有未退还的保证金");
            }
            //是否有未完成的订单
            int orderNum = mchModelMapper.countMchUnfinishedOrder(storeId, mchModel.getUser_id());
            if (orderNum > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYJXZDDWFZX, "您的店铺账户尚有进行中订单,无法注销");
            }

            //删除店铺
            MchModel mchDel = new MchModel();
            mchDel.setId(mchId);
            mchDel.setRecovery(DictionaryConst.ProductRecycle.RECOVERY);
            int count = mchModelMapper.updateByPrimaryKeySelective(mchDel);
            if (count < 1)
            {
                logger.info("店铺注销失败 参数L:" + JSON.toJSONString(mchDel));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPZXSB, "店铺注销失败", "cancellationShop");
            }
            //自营店不能删除
            AdminModel adminModel = adminModelMapper.getAdminCustomer(storeId);
            if (adminModel != null && adminModel.getShop_id().equals(mchId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZYDBNSC, "自营店不能删除");
            }
            //删除跳转地址
            JumpPathModel jumpPathModel = new JumpPathModel();
            jumpPathModel.setSid(mchModel.getId() + "");
            jumpPathModel.setType0(JumpPathModel.JumpType.JUMP_TYPE0_MCH);
            jumpPathModelMapper.delete(jumpPathModel);

            //删除店铺商品
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setMch_id(mchId);
            List<ProductListModel> productListModelList = productListModelMapper.select(productListModel);
            for (ProductListModel goods : productListModelList)
            {
                if (!publicGoodsService.delGoodsById(storeId, goods.getId(), mchId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPSCSB, "商品删除失败", "cancellationShop");
                }
            }
            //删除对应优惠券
            couponActivityModelMapper.delCouponByMchId(storeId, mchId);

            return true;
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
    public boolean isPromisePay(MainVo vo, String userId) throws LaiKeAPIException
    {
        boolean isPromisePay = false;
        try
        {
            User user = new User();
            user.setUser_id(userId);
            user = userMapper.selectOne(user);

            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                String mchId = userAuthorityModelMapper.getMchIdByUserId(userId);
                if (StringUtils.isEmpty(mchId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
                mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
            }
            MchPromiseModel mchPromiseCount = new MchPromiseModel();
            mchPromiseCount.setMch_id(mchModel.getId());
            mchPromiseCount.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            if (mchPromiseModelMapper.selectCount(mchPromiseCount) > 0)
            {
                isPromisePay = true;
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("是否缴纳保证金 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("是否缴纳保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isPromisePay");
        }
        return isPromisePay;
    }

    @Autowired
    private PublicMchService publicMchService;

    @Override
    public Map<String, Object> paymentPromise(MainVo vo, String payType, String pwd, String userId, String orderNo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = new User();
            user.setUser_id(userId);
            user = userMapper.selectOne(user);
            //获取用户店铺
            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);
            if (mchModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FFRQ, "非法入侵");
            }
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), null);
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCDPPZWCSH, "商城店铺配置未初始化");
            }
            if (mchConfigModel.getPromise_switch() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DPWKQBZJ, "店铺未开启保证金");
            }
            if (isPromisePay(vo, user.getUser_id()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BZJYJJN, "保证金已经缴纳");
            }
            MchPromiseModel mchPromiseSave = new MchPromiseModel();
            OrderDataModel  orderDataSave  = new OrderDataModel();

            String sno = publicOrderService.createOrderNo(DictionaryConst.OrdersType.ORDERS_HEADER_MCH_PROMISE);
            if (DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY.equals(payType))
            {
                mchPromiseSave.setOrderNo(orderNo = sno);
                publicUserService.validatePayPwd(user.getUser_id(), pwd);
                String text = userId + "缴纳了" + mchConfigModel.getPromise_amt().toString() + "元店铺押金!";
                publicPaymentService.walletPay(user.getUser_id(), mchConfigModel.getPromise_amt(), GloabConst.RedisHeaderKey.LOGIN_ACCESS_TOKEN, vo.getAccessId(), RecordModel.RecordType.PAY_MCH_DEPOSIT, text, sno);
                orderDataSave.setStatus(OrderDataModel.PayStatus.PAYMENT);
            }
            else
            {
                //第三方支付
                orderDataSave.setStatus(OrderDataModel.PayStatus.NOT_PAY);
            }

            //下单操作
            //往lkt_order_data表存数据
            OrderDataModel orderDataByTradeNo = orderDataModelMapper.getOrderDataByTradeNo(sno);

            int row = 0;
            if (orderDataByTradeNo == null)

            {
                Map<String, Object> dataMap = new HashMap<>(16);
                dataMap.put("paymentAmt", mchConfigModel.getPromise_amt());
                dataMap.put("storeId", mchModel.getStore_id());
                dataMap.put("mchId", mchModel.getId());
                dataMap.put("pay", payType);
                dataMap.put("user_id", userId);
                orderDataSave.setData(JSON.toJSONString(dataMap));
                orderDataSave.setTrade_no(sno);
                orderDataSave.setOrder_type(payType);
                orderDataSave.setAddtime(new Date());
                row = orderDataModelMapper.insertSelective(orderDataSave);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }

            if (StringUtils.isNotEmpty(orderNo))
            {
                //是否有生成过订单,如果生成过则修改记录
                MchPromiseModel mchPromiseOld = new MchPromiseModel();
                mchPromiseOld.setMch_id(mchModel.getId());
                mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);

                //支付记录
                mchPromiseSave.setPay_type(payType);
                mchPromiseSave.setOrderNo(orderNo);
                mchPromiseSave.setPromise_amt(mchConfigModel.getPromise_amt());
                mchPromiseSave.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                if (mchPromiseOld == null)
                {
                    //支付成功
                    mchPromiseSave.setAdd_date(new Date());
                    mchPromiseSave.setMch_id(mchModel.getId());
                    mchPromiseSave.setId(BuilderIDTool.getGuid());
                    row = mchPromiseModelMapper.insertSelective(mchPromiseSave);
                }
                else
                {
                    mchPromiseSave.setId(mchPromiseOld.getId());
                    mchPromiseSave.setIs_return_pay(DictionaryConst.WhetherMaven.WHETHER_NO);
                    //更新缴纳时间
                    mchPromiseSave.setAdd_date(new Date());
                    //修改时间
                    mchPromiseSave.setUpdate_date(new Date());
                    row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseSave);
                }

                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败");
                }

                //添加缴纳店铺保证金记录
                RecordModel recordModel = new RecordModel();
                recordModel.setStore_id(user.getStore_id());
                recordModel.setUser_id(userId);
                recordModel.setMoney(mchConfigModel.getPromise_amt());
                recordModel.setOldmoney(user.getMoney());
                recordModel.setEvent("缴纳店铺保证金");
                recordModel.setType(RecordModel.RecordType.PAY_MCH_BOND);
                recordModel.setAdd_date(new Date());
                recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                recordModel.setMain_id(mchModel.getId().toString());
                recordModelMapper.insertSelective(recordModel);

                PromiseRecordModel promiseRecordModel = new PromiseRecordModel();
                promiseRecordModel.setStore_id(user.getStore_id());
                promiseRecordModel.setMch_id(mchModel.getId());
                promiseRecordModel.setMoney(mchConfigModel.getPromise_amt());
                promiseRecordModel.setType(PromiseRecordModel.RecordType.PAY_MCH_MARGIN);
                promiseRecordModel.setAdd_date(new Date());
                promiseRecordModelMapper.insertSelective(promiseRecordModel);

            }
            resultMap.put("total", mchConfigModel.getPromise_amt());
            resultMap.put("orderId", orderDataSave.getId());
            resultMap.put("orderNo", orderDataSave.getTrade_no());
        }
        catch (LaiKeAPIException e)
        {
            logger.error("支付保证金 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("支付保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "paymentPromise");
        }
        return resultMap;
    }

    @Override
    public MchConfigModel getMchConfig(Integer storeId, Integer mchId) throws LaiKeAPIException
    {
        MchConfigModel mchConfigModel = new MchConfigModel();
        try
        {
            Integer storeMchId = customerModelMapper.getStoreMchId(storeId);
            mchConfigModel.setStore_id(storeId);
            mchConfigModel.setMch_id(mchId != null ? mchId : storeMchId);
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
            if (Objects.isNull(mchConfigModel))
            {
                //店铺无设置默认查询自营店配置
                mchConfigModel = new MchConfigModel();
                mchConfigModel.setStore_id(storeId);
                mchConfigModel.setMch_id(storeMchId);
                mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);
                if (mchConfigModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_PZBCZ, "找不到店铺配置");
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("查询店铺配置 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("查询店铺配置 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMchConfig");
        }
        return mchConfigModel;
    }

    @Override
    public String mchIsOpen(Integer mchId) throws LaiKeAPIException
    {
        String isOpen = "0";
        try
        {
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(mchId);
            if (mchModel.getIs_open().equals("0") || mchModel.getIs_open().equals("2"))
            {
                isOpen = mchModel.getIs_open();
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
                        isOpen = "1";
                    }
                    else
                    {
                        //未营业
                        isOpen = "2";
                    }
                }
                else
                {//开始时间小于结束时间（当天）则当前时间 >= 19:00 && 当前时间 <= 04:00 -> 营业
                    if (!DateUtil.dateCompare(startTime, currentDate)
                            && !DateUtil.dateCompare(currentDate, endTime))
                    {
                        //营业
                        isOpen = "1";
                    }
                    else
                    {
                        //未营业
                        isOpen = "2";
                    }
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("查询店铺是否已打烊 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("查询店铺是否已打烊 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "mchIsOpen");
        }
        return isOpen;
    }

    @Override
    public boolean judgeMchPromise(MainVo vo, String userId) throws LaiKeAPIException
    {
        boolean isPromisePay = false;
        try
        {
            User user = new User();
            user.setUser_id(userId);
            user = userMapper.selectOne(user);

            MchModel mchModel = new MchModel();
            mchModel.setStore_id(vo.getStoreId());
            mchModel.setUser_id(user.getUser_id());
            mchModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
            mchModel = mchModelMapper.selectOne(mchModel);

            if (mchModel == null)
            {
                String mchId = userAuthorityModelMapper.getMchIdByUserId(userId);
                if (StringUtils.isEmpty(mchId))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
                mchModel = mchModelMapper.selectByPrimaryKey(mchId);
                if (mchModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WKTDPHZDPYZX, "未开通店铺或者店铺已删除");
                }
            }
            MchConfigModel mchConfigModel = new MchConfigModel();
            mchConfigModel.setStore_id(vo.getStoreId());
            //查询自营店的id
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            mchConfigModel.setMch_id(mchId);
            mchConfigModel = mchConfigModelMapper.selectOne(mchConfigModel);

            if (mchConfigModel != null)
            {
                if (mchConfigModel.getPromise_switch() == 0)
                {
                    isPromisePay = true;
                }
                else
                {
                    MchPromiseModel mchPromiseCount = new MchPromiseModel();
                    mchPromiseCount.setMch_id(mchModel.getId());
                    mchPromiseCount.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                    if (mchPromiseModelMapper.selectCount(mchPromiseCount) > 0)
                    {
                        isPromisePay = true;
                    }
                }
            }
        }
        catch (LaiKeAPIException e)
        {
            logger.error("是否缴纳保证金 异常", e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error("是否缴纳保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isPromisePay");
        }
        return isPromisePay;
    }

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicGoodsService publicGoodsService;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private MchStoreModelMapper mchStoreModelMapper;

    @Autowired
    private MchAccountLogModelMapper mchAccountLogModelMapper;

    @Autowired
    private FreightModelMapper freightModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private AdminCgModelMapper adminCgModelMapper;
    @Autowired
    private AdminModelMapper   adminModelMapper;


    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicPaymentService;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private PublicOrderService publicOrderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JumpPathModelMapper jumpPathModelMapper;

    @Autowired
    private OrderDataModelMapper orderDataModelMapper;

    @Autowired
    private CouponActivityModelMapper couponActivityModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private PromiseRecordModelMapper promiseRecordModelMapper;
}

