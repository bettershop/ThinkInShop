package com.laiketui.domain.vo.diy;

import com.laiketui.domain.vo.MainVo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@ApiModel(description = "添加/编辑diy页面")
@Setter
@Getter
public class DiyPageVo extends MainVo
{

    /**
     * 页面名称
     */
    private String page_name;

    /**
     * 状态 0:不启用 1:启用
     */
    private Integer status;

    /**
     * id
     */
    private Integer id;

    /**
     * 主题id
     */
    private Integer diyId;

    /**
     * 图片
     */
    private String url;

    /**
     * 链接
     */
    private String link;

    /**
     * 页面类型 0；系统页面 1：自定义页面
     */
    private Integer page_type;

    /**
     * 主题类型 1:系统主题 2：自定义主题
     */
    private Integer theme_type;

    /**
     * 是否回收 0：显示 1：回收
     */
    private Integer recycle;

    /**
     * 添加时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 创建人
     */
    private String create_by;

    /**
     * 页面内容
     */
    private String page_context;

    /**
     * diy数据使用链接对应的json key，逗号拼接
     */
    private String link_key;

    /**
     * 页面对应的key
     */
    private String page_key;

    /**
     * 子组件value
     */
    private String unit;

    /**
     * 主题列表
     */
    private List<Integer> diyIds;

    /**
     * 判断页面是否新增 0：不新增
     */
    private Integer type = 1;

    /**
     * 旧链接
     */
    private String old_link_key;
}
