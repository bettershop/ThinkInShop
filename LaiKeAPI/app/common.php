<?php
// 应用公共文件
use think\facade\Db;

function output($code = 200,$message = '',$data = '')
{   
    $result = ['code' => $code,'message' => $message,'data' => $data];
    return json($result,200); 
}


if (!function_exists('langCode2Name')) {
    /**
     * 根据 lang_code 查询 lkt_lang 表，返回 lang_name
     * @param string $code 语言代码，如 'zh_CN'
     * @param string $default 默认返回值（当未找到时）
     * @return string
     */
    function langCode2Name(string $code, string $default = '中文简体'): string
    {
        if (empty($code)) {
            return $default;
        }
        // 直接查询 lkt_lang 表
        $name = Db::name('lang')
            ->where('lang_code', $code)
            ->value('lang_name');

        return $name ?: $default;
    }
}

if (!function_exists('is_china_calling_code')) {
    /**
     * 判断国际电话区号是否属于中国（含港澳台）
     * @param string|int $code 区号，如 '86', '+852', 886
     * @return bool
     */
    function is_china_calling_code($code): bool
    {
        $code = ltrim((string)$code, '+');
        return in_array($code, ['86', '852', '886', '853'], true);
    }
}


