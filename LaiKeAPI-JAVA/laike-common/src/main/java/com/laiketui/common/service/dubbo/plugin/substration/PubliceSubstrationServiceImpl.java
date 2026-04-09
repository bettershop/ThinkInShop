package com.laiketui.common.service.dubbo.plugin.substration;

import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.plugin.substration.PubliceSubstrationService;
import com.laiketui.common.mapper.AdminModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.SubtractionModalMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.subtraction.SubtractionModal;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 满减公共方法
 *
 * @author Trick
 * @date 2021/4/16 9:52
 */
@Service
public class PubliceSubstrationServiceImpl implements PubliceSubstrationService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SubtractionModalMapper subtractionModalMapper;

    @Autowired
    private ProductListModelMapper productListModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Override
    public Map<String, Object> getSubstrationGoodsList(MainVo vo, User user, int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //获取店铺
            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(vo.getStoreId());
            adminModel.setType(AdminModel.TYPE_CLIENT);
            adminModel = adminModelMapper.selectOne(adminModel);
            int mchId = adminModel.getShop_id();
            //获取满减活动信息
            SubtractionModal subtractionModal = new SubtractionModal();
            subtractionModal.setStore_id(vo.getStoreId());
            subtractionModal.setId(id);
            subtractionModal = subtractionModalMapper.selectOne(subtractionModal);
            if (subtractionModal == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MJHDBCZ, "满减活动不存在");
            }

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("class_sort_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("upper_shelf_time_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageNo", vo.getPageNo());
            parmaMap.put("pageSize", vo.getPageSize());
            //满减范围参数
            List<String> idList = new ArrayList<>();
            if (!StringUtils.isEmpty(subtractionModal.getSubtraction_parameter()))
            {
                idList = Arrays.asList(subtractionModal.getSubtraction_parameter().split(","));
            }
            //指定分类
            if ("1".equals(subtractionModal.getSubtraction_range()))
            {
                parmaMap.put("likeClassIdList", idList);
            }
            else if ("3".equals(subtractionModal.getSubtraction_range()))
            {
                //指定品牌
                parmaMap.put("brandIdList", idList);
            }
            //店铺
            if (subtractionModal.getSubtraction_type() == 3)
            {
                parmaMap.put("mch_id", mchId);
            }
            List<Map<String, Object>> goodsList = productListModelMapper.getProductListDynamic(parmaMap);
            for (Map<String, Object> map : goodsList)
            {
                BigDecimal goodsPrice = new BigDecimal(map.get("price").toString());
                map.put("vip_yprice", goodsPrice);
                BigDecimal grade = publiceService.getUserGradeRate(vo.getStoreId(), user);
                map.put("vip_price", goodsPrice.multiply(grade));
                map.put("imgurl", publiceService.getImgPath(map.get("imgurl").toString(), vo.getStoreId()));
            }

            resultMap.put("title", subtractionModal.getTitle());
            resultMap.put("name", subtractionModal.getTitle());
            resultMap.put("list", goodsList);
            resultMap.put("starttime", DateUtil.dateFormate(subtractionModal.getStarttime(), GloabConst.TimePattern.YMDHMS));
            resultMap.put("endtime", DateUtil.dateFormate(subtractionModal.getEndtime(), GloabConst.TimePattern.YMDHMS));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取满减商品列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getSubstrationGoodsList");
        }
        return resultMap;
    }
}

