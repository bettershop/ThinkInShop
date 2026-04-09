package com.laiketui.admins.mch.services.authority;

import cn.hutool.core.map.MapUtil;
import com.laiketui.admins.api.mch.authority.MchMenuService;
import com.laiketui.common.api.PublicSortService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.AuthorityMappingModelMapper;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.common.mapper.MchModelMapper;
import com.laiketui.common.mapper.MenuModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.authority.AuthorityMappingModel;
import com.laiketui.domain.authority.MenuModel;
import com.laiketui.domain.authority.UserAuthorityModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.user.User;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.admin.menu.AddMenuMainVo;
import com.laiketui.root.common.BuilderIDTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 权限菜单实现
 *
 * @author Trick
 * @date 2021/12/19 10:30
 */
@Service("menuServiceImpl")
public class MchMenuServiceImpl implements MchMenuService, PublicSortService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


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

    @Override
    public Map<String, Object> getMenuList(MainVo vo, String name, String id, String sid) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            User                user     = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            Map<String, Object> paramMap = new HashMap<>(16);
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
            int mchId = user.getMchId();
            //获取当前商城自营店
            Integer zyMchId = customerModelMapper.getStoreMchId(user.getStore_id());
            //如果自营店是自己则获取系统菜单+自定义菜单
            if (zyMchId != null && mchId == zyMchId)
            {
                Integer[] mchIdList = {mchId, 0};
                paramMap.put("main_ids", mchIdList);
            }
            else
            {
                //如果是店主则获取所有系统菜单+自定义菜单,如果是管理员则获取管理员自己的菜单
                if (mchModelMapper.countMchIsByUser(vo.getStoreId(), user.getUser_id()) > 0)
                {
                    Integer[] mchIdList = {mchId, 0};
                    paramMap.put("main_ids", mchIdList);
                }
                else
                {
                    //普通管理员
                    paramMap.put("user_id", user.getUser_id());
                    paramMap.put("authority_type", UserAuthorityModel.USER_ID);
                }
            }
            if (!paramMap.containsKey("authority_type"))
            {
                paramMap.put("type", GloabConst.MenuType.MENU_MCH_ID);
            }
            paramMap.put("sort_sort", DataUtils.Sort.DESC.toString());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            paramMap.put("lang_code", vo.getLang_code());

            int                       total    = menuModelMapper.countMenuList(paramMap);
            List<Map<String, Object>> dataList = new ArrayList<>();
            if (total > 0)
            {
                dataList = menuModelMapper.getMenuList(paramMap);
                for (Map<String, Object> map : dataList)
                {
                    map.put("lang_name", publiceService.getLangName(MapUtil.getStr(map, "lang_code")));
                }
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
            User      user    = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            int       row;
            int       count;
            MenuModel menuOld = null;
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
            //只有商城自营店才可以添加菜单
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (!user.getMchId().equals(storeMchId))
            {
                logger.debug("当前商城自营店{},只有自营店账号才可以添加菜单", storeMchId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_FZYWFTJCDQLXGLY, "非自营店不能添加菜单，请联系管理员");
            }

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
            menuSave.setLang_code(vo.getLang_code());
            menuSave.setCountry_num(vo.getCountry_num());
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
            menuCount.setMain_id(user.getMchId() + "");
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

                String menuId = BuilderIDTool.getSnowflakeId() + "";

                menuSave.setSort(maxSort);
                menuSave.setId(menuId);
                menuSave.setType(GloabConst.MenuType.MENU_MCH_ID);
                menuSave.setMain_id("0");
                menuSave.setAdd_date(new Date());
                row = menuModelMapper.insertSelective(menuSave);
                //增加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "添加了菜单ID：" + menuId, AdminRecordModel.Type.ADD, vo.getAccessId());
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
                publiceService.addAdminRecord(vo.getStoreId(), "修改了菜单ID：" + vo.getId(), AdminRecordModel.Type.UPDATE, vo.getAccessId());
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
            int  row  = 0;
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            MenuModel menuOld = menuModelMapper.selectByPrimaryKey(id);
            if (menuOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
            //只有商城自营店才可以添加菜单
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (!user.getMchId().equals(storeMchId))
            {
                logger.debug("当前商城自营店{},只有自营店账号才可以增/删菜单,普通账号只删除自己的对应权限", storeMchId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
            }
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
            publiceService.addAdminRecord(vo.getStoreId(), "删除了菜单ID：" + id, AdminRecordModel.Type.DEL, vo.getAccessId());
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
    @Transactional(rollbackFor = Exception.class)
    public void sortMove(MainVo vo, String moveId, String moveId1) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            //只有商城自营店才可以添加菜单
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (!user.getMchId().equals(storeMchId))
            {
                logger.debug("当前商城自营店{},只有自营店账号才可以添加菜单", storeMchId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
            }
            int row = menuModelMapper.move(moveId, moveId1);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            // 查询菜单名称
            MenuModel firstMenuModel  = menuModelMapper.selectByPrimaryKey(moveId);
            MenuModel secondMenuModel = menuModelMapper.selectByPrimaryKey(moveId1);
            //增加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "上移/下移了菜单ID：" + moveId + "," + moveId1, AdminRecordModel.Type.UPDATE, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("菜单上下移动 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "sortMove");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void top(MainVo vo, String id) throws LaiKeAPIException
    {
        try
        {
            User user = RedisDataTool.getRedisUserCache(vo.getAccessId(), redisUtil, GloabConst.RedisHeaderKey.LOGIN_ACCESS_MCH_TOKEN, true);
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSCW, "参数错误");
            }
            //只有商城自营店才可以添加菜单
            Integer storeMchId = customerModelMapper.getStoreMchId(vo.getStoreId());
            if (!user.getMchId().equals(storeMchId))
            {
                logger.debug("当前商城自营店{},只有自营店账号才可以添加菜单", storeMchId);
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXBZ, "权限不足");
            }
            MenuModel menuOld = menuModelMapper.selectByPrimaryKey(id);
            if (menuOld == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "菜单不存在");
            }
/*            if("0".equals(menuOld.getMain_id())){
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CDBCZ, "默认菜单不允许操作");
            }*/
            int       maxSort    = menuModelMapper.maxSort(menuOld.getType(), menuOld.getLevel(), menuOld.getSid());
            MenuModel menuUpdate = new MenuModel();
            menuUpdate.setId(menuOld.getId());
            menuUpdate.setSort(maxSort + 1);
            menuUpdate.setUpdate_date(new Date());
            int row = menuModelMapper.updateByPrimaryKeySelective(menuUpdate);
            if (row < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CZSB, "操作失败");
            }
            publiceService.addAdminRecord(vo.getStoreId(), "置顶了菜单ID：" + id, AdminRecordModel.Type.ADD, vo.getAccessId());
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("菜单置顶 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "top");
        }
    }
}

