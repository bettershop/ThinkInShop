<!-- TOC -->
  * [来客电商-IM](#来客电商-im)
    * [简介](#简介)
    * [功能](#功能)
    * [本地开发调试](#本地开发调试)
    * [线上部署](#线上部署)
    * [线上IM配置wss](#线上im配置wss)
<!-- TOC -->

## 来客电商-IM

### 简介
- 店铺端店主和普通用户之间进行在线实时沟通的功能，使用的技术是websocket。
- 对应的子模块是:laike-online-message
- 线上需要单独部署

### 功能
    目前IM功能发送图片、表情、链接、文字的功能；

### 本地开发调试
- 直接启动即可。
- 移动端需要配置ws的服务器地址：
移动端配置信息：LaiKeJavaPages/components/laiketuiCommon.vue
    ```vue
    /*
     * websocket 客服配置地址
     */
    const LKT_WEBSOCKET = 'ws://ip:port/message/onlineMessage/'
    ```

### 线上部署
【laike-online-message】功能集成在laike-comps子模块的im文件夹。所以只需要部署并启动 comps.war 包 

### 线上IM配置wss
前提是已经配置好了域名的ssl证书。开放了80，443端口。域名如何配置ssl证书请宝塔直接配置，简单方便。
```markdown
# websocket 配置 

location /wss/  {
    #通过配置端口指向部署websocker的项目
    proxy_pass http://localhost:laike-comps模块yml文件配置的端口/;        
    proxy_http_version 1.1;    
    proxy_set_header Upgrade $http_upgrade;    
    proxy_set_header Connection "Upgrade";    
    proxy_set_header X-real-ip $remote_addr;
    proxy_set_header X-Forwarded-For $remote_addr;
}

```
配置好之后移动端配置wss地址为：
```vue
/*
 * websocket 客服配置地址
 */
 const LKT_WEBSOCKET = 'wss://域名/wss/message/onlineMessage/'
```