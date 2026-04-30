<?php
require 'vendor/autoload.php';
$app = new \think\App();
$app->initialize();
$_GET['api'] = 'admin.system.GetBasicConfiguration';
$_GET['storeId'] = '123';
$_GET['storeType'] = '1';
$_GET['accessId'] = '1';
$gateway = new \app\admin\controller\Gateway();
try {
    $response = $gateway->index();
    print_r($response->getData());
} catch (\Throwable $e) {
    echo "Fatal Error: " . $e->getMessage() . "\n";
}
