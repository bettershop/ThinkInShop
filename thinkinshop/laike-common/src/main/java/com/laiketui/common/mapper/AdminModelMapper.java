package com.laiketui.common.mapper;

import com.laiketui.core.db.BaseMapper;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.domain.mch.AdminModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 管理员表
 *
 * @author Trick
 * @date 2020/11/11 9:15
 */
public interface AdminModelMapper extends BaseMapper<AdminModel>
{

    /**
     * 获取商城自营店铺
     *
     * @param storeId -
     * @return CustomerModel
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/11 9:13
     */
    @Select("select * from lkt_admin where store_id = #{storeId} and type = 1 and `recycle` = 0 ")
    AdminModel getAdminCustomer(int storeId) throws LaiKeAPIException;

    @Update("update lkt_admin set recycle = 1 where store_id = #{storeId}")
    int delAdminByStoreId(int storeId);

    /**
     * 登录失败
     */
    @Update("update lkt_admin set login_num = login_num+1 where id = #{id}")
    int adminLoginFail(int id);

    /**
     * 获取绑定角色的管理员
     *
     * @param roleId -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 16:07
     */
    @Select("select b.name from lkt_admin as a left join lkt_customer as b on a.store_id = b.id where a.type = 1 and a.role = #{roleId} " +
            "and a.recycle = 0 and b.recycle = 0")
    List<String> getAdminBindNameList(int roleId) throws LaiKeAPIException;


    /**
     * 获取绑定/非绑定商城
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/29 16:45
     */
    @Select("<script> select a.id,a.tel,b.name from lkt_admin as a left join lkt_customer as b on a.store_id = b.id " +
            "where a.recycle = 0 and b.recycle = 0" +
            "<if test='type != null '> and a.type = #{type} </if>" +
            "<if test='role != null '> and a.role = #{role} </if>" +
            "<if test='notRole != null '> and a.role != #{notRole} </if>" +
            "<if test='name != null '> and b.name like concat('%',#{name},'%') </if>" +
            "</script>")
    List<Map<String, Object>> getBindListInfo(Map<String, Object> map) throws LaiKeAPIException;


    /**
     * 获取管理员信息
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/13 15:00
     */
    List<Map<String, Object>> selectAdminListInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取管理员信息-统计
     *
     * @param map -
     * @return List
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/1/13 15:00
     */
    int countAdminListInfo(Map<String, Object> map) throws LaiKeAPIException;

    /**
     * 获取商城权限
     */
    @Select("select b.role from  lkt_customer a,lkt_admin b where a.id=#{storeId} and a.id=b.store_id and b.recycle = 0 and b.type=1 limit 1")
    Integer getStoreRole(int storeId);


    /**
     * 验证商户是否绑定
     */
    @Select("select count(1) from lkt_admin a,lkt_role b where a.id=#{adminId} and a.recycle=0 and a.role=b.id")
    int verificationBind(int adminId);

    //获取系统管理员
    @Select("select * from lkt_admin where type=0 and store_id=0 and status=2 limit 1")
    AdminModel getSystemAdmin();


    /**
     * 设置管理员自营店id
     */
    @Update("update lkt_admin set shop_id = #{mchId} where store_id = #{storeId}")
    int setAdminMchId(int storeId, int mchId);

    //获取商城所有管理员id
    @Select("select id from lkt_admin where store_id=#{storeId} and recycle=0 and status=2")
    List<Integer> getAdminIdList(int storeId);

    //恢复商城管理员账号
    @Update("update lkt_admin a set a.status = 2,a.login_num=0 where store_id=#{storeId} and recycle=0 ")
    int reductionAdmin(int storeId);

    /**
     * 根据管理员名称更新商城样式颜色
     *
     * @param name   管理员名称
     * @param color 颜色值
     * @return 受影响的行数
     */
    @Update("UPDATE lkt_admin " +
            "SET color = #{color} " +
            "WHERE name = #{name} ")
    int updateColorByName(@Param("name") String name, @Param("color") String color);

    List<AdminModel> selectByIds(ArrayList<Integer> integers);
}
