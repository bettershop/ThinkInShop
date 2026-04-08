package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.config.ConfigModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 配置
 *
 * @author Trick
 * @date 2020/10/13 10:50
 */
public interface ConfigModelMapper extends BaseMapper<ConfigModel>
{


    /**
     * 获取所有配置信息
     *
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/14 15:38
     */
    @Select("select a.* from lkt_config a inner join lkt_customer b on a.store_id=b.id and b.recycle=0 order by a.store_id")
    List<ConfigModel> getConfigAllInfo() throws LaiKeAPIException;

    //获取移动端登录有效时间
    @Select("select exp_time*60*60 from lkt_config where store_id=#{storeId} and exp_time>0")
    Integer getLoginExistenceTime(int storeId);

    /**
     * 修改所有商城上传图片设置
     *
     * @param upserver -
     * @return int
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 18:37
     */
    @Update("update lkt_config set upserver = #{upserver}")
    int updateConfigAll(int upserver) throws LaiKeAPIException;


    @Update("update lkt_config set app_domain_name = #{domainUrl},modify_date = CURRENT_TIMESTAMP where store_id = #{storeId}")
    int updateConfigDomain(String domainUrl, int storeId) throws LaiKeAPIException;

    /**
     *
     * 修改安卓、苹果下载链接
     *
     * @param androidDownloadLink
     * @param iosDownloadLink
     * @param storeId
     * @return
     * @throws LaiKeAPIException
     */
    @Update("update lkt_config set android_download_link = #{androidDownloadLink},ios_download_link = #{iosDownloadLink} where store_id = #{storeId}")
    int updateDownloadLink(String androidDownloadLink, String iosDownloadLink, int storeId) throws LaiKeAPIException;


    /**
     * 获取邮箱配置
     * @param storeId
     * @return
     */
    @Select("select mail_config,id,store_name from lkt_config where store_id = #{storeId}")
    Map<String,Object> getEmailConfig(int storeId);

    List<ConfigModel> selectByStoreIds(@Param("storeIds") ArrayList<Integer> integers);
}
