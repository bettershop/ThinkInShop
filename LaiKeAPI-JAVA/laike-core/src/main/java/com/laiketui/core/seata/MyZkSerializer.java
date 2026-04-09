package com.laiketui.core.seata;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.Charset;

/**
 * @author wangxian
 * 注入的时候key乱码问题处理类
 */
public class MyZkSerializer implements ZkSerializer
{
    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError
    {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    @Override
    public byte[] serialize(Object serializable) throws ZkMarshallingError
    {
        return ((String) serializable).getBytes(Charset.forName("UTF-8"));
    }
}