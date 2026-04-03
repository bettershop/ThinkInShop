package com.laiketui.domain.vo.Tool;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * excel解析参数
 *
 * @author Trick
 * @date 2021/12/7 15:40
 */
public class ExcelAnalysisVo
{

    /**
     * 文件流
     */
    private List<MultipartFile> file;

    /**
     * 校验表头。按顺序
     */
    private List<String> titleName;
    /**
     * 内容对应的key
     * 例如 : key = order Map结构就是 order:xxx
     */
    private List<String> valueKey;

    public List<String> getValueKey()
    {
        return valueKey;
    }

    public void setValueKey(List<String> valueKey)
    {
        this.valueKey = valueKey;
    }

    public List<MultipartFile> getFile()
    {
        return file;
    }

    public void setFile(List<MultipartFile> file)
    {
        this.file = file;
    }

    public List<String> getTitleName()
    {
        return titleName;
    }

    public void setTitleName(List<String> titleName)
    {
        this.titleName = titleName;
    }
}
