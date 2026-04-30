<?php
// +----------------------------------------------------------------------
// | ThinkPHP [ WE CAN DO IT JUST THINK ]
// +----------------------------------------------------------------------
// | Copyright (c) 2006~2018 http://thinkphp.cn All rights reserved.
// +----------------------------------------------------------------------
// | Licensed ( http://www.apache.org/licenses/LICENSE-2.0 )
// +----------------------------------------------------------------------
// | Author: liu21st <liu21st@gmail.com>
// +----------------------------------------------------------------------
use think\facade\Route;
use think\facade\Request;

Route::get('think', function () {
    return 'hello,ThinkPHP6!';
});

Route::get('hello/:name', 'index/hello');

Route::any('/', function () {
    return app()->make(\app\admin\controller\Gateway::class)->index();
});

Route::any('gw', function () {
    return app()->make(\app\admin\controller\Gateway::class)->index();
});

Route::miss(function () {
    if (Request::param('api')) {
        return app()->make(\app\admin\controller\Gateway::class)->index();
    }
    return output(404, 'Not Found');
});
