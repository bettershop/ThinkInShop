<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $res = \think\facade\Db::table('lkt_admin')->where('name', 'admin')->find();
    if ($res) {
        echo "User: " . $res['name'] . "\n";
        echo "Password Hash: " . $res['password'] . "\n";
        echo "Status: " . $res['status'] . "\n";
        echo "Login Num: " . $res['login_num'] . "\n";
        echo "Recycle: " . $res['recycle'] . "\n";
        echo "Store ID: " . $res['store_id'] . "\n";
        echo "Admin Type: " . $res['admin_type'] . "\n";
        echo "Type: " . $res['type'] . "\n";
        echo "Shop ID: " . $res['shop_id'] . "\n";
        echo "Role: " . $res['role'] . "\n";
        
        $expected = md5('000000');
        if ($res['password'] === $expected) {
            echo "Password matches MD5('000000')\n";
        } else {
            echo "Password DOES NOT match MD5('000000')\n";
            echo "Expected: " . $expected . "\n";
        }
    } else {
        echo "User 'admin' not found.\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
