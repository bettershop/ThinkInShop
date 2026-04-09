package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminLabelService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ProLabelModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.ProLabelModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品标签
 *
 * @author Trick
 * @date 2021/6/25 18:07
 */
@Service
public class AdminLabelServiceImpl implements AdminLabelService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProLabelModelMapper proLabelModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> index(MainVo vo, String name, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("id", id);
            parmaMap.put("name", name);
            parmaMap.put("add_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("lang_code", vo.getLang_code());
            parmaMap.put("country_num", vo.getCountry_num());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = proLabelModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> list  = proLabelModelMapper.selectDynamic(parmaMap);

            for (Map<String, Object> map : list)
            {
                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                map.put("country_name", publiceService.getCountryName(MapUtils.getInteger(map, "country_num")));
            }

            resultMap.put("list", list);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Override
    public void addGoodsLabel(MainVo vo, String name, Integer id, String color) throws LaiKeAPIException
    {
        try
        {
            int           row;
            AdminModel    userCache    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProLabelModel proLabelSave = new ProLabelModel();
            ProLabelModel proLabelOld  = null;
            if (id != null)
            {
                proLabelOld = proLabelModelMapper.selectByPrimaryKey(id);
                if (proLabelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBQBCZ, "商品标签不存在");
                }
            }
            proLabelSave.setName(name);
            proLabelSave.setColor(color);
            proLabelSave.setCountry_num(vo.getCountry_num());
            proLabelSave.setLang_code(vo.getLang_code());
            proLabelSave.setAdd_time(new Date());
            if (proLabelOld == null || !proLabelOld.getName().equals(name))
            {
                ProLabelModel proLabelModel = new ProLabelModel();
                proLabelModel.setStore_id(vo.getStoreId());
                proLabelModel.setName(name);
                int count = proLabelModelMapper.selectCount(proLabelModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBQYCZ, "商品标签已存在");
                }
            }
            if (proLabelOld != null)
            {
                proLabelSave.setId(id);
                row = proLabelModelMapper.updateByPrimaryKeySelective(proLabelSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了标签ID：" + proLabelSave.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                proLabelSave.setStore_id(vo.getStoreId());
                row = proLabelModelMapper.insertSelective(proLabelSave);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了标签ID：" + proLabelSave.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());


            }

            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJSPBQSB, "添加商品标签失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑商品标签 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addGoodsLabel");
        }
    }

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Override
    public void delGoodsLabel(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            int           row;
            AdminModel    userCache   = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ProLabelModel proLabelOld = proLabelModelMapper.selectByPrimaryKey(id);
            if (proLabelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SPBQBCZ, "商品标签不存在");
            }
            row = proLabelModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSPBQSB, "删除商品标签失败");
            }
            //同步删除所有商品所绑定的该标签 禅道45881
            List<Integer> proByLabel = productListModelMapper.getProByLabel(id);
            if (proByLabel != null && proByLabel.size() > 0)
            {
                for (Integer proId : proByLabel)
                {
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(proId);
                    String           sType            = productListModel.getS_type();
                    sType = sType.replaceAll(SplitUtils.DH + id, "");
                    if (sType.equals(SplitUtils.DH))
                    {
                        sType = "";
                    }
                    productListModel.setS_type(sType);
                    productListModelMapper.updateByPrimaryKeySelective(productListModel);
                }
            }
            publiceService.addAdminRecord(vo.getStoreId(), "删除了标签ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除商品标签 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delGoodsLabel");
        }
    }


}

