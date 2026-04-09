package com.laiketui.admins.admin.services.mch;

import com.laiketui.admins.admin.services.supplier.AdminSupplierClassServiceImpl;
import com.laiketui.admins.api.admin.mch.AdminMchAuthorityService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.authority.AuthorityMappingModel;
import com.laiketui.domain.authority.MenuModel;
import com.laiketui.domain.authority.UserAuthorityModel;
import com.laiketui.domain.authority.UserRoleModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.MchModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;
import com.laiketui.domain.vo.mch.pc.AddRoleMainVo;
import com.laiketui.domain.vo.user.UserRegisterVo;
import com.laiketui.root.common.BuilderIDTool;
import com.laiketui.root.license.CryptoUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: sunH_
 * @Date: Create in 15:18 2023/7/4
 */
@Service
public class AdminMchAuthorityServiceImpl implements AdminMchAuthorityService
{

    private final Logger logger = LoggerFactory.getLogger(AdminSupplierClassServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MenuModelMapper menuModelMapper;

    @Autowired
    private AuthorityMappingModelMapper authorityMappingModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private UserRoleModelMapper userRoleModelMapper;

    @Autowired
    private UserAuthorityModelMapper userAuthorityModelMapper;

    @Autowired
    private PublicUserService publicUserService;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Override
    public Map<String, Object> getMenuList(MainVo vo, String name, String id, String sid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap   = new HashMap<>(16);
            if (StringUtils.isEmpty(sid))
            {
                sid = "0";
            }
            else
            {
                MenuModel menuModel = menuModelMapper.selectByPrimaryKey(sid);
                if (menuModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCDBCZ, "上级菜单不存在");
                }
                resultMap.put("fatherName", menuModel.getName());
            }
            if (StringUtils.isNotEmpty(name))
            {
                paramMap.put("key", name);
            }
            paramMap.put("is_display_off", "is_display_off");
            if (StringUtils.isNotEmpty(id))
            {
                paramMap.put("id", id);
            }
            else
            {
                paramMap.put("sid", sid);
            }
//            int mchId = user.getMchId();
//            //获取当前商城自营店
//            Integer zyMchId = customerModelMapper.getStoreMchId(user.getStore_id());
//            //如果自营店是自己则获取系统菜单+自定义菜单
//            if (zyMchId != null && mchId == zyMchId) {
//                Integer[] mchIdList = {mchId, 0};
//                paramMap.put("main_ids", mchIdList);
//            } else {
//                //如果是店主则获取所有系统菜单+自定义菜单,如果是管理员则获取管理员自己的菜单
//                if (mchModelMapper.countMchIsByUser(vo.getStoreId(), user.getUser_id()) > 0) {
//                    Integer[] mchIdList = {mchId, 0};
//                    paramMap.put("main_ids", mchIdList);
//                } else {
//                    //普通管理员
//                    paramMap.put("user_id", user.getUser_id());
//                    paramMap.put("authority_type", UserAuthorityModel.USER_ID);
//                }
//            }
            if (!paramMap.containsKey("authority_type"))
            {
                paramMap.put("type", GloabConst.MenuType.MENU_MCH_ID);
            }
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            int                       total    = menuModelMapper.countMenuList(paramMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (total > 0)
            {
                dataList = menuModelMapper.getMenuList(paramMap);
            }

            resultMap.put("total", total);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取菜单列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMenuList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(AddMenuMainVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        row;
            int        count;
            MenuModel  menuOld    = null;
            if (StringUtils.isNotEmpty(vo.getId()))
            {
                menuOld = menuModelMapper.selectByPrimaryKey(vo.getId());
                if (menuOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
                }
            }
            if (vo.getLevel() == null)
            {
                vo.setLevel(1);
            }
//            //只有商城自营店才可以添加菜单
//            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
//            if (!user.getMchId().equals(storeMchId)) {
//                logger.debug("当前商城自营店{},只有自营店账号才可以添加菜单", storeMchId);
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
//            }

            MenuModel menuSave = new MenuModel();
            //一级菜单需要logo 用于展示菜单效果
            if (vo.getLevel() == 1)
            {
                if (StringUtils.isEmpty(vo.getDefaultLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZ, "请选择logo");
                }
                if (StringUtils.isEmpty(vo.getCheckedLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZXZ, "请选择选中logo");
                }
                menuSave.setLogo(vo.getDefaultLogo());
                menuSave.setChecked_logo(vo.getCheckedLogo());
            }
            if (StringUtils.isEmpty(vo.getMenuName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDMCBNWK, "菜单名称不能为空");
            }
            menuSave.setPath(vo.getPath());
            menuSave.setUrl(vo.getMenuUrl());
            menuSave.setName(vo.getMenuName());
            menuSave.setSid(vo.getFatherMenuId());
            String fatherMenuId = "0";
            if (!fatherMenuId.equals(vo.getFatherMenuId()))
            {
                //获取上级菜单信息
                MenuModel menuFather = menuModelMapper.selectByPrimaryKey(vo.getFatherMenuId());
                if (menuFather == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCDBCZ, "上级菜单不存在");
                }
                vo.setLevel(menuFather.getLevel() + 1);
            }

            menuSave.setLevel(vo.getLevel());
            menuSave.setText(vo.getText());
            menuSave.setIs_display(vo.getIsDisplay());

            //校验菜单名称
            MenuModel menuCount = new MenuModel();
            menuCount.setType(GloabConst.MenuType.MENU_MCH_ID);
            menuCount.setMain_id(0 + "");
            menuCount.setLevel(vo.getLevel());
            menuCount.setSid(vo.getFatherMenuId());
            menuCount.setName(vo.getMenuName());
            if (menuOld == null || !vo.getMenuName().equals(menuOld.getName()))
            {
                //校验菜单名称
                count = menuModelMapper.selectCount(menuCount);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDYCZ, "菜单已存在");
                }
            }
            if (menuOld == null)
            {
                //获取最新序号
                int maxSort = menuModelMapper.maxSort(GloabConst.MenuType.MENU_MCH_ID, vo.getLevel(), vo.getFatherMenuId());
                menuSave.setSort(maxSort);
                menuSave.setId(BuilderIDTool.getSnowflakeId() + "");
                menuSave.setType(GloabConst.MenuType.MENU_MCH_ID);
                menuSave.setMain_id(0 + "");
                menuSave.setAdd_date(new Date());
                row = menuModelMapper.insertSelective(menuSave);
                //增加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "新增pc店铺菜单" + menuSave.getId() + "的信息", AdminRecordModel.Type.ADD, AdminRecordModel.Source.PC_PLATFORM);
            }
            else
            {
                //菜单上级不能是自己
                if (StringUtils.isNotEmpty(vo.getFatherMenuId()))
                {
                    if (vo.getFatherMenuId().equals(menuOld.getId()))
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDSJBNSDQCD, "菜单上级不能是当前菜单");
                    }
                    //如果换了上级则重新排序 获取最新序号
                    int maxSort = menuModelMapper.maxSort(GloabConst.MenuType.MENU_MCH_ID, vo.getLevel(), vo.getFatherMenuId());
                    menuSave.setSort(maxSort);
                }
                menuSave.setId(menuOld.getId());
                menuSave.setUpdate_date(new Date());
                row = menuModelMapper.updateByPrimaryKeySelective(menuSave);
                //增加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "修改pc店铺菜单" + menuSave.getId() + "的信息", AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM);
            }
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
            logger.error("添加菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addMenu");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMenu(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            int        row        = 0;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            MenuModel menuOld = menuModelMapper.selectByPrimaryKey(id);
            if (menuOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
//            //只有商城自营店才可以添加菜单
//            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
//            if (!user.getMchId().equals(storeMchId)) {
//                logger.debug("当前商城自营店{},只有自营店账号才可以增/删菜单,普通账号只删除自己的对应权限", storeMchId);
//                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
//            }
            //删除菜单
            row = menuModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //删除菜单对应权限
            AuthorityMappingModel authorityMappingDel = new AuthorityMappingModel();
            authorityMappingDel.setMenu_id(menuOld.getId());
            row = authorityMappingModelMapper.deleteByPrimaryKey(id);
            //增加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "删除" + menuOld.getName() + "菜单", AdminRecordModel.Type.DEL, AdminRecordModel.Source.PC_PLATFORM);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delMenu");
        }
    }

    @Override
    public Map<String, Object> getUserAuthorityTree(MainVo vo, String sid, String roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String              zero       = "0";
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap   = new HashMap<>(16);
            paramMap.put("type", DictionaryConst.MenuType.MCH);
            if (sid == null)
            {
                sid = zero;
            }
            paramMap.put("sid", sid);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());

            //是否是店铺主人
            boolean isMain = false;
            //获取当前用户权限id集
            List<String> roleIdList = new ArrayList<>();
            if (StringUtils.isEmpty(roleId) || zero.equals(roleId))
            {
//                //获取当前用户权限id 如果是店铺主人则获取全部菜单
//                int count = mchModelMapper.countUserIsByMch(vo.getStoreId(), user.getUser_id(), user.getMchId());
//                if (count == 0) {
//                    //获取当前权限
//                    String userRoleId = userAuthorityModelMapper.getRoleIdByUserId(user.getUser_id());
//                    roleIdList = menuModelMapper.getRoleMenuIdList(userRoleId, DictionaryConst.MenuType.MCH);
//                    if (roleIdList == null || roleIdList.size() == 0) {
//                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
//                    }
//                } else {
                isMain = true;
//                }
            }
            else
            {
                //获取指定角色拥有的权限
                roleIdList = menuModelMapper.getRoleMenuIdList(vo.getStoreId(), roleId, DictionaryConst.MenuType.MCH);
            }

            List<Map<String, Object>> menuTopList = menuModelMapper.getMenuList(paramMap);
            for (Map<String, Object> map : menuTopList)
            {
                String  id      = MapUtils.getString(map, "id");
                boolean checked = roleIdList.contains(id);
                if (isMain)
                {
                    //店铺主人拥有所有权限
                    checked = true;
                }
                map.put("checked", checked);
                List<Map<String, Object>> childrenList = new ArrayList<>();
                if (StringUtils.isNotEmpty(roleId))
                {
                    childrenList = this.getRoleChildrenAll(id, roleIdList);
                }
                map.put("children", childrenList);
            }

            resultMap.put("list", menuTopList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取用户所有的菜单权限 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserAuthorityTree");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> roleList(MainVo vo, String roleId, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap   = new HashMap<>(16);
            paramMap.put("id", roleId);
            paramMap.put("main_id", mchId);
            paramMap.put("type", UserRoleModel.SHOP_ID);
            paramMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());

            int                       total = userRoleModelMapper.countDynamic(paramMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = userRoleModelMapper.selectDynamic(paramMap);
                //查看单个
                if (StringUtils.isNotEmpty(roleId))
                {
                    for (Map<String, Object> map : list)
                    {
                        String currentRoleId = MapUtils.getString(map, "id");
                        //获取当前权限
                        List<Map<String, Object>> roleMenuList = authorityMappingModelMapper.getRoleMenu(1, currentRoleId);
                        resultMap.put("currentRoleMenuList", roleMenuList);
                    }
                }
            }

            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("角色列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "roleList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(AddRoleMainVo vo) throws LaiKeAPIException
    {
        try
        {
            int           row;
            AdminModel    adminModel  = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            UserRoleModel userRoleOld = null;
            if (vo.getId() != null)
            {
                userRoleOld = userRoleModelMapper.selectByPrimaryKey(vo.getId());
                if (userRoleOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSBCZ, "角色不存在");
                }
            }
            if (Objects.isNull(vo.getMchId()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "请选择需要添加角色的店铺");
            }
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMCBNWK, "角色名称不能为空");
            }
            if (StringUtils.isEmpty(vo.getDescribe()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMSBNWK, "角色描述不能为空");
            }
            if (StringUtils.isEmpty(vo.getRoleIdTree()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZQX, "请选择权限");
            }

            if (userRoleOld == null || !userRoleOld.getName().equals(vo.getName()))
            {
                UserRoleModel userRoleCount = new UserRoleModel();
                userRoleCount.setName(vo.getName());
                userRoleCount.setType(UserRoleModel.SHOP_ID);
                userRoleCount.setMain_id(vo.getMchId());
                int count = userRoleModelMapper.selectCount(userRoleCount);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSYCZ, "角色已存在");
                }
            }
            UserRoleModel userRoleSave = new UserRoleModel();
            userRoleSave.setName(vo.getName());
            userRoleSave.setText(vo.getDescribe());
            if (userRoleOld == null)
            {
                userRoleSave.setType(UserRoleModel.SHOP_ID);
                userRoleSave.setId(BuilderIDTool.getSnowflakeId() + "");
                userRoleSave.setMain_id(vo.getMchId());
                userRoleSave.setAdd_date(new Date());
                row = userRoleModelMapper.insertSelective(userRoleSave);
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "增加店铺" + vo.getMchId() + "为" + userRoleSave.getName() + "的角色,ID为" + userRoleSave.getId(), AdminRecordModel.Type.ADD, AdminRecordModel.Source.PC_PLATFORM);
            }
            else
            {
                userRoleSave.setId(userRoleOld.getId());
                userRoleSave.setUpdate_date(new Date());
                row = userRoleModelMapper.updateByPrimaryKeySelective(userRoleSave);
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "修改店铺" + vo.getMchId() + "为" + userRoleSave.getName() + "的角色,ID为" + userRoleSave.getId(), AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_PLATFORM);
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }

            //删除之前权限
            AuthorityMappingModel authorityMappingDel = new AuthorityMappingModel();
            authorityMappingDel.setRole_id(userRoleSave.getId());
            authorityMappingModelMapper.delete(authorityMappingDel);
            //添加角色权限
            List<AuthorityMappingModel> authorityMappingModelList = new ArrayList<>();
            String[]                    menuIdList                = vo.getRoleIdTree().split(SplitUtils.DH);
            for (String menuId : menuIdList)
            {
                AuthorityMappingModel authorityMappingModel = new AuthorityMappingModel();
                authorityMappingModel.setId(BuilderIDTool.getSnowflakeId() + "");
                authorityMappingModel.setRole_id(userRoleSave.getId());
                authorityMappingModel.setMenu_id(menuId);
                authorityMappingModel.setAdd_date(new Date());
                authorityMappingModelList.add(authorityMappingModel);
            }
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("list", authorityMappingModelList);
            authorityMappingModelMapper.saveList(parmaMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addRole");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRole(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //删除之前权限
            AuthorityMappingModel authorityMappingDel = new AuthorityMappingModel();
            authorityMappingDel.setRole_id(id);
            authorityMappingModelMapper.delete(authorityMappingDel);
            //解除所有绑定了该角色的用户
            UserAuthorityModel userAuthorityModel = new UserAuthorityModel();
            userAuthorityModel.setRole_id(id);
            userAuthorityModel.setType(DictionaryConst.AuthorityType.USERID);
            List<UserAuthorityModel> userAuthorityModels = userAuthorityModelMapper.select(userAuthorityModel);
            if (userAuthorityModels != null && userAuthorityModels.size() > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXJCBDGXZJXSCCZ, "请先解除绑定关系,再进行删除操作!");
            }
            //删除角色
            UserRoleModel userRoleModel = new UserRoleModel();
            userRoleModel.setType(DictionaryConst.MenuType.MCH);
            userRoleModel.setId(id);
            userRoleModel = userRoleModelMapper.selectOne(userRoleModel);
            int row = userRoleModelMapper.delete(userRoleModel);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "删除" + userRoleModel.getName() + "的店铺角色,ID为" + userRoleModel.getId(), AdminRecordModel.Type.DEL, AdminRecordModel.Source.PC_PLATFORM);
            if (userAuthorityModels != null)
            {
                userAuthorityModels.forEach(userAuthority ->
                {
                    userAuthorityModelMapper.deleteByPrimaryKey(userAuthority.getId());
                    //管理员踢下线
                    Object token = redisUtil.get(GloabConst.RedisHeaderKey.LOGIN_ACCESS_FLAG_MCH + userAuthority.getMain_id());
                    if (token != null)
                    {
                        logger.debug("{}用户被踢下线", userAuthority.getMain_id());
                        redisUtil.del(token.toString());
                    }
                });
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delRole");
        }
    }

    @Override
    public Map<String, Object> insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //重新加一个参数来判断是更新还是新增
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //操作人用户id
            MchModel mchModel = mchModelMapper.selectByPrimaryKey(adminModel.getShop_id());
            boolean  isexist;
            if (StringUtils.isEmpty(vo.getMima()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MMBNWK, "密码不能为空");
            }
            if (StringUtils.isEmpty(vo.getZhanghao()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZHBNWK, "账号不能为空");
            }
            User user = new User();
            user.setMima(CryptoUtil.strEncode(vo.getMima()));
            user.setZhanghao(vo.getZhanghao());
            //校验数据格式
//            user = DataCheckTool.checkUserDataFormate(user);

            //检查账户/手机号是否已存在
            User userTemp = new User();
            userTemp.setZhanghao(vo.getZhanghao());
            //修改时显示账号已注册
            isexist = this.isRegister(vo.getStoreId(), vo.getZhanghao());
            int row;
            //添加
            if (isUpdate == 1)
            {
                //判断是否已经注册
                if (!isexist)
                {
                    vo.setAccessId("");
                    //注册
                    publicUserService.register(vo, vo.getPid(), user);
                    //查询权限表
                    UserAuthorityModel userAuthorityOld = new UserAuthorityModel();
                    userAuthorityOld.setType(UserAuthorityModel.USER_ID);
                    userAuthorityOld.setMain_id(user.getUser_id());
                    userAuthorityOld = userAuthorityModelMapper.selectOne(userAuthorityOld);

                    //修改/添加角色
                    UserAuthorityModel userAuthoritySave = new UserAuthorityModel();
                    userAuthoritySave.setRole_id(roleId);
                    userAuthoritySave.setCreate_id(mchModel.getUser_id());
                    userAuthoritySave.setAdd_date(new Date());
                    if (userAuthorityOld == null)
                    {
                        userAuthoritySave.setId(BuilderIDTool.getSnowflakeId() + "");
                        userAuthoritySave.setType(UserAuthorityModel.USER_ID);
                        userAuthoritySave.setMain_id(user.getUser_id());
                        userAuthoritySave.setCreate_id(mchModel.getUser_id());
                        row = userAuthorityModelMapper.insertSelective(userAuthoritySave);
                        if (row < 1)
                        {
                            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                        }
                    }
                }
                else
                {
                    logger.debug("{}或者{} 该账号已被注册!", user.getZhanghao(), user.getMobile());
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GZHYBZC, "该账号已被注册", "insertUser");
                }
                resultMap.put("access_id", user.getAccess_id());
                resultMap.put("user_name", user.getMobile());
                resultMap.put("headimgurl", user.getHeadimgurl());
                resultMap.put("y_password", 1);
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "添加用户【" + user.getUser_id() + "】为店铺管理员", AdminRecordModel.Type.ADD, AdminRecordModel.Source.PC_PLATFORM);
            }
            else
            {
                //查询权限表
                UserAuthorityModel userAuthorityOld = new UserAuthorityModel();
                userAuthorityOld = userAuthorityModelMapper.selectByPrimaryKey(id);

                //修改/添加角色
                UserAuthorityModel userAuthoritySave = new UserAuthorityModel();
                userAuthoritySave.setRole_id(roleId);
                userAuthoritySave.setCreate_id(user.getUser_id());
                userAuthoritySave.setUpdate_date(new Date());
                userAuthoritySave.setId(userAuthorityOld.getId());
                row = userAuthorityModelMapper.updateByPrimaryKeySelective(userAuthoritySave);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
                User afterUser = new User();
                afterUser.setZhanghao(vo.getZhanghao());
                afterUser.setUser_id(userAuthorityOld.getMain_id());
                afterUser = userBaseMapper.selectOne(afterUser);
                User user1 = new User();
                //修改用户密码
                user1.setMima(CryptoUtil.strEncode(vo.getMima()));
                user1.setId(afterUser.getId());
                int row2 = userBaseMapper.updateByPrimaryKeySelective(user1);
                if (row2 < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "修改失败");
                }
                publiceService.addAdminRecord(vo.getStoreId(), adminModel.getName(), "修改了用户【" + afterUser.getUser_id() + "】密码", AdminRecordModel.Type.ADD, AdminRecordModel.Source.PC_PLATFORM);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("用户注册 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "insertUser");
        }
        return resultMap;
    }

    @Override
    public boolean isRegister(int stroeId, String zhanghao) throws LaiKeAPIException
    {
        try
        {
            int flag = userBaseMapper.validataUserPhoneOrNoIsRegister(stroeId, zhanghao);
            return flag > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("验证账号是否注册 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isRegister");
        }
    }

    @Override
    public Map<String, Object> getAdminList(MainVo vo, String roleId, String id, Integer mchId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> paramMap   = new HashMap<>(16);
            if (StringUtils.isNotEmpty(id))
            {
                paramMap.put("uid", id);
                List<Map<String, Object>> list = userAuthorityModelMapper.getAdminList(paramMap);
                resultMap.put("list", list);
            }
            else
            {
                paramMap.put("type_authority", DictionaryConst.AuthorityType.USERID);
                paramMap.put("type_role", DictionaryConst.MenuType.MCH);
                if (StringUtils.isNotEmpty(roleId))
                {
                    paramMap.put("not_roleId", roleId);
                }
                paramMap.put("mchId", mchId);
                paramMap.put("pageStart", vo.getPageNo());
                paramMap.put("pageEnd", vo.getPageSize());
                int                       total = userAuthorityModelMapper.countAdminList(paramMap);
                List<Map<String, Object>> list  = new ArrayList<>();
                if (total > 0)
                {
                    list = userAuthorityModelMapper.getAdminList(paramMap);
                }

                resultMap.put("total", total);
                resultMap.put("list", list);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取管理员列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAdminList");
        }
        return resultMap;
    }

    @Override
    public void delBindUserAuthorityTree(MainVo vo, String id) throws LaiKeAPIException
    {

    }

    /**
     * 获取所有子权限菜单
     *
     * @param fatherId   - 上级id
     * @param roleMenuId - 当前角色拥有的菜单,用于选中 为 null 则表示所有权限都有
     * @return List -
     * @throws LaiKeAPIException -
     */
    private List<Map<String, Object>> getRoleChildrenAll(String fatherId, List<String> roleMenuId) throws LaiKeAPIException
    {
        try
        {
            //是否选中上级标识
            boolean             isShow   = false;
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("sid", fatherId);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> dataList = menuModelMapper.selectDynamic(paramMap);
            for (Map<String, Object> map : dataList)
            {
                String id  = MapUtils.getString(map, "id");
                String sid = MapUtils.getString(map, "id");
                if (roleMenuId == null || roleMenuId.contains(id))
                {
                    isShow = true;
                }
                List<Map<String, Object>> temp = null;
                if (StringUtils.isNotEmpty(sid) && !"0".equals(sid))
                {
                    temp = getRoleChildrenAll(id, roleMenuId);
                }
                map.put("checked", isShow);
                map.put("children", temp);
            }
            return dataList;
        }
        catch (Exception e)
        {
            logger.error("获取所有子权限菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleChildrenAll");
        }
    }
}
