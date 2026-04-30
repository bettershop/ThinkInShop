<?php
namespace app\admin\controller;

use think\facade\Request;

class Gateway
{
    public function index()
    {
        $api = safe_trim(Request::param('api', ''));
        if ($api === '') {
            return output(109, 'api不能为空');
        }

        if (!preg_match('/^[A-Za-z0-9_\\.]+$/', $api)) {
            return output(109, 'api格式错误');
        }

        $parts = explode('.', $api);
        $count = count($parts);
        if ($count !== 3 && $count !== 4) {
            return output(109, 'api段数错误');
        }

        $method = array_pop($parts);
        if ($method === '' || str_starts_with($method, '__') || str_starts_with($method, '_')) {
            return output(109, 'api方法非法');
        }

        // Fix class name case: the last part (controller) should have first letter capitalized
        $parts[count($parts) - 1] = ucfirst($parts[count($parts) - 1]);
        $class = 'app\\admin\\controller\\' . implode('\\', $parts);
        if (!class_exists($class)) {
            return output(109, '接口不存在');
        }

        if (!method_exists($class, $method)) {
            return output(109, '接口不存在');
        }

        try {
            $ref = new \ReflectionMethod($class, $method);
            if (!$ref->isPublic() || $ref->isConstructor() || $ref->isDestructor()) {
                return output(109, '接口不存在');
            }
        } catch (\ReflectionException) {
            return output(109, '接口不存在');
        }

        try {
            $instance = app()->make($class);
            return $instance->{$method}();
        } catch (\Throwable $e) {
            $file = $e->getFile();
            $line = $e->getLine();
            $location = '';
            if (is_string($file) && $file !== '' && is_int($line) && $line > 0) {
                $location = ' @' . basename($file) . ':' . $line;
            }
            return output(109, '接口执行失败:' . $e->getMessage() . $location);
        }
    }
}
