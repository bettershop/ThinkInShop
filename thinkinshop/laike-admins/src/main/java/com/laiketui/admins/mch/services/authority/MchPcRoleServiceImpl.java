package com.laiketui.admins.mch.services.authority;

import com.laiketui.admins.api.mch.authority.MchPcRoleService;
import com.laiketui.common.api.PublicUserService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.authority.AuthorityMappingModel;
import com.laiketui.domain.authority.UserAuthorityModel;
import com.laiketui.domain.authority.UserRoleModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
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

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * pc店铺权限
 *
 * @author Trick
 * @date 2021/12/12 11:57
 */
@Service
public class MchPcRoleServiceImpl implements MchPcRoleService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserRoleModelMapper userRoleModelMapper;

    @Autowired
    private MenuModelMapper menuModelMapper;

    @Autowired
    private MchModelMapper mchModelMapper;

    @Autowired
    private AuthorityMappingModelMapper authorityMappingModelMapper;

    @Autowired
    private UserBaseMapper userBaseMapper;

    @Autowired
    private UserAuthorityModelMapper userAuthorityModelMapper;

    @Autowired
    private RecordModelMapper recordModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private AdminRecordModelMapper adminRecordModelMapper;

    @Override
    public Map<String, Object> roleList(MainVo vo, String roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("id", roleId);
            paramMap.put("main_id", user.getMchId());
            paramMap.put("type", DictionaryConst.AuthorityType.ADMIN_ID);
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
            User          user        = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            UserRoleModel userRoleOld = null;
            if (vo.getId() != null)
            {
                userRoleOld = userRoleModelMapper.selectByPrimaryKey(vo.getId());
                if (userRoleOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSBCZ, "角色不存在");
                }
            }
            if (StringUtils.isEmpty(vo.getName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMCBNWK, "角色名称不能为空");
            }
            /*if (StringUtils.isEmpty(vo.getDescribe())) {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMSBNWK, "角色描述不能为空");
            }*/
            if (StringUtils.isEmpty(vo.getRoleIdTree()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZQX, "请选择权限");
            }

            if (userRoleOld == null || !userRoleOld.getName().equals(vo.getName()))
            {
                UserRoleModel userRoleCount = new UserRoleModel();
                userRoleCount.setName(vo.getName());
                userRoleCount.setType(UserRoleModel.SHOP_ID);
                userRoleCount.setMain_id(user.getMchId().toString());
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
                userRoleSave.setMain_id(user.getMchId() + "");
                userRoleSave.setAdd_date(new Date());
                row = userRoleModelMapper.insertSelective(userRoleSave);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了角色名称：" + userRoleSave.getName(), AdminRecordModel.Type.ADD, vo.getAccessId());
            }
            else
            {
                userRoleSave.setId(userRoleOld.getId());
                userRoleSave.setUpdate_date(new Date());
                row = userRoleModelMapper.updateByPrimaryKeySelective(userRoleSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了角色名称：" + userRoleSave.getName(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
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
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
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
            publiceService.addAdminRecord(vo.getStoreId(), "删除了角色名称：" + userRoleModel.getName(), AdminRecordModel.Type.DEL, vo.getAccessId());
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

    /**
     * 获取所有子权限菜单
     *
     * @param fatherId   - 上级id
     * @param roleMenuId - 当前角色拥有的菜单,用于选中 为 null 则表示所有权限都有
     * @param isMain     - 是否是店主 店主有所有权限
     * @return List -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/14 12:00
     */
    private List<Map<String, Object>> getRoleChildrenAll(Integer storeId, String fatherId, List<String> roleMenuId, Boolean isMain) throws LaiKeAPIException
    {
        try
        {
            //是否选中上级标识
            boolean             isShow   = false;
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("sid", fatherId);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("storeId", storeId);
            List<Map<String, Object>> dataList = menuModelMapper.selectDynamic(paramMap);
            for (Map<String, Object> map : dataList)
            {
                isShow = false;
                String id  = MapUtils.getString(map, "id");
                String sid = MapUtils.getString(map, "id");
                if (roleMenuId == null || roleMenuId.contains(id) || isMain)
                {
                    isShow = true;
                }
                List<Map<String, Object>> temp = null;
                if (StringUtils.isNotEmpty(sid) && !"0".equals(sid))
                {
                    temp = getRoleChildrenAll(storeId, id, roleMenuId, isMain);
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

    @Override
    public Map<String, Object> getUserAuthorityTree(MainVo vo, String sid, String roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            String              zero     = "0";
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("type", DictionaryConst.MenuType.MCH);
            if (sid == null)
            {
                sid = zero;
            }
            paramMap.put("sid", sid);
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("storeId", vo.getStoreId());
            //是否是店铺主人
            boolean isMain = false;
            //获取当前用户权限id集
            List<String> roleIdList = new ArrayList<>();
            if (StringUtils.isEmpty(roleId) || zero.equals(roleId))
            {
                //获取当前用户权限id 如果是店铺主人则获取全部菜单
                int count = mchModelMapper.countUserIsByMch(vo.getStoreId(), user.getUser_id(), user.getMchId());
                if (count == 0)
                {
                    //获取当前权限
                    String userRoleId = userAuthorityModelMapper.getRoleIdByUserId(user.getUser_id());
                    roleIdList = menuModelMapper.getRoleMenuIdList(vo.getStoreId(), userRoleId, DictionaryConst.MenuType.MCH);
                    if (roleIdList == null || roleIdList.size() == 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
                    }
                }
                else
                {
                    isMain = true;
                }
            }
            else
            {
                //获取指定角色拥有的权限
                roleIdList = menuModelMapper.getRoleMenuIdList(vo.getStoreId(), roleId, DictionaryConst.MenuType.MCH);
            }
            paramMap.put("lang_code", vo.getLang_code());
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
                    childrenList = this.getRoleChildrenAll(vo.getStoreId(), id, roleIdList, isMain);
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
    public Map<String, Object> getUserList(MainVo vo, String key, String roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("user_name1", key);
            parmaMap.put("not_mch", "not_mch");
            parmaMap.put("user_roleId", roleId);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            int                       total = userBaseMapper.countUserList(parmaMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = userBaseMapper.getUserList(parmaMap);
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
            logger.error("为用户绑定角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserList");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserAuthorityTree(MainVo vo, String userId, String roleId) throws LaiKeAPIException
    {
        try
        {
            User userCache = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int  row;
            if (StringUtils.isEmpty(userId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZYH, "请选择用户");
            }
            if (StringUtils.isEmpty(roleId))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZJS, "请选择角色");
            }
            String[] userIdList = userId.split(SplitUtils.DH);
            for (String uid : userIdList)
            {
                User user = new User();
                user.setStore_id(vo.getStoreId());
                user.setUser_id(uid);
                int count = userBaseMapper.selectCount(user);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YHBCZ, "用户不存在");
                }

                UserAuthorityModel userAuthorityOld = new UserAuthorityModel();
                userAuthorityOld.setType(UserAuthorityModel.USER_ID);
                userAuthorityOld.setMain_id(uid);
                userAuthorityOld = userAuthorityModelMapper.selectOne(userAuthorityOld);
                //修改/添加角色
                UserAuthorityModel userAuthoritySave = new UserAuthorityModel();
                userAuthoritySave.setRole_id(roleId);
                userAuthoritySave.setCreate_id(userCache.getUser_id());
                userAuthoritySave.setAdd_date(new Date());
                if (userAuthorityOld == null)
                {
                    userAuthoritySave.setId(BuilderIDTool.getSnowflakeId() + "");
                    userAuthoritySave.setType(UserAuthorityModel.USER_ID);
                    userAuthoritySave.setMain_id(uid);
                    row = userAuthorityModelMapper.insertSelective(userAuthoritySave);
                }
                else
                {
                    userAuthoritySave.setId(userAuthorityOld.getId());
                    row = userAuthorityModelMapper.updateByPrimaryKeySelective(userAuthoritySave);
                }

                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("为用户绑定角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bindUserAuthorityTree");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBindUserAuthorityTree(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            UserAuthorityModel userAuthorityOld = userAuthorityModelMapper.selectByPrimaryKey(id);

            if (userAuthorityOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GYHWBDGJS, "该用户未绑定该角色");
            }
            //解除绑定
            int row = userAuthorityModelMapper.deleteByPrimaryKey(id);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //删除用户表
            User user = new User();
            user.setUser_id(userAuthorityOld.getMain_id());

            publiceService.addAdminRecord(vo.getStoreId(), "删除了管理员名称：" + userAuthorityOld.getMain_id(), AdminRecordModel.Type.DEL, vo.getAccessId());

            int row1 = userBaseMapper.deleteByPrimaryKey(userBaseMapper.selectOne(user).getId());
            if (row1 < 1)
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
            logger.error("解除绑定 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBindUserAuthorityTree");
        }
    }

    @Override
    public Map<String, Object> getAdminList(MainVo vo, String roleId, String id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> paramMap = new HashMap<>(16);
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
                paramMap.put("mchId", user.getMchId());
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
    public Map<String, Object> getRecord(MainVo vo, String zhangHao, String startDate, String endDate, HttpServletResponse response, String logOperationType, String logAccountType) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("source", AdminRecordModel.Source.PC_SHOP);
            parmaMap.put("mch_id", user.getMchId());
            parmaMap.put("logAccountType", logAccountType);//日志账号类型
            parmaMap.put("logOperationType", logOperationType);//日志操作类型
            //店铺管理员可以查看所有的日志，普通管理员只能查看自己的
            if (mchModelMapper.countMchIsByUser(vo.getStoreId(), user.getUser_id()) < 1)
            {
                parmaMap.put("user_id", user.getUser_id());
            }
            if (!StringUtils.isEmpty(startDate))
            {
                parmaMap.put("startDate", startDate);
                if (!StringUtils.isEmpty(startDate))
                {
                    parmaMap.put("endDate", endDate);
                }
            }
            if (!StringUtils.isEmpty(zhangHao))
            {
                parmaMap.put("zhanghao_like", zhangHao);
            }

            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());

            int total = adminRecordModelMapper.countDynamic(parmaMap);

            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = new ArrayList<>();
            if (total > 0)
            {
                dataList = adminRecordModelMapper.selectDynamic(parmaMap);
                dataList.forEach(data -> data.put("add_date", DateUtil.dateFormate(MapUtils.getString(data, "add_date"), GloabConst.TimePattern.YMDHMS)));
            }

            for (Map<String, Object> map : dataList)
            {
                Integer operationType  = MapUtils.getIntValue(map, "type");
                String  operation_type = "";
                if (operationType == AdminRecordModel.Type.LOGIN_OR_OUT)
                {
                    operation_type = "登陆/退出";
                }
                else if (operationType == AdminRecordModel.Type.ADD)
                {
                    operation_type = "添加";
                }
                else if (operationType == AdminRecordModel.Type.UPDATE)
                {
                    operation_type = "修改";
                }
                else if (operationType == AdminRecordModel.Type.DEL)
                {
                    operation_type = "删除";
                }
                else if (operationType == AdminRecordModel.Type.EXCEL_OUT)
                {
                    operation_type = "导出";
                }
                else if (operationType == AdminRecordModel.Type.OPEN_OR_CLOSE)
                {
                    operation_type = "启用/禁用";
                }
                else if (operationType == AdminRecordModel.Type.PASS_OR_REFUSE)
                {
                    operation_type = "通过/拒绝";
                }
                else if (operationType == AdminRecordModel.Type.DEL_ORDER)
                {
                    operation_type = "删除订单";
                }
                map.put("operation_type", operation_type);
            }
            if (vo.getExportType() == 1)
            {
                exportData(dataList, response);
                return null;
            }

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("管理员日志 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRecord");
        }
        return resultMap;
    }

    /**
     * 导出管理员日志信息
     *
     * @param list     -
     * @param response -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/29 10:22
     */
    private void exportData(List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            //表头
            String[] headerList = new String[]{"账号ID", "账号名称", "账号类型", "操作类型", "操作说明", "操作时间"};
            //对应字段
            String[]     kayList = new String[]{"admin_id", "admin_name", "account_type", "operation_type", "event", "add_date"};
            ExcelParamVo vo      = new ExcelParamVo();
            vo.setTitle("管理员日志");
            vo.setHeaderList(headerList);
            vo.setValueList(kayList);
            vo.setList(list);
            vo.setResponse(response);
            vo.setNeedNo(true);
            EasyPoiExcelUtil.excelExport(vo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("导出管理员日志信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportData");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRecord(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int  row;
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            String[] idList = ids.split(SplitUtils.DH);
            for (String id : idList)
            {
                row = adminRecordModelMapper.deleteByPrimaryKey(id);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败", "delRecord");
                }

                publiceService.addAdminRecord(vo.getStoreId(), user.getUser_id(), "删除ID为" + id + "操作日志", AdminRecordModel.Type.DEL, AdminRecordModel.Source.PC_SHOP, user.getMchId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除管理员日志 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delRecord");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> insertUser(UserRegisterVo vo, String roleId, Integer isUpdate, String id, String token) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {//重新加一个参数来判断是更新还是新增
            User    currentUser = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            boolean isexist;
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
                    userAuthoritySave.setCreate_id(currentUser.getUser_id());
                    userAuthoritySave.setAdd_date(new Date());
                    if (userAuthorityOld == null)
                    {
                        userAuthoritySave.setId(BuilderIDTool.getSnowflakeId() + "");
                        userAuthoritySave.setType(UserAuthorityModel.USER_ID);
                        userAuthoritySave.setMain_id(user.getUser_id());
                        userAuthoritySave.setCreate_id(currentUser.getUser_id());
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
                publiceService.addAdminRecord(vo.getStoreId(), "添加了管理员名称：" + vo.getZhanghao(), AdminRecordModel.Type.ADD, token);
            }
            else
            {
                //查询权限表
                UserAuthorityModel userAuthorityOld = new UserAuthorityModel();
                userAuthorityOld = userAuthorityModelMapper.selectByPrimaryKey(id);

                //修改/添加角色
                UserAuthorityModel userAuthoritySave = new UserAuthorityModel();
                userAuthoritySave.setRole_id(roleId);
                userAuthoritySave.setCreate_id(currentUser.getUser_id());
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
//                publiceService.addAdminRecord(vo.getStoreId(), currentUser.getUser_id(), "修改了用户【" + afterUser.getUser_id() + "】密码", AdminRecordModel.Type.UPDATE, AdminRecordModel.Source.PC_SHOP, currentUser.getMchId());
                publiceService.addAdminRecord(vo.getStoreId(), "修改了管理员名称：" + vo.getZhanghao() + " 的信息", AdminRecordModel.Type.UPDATE, token);
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
    public boolean isRegister(int storeId, String zhanghao) throws LaiKeAPIException
    {
        try
        {
            int flag = userBaseMapper.validataUserPhoneOrNoIsRegister(storeId, zhanghao);
            return flag > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("验证账号是否注册 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isRegister");
        }
    }

    @Autowired
    private PublicUserService publicUserService;
}

