<?php
require 'vendor/autoload.php';
if (!defined('MYSQL_USER')) define('MYSQL_USER', 'root');
if (!defined('MYSQL_PASSWORD')) define('MYSQL_PASSWORD', '123456');
if (!defined('MYSQL_DATABASE')) define('MYSQL_DATABASE', 'lkt_db');
if (!defined('MYSQL_SERVER')) define('MYSQL_SERVER', '127.0.0.1');
if (!defined('MYSQL_PORT')) define('MYSQL_PORT', 3306);
if (!defined('MYSQL_TYPE')) define('MYSQL_TYPE', 'mysql');
if (!defined('MYSQL_CHARSET')) define('MYSQL_CHARSET', 'utf8mb4');
if (!defined('MYSQL_DEBUG')) define('MYSQL_DEBUG', true);

$app = new \think\App();
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
