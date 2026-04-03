package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicStockService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfiGureModel;
import com.laiketui.domain.config.SkuModel;
import com.laiketui.domain.message.MessageLoggingModal;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.product.StockModel;
import com.laiketui.domain.supplier.SupplierConfigureModel;
import com.laiketui.domain.supplier.SupplierStockModel;
import com.laiketui.domain.vo.goods.AddStockVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 关于库存
 *
 * @author Trick
 * @date 2020/11/19 15:56
 */
@Service
public class PublicStockServiceImpl implements PublicStockService
{

    private final Logger logger = LoggerFactory.getLogger(PublicStockServiceImpl.class);


    @Autowired
    private StockModelMapper stockModelMapper;

    @Autowired
    private SkuModelMapper skuModelMapper;

    @Autowired
    private ConfiGureModelMapper confiGureModelMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private MessageLoggingModalMapper messageLoggingModalMapper;

    @Autowired
    private SupplierProModelMapper supplierProModelMapper;

    @Autowired
    private SupplierConfigureModelMapper supplierConfigureModelMapper;

    @Autowired
    private SupplierStockModelMapper supplierStockModelMapperl;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Override
    public SkuModel saveSku(SkuModel skuModel, int num) throws LaiKeAPIException
    {
        try
        {
            String code = "";
            if (SkuModel.SKU_TYPE_ATTRIBUTE_NAME.equals(skuModel.getType()))
            {
                code = PinyinUtils.getPinYinHeadChar(skuModel.getName());
                code = "LKT_" + code + "_" + skuModel.getStore_id();
            }
            else
            {
                //获取属性上级code LKT_YS_0
                SkuModel sku = new SkuModel();
                sku.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                sku.setId(skuModel.getSid());
                sku = skuModelMapper.selectOne(sku);
                String thisCode = "";
                if (sku != null)
                {
                    thisCode = sku.getCode();
                }
                //获取之前最新子级规则然后+1
                String code1 = skuModelMapper.getAttributeByCode(skuModel.getSid());
                if (!StringUtils.isEmpty(code1))
                {
                    //属性子级拼接规则 LKT_YS_0_子级名称_001(++)...
                    num = Integer.parseInt(code1.substring(code1.lastIndexOf("_") + 1));
                }
                code = thisCode + "_" + String.format("%03d", num + 1);
            }
            skuModel.setAdmin_name("admin");
            skuModel.setCode(code);
            int count = skuModelMapper.insertSelective(skuModel);
            if (count > 0)
            {
                return skuModel;
            }
        }
        catch (Exception e)
        {
            logger.error("属性添加 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveSku");
        }
        return null;
    }

    @Override
    public boolean addStockWarning(int storeId, int attrId) throws LaiKeAPIException
    {
        try
        {
            //获取属性库存信息
            Map<String, Object> map = confiGureModelMapper.getGoodsStockInfo(attrId);
            if (map != null && !map.isEmpty())
            {
                int              minNum           = Integer.parseInt(map.get("min_inventory").toString());
                int              num              = Integer.parseInt(map.get("num").toString());
                int              pid              = Integer.parseInt(map.get("pid").toString());
                ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(pid);
                int              totalNum         = Integer.parseInt(map.get("total_num").toString());
                int              mchId            = MapUtils.getInteger(map, "mch_id");
                //预售商品没有预警值 暂时不做预警处理 20241220 wx
                if (minNum > num && productListModel.getIs_presell() == 0)
                {
                    String     text       = "预警";
                    StockModel stockModel = new StockModel();
                    stockModel.setStore_id(storeId);
                    stockModel.setProduct_id(pid);
                    stockModel.setAttribute_id(attrId);
                    stockModel.setTotal_num(totalNum);
                    stockModel.setMch_id(mchId);
                    //预警数量
                    stockModel.setFlowing_num(minNum - num);
                    stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_WARNING);
                    stockModel.setContent(text);
                    stockModel.setAdd_date(new Date());
                    int count = stockModelMapper.insertSelective(stockModel);
                    if (count > 0)
                    {
                        logger.debug("库存预警添加成功 id={}", attrId);
                        //通知后台消息
                        MessageLoggingModal messageLoggingSave = new MessageLoggingModal();
                        messageLoggingSave.setStore_id(storeId);
                        if (StringUtils.isNotEmpty(productListModel.getGongyingshang()))
                        {
                            messageLoggingSave.setSupplier_id(Integer.valueOf(productListModel.getGongyingshang()));
                        }
                        else
                        {
                            messageLoggingSave.setMch_id(mchId);
                        }
                        messageLoggingSave.setType(MessageLoggingModal.Type.TYPE_GOODS_REPLENISHMENT);
                        messageLoggingSave.setParameter(attrId + "");
                        messageLoggingSave.setContent(String.format("商品ID为%s的商品库存不足，请尽快补充库存", pid));
                        messageLoggingSave.setAdd_date(new Date());
                        messageLoggingModalMapper.insertSelective(messageLoggingSave);
                        return true;
                    }
                }
                else
                {
                    logger.debug("库存充足,无需预警 id={}", attrId);
                    return true;
                }
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addStockWarning");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加库存预警 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addStockWarning");
        }
        return false;
    }

    @Override
    public void addGoodsStock(AddStockVo vo, String uname) throws LaiKeAPIException
    {
        try
        {
            int id       = vo.getId();
            int stockNum = vo.getAddNum();
            int goodsId  = vo.getPid();
            //判断是否为代售供应商商品 true就操作该商品上级的商品库存 并且同步该上级商品以下所有代售商品库存
            ProductListModel product = productListModelMapper.selectByPrimaryKey(goodsId);
            if (product == null)
            {
                logger.debug("商品id{} 不存在", goodsId);
                return;
            }
            //id即cid，880   goodsId即pid，555
            if (StringUtils.isNotEmpty(product.getGongyingshang()) && product.getSupplier_superior() != null && product.getSupplier_superior() > 0)
            {
                //供应商商品信息
                ProductListModel productListModel  = productListModelMapper.selectByPrimaryKey(product.getSupplier_superior());
                //当前用户购买的商品规格信息
                ConfiGureModel   confiGureModel    = confiGureModelMapper.selectByPrimaryKey(id);
                //当前用户购买的商品规格的供应商商品规格信息
                ConfiGureModel   supplierConfigure = confiGureModelMapper.selectByPrimaryKey(confiGureModel.getSupplier_superior());
                //供应商商品规格id
                id = supplierConfigure.getId();
                //供应商商品id
                goodsId = productListModel.getId();

                //更新的库存
                // 1：当前用户购买的商品规格库存和商品总库存
                // 2：供应商对应商品总库存和对应商品规格库存
                this.synchronizationOtherStock(vo.getStoreId(), id, goodsId, stockNum, vo.isUpStockTotal());
            }
            //增加商品总库存
            int count = productListModelMapper.addGoodsStockNum(goodsId, stockNum);
            if (count < 1)
            {
                logger.debug("添加商品库存失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            ConfiGureModel confiGure = new ConfiGureModel();
            confiGure.setTotal_num(0);
            if (vo.isUpStockTotal())
            {
                //总库存只增不减
                if (stockNum > 0)
                {
                    confiGure.setTotal_num(stockNum);
                }
            }
            confiGure.setNum(stockNum);
            confiGure.setPid(goodsId);
            confiGure.setId(id);
            //刷新库存信息
            ConfiGureModel confiGureModel = confiGureModelMapper.selectByPrimaryKey(id);
            if (Objects.nonNull(confiGureModel))
            {
                count = confiGureModelMapper.addGoodsAttrStockNum(confiGure);
                if (count < 1)
                {
                    logger.debug("添加商品规格库存失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添视商品规格库存失败");
                }


                String text = vo.getText();
                if (StringUtils.isEmpty(text))
                {
                    text = uname + "增加商品规格库存" + stockNum;
                }
                StockModel stockModel = new StockModel();
                stockModel.setStore_id(product.getStore_id());
                stockModel.setProduct_id(confiGureModel.getPid());
                stockModel.setAttribute_id(confiGureModel.getId());
                stockModel.setTotal_num(confiGureModel.getTotal_num());
                stockModel.setFlowing_num(Math.abs(stockNum));
                if (stockNum > 0)
                {
                    stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
                }
                else
                {
                    stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
                }
                stockModel.setContent(text);
                stockModel.setMch_id(vo.getMchId());
                stockModel.setAdd_date(new Date());
                count = stockModelMapper.insertSelective(stockModel);
                if (count < 1)
                {
                    logger.debug("库存记录失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
                }
                //是否需要库存添加库存预警
                if (!this.addStockWarning(vo.getStoreId(), id))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
                }
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addGoodsStock");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsStock");
        }
    }

    /**
     * @param storeId        店铺id
     * @param cid            供应商商品最父级的configureId
     * @param pid            供应商商品最父级的pid
     * @param stockNum
     * @param isUpStockTotal
     * @throws LaiKeAPIException
     */
    @Override
    public void synchronizationOtherStock(int storeId, int cid, int pid, int stockNum, boolean isUpStockTotal) throws LaiKeAPIException
    {
        try
        {
            int              count;
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setSupplier_superior(pid);
            productListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            //查出此供应商的商品的子集，即所有店铺的代售商品集合
            List<ProductListModel> proList = productListModelMapper.select(productListModel);
            //1、同步代售商品主表总库存字段
            for (ProductListModel consignmentPro : proList)
            {
                count = productListModelMapper.addGoodsStockNum(consignmentPro.getId(), stockNum);
                if (count < 1)
                {
                    logger.debug("同步代售供应商商品库存失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
                }
            }

            //2、同步代售商品规格表库存字段
            ConfiGureModel query = new ConfiGureModel();
            query.setSupplier_superior(cid);
            query.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS.toString());
            List<ConfiGureModel> configureList = confiGureModelMapper.select(query);
            for (ConfiGureModel consignmentConfigure : configureList)
            {
                //说明库存已不正常,正常情况是所有子集的规格库存和商品主表的总库存的值应该分别相等
                if (consignmentConfigure.getNum() <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足", "synchronizationOtherStock");
                }
                ConfiGureModel updateConfigure = new ConfiGureModel();
                updateConfigure.setTotal_num(0);
                if (isUpStockTotal)
                {
                    //总库存只增不减
                    if (stockNum > 0)
                    {
                        updateConfigure.setTotal_num(stockNum);
                    }
                }
                updateConfigure.setNum(stockNum);
                updateConfigure.setPid(consignmentConfigure.getPid());
                updateConfigure.setId(consignmentConfigure.getId());
                count = confiGureModelMapper.addGoodsAttrStockNum(updateConfigure);
                if (count < 1)
                {
                    logger.debug("同步代售供应商商品规格库存失败");
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("同步代售供应商商品库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsStock");
        }
    }

    @Override
    public void addSupplierGoodsStock(AddStockVo vo, String uname) throws LaiKeAPIException
    {
        try
        {
            int id       = vo.getId();
            int stockNum = vo.getAddNum();
            int goodsId  = vo.getPid();
            //增加商品总库存
            int count = supplierProModelMapper.addGoodsStockNum(goodsId, stockNum);
            if (count < 1)
            {
                logger.debug("添加商品库存失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            SupplierConfigureModel confiGure = new SupplierConfigureModel();
            confiGure.setTotal_num(0);
            if (vo.isUpStockTotal())
            {
                //总库存只增不减
                if (stockNum > 0)
                {
                    confiGure.setTotal_num(stockNum);
                }
            }
            confiGure.setNum(stockNum);
            confiGure.setPid(goodsId);
            confiGure.setId(id);
            count = supplierConfigureModelMapper.addGoodsAttrStockNum(confiGure);
            if (count < 1)
            {
                logger.debug("添加商品规格库存失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //刷新库存信息
            SupplierConfigureModel confiGureModel = supplierConfigureModelMapper.selectByPrimaryKey(id);

            String text = vo.getText();
            if (StringUtils.isEmpty(text))
            {
                text = uname + "增加商品规格库存" + stockNum;
            }
            SupplierStockModel stockModel = new SupplierStockModel();
            stockModel.setStore_id(vo.getStoreId());
            stockModel.setProduct_id(confiGureModel.getPid());
            stockModel.setAttribute_id(confiGureModel.getId());
            stockModel.setTotal_num(confiGureModel.getTotal_num());
            stockModel.setFlowing_num(Math.abs(stockNum));
            if (stockNum > 0)
            {
                stockModel.setType(StockModel.StockType.STOCKTYPE_WAREHOUSING);
            }
            else
            {
                stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
            }
            stockModel.setContent(text);
            stockModel.setAdd_date(new Date());
            count = supplierStockModelMapperl.insertSelective(stockModel);
            if (count < 1)
            {
                logger.debug("库存记录失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //是否需要库存添加库存预警
            if (!this.addStockWarning(vo.getStoreId(), id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
        }
        catch (ClassCastException c)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSLXBZQ, "参数类型不正确", "addGoodsStock");
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加商品库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsStock");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refreshGoodsStock(int storeId, int goodsId) throws LaiKeAPIException
    {
        int stockNum = 0;
        try
        {
            ProductListModel productListModel = new ProductListModel();
            productListModel.setStore_id(storeId);
            productListModel.setId(goodsId);
            productListModel = productListModelMapper.selectOne(productListModel);
            if (productListModel != null)
            {
                stockNum = confiGureModelMapper.countConfigGureNum(goodsId);
                if (stockNum == productListModel.getNum())
                {
                    logger.debug("商品id{} 无需修正库存 原有库存{} 规格总库存{}", goodsId, productListModel.getNum(), stockNum);
                    return stockNum;
                }
                logger.debug("商品id{} 正在修正库存 原有库存{} 规格总库存{}", goodsId, productListModel.getNum(), stockNum);
                ProductListModel productListUpdate = new ProductListModel();
                productListUpdate.setId(goodsId);
                productListUpdate.setNum(stockNum);
                int row = productListModelMapper.updateByPrimaryKeySelective(productListUpdate);
                if (row > 0)
                {
                    logger.debug("商品id{} 库存已经修正", goodsId);
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("修正商品总库存 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "refreshGoodsStock");
        }
        return stockNum;
    }


    @Override
    public void outStockNum(int storeId, int goodsId, int attrId, int needNum, boolean isPlugin) throws LaiKeAPIException
    {
        try
        {
            ConfiGureModel confiGureModel = new ConfiGureModel();
            confiGureModel.setId(attrId);
            confiGureModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS + "");
            ConfiGureModel attr = confiGureModelMapper.selectOne(confiGureModel);
            if (attr == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GGBCZ, "规格不存在");
            }
            if (attr.getNum() < needNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_KCBZ, "库存不足");
            }
            logger.debug("规格id{} 出库{}个", attrId, needNum);
            String text = "商品规格出库" + needNum;
            if (isPlugin)
            {
                text = "插件添加商品," + text;
            }
            StockModel stockModel = new StockModel();
            stockModel.setStore_id(storeId);
            stockModel.setProduct_id(goodsId);
            stockModel.setAttribute_id(attrId);
            stockModel.setTotal_num(attr.getTotal_num());
            stockModel.setFlowing_num(needNum);
            stockModel.setType(StockModel.StockType.AGREEMENTTYPE_WAREHOUSING_OUT);
            stockModel.setContent(text);
            stockModel.setAdd_date(new Date());
            int count = stockModelMapper.insertSelective(stockModel);
            if (count < 1)
            {
                logger.debug("库存记录失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSB, "添加失败");
            }
            //扣除规格库存
            confiGureModelMapper.addGoodsAttrStockNumByPid(-needNum, attrId);
            //商品总库存
            productListModelMapper.addGoodsStockNum(goodsId, -needNum);
            if (!isPlugin)
            {
                //更新商品销量
                productListModelMapper.updateProductListVolume(needNum, storeId, goodsId);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("商品出库 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "outStockNum");
        }
    }

    @Override
    public void outStockNum(int storeId, int goodsId, int attrId, int needNum) throws LaiKeAPIException
    {
        try
        {
            outStockNum(storeId, goodsId, attrId, needNum, false);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
    }

}

