<?php

if (!function_exists('dbConfigEnv')) {
    function dbConfigEnv($name, $default)
    {
        $value = getenv($name);
        if ($value !== false && $value !== '') {
            return $value;
        }

        if (isset($_ENV[$name]) && $_ENV[$name] !== '') {
            return $_ENV[$name];
        }

        if (isset($_SERVER[$name]) && $_SERVER[$name] !== '') {
            return $_SERVER[$name];
        }

        $alias = [
            'MYSQL_SERVER' => 'HOSTNAME',
            'MYSQL_USER' => 'USERNAME',
            'MYSQL_PASSWORD' => 'PASSWORD',
            'MYSQL_DATABASE' => 'DATABASE',
            'MYSQL_PORT' => 'HOSTPORT',
        ];
        if (isset($alias[$name])) {
            $aliasKey = $alias[$name];
            $aliasVal = getenv($aliasKey);
            if ($aliasVal !== false && $aliasVal !== '') {
                return $aliasVal;
            }
            if (isset($_ENV[$aliasKey]) && $_ENV[$aliasKey] !== '') {
                return $_ENV[$aliasKey];
            }
            if (isset($_SERVER[$aliasKey]) && $_SERVER[$aliasKey] !== '') {
                return $_SERVER[$aliasKey];
            }
        }

        $envFile = dirname(__DIR__) . '/.env';
        if (is_file($envFile)) {
            $lines = @file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
            if (is_array($lines)) {
                $envMap = [];
                foreach ($lines as $line) {
                    $line = trim($line);
                    if ($line === '' || strpos($line, '#') === 0) {
                        continue;
                    }
                    $pos = strpos($line, '=');
                    if ($pos === false) {
                        continue;
                    }
                    $k = trim(substr($line, 0, $pos));
                    $v = trim(substr($line, $pos + 1));
                    $v = trim($v, " \t\n\r\0\x0B\"'");
                    if ($k !== '') {
                        $envMap[$k] = $v;
                    }
                }

                if (isset($envMap[$name]) && $envMap[$name] !== '') {
                    return $envMap[$name];
                }
                if (isset($alias[$name])) {
                    $aliasKey = $alias[$name];
                    if (isset($envMap[$aliasKey]) && $envMap[$aliasKey] !== '') {
                        return $envMap[$aliasKey];
                    }
                }
            }
        }

        return $default;
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
