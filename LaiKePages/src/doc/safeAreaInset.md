# iPhoneX 底部安全区适配

## 使用

在 `public.less` 中编写了如下代码，只需在需要适配的元素上添加 `safe-area-inset-bottom` 类即可。


```css
@supports (bottom: constant(safe-area-inset-bottom)) or (bottom: env(safe-area-inset-bottom)) {
    .safe-area-inset-bottom {
        padding-bottom: constant(safe-area-inset-bottom);
        padding-bottom: env(safe-area-inset-bottom);
    }
}
```




## 参考

[1. 网页适配 iPhoneX，就是这么简单
](https://aotu.io/notes/2017/11/27/iphonex/index.html)
