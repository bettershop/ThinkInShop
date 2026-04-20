# navigator 工具

## 使用

在模板中使用
```html
<view @tap="navTo('/pagesA/distribution/fxProduct')">分销商城</view>
```

在 js 中使用 

```
this.navTo('/pagesA/distribution/fxProduct')
```

## api

```js
// 1. 页面跳转 
navTo('url')

// 2.返回上一页 
navBack()


// 3. 跳转 switchTab
navSwitchTab()

// 4. 返回首页
navToHome()

// 5. 检测登录后跳转 
navTo('url', true, me)
```

