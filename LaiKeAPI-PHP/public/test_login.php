<?php
// 直接调用登录接口，看看后端到底返回什么
$_POST['api'] = 'admin.saas.user.login';
$_POST['userName'] = 'admin';
$_POST['pwd'] = 'Lkt23';
$_POST['customerNumber'] = '';
$_POST['imgCodeToken'] = '';
$_POST['imgCode'] = '';

require __DIR__ . '/index.php';