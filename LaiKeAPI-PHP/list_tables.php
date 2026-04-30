<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $res = \think\facade\Db::query('SHOW TABLES');
    echo "Tables:\n";
    foreach ($res as $row) {
        echo current($row) . "\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
