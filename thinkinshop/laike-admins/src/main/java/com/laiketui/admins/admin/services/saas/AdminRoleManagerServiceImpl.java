package com.laiketui.admins.admin.services.saas;

import com.laiketui.admins.api.admin.saas.AdminRoleManagerService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.api.admin.PublicAdminService;
import com.laiketui.common.api.admin.PublicRoleService;
import com.laiketui.common.mapper.*;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.mch.RoleModel;
import com.laiketui.domain.role.CoreMenuModel;
import com.laiketui.domain.role.GuideMenuModel;
import com.laiketui.domain.role.RoleMenuModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

/**
 * 权限管理
 *
 * @author Trick
 * @date 2021/1/28 16:16
 */
@Service
public class AdminRoleManagerServiceImpl implements AdminRoleManagerService
{

    private final Logger logger = LoggerFactory.getLogger(AdminShopManageServiceImpl.class);

    @Override
    public Map<String, Object> getMenuInfo(MainVo vo, String name, Integer id, Integer sid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("recycle", 0);
            if (id != null)
            {
                parmaMap.put("id", id);
                //获取所有上级权限
                List<CoreMenuModel> coreMenuModelList = new ArrayList<>();
                publicRoleService.getRoleFatherById(id, coreMenuModelList);
                if (coreMenuModelList.size() > 0)
                {
                    coreMenuModelList.remove(0);
                }
                resultMap.put("roleList", coreMenuModelList);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("title", name);
            }
            if (sid != null && sid > 0)
            {
                parmaMap.put("sid", sid);
            }
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());

            int                       total    = coreMenuModelMapper.countDynamic(parmaMap);
            List<Map<String, Object>> dataList = coreMenuModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                int fatherId = (int) map.get("s_id");
                if (fatherId > 0)
                {
                    //获取上级名称
                    CoreMenuModel coreMenuModel = coreMenuModelMapper.selectByPrimaryKey(fatherId);
                    map.put("fatherName", coreMenuModel.getTitle());
                }
            }

            resultMap.put("total", total);
            resultMap.put("dataList", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取核心菜单信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getMenuInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getMenuLeveInfo(MainVo vo, String name, Integer id, Integer sid, Integer type, Integer isCore) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            if (id != null && id > 0)
            {
                parmaMap.put("id", id);
            }
            else
            {
                if (sid != null && sid > 0)
                {
                    parmaMap.put("sid", sid);
                }
                else
                {
                    parmaMap.put("sid", 0);
                }
            }
            if (type != null)
            {
                parmaMap.put("type", type);
            }
            if (isCore != null)
            {
                parmaMap.put("isCore", isCore);
            }
            if (!StringUtils.isEmpty(name))
            {
                parmaMap.put("titleORid", name);
            }

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> dataList = coreMenuModelMapper.selectDynamic(parmaMap);
            for (Map<String, Object> map : dataList)
            {
                int    mid   = MapUtils.getInteger(map, "id");
                String mname = MapUtils.getString(map, "title");
                mname = PinyinUtils.getPinYinHeadChar(mname);
                int fatherId = (int) map.get("s_id");
                map.put("id_id", mname + "_" + mid);
                map.put("sid", mname + "_" + fatherId);

                map.put("lang_name", publiceService.getLangName(MapUtils.getString(map, "lang_code")));
                map.put("image", publiceService.getImgPath(MapUtils.getString(map, "image"), vo.getStoreId()));
                map.put("image1", publiceService.getImgPath(MapUtils.getString(map, "image1"), vo.getStoreId()));
                if (fatherId > 0)
                {
                    //获取上级名称
                    CoreMenuModel coreMenuModel = coreMenuModelMapper.selectByPrimaryKey(fatherId);
                    map.put("fatherName", coreMenuModel.getTitle());
                }
            }
            int total = coreMenuModelMapper.countDynamic(parmaMap);

            resultMap.put("list", dataList);
            resultMap.put("total", total);
            resultMap.put("sid", sid);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取上级菜单信息 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getFatherMenuInfo");
        }
        return resultMap;
    }

    @Override
    public void moveMenuSort(MainVo vo, int id, Integer moveId, Integer type) throws LaiKeAPIException
    {
        try
        {
            AdminModel    adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int           row           = 0;
            CoreMenuModel coreMenuModel = new CoreMenuModel();
            coreMenuModel.setId(id);
            coreMenuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            coreMenuModel = coreMenuModelMapper.selectOne(coreMenuModel);
            if (coreMenuModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            //1=置顶 2=上移 下移
            String event;
            switch (type)
            {
                case 1:
                    int sort = coreMenuModelMapper.maxSort(coreMenuModel.getLevel());
                    CoreMenuModel coreMenuUpdate = new CoreMenuModel();
                    coreMenuUpdate.setId(id);
                    coreMenuUpdate.setSort(sort + 1);
                    row = coreMenuModelMapper.updateByPrimaryKeySelective(coreMenuUpdate);
                    //添加操作日志
                    publiceService.addAdminRecord(vo.getStoreId(), "置顶了菜单ID：" + id, AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    break;
                case 2:
                    row = coreMenuModelMapper.moveSort(id, moveId);
                    publiceService.addAdminRecord(vo.getStoreId(), "上移/下移了菜单ID：" + id + "," + moveId, AdminRecordModel.Type.UPDATE, vo.getAccessId());
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XGSB, "修改失败");
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("菜单顺序移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "moveMenuSort");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenuInfo(AddMenuVo vo) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            if (StringUtils.isEmpty(vo.getMenuName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDMCBNWK, "菜单名称不能为空");
            }
            else if (vo.getMenuName().length() > 20)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDMCBNCGLGZF, "菜单名称不能超过六个字符");
            }
            if (vo.getLevel() == null || vo.getLevel() < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDJBBNWK, "菜单级别不能为空");
            }
            if (vo.getLevel().equals(1))
            {
                if (StringUtils.isEmpty(vo.getDefaultLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZCDMRTB, "请选择菜单默认图标");
                }
                if (StringUtils.isEmpty(vo.getChekedLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXZCDXZTB, "请选择菜单选中图标");
                }
            }

            CoreMenuModel coreMenuModelSave = new CoreMenuModel();
            coreMenuModelSave.setTitle(vo.getMenuName());
            coreMenuModelSave.setLevel(vo.getLevel());
            coreMenuModelSave.setIs_core(vo.getIsCore());
            coreMenuModelSave.setIs_plug_in(0);
            coreMenuModelSave.setType(vo.getMenuClass());
            coreMenuModelSave.setGuide_name(vo.getGuideName());
            coreMenuModelSave.setUrl(vo.getMenuUrl());
            coreMenuModelSave.setModule(vo.getPath());
            coreMenuModelSave.setAction(vo.getMenuPath());
            coreMenuModelSave.setCountry_num(vo.getCountry_num());
            coreMenuModelSave.setLang_code(vo.getLang_code());

            if (StringUtils.isNotEmpty(vo.getDefaultLogo()))
            {
                coreMenuModelSave.setImage(vo.getDefaultLogo());
            }
            if (StringUtils.isNotEmpty(vo.getChekedLogo()))
            {
                coreMenuModelSave.setImage1(vo.getChekedLogo());
            }

            CoreMenuModel coreMenuModel = null;
            if (vo.getMid() != null)
            {
                coreMenuModel = coreMenuModelMapper.selectByPrimaryKey(vo.getMid());
                if (coreMenuModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
                }
            }

            if (coreMenuModel == null || !coreMenuModel.getTitle().equals(vo.getMenuName()))
            {
                Map<String, Object> parmaMap = new HashMap<>(16);
                parmaMap.put("title", vo.getMenuName());
                parmaMap.put("sid", vo.getFatherMenuId());
                parmaMap.put("type", vo.getMenuClass());
                parmaMap.put("isCore", vo.getIsCore());

                parmaMap.put("lang_code", vo.getLang_code());
                parmaMap.put("country_num", vo.getCountry_num());

                parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
                count = coreMenuModelMapper.countDynamic(parmaMap);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDMCYCZ, "菜单名称已存在");
                }
            }

            if (vo.getLevel() == 1)
            {
                //一级菜单
                if (StringUtils.isEmpty(vo.getDefaultLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_MRTBBNWK, "默认图标不能为空");
                }
                if (StringUtils.isEmpty(vo.getChekedLogo()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XZTBBNWK, "选中图标不能为空");
                }
                coreMenuModelSave.setS_id(0);
            }
            else
            {
                if (StringUtils.isEmpty(vo.getMenuUrl()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LJBNWK, "路径不能为空");
                }
                if (vo.getFatherMenuId() == null || vo.getFatherMenuId() < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCDBNWK, "上级菜单不能为空");
                }
                coreMenuModelSave.setS_id(vo.getFatherMenuId());
                coreMenuModelSave.setBriefintroduction(vo.getBriefintroduction());
            }
            if (vo.getLevel() == 1 && vo.getIsButton() == 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJCDBKZWQXAN, "一级菜单不可作为权限按钮");
            }
            if (vo.getLevel() == 1 && vo.getIsTab() == 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YJCDBKSZWTABYM, "一级菜单不可设置为tab页面");
            }
            coreMenuModelSave.setIs_button(vo.getIsButton());
            coreMenuModelSave.setIsTab(vo.getIsTab());
            if (coreMenuModel != null)
            {
                coreMenuModelSave.setId(coreMenuModel.getId());
                //如果是从别的菜单改过来的则重新获取序号
                if (!coreMenuModel.getS_id().equals(vo.getFatherMenuId()))
                {
                    int sort = coreMenuModelMapper.maxSortByLevel(vo.getLevel(), vo.getFatherMenuId(), vo.getMenuClass()) + 1;
                    logger.debug("菜单id{}从别的菜单改过来 重新获取序号{}", coreMenuModel.getId(), sort);
                    coreMenuModelSave.setSort(sort);
                }
                count = coreMenuModelMapper.updateByPrimaryKeySelective(coreMenuModelSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了菜单ID：" + coreMenuModelSave.getId() + " 的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                //获取当前级别最新序号
                int sort = coreMenuModelMapper.maxSortByLevel(vo.getLevel(), vo.getFatherMenuId(), vo.getMenuClass()) + 1;
                coreMenuModelSave.setSort(sort);
                coreMenuModelSave.setAdd_time(new Date());
                coreMenuModelSave.setRecycle(DictionaryConst.WhetherMaven.WHETHER_NO);
                count = coreMenuModelMapper.insertSelective(coreMenuModelSave);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了菜单ID：" + coreMenuModelSave.getId(), AdminRecordModel.Type.ADD, vo.getAccessId());
                //如果是控制台,则给系统管理员绑定权限
                if (vo.getMenuClass() == 1)
                {
                    AdminModel sysAdmin = adminModelMapper.getSystemAdmin();
                    if (sysAdmin != null)
                    {
                        //保存功能导览
                        logger.debug("给系统管理员{} 绑定权限", sysAdmin.getName());
                        GuideMenuModel guideMenuModel = new GuideMenuModel();

                        guideMenuModel.setCountry_num(vo.getCountry_num());
                        guideMenuModel.setLang_code(vo.getLang_code());

                        guideMenuModel.setStore_id(vo.getStoreId());
                        guideMenuModel.setRole_id(Integer.parseInt(sysAdmin.getRole()));
                        guideMenuModel.setMenu_id(coreMenuModelSave.getId());
                        //判断功能导览是否已经存在,如果存在则不操作
                        if (guideMenuModelMapper.selectCount(guideMenuModel) == 0)
                        {
                            //排序
                            guideMenuModel.setGuide_sort(guideMenuModelMapper.maxSort(vo.getStoreId(), guideMenuModel.getRole_id()));
                            guideMenuModel.setAdd_date(new Date());
                            guideMenuModelMapper.insertSelective(guideMenuModel);
                        }
                        //权限菜单
                        RoleMenuModel roleMenuModel = new RoleMenuModel();
                        roleMenuModel.setRole_id(guideMenuModel.getRole_id());
                        roleMenuModel.setMenu_id(coreMenuModelSave.getId());
                        if (guideMenuModelMapper.selectCount(guideMenuModel) == 0)
                        {
                            roleMenuModel.setAdd_date(new Date());
                            roleMenuModelMapper.insertSelective(roleMenuModel);
                        }

                    }
                }
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
            logger.error("添加/编辑菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addMenuInfo");
        }
        finally
        {
            redisUtil.delByPattern("admin:async_routes:*");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delMenu(MainVo vo, int menuId) throws LaiKeAPIException
    {
        try
        {
            AdminModel    adminModel    = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            CoreMenuModel coreMenuModel = new CoreMenuModel();
            coreMenuModel.setId(menuId);
            coreMenuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            CoreMenuModel coreMenuModelOld = coreMenuModelMapper.selectOne(coreMenuModel);
            if (coreMenuModelOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            coreMenuModel.setId(null);
            coreMenuModel.setS_id(coreMenuModelOld.getId());
            int count = coreMenuModelMapper.selectCount(coreMenuModel);
            if (count > 0)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SCSBQXSCZCD, "删除失败,请先删除子菜单");
            }

            count = coreMenuModelMapper.delCoreMenu(coreMenuModelOld.getId());
            logger.debug("删除菜单id{},一共删除{}条权限信息", coreMenuModelOld.getId(), count);
            if (count < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDSCSB, "菜单删除失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "删除了菜单ID：" + menuId, AdminRecordModel.Type.DEL, vo.getAccessId());
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
        finally
        {
            redisUtil.delByPattern("admin:async_routes:*");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean bindRole(MainVo vo, int roleId, List<Integer> adminIds) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminUserCache = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            for (Integer adminId : adminIds)
            {
                AdminModel adminModel = new AdminModel();
                adminModel.setId(adminId);
                adminModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                adminModel = adminModelMapper.selectOne(adminModel);
                if (adminModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LBZYGLYBCZ, "列表中有管理员不存在");
                }
                AdminModel adminModelUpdate = new AdminModel();
                adminModelUpdate.setId(adminModel.getId());
                adminModelUpdate.setStore_id(adminModel.getStore_id());
                //是否绑定,如果绑定则取消绑定,未绑定则绑定
                if (!StringUtils.isEmpty(adminModel.getRole()) && adminModel.getRole().equals(roleId + ""))
                {
                    //取消绑定  设定一个默认角色，此角色不显示在角色列表内以防被删除，商城无任何角色绑定时绑定此角色作为缺省值(禅道 45626)
                    RoleModel roleModelSave = new RoleModel();
                    roleModelSave.setStatus(RoleModel.STATUS_ACQUIESCE);
                    List<RoleModel> roleModelList = roleModelMapper.select(roleModelSave);
                    if (StringUtils.isNotEmpty(roleModelList) && roleModelList.size() > 0)
                    {
                        adminModelUpdate.setRole(roleModelList.get(0).getId().toString());
                    }
                    else
                    {
                        adminModelUpdate.setRole("");
                    }
                }
                else
                {
                    adminModelUpdate.setRole(String.valueOf(roleId));
                }
                //踢下线
                publicAdminService.outLoginAdminById(adminModel.getStore_id(), adminId);
                int count = adminModelMapper.updateByPrimaryKeySelective(adminModelUpdate);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                }

                RoleModel roleModel = roleModelMapper.selectByPrimaryKey(roleId);
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了角色名称：" + roleModel.getName() + "的绑定商户", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("绑定/解绑角色 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "bindRole");
        }
        finally
        {
            redisUtil.delByPattern("admin:async_routes:*");
        }

    }

    @Override
    public Map<String, Object> getRoleMenu(MainVo vo, int roleId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("roleId", roleId);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("levelList", new Integer[]{1, 2});
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Integer> menuList = coreMenuModelMapper.getRoleMenuIds(parmaMap);

            resultMap.put("menuList", menuList);
            resultMap.put("btnIds", new ArrayList<>());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("根据角色获取菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleMenu");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getAsyncRoutesByRoutes(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            //禅道47644 前端固定使用了1 导致了该问题, 前端不使用默认1后端特殊处理
            if (vo.getStoreId() == 0)
            {
                vo.setStoreId(1);
            }
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //获取当前商城自营店
            Integer mchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (mchId != null)
            {
                user.setShop_id(mchId);
                //暂时不修改
//                user.setStore_id(vo.getStoreId());
            }
            RedisDataTool.refreshRedisAdminCache(vo.getAccessId(), user, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MANAGE_TOKEN, redisUtil);
            //是否平台账号
            boolean isPtUser = user.getType().equals(AdminModel.TYPE_SYSTEM_ADMIN);
            //需要剔除的菜单id集
            List<Integer> limitMenuIdList = new ArrayList<>();
            //获取当前商城账号权限,如果是平台则获取商城管理员账号
            Integer roleId;
            if (isPtUser)
            {
                roleId = adminModelMapper.getStoreRole(vo.getStoreId());
            }
            else
            {
                roleId = Integer.parseInt(user.getRole());
                //如果账号是平台普通管理员,自身的权限以商城管理员拥有的权限为基础做筛选
                if (user.getType().equals(AdminModel.TYPE_STORE_ADMIN))
                {
                    limitMenuIdList = roleMenuModelMapper.getStoreRoleMenuInfo(roleId, adminModelMapper.getStoreRole(vo.getStoreId()));
                }
            }
            if (roleId == null)
            {
                logger.error("商城{},adminId【{}】 权限为空", vo.getStoreId(), roleId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据错误");
            }
            String              cacheKey = buildAsyncRoutesCacheKey(roleId, vo.getStoreId(), vo.getLanguage(), user.getType());
            Map<String, Object> cached   = (Map<String, Object>) redisUtil.get(cacheKey);
            if (cached != null)
            {
                logger.debug("菜单路由命中缓存: {}", cacheKey);
                return cached;
            }

            List<Map<String, Object>> menuList = new ArrayList<>();
            //判断是否是平台,如果是平台则再当前商城权限基础上增加平台菜单
            if (AdminModel.TYPE_SYSTEM_ADMIN == user.getType())
            {
                menuList.addAll(buildPlatformMenus(vo.getLanguage()));
            }

            menuList.addAll(buildRoleMenus(roleId, vo.getLanguage(), limitMenuIdList));
            resultMap.put("menu", menuList);
            // 1800 秒 = 30 分钟
            redisUtil.set(cacheKey, resultMap, 1800);
            return resultMap;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取权限菜单---路由结构 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAsyncRoutesByRoutes");
        }
    }

    /**
     * 构建菜单路由缓存key（基于角色/商城/语种/账号类型）
     */
    private String buildAsyncRoutesCacheKey(Integer roleId, Integer storeId, String language, Integer userType)
    {
        return "admin:async_routes:role:" + roleId + ":store:" + storeId + ":lang:" + language + ":type:" + userType;
    }

    /**
     * 组装平台菜单（type=0）
     */
    private List<Map<String, Object>> buildPlatformMenus(String language) throws LaiKeAPIException
    {
        Map<String, Object> parmaMap = new HashMap<>(16);
        parmaMap.put("type", 0);
        parmaMap.put("lang_code", language);
        parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
        List<Map<String, Object>> allMenus = coreMenuModelMapper.getRoleMenuInfos(parmaMap);
        return buildMenuTree(allMenus, null);
    }

    /**
     * 组装角色菜单
     */
    private List<Map<String, Object>> buildRoleMenus(Integer roleId, String language, List<Integer> limitMenuIds) throws LaiKeAPIException
    {
        Map<String, Object> parmaMap = new HashMap<>(16);
        parmaMap.put("roleId", roleId);
        parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
        parmaMap.put("lang_code", language);
        parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
        List<Map<String, Object>> allMenus = coreMenuModelMapper.getRoleMenuInfos(parmaMap);
        return buildMenuTree(allMenus, limitMenuIds);
    }

    /**
     * 构建菜单树（批量加载 + 内存组装）
     */
    private List<Map<String, Object>> buildMenuTree(List<Map<String, Object>> allMenus, List<Integer> limitMenuIds) throws LaiKeAPIException
    {
        if (allMenus == null || allMenus.isEmpty())
        {
            return new ArrayList<>();
        }

        Set<Integer> limitSet = null;
        if (limitMenuIds != null && !limitMenuIds.isEmpty())
        {
            limitSet = new HashSet<>(limitMenuIds);
        }

        Map<Integer, List<Map<String, Object>>> childrenMap = new HashMap<>(16);
        List<Integer> menuIds = new ArrayList<>();
        for (Map<String, Object> menu : allMenus)
        {
            Integer id = MapUtils.getInteger(menu, "id");
            if (id == null)
            {
                continue;
            }
            if (limitSet != null && !limitSet.contains(id))
            {
                continue;
            }
            Integer sid = MapUtils.getInteger(menu, "s_id");
            if (sid == null)
            {
                sid = 0;
            }
            childrenMap.computeIfAbsent(sid, k -> new ArrayList<>()).add(menu);
            menuIds.add(id);
        }

        Set<Integer> tabParentIds = new HashSet<>();
        if (!menuIds.isEmpty())
        {
            List<Integer> tabIds = coreMenuModelMapper.getMenuIdsWithTabChildren(menuIds);
            if (tabIds != null)
            {
                tabParentIds.addAll(tabIds);
            }
        }

        return buildMenuChildren(0, 0, childrenMap, tabParentIds);
    }

    private List<Map<String, Object>> buildMenuChildren(int parentId, int parentLevel,
                                                        Map<Integer, List<Map<String, Object>>> childrenMap,
                                                        Set<Integer> tabParentIds)
    {
        List<Map<String, Object>> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty())
        {
            return new ArrayList<>();
        }

        int expectedLevel = parentLevel + 1;
        List<Map<String, Object>> levelChildren = new ArrayList<>();
        for (Map<String, Object> menu : children)
        {
            int level = MapUtils.getIntValue(menu, "level");
            if (level == expectedLevel)
            {
                levelChildren.add(menu);
            }
        }

        levelChildren.sort((a, b) ->
                Integer.compare(MapUtils.getIntValue(b, "sort"), MapUtils.getIntValue(a, "sort")));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> menu : levelChildren)
        {
            int id = MapUtils.getIntValue(menu, "id");
            int level = MapUtils.getIntValue(menu, "level");

            String image = publiceService.getImgPath(MapUtils.getString(menu, "image"), 0);
            String image1 = publiceService.getImgPath(MapUtils.getString(menu, "image1"), 0);
            menu.put("image", image);
            menu.put("image1", image1);

            List<Map<String, Object>> childList = buildMenuChildren(id, level, childrenMap, tabParentIds);

            if (tabParentIds.contains(id))
            {
                boolean haveTab = false;
                for (Map<String, Object> child : childList)
                {
                    if (MapUtils.getIntValue(child, "is_tab") == DictionaryConst.WhetherMaven.WHETHER_OK)
                    {
                        if (StringUtils.isEmpty(MapUtils.getString(menu, "url")))
                        {
                            menu.put("url", MapUtils.getString(child, "url"));
                            menu.put("action", MapUtils.getString(child, "action"));
                        }
                        haveTab = true;
                        break;
                    }
                }
                if (!haveTab)
                {
                    continue;
                }
            }

            boolean isChildren = !childList.isEmpty() && level < 3;
            menu.put("children", childList);
            menu.put("isChildren", isChildren);
            result.add(menu);
        }

        return result;
    }


    @Override
    public Map<String, Object> getAsyncRoutesByRoutes(MainVo vo, Integer sid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("roleId", user.getRole());
            if (sid == null)
            {
                sid = 0;
            }
            parmaMap.put("s_id", sid);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> menuList = getRoleMenuList(parmaMap, true);

            resultMap.put("menu", menuList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取权限菜单---路由结构-只获取一级 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAsyncRoutesByRoutes");
        }
        return resultMap;
    }

    /**
     * 获取路由
     *
     * @param parmaMap     -
     * @param isTop        -  是否只加载顶级
     * @param limitMenuIds -  商城拥有的权限，用于做基准(例:商城没有的权限，子管理员一定没有)
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/5 20:00
     */
    private List<Map<String, Object>> getRoleMenuList(Map<String, Object> parmaMap, boolean isTop, List<Integer> limitMenuIds) throws LaiKeAPIException
    {
        try
        {
            //需要剔除的id
            List<Integer> delMenuIdList = new ArrayList<>();
            if (limitMenuIds == null)
            {
                limitMenuIds = new ArrayList<>();
            }
            //获取1级菜单 select a.* FROM lkt_core_menu as a LEFT JOIN lkt_guide_menu as b ON a.id = b.menu_id LEFT JOIN lkt_role as c ON b.role_id = c.id WHERE a.recycle = 0 and a.s_id = 0 and c.id = 186 and level in( 1) order by a.sort DESC;
            List<Map<String, Object>> menuList = coreMenuModelMapper.getRoleMenuInfos(parmaMap);
            if (menuList != null)
            {
                for (int i = 0; i < menuList.size(); i++)
                {
                    Map<String, Object> menuMap = menuList.get(i);
                    parmaMap.remove("id");
                    int id = MapUtils.getIntValue(menuMap, "id");
                    //当前菜单级别
                    int menuLevel = MapUtils.getIntValue(menuMap, "level");
                    //获取菜单下tab页面数量
                    List<Integer> menuTabList = coreMenuModelMapper.getCoreMenuTabIdBySid(id);
                    //剔除菜单
                    if (limitMenuIds.size() > 0 && !limitMenuIds.contains(id))
                    {
                        //如果一级菜单都没有则跳出循环
                        delMenuIdList.add(id);
                        menuList.remove(i);
                        i--;
                        continue;
                    }
                    //默认图片
                    String image  = publiceService.getImgPath(MapUtils.getString(menuMap, "image"), 0);
                    String image1 = publiceService.getImgPath(MapUtils.getString(menuMap, "image1"), 0);
                    menuMap.put("image", image);
                    menuMap.put("image1", image1);

                    //是否有下级菜单标识
                    boolean isChildren = false;
                    //获取当前级别子菜单
                    parmaMap.put("levelList", new Integer[]{menuLevel + 1});
                    parmaMap.put("s_id", id);
                    List<Map<String, Object>> childrenList = new ArrayList<>();
                    if (!isTop)
                    {
                        //递归调用，查询子菜单，一般最多为3级，3级一般就是右边的整块页面及其上面的tab和按钮
                        childrenList = getRoleMenuList(parmaMap, limitMenuIds);
                    }
                    if (childrenList != null && childrenList.size() > 0)
                    {
                        //只有有二级菜单需要路由配置
                        if (menuLevel < 3)
                        {
                            isChildren = true;
                        }
                        if (menuTabList != null && menuTabList.size() > 0)
                        {
                            //是否存在tab页面权限
                            boolean haveTab = false;
                            for (Map<String, Object> coreMenu : childrenList)
                            {
                                //计算子级有权限的tab页面
                                // 【注！！！：当点击一级菜单对应的二级菜单第一个菜单没有被选中说明 平台 - 权限管理 - 菜单管理 中的 这个二级菜单下面的三级菜单的“是否为tab页面”属性没有选中 是 】
                                if (MapUtils.getInteger(coreMenu, "is_tab").equals(DictionaryConst.WhetherMaven.WHETHER_OK))
                                {
                                    String currentUrl = MapUtils.getString(menuMap, "url");
                                    if (StringUtils.isEmpty(currentUrl))
                                    {
                                        menuMap.put("url", MapUtils.getString(coreMenu, "url"));
                                        menuMap.put("action", MapUtils.getString(coreMenu, "action"));
                                    }
                                    haveTab = true;
                                    break;
                                }
                            }
                            //该菜单下级含有tab页面,但是没有tab页面权限，不显示该菜单
                            if (!haveTab)
                            {
                                menuList.remove(i);
                                i--;
                                continue;
                            }
                        }
                    }
                    menuMap.put("children", childrenList);
                    menuMap.put("isChildren", isChildren);
                }
//            publicRoleService.delMenuId(delMenuIdList, 1);
            }
            return menuList;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取当前角色拥有的菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleMenuList");
        }
    }


    /**
     * 获取路由
     *
     * @param parmaMap -
     * @param isTop    -  是否只加载顶级
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/5 20:00
     */
    private List<Map<String, Object>> getRoleMenuList(Map<String, Object> parmaMap, boolean isTop) throws LaiKeAPIException
    {
        try
        {
            return getRoleMenuList(parmaMap, isTop, null);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取当前角色拥有的菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getRoleMenuList");
        }
    }

    @Override
    public List<Map<String, Object>> getButton(MainVo vo, BigInteger menuId) throws LaiKeAPIException
    {
        List<Map<String, Object>> mapList = new ArrayList<>();
        try
        {
            AdminModel user = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            //顶级管理员，使用type=1且是默认商城的权限
            if (user.getType() != null && user.getType() == 0)
            {
                Integer defaultStoreRole = customerModelMapper.getDefaultStoreRole();
                user.setRole(String.valueOf(defaultStoreRole));
            }
            List<Map<String, Object>> buttonList = coreMenuModelMapper.getButton(Integer.valueOf(user.getRole()), menuId);
            buttonList.stream().forEach(button ->
            {
                Map<String, Object> map = new HashMap<>(16);
                map.put("id", MapUtils.getIntValue(button, "id"));
                map.put("title", MapUtils.getString(button, "title"));
                //默认图片
                String image  = publiceService.getImgPath(MapUtils.getString(button, "image"), 0);
                String image1 = publiceService.getImgPath(MapUtils.getString(button, "image1"), 0);
                map.put("image", image);
                map.put("image1", image1);
                mapList.add(map);
            });
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取角色权限按钮异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAsyncRoutesByRoutes");
        }
        return mapList;
    }


    @Override
    public Map<String, Object> getTab(MainVo vo, Integer menuId) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            AdminModel                user       = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            List<Map<String, Object>> buttonList = coreMenuModelMapper.getTab(Integer.valueOf(user.getRole()), menuId);
            resultMap.put("total", buttonList.size());
            resultMap.put("list", buttonList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取角色菜单tab页面权限", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAsyncRoutesByRoutes");
        }
        return resultMap;
    }

    private List<Map<String, Object>> getRoleMenuList(Map<String, Object> parmaMap, List<Integer> limitMenuIds)
    {
        try
        {
            return getRoleMenuList(parmaMap, false, limitMenuIds);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取当前角色拥有的菜单 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getAsyncRoutesByRoutes");
        }
    }

    @Autowired
    private PublicRoleService publicRoleService;

    @Autowired
    private CoreMenuModelMapper coreMenuModelMapper;

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private RoleMenuModelMapper roleMenuModelMapper;

    @Autowired
    private GuideMenuModelMapper guideMenuModelMapper;

    @Autowired
    private CustomerModelMapper customerModelMapper;

    @Autowired
    private PublicAdminService publicAdminService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RoleModelMapper roleModelMapper;
}
