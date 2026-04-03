package com.laiketui.apps.mch.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.apps.api.mch.AppsMchPromiseService;
import com.laiketui.common.api.PublicMchService;
import com.laiketui.common.api.PublicPaymentConfigService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.pay.PublicPaymentService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.service.dubbo.third.PublicPaypalServiceImpl;
import com.laiketui.common.service.dubbo.third.PublicStripeServiceImpl;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.SerializePhpUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.log.RecordDetailsModel;
import com.laiketui.domain.log.RecordModel;
import com.laiketui.domain.mch.*;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 保证金
 *
 * @author Trick
 * @date 2021/10/26 10:55
 */
@Service
public class AppsMchPromiseServiceImpl implements AppsMchPromiseService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MchPromiseModelMapper mchPromiseModelMapper;

    @Autowired
    private MchConfigModelMapper mchConfigModelMapper;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicPaymentService;

    @Autowired
    @Qualifier("publicAlipayServiceImpl")
    private PublicPaymentService publicAlipayServiceImpl;

    @Autowired
    @Qualifier("publicWechatServiceImpl")
    private PublicPaymentService publicWechatServiceImpl;

    @Autowired
    private OrderModelMapper orderModelMapper;

    @Autowired
    private PaymentConfigModelMapper paymentConfigModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private PublicMchService publicMchService;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private PromiseShModelMapper promiseShModelMapper;

    @Autowired
    private MessageListModelMapper messageListModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private RecordDetailsModelMapper recordDetailsModelMapper;

    @Autowired
    private PromiseRecordModelMapper promiseRecordModelMapper;

    @Autowired
    private PublicPaypalServiceImpl publicPaypalService;

    @Autowired
    private PublicStripeServiceImpl publicStripeServiceImpl;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private CurrencyStoreModelMapper currencyStoreModelMapper;

    @Autowired
    private PublicPaymentConfigService publicPaymentConfigService;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User           user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取自营店id
            Integer        storeMchId     = customerModelMapper.getStoreMchId(vo.getStoreId());
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), storeMchId);
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCDPPZWCSH, "商城店铺配置未初始化");
            }
//            if (mchConfigModel.getPromise_switch() == DictionaryConst.WhetherMaven.WHETHER_OK) {
            resultMap.put("promisePrice", mchConfigModel.getPromise_amt());
            resultMap.put("promiseText", mchConfigModel.getPromise_text());
//            }
            //是否设置支付密码,如果没有设置支付密码则提示支付密码
            boolean isSetPayment = false;
            user = userBaseMapper.selectByPrimaryKey(user.getId());
            if (StringUtils.isNotEmpty(user.getPassword()))
            {
                isSetPayment = true;
            }
            resultMap.put("isSetPayment", isSetPayment);
            resultMap.put("promiseSwitch", mchConfigModel.getPromise_switch());
            //支付方式
            resultMap.putAll(this.getPaymentList(vo));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("缴纳保证金页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public boolean isPromisePay(MainVo vo) throws LaiKeAPIException
    {
        //是否缴纳保证金
        boolean isPromisePay;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            isPromisePay = publicMchService.isPromisePay(vo, user.getUser_id());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否缴纳保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return isPromisePay;
    }

    @Override
    public boolean examineStatus(int mchId) throws LaiKeAPIException
    {
        //店铺保证金退还记录在审核中则不允许操作任何店铺功能
        boolean examineStatus = true;
        try
        {
            Map<String, Object> first = promiseShModelMapper.getFirst(mchId);
            Integer             id    = MapUtils.getInteger(first, "id");
            if (!Objects.isNull(id))
            {
                Integer status = MapUtils.getInteger(first, "status");
                if (status == 3)
                {
                    examineStatus = false;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("店铺是否可以操作 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "examineStatus");
        }
        return examineStatus;
    }

    @Override
    public Map<String, Object> getPaymentList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            user = userBaseMapper.selectByPrimaryKey(user.getId());

            //各个支付的开启、关闭状态
            Map payInfo = new HashMap();
            int storeId = vo.getStoreId();
            payInfo = publicPaymentConfigService.getPaymentInfos(storeId);

            resultMap.put("userMoney", user.getMoney());
            resultMap.putAll(payInfo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取支付方式列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getPaymentList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> payment(MainVo vo, String payType, String pwd) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            resultMap = publicMchService.paymentPromise(vo, payType, pwd, user.getUser_id(), null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("支付保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "payment");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> promiseManage(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap  = new HashMap<>(16);
        Map<String, Object> returntMap = new HashMap<>(16);
        try
        {
            User           user           = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            MchConfigModel mchConfigModel = publicMchService.getMchConfig(vo.getStoreId(), user.getMchId());
            if (mchConfigModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCDPPZWCSH, "商城店铺配置未初始化");
            }

            MchPromiseModel mchPromiseModel = new MchPromiseModel();
            mchPromiseModel.setMch_id(user.getMchId());
            mchPromiseModel.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseModel = mchPromiseModelMapper.selectOne(mchPromiseModel);
            if (mchPromiseModel != null)
            {
                resultMap.put("promisePrice", mchPromiseModel.getPromise_amt());
                resultMap.put("payDate", DateUtil.dateFormate(mchPromiseModel.getAdd_date(), GloabConst.TimePattern.YMDHMS));
            }
            else
            {
                resultMap.put("promisePrice", mchConfigModel.getPromise_amt());
            }
            resultMap.put("promiseText", mchConfigModel.getPromise_text());
            returntMap.put("mch_id", user.getMchId());
            List<Map<String, Object>> promiseShInfoList = promiseShModelMapper.getPromiseShRefusedWhyDynamic(returntMap);
            List<Map<String, Object>> promiseShList     = promiseShModelMapper.getPromiseShWhyDynamic(returntMap);
            String                    status            = null;
            for (Map<String, Object> promiseSh : promiseShList)
            {
                status = promiseSh.get("status").toString();
            }
            for (Map<String, Object> map : promiseShInfoList)
            {
                String reFusedDate = "";
                if (map.containsKey("refused_date"))
                {
                    reFusedDate = DateUtil.dateFormate(MapUtils.getString(map, "refused_date"), GloabConst.TimePattern.YMDHMS);
                }
                String addDate = DateUtil.dateFormate(MapUtils.getString(map, "add_date"), GloabConst.TimePattern.YMDHMS);
                map.put("refused_date", reFusedDate);
                map.put("add_date", addDate);
            }
            resultMap.put("refusedWhy", promiseShInfoList);
            resultMap.put("status", status);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金管理页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "promiseManage");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> promiseList(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("main_id", user.getMchId());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            List<Integer> typeList = new ArrayList<>();
            typeList.add(RecordModel.RecordType.PAY_MCH_BOND);
            typeList.add(RecordModel.RecordType.REFUND_MCH_BOND);
            parmaMap.put("typeList", typeList);
            int                       total       = recordModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> promiseList = new ArrayList<>();
            if (total > 0)
            {
                promiseList = recordModelMapper.selectDynamic(parmaMap);
                for (Map<String, Object> map : promiseList)
                {
                    String addDate = MapUtils.getString(map, "add_date");
                    map.put("add_date", DateUtil.dateFormate(addDate, GloabConst.TimePattern.YMDHMS));
                    map.put("money", new BigDecimal(MapUtils.getString(map, "money")).setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
                }
            }

            resultMap.put("list", promiseList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金管理页面 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "promiseManage");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnPromisePrice(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(user.getMchId());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            if (mchPromiseOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JNJWJ, "缴纳金未缴");
            }
            //是否有未完成的订单
            int orderNum = mchModelMapper.countMchUnfinishedOrder(vo.getStoreId(), user.getUser_id());
            if (orderNum > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYJXZDDWFTHBZJ, "您的店铺账户尚有进行中订单,无法退还保证金");
            }
            //有商品不能退款
            ProductListModel productListCount = new ProductListModel();
            productListCount.setStore_id(vo.getStoreId());
            productListCount.setMch_id(user.getMchId());
            productListCount.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            if (productListModelMapper.selectCount(productListCount) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYSJDSPWFTHBZJ, "您的店铺账户尚有上架的商品,无法退还保证金");
            }

            switch (mchPromiseOld.getPay_type())
            {
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                    publicPaymentService.walletReturnPay(user.getUser_id(), mchPromiseOld.getPromise_amt(), vo.getAccessId());
                    break;
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在");
            }
            MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
            mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_RETURN_PAY);
            mchPromiseUpdate.setIs_return_pay(1);
            mchPromiseUpdate.setId(mchPromiseOld.getId());
            mchPromiseUpdate.setUpdate_date(new Date());
            int row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            String      text        = "店铺押金退还" + mchPromiseOld.getPromise_amt().toString() + "元!";
            RecordModel recordModel = new RecordModel(vo.getStoreId(), user.getUser_id(), mchPromiseOld.getPromise_amt(), user.getMoney(), new Date(), text, RecordModel.RecordType.REFUND_MCH_DEPOSIT);
            recordModelMapper.insertSelective(recordModel);
            //增加店铺保证金退还记录
            recordModel = new RecordModel(vo.getStoreId(), user.getUser_id(), mchPromiseOld.getPromise_amt(), user.getMoney(), new Date(), "退还保证金", RecordModel.RecordType.REFUND_MCH_BOND);
            recordModelMapper.insertSelective(recordModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "returnPromisePrice");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertPromisePrice(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);

            MchPromiseModel mchPromiseOld = new MchPromiseModel();
            mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
            mchPromiseOld.setMch_id(user.getMchId());
            mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
            if (mchPromiseOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JNJWJ, "缴纳金未缴");
            }
            //是否有未完成的订单
            int orderNum = mchModelMapper.countMchUnfinishedOrder(vo.getStoreId(), user.getUser_id());
            if (orderNum > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYJXZDDWFTHBZJ, "您的店铺账户尚有进行中订单,无法退还保证金");
            }
            //有商品不能退款
            ProductListModel productListCount = new ProductListModel();
            productListCount.setStore_id(vo.getStoreId());
            productListCount.setMch_id(user.getMchId());
            productListCount.setStatus(DictionaryConst.GoodsStatus.NEW_GROUNDING.toString());
            productListCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            if (productListModelMapper.selectCount(productListCount) > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_NDDPZHSYSJDSPWFTHBZJ, "您的店铺账户尚有上架的商品,无法退还保证金");
            }
            PromiseShModel oldPromise = new PromiseShModel();
            oldPromise.setMch_id(user.getMchId());
            oldPromise.setIs_pass(3);
            int i = promiseShModelMapper.selectCount(oldPromise);
            if (i > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_ALREADY_EXISTS, "当前已存在正在审核的保证金记录");
            }
            //保存保证金信息到lkt_promise_sh表
            PromiseShModel promiseShModel = new PromiseShModel();
            promiseShModel.setMch_id(user.getMchId());
            promiseShModel.setPromise_amt(mchPromiseOld.getPromise_amt());
            promiseShModel.setStatus(3);
            promiseShModel.setPay_type(mchPromiseOld.getPay_type());
            promiseShModel.setIs_pass(3);
            promiseShModel.setAdd_date(new Date());
            int row = promiseShModelMapper.insertSelective(promiseShModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败");
            }
            MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
            messageLoggingSave.setMch_id(user.getMchId());
            messageLoggingSave.setStore_id(vo.getStoreId());
            messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_STORE_WITHDRAWAL);
            messageLoggingSave.setTo_url(MessageLoggingModal.AdminUrl.MCH_SECURITY_DEPOSIT_URL);
            messageLoggingSave.setParameter(user.getUser_id());
            messageLoggingSave.setContent(String.format("ID为%s的店铺申请提取保证金，请及时处理！", user.getMchId()));
            messageLoggingSave.setAdd_date(new Date());
            messageLoggingModalMapper.insertSelective(messageLoggingSave);

            PromiseRecordModel promiseRecordModel = new PromiseRecordModel();
            promiseRecordModel.setStore_id(user.getStore_id());
            promiseRecordModel.setMch_id(user.getMchId());
            promiseRecordModel.setMoney(mchPromiseOld.getPromise_amt());
            promiseRecordModel.setType(PromiseRecordModel.RecordType.REFUND_MCH_MARGIN);
            promiseRecordModel.setStatus(DictionaryConst.ExameStatus.EXAME_WAIT_STATUS);
            promiseRecordModel.setAdd_date(new Date());
            promiseRecordModel.setPromise_sh_id(promiseShModel.getId().toString());
            promiseRecordModelMapper.insertSelective(promiseRecordModel);

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "returnPromisePrice");
        }
    }

    @Override
    public Map<String, Object> selectPromisePrice(MainVo vo, String title) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isNotEmpty(title))
            {
                resultMap.put("title", title);
            }
            resultMap.put("pageStart", vo.getPageNo());
            resultMap.put("storeId", vo.getStoreId());
            resultMap.put("pageEnd", vo.getPageSize());
            List<Map<String, Object>> promiseShInfoList = promiseShModelMapper.getPromiseShInfoDynamic(resultMap);
            for (Map<String, Object> map : promiseShInfoList)
            {
                String logoUrl = MapUtils.getString(map, "logo");
                map.put("logo", publiceService.getImgPath(logoUrl, vo.getStoreId()));
                String headImg = MapUtils.getString(map, "head_img");
                map.put("headimgurl", publiceService.getImgPath(headImg, vo.getStoreId()));
            }
            int total = promiseShModelMapper.countPromiseShInfoDynamic(resultMap);
            resultMap.put("list", promiseShInfoList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保证金审核列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passOrRefused(MainVo vo, int id, int isPass, String refusedWhy) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            PromiseShModel     promiseShModel     = new PromiseShModel();
            PromiseShModel     promiseShTKModel   = promiseShModelMapper.selectByPrimaryKey(id);
            PromiseRecordModel promiseRecordModel = new PromiseRecordModel();
            promiseRecordModel.setPromise_sh_id(String.valueOf(id));
            promiseRecordModel = promiseRecordModelMapper.selectOne(promiseRecordModel);
            MessageLoggingModal messageLoggingSave;
            //根据商户id查询用户id
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(promiseShTKModel.getMch_id());
            //查询用户信息
            User user = new User();
            user.setUser_id(mchModel.getUser_id());
            user.setStore_id(vo.getStoreId());
            user = userBaseMapper.selectOne(user);
            if (user == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
            }
            String   event    = "";
            if (isPass == 1)
            {//审核通过
                promiseShModel.setStatus(isPass);
                promiseShModel.setIs_pass(isPass);
                promiseShModel.setId(id);
                //根据审核表获取保证金表
                MchPromiseModel mchPromiseOld = new MchPromiseModel();
                mchPromiseOld.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
                mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
                logger.error("mchPromiseOld为：" + mchPromiseOld.toString());

                //是否短信配置
                boolean isSendNotice = false;
                //获取短信列表
                Map<String, Object> messageInfo = messageListModelMapper.getMessageListInfoByType(vo.getStoreId(), GloabConst.VcodeCategory.TYPE_NOTICE, GloabConst.VcodeCategory.PAY_REFUND_ORDER);
                String              content;
                String              parmaJson;
                Map<String, String> smsParmaMap = null;
                if (messageInfo != null)
                {
                    isSendNotice = true;
                    content = messageInfo.get("content").toString();
                    parmaJson = JSON.toJSONString(SerializePhpUtils.getUnserializeObj(content, Map.class));
                    smsParmaMap = JSON.parseObject(parmaJson, new TypeReference<Map<String, String>>()
                    {
                    });
                }
                else
                {
                    logger.error("商城【{}】未配置通知短信服务!", vo.getStoreId());
                }
                //审核通过时退还保证金
                switch (promiseShTKModel.getPay_type())
                {
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_WALLET_PAY:
                        publicPaymentService.walletReturnPay(mchModel.getUser_id(), promiseShModel, promiseShTKModel.getPromise_amt(), vo.getAccessId(), mchModel.getId());
                        break;
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_TMP:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_ALIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_MINIPAY:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_ALIPAY_WAP:
                        //发起退款
                        Map<String, String> map = publicAlipayServiceImpl.refundOrder(vo.getStoreId(), id, isPass, refusedWhy, mchPromiseOld.getPay_type(), mchPromiseOld.getOrderNo(), mchPromiseOld.getPromise_amt());
                        String code = map.get("code");
                        if (DictionaryConst.AliApiCode.ALIPAY_ACQ_SELLER_BALANCE_NOT_ENOUGH.equals(code))
                        {
                            //短信通知商家
                            if (isSendNotice)
                            {
//                                publiceService.sendSms(vo.getStoreId(), mchModel.getTel(), GloabConst.VcodeCategory.TYPE_NOTICE, vo.getType(), smsParmaMap);
                            }
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "退款失败", "refund");
                        }
                        break;
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_APP_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_MINI_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_PC_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_H5_WECHAT:
                    case DictionaryConst.OrderPayType.ORDERPAYTYPE_JSAPI_WECHAT:
                        //微信退款
                        logger.info("退款参数-{}-,", JSON.toJSONString(vo));
//                        logger.info("退款参数orderId-{},", orderId);
                        logger.info("退款参数payType-{}-,", mchPromiseOld.getPay_type());
                        logger.info("退款参数realOrderno-{},", mchPromiseOld.getOrderNo());
                        map = publicWechatServiceImpl.refundOrder(vo.getStoreId(), promiseShModel, mchPromiseOld.getPay_type(), mchPromiseOld.getOrderNo(), mchPromiseOld.getPromise_amt());
                        logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                        break;
                    //贝宝退款
                    case DictionaryConst.OrderPayType.PAYPAL_PAY:
                        try
                        {
                            //添加充值记录详情
                            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                            recordDetailsModel.setStore_id(user.getStore_id());
                            recordDetailsModel.setMoney(promiseShTKModel.getPromise_amt());
                            recordDetailsModel.setUserMoney(user.getMoney());
                            recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                            recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MCH_DEPOSIT);
                            recordDetailsModel.setType(RecordDetailsModel.type.MCH_DEPOSIT_WITHDRAWAL);
                            recordDetailsModel.setRecordTime(new Date());
                            recordDetailsModel.setAddTime(new Date());

                            Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency());
                            Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(user.getStore_id());
                            recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                            recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                            recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                            int row = recordDetailsModelMapper.insert(recordDetailsModel);

                            //增加店铺保证金退还记录
                            RecordModel recordModel = new RecordModel();
                            recordModel.setStore_id(user.getStore_id());
                            recordModel.setUser_id(user.getUser_id());
                            recordModel.setMoney(promiseShTKModel.getPromise_amt());
                            recordModel.setOldmoney(user.getMoney());
                            recordModel.setEvent("退还店铺保证金");
                            recordModel.setType(RecordModel.RecordType.REFUND_MCH_BOND);
                            recordModel.setAdd_date(new Date());
                            recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                            recordModel.setDetails_id(recordDetailsModel.getId());
                            recordModel.setMain_id(String.valueOf(promiseShTKModel.getMch_id()));
                            row = recordModelMapper.insertSelective(recordModel);
                            if (row > 0)
                            {
                                logger.info("新增记录成功");
                            }
                            else
                            {
                                logger.info("新增记录失败");
                            }
                        }
                        catch (LaiKeAPIException l)
                        {
                            throw l;
                        }
                        catch (Exception e)
                        {
                            logger.error("贝宝退款 异常:", e);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletReturnPay");
                        }
                        logger.info("退款参数-{}-,", JSON.toJSONString(vo));
                        logger.info("退款参数paypal_id-{},", mchPromiseOld.getPaypal_id());
                        map = publicPaypalService.refundOrder(vo.getStoreId(), promiseShModel, mchPromiseOld.getPay_type(), mchPromiseOld.getOrderNo(), mchPromiseOld.getPromise_amt());
                        logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                        break;
                    //stripe退款
                    case DictionaryConst.OrderPayType.STRIPE_PAY:
                        try
                        {
                            //添加充值记录详情
                            RecordDetailsModel recordDetailsModel = new RecordDetailsModel();
                            recordDetailsModel.setStore_id(user.getStore_id());
                            recordDetailsModel.setMoney(promiseShTKModel.getPromise_amt());
                            recordDetailsModel.setUserMoney(user.getMoney());
                            recordDetailsModel.setMoneyType(RecordDetailsModel.moneyType.INCOME);
                            recordDetailsModel.setMoneyTypeName(RecordDetailsModel.moneyTypeName.MCH_DEPOSIT);
                            recordDetailsModel.setType(RecordDetailsModel.type.MCH_DEPOSIT_WITHDRAWAL);
                            recordDetailsModel.setRecordTime(new Date());
                            recordDetailsModel.setAddTime(new Date());

                            Map<String, Object> userCurrecyMap  = currencyStoreModelMapper.getCurrencyInfo(user.getStore_id(), user.getPreferred_currency());
                            Map<String, Object> storeCurrecyMap = currencyStoreModelMapper.getDefaultCurrency(user.getStore_id());
                            recordDetailsModel.setCurrency_code(DataUtils.getStringVal(userCurrecyMap, "currency_code", DataUtils.getStringVal(storeCurrecyMap, "currency_code")));
                            recordDetailsModel.setCurrency_symbol(DataUtils.getStringVal(userCurrecyMap, "currency_symbol", DataUtils.getStringVal(storeCurrecyMap, "currency_symbol")));
                            recordDetailsModel.setExchange_rate(DataUtils.getBigDecimalVal(userCurrecyMap, "exchange_rate", DataUtils.getBigDecimalVal(storeCurrecyMap, "exchange_rate")));

                            int row = recordDetailsModelMapper.insert(recordDetailsModel);

                            //增加店铺保证金退还记录
                            RecordModel recordModel = new RecordModel();
                            recordModel.setStore_id(user.getStore_id());
                            recordModel.setUser_id(user.getUser_id());
                            recordModel.setMoney(promiseShTKModel.getPromise_amt());
                            recordModel.setOldmoney(user.getMoney());
                            recordModel.setEvent("退还店铺保证金");
                            recordModel.setType(RecordModel.RecordType.REFUND_MCH_BOND);
                            recordModel.setAdd_date(new Date());
                            recordModel.setIs_mch(DictionaryConst.WhetherMaven.WHETHER_NO);
                            recordModel.setDetails_id(recordDetailsModel.getId());
                            recordModel.setMain_id(String.valueOf(promiseShTKModel.getMch_id()));
                            row = recordModelMapper.insertSelective(recordModel);
                            if (row > 0)
                            {
                                logger.info("新增记录成功");
                            }
                            else
                            {
                                logger.info("新增记录失败");
                            }
                        }
                        catch (LaiKeAPIException l)
                        {
                            throw l;
                        }
                        catch (Exception e)
                        {
                            logger.error("stripe退款 异常:", e);
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "walletReturnPay");
                        }
                        logger.info("退款参数-{}-,", JSON.toJSONString(vo));
                        logger.info("退款参数paypal_id-{},", mchPromiseOld.getPaypal_id());
                        map = publicStripeServiceImpl.refundOrder(vo.getStoreId(), promiseShModel, mchPromiseOld.getPay_type(), mchPromiseOld.getOrderNo(), mchPromiseOld.getPromise_amt());
                        logger.info("#########退款返回##### {}", JSONObject.toJSONString(map));
                        break;
                    default:
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFFSBCZ, "支付方式不存在");
                }
//                User user = userBaseMapper.selectByUserId(vo.getStoreId(), mchModel.getUser_id());

                //h5店铺增加保证金通过消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(promiseShTKModel.getMch_id());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_BAIL);
                messageLoggingSave.setParameter("店铺退还保证金审核通过");
                messageLoggingSave.setContent(String.format("店铺退还保证金审核通过，金额:%s。", mchPromiseOld.getPromise_amt()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //pc店铺增加保证金通过消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(promiseShTKModel.getMch_id());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_PCMCH_BAIL);
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setParameter(id + "");
                messageLoggingSave.setContent(String.format("店铺退还保证金审核通过，金额:%s。", mchPromiseOld.getPromise_amt()));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                promiseRecordModel.setStatus(DictionaryConst.ExameStatus.EXAME_PASS_STATUS);

                event = "通过了店铺ID：" + mchModel.getId() + " 的保证金审核";
            }
            else
            {//拒绝
                //修改保证金审核表
                promiseShModel.setStatus(isPass);
                promiseShModel.setIs_pass(isPass);
                promiseShModel.setRefused_date(new Date());
                promiseShModel.setRefused_why(refusedWhy);
                promiseShModel.setId(id);
                promiseShModelMapper.updateByPrimaryKeySelective(promiseShModel);
                //查询审核表
                MchPromiseModel mchPromiseOld = new MchPromiseModel();
                mchPromiseOld.setMch_id(promiseShTKModel.getMch_id());
                mchPromiseOld = mchPromiseModelMapper.selectOne(mchPromiseOld);
                //修改保证金表
                MchPromiseModel mchPromiseUpdate = new MchPromiseModel();
                mchPromiseUpdate.setStatus(MchPromiseModel.PromiseConstant.STATUS_PAY);
                mchPromiseUpdate.setIs_return_pay(0);
                mchPromiseUpdate.setId(mchPromiseOld.getId());
                mchPromiseUpdate.setUpdate_date(new Date());
                int row = mchPromiseModelMapper.updateByPrimaryKeySelective(mchPromiseUpdate);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TKSB, "拒绝失败");
                }
                //h5店铺增加保证金拒接消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(promiseShTKModel.getMch_id());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_H5MCH_BAIL);
                messageLoggingSave.setParameter("店铺退还保证金审核被拒绝");
                messageLoggingSave.setContent(String.format("店铺退还保证金审核被拒绝，拒绝理由为%s。", refusedWhy));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);
                //pc店铺增加保证金拒接消息通知
                messageLoggingSave = new MessageLoggingModal();
                messageLoggingSave.setMch_id(promiseShTKModel.getMch_id());
                messageLoggingSave.setStore_id(vo.getStoreId());
                messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_PCMCH_BAIL);
                messageLoggingSave.setTo_url("");
                messageLoggingSave.setParameter(id + "");
                messageLoggingSave.setContent(String.format("店铺退还保证金审核被拒绝，拒绝理由为%s。", refusedWhy));
                messageLoggingSave.setAdd_date(new Date());
                messageLoggingModalMapper.insertSelective(messageLoggingSave);

                promiseRecordModel.setStatus(DictionaryConst.ExameStatus.EXAME_NOT_PASS_STATUS);
                promiseRecordModel.setRemarks(refusedWhy);

                event = "拒绝了店铺ID：" + mchModel.getId() + " 的保证金审核";
            }
            promiseRecordModelMapper.updateByPrimaryKeySelective(promiseRecordModel);

            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.PASS_OR_REFUSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("退还保证金 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZFSB, "支付失败", "returnPromisePrice");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePromisePrice(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            PromiseShModel promiseShModel = new PromiseShModel();
            promiseShModel.setId(id);


            PromiseShModel promiseShModel1 = promiseShModelMapper.selectByPrimaryKey(id);
            publiceService.addAdminRecord(vo.getStoreId(), "删除了店铺ID：" + promiseShModel1.getMch_id() + " 保证金的审核记录", AdminRecordModel.Type.DEL, vo.getAccessId());


            promiseShModelMapper.deleteByPrimaryKey(promiseShModel);


        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除保证金记录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "deletePromisePrice");
        }
    }
}

