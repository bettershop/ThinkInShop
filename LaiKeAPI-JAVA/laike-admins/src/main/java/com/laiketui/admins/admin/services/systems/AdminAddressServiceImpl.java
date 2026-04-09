package com.laiketui.admins.admin.services.systems;

import com.laiketui.admins.api.admin.systems.AdminAddressService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.ServiceAddressModelMapper;
import com.laiketui.common.utils.tool.MobileUtils;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.ServiceAddressModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.systems.AddressVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址管理
 *
 * @author Trick
 * @date 2021/1/15 9:28
 */
@Service
public class AdminAddressServiceImpl implements AdminAddressService
{
    private final Logger logger = LoggerFactory.getLogger(AdminAddressServiceImpl.class);

    @Override
    public Map<String, Object> getAddressInfo(MainVo vo, Integer id, String name, Integer type) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            if (type == null || type < AddressType.SHIPPING_ADDRESS)
            {
                type = ServiceAddressModel.TYPE_RETURN_GOODS;
            }
            //设置默认地址
            int count = serviceAddressModelMapper.setDefualtAddress(vo.getStoreId(), type);
            logger.debug("是否存没有默认地址 {}", count > 0);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("uid", "admin");
            if (StringUtils.isNotEmpty(name))
            {
                parmaMap.put("nameOrTel", name);
            }
            if (id != null && id > 0)
            {
                parmaMap.put("id", id);
            }
            parmaMap.put("is_default_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("type", type);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = serviceAddressModelMapper.selectDynamic(parmaMap);
            int                       total    = serviceAddressModelMapper.countDynamic(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取地址列表 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAddressInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addAddressInfo(AddressVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //邮政编码数字限制
            int                 length                 = 6;
            int                 count;
            ServiceAddressModel serviceAddressModelOld = null;
            if (vo.getId() != null && vo.getId() > 0)
            {
                serviceAddressModelOld = new ServiceAddressModel();
                serviceAddressModelOld.setStore_id(vo.getStoreId());
                serviceAddressModelOld.setId(vo.getId());
                serviceAddressModelOld = serviceAddressModelMapper.selectOne(serviceAddressModelOld);
                if (serviceAddressModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZBCZ, "地址不存在");
                }
            }
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXRBNWK, "联系人不能为空");
            }
            if (vo.getType() == null || vo.getType() < AddressType.SHIPPING_ADDRESS)
            {
                vo.setType(ServiceAddressModel.TYPE_RETURN_GOODS);
            }
            if (StringUtils.isEmpty(vo.getAddress()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XXDZBNWK, "详细地址不能为空");
            }
            if (vo.getCode() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YZBMBNWK, "邮政编码不能为空");
            }

            if (StringUtils.isEmpty(vo.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXRDHBNWK, "联系人电话不能为空");
            }
           /* else if (!MobileUtils.isMobile(vo.getTel()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJHGSBZQ, "手机号格式不正确");
            }*/
            else
            {
                if (serviceAddressModelOld == null || !vo.getTel().equals(serviceAddressModelOld.getTel()))
                {
                    //验证电话是否存在
                    ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
                    serviceAddressModel.setStore_id(vo.getStoreId());
                    serviceAddressModel.setUid("admin");
                    serviceAddressModel.setTel(vo.getTel());
                    serviceAddressModel.setCpc(vo.getCpc());
                    serviceAddressModel.setType(vo.getType());
                    count = serviceAddressModelMapper.selectCount(serviceAddressModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LXDHYCZ, "联系电话已存在");
                    }
                }
            }
            //如果是默认,则取消之前的默认
            if (vo.getIsDefault() == DictionaryConst.DefaultMaven.DEFAULT_OK)
            {
                serviceAddressModelMapper.updateDefualtClear(vo.getStoreId(), vo.getType());
            }

            String sheng = vo.getShen();
            String shi = vo.getShi();

            //添加/修改地址
            ServiceAddressModel serviceAddressModelSave = new ServiceAddressModel();

            if (StringUtils.isNotEmpty(vo.getProvince()))
            {
                sheng = vo.getProvince();
            }
            if (StringUtils.isNotEmpty(vo.getCity()))
            {
                shi = vo.getCity();
            }

            //如果地址不是本国的，详细地址需要倒过来
            String address = vo.getAddress() + shi + sheng;
            if (vo.getCpc().equals("86") || vo.getCpc().equals("852"))
            {
                address = sheng + shi + vo.getXian() + vo.getAddress();
            }
            serviceAddressModelSave.setUid("admin");
            serviceAddressModelSave.setTel(vo.getTel());
            serviceAddressModelSave.setName(vo.getName());
            serviceAddressModelSave.setCode(vo.getCode());
            serviceAddressModelSave.setType(vo.getType());
            serviceAddressModelSave.setSheng(sheng);
            serviceAddressModelSave.setCpc(vo.getCpc());
            serviceAddressModelSave.setCountry_num(vo.getCountry_num());
            serviceAddressModelSave.setShi(shi);
            serviceAddressModelSave.setXian(vo.getXian());
            serviceAddressModelSave.setAddress(address);
            serviceAddressModelSave.setIs_default(vo.getIsDefault());
            serviceAddressModelSave.setAddress_xq(address);

            if (serviceAddressModelOld != null)
            {
                serviceAddressModelSave.setId(vo.getId());
                count = serviceAddressModelMapper.updateByPrimaryKeySelective(serviceAddressModelSave);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了售后地址ID：" + serviceAddressModelSave.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                serviceAddressModelSave.setStore_id(vo.getStoreId());
                serviceAddressModelSave.setCode(vo.getCode());
                count = serviceAddressModelMapper.insertSelective(serviceAddressModelSave);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了" + serviceAddressModelSave.getAddress_xq() + " 的售后地址", AdminRecordModel.Type.ADD, vo.getAccessId());

            }

            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑地址信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAddressInfo");
        }
    }

    @Override
    public void setDefaultAddress(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user                   = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ServiceAddressModel serviceAddressModelOld = new ServiceAddressModel();
            serviceAddressModelOld.setStore_id(vo.getStoreId());
            serviceAddressModelOld.setId(id);
            serviceAddressModelOld = serviceAddressModelMapper.selectOne(serviceAddressModelOld);
            if (serviceAddressModelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZBCZ, "地址不存在");
            }
            int isDefault = 1;
            if (serviceAddressModelOld.getIs_default() == isDefault)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DQDZYSMR, "当前地址已是默认");
            }
            //取消之前的默认
            serviceAddressModelMapper.updateDefualtClear(vo.getStoreId(), serviceAddressModelOld.getType());
            //设置当前为默认
            ServiceAddressModel serviceAddressUpdate = new ServiceAddressModel();
            serviceAddressUpdate.setIs_default(isDefault);
            serviceAddressUpdate.setId(id);
            int row = serviceAddressModelMapper.updateByPrimaryKeySelective(serviceAddressUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SZSB, "设置失败", "setDefaultAddress");
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将售后地址ID：" + serviceAddressUpdate.getId() + "设为了默认", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("设置默认 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "setDefaultAddress");
        }
    }

    @Override
    public boolean delAddress(MainVo vo, int id, int type) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user                = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ServiceAddressModel serviceAddressModel = new ServiceAddressModel();
            serviceAddressModel.setStore_id(vo.getStoreId());
            serviceAddressModel.setId(id);
            serviceAddressModel = serviceAddressModelMapper.selectOne(serviceAddressModel);
            if (serviceAddressModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_DZBCZ, "地址不存在");
            }
            int count = serviceAddressModelMapper.deleteByPrimaryKey(id);
            if (count > 0)
            {
                //设置默认
                count = serviceAddressModelMapper.setDefualtAddress(vo.getStoreId(), type);
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了售后地址ID：" + id + "的信息", AdminRecordModel.Type.DEL, vo.getAccessId());
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("删除地址 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAddress");
        }
    }


    @Autowired
    private ServiceAddressModelMapper serviceAddressModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PubliceService publiceService;

}

