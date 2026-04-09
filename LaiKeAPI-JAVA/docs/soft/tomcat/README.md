## 说明

1. 目前本项目使用的是war部署，tomcat版本也只适配了 给定的这个9.0.55 版本。
2. server.xml的端口必须要和thinkinshop中的打包环境对应的配置yml文件中的端口一致
3. {TOMCAT_HOME}/conf/catalina.properties 文件用soft目录下的这个配置文件替换 

### 关于端口的说明

```xml
<!-- 端口18001 必须要和laike-xxx 模块resources 文件目录中对应的yml 文件中配置的 server.port 端口一致 -->
<Connector port="18001" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
```

开发环境配置文件：application-dev.yml
```yaml
server:
  port: ${tomcat_port:18003}
```
本地环境配置文件：application-local.yml
```yaml
server:
  port: ${tomcat_port:18003}
```
生产环境配置文件：application-prod.yml
```yaml
server:
  port: ${tomcat_port:18001}
```

