package com.laiketui.common.utils.tool;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util
{
    /**
     * 32位
     *
     * @param sourceText
     * @param charset
     * @return
     */
    public static String MD5Is32(String sourceText, String charset)
    {
        byte[] secretBytes = null;
        if (charset == null)
        {
            charset = "UTF-8";
        }
        try
        {
            secretBytes = MessageDigest.getInstance("md5").digest(sourceText.getBytes(charset));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("MessageDigest get md5 instance error.");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(String.format("charset(%s) is not supported.", charset));
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        int    len     = 32 - md5code.length();
        for (int i = 0; i < len; i++)
        {
            md5code = "0" + md5code;
        }
        return md5code;
    }
}
