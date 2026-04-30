<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    $sql_m = "select b.shop_id,a.id from lkt_customer a left join lkt_admin as b on a.admin_id = b.id where a.status = 0 and a.recycle = 0 and a.is_default = 1";
    $res_m = \think\facade\Db::query($sql_m);
    if ($res_m) {
        echo "Default store found: ID=" . $res_m[0]['id'] . ", ShopID=" . $res_m[0]['shop_id'] . "\n";
    } else {
        echo "No default store found in lkt_customer.\n";
        
        // Let's see what's in lkt_customer
        $all = \think\facade\Db::table('lkt_customer')->select();
        echo "Total customers: " . count($all) . "\n";
        foreach ($all as $c) {
            echo "Customer ID=" . $c['id'] . ", Status=" . $c['status'] . ", Recycle=" . $c['recycle'] . ", IsDefault=" . $c['is_default'] . "\n";
        }
    }
} catch (\Throwable $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
