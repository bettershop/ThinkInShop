<?php

namespace app\middleware;

use think\facade\Lang;

class LoadAdminLangPack
{
    public function handle($request, \Closure $next)
    {
        $language = (string)$request->param('language');
        if ($language !== '') {
            $normalized = strtolower(str_replace('-', '_', $language));
            $path = app()->getAppPath() . 'admin' . DIRECTORY_SEPARATOR . 'lang' . DIRECTORY_SEPARATOR . $normalized . '.php';
            if (is_file($path)) {
                Lang::setLangSet($normalized);
                Lang::load($path);
            }
        }

        return $next($request);
    }
}

