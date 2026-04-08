    ## 环境问题

1、数据库Mysql建议使用5.6 其他版本未适配
2、Maven版本使用3.6.1 其他3.8版本以上有问题
3、Tomcat使用9版本 其他版本未适配
4、jdk版本是1.8.211
5、zookeeper版本是3.7.1【 Nacos版本已经弃用zookeeper ，以前版本保持不变 】
6、编码统一使用UTF8
7、使用的部署机器建议配置：8核16G-24G
8、有些linux系统无中文字体的需要安装中文字体否则分享的时候图片文字会有乱码。
9、Nacos版本 2.1.1
10、Seata版本 1.6.1

## 打包问题

1、打包前确定好环境 目前主要使用三个环境配置文件：

```xml

<profiles>
    <profile>
        <!-- 本地环境 -->
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <id>local</id>
        <properties>
            <profiles.active>local</profiles.active>
            <!-- 根据具体情况修改 命名空间 在nacos控制界面的命名空间列表中获取值 -->
            <nacos.namespace>f124b515-5b21-4bf8-9f72-25b585ee1343</nacos.namespace>
            <!-- nacos访问地址  -->
            <nacos.address>localhost:8848</nacos.address>
            <!--  本地环境公共配置 -->
            <nacos.comm>comm-local.yml</nacos.comm>
            <!--  公共配置  -->
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <!--  公共配置 -->
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java开发环境 -->
        <id>dev</id>
        <properties>
            <profiles.active>dev</profiles.active>
            <!-- 根据具体情况修改 命名空间 在nacos控制界面的命名空间列表中获取值 -->
            <nacos.namespace>da8f7d37-960a-4c94-9691-9c77711d995c</nacos.namespace>
            <!-- nacos访问地址  -->
            <nacos.address>localhost:8848</nacos.address>
            <!--  开发环境公共配置 -->
            <nacos.comm>comm-dev.yml</nacos.comm>
            <!--  公共配置  -->
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <!--  公共配置 -->
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java生产环境 -->
        <id>prod</id>
        <properties>
            <profiles.active>prod</profiles.active>
            <!-- 根据具体情况修改 命名空间 在nacos控制界面的命名空间列表中获取值 -->
            <nacos.namespace>23467031-bda4-48a0-a7ab-6bc1a4be22fd</nacos.namespace>

            <nacos.address>localhost:8848</nacos.address>
            <!--  生产环境公共配置 -->
            <nacos.comm>comm-prod.yml</nacos.comm>
            <!--  公共配置  -->
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <!--  公共配置 -->
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java演示环境 -->
        <id>demo</id>
        <properties>
            <profiles.active>demo</profiles.active>
            <!-- 根据具体情况修改 命名空间 在nacos控制界面的命名空间列表中获取值 -->
            <nacos.namespace>41b2716b-f0ea-443b-a9e1-481bdde0c62f</nacos.namespace>
            <!-- nacos访问地址  -->
            <nacos.address>localhost:8848</nacos.address>
            <!--  演示环境公共配置 -->
            <nacos.comm>comm-demo.yml</nacos.comm>
            <!--  公共配置  -->
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <!--  公共配置 -->
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java测试环境 -->
        <id>test</id>
        <properties>
            <profiles.active>test</profiles.active>
            <!-- 根据具体情况修改 命名空间 在nacos控制界面的命名空间列表中获取值 -->
            <nacos.namespace>a8d5eeb0-856f-4cdb-afe3-0fbd8d5bb597</nacos.namespace>
            <!-- nacos访问地址  -->
            <nacos.address>localhost:8848</nacos.address>
            <!--  测试环境公共配置 -->
            <nacos.comm>comm-test.yml</nacos.comm>
            <!--  公共配置  -->
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <!--  公共配置 -->
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
</profiles>
```
2、确定好环境后台 配置数据库和redis 在laike-core模块下面的resources下面对应有三个环境的配置请不要改错文件
3、zookeeper配置是非必须的如果不是在本地则需要配置zookeeper的ip和端口
4、maven打包前需要切换环境，clean 和 reload ；如果不生效clean。
5、授权包安装命令 如下；其他的如果下载不到，请到微信群里面索取maven仓库包。

```shell 
mvn install:install-file -Dfile=/path/laike-root-0.0.1-SNAPSHOT.jar -DgroupId=com.laiketui -DartifactId=laike-root -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
```
6、打包时候出现jitpack.io 获取不到root包的问题，修改pom.xml文件
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
		<!-- 新增这行 -->
        <snapshots><enabled>false</enabled></snapshots>
    </repository>
</repositories> 
```

在MySQL5.7中timestamp是不能设置默认null的，需要修改配置。

## 端口配置
1、需要启动的子模块打包时需要修改端口必须要和这个子模块打包后存放的tomcat的server.xml 中的端口一致。
```YML
server:
  #这个端口必须要和这个模块最终部署到的tomcat的端口一致否则会获取不到服务，服务未启动或接口不存在
  port: ${TOMCAT_PORT:18186} 
```
2、如果不一致，需要改成一致。
A-可以改tomcat的server.xml 中的端口,这种情况只要启动tomcat就行。
B-也可以修改子模块对应环境的yml文件中的port端口。
由于端不一样导致服务不能访问提示获取服务异常的打包后请一定要先清空redis里面的信息，再启动tomcat。
改过端口 redis中 一个服务存了两个 uri 一个是正常的一个是不能用的 ，清空redis 然后重启tomcat是重新注册服务。这种情况包含修改了controller层的接口方法入参也需要重新删掉对应的reids服务新增，重启后重新注册服务。

## 启动tomcat错误问题
修改 tomcat_home/conf/catanina.properties 文件第108行
```properties
tomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar
```
## 配置问题
1、OSS请去阿里云服务器申请开通OSS 服务并配置好跨域权限。
2、支付信息需要去微信或者支付宝那边申请开通，审核通过后再到商城后台配置支付信息。
3、微信小程序授权需要在两个地方配置好微信小程序的 appid 和 密钥
商城 - 支付管理 - 微信小程序支付
商城 - 终端管理 - 微信小程序
4、微信公众号、微信APP授权登录问题
5、微信支付证书需要在 laike-admin-store的资源文件application.yml 中配置
```yml
node:
	# 微信p12证书保存位置：不同的操作系统需要修改保存的位置 需要读写权限
  wx-certp12-path: /var/pay
```

> 开通

登录[微信开放平台](https://open.weixin.qq.com/)，添加移动应用并提交审核，审核通过后可获取应用ID（AppID），AppSecret等信息
在应用详情中申请开通微信登录功能，根据页面提示填写资料，提交审核
申请审核通过后即可打包使用微信授权登录功能
更多信息详见微信官方文档 移动应用微信登录开放指南

> 小程自动发货功能需要开通


> 在MySQL5.7中timestamp是不能设置默认null的，需要修改mysql配置文件my.cnf/my.ini配置。

```properties
[mysqld]
explicit_defaults_for_timestamp=on
```





