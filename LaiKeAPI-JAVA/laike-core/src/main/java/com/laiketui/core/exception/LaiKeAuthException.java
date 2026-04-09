package com.laiketui.core.exception;

/**
 * 来客授权异常类
 *
 * @author Administrator
 */
public class LaiKeAuthException extends LaiKeAPIException
{

    public LaiKeAuthException(String code, String message, String method, String descinfo)
    {
        super(code, message, method, descinfo);
    }

}
