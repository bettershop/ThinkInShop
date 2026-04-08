package com.laiketui.common.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: sunH_
 * @Date: Create in 17:36 2022/5/11
 */
public class FileUtils
{
    private static List<String> pathList = new ArrayList<>();

    public static List<String> filesDirs(File file)
    {
        String suffix = "ServiceImpl.java";
        //File对象是文件或文件夹的路径，第一层判断路径是否为空
        if (file != null)
        {
            //第二层路径不为空，判断是文件夹还是文件
            if (file.isDirectory())
            {
                //进入这里说明为文件夹，此时需要获得当前文件夹下所有文件，包括目录
                //注意:这里只能用listFiles()，不能使用list()
                File[] files = file.listFiles();
                //files下的所有内容，可能是文件夹，也可能是文件，那么需要一个个去判断是文件还是文件夹，这个判断过程就是这里封装的方法
                //因此可以调用自己来判断，实现递归
                for (File flies2 : files)
                {
                    filesDirs(flies2);
                }
            }
            else
            {
                if (file.toString().contains(suffix))
                {
                    pathList.add(file.getPath());
//                    System.out.println("文件名字"+file);
                }
            }
        }
        return pathList;
    }

    public static StringBuffer fileContent(String filePath)
    {
        StringBuffer stringBuffer = new StringBuffer();
        String       line         = null;
        String       property     = System.getProperty("line.separator");
        try
        {
            File              file            = new File(filePath);
            FileInputStream   fileInputStream = new FileInputStream(file);
            InputStreamReader inReader        = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader    bufReader       = new BufferedReader(inReader);
            while ((line = bufReader.readLine()) != null)
            {
                stringBuffer.append(line).append(property);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return stringBuffer;
    }
}
