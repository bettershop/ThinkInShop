## 说明

laike-cloud-gateway 这个网关只支持jar包部署

# laike-gateway
laike-gateway模块是我们系统建设开始到引入 springcloud之前一直使用的自研网关。系统中的接口访问都是统一从我们的网关进。

说明：laike-gateway模块现在已经变更集成到laike-comps模块中的gateway子文件夹中

# laike-cloud-gateway
引入 springcloud 之后，为使用openfeign，引入了 spring-gateway 这个网关是spring的一个组件。加入这个gateway 之后为了适配之前的接口调用，目前 的处理方式是将我们的自研网关注入到nacos里面了 其他的不动。客户新增的接口调用可以用 通过网关使用openfeign 来调用 新增的接口。

> 注意：若不需要使用 openfeign ，可以不用部署 laike-cloud-gateway 模块! laike-comps服务必须要启动。
> 注意：若不需要使用 openfeign ，可以不用部署 laike-cloud-gateway 模块! laike-comps服务必须要启动。
> 注意：若不需要使用 openfeign ，可以不用部署 laike-cloud-gateway 模块! laike-comps服务必须要启动。


## 一、环境配置

pom.xml 中修改网关的Nacos 配置命名空间 和 Nacos访问地址

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
            <nacos.namespace>f124b515-5b21-4bf8-9f72-25b585ee1343</nacos.namespace>
            <nacos.address>localhost:8848</nacos.address>
            <nacos.comm>comm-local.yml</nacos.comm>
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java开发环境 -->
        <id>dev</id>
        <properties>
            <profiles.active>dev</profiles.active>
            <nacos.namespace>da8f7d37-960a-4c94-9691-9c77711d995c</nacos.namespace>
            <nacos.address>localhost:8848</nacos.address>
            <nacos.comm>comm-dev.yml</nacos.comm>
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java生产环境 -->
        <id>prod</id>
        <properties>
            <profiles.active>prod</profiles.active>
            <nacos.namespace>23467031-bda4-48a0-a7ab-6bc1a4be22fd</nacos.namespace>
            <nacos.address>localhost:8848</nacos.address>
            <nacos.comm>comm-prod.yml</nacos.comm>
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java演示环境 -->
        <id>demo</id>
        <properties>
            <profiles.active>demo</profiles.active>
            <nacos.namespace>41b2716b-f0ea-443b-a9e1-481bdde0c62f</nacos.namespace>
            <nacos.address>localhost:8848</nacos.address>
            <nacos.comm>comm-demo.yml</nacos.comm>
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
    <profile>
        <!-- java测试环境 -->
        <id>test</id>
        <properties>
            <profiles.active>test</profiles.active>
            <nacos.namespace>a8d5eeb0-856f-4cdb-afe3-0fbd8d5bb597</nacos.namespace>
            <!-- 改成nacos的地址 可以是域名或者 ip:port -->
            <nacos.address>localhost:8848</nacos.address>
            <nacos.comm>comm-test.yml</nacos.comm>
            <nacos.base.mybatis>laike-base-mybatis.yml</nacos.base.mybatis>
            <nacos.plugins.mybatics>laike-plugins-mybatis.yml</nacos.plugins.mybatics>
        </properties>
    </profile>
</profiles>
```

## 二、网关端口配置

打开浏览器输入 nacos 访问地址

http://localhost:8848/nacos
修改对应的环境laike-cloud-gateway-{active-profile}.yml 文件配置

```yaml
server:
  port: 9526 # 一般只要这个端口不被占用就行 
  tomcat:
    uri-encoding: UTF-8
  ssl:
    enabled: false
    connection-timeout: 20000

spring:
  cloud:
    gateway:
      # 处理跨域请求
      globalcors:
        add-to-simple-url-handler-mapping: true
        cors-configurations:
          '[/**]':
            allowedHeaders: "*"
            allowedOrigins: "*"
            allowCredentials: true
            allowedMethods:
              - GET
              - POST
              - DELETE
              - HEAD
              - PUT
              - OPTION
      default-filters:
        - DedupeResponseHeader=Vary Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST

      routes:
        # 配置模块路由
        # PC店铺端接口
        - id: admin-mch
          uri: lb://admin-mch
          predicates:
            - Path=/admin-mch/**
        #          filters:
        #            - StripPrefix=1
        # 商城管理后台
        - id: admin
          uri: lb://admin
          predicates:
            - Path=/admin/**,/saas/**
        #  filters:
        #   - StripPrefix=1
        # 供应商
        - id: supplier
          uri: lb://supplier
          predicates:
            - Path=/supplier/**
        #          filters:
        #            - StripPrefix=1
        # 移动端接口
        - id: app
          uri: lb://app
          predicates:
            - Path=/app/**
        #          filters:
        #            - StripPrefix=1
        # 优惠券
        - id: coupon
          uri: lb://coupon
          predicates:
            - Path=/coupon/**
        #          filters:
        #            - StripPrefix=1
        # 首页DIY
        - id: diy
          uri: lb://diy
          predicates:
            - Path=/diy/**
        #          filters:
        #            - StripPrefix=1
        # 资源文件
        - id: file
          uri: lb://file
          predicates:
            - Path=/file/**
        #          filters:
        #            - StripPrefix=1
        # 运费
        - id: freight
          uri: lb://freight
          predicates:
            - Path=/freight/**
        #          filters:
        #            - StripPrefix=1
        # 网关
        - id: gateway
          uri: lb://gateway
          predicates:
            - Path=/gateway/**,/gw/**
        # 发票
        - id: invoice
          uri: lb://invoice
          predicates:
            - Path=/invoice/**
        #          filters:
        #            - StripPrefix=1
        # 移动端店铺接口
        - id: mch
          uri: lb://mch
          predicates:
            - Path=/mch/**
        #          filters:
        #            - StripPrefix=1
        # 会员制
        - id: member
          uri: lb://member
          predicates:
            - Path=/member/**
        #          filters:
        #            - StripPrefix=1
        # 订单
        - id: order
          uri: lb://order
          predicates:
            - Path=/order/**
        #          filters:
        #            - StripPrefix=1
        # 支付
        - id: payment
          uri: lb://payment
          predicates:
            - Path=/payment/**
        #          filters:
        #            - StripPrefix=1
        # 竞拍
        - id: auction
          uri: lb://auction
          predicates:
            - Path=/auction/**
        #          filters:
        #            - StripPrefix=1
        # 分销
        - id: distribute
          uri: lb://distribute
          predicates:
            - Path=/distribute/**
        #          filters:
        #            - StripPrefix=1
        # 拼团
        - id: group
          uri: lb://group
          predicates:
            - Path=/group/**
        #          filters:
        #            - StripPrefix=1
        #
        - id: integral
          uri: lb://integral
          predicates:
            - Path=/integral/**
        #          filters:
        #            - StripPrefix=1
        # 签到
        - id: sign
          uri: lb://sign
          predicates:
            - Path=/sign/**
        #          filters:
        #            - StripPrefix=1
        # 预售
        - id: presell
          uri: lb://presell
          predicates:
            - Path=/presell/**
        #          filters:
        #            - StripPrefix=1
        # 移动端商品
        - id: products
          uri: lb://products
          predicates:
            - Path=/products/**
        #          filters:
        #            - StripPrefix=1
        # 秒杀
        - id: seckill
          uri: lb://seckill
          predicates:
            - Path=/seckill/**
        #          filters:
        #            - StripPrefix=1
        # PC商城
        - id: mall
          uri: lb://mall
          predicates:
            - Path=/mall/**
        #          filters:
        #            - StripPrefix=1
        # 定时任务
        - id: laike-task
          uri: lb://laike-task
          predicates:
            - Path=/task/**
        #          filters:
        #            - StripPrefix=1
        # 用户信息
        - id: user
          uri: lb://user
          predicates:
            - Path=/user/**
          # filters:
          #   - StripPrefix=1
        # seata
        - id: seata-demo
          uri: lb://seata-demo
          predicates:
            - Path=/seata-demo/**
          # filters:
          #   - StripPrefix=1
        - id: flashsale
          uri: lb://flashsale
          predicates:
            - Path=/flashsale/**
          # filters:
          #   - StripPrefix=1

```

## 三、网关启动：

```shell
 # nohup java -jar scgw.jar & 

nohup java -jar -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xms256m -Xmx512m -Xmn128m -Xss256k -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC scgw.jar &

```

## 四、网关nginx 配置

```nginx

    # 旧网关 
    # location /old_gw {
    #     proxy_pass http://localhost:18001/gateway/gw; 
    #     proxy_set_header X-Real-IP $remote_addr;
    #     proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    # }
    
    # 新网关
    location /gw {
        proxy_pass http://localhost:9526/gateway/gw; 
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
```