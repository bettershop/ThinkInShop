package com.laiketui.core.seata;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * seata 注册到zk
 */
public class SeataRegisterZookeeper
{

    private static final Logger log = LoggerFactory.getLogger(SeataRegisterZookeeper.class);

    private static volatile ZkClient zkClient;

    public static void register()
    {
        if (zkClient == null)
        {
            zkClient = new ZkClient("127.0.0.1:2181", 6000, 2000);
        }
        if (!zkClient.exists("/seata"))
        {
            zkClient.createPersistent("/seata", true);
        }
        //获取key对应的value值
        Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        // 使用properties对象加载输入流
        try
        {
            File        file = ResourceUtils.getFile("classpath:seata-config.properties");
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            }
            //返回属性key的集合
            Set<Object> keys = properties.keySet();
            for (Object key : keys)
            {
                boolean b = putConfig(key.toString(), properties.get(key).toString());
                System.out.println(key.toString() + "<=>" + properties.get(key));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean putConfig(final String dataId, final String content)
    {
        Boolean flag = false;
        String  path = "/seata/" + dataId;
        zkClient.setZkSerializer(new MyZkSerializer());
        if (!zkClient.exists(path))
        {
            zkClient.create(path, content, CreateMode.PERSISTENT);
            flag = true;
        }
        else
        {
            zkClient.writeData(path, content);
            flag = true;
        }
        return flag;
    }

    public static void main(String[] args)
    {
        SeataRegisterZookeeper.register();
    }
}
