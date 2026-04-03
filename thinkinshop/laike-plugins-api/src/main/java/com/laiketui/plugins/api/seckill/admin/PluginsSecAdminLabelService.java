package com.laiketui.plugins.api.seckill.admin;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.sec.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 秒杀标签接口
 *
 * @author Trick
 * @date 2021/10/14 11:30
 */
public interface PluginsSecAdminLabelService
{

    /**
     * 获取标签列表
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:32
     */
    Map<String, Object> index(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 添加/编辑秒杀标签
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:59
     */
    void addLabel(AddLabelVo vo) throws LaiKeAPIException;

    /**
     * 删除商品标签
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 12:00
     */
    void delLabel(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 秒杀标签显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/14 11:33
     */
    void labelSwitch(MainVo vo, String id) throws LaiKeAPIException;


    /**
     * 获取商品信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 14:52
     */
    Map<String, Object> getProList(QueryProVo vo) throws LaiKeAPIException;

    /**
     * 获取商品规格列表
     *
     * @param vo      -
     * @param goodsId -
     * @param acId    - 商品活动id
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/11/16 11:28
     */
    Map<String, Object> getProAttrList(MainVo vo, Integer goodsId, Integer acId) throws LaiKeAPIException;

    /**
     * 获取秒杀标签商品
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/19 10:54
     */
    Map<String, Object> secLabelGoodsList(QuerySecProVo vo) throws LaiKeAPIException;


    /**
     * 添加/编辑秒杀商品
     *
     * @param vo -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/7 9:43
     */
    void addPro(AddProVo vo) throws LaiKeAPIException;

    /**
     * 增加库存
     *
     * @param vo         -
     * @param secGoodsId -
     * @param needNum    -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 12:57
     */
    void addProStock(MainVo vo, Integer secGoodsId, int needNum) throws LaiKeAPIException;

    /**
     * 删除秒杀商品
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/19 15:06
     */
    void delPro(MainVo vo, String id) throws LaiKeAPIException;

    /**
     * 标签商品显示开关
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/19 17:25
     */
    void labelGoodsSwitch(MainVo vo, Integer id) throws LaiKeAPIException;

    /**
     * 获取秒杀设置
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 9:59
     */
    Map<String, Object> getSecConfig(MainVo vo) throws LaiKeAPIException;

    /**
     * 配置秒杀设置
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 10:02
     */
    void setSecConfig(AddSecConfigVo vo) throws LaiKeAPIException;


    /**
     * 获取秒杀记录列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-10-20 14:46:43
     */
    Map<String, Object> getSecRecord(QuerySecRecordVo vo) throws LaiKeAPIException;

    /**
     * 删除秒杀记录
     *
     * @param vo  -
     * @param rid -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/10/20 14:46
     */
    void delSecRecord(MainVo vo, int rid) throws LaiKeAPIException;

    public static void main(String[] args)
    {
        List<String> remindList = new ArrayList<>();
        remindList.add("1");
        remindList.add("3");
        remindList.add("2");
        remindList.sort((o1, o2) ->
        {
            Integer num  = Integer.parseInt(o1);
            Integer num2 = Integer.parseInt(o2);
            return num < num2 ? 0 : -1;
        });
        System.out.println(remindList);
    }

}
