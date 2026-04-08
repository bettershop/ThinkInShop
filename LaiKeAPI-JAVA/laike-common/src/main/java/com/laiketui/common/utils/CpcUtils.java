package com.laiketui.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Struct;
import java.util.Objects;

/**
 * @Author: liuao
 * @Date: 2026-01-27-10:43
 * @Description:
 */
public class CpcUtils
{

    /**
     * 根据区号转换地址
     * @param cpc
     * @return
     */
    public static String coverAddress(String cpc,String sheng,String shi,String xian,String address)
    {
        String val;
        if (isHome(cpc))
        {
            val = sheng + shi + xian + address;
        }
        else
        {
            val  = address + shi + sheng;
        }
        return val;
    }

    /**
     * 根据区号判断是否是国内
     * @param cpc
     * @return
     */
    public static boolean isHome(String cpc)
    {
        if (StringUtils.isEmpty(cpc))
        {
            cpc = "86";
        }
        return Objects.equals("86",cpc) || Objects.equals("852",cpc);
    }
}
