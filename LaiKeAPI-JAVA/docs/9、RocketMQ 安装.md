## ROCKETMQ部署与集成 【目前未用】

### 一、部署Name Server

```shell
docker pull foxiswho/rocketmq:server-4.7.0 
```



#### 2、创建持久化挂载文件

```shell
mkdir -p /usr/local/rockermq/rocketmq-server
```



#### 3、运行name server镜像

```shell
docker run -d \
--restart=always \
--name rmq-namesrver \
-p 9876:9876 \
-v /usr/local/rockermq/rocketmq-server/logs:/root/logs \
-v /usr/local/rockermq/rocketmq-server/store:/root/store \
-e "MAX_POSSIBLE_HEAP=100000000" \
-e "JAVA_OPT_EXT=-server -Xms128m -Xmx128m -Xmn128m" \
foxiswho/rocketmq:server-4.7.0  \
sh mqnamesrv


```



–restart=always: 容器停止后自动重启
–name rmqnamesrv: 容器的名称为"rmqnamesrv"
-p 9876:9876: 将主机的9876端口映射到容器的9876端口
-v /usr/local/rockermq/rocketmq-server/logs:/root/logs: 将主机的/usr/local/rockermq/rocketmq-server/logs目录挂载到容器的/root/logs目录
-v /usr/local/rockermq/rocketmq-server/store:/root/store: 将主机的/usr/local/rockermq/rocketmq-server/store目录挂载到容器的/root/store目录
-e “MAX_POSSIBLE_HEAP=100000000”: 设置环境变量MAX_POSSIBLE_HEAP为100000000
sh mq-namesrver: 在容器内执行shmq-namesrver命令



#### 4、开放防火墙端口

```shell
firewall-cmd --zone=public --add-port=9867/tcp --permanent
systemctl restart firewalld.service
```





### 二、部署Broker

#### 拉取镜像

```shell
docker pull foxiswho/rocketmq:broker-4.7.0
broker-4.7.0: Pulling from foxiswho/rocketmq
ab5ef0e58194: Already exists 
889a01968de9: Already exists 
18bd342b27ff: Already exists 
72ffdb70a768: Already exists 
4fb66fd2eb2c: Already exists 
886099d0389f: Already exists 
2f582ad2dfc7: Already exists 
2b68aec016ce: Already exists 
0be3745907f1: Already exists 
c1c497c9fc50: Already exists 
error pulling image configuration: Get "https://production.cloudflare.docker.com/registry-v2/docker/registry/v2/blobs/sha256/7f/7f35e57e1383e88aa7bb8b6ec40179855b68289246019633aa9380499a82fc67/data?verify=1725852305-8WSD5xxy7efnoJc%2F3mlW%2FbL8SR0%3D": read tcp 172.18.187.203:52840->104.16.99.215:443: read: connection reset by peer
```



#### 创建挂载文件以及配置文件

```shell
mkdir -p /usr/local/rockermq/rocketmq-broker/conf
touch /usr/local/rockermq/rocketmq-broker/conf/broker.conf
vim /usr/local/rockermq/rocketmq-broker/conf/broker.conf
```

broker.conf配置文件内容如下

```conf
# 所属集群名字
brokerClusterName=DefaultCluster

# broker 名字，注意此处不同的配置文件填写的不一样，如果在 broker-a.properties 使用: broker-a,
# 在 broker-b.properties 使用: broker-b
brokerName=broker-a

# 0 表示 Master，> 0 表示 Slave
brokerId=0

# nameServer地址，多个的话用分号分割
# namesrvAddr=rocketmq-nameserver1:9876;rocketmq-nameserver2:9876
namesrvAddr=[服务器ip]:9876

# 启动IP,如果 docker 报 com.alibaba.rocketmq.remoting.exception.RemotingConnectException: connect to <192.168.0.120:10909> failed
# 解决方式1 加上一句 producer.setVipChannelEnabled(false);
# 解决方式2 brokerIP1 设置宿主机IP，不要使用docker 内部IP (建议直接设置)
brokerIP1=[服务器ip]

# 在发送消息时，自动创建服务器不存在的topic，默认创建的队列数
#defaultTopicQueueNums=4

# 是否允许 Broker 自动创建 Topic，建议线下开启，线上关闭 ！！！这里仔细看是 false，false，false
#autoCreateTopicEnable=false

# 是否允许 Broker 自动创建订阅组，建议线下开启，线上关闭
#autoCreateSubscriptionGroup=false

# Broker 对外服务的监听端口
listenPort=10911

# 删除文件时间点，默认凌晨4点
deleteWhen=04

# 文件保留时间，默认48小时
fileReservedTime= 120

# commitLog 每个文件的大小默认1G
#mapedFileSizeCommitLog=1073741824

# ConsumeQueue 每个文件默认存 30W 条，根据业务情况调整
#mapedFileSizeConsumeQueue=300000

# destroyMapedFileIntervalForcibly=120000
# redeleteHangedFileInterval=120000
# 检测物理文件磁盘空间
#diskMaxUsedSpaceRatio=88
# 存储路径
# storePathRootDir=/home/ztztdata/rocketmq-all-4.1.0-incubating/store
# commitLog 存储路径
# storePathCommitLog=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/commitlog
# 消费队列存储
# storePathConsumeQueue=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/consumequeue
# 消息索引存储路径
# storePathIndex=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/index
# checkpoint 文件存储路径
# storeCheckpoint=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/checkpoint
# abort 文件存储路径
# abortFile=/home/ztztdata/rocketmq-all-4.1.0-incubating/store/abort
# 限制的消息大小
maxMessageSize=65536

# flushCommitLogLeastPages=4
# flushConsumeQueueLeastPages=2
# flushCommitLogThoroughInterval=10000
# flushConsumeQueueThoroughInterval=60000

# Broker 的角色
# - ASYNC_MASTER 异步复制Master
# - SYNC_MASTER 同步双写Master
# - SLAVE
brokerRole=ASYNC_MASTER

# 刷盘方式
# - ASYNC_FLUSH 异步刷盘
# - SYNC_FLUSH 同步刷盘
flushDiskType=ASYNC_FLUSH

# 发消息线程池数量
# sendMessageThreadPoolNums=128
# 拉消息线程池数量
# pullMessageThreadPoolNums=128
```

#### 运行broker镜像

```shell
docker run -d \
--restart=always \
--name rmq-broker \
--link rmq-namesrver \
-p 10911:10911 \
-p 10909:10909 \
-v /usr/local/rockermq/rocketmq-broker/logs:/root/logs \
-v /usr/local/rockermq/rocketmq-broker/store:/root/store \
-v /usr/local/rockermq/rocketmq-broker/conf:/opt/rocketmq-4.7.0/conf \
-e "NAMESRV_ADDR=namesrv:9876" \
-e "MAX_POSSIBLE_HEAP=200000000" \
-e "autoCreateTopicEnable=true" \
-e "JAVA_OPT_EXT=-server -Xms128m -Xmx128m -Xmn128m" \
foxiswho/rocketmq:broker-4.7.0 \
sh mqbroker -c /opt/rocketmq-4.7.0/conf/broker.conf
```

–restart=always: 如果容器发生错误或被停止，自动重新启动。
–name rmq-broker: 容器的名称为rmq-broker。
–link rmq-namesrv:namesrv: 将rmq-namesrv容器链接到rmq-broker容器，以便它们可以相互通信。
-p 10911:10911: 将主机的10911端口映射到容器的10911端口，用于消息存储。
-p 10909:10909: 将主机的10909端口映射到容器的10909端口，用于管理控制台。
-v /usr/local/rockermq/rocketmq-broker/logs:/root/logs: 将主机的日志目录挂载到容器内的日志目录。
-v /usr/local/rockermq/rocketmq-broker/store:/root/store: 将主机的存储目录挂载到容器内的存储目录。
-v /usr/local/rockermq/rocketmq-broker/conf:/opt/rocketmq-4.7.0/conf: 将主机的配置文件目录挂载到容器内的配置文件目录。
-e “NAMESRV_ADDR=namesrv:9876”: 设置环境变量NAMESRV_ADDR为namesrv:9876，指定NameServer的地址和端口。
-e “MAX_POSSIBLE_HEAP=200000000”: 设置环境变量MAX_POSSIBLE_HEAP为200000000，指定Broker的堆内存大小。



#### 开放防火墙端口

```shell
firewall-cmd --zone=public --add-port=10911/tcp --permanent
systemctl restart firewalld.service
```



### 部署client客户端界面



```shell
docker run -itd -e "JAVA_OPTS=-Drocketmq.namesrv.addr=[服务器ip]:9876 \
-Dcom.rocketmq.sendMessageWithVIPChannel=false" \
-p 8087:8080 \
--name rmq-client \
-t styletang/rocketmq-console-ng:latest
```

-itd：创建一个交互式容器，并在后台运行。
-e “JAVA_OPTS=-Drocketmq.namesrv.addr=[服务器ip]:9876 \ -Dcom.rocketmq.sendMessageWithVIPChannel=false”：设置容器内的环境变量，指定RocketMQ的地址和端口。
-p 8087:8080：将容器内的8080端口映射到主机的8082端口。
–name rmq-client：给容器指定一个名称为rmq-client。
-t styletang/rocketmq-console-ng:latest：指定容器使用的镜像为styletang/rocketmq-console-ng:latest。



> 客户端访问地址：

http://IP:8087



参考网址：https://blog.csdn.net/weixin_43336075/article/details/136358550


## 代码集成

已在laike-datax中集成测试成功。