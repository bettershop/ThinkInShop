package com.laiketui.common.service.dubbo;

import com.laiketui.common.api.PublicDictionaryService;
import com.laiketui.common.api.PubliceService;
import com.laiketui.common.mapper.DictionaryListModelMapper;
import com.laiketui.common.mapper.DictionaryNameModelMapper;
import com.laiketui.common.mapper.ProductClassModelMapper;
import com.laiketui.common.mapper.SupplierModelMapper;
import com.laiketui.common.utils.tool.cache.RedisDataTool;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.dictionary.DictionaryListModel;
import com.laiketui.domain.dictionary.DictionaryNameModel;
import com.laiketui.domain.log.AdminRecordModel;
import com.laiketui.domain.mch.AdminModel;
import com.laiketui.domain.product.ProductClassModel;
import com.laiketui.domain.supplier.SupplierModel;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.dic.DicVo;
import com.laiketui.domain.vo.systems.AddDictionaryDetailVo;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典操作
 *
 * @author Trick
 * @date 2020/12/28 14:55
 */
@Service
public class PublicDictionaryServiceImpl implements PublicDictionaryService
{
    private final Logger logger = LoggerFactory.getLogger(PublicDictionaryServiceImpl.class);

    @Override
    public Map<String, Object> getDictionaryByName(String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap;
        try
        {
            DicVo dicVo = new DicVo();
            dicVo.setName(name);
            resultMap = getDictionaryByName2(dicVo);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryByName");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getDictionaryByName(DicVo dicVo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
            dictionaryNameModel.setName(dicVo.getName());
            if (dicVo.getId() != null)
            {
                //根据id查找字典
                dictionaryNameModel.setId(dicVo.getId());
            }
            if (!dicVo.isShowFather())
            {
                dictionaryNameModel.setStatus(DictionaryNameModel.STATUS_OPEN);
                dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            }
            dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
            resultMap.put("name", dictionaryNameModel);
            if (dictionaryNameModel != null)
            {
                //获取明细
                DictionaryListModel dictionaryListModel = new DictionaryListModel();
                dictionaryListModel.setSid(dictionaryNameModel.getId());
                if (!dicVo.isShowChild())
                {
                    dictionaryListModel.setStatus(DictionaryNameModel.STATUS_OPEN);
                    dictionaryListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                }
                if (dicVo.getSid() != null)
                {
                    //根据id查找明细
                    dictionaryListModel.setId(dicVo.getSid());
                }
                if (dicVo.getValue() != null)
                {
                    dictionaryListModel.setValue(dicVo.getValue());
                }
                List<DictionaryListModel> dictionaryListModelList = dictionaryListModelMapper.select(dictionaryListModel);
                resultMap.put("value", dictionaryListModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryByName");
        }
        return resultMap;
    }


    @Override
    public Map<String, Object> getDictionaryByName2(DicVo dicVo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
            dictionaryNameModel.setName(dicVo.getName());
            if (dicVo.getId() != null)
            {
                //根据id查找字典
                dictionaryNameModel.setId(dicVo.getId());
            }
            if (!dicVo.isShowFather())
            {
                dictionaryNameModel.setStatus(DictionaryNameModel.STATUS_OPEN);
                dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            }
            dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
            resultMap.put("name", dictionaryNameModel);
            if (dictionaryNameModel != null)
            {
                //获取明细
                DictionaryListModel dictionaryListModel = new DictionaryListModel();
                dictionaryListModel.setSid(dictionaryNameModel.getId());
                if (!dicVo.isShowChild())
                {
                    dictionaryListModel.setStatus(DictionaryNameModel.STATUS_OPEN);
                    dictionaryListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                }
                if (dicVo.getSid() != null)
                {
                    //根据id查找明细
                    dictionaryListModel.setId(dicVo.getSid());
                }
                if (dicVo.getValue() != null)
                {
                    dictionaryListModel.setValue(dicVo.getValue());
                }
                List<DictionaryListModel> dictionaryListModelList = dictionaryListModelMapper.select(dictionaryListModel);

                dictionaryListModelList.forEach(model ->
                {
                    model.setText(model.getCtext());
                });

                resultMap.put("value", dictionaryListModelList);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryByName");
        }
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getDictionaryById(String name, String superName) throws LaiKeAPIException
    {
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("name", name);
            parmaMap.put("sname", superName);

            return dictionaryNameModelMapper.selectDynamic(parmaMap);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryById");
        }
    }

    @Override
    public Map<String, Object> getDictionaryCatalogList() throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
            dictionaryNameModel.setStatus(DictionaryNameModel.STATUS_OPEN);
            dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            List<DictionaryNameModel> dictionaryNameModelList = dictionaryNameModelMapper.select(dictionaryNameModel);

            resultMap.put("data", dictionaryNameModelList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取目录下拉 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryCatalogList");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getDictionaryInfo(MainVo vo, Integer id, String dicNo, String key, String value, Integer status) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", id);
            parmaMap.put("code_like", dicNo);
            parmaMap.put("name_like", key);
            parmaMap.put("text_like", value);
            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());

            //这个是功能界面选择语种作为查询条件
            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                parmaMap.put("lang_code", langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
            String language = vo.getLanguage();
            if (StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
            {
//                logger.info("默认语种:{}", language);
                parmaMap.put("lang_code", language);
            }

            if (status != null)
            {
                parmaMap.put("status", status.toString());
            }
            int                       total    = dictionaryNameModelMapper.countDynamicInfo(parmaMap);
            List<Map<String, Object>> dataList = new ArrayList<>();

            if (total > 0)
            {
                dataList = dictionaryNameModelMapper.selectDynamicInfo(parmaMap);
                if ("日志操作账号".equals(key))
                {
                    dataList = dataList.stream().filter(x -> !"超级管理员".equals(DataUtils.getStringVal(x, "text"))).collect(Collectors.toList());
                }
                dataList.forEach(model ->
                {
                    model.put("lang_name", publiceService.getLangName(MapUtils.getString(model, "lang_code")));
                    model.put("country_name", publiceService.getCountryName(MapUtils.getInteger(model, "country_num")));
                });
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
            logger.error("获取字典明细数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getDictionaryCatalogInfo(MainVo vo, Integer id, String name) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> parmaMap = new HashMap<>(16);
            parmaMap.put("id", id);
            parmaMap.put("dicName", name);

            parmaMap.put("pageStart", vo.getPageNo());
            parmaMap.put("pageEnd", vo.getPageSize());
            parmaMap.put("add_date_sort", DataUtils.Sort.DESC.toString());

            int                       total    = dictionaryNameModelMapper.countDynamic1(parmaMap);
            List<Map<String, Object>> dataList = dictionaryNameModelMapper.selectDynamic1(parmaMap);

            resultMap.put("total", total);
            resultMap.put("list", dataList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典目录数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryInfo");
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getDictionaryCode(int id) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        String              code      = "%s_%03d";
        try
        {
            int                 count;
            DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
            dictionaryNameModel.setId(id);
            dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
            dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
            if (dictionaryNameModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDBCZ, "字典不存在");
            }
            //如果是短信模板类别则特殊处理 为短信模板设置提供数据 2021-10-22 14:36:10
            //应该是类别，原代码写错了
            if ("短信模板类别".equals(dictionaryNameModel.getName()))
            {
                DictionaryListModel dictionaryListSms = new DictionaryListModel();
                dictionaryListSms.setStatus(DictionaryNameModel.STATUS_OPEN);
                dictionaryListSms.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryListSms.setSid(16);
                List<DictionaryListModel> listSms = dictionaryListModelMapper.select(dictionaryListSms);
                resultMap.put("list", listSms);
            }

            //获取当前字典数量
            DictionaryListModel dictionaryListModel = new DictionaryListModel();
            dictionaryListModel.setSid(id);
            count = dictionaryListModelMapper.selectCount(dictionaryListModel) + 1;
            code = String.format(code, dictionaryNameModel.getDic_code(), count);

            resultMap.put("code", code);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryInfo");
        }
        return resultMap;
    }

    private String dicNameCodeBuilder(String codePinYin) throws LaiKeAPIException
    {
        String code = "";
        try
        {
            DictionaryNameModel dictionaryNameCount = new DictionaryNameModel();
            int                 count;
            String              key                 = codePinYin.toUpperCase();
            int                 num                 = 0;
            do
            {
                code = String.format("LKT_%s", key);
                dictionaryNameCount.setDic_code(code);
                count = dictionaryNameModelMapper.selectCount(dictionaryNameCount);
                if (count > 0)
                {
                    //重名规则 +1
                    num++;
                    key = codePinYin.toUpperCase() + num;
                }
            }
            while (count > 0);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("获取字典数据 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryInfo");
        }
        return code;
    }

    @Override
    public boolean addDictionaryInfo(Integer id, String name, int isOpen, String token) throws LaiKeAPIException
    {
        try
        {
            int                 count                  = 0;
            AdminModel          adminModel             = RedisDataTool.getRedisAdminUserCache(token, redisUtil);
            DictionaryNameModel dictionaryNameModelOld = null;
            if (id != null)
            {
                dictionaryNameModelOld = new DictionaryNameModel();
                dictionaryNameModelOld.setId(id);
                dictionaryNameModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryNameModelOld = dictionaryNameModelMapper.selectOne(dictionaryNameModelOld);
                if (dictionaryNameModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDBCZ, "字典不存在");
                }
                if (!DictionaryNameModel.STATUS_CLOSE.equals(dictionaryNameModelOld.getStatus()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SXZTJZXG, "生效状态禁止修改");
                }
            }
            if (StringUtils.isEmpty(name))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMCBNWK, "字典名称不能为空");
            }

            if (dictionaryNameModelOld == null || !dictionaryNameModelOld.getName().equals(name))
            {
                DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
                dictionaryNameModel.setName(StringUtils.trim(name));
                dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                count = dictionaryNameModelMapper.selectCount(dictionaryNameModel);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJMCCZ, "数据名称存在");
                }
            }

            DictionaryNameModel dictionaryNameModelSave = new DictionaryNameModel();
            dictionaryNameModelSave.setName(name);
            dictionaryNameModelSave.setStatus(isOpen);
            if (dictionaryNameModelOld != null)
            {
                dictionaryNameModelSave.setId(dictionaryNameModelOld.getId());
                count = dictionaryNameModelMapper.updateByPrimaryKeySelective(dictionaryNameModelSave);
                //添加操作日志
                publiceService.addAdminRecord(adminModel.getStore_id(), "修改了数据名称：" + name + "的信息", AdminRecordModel.Type.UPDATE, token);
            }
            else
            {
                dictionaryNameModelSave.setDic_code(dicNameCodeBuilder(PinyinUtils.getPinYinHeadChar(name)).toUpperCase());
                dictionaryNameModelSave.setAdmin_name(adminModel.getName());
                dictionaryNameModelSave.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryNameModelSave.setAdd_date(new Date());
                count = dictionaryNameModelMapper.insertSelective(dictionaryNameModelSave);
                //添加操作日志
                publiceService.addAdminRecord(adminModel.getStore_id(), "添加了数据名称：" + name, AdminRecordModel.Type.ADD, token);
            }
            return count > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加/修改字典表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addDictionaryInfo");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDictionaryDetailInfo(AddDictionaryDetailVo vo) throws LaiKeAPIException
    {
        try
        {
            int        count;
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            if (StringUtils.isEmpty(vo.getValueName()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDZBNWK, "字典值不能为空");
            }
            if (StringUtils.isEmpty(vo.getValueCode()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDZDMBNWK, "字典值代码不能为空");
            }
            DictionaryListModel dictionaryListModelOld = null;
            if (vo.getId() != null)
            {
                dictionaryListModelOld = new DictionaryListModel();
                dictionaryListModelOld.setId(vo.getId());
                dictionaryListModelOld.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryListModelOld = dictionaryListModelMapper.selectOne(dictionaryListModelOld);
                if (dictionaryListModelOld == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDZBCZ, "字典值不存在");
                }
            }
            if (dictionaryListModelOld == null)
            {
                //数据验证
                DictionaryListModel dictionaryListModel = new DictionaryListModel();
                dictionaryListModel.setCode(vo.getDataCode());
                dictionaryListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                count = dictionaryListModelMapper.selectCount(dictionaryListModel);

                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDBMYCZ, "字典编码已存在");
                }
            }
            DictionaryNameModel dictionaryListFather = dictionaryNameModelMapper.selectByPrimaryKey(vo.getSid());
            if (dictionaryListFather == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMLBCZ, "字典目录不存在");
            }

            DictionaryListModel dictionaryListModelSave = new DictionaryListModel();
            dictionaryListModelSave.setCtext(vo.getValueCode());
            dictionaryListModelSave.setStatus(vo.getIsOpen());

            String langCode = vo.getLang_code();
            if (StringUtils.isNotEmpty(langCode))
            {
                dictionaryListModelSave.setLang_code(langCode);
            }

            //系统的整体默认或者用户右上角所选的语种，这种场景只适用于第一次进入功能界面的查询
//            String language = vo.getLanguage();
//            if(StringUtils.isEmpty(langCode) && StringUtils.isNotEmpty(language))
//            {
//                logger.info("默认语种:{}",language);
//                dictionaryListModelSave.setLang_code(language);
//            }

            Integer countryNum = vo.getCountry_num();
            if (countryNum != null)
            {
                dictionaryListModelSave.setCountry_num(countryNum);
            }

            //值和code不能相同
            int countNum = 0;
            count = dictionaryListModelMapper.countDicListName(vo.getSid(), vo.getValueCode());
            //如果名称是本身则通过名称校验
            if (dictionaryListModelOld != null)
            {
                if (dictionaryListModelOld.getCtext().equals(vo.getValueCode()))
                {
                    countNum++;
                }
            }
            if (count > countNum)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDZYCZ, "字典值已存在");
            }
            if (dictionaryListModelOld != null)
            {
                dictionaryListModelSave.setId(dictionaryListModelOld.getId());
                count = dictionaryListModelMapper.updateByPrimaryKeySelective(dictionaryListModelSave);

                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "修改了数据字典：" + dictionaryListModelSave.getCtext() + "的信息", AdminRecordModel.Type.UPDATE, vo.getAccessId());
            }
            else
            {
                if (StringUtils.isEmpty(vo.getDataCode()))
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDBMBNWK, "字典编码不能为空");
                }
                count = dictionaryListModelMapper.countDicListCode(vo.getSid(), vo.getValueName(), vo.getLang_code());
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDYCZ, "字典code已存在");
                }
                dictionaryListModelSave.setS_name("");
                //如果是 短信模板类别 则特殊处理 给短信配置提供数据源 2021-10-22 14:52:26 update:2022-10-17 15:42:17
                if ("短信模板类别".equals(dictionaryListFather.getName()))
                {
                    logger.debug("短信模板类别 添加数值");
                    DictionaryListModel dictionaryListAttr = dictionaryListModelMapper.selectByPrimaryKey(vo.getAttrId());
                    if (dictionaryListAttr != null)
                    {
                        dictionaryListModelSave.setS_name(dictionaryListAttr.getCtext());
                    }
                }
                dictionaryListModelSave.setValue(vo.getValueName());
                dictionaryListModelSave.setSid(vo.getSid());
                dictionaryListModelSave.setCode(vo.getDataCode());
                dictionaryListModelSave.setAdmin_name(adminModel.getName());
                dictionaryListModelSave.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryListModelSave.setAdd_date(new Date());
                count = dictionaryListModelMapper.insertSelective(dictionaryListModelSave);
                //添加操作日志

                publiceService.addAdminRecord(vo.getStoreId(), "添加了数据字典：" + dictionaryListModelSave.getCtext(), AdminRecordModel.Type.ADD, vo.getAccessId());
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
            logger.error("添加/修改字典表 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addDictionaryInfo");
        }
    }

    @Override
    public boolean switchDictionaryDetail(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel          adminModel          = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DictionaryListModel dictionaryListModel = dictionaryListModelMapper.selectByPrimaryKey(id);
            if (dictionaryListModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMXBCZ, "字典明细不存在");
            }
            DictionaryListModel dictionaryListUpdate = new DictionaryListModel();
            dictionaryListUpdate.setId(dictionaryListModel.getId());
            int isOpen = DictionaryConst.WhetherMaven.WHETHER_NO;
            if (dictionaryListModel.getStatus() == DictionaryConst.WhetherMaven.WHETHER_NO)
            {
                isOpen = DictionaryConst.WhetherMaven.WHETHER_OK;
            }
            dictionaryListUpdate.setStatus(isOpen);
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将数据字典：" + dictionaryListModel.getCtext() + "进行了是否生效操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
            return dictionaryListModelMapper.updateByPrimaryKeySelective(dictionaryListUpdate) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("字典明细开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "switchDictionaryDetail");
        }
    }

    @Override
    public boolean switchDictionary(MainVo vo, int id) throws LaiKeAPIException
    {
        try
        {
            AdminModel          adminModel          = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            DictionaryNameModel dictionaryNameModel = dictionaryNameModelMapper.selectByPrimaryKey(id);
            if (dictionaryNameModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMXBCZ, "字典明细不存在");
            }
            DictionaryNameModel dictionaryNameUpdate = new DictionaryNameModel();
            dictionaryNameUpdate.setId(dictionaryNameModel.getId());
            int isOpen = DictionaryConst.WhetherMaven.WHETHER_NO;
            if (dictionaryNameModel.getStatus() == DictionaryConst.WhetherMaven.WHETHER_NO)
            {
                isOpen = DictionaryConst.WhetherMaven.WHETHER_OK;
            }
            dictionaryNameUpdate.setStatus(isOpen);
            //添加操作日志
            publiceService.addAdminRecord(vo.getStoreId(), "将数据名称：" + dictionaryNameModel.getName() + "进行了是否生效操作", AdminRecordModel.Type.OPEN_OR_CLOSE, vo.getAccessId());
            return dictionaryNameModelMapper.updateByPrimaryKeySelective(dictionaryNameUpdate) > 0;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("字典开关 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "switchDictionaryDetail");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delDictionary(MainVo vo, List<Integer> idList) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            int        count;
            for (Integer id : idList)
            {
                DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
                dictionaryNameModel.setId(id);
                dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
                if (dictionaryNameModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMLBCZ, "字典目录不存在");
                }
                if (dictionaryNameModel.getStatus() == DictionaryConst.WhetherMaven.WHETHER_OK)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSXZWFSC, "字典生效中无法删除");
                }
                if (dictionaryNameModel.getIs_core() == DictionaryConst.WhetherMaven.WHETHER_OK)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_XTZDWFSC, "系统字典无法删除");
                }
                DictionaryListModel dictionaryListCount = new DictionaryListModel();
                dictionaryListCount.setSid(id);
                dictionaryListCount.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                count = dictionaryListModelMapper.selectCount(dictionaryListCount);
                if (count > 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GSJMCXYGLSJZWFSC, "该数据名称下有关联数据值，无法删除!");
                }

                //删除字典
                DictionaryNameModel dictionaryNameModelUpdate = new DictionaryNameModel();
                dictionaryNameModelUpdate.setId(dictionaryNameModel.getId());
                dictionaryNameModelUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                count = dictionaryNameModelMapper.updateByPrimaryKeySelective(dictionaryNameModelUpdate);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSCSB, "字典删除失败");
                }
                //添加操作日志
                publiceService.addAdminRecord(vo.getStoreId(), "删除了数据名称：" + dictionaryNameModel.getName(), AdminRecordModel.Type.DEL, vo.getAccessId());
            }
            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除字典目录 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDictionary");
        }
    }

    @Autowired
    private SupplierModelMapper supplierModelMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean delDictionaryDetailInfo(MainVo vo, List<Integer> idList) throws LaiKeAPIException
    {
        try
        {
            AdminModel adminModel = RedisDataTool.getRedisAdminUserCache(vo.getAccessId(), redisUtil);
            for (Integer id : idList)
            {
                DictionaryListModel dictionaryListModel = new DictionaryListModel();
                dictionaryListModel.setId(id);
                dictionaryListModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryListModel = dictionaryListModelMapper.selectOne(dictionaryListModel);
                if (dictionaryListModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMXTMBCZ, "字典明细条目不存在");
                }
                if (dictionaryListModel.getStatus() == DictionaryConst.WhetherMaven.WHETHER_OK)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDSXZSCSB, "字典生效中,删除失败");
                }
                DictionaryListModel dictionaryListModelUpdate = new DictionaryListModel();
                dictionaryListModelUpdate.setId(dictionaryListModel.getId());
                dictionaryListModelUpdate.setRecycle(DictionaryConst.ProductRecycle.RECOVERY);
                int count = dictionaryListModelMapper.updateByPrimaryKeySelective(dictionaryListModelUpdate);
                if (count < 1)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                }
                DictionaryNameModel dictionaryNameModel = new DictionaryNameModel();
                dictionaryNameModel.setId(dictionaryListModel.getSid());
                dictionaryNameModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                dictionaryNameModel = dictionaryNameModelMapper.selectOne(dictionaryNameModel);
                if (dictionaryNameModel == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_ZDMLBCZ, "字典目录不存在");
                }
                //特殊字典值校验
                if ("商品分类".equals(dictionaryNameModel.getName()))
                {
                    //查询当前级别正是否在使用
                    ProductClassModel productClassModel = new ProductClassModel();
                    productClassModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    productClassModel.setLevel(Integer.parseInt(dictionaryListModel.getValue()));
                    count = productClassModelMapper.selectCount(productClassModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GCJBZZSYWFSC, "该层级别正在使用,无法删除");
                    }
                }
                //特殊字典值校验
                if ("供应商类型".equals(dictionaryNameModel.getName()))
                {
                    //查询当前级别正是否在使用
                    SupplierModel supplierModel = new SupplierModel();
                    supplierModel.setRecovery(DictionaryConst.ProductRecycle.NOT_STATUS);
                    supplierModel.setDic_id(dictionaryListModel.getId());
                    count = supplierModelMapper.selectCount(supplierModel);
                    if (count > 0)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_GCJBZZSYWFSC, "该字典正在使用,无法删除");
                    }
                }
                //删除特殊字典值
                if ("属性名".equals(dictionaryNameModel.getName()))
                {
                    //删除下级
                    count = dictionaryNameModelMapper.delLower(dictionaryNameModel.getName());
                    if (count < 1)
                    {
                        throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLGZ, "网络故障");
                    }
                }
                //添加操作日志

                publiceService.addAdminRecord(vo.getStoreId(), "删除了数据字典：" + dictionaryListModel.getCtext(), AdminRecordModel.Type.DEL, vo.getAccessId());
            }

            return true;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除字典明细 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delDictionaryDetailInfo");
        }
    }

    /**
     * 根据字典名称获取字典列表
     *
     * @param map 参数Map，应包含：
     *            - name: 字典名称（如"积分类型"）
     *            - status: 状态（可选）
     *            - lang_code: 语言代码（可选）
     * @return 字典列表
     * @throws LaiKeAPIException 业务异常
     */
    @Override
    public List<Map<String, Object>> getDictionaryByName(Map<String, Object> map) throws LaiKeAPIException
    {
        try
        {
            // 调用Mapper查询
            List<Map<String, Object>> result = dictionaryListModelMapper.getDictionaryByName(map);

            return result;
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询字典列表异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "getDictionaryByName");
        }
    }

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DictionaryNameModelMapper dictionaryNameModelMapper;

    @Autowired
    private DictionaryListModelMapper dictionaryListModelMapper;

    @Autowired
    private ProductClassModelMapper productClassModelMapper;

    @Autowired
    private PubliceService publiceService;

    @Autowired
    private PublicDictionaryService publicDictionaryService;
}

