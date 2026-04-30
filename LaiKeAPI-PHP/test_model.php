<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $admin = new \app\admin\model\AdminModel();
    $res = $admin->where('name', 'admin')->find();
    if ($res) {
        echo "AdminModel found user: " . $res->name . "\n";
    } else {
        echo "AdminModel DID NOT find user 'admin'\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
