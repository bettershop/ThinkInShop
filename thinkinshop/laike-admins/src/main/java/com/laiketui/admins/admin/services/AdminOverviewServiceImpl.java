package com.laiketui.admins.admin.services;

import com.laiketui.admins.api.admin.AdminOverviewService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AdminModelMapper;
import com.laiketui.common.mapper.CoreMenuModelMapper;
import com.laiketui.common.mapper.GuideModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.role.CoreMenuModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.saas.OverviewVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 功能导览
 *
 * @author Trick
 * @date 2021/1/26 11:30
 */
@Service
public class AdminOverviewServiceImpl implements AdminOverviewService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AdminModelMapper adminModelMapper;

    @Autowired
    private CoreMenuModelMapper coreMenuModelMapper;

    @Autowired
    private GuideModelMapper guideModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> index(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            AdminModel                adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);

            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("is_core", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("s_id", 0);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("is_display", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap.put("roleId", adminModel.getRole());

            parmaMap.put("guide_sort", DataUtils.Sort.DESC.toString());
            List<Map<String, Object>> coreMenuList = coreMenuModelMapper.getFunctionOverview(parmaMap);
            for (Map<String, Object> map : coreMenuList)
            {
                Map<String, Object> mainMap = new HashMap<>(16);
                int                 id      = MapUtils.getIntValue(map, "id");
                String              title   = MapUtils.getString(map, "guide_name");
                int                 type    = MapUtils.getIntValue(map, "type");
                if (type == AdminModel.TYPE_SYSTEM_ADMIN)
                {
                    //平台不显示
                    continue;
                }
                //获取菜单下所有功能
                List<Map<String, Object>> subclassList = new ArrayList<>();
                findSubordinates(adminModel.getType() == AdminModel.TYPE_SYSTEM_ADMIN, vo.getStoreId(), id, subclassList, Integer.parseInt(adminModel.getRole()), null);
                mainMap.put("title", title);
                mainMap.put("list", subclassList);
                resultList.add(mainMap);
            }
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("功能导览页面数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "index");
        }
        return resultMap;
    }

    /**
     * 获取权限下所有功能
     *
     * @param isAdmin      -
     * @param storeId      -
     * @param sid          -
     * @param subclassList -
     * @param roleId       -
     * @param goToUrl      -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/6/15 15:11
     */
    private void findSubordinates(boolean isAdmin, int storeId, int sid, List<Map<String, Object>> subclassList, int roleId, String goToUrl) throws LaiKeAPIException
    {
        try
        {
            logger.debug("roleId={} sid={} 正在递归", roleId, sid);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("s_id", sid);
            parmaMap.put("is_core", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("guide_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);

            parmaMap.put("is_display", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap.put("roleId", roleId);
            List<Map<String, Object>> list = coreMenuModelMapper.getFunctionOverview(parmaMap);
            for (Map<String, Object> map : list)
            {
                int id    = MapUtils.getIntValue(map, "id");
                int level = MapUtils.getIntValue(map, "level");
                //跳转路径
                if (level == 2)
                {
                    goToUrl = MapUtils.getString(map, "url");
                }
                //只显示到二级
                if (level > 2)
                {
                    continue;
                }
                Map<String, Object> subclassMap = new HashMap<>(16);
                subclassMap.put("title", MapUtils.getString(map, "guide_name"));
                subclassMap.put("image", publiceService.getImgPath(MapUtils.getString(map, "image1"), 0));
                subclassMap.put("introduction", MapUtils.getString(map, "briefintroduction"));
                subclassMap.put("url", goToUrl);
                subclassMap.put("menuPath", MapUtils.getString(map, "action"));
                subclassList.add(subclassMap);
                findSubordinates(isAdmin, storeId, id, subclassList, roleId, goToUrl);
            }
        }
        catch (Exception e)
        {
            logger.error("获取权限下所有功能 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "findSubordinates");
        }
    }

    @Override
    public Map<String, Object> functionList(OverviewVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            AdminModel                adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object>       parmaMap   = new HashMap<>(16);
            parmaMap.put("is_core", DictionaryConst.WhetherMaven.WHETHER_OK);
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("roleId", adminModel.getRole());
            //如果是系统管理员则无视商城
            if (adminModel.getType() != AdminModel.TYPE_SYSTEM_ADMIN)
            {
                parmaMap.put("store_id_pt", vo.getStoreId());
            }
            parmaMap.put("guide_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            if (StringUtils.isNotEmpty(vo.getName()))
            {
                parmaMap.put("name", vo.getName());
            }
            parmaMap.put("id", vo.getId());
            if (!Objects.isNull(vo.getSid()))
            {
                parmaMap.put("s_id", vo.getSid());
            }
            else
            {
                parmaMap.put("s_id", 0);
            }
            int                       total        = coreMenuModelMapper.countFunctionOverview(parmaMap);
            List<Map<String, Object>> coreMenuList = new ArrayList<>();
            if (total > 0)
            {
                coreMenuList = coreMenuModelMapper.getFunctionOverview(parmaMap);
            }
            for (Map<String, Object> map : coreMenuList)
            {
                int    sid    = MapUtils.getIntValue(map, "s_id");
                String idStr  = MapUtils.getString(map, "id");
                String sidStr = MapUtils.getString(map, "s_id");
                int    type   = MapUtils.getIntValue(map, "type");
                if (type == AdminModel.TYPE_SYSTEM_ADMIN)
                {
                    //平台不显示
                    continue;
                }
                String py    = "";
                String title = MapUtils.getString(map, "title");
                map.put("title", title);
                if (sid > 0)
                {
                    //获取上级菜单信息
                    CoreMenuModel coreMenuModel = new CoreMenuModel();
                    coreMenuModel.setId(sid);
                    coreMenuModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    coreMenuModel = coreMenuModelMapper.selectOne(coreMenuModel);
                    if (coreMenuModel != null)
                    {
                        title = coreMenuModel.getTitle();
                        sidStr = coreMenuModel.getS_id().toString();
                    }
                }
                String format = "%s_%s";
                py = PinyinUtils.getPinYinHeadChar(title);
                idStr = String.format(format, py, idStr);
                sidStr = String.format(format, py, sidStr);
                map.put("id_id", idStr);
                map.put("sid", sidStr);
                map.put("image1", publiceService.getImgPath(MapUtils.getString(map, "image1"), 0));
                if (sid > 0)
                {
                    //获取菜单下所有功能
                    List<Map<String, Object>> subclassList = new ArrayList<>();
                    findSubordinates(adminModel.getType() == 0, vo.getStoreId(), sid, subclassList, Integer.parseInt(adminModel.getRole()), null);
                }
                resultList.add(map);
            }
            resultMap.put("list", resultList);
            resultMap.put("total", total);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("编辑商城导览列表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "functionList");
        }
        return resultMap;
    }

    @Override
    public void rsort(MainVo vo) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("is_core", DictionaryConst.WhetherMaven.WHETHER_NO);
            parmaMap.put("s_id", 0);
            parmaMap.put("store_id", vo.getStoreId());
            parmaMap.put("guide_sort", DataUtils.Sort.DESC.toString());

            List<Map<String, Object>> coreMenuList = coreMenuModelMapper.getFunctionOverview(parmaMap);
            int                       sort         = 1;
            //子类从一万开始排
            int sort_sun = 10000;
            for (Map<String, Object> map : coreMenuList)
            {
                int    id    = MapUtils.getIntValue(map, "id");
                String title = MapUtils.getString(map, "title");
                //重新排序
                guideModelMapper.test(sort, MapUtils.getIntValue(map, "guidId"));
                //获取菜单下所有功能
                parmaMap.put("s_id", id);
                List<Map<String, Object>> subclassList = coreMenuModelMapper.getFunctionOverview(parmaMap);
                for (Map<String, Object> sunMap : subclassList)
                {
                    String sunTitle = MapUtils.getString(sunMap, "title");
                    //重新排序
                    guideModelMapper.test(sort_sun, MapUtils.getIntValue(sunMap, "guidId"));
                    sort_sun++;
                }
                sort++;
            }

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("排序出现问题的时候使用 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "functionList");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sortTop(MainVo vo, int id, Integer sid) throws LaiKeAPIException
    {
        try
        {
            if (sid == null)
            {
                sid = 0;
            }
            AdminModel          adminUser = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int                 roleId    = adminModelMapper.getStoreRole(vo.getStoreId());
            Map<String, Object> parmaMap  = new HashMap<>(16);
            if (sid > 0)
            {
                parmaMap.put("s_id", sid);
            }
            else
            {
                parmaMap.put("s_id", 0);
            }
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            parmaMap.put("roleId", adminUser.getRole());
            parmaMap.put("guide_sort", DataUtils.Sort.DESC.toString());
            parmaMap.put("pageStart", 0);
            parmaMap.put("pageEnd", 1);
            List<Map<String, Object>> coreMenuList = coreMenuModelMapper.getFunctionOverview(parmaMap);
            int                       maxSort      = MapUtils.getIntValue(coreMenuList.get(0), "guide_sort") + 1;
            int                       row          = guideModelMapper.sortMove(vo.getStoreId(), Integer.parseInt(adminUser.getRole()), id, maxSort);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSB, "置顶失败");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "置顶了导览名称：" + MapUtils.getString(coreMenuList.get(0), "title"), AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sortTop");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void move(MainVo vo, int id, int id2) throws LaiKeAPIException
    {
        try
        {
            AdminModel    adminUser      = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            CoreMenuModel coreMenuModel  = coreMenuModelMapper.selectByPrimaryKey(id);
            CoreMenuModel coreMenuModel2 = coreMenuModelMapper.selectByPrimaryKey(id);
            int           roleId         = adminModelMapper.getStoreRole(vo.getStoreId());
            int           row            = guideModelMapper.moveSort(vo.getStoreId(), Integer.parseInt(adminUser.getRole()), id, id2);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDYC, "上移/下移异常");
            }
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将导览名称：" + coreMenuModel.getTitle() + "," + coreMenuModel2.getTitle() + "进行了上移、下移操作", AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "move");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void isDisplaySwitch(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel          user     = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", id);
            parmaMap.put("roleId", user.getRole());
            parmaMap.put("recycle", DictionaryConst.ProductRecycle.NOT_STATUS);
            List<Map<String, Object>> coreMenuList = coreMenuModelMapper.getFunctionOverview(parmaMap);
            if (coreMenuList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            for (Map<String, Object> map : coreMenuList)
            {
                //是否显示
                int isDisplay = 1;
                if (MapUtils.getIntValue(map, "is_display") == DictionaryConst.WhetherMaven.WHETHER_OK)
                {
                    isDisplay = 0;
                }
                int row = guideModelMapper.displaySwitch(Integer.parseInt(user.getRole()), id, isDisplay);
                if (row < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
                }
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "将导览名称：" + map.get("title") + " 进行了是否显示操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("是否显示开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "isDisplaySwitch");
        }
    }

    @Autowired
    private PubliceService publiceService;
}

