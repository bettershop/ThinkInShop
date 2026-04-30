<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $res = \think\facade\Db::table('lkt_admin')->where('name', 'admin')->select();
    foreach ($res as $r) {
        echo "User: " . $r['name'] . " | ID: " . $r['id'] . " | StoreID: " . $r['store_id'] . " | Status: " . $r['status'] . " | Password: " . $r['password'] . "\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
