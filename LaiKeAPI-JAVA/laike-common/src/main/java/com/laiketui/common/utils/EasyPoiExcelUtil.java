package com.laiketui.common.utils;

import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DataUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.vo.Tool.ExcelAnalysisVo;
import com.laiketui.domain.vo.Tool.ExcelParamVo;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * exceld导出工具类
 *
 * @author Trick
 * @date 2021/7/14 10:46
 */
public class EasyPoiExcelUtil
{


    /**
     * 导出excel
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/29 16:07
     */
    public static void excelExport(ExcelParamVo vo) throws LaiKeAPIException
    {
        try
        {
            if (vo.getResponse() == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            if (StringUtils.isEmpty(vo.getTitle()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            if (vo.getList() == null || vo.getList().size() < 1)
            {
                //如果没有数据则导出空数据
                if (vo.getList() == null || vo.getList().size() < 1)
                {
                    vo.setNeedNo(false);
                    vo.setList(new ArrayList<>());
                    Map<String, Object> tempMap = new HashMap<>(16);
                    for (String key : vo.getValueList())
                    {
                        tempMap.put(key, "");
                    }
                    vo.getList().add(tempMap);
                }
            }
            if (vo.getHeaderList() == null || vo.getHeaderList().length < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            if (vo.getHeaderList().length != vo.getHeaderList().length)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "数据内容和表头数量不匹配");
            }

            //创建Excel工作簿对象
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet    sheet    = workbook.createSheet(vo.getTitle());
            //设置表头样式
            HSSFCellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);

            //表头开始位置
            int topTitleIndex = 0;
            //顶部合并单元格-说明专用
            if (vo.getMerge() != null && vo.getMerge().length == 4)
            {
                //顶部占一行
                HSSFRow rowTop = sheet.createRow(topTitleIndex++);
                //设置表头高度
                rowTop.setHeightInPoints(30);
                //设置内容
                rowTop.createCell(0).setCellStyle(style);
                rowTop.getCell(0).setCellValue(vo.getTopTitle());
                //合并单元格
                CellRangeAddress region = new CellRangeAddress(vo.getMerge()[0], vo.getMerge()[1], vo.getMerge()[2], vo.getMerge()[3]);
                sheet.addMergedRegion(region);
            }

            //创建表头
            HSSFRow row = sheet.createRow(topTitleIndex);
            //设置表头高度
            row.setHeightInPoints(30);
            //生成序号
            if (vo.isNeedNo())
            {
                List<String> headerTempList = new ArrayList<>(Arrays.asList(vo.getHeaderList()));
                headerTempList.add(0, "序号");
                vo.setHeaderList(headerTempList.toArray(new String[0]));
                List<String> headerTempValueList = new ArrayList<>(Arrays.asList(vo.getValueList()));
                vo.setValueList(headerTempValueList.toArray(new String[0]));
            }
            //生成自定义表头
            for (int i = 0; i < vo.getHeaderList().length; i++)
            {
                row.createCell(i).setCellStyle(style);
                row.getCell(i).setCellValue(vo.getHeaderList()[i]);
            }

            //当前行对应的key
            List<String> valList = DataUtils.convertToList(vo.getValueList());
            if (valList == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误");
            }
            //设置内容
            for (int i = 0; i < vo.getList().size(); i++)
            {
                //当前找到的数量
                int num = 0;
                //生成当前行
                HSSFRow             fieldRow = sheet.createRow(i + topTitleIndex + 1);
                Map<String, Object> dataMap  = vo.getList().get(i);
                //循环获取当前列数据
                for (String keyTemp : dataMap.keySet())
                {
                    if (num == valList.size())
                    {
                        break;
                    }
                    //获取当前行数据
                    if (valList.contains(keyTemp))
                    {
                        //当前行对应的值的坐标
                        int index = valList.indexOf(keyTemp);
                        //获取当前x坐标数据
                        String rowValue = dataMap.get(keyTemp) + "";
                        if (StringUtils.isEmpty(rowValue))
                        {
                            rowValue = "";
                        }
                        //生成序号,序号每行只生成一个
                        if (vo.isNeedNo())
                        {
                            if (num == 0)
                            {
                                fieldRow.createCell(0).setCellStyle(style);
                                fieldRow.getCell(0).setCellValue(i + 1);
                            }
                            //加了序号,所以每行偏移一个位置
                            index++;
                        }
                        //创建行 x坐标>样式/值
                        fieldRow.createCell(index).setCellStyle(style);
                        fieldRow.getCell(index).setCellValue(rowValue);
                        num++;
                    }
                }
            }
            for (int i = 0; i < vo.getHeaderList().length; i++)
            {
                //设置自适应宽度
                sheet.autoSizeColumn(i);
            }
            //设置sheet页
            workbook.setActiveSheet(0);

            vo.getResponse().setContentType("application/msexcel");
            String title = String.format("%s.xls", vo.getTitle());
            vo.getResponse().addHeader("Content-Disposition", "filename=" + URLDecoder.decode(title, GloabConst.Chartset.UTF8));
            OutputStream outputStream = vo.getResponse().getOutputStream();

            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.FILE_EXCEL_ERROR, "EXCEL文件导出失败", "excelExport");
        }
    }

    @Deprecated
    public static void excelExport(String title, String[] headerList, String[] valueList, List<Map<String, Object>> list, HttpServletResponse response) throws LaiKeAPIException
    {
        ExcelParamVo vo = new ExcelParamVo();
        vo.setTitle(title);
        vo.setHeaderList(headerList);
        vo.setValueList(valueList);
        vo.setList(list);
        vo.setResponse(response);
        excelExport(vo);
    }

    /**
     * 解析excel
     *
     * @param vo -
     * @return List
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/7 15:41
     */
    public static Map<String, Object> analysisExcel(ExcelAnalysisVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (MultipartFile file : vo.getFile())
            {
                File fileTemp = multipartFileToFile(file);
                if (fileTemp == null)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.FILE_EXCEL_NOT_EXISTS, "文件不存在", "analysisExcel");
                }
                //第一个参数表示行数 第二个List保存该行的cell数据
                Map<Integer, List<String>> map = new LinkedHashMap<>(16);
                try (FileInputStream fis = new FileInputStream(fileTemp))
                {
                    Workbook workBook = new XSSFWorkbook(fis);
                    // 获取第一个sheet
                    Sheet sheet = workBook.getSheetAt(0);
                    //标题数量
                    int titleNum  = 0;
                    int lineIndex = 0;
                    for (Row row : sheet)
                    {
                        if (titleNum == 0)
                        {
                            //获取有效标题数量
                            for (Cell cell : row)
                            {
                                titleNum++;
                            }
                        }
                        map.put(lineIndex, new ArrayList<>());
                        //循环取值
                        for (int i = 0; i < titleNum; i++)
                        {
                            DataFormatter formatter = new DataFormatter();
                            map.get(lineIndex).add(formatter.formatCellValue(row.getCell(i)));
                        }
                        lineIndex++;
                    }
                }
                catch (Exception e)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.FILE_EXCEL_READ_ERROR, "EXCEL文件解析失败", "analysisExcel");
                }
                // 以下为遍历 Map解析结果
                Set<Integer> keys = map.keySet();
                if (keys.size() <= 0)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QWTJKBWJ, "请勿提交空白文件");
                }
                currentExcel:
                //循环每行
                for (Integer key : keys)
                {
                    //获取当前行列值
                    List<String> list = map.get(key);
                    //当前列
                    int index = 0;
                    //失败原因
                    StringBuilder errorText = new StringBuilder();
                    //组装每列内容
                    Map<String, Object> excelMap = new HashMap<>(1);
                    //excelMap.put("fileName", file.getOriginalFilename());
                    //循环当前行每列
                    for (String s : list)
                    {
                        if (key == 0)
                        {
                            if (vo.getTitleName() == null || list.size() != vo.getTitleName().size())
                            {
                                errorText.append(String.format("第%s行第%s列", key + 1, index + 1)).append(" 标题参数错误").append(SplitUtils.DH);
                            }
                            if (vo.getTitleName() == null || !s.equals(vo.getTitleName().get(index)))
                            {
                                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSCZQDRMB,"请上传正确的导入模板");
                            }
                            if (StringUtils.isNotEmpty(errorText))
                            {
                                excelMap.put("errorText", errorText);
                                resultList.add(excelMap);
                                break currentExcel;
                            }
                        }
                        if (key > 0)
                        {
                            if (vo.getValueKey() == null || list.size() != vo.getValueKey().size())
                            {
                                errorText.append(String.format("第%s行第%s列", key + 1, index)).append(" 内容数据错误").append(SplitUtils.DH);
                            }
                            excelMap.put(vo.getValueKey().get(index), s);
                            excelMap.put("x", key + 1);
                            excelMap.put("y", index + 1);
                        }
                        index++;
                    }
                    if (StringUtils.isNotEmpty(errorText))
                    {
                        excelMap.put("errorText", errorText);
                    }
                    if (MapUtils.isNotEmpty(excelMap))
                    {
                        resultList.add(excelMap);
                    }
                }
            }
            resultMap.put("list", resultList);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "batchDelivery");
        }
        return resultMap;
    }

    public static File multipartFileToFile(MultipartFile multiFile)
    {
        // 获取文件名
        String fileName = multiFile.getOriginalFilename();
        if (fileName == null)
        {
            fileName = "";
        }
        try
        {
            // 获取文件后缀
            String prefix = fileName.substring(fileName.lastIndexOf("."));
            // 若须要防止生成的临时文件重复,能够在文件名后添加随机码
            File file = File.createTempFile(fileName, prefix);
            multiFile.transferTo(file);
            return file;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
