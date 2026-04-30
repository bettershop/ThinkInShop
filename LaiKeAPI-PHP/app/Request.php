<?php
namespace app;

// 应用请求对象类
class Request extends \think\Request
{
    public function param($name = '', $default = null, $filter = '')
    {
        $value = parent::param($name, $default, $filter);
        if ($default === null && is_string($name) && $name !== '' && $value === null) {
            return '';
        }
        return $value;
    }
}
