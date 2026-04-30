<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $res = \think\facade\Db::table('lkt_user')->where('zhanghao', 'admin')->find();
    if ($res) {
        echo "Found in lkt_user\n";
    } else {
        echo "Not found in lkt_user\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
