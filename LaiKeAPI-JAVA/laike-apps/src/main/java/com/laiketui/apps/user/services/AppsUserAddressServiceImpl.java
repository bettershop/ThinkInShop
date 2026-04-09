package com.laiketui.apps.user.services;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.laiketui.apps.api.user.AppsUserAddressService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.mapper.FreightModelMapper;
import com.laiketui.common.mapper.ProductListModelMapper;
import com.laiketui.common.mapper.UserAddressMapper;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.distribution.FreightModel;
import com.laiketui.domain.product.ProductListModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.user.UserAddress;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.SaveAddressVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 我的地址实现
 *
 * @author Trick
 * @date 2020/11/4 17:02
 */
@Service
public class AppsUserAddressServiceImpl implements AppsUserAddressService
{

    private final Logger logger = LoggerFactory.getLogger(AppsUserAddressServiceImpl.class);

    @Autowired
    private ProductListModelMapper productListModelMapper;
    @Autowired
    private FreightModelMapper     freightModelMapper;
    @Autowired
    private AdminCgModelMapper     adminCgModelMapper;

    @Override
    public Map<String, Object> index(MainVo vo, String pid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //查询地址列表
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("uid", user.getUser_id());
            parmaMap.put("id_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> userAddressList = userAddressMapper.selectDynamic(parmaMap);
            //可配送列表
            List<Map<String, Object>> deliveryList = new ArrayList<>();
            //不可配送列表
            List<Map<String, Object>> notDeliveryList = new ArrayList<>();
            //不能配送的地区列表
            List<String> freightNotDelivery = new ArrayList<>();
            if (StringUtils.isNotEmpty(pid))
            {
                String[] pIdList = pid.split(SplitUtils.DH);
                for (String goodsId : pIdList)
                {
                    ProductListModel productListModel = productListModelMapper.selectByPrimaryKey(goodsId);
                    FreightModel     freightModel     = freightModelMapper.selectByPrimaryKey(productListModel.getFreight());
                    if (StringUtils.isEmpty(freightModel.getNo_delivery()))
                    {
                        continue;
                    }
                    List<String> stringList = JSONArray.parseArray(freightModel.getNo_delivery(), String.class);
                    freightNotDelivery.addAll(stringList);
                }
            }
            userAddressList.forEach(userAddress ->
            {
                String shen        = MapUtils.getString(userAddress, "sheng");
                String city        = MapUtils.getString(userAddress, "city");
                String quyu        = MapUtils.getString(userAddress, "quyu");
                String addressInfo = shen + "-" + city + "-" + quyu;
                if (freightNotDelivery.contains(addressInfo))
                {
                    notDeliveryList.add(userAddress);
                }
                else
                {
                    deliveryList.add(userAddress);
                }
            });
            resultMap.put("adds", deliveryList);
            resultMap.put("adds0", notDeliveryList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加地址首页异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAddress(SaveAddressVo vo) throws LaiKeAPIException
    {
        try
        {
            //校验手机号是否正确
            //字符串长度限制
            Integer least   = 1;
            Integer longest = 10;
            if (!DataCheckTool.checkLength(vo.getUserName(), least, longest))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SHRMCGSBZQW, "收货人名称格式不正确(1-10位)");
            }
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //是否为默认
            int isDefault = vo.getIsDefault();
            //拆分省市县
            StringBuilder addressXq = new StringBuilder("");
            String[] place = new String[3];
            if (StringUtils.isNotEmpty(vo.getPlace()))
            {
                place = vo.getPlace().split(SplitUtils.HG);
                for (String str : place)
                {
                    addressXq.append(str);
                }
            }

            addressXq.append(vo.getAddress());
            //获取当前用户默认地址
            UserAddress userAddress = new UserAddress();
            userAddress.setStore_id(vo.getStoreId());
            userAddress.setUid(user.getUser_id());
            userAddress.setIs_default(UserAddress.DefaultMaven.DEFAULT_OK);
            //判断是否为默认地址模式
            if (isDefault == UserAddress.DefaultMaven.DEFAULT_OK)
            {
                userAddress = userAddressMapper.selectOne(userAddress);
                if (userAddress != null)
                {
                    //重置当前默认的地址
                    UserAddress updateUserAddress = new UserAddress();
                    updateUserAddress.setId(userAddress.getId());
                    updateUserAddress.setUid(user.getUser_id());
                    updateUserAddress.setIs_default(UserAddress.DefaultMaven.DEFAULT_NO);
                    int count = userAddressMapper.updateById(updateUserAddress);
                    if (count < 1)
                    {
                        logger.info("用户:" + user.getUser_id() + "重置默认地址失败");
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJDZSB, "添加地址失败");
                    }
                }
            }
            else
            {
                //判断是否有默认地址,没有则当条为默认
                if (userAddressMapper.selectCount(userAddress) < 1)
                {
                    isDefault = UserAddress.DefaultMaven.DEFAULT_OK;
                }
            }

            //添加地址
            UserAddress addUserAddress = new UserAddress();
            addUserAddress.setStore_id(vo.getStoreId());
            addUserAddress.setName(vo.getUserName());
            addUserAddress.setTel(vo.getMobile());
            if (place != null && place.length > 0)
            {
                addUserAddress.setSheng(place[0]);
                addUserAddress.setCity(place[1]);
                addUserAddress.setQuyu(place[2]);
            }

            addUserAddress.setCpc(vo.getCpc());
            //address = address_xq 国外只用这个
            addUserAddress.setAddress(vo.getAddress());
            addUserAddress.setAddress_xq(addressXq.toString());
            addUserAddress.setUid(user.getUser_id());
            addUserAddress.setIs_default(isDefault);

            int count = userAddressMapper.insertSelective(addUserAddress);
            if (count < 1)
            {
                logger.info("用户:" + user.getUser_id() + "添加地址失败");
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_TJDZSB, "添加地址失败");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("保存地址异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "saveAddress");
        }
    }

    public static void main(String[] args)
    {
        boolean mobile = MobileUtils.isMobile("020-81167888");
        System.out.println(mobile);
    }


    @Override
    public Map<String, Object> getAddressById(MainVo vo, int addrId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            UserAddress userAddress = userAddressMapper.selectByPrimaryKey(addrId);
            userAddress.setProvince(userAddress.getSheng());
            resultMap.put("address", userAddress);
            resultMap.put("province", userAddress.getSheng());
            resultMap.put("city_id", userAddress.getCity());
            resultMap.put("county", userAddress.getQuyu());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据id获取地址信息异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAddressById");
        }

        return resultMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(SaveAddressVo vo, int addsId) throws LaiKeAPIException
    {
        try
        {

            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //是否为默认
            int isDefault = vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK ? DictionaryConst.DefaultMaven.DEFAULT_OK : DictionaryConst.DefaultMaven.DEFAULT_NO;

            UserAddress userAddress = new UserAddress();
            userAddress.setUid(user.getUser_id());
            if (isDefault == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                //把之前的默认地址置为非默认
                userAddress.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_NO);
                int count = userAddressMapper.updateById(userAddress);
                if (count < 1)
                {
                    logger.info("把之前的默认地址置为非默认失败 参数:" + JSON.toJSONString(userAddress));
                    return false;
                }
            }
            userAddress.setId(addsId);
            userAddress.setName(vo.getUserName());
            userAddress.setTel(vo.getMobile());

            String sheng = "";
            String city = "";
            String quyu = "";


            //合并地址
            StringBuilder addressXq = new StringBuilder("");
            String[] place = new String[3];
            if (StringUtils.isNotEmpty(vo.getPlace()))
            {
                place = vo.getPlace().split(SplitUtils.HG);
                for (String str : place)
                {
                    addressXq.append(str);
                }
            }

            addressXq.append(vo.getAddress());


            if (place.length > 0)
            {
                sheng = place[0];
                city = place[1];
                quyu = place[2];
            }

            if(!Objects.equals(vo.getCpc(),"86") && !Objects.equals(vo.getCpc(),"852"))
            {
                if (StringUtils.isNotEmpty(vo.getProvince()))
                {
                    sheng = vo.getProvince();
                }
                if (StringUtils.isNotEmpty(vo.getCity()))
                {
                    city = vo.getCity();
                }
                addressXq = new StringBuilder(vo.getAddress());
                addressXq.append(city);
                addressXq.append(sheng);
                quyu = null;
            }

            userAddress.setSheng(sheng);
            userAddress.setCity(city);
            userAddress.setQuyu(quyu);

            userAddress.setCpc(vo.getCpc());
            userAddress.setAddress(vo.getAddress());
            userAddress.setAddress_xq(addressXq.toString());
            userAddress.setIs_default(isDefault);
            int count = userAddressMapper.updateById(userAddress);
            if (count < 1)
            {
                logger.info("地址修改失败 参数:" + JSON.toJSONString(userAddress));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            else
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
            logger.error("修改地址 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "updateAddress");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(MainVo vo, int addsId) throws LaiKeAPIException
    {
        try
        {
            User        user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            UserAddress userAddress = new UserAddress();
            userAddress.setUid(user.getUser_id());
            userAddress.setIs_default(UserAddress.DefaultMaven.DEFAULT_NO);
            int count = userAddressMapper.updateById(userAddress);
            if (count < 1)
            {
                logger.info("把之前的默认地址置为非默认失败 参数:" + JSON.toJSONString(userAddress));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            userAddress.setId(addsId);
            userAddress.setIs_default(UserAddress.DefaultMaven.DEFAULT_OK);
            count = userAddressMapper.updateById(userAddress);
            if (count < 1)
            {
                logger.info("设置默认地址失败 参数:" + JSON.toJSONString(userAddress));
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            else
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
            logger.error("设置默认地址 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefaultAddress");
        }
    }

    @Override
    public Map<String, Object> getJoinCityCounty(MainVo vo, int level, int groupId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap.put("list", publiceService.getJoinCityCounty(level, groupId));
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
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delAdds(MainVo vo, int addrId) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, true);
            //获取当前需要删除地址的信息
            UserAddress userAddress = new UserAddress();
            userAddress.setId(addrId);
            userAddress.setUid(user.getUser_id());
            userAddress = userAddressMapper.selectOne(userAddress);
            if (userAddress != null)
            {
                int count;
                count = userAddressMapper.deleteByPrimaryKey(userAddress);
                if (count < 1)
                {
                    logger.info("删除地址失败 参数:" + JSON.toJSONString(userAddress));
                }
                //是否只有一条了,只有一条不做默认逻辑
                UserAddress userAddressCount = new UserAddress();
                userAddressCount.setStore_id(vo.getStoreId());
                userAddressCount.setUid(user.getUser_id());
                if (userAddressMapper.selectCount(userAddressCount) > 0)
                {
                    if (userAddress.getIs_default() == UserAddress.DefaultMaven.DEFAULT_OK)
                    {
                        //设置默认地址
                        count = userAddressMapper.setDefaultAddress(user.getUser_id());
                        if (count < 1)
                        {
                            logger.info("设置默认地址失败 参数: userid=" + user.getUser_id());
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FWQFM, "服务器繁忙");
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
            logger.error("收货地址删除 异常: ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAdds");
        }
    }

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;
}

