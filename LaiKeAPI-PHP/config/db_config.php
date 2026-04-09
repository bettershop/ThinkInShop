<?php

if (!function_exists('dbConfigEnv')) {
    function dbConfigEnv($name, $default)
    {
        $value = getenv($name);
        return ($value === false || $value === '') ? $default : $value;
    }
}

if (!defined('MYSQL_TYPE')) {
    define('MYSQL_TYPE', dbConfigEnv('MYSQL_TYPE', 'mysql'));
}
if (!defined('MYSQL_SERVER')) {
    define('MYSQL_SERVER', dbConfigEnv('MYSQL_SERVER', '127.0.0.1'));
}
if (!defined('MYSQL_USER')) {
    define('MYSQL_USER', dbConfigEnv('MYSQL_USER', 'root'));
}
if (!defined('MYSQL_PASSWORD')) {
    define('MYSQL_PASSWORD', dbConfigEnv('MYSQL_PASSWORD', '123456'));
}
if (!defined('MYSQL_DATABASE')) {
    define('MYSQL_DATABASE', dbConfigEnv('MYSQL_DATABASE', 'lkt_db'));
}
if (!defined('MYSQL_PORT')) {
    define('MYSQL_PORT', (int)dbConfigEnv('MYSQL_PORT', 3306));
}
if (!defined('MYSQL_CHARSET')) {
    define('MYSQL_CHARSET', dbConfigEnv('MYSQL_CHARSET', 'utf8mb4'));
}
if (!defined('MYSQL_DEBUG')) {
    $debugRaw = strtolower((string)dbConfigEnv('MYSQL_DEBUG', 'true'));
    define('MYSQL_DEBUG', in_array($debugRaw, ['1', 'true', 'yes', 'on'], true));
}
