package com.laiketui.core.exception;

/**
 * 用于忽略事物不回滚的自定义异常类
 * [如果该类被定义为不回滚,则会提交事物]
 *
 * @author Trick
 * @date 2022/7/5 15:16
 */
public class LaiKeApiWarnException extends LaiKeAPIException
{

    public LaiKeApiWarnException(String code, String message, String method, String descinfo)
    {
        super.setCode(code);
        super.setMessage(message);
        super.setMethod(method);
        super.setDescinfo(descinfo);
    }

    public LaiKeApiWarnException(String code, String message, String method)
    {
        super.setCode(code);
        super.setMessage(message);
        super.setMethod(method);
    }

    public LaiKeApiWarnException(String code, String message)
    {
        super.setCode(code);
        super.setMessage(message);
    }
}
