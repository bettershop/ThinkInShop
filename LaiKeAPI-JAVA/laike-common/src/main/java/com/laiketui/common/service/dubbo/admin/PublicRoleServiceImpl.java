package com.laiketui.common.service.dubbo.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.admin.PublicRoleService;
import com.laiketui.common.mapper.*;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.role.CoreMenuModel;
import com.laiketui.domain.vo.dic.DicVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 关于菜单、权限
 *
 * @author Trick
 * @date 2021/1/29 9:41
 */
@Service
public class PublicRoleServiceImpl implements PublicRoleService
{
    private final Logger logger = LoggerFactory.getLogger(PublicRoleServiceImpl.class);


    @Autowired
    private CoreMenuModelMapper coreMenuModelMapper;

    @Autowired
    private PublicDictionaryService publicDictionaryService;

    @Autowired
    private RoleMenuModelMapper roleMenuModelMapper;

    @Autowired
    private MenuModelMapper menuModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;
    @Autowired
    private AdminModelMapper adminModelMapper;

    @Override
    public List<Map<String, Object>> getMenuTreeList(Integer roleId) throws LaiKeAPIException
    {
        List<Map<String, Object>> menuTreeMap = new ArrayList<>();
        try
        {
            //获取所有平台
            DicVo dicVo = new DicVo();
            dicVo.setName("积分类型");
            Map<String, Object> headerMap = publicDictionaryService.getDictionaryByName(dicVo);
            String              json      = JSON.toJSONString(headerMap.get("value"));
            List<DictionaryListModel> dictionaryListModelList = JSON.parseObject(json, new TypeReference<List<DictionaryListModel>>()
            {
            });
            //头部索引
            Map<String, Map<String, Object>> headerIndexTempMap = new HashMap<>(16);
            //构造树形头部信息
            for (DictionaryListModel dictionary : dictionaryListModelList)
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("title", dictionary.getCtext());
                map.put("id", dictionary.getValue());
                map.put("checked", false);
                //添加索引
                headerIndexTempMap.put(dictionary.getValue(), map);
            }
            //获取当前角色拥有的权限
            List<Integer> currentMenus = new ArrayList<>();
            if (roleId != null)
            {
                currentMenus = roleMenuModelMapper.getUserRoleMenuInfoToId(roleId);
            }

            //获取所有菜单树形结构
            List<Map<String, Object>> dataList = coreMenuModelMapper.getCoreMenuInfoBySid(0, null);
            for (Map<String, Object> map : dataList)
            {
                int fatherId = Integer.parseInt(map.get("id").toString());
                //所属平台
                String type = String.valueOf(map.get("type"));
                //获取当前平台
                Map<String, Object> currentHeaderMap = headerIndexTempMap.get(type);
                if (currentHeaderMap.isEmpty())
                {
                    //如果没有所属则跳过当前菜单
                    logger.debug("菜单【{}】没有所属平台", map.get("title"));
                    continue;
                }
                //选中标识
                map.put("checked", false);
                //获取子菜单结构
                List<Map<String, Object>> childrenList = getRoleChildrenAll(fatherId, currentMenus);
                map.put("children", childrenList);
                if (childrenList != null && childrenList.size() > 0)
                {
                    //选中
                    map.put("checked", true);
                    currentHeaderMap.put("checked", true);
                    //归类
                    currentHeaderMap.put("children", childrenList);
                }
            }
            //整合数据
            for (String key : headerIndexTempMap.keySet())
            {
                menuTreeMap.add(headerIndexTempMap.get(key));
            }
            return menuTreeMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("更具菜单id获取树形结构 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMenuTreeList");
        }
    }

    @Override
    public List<Map<String, Object>> getRoleTreeList(int storeId, Integer roleId, Integer adminId, boolean isPt, String lang_code) throws LaiKeAPIException
    {
        List<Map<String, Object>> menuTreeMap = new ArrayList<>();
        try
        {
            List<Integer> currentMenus = new ArrayList<>();
            //需要筛选的菜单集合
            List<Integer> storeMenuId = new ArrayList<>();
            AdminModel    adminModel  = adminModelMapper.selectByPrimaryKey(adminId);
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QZXDL, "请重新登录");
            }
            //获取商城权限
            int storeRoleId;
            //是否平台账号
            boolean isPtUser = adminModel.getType().equals(AdminModel.TYPE_SYSTEM_ADMIN);
            //如果是平台菜单操作则获取所有菜单
            if (isPt && isPtUser)
            {
                //获取当前系统拥有的权限
                currentMenus = storeMenuId = coreMenuModelMapper.getSystemMenuList(lang_code);
            }
            else
            {
                if (storeId != 0)
                {
                    storeRoleId = adminModelMapper.getStoreRole(storeId);
                    //获取当前商城拥有的权限
                    currentMenus = storeMenuId = roleMenuModelMapper.getUserRoleMenuInfoToId(storeRoleId);
                }
            }
            if (roleId != null)
            {
                //获取指定角色列表中的菜单
                currentMenus = storeMenuId = roleMenuModelMapper.getUserRoleMenuInfoToId(roleId);
            }

            //获取所有菜单树形结构
            List<Map<String, Object>> resultMenuTreeMap = coreMenuModelMapper.getCoreMenuInfoBySid(0, lang_code);
            for (Map<String, Object> map : resultMenuTreeMap)
            {
                int type = MapUtils.getIntValue(map, "type");
                //不显示平台
                if (type == 0)
                {
                    continue;
                }
                int fatherId = Integer.parseInt(map.get("id").toString());

                //筛选一级菜单
//                if (storeMenuId != null && !storeMenuId.contains(fatherId))
//                {
//                    continue;
//                }
                //选中标识
                map.put("checked", false);
                //获取子菜单结构
                List<Map<String, Object>> childrenList = getRoleChildrenAll(fatherId, currentMenus, storeMenuId, null);
                if (childrenList != null && childrenList.size() > 0 && childrenList.get(0) != null)
                {
                    //选中
                    map.put("checked", true);
                }
                else if (childrenList != null && childrenList.size() > 0 && childrenList.get(0) == null)
                {
                    //去除之前的标识
                    childrenList.remove(0);
                }
                map.put("children", childrenList);
                menuTreeMap.add(map);
            }
            return menuTreeMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("更具权限id获取树形结构 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleTreeList");
        }
    }

    /**
     * 获取所有子权限菜单
     *
     * @param fatherId   - 上级id
     * @param roleMenuId - 当前角色拥有的菜单,用于选中
     * @return List -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 12:00
     */
    private List<Map<String, Object>> getRoleChildrenAll(int fatherId, List<Integer> roleMenuId) throws LaiKeAPIException
    {
        try
        {
            return getRoleChildrenAll(fatherId, roleMenuId, null, null);
        }
        catch (Exception e)
        {
            logger.error("获取所有子权限菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleChildrenAll");
        }
    }

    /**
     * 获取所有子权限菜单
     *
     * @param fatherId    - 上级id
     * @param roleMenuId  - 当前角色拥有的菜单,用于选中
     * @param storeMenuId - 如果不是平台则需要筛选菜单,平台有所有菜单权限,商户则只能分配商城有的权限D
     * @return List -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2022-05-18 17:57:03
     */
    private List<Map<String, Object>> getRoleChildrenAll(int fatherId, List<Integer> roleMenuId, List<Integer> storeMenuId, String lang_code) throws LaiKeAPIException
    {
        try
        {
            //是否选中上级标识
            boolean isShow = false;

            List<Map<String, Object>> dataList = coreMenuModelMapper.getCoreMenuInfoBySid(fatherId, lang_code);
            for (int i = 0; i < dataList.size(); i++)
            {
                Map<String, Object> map = dataList.get(i);
                int                 id  = Integer.parseInt(map.get("id").toString());
                //非平台筛选权限
//                if (storeMenuId != null)
//                {
//                    if (!storeMenuId.contains(id))
//                    {
//                       /* dataList.remove(i);
//                        i--;*/
//                        continue;
//                    }
//                }
                //选中标识
                map.put("checked", false);
                if (roleMenuId != null)
                {
                    //是否选中
                    if (roleMenuId.contains(id))
                    {
                        isShow = true;
                        map.put("checked", true);
                    }
                }
                List<Map<String, Object>> temp = getRoleChildrenAll(id, roleMenuId, storeMenuId, lang_code);
                if (temp != null && temp.size() > 0 && temp.get(0) == null)
                {
                    temp.remove(0);
                }
                map.put("children", temp);
            }
            if (!isShow)
            {
                dataList.add(0, null);
            }
            return dataList;
        }
        catch (Exception e)
        {
            logger.error("获取所有子权限菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleChildrenAll");
        }
    }

    @Override
    public void getRoleFatherById(int fatherId, List<CoreMenuModel> list) throws LaiKeAPIException
    {
        try
        {
            // 防止循环
            Set<Integer> visited   = new HashSet<>();
            int          currentId = fatherId;
            while (currentId > 0)
            {
                // 如果已经访问过，说明存在循环，直接退出
                if (visited.contains(currentId))
                {
                    logger.warn("发现循环菜单结构，id={}", currentId);
                    break;
                }
                visited.add(currentId);
                CoreMenuModel coreMenuModel = coreMenuModelMapper.selectByPrimaryKey(currentId);
                if (coreMenuModel == null)
                {
                    // 找不到父级就结束
                    break;
                }
                list.add(coreMenuModel);
                currentId = coreMenuModel.getS_id();
            }
        }
        catch (Exception e)
        {
            logger.error("获取当前权限id获取上级菜单信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleFatherById");
        }
    }

    @Override
    public Map<String, Object> getBindListInfo(int roleId, String name, int isBind) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap1 = new HashMap<>(16);
            parmaMap1.put("type", AdminModel.TYPE_CLIENT);
            if (isBind == 1)
            {
                parmaMap1.put("role", roleId);
            }
            else
            {
                parmaMap1.put("notRole", roleId);
            }
            parmaMap1.put("name", name);
            //获取绑定的商户
            List<Map<String, Object>> bindAdminList = adminModelMapper.getBindListInfo(parmaMap1);

            resultMap.put("bindAdminList", bindAdminList);
        }
        catch (Exception e)
        {
            logger.error("获取所有商户 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getBindListInfo");
        }
        return resultMap;
    }

    @Override
    public boolean verificationBind(List<Integer> adminIds) throws LaiKeAPIException
    {
        try
        {
            for (Integer adminId : adminIds)
            {
                int count = adminModelMapper.verificationBind(adminId);
                if (count > 0)
                {
                    return true;
                }
            }
            return false;
        }
        catch (Exception e)
        {
            logger.error("验证商户是否绑定角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "verificationBind");
        }
    }

    @Override
    public void delMenuId(List<Integer> menuIds, int level) throws LaiKeAPIException
    {
        try
        {

        }
        catch (Exception e)
        {
            logger.error("删除权限菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "删除权限菜单", "delMenuId");
        }
    }
}

