<?php
require 'vendor/autoload.php';
$api = 'admin.system.GetBasicConfiguration';
$parts = explode('.', $api);
$method = array_pop($parts);
$class = 'app\\admin\\controller\\' . implode('\\', $parts);

echo "Class to check: $class\n";
if (!class_exists($class)) {
    echo "class_exists failed: " . $class . "\n";
    exit;
}
echo "Method to check: $method\n";
if (!method_exists($class, $method)) {
    echo "method_exists failed: " . $method . "\n";
    exit;
}

try {
    $ref = new ReflectionMethod($class, $method);
    if (!$ref->isPublic() || $ref->isConstructor() || $ref->isDestructor()) {
        echo "isPublic failed\n";
        exit;
    }
} catch (ReflectionException $e) {
    echo "ReflectionException\n";
    exit;
}
echo "Success!\n";
