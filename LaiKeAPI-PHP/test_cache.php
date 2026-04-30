<?php
namespace think;
require __DIR__ . '/vendor/autoload.php';
$app = new App();
$app->initialize();
try {
    echo "Testing cache...\n";
    cache('test_key', 'test_value', 10);
    $val = cache('test_key');
    echo "Cache value: " . $val . "\n";
} catch (\Throwable $e) {
    echo "Cache Error: " . $e->getMessage() . "\n";
    echo "Trace: " . $e->getTraceAsString() . "\n";
}
