package com.laiketui.admins.admin.services.role;

import com.laiketui.admins.api.admin.role.AdminRoleService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicRoleService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.EasyPoiExcelUtil;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.mch.RoleModel;
import com.laiketui.domain.role.CoreMenuModel;
import com.laiketui.domain.role.GuideMenuModel;
import com.laiketui.domain.role.RoleMenuModel;
import com.laiketui.domain.systems.SystemConfigurationModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import com.laiketui.domain.vo.admin.role.AddAdminVo;
import com.laiketui.domain.vo.admin.role.LoggerAdminVo;
import com.laiketui.domain.vo.role.AddRoleVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 权限管理
 *
 * @author Trick
 * @date 2021/1/13 12:15
 */
@Service
public class AdminRoleServiceImpl implements AdminRoleService
{
    private final Logger logger = LoggerFactory.getLogger(AdminRoleServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SystemConfigurationModelMapper systemConfigurationModelMapper;

    @Autowired
    private ConfigModelMapper configModelMapper;
    @Autowired
    private PublicRoleService publicRoleService;
    @Autowired
    private CustomerModelMapper customerModelMapper;
    @Autowired
    private AdminModelMapper adminModelMapper;
    @Autowired
    private RoleModelMapper roleModelMapper;
    @Autowired
    private AdminRecordModelMapper adminRecordModelMapper;
    @Autowired
    private RoleMenuModelMapper roleMenuModelMapper;
    @Autowired
    private GuideMenuModelMapper guideMenuModelMapper;
    @Autowired
    private CoreMenuModelMapper coreMenuModelMapper;
    @Autowired
    private PubliceService publiceService;
    @Autowired
    private PluginsModelMapper pluginsModelMapper;

    @Override
    public Map<String, Object> getAdminInfo(MainVo vo, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (admin != null)
            {
                //判断是否是超级管理员(查看该商城所有管理员)
                boolean isSuperAdmin = admin.getType() == AdminModel.TYPE_SYSTEM_ADMIN || admin.getType() == AdminModel.TYPE_CLIENT;

                CustomerModel customerModel = customerModelMapper.selectByPrimaryKey(vo.getStoreId());
                if (customerModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
                }

                Map<String, Object> parmaMap = new HashMap<>(16);
                if (id != null && id > 0)
                {
                    parmaMap.put("id", id);
                }
                parmaMap.put("type", AdminModel.TYPE_STORE_ADMIN);
                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                parmaMap.put("add_date_sort", "desc");
                parmaMap.put("pageStart", vo.getPageNo());
                parmaMap.put("pageEnd", vo.getPageSize());
                if (isSuperAdmin)
                {
                    parmaMap.put("store_id", vo.getStoreId());
                }
                else
                {
                    //不是超级管理员则只能查看自己的
                    parmaMap.put("id", admin.getId());
                }
                int                       total         = adminModelMapper.countAdminListInfo(parmaMap);
                List<Map<String, Object>> adminListInfo = adminModelMapper.selectAdminListInfo(parmaMap);

                resultMap.put("total", total);
                resultMap.put("list", adminListInfo);
                resultMap.put("customer_number", customerModel.getCustomer_number());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取管理员列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAdminInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getRoleListInfo(MainVo vo, Integer status, Integer id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            //禅道 56643
            parmaMap.put("store_id_pt", vo.getStoreId());
            parmaMap.put("id", id);
            //过滤没有菜单的角色
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            int                       total         = roleModelMapper.countRoleInfo(parmaMap);
            List<Map<String, Object>> roleModelList = roleModelMapper.selectRoleInfo(parmaMap);

            for (Map<String, Object> map : roleModelList)
            {
                int                 roleId    = MapUtils.getIntValue(map, "id");
                Map<String, Object> parmaMap1 = new HashMap<>(16);
                parmaMap1.put("role", roleId);
                parmaMap1.put("type", AdminModel.TYPE_CLIENT);
                //获取绑定的商户
                List<Map<String, Object>> bindAdminList = adminModelMapper.getBindListInfo(parmaMap1);
                map.put("bindAdminList", bindAdminList);
            }
            resultMap.put("total", total);
            resultMap.put("list", roleModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取角色列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleListInfo");
        }
        return resultMap;
    }

    @Override
    public void addAdminInfo(AddAdminVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel  admin       = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            ConfigModel configModel = new ConfigModel();
            configModel.setStore_id(vo.getStoreId());
            configModel = configModelMapper.selectOne(configModel);

            if (configModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCWPZ, "商城未配置", "goodsShare");
            }

            String defaultLangCode = configModel.getDefault_lang_code();

            int count;
            //是否为修改
            boolean    isUpdate      = false;
            AdminModel adminModelOld = null;
            if (vo.getId() != null && vo.getId() > 0)
            {
                isUpdate = true;
                adminModelOld = new AdminModel();
                adminModelOld.setId(vo.getId());
                adminModelOld.setStore_id(vo.getStoreId());
                adminModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                adminModelOld = adminModelMapper.selectOne(adminModelOld);
                if (adminModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYBCZ, "管理员不存在", "addAdminInfo");
                }
            }

            AdminModel adminModelSave = new AdminModel();
            adminModelSave.setPassword(vo.getAdminPWD());
            adminModelSave.setRole(vo.getRoleId() + "");
            adminModelSave.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);

            if (!StringUtils.isNotEmpty(defaultLangCode))
            {
                defaultLangCode = GloabConst.Lang.CN;
            }
            adminModelSave.setLang(defaultLangCode);

            //校验数据
            if (!isUpdate)
            {
                SystemConfigurationModel systemConfiguration = new SystemConfigurationModel();
                systemConfiguration.setStore_id(vo.getStoreId());
                systemConfiguration = systemConfigurationModelMapper.selectOne(systemConfiguration);
                //添加
                AdminModel adminModel = new AdminModel();
                adminModel.setStore_id(vo.getStoreId());
                adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                adminModel.setName(vo.getAdminName());
                count = adminModelMapper.selectCount(adminModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_ZHYCZQCXSR, "账户已存在，请重新输入", "addAdminInfo");
                }
                //不能和超级管理员重复账号
                adminModel.setStore_id(AdminModel.PLATFORM_STORE_ID);
                count = adminModelMapper.selectCount(adminModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERRCODE_ZHYCZQCXSR, "账户已存在，请重新输入", "addAdminInfo");
                }
                adminModelSave.setSid(admin.getId());
                adminModelSave.setType(AdminModel.TYPE_STORE_ADMIN);
                adminModelSave.setStore_id(vo.getStoreId());
                adminModelSave.setName(vo.getAdminName());
                adminModelSave.setAdd_date(new Date());
                //设置默认头像
                if (systemConfiguration != null && systemConfiguration.getAdminDefaultPortrait() != null)
                {
                    adminModelSave.setPortrait(systemConfiguration.getAdminDefaultPortrait());
                }
            }
            else
            {
                //修改
                adminModelSave.setId(adminModelOld.getId());
            }
            adminModelSave = DataCheckTool.checkAdminDataFormate(adminModelSave, isUpdate);
            if (isUpdate)
            {
                count = adminModelMapper.updateByPrimaryKeySelective(adminModelSave);
                publiceService.addAdminRecord(vo.getStoreId(), "修改了管理员ID：" + adminModelOld.getId() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                count = adminModelMapper.insertSelective(adminModelSave);
                publiceService.addAdminRecord(vo.getStoreId(), "添加了管理员名称：" + adminModelSave.getName(), AdminRecordModel.Type.ADD, vo.getAccessId());

            }

            if (count < 1)
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
            logger.error("添加/修改管理员信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addAdminInfo");
        }
    }

    @Override
    public void delAdminInfo(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (admin.getId().equals(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            String     adminName  = adminModelMapper.selectByPrimaryKey(id).getName();
            AdminModel adminModel = new AdminModel();
            adminModel.setStore_id(vo.getStoreId());
            adminModel.setId(id);

            //添加日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了管理员名称：" + adminName, AdminRecordModel.Type.DEL, vo.getAccessId());


            if (adminModelMapper.delete(adminModel) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSB, "删除失败");
            }
            //踢出当前正在登录的该账号
            String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId();
            String tokenOld = redisUtil.get(logKey) + "";
            if (StringUtils.isNotEmpty(tokenOld) && redisUtil.hasKey(tokenOld))
            {
                logger.debug("踢出当前正在登录的该账号 token{}", tokenOld);
                redisUtil.del(logKey);
                redisUtil.del(tokenOld);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除管理员信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAdminInfo");
        }
    }

    @Override
    public void stopAdmin(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel admin = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            String     event = "";
            if (admin.getId().equals(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            AdminModel adminModel = new AdminModel();
            adminModel.setId(id);
            adminModel.setStore_id(vo.getStoreId());
            adminModel = adminModelMapper.selectOne(adminModel);
            if (adminModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GLYBCZ, "管理员不存在");
            }
            AdminModel adminModelUpdate = new AdminModel();
            adminModelUpdate.setId(id);
            int status = adminModel.getStatus();
            if (status == AdminModel.STATUS_DISABLE)
            {
                status = AdminModel.STATUS_OPEN;
                event = "将管理员名称：" + adminModel.getName() + "进行了启用操作";
            }
            else
            {
                status = AdminModel.STATUS_DISABLE;
                event = "将管理员名称：" + adminModel.getName() + "进行了禁用操作";
            }
            //重置登录次数
            adminModelUpdate.setLogin_num(0);
            adminModelUpdate.setStatus(status);

            if (adminModelMapper.updateByPrimaryKeySelective(adminModelUpdate) < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            //踢出当前正在登录的该账号
            String logKey   = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + adminModel.getId();
            String tokenOld = redisUtil.get(logKey) + "";
            if (StringUtils.isNotEmpty(tokenOld) && redisUtil.hasKey(tokenOld))
            {
                logger.debug("踢出当前正在登录的该账号 token{}", tokenOld);
                redisUtil.del(logKey);
                redisUtil.del(tokenOld);
            }
            publiceService.addAdminRecord(vo.getStoreId(), event, AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("禁用/启用管理员信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "stopAdmin");
        }
    }

    @Override
    public Map<String, Object> getAdminLoggerInfo(LoggerAdminVo vo, HttpServletResponse response) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("source", AdminRecordModel.Source.PC_PLATFORM);
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("logAccountType", vo.getLogAccountType());//日志账号类型
            parmaMap.put("logOperationType", vo.getLogOperationType());//日志操作类型
            if (!StringUtils.isEmpty(vo.getStartDate()))
            {
                parmaMap.put("startDate", vo.getStartDate());
                if (!StringUtils.isEmpty(vo.getEndDate()))
                {
                    parmaMap.put("endDate", vo.getEndDate());
                }
            }
            if (!StringUtils.isEmpty(vo.getAdminName()))
            {
                parmaMap.put("adminName_like", vo.getAdminName());
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());

            List<Map<String, Object>> dataList = adminRecordModelMapper.selectAdminLoggerInfo(parmaMap);
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
            int total = adminRecordModelMapper.countAdminLoggerInfo(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取管理眼日志列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAdminLoggerInfo");
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
    public boolean delAdminLogger(MainVo vo, String ids) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(ids))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_BNWK, "id不能为空");
            }
            List<String> idList = Arrays.asList(ids.split(","));

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("idList", idList);

            publiceService.addAdminRecord(vo.getStoreId(), "将管理员日志进行了批量删除操作", AdminRecordModel.Type.DEL, vo.getAccessId());
            return adminRecordModelMapper.delAdminLoggerInfo(parmaMap) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除管理员日志信息 异常" + e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delAdminLogger");
        }
    }

    @Override
    public Map<String, Object> getUserRoleInfo(MainVo vo, Integer id, boolean isPt, String lang_code) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel                adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<Map<String, Object>> menuList   = publicRoleService.getRoleTreeList(vo.getStoreId(), id, adminModel.getId(), isPt, lang_code);
            resultMap.put("menuList", menuList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取权限列表信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserRoleInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserRoles(MainVo vo, Integer roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (roleId != null)
            {
                resultMap.put("roleId", roleId);
            }
            else
            {
                //获取当前用户权限id
                resultMap.put("adminName", adminModel.getName());
                resultMap.put("roleId", adminModel.getRole());
            }

            //获取商城权限下拉
            RoleModel roleModel = new RoleModel();
            roleModel.setStore_id(vo.getStoreId());
            roleModel.setStatus(RoleModel.STATUS_ROLE);

            Map<String, Object> paramMap = new HashMap<>(16);
            paramMap.put("store_id_pt", vo.getStoreId());
            paramMap.put("status", RoleModel.STATUS_ROLE);
            paramMap.put("existsMenu", "existsMenu");
            List<Map<String, Object>> roleModelList = roleModelMapper.selectRoleInfo(paramMap);

            resultMap.put("roleList", roleModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取权限下拉信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserRoles");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getUserRoleMenuInfo(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            resultMap.put("menuList", publicRoleService.getMenuTreeList(null));
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("获取权限菜单信息 异常" + e.getMessage());
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getUserRoleMenuInfo");
        }
        return resultMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserRoleMenu(AddRoleVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            // 1. 基础校验（不变）
            if (StringUtils.isEmpty(vo.getRoleName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMCBNWK, "角色名称不能为空");
            }
            if (vo.getRoleName().length() > 20)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMCBNCGGZWZZD, "角色名称不能超过20个中文字长度");
            }
            if (vo.getPermissions() == null || vo.getPermissions().isEmpty())
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZBDQX, "请选择绑定权限");
            }
            if (vo.getStatus() == null || vo.getStatus() < 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZTBNWK, "状态不能为空");
            }

            // 获取当前语种
            String langCode = vo.getLang_code();

            // 2. 判断是新增还是修改
            RoleModel roleModelOld = null;
            if (id != null && id > 0)
            {
                roleModelOld = roleModelMapper.selectByPrimaryKey(id);
                if (roleModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSBCZ, "角色不存在");
                }
            }

            // 3. 校验角色名称唯一性（不变）
            if (roleModelOld == null || !roleModelOld.getName().equals(vo.getRoleName()))
            {
                RoleModel check = new RoleModel();
                check.setStore_id(vo.getStoreId());
                check.setName(vo.getRoleName());
                if (roleModelMapper.selectCount(check) > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_JSMCYCZ, "角色名称已存在");
                }
            }

            // 4. 保存或更新角色基本信息（不变）
            RoleModel roleModelSave = new RoleModel();
            roleModelSave.setName(vo.getRoleName());
            roleModelSave.setRole_describe(vo.getDescribe());
            roleModelSave.setStatus(vo.getStatus());

            if (roleModelOld != null)
            {
                // 修改
                roleModelSave.setId(roleModelOld.getId());
                roleModelMapper.updateByPrimaryKeySelective(roleModelSave);
                publiceService.addAdminRecord(vo.getStoreId(),
                        "修改了角色名称：" + roleModelSave.getName() + "的 " + langCode + " 语种权限",
                        AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                // 新增
                roleModelSave.setStore_id(vo.getStoreId());
                roleModelSave.setAdd_date(new Date());
                roleModelMapper.insertSelective(roleModelSave);
                publiceService.addAdminRecord(vo.getStoreId(),
                        "添加了角色名称：" + roleModelSave.getName() + "（" + langCode + " 语种）",
                        AdminRecordModel.Type.ADD, vo.getAccessId());
            }

            final Integer roleId = roleModelSave.getId();

            // 5. 只删除当前语种的旧权限菜单和导览菜单（关键改动！）
            // 先查当前语种的所有菜单 ID
            List<Integer> currentLangMenuIds = coreMenuModelMapper.getMenuIdsByLangCode(langCode);
            if (!CollectionUtils.isEmpty(currentLangMenuIds))
            {
                // 批量删除当前语种的权限菜单
                roleMenuModelMapper.deleteByRoleAndMenuIds(roleId, currentLangMenuIds);
                logger.info("删除角色 {} 的 {} 语种旧权限菜单，共 {} 条", roleId, langCode, currentLangMenuIds.size());

                // 批量删除当前语种的导览菜单
                guideMenuModelMapper.deleteGuideByRoleAndMenuIds(roleId, currentLangMenuIds);
                logger.info("删除角色 {} 的 {} 语种旧导览菜单，共 {} 条", roleId, langCode, currentLangMenuIds.size());
            }

            // 6. 批量插入当前语种的新权限菜单和导览菜单
            List<RoleMenuModel>  roleMenuList  = new ArrayList<>();
            List<GuideMenuModel> guideMenuList = new ArrayList<>();
            int                  guideSort     = 0;

            // 获取所有插件菜单ID（不变）
            Set<Integer> plugMenuIds = new HashSet<>(coreMenuModelMapper.getAllPlugMenuId());

            // 只插入当前语种的权限
            for (Integer menuId : vo.getPermissions())
            {
                // 校验该 menuId 是否属于当前语种（防止前端传错）
                CoreMenuModel menu = coreMenuModelMapper.selectByPrimaryKey(menuId);
                if (menu == null)
                {
                    logger.warn("跳过不属于 {} 语种的菜单ID：{}", langCode, menuId);
                    continue;
                }

                // 权限菜单
                RoleMenuModel rm = new RoleMenuModel();
                rm.setRole_id(roleId);
                rm.setMenu_id(menuId);
                rm.setAdd_date(new Date());
                roleMenuList.add(rm);

                // 导览菜单
                GuideMenuModel gm = new GuideMenuModel();
                gm.setStore_id(vo.getStoreId());
                gm.setRole_id(roleId);
                gm.setMenu_id(menuId);
                gm.setGuide_sort(guideSort++);
                gm.setAdd_date(new Date());
                guideMenuList.add(gm);
            }

            // 批量插入权限菜单（避免重复）
            if (!roleMenuList.isEmpty())
            {
                roleMenuModelMapper.batchInsertIgnore(roleMenuList);
            }

            // 批量插入导览菜单
            if (!guideMenuList.isEmpty())
            {
                guideMenuModelMapper.batchInsert(guideMenuList);
            }

            // 7. 处理插件权限开关（优化为批量更新）
            Map<Integer, Boolean> plugStatusMap = new HashMap<>();
            for (Integer menuId : vo.getPermissions())
            {
                if (plugMenuIds.contains(menuId))
                {
                    plugStatusMap.put(menuId, true);
                }
            }

            // 获取所有平台管理员（只查一次）
            AdminModel adminQuery = new AdminModel();
            adminQuery.setType(AdminModel.TYPE_CLIENT);
            adminQuery.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<AdminModel> platformAdmins = adminModelMapper.select(adminQuery);

            // 批量更新插件状态
            for (AdminModel admin : platformAdmins)
            {
                // 跳过平台默认管理员
                if (admin.getStore_id() == 0) continue;

                for (Map.Entry<Integer, Boolean> entry : plugStatusMap.entrySet())
                {
                    CoreMenuModel menu = coreMenuModelMapper.selectByPrimaryKey(entry.getKey());
                    if (menu == null) continue;

                    String pluginCode = getPluginCode(menu.getModule());
                    int flag = entry.getValue() ? DictionaryConst.WhetherMaven.WHETHER_NO
                            : DictionaryConst.WhetherMaven.WHETHER_OK;

                    pluginsModelMapper.updateFlagByPluginCodeAndStoreId(admin.getStore_id(), pluginCode, flag);
                }
            }

            // 8. 踢出所有绑定该角色的管理员登录（优化为批量删除 Redis）
            List<String> tokensToDel  = new ArrayList<>();
            List<String> logKeysToDel = new ArrayList<>();

            for (AdminModel admin : platformAdmins)
            {
                if (admin.getRole().equals(String.valueOf(roleId)))
                {
                    String logKey = GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_FLAG + admin.getId();
                    String token  = redisUtil.get(logKey) + "";
                    if (StringUtils.isNotEmpty(token))
                    {
                        tokensToDel.add(token);
                        logKeysToDel.add(logKey);
                        logger.info("修改权限，踢出登录：{}", admin.getName());
                    }
                }
            }

            // 批量删除 Redis
            if (!tokensToDel.isEmpty())
            {
                redisUtil.del(tokensToDel);
            }
            if (!logKeysToDel.isEmpty())
            {
                redisUtil.del(logKeysToDel);
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/编辑角色信息异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addUserRoleMenu");
        }
        finally
        {
            redisUtil.delByPattern("admin:async_routes:*");
        }
    }

    /**
     * 插件模块转插件code（可抽取到常量类）
     */
    private String getPluginCode(String module)
    {
        switch (module)
        {
            case "coupons":
                return "coupon";
            case "stores":
                return "mch";
            case "seckill":
                return "seconds";
            case "integralMall":
                return "integral";
            case "preSale":
                return "presell";
            case "group":
                return "go_group";
            case "discountSetInfo":
                return "flashsale";
            default:
                return module;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delUserRoleMenu(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel userCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //权限是否绑定
            AdminModel adminModel = new AdminModel();
            adminModel.setRole(id + "");
            adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            int count = adminModelMapper.selectCount(adminModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXJCBDGXZJXSCCZ, "请先解除绑定关系,再进行删除操作!");
            }
            //获取平台菜单id
            List<CoreMenuModel> coreMenuSystems = coreMenuModelMapper.getSystemMenu();
            if (coreMenuSystems == null || coreMenuSystems.size() == 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDSJCW, "菜单数据错误");
            }
            //删除对应角色
            RoleModel roleModel = roleModelMapper.selectByPrimaryKey(id);
            count = roleModelMapper.deleteByPrimaryKey(roleModel.getId());
            if (count < 1)
            {
                logger.debug("角色删除失败 roleId:{}", id);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLFM, "网络繁忙");
            }

            for (CoreMenuModel coreMenuSystem : coreMenuSystems)
            {
                //删除之前权限菜单
                count = roleMenuModelMapper.deleteMenu(id, coreMenuSystem.getId());
                logger.info("一共删除权限菜单 {} 个", count);
                //删除之前的权限导览
                count = guideMenuModelMapper.deleteGuidMenu(coreMenuSystem.getId(), id);
                logger.info("一共删除权限菜单(功能导览) {} 个", count);
            }

            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了角色名称：" + roleModel.getName(), AdminRecordModel.Type.DEL, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除角色信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delUserRoleMenu");
        }
    }
}

