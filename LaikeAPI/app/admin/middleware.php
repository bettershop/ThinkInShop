<?php
// 这是系统自动生成的middleware定义文件
return [
	// Session初始化
    \think\middleware\SessionInit::class,
    // 多语言加载
    \think\middleware\LoadLangPack::class,
    //跨域请求支持
    \think\middleware\AllowCrossDomain::class,
];
