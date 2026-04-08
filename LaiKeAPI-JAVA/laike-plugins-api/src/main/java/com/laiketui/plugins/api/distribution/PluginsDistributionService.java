package com.laiketui.plugins.api.distribution;

import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.vo.MainVo;
import com.laiketui.domain.vo.plugin.AddDistributionConfigVo;
import com.laiketui.domain.vo.plugin.AddDistributionGradeVo;
import com.laiketui.domain.vo.plugin.DistributionQueryVo;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 分销
 *
 * @author Trick
 * @date 2021/2/4 16:29
 */
public interface PluginsDistributionService
{


    /**
     * 获取分销等级下拉
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 16:39
     */
    Map<String, Object> getDistributionGradeList(MainVo vo) throws LaiKeAPIException;


    /**
     * 获取分销列表
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/4 16:49
     */
    Map<String, Object> getDistributionInfo(DistributionQueryVo vo) throws LaiKeAPIException;


    /**
     * 编辑分佣
     *
     * @param id             -
     * @param consumptionAmt -累计消费
     * @param achievementAmt -推广业绩
     * @param withdrawalAmt  -可提现
     * @param token          -
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/5 9:46
     */
    void editDistribution(int id, BigDecimal consumptionAmt, BigDecimal achievementAmt, BigDecimal withdrawalAmt, String token) throws LaiKeAPIException;


    /**
     * 修改推荐人
     *
     * @param vo        -
     * @param id        -
     * @param fatherUid -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/5 11:07
     */
    boolean editReferences(MainVo vo, int id, String fatherUid) throws LaiKeAPIException;

    /**
     * 修改分销商等级
     *
     * @param id    -
     * @param level -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 14:00
     */
    boolean editeGrade(MainVo vo, int id, int level) throws LaiKeAPIException;


    /**
     * 删除分销商
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 14:20
     */
    boolean delDistribution(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取未开通分销商的列表
     *
     * @param vo       -
     * @param userName -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 14:35
     */
    Map<String, Object> getGrabbleListInfo(MainVo vo, String userName, String userId, String phone) throws LaiKeAPIException;


    /**
     * 开通分销商
     *
     * @param vo     -
     * @param userid -
     * @param level  -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 15:02
     */
    boolean builderLevel(MainVo vo, String userid, int level) throws LaiKeAPIException;


    /**
     * 获取分销等级信息
     *
     * @param vo -
     * @param id -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 16:13
     */
    Map<String, Object> getGradeInfo(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 添加/编辑分销等级
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/6 16:36
     */
    boolean addGradeInfo(AddDistributionGradeVo vo) throws LaiKeAPIException;


    /**
     * 删除等级
     *
     * @param vo -
     * @param id -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 9:39
     */
    boolean delGrade(MainVo vo, int id) throws LaiKeAPIException;


    /**
     * 获取佣金列表
     *
     * @param vo        -
     * @param id        -
     * @param startDate -
     * @param endDate   -
     * @param type      -
     * @param userName  -
     * @param fromId    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 10:19
     */
    Map<String, Object> getRecordInfo(MainVo vo, Integer id, String startDate, String endDate, Integer type, String userName, String fromId, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 获取关系列表
     *
     * @param vo        -
     * @param id        -
     * @param startDate -
     * @param endDate   -
     * @param type      -
     * @param userName  -
     * @param fatherId  -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 11:12
     */
    Map<String, Object> getRelationshipInfo(MainVo vo, Integer id, String startDate, String endDate, Integer type, String userName, String fatherId) throws LaiKeAPIException;


    /**
     * 获取提现记录
     *
     * @param vo          -
     * @param startDate   -
     * @param endDate     -
     * @param examineType -
     * @param name        -
     * @param bankName    -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 14:18
     */
    Map<String, Object> getWithdrawalRecordInfo(MainVo vo, String startDate, String endDate, Integer examineType, String name, String bankName, HttpServletResponse response) throws LaiKeAPIException;

    /**
     * 佣金提现审核
     *
     * @param vo          -
     * @param examineType -
     * @param wid         -
     * @param refuse      - 拒绝原因
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/2/9 11:29
     */
    void withdrawalExamine(MainVo vo, Integer examineType, Integer wid, String refuse) throws LaiKeAPIException;

    /**
     * 删除分销提现记录
     *
     * @param vo -
     * @param id -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/1/25 11:29
     */
    void delWithdrawalRecord(MainVo vo, Integer id) throws LaiKeAPIException;


    /**
     * 获取分销配置信息
     *
     * @param vo -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 14:58
     */
    Map<String, Object> getDistributionConfigInfo(MainVo vo) throws LaiKeAPIException;

    /**
     * 添加/编辑分销配置信息
     *
     * @param vo -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/7 15:25
     */
    boolean addDistributionConfigInfo(AddDistributionConfigVo vo) throws LaiKeAPIException;


    /**
     * 获取佣金排行榜
     *
     * @param vo   -
     * @param id   -
     * @param type -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 14:25
     */
    Map<String, Object> getDistributionTopInfo(MainVo vo, Integer id, int type) throws LaiKeAPIException;


    /**
     * 获取非当前排行榜的用户
     *
     * @param vo   -
     * @param name -
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 14:34
     */
    Map<String, Object> getDistributionUserInfo(MainVo vo, String name) throws LaiKeAPIException;


    /**
     * 添加/编辑用户排行榜
     *
     * @param vo         -
     * @param id         -
     * @param userId     -
     * @param type       -
     * @param commission -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 15:22
     */
    boolean addDistributionRankInfo(MainVo vo, Integer id, String userId, Integer type, BigDecimal commission) throws LaiKeAPIException;


    /**
     * 批量删除排行榜数据
     *
     * @param vo  -
     * @param ids -
     * @return boolean
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/2/8 15:48
     */
    boolean delBatchRank(MainVo vo, List<Integer> ids) throws LaiKeAPIException;
}
