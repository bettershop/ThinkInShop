<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $res = \think\facade\Db::query('DESCRIBE lkt_admin');
    foreach ($res as $row) {
        echo $row['Field'] . ' (' . $row['Type'] . ")\n";
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
