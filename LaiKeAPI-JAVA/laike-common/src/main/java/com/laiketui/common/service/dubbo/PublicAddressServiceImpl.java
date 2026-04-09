package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicAddressService;
import com.laiketui.common.mapper.AdminCgModelMapper;
import com.laiketui.common.mapper.UserAddressMapper;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.AdminCgModel;
import com.laiketui.domain.user.UserAddress;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通用地址服务
 *
 * @author wangxian
 */
@Service("commonAddressService")
public class PublicAddressServiceImpl implements PublicAddressService
{

    private final Logger logger = LoggerFactory.getLogger(PublicAddressServiceImpl.class);

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public UserAddress findAddress(Map<String, Object> map)
    {
        UserAddress userAddress = new UserAddress();
        try
        {
            userAddress.setUid(MapUtils.getString(map, "user_id"));
            userAddress.setStore_id(MapUtils.getInteger(map, "store_id"));
            Object addressIdObj = map.get("address_id");
            if (addressIdObj != null)
            {
                userAddress.setId((Integer) addressIdObj);
            }
            /**
             * 根据不同的条件来查找用户的地址
             */
            List<UserAddress> retList = userAddressMapper.select(userAddress);
            if (!CollectionUtils.isEmpty(retList))
            {
                return retList.get(0);
            }
            else
            {
                userAddress.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                retList = userAddressMapper.select(userAddress);
                if (!CollectionUtils.isEmpty(retList))
                {
                    userAddress = retList.get(0);
                }
                else
                {
                    userAddress.setIs_default(null);
                    retList = userAddressMapper.select(userAddress);
                    if (!CollectionUtils.isEmpty(retList))
                    {
                        userAddress = retList.get(0);
                        userAddress.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                        userAddressMapper.updateByPrimaryKeySelective(userAddress);
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            return null;
        }
        return userAddress;
    }

    @Override
    public UserAddress findAddress(int storeId, String uid, Integer id) throws LaiKeAPIException
    {
        UserAddress resultUserAddress;
        try
        {
            //获取收货地址信息
            UserAddress userAddress = new UserAddress();
            userAddress.setStore_id(storeId);
            userAddress.setUid(uid);
            if (id != null && id > 0)
            {
                userAddress.setId(id);
                resultUserAddress = userAddressMapper.selectOne(userAddress);
                if (resultUserAddress != null)
                {
                    return resultUserAddress;
                }
            }
            //获取用户默认地址
            userAddress.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
            resultUserAddress = userAddressMapper.selectOne(userAddress);
            if (resultUserAddress == null)
            {
                //获取最新地址
                resultUserAddress = userAddressMapper.getUserAddress(storeId, uid);
                if (resultUserAddress == null)
                {
                    //没有地址
                    return null;
                }
                //把最新地址设置成默认地址
                UserAddress userAddressUpdate = new UserAddress();
                userAddressUpdate.setId(userAddress.getId());
                userAddressUpdate.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                int count = userAddressMapper.updateByPrimaryKeySelective(userAddressUpdate);
                if (count > 0)
                {
                    resultUserAddress.setIs_default(DictionaryConst.DefaultMaven.DEFAULT_OK);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("获取用户地址 异常:", e);
            return null;
        }
        return resultUserAddress;
    }

    @Override
    public List<Integer> findAddressIdByName(int storeId, List<String> addressName) throws LaiKeAPIException
    {
        List<Integer> addressIdList = new ArrayList<>();
        try
        {
            if (addressName == null || addressName.size() != 3)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //全名称
            String addressNameStr = StringUtils.stringImplode(addressName, SplitUtils.HG);
            if (addressNameStr == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            //匹配的名称
            StringBuilder addressNameTemp;
            //获取当前区名称 再继续往上找,然后匹配全名称
            AdminCgModel adminCgModel = new AdminCgModel();
            adminCgModel.setDistrictName(addressName.get(2));
            List<AdminCgModel> countyList = adminCgModelMapper.select(adminCgModel);
            for (AdminCgModel qu : countyList)
            {
                addressNameTemp = new StringBuilder();
                if (!addressNameStr.contains(qu.getDistrictName()))
                {
                    //如果区名称匹配不到则下一个
                    continue;
                }
                addressNameTemp.insert(0, SplitUtils.HG).insert(0, qu.getDistrictName());
                //获取市
                AdminCgModel shi = adminCgModelMapper.selectByPrimaryKey(qu.getDistrictPid());
                if (!addressNameStr.contains(shi.getDistrictName()))
                {
                    //如果区名称匹配不到则下一个
                    continue;
                }
                addressNameTemp.insert(0, SplitUtils.HG).insert(0, shi.getDistrictName());
                //获取省
                AdminCgModel shen = adminCgModelMapper.selectByPrimaryKey(shi.getDistrictPid());
                if (!addressNameStr.contains(shen.getDistrictName()))
                {
                    //如果区名称匹配不到则下一个
                    continue;
                }
                addressNameTemp.insert(0, SplitUtils.HG).insert(0, shen.getDistrictName());
                //是否可以匹配上,匹配上则退出循环
                if (StringUtils.trim(addressNameTemp.toString(), SplitUtils.HG).equals(addressNameStr))
                {
                    addressIdList.add(shen.getId());
                    addressIdList.add(shi.getId());
                    addressIdList.add(qu.getId());
                    break;
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("根据 省市县名称获取地址id 失败:", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据 省市县名称获取地址id 异常:", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_HQDZXXYC, "获取地址信息异常", "findAddressIdByName");
        }
        return addressIdList;
    }


    @Autowired
    private AdminCgModelMapper adminCgModelMapper;

}

