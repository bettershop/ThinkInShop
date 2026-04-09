## Seata安装部署 【目前弃用】

目前是用的nacos 配置信息存储在mysql数据库中。

### 一、安装

项目docs目录下面的soft文件夹中有[seata1.6.1](./soft/seata1.6.1)文件夹 可以直接拷贝出来使用。

### 二、配置

1、在mysql数据库中新建 seata_db 数据库，并导入脚本 [mysql.sql](./soft/seata1.6.1/script/server/db/mysql.sql).
2、修改Seata配置文件 [application.yml](./soft/seata1.6.1/conf/application.yml).

#### 在Nacos中配置SeataServer的配置信息 data-id名称要和{seata_home}/conf/application.yml中的一致

在nacos的public命名空间加入 配置 seataServer-dev.properties 文件如下配置 需要修改数据库配置

```properties
#For details about configuration items, see https://seata.io/zh-cn/docs/user/configurations.html
#Transport configuration, for client and server
transport.type=TCP
transport.server=NIO
transport.heartbeat=true
transport.enableTmClientBatchSendRequest=false
transport.enableRmClientBatchSendRequest=true
transport.enableTcServerBatchSendResponse=false
transport.rpcRmRequestTimeout=30000
transport.rpcTmRequestTimeout=30000
transport.rpcTcRequestTimeout=30000
transport.threadFactory.bossThreadPrefix=NettyBoss
transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
transport.threadFactory.shareBossWorker=false
transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
transport.threadFactory.clientSelectorThreadSize=1
transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
transport.threadFactory.bossThreadSize=1
transport.threadFactory.workerThreadSize=default
transport.shutdown.wait=3
transport.serialization=seata
transport.compressor=none
#Transaction routing rules configuration, only for the client
# 此处的mygroup名字可以自定义，只修改这个值即可
service.vgroupMapping.mygroup=default
#If you use a registry, you can ignore it
#service.default.grouplist=127.0.0.1:8091
#service.enableDegrade=false
#service.disableGlobalTransaction=false
#Transaction rule configuration, only for the client
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=true
client.rm.tableMetaCheckerInterval=60000
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
client.rm.sagaJsonParser=fastjson
client.rm.tccActionInterceptorOrder=-2147482648
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000
client.tm.interceptorOrder=-2147482648
client.undo.dataValidation=true
client.undo.logSerialization=jackson
client.undo.onlyCareUpdateColumns=true
server.undo.logSaveDays=7
server.undo.logDeletePeriod=86400000
client.undo.logTable=undo_log
client.undo.compress.enable=true
client.undo.compress.type=zip
client.undo.compress.threshold=64k
#For TCC transaction mode
tcc.fence.logTableName=tcc_fence_log
tcc.fence.cleanPeriod=1h
#Log rule configuration, for client and server
log.exceptionRate=100
#Transaction storage configuration, only for the server. The file, db, and redis configuration values are optional.
# 默认为file，一定要改为db，我们自己的服务启动会连接不到seata
store.mode=db
store.lock.mode=db
store.session.mode=db
#Used for password encryption
#These configurations are required if the `store mode` is `db`. If `store.mode,store.lock.mode,store.session.mode` are not equal to `db`, you can remove the configuration block.
# 修改mysql的配置
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
# 指定seata的数据库，下面会提
store.db.url=jdbc:mysql://localhost:3306/seata_db?useUnicode=true&rewriteBatchedStatements=true
store.db.user=root
store.db.password=root
store.db.minConn=5
store.db.maxConn=30
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.distributedLockTable=distributed_lock
store.db.queryLimit=100
store.db.lockTable=lock_table
store.db.maxWait=50000
#Transaction rule configuration, only for the server
server.recovery.committingRetryPeriod=600000
server.recovery.asynCommittingRetryPeriod=600000
server.recovery.rollbackingRetryPeriod=600000
server.recovery.timeoutRetryPeriod=600000
server.maxCommitRetryTimeout=-1
server.maxRollbackRetryTimeout=-1
server.rollbackRetryTimeoutUnlockEnable=false
server.distributedLockExpireTime=600000
server.xaerNotaRetryTimeout=600000
server.session.branchAsyncQueueSize=50000
server.session.enableBranchAsyncRemove=false
server.enableParallelRequestHandle=false
#Metrics configuration, only for the server
metrics.enabled=false
metrics.registryType=compact
metrics.exporterList=prometheus
metrics.exporterPrometheusPort=9898

```

#### {seata_home}/conf/application.yml 中的 注册中心 配置中心 和seata的数据存储配置

```yaml
server:
  # 访问端口 可以修改
  port: 7091

spring:
  application:
    # 应用名称  可以修改
    name: seata-server

logging:
  config: classpath:logback-spring.xml
  file:
    path: ${user.home}/logs/seata
  extend:
    logstash-appender:
      destination: 127.0.0.1:4560
    kafka-appender:
      bootstrap-servers: 127.0.0.1:9092
      topic: logback_to_logstash

## 修改配置文件开始 
# 控制台 
console:
  user:
    username: seata
    password: seata

seata:
  # 配置中心 
  config:
    type: nacos
    nacos:
      # nacos 访问地址 [可修改]
      server-addr: localhost:8848
      # nacos 命名空间 [可修改]
      namespace: da8f7d37-960a-4c94-9691-9c77711d995c
      # nacos 分组 [可修改]
      group: DEFAULT_GROUP
      # nacos 账户密码 [可修改]
      username: nacos
      password: laike
      # seata配置 先配好nacos 把seataServer-dev.properties 复制到nacos里面 
      # 这个可以直接放在nacos的public 命令空间里面  [可修改,一定要跟上面的seata-server.properties文件名一致]
      data-id: seataServer-dev.properties
  # 注册中心
  registry:
    type: nacos
    nacos:
      # 应用名
      application: seata-server
      # nacos 访问地址
      server-addr: localhost:8848
      # nacos 分组
      group: DEFAULT_GROUP
      # nacos 命名空间
      namespace: da8f7d37-960a-4c94-9691-9c77711d995c
      cluster: default
      # 账户密码
      username: nacos
      password: laike
  ## 修改配置文件结束

  security:
    secretKey: SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
    tokenValidityInMilliseconds: 1800000
    ignore:
      urls: /,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/api/v1/auth/login
```

### 三、启动

#### 方式一、Linux、MacOS 启动

```shell
# 切换到seata的bin目录
cd {seata_home}/bin/
./seata-server.sh 
# 查看日志
tail -f ../logs/start.out 

```

#### 方式二、Windows

```shell
# 切换到seata的bin目录
cd {seata_home}/bin/
./seata-server.bat
# 查看日志,直接打开
# {seata_home}/logs/start.out 
```

### 四、访问

打开浏览器输入，访问地址：http://localhost:7091/#/
账户密码：seata/seata